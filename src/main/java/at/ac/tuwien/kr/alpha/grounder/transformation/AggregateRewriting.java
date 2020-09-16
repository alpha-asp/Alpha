package at.ac.tuwien.kr.alpha.grounder.transformation;

import at.ac.tuwien.kr.alpha.common.Predicate;
import at.ac.tuwien.kr.alpha.common.program.InputProgram;

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
		AggregateLiteralSplitting operatorRewriting = new AggregateLiteralSplitting();
		return operatorRewriting.apply(inputProgram);
	}
}
