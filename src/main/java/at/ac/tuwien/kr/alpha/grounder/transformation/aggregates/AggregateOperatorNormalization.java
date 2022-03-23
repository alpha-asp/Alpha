package at.ac.tuwien.kr.alpha.grounder.transformation.aggregates;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import at.ac.tuwien.kr.alpha.common.ComparisonOperator;
import at.ac.tuwien.kr.alpha.common.atoms.AggregateAtom;
import at.ac.tuwien.kr.alpha.common.atoms.AggregateAtom.AggregateFunctionSymbol;
import at.ac.tuwien.kr.alpha.common.atoms.AggregateLiteral;
import at.ac.tuwien.kr.alpha.common.atoms.ComparisonAtom;
import at.ac.tuwien.kr.alpha.common.atoms.Literal;
import at.ac.tuwien.kr.alpha.common.rule.BasicRule;
import at.ac.tuwien.kr.alpha.common.terms.ArithmeticTerm;
import at.ac.tuwien.kr.alpha.common.terms.ArithmeticTerm.ArithmeticOperator;
import at.ac.tuwien.kr.alpha.common.terms.ConstantTerm;
import at.ac.tuwien.kr.alpha.common.terms.Term;
import at.ac.tuwien.kr.alpha.common.terms.VariableTerm;

/**
 * Transforms an {@link at.ac.tuwien.kr.alpha.common.program.InputProgram} such that, for all aggregate (body-)literals, only the comparison operators "="
 * and "<=" are used.
 *
 * Rewriting of "#count" and "#sum" aggregates is done using the following equivalences:
 * <ul>
 * <li><code>X < #aggr{...}</code> == <code>XP <= #aggr{...}, XP = X + 1</code></li>
 * <li><code>X != #aggr{...}</code> == <code>not X = #aggr{...}</code></li>
 * <li><code>X > #aggr{...}</code> == <code>not X <= #aggr{...}</code></li>
 * <li><code>X >= #aggr{...}</code> == <code>not XP <= #aggr{...}, XP = X + 1</code></li>
 * <li><code>not X < #aggr{...}</code> == <code>not XP <= #aggr{...}, XP = X + 1</code></li>
 * <li><code>not X != #aggr{...}</code> == <code>X = #aggr{...}</code></li>
 * <li><code>not X > #aggr{...}</code> == <code>X <= #aggr{...}</code></li>
 * <li><code>not X >= #aggr{...}</code> == <code>XP <= #aggr{...}, XP = X + 1</code></li>
 * </ul>
 * Operators for "#min" and "#max" aggregates are not rewritten.
 *
 * Note that input programs must only contain aggregate literals of form <code>TERM OP #aggr{...}</code> or <code>#aggr{...} OP TERM</code>,
 * i.e. with only
 * a left or right term and operator (but not both). When preprocessing programs, apply this transformation AFTER
 * {@link at.ac.tuwien.kr.alpha.grounder.transformation.aggregates.AggregateLiteralSplitting}.
 *
 * Copyright (c) 2020-2021, the Alpha Team.
 */
public final class AggregateOperatorNormalization {

	private AggregateOperatorNormalization() {
		throw new UnsupportedOperationException("Utility class - cannot instantiate!");
	}

	public static BasicRule normalize(BasicRule rule) {
		List<Literal> rewrittenBody = new ArrayList<>();
		for (Literal lit : rule.getBody()) {
			rewrittenBody.addAll(rewriteLiteral(lit));
		}
		return new BasicRule(rule.getHead(), rewrittenBody);
	}

	private static List<Literal> rewriteLiteral(Literal lit) {
		if (lit instanceof AggregateLiteral) {
			return rewriteAggregateOperator((AggregateLiteral) lit);
		} else {
			return Collections.singletonList(lit);
		}
	}

