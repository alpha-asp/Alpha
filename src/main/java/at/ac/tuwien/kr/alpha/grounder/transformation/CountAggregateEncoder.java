package at.ac.tuwien.kr.alpha.grounder.transformation;

import java.util.List;

import at.ac.tuwien.kr.alpha.common.atoms.AggregateAtom.AggregateElement;
import at.ac.tuwien.kr.alpha.common.atoms.AggregateAtom.AggregateFunctionSymbol;
import at.ac.tuwien.kr.alpha.common.rule.BasicRule;

public class CountAggregateEncoder extends AbstractAggregateEncoder {

	public CountAggregateEncoder() {
		super(AggregateFunctionSymbol.COUNT);
	}

	@Override
	protected List<BasicRule> encodeAggregateResult(String aggregateId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected BasicRule encodeAggregateElement(String aggregateId, AggregateElement element) {
		// TODO Auto-generated method stub
		return null;
	}

}
