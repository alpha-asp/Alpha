package at.ac.tuwien.kr.alpha.grounder.instantiation;

import at.ac.tuwien.kr.alpha.common.Assignment;
import at.ac.tuwien.kr.alpha.common.AtomStore;
import at.ac.tuwien.kr.alpha.common.Predicate;
import at.ac.tuwien.kr.alpha.common.atoms.Atom;
import at.ac.tuwien.kr.alpha.common.atoms.BasicAtom;
import at.ac.tuwien.kr.alpha.common.atoms.Literal;
import at.ac.tuwien.kr.alpha.grounder.IndexedInstanceStorage;
import at.ac.tuwien.kr.alpha.grounder.Instance;
import at.ac.tuwien.kr.alpha.grounder.Substitution;
import at.ac.tuwien.kr.alpha.grounder.WorkingMemory;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

public class DefaultLazyGroundingInstantiationStrategy implements InstantiationStrategy {

	private static final Logger LOGGER = LoggerFactory.getLogger(DefaultLazyGroundingInstantiationStrategy.class);

	private WorkingMemory workingMemory;
	private AtomStore atomStore;
	private Assignment currentAssignment;
	private LinkedHashSet<Atom> staleWorkingMemoryEntries;
	private Map<Predicate, LinkedHashSet<Instance>> facts;

	public DefaultLazyGroundingInstantiationStrategy(WorkingMemory workingMemory, AtomStore atomStore,
			Map<Predicate, LinkedHashSet<Instance>> facts) {
		this.workingMemory = workingMemory;
		this.atomStore = atomStore;
		this.facts = facts;
	}

	private boolean isFact(Atom atom) {
		if (this.facts.get(atom.getPredicate()) == null) {
			return false;
		} else {
			return this.facts.get(atom.getPredicate()).contains(Instance.fromAtom(atom));
		}
	}

	@Override
	public AssignmentStatus getTruthForGroundLiteral(Literal groundLiteral) {
		AssignmentStatus retVal;
		if (groundLiteral.isNegated()) {
			// In the context of this InstantiationStrategy, we consider negated literals
			// to be always true
			retVal = AssignmentStatus.TRUE;
		} else {
			Atom atom = groundLiteral.getAtom();
			if (this.isFact(atom)) {
				retVal = AssignmentStatus.TRUE;
			} else {
				// "false" atoms may not exist in WM since they would have been removed in
				// earlier iterations after generating nogoods
				retVal = this.getTruthValueForAtom(atom);
//				IndexedInstanceStorage instanceStorage = this.workingMemory.get(atom, true);
//				if (!instanceStorage.containsInstance(Instance.fromAtom(atom))) {
//					retVal = AssignmentStatus.UNASSIGNED;
//				} else {
//					retVal = this.getTruthValueForAtom(atom);
//				}
			}
		}
		LOGGER.trace("Got assignmentStatus = {} for literal {}", retVal, groundLiteral);
		return retVal;
	}

	// Contract: lit is already substituted with partialSubstitution!
	// Result represents entries for <substitution, literalSatisfied>
	// literalSatisfied is true iff lit is true under current assignment,
	// false if unassigned
	// (Note: substitutions where instances are false will be quietly discarded)
	// TODO negative literals??
	@Override
	public List<ImmutablePair<Substitution, AssignmentStatus>> getAcceptedSubstitutions(Literal lit, Substitution partialSubstitution) {
		List<ImmutablePair<Substitution, AssignmentStatus>> retVal = new ArrayList<>();
		Atom atom = lit.getAtom();

		// First, we get all ground instances from working memory that
		// could potentially fit, according to partialSubstitution.
		IndexedInstanceStorage instanceStorage = this.workingMemory.get(atom, true);
		List<Instance> groundInstances = instanceStorage.getInstancesFromPartiallyGroundAtom(atom);

		// Now filter for only instances unifying with partialSubsitution,
		// i.e. "where all joins work out".
		Substitution currentInstanceSubstitution;
		AssignmentStatus truthForCurrentAtom;
		Atom atomForCurrentInstance;
		for (Instance instance : groundInstances) {
			currentInstanceSubstitution = Substitution.unify(atom, instance, new Substitution(partialSubstitution));
			if (currentInstanceSubstitution == null) {
				// Instance does not unify with partialSubstitution,
				// move on to the next instance
				continue;
			}
			// At this point, we know that the substitution works out.
			// Now check that the resulting Atom is either true or unassigned
			atomForCurrentInstance = new BasicAtom(atom.getPredicate(), atom.getTerms())
					.substitute(currentInstanceSubstitution);
			if (this.isFact(atomForCurrentInstance)) {
				truthForCurrentAtom = AssignmentStatus.TRUE;
			} else {
				truthForCurrentAtom = this.getTruthValueForAtom(atomForCurrentInstance);
			}
			switch (truthForCurrentAtom) {
				case FALSE:
					// Atom is assigned as false - discard it, move on to next instance
					// Discarding from working memory is done in a lazy fashion:
					// Add the atom to the stale set which will be worked off
					// in the next grounder run
					this.staleWorkingMemoryEntries.add(atomForCurrentInstance);
					continue;
				case UNASSIGNED:	// FIXME: check if correctly placed (avoids ignoring of re-added first-unassigned instances).
					this.staleWorkingMemoryEntries.add(atomForCurrentInstance);
					retVal.add(new ImmutablePair<>(currentInstanceSubstitution, truthForCurrentAtom));
					break;
				default:
					retVal.add(new ImmutablePair<>(currentInstanceSubstitution, truthForCurrentAtom));
			}
		}
		return retVal;
	}

	// FIXME we also need to check assignment for atoms that do not exist in working
	// memory... 4 realz?
	// ... is that really necessary, or rather just an artefact from test case
	// NaiveGrounderTest#testGroundingOfRuleSwitchedOffByFalsePositiveBody???
	private AssignmentStatus getTruthValueForAtom(Atom atom) {
		if (this.currentAssignment == null) {
			// legitimate case, grounder may be in bootstrap and will call bindNextAtom with
			// null assignment in that case
			// Assumption: since the atom came from working memory and we must be in
			// bootstrap here, we can assume for the atom to be true
			return AssignmentStatus.TRUE;
		}
		// First, make sure that the Atom in question exists in the AtomStore
		int atomId = this.atomStore.putIfAbsent(atom);
		// newly obtained atomId might be higher than the maximum in the current
		// assignment, grow the assignment
		this.currentAssignment.growForMaxAtomId();
		if (currentAssignment.isAssigned(atomId)) {
			return currentAssignment.getTruth(atomId).toBoolean() ? AssignmentStatus.TRUE : AssignmentStatus.FALSE;
		} else {
			return AssignmentStatus.UNASSIGNED;
		}
	}

	public Assignment getCurrentAssignment() {
		return this.currentAssignment;
	}

	public void setCurrentAssignment(Assignment currentAssignment) {
		this.currentAssignment = currentAssignment;
	}

	public LinkedHashSet<Atom> getStaleWorkingMemoryEntries() {
		return this.staleWorkingMemoryEntries;
	}

	public void setStaleWorkingMemoryEntries(LinkedHashSet<Atom> staleWorkingMemoryEntries) {
		this.staleWorkingMemoryEntries = staleWorkingMemoryEntries;
	}

	// Not to be confused with ThriceTruth, only using this in order to be able to
	// distinguish between atoms that are true (or MBT) and ones that are unassigned
	// NOTE: Could use a Boolean and null for unassigned, but would be weird to read
	// for anyone not intimately familiar with the code
	public enum AssignmentStatus {
		TRUE, FALSE, UNASSIGNED;
	}

}
