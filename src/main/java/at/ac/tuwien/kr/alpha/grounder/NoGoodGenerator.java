package at.ac.tuwien.kr.alpha.grounder;

import at.ac.tuwien.kr.alpha.common.NoGood;
import at.ac.tuwien.kr.alpha.common.Predicate;
import at.ac.tuwien.kr.alpha.common.atoms.Atom;
import at.ac.tuwien.kr.alpha.common.atoms.ExternalAtom;
import at.ac.tuwien.kr.alpha.common.atoms.FixedInterpretationLiteral;
import at.ac.tuwien.kr.alpha.grounder.atoms.ChoiceAtom;
import at.ac.tuwien.kr.alpha.grounder.atoms.IntervalAtom;
import at.ac.tuwien.kr.alpha.grounder.atoms.RuleAtom;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptySet;
import static java.util.Collections.singletonList;

/**
 * Class to generate ground NoGoods out of non-ground rules and grounding substitutions.
 * Copyright (c) 2017, the Alpha Team.
 */
public class NoGoodGenerator {
	private static final IntIdGenerator ID_GENERATOR = new IntIdGenerator();
	
	private static final Logger LOGGER = LoggerFactory.getLogger(NoGoodGenerator.class);

	private Map<NoGood, Integer> noGoodIdentifiers = new LinkedHashMap<>();

	private final AtomStore store;
	private final NogoodRegistry registry;
	private final ChoiceRecorder recorder;
	private final Map<Predicate, LinkedHashSet<Instance>> factsFromProgram;
	private final Map<Predicate, HashSet<NonGroundRule>> ruleHeadsToDefiningRules;
	private final Set<NonGroundRule> uniqueGroundRulePerGroundHead;

	NoGoodGenerator(AtomStore store, NogoodRegistry registry, ChoiceRecorder recorder, Map<Predicate, LinkedHashSet<Instance>> factsFromProgram, Map<Predicate, HashSet<NonGroundRule>> ruleHeadsToDefiningRules, Set<NonGroundRule> uniqueGroundRulePerGroundHead) {
		this.store = store;
		this.registry = registry;
		this.recorder = recorder;
		this.factsFromProgram = factsFromProgram;
		this.ruleHeadsToDefiningRules = ruleHeadsToDefiningRules;
		this.uniqueGroundRulePerGroundHead = uniqueGroundRulePerGroundHead;
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
			if (atom instanceof FixedInterpretationLiteral) {
				// Atom has fixed interpretation, hence was checked earlier that it evaluates to true under the given substitution.
				// FixedInterpretationAtoms need not be shown to the solver, skip it.
				continue;
			}

			Atom groundAtom = atom.substitute(substitution);
			// Consider facts to eliminate ground atoms from the generated nogoods that are always true
			// and eliminate nogoods that are always satisfied due to facts.
			Set<Instance> factInstances = factsFromProgram.get(groundAtom.getPredicate());
			if (factInstances != null && factInstances.contains(new Instance(groundAtom.getTerms()))) {
				// Skip positive atoms that are always true.
				continue;
			}

			HashSet<NonGroundRule> rulesWithPredicateInHead = ruleHeadsToDefiningRules.get(groundAtom.getPredicate());

			if (rulesWithPredicateInHead == null || rulesWithPredicateInHead.isEmpty()) {
				// Atom is no fact and no rule defines it, it cannot be derived (i.e., is always false), skip whole rule as it will never fire.
				return emptyList();
			}
			int groundAtomPositive = store.add(groundAtom);
			bodyAtomsPositive.add(groundAtomPositive);
		}

		if (LOGGER.isDebugEnabled()) {
			// Debugging helper: record known grounding substitutions.
			//knownGroundingSubstitutions.putIfAbsent(nonGroundRule, new LinkedHashSet<>());
			//knownGroundingSubstitutions.get(nonGroundRule).add(substitution);
		}

		final List<Integer> pos = collectPos(nonGroundRule, substitution);
		final List<Integer> neg = collectNeg(nonGroundRule, substitution);

		if (pos == null || neg == null) {
			return emptyList();
		}

		// A constraint is represented by exactly one NoGood.
		if (nonGroundRule.isConstraint()) {
			return singletonList(NoGood.fromConstraint(pos, neg));
		}

