package at.ac.tuwien.kr.alpha.core.programs.transformation.aggregates;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import at.ac.tuwien.kr.alpha.api.ComparisonOperator;
import at.ac.tuwien.kr.alpha.api.programs.atoms.AggregateAtom;
import at.ac.tuwien.kr.alpha.api.programs.atoms.AggregateAtom.AggregateFunctionSymbol;
import at.ac.tuwien.kr.alpha.api.programs.atoms.ComparisonAtom;
import at.ac.tuwien.kr.alpha.api.programs.literals.AggregateLiteral;
import at.ac.tuwien.kr.alpha.api.programs.literals.Literal;
import at.ac.tuwien.kr.alpha.api.programs.rules.Rule;
import at.ac.tuwien.kr.alpha.api.programs.rules.heads.Head;
import at.ac.tuwien.kr.alpha.api.programs.terms.ArithmeticOperator;
import at.ac.tuwien.kr.alpha.api.programs.terms.Term;
import at.ac.tuwien.kr.alpha.api.programs.terms.VariableTerm;
import at.ac.tuwien.kr.alpha.commons.comparisons.ComparisonOperators;
import at.ac.tuwien.kr.alpha.commons.programs.atoms.Atoms;
import at.ac.tuwien.kr.alpha.commons.programs.literals.Literals;
import at.ac.tuwien.kr.alpha.commons.programs.rules.Rules;
import at.ac.tuwien.kr.alpha.commons.programs.terms.Terms;

/**
 * Transforms an {@link AspCore2ProgramImpl} such that, for all aggregate (body-)literals, only the comparison operators "="
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

	public static Rule<Head> normalize(Rule<Head> rule) {
		List<Literal> rewrittenBody = new ArrayList<>();
		for (Literal lit : rule.getBody()) {
			rewrittenBody.addAll(rewriteLiteral(lit));
		}
		return Rules.newRule(rule.getHead(), rewrittenBody);
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
		if (lit.getAtom().getAggregateFunction() == AggregateFunctionSymbol.MIN || lit.getAtom().getAggregateFunction() == AggregateFunctionSymbol.MAX) {
			// No operator normalization needed for #min/#max aggregates.
			return Collections.singletonList(lit);
		}
		if (atom.getLowerBoundOperator().equals(ComparisonOperators.EQ) || atom.getLowerBoundOperator().equals(ComparisonOperators.LE)) {
			// Nothing to do for operator "=" or "<=".
			return Collections.singletonList(lit);
		} else {
			List<Literal> retVal = new ArrayList<>();
			VariableTerm decrementedBound;
			ComparisonOperator lowerBoundOp = atom.getLowerBoundOperator();
			if (lowerBoundOp.equals(ComparisonOperators.LT)) {
				decrementedBound = Terms.newAnonymousVariable();
				retVal.add(createLowerBoundedAggregateLiteral(ComparisonOperators.LE, decrementedBound, atom, !lit.isNegated()));
				retVal.add(createPlusOneTerm(atom.getLowerBoundTerm(), decrementedBound));
			} else if (lowerBoundOp.equals(ComparisonOperators.NE)) {
				retVal.add(createLowerBoundedAggregateLiteral(ComparisonOperators.EQ, atom.getLowerBoundTerm(), atom, lit.isNegated()));
			} else if (lowerBoundOp.equals(ComparisonOperators.GT)) {
				retVal.add(createLowerBoundedAggregateLiteral(ComparisonOperators.LE, atom.getLowerBoundTerm(), atom, lit.isNegated()));
			} else if (lowerBoundOp.equals(ComparisonOperators.GE)) {
				decrementedBound = Terms.newAnonymousVariable();
				retVal.add(createLowerBoundedAggregateLiteral(ComparisonOperators.LE, decrementedBound, atom, lit.isNegated()));
				retVal.add(createPlusOneTerm(atom.getLowerBoundTerm(), decrementedBound));
			} else {
				throw new IllegalStateException("No operator rewriting logic available for literal: " + lit);
			}
			return retVal;
		}
	}

	private static AggregateLiteral createLowerBoundedAggregateLiteral(ComparisonOperator op, Term lowerBoundTerm, AggregateAtom aggregateAtom,
			boolean isNegated) {
		return Literals.fromAtom(Atoms.newAggregateAtom(op, lowerBoundTerm, aggregateAtom.getAggregateFunction(),
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
		Term increment = Terms.newArithmeticTerm(term, ArithmeticOperator.PLUS, Terms.newConstant(1));
		ComparisonAtom atom = Atoms.newComparisonAtom(targetVariable, increment, ComparisonOperators.EQ);
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
		if (operator.equals(ComparisonOperators.EQ) || operator.equals(ComparisonOperators.NE)) {
			flippedOperator = operator;
		} else if (operator.equals(ComparisonOperators.LE)) {
			flippedOperator = ComparisonOperators.GE;
		} else if (operator.equals(ComparisonOperators.LT)) {
			flippedOperator = ComparisonOperators.GT;
		} else if (operator.equals(ComparisonOperators.GE)) {
			flippedOperator = ComparisonOperators.LE;
		} else if (operator.equals(ComparisonOperators.GT)) {
			flippedOperator = ComparisonOperators.LT;
		} else {
			throw new IllegalArgumentException("Unsupported comparison operator for aggregate atom: " + operator);
		}
		return Atoms.newAggregateAtom(flippedOperator, atom.getUpperBoundTerm(), atom.getAggregateFunction(), atom.getAggregateElements())
				.toLiteral(!lit.isNegated());
	}

}
