package at.ac.tuwien.kr.alpha.grounder;

import at.ac.tuwien.kr.alpha.AnswerSet;
import at.ac.tuwien.kr.alpha.AnswerSetFilter;
import at.ac.tuwien.kr.alpha.NoGood;
import at.ac.tuwien.kr.alpha.grounder.parser.ParsedProgram;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * Copyright (c) 2016, the Alpha Team.
 */
public class DummyGrounder extends AbstractGrounder {
	public DummyGrounder(ParsedProgram program) {
		super(program);
		initDummy();
	}



	HashMap<Integer, String> atomIdToString;
	byte[] currentTruthValues;

	private void initDummy() {
		// The dummy represents a small ASP program { c :- a, b.  a.  b. }
		currentTruthValues = new byte[]{-2, -1, -1, -1, -1};
		atomIdToString = new HashMap<>();
		atomIdToString.put(1, "a");
		atomIdToString.put(2, "b");
		atomIdToString.put(3, "_br1");
		atomIdToString.put(4, "c");
	}

	@Override
	public void updateAssignment(int[] atomIds, boolean[] truthValues) {
		for (int i = 0; i < atomIds.length; i++) {
			currentTruthValues[atomIds[i]] = truthValues[i] ? (byte)1 : (byte)0;
		}
	}

	@Override
	public void forgetAssignment(int[] atomIds) {
		for (int i = 0; i < atomIds.length; i++) {
			currentTruthValues[atomIds[i]] = -1;
		}
	}

	@Override
	public AnswerSet assignmentToAnswerSet(AnswerSetFilter filter, int[] trueAtoms) {
		// TODO: we need a representation for AnswerSet.

		String result = "{ ";
		for (int i = 0; i < trueAtoms.length; i++) {
			if (i != 0) {
				result += ", ";
			}
			result += atomIdToString.get(trueAtoms[i]);
		}
		result += " }\n";

		// result contains now a string representation of the answer-set, may be useful during debugging.

		return null;
	}

	@Override
	public Map<Integer, NoGood> getNoGoods() {

		// Construct all NoGoods according to { c :- a, b.  a.  b. }

		// { -a }
		NoGood ngFa = new NoGood(1);
		ngFa.noGoodLiterals[0] = -1;
		// id: 11

		// { -b }
		NoGood ngFb = new NoGood(1);
		ngFb.noGoodLiterals[0] = -2;
		// id: 12

		// { -_br1, a, b }
		NoGood ngR1body = new NoGood(3);
		ngR1body.noGoodLiterals[0] = -4;
		ngR1body.noGoodLiterals[1] = 1;
		ngR1body.noGoodLiterals[2] = 2;
		// id: 13

		// { -c, _br1 }
		NoGood ngR1h = new NoGood(2);
		ngR1h.noGoodLiterals[0] = 4;
		ngR1h.noGoodLiterals[1] = -3;
		ngR1h.posHeadLiteral = 1;
		// id: 14

		// Return NoGoods depending on current assignment
		HashMap<Integer, NoGood> returnNoGoods = new HashMap<>();
		if (currentTruthValues[1] == 1 && currentTruthValues[2] == 1) {
			addNoGoodIfNotAlreadyReturned(returnNoGoods, 13, ngR1body);
			addNoGoodIfNotAlreadyReturned(returnNoGoods, 14, ngR1h);
		} else {
			addNoGoodIfNotAlreadyReturned(returnNoGoods, 11, ngFa);
			addNoGoodIfNotAlreadyReturned(returnNoGoods, 12, ngFb);
		}
		return returnNoGoods;
	}

	private HashSet<NoGood> returnedNogoods;
	private void addNoGoodIfNotAlreadyReturned(Map<Integer, NoGood> integerNoGoodMap, Integer idNoGood, NoGood noGood) {
		if (returnedNogoods == null) {
			returnedNogoods = new HashSet<>();
		}
		if (!returnedNogoods.contains(noGood)) {
			integerNoGoodMap.put(idNoGood, noGood);
			returnedNogoods.add(noGood);
		}

	}
}
