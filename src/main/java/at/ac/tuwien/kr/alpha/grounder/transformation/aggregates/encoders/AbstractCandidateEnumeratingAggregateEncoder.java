package at.ac.tuwien.kr.alpha.grounder.transformation.aggregates.encoders;

import java.util.Set;

import at.ac.tuwien.kr.alpha.common.ComparisonOperator;
import at.ac.tuwien.kr.alpha.common.atoms.AggregateAtom.AggregateFunctionSymbol;
import at.ac.tuwien.kr.alpha.common.program.InputProgram;
import at.ac.tuwien.kr.alpha.grounder.transformation.aggregates.AggregateRewritingContext;
import at.ac.tuwien.kr.alpha.grounder.transformation.aggregates.AggregateRewritingContext.AggregateInfo;

/**
 * Abstract base class for aggregate encoders that create encodings based around enumerations.
 * Aggregate functions like "count" and "sum" need to generate "candidate" values that are then tested against the
 * comparison operator of the aggregate literal to encode. These encodings have in common that each aggregate element
 * tuple, i.e. every ground tuple derivable from the literals of an aggregate element, is indexed using an enumeration,
 * thereby generating a result candidate.
 * 
 * Copyright (c) 2020, the Alpha Team.
 */
public abstract class AbstractCandidateEnumeratingAggregateEncoder extends AbstractAggregateEncoder {

	protected AbstractCandidateEnumeratingAggregateEncoder(AggregateFunctionSymbol aggregateFunctionToEncode, Set<ComparisonOperator> acceptedOperators) {
		super(aggregateFunctionToEncode, acceptedOperators);
	}
	
	@Override
	protected InputProgram encodeAggregateResult(AggregateInfo aggregateToEncode, AggregateRewritingContext ctx) {
		// TODO Auto-generated method stub
		return null;
	}
	
	protected abstract InputProgram generateBaseEncoding(AggregateInfo aggregateToEncode, AggregateRewritingContext ctx);

}
