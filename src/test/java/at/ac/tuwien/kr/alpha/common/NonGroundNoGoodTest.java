/*
 *  Copyright (c) 2020 Siemens AG
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

import at.ac.tuwien.kr.alpha.common.atoms.BasicAtom;
import at.ac.tuwien.kr.alpha.common.atoms.Literal;
import at.ac.tuwien.kr.alpha.common.terms.VariableTerm;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Tests {@link NonGroundNoGood}
 */
public class NonGroundNoGoodTest {

	@Test
	public void testEqualsIgnoreOrderOfLiterals() {
		final Predicate predA = Predicate.getInstance("a", 1);
		final Predicate predB = Predicate.getInstance("b", 1);
		final VariableTerm varX = VariableTerm.getInstance("X");
		final VariableTerm varY = VariableTerm.getInstance("Y");
		final Literal literalAX = new BasicAtom(predA, varX).toLiteral();
		final Literal literalBY = new BasicAtom(predB, varY).toLiteral();
		final NonGroundNoGood ng1 = new NonGroundNoGood(literalAX, literalBY);
		final NonGroundNoGood ng2 = new NonGroundNoGood(literalBY, literalAX);
		assertEquals(ng1, ng2);
		assertEquals(ng1.hashCode(), ng2.hashCode());
	}

}
