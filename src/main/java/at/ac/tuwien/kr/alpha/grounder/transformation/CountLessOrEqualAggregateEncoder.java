package at.ac.tuwien.kr.alpha.grounder.transformation;

import java.util.List;

import at.ac.tuwien.kr.alpha.common.atoms.AggregateAtom.AggregateElement;
import at.ac.tuwien.kr.alpha.common.atoms.AggregateAtom.AggregateFunctionSymbol;
import at.ac.tuwien.kr.alpha.common.atoms.AggregateLiteral;
import at.ac.tuwien.kr.alpha.common.rule.BasicRule;

public class CountLessOrEqualAggregateEncoder extends AbstractAggregateEncoder {

	protected CountLessOrEqualAggregateEncoder(AggregateFunctionSymbol aggregateFunctionToEncode) {
		super(aggregateFunctionToEncode);
	}

	@Override
	protected List<BasicRule> encodeAggregateResult(AggregateLiteral lit, AggregateRewritingContext ctx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected BasicRule encodeAggregateElement(String aggregateId, AggregateElement element) {
		// TODO Auto-generated method stub
		return null;
	}

}
