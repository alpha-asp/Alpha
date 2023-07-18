/**
 * Copyright (c) 2019, the Alpha Team.
 * All rights reserved.
 * <p>
 * Additional changes made by Siemens.
 * <p>
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * <p>
 * 1) Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 * <p>
 * 2) Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * <p>
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
package at.ac.tuwien.kr.alpha.commons.programs;

import at.ac.tuwien.kr.alpha.api.programs.ASPCore2Program;
import at.ac.tuwien.kr.alpha.api.programs.InlineDirectives;
import at.ac.tuwien.kr.alpha.api.programs.atoms.Atom;
import at.ac.tuwien.kr.alpha.api.programs.rules.Rule;
import at.ac.tuwien.kr.alpha.api.programs.rules.heads.Head;
import at.ac.tuwien.kr.alpha.api.programs.tests.TestCase;
import at.ac.tuwien.kr.alpha.commons.util.Util;

import java.util.Collections;
import java.util.List;

/**
 * Alpha-internal representation of an ASP program, i.e., a set of ASP rules.
 * <p>
 * Copyright (c) 2017-2019, the Alpha Team.
 */
class ASPCore2ProgramImpl extends AbstractProgram<Rule<Head>> implements ASPCore2Program{

	static final ASPCore2ProgramImpl EMPTY = new ASPCore2ProgramImpl(Collections.emptyList(), Collections.emptyList(), new InlineDirectivesImpl(), Collections.emptyList());

	private final List<TestCase> testCases;

	ASPCore2ProgramImpl(List<Rule<Head>> rules, List<Atom> facts, InlineDirectives inlineDirectives, List<TestCase> testCases) {
		super(rules, facts, inlineDirectives);
		this.testCases = testCases;
	}

	@Override
	public List<TestCase> getTestCases() {
		return testCases;
	}

	@Override
	public String toString() {
		String ls = System.lineSeparator();
		final String testsString = testCases.isEmpty() ? "" : Util.join("", testCases, ls, ls);
		return super.toString() + testsString;
	}

}
