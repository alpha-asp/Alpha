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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Provides test cases for creating nogoods and checking correctness
 * of the atom id translation performed by a {@link AtomizedNoGoodCollection}.
 */
class AtomizedNoGoodCollectionTest {
	AtomStore atomStore;
	TestAtomGenerator atomGenerator;

	public AtomizedNoGoodCollectionTest() {
		atomStore = new AtomStoreImpl();
		atomGenerator = new TestAtomGenerator(atomStore);
	}

	@BeforeEach
	public void setUp() {
		atomStore = new AtomStoreImpl();
		atomGenerator = new TestAtomGenerator(atomStore);
	}


	/**
	 * Creates a nogood with a single literal
	 * and tests conversion correctness after change in the single atom id.
	 */
	@Test
	void oneSingleLiteralNoGood() {
		atomGenerator.generateAndRegisterFillerAtoms(1);
		List<AtomValuePair> literals = Collections.singletonList(
				new AtomValuePair(atomGenerator.generateAndRegisterAtom("_TEST_", 0), true)
		);
		int[] literalIds = atomGenerator.getLiteralIds(literals);
		NoGood noGood = new NoGood(literalIds);

		AtomizedNoGoodCollection atomizedCollection = new AtomizedNoGoodCollection(atomStore);
		atomizedCollection.add(noGood);

		atomStore.reset();
		atomStore.putIfAbsent(literals.get(0).getAtom());
		extractAndCheckNoGoods(Collections.singletonList(literals), atomizedCollection);
	}

	/**
	 * Creates two nogoods with a single literal
	 * and tests conversion correctness after change in an atom id of one of them.
	 */
	@Test
	void twoSingleLiteralNoGoodsChangeIdForOne() {
		atomGenerator.generateAndRegisterFillerAtoms(2);
		List<AtomValuePair> noGood1 = Collections.singletonList(
				new AtomValuePair(atomGenerator.generateAndRegisterAtom("_TEST1_", 0), false)
		);
		List<AtomValuePair> noGood2 = Collections.singletonList(
				new AtomValuePair(atomGenerator.generateAndRegisterAtom("_TEST2_", 2), true)
		);

		AtomizedNoGoodCollection atomizedCollection = new AtomizedNoGoodCollection(atomStore);
		List<List<AtomValuePair>> noGoods = Arrays.asList(noGood1, noGood2);
		addAtomizedNoGoods(atomizedCollection, noGoods);

		atomStore.reset();
		atomGenerator.generateAndRegisterFillerAtoms(2);
		atomStore.putIfAbsent(noGood1.get(0).getAtom());
		atomGenerator.generateAndRegisterFillerAtoms(2, 2);
		atomStore.putIfAbsent(noGood2.get(0).getAtom());
		extractAndCheckNoGoods(noGoods, atomizedCollection);
	}


	/**
	 * Creates a nogood with multiple literals
	 * and tests conversion correctness after change in all atom ids.
	 */
	@Test
	void oneMultiLiteralNoGoodChangeAllIds() {

	}

	/**
	 * Creates a nogood with multiple literals
	 * and tests conversion correctness after change in a single atom id.
	 */
	@Test
	void oneMultiLiteralNoGoodChangeSingleId() {
		atomGenerator.generateAndRegisterFillerAtoms(1);
		List<AtomValuePair> noGood = Arrays.asList(
				new AtomValuePair(atomGenerator.generateAndRegisterAtom("_TEST1_", 0), true),
				new AtomValuePair(atomGenerator.generateAndRegisterAtom("_TEST2_", 2), false)
		);

		AtomizedNoGoodCollection atomizedCollection = new AtomizedNoGoodCollection(atomStore);
		List<List<AtomValuePair>> noGoods = Collections.singletonList(noGood);
		addAtomizedNoGoods(atomizedCollection, noGoods);

		atomStore.reset();
		atomStore.putIfAbsent(noGood.get(0).getAtom());
		atomStore.putIfAbsent(noGood.get(1).getAtom());
		extractAndCheckNoGoods(noGoods, atomizedCollection);
	}

