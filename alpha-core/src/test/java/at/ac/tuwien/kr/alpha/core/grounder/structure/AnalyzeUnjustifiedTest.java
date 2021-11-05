/**
 * Copyright (c) 2018-2019, the Alpha Team.
 * All rights reserved.
 * 
 * Additional changes made by Siemens.
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
package at.ac.tuwien.kr.alpha.core.grounder.structure;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;

import org.junit.jupiter.api.Test;

import at.ac.tuwien.kr.alpha.api.programs.Predicate;
import at.ac.tuwien.kr.alpha.api.programs.atoms.Atom;
import at.ac.tuwien.kr.alpha.api.programs.atoms.BasicAtom;
import at.ac.tuwien.kr.alpha.api.programs.literals.Literal;
import at.ac.tuwien.kr.alpha.commons.Predicates;
import at.ac.tuwien.kr.alpha.commons.atoms.Atoms;
import at.ac.tuwien.kr.alpha.commons.rules.heads.Heads;
import at.ac.tuwien.kr.alpha.commons.terms.Terms;
import at.ac.tuwien.kr.alpha.core.common.AtomStore;
import at.ac.tuwien.kr.alpha.core.common.AtomStoreImpl;
import at.ac.tuwien.kr.alpha.core.grounder.NaiveGrounder;
import at.ac.tuwien.kr.alpha.core.programs.CompiledProgram;
import at.ac.tuwien.kr.alpha.core.programs.InternalProgram;
import at.ac.tuwien.kr.alpha.core.rules.InternalRule;
import at.ac.tuwien.kr.alpha.core.solver.ThriceTruth;
import at.ac.tuwien.kr.alpha.core.solver.TrailAssignment;

/**
 * Copyright (c) 2018-2020, the Alpha Team.
 */
public class AnalyzeUnjustifiedTest {

	@Test
	public void justifySimpleRules() {
		/*
		 * program :=
		 * p(X) :- q(X).
		 * q(X) :- p(X).
		 * q(5) :- r.
		 * r :- not nr.
		 * nr :- not r.
		 * :- not p(5).
		 */
		CompiledProgram program = new InternalProgram(Arrays.asList(
				new InternalRule(Heads.newNormalHead(Atoms.newBasicAtom(Predicates.getPredicate("p", 1), Terms.newVariable("X"))),
						Atoms.newBasicAtom(Predicates.getPredicate("q", 1), Terms.newVariable("X")).toLiteral()),
				new InternalRule(Heads.newNormalHead(Atoms.newBasicAtom(Predicates.getPredicate("q", 1), Terms.newVariable("X"))),
						Atoms.newBasicAtom(Predicates.getPredicate("p", 1), Terms.newVariable("X")).toLiteral()),
				new InternalRule(Heads.newNormalHead(Atoms.newBasicAtom(Predicates.getPredicate("q", 1), Terms.newConstant(5))),
						Atoms.newBasicAtom(Predicates.getPredicate("r", 0)).toLiteral()),
				new InternalRule(Heads.newNormalHead(Atoms.newBasicAtom(Predicates.getPredicate("r", 0))),
						Atoms.newBasicAtom(Predicates.getPredicate("nr", 0)).toLiteral(false)),
				new InternalRule(Heads.newNormalHead(Atoms.newBasicAtom(Predicates.getPredicate("nr", 0))),
						Atoms.newBasicAtom(Predicates.getPredicate("r", 0)).toLiteral(false)),
				new InternalRule(null,
						Atoms.newBasicAtom(Predicates.getPredicate("p", 1), Terms.newConstant(5)).toLiteral(false))),
				Collections.emptyList());

		AtomStore atomStore = new AtomStoreImpl();
		NaiveGrounder grounder = new NaiveGrounder(program, atomStore, true);
		grounder.getNoGoods(null);
		TrailAssignment assignment = new TrailAssignment(atomStore);
		int rId = atomStore.get(Atoms.newBasicAtom(Predicates.getPredicate("r", 0)));
		int nrId = atomStore.get(Atoms.newBasicAtom(Predicates.getPredicate("nr", 0)));
		assignment.growForMaxAtomId();
		assignment.assign(rId, ThriceTruth.FALSE);
		assignment.assign(nrId, ThriceTruth.TRUE);
		BasicAtom p5 = Atoms.newBasicAtom(Predicates.getPredicate("p", 1), Collections.singletonList(Terms.newConstant(5)));
		assignment.assign(atomStore.get(p5), ThriceTruth.MBT);
		Set<Literal> reasons = grounder.justifyAtom(atomStore.get(p5), assignment);
		assertFalse(reasons.isEmpty());
	}

