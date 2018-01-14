package at.ac.tuwien.kr.alpha.grounder.structure;

import at.ac.tuwien.kr.alpha.common.*;
import at.ac.tuwien.kr.alpha.common.atoms.Atom;
import at.ac.tuwien.kr.alpha.common.atoms.BasicAtom;
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
	public final AtomStore atomStore;
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

	public static class LiteralSet {
		final Literal literal;
		final Set<Substitution> complementSubstitutions;

		public LiteralSet(Literal literal, Set<Substitution> complementSubstitutions) {
			this.literal = literal;
			this.complementSubstitutions = complementSubstitutions;
		}

		/**
		 * Returns true if the left {@link LiteralSet} is a specialization of the right {@link LiteralSet}.
		 * @param left
		 * @param right
		 * @return
		 */
		public static boolean isSpecialization(LiteralSet left, LiteralSet right) {
			if (Unification.unifyAtoms(left.literal, right.literal) == null) {
				return false;
			}
			rightLoop:
			for (Substitution rightComplementSubstitution : right.complementSubstitutions) {
				Atom rightSubstitution = right.literal.substitute(rightComplementSubstitution).renameVariables("_X");
				for (Substitution leftComplementSubstitution : left.complementSubstitutions) {
					Atom leftSubstitution = left.literal.substitute(leftComplementSubstitution).renameVariables("_Y");
					Substitution specializingSubstitution = Unification.isMoreGeneral(rightSubstitution, leftSubstitution);
					if (specializingSubstitution != null) {
						continue rightLoop;
					}
				}
				// Right substitution has no matching left one
				return false;
			}
			return true;
		}

		@Override
		public String toString() {
			return "(" + literal + ", " + complementSubstitutions +	")";
		}
	}

	Map<Predicate, List<Atom>> assignedAtoms;

	Set<Literal> reasonsForUnjustified(Literal literal, Assignment currentAssignment) {
		assignedAtoms = new LinkedHashMap<>();
		for (int i = 1; i <= atomStore.getHighestAtomId(); i++) {
			Assignment.Entry entry = currentAssignment.get(i);
			if (entry == null) {
				continue;
			}
			Atom atom = atomStore.get(i);
			assignedAtoms.putIfAbsent(atom.getPredicate(), new ArrayList<>());
			assignedAtoms.get(atom.getPredicate()).add(atom);
		}
		return whyNotMore(new LiteralSet(literal, new LinkedHashSet<>()), new LinkedHashSet<>(), currentAssignment);
	}

	private int renamingCounter;

	private Set<Literal> whyNotMore(LiteralSet toJustify, Set<LiteralSet> inJustificationHigherUp, Assignment currentAssignment) {
		Set<Literal> reasons = new HashSet<>();

		Predicate predicate = toJustify.literal.getPredicate();
		// Check if literal is a fact.
		LinkedHashSet<Instance> factInstances = factsFromProgram.get(predicate);
		if (factInstances != null) {
			// If literal is ground, simply check containment.
			if (toJustify.literal.isGround() && factInstances.contains(new Instance(toJustify.literal.getTerms()))) {
				// Facts have no reasons, they are always justified.
				return reasons;
			}
			// Literal is non-ground, search for matching instances.
			for (Instance instance : factInstances) {
				if (Substitution.unify(toJustify.literal, instance, new Substitution()) != null) {
					return reasons;
				}
			}
		}
		HashSet<NonGroundRule> rulesDefiningPredicate = getPredicateDefiningRules().get(predicate);
		eachRule:
		for (NonGroundRule nonGroundRule : rulesDefiningPredicate) {
			// First rename all variables in the rule.
			Rule rule = nonGroundRule.getRule().renameVariables("_" + renamingCounter++);
			List<Literal> body = rule.getBody();
			if (!rule.getHead().isNormal()) {
				throw oops("NonGroundRule has no normal head.");
			}
			// Unify rule head with literal to justify.
			Atom headAtom = ((DisjunctiveHead) rule.getHead()).disjunctiveAtoms.get(0);
			Substitution unifier = Unification.unifyAtoms(toJustify.literal, headAtom);
			// Skip if unification failed.
			if (unifier == null) {
				continue;
			}
			// a) Check if unifier is more precise than some substitution in the complementSubstitutions.
			for (Substitution complement : toJustify.complementSubstitutions) {
				if (Substitution.isMorePrecise(unifier, complement)) {
					continue eachRule;
				}
			}
			// b)
			if (!inJustificationHigherUp.isEmpty()) {
				// Iterate over rule body and check each literal if it is a specialization of some covered by inJustificationHigherUp.
				for (Literal bodyLiteral : body) {
					final LiteralSet bodyLiteralSet = new LiteralSet((Literal) bodyLiteral.substitute(unifier), new LinkedHashSet<>());
					for (LiteralSet higherLiteralSet : inJustificationHigherUp) {
						if (LiteralSet.isSpecialization(bodyLiteralSet, higherLiteralSet)) {
							continue eachRule;
						}
					}
				}
			}

			// c)
			// Find a negated literal of the rule that is true (extend the unifier if needed).
			Collection<Substitution> matchingSubstitutions = new ArrayList<>();
			for (Literal literal : body) {
				if (!literal.isNegated()) {
					continue;
				}
				IndexedInstanceStorage positiveStorage = workingMemory.get(literal, true);
				Atom bodyAtom = literal.substitute(unifier);
				if (bodyAtom.isGround()) {
					reasons.add((Literal)bodyAtom);
				} else {
					// Find more substitutions, consider currentAssignment.
					List<Atom> assignedAtomsOverPredicate = assignedAtoms.get(bodyAtom.getPredicate());
					if (assignedAtomsOverPredicate == null) {
						continue;
					}
					for (Atom assignedAtom : assignedAtomsOverPredicate) {
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
			}

			// d)
			Set<LiteralSet> newHigherJustifications = new LinkedHashSet<>(inJustificationHigherUp);
			newHigherJustifications.add(new LiteralSet((Literal) toJustify.literal.substitute(unifier), toJustify.complementSubstitutions));
			Set<Substitution> newComplementSubstitutions = new LinkedHashSet<>(toJustify.complementSubstitutions);
			newComplementSubstitutions.addAll(matchingSubstitutions);
			List<Literal> positiveBody = new ArrayList<>(body.size());
			for (Literal literal : body) {
				if (!literal.isNegated()) {
					positiveBody.add(literal);
				}
			}
			reasons.addAll(explainPosBody(positiveBody, Collections.singleton(unifier), newHigherJustifications, newComplementSubstitutions, currentAssignment));
		}

		return reasons;
	}

	private Collection<Literal> explainPosBody(List<Literal> bodyLiterals, Set<Substitution> unifiers, Set<LiteralSet> inJustificationHigherUp, Set<Substitution> complementSubstitutions, Assignment currentAssignment) {
		ArrayList<Literal> reasons = new ArrayList<>();
		if (bodyLiterals.isEmpty()) {
			return reasons;
		}
		int pickedBodyLiteral = 0;
		Literal bodyLiteral = bodyLiterals.get(pickedBodyLiteral);
		for (Substitution unifier : unifiers) {
			Atom substitutedBodyLiteral = bodyLiteral.substitute(unifier);
			IndexedInstanceStorage storage = workingMemory.get(bodyLiteral, true);
			Collection<Instance> matchingInstances = storage.getInstancesMatching(substitutedBodyLiteral);
			Set<Substitution> justifiedInstantiationsOfBodyLiteral = new LinkedHashSet<>();
			for (Instance matchingInstance : matchingInstances) {
				// Check if matchingInstance of bodyLiteral is justified.
				int matchingInstanceAtomId = atomStore.add(new BasicAtom(bodyLiteral.getPredicate(), matchingInstance.terms));
				// Atom may be fact hence not occur in the Assignment.
				LinkedHashSet<Instance> factInstances = factsFromProgram.get(bodyLiteral.getPredicate());
				if (factInstances != null && factInstances.contains(matchingInstance) ||
					currentAssignment.getTruth(matchingInstanceAtomId) == ThriceTruth.TRUE) {
					// Construct corresponding substitution and add it.
					justifiedInstantiationsOfBodyLiteral.add(Substitution.unify(bodyLiteral, matchingInstance, new Substitution(unifier)));
				}
			}
			// Search justification for unjustified instances.
			Set<Substitution> newComplementSubstitution = new LinkedHashSet<>(complementSubstitutions);
			newComplementSubstitution.addAll(justifiedInstantiationsOfBodyLiteral);
			reasons.addAll(whyNotMore(
				new LiteralSet((Literal) substitutedBodyLiteral, newComplementSubstitution),
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
