package at.ac.tuwien.kr.alpha.grounder.transformation;

import static at.ac.tuwien.kr.alpha.grounder.transformation.PredicateInternalizer.makePredicatesInternal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import at.ac.tuwien.kr.alpha.common.atoms.AggregateAtom;
import at.ac.tuwien.kr.alpha.common.atoms.AggregateLiteral;
import at.ac.tuwien.kr.alpha.common.atoms.BasicAtom;
import at.ac.tuwien.kr.alpha.common.atoms.CoreLiteral;
import at.ac.tuwien.kr.alpha.common.program.InputProgram;
import at.ac.tuwien.kr.alpha.common.rule.BasicRule;
import at.ac.tuwien.kr.alpha.common.rule.head.NormalHead;
import at.ac.tuwien.kr.alpha.common.terms.CoreConstantTerm;
import at.ac.tuwien.kr.alpha.common.terms.CoreTerm;
import at.ac.tuwien.kr.alpha.common.terms.FunctionTerm;
import at.ac.tuwien.kr.alpha.common.terms.VariableTerm;
import at.ac.tuwien.kr.alpha.grounder.Unifier;
import at.ac.tuwien.kr.alpha.grounder.atoms.EnumerationAtom;
import at.ac.tuwien.kr.alpha.grounder.parser.ProgramParser;

/**
 * Rewrites #sum aggregates into normal rules.
 * Note: Currently only works for a restricted form.
 *
 * Copyright (c) 2018-2020, the Alpha Team.
 */
public class SumNormalization extends ProgramTransformation<InputProgram, InputProgram> {

	private int aggregateCount;
	private ProgramParser parser = new ProgramParser();

	private InputProgram parse(String program) {
		return parser.parse(program);
	}

	@Override
	public InputProgram apply(InputProgram inputProgram) {
		if (!rewritingNecessary(inputProgram)) {
			return inputProgram;
		}
		String summationSubprogram = "interesting_number(R, 1..I1) :- input_number_with_first(R, I, _), I1 = I - 1.\n"
				+ "prefix_subset_sum(R, 0, 0) :- input_number_with_first(R, _, _).\n"
				+ "prefix_subset_sum(R, I, S) :- prefix_subset_sum(R, I1, S), I1 = I - 1, interesting_number(R, I).\n"
				+ "prefix_subset_sum(R, I, SF) :- prefix_subset_sum(R, I1, S), I1 = I - 1, SF = S + F, input_number_with_first(R, I, F),  bound(R, K), SF < K.\n"
				+ "output(R, K) :- bound(R, K), K <= 0."
				+ "output(R, K) :- prefix_subset_sum(R, I1, S), I1 = I - 1, input_number_with_first(R, I, F), bound(R, K), K <= S + F.";

		// Connect/Rewrite every aggregate in each rule.
		List<BasicRule> rewrittenRules = rewriteAggregates(inputProgram.getRules());

		InputProgram.Builder prgBuilder = InputProgram.builder();
		prgBuilder.addFacts(inputProgram.getFacts());
		InputProgram summationEncoding = makePredicatesInternal(new ProgramParser().parse(summationSubprogram));
		prgBuilder.accumulate(summationEncoding);
		prgBuilder.addRules(rewrittenRules);

		// Add enumeration rule that uses the special EnumerationAtom.
		// The enumeration rule is: "input_number_with_first(A, I, F) :- input_with_first(A, X, F), _index(A, X, I)."
		BasicRule tmpEnumRule = makePredicatesInternal(parse("input_number_with_first(A, I, F) :- input_with_first(A, X, F).")).getRules().get(0);
		EnumerationAtom enumerationAtom = new EnumerationAtom(parse("index(A, X, I).").getFacts().get(0).getTerms());
		List<CoreLiteral> enumerationRuleBody = new ArrayList<>(tmpEnumRule.getBody());
		enumerationRuleBody.add(enumerationAtom.toLiteral());
		BasicRule enumerationRule = new BasicRule(tmpEnumRule.getHead(), enumerationRuleBody);
		prgBuilder.addRule(enumerationRule);

		return prgBuilder.build();
	}

	/**
	 * Checks if rewriting of sum aggregates is necessary for the given program, i.e. if such aggregates exist.
	 * 
	 * @param program the program.
	 * @return true if sum aggregates occur, false otherwise.
	 */
	private boolean rewritingNecessary(InputProgram program) {
		for (BasicRule rule : program.getRules()) {
			for (CoreLiteral lit : rule.getBody()) {
				if (lit instanceof AggregateLiteral) {
					AggregateAtom aggregateAtom = ((AggregateLiteral) lit).getAtom();
					if (aggregateAtom.getAggregatefunction() == AggregateAtom.AGGREGATEFUNCTION.SUM) {
						return true;
					}
				}
			}
		}
		return false;
	}