	@Test
	public void justifyLargerRules() {
		/*
		 * program :=
		 *     dom(1). dom(2). dom(3).
		 *     p(X) :- q(X,Y), r(Y), not s(X,Y).
		 *     q(1,X) :- dom(X), not nq(1, X).
		 *     nq(1, X) :- dom(X), not q(1, X).
		 *     r(X) :- p(X), not nr(X).
		 *     nr(X) :- p(X), not r(X).
		 *     r(2) :- not n1r(2).
		 *     n1r(2) :- not r(2).
		 *     s(1, 2) :- not ns(1, 2).
		 *     ns(1, 2) :- not s(1, 2).
		 *     :- not p(1).
		 */
		CompiledProgram program = new InternalProgram(Arrays.asList(
				new InternalRule(Heads.newNormalHead(Atoms.newBasicAtom(Predicates.getPredicate("p", 1), Terms.newVariable("X"))),
						Atoms.newBasicAtom(Predicates.getPredicate("q", 2), Terms.newVariable("X"), Terms.newVariable("Y")).toLiteral(),
						Atoms.newBasicAtom(Predicates.getPredicate("r", 1), Terms.newVariable("Y")).toLiteral(),
						Atoms.newBasicAtom(Predicates.getPredicate("s", 2), Terms.newVariable("X"), Terms.newVariable("Y")).toLiteral(false)),
				new InternalRule(Heads.newNormalHead(Atoms.newBasicAtom(Predicates.getPredicate("q", 2), Terms.newConstant(1), Terms.newVariable("X"))),
						Atoms.newBasicAtom(Predicates.getPredicate("dom", 1), Terms.newVariable("X")).toLiteral(),
						Atoms.newBasicAtom(Predicates.getPredicate("nq", 2), Terms.newConstant(1), Terms.newVariable("X")).toLiteral(false)),
				new InternalRule(Heads.newNormalHead(Atoms.newBasicAtom(Predicates.getPredicate("nq", 2), Terms.newConstant(1), Terms.newVariable("X"))),
						Atoms.newBasicAtom(Predicates.getPredicate("dom", 1), Terms.newVariable("X")).toLiteral(),
						Atoms.newBasicAtom(Predicates.getPredicate("q", 2), Terms.newConstant(1), Terms.newVariable("X")).toLiteral(false)),
				new InternalRule(Heads.newNormalHead(Atoms.newBasicAtom(Predicates.getPredicate("r", 1), Terms.newVariable("X"))),
						Atoms.newBasicAtom(Predicates.getPredicate("p", 1), Terms.newVariable("X")).toLiteral(),
						Atoms.newBasicAtom(Predicates.getPredicate("nr", 1), Terms.newVariable("X")).toLiteral(false)),
				new InternalRule(Heads.newNormalHead(Atoms.newBasicAtom(Predicates.getPredicate("nr", 1), Terms.newVariable("X"))),
						Atoms.newBasicAtom(Predicates.getPredicate("p", 1), Terms.newVariable("X")).toLiteral(),
						Atoms.newBasicAtom(Predicates.getPredicate("r", 1), Terms.newVariable("X")).toLiteral(false)),
				new InternalRule(Heads.newNormalHead(Atoms.newBasicAtom(Predicates.getPredicate("r", 1), Terms.newConstant(2))),
						Atoms.newBasicAtom(Predicates.getPredicate("n1r", 1), Terms.newConstant(2)).toLiteral(false)),
				new InternalRule(Heads.newNormalHead(Atoms.newBasicAtom(Predicates.getPredicate("n1r", 1), Terms.newConstant(2))),
						Atoms.newBasicAtom(Predicates.getPredicate("r", 1), Terms.newConstant(2)).toLiteral(false)),
				new InternalRule(Heads.newNormalHead(Atoms.newBasicAtom(Predicates.getPredicate("s", 2), Terms.newConstant(1), Terms.newConstant(2))),
						Atoms.newBasicAtom(Predicates.getPredicate("ns", 2), Terms.newConstant(1), Terms.newConstant(2)).toLiteral(false)),
				new InternalRule(Heads.newNormalHead(Atoms.newBasicAtom(Predicates.getPredicate("ns", 2), Terms.newConstant(1), Terms.newConstant(2))),
						Atoms.newBasicAtom(Predicates.getPredicate("s", 2), Terms.newConstant(1), Terms.newConstant(2)).toLiteral(false)),
				new InternalRule(null,
						Atoms.newBasicAtom(Predicates.getPredicate("p", 1), Terms.newConstant(1)).toLiteral(false))
				), Arrays.asList(
						Atoms.newBasicAtom(Predicates.getPredicate("dom", 1), Terms.newConstant(1)),
						Atoms.newBasicAtom(Predicates.getPredicate("dom", 1), Terms.newConstant(2)),
						Atoms.newBasicAtom(Predicates.getPredicate("dom", 1), Terms.newConstant(3))
						)
				);
				
		AtomStore atomStore = new AtomStoreImpl();
		NaiveGrounder grounder = new NaiveGrounder(program, atomStore, true);
		grounder.getNoGoods(null);
		TrailAssignment assignment = new TrailAssignment(atomStore);
		Atom p1 = Atoms.newBasicAtom(Predicates.getPredicate("p", 1), Terms.newConstant(1));
		Atom r2 = Atoms.newBasicAtom(Predicates.getPredicate("r", 1), Terms.newConstant(2));
		Atom s12 = Atoms.newBasicAtom(Predicates.getPredicate("s", 2), Terms.newConstant(1), Terms.newConstant(2));
		Atom q11 = Atoms.newBasicAtom(Predicates.getPredicate("q", 2), Terms.newConstant(1), Terms.newConstant(1));
		Atom q12 = Atoms.newBasicAtom(Predicates.getPredicate("q", 2), Terms.newConstant(1), Terms.newConstant(2));
		Atom q13 = Atoms.newBasicAtom(Predicates.getPredicate("q", 2), Terms.newConstant(1), Terms.newConstant(3));
		int p1Id = atomStore.get(p1);
		int r2Id = atomStore.get(r2);
		int s12Id = atomStore.get(s12);
		int q11Id = atomStore.get(q11);
		int q12Id = atomStore.get(q12);
		int q13Id = atomStore.get(q13);
		assignment.growForMaxAtomId();
		assignment.assign(p1Id, ThriceTruth.MBT);
		assignment.assign(r2Id, ThriceTruth.TRUE);
		assignment.assign(s12Id, ThriceTruth.TRUE);
		assignment.assign(q11Id, ThriceTruth.TRUE);
		assignment.assign(q12Id, ThriceTruth.TRUE);
		assignment.assign(q13Id, ThriceTruth.FALSE);

		Set<Literal> reasons = grounder.justifyAtom(p1Id, assignment);
		assertFalse(reasons.isEmpty());
	}

