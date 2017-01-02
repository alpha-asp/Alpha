package at.ac.tuwien.kr.alpha.grounder.bridges;

import at.ac.tuwien.kr.alpha.common.*;
import at.ac.tuwien.kr.alpha.grounder.AtomStore;
import at.ac.tuwien.kr.alpha.grounder.IntIdGenerator;
import at.ac.tuwien.kr.alpha.grounder.NonGroundRule;

import java.util.*;

public class HexBridge implements Bridge {
	private List<Integer> processedNoGoods = new ArrayList<>();

	public static native void sendResults(String[][] resultsArray);
	private static native String[][] externalAtomsQuery(String[] trueAtoms, String[] falseAtoms);

	public Collection<NoGood> getNoGoods(ReadableAssignment assignment, AtomStore atomStore) {
		Set<NoGood> noGoods = new HashSet<>();
/*
		List<String[]> externalNogoods = getExternalNoGoods(assignment, atomStore);

		for (String[] externalNogood : externalNogoods) {

			int[] externalNgLiterals = new int[externalNogood.length];

			externalNogood[0] = externalNogood[0].replace("aux_n_", "-aux_r_");

			for (int k = 0; k < externalNogood.length; k++) {
				boolean isNegative = false;

				if (externalNogood[k].charAt(0) == '-') {
					isNegative = true;
					externalNogood[k] = externalNogood[k].substring(1, externalNogood[k].length());
				}

				BasicAtom atom = atomFromLiteral(externalNogood[k]);

				int atomId = atomStore.add(atom);

				if (isNegative) {
					externalNgLiterals[k] = -atomId;
				} else {
					externalNgLiterals[k] = atomId;
				}
			}

			noGoods.add(new NoGood(externalNgLiterals, 0));
		}*/
		return noGoods;
	}

	public Collection<NonGroundRule> getRules(ReadableAssignment assignment, AtomStore atomStore, IntIdGenerator intIdGenerator) {
		Set<NonGroundRule> rules = new HashSet<>();

		List<String[]> externalNogoods = getExternalNoGoods(assignment, atomStore);

		for (String[] externalNogood : externalNogoods) {

			List<Atom> pos = new ArrayList<>();
			List<Atom> neg = new ArrayList<>();
			Atom head = null;

			externalNogood[0] = externalNogood[0].replace("aux_n_", "-aux_r_");

			for (int k = 0; k < externalNogood.length; k++) {
				boolean isNegative = false;

				if (externalNogood[k].charAt(0) == '-') {
					isNegative = true;
					externalNogood[k] = externalNogood[k].substring(1, externalNogood[k].length());
				}

				BasicAtom atom = atomFromLiteral(externalNogood[k]);

				if (isNegative) {
					if (k == 0) {
						head = atom;
					} else {
						neg.add(atom);
					}
				} else {
					pos.add(atom);
				}
			}

			rules.add(new NonGroundRule(
				intIdGenerator.getNextId(),
				pos,
				neg,
				head
			));
		}
		return rules;
	}

	private BasicAtom atomFromLiteral(String literal) {
		String[] lit = literal.split("\\(|,|\\)");
		Term[] terms = new Term[lit.length - 1];

		for (int j = 1; j < lit.length; j++) {
			terms[j - 1] = ConstantTerm.getInstance(lit[j]);
		}

		return new BasicAtom(new BasicPredicate(lit[0], lit.length - 1), false, terms);
	}

	private List<String[]> getExternalNoGoods(ReadableAssignment assignment, AtomStore atomStore) {
		List<String> trueAtoms = new ArrayList<>();
		List<String> falseAtoms = new ArrayList<>();

		for (ListIterator<BasicAtom> it = atomStore.listIterator(); it.hasNext(); ) {
			int id = it.nextIndex();
			BasicAtom basicAtom = it.next();

			if (basicAtom.isInternal() || !assignment.contains(id)) {
				continue;
			}

			List<String> l = assignment.get(id).getTruth().toBoolean() ? trueAtoms : falseAtoms;
			l.add(basicAtom.toString().replace(" ", "").replace("()", ""));
		}

		String[][] externalNgs = externalAtomsQuery(trueAtoms.toArray(new String[trueAtoms.size()]), falseAtoms.toArray(new String[falseAtoms.size()]));
		List<String[]> externalNoGoods = new ArrayList<>();

		for (int i = 0; i < externalNgs.length; i++) {
			if (!processedNoGoods.contains(noGoodHashCode(externalNgs[i]))) {
				processedNoGoods.add(noGoodHashCode(externalNgs[i]));
				externalNoGoods.add(externalNgs[i]);
			}
		}

		return externalNoGoods;
	}

	private int noGoodHashCode(String[] rule) {
		String ruleString = "";

		for (int i = 0; i < rule.length; i++) {
			ruleString += rule[i];
		}

		return ruleString.hashCode();
	}
}
