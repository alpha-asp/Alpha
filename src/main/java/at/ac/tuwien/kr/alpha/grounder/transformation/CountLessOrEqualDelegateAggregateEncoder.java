package at.ac.tuwien.kr.alpha.grounder.transformation;

// TODO do this via decorator pattern on top of any encoder!
public class CountLessOrEqualDelegateAggregateEncoder extends CountLessOrEqualSortingGridEncoder {

	private final String elementEncodingDelegateId;

	protected CountLessOrEqualDelegateAggregateEncoder(String elementEncodingDelegateId) {
		this.elementEncodingDelegateId = elementEncodingDelegateId;
	}

}