	@Test
	public void justifyMultipleReasons() {
		/*
		 * program :=
		 *     n(a). n(b). n(c). n(d). n(e).
		 *     s(a,b). s(b,c). s(c,d). s(d,e).
		 *     q(X) :- n(X), not nq(1, X).
		 *     nq(1, X) :- n(X), not q(X).
		 *     p(X) :- q(X).
		 *     p(X) :- p(Y), s(Y,X).
		 *     :- not p(c).
		 */
		CompiledProgram program = new InternalProgram(Arrays.asList(
				new InternalRule(Heads.newNormalHead(Atoms.newBasicAtom(Predicates.getPredicate("q", 1), Terms.newVariable("X"))),
						Atoms.newBasicAtom(Predicates.getPredicate("n", 1), Terms.newVariable("X")).toLiteral(),
						Atoms.newBasicAtom(Predicates.getPredicate("nq", 2), Terms.newConstant(1), Terms.newVariable("X")).toLiteral(false)),
				new InternalRule(Heads.newNormalHead(Atoms.newBasicAtom(Predicates.getPredicate("nq", 2), Terms.newConstant(1), Terms.newVariable("X"))),
						Atoms.newBasicAtom(Predicates.getPredicate("n", 1), Terms.newVariable("X")).toLiteral(),
						Atoms.newBasicAtom(Predicates.getPredicate("q", 1), Terms.newVariable("X")).toLiteral(false)),
				new InternalRule(Heads.newNormalHead(Atoms.newBasicAtom(Predicates.getPredicate("p", 1), Terms.newVariable("X"))),
						Atoms.newBasicAtom(Predicates.getPredicate("q", 1), Terms.newVariable("X")).toLiteral()),
				new InternalRule(Heads.newNormalHead(Atoms.newBasicAtom(Predicates.getPredicate("p", 1), Terms.newVariable("X"))),
						Atoms.newBasicAtom(Predicates.getPredicate("p", 1), Terms.newVariable("Y")).toLiteral(),
						Atoms.newBasicAtom(Predicates.getPredicate("s", 2), Terms.newVariable("Y"), Terms.newVariable("Y")).toLiteral()),
				new InternalRule(null,
						Atoms.newBasicAtom(Predicates.getPredicate("p", 1), Terms.newSymbolicConstant("c")).toLiteral())
				), Arrays.asList(
						Atoms.newBasicAtom(Predicates.getPredicate("n", 1), Terms.newSymbolicConstant("a")),
						Atoms.newBasicAtom(Predicates.getPredicate("n", 1), Terms.newSymbolicConstant("b")),
						Atoms.newBasicAtom(Predicates.getPredicate("n", 1), Terms.newSymbolicConstant("c")),
						Atoms.newBasicAtom(Predicates.getPredicate("n", 1), Terms.newSymbolicConstant("d")),
						Atoms.newBasicAtom(Predicates.getPredicate("n", 1), Terms.newSymbolicConstant("e")),
						Atoms.newBasicAtom(Predicates.getPredicate("s", 2), Terms.newSymbolicConstant("a"), Terms.newSymbolicConstant("b")),
						Atoms.newBasicAtom(Predicates.getPredicate("s", 2), Terms.newSymbolicConstant("b"), Terms.newSymbolicConstant("c")),
						Atoms.newBasicAtom(Predicates.getPredicate("s", 2), Terms.newSymbolicConstant("c"), Terms.newSymbolicConstant("d")),
						Atoms.newBasicAtom(Predicates.getPredicate("s", 2), Terms.newSymbolicConstant("d"), Terms.newSymbolicConstant("e"))
						)
				);
		
		AtomStore atomStore = new AtomStoreImpl();
		NaiveGrounder grounder = new NaiveGrounder(program, atomStore, true);
		grounder.getNoGoods(null);
		TrailAssignment assignment = new TrailAssignment(atomStore);
		Atom qa = Atoms.newBasicAtom(Predicates.getPredicate("q", 1), Terms.newSymbolicConstant("a"));
		Atom qb = Atoms.newBasicAtom(Predicates.getPredicate("q", 1), Terms.newSymbolicConstant("b"));
		Atom qc = Atoms.newBasicAtom(Predicates.getPredicate("q", 1), Terms.newSymbolicConstant("c"));
		Atom qd = Atoms.newBasicAtom(Predicates.getPredicate("q", 1), Terms.newSymbolicConstant("d"));
		Atom qe = Atoms.newBasicAtom(Predicates.getPredicate("q", 1), Terms.newSymbolicConstant("e"));
		int qaId = atomStore.get(qa);
		int qbId = atomStore.get(qb);
		int qcId = atomStore.get(qc);
		int qdId = atomStore.get(qd);
		int qeId = atomStore.get(qe);

		assignment.growForMaxAtomId();
		assignment.assign(qaId, ThriceTruth.FALSE);
		assignment.assign(qbId, ThriceTruth.FALSE);
		assignment.assign(qcId, ThriceTruth.FALSE);
		assignment.assign(qdId, ThriceTruth.FALSE);
		assignment.assign(qeId, ThriceTruth.FALSE);

		Predicate nq = Predicates.getPredicate("nq", 2);
		Atom nqa = Atoms.newBasicAtom(nq, Arrays.asList(Terms.newConstant(1), Terms.newSymbolicConstant("a")));
		Atom nqb = Atoms.newBasicAtom(nq, Arrays.asList(Terms.newConstant(1), Terms.newSymbolicConstant("b")));
		Atom nqc = Atoms.newBasicAtom(nq, Arrays.asList(Terms.newConstant(1), Terms.newSymbolicConstant("c")));
		Atom nqd = Atoms.newBasicAtom(nq, Arrays.asList(Terms.newConstant(1), Terms.newSymbolicConstant("d")));
		Atom nqe = Atoms.newBasicAtom(nq, Arrays.asList(Terms.newConstant(1), Terms.newSymbolicConstant("e")));
		int nqaId = atomStore.get(nqa);
		int nqbId = atomStore.get(nqb);
		int nqcId = atomStore.get(nqc);
		int nqdId = atomStore.get(nqd);
		int nqeId = atomStore.get(nqe);

		assignment.growForMaxAtomId();
		assignment.assign(nqaId, ThriceTruth.TRUE);
		assignment.assign(nqbId, ThriceTruth.TRUE);
		assignment.assign(nqcId, ThriceTruth.TRUE);
		assignment.assign(nqdId, ThriceTruth.TRUE);
		assignment.assign(nqeId, ThriceTruth.TRUE);

		Atom pc = Atoms.newBasicAtom(Predicates.getPredicate("p", 1), Terms.newSymbolicConstant("c"));
		Set<Literal> reasons = grounder.justifyAtom(atomStore.get(pc), assignment);
		assertFalse(reasons.isEmpty());
	}

