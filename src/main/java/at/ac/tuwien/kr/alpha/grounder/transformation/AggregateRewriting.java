package at.ac.tuwien.kr.alpha.grounder.transformation;

import at.ac.tuwien.kr.alpha.common.program.InputProgram;

// FIXME do proper internalizing of predicates
// Internalize before stitching programs together (otherwise clashes!)
// (maybe internalize on getInstance? in that case don't forget stuff that's parsed internally!)
public class AggregateRewriting extends ProgramTransformation<InputProgram, InputProgram> {

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
		AggregateLiteralSplitting literalSplitting = new AggregateLiteralSplitting();
		AggregateOperatorNormalization operatorNormalization = new AggregateOperatorNormalization();
		AbstractAggregateTransformation bindingAggregateTransformation = new BindingAggregateTransformation();
		CardinalityNormalization cardinalityNormalization = new CardinalityNormalization(config.isUseSortingCircuitEncoding());
		SumNormalization sumNormalization = new SumNormalization();
		InputProgram result = literalSplitting
				.andThen(operatorNormalization)
				.andThen(bindingAggregateTransformation)
				.andThen(cardinalityNormalization)
				.andThen(sumNormalization)
				.apply(inputProgram);
		return result;
	}
}
