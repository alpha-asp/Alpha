package at.ac.tuwien.kr.alpha.grounder.bridges;

import at.ac.tuwien.kr.alpha.common.Assignment;
import at.ac.tuwien.kr.alpha.common.BasicPredicate;
import at.ac.tuwien.kr.alpha.common.atoms.Atom;
import at.ac.tuwien.kr.alpha.common.atoms.BasicAtom;
import at.ac.tuwien.kr.alpha.common.terms.ConstantTerm;
import at.ac.tuwien.kr.alpha.common.terms.Term;
import at.ac.tuwien.kr.alpha.grounder.AtomStore;
import at.ac.tuwien.kr.alpha.grounder.IntIdGenerator;
import at.ac.tuwien.kr.alpha.grounder.NonGroundRule;

import java.util.*;

public class HexBridge implements Bridge {
	public static native void sendResults(String[][] resultsArray);
	private static native String[][] externalAtomsQuery(String[] trueAtoms, String[] falseAtoms);

	ArrayList<String> importedRules = new ArrayList<>();

	@Override
	public Collection<NonGroundRule> getRules(Assignment assignment, AtomStore atomStore, IntIdGenerator intIdGenerator) {
		Set<NonGroundRule> ioRules = new HashSet<>();

		String[][] externalNogoods = getExternalNoGoods(assignment, atomStore);

		for (String[] stra : externalNogoods) {
			for (String str : stra) {
				System.out.print(str + " ");
			}
			System.out.println();
		}

		for (int m = 0; m < externalNogoods.length; m++) {
			// Collect ground atoms in the body
			List<Atom> bodyAtoms = new ArrayList<>();
			List<Atom> negAtoms = new ArrayList<>();

			String importedRule = "";

			for (int k = 1; k < externalNogoods[m].length; k++) {
				if (externalNogoods[m][k].charAt(0) == '-') {
					externalNogoods[m][k] = "aux_not_" + externalNogoods[m][k].substring(1, externalNogoods[m][k].length());
				}

				BasicAtom atom = atomFromLiteral(externalNogoods[m][k]);
				bodyAtoms.add(atom);

				importedRule += externalNogoods[m][k];
			}

			importedRule += externalNogoods[m][0];

			if (importedRules.contains(importedRule)) {
				continue;
			}

			importedRules.add(importedRule);

			BasicAtom headAtom = atomFromLiteral(externalNogoods[m][0].replace("aux_n_", "aux_r_"));

			NonGroundRule ioRule = new NonGroundRule(intIdGenerator.getNextId(), bodyAtoms, negAtoms, headAtom);
			ioRules.add(ioRule);
		}
		return ioRules;
	}

	private BasicAtom atomFromLiteral(String literal) {
		String[] lit = literal.split("\\(|,|\\)");
		Term[] terms = new Term[lit.length - 1];

		for (int j = 1; j < lit.length; j++) {
			terms[j - 1] = ConstantTerm.getInstance(lit[j]);
		}

		return new BasicAtom(new BasicPredicate(lit[0], lit.length - 1), terms);
	}

	private String[][] getExternalNoGoods(Assignment assignment, AtomStore atomStore) {
		List<String> trueAtoms = new ArrayList<>();
		List<String> falseAtoms = new ArrayList<>();

		for (ListIterator<Atom> it = atomStore.listIterator(); it.hasNext();) {
			int id = it.nextIndex();
			Atom atom = it.next();

			if (atom == null || atom.isInternal() || !assignment.isAssigned(id)) {
				continue;
			}

			if (assignment.get(id).getTruth().toBoolean()) {
				String atomString = atom.toString().replace("()", "");

				String regex = "\\s*(?=([^\"]*\"[^\"]*\")*[^\"]*$)";

				atomString = atomString.replaceAll(regex, "");

				if (atomString.length() < 8 || !atomString.substring(0, 8).equals("aux_ext_")) {
					if (atomString.length() >= 8 && atomString.substring(0, 8).equals("aux_not_")) {
						falseAtoms.add(atomString.substring(8, atomString.length()));
					} else {
						trueAtoms.add(atomString);
					}
				}
			}
		}

		return externalAtomsQuery(trueAtoms.toArray(new String[trueAtoms.size()]), falseAtoms.toArray(new String[falseAtoms.size()]));
	}
}