	private List<BasicRule> rewriteAggregates(List<BasicRule> srcRules) {
		List<BasicRule> rewrittenRules = new ArrayList<>();
		for (BasicRule rule : srcRules) {
			rewrittenRules.addAll(rewriteAggregatesInRule(rule));
		}
		return rewrittenRules;
	}

	private List<BasicRule> rewriteAggregatesInRule(BasicRule rule) {

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

		ArrayList<CoreLiteral> aggregateOutputAtoms = new ArrayList<>();
		int aggregatesInRule = 0;	// Only needed for limited rewriting.
		ArrayList<BasicRule> additionalRules = new ArrayList<>();

		List<CoreLiteral> rewrittenBody = new ArrayList<>(rule.getBody());
		for (Iterator<CoreLiteral> iterator = rewrittenBody.iterator(); iterator.hasNext();) {
			CoreLiteral bodyElement = iterator.next();
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
				throw new UnsupportedOperationException("Only limited #count/#sum aggregates without upper bound are currently supported." + "No rule may have more than one aggregate.");
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
			Collection<CoreTerm> globalVariables = CardinalityNormalization.getGlobalVariables(rewrittenBody, aggregateAtom);
			if (globalVariables.isEmpty()) {
				aggregateUnifier.put(VariableTerm.getInstance("AGGREGATE_ID"), CoreConstantTerm.getInstance(aggregateCount));
			} else {
				// In case some variables are not local to the aggregate, add them to the aggregate identifier
				ArrayList<CoreTerm> globalVariableTermlist = new ArrayList<>(globalVariables);
				globalVariableTermlist.add(CoreConstantTerm.getInstance(aggregateCount));
				aggregateUnifier.put(VariableTerm.getInstance("AGGREGATE_ID"), FunctionTerm.getInstance("agg", globalVariableTermlist));
			}
			aggregateUnifier.put(VariableTerm.getInstance("LOWER_BOUND"), aggregateAtom.getLowerBoundTerm());

			// Create new output atom for addition to rule body instead of the aggregate.
			aggregateOutputAtoms.add(aggregateOutputAtom.substitute(aggregateUnifier).toLiteral());

			// Create input to sorting network from aggregate elements.
			for (AggregateAtom.AggregateElement aggregateElement : aggregateAtom.getAggregateElements()) {
				// Prepare element substitution.
				List<CoreTerm> elementTerms = aggregateElement.getElementTerms();
				FunctionTerm elementTuple = FunctionTerm.getInstance("element_tuple", elementTerms);
				Unifier elementUnifier = new Unifier(aggregateUnifier);
				elementUnifier.put(VariableTerm.getInstance("ELEMENT_TUPLE"), elementTuple);
				elementUnifier.put(VariableTerm.getInstance("FIRST_VARIABLE"), elementTuple.getTerms().get(0));

				// Create new rule for input.
				BasicAtom inputHeadAtom = aggregateInputAtom.substitute(elementUnifier);
				List<CoreLiteral> elementLiterals = new ArrayList<>(aggregateElement.getElementLiterals());

				// If there are global variables used inside the aggregate, add original rule body (minus the aggregate itself) to input rule.
				if (!globalVariables.isEmpty()) {
					elementLiterals.addAll(rewrittenBody);
				}
				BasicRule inputRule = new BasicRule(new NormalHead(inputHeadAtom), elementLiterals);
				additionalRules.add(inputRule);
			}

			// Create lower bound for the aggregate.
			BasicAtom lowerBoundHeadAtom = lowerBoundAtom.substitute(aggregateUnifier);
			List<CoreLiteral> lowerBoundBody = rewrittenBody; // Note: this is only correct if no other aggregate occurs in the rule.
			additionalRules.add(new BasicRule(new NormalHead(lowerBoundHeadAtom), lowerBoundBody));
		}
		if (aggregatesInRule > 0) {
			rewrittenBody.addAll(aggregateOutputAtoms);
			additionalRules.add(new BasicRule(rule.getHead(), rewrittenBody));
		} else {
			// Return original rule if no aggregate occurs in it.
			additionalRules.add(rule);
		}
		return additionalRules;
	}
}
