package at.ac.tuwien.kr.alpha.grounder;

import at.ac.tuwien.kr.alpha.Main;
import at.ac.tuwien.kr.alpha.common.*;
import at.ac.tuwien.kr.alpha.grounder.parser.ParsedProgram;
import at.ac.tuwien.kr.alpha.solver.Assignment;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AlphahexGrounder extends NaiveGrounder {
	public AlphahexGrounder(ParsedProgram program) {
		super(program);
	}

	public AlphahexGrounder(ParsedProgram program, java.util.function.Predicate<Predicate> filter) {
		super(program, filter);
	}

	public Map<Integer, NoGood> getNoGoods(Assignment assignment) {
		Map<Integer, NoGood> nogoods = super.getNoGoods();

		int highestAtomId = atomStore.getHighestAtomId().atomId;

		List<String> trueAtoms = new ArrayList<>();
		List<String> falseAtoms = new ArrayList<>();

		for(int i = 1; i <= highestAtomId; i++) {
			BasicAtom basicAtom = atomStore.getBasicAtom(new AtomId(i));

			if (basicAtom.predicate.getPredicateName() != RULE_BODIES_PREDICATE.getPredicateName() &&
				basicAtom.predicate.getPredicateName() != CHOICE_ON_PREDICATE.getPredicateName() &&
				basicAtom.predicate.getPredicateName() != CHOICE_OFF_PREDICATE.getPredicateName()) {
				if (assignment.contains(i)) {
					if (assignment.get(i).getTruth().toBoolean()) {
						trueAtoms.add(basicAtom.toString().replace(" ", "").replace("()", ""));
					} else {
						falseAtoms.add(basicAtom.toString().replace(" ", "").replace("()", ""));
					}
				}
			}
		}

		String[][] externalNogoods = Main.externalAtomsQuery(trueAtoms.toArray(new String[trueAtoms.size()]), falseAtoms.toArray(new String[falseAtoms.size()]));

		for(int i = 0; i < externalNogoods.length; i++) {

			int[] externalNgLiterals = new int[externalNogoods[i].length];

			for(int k = 0; k < externalNogoods[i].length; k++) {
				externalNogoods[i][k] = externalNogoods[i][k].replace("aux_n_","-aux_r_");

				boolean isNegative = false;

				if(externalNogoods[i][k].charAt(0) == '-') {
					isNegative = true;
					externalNogoods[i][k] = externalNogoods[i][k].substring(1, externalNogoods[i][k].length());
				}

				String[] literal = externalNogoods[i][k].split("\\(|,|\\)");
				Term[] terms = new Term[literal.length-1];

				for(int j = 1; j < literal.length; j++) {
					terms[j-1] = ConstantTerm.getInstance(literal[j]);
				}

				BasicAtom atom = new BasicAtom(new BasicPredicate(literal[0],literal.length-1), terms);
				AtomId atomId = atomStore.createAtomId(atom);

				if(isNegative) {
					externalNgLiterals[k] = -atomId.atomId;
				} else {
					externalNgLiterals[k] = atomId.atomId;
				}
			}

			NoGood externalNoGood = new NoGood(externalNgLiterals, 0);

			if (!nogoodIdentifiers.containsKey(externalNoGood)) {
				int noGoodId = super.nogoodIdGenerator.getNextId();
				nogoodIdentifiers.put(externalNoGood, noGoodId);
				nogoods.put(noGoodId, externalNoGood);
			}
		}
		// TODO: move into method
		return nogoods;
	}
}
