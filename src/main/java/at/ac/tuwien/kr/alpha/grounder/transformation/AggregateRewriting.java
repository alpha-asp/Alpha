package at.ac.tuwien.kr.alpha.grounder.transformation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import at.ac.tuwien.kr.alpha.common.Predicate;
import at.ac.tuwien.kr.alpha.common.atoms.AggregateAtom;
import at.ac.tuwien.kr.alpha.common.atoms.AggregateAtom.AggregateFunctionSymbol;
import at.ac.tuwien.kr.alpha.common.atoms.AggregateLiteral;
import at.ac.tuwien.kr.alpha.common.atoms.BasicAtom;
import at.ac.tuwien.kr.alpha.common.atoms.BasicLiteral;
import at.ac.tuwien.kr.alpha.common.atoms.Literal;
import at.ac.tuwien.kr.alpha.common.program.InputProgram;
import at.ac.tuwien.kr.alpha.common.rule.BasicRule;
import at.ac.tuwien.kr.alpha.common.terms.ConstantTerm;

public class AggregateRewriting extends ProgramTransformation<InputProgram, InputProgram> {

	public static final Predicate AGGREGATE_RESULT = Predicate.getInstance("_aggregate_result", 2);
	public static final Predicate AGGREGATE_ELEMENT_TUPLE = Predicate.getInstance("_aggregate_element_tuple", 3);

	// TODO add a switch to control whether internal predicates should be internalized (debugging!)
	private final AggregateRewritingConfig config;

	public AggregateRewriting(AggregateRewritingConfig config) {
		this.config = config;
	}

	/**
	 * Transformation steps:
	 * - Preprocessing: build a "symbol table", assigning an ID to each distinct aggregate literal
	 * - Bounds normalization: everything to "left-associative" expressions with one operator
	 * - Operator normalization: everything to expressions of form "RESULT LEQ #agg{...}"
	 * - Cardinality normalization: rewrite #count expressions
	 * - Sum normalization: rewrite #sum expressions
	 */
	@Override
	public InputProgram apply(InputProgram inputProgram) {
		// Build an "index" of all aggregates in the program.
		Map<AggregateLiteral, String> aggregateIds = this.buildAggregateTable(inputProgram);
		for (Entry<AggregateLiteral, String> entry : aggregateIds.entrySet()) {
			System.out.println("Mapped aggregate: " + entry.getKey() + " => " + entry.getValue());
		}

		// Exchange all aggregate literals in rule bodies with their respective result literals,
		// which will be derived by the rewritten code
		List<BasicRule> rewrittenRules = this.rewriteAggregateLiterals(aggregateIds, inputProgram);
		for (BasicRule rule : rewrittenRules) {
			System.out.println(rule);
		}

		AggregateOperatorRewriting operatorRewriting = new AggregateOperatorRewriting(this.config);
		CardinalityNormalization cardinalityNormalization = new CardinalityNormalization(this.config.isUseSortingCircuitEncoding());
		SumNormalization sumNormalization = new SumNormalization();
		InputProgram inputWithRewrittenOperators = operatorRewriting.apply(inputProgram);
		InputProgram inputWithCountAggregatesRewritten = cardinalityNormalization.apply(inputWithRewrittenOperators);
		InputProgram inputWithAllAggregatesRewritten = sumNormalization.apply(inputWithCountAggregatesRewritten);
		return inputWithAllAggregatesRewritten;
	}

	private Map<AggregateLiteral, String> buildAggregateTable(InputProgram program) {
		// One ID counter per aggregate function type to make reading generated code easier.
		Map<AggregateFunctionSymbol, Integer> countPerType = new HashMap<>();
		for (AggregateFunctionSymbol sym : AggregateFunctionSymbol.values()) {
			countPerType.put(sym, 1);
		}

		Map<AggregateLiteral, String> aggregateIds = new HashMap<>();
		for (BasicRule rule : program.getRules()) {
			for (Literal lit : rule.getBody()) {
				if (lit instanceof AggregateLiteral) {
					AggregateLiteral aggLit = (AggregateLiteral) lit;
					if (aggregateIds.containsKey(aggLit)) {
						continue;
					}
					int aggregateNum = countPerType.get(aggLit.getAtom().getAggregatefunction());
					countPerType.put(aggLit.getAtom().getAggregatefunction(), aggregateNum + 1);
					String id = aggLit.getAtom().getAggregatefunction().toString().toLowerCase() + "_" + aggregateNum;
					aggregateIds.put(aggLit, id);
				}
			}
		}
		return aggregateIds;
	}

	private List<BasicRule> rewriteAggregateLiterals(Map<AggregateLiteral, String> aggregateIds, InputProgram input) {
		List<BasicRule> rewrittenRules = new ArrayList<>();
		for (BasicRule rule : input.getRules()) {
			boolean hasAggregate = false;
			for (Literal lit : rule.getBody()) {
				if (lit instanceof AggregateLiteral) {
					hasAggregate = true;
					break;
				}
			}
			rewrittenRules.add(hasAggregate ? rewriteAggregateLiterals(aggregateIds, rule) : rule);
		}
		return rewrittenRules;
	}

	private BasicRule rewriteAggregateLiterals(Map<AggregateLiteral, String> aggregateIds, BasicRule rule) {
		List<Literal> rewrittenBody = new ArrayList<>();
		for (Literal lit : rule.getBody()) {
			if (lit instanceof AggregateLiteral) {
				AggregateAtom atom = ((AggregateLiteral) lit).getAtom();
				BasicAtom aggregateOutputAtom = new BasicAtom(AGGREGATE_RESULT, ConstantTerm.getSymbolicInstance(aggregateIds.get((AggregateLiteral) lit)),
						atom.getLowerBoundTerm());
				rewrittenBody.add(new BasicLiteral(aggregateOutputAtom, !lit.isNegated()));
			} else {
				rewrittenBody.add(lit);
			}
		}
		return new BasicRule(rule.getHead(), rewrittenBody);
	}

}
