package at.ac.tuwien.kr.alpha.grounder.transformation.aggregates;

public class AggregateRewritingConfig {
	
	public static final boolean DEFAULT_USE_SORTING_GRID = true;
	public static final boolean DEFAULT_SUPPORT_NEGATIVE_INTEGERS = true;
	
	private final boolean useSortingGridEncoding;
	private final boolean supportNegativeValuesInSums;
	
	public AggregateRewritingConfig(boolean useSortingGridEncoding, boolean supportNegativeValuesInSums) {
		this.useSortingGridEncoding = useSortingGridEncoding;
		this.supportNegativeValuesInSums = supportNegativeValuesInSums;
	}
	
	public boolean isUseSortingGridEncoding() {
		return this.useSortingGridEncoding;
	}
	
	public boolean isSupportNegativeValuesInSums() {
		return this.supportNegativeValuesInSums;
	}

}
