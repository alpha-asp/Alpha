package at.ac.tuwien.kr.alpha.grounder.instantiation;

import java.util.List;

import at.ac.tuwien.kr.alpha.common.atoms.Atom;
import at.ac.tuwien.kr.alpha.grounder.Instance;

public interface InstanceStorageView {

	boolean containsInstance(Instance instance);

	List<Instance> getInstancesFromPartiallyGroundAtom(Atom atom);

}
