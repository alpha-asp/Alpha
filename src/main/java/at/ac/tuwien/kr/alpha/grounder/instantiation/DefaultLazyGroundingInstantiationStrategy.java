package at.ac.tuwien.kr.alpha.grounder.instantiation;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

import org.apache.commons.lang3.tuple.ImmutablePair;

import at.ac.tuwien.kr.alpha.common.Assignment;
import at.ac.tuwien.kr.alpha.common.AtomStore;
import at.ac.tuwien.kr.alpha.common.atoms.Atom;
import at.ac.tuwien.kr.alpha.common.atoms.BasicAtom;
import at.ac.tuwien.kr.alpha.common.atoms.Literal;
import at.ac.tuwien.kr.alpha.grounder.IndexedInstanceStorage;
import at.ac.tuwien.kr.alpha.grounder.Instance;
import at.ac.tuwien.kr.alpha.grounder.Substitution;
import at.ac.tuwien.kr.alpha.grounder.WorkingMemory;

public class DefaultLazyGroundingInstantiationStrategy implements InstantiationStrategy {

	private WorkingMemory workingMemory;
	private AtomStore atomStore;
	private Assignment currentAssignment;
	private LinkedHashSet<Atom> staleWorkingMemoryEntries;

	public DefaultLazyGroundingInstantiationStrategy(WorkingMemory workingMemory, AtomStore atomStore) {
		this.workingMemory = workingMemory;
		this.atomStore = atomStore;
	}

	@Override
	public AssignmentStatus getTruthForGroundLiteral(Literal groundLiteral) {
		if (groundLiteral.isNegated()) {
			// In the context of this InstantiationStrategy, we consider negated literals
			// to be always true
			return AssignmentStatus.TRUE;
		}
		Atom atom = groundLiteral.getAtom();
		IndexedInstanceStorage instanceStorage = this.workingMemory.get(atom, true);
		if (!instanceStorage.containsInstance(Instance.fromAtom(atom))) {
			return AssignmentStatus.UNASSIGNED;
		} else {
			return this.getTruthValueForAtom(atom);
		}
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
			// TODO check if atom is a fact - if so, we're done here
			truthForCurrentAtom = this.getTruthValueForAtom(atomForCurrentInstance);
			switch (truthForCurrentAtom) {
				case FALSE:
					// Atom is assigned as false - discard it, move on to next instance
					// Discarding from working memory is done in a lazy fashion:
					// Add the atom to the stale set which will be worked off
					// in the next grounder run
					this.staleWorkingMemoryEntries.add(atomForCurrentInstance);
					continue;
				default:
					retVal.add(new ImmutablePair<>(currentInstanceSubstitution, truthForCurrentAtom));
			}
		}
		return retVal;
	}

	private AssignmentStatus getTruthValueForAtom(Atom atom) {
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
