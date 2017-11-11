package at.ac.tuwien.kr.alpha.grounder;

import at.ac.tuwien.kr.alpha.common.atoms.Atom;
import at.ac.tuwien.kr.alpha.common.terms.Constant;
import at.ac.tuwien.kr.alpha.common.terms.FunctionTerm;
import at.ac.tuwien.kr.alpha.common.terms.IntervalTerm;
import at.ac.tuwien.kr.alpha.common.terms.Term;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Helper functions to evaluate facts potentially containing intervals.
 * Copyright (c) 2017, the Alpha Team.
 */
public class FactIntervalEvaluator {

	/**
	 * Helper to construct Instances from a fact that may contain intervals.
	 * @param fact the fact potentially containing intervals.
	 * @return all instances stemming from unfolding the intervals.
	 */
	public static List<Instance> constructFactInstances(Atom fact) {
		// Construct instance(s) from the fact.
		int arity = fact.getPredicate().getRank();
		Term[] currentTerms = new Term[arity];
		boolean containsIntervals = false;
		// Check if instance contains intervals at all.
		for (int i = 0; i < arity; i++) {
			Term term = fact.getTerms().get(i);
			currentTerms[i] = term;
			if (term instanceof IntervalTerm) {
				containsIntervals = true;
			} else if (term instanceof FunctionTerm && IntervalTerm.functionTermContainsIntervals((FunctionTerm) term)) {
				containsIntervals = true;
				throw new RuntimeException("Intervals inside function terms in facts are not supported yet. Try turning the fact into a rule.");
			}
		}
		// If fact contains no intervals, simply return the single instance.
		if (!containsIntervals) {
			return Collections.singletonList(new Instance(currentTerms));
		}
		// Fact contains intervals, unroll them all.
		return unrollInstances(currentTerms, 0);
	}

	@SuppressWarnings("unchecked")
	private static List<Instance> unrollInstances(Term[] currentTerms, int currentPosition) {
		if (currentPosition == currentTerms.length) {
			return Collections.singletonList(new Instance(currentTerms));
		}
		Term currentTerm = currentTerms[currentPosition];
		if (currentTerm instanceof IntervalTerm) {
			List<Instance> instances = new ArrayList<>();

			int lower = ((Constant<Integer>)((IntervalTerm) currentTerm).getLowerBound()).getSymbol();
			int upper = ((Constant<Integer>)((IntervalTerm) currentTerm).getUpperBound()).getSymbol();

			for (int i = lower; i <= upper; i++) {
				Term[] clonedTerms = currentTerms.clone();
				clonedTerms[currentPosition] = Constant.getInstance(i);
				instances.addAll(unrollInstances(clonedTerms, currentPosition + 1));
			}
			return instances;
		}
		return unrollInstances(currentTerms, currentPosition + 1);
	}
}
