package at.ac.tuwien.kr.alpha.grounder.instantiation;

import java.util.LinkedHashSet;
import java.util.Map;

import at.ac.tuwien.kr.alpha.Util;
import at.ac.tuwien.kr.alpha.common.Assignment;
import at.ac.tuwien.kr.alpha.common.AtomStore;
import at.ac.tuwien.kr.alpha.common.Predicate;
import at.ac.tuwien.kr.alpha.common.atoms.Atom;
import at.ac.tuwien.kr.alpha.common.atoms.Literal;
import at.ac.tuwien.kr.alpha.grounder.IndexedInstanceStorage;
import at.ac.tuwien.kr.alpha.grounder.Instance;
import at.ac.tuwien.kr.alpha.grounder.WorkingMemory;

public class DefaultLazyGroundingInstantiationStrategy extends AbstractLiteralInstantiationStrategy {

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

	@Override
	protected Iterable<Instance> computeCandidateInstances(Atom partiallyGroundAtom) {
		IndexedInstanceStorage instanceStorage = this.workingMemory.get(partiallyGroundAtom, true);
		return instanceStorage.getInstancesFromPartiallyGroundAtom(partiallyGroundAtom);
	}

	@Override
	protected AssignmentStatus getAssignmentStatusForAtom(Atom atom) {
		if (this.currentAssignment == null || this.isFact(atom)) {
			// currentAssignment == null is a legitimate case, grounder may be in bootstrap
			// and will call bindNextAtom with
			// null assignment in that case
			// Assumption: since the atom came from working memory and we must be in
			// bootstrap here, we can assume for the atom to be true (or atom is a fact
			// anyway, in which case it's also true)
			return AssignmentStatus.TRUE;
		}
		AssignmentStatus retVal;
		// First, make sure that the Atom in question exists in the AtomStore
		int atomId = this.atomStore.putIfAbsent(atom);
		// newly obtained atomId might be higher than the maximum in the current
		// assignment, grow the assignment
		this.currentAssignment.growForMaxAtomId();
		if (currentAssignment.isAssigned(atomId)) {
			retVal = currentAssignment.getTruth(atomId).toBoolean() ? AssignmentStatus.TRUE : AssignmentStatus.FALSE;
		} else {
			retVal = AssignmentStatus.UNASSIGNED;
		}
		if (retVal == AssignmentStatus.FALSE || retVal == AssignmentStatus.UNASSIGNED) {
			this.staleWorkingMemoryEntries.add(atom);
		}
		return retVal;
	}

	private boolean isFact(Atom atom) {
		if (this.facts.get(atom.getPredicate()) == null) {
			return false;
		} else {
			return this.facts.get(atom.getPredicate()).contains(Instance.fromAtom(atom));
		}
	}

	@Override
	protected AssignmentStatus getAssignmentStatusForNegatedGroundLiteral(Literal negatedGroundLiteral) {
		return AssignmentStatus.TRUE;
	}

	@Override
	protected boolean assignmentStatusAccepted(AssignmentStatus assignmentStatus) {
		switch (assignmentStatus) {
			case TRUE:
			case UNASSIGNED:
				return true;
			case FALSE:
				return false;
			default:
				throw Util.oops("Unsupported AssignmentStatus: " + assignmentStatus);
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

}
