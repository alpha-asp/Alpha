package at.ac.tuwien.kr.alpha.core.programs.transformation.aggregates.encoders;

import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;

import at.ac.tuwien.kr.alpha.api.ComparisonOperator;
import at.ac.tuwien.kr.alpha.api.programs.atoms.AggregateAtom.AggregateFunctionSymbol;
import at.ac.tuwien.kr.alpha.commons.comparisons.ComparisonOperators;
import at.ac.tuwien.kr.alpha.commons.util.Util;

public final class CountEncoder extends StringtemplateBasedAggregateEncoder {

	private static final STGroup AGGREGATE_ENCODINGS = Util.loadStringTemplateGroup(
			SumEncoder.class.getResource("/stringtemplates/aggregate-encodings.stg"));

	private static final ST CNT_LE_SORTING_GRID_TEMPLATE = AGGREGATE_ENCODINGS.getInstanceOf("cnt_le_sorting_grid");
	private static final ST CNT_EQ_TEMPLATE = AGGREGATE_ENCODINGS.getInstanceOf("cnt_eq");
	private static final ST CNT_LE_COUNTING_GRID_TEMPLATE = AGGREGATE_ENCODINGS.getInstanceOf("cnt_le_counting_grid");

	private CountEncoder(ComparisonOperator acceptedOperator, ST encodingTemplate) {
		super(AggregateFunctionSymbol.COUNT, acceptedOperator, encodingTemplate);
	}

	public static CountEncoder buildCountLessOrEqualEncoder(boolean useSortingGrid) {
		return new CountEncoder(ComparisonOperators.LE, useSortingGrid ? CNT_LE_SORTING_GRID_TEMPLATE : CNT_LE_COUNTING_GRID_TEMPLATE);
	}

	public static CountEncoder buildCountEqualsEncoder() {
		return new CountEncoder(ComparisonOperators.EQ, CNT_EQ_TEMPLATE);
	}

}
