package at.ac.tuwien.kr.alpha.core.solver;

import at.ac.tuwien.kr.alpha.core.common.AtomStore;
import at.ac.tuwien.kr.alpha.core.common.NoGood;

import java.util.LinkedList;
import java.util.List;

/**
 * A store for nogoods that allows extraction of nogoods also after changes in atom ids.
 */
public class AtomizedNoGoodStore {
	private final AtomStore atomStore;
	private final List<AtomizedNoGood> atomizedNoGoods;

	public AtomizedNoGoodStore(AtomStore atomStore) {
		this.atomStore = atomStore;
		this.atomizedNoGoods = new LinkedList<>();
	}

	public void add(NoGood noGood) {
		atomizedNoGoods.add(new AtomizedNoGood(noGood, atomStore));
	}

	public List<NoGood> getNoGoods() {
		List<NoGood> noGoods = new LinkedList<>();
		for (AtomizedNoGood atomizedNoGood : atomizedNoGoods) {
			noGoods.add(atomizedNoGood.deatomize(atomStore));
		}
		return noGoods;
	}
}
