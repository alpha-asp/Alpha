package at.ac.tuwien.kr.alpha.grounder;

import at.ac.tuwien.kr.alpha.common.NoGood;
import at.ac.tuwien.kr.alpha.common.atoms.Atom;
import at.ac.tuwien.kr.alpha.common.atoms.FixedInterpretationAtom;
import at.ac.tuwien.kr.alpha.grounder.atoms.ChoiceAtom;
import at.ac.tuwien.kr.alpha.grounder.atoms.RuleAtom;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

/**
 * Class to generate ground NoGoods out of non-ground rules and grounding substitutions.
 * Copyright (c) 2017, the Alpha Team.
 */
public class NoGoodGenerator {
	private static final Logger LOGGER = LoggerFactory.getLogger(NoGoodGenerator.class);

	private final IntIdGenerator noGoodIdGenerator;
	private Map<NoGood, Integer> noGoodIdentifiers = new LinkedHashMap<>();
	private final IntIdGenerator choiceAtomsGenerator;
	private Pair<Map<Integer, Integer>, Map<Integer, Integer>> newChoiceAtoms = new ImmutablePair<>(new LinkedHashMap<>(), new LinkedHashMap<>());
	private final NaiveGrounder naiveGrounder;

	NoGoodGenerator(IntIdGenerator noGoodIdGenerator, IntIdGenerator choiceAtomsGenerator, NaiveGrounder naiveGrounder) {
		this.noGoodIdGenerator = noGoodIdGenerator;
		this.choiceAtomsGenerator = choiceAtomsGenerator;
		this.naiveGrounder = naiveGrounder;
	}

