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

import at.ac.tuwien.kr.alpha.common.atoms.Atom;
import at.ac.tuwien.kr.alpha.common.atoms.Literal;
import at.ac.tuwien.kr.alpha.common.terms.VariableTerm;
import at.ac.tuwien.kr.alpha.grounder.NoGoodGenerator;
import at.ac.tuwien.kr.alpha.solver.Antecedent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static at.ac.tuwien.kr.alpha.Util.join;
import static at.ac.tuwien.kr.alpha.Util.oops;
import static at.ac.tuwien.kr.alpha.common.Literals.atomOf;
import static at.ac.tuwien.kr.alpha.common.Literals.isPositive;
import static at.ac.tuwien.kr.alpha.common.NoGoodInterface.Type.LEARNT;
import static at.ac.tuwien.kr.alpha.common.NoGoodInterface.Type.STATIC;

/**
 * Represents a non-ground nogood that corresponds to several ground nogoods generated from it.
 * <p>
 * This class assumes the following to hold without checking it:
 * <ul>
 *     <li>no literal appears twice (different variable names are OK, if different literals correspond to different ground literals)</li>
 *     <li>the non-ground literals here appear in the same order as the corresponding ground literals in the corresponding ground nogood</li>
 *     <li>any literals not present in the corresponding ground nogood (e.g., arithmetic literals removed by the grounder)
 *         appear after all literals present in the ground nogood (i.e., if the ground nogood contains N literals, then
 *         the first N literals in the non-ground nogood correspond directly to ground literals at the same positions in
 *         the ground nogood, and all positions from N+1 till the end are for literals not present in the ground nogood)</li>
 * </ul>
 */
public class NonGroundNoGood implements NoGoodInterface<Literal> {

	protected final Literal[] literals;
	private final Literal[] sortedLiterals;
	private final boolean head;
	private final Type type;

	public NonGroundNoGood(Literal... literals) {
		this(STATIC, literals, false);
	}

	public NonGroundNoGood(Type type, Literal... literals) {
		this(type, literals, false);
	}

	NonGroundNoGood(Type type, Literal[] literals, boolean head) {
		this.type = type;
		this.head = head;
		if (head && !literals[HEAD].isNegated()) {
			throw oops("Head is not negative");
		}

		this.literals = Arrays.copyOf(literals, literals.length);
		// note: literals are not sorted here (in contrast to ground NoGood) because of the assumption that the literals
		// appear in the same order as in the corresponding ground nogood
		// however, we maintain a second array of literals that is sorted for comparison purposes:
		// (the following code is duplicated from the constructor of NoGood)

		Arrays.sort(literals, head ? 1 : 0, literals.length);

		int shift = 0;
		for (int i = 1; i < literals.length; i++) {
			if (literals[i - 1] == literals[i]) { // check for duplicate
				shift++;
			}
			literals[i - shift] = literals[i]; // Remove duplicates in place by shifting remaining literals.
		}

		// copy-shrink array if needed.
		this.sortedLiterals = shift <= 0 ? literals : Arrays.copyOf(literals, literals.length - shift);
	}

	public static NonGroundNoGood forGroundNoGood(NoGood groundNoGood, Map<Integer, Atom> atomMapping) {
		return new NonGroundNoGood(groundNoGood.getType(), literalsForGroundNoGood(groundNoGood, atomMapping), groundNoGood.hasHead());
	}

	public static NonGroundNoGood fromBody(NoGood groundNoGood, NoGoodGenerator.CollectedLiterals posLiterals, NoGoodGenerator.CollectedLiterals negLiterals, Map<Integer, Atom> atomMapping) {
		final List<Literal> literals = new ArrayList<>(Arrays.asList(literalsForGroundNoGood(groundNoGood, atomMapping)));
		literals.addAll(posLiterals.getSkippedFacts());
		literals.addAll(posLiterals.getSkippedFixedInterpretationLiterals());
		literals.addAll(negLiterals.getSkippedFacts().stream().map(Literal::negate).collect(Collectors.toList()));
		literals.addAll(negLiterals.getSkippedFixedInterpretationLiterals().stream().map(Literal::negate).collect(Collectors.toList()));
		return new NonGroundNoGood(groundNoGood.getType(), literals.toArray(new Literal[]{}), groundNoGood.hasHead());
	}

	public static NonGroundNoGood learnt(Collection<Literal> literals) {
		return new NonGroundNoGood(LEARNT, literals.toArray(new Literal[]{}));
	}

	private static Literal[] literalsForGroundNoGood(NoGood groundNoGood, Map<Integer, Atom> atomMapping) {
		final Literal[] literals = new Literal[groundNoGood.size()];
		for (int i = 0; i < groundNoGood.size(); i++) {
			final int groundLiteral = groundNoGood.getLiteral(i);
			literals[i] = atomMapping.get(atomOf(groundLiteral)).toLiteral(isPositive(groundLiteral));
		}
		return literals;
	}

	@Override
	public Literal getLiteral(int index) {
		return literals[index];
	}

	@Override
	public boolean hasHead() {
		return head;
	}

	@Override
	public int size() {
		return literals.length;
	}

	public Set<VariableTerm> getOccurringVariables() {
		final Set<VariableTerm> occurringVariables = new HashSet<>();
		for (Literal literal : literals) {
			occurringVariables.addAll(literal.getOccurringVariables());
		}
		return occurringVariables;
	}

	@Override
	public Antecedent asAntecedent() {
		throw new UnsupportedOperationException("Non-ground nogood cannot be represented as an antecedent");
	}

	@Override
	public Type getType() {
		return type;
	}

	@Override
	public Iterator<Literal> iterator() {
		return new Iterator<Literal>() {
			private int i;

			@Override
			public boolean hasNext() {
				return literals.length > i;
			}

			@Override
			public Literal next() {
				return literals[i++];
			}
		};
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		NonGroundNoGood that = (NonGroundNoGood) o;
		return head == that.head &&
				Arrays.equals(sortedLiterals, that.sortedLiterals) &&
				type == that.type;
	}

	@Override
	public int hashCode() {
		return Objects.hash(Arrays.hashCode(sortedLiterals), head, type);
	}

	@Override
	public String toString() {
		final List<String> stringLiterals = new ArrayList<>(literals.length);
		for (Literal literal : literals) {
			stringLiterals.add((literal.isNegated() ? "-" : "+") + "(" + literal.getAtom() + ")");
		}
		return (head ? "*" : "") + join("{ ", stringLiterals, ", ", " }");
	}
}
