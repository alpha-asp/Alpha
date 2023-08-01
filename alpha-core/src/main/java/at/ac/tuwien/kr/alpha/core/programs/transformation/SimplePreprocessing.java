package at.ac.tuwien.kr.alpha.core.programs.transformation;

import at.ac.tuwien.kr.alpha.api.programs.NormalProgram;
import at.ac.tuwien.kr.alpha.api.programs.atoms.Atom;
import at.ac.tuwien.kr.alpha.api.programs.atoms.BasicAtom;
import at.ac.tuwien.kr.alpha.api.programs.literals.BasicLiteral;
import at.ac.tuwien.kr.alpha.api.programs.literals.Literal;
import at.ac.tuwien.kr.alpha.api.rules.NormalRule;
import at.ac.tuwien.kr.alpha.api.rules.Rule;
import at.ac.tuwien.kr.alpha.commons.rules.heads.Heads;
import at.ac.tuwien.kr.alpha.core.grounder.Unification;
import at.ac.tuwien.kr.alpha.core.programs.NormalProgramImpl;
import at.ac.tuwien.kr.alpha.core.rules.NormalRuleImpl;

import java.util.*;

import static java.util.stream.Collectors.toList;

/**
 * Simplifies the input program by deleting redundant literals and rules, as well as identifying rule heads that can
 * always be derived from facts and adding them to the known facts. The approach is adopted from preprocessing
 * techniques employed by traditional ground ASP solvers, as seen in:
 * @see <a href="https://doi.org/10.3233/978-1-58603-891-5-15">doi:10.3233/978-1-58603-891-5-15</a>
 */
public class SimplePreprocessing extends ProgramTransformation<NormalProgram, NormalProgram> {


	/**
	 * Evaluates all rules of the input program and tries to simplify them by applying a number of rule transformations.
	 * @param inputProgram a normalized ASP program to be preprocessed.
	 * @return the preprocessed program.
	 */
	@Override
	public NormalProgram apply(NormalProgram inputProgram) {
		List<NormalRule> srcRules = inputProgram.getRules();
		Set<NormalRule> newRules = new LinkedHashSet<>();
		Set<Atom> facts = new LinkedHashSet<>(inputProgram.getFacts());

		// Check if the rules are redundant or will never fire, without taking other rules into consideration.
		for (NormalRule rule: srcRules) {
			if (facts.contains(rule.getHeadAtom()) || containsConflictingBodyLiterals(rule) || containsHeadInPositiveBody(rule)) {
				continue;
			}
			if (containsHeadInNegativeBody(rule)) {
				newRules.add(new NormalRuleImpl(null, new ArrayList<>(rule.getBody())));
			}
			newRules.add(rule);
		}
		srcRules = new LinkedList<>(newRules);

		// Analyze every rule in regard to the other rules and facts and if applicable, simplify or remove it. Repeat
		// until a fixpoint is reached.
		boolean canBePreprocessed = true;
		while (canBePreprocessed) {
			newRules = new LinkedHashSet<>();
			canBePreprocessed = false;
			for (NormalRule rule : srcRules) {
				RuleEvaluation eval = evaluateRule(rule, srcRules, facts);
				if (eval == RuleEvaluation.NO_FIRE) {
					canBePreprocessed = true;
				} else if (eval.isFact()) {
					facts.add(eval.getRule().getHeadAtom());
					canBePreprocessed = true;
				} else if (eval.isModified()) {
					newRules.add(eval.getRule());
					canBePreprocessed = true;
				} else {
					newRules.add(rule);
				}
			}
			srcRules = new LinkedList<>(newRules);
		}
		return new NormalProgramImpl(new LinkedList<>(newRules), new LinkedList<>(facts), inputProgram.getInlineDirectives());
	}

	private boolean containsConflictingBodyLiterals(NormalRule rule) {
		for (Literal positiveLiteral : rule.getPositiveBody()) {
			if (rule.getNegativeBody().contains(positiveLiteral.negate())) {
				return true;
			}
		}
		return false;
	}

	private boolean containsHeadInPositiveBody(NormalRule rule) {
		Atom headAtom = rule.getHeadAtom();
		for (Literal literal : rule.getPositiveBody()) {
			if (literal.getAtom().equals(headAtom)) {
				return true;
			}
		}
		return false;
	}

