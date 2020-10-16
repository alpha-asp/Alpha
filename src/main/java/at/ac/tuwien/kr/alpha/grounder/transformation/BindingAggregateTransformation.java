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
import at.ac.tuwien.kr.alpha.common.terms.Terms;
import at.ac.tuwien.kr.alpha.common.terms.VariableTerm;
import at.ac.tuwien.kr.alpha.grounder.parser.InlineDirectives;
import at.ac.tuwien.kr.alpha.grounder.parser.InlineDirectives.DIRECTIVE;
import at.ac.tuwien.kr.alpha.grounder.parser.ProgramParser;

public class BindingAggregateTransformation extends AbstractAggregateTransformation {

	private static final Predicate AGGREGATE = Predicate.getInstance("aggregate", 1);
	private static final Predicate LEQ_VALUE = Predicate.getInstance("leq_value", 2);

	private static final Predicate CNT_CANDIDATE = Predicate.getInstance("cnt_candidate", 2);
	private static final Predicate ELEMENT_TUPLE = Predicate.getInstance("aggregate_element_tuple", 2);
	private static final Predicate ELEMENT_TUPLE_ORDINAL = Predicate.getInstance("element_tuple_ordinal", 3);

	private static final String ELEMENT_TUPLE_FN_SYM = "tuple";

	// TODO add enum directive
	private static final String CNT_CANDIDATE_RULE = String.format(
			"%s(AGGREGATE_ID, I) :- %s(AGGREGATE_ID), %s(AGGREGATE_ID, TUPLE), %s(AGGREGATE_ID, TUPLE, I).",
			CNT_CANDIDATE.getName(), AGGREGATE.getName(), ELEMENT_TUPLE.getName(), ELEMENT_TUPLE_ORDINAL.getName());

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
		// TODO remove this, for development testing only!
		System.out.println(aggregateEncoding);
		return aggregateEncoding;
	}

	private BasicRule buildEqualityRule() {
		VariableTerm aggregateIdVar = VariableTerm.getInstance("AGGREGATE_ID");
		VariableTerm valueVar = VariableTerm.getInstance("VAL");
		VariableTerm nextValVar = VariableTerm.getInstance("NEXTVAL");
		Atom headAtom = new BasicAtom(AbstractAggregateTransformation.AGGREGATE_RESULT, aggregateIdVar, valueVar);
		Literal leqValue = new BasicLiteral(new BasicAtom(LEQ_VALUE, aggregateIdVar, valueVar), true);
		Literal notLeqNextval = new BasicLiteral(new BasicAtom(LEQ_VALUE, aggregateIdVar, nextValVar), false);
		Literal bindNextval = Terms.incrementTerm(valueVar, nextValVar);
		Literal bindAggregateId = new BasicLiteral(new BasicAtom(AGGREGATE, aggregateIdVar), true);
		return BasicRule.getInstance(new NormalHead(headAtom), leqValue, notLeqNextval, bindNextval, bindAggregateId);
	}

	private BasicRule buildElementTupleRule(String aggregateId, AggregateElement element) {
		FunctionTerm elementTuple = FunctionTerm.getInstance(ELEMENT_TUPLE_FN_SYM, element.getElementTerms());
		Atom headAtom = new BasicAtom(ELEMENT_TUPLE, ConstantTerm.getSymbolicInstance(aggregateId), elementTuple);
		return new BasicRule(new NormalHead(headAtom), element.getElementLiterals());
	}

	private BasicRule buildCntLeqRule(AggregateLiteral countEq, String countEqId) {
		VariableTerm cnt = VariableTerm.getInstance("CNT");
		Atom headAtom = new BasicAtom(LEQ_VALUE, ConstantTerm.getSymbolicInstance(countEqId), cnt);
		AggregateAtom sourceAggregate = countEq.getAtom();
		Literal valueLeqCnt = new AggregateLiteral(
				new AggregateAtom(ComparisonOperator.LE, cnt, null, null, AggregateFunctionSymbol.COUNT, sourceAggregate.getAggregateElements()), true);
		Literal valueIsCandidate = new BasicLiteral(new BasicAtom(CNT_CANDIDATE, ConstantTerm.getSymbolicInstance(countEqId), cnt), true);
		return BasicRule.getInstance(new NormalHead(headAtom), valueLeqCnt, valueIsCandidate);
	}

	private List<BasicRule> encodeAggregateFunction(AggregateFunctionSymbol func, List<AggregateLiteral> literals, AggregateRewritingContext ctx) {
		switch (func) {
			case COUNT:
				return encodeCountAggregate(literals, ctx);
			case SUM:
				throw new UnsupportedOperationException();
			default:
				throw new UnsupportedOperationException();
		}
	}

	private List<BasicRule> encodeCountAggregate(List<AggregateLiteral> literals, AggregateRewritingContext ctx) {
		List<BasicRule> retVal = new ArrayList<>();
		for (AggregateLiteral countAggregate : literals) {
			for (AggregateElement element : countAggregate.getAtom().getAggregateElements()) {
				retVal.add(buildElementTupleRule(ctx.getAggregateId(countAggregate), element));
			}
			retVal.add(buildCntLeqRule(countAggregate, ctx.getAggregateId(countAggregate)));
		}
		InputProgram candidateRulePrg = parser.parse(CNT_CANDIDATE_RULE);
		retVal.add(candidateRulePrg.getRules().get(0));
		return retVal;
	}

}
