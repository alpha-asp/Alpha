package at.ac.tuwien.kr.alpha.core.programs.transformation;

import at.ac.tuwien.kr.alpha.api.programs.Program;
import at.ac.tuwien.kr.alpha.api.programs.atoms.Atom;
import at.ac.tuwien.kr.alpha.api.programs.literals.Literal;
import at.ac.tuwien.kr.alpha.api.rules.Rule;
import at.ac.tuwien.kr.alpha.api.rules.heads.NormalHead;
import at.ac.tuwien.kr.alpha.core.grounder.Unification;
import at.ac.tuwien.kr.alpha.core.programs.AnalyzedProgram;
import at.ac.tuwien.kr.alpha.core.programs.CompiledProgram;
import at.ac.tuwien.kr.alpha.core.programs.InputProgram;
import at.ac.tuwien.kr.alpha.core.programs.InternalProgram;
import at.ac.tuwien.kr.alpha.core.rules.CompiledRule;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Simplifies an internal input program by simplifying and deleting redundant rules.
 */

public class SimplePreprocessing extends ProgramTransformation<CompiledProgram, CompiledProgram> {

	@Override
	public CompiledProgram apply(CompiledProgram inputProgram) {
		List<CompiledRule> srcRules = new ArrayList<>(inputProgram.getRules());
		List<CompiledRule> transformedRules = new ArrayList<>();

		for (CompiledRule rule : srcRules) {
			boolean redundantRule = false;

			Atom headAtom = rule.getHead().getAtom();
			Set<Literal> body = rule.getBody();
			Set<Literal> positiveBody = rule.getPositiveBody();
			Set<Literal> negativeBody = rule.getNegativeBody();
			CompiledRule simplifiedRule = null;

			//implements s0: delete duplicate rules
			if (transformedRules.contains(rule)) {
				redundantRule = true;
			}
			//implements s2
			if (!redundantRule && checkForConflictingBodyLiterals(positiveBody, negativeBody)) {
				redundantRule = true;
			}
			//implements s3
			if (!redundantRule && checkForHeadInBody(body, headAtom)) {
				redundantRule = true;
			}
			//implements s9
			if (!redundantRule && checkForUnreachableLiterals(srcRules, rule, inputProgram.getFacts())) {
				redundantRule = true;
			}
			//implements s10
			simplifiedRule = checkForSimplifiableRule(srcRules, rule, inputProgram.getFacts());
			if (simplifiedRule != null) {
				rule = simplifiedRule;
			}

			if(!redundantRule) {
				transformedRules.add(rule);
			}
		}

		if(inputProgram.getClass() == InternalProgram.class) {
			return new InternalProgram(transformedRules, inputProgram.getFacts());
		}
		else {
			return new AnalyzedProgram(transformedRules, inputProgram.getFacts());
		}
	}

	/**
	 * This method checks if a rule contains a literal in both the positive and the negative body.
	 * implements s2
	 */
	private boolean checkForConflictingBodyLiterals(Set<Literal> positiveBody, Set<Literal> negativeBody) {
		for (Literal positiveLiteral : positiveBody) {
			if (negativeBody.contains(positiveLiteral.negate())) {
				return true;
			}
		}
		return false;
	}

	/**
	 * This method checks if the head atom occurs in the rule's body.
	 * implements s3
	 */
	private boolean checkForHeadInBody(Set<Literal> body, Atom headAtom) {
		for (Literal literal : body) {
			if (literal.getAtom().equals(headAtom)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * This method checks for rules with bodies containing not derivable literals or negated literals, that are facts.
	 * implements s9
	 */
	private boolean checkForUnreachableLiterals (List<CompiledRule> rules, CompiledRule rule, List<Atom> facts) {
		for (Literal literal : rule.getBody()) {
			if (literal.isNegated()) {
				if (!facts.contains(literal.getAtom())) {
					return false;
				}
			} else {
				if (isDerivable(literal, rules)) {
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * This method checks for literals from rule bodies, that are already facts (when positive)
	 * or not derivable (when negated).
	 * implements s10
	 */
	private CompiledRule checkForSimplifiableRule (List<CompiledRule> rules, CompiledRule rule, List<Atom> facts) {
		for (Literal literal : rule.getBody()) {
			if (literal.isNegated()) {
				if (facts.contains(literal.getAtom())) {
					return null;
				} else return rule.returnCopyWithoutLiteral(literal);
			} else {
				if (!isDerivable(literal, rules)) {
					return rule.returnCopyWithoutLiteral(literal);
				} else return null;
			}
		}
		return null;
	}

	/**
	 * This method checks whether a literal is derivable, ie. it is unifiable with the head atom of a rule.
	 * implements s5 conditions
	 */
	private boolean isDerivable(Literal literal, List<CompiledRule> rules){
		for (Rule<? extends NormalHead> rule : rules) {
			if (Unification.unifyAtoms(literal.getAtom(), rule.getHead().getAtom()) != null) {
				return true;
			}
		}
		return false;
	}
}