	private boolean containsHeadInNegativeBody(NormalRule rule) {
		Atom headAtom = rule.getHeadAtom();
		for (Literal literal : rule.getNegativeBody()) {
			if (literal.getAtom().equals(headAtom)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Evaluates and attempts to simplify a rule by looking for always-true and always-false literals.
	 * @param rule the rule to be evaluated.
	 * @param rules the rules from which literals could be derived from.
	 * @param facts the facts from which literals could be derived from.
	 * @return The ({@link RuleEvaluation}) contains the possibly simplified rule and indicates whether it was
	 * modified, is a fact or will never fire.
	 */
	private RuleEvaluation evaluateRule(NormalRule rule, List<NormalRule> rules, Set<Atom> facts) {
		// Collect all literals that can be removed from the rule here.
		Set<Literal> redundantLiterals = new LinkedHashSet<>();

		// Check if body literals can be derived from facts or other rules. If a body literal is not derivable, the
		// rule can never fire. If a literal is already proven within the program context, the rule will be simplified
		// by removing the literal.
		List<Literal> basicBodyLiterals = rule.getBody().stream().filter(BasicLiteral.class::isInstance).collect(toList());
		for (Literal literal : basicBodyLiterals) {
			if (literal.isNegated() && facts.contains(literal.getAtom())
					|| !literal.isNegated() && isNonDerivable(literal.getAtom(), rules, facts)) {
				return RuleEvaluation.NO_FIRE;
			}
			if (literal.isNegated() && isNonDerivable(literal.getAtom(), rules, facts)
					|| !literal.isNegated() && facts.contains(literal.getAtom())) {
				redundantLiterals.add(literal);
			}
		}

		// Checks if a constraint has the same body, meaning the had of the rule cannot be derived by the rule.
		List<NormalRule> constraints = rules.stream().filter(Rule::isConstraint).collect(toList());
		for (NormalRule constraint : constraints) {
			if (!rule.isConstraint() && rule.getBody().equals(constraint.getBody())) {
				return RuleEvaluation.NO_FIRE;
			}
		}

		if (redundantLiterals.isEmpty()) {
			return new RuleEvaluation(rule, false, false);
		} else {
			return removeLiteralsFromRule(rule, redundantLiterals);
		}
	}

	// Checks if an atom cannot be derived from a rule or a fact.
	private boolean isNonDerivable(Atom atom, List<NormalRule> rules, Set<Atom> facts) {
		Atom tempAtom = atom.renameVariables("Prep");
		for (NormalRule rule : rules) {
			if (!rule.isConstraint() && Unification.unifyAtoms(rule.getHeadAtom(), tempAtom) != null) {
				return false;
			}
		}
		for (Atom fact:facts) {
			if ((Unification.instantiate(tempAtom, fact)) != null) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Removes a set of given literals from the rule body.
	 * @param rule the rule from which literals should be removed.
	 * @param literals The literals to remove.
	 * @return the resulting rule or fact.
	 */
	private RuleEvaluation removeLiteralsFromRule(NormalRule rule, Set<Literal> literals) {
		BasicAtom headAtom = rule.getHeadAtom();
		Set<Literal> newBody = new LinkedHashSet<>();
		for (Literal bodyLiteral : rule.getBody()) {
			if (!literals.contains(bodyLiteral)) {
				newBody.add(bodyLiteral);
			}
		}
		NormalRuleImpl newRule = new NormalRuleImpl(headAtom != null ? Heads.newNormalHead(headAtom) : null, new LinkedList<>(newBody));
		if (newRule.isConstraint() && newBody.isEmpty()) {
			return RuleEvaluation.NO_FIRE;
		}
		return new RuleEvaluation(newRule, !literals.isEmpty(), newBody.isEmpty() && !newRule.isConstraint());
	}

	/**
	 * Internal helper class containing the possibly simplified rule and indicates whether it could
	 * or could not be simplified, is a fact or will never fire.
	 */
	private static class RuleEvaluation {
		public static final RuleEvaluation NO_FIRE = new RuleEvaluation(null, false, false);
		private final NormalRule rule;
		private final boolean isModified;
		private final boolean isFact;

		public RuleEvaluation(NormalRule rule, boolean isModified, boolean isFact) {
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
}
