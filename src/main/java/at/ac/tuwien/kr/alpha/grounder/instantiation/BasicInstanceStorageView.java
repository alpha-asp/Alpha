package at.ac.tuwien.kr.alpha.grounder.instantiation;

import java.util.List;

import at.ac.tuwien.kr.alpha.common.atoms.Atom;
import at.ac.tuwien.kr.alpha.grounder.IndexedInstanceStorage;
import at.ac.tuwien.kr.alpha.grounder.Instance;
import at.ac.tuwien.kr.alpha.grounder.WorkingMemory;

public class BasicInstanceStorageView implements InstanceStorageView {

	private final WorkingMemory workingMemory;

	public BasicInstanceStorageView(WorkingMemory workingMemory) {
		this.workingMemory = workingMemory;
	}

	@Override
	public boolean containsInstanceForAtom(Atom atom) {
		IndexedInstanceStorage instanceStorage = this.workingMemory.get(atom, true);
		return instanceStorage.containsInstance(Instance.fromAtom(atom));
	}

	@Override
	public List<Instance> getInstancesFromPartiallyGroundAtom(Atom atom) {
		IndexedInstanceStorage instanceStorage = this.workingMemory.get(atom, true);
		return instanceStorage.getInstancesFromPartiallyGroundAtom(atom);
	}

}
