package at.ac.tuwien.kr.alpha.grounder.transformation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import at.ac.tuwien.kr.alpha.common.ComparisonOperator;
import at.ac.tuwien.kr.alpha.common.Predicate;
import at.ac.tuwien.kr.alpha.common.atoms.AggregateAtom;
import at.ac.tuwien.kr.alpha.common.atoms.AggregateAtom.AggregateElement;
import at.ac.tuwien.kr.alpha.common.atoms.AggregateAtom.AggregateFunctionSymbol;
import at.ac.tuwien.kr.alpha.common.atoms.AggregateLiteral;
import at.ac.tuwien.kr.alpha.common.atoms.Atom;
import at.ac.tuwien.kr.alpha.common.atoms.BasicAtom;
import at.ac.tuwien.kr.alpha.common.atoms.BasicLiteral;
import at.ac.tuwien.kr.alpha.common.atoms.Literal;
import at.ac.tuwien.kr.alpha.common.program.InputProgram;
import at.ac.tuwien.kr.alpha.common.rule.BasicRule;
import at.ac.tuwien.kr.alpha.common.rule.head.NormalHead;
import at.ac.tuwien.kr.alpha.common.terms.ConstantTerm;
import at.ac.tuwien.kr.alpha.common.terms.FunctionTerm;
import at.ac.tuwien.kr.alpha.common.terms.Term;
import at.ac.tuwien.kr.alpha.common.terms.Terms;
import at.ac.tuwien.kr.alpha.common.terms.VariableTerm;
import at.ac.tuwien.kr.alpha.grounder.parser.InlineDirectives;
import at.ac.tuwien.kr.alpha.grounder.parser.InlineDirectives.DIRECTIVE;
import at.ac.tuwien.kr.alpha.grounder.parser.ProgramParser;

public class BindingAggregateTransformation extends AbstractAggregateTransformation {

	private static final Predicate AGGREGATE = Predicate.getInstance("aggregate", 1);
	private static final Predicate LEQ_AGGREGATE = Predicate.getInstance("leq_aggregate", 2);

	private static final Predicate CNT_CANDIDATE = Predicate.getInstance("cnt_candidate", 2);
	private static final Predicate SUM_CANDIDATE = Predicate.getInstance("sum_candidate", 2);
	private static final Predicate CNT_ELEMENT_TUPLE = Predicate.getInstance("cnt_element_tuple", 2);
	private static final Predicate SUM_ELEMENT_TUPLE = Predicate.getInstance("sum_element_tuple", 3);
	private static final Predicate ELEMENT_TUPLE_ORDINAL = Predicate.getInstance("element_tuple_ordinal", 3);

	private static final String ELEMENT_TUPLE_FN_SYM = "tuple";

	private static final String CNT_CANDIDATE_RULE = String.format(
			"%s(AGGREGATE_ID, I) :- %s(AGGREGATE_ID), %s(AGGREGATE_ID, TUPLE), %s(AGGREGATE_ID, TUPLE, I).",
			CNT_CANDIDATE.getName(), AGGREGATE.getName(), CNT_ELEMENT_TUPLE.getName(), ELEMENT_TUPLE_ORDINAL.getName());

	//@formatter:off
	// TODO can we get around hardcoded predicate names here??
	private static final String SUM_CANDIDATE_PROG = 
			"sum_element_at_index(SUM_ID, VAL, IDX) :- aggregate(SUM_ID), sum_element_tuple(SUM_ID, TPL, VAL), element_tuple_ordinal(SUM_ID, TPL, IDX)."
			+ "sum_element_index(SUM_ID, IDX) :- sum_element_at_index(SUM_ID, _, IDX)."
			// In case all elements are false, 0 is a candidate sum
			+ "sum_at_idx_candidate(SUM_ID, 0, 0) :- sum_element_at_index(SUM_ID, _, _)."
			// Assuming the element with index I is false, all candidate sums up to the last index are also valid candidates for
			// this index
			+ "sum_at_idx_candidate(SUM_ID, I, CSUM) :- sum_element_index(SUM_ID, I), sum_at_idx_candidate(SUM_ID, IPREV, CSUM), IPREV = I - 1."
			// Assuming the element with index I is true, all candidate sums up to the last index plus the value of the element at
			// index I are candidate sums."
			+ "sum_at_idx_candidate(SUM_ID, I, CSUM) :- sum_element_at_index(SUM_ID, VAL, I), sum_at_idx_candidate(SUM_ID, IPREV, PSUM), IPREV = I - 1, CSUM = PSUM + VAL."
			// Project indices away, we only need candidate values for variable binding.
			+ "sum_candidate(SUM_ID, CSUM) :- sum_at_idx_candidate(SUM_ID, _, CSUM).";
	//@formatter:on

