package at.ac.tuwien.kr.alpha.grounder.instantiation;

import java.util.List;

import at.ac.tuwien.kr.alpha.common.atoms.Atom;
import at.ac.tuwien.kr.alpha.grounder.IndexedInstanceStorage;
import at.ac.tuwien.kr.alpha.grounder.Instance;

public class BasicInstanceStorageView implements InstanceStorageView {

	private final IndexedInstanceStorage instanceStorage;

	public BasicInstanceStorageView(IndexedInstanceStorage instanceStorage) {
		this.instanceStorage = instanceStorage;
	}

	@Override
	public boolean containsInstance(Instance instance) {
		return this.instanceStorage.containsInstance(instance);
	}

	@Override
	public List<Instance> getInstancesFromPartiallyGroundAtom(Atom atom) {
		return this.instanceStorage.getInstancesFromPartiallyGroundAtom(atom);
	}

}