	@Test
	public void justifyNegatedFactsRemovedFromReasons() {
		/*
		 * program :=
		 *     forbidden(2,9). forbidden(1,9).
		 *     p(X) :- q(X).
		 *     q(X) :- p(X).
		 *     q(5) :- r.
		 *     r :- not nr, not forbidden(2,9), not forbidden(1,9).
		 *     nr :- not r.
		 *     :- not p(5).
		 */
		CompiledProgram program = new InternalProgram(Arrays.asList(
				new InternalRule(Heads.newNormalHead(Atoms.newBasicAtom(Predicates.getPredicate("p", 1), Terms.newVariable("X"))),
						Atoms.newBasicAtom(Predicates.getPredicate("q", 1), Terms.newVariable("X")).toLiteral()),
				new InternalRule(Heads.newNormalHead(Atoms.newBasicAtom(Predicates.getPredicate("q", 1), Terms.newVariable("X"))),
						Atoms.newBasicAtom(Predicates.getPredicate("p", 1), Terms.newVariable("X")).toLiteral()),
				new InternalRule(Heads.newNormalHead(Atoms.newBasicAtom(Predicates.getPredicate("q", 1), Terms.newConstant(5))),
						Atoms.newBasicAtom(Predicates.getPredicate("r", 0)).toLiteral()),
				new InternalRule(Heads.newNormalHead(Atoms.newBasicAtom(Predicates.getPredicate("r", 0))),
						Atoms.newBasicAtom(Predicates.getPredicate("nr", 0)).toLiteral(false),
						Atoms.newBasicAtom(Predicates.getPredicate("forbidden", 2), Terms.newConstant(2), Terms.newConstant(9)).toLiteral(false),
						Atoms.newBasicAtom(Predicates.getPredicate("forbidden", 2), Terms.newConstant(1), Terms.newConstant(9)).toLiteral(false)),
				new InternalRule(Heads.newNormalHead(Atoms.newBasicAtom(Predicates.getPredicate("nr", 0))),
						Atoms.newBasicAtom(Predicates.getPredicate("r", 0)).toLiteral(false)),
				new InternalRule(null,
						Atoms.newBasicAtom(Predicates.getPredicate("p", 1), Terms.newConstant(5)).toLiteral())
				), Arrays.asList(
						Atoms.newBasicAtom(Predicates.getPredicate("forbidden", 2), Terms.newConstant(2), Terms.newConstant(9)),
						Atoms.newBasicAtom(Predicates.getPredicate("forbidden", 2), Terms.newConstant(1), Terms.newConstant(9))
						)
				);
		AtomStore atomStore = new AtomStoreImpl();
		NaiveGrounder grounder = new NaiveGrounder(program, atomStore, true);
		grounder.getNoGoods(null);
		TrailAssignment assignment = new TrailAssignment(atomStore);
		int rId = atomStore.get(Atoms.newBasicAtom(Predicates.getPredicate("r", 0)));
		int nrId = atomStore.get(Atoms.newBasicAtom(Predicates.getPredicate("nr", 0)));
		assignment.growForMaxAtomId();
		assignment.assign(rId, ThriceTruth.FALSE);
		assignment.assign(nrId, ThriceTruth.TRUE);
		BasicAtom p5 = Atoms.newBasicAtom(Predicates.getPredicate("p", 1), Collections.singletonList(Terms.newConstant(5)));
		assignment.assign(atomStore.get(p5), ThriceTruth.MBT);
		Set<Literal> reasons = grounder.justifyAtom(atomStore.get(p5), assignment);
		assertFalse(reasons.isEmpty());
		for (Literal literal : reasons) {
			// Check that facts are not present in justification.
			assertNotEquals(literal.getPredicate(), Predicates.getPredicate("forbidden", 2));
		}
	}
}
