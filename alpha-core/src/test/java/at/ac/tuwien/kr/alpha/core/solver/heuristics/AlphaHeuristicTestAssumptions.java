/**
 * Copyright (c) 2017-2018 Siemens AG
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
package at.ac.tuwien.kr.alpha.core.solver.heuristics;

import static at.ac.tuwien.kr.alpha.core.atoms.Literals.atomOf;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import at.ac.tuwien.kr.alpha.api.programs.atoms.Atom;
import at.ac.tuwien.kr.alpha.commons.Predicates;
import at.ac.tuwien.kr.alpha.commons.atoms.Atoms;
import at.ac.tuwien.kr.alpha.commons.rules.heads.Heads;
import at.ac.tuwien.kr.alpha.core.common.AtomStore;
import at.ac.tuwien.kr.alpha.core.common.AtomStoreImpl;
import at.ac.tuwien.kr.alpha.core.common.NoGood;
import at.ac.tuwien.kr.alpha.core.grounder.Grounder;
import at.ac.tuwien.kr.alpha.core.grounder.NaiveGrounder;
import at.ac.tuwien.kr.alpha.core.programs.CompiledProgram;
import at.ac.tuwien.kr.alpha.core.programs.InternalProgram;
import at.ac.tuwien.kr.alpha.core.rules.CompiledRule;
import at.ac.tuwien.kr.alpha.core.rules.InternalRule;
import at.ac.tuwien.kr.alpha.core.solver.ChoiceManager;
import at.ac.tuwien.kr.alpha.core.solver.NaiveNoGoodStore;
import at.ac.tuwien.kr.alpha.core.solver.TrailAssignment;
import at.ac.tuwien.kr.alpha.core.solver.WritableAssignment;

/**
 * Tests assumptions made by {@link DependencyDrivenHeuristic} and other domain-independent heuristics.
 * Even if these test cases do not test {@link DependencyDrivenHeuristic} directly,
 * it will break if these test cases break.
 * 
 * Copyright (c) 2017-2018 Siemens AG
 *
 */
public class AlphaHeuristicTestAssumptions {

	private Grounder grounder;
	private WritableAssignment assignment;
	private ChoiceManager choiceManager;
	private AtomStore atomStore;

	@BeforeEach
	public void setUp() {
		/* program :=
		 *     b1. b2.
		 *     b3 :- not nb3.
		 *     nb3 :- not b3.
		 *     b4 :- not nb4.
		 *     nb4 :- not b4.
		 *     h :- b1, b2, not b3, not b4.
		 */
		List<Atom> facts = Arrays.asList(Atoms.newBasicAtom(Predicates.getPredicate("b1", 0)), Atoms.newBasicAtom(Predicates.getPredicate("b2", 0)));
		List<CompiledRule> rules = Arrays.asList(
				new InternalRule(Heads.newNormalHead(Atoms.newBasicAtom(Predicates.getPredicate("b3", 0))), Atoms.newBasicAtom(Predicates.getPredicate("nb3", 0)).toLiteral(false)),
				new InternalRule(Heads.newNormalHead(Atoms.newBasicAtom(Predicates.getPredicate("nb3", 0))), Atoms.newBasicAtom(Predicates.getPredicate("b3", 0)).toLiteral(false)),
				new InternalRule(Heads.newNormalHead(Atoms.newBasicAtom(Predicates.getPredicate("b4", 0))), Atoms.newBasicAtom(Predicates.getPredicate("nb4", 0)).toLiteral(false)),
				new InternalRule(Heads.newNormalHead(Atoms.newBasicAtom(Predicates.getPredicate("nb4", 0))), Atoms.newBasicAtom(Predicates.getPredicate("b4", 0)).toLiteral(false)),
				new InternalRule(Heads.newNormalHead(Atoms.newBasicAtom(Predicates.getPredicate("h", 0))),
						Atoms.newBasicAtom(Predicates.getPredicate("b1", 0)).toLiteral(),
						Atoms.newBasicAtom(Predicates.getPredicate("b2", 0)).toLiteral(),
						Atoms.newBasicAtom(Predicates.getPredicate("b3", 0)).toLiteral(false),
						Atoms.newBasicAtom(Predicates.getPredicate("b4", 0)).toLiteral(false))
				);
		CompiledProgram program = new InternalProgram(rules, facts);
		
		atomStore = new AtomStoreImpl();
		grounder = new NaiveGrounder(program, atomStore, true);
		assignment = new TrailAssignment(atomStore);
		choiceManager = new ChoiceManager(assignment, new NaiveNoGoodStore(assignment));
	}

