package at.ac.tuwien.kr.alpha.grounder.instantiation;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.ImmutablePair;

import at.ac.tuwien.kr.alpha.common.atoms.Literal;
import at.ac.tuwien.kr.alpha.grounder.Instance;
import at.ac.tuwien.kr.alpha.grounder.Substitution;
import at.ac.tuwien.kr.alpha.grounder.WorkingMemory;
import at.ac.tuwien.kr.alpha.grounder.instantiation.DefaultLazyGroundingInstantiationStrategy.AssignmentStatus;

public class CautiousInstantiationStrategy implements InstantiationStrategy {

	private final WorkingMemory workingMemory;

	public CautiousInstantiationStrategy(WorkingMemory workingMemory) {
		this.workingMemory = workingMemory;
	}

	@Override
	public AssignmentStatus getTruthForGroundLiteral(Literal groundLiteral) {
		boolean atomTruth;
		if (!this.workingMemory.contains(groundLiteral.getPredicate())) {
			atomTruth = false;
		} else {
			if (this.workingMemory.get(groundLiteral.getPredicate(), true).containsInstance(Instance.fromAtom(groundLiteral.getAtom()))) {
				atomTruth = true;
			} else {
				atomTruth = false;
			}
		}
		boolean litTruth = groundLiteral.isNegated() ? !atomTruth : atomTruth;
		return litTruth ? AssignmentStatus.TRUE : AssignmentStatus.FALSE;
	}

	@Override
	public List<ImmutablePair<Substitution, AssignmentStatus>> getAcceptedSubstitutions(Literal lit, Substitution partialSubstitution) {
		if (lit.isNegated()) {
			throw new UnsupportedOperationException("Cannot extend substitution for negated literal - literal should be ground already!");
		}
		List<Instance> instances = this.workingMemory.get(lit).getInstancesFromPartiallyGroundAtom(lit.getAtom());
		// could do this in a loop, but let's see what's faster...
		List<ImmutablePair<Substitution, AssignmentStatus>> extendedSubstitutions = instances.stream().parallel()
				.map((instance) -> Substitution.unify(lit.getAtom(), instance, new Substitution(partialSubstitution)))
				.filter((unified) -> unified != null)
				.map((substitution) -> new ImmutablePair<>(substitution, AssignmentStatus.TRUE))
				.collect(Collectors.toList());
		return extendedSubstitutions;
	}

}