	private final ProgramParser parser = new ProgramParser();

	@Override
	protected boolean shouldHandle(AggregateLiteral lit) {
		return lit.getAtom().getLowerBoundOperator() == ComparisonOperator.EQ;
	}

	@Override
	protected InputProgram encodeAggregates(AggregateRewritingContext ctx) {
		// create aggregate facts
		List<Atom> aggregateFacts = new ArrayList<>();
		Map<AggregateFunctionSymbol, List<AggregateLiteral>> aggregateFunctionsToEncode = new HashMap<>();
		for (AggregateLiteral lit : ctx.getLiteralsToRewrite()) {
			aggregateFacts.add(new BasicAtom(AGGREGATE, ConstantTerm.getSymbolicInstance(ctx.getAggregateId(lit))));
			aggregateFunctionsToEncode.putIfAbsent(lit.getAtom().getAggregatefunction(), new ArrayList<>());
			aggregateFunctionsToEncode.get(lit.getAtom().getAggregatefunction()).add(lit);
		}
		List<BasicRule> aggregateEncodingRules = new ArrayList<>();
		aggregateEncodingRules.add(buildEqualityRule());
		for (AggregateFunctionSymbol func : aggregateFunctionsToEncode.keySet()) {
			aggregateEncodingRules.addAll(encodeAggregateFunction(func, aggregateFunctionsToEncode.get(func), ctx));
		}
		InlineDirectives directives = new InlineDirectives();
		directives.addDirective(DIRECTIVE.enum_predicate_is, ELEMENT_TUPLE_ORDINAL.getName());
		InputProgram aggregateEncoding = new InputProgram(aggregateEncodingRules, aggregateFacts, directives);
		return aggregateEncoding;
	}

	private BasicRule buildEqualityRule() {
		VariableTerm aggregateIdVar = VariableTerm.getInstance("AGGREGATE_ID");
		VariableTerm valueVar = VariableTerm.getInstance("VAL");
		VariableTerm nextValVar = VariableTerm.getInstance("NEXTVAL");
		Atom headAtom = new BasicAtom(AbstractAggregateTransformation.AGGREGATE_RESULT, aggregateIdVar, valueVar);
		Literal leqValue = new BasicLiteral(new BasicAtom(LEQ_AGGREGATE, aggregateIdVar, valueVar), true);
		Literal notLeqNextval = new BasicLiteral(new BasicAtom(LEQ_AGGREGATE, aggregateIdVar, nextValVar), false);
		Literal bindNextval = Terms.incrementTerm(valueVar, nextValVar);
		Literal bindAggregateId = new BasicLiteral(new BasicAtom(AGGREGATE, aggregateIdVar), true);
		return BasicRule.getInstance(new NormalHead(headAtom), leqValue, notLeqNextval, bindNextval, bindAggregateId);
	}

	private BasicRule buildCountElementTupleRule(String aggregateId, AggregateElement element) {
		FunctionTerm elementTuple = FunctionTerm.getInstance(ELEMENT_TUPLE_FN_SYM, element.getElementTerms());
		Atom headAtom = new BasicAtom(CNT_ELEMENT_TUPLE, ConstantTerm.getSymbolicInstance(aggregateId), elementTuple);
		return new BasicRule(new NormalHead(headAtom), element.getElementLiterals());
	}

