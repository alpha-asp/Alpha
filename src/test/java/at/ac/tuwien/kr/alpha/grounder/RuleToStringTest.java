/**
 * Copyright (c) 2017 Siemens AG
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1) Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 
 * 2) Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package at.ac.tuwien.kr.alpha.grounder;

import at.ac.tuwien.kr.alpha.common.Program;
import at.ac.tuwien.kr.alpha.common.Rule;
import at.ac.tuwien.kr.alpha.grounder.parser.ProgramParser;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Tests {@link Rule#toString()} and {@link NonGroundRule#toString()}.
 */
public class RuleToStringTest {
	private final ProgramParser parser = new ProgramParser();

	@Test
	public void normalRuleToString() {
		parseSingleRuleAndCheckToString("a :- b1, b2, not c1, not c2.");
	}

	@Test
	public void constraintToString() {
		parseSingleRuleAndCheckToString(":- b1, b2, not c1, not c2.");
	}

	@Test
	public void nonGroundRuleToString() {
		constructNonGroundRuleAndCheckToString("a(X) :- b1(X), b2(X), not c1(X), not c2(X).");
	}

	@Test
	public void nonGroundConstraintToString() {
		constructNonGroundRuleAndCheckToString(":- b1(X), b2(X), not c1(X), not c2(X).");
	}

	@Test
	public void ruleWithHeuristicAnnotationToString_W() {
		constructNonGroundRuleAndCheckToString("a(X) :- b1(X), b2(X), not c1(X), not c2(X). [X]", "a(X) :- b1(X), b2(X), not c1(X), not c2(X). [X@1]");
	}

	@Test
	public void ruleWithHeuristicAnnotationToString_WL() {
		constructNonGroundRuleAndCheckToString("a(X) :- b1(X), b2(X), not c1(X), not c2(X). [X@2]");
	}

	@Test
	public void ruleWithHeuristicAnnotationToString_WLG() {
		constructNonGroundRuleAndCheckToString("a(X) :- b1(X), b2(X), not c1(X), not c2(X). [X@2 : not c3(X), c4(X)]");
	}

	private void parseSingleRuleAndCheckToString(String rule) {
		Rule parsedRule = parseSingleRule(rule);
		assertEquals(rule, parsedRule.toString());
	}

	private void constructNonGroundRuleAndCheckToString(String textualRule) {
		constructNonGroundRuleAndCheckToString(textualRule, textualRule);
	}

	private void constructNonGroundRuleAndCheckToString(String textualRule, String expectedToString) {
		NonGroundRule nonGroundRule = NonGroundRule.constructNonGroundRule(parseSingleRule(textualRule));
		assertEquals(expectedToString, nonGroundRule.toString());
	}

	private Rule parseSingleRule(String rule) {
		Program program = parser.parse(rule);
		List<Rule> rules = program.getRules();
		assertEquals("Number of rules", 1, rules.size());
		return rules.get(0);
	}
}