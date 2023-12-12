package at.ac.tuwien.kr.alpha.core.solver.reboot;

import at.ac.tuwien.kr.alpha.core.solver.RegressionTest;
import at.ac.tuwien.kr.alpha.core.solver.RegressionTestConfig;

import java.util.stream.IntStream;

import static at.ac.tuwien.kr.alpha.core.test.util.TestUtils.assertRegressionTestAnswerSets;

public class GroundExplosionTest {
	@RegressionTest
	public void testGroundExplosion_1(RegressionTestConfig cfg) {
		String domainStr = getDomainString(1);
		assertRegressionTestAnswerSets(cfg,
				getProgramString(1),
				domainStr,
				"sel(1), p(1,1,1,1,1,1), " + domainStr);
	}

	@RegressionTest
	public void testGroundExplosion_5(RegressionTestConfig cfg) {
		String domainStr = getDomainString(5);
		assertRegressionTestAnswerSets(cfg,
				getProgramString(5),
				domainStr,
				"sel(1), p(1,1,1,1,1,1), " + domainStr,
				"sel(2), p(2,2,2,2,2,2), " + domainStr,
				"sel(3), p(3,3,3,3,3,3), " + domainStr,
				"sel(4), p(4,4,4,4,4,4), " + domainStr,
				"sel(5), p(5,5,5,5,5,5), " + domainStr);
	}

	/**
	 * Constructs a ground explosion program string with the given domain size.
	 *
	 * @param n the size of the encoded domain.
	 */
	private String getProgramString(int n) {
		return "{ sel(X) } :- dom(X)." +
				":- sel(X), sel(Y), X != Y." +
				"p(X1,X2,X3,X4,X5,X6) :- sel(X1), sel(X2), sel(X3), sel(X4), sel(X5), sel(X6)." +
				String.format("dom(1..%d).", n);
	}

	/**
	 * Constructs a string of atoms representing a domain of the given size.
	 *
	 * @param n the size of the encoded domain.
	 */
	private String getDomainString(int n) {
		return IntStream.range(1, n + 1)
				.mapToObj(x -> String.format("dom(%d)", x))
				.reduce((x, y) -> String.format("%s, %s", x, y))
				.orElse("");
	}
}
