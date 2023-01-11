package at.ac.tuwien.kr.alpha.api.config;

import at.ac.tuwien.kr.alpha.api.Alpha;
import at.ac.tuwien.kr.alpha.api.programs.literals.AggregateLiteral;

/**
 * Configuration structure controlling how {@link AggregateLiteral}s are compiled during program normalization in
 * {@link Alpha#normalizeProgram(at.ac.tuwien.kr.alpha.api.programs.ASPCore2Program)}.
 * 
 * Copyright (c) 2021, the Alpha Team.
 */
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

	/**
	 * Indicates whether "#count" aggregates should be compiled using a sorting-grid based encoding
	 * (<a href="https://en.wikipedia.org/wiki/Batcher_odd%E2%80%93even_mergesort">details</a>)
	 * rather than a simpler version with quadratic complexity in grounding.
	 */
	public void setUseSortingGridEncoding(boolean useSortingGridEncoding) {
		this.useSortingGridEncoding = useSortingGridEncoding;
	}

	/**
	 * Indicates whether encodings for "#sum" aggregates should support negative elements in the aggregated sets. While being more generic, this
	 * encoding negatively impacts grounding performance.
	 */
	public void setSupportNegativeValuesInSums(boolean supportNegativeValuesInSums) {
		this.supportNegativeValuesInSums = supportNegativeValuesInSums;
	}

}
