package at.ac.tuwien.kr.alpha.grounder.transformation;

import org.junit.Assert;
import org.junit.Test;

import at.ac.tuwien.kr.alpha.api.Alpha;
import at.ac.tuwien.kr.alpha.common.program.InputProgram;
import at.ac.tuwien.kr.alpha.common.rule.BasicRule;
import at.ac.tuwien.kr.alpha.test.util.RuleParser;
import at.ac.tuwien.kr.alpha.test.util.TestUtils;

public class AggregateRewritingTest {

	//@formatter:off
	public static final String LITERAL_SPLITTING_POS1_ASP = 
			"thing1(1..3)."
			+ "thing2(1..5)."
			+ "thing3(1..7)."
			+ "count_between :- "
				+ "X < #count{ N : thing2(N) } < Y, "
				+ "X = #count{ K : thing1(K) },"
				+ "Y = #count{ L : thing3(L) }.";
	//@formatter:on

	@Test
	public void literalSplittingSinglePositiveLiteral() {
		Alpha alpha = new Alpha();
		ProgramTransformation<InputProgram, InputProgram> aggregateRewriting = new AggregateRewriting(null);
		InputProgram input = alpha.readProgramString(LITERAL_SPLITTING_POS1_ASP);
		InputProgram rewritten = aggregateRewriting.apply(input);
		BasicRule expectedRewrittenRule = RuleParser
				.parse("count_between :- X < #count{N : thing2(N)}, Y > #count{N : thing2(N)}, X = #count{ K : thing1(K) }, Y = #count{ L : thing3(L) }.");
		TestUtils.assertProgramContainsRule(rewritten, expectedRewrittenRule);
		Assert.assertEquals(1, rewritten.getRules().size());
	}

}
