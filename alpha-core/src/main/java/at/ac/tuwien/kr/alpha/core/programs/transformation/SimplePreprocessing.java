package at.ac.tuwien.kr.alpha.core.programs.transformation;

import at.ac.tuwien.kr.alpha.api.programs.NormalProgram;
import at.ac.tuwien.kr.alpha.api.programs.atoms.Atom;
import at.ac.tuwien.kr.alpha.api.programs.atoms.BasicAtom;
import at.ac.tuwien.kr.alpha.api.programs.literals.Literal;
import at.ac.tuwien.kr.alpha.api.rules.NormalRule;
import at.ac.tuwien.kr.alpha.commons.rules.heads.Heads;
import at.ac.tuwien.kr.alpha.core.grounder.Unification;
import at.ac.tuwien.kr.alpha.core.programs.NormalProgramImpl;
import at.ac.tuwien.kr.alpha.core.rules.NormalRuleImpl;

import at.ac.tuwien.kr.alpha.commons.substitutions.Unifier;
import java.util.*;

/**
 * Simplifies an internal input program by simplifying and deleting redundant rules.
 */

public class SimplePreprocessing extends ProgramTransformation<NormalProgram, NormalProgram> {

	@Override
	public NormalProgram apply(NormalProgram inputProgram) {
		List<NormalRule> srcRules = inputProgram.getRules();
		//Implements s0 by using a Set (delete duplicate rules).
		Set<NormalRule> newRules = new LinkedHashSet<>();
		Set<Atom> facts = new LinkedHashSet<>(inputProgram.getFacts());
		boolean canBePreprocessed = true;
		for (NormalRule rule: srcRules) {
			//s2
			if (checkForConflictingBodyLiterals(rule.getPositiveBody(), rule.getNegativeBody())) {
				continue;
			}
			//s3
			if (checkForHeadInBody(rule.getBody(), rule.getHeadAtom())) {
				continue;
			}
			newRules.add(rule);
		}
		srcRules = new LinkedList<>(newRules);
		//s9 + s10
		while (canBePreprocessed) {
			newRules = new LinkedHashSet<>();
			canBePreprocessed = false;
			for (NormalRule rule : srcRules) {
				SimpleReturn simplifiedRule = simplifyRule(rule, srcRules, facts);
				if (simplifiedRule != null) {
					if (simplifiedRule == SimpleReturn.NO_FIRE) {
						canBePreprocessed = true;
					} else if (simplifiedRule.isFact()) {
						facts.add(simplifiedRule.getRule().getHeadAtom());
						canBePreprocessed = true;
					} else if (simplifiedRule.isModified()) {
						newRules.add(simplifiedRule.getRule());
						canBePreprocessed = true;
					} else {
						newRules.add(rule);
					}
				}
			}
			srcRules = new LinkedList<>(newRules);
		}
		return new NormalProgramImpl(new LinkedList<>(newRules), new LinkedList<>(facts), inputProgram.getInlineDirectives());
	}

	private static class SimpleReturn {
		public final static SimpleReturn NO_FIRE = new SimpleReturn(null, false, false);
		private final NormalRule rule;
		private final boolean isModified;
		private final boolean isFact;

		public SimpleReturn(NormalRule rule, boolean isModified, boolean isFact) {
			this.rule = rule;
			this.isModified = isModified;
			this.isFact = isFact;
		}

		public NormalRule getRule() {
			return rule;
		}

		public boolean isModified() {
			return isModified;
		}

		public boolean isFact() {
			return isFact;
		}
	}

	/**
	 * This method checks if a rule contains a literal in both the positive and the negative body.
	 * implements s2
	 * @return true if the rule contains conflicting literals, false otherwise
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
	 * @return true if the body contains the head atom, false otherwise
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
	 * This method checks for literals from rule bodies, that are already facts or non-derivable.
	 * implements s9 + s10
	 * @param rule the rule to check
	 * @param rules the rules from which the literals should be derivable from
	 * @param facts the facts from which the literals should be derivable from
	 * @return SimpleReturn containing the (possibly modified) rule, a boolean isModified and a boolean isFact. On
	 * non-derivable rules it returns the value NO_FIRE.
	 */
	private SimpleReturn simplifyRule(NormalRule rule, List<NormalRule> rules, Set<Atom> facts) {
		Set<Literal> redundantLiterals = new LinkedHashSet<>();
		for (Literal literal : rule.getBody()) {
			if (literal.toString().startsWith("&") || literal.toString().startsWith("not &")) {
				continue;
			}
			if (literal.isNegated()) {
				if (facts.contains(literal.getAtom())) {
					return SimpleReturn.NO_FIRE;
				}
				if (isNonDerivable(literal.getAtom(), rules, facts)) {
					redundantLiterals.add(literal);
				}
			} else {
				if (facts.contains(literal.getAtom())) {
					redundantLiterals.add(literal);
				} else if (isNonDerivable(literal.getAtom(), rules, facts)) {
					return SimpleReturn.NO_FIRE;
				}
			}
		}
		if (redundantLiterals.isEmpty()) {
			return new SimpleReturn(rule, false, false);
		} else {
			return removeLiteralsFromRule(rule, redundantLiterals);
		}
	}

	/**
	 * This method checks whether an atom is non-derivable, i.e. it is not unifiable with the head atom of a rule.
	 * implements s5 conditions
	 * @param atom the atom to check
	 * @param rules the rules from which the literal should be derivable from
	 * @return true if the literal is not derivable, false otherwise
	 */
	private boolean isNonDerivable(Atom atom, List<NormalRule> rules, Set<Atom> facts) {
		Atom tempAtom = atom.renameVariables("Prep");
		for (NormalRule rule : rules) {
			if (!rule.isConstraint()) {
				if (Unification.unifyAtoms(rule.getHeadAtom(), tempAtom) != null) {
					return false;
				}
			}
		}
		Unifier uni;
		for (Atom fact:facts) {
			if ((uni = Unification.instantiate(tempAtom, fact)) != null) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Returns a copy of the rule with the specified literals removed
	 ** @return the rule without the specified literals
	 */
	private SimpleReturn removeLiteralsFromRule(NormalRule rule, Set<Literal> literals) {
		BasicAtom headAtom = rule.getHeadAtom();
		Set<Literal> newBody = new LinkedHashSet<>();
		for (Literal bodyLiteral : rule.getBody()) {
			if (!literals.contains(bodyLiteral)) {
				newBody.add(bodyLiteral);
			}
		}
		NormalRuleImpl newRule = new NormalRuleImpl(headAtom != null ? Heads.newNormalHead(headAtom) : null, new LinkedList<>(newBody));
		if (newRule.isConstraint() && newBody.isEmpty()) {
			return SimpleReturn.NO_FIRE;
		}
		return new SimpleReturn(newRule, !literals.isEmpty(), newBody.isEmpty() && !newRule.isConstraint());
	}
}
