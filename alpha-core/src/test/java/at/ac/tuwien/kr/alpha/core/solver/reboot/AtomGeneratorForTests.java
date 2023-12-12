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
package at.ac.tuwien.kr.alpha.core.solver.reboot;

import at.ac.tuwien.kr.alpha.api.programs.Predicate;
import at.ac.tuwien.kr.alpha.api.programs.atoms.Atom;
import at.ac.tuwien.kr.alpha.api.programs.terms.Term;
import at.ac.tuwien.kr.alpha.commons.Predicates;
import at.ac.tuwien.kr.alpha.commons.programs.atoms.Atoms;
import at.ac.tuwien.kr.alpha.commons.programs.terms.Terms;
import at.ac.tuwien.kr.alpha.core.common.AtomStore;
import at.ac.tuwien.kr.alpha.core.programs.atoms.Literals;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Provides utility methods to generate atoms and register them at an {@link AtomStore}.
 */
public class AtomGeneratorForTests {
	/**
	 * Maps a given list of literals to literal ids based on a given {@link AtomStore}.
	 *
	 * @param literals  the list of literals where each literal is represented by a pair of atom and truth value.
	 * @param atomStore the {@link AtomStore} to use for converting atom ids.
	 * @return the list of ids for the given literals based on the current {@link AtomStore}.
	 */
	public static int[] getLiteralIds(List<AtomValuePair> literals, AtomStore atomStore) {
		int[] literalIds = new int[literals.size()];

		int position = 0;
		for (AtomValuePair literal : literals) {
			literalIds[position] = Literals.atomToLiteral(atomStore.get(literal.getAtom()), literal.isPositive());
			position++;
		}

		return literalIds;
	}

	/**
	 * Generates a given number of new filler atoms and registers them at the current {@link AtomStore}.
	 *
	 * @param count     the number of filler atoms to generate and register.
	 * @param atomStore the {@link AtomStore} to use for converting atom ids.
	 */
	public static void generateAndRegisterFillerAtoms(int count, AtomStore atomStore) {
		generateAndRegisterFillerAtoms(count, 0, atomStore);
	}

	/**
	 * Generates a given number of new filler atoms and registers them at the current {@link AtomStore}.
	 * Filler atoms are generated under the assumption that a given number of filler atoms were already generated.
	 *
	 * @param count     the number of filler atoms to generate and register.
	 * @param existing  the number of filler atoms already generated before.
	 * @param atomStore the {@link AtomStore} to use for converting atom ids.
	 */
	public static void generateAndRegisterFillerAtoms(int count, int existing, AtomStore atomStore) {
		for (int i = existing; i < existing + count; i++) {
			generateAndRegisterAtom(String.format("_FILL%d_", i), 0, atomStore);
		}
	}

	/**
	 * Generates a new {@link Atom} with a given predicate symbol and arity.
	 * The generated {@link Atom} is then registered at the current {@link AtomStore}.
	 * Constant terms starting from 1 up to the arity are used.
	 *
	 * @param symbol    the predicate symbol of the {@link Atom} to generate.
	 * @param arity     the arity of the atom to generate.
	 * @param atomStore the {@link AtomStore} to use for converting atom ids.
	 * @return the generated {@link Atom}.
	 */
	public static Atom generateAndRegisterAtom(String symbol, int arity, AtomStore atomStore) {
		Predicate predicate = Predicates.getPredicate(symbol, arity);

		Atom atom;
		if (arity == 0) {
			atom = Atoms.newBasicAtom(predicate);
		} else {
			List<Term> terms = IntStream.iterate(1, x -> x + 1).limit(arity)
					.mapToObj(Integer::toString)
					.map(Terms::newConstant)
					.collect(Collectors.toList());
			atom = Atoms.newBasicAtom(predicate, terms);
		}

		atomStore.putIfAbsent(atom);
		return atom;
	}
}
