package at.ac.tuwien.kr.alpha.grounder.transformation.aggregates.encoders;

public class AggregateEncoderFactory {

	private final boolean encodeCountUsingSortingCircuit;

	public AggregateEncoderFactory() {
		this.encodeCountUsingSortingCircuit = true;
	}

	public AggregateEncoderFactory(boolean encodeCountUsingSortingCircuit) {
		this.encodeCountUsingSortingCircuit = encodeCountUsingSortingCircuit;
	}

	public AbstractAggregateEncoder buildCountLessOrEqualEncoder() {
		return new CountLessOrEqualEncoder();
	}

	public AbstractAggregateEncoder buildSumLessOrEqualEncoder() {
		return SumEncoder.buildSumLessOrEqualEncoder();
	}

	public AbstractAggregateEncoder buildSumEqualsEncoder() {
		return SumEncoder.buildSumEqualsEncoder();
	}

}
