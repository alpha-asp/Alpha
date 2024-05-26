package at.ac.tuwien.kr.alpha.api.programs;

import at.ac.tuwien.kr.alpha.api.programs.rules.Rule;
import at.ac.tuwien.kr.alpha.api.programs.rules.heads.Head;
import at.ac.tuwien.kr.alpha.api.programs.tests.TestCase;

import java.util.List;

public interface InputProgram extends Program<Rule<Head>> {

    /**
     * The test cases associated with this program.
     */
    List<TestCase> getTestCases();

}
