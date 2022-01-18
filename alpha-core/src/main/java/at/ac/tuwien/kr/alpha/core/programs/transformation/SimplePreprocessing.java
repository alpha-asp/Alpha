package at.ac.tuwien.kr.alpha.core.programs.transformation;

import at.ac.tuwien.kr.alpha.api.programs.NormalProgram;
import at.ac.tuwien.kr.alpha.api.programs.atoms.Atom;
import at.ac.tuwien.kr.alpha.api.programs.atoms.BasicAtom;
import at.ac.tuwien.kr.alpha.api.programs.literals.Literal;
import at.ac.tuwien.kr.alpha.api.rules.NormalRule;
import at.ac.tuwien.kr.alpha.api.terms.VariableTerm;
import at.ac.tuwien.kr.alpha.commons.rules.heads.Heads;
import at.ac.tuwien.kr.alpha.core.grounder.Unification;
import at.ac.tuwien.kr.alpha.core.programs.NormalProgramImpl;
import at.ac.tuwien.kr.alpha.core.rules.NormalRuleImpl;

import java.util.*;

/**
 * Simplifies an internal input program by simplifying and deleting redundant rules.
 */

public class SimplePreprocessing extends ProgramTransformation<NormalProgram, NormalProgram> {

	@Override
	public NormalProgram apply(NormalProgram inputProgram) {
		List<NormalRule> srcRules = inputProgram.getRules();
		Set<Atom> facts = new LinkedHashSet<>(inputProgram.getFacts());
		boolean modified = true;
		while (modified) {
			modified = false;
			//implements s0 by using a Set (delete duplicate rules)
			Set<NormalRule> newRules = new LinkedHashSet<>();
			for (NormalRule rule : srcRules) {
				//s9 + s10
				NormalRule simplifiedRule = simplifyRule(rule, srcRules, facts);
				if (simplifiedRule == null) {
					continue;
				}
				else if (simplifiedRule.getBody().isEmpty()) {
					facts.add(simplifiedRule.getHeadAtom());
					modified = true;
					continue;
				} else {
					if(!simplifiedRule.equals(rule)) {
						modified = true;
						rule = simplifiedRule;
					}
				}
				newRules.add(rule);
			}
			srcRules = new LinkedList<>(newRules);
		}
		Set<NormalRule> newRules = new LinkedHashSet<>();
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
		return new NormalProgramImpl(new LinkedList<>(newRules),new LinkedList<>(facts),inputProgram.getInlineDirectives());
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
	 * This method checks rules for bodies containing non-derivable literals or negated literals, that are facts.
	 * implements s9
	 * @param rule the rule to check
	 * @param rules the rules from which the literals should be derivable from
	 * @param facts the facts from which the literals should be derivable from
	 * @return true if the rule is non-derivable, false if it is derivable
	 */
	private boolean checkForNonDerivableLiterals(NormalRule rule, List<NormalRule> rules, Set<Atom> facts) {
		for (Literal literal : rule.getBody()) {
			if (literal.isNegated()) {
				if (facts.contains(literal.getAtom())) {
					return true;
				}
			} else {
				if (isNonDerivable(literal, rules)) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * This method checks for literals from rule bodies, that are already facts or non-derivable.
	 * implements s10
	 * @param rule the rule to check
	 * @param rules the rules from which the literals should be derivable from
	 * @param facts the facts from which the literals should be derivable from
	 * @return the possibly shortened rule or null, if the rule is not derivable
	 */
	private NormalRule simplifyRule(NormalRule rule, List<NormalRule> rules, Set<Atom> facts) {
		for (Literal literal : rule.getBody()) {
			if (literal.isNegated()) {
				if (facts.contains(literal.getAtom())) {
					return null;
				}
				else if (isNonDerivable(literal, rules)) {
					return simplifyRule(removeLiteralFromRule(rule, literal), rules, facts);
				}
			}
			else {
				if (isNonDerivable(literal, rules)) {
					return null;
				}
				else if (facts.contains(literal.getAtom())) {
					return simplifyRule(removeLiteralFromRule(rule, literal), rules, facts);
				}
			}
		}
		return rule;
	}

	/**
	 * This method checks whether a literal is non-derivable, i.e. it is not unifiable with the head atom of a rule.
	 * implements s5 conditions
	 * @param literal the literal to check
	 * @param rules the rules from which the literal should be derivable from
	 * @return true if the literal is not derivable, false otherwise
	 */
	private boolean isNonDerivable(Literal literal, List<NormalRule> rules) {
		for (NormalRule rule : rules) {
			boolean hasSharedVariable = false;
			Set<VariableTerm> leftOccurringVariables = literal.getAtom().getOccurringVariables();
			Set<VariableTerm> rightOccurringVariables = rule.getHeadAtom().getOccurringVariables();
			boolean leftSmaller = leftOccurringVariables.size() < rightOccurringVariables.size();
			Set<VariableTerm> smallerSet = leftSmaller ? leftOccurringVariables : rightOccurringVariables;
			Set<VariableTerm> largerSet = leftSmaller ? rightOccurringVariables : leftOccurringVariables;
			for (VariableTerm variableTerm : smallerSet) {
				if (largerSet.contains(variableTerm)) {
					hasSharedVariable = true;
					break;
				}
			}
			if (!hasSharedVariable) {
				if(Unification.unifyAtoms(literal.getAtom(), rule.getHeadAtom()) != null) {
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * Returns a copy of the rule with the specified literal removed
	 ** @return the rule without the specified literal
	 */
	private NormalRule removeLiteralFromRule(NormalRule rule, Literal literal) {
		BasicAtom headAtom = rule.getHeadAtom();
		List<Literal> newBody = new LinkedList<>();
		for (Literal bodyLiteral : rule.getBody()) {
			if (!literal.equals(bodyLiteral)) {
				newBody.add(bodyLiteral);
			}
		}
		return new NormalRuleImpl(Heads.newNormalHead(headAtom), newBody);
	}
}
