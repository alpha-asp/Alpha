package at.ac.tuwien.kr.alpha.grounder.transformation.aggregates;

public class AggregateRewritingConfig {
	
	public static final boolean DEFAULT_USE_SORTING_GRID = true;
	public static final boolean DEFAULT_SUPPORT_NEGATIVE_INTEGERS = true;
	
	private boolean useSortingGridEncoding = DEFAULT_USE_SORTING_GRID;
	private boolean supportNegativeValuesInSums = DEFAULT_SUPPORT_NEGATIVE_INTEGERS;
	
	public boolean isUseSortingGridEncoding() {
		return this.useSortingGridEncoding;
	}
	
	public boolean isSupportNegativeValuesInSums() {
		return this.supportNegativeValuesInSums;
	}

	public void setUseSortingGridEncoding(boolean useSortingGridEncoding) {
		this.useSortingGridEncoding = useSortingGridEncoding;
	}

	public void setSupportNegativeValuesInSums(boolean supportNegativeValuesInSums) {
		this.supportNegativeValuesInSums = supportNegativeValuesInSums;
	}

}
