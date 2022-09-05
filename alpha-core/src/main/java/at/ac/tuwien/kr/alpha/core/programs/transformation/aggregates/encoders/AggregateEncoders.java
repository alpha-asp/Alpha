package at.ac.tuwien.kr.alpha.core.programs.transformation.aggregates.encoders;

import at.ac.tuwien.kr.alpha.api.programs.ProgramParser;
import at.ac.tuwien.kr.alpha.api.programs.atoms.AggregateAtom.AggregateFunctionSymbol;

public final class AggregateEncoders {
	
	private AggregateEncoders() {
		throw new AssertionError("Cannot instantiate utility class!");
	}

	public static CountEncoder newCountEqualsEncoder(ProgramParser parser) {
		return CountEncoder.buildCountEqualsEncoder(parser);
	}
	
	public static CountEncoder newCountLessOrEqualEncoder(ProgramParser parser, boolean useSortingGridEncoding) {
		return CountEncoder.buildCountLessOrEqualEncoder(parser, useSortingGridEncoding);
	}
	
	public static SumEncoder newSumEqualsEncoder(ProgramParser parser, boolean supportNegativeSumElements) {
		return SumEncoder.buildSumEqualsEncoder(parser, supportNegativeSumElements);
	}
	
	public static SumEncoder newSumLessOrEqualEncoder(ProgramParser parser, boolean supportNegativeSumElements) {
		return SumEncoder.buildSumLessOrEqualEncoder(parser, supportNegativeSumElements);
	}
	
	public static MinMaxEncoder newMinEncoder(ProgramParser parser) {
		return new MinMaxEncoder(parser, AggregateFunctionSymbol.MIN);
	}
	
	public static MinMaxEncoder newMaxEncoder(ProgramParser parser) {
		return new MinMaxEncoder(parser, AggregateFunctionSymbol.MAX);
	}

}
