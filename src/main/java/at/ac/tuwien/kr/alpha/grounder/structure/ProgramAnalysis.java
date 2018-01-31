package at.ac.tuwien.kr.alpha.grounder.structure;

import at.ac.tuwien.kr.alpha.common.*;
import at.ac.tuwien.kr.alpha.common.atoms.Atom;
import at.ac.tuwien.kr.alpha.common.atoms.BasicAtom;
import at.ac.tuwien.kr.alpha.common.atoms.FixedInterpretationLiteral;
import at.ac.tuwien.kr.alpha.common.atoms.Literal;
import at.ac.tuwien.kr.alpha.grounder.*;
import at.ac.tuwien.kr.alpha.solver.ThriceTruth;

import java.util.*;

import static at.ac.tuwien.kr.alpha.Util.oops;

/**
 * Copyright (c) 2017, the Alpha Team.
 */
public class ProgramAnalysis {

	private final Map<Predicate, HashSet<NonGroundRule>> predicateDefiningRules;
	private final PredicateDependencyGraph predicateDependencyGraph;
	private final AtomStore atomStore;
	private final WorkingMemory workingMemory;
	private final Map<Predicate, LinkedHashSet<Instance>> factsFromProgram;

	public ProgramAnalysis(Program program, AtomStore atomStore, WorkingMemory workingMemory, Map<Predicate, LinkedHashSet<Instance>> factsFromProgram) {
		this.atomStore = atomStore;
		this.workingMemory = workingMemory;
		this.factsFromProgram = factsFromProgram;
		predicateDefiningRules = new HashMap<>();
		predicateDependencyGraph = PredicateDependencyGraph.buildFromProgram(program);
	}

	public void recordDefiningRule(Predicate headPredicate, NonGroundRule rule) {
		predicateDefiningRules.putIfAbsent(headPredicate, new HashSet<>());
		predicateDefiningRules.get(headPredicate).add(rule);
	}

	public Map<Predicate, HashSet<NonGroundRule>> getPredicateDefiningRules() {
		return Collections.unmodifiableMap(predicateDefiningRules);
	}

	private Map<Predicate, List<Atom>> assignedAtoms;

	public Set<Literal> reasonsForUnjustified(int atomToJustify, Assignment currentAssignment) {
		Atom literal = atomStore.get(atomToJustify);
		return reasonsForUnjustified(literal, currentAssignment);
	}

	Set<Literal> reasonsForUnjustified(Atom atom, Assignment currentAssignment) {
		assignedAtoms = new LinkedHashMap<>();
		for (int i = 1; i <= atomStore.getHighestAtomId(); i++) {
			Assignment.Entry entry = currentAssignment.get(i);
			if (entry == null) {
				continue;
			}
			Atom assignedAtom = atomStore.get(i);
			assignedAtoms.putIfAbsent(assignedAtom.getPredicate(), new ArrayList<>());
			assignedAtoms.get(assignedAtom.getPredicate()).add(assignedAtom);
		}
		return whyNotMore(new AtomSet(atom, new Substitution(), new LinkedHashSet<>()), new LinkedHashSet<>(), currentAssignment);
	}

	private int renamingCounter;

	private static class FactOrNonGroundRule {
		final Instance factInstance;
		final NonGroundRule nonGroundRule;

		private FactOrNonGroundRule(Instance factInstance) {
			this.factInstance = factInstance;
			this.nonGroundRule = null;
		}

		private FactOrNonGroundRule(NonGroundRule nonGroundRule) {
			this.nonGroundRule = nonGroundRule;
			this.factInstance = null;
		}
	}

