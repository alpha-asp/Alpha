package at.ac.tuwien.kr.alpha.grounder.transformation;

import at.ac.tuwien.kr.alpha.common.Program;
import at.ac.tuwien.kr.alpha.common.atoms.*;
import at.ac.tuwien.kr.alpha.common.rule.head.impl.DisjunctiveHead;
import at.ac.tuwien.kr.alpha.common.rule.impl.Rule;
import at.ac.tuwien.kr.alpha.common.terms.ConstantTerm;
import at.ac.tuwien.kr.alpha.common.terms.FunctionTerm;
import at.ac.tuwien.kr.alpha.common.terms.Term;
import at.ac.tuwien.kr.alpha.common.terms.VariableTerm;
import at.ac.tuwien.kr.alpha.grounder.Unifier;
import at.ac.tuwien.kr.alpha.grounder.atoms.EnumerationAtom;
import at.ac.tuwien.kr.alpha.grounder.parser.ProgramParser;

import java.util.*;

import static at.ac.tuwien.kr.alpha.grounder.transformation.PredicateInternalizer.makePredicatesInternal;

/**
 * Rewrites #sum aggregates into normal rules.
 * Note: Currently only in a restricted form.
 * Copyright (c) 2018, the Alpha Team.
 */
public class SumNormalization implements ProgramTransformation {

	private int aggregateCount;
	private ProgramParser parser = new ProgramParser();

	private Program parse(String program) {
		return parser.parse(program);
	}

	@Override
	public void transform(Program inputProgram) {
		String summationSubprogram = "interesting_number(R, 1..I1) :- input_number_with_first(R, I, _), I1 = I - 1.\n" +
			"prefix_subset_sum(R, 0, 0) :- input_number_with_first(R, _, _).\n" +
			"prefix_subset_sum(R, I, S) :- prefix_subset_sum(R, I1, S), I1 = I - 1, interesting_number(R, I).\n" +
			"prefix_subset_sum(R, I, SF) :- prefix_subset_sum(R, I1, S), I1 = I - 1, SF = S + F, input_number_with_first(R, I, F),  bound(R, K), SF < K.\n" +
			"output(R, K) :- bound(R, K), K <= 0." +
			"output(R, K) :- prefix_subset_sum(R, I1, S), I1 = I - 1, input_number_with_first(R, I, F), bound(R, K), K <= S + F.";

		// Connect/Rewrite every aggregate in each rule.
		ArrayList<Rule> additionalRules = new ArrayList<>();
		for (Rule rule : inputProgram.getRules()) {
			additionalRules.addAll(rewriteAggregates(rule));
		}
		// Leave program as-is if no aggregates occur.
		if (additionalRules.isEmpty()) {
			return;
		}
		Program summationEncoding = makePredicatesInternal(new ProgramParser().parse(summationSubprogram));
		summationEncoding.accumulate(additionalRules);

		// Add enumeration rule that uses the special EnumerationAtom.
		// The enumeration rule is: "input_number_with_first(A, I, F) :- input_with_first(A, X, F), _index(A, X, I)."
		Rule enumerationRule = makePredicatesInternal(parse("input_number_with_first(A, I, F) :- input_with_first(A, X, F).")).getRules().get(0);
		EnumerationAtom enumerationAtom = new EnumerationAtom(parse("index(A, X, I).").getFacts().get(0).getTerms());
		enumerationRule.getBody().add(enumerationAtom.toLiteral());
		summationEncoding.accumulate(enumerationRule);

		// Add sum encoding to program.
		inputProgram.accumulate(summationEncoding);
	}

