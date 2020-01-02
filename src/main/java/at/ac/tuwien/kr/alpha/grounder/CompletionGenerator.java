package at.ac.tuwien.kr.alpha.grounder;

import at.ac.tuwien.kr.alpha.common.AtomStore;
import at.ac.tuwien.kr.alpha.common.NoGood;
import at.ac.tuwien.kr.alpha.common.atoms.Atom;
import at.ac.tuwien.kr.alpha.grounder.atoms.RuleAtom;
import at.ac.tuwien.kr.alpha.grounder.structure.ProgramAnalysis;

import java.util.*;

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

	private final ProgramAnalysis programAnalysis;
	private final Map<Atom, PartialCompletion> partiallyCompletedCompletions = new HashMap<>();

	public CompletionGenerator(ProgramAnalysis programAnalysis) {
		this.programAnalysis = programAnalysis;
	}

	public List<NoGood> generateCompletionNoGoods(NonGroundRule nonGroundRule, Atom groundedHeadAtom, int headLiteral, int bodyRepresentingLiteral) {
		if (!programAnalysis.isRuleFullyNonProjective(nonGroundRule)) {
			return Collections.emptyList();
		}

		// Rule is fully non-projective at this point.

		LinkedHashSet<NonGroundRule> rulesDerivingSameHead = programAnalysis.getRulesDerivingSameHead().get(nonGroundRule);
		if (rulesDerivingSameHead.size() == 1) {
			// Rule has unique-head predicate property.
			return Collections.singletonList(NoGood.support(headLiteral, bodyRepresentingLiteral));
			// TODO: if non-projective condition is generalized, this may be more complicated here (i.e., generate functionally-dependent variables for ground instantiation)
		}

		// If multiple rules can derive the same head, add all their respective bodyRepresenting literals to the completion nogood.

		// Check if a partial completion already exists.
		PartialCompletion partialCompletion = partiallyCompletedCompletions.get(groundedHeadAtom);
		if (partialCompletion == null) {
			// Create new partial completion and store it.
			PartialCompletion newPartialCompletion = new PartialCompletion(2);
			newPartialCompletion.addBodyLiteral(bodyRepresentingLiteral);
			partiallyCompletedCompletions.put(groundedHeadAtom, newPartialCompletion);
		} else {
			// Add bodyRepresentingLiteral to partial completion.
			partialCompletion.addBodyLiteral(bodyRepresentingLiteral);
			// Check if partial completion is a full completion now.
			if (partialCompletion.isComplete()) {
				// Generate completion NoGood.
				return Collections.singletonList(NoGood.support(headLiteral, partialCompletion.getGeneratedBodyLiterals()));
			}
		}
		// No full completion NoGood can be generated yet.
		return Collections.emptyList();
	}
}
