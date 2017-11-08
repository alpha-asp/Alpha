package at.ac.tuwien.kr.alpha.grounder;

import at.ac.tuwien.kr.alpha.common.atoms.BasicAtom;
import at.ac.tuwien.kr.alpha.common.atoms.Literal;
import at.ac.tuwien.kr.alpha.common.terms.VariableTerm;

import java.util.*;

/**
 * Provides the grounder with information on the order to ground the literals in the body of a rule.
 * Copyright (c) 2017, the Alpha Team.
 */
public class RuleGroundingOrder {
	private final NonGroundRule nonGroundRule;
	private HashMap<Literal, List<Literal>> groundingOrder;
	private HashMap<Literal, Float> literalSelectivity;
	private LinkedList<BasicAtom> startingLiterals;

	public RuleGroundingOrder(NonGroundRule nonGroundRule) {
		this.nonGroundRule = nonGroundRule;
		this.literalSelectivity = new HashMap<>();
		resetLiteralSelectivity();
		this.groundingOrder = new HashMap<>();
		computeStartingLiterals();
	}

	private void resetLiteralSelectivity() {
		// Set selectivity of all literals to 1.0f.
		for (Literal literal : nonGroundRule.getRule().getBody()) {
			literalSelectivity.put(literal, 1.0f);
		}
	}

	private void computeStartingLiterals() {
		startingLiterals = new LinkedList<>();
		// Check each literal in the rule body whether it is eligible.
		for (Literal literal : nonGroundRule.getRule().getBody()) {
			// Only a positive BasicAtom that needs no variables bound, but has some variables can be start of grounding.
			if (literal instanceof BasicAtom
				&& !literal.isNegated()
				&& literal.getNonBindingVariables().size() == 0
				&& !literal.isGround()) {
				// A rule might contain two literals that bind the same instances,
				// e.g., in p(X,A) :- q(X), q(A). both q(X) and q(A) can be starting but since they
				// derive exactly the same ground instances, we select only one to be starting.
				// Specifically, we select the most general literal of the body in the following.

				// Iterate over all known starting literals.
				boolean lessGeneralThanExisting = false;
				Iterator<BasicAtom> it = startingLiterals.iterator();
				while (it.hasNext()) {
					BasicAtom currentStartingLiteral = it.next();
					// Test if literal is more general than startingLiteral, or other way round, or incomparable.
					if (Substitution.findEqualizingSubstitution((BasicAtom) literal, currentStartingLiteral) != null) {
						// The current literal is more general than the starting one, remove it.
						it.remove();
					} else if (Substitution.findEqualizingSubstitution(currentStartingLiteral, (BasicAtom) literal) != null) {
						// The current literal is less general, it is no starting literal.
						lessGeneralThanExisting = true;
						break;
					}
				}
				// Add literal if it is more general or incomparable with other starting literals.
				if (!lessGeneralThanExisting) {
					startingLiterals.add((BasicAtom) literal);
				}
			}
		}
	}

	public Collection<Literal> getStartingLiterals() {
		return Collections.unmodifiableList(startingLiterals);
	}

	public void updateLiteralSelectivity(Literal literal, int numGivenTuples, int numObtainedTuples) {
		// TODO: add old selectivity (with a decay factor) and new selectivity.
	}

	public List<Literal> getGroundingOrder(Literal startingLiteral) {
		return groundingOrder.get(startingLiteral);
	}

	public void computeGroundingOrders() {
		for (Literal literal : startingLiterals) {
			computeGroundingOrder(literal);
		}
	}

	private void computeGroundingOrder(Literal startingLiteral) {
		List<Literal> bodyLiterals = nonGroundRule.getRule().getBody();
		HashSet<VariableTerm> boundVariables = new HashSet<>();
		boundVariables.addAll(startingLiteral.getBindingVariables());
		LinkedHashSet<Literal> remainingLiterals = new LinkedHashSet<>(bodyLiterals);
		remainingLiterals.remove(startingLiteral);
		ArrayList<Literal> literalsOrder = new ArrayList<>(bodyLiterals.size() - 1);
		while (!remainingLiterals.isEmpty()) {
			Literal nextGroundingLiteral = selectNextGroundingLiteral(remainingLiterals, boundVariables);
			if (nextGroundingLiteral == null) {
				throw new RuntimeException("Could not find a grounding order for rule " + nonGroundRule + " with starting literal: " + startingLiteral + ". Rule is not safe.");
			}
			remainingLiterals.remove(nextGroundingLiteral);
			boundVariables.addAll(nextGroundingLiteral.getBindingVariables());
			literalsOrder.add(nextGroundingLiteral);
		}
		groundingOrder.put(startingLiteral, literalsOrder);
	}

	private Literal selectNextGroundingLiteral(LinkedHashSet<Literal> remainingLiterals, Set<VariableTerm> boundVariables) {
		Float bestSelectivity = Float.MAX_VALUE;
		Literal bestLiteral = null;
		boolean bestLiteralSharesVariables = false;
		// Find the best literal whose nonbinding variables are already bound and whose selectivity is highest.
		// To avoid cross products, select those first that have some of their variables already bound.
		for (Literal literal : remainingLiterals) {
			if (!boundVariables.containsAll(literal.getNonBindingVariables())) {
				// Only consider literals whose nonbinding variables are already bound.
				continue;
			}
			Float selectivity = literalSelectivity.get(literal);
			boolean sharesVariables = sharesVariables(boundVariables, literal.getBindingVariables(), literal.getNonBindingVariables());
			if (bestLiteral == null
				|| sharesVariables && selectivity < bestSelectivity
				|| sharesVariables && !bestLiteralSharesVariables) {
				bestLiteral = literal;
				bestSelectivity = selectivity;
				bestLiteralSharesVariables = sharesVariables;
			}
		}
		return bestLiteral;
	}

	private boolean sharesVariables(Collection<VariableTerm> set1, Collection<VariableTerm> set2part1, Collection<VariableTerm> set2part2) {
		return !Collections.disjoint(set1, set2part1) || !Collections.disjoint(set1, set2part2);
	}
}