	private BasicRule buildSumElementTupleRule(String aggregateId, AggregateElement element) {
		FunctionTerm elementTuple = FunctionTerm.getInstance(ELEMENT_TUPLE_FN_SYM, element.getElementTerms());
		Term sumTerm = element.getElementTerms().get(0);
		Atom headAtom = new BasicAtom(SUM_ELEMENT_TUPLE, ConstantTerm.getSymbolicInstance(aggregateId), elementTuple, sumTerm);
		return new BasicRule(new NormalHead(headAtom), element.getElementLiterals());
	}

	private BasicRule buildCntLeqRule(AggregateLiteral countEq, String countEqId) {
		VariableTerm cnt = VariableTerm.getInstance("CNT");
		Atom headAtom = new BasicAtom(LEQ_AGGREGATE, ConstantTerm.getSymbolicInstance(countEqId), cnt);
		AggregateAtom sourceAggregate = countEq.getAtom();
		Literal valueLeqCnt = new AggregateLiteral(
				new AggregateAtom(ComparisonOperator.LE, cnt, null, null, AggregateFunctionSymbol.COUNT, sourceAggregate.getAggregateElements()), true);
		Literal valueIsCandidate = new BasicLiteral(new BasicAtom(CNT_CANDIDATE, ConstantTerm.getSymbolicInstance(countEqId), cnt), true);
		return BasicRule.getInstance(new NormalHead(headAtom), valueLeqCnt, valueIsCandidate);
	}

	private BasicRule buildSumLeqRule(AggregateLiteral sumEq, String sumEqId) {
		VariableTerm sum = VariableTerm.getInstance("SUM");
		Atom headAtom = new BasicAtom(LEQ_AGGREGATE, ConstantTerm.getSymbolicInstance(sumEqId), sum);
		AggregateAtom sourceAggregate = sumEq.getAtom();
		Literal valueLeqSum = new AggregateLiteral(
				new AggregateAtom(ComparisonOperator.LE, sum, null, null, AggregateFunctionSymbol.SUM, sourceAggregate.getAggregateElements()), true);
		Literal valueIsCandidate = new BasicLiteral(new BasicAtom(SUM_CANDIDATE, ConstantTerm.getSymbolicInstance(sumEqId), sum), true);
		return BasicRule.getInstance(new NormalHead(headAtom), valueLeqSum, valueIsCandidate);
	}

	private List<BasicRule> encodeAggregateFunction(AggregateFunctionSymbol func, List<AggregateLiteral> literals, AggregateRewritingContext ctx) {
		switch (func) {
			case COUNT:
				return encodeCountAggregate(literals, ctx);
			case SUM:
				return encodeSumAggregate(literals, ctx);
			default:
				throw new UnsupportedOperationException();
		}
	}

	private List<BasicRule> encodeCountAggregate(List<AggregateLiteral> literals, AggregateRewritingContext ctx) {
		List<BasicRule> retVal = new ArrayList<>();
		for (AggregateLiteral countAggregate : literals) {
			for (AggregateElement element : countAggregate.getAtom().getAggregateElements()) {
				retVal.add(buildCountElementTupleRule(ctx.getAggregateId(countAggregate), element));
			}
			retVal.add(buildCntLeqRule(countAggregate, ctx.getAggregateId(countAggregate)));
		}
		InputProgram candidateRulePrg = parser.parse(CNT_CANDIDATE_RULE);
		retVal.add(candidateRulePrg.getRules().get(0));
		return retVal;
	}

	private List<BasicRule> encodeSumAggregate(List<AggregateLiteral> literals, AggregateRewritingContext ctx) {
		List<BasicRule> retVal = new ArrayList<>();
		for (AggregateLiteral sumAggregate : literals) {
			for (AggregateElement element : sumAggregate.getAtom().getAggregateElements()) {
				retVal.add(buildSumElementTupleRule(ctx.getAggregateId(sumAggregate), element));
			}
			retVal.add(buildSumLeqRule(sumAggregate, ctx.getAggregateId(sumAggregate)));
		}
		InputProgram candidateGenerationPrg = parser.parse(SUM_CANDIDATE_PROG);
		retVal.addAll(candidateGenerationPrg.getRules());
		return retVal;
	}

}
