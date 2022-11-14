package at.ac.tuwien.kr.alpha.core.util;

import at.ac.tuwien.kr.alpha.api.grounder.Substitution;
import at.ac.tuwien.kr.alpha.api.terms.ConstantTerm;
import at.ac.tuwien.kr.alpha.api.terms.Term;
import at.ac.tuwien.kr.alpha.api.terms.VariableTerm;
import at.ac.tuwien.kr.alpha.commons.substitutions.BasicSubstitution;
import at.ac.tuwien.kr.alpha.commons.terms.Terms;
import at.ac.tuwien.kr.alpha.core.atoms.RuleAtom;
import at.ac.tuwien.kr.alpha.core.grounder.ProgramAnalyzingGrounder;
import at.ac.tuwien.kr.alpha.core.parser.ProgramPartParser;
import at.ac.tuwien.kr.alpha.core.rules.CompiledRule;

public final class Substitutions {

	private static final ProgramPartParser PROGRAM_PART_PARSER = new ProgramPartParser();

	private Substitutions() {
		throw new AssertionError("Cannot instantiate utility class!");
	}

	public static Substitution fromString(String str) {
		String bare = str.substring(1, str.length() - 1);
		String[] assignments = bare.split(",");
		BasicSubstitution ret = new BasicSubstitution();
		for (String assignment : assignments) {
			if (!assignment.equals("")) {
				String[] keyVal = assignment.split("->");
				VariableTerm variable = Terms.newVariable(keyVal[0]);
				Term assignedTerm = PROGRAM_PART_PARSER.parseTerm(keyVal[1]);
				ret.put(variable, assignedTerm);
			}
		}
		return ret;
	}

	public static Substitution getSubstitutionFromRuleAtom(RuleAtom atom) {
		String substitution = (String) ((ConstantTerm<?>)atom.getTerms().get(1)).getObject();
		return fromString(substitution);
	}

	public static CompiledRule getNonGroundRuleFromRuleAtom(RuleAtom atom, ProgramAnalyzingGrounder grounder) {
		String ruleId = (String) ((ConstantTerm<?>)atom.getTerms().get(0)).getObject();
		return grounder.getNonGroundRule(Integer.parseInt(ruleId));
	}
}
