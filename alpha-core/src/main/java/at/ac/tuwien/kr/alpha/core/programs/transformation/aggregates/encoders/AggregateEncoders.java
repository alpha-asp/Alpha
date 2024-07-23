package at.ac.tuwien.kr.alpha.core.programs.transformation.aggregates.encoders;

import at.ac.tuwien.kr.alpha.api.programs.atoms.AggregateAtom.AggregateFunctionSymbol;

public final class AggregateEncoders {
	
	private AggregateEncoders() {
		throw new AssertionError("Cannot instantiate utility class!");
	}

	public static CountEncoder newCountEqualsEncoder() {
		return CountEncoder.buildCountEqualsEncoder();
	}
	
	public static CountEncoder newCountLessOrEqualEncoder(boolean useSortingGridEncoding) {
		return CountEncoder.buildCountLessOrEqualEncoder(useSortingGridEncoding);
	}
	
	public static SumEncoder newSumEqualsEncoder(boolean supportNegativeSumElements) {
		return SumEncoder.buildSumEqualsEncoder(supportNegativeSumElements);
	}
	
	public static SumEncoder newSumLessOrEqualEncoder(boolean supportNegativeSumElements) {
		return SumEncoder.buildSumLessOrEqualEncoder(supportNegativeSumElements);
	}
	
	public static MinMaxEncoder newMinEncoder() {
		return new MinMaxEncoder(AggregateFunctionSymbol.MIN);
	}
	
	public static MinMaxEncoder newMaxEncoder() {
		return new MinMaxEncoder(AggregateFunctionSymbol.MAX);
	}

}
