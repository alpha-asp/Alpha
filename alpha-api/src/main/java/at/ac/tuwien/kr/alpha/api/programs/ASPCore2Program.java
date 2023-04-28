package at.ac.tuwien.kr.alpha.api.programs;

import at.ac.tuwien.kr.alpha.api.programs.rules.Rule;
import at.ac.tuwien.kr.alpha.api.programs.rules.heads.Head;
import at.ac.tuwien.kr.alpha.api.programs.tests.TestCase;

import java.util.List;

/**
 * A {@link Program} that conforms to Alphas implementation of the ASP-Core2-Standard.
 *
 * Copyright (c) 2021, the Alpha Team.
 */
public interface ASPCore2Program extends Program<Rule<Head>> {

	/**
	 * The test cases associated with this program.
	 */
	List<TestCase> getTestCases();

}
