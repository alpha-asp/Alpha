package at.ac.tuwien.kr.alpha.grounder;

import at.ac.tuwien.kr.alpha.common.atoms.BasicAtom;
import at.ac.tuwien.kr.alpha.common.atoms.Literal;
import at.ac.tuwien.kr.alpha.common.predicates.FixedInterpretationPredicate;
import at.ac.tuwien.kr.alpha.common.terms.VariableTerm;

import java.util.*;

/**
 * Provides the grounder with information on the order to ground the literals in the body of a rule.
 * Copyright (c) 2017, the Alpha Team.
 */
public class RuleGroundingOrder {
	private final NonGroundRule nonGroundRule;
	private HashMap<Literal, Literal[]> groundingOrder;
	private HashMap<Literal, Float> literalSelectivity;
	private LinkedList<Literal> startingLiterals;
	private final boolean fixedGroundingInstantiation;

	RuleGroundingOrder(NonGroundRule nonGroundRule) {
		this.nonGroundRule = nonGroundRule;
		this.literalSelectivity = new HashMap<>();
		resetLiteralSelectivity();
		this.groundingOrder = new HashMap<>();
		this.fixedGroundingInstantiation = computeStartingLiterals();
	}

	private void resetLiteralSelectivity() {
		// Set selectivity of all literals to 1.0f.
		for (Literal literal : nonGroundRule.getRule().getBody()) {
			literalSelectivity.put(literal, 1.0f);
		}
	}

	private boolean computeStartingLiterals() {
		LinkedHashSet<Literal> fixedStartingLiterals = new LinkedHashSet<>();
		LinkedHashSet<Literal> ordinaryStartingLiterals = new LinkedHashSet<>();
		// Check each literal in the rule body whether it is eligible.
		for (Literal literal : nonGroundRule.getRule().getBody()) {
			// Only positive literals that need no variables already bound can start grounding.
			if (literal.isNegated() || literal.getNonBindingVariables().size() != 0) {
				continue;
			}

			if (literal instanceof BasicAtom) {
				// BasicAtom is the main/ordinary case.
				ordinaryStartingLiterals.add(literal);
			} else if (literal.getPredicate() instanceof FixedInterpretationPredicate) {
				// If positive literal is no BasicAtom but has fixed instantiations,
				// it can be the starting literal for some fixed instantiation.
				fixedStartingLiterals.add(literal);
			} else {
				throw new RuntimeException("Rule contains a positive literal that neither is an " +
					"ordinary atom and nor an atom whose truth value is fixed (built-in): " + literal + "\nRule: " + nonGroundRule + "\n Should not happen.");
			}
		}
		// If there are no positive BasicAtoms, the rule only contains fixed ground instantiation literals and those are starting for the one-time grounding.
		if (ordinaryStartingLiterals.isEmpty()) {
			startingLiterals = new LinkedList<>(fixedStartingLiterals);
			return true;
		} else {
			startingLiterals = new LinkedList<>(ordinaryStartingLiterals);
			return false;
		}

	}

	Collection<Literal> getStartingLiterals() {
		return Collections.unmodifiableList(startingLiterals);
	}

	public void updateLiteralSelectivity(Literal literal, int numGivenTuples, int numObtainedTuples) {
		// TODO: add old selectivity (with a decay factor) and new selectivity.
	}

	public Literal[] orderStartingFrom(Literal startingLiteral) {
		return groundingOrder.get(startingLiteral);
	}

	/**
	 * States whether the rule is without positive ordinary atoms, as for example in: p(Y) :- X = 1..3, Y = X + 2, &ext[X,Y]().
	 * @return true if the rule has a (limited number of) fixed grounding instantiation(s).
	 */
	public boolean fixedInstantiation() {
		return fixedGroundingInstantiation;
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
		ArrayList<Literal> literalsOrder;
		if (fixedGroundingInstantiation) {
			// Fixed grounding starts without the startingLiteral being bound already, add it to the order.
			literalsOrder = new ArrayList<>(bodyLiterals.size());
			literalsOrder.add(startingLiteral);
		} else {
			literalsOrder = new ArrayList<>(bodyLiterals.size() - 1);
		}
		while (!remainingLiterals.isEmpty()) {
			Literal nextGroundingLiteral = selectNextGroundingLiteral(remainingLiterals, boundVariables);
			if (nextGroundingLiteral == null) {
				throw new RuntimeException("Could not find a grounding order for rule " + nonGroundRule + " with starting literal: " + startingLiteral + ". Rule is not safe.");
			}
			remainingLiterals.remove(nextGroundingLiteral);
			boundVariables.addAll(nextGroundingLiteral.getBindingVariables());
			literalsOrder.add(nextGroundingLiteral);
		}
		groundingOrder.put(startingLiteral, literalsOrder.toArray(new Literal[0]));
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