	private List<Rule> rewriteAggregates(Rule rule) {

		// Example rewriting/connection:
		// x :- 6 <= #sum {3,a:a; 4,b:b; 5,c:c}.
		// is rewritten to:
		// x :- output(aggregate(1), 6).
		// input_with_first(aggregate(1), element_tuple(3, a), 3) :- a.
		// input_with_first(aggregate(1), element_tuple(4, b), 4) :- b.
		// input_with_first(aggregate(1), element_tuple(5, c), 5) :- c.
		// bound(aggregate(1), 6).

		// Create interface atoms to the aggregate encoding.
		final BasicAtom aggregateOutputAtom = (BasicAtom) makePredicatesInternal(parse(
			"output(aggregate(AGGREGATE_ID), LOWER_BOUND).")).getFacts().get(0);
		final BasicAtom aggregateInputAtom = (BasicAtom) makePredicatesInternal(parse(
			"input_with_first(aggregate(AGGREGATE_ID), ELEMENT_TUPLE, FIRST_VARIABLE).")).getFacts().get(0);
		final BasicAtom lowerBoundAtom = (BasicAtom) makePredicatesInternal(parse(
			"bound(aggregate(AGGREGATE_ID), LOWER_BOUND).")).getFacts().get(0);




		ArrayList<Literal> aggregateOutputAtoms = new ArrayList<>();
		int aggregatesInRule = 0;	// Only needed for limited rewriting.
		ArrayList<Rule> additionalRules = new ArrayList<>();

		for (Iterator<Literal> iterator = rule.getBody().iterator(); iterator.hasNext();) {
			Literal bodyElement = iterator.next();
			// Skip non-aggregates.
			if (!(bodyElement instanceof AggregateLiteral)) {
				continue;
			}
			AggregateLiteral aggregateLiteral = (AggregateLiteral) bodyElement;
			AggregateAtom aggregateAtom = aggregateLiteral.getAtom();

			// Check that aggregate is limited to what we currently can deal with.
			if (aggregateLiteral.isNegated() || aggregateAtom.getUpperBoundOperator() != null
				|| (aggregateAtom.getAggregatefunction() != AggregateAtom.AGGREGATEFUNCTION.COUNT
					&& aggregateAtom.getAggregatefunction() != AggregateAtom.AGGREGATEFUNCTION.SUM)
				|| aggregatesInRule++ > 0) {
				throw new UnsupportedOperationException("Only limited #count/#sum aggregates without upper bound are currently supported." +
					"No rule may have more than one aggregate.");
			}

			// Only treat sum aggregates.
			if (aggregateAtom.getAggregatefunction() != AggregateAtom.AGGREGATEFUNCTION.SUM) {
				continue;
			}
			// Remove aggregate from rule body.
			iterator.remove();

			// Prepare aggregate parameters.
			aggregateCount++;
			Unifier aggregateUnifier = new Unifier();
			Collection<Term> globalVariables = CardinalityNormalization.getGlobalVariables(rule, aggregateAtom);
			if (globalVariables.isEmpty()) {
				aggregateUnifier.put(VariableTerm.getInstance("AGGREGATE_ID"), ConstantTerm.getInstance(aggregateCount));
			} else {
				// In case some variables are not local to the aggregate, add them to the aggregate identifier
				ArrayList<Term> globalVariableTermlist = new ArrayList<>(globalVariables);
				globalVariableTermlist.add(ConstantTerm.getInstance(aggregateCount));
				aggregateUnifier.put(VariableTerm.getInstance("AGGREGATE_ID"), FunctionTerm.getInstance("agg", globalVariableTermlist));
			}
			aggregateUnifier.put(VariableTerm.getInstance("LOWER_BOUND"), aggregateAtom.getLowerBoundTerm());

			// Create new output atom for addition to rule body instead of the aggregate.
			aggregateOutputAtoms.add(aggregateOutputAtom.substitute(aggregateUnifier).toLiteral());

			// Create input to sorting network from aggregate elements.
			for (AggregateAtom.AggregateElement aggregateElement : aggregateAtom.getAggregateElements()) {
				// Prepare element substitution.
				List<Term> elementTerms = aggregateElement.getElementTerms();
				FunctionTerm elementTuple = FunctionTerm.getInstance("element_tuple", elementTerms);
				Unifier elementUnifier = new Unifier(aggregateUnifier);
				elementUnifier.put(VariableTerm.getInstance("ELEMENT_TUPLE"), elementTuple);
				elementUnifier.put(VariableTerm.getInstance("FIRST_VARIABLE"), elementTuple.getTerms().get(0));

				// Create new rule for input.
				BasicAtom inputHeadAtom = aggregateInputAtom.substitute(elementUnifier);
				List<Literal> elementLiterals = new ArrayList<>(aggregateElement.getElementLiterals());

				// If there are global variables used inside the aggregate, add original rule body (minus the aggregate itself) to input rule.
				if (!globalVariables.isEmpty()) {
					elementLiterals.addAll(rule.getBody());
				}
				Rule inputRule = new Rule(new DisjunctiveHead(Collections.singletonList(inputHeadAtom)), elementLiterals);
				additionalRules.add(inputRule);
			}

			// Create lower bound for the aggregate.
			BasicAtom lowerBoundHeadAtom = lowerBoundAtom.substitute(aggregateUnifier);
			List<Literal> lowerBoundBody = rule.getBody();	// Note: this is only correct if no other aggregate occurs in the rule.
			additionalRules.add(new Rule(new DisjunctiveHead(Collections.singletonList(lowerBoundHeadAtom)), lowerBoundBody));

		}
		rule.getBody().addAll(aggregateOutputAtoms);
		return additionalRules;
	}
}
