package at.ac.tuwien.kr.alpha.core.programs.transformation.aggregates.encoders;

import java.util.function.Supplier;

import at.ac.tuwien.kr.alpha.api.programs.ProgramParser;
import at.ac.tuwien.kr.alpha.api.programs.atoms.AggregateAtom.AggregateFunctionSymbol;

public class AggregateEncoderFactory {

	private final Supplier<ProgramParser> parserFactory;
	private final boolean useSortingGridEncoding;
	private final boolean supportNegativeSumElements;

	public AggregateEncoderFactory(Supplier<ProgramParser> parserFactory, boolean useSortingGridEncoding, boolean supportNegativeSumElements) {
		this.parserFactory = parserFactory;
		this.useSortingGridEncoding = useSortingGridEncoding;
		this.supportNegativeSumElements = supportNegativeSumElements;
	}

	public CountEncoder newCountEqualsEncoder() {
		return CountEncoder.buildCountEqualsEncoder(parserFactory.get());
	}
	
	public CountEncoder newCountLessOrEqualEncoder() {
		return CountEncoder.buildCountLessOrEqualEncoder(parserFactory.get(), useSortingGridEncoding);
	}
	
	public SumEncoder newSumEqualsEncoder() {
		return SumEncoder.buildSumEqualsEncoder(parserFactory.get(), supportNegativeSumElements);
	}
	
	public SumEncoder newSumLessOrEqualEncoder() {
		return SumEncoder.buildSumLessOrEqualEncoder(parserFactory.get(), supportNegativeSumElements);
	}
	
	public MinMaxEncoder newMinEncoder() {
		return new MinMaxEncoder(AggregateFunctionSymbol.MIN);
	}
	
	public MinMaxEncoder newMaxEncoder() {
		return new MinMaxEncoder(AggregateFunctionSymbol.MAX);
	}
	
}
