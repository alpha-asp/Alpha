package at.ac.tuwien.kr.alpha.grounder.instantiation;

import at.ac.tuwien.kr.alpha.common.atoms.Atom;
import at.ac.tuwien.kr.alpha.common.atoms.Literal;
import at.ac.tuwien.kr.alpha.grounder.Instance;
import at.ac.tuwien.kr.alpha.grounder.WorkingMemory;

/**
 * A very basic implementation of {@link AbstractLiteralInstantiationStrategy}
 * that determines truth of an atom solely based on the atom's presence in a
 * working memory. Atoms that have a corresponding positive instance in the
 * working memory have {@link AssignmentStatus#TRUE}, all other atoms have
 * {@link AssignmentStatus#FALSE}. A negated literal lit is true iff
 * <code>getAssignmentStatusForAtom(lit.getAtom()) == AssignmentStatus.FALSE</code>,
 * false otherwise
 * 
 * Copyright (c) 2020, the Alpha Team.
 */
public class CautiousInstantiationStrategy extends AbstractLiteralInstantiationStrategy {

	private final WorkingMemory workingMemory;

	public CautiousInstantiationStrategy(WorkingMemory workingMemory) {
		this.workingMemory = workingMemory;
	}

	@Override
	protected Iterable<Instance> computeCandidateInstances(Atom partiallyGroundAtom) {
		return this.workingMemory.get(partiallyGroundAtom, true).getInstancesFromPartiallyGroundAtom(partiallyGroundAtom);
	}

	@Override
	protected AssignmentStatus getAssignmentStatusForAtom(Atom atom) {
		return this.workingMemory.get(atom, true).containsInstance(Instance.fromAtom(atom)) ? AssignmentStatus.TRUE : AssignmentStatus.FALSE;
	}

	@Override
	protected AssignmentStatus getAssignmentStatusForNegatedGroundLiteral(Literal negatedGroundLiteral) {
		return this.getAssignmentStatusForAtom(negatedGroundLiteral.getAtom()) == AssignmentStatus.TRUE ? AssignmentStatus.FALSE : AssignmentStatus.TRUE;
	}

	@Override
	protected boolean assignmentStatusAccepted(AssignmentStatus assignmentStatus) {
		if (assignmentStatus == AssignmentStatus.TRUE) {
			return true;
		}
		return false;
	}

}