	/**
	 * Generates all NoGoods resulting from a non-ground rule and a variable substitution.
	 * @param nonGroundRule the non-ground rule.
	 * @param substitution the grounding substitution, i.e., applying substitution to nonGroundRule results in a ground rule.
	 *                     Assumption: atoms with fixed interpretation evaluate to true under the substitution.
	 * @return the NoGoods corresponding to the ground rule.
	 */
	List<NoGood> generateNoGoodsFromGroundSubstitution(NonGroundRule nonGroundRule, Substitution substitution) {

		// Collect ground atoms in the body
		ArrayList<Integer> bodyAtomsPositive = new ArrayList<>();
		ArrayList<Integer> bodyAtomsNegative = new ArrayList<>();
		// FIXME: iterate on literals of the rule instead of NonGroundRule.
		for (Atom atom : nonGroundRule.getBodyAtomsPositive()) {
			if (atom instanceof FixedInterpretationAtom) {
				// Atom has fixed interpretation, hence was checked earlier that it evaluates to true under the given substitution.
				// FixedInterpretationAtoms need not be shown to the solver, skip it.
				continue;
			}

			Atom groundAtom = atom.substitute(substitution);
			// Consider facts to eliminate ground atoms from the generated nogoods that are always true
			// and eliminate nogoods that are always satisfied due to facts.
			Set<Instance> factInstances = naiveGrounder.getFactsFromProgram(groundAtom.getPredicate());
			if (factInstances != null && factInstances.contains(new Instance(groundAtom.getTerms()))) {
				// Skip positive atoms that are always true.
				continue;
			}
			if (!naiveGrounder.existsRuleWithPredicateInHead(groundAtom.getPredicate())) {
				// Atom is no fact and no rule defines it, it cannot be derived (i.e., is always false), skip whole rule as it will never fire.
				return emptyList();
			}
			int groundAtomPositive = naiveGrounder.atomStore.add(groundAtom);
			bodyAtomsPositive.add(groundAtomPositive);
		}

		for (Atom atom : nonGroundRule.getBodyAtomsNegative()) {
			Atom groundAtom = atom.substitute(substitution);
			Set<Instance> factInstances = naiveGrounder.getFactsFromProgram(groundAtom.getPredicate());
			if (factInstances != null && factInstances.contains(new Instance(groundAtom.getTerms()))) {
				// Negative atom that is always true encountered, skip whole rule as it will never fire.
				return emptyList();
			}
			if (!naiveGrounder.existsRuleWithPredicateInHead(groundAtom.getPredicate())) {
				// Negative atom is no fact and no rule defines it, it is always false, skip it.
				continue;
			}
			int groundAtomNegative = naiveGrounder.atomStore.add(groundAtom);
			bodyAtomsNegative.add(groundAtomNegative);
		}

		int bodySize = bodyAtomsPositive.size() + bodyAtomsNegative.size();

		// A constraint is represented by exactly one NoGood.
		if (nonGroundRule.isConstraint()) {
			int[] constraintLiterals = new int[bodySize];
			int i = 0;
			for (Integer atomId : bodyAtomsPositive) {
				constraintLiterals[i++] = +atomId;
			}
			for (Integer atomId : bodyAtomsNegative) {
				constraintLiterals[i++] = -atomId;
			}
			return singletonList(new NoGood(constraintLiterals));
		}

		// Prepare atom representing the rule body
		Atom ruleBodyRepresentingPredicate = new RuleAtom(nonGroundRule, substitution);
		// Check uniqueness of ground rule by testing whether the body representing atom already has an id
		if (naiveGrounder.atomStore.contains(ruleBodyRepresentingPredicate)) {
			// The current ground instance already exists, therefore all NoGoods have already been created.
			return emptyList();
		}

		int bodyRepresentingAtomId = naiveGrounder.atomStore.add(ruleBodyRepresentingPredicate);

		// Prepare head atom
		int headAtomId = naiveGrounder.atomStore.add(nonGroundRule.getHeadAtom().substitute(substitution));

		// Create NoGood for body.
		int[] bodyLiterals = new int[bodySize + 1];
		bodyLiterals[0] = -bodyRepresentingAtomId;
		int i = 1;
		for (Integer atomId : bodyAtomsPositive) {
			bodyLiterals[i++] = atomId;
		}
		for (Integer atomId : bodyAtomsNegative) {
			bodyLiterals[i++] = -atomId;
		}
		NoGood ruleBody = NoGood.headFirst(bodyLiterals);

		List<NoGood> generatedNoGoods = new ArrayList<>();

		// Generate NoGoods such that the atom representing the body is true iff the body is true.
		for (int j = 1; j < bodyLiterals.length; j++) {
			generatedNoGoods.add(new NoGood(bodyRepresentingAtomId, -bodyLiterals[j]));
		}

		// Create NoGood for head.
		NoGood ruleHead = NoGood.headFirst(-headAtomId, bodyRepresentingAtomId);

		generatedNoGoods.add(ruleBody);
		generatedNoGoods.add(ruleHead);

		// Check if the rule head is unique, add support then:
		if (naiveGrounder.createsUniqueGroundHead(nonGroundRule)) {
			generatedNoGoods.add(supportednessNoGoodUniqueRule(headAtomId, bodyRepresentingAtomId));
		}

		// Check if the body of the rule contains negation, add choices then
		if (bodyAtomsNegative.size() != 0) {
			Map<Integer, Integer> newChoiceOn = newChoiceAtoms.getLeft();
			Map<Integer, Integer> newChoiceOff = newChoiceAtoms.getRight();
			// Choice is on the body representing atom

			// ChoiceOn if all positive body atoms are satisfied
			int[] choiceOnLiterals = new int[bodyAtomsPositive.size() + 1];
			i = 1;
			for (Integer atomId : bodyAtomsPositive) {
				choiceOnLiterals[i++] = atomId;
			}
			int choiceId = choiceAtomsGenerator.getNextId();
			Atom choiceOnAtom = ChoiceAtom.on(choiceId);
			int choiceOnAtomIdInt = naiveGrounder.atomStore.add(choiceOnAtom);
			choiceOnLiterals[0] = -choiceOnAtomIdInt;
			// Add corresponding NoGood and ChoiceOn
			generatedNoGoods.add(NoGood.headFirst(choiceOnLiterals));	// ChoiceOn and ChoiceOff NoGoods avoid MBT and directly set to true, hence the rule head pointer.
			newChoiceOn.put(bodyRepresentingAtomId, choiceOnAtomIdInt);

			// ChoiceOff if some negative body atom is contradicted
			Atom choiceOffAtom = ChoiceAtom.off(choiceId);
			int choiceOffAtomIdInt = naiveGrounder.atomStore.add(choiceOffAtom);
			for (Integer negAtomId : bodyAtomsNegative) {
				// Choice is off if any of the negative atoms is assigned true, hence we add one NoGood for each such atom.
				generatedNoGoods.add(NoGood.headFirst(-choiceOffAtomIdInt, negAtomId));
			}
			newChoiceOff.put(bodyRepresentingAtomId, choiceOffAtomIdInt);
		}
		return generatedNoGoods;
	}

