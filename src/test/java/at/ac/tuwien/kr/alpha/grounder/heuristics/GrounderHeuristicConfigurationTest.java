/**
 * Copyright (c) 2019 Siemens AG
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
package at.ac.tuwien.kr.alpha.grounder.heuristics;

import org.junit.Test;

import static at.ac.tuwien.kr.alpha.grounder.heuristics.GrounderHeuristicsConfiguration.LAX_STRING;
import static at.ac.tuwien.kr.alpha.grounder.heuristics.GrounderHeuristicsConfiguration.STRICT_STRING;
import static org.junit.Assert.*;

/**
 * Tests {@link GrounderHeuristicsConfiguration}
 */
public class GrounderHeuristicConfigurationTest {

	@Test
	public void testGetInstanceStrictStrict() {
		GrounderHeuristicsConfiguration grounderHeuristicsConfiguration = GrounderHeuristicsConfiguration.getInstance(STRICT_STRING, STRICT_STRING);
		assertFalse(grounderHeuristicsConfiguration.isLax(true));
		assertFalse(grounderHeuristicsConfiguration.isLax(false));
		assertEquals(0, grounderHeuristicsConfiguration.getToleranceConstraints());
		assertEquals(0, grounderHeuristicsConfiguration.getToleranceRules());
	}

	@Test
	public void testGetInstanceStrictLax() {
		GrounderHeuristicsConfiguration grounderHeuristicsConfiguration = GrounderHeuristicsConfiguration.getInstance(STRICT_STRING, LAX_STRING);
		assertFalse(grounderHeuristicsConfiguration.isLax(true));
		assertTrue(grounderHeuristicsConfiguration.isLax(false));
		assertEquals(0, grounderHeuristicsConfiguration.getToleranceConstraints());
		assertEquals(-1, grounderHeuristicsConfiguration.getToleranceRules());
	}

	@Test
	public void testGetInstanceLaxStrict() {
		GrounderHeuristicsConfiguration grounderHeuristicsConfiguration = GrounderHeuristicsConfiguration.getInstance(LAX_STRING, STRICT_STRING);
		assertTrue(grounderHeuristicsConfiguration.isLax(true));
		assertFalse(grounderHeuristicsConfiguration.isLax(false));
		assertEquals(-1, grounderHeuristicsConfiguration.getToleranceConstraints());
		assertEquals(0, grounderHeuristicsConfiguration.getToleranceRules());
	}

	@Test
	public void testGetInstanceLaxLax() {
		GrounderHeuristicsConfiguration grounderHeuristicsConfiguration = GrounderHeuristicsConfiguration.getInstance(LAX_STRING, LAX_STRING);
		assertTrue(grounderHeuristicsConfiguration.isLax(true));
		assertTrue(grounderHeuristicsConfiguration.isLax(false));
		assertEquals(-1, grounderHeuristicsConfiguration.getToleranceConstraints());
		assertEquals(-1, grounderHeuristicsConfiguration.getToleranceRules());
	}

	@Test
	public void testGetInstanceIntInt() {
		GrounderHeuristicsConfiguration grounderHeuristicsConfiguration = GrounderHeuristicsConfiguration.getInstance(5, 1);
		assertTrue(grounderHeuristicsConfiguration.isLax(true));
		assertTrue(grounderHeuristicsConfiguration.isLax(false));
		assertEquals(5, grounderHeuristicsConfiguration.getToleranceConstraints());
		assertEquals(1, grounderHeuristicsConfiguration.getToleranceRules());
	}

	@Test
	public void testGetInstanceStringIntStringInt() {
		GrounderHeuristicsConfiguration grounderHeuristicsConfiguration = GrounderHeuristicsConfiguration.getInstance("5", "1");
		assertTrue(grounderHeuristicsConfiguration.isLax(true));
		assertTrue(grounderHeuristicsConfiguration.isLax(false));
		assertEquals(5, grounderHeuristicsConfiguration.getToleranceConstraints());
		assertEquals(1, grounderHeuristicsConfiguration.getToleranceRules());
	}

}
