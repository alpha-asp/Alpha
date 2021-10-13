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
package at.ac.tuwien.kr.alpha.grounder.structure;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;

import org.junit.jupiter.api.Test;

import at.ac.tuwien.kr.alpha.api.Alpha;
import at.ac.tuwien.kr.alpha.common.AtomStore;
import at.ac.tuwien.kr.alpha.common.AtomStoreImpl;
import at.ac.tuwien.kr.alpha.common.Predicate;
import at.ac.tuwien.kr.alpha.common.atoms.Atom;
import at.ac.tuwien.kr.alpha.common.atoms.BasicAtom;
import at.ac.tuwien.kr.alpha.common.atoms.Literal;
import at.ac.tuwien.kr.alpha.common.program.InputProgram;
import at.ac.tuwien.kr.alpha.common.program.InternalProgram;
import at.ac.tuwien.kr.alpha.common.program.NormalProgram;
import at.ac.tuwien.kr.alpha.common.terms.ConstantTerm;
import at.ac.tuwien.kr.alpha.grounder.NaiveGrounder;
import at.ac.tuwien.kr.alpha.grounder.parser.ProgramParser;
import at.ac.tuwien.kr.alpha.solver.ThriceTruth;
import at.ac.tuwien.kr.alpha.solver.TrailAssignment;

/**
 * Copyright (c) 2018-2020, the Alpha Team.
 */
public class AnalyzeUnjustifiedTest {

	private final ProgramParser parser = new ProgramParser();
	private final HeuristicsConfiguration heuristicsConfiguration = new HeuristicsConfigurationBuilder().build();

	@Test
	public void justifySimpleRules() {
		Alpha system = new Alpha();
		String program = "p(X) :- q(X)." +
			"q(X) :- p(X)." +
			"q(5) :- r." +
			"r :- not nr." +
			"nr :- not r." +
			":- not p(5).";
		InputProgram parsedProgram = parser.parse(program);
		NormalProgram normal = system.normalizeProgram(parsedProgram);
		InternalProgram internalProgram = InternalProgram.fromNormalProgram(normal);
		AtomStore atomStore = new AtomStoreImpl();
		NaiveGrounder grounder = new NaiveGrounder(internalProgram, atomStore, heuristicsConfiguration, true);
		grounder.getNoGoods(null);
		TrailAssignment assignment = new TrailAssignment(atomStore);
		int rId = atomStore.get(new BasicAtom(Predicate.getInstance("r", 0)));
		int nrId = atomStore.get(new BasicAtom(Predicate.getInstance("nr", 0)));
		assignment.growForMaxAtomId();
		assignment.assign(rId, ThriceTruth.FALSE);
		assignment.assign(nrId, ThriceTruth.TRUE);
		BasicAtom p5 = new BasicAtom(Predicate.getInstance("p", 1), Collections.singletonList(ConstantTerm.getInstance(5)));
		assignment.assign(atomStore.get(p5), ThriceTruth.MBT);
		Set<Literal> reasons = grounder.justifyAtom(atomStore.get(p5), assignment);
		assertFalse(reasons.isEmpty());
	}

