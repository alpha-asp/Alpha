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
package at.ac.tuwien.kr.alpha.solver.heuristics;

import at.ac.tuwien.kr.alpha.common.AtomStore;
import at.ac.tuwien.kr.alpha.common.AtomStoreTest;
import at.ac.tuwien.kr.alpha.common.Literals;
import at.ac.tuwien.kr.alpha.common.NoGood;
import at.ac.tuwien.kr.alpha.solver.NoGoodStoreAlphaRoaming;
import at.ac.tuwien.kr.alpha.solver.WritableAssignment;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;

public class HeuristicTestUtils {

	static void addNoGoods(AtomStore atomStore, WritableAssignment assignment, NoGoodStoreAlphaRoaming noGoodStore, HeapOfActiveAtoms heapOfActiveAtoms, NoGood... noGoods) {
		int numberOfAtoms = Arrays.stream(noGoods).flatMapToInt(NoGood::stream).map(Literals::atomOf).max().getAsInt();
		AtomStoreTest.fillAtomStore(atomStore, numberOfAtoms);
		assignment.growForMaxAtomId();
		noGoodStore.growForMaxAtomId(numberOfAtoms);
		Collection<NoGood> setOfNoGoods = new HashSet<>();
		int noGoodId = 1;
		for (NoGood noGood : noGoods) {
			setOfNoGoods.add(noGood);
			noGoodStore.add(noGoodId++, noGood);
		}
		heapOfActiveAtoms.initActity(setOfNoGoods);
	}

}
