package at.ac.tuwien.kr.alpha.grounder.transformation;

import org.apache.commons.lang3.tuple.ImmutablePair;

import java.util.ArrayList;
import java.util.List;

import at.ac.tuwien.kr.alpha.common.ComparisonOperator;
import at.ac.tuwien.kr.alpha.common.atoms.AggregateAtom;
import at.ac.tuwien.kr.alpha.common.atoms.AggregateLiteral;
import at.ac.tuwien.kr.alpha.common.atoms.Literal;
import at.ac.tuwien.kr.alpha.common.program.InputProgram;
import at.ac.tuwien.kr.alpha.common.rule.BasicRule;

/**
 * Splits aggregate literals with both "lower" and "upper" bound operators into literals with only one operator each.
 * 
 * Follows The <a href="https://www.mat.unical.it/aspcomp2013/files/ASP-CORE-2.03c.pdf">ASP-Core2 Standard</a> (Section
 * 3.3) for splitting logic.<br/>
 * Aggregate (body-)literals of format <code>LT LO #aggr{...} RO RT</code> are rewritten into two separate literals,
 * <code>LT LO #aggr{...}</code> and <code>RT inv(RO) #aggr{...}</code>, where <code>inv(OP)</code> for an operator OP
 * denotes the inverse comparison operator as defined in section 3.3 of the standard. <br/>
 * Negative aggregate (body-)literals of format <code>not LT LO #aggr{...} RO RT</code> require the source rule to be
 * split into rules:
 * <ul>
 * <li>H :- b1,...,bn, not LT LO #aggr{...}.</li>
 * <li>H :- b1,...,bn, not RT inv(RO) #aggr{...}.</li>
 * </ul>
 * 
 * Copyright (c) 2020, the Alpha Team.
 */
public class AggregateLiteralSplitting extends ProgramTransformation<InputProgram, InputProgram> {

	@Override
	public InputProgram apply(InputProgram inputProgram) {
		List<BasicRule> rewrittenRules = new ArrayList<>();
		for (BasicRule rule : inputProgram.getRules()) {
			rewrittenRules.addAll(handleRule(rule));
		}
		return new InputProgram(rewrittenRules, inputProgram.getFacts(), inputProgram.getInlineDirectives());
	}

	private List<BasicRule> handleRule(BasicRule rule) {
		List<BasicRule> retVal = new ArrayList<>();
		rewriteRule(rule, retVal);
		return retVal;
	}

	private void rewriteRule(BasicRule sourceRule, List<BasicRule> resultRules) {
		boolean containsRewrittenAggregate = false;
		for (Literal lit : sourceRule.getBody()) {
			AggregateLiteral aggLit;
			if ((lit instanceof AggregateLiteral) && shouldRewrite((aggLit = (AggregateLiteral) lit))) {
				containsRewrittenAggregate = true;
				for (BasicRule rewritten : splitAggregateLiteral(sourceRule, aggLit)) {
					rewriteRule(rewritten, resultRules);
				}
			}
		}
		if (!containsRewrittenAggregate) {
			resultRules.add(sourceRule);
		}
	}

	// @Override
	protected boolean shouldRewrite(AggregateLiteral lit) {
		return lit.getAtom().getLowerBoundTerm() != null && lit.getAtom().getUpperBoundTerm() != null;
	}

	private List<BasicRule> splitAggregateLiteral(BasicRule sourceRule, AggregateLiteral aggLit) {
		List<Literal> remainingBody = new ArrayList<>();
		sourceRule.getBody().forEach((lit) -> {
			if (lit != aggLit) {
				remainingBody.add(lit);
			}
		});
		ImmutablePair<AggregateAtom, AggregateAtom> normalizedAtoms = splitCombinedAggregateAtom(aggLit.getAtom());
		List<BasicRule> retVal = new ArrayList<>();
		if (aggLit.isNegated()) {
			List<Literal> leftRuleBody = new ArrayList<>(remainingBody);
			leftRuleBody.add(normalizedAtoms.left.toLiteral(false));
			List<Literal> rightRuleBody = new ArrayList<>(remainingBody);
			rightRuleBody.add(normalizedAtoms.right.toLiteral(false));
			retVal.add(new BasicRule(sourceRule.getHead(), leftRuleBody));
			retVal.add(new BasicRule(sourceRule.getHead(), rightRuleBody));
		} else {
			List<Literal> resultBody = new ArrayList<>(remainingBody);
			resultBody.add(normalizedAtoms.left.toLiteral(true));
			resultBody.add(normalizedAtoms.right.toLiteral(true));
			retVal.add(new BasicRule(sourceRule.getHead(), resultBody));
		}
		return retVal;
	}

	private ImmutablePair<AggregateAtom, AggregateAtom> splitCombinedAggregateAtom(AggregateAtom atom) {
		AggregateAtom leftHandAtom = new AggregateAtom(atom.getLowerBoundOperator(), atom.getLowerBoundTerm(), null, null, atom.getAggregatefunction(),
				atom.getAggregateElements());
		AggregateAtom rightHandAtom = new AggregateAtom(switchOperands(atom.getUpperBoundOperator()), atom.getUpperBoundTerm(), null, null,
				atom.getAggregatefunction(), atom.getAggregateElements());
		return new ImmutablePair<>(leftHandAtom, rightHandAtom);
	}

	private static ComparisonOperator switchOperands(ComparisonOperator op) {
		switch (op) {
			case EQ:
				return op;
			case NE:
				return op;
			case LT:
				return ComparisonOperator.GT;
			case LE:
				return ComparisonOperator.GE;
			case GT:
				return ComparisonOperator.LT;
			case GE:
				return ComparisonOperator.LE;
			default:
				throw new IllegalArgumentException("Unsupported ComparisonOperator " + op + "!");
		}
	}

}
