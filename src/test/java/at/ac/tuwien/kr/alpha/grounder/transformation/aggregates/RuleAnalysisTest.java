package at.ac.tuwien.kr.alpha.grounder.transformation.aggregates;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;

import at.ac.tuwien.kr.alpha.common.atoms.AggregateLiteral;
import at.ac.tuwien.kr.alpha.grounder.transformation.aggregates.AggregateRewritingContext.RuleAnalysis;
import at.ac.tuwien.kr.alpha.test.util.RuleParser;

public class RuleAnalysisTest {

	//@formatter:off
	private static final String BINDING_AGGREGATE_NO_GLOBALS = 
			"p(X) :- X = #max{N : q(N)}, X < 10, p(X, Y).";
	//@formatter:on

	private static final RuleAnalysis analyze(String rule) {
		return RuleAnalysis.analyzeRule(RuleParser.parse(rule));
	}

	@Test
	public void bindingAggregateNoGlobals() {
		RuleAnalysis analysis = analyze(BINDING_AGGREGATE_NO_GLOBALS);
		Assert.assertEquals(1, analysis.globalVariablesPerAggregate.size());
		Assert.assertEquals(1, analysis.dependenciesPerAggregate.size());

		AggregateLiteral aggregate = new ArrayList<>(analysis.globalVariablesPerAggregate.keySet()).get(0);
		Assert.assertTrue(analysis.globalVariablesPerAggregate.get(aggregate).isEmpty());
		Assert.assertTrue(analysis.dependenciesPerAggregate.get(aggregate).isEmpty());
	}

}
