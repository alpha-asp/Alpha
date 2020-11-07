package at.ac.tuwien.kr.alpha.grounder.transformation;

import at.ac.tuwien.kr.alpha.common.atoms.AggregateAtom.AggregateFunctionSymbol;

public class CountLessOrEqualDelegateAggregateEncoder extends CountLessOrEqualAggregateEncoder {

	private final String elementEncodingDelegateId;

	protected CountLessOrEqualDelegateAggregateEncoder(String elementEncodingDelegateId) {
		super(AggregateFunctionSymbol.COUNT);
		this.elementEncodingDelegateId = elementEncodingDelegateId;
	}

}
