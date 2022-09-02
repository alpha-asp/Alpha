/**
 * Copyright (c) 2022, the Alpha Team.
 * All rights reserved.
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
package at.ac.tuwien.kr.alpha.core.solver.reset;

import static org.junit.jupiter.api.Assertions.assertEquals;

import at.ac.tuwien.kr.alpha.api.programs.atoms.Atom;
import at.ac.tuwien.kr.alpha.core.atoms.Literals;
import at.ac.tuwien.kr.alpha.core.common.AtomStore;
import at.ac.tuwien.kr.alpha.core.common.AtomStoreImpl;
import at.ac.tuwien.kr.alpha.core.common.NoGood;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Provides test cases for creating nogoods and checking correctness
 * of the atom id translation performed by a {@link NoGoodAtomizer}.
 */
class NoGoodAtomizerTest {
	AtomStore atomStore;
	TestAtomGenerator atomGenerator;

	public NoGoodAtomizerTest() {
		atomStore = new AtomStoreImpl();
		atomGenerator = new TestAtomGenerator(atomStore);
	}

	@BeforeEach
	public void setUp() {
		atomStore = new AtomStoreImpl();
		atomGenerator = new TestAtomGenerator(atomStore);
	}

	/**
	 * Registers multiple atoms at the {@link AtomStore} and resets the {@link AtomStore}.
	 * Then registers the same atoms in the same order again and checks whether all of them receive
	 * the same id as before.
	 */
	@Test
	void sameAtomIdsAfterAtomStoreReset() {
		int atomCount = 6;

		List<Atom> atoms = new ArrayList<>();
		List<Integer> atomIds = new ArrayList<>();
		for (int i = 0; i < atomCount; i++) {
			Atom atom = atomGenerator.generateAndRegisterAtom(String.format("_TEST%d_", i), 0);
			atoms.add(atom);
			atomIds.add(atomStore.get(atom));
		}

		atomStore.reset();
		for (int i = 0; i < atomCount; i++) {
			Atom atom = atoms.get(i);
			atomStore.putIfAbsent(atom);
			assertEquals(atomIds.get(i), atomStore.get(atom));
		}
	}

	/**
	 * Creates a nogood with a single literal
	 * and tests conversion correctness after change in atom id.
	 */
	@Test
	void singleLiteralNoGood() {
		List<AtomValuePair> literals = Collections.singletonList(
				new AtomValuePair(atomGenerator.generateAndRegisterAtom("_TEST_", 0), false)
		);
		int[] literalIds = atomGenerator.getLiteralIds(literals);
		NoGoodAtomizer noGoodAtomizer = new NoGoodAtomizer(new NoGood(literalIds), atomStore);

		atomStore.reset();
		atomGenerator.generateAndRegisterFillerAtoms(1);
		atomStore.putIfAbsent(literals.get(0).getAtom());
		extractAndCheckLiterals(literals, noGoodAtomizer);
	}

	/**
	 * Creates a nogood with a single literal of arity > 0
	 * and tests conversion correctness after change in atom id.
	 */
	@Test
	void singleTwoTermLiteralNoGood() {
		atomGenerator.generateAndRegisterFillerAtoms(1);
		List<AtomValuePair> literals = Collections.singletonList(
				new AtomValuePair(atomGenerator.generateAndRegisterAtom("_TEST_", 2), true)
		);
		int[] literalIds = atomGenerator.getLiteralIds(literals);
		NoGoodAtomizer noGoodAtomizer = new NoGoodAtomizer(new NoGood(literalIds), atomStore);

		atomStore.reset();
		atomStore.putIfAbsent(literals.get(0).getAtom());
		extractAndCheckLiterals(literals, noGoodAtomizer);
	}

	/**
	 * Creates a nogood with multiple literals
	 * and tests conversion correctness after change in a single atom id.
	 */
	@Test
	void multiLiteralNoGoodChangeSingleId() {
		atomGenerator.generateAndRegisterFillerAtoms(2);
		List<AtomValuePair> literals = Arrays.asList(
				new AtomValuePair(atomGenerator.generateAndRegisterAtom("_TEST1_", 0), true),
				new AtomValuePair(atomGenerator.generateAndRegisterAtom("_TEST2_", 0), false)
		);
		int[] literalIds = atomGenerator.getLiteralIds(literals);
		NoGoodAtomizer noGoodAtomizer = new NoGoodAtomizer(new NoGood(literalIds), atomStore);

		atomStore.reset();
		atomGenerator.generateAndRegisterFillerAtoms(2);
		atomStore.putIfAbsent(literals.get(0).getAtom());
		atomGenerator.generateAndRegisterFillerAtoms(2, 2);
		atomStore.putIfAbsent(literals.get(1).getAtom());
		extractAndCheckLiterals(literals, noGoodAtomizer);
	}

	/**
	 * Creates a nogood with multiple literals
	 * and tests conversion correctness after change in all atom ids.
	 */
	@Test
	void multiLiteralNoGoodChangeAllIds() {
		atomGenerator.generateAndRegisterFillerAtoms(2);
		List<AtomValuePair> literals = Arrays.asList(
				new AtomValuePair(atomGenerator.generateAndRegisterAtom("_TEST1_", 0), true),
				new AtomValuePair(atomGenerator.generateAndRegisterAtom("_TEST2_", 0), false)
		);
		int[] literalIds = atomGenerator.getLiteralIds(literals);
		NoGoodAtomizer noGoodAtomizer = new NoGoodAtomizer(new NoGood(literalIds), atomStore);

		atomStore.reset();
		atomStore.putIfAbsent(literals.get(0).getAtom());
		atomStore.putIfAbsent(literals.get(1).getAtom());
		extractAndCheckLiterals(literals, noGoodAtomizer);
	}

	/**
	 * Extracts the {@link NoGood} from a given {@link NoGoodAtomizer} and asserts that the literals in this
	 * {@link NoGood} are the same as the given literals.
	 *
	 * @param literals       a list of literals where each literal is represented by a pair of atom and truth value.
	 * @param noGoodAtomizer the {@link NoGoodAtomizer} to extract the {@link NoGood} from.
	 */
	private void extractAndCheckLiterals(List<AtomValuePair> literals, NoGoodAtomizer noGoodAtomizer) {
		NoGood noGood = noGoodAtomizer.deatomize(atomStore);
		assertEquals(literals.size(), noGood.size());

		for (int i = 0; i < literals.size(); i++) {
			AtomValuePair literal = literals.get(i);
			int literalExtractedId = noGood.getLiteral(i);

			Atom atomExtracted = atomStore.get(Literals.atomOf(literalExtractedId));
			assertEquals(literal.getAtom(), atomExtracted);

			boolean truthValueExtracted = Literals.isPositive(literalExtractedId);
			assertEquals(literal.isPositive(), truthValueExtracted);
		}
	}
}