	private Set<Literal> whyNotMore(AtomSet toJustify, Set<AtomSet> inJustificationHigherUp, Assignment currentAssignment) {
		Set<Literal> reasons = new HashSet<>();

		Predicate predicate = toJustify.getLiteral().getPredicate();
		// Check if literal is built-in with a fixed interpretation.
		if (toJustify.getLiteral() instanceof FixedInterpretationLiteral) {
			return reasons;
		}
		ArrayList<FactOrNonGroundRule> definingRulesAndFacts = new ArrayList<>();
		// Get facts over the same predicate.
		LinkedHashSet<Instance> factInstances = factsFromProgram.get(predicate);
		if (factInstances != null) {
			for (Instance factInstance : factInstances) {
				definingRulesAndFacts.add(new FactOrNonGroundRule(factInstance));
			}
		}

		HashSet<NonGroundRule> rulesDefiningPredicate = getPredicateDefiningRules().get(predicate);
		if (rulesDefiningPredicate != null) {
			for (NonGroundRule nonGroundRule : rulesDefiningPredicate) {
				definingRulesAndFacts.add(new FactOrNonGroundRule(nonGroundRule));
			}
		}
		eachRule:
		for (FactOrNonGroundRule factOrNonGroundRule : definingRulesAndFacts) {
			boolean isNonGroundRule = factOrNonGroundRule.nonGroundRule != null;
			List<Literal> renamedBody;
			Atom headAtom;
			if (isNonGroundRule) {
				// First rename all variables in the rule.
				Rule rule = factOrNonGroundRule.nonGroundRule.getRule().renameVariables("_" + renamingCounter++);
				renamedBody = rule.getBody();
				if (!rule.getHead().isNormal()) {
					throw oops("NonGroundRule has no normal head.");
				}
				// Unify rule head with literal to justify.
				headAtom = ((DisjunctiveHead) rule.getHead()).disjunctiveAtoms.get(0);
			} else {
				// Create atom and empty rule body out of instance.
				headAtom = new BasicAtom(toJustify.getLiteral().getPredicate(), factOrNonGroundRule.factInstance.terms);
				renamedBody = Collections.emptyList();
			}
			Substitution unifier = Unification.unifyAtoms(toJustify.getLiteral(), headAtom);
			// Skip if unification failed.
			if (unifier == null) {
				continue;
			}
			// a) Check if unifier is more precise than some substitution in the complementSubstitutions.
			for (Substitution complement : toJustify.getComplementSubstitutions()) {
				if (Substitution.isMorePrecise(unifier, complement)) {
					continue eachRule;
				}
			}
			// b) Check if this is already justified higher up.
			if (!inJustificationHigherUp.isEmpty()) {
				// Iterate over rule body and check each literal if it is a specialization of some covered by inJustificationHigherUp.
				for (Literal bodyLiteral : renamedBody) {
					final AtomSet bodyAtomSet = new AtomSet(bodyLiteral, unifier, new LinkedHashSet<>());
					for (AtomSet higherAtomSet : inJustificationHigherUp) {
						if (AtomSet.isSpecialization(bodyAtomSet, higherAtomSet)) {
							continue eachRule;
						}
					}
				}
			}

			// c)
			// Find a negated literal of the rule that is true (extend the unifier if needed).
			Collection<Substitution> matchingSubstitutions = new ArrayList<>();
			for (Literal literal : renamedBody) {
				if (!literal.isNegated()) {
					continue;
				}
				Atom bodyAtom = literal.substitute(unifier);
				// Find more substitutions, consider currentAssignment.
				List<Atom> assignedAtomsOverPredicate = assignedAtoms.get(bodyAtom.getPredicate());
				// Add instances from facts.
				LinkedHashSet<Instance> factsOverPredicate = factsFromProgram.get(bodyAtom.getPredicate());
				if (factsOverPredicate != null) {
					if (assignedAtomsOverPredicate == null) {
						assignedAtomsOverPredicate = new ArrayList<>();
					}
					for (Instance factInstance : factsOverPredicate) {
						assignedAtomsOverPredicate.add(new BasicAtom(bodyAtom.getPredicate(), factInstance.terms));
					}
				}
				if (assignedAtomsOverPredicate == null) {
					continue;
				}
				for (Atom assignedAtom : assignedAtomsOverPredicate) {
					// Note: if an atom has no assigned atomId, then it stems from a fact and hence is true.
					if (atomStore.contains(assignedAtom) &&
						!currentAssignment.get(atomStore.getAtomId(assignedAtom)).getTruth().toBoolean()) {
						// Atom is not assigned true/must-be-true, skip it.
						continue;
					}
					Substitution unified = Substitution.unify(bodyAtom, new Instance(assignedAtom.getTerms()), new Substitution(unifier));
					// Skip instance if it does not unify with the bodyAtom.
					if (unified != null) {
						matchingSubstitutions.add(unified);
						// Record as reason (ground body literal is negated but stored as true).
						reasons.add((Literal) bodyAtom.substitute(unified));
					}
				}
					/*
					// Find more substitutions, consider current workingMemory.
					Collection<Instance> potentiallyMatchingInstances = positiveStorage.getInstancesFromPartiallyGroundAtom(bodyAtom);

					for (Instance instance : potentiallyMatchingInstances) {
						Substitution unified = Substitution.unify(bodyAtom, instance, new Substitution(unifier));
						// Skip instance if it does not unify with the bodyAtom.
						if (unified != null) {
							matchingSubstitutions.add(unified);
							// Record as reason (ground body literal is negated but stored as true).
							reasons.add((Literal) bodyAtom.substitute(unified));
						}
					}
					*/
			}

			// d)
			Set<AtomSet> newHigherJustifications = new LinkedHashSet<>(inJustificationHigherUp);
			newHigherJustifications.add(new AtomSet(toJustify.getLiteral(), unifier, toJustify.getComplementSubstitutions()));
			Set<Substitution> newComplementSubstitutions = new LinkedHashSet<>(toJustify.getComplementSubstitutions());
			newComplementSubstitutions.addAll(matchingSubstitutions);
			List<Literal> positiveBody = new ArrayList<>(renamedBody.size());
			for (Literal literal : renamedBody) {
				if (!literal.isNegated()) {
					positiveBody.add(literal);
				}
			}
			reasons.addAll(explainPosBody(positiveBody, Collections.singleton(unifier), newHigherJustifications, newComplementSubstitutions, currentAssignment));
		}

		return reasons;
	}

