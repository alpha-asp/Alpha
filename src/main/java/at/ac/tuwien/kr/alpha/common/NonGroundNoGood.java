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
import at.ac.tuwien.kr.alpha.grounder.NoGoodGenerator;
import at.ac.tuwien.kr.alpha.solver.Antecedent;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static at.ac.tuwien.kr.alpha.Util.join;
import static at.ac.tuwien.kr.alpha.Util.oops;
import static at.ac.tuwien.kr.alpha.common.Literals.atomOf;
import static at.ac.tuwien.kr.alpha.common.Literals.isPositive;
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

	protected final List<Literal> literals;
	private final boolean head;
	private final Type type;

	public NonGroundNoGood(List<Literal> literals) {
		this(STATIC, literals, false);
	}

	public NonGroundNoGood(Type type, List<Literal> literals) {
		this(type, literals, false);
	}

	private NonGroundNoGood(Type type, List<Literal> literals, boolean head) {
		this.type = type;
		this.head = head;
		if (head && !literals.get(HEAD).isNegated()) {
			throw oops("Head is not negative");
		}
		this.literals = literals;
	}

	public static NonGroundNoGood forGroundNoGood(NoGood groundNoGood, Map<Integer, Atom> atomMapping) {
		final List<Literal> literals = literalsForGroundNoGood(groundNoGood, atomMapping);
		return new NonGroundNoGood(groundNoGood.getType(), literals, groundNoGood.hasHead());
	}

	public static NonGroundNoGood fromBody(NoGood groundNoGood, NoGoodGenerator.CollectedLiterals posLiterals, NoGoodGenerator.CollectedLiterals negLiterals, Literal nonGroundBodyRepresentingLiteral, Map<Integer, Atom> atomMapping) {
		final List<Literal> literals = literalsForGroundNoGood(groundNoGood, atomMapping);
		literals.addAll(posLiterals.getSkippedFacts());
		literals.addAll(posLiterals.getSkippedFixedInterpretationLiterals());
		literals.addAll(negLiterals.getSkippedFacts().stream().map(Literal::negate).collect(Collectors.toList()));
		literals.addAll(negLiterals.getSkippedFixedInterpretationLiterals().stream().map(Literal::negate).collect(Collectors.toList()));
		return new NonGroundNoGood(groundNoGood.getType(), literals, groundNoGood.hasHead());
	}

	private static List<Literal> literalsForGroundNoGood(NoGood groundNoGood, Map<Integer, Atom> atomMapping) {
		final List<Literal> literals = new ArrayList<>(groundNoGood.size());
		for (int groundLiteral : groundNoGood) {
			literals.add(atomMapping.get(atomOf(groundLiteral)).toLiteral(isPositive(groundLiteral)));
		}
		return literals;
	}

	@Override
	public Literal getLiteral(int index) {
		return literals.get(index);
	}

	@Override
	public boolean hasHead() {
		return head;
	}

	@Override
	public int size() {
		return literals.size();
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
		return literals.iterator();
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
				literals.equals(that.literals) &&
				type == that.type;
	}

	@Override
	public int hashCode() {
		return Objects.hash(literals, head, type);
	}

	@Override
	public String toString() {
		final List<String> stringLiterals = new ArrayList<>(literals.size());
		for (Literal literal : literals) {
			stringLiterals.add((literal.isNegated() ? "-" : "+") + "(" + literal.getAtom() + ")");
		}
		return (head ? "*" : "") + join("{ ", stringLiterals, ", ", " }");
	}
}
