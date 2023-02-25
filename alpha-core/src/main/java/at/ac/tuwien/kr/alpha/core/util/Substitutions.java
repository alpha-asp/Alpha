package at.ac.tuwien.kr.alpha.core.util;

import at.ac.tuwien.kr.alpha.api.grounder.Substitution;
import at.ac.tuwien.kr.alpha.api.programs.terms.Term;
import at.ac.tuwien.kr.alpha.api.programs.terms.VariableTerm;
import at.ac.tuwien.kr.alpha.commons.programs.terms.Terms;
import at.ac.tuwien.kr.alpha.commons.substitutions.BasicSubstitution;
import at.ac.tuwien.kr.alpha.core.parser.ProgramPartParser;

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
				if (keyVal.length < 2) {
					System.out.println(assignment);
				}
				Term assignedTerm = PROGRAM_PART_PARSER.parseTerm(keyVal[1]);
				ret.put(variable, assignedTerm);
			}
		}
		return ret;
	}
}
