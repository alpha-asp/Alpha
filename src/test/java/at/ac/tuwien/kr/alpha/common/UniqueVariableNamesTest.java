/*
 *  Copyright (c) 2022 Siemens AG
 *  All rights reserved.
 *
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are met:
 *
 *  1) Redistributions of source code must retain the above copyright notice, this
 *     list of conditions and the following disclaimer.
 *
 *  2) Redistributions in binary form must reproduce the above copyright notice,
 *     this list of conditions and the following disclaimer in the documentation
 *     and/or other materials provided with the distribution.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 *  AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 *  IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 *  FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 *  DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 *  SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 *  CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 *  OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 *  OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package at.ac.tuwien.kr.alpha.common;

import org.junit.jupiter.api.Test;

import at.ac.tuwien.kr.alpha.common.rule.BasicRule;
import at.ac.tuwien.kr.alpha.common.terms.VariableTerm;
import at.ac.tuwien.kr.alpha.grounder.Unifier;
import at.ac.tuwien.kr.alpha.grounder.parser.ProgramPartParser;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests {@link UniqueVariableNames}.
 */
public class UniqueVariableNamesTest {

	private final ProgramPartParser programPartParser = new ProgramPartParser();

	@Test
	public void testHeuristicDirectiveAndRule() {
		final HeuristicDirective heuristicDirective = programPartParser.parseHeuristicDirective("#heuristic h(X,Y) : b1(X), b2(Y), b3(Z).");
		final BasicRule rule = programPartParser.parseBasicRule("h(Y,Z) :- b1(Y), b2(Z), b3(X).");
		final UniqueVariableNames uniqueVariableNames = new UniqueVariableNames();
		final Unifier actualUnifier = uniqueVariableNames.makeVariableNamesUnique(heuristicDirective, rule);
		final Unifier expectedUnifier = new Unifier();
		expectedUnifier.put(VariableTerm.getInstance("X"), VariableTerm.getInstance("X_1"));
		expectedUnifier.put(VariableTerm.getInstance("Y"), VariableTerm.getInstance("Y_1"));
		expectedUnifier.put(VariableTerm.getInstance("Z"), VariableTerm.getInstance("Z_1"));
		assertEquals(expectedUnifier, actualUnifier);
	}

}
