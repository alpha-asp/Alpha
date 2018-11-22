/**
 * Copyright (c) 2018 Siemens AG
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

import at.ac.tuwien.kr.alpha.common.AtomStore;
import at.ac.tuwien.kr.alpha.common.AtomStoreImpl;
import at.ac.tuwien.kr.alpha.common.NoGood;
import at.ac.tuwien.kr.alpha.common.Program;
import at.ac.tuwien.kr.alpha.grounder.parser.ProgramParser;
import at.ac.tuwien.kr.alpha.solver.TrailAssignment;
import org.junit.Before;
import org.junit.Test;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.Assert.assertTrue;

/**
 * Tests {@link NaiveGrounder}
 */
public class NaiveGrounderTest {
	private static final ProgramParser PARSER = new ProgramParser();
	
	@Before
	public void resetRuleIdGenerator() {
		NonGroundRule.ID_GENERATOR.resetGenerator();
	}
	
	/**
	 * Asserts that a ground rule whose positive body is not satisfied by the empty assignment
	 * is grounded immediately.
	 */
	@Test
	public void groundRuleAlreadyGround() {
		Program program = PARSER.parse("a :- not b. "
				+ "b :- not a. "
				+ "c :- b.");
		
		AtomStore atomStore = new AtomStoreImpl();
		Grounder grounder = GrounderFactory.getInstance("naive", program, atomStore, true);
		Map<Integer, NoGood> noGoods = grounder.getNoGoods(new TrailAssignment(atomStore));
		Set<String> strNoGoods = noGoods.values().stream().map(atomStore::noGoodToString).collect(Collectors.toSet());
		expectContains(strNoGoods, "*{-(a), +(_R_(\"0\",\"{}\"))}");
		expectContains(strNoGoods, "*{-(b), +(_R_(\"1\",\"{}\"))}");
		expectContains(strNoGoods, "*{-(c), +(_R_(\"2\",\"{}\"))}");
	}
	
	/**
	 * Asserts that a ground rule whose positive non-unary body is not satisfied by the empty assignment
	 * is grounded immediately.
	 */
	@Test
	public void groundRuleWithLongerBodyAlreadyGround() {
		Program program = PARSER.parse("a :- not b. "
				+ "b :- not a. "
				+ "c :- b. "
				+ "d :- b, c. ");
		
		AtomStore atomStore = new AtomStoreImpl();
		Grounder grounder = GrounderFactory.getInstance("naive", program, atomStore, true);
		Map<Integer, NoGood> noGoods = grounder.getNoGoods(new TrailAssignment(atomStore));
		Set<String> strNoGoods = noGoods.values().stream().map(atomStore::noGoodToString).collect(Collectors.toSet());
		expectContains(strNoGoods, "*{-(a), +(_R_(\"0\",\"{}\"))}");
		expectContains(strNoGoods, "*{-(b), +(_R_(\"1\",\"{}\"))}");
		expectContains(strNoGoods, "*{-(c), +(_R_(\"2\",\"{}\"))}");
		expectContains(strNoGoods, "*{-(d), +(_R_(\"3\",\"{}\"))}");
	}
	
	/**
	 * Asserts that a ground constraint whose positive body is not satisfied by the empty assignment
	 * is grounded immediately.
	 */
	@Test
	public void groundConstraintAlreadyGround() {
		Program program = PARSER.parse("a :- not b. "
				+ "b :- not a. "
				+ ":- b.");
		
		AtomStore atomStore = new AtomStoreImpl();
		Grounder grounder = GrounderFactory.getInstance("naive", program, atomStore, true);
		Map<Integer, NoGood> noGoods = grounder.getNoGoods(new TrailAssignment(atomStore));
		Set<String> strNoGoods = noGoods.values().stream().map(atomStore::noGoodToString).collect(Collectors.toSet());
		expectContains(strNoGoods, "*{-(a), +(_R_(\"0\",\"{}\"))}");
		expectContains(strNoGoods, "*{-(b), +(_R_(\"1\",\"{}\"))}");
		expectContains(strNoGoods, "{+(b)}");
	}

	private void expectContains(Collection<String> strings, String string) {
		assertTrue("Collection of strings does not contain \"" + string + "\"", strings.contains(string));
	}

}