	@Test
	public void testNumbersOfNoGoods_GrounderIsAtomChoicePoint() {
		testNumbersOfNoGoods(atomStore::isAtomChoicePoint);
	}

	@Test
	public void testNumbersOfNoGoods_ChoiceManagerIsAtomChoice() {
		testNumbersOfNoGoods(choiceManager::isAtomChoice);
	}

	private void testNumbersOfNoGoods(Predicate<? super Integer> isRuleBody) {
		int n = 0;
		int bodyNotHead = 0;
		int bodyElementsNotBody = 0;
		int noHead = 0;
		int other = 0;

		Collection<NoGood> noGoods = getNoGoods();
		assignment.growForMaxAtomId();
		choiceManager.growForMaxAtomId(atomStore.getMaxAtomId());
		choiceManager.addChoiceInformation(grounder.getChoiceAtoms(), grounder.getHeadsToBodies());
		for (NoGood noGood : noGoods) {
			n++;
			boolean knownType = false;
			if (DependencyDrivenHeuristic.isBodyNotHead(noGood, isRuleBody)) {
				bodyNotHead++;
				knownType = true;
			}
			if (DependencyDrivenHeuristic.isBodyElementsNotBody(noGood, isRuleBody)) {
				bodyElementsNotBody++;
				knownType = true;
			}
			if (!noGood.hasHead()) {
				noHead++;
				knownType = true;
			}
			if (!knownType) {
				other++;
			}
		}

		System.out.println(noGoods.stream().map(atomStore::noGoodToString).collect(Collectors.joining(", ")));

		assertEquals(5, bodyNotHead, "Unexpected number of bodyNotHead nogoods");
		assertEquals(5, bodyElementsNotBody, "Unexpected number of bodyElementsNotBody nogoods");
		assertGreaterThan("Unexpected number of nogoods without head", 4, noHead);

		// there may be other nogoods (e.g. for ChoiceOn, ChoiceOff) which we do not care for here
		System.out.println("Total number of NoGoods: " + n);
		System.out.println("Number of NoGoods of unknown type: " + other);
	}

	@Test
	public void testIsAtomChoice_GrounderIsAtomChoicePoint() {
		testIsAtomChoice(atomStore::isAtomChoicePoint);
	}

	@Test
	public void testIsAtomChoice_ChoiceManagerIsAtomChoice() {
		testIsAtomChoice(choiceManager::isAtomChoice);
	}

	private void testIsAtomChoice(Predicate<? super Integer> isRuleBody) {
		Collection<NoGood> noGoods = getNoGoods();
		assignment.growForMaxAtomId();
		choiceManager.growForMaxAtomId(atomStore.getMaxAtomId());
		choiceManager.addChoiceInformation(grounder.getChoiceAtoms(), grounder.getHeadsToBodies());
		for (NoGood noGood : noGoods) {
			for (Integer literal : noGood) {
				int atom = atomOf(literal);
				String atomToString = atomStore.atomToString(atom);
				if (atomToString.startsWith("_R_")) {
					assertTrue(isRuleBody.test(atom), "Atom not choice: " + atomToString);
				}
			}
		}
	}

	private Collection<NoGood> getNoGoods() {
		return grounder.getNoGoods(null).values();
	}

	private void assertGreaterThan(String message, long expected, long actual) {
		assertTrue(actual > expected, message);
	}
}
