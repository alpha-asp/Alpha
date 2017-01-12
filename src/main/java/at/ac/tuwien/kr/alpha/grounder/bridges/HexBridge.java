package at.ac.tuwien.kr.alpha.grounder.bridges;

import at.ac.tuwien.kr.alpha.common.*;
import at.ac.tuwien.kr.alpha.grounder.AtomStore;
import at.ac.tuwien.kr.alpha.grounder.IntIdGenerator;
import at.ac.tuwien.kr.alpha.grounder.NaiveGrounder;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;

public class HexBridge implements Bridge {
	public static native void sendResults(String[][] resultsArray);
	private static native String[][] externalAtomsQuery(String[] trueAtoms, String[] falseAtoms);

	public Collection<NoGood> getNoGoods(ReadableAssignment assignment, AtomStore atomStore, Pair<Map<Integer, Integer>, Map<Integer, Integer>> newChoiceAtoms, IntIdGenerator choiceAtomsGenerator) {
		Set<NoGood> noGoods = new HashSet<>();

		String[][] externalNogoods = getExternalNoGoods(assignment, atomStore);

		for (int m = 0; m < externalNogoods.length; m++) {
			// Collect ground atoms in the body
			ArrayList<Integer> bodyAtomsPositive = new ArrayList<>();
			ArrayList<Integer> bodyAtomsNegative = new ArrayList<>();

			for (int k = 1; k < externalNogoods[m].length; k++) {
				boolean isNegative = false;

				if (externalNogoods[m][k].charAt(0) == '-') {
					isNegative = true;
					externalNogoods[m][k] = externalNogoods[m][k].substring(1, externalNogoods[m][k].length());
				}

				BasicAtom atom = atomFromLiteral(externalNogoods[m][k]);

				int atomId = atomStore.add(atom);

				if (isNegative) {
					bodyAtomsNegative.add(atomId);
				} else {
					bodyAtomsPositive.add(atomId);
				}
			}

			int bodySize = bodyAtomsPositive.size() + bodyAtomsNegative.size();

			if (externalNogoods[m][0].substring(0, 6).equals("aux_r_")) {
				// A constraint is represented by one NoGood.
				int[] constraintLiterals = new int[bodySize + 1];
				int i = 0;
				for (Integer atomId : bodyAtomsPositive) {
					constraintLiterals[i++] = atomId;
				}
				for (Integer atomId : bodyAtomsNegative) {
					constraintLiterals[i++] = -atomId;
				}

				BasicAtom atom = atomFromLiteral(externalNogoods[m][0]);

				constraintLiterals[i++] = atomStore.add(atom);

				NoGood constraintNoGood = new NoGood(constraintLiterals);
				noGoods.add(constraintNoGood);
			} else if (externalNogoods[m][0].substring(0, 6).equals("aux_n_")) {
				// Prepare atom representing the external rule body
				BasicAtom ruleBodyRepresentingPredicate = new BasicAtom(NaiveGrounder.RULE_BODIES_PREDICATE,
					true, ConstantTerm.getInstance("aux"), ConstantTerm.getInstance(uniformString(externalNogoods[m])));

				// Check uniqueness of ground rule by testing whether the body representing atom already has an id
				if (atomStore.contains(ruleBodyRepresentingPredicate)) {
					continue;
				}

				int bodyRepresentingAtomId = atomStore.add(ruleBodyRepresentingPredicate);
				// Prepare head atom
				int headAtomId = atomStore.add(atomFromLiteral(externalNogoods[m][0].replace("aux_n_", "aux_r_")));

				// Create NoGood for body.
				int[] bodyLiterals = new int[bodySize + 1];
				bodyLiterals[0] = -bodyRepresentingAtomId;
				int i = 1;
				for (Integer atomId : bodyAtomsPositive) {
					bodyLiterals[i++] = atomId;
				}
				for (Integer atomId : bodyAtomsNegative) {
					bodyLiterals[i++] = -atomId;
				}
				NoGood ruleBody = new NoGood(bodyLiterals, 0);

				// Generate NoGoods such that the atom representing the body is true iff the body is true.
				for (int j = 1; j < bodyLiterals.length; j++) {
					noGoods.add(new NoGood(bodyRepresentingAtomId, -bodyLiterals[j]));
				}

				// Create NoGood for head.
				NoGood ruleHead = new NoGood(new int[]{-headAtomId, bodyRepresentingAtomId}, 0);

				noGoods.add(ruleBody);
				noGoods.add(ruleHead);

				// Check if the body of the rule contains negation, add choices then
				if (bodyAtomsNegative.size() != 0) {
					Map<Integer, Integer> newChoiceOn = newChoiceAtoms.getLeft();
					Map<Integer, Integer> newChoiceOff = newChoiceAtoms.getRight();
					// Choice is on the body representing atom

					// ChoiceOn if all positive body atoms are satisfied
					int[] choiceOnLiterals = new int[bodyAtomsPositive.size() + 1];
					i = 1;
					for (Integer atomId : bodyAtomsPositive) {
						choiceOnLiterals[i++] = atomId;
					}
					int choiceId = choiceAtomsGenerator.getNextId();
					BasicAtom choiceOnAtom = new BasicAtom(NaiveGrounder.CHOICE_ON_PREDICATE, true, ConstantTerm.getInstance(Integer.toString(choiceId)));
					int choiceOnAtomIdInt = atomStore.add(choiceOnAtom);
					choiceOnLiterals[0] = -choiceOnAtomIdInt;
					// Add corresponding NoGood and ChoiceOn
					noGoods.add(NoGood.headFirst(choiceOnLiterals));        // ChoiceOn and ChoiceOff NoGoods avoid MBT and directly set to true, hence the rule head pointer.
					newChoiceOn.put(bodyRepresentingAtomId, choiceOnAtomIdInt);

					// ChoiceOff if some negative body atom is contradicted
					BasicAtom choiceOffAtom = new BasicAtom(NaiveGrounder.CHOICE_OFF_PREDICATE, true, ConstantTerm.getInstance(Integer.toString(choiceId)));
					int choiceOffAtomIdInt = atomStore.add(choiceOffAtom);
					for (Integer negAtomId : bodyAtomsNegative) {
						// Choice is off if any of the negative atoms is assigned true, hence we add one NoGood for each such atom.
						noGoods.add(NoGood.headFirst(-choiceOffAtomIdInt, negAtomId));
					}
					newChoiceOff.put(bodyRepresentingAtomId, choiceOffAtomIdInt);

				}

			}
		}

		return noGoods;
	}

