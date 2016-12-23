package at.ac.tuwien.kr.alpha.grounder.bridges;

import at.ac.tuwien.kr.alpha.common.*;
import at.ac.tuwien.kr.alpha.grounder.AtomStore;

import java.util.*;

public class HexBridge implements Bridge {
	public static native void sendResults(String[][] resultsArray);
	private static native String[][] externalAtomsQuery(String[] trueAtoms, String[] falseAtoms);

	public Collection<NoGood> getNoGoods(ImmutableAssignment assignment, AtomStore atomStore) {
		Set<NoGood> nogoods = new HashSet<>();

		List<String> trueAtoms = new ArrayList<>();
		List<String> falseAtoms = new ArrayList<>();

		for (ListIterator<BasicAtom> it = atomStore.listIterator(); it.hasNext();) {
			int id = it.nextIndex();
			BasicAtom basicAtom = it.next();

			if (basicAtom.isInternal() || !assignment.contains(id)) {
				continue;
			}

			List<String> l = assignment.get(id).getTruth().toBoolean() ? trueAtoms : falseAtoms;
			l.add(basicAtom.toString().replace(" ", "").replace("()", ""));
		}

		String[][] externalNogoods = externalAtomsQuery(trueAtoms.toArray(new String[trueAtoms.size()]), falseAtoms.toArray(new String[falseAtoms.size()]));

		for (int i = 0; i < externalNogoods.length; i++) {

			int[] externalNgLiterals = new int[externalNogoods[i].length];

			for (int k = 0; k < externalNogoods[i].length; k++) {
				externalNogoods[i][k] = externalNogoods[i][k].replace("aux_n_", "-aux_r_");

				boolean isNegative = false;

				if (externalNogoods[i][k].charAt(0) == '-') {
					isNegative = true;
					externalNogoods[i][k] = externalNogoods[i][k].substring(1, externalNogoods[i][k].length());
				}

				String[] literal = externalNogoods[i][k].split("\\(|,|\\)");
				Term[] terms = new Term[literal.length - 1];

				for (int j = 1; j < literal.length; j++) {
					terms[j - 1] = ConstantTerm.getInstance(literal[j]);
				}

				BasicAtom atom = new BasicAtom(new BasicPredicate(literal[0], literal.length - 1), false, terms);
				int atomId = atomStore.add(atom);

				if (isNegative) {
					externalNgLiterals[k] = -atomId;
				} else {
					externalNgLiterals[k] = atomId;
				}
			}

			nogoods.add(new NoGood(externalNgLiterals, 0));
		}
		return nogoods;
	}
}
