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
import at.ac.tuwien.kr.alpha.common.atoms.BasicLiteral;
import at.ac.tuwien.kr.alpha.common.atoms.ComparisonAtom;
import at.ac.tuwien.kr.alpha.common.atoms.ComparisonLiteral;
import at.ac.tuwien.kr.alpha.common.atoms.Literal;
import at.ac.tuwien.kr.alpha.common.terms.ArithmeticTerm;
import at.ac.tuwien.kr.alpha.common.terms.ConstantTerm;
import at.ac.tuwien.kr.alpha.common.terms.Term;
import at.ac.tuwien.kr.alpha.common.terms.VariableTerm;
import org.junit.Before;
import org.junit.Test;

import static at.ac.tuwien.kr.alpha.common.ComparisonOperator.EQ;
import static at.ac.tuwien.kr.alpha.common.NoGoodInterface.Type.LEARNT;
import static at.ac.tuwien.kr.alpha.common.NoGoodInterface.Type.STATIC;
import static at.ac.tuwien.kr.alpha.common.terms.ArithmeticTerm.ArithmeticOperator.PLUS;
import static org.junit.Assert.assertEquals;

/**
 * Tests {@link UniqueVariableNames}
 */
public class UniqueVariableNamesTest {

	private UniqueVariableNames uniqueVariableNames;

	@Before
	public void setUp() {
		this.uniqueVariableNames = new UniqueVariableNames();
	}

	@Test
	public void testRenameInSecondNoGood() {
		final Predicate predA = Predicate.getInstance("a", 2);
		final Predicate predB = Predicate.getInstance("b", 2);
		final VariableTerm varX = VariableTerm.getInstance("X");
		final VariableTerm varY = VariableTerm.getInstance("Y");
		final VariableTerm varY1 = VariableTerm.getInstance("Y_1");
		final VariableTerm varY2 = VariableTerm.getInstance("Y_2");
		final VariableTerm varZ = VariableTerm.getInstance("Z");

		final Literal[] literals1 = new Literal[]{
				lit(true, predA, varX, varY),
				lit(false, predA, varX, varY1),
				comp(varY1, plus(varY, ConstantTerm.getInstance(1)), EQ)
		};

		final Literal[] literals2 = new Literal[]{
				lit(true, predB, varY, varZ)
		};

		final Literal[] expectedModifiedLiterals2 = new Literal[]{
				lit(true, predB, varY2, varZ)
		};

		final NonGroundNoGood noGood1 = new NonGroundNoGood(LEARNT, literals1, false);
		final NonGroundNoGood noGood2 = new NonGroundNoGood(STATIC, literals2, false);
		final NonGroundNoGood expectedModifiedNoGood2 = new NonGroundNoGood(STATIC, expectedModifiedLiterals2, false);

		final NonGroundNoGood modifiedNoGood1 = uniqueVariableNames.makeVariableNamesUnique(noGood1);
		final NonGroundNoGood modifiedNoGood2 = uniqueVariableNames.makeVariableNamesUnique(noGood2);

		assertEquals(noGood1, modifiedNoGood1);
		assertEquals(expectedModifiedNoGood2, modifiedNoGood2);
	}

	private BasicLiteral lit(boolean positive, Predicate predicate, Term... terms) {
		return new BasicAtom(predicate, terms).toLiteral(positive);
	}

	private ComparisonLiteral comp(Term term1, Term term2, ComparisonOperator operator) {
		return new ComparisonAtom(term1, term2, operator).toLiteral(true);
	}

	private Term plus(VariableTerm term1, Term term2) {
		return ArithmeticTerm.getInstance(term1, PLUS, term2);
	}

}