	private BasicAtom atomFromLiteral(String literal) {
		String[] lit = literal.split("\\(|,|\\)");
		Term[] terms = new Term[lit.length - 1];

		for (int j = 1; j < lit.length; j++) {
			terms[j - 1] = ConstantTerm.getInstance(lit[j]);
		}

		return new BasicAtom(new BasicPredicate(lit[0], lit.length - 1), false, terms);
	}

	private String[][] getExternalNoGoods(ReadableAssignment assignment, AtomStore atomStore) {
		List<String> trueAtoms = new ArrayList<>();
		List<String> falseAtoms = new ArrayList<>();

		for (ListIterator<BasicAtom> it = atomStore.listIterator(); it.hasNext();) {
			int id = it.nextIndex();
			BasicAtom basicAtom = it.next();

			if (basicAtom.isInternal() || !assignment.isAssigned(id)) {
				continue;
			}

			List<String> l = assignment.get(id).getTruth().toBoolean() ? trueAtoms : falseAtoms;
			l.add(basicAtom.toString().replace(" ", "").replace("()", ""));
		}

		return externalAtomsQuery(trueAtoms.toArray(new String[trueAtoms.size()]), falseAtoms.toArray(new String[falseAtoms.size()]));
	}

	private String uniformString(String[] str) {
		String[] strings = str.clone();
		Arrays.sort(strings);
		String string = "";

		for (int i = 0; i < strings.length; i++) {
			string += strings[i];
		}

		return string;
	}
}