	private static List<Literal> rewriteAggregateOperator(AggregateLiteral lit) {
		AggregateAtom atom = lit.getAtom();
		if (atom.getLowerBoundOperator() == null && atom.getUpperBoundOperator() != null) {
			return rewriteAggregateOperator(convertToLeftHandComparison(lit));
		}
		if (lit.getAtom().getAggregatefunction() == AggregateFunctionSymbol.MIN || lit.getAtom().getAggregatefunction() == AggregateFunctionSymbol.MAX) {
			// No operator normalization needed for #min/#max aggregates.
			return Collections.singletonList(lit);
		}
		if (atom.getLowerBoundOperator().equals(ComparisonOperator.EQ) || atom.getLowerBoundOperator().equals(ComparisonOperator.LE)) {
			// Nothing to do for operator "=" or "<=".
			return Collections.singletonList(lit);
		} else {
			List<Literal> retVal = new ArrayList<>();
			VariableTerm decrementedBound;
			ComparisonOperator lowerBoundOp = atom.getLowerBoundOperator();
			if (lowerBoundOp.equals(ComparisonOperator.LT)) {
				decrementedBound = VariableTerm.getAnonymousInstance();
				retVal.add(createLowerBoundedAggregateLiteral(ComparisonOperator.LE, decrementedBound, atom, !lit.isNegated()));
				retVal.add(createPlusOneTerm(atom.getLowerBoundTerm(), decrementedBound));
			} else if (lowerBoundOp.equals(ComparisonOperator.NE)) {
				retVal.add(createLowerBoundedAggregateLiteral(ComparisonOperator.EQ, atom.getLowerBoundTerm(), atom, lit.isNegated()));
			} else if (lowerBoundOp.equals(ComparisonOperator.GT)) {
				retVal.add(createLowerBoundedAggregateLiteral(ComparisonOperator.LE, atom.getLowerBoundTerm(), atom, lit.isNegated()));
			} else if (lowerBoundOp.equals(ComparisonOperator.GE)) {
				decrementedBound = VariableTerm.getAnonymousInstance();
				retVal.add(createLowerBoundedAggregateLiteral(ComparisonOperator.LE, decrementedBound, atom, lit.isNegated()));
				retVal.add(createPlusOneTerm(atom.getLowerBoundTerm(), decrementedBound));
			} else {
				throw new IllegalStateException("No operator rewriting logic available for literal: " + lit);
			}
			return retVal;
		}
	}

	private static AggregateLiteral createLowerBoundedAggregateLiteral(ComparisonOperator op, Term lowerBoundTerm, AggregateAtom aggregateAtom, boolean isNegated) {
		return new AggregateLiteral(new AggregateAtom(op, lowerBoundTerm, aggregateAtom.getAggregatefunction(),
			aggregateAtom.getAggregateElements()), isNegated);
	}

	/**
	 * Creates a new {@link Literal} that assigns the given target variable to the given (integer) term plus one.
	 *
	 * @param term
	 * @param targetVariable
	 * @return
	 */
	private static Literal createPlusOneTerm(Term term, VariableTerm targetVariable) {
		Term increment = ArithmeticTerm.getInstance(term, ArithmeticOperator.PLUS, ConstantTerm.getInstance(1));
		ComparisonAtom atom = new ComparisonAtom(targetVariable, increment, ComparisonOperator.EQ);
		return atom.toLiteral();
	}

	/**
	 * Helper function to convert aggregate literals of form <code>#aggr{...} OP TERM</code> to literals of form
	 * <code>TERM OP #aggr{...}</code>.
	 *
	 * @param lit an aggregate literal with only a right-hand term comparison
	 * @return a semantically equivalent literal with only a left-hand comparison
	 */
	private static AggregateLiteral convertToLeftHandComparison(AggregateLiteral lit) {
		AggregateAtom atom = lit.getAtom();
		ComparisonOperator operator = atom.getUpperBoundOperator();
		ComparisonOperator flippedOperator;
		if (operator.equals(ComparisonOperator.EQ) || operator.equals(ComparisonOperator.NE)) {
			flippedOperator = operator;
		} else if (operator.equals(ComparisonOperator.LE)) {
			flippedOperator = ComparisonOperator.GE;
		} else if (operator.equals(ComparisonOperator.LT)) {
			flippedOperator = ComparisonOperator.GT;
		} else if (operator.equals(ComparisonOperator.GE)) {
			flippedOperator = ComparisonOperator.LE;
		} else if (operator.equals(ComparisonOperator.GT)) {
			flippedOperator = ComparisonOperator.LT;
		} else {
			throw new IllegalArgumentException("Unsupported comparison operator for aggregate atom: " + operator);
		}
		return new AggregateAtom(flippedOperator, atom.getUpperBoundTerm(), atom.getAggregatefunction(), atom.getAggregateElements())
				.toLiteral(!lit.isNegated());
	}

}
