package at.ac.tuwien.kr.alpha.grounder.transformation.aggregates.encoders;

import at.ac.tuwien.kr.alpha.common.atoms.AggregateAtom.AggregateFunctionSymbol;

/**
 * Copyright (c) 2020, the Alpha Team.
 */
public class AggregateEncoderFactory {

	private final boolean encodeCountUsingSortingCircuit;

	public AggregateEncoderFactory() {
		this.encodeCountUsingSortingCircuit = true;
	}

	public AggregateEncoderFactory(boolean encodeCountUsingSortingCircuit) {
		this.encodeCountUsingSortingCircuit = encodeCountUsingSortingCircuit;
	}

	public AbstractAggregateEncoder buildCountLessOrEqualEncoder() {
		return CountEncoder.buildCountLessOrEqualEncoder(this.encodeCountUsingSortingCircuit);
	}

	public AbstractAggregateEncoder buildCountEqualsEncoder() {
		return CountEncoder.buildCountEqualsEncoder(this.encodeCountUsingSortingCircuit);
	}

	public AbstractAggregateEncoder buildSumLessOrEqualEncoder() {
		return SumEncoder.buildSumLessOrEqualEncoder();
	}

	public AbstractAggregateEncoder buildSumEqualsEncoder() {
		return SumEncoder.buildSumEqualsEncoder();
	}

	public AbstractAggregateEncoder buildMinEncoder() {
		return new MinMaxEncoder(AggregateFunctionSymbol.MIN);
	}

	public AbstractAggregateEncoder buildMaxEncoder() {
		return new MinMaxEncoder(AggregateFunctionSymbol.MAX);
	}

}
