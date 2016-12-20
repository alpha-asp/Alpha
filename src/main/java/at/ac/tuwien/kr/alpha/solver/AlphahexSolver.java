package at.ac.tuwien.kr.alpha.solver;

import at.ac.tuwien.kr.alpha.common.NoGood;
import at.ac.tuwien.kr.alpha.grounder.AlphahexGrounder;
import at.ac.tuwien.kr.alpha.grounder.Grounder;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Map;

public class AlphahexSolver extends DefaultSolver {

	public AlphahexSolver(Grounder grounder) {
		super(grounder);
	}

	@Override
	protected void obtainNoGoodsFromGrounder() {
		Map<Integer, NoGood> obtained = ((AlphahexGrounder)grounder).getNoGoods(super.assignment);

		if (!obtained.isEmpty()) {
			// Record to detect propagation fixpoint, checking if new NoGoods were reported would be better here.
			super.didChange = true;
		}

		super.store.addAll(obtained);

		// Record choice atoms.
		final Pair<Map<Integer, Integer>, Map<Integer, Integer>> choiceAtoms = grounder.getChoiceAtoms();
		super.choiceOn.putAll(choiceAtoms.getKey());
		super.choiceOff.putAll(choiceAtoms.getValue());
	}
}
