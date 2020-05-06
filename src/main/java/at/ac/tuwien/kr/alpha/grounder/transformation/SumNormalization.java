package at.ac.tuwien.kr.alpha.grounder.transformation;

import static at.ac.tuwien.kr.alpha.grounder.transformation.PredicateInternalizer.makePredicatesInternal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import at.ac.tuwien.kr.alpha.common.atoms.AggregateAtom;
import at.ac.tuwien.kr.alpha.common.atoms.AggregateLiteral;
import at.ac.tuwien.kr.alpha.common.atoms.BasicAtom;
import at.ac.tuwien.kr.alpha.common.atoms.Literal;
import at.ac.tuwien.kr.alpha.common.program.InputProgram;
import at.ac.tuwien.kr.alpha.common.rule.BasicRule;
import at.ac.tuwien.kr.alpha.common.rule.head.DisjunctiveHead;
import at.ac.tuwien.kr.alpha.common.rule.head.NormalHead;
import at.ac.tuwien.kr.alpha.common.terms.ConstantTerm;
import at.ac.tuwien.kr.alpha.common.terms.FunctionTerm;
import at.ac.tuwien.kr.alpha.common.terms.Term;
import at.ac.tuwien.kr.alpha.common.terms.VariableTerm;
import at.ac.tuwien.kr.alpha.grounder.Unifier;
import at.ac.tuwien.kr.alpha.grounder.atoms.EnumerationAtom;
import at.ac.tuwien.kr.alpha.grounder.parser.ProgramParser;

/**
 * Rewrites #sum aggregates into normal rules. Note: Currently only in a restricted form. Copyright (c) 2018-2019, the Alpha Team.
 */
public class SumNormalization extends ProgramTransformation<InputProgram, InputProgram> {

	private int aggregateCount;
	private ProgramParser parser = new ProgramParser();

	private InputProgram parse(String program) {
		return parser.parse(program);
	}

	@Override
	public InputProgram apply(InputProgram inputProgram) {
		if (!this.rewritingNecessary(inputProgram)) {
			return inputProgram;
		}
		String summationSubprogram = "interesting_number(R, 1..I1) :- input_number_with_first(R, I, _), I1 = I - 1.\n"
				+ "prefix_subset_sum(R, 0, 0) :- input_number_with_first(R, _, _).\n"
				+ "prefix_subset_sum(R, I, S) :- prefix_subset_sum(R, I1, S), I1 = I - 1, interesting_number(R, I).\n"
				+ "prefix_subset_sum(R, I, SF) :- prefix_subset_sum(R, I1, S), I1 = I - 1, SF = S + F, input_number_with_first(R, I, F),  bound(R, K), SF < K.\n"
				+ "output(R, K) :- bound(R, K), K <= 0."
				+ "output(R, K) :- prefix_subset_sum(R, I1, S), I1 = I - 1, input_number_with_first(R, I, F), bound(R, K), K <= S + F.";

		// Connect/Rewrite every aggregate in each rule.
		List<BasicRule> rewrittenRules = this.rewriteAggregates(inputProgram.getRules());

		InputProgram.Builder prgBuilder = InputProgram.builder();
		prgBuilder.addFacts(inputProgram.getFacts());
		InputProgram summationEncoding = makePredicatesInternal(new ProgramParser().parse(summationSubprogram));
		prgBuilder.accumulate(summationEncoding);
		prgBuilder.addRules(rewrittenRules);
		// summationEncoding.accumulate(additionalRules);

		// Add enumeration rule that uses the special EnumerationAtom.
		// The enumeration rule is: "input_number_with_first(A, I, F) :- input_with_first(A, X, F), _index(A, X, I)."
		BasicRule tmpEnumRule = makePredicatesInternal(parse("input_number_with_first(A, I, F) :- input_with_first(A, X, F).")).getRules().get(0);
		EnumerationAtom enumerationAtom = new EnumerationAtom(parse("index(A, X, I).").getFacts().get(0).getTerms());
		List<Literal> enumerationRuleBody = new ArrayList<>(tmpEnumRule.getBody());
		enumerationRuleBody.add(enumerationAtom.toLiteral());
		BasicRule enumerationRule = new BasicRule(tmpEnumRule.getHead(), enumerationRuleBody);
		prgBuilder.addRule(enumerationRule);

		return prgBuilder.build();
	}

	/**
	 * Checks if rewriting of sum aggregates is necessary for the given program, i.e. if such aggregates exist
	 * 
	 * @param program the program
	 * @return true if sum aggregates occur, false otherwise
	 */
	private boolean rewritingNecessary(InputProgram program) {
		for (BasicRule rule : program.getRules()) {
			for (Literal lit : rule.getBody()) {
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
		List<BasicRule> retVal = new ArrayList<>();
		for (BasicRule rule : srcRules) {
			retVal.addAll(this.rewriteAggregatesInRule(rule));
		}
		return retVal;
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
		final BasicAtom aggregateOutputAtom = (BasicAtom) makePredicatesInternal(parse("output(aggregate(AGGREGATE_ID), LOWER_BOUND).")).getFacts().get(0);
		final BasicAtom aggregateInputAtom = (BasicAtom) makePredicatesInternal(
				parse("input_with_first(aggregate(AGGREGATE_ID), ELEMENT_TUPLE, FIRST_VARIABLE).")).getFacts().get(0);
		final BasicAtom lowerBoundAtom = (BasicAtom) makePredicatesInternal(parse("bound(aggregate(AGGREGATE_ID), LOWER_BOUND).")).getFacts().get(0);

		ArrayList<Literal> aggregateOutputAtoms = new ArrayList<>();
		int aggregatesInRule = 0; // Only needed for limited rewriting.
		ArrayList<BasicRule> additionalRules = new ArrayList<>();

		List<Literal> rewrittenBody = new ArrayList<>(rule.getBody());
		for (Iterator<Literal> iterator = rewrittenBody.iterator(); iterator.hasNext();) {
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
				throw new UnsupportedOperationException(
						"Only limited #count/#sum aggregates without upper bound are currently supported." + "No rule may have more than one aggregate.");
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
			Collection<Term> globalVariables = CardinalityNormalization.getGlobalVariables(rewrittenBody, aggregateAtom);
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
					elementLiterals.addAll(rewrittenBody);
				}
				BasicRule inputRule = new BasicRule(new DisjunctiveHead(Collections.singletonList(inputHeadAtom)), elementLiterals);
				additionalRules.add(inputRule);
			}

			// Create lower bound for the aggregate.
			BasicAtom lowerBoundHeadAtom = lowerBoundAtom.substitute(aggregateUnifier);
			List<Literal> lowerBoundBody = rewrittenBody; // Note: this is only correct if no other aggregate occurs in the rule.
			additionalRules.add(new BasicRule(new NormalHead(lowerBoundHeadAtom), lowerBoundBody));
		}
		rewrittenBody.addAll(aggregateOutputAtoms);
		BasicRule rewrittenSrcRule = new BasicRule(rule.getHead(), rewrittenBody);
		additionalRules.add(rewrittenSrcRule);
		return additionalRules;
	}
}