	/**
	 * Associates each new noGood from the given collection with a new Id and adds it to the given map.
	 * @param noGoods the noGoods to associate, may contain already constructed noGoods.
	 * @param newNoGoods the map where the new noGoods are put into.
	 */
	void register(Iterable<NoGood> noGoods, Map<Integer, NoGood> newNoGoods) {
		for (NoGood noGood : noGoods) {
			// Check if noGood was already derived earlier, add if it is new
			if (!noGoodIdentifiers.containsKey(noGood)) {
				int noGoodId = noGoodIdGenerator.getNextId();
				noGoodIdentifiers.put(noGood, noGoodId);
				newNoGoods.put(noGoodId, noGood);
			}
		}
	}

	/**
	 * Associates a noGood with an Id. If a noGood already is associated an Id, it will return that.
	 * @param noGood the noGood to associate.
	 * @return the Id of the noGood.
	 */
	int registerOutsideNoGood(NoGood noGood) {
		if (!noGoodIdentifiers.containsKey(noGood)) {
			int noGoodId = noGoodIdGenerator.getNextId();
			noGoodIdentifiers.put(noGood, noGoodId);
			return noGoodId;
		}
		return noGoodIdentifiers.get(noGood);
	}


	private NoGood supportednessNoGoodUniqueRule(int headAtom, int ruleBodyAtom) {
		return new NoGood(headAtom, -ruleBodyAtom);
	}

	/**
	 * Returns the choice atoms that were created by the NoGoodGenerator since the last time this method was called.
	 * @return the new choice atoms (enabler/disabler mapping) since the last call.
	 */
	Pair<Map<Integer, Integer>, Map<Integer, Integer>> getChoiceAtoms() {
		Pair<Map<Integer, Integer>, Map<Integer, Integer>> currentChoiceAtoms = newChoiceAtoms;
		newChoiceAtoms = new ImmutablePair<>(new LinkedHashMap<>(), new LinkedHashMap<>());
		return currentChoiceAtoms;
	}

	void logChoiceInformation() {
		LOGGER.debug("Choice information is:");
		for (Map.Entry<Integer, Integer> enablers : newChoiceAtoms.getLeft().entrySet()) {
			LOGGER.debug("{} enabled by {}.", enablers.getKey(), enablers.getValue());
		}
		for (Map.Entry<Integer, Integer> disablers : newChoiceAtoms.getRight().entrySet()) {
			LOGGER.debug("{} disabled by {}.", disablers.getKey(), disablers.getValue());
		}
	}

	/**
	 * Helper methods to analyze average nogood length.
	 * @return the average length over all known noGoods.
	 */
	public float computeAverageNoGoodLength() {
		int totalSizes = 0;
		for (Map.Entry<NoGood, Integer> noGoodEntry : noGoodIdentifiers.entrySet()) {
			totalSizes += noGoodEntry.getKey().size();
		}
		return ((float) totalSizes) / noGoodIdentifiers.size();
	}
}
