package at.ac.tuwien.kr.alpha.grounder.transformation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import at.ac.tuwien.kr.alpha.common.ComparisonOperator;
import at.ac.tuwien.kr.alpha.common.Predicate;
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
import at.ac.tuwien.kr.alpha.common.terms.Terms;
import at.ac.tuwien.kr.alpha.common.terms.VariableTerm;
import at.ac.tuwien.kr.alpha.grounder.parser.InlineDirectives;

public class BindingAggregateTransformation extends AbstractAggregateTransformation {

	private static final Predicate AGGREGATE = Predicate.getInstance("_aggregate", 1);
	private static final Predicate LEQ_VALUE = Predicate.getInstance("_leq_value", 2);

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
			aggregateEncodingRules.addAll(encodeAggregateFunction(func, aggregateFunctionsToEncode.get(func)));
		}
		return new InputProgram(aggregateEncodingRules, aggregateFacts, new InlineDirectives());
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

	private List<BasicRule> encodeAggregateFunction(AggregateFunctionSymbol func, List<AggregateLiteral> literals) {
		switch (func) {
			case COUNT:
				return encodeCountAggregate(literals);
			case SUM:
				throw new UnsupportedOperationException();
			default:
				throw new UnsupportedOperationException();
		}
	}

	private List<BasicRule> encodeCountAggregate(List<AggregateLiteral> literals) {

	}

}