		// Prepare atom representing the rule body
		final Atom ruleBodyAtom = new RuleAtom(nonGroundRule, substitution);

		// Check uniqueness of ground rule by testing whether the body representing atom already has an id
		if (store.contains(ruleBodyAtom)) {
			// The current ground instance already exists, therefore all NoGoods have already been created.
			return emptyList();
		}

		final int bodyAtom = store.add(ruleBodyAtom);
		final int headAtom = store.add(nonGroundRule.getHeadAtom().substitute(substitution));

		final List<NoGood> generatedNoGoods = new ArrayList<>();

		// Create NoGood for body.
		final NoGood ruleBody = NoGood.fromBody(pos, neg, bodyAtom);
		generatedNoGoods.add(ruleBody);

		// Generate NoGoods such that the atom representing the body is true iff the body is true.
		for (int j = 1; j < ruleBody.size(); j++) {
			generatedNoGoods.add(new NoGood(bodyAtom, -ruleBody.getLiteral(j)));
		}
		generatedNoGoods.add(NoGood.headFirst(-headAtom, bodyAtom));

		// Check if the rule head is unique, add support then:
		if (uniqueGroundRulePerGroundHead.contains(nonGroundRule)) {
			generatedNoGoods.add(NoGood.support(headAtom, bodyAtom));
		}

		// Check if the body of the rule contains negation, add choices then
		if (!neg.isEmpty()) {
			generatedNoGoods.addAll(recorder.generate(pos, neg, bodyAtom));
		}

		return generatedNoGoods;
	}

	private List<Integer> collectNeg(NonGroundRule nonGroundRule, Substitution substitution) {
		final List<Integer> neg = new ArrayList<>(nonGroundRule.getBodyAtomsNegative().size());
		for (Atom atom : nonGroundRule.getBodyAtomsNegative()) {
			final Atom groundAtom = atom.substitute(substitution);

			HashSet<Instance> factInstances = factsFromProgram.get(groundAtom.getPredicate());
			if (factInstances != null && factInstances.contains(new Instance(groundAtom.getTerms()))) {
				// Negative atom that is always true encountered, skip whole rule as it will never fire.
				return null;
			}

			HashSet<NonGroundRule> definingRules = ruleHeadsToDefiningRules.get(groundAtom.getPredicate());
			if (definingRules == null || definingRules.isEmpty()) {
				// Negative atom is no fact and no rule defines it, it is always false, skip it.
				continue;
			}

			neg.add(store.add(groundAtom));
		}
		return neg;
	}

	private List<Integer> collectPos(NonGroundRule nonGroundRule, Substitution substitution) {
		final List<Integer> pos = new ArrayList<>(nonGroundRule.getBodyAtomsNegative().size());

		for (Atom atom : nonGroundRule.getBodyAtomsPositive()) {
			if (atom instanceof ExternalAtom) {
				ExternalAtom external = (ExternalAtom) atom;

				if (external.hasOutput()) {
					continue;
				}

				// Truth of builtin atoms does not depend on any assignment
				// hence, they need not be represented as long as they evaluate to true
				List<Substitution> substitutions = external.getSubstitutions(substitution);

				if (substitutions.isEmpty()) {
					return null;
				}
				continue;
			}

			if (atom instanceof IntervalAtom) {
				// IntervalAtoms are needed for deriving all substitutions of intervals but otherwise can be ignored.
				continue;
			}

			Atom groundAtom = atom.substitute(substitution);
			// Consider facts to eliminate ground atoms from the generated nogoods that are always true
			// and eliminate nogoods that are always satisfied due to facts.
			HashSet<Instance> factInstances = factsFromProgram.get(groundAtom.getPredicate());
			if (factInstances != null && factInstances.contains(new Instance(groundAtom.getTerms()))) {
				// Skip positive atoms that are always true.
				continue;
			}

			HashSet<NonGroundRule> definingRules = ruleHeadsToDefiningRules.get(groundAtom.getPredicate());
			if (definingRules == null || definingRules.isEmpty()) {
				// Atom is no fact and no rule defines it, it cannot be derived (i.e., is always false), skip whole rule as it will never fire.
				return null;
			}
			pos.add(store.add(groundAtom));
		}
		return pos;
	}
}
