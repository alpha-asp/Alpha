package at.ac.tuwien.kr.alpha.grounder;

import at.ac.tuwien.kr.alpha.common.NoGood;
import at.ac.tuwien.kr.alpha.common.atoms.Atom;
import at.ac.tuwien.kr.alpha.common.program.InternalProgram;
import at.ac.tuwien.kr.alpha.common.rule.InternalRule;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static at.ac.tuwien.kr.alpha.common.Literals.atomOf;

/**
 * Generates completion nogoods if possible.
 */
public class CompletionGenerator {

	/**
	 * Stores a partial completion.
	 */
	private static class PartialCompletion {
		private int remainingBodyLiterals;
		private final int[] generatedBodyLiterals;

		PartialCompletion(int requiredBodyLiterals) {
			remainingBodyLiterals = requiredBodyLiterals;
			generatedBodyLiterals = new int[requiredBodyLiterals];
		}

		void addBodyLiteral(int bodyRepresentingLiteral) {
			remainingBodyLiterals--;
			generatedBodyLiterals[remainingBodyLiterals] = bodyRepresentingLiteral;
		}

		boolean isComplete() {
			return remainingBodyLiterals == 0;
		}

		int[] getGeneratedBodyLiterals() {
			return generatedBodyLiterals;
		}
	}

	private final InternalProgram programAnalysis;
	private final Map<Atom, PartialCompletion> partiallyCompletedCompletions = new HashMap<>();
	private final CompletionConfiguration completionConfiguration;
	private HashSet<Integer> newlyCompletedAtoms = new HashSet<>();

	CompletionGenerator(InternalProgram programAnalysis, CompletionConfiguration completionConfiguration) {
		this.programAnalysis = programAnalysis;
		this.completionConfiguration = completionConfiguration;
	}

	List<NoGood> generateCompletionNoGoods(InternalRule nonGroundRule, Atom groundedHeadAtom, int headLiteral, int bodyRepresentingLiteral) {
		if (!completionConfiguration.isCompletionEnabled()) {
			return Collections.emptyList();
		}
		if (!programAnalysis.isRuleFullyNonProjective(nonGroundRule)) {
			return Collections.emptyList();
		}

		// Rule is fully non-projective at this point.

		Set<InternalRule> rulesDerivingSameHead = programAnalysis.getRulesUnifyingWithGroundHead(groundedHeadAtom);
		if (rulesDerivingSameHead.size() == 1) {
			// Rule has unique-head predicate property.
			newlyCompletedAtoms.add(atomOf(headLiteral));
			return Collections.singletonList(NoGood.support(headLiteral, bodyRepresentingLiteral));
		}
		// Stop if only unique-head completion nogoods are configured.
		if (!completionConfiguration.isEnableCompletionForMultipleRules()) {
			return Collections.emptyList();
		}

		// If multiple rules can derive the same head, add all their respective bodyRepresenting literals to the completion nogood.

		// Check if a partial completion already exists.
		PartialCompletion partialCompletion = partiallyCompletedCompletions.get(groundedHeadAtom);
		if (partialCompletion == null) {
			// Create new partial completion and store it.
			PartialCompletion newPartialCompletion = new PartialCompletion(rulesDerivingSameHead.size());
			newPartialCompletion.addBodyLiteral(bodyRepresentingLiteral);
			partiallyCompletedCompletions.put(groundedHeadAtom, newPartialCompletion);
		} else {
			// Add bodyRepresentingLiteral to partial completion.
			partialCompletion.addBodyLiteral(bodyRepresentingLiteral);
			// Check if partial completion is a full completion now.
			if (partialCompletion.isComplete()) {
				newlyCompletedAtoms.add(atomOf(headLiteral));
				partiallyCompletedCompletions.remove(groundedHeadAtom);
				// Generate completion NoGood.
				return Collections.singletonList(NoGood.support(headLiteral, partialCompletion.getGeneratedBodyLiterals()));
			}
		}
		// No full completion NoGood can be generated yet.
		return Collections.emptyList();
	}

	HashSet<Integer> getNewlyCompletedAtoms() {
		if (newlyCompletedAtoms.isEmpty()) {
			return newlyCompletedAtoms;
		}
		HashSet<Integer> ret = newlyCompletedAtoms;
		newlyCompletedAtoms = new HashSet<>();
		return ret;
	}
}