	@Test
	public void justifyLargerRules() {
		Alpha system = new Alpha();
		String program = "p(X) :- q(X,Y), r(Y), not s(X,Y)." +
			"{ q(1,X)} :- dom(X)." +
			"dom(1..3)." +
			"{r(X)} :- p(X)." +
			"{r(2)}." +
			"{s(1,2)}." +
			":- not p(1).";
		InputProgram parsedProgram = parser.parse(program);
		NormalProgram normal = system.normalizeProgram(parsedProgram);
		InternalProgram internalProgram = InternalProgram.fromNormalProgram(normal);
		AtomStore atomStore = new AtomStoreImpl();
		NaiveGrounder grounder = new NaiveGrounder(internalProgram, atomStore, heuristicsConfiguration, true);
		grounder.getNoGoods(null);
		TrailAssignment assignment = new TrailAssignment(atomStore);
		Atom p1 = parser.parse("p(1).").getFacts().get(0);
		Atom r2 = parser.parse("r(2).").getFacts().get(0);
		Atom s12 = parser.parse("s(1,2).").getFacts().get(0);
		Atom q11 = parser.parse("q(1,1).").getFacts().get(0);
		Atom q12 = parser.parse("q(1,2).").getFacts().get(0);
		Atom q13 = parser.parse("q(1,3).").getFacts().get(0);
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
		Alpha system = new Alpha();
		String program = "n(a). n(b). n(c). n(d). n(e)." +
			"s(a,b). s(b,c). s(c,d). s(d,e)." +
			"{ q(X) } :- n(X)." +
			"p(X) :- q(X)." +
			"p(X) :- p(Y), s(Y,X)." +
			":- not p(c).";
		InputProgram parsedProgram = parser.parse(program);
		NormalProgram normal = system.normalizeProgram(parsedProgram);
		InternalProgram internalProgram = InternalProgram.fromNormalProgram(normal);
		AtomStore atomStore = new AtomStoreImpl();
		NaiveGrounder grounder = new NaiveGrounder(internalProgram, atomStore, heuristicsConfiguration, true);
		grounder.getNoGoods(null);
		TrailAssignment assignment = new TrailAssignment(atomStore);
		Atom qa = parser.parse("q(a).").getFacts().get(0);
		Atom qb = parser.parse("q(b).").getFacts().get(0);
		Atom qc = parser.parse("q(c).").getFacts().get(0);
		Atom qd = parser.parse("q(d).").getFacts().get(0);
		Atom qe = parser.parse("q(e).").getFacts().get(0);
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

		Predicate nq = Predicate.getInstance("_nq", 2, true);
		Atom nqa = new BasicAtom(nq, Arrays.asList(ConstantTerm.getInstance("1"), ConstantTerm.getSymbolicInstance("a")));
		Atom nqb = new BasicAtom(nq, Arrays.asList(ConstantTerm.getInstance("1"), ConstantTerm.getSymbolicInstance("b")));
		Atom nqc = new BasicAtom(nq, Arrays.asList(ConstantTerm.getInstance("1"), ConstantTerm.getSymbolicInstance("c")));
		Atom nqd = new BasicAtom(nq, Arrays.asList(ConstantTerm.getInstance("1"), ConstantTerm.getSymbolicInstance("d")));
		Atom nqe = new BasicAtom(nq, Arrays.asList(ConstantTerm.getInstance("1"), ConstantTerm.getSymbolicInstance("e")));
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

		Atom pc = parser.parse("p(c).").getFacts().get(0);
		Set<Literal> reasons = grounder.justifyAtom(atomStore.get(pc), assignment);
		assertFalse(reasons.isEmpty());
	}

	@Test
	public void justifyNegatedFactsRemovedFromReasons() {
		Alpha system = new Alpha();
		String program = "forbidden(2,9). forbidden(1,9)." +
			"p(X) :- q(X)." +
			"q(X) :- p(X)." +
			"q(5) :- r." +
			"r :- not nr, not forbidden(2,9), not forbidden(1,9)." +
			"nr :- not r." +
			":- not p(5).";
		InputProgram parsedProgram = parser.parse(program);
		NormalProgram normal = system.normalizeProgram(parsedProgram);
		InternalProgram internalProgram = InternalProgram.fromNormalProgram(normal);
		AtomStore atomStore = new AtomStoreImpl();
		NaiveGrounder grounder = new NaiveGrounder(internalProgram, atomStore, heuristicsConfiguration, true);
		grounder.getNoGoods(null);
		TrailAssignment assignment = new TrailAssignment(atomStore);
		int rId = atomStore.get(new BasicAtom(Predicate.getInstance("r", 0)));
		int nrId = atomStore.get(new BasicAtom(Predicate.getInstance("nr", 0)));
		assignment.growForMaxAtomId();
		assignment.assign(rId, ThriceTruth.FALSE);
		assignment.assign(nrId, ThriceTruth.TRUE);
		BasicAtom p5 = new BasicAtom(Predicate.getInstance("p", 1), Collections.singletonList(ConstantTerm.getInstance(5)));
		assignment.assign(atomStore.get(p5), ThriceTruth.MBT);
		Set<Literal> reasons = grounder.justifyAtom(atomStore.get(p5), assignment);
		assertFalse(reasons.isEmpty());
		for (Literal literal : reasons) {
			// Check that facts are not present in justification.
			assertNotEquals(literal.getPredicate(), Predicate.getInstance("forbidden", 2));
		}
	}
}
