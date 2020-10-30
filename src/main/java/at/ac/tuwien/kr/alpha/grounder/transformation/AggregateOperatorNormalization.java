package at.ac.tuwien.kr.alpha.grounder.transformation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import at.ac.tuwien.kr.alpha.common.ComparisonOperator;
import at.ac.tuwien.kr.alpha.common.atoms.AggregateAtom;
import at.ac.tuwien.kr.alpha.common.atoms.AggregateAtom.AggregateFunctionSymbol;
import at.ac.tuwien.kr.alpha.common.atoms.AggregateLiteral;
import at.ac.tuwien.kr.alpha.common.atoms.Literal;
import at.ac.tuwien.kr.alpha.common.program.InputProgram;
import at.ac.tuwien.kr.alpha.common.rule.BasicRule;
import at.ac.tuwien.kr.alpha.common.terms.Terms;
import at.ac.tuwien.kr.alpha.common.terms.VariableTerm;

/**
 * Transforms an {@link InputProgram} such that, for all aggregate (body-)literals, only the comparison operators "="
 * and "<=" are used.
 * 
 * Rewriting of "#count" and "#sum" aggregates is done using the following equivalences:
 * <ul>
 * <li><code>X < #aggr{...}</code> == <code>XP <= #aggr{...}, XP = X - 1</code></li>
 * <li><code>X != #aggr{...}</code> == <code>not X = #aggr{...}</code></li>
 * <li><code>X > #aggr{...}</code> == <code>not X <= #aggr{...}</code></li>
 * <li><code>X >= #aggr{...}</code> == <code>not XP <= #aggr{...}, XP = X - 1</code></li>
 * <li><code>not X < #aggr{...}</code> == <code>not XP <= #aggr{...}, XP = X - 1</code></li>
 * <li><code>not X != #aggr{...}</code> == <code>X = #aggr{...}</code></li>
 * <li><code>not X > #aggr{...}</code> == <code>X <= #aggr{...}</code></li>
 * <li><code>not X >= #aggr{...}</code> == <code>XP <= #aggr{...}, XP = X - 1</code></li>
 * </ul>
 * Operators for "#min" and "#max" aggregates are not rewritten.
 * 
 * Note that input programs must only contain aggregate literals of form <code>VAR OP #aggr{...}</code>, i.e. with only
 * a left term and operator. When preprocessing programs, apply this transformation AFTER
 * {@link at.ac.tuwien.kr.alpha.grounder.transformation.AggregateLiteralSplitting}.
 * 
 * Copyright (c) 2020, the Alpha Team.
 */
// TODO introduce type "NormalizedAggregateLiteral" or something
public class AggregateOperatorNormalization extends ProgramTransformation<InputProgram, InputProgram> {

	@Override
	public InputProgram apply(InputProgram inputProgram) {
		List<BasicRule> rewrittenRules = new ArrayList<>();
		for (BasicRule rule : inputProgram.getRules()) {
			rewrittenRules.add(handleRule(rule));
		}
		return new InputProgram(rewrittenRules, inputProgram.getFacts(), inputProgram.getInlineDirectives());
	}

	private BasicRule handleRule(BasicRule rule) {
		List<Literal> rewrittenBody = new ArrayList<>();
		for (Literal lit : rule.getBody()) {
			rewrittenBody.addAll(rewriteLiteral(lit));
		}
		return new BasicRule(rule.getHead(), rewrittenBody);
	}

	private List<Literal> rewriteLiteral(Literal lit) {
		if (lit instanceof AggregateLiteral) {
			return rewriteAggregateOperator((AggregateLiteral) lit);
		} else {
			return Collections.singletonList(lit);
		}
	}

	private List<Literal> rewriteAggregateOperator(AggregateLiteral lit) {
		AggregateAtom atom = lit.getAtom();
		if (lit.getAtom().getAggregatefunction() == AggregateFunctionSymbol.MIN || lit.getAtom().getAggregatefunction() == AggregateFunctionSymbol.MAX) {
			// No operator normalization needed for #min/#max aggregates
			return Collections.singletonList(lit);
		}
		if (atom.getLowerBoundOperator() == ComparisonOperator.EQ || atom.getLowerBoundOperator() == ComparisonOperator.LE) {
			// Nothing to do for operator "=" or "<=".
			return Collections.singletonList(lit);
		} else {
			List<Literal> retVal = new ArrayList<>();
			VariableTerm decrementedBound;
			switch (atom.getLowerBoundOperator()) {
				case LT:
					decrementedBound = VariableTerm.getAnonymousInstance();
					retVal.add(new AggregateLiteral(
							new AggregateAtom(
									ComparisonOperator.LE, decrementedBound, null, null, atom.getAggregatefunction(), atom.getAggregateElements()),
							!lit.isNegated()));
					retVal.add(Terms.decrementTerm(atom.getLowerBoundTerm(), decrementedBound));
					break;
				case NE:
					retVal.add(new AggregateLiteral(
							new AggregateAtom(
									ComparisonOperator.EQ, atom.getLowerBoundTerm(), null, null, atom.getAggregatefunction(), atom.getAggregateElements()),
							lit.isNegated()));
					break;
				case GT:
					retVal.add(new AggregateLiteral(
							new AggregateAtom(
									ComparisonOperator.LE, atom.getLowerBoundTerm(), null, null, atom.getAggregatefunction(), atom.getAggregateElements()),
							lit.isNegated()));
					break;
				case GE:
					decrementedBound = VariableTerm.getAnonymousInstance();
					retVal.add(new AggregateLiteral(
							new AggregateAtom(
									ComparisonOperator.LE, decrementedBound, null, null, atom.getAggregatefunction(), atom.getAggregateElements()),
							lit.isNegated()));
					retVal.add(Terms.decrementTerm(atom.getLowerBoundTerm(), decrementedBound));
					break;
				default:
					throw new IllegalStateException("No operator rewriting logic available for literal: " + lit);
			}
			return retVal;
		}
	}

}