	private Collection<Literal> explainPosBody(List<Literal> bodyLiterals, Set<Substitution> unifiers, Set<AtomSet> inJustificationHigherUp, Set<Substitution> complementSubstitutions, Assignment currentAssignment) {
		ArrayList<Literal> reasons = new ArrayList<>();
		if (bodyLiterals.isEmpty()) {
			return reasons;
		}
		int pickedBodyLiteral = 0;
		Literal bodyLiteral = bodyLiterals.get(pickedBodyLiteral);
		for (Substitution unifier : unifiers) {
			Atom substitutedBodyLiteral = bodyLiteral.substitute(unifier);
			Set<Substitution> justifiedInstantiationsOfBodyLiteral = new LinkedHashSet<>();
			// Consider FixedInterpretationLiterals here and evaluate them.
			if (substitutedBodyLiteral instanceof FixedInterpretationLiteral && substitutedBodyLiteral.isGround()) {
				List<Substitution> substitutions = ((FixedInterpretationLiteral) substitutedBodyLiteral).getSubstitutions(new Substitution(unifier));
				justifiedInstantiationsOfBodyLiteral.addAll(substitutions);
			}
			// FIXME: might be better to use the instances from the assignment (and facts).
			// Get currently true instances from workingMemory (also contains facts).
			IndexedInstanceStorage storage = workingMemory.get(bodyLiteral, true);
			Collection<Instance> matchingInstances = storage.getInstancesMatching(substitutedBodyLiteral);
			for (Instance matchingInstance : matchingInstances) {
				// Check if matchingInstance of bodyLiteral is justified.
				int matchingInstanceAtomId = atomStore.add(new BasicAtom(bodyLiteral.getPredicate(), matchingInstance.terms));
				// Atom may be fact hence not occur in the Assignment.
				LinkedHashSet<Instance> factInstances = factsFromProgram.get(bodyLiteral.getPredicate());
				if (factInstances != null && factInstances.contains(matchingInstance) ||
					currentAssignment.getTruth(matchingInstanceAtomId) == ThriceTruth.TRUE) {
					// Construct corresponding substitution and add it.
					Substitution matchingUnifier = Substitution.unify(substitutedBodyLiteral, matchingInstance, new Substitution());
					if (matchingUnifier.toString().equals("_Id_5:id(2)_S_5:c_T_5:0__AS250:T1_5__AS251:S_5")) {
						System.out.println("Marker hit.");
					}
					if (justifiedInstantiationsOfBodyLiteral.contains(matchingUnifier)) {
						System.out.println("Problem.");
					}
					if (!substitutedBodyLiteral.substitute(matchingUnifier).isGround()) {
						System.out.println("Mark hit.");
					}
					if (!substitutedBodyLiteral.substitute(matchingUnifier).isGround()) {
						System.out.println("Mark hit.");
					}
					justifiedInstantiationsOfBodyLiteral.add(matchingUnifier);
				}
			}
			// Search justification for unjustified instances.
			Set<Substitution> newComplementSubstitution = new LinkedHashSet<>(complementSubstitutions);
			newComplementSubstitution.addAll(justifiedInstantiationsOfBodyLiteral);
			reasons.addAll(whyNotMore(
				new AtomSet(substitutedBodyLiteral, unifier, newComplementSubstitution),
				inJustificationHigherUp,
				currentAssignment));

			// Search justification for this rule being unjustified in other body literals.
			bodyLiterals.remove(bodyLiteral);
			reasons.addAll(
				explainPosBody(bodyLiterals, justifiedInstantiationsOfBodyLiteral,
					inJustificationHigherUp, complementSubstitutions, currentAssignment));
		}
		return reasons;
	}
}
