package at.ac.tuwien.kr.alpha.grounder.instantiation;

import java.util.List;

import at.ac.tuwien.kr.alpha.common.atoms.Atom;
import at.ac.tuwien.kr.alpha.grounder.IndexedInstanceStorage;
import at.ac.tuwien.kr.alpha.grounder.Instance;
import at.ac.tuwien.kr.alpha.grounder.WorkingMemory;

public class DefaultLazyGroundingInstanceStorageView implements InstanceStorageView {

	private final WorkingMemory workingMemory;

	public DefaultLazyGroundingInstanceStorageView(WorkingMemory workingMemory) {
		this.workingMemory = workingMemory;
	}

	@Override
	public boolean containsInstanceForAtom(Atom atom) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public List<Instance> getInstancesFromPartiallyGroundAtom(Atom atom) {
		IndexedInstanceStorage instanceStorage = this.workingMemory.get(atom, true);
		return instanceStorage.getInstancesFromPartiallyGroundAtom(atom);
	}

}