	/**
	 * Creates two nogoods with multiple literals
	 * and tests conversion correctness after change in a single atom id.
	 */
	@Test
	void twoMultiLiteralNoGoodsChangeSingleId() {
		atomGenerator.generateAndRegisterFillerAtoms(2);
		List<AtomValuePair> noGood1 = Arrays.asList(
				new AtomValuePair(atomGenerator.generateAndRegisterAtom("_TEST1_", 0), false),
				new AtomValuePair(atomGenerator.generateAndRegisterAtom("_TEST2_", 0), false)
		);
		List<AtomValuePair> noGood2 = Arrays.asList(
				new AtomValuePair(atomGenerator.generateAndRegisterAtom("_TEST3_", 2), true),
				new AtomValuePair(atomGenerator.generateAndRegisterAtom("_TEST4_", 1), false)
		);

		AtomizedNoGoodCollection atomizedCollection = new AtomizedNoGoodCollection(atomStore);
		List<List<AtomValuePair>> noGoods = Arrays.asList(noGood1, noGood2);
		addAtomizedNoGoods(atomizedCollection, noGoods);

		atomStore.reset();
		atomGenerator.generateAndRegisterFillerAtoms(2);
		atomStore.putIfAbsent(noGood1.get(0).getAtom());
		atomStore.putIfAbsent(noGood1.get(1).getAtom());
		atomStore.putIfAbsent(noGood2.get(0).getAtom());
		atomGenerator.generateAndRegisterFillerAtoms(2, 2);
		atomStore.putIfAbsent(noGood2.get(1).getAtom());
		extractAndCheckNoGoods(noGoods, atomizedCollection);
	}

	/**
	 * Creates two nogoods with multiple literals
	 * and tests conversion correctness after change in all atom ids.
	 */
	@Test
	void twoMultiLiteralNoGoodsChangeAllIds() {
		atomGenerator.generateAndRegisterFillerAtoms(2);
		List<AtomValuePair> noGood1 = Arrays.asList(
				new AtomValuePair(atomGenerator.generateAndRegisterAtom("_TEST1_", 0), false),
				new AtomValuePair(atomGenerator.generateAndRegisterAtom("_TEST2_", 0), true)
		);
		List<AtomValuePair> noGood2 = Arrays.asList(
				new AtomValuePair(atomGenerator.generateAndRegisterAtom("_TEST3_", 2), true),
				new AtomValuePair(atomGenerator.generateAndRegisterAtom("_TEST4_", 1), true)
		);

		AtomizedNoGoodCollection atomizedCollection = new AtomizedNoGoodCollection(atomStore);
		List<List<AtomValuePair>> noGoods = Arrays.asList(noGood1, noGood2);
		addAtomizedNoGoods(atomizedCollection, noGoods);

		atomStore.reset();
		atomStore.putIfAbsent(noGood1.get(0).getAtom());
		atomStore.putIfAbsent(noGood1.get(1).getAtom());
		atomStore.putIfAbsent(noGood2.get(0).getAtom());
		atomStore.putIfAbsent(noGood2.get(1).getAtom());
		extractAndCheckNoGoods(noGoods, atomizedCollection);
	}

	/**
	 * Extracts the {@link NoGood}s from a given {@link AtomizedNoGoodCollection} and asserts that the literals
	 * in these {@link NoGood}s are the same as the literals in the given literal lists.
	 *
	 * @param noGoods            the lists of literals where each nogood is represented by a list that contains pairs
	 *                           of atom and truth value.
	 * @param atomizedCollection the {@link AtomizedNoGoodCollection} to extract the {@link NoGood}s from.
	 */
	private void extractAndCheckNoGoods(List<List<AtomValuePair>> noGoods,
										AtomizedNoGoodCollection atomizedCollection) {
		List<NoGood> noGoodsExtracted = atomizedCollection.getNoGoods();
		assertEquals(noGoods.size(), noGoodsExtracted.size());

		for (int i = 0; i < noGoods.size(); i++) {
			List<AtomValuePair> literals = noGoods.get(i);
			NoGood noGoodExtracted = noGoodsExtracted.get(i);
			checkNoGood(literals, noGoodExtracted);
		}
	}

	/**
	 * Asserts that the literals in the given {@link NoGood} are the same as the given literals.
	 *
	 * @param literals the literals to compare to represented as pairs of atom and truth value.
	 * @param noGood   the given {@link NoGood} to check the literals of.
	 */
	private void checkNoGood(List<AtomValuePair> literals, NoGood noGood) {
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

	/**
	 * Adds the given nogoods to the given {@link AtomizedNoGoodCollection}.
	 *
	 * @param atomizedCollection the {@link AtomizedNoGoodCollection} to add to.
	 * @param noGoods            the nogoods to add to the collection. Each nogood is represented by a list that contains pairs
	 *                           of atom and truth value.
	 */
	private void addAtomizedNoGoods(AtomizedNoGoodCollection atomizedCollection,
									List<List<AtomValuePair>> noGoods) {
		for (List<AtomValuePair> literals : noGoods) {
			int[] literalIds = atomGenerator.getLiteralIds(literals);
			NoGood noGood = new NoGood(literalIds);
			atomizedCollection.add(noGood);
		}
	}
}