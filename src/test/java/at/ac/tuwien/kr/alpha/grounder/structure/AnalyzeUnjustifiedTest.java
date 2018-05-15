package at.ac.tuwien.kr.alpha.grounder.structure;

import at.ac.tuwien.kr.alpha.common.Predicate;
import at.ac.tuwien.kr.alpha.common.Program;
import at.ac.tuwien.kr.alpha.common.atoms.Atom;
import at.ac.tuwien.kr.alpha.common.atoms.BasicAtom;
import at.ac.tuwien.kr.alpha.common.atoms.Literal;
import at.ac.tuwien.kr.alpha.common.terms.ConstantTerm;
import at.ac.tuwien.kr.alpha.grounder.NaiveGrounder;
import at.ac.tuwien.kr.alpha.grounder.parser.ProgramParser;
import at.ac.tuwien.kr.alpha.solver.ArrayAssignment;
import at.ac.tuwien.kr.alpha.solver.ThriceTruth;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;

import static junit.framework.TestCase.assertFalse;

/**
 * Copyright (c) 2018, the Alpha Team.
 */
public class AnalyzeUnjustifiedTest {

	private final ProgramParser parser = new ProgramParser();

	@Test
	public void justifySimpleRules() {
		String program = "p(X) :- q(X)." +
			"q(X) :- p(X)." +
			"q(5) :- r." +
			"r :- not nr." +
			"nr :- not r." +
			":- not p(5).";
		Program parsedProgram = parser.parse(program);
		NaiveGrounder grounder = new NaiveGrounder(parsedProgram);
		grounder.getNoGoods(null);
		ArrayAssignment assignment = new ArrayAssignment(grounder);
		int rId = grounder.getAtom(new BasicAtom(Predicate.getInstance("r", 0)));
		int nrId = grounder.getAtom(new BasicAtom(Predicate.getInstance("nr", 0)));
		assignment.growForMaxAtomId(grounder.getMaxAtomId());
		assignment.assign(rId, ThriceTruth.FALSE);
		assignment.assign(nrId, ThriceTruth.TRUE);
		BasicAtom p5 = new BasicAtom(Predicate.getInstance("p", 1), Collections.singletonList(ConstantTerm.getInstance(5)));
		assignment.assign(grounder.getAtom(p5), ThriceTruth.MBT);
		Set<Literal> reasons = grounder.justifyAtom(grounder.getAtom(p5), assignment);
		assertFalse(reasons.isEmpty());
	}

	@Test
	public void justifyLargerRules() {
		String program = "p(X) :- q(X,Y), r(Y), not s(X,Y)." +
			"{ q(1,X)} :- dom(X)." +
			"dom(1..3)." +
			"{r(X)} :- p(X)." +
			"{r(2)}." +
			"{s(1,2)}." +
			":- not p(1).";
		Program parsedProgram = parser.parse(program);
		NaiveGrounder grounder = new NaiveGrounder(parsedProgram);
		grounder.getNoGoods(null);
		ArrayAssignment assignment = new ArrayAssignment(grounder);
		Atom p1 = parser.parse("p(1).").getFacts().get(0);
		Atom r2 = parser.parse("r(2).").getFacts().get(0);
		Atom s12 = parser.parse("s(1,2).").getFacts().get(0);
		Atom q11 = parser.parse("q(1,1).").getFacts().get(0);
		Atom q12 = parser.parse("q(1,2).").getFacts().get(0);
		Atom q13 = parser.parse("q(1,3).").getFacts().get(0);
		int p1Id = grounder.getAtom(p1);
		int r2Id = grounder.getAtom(r2);
		int s12Id = grounder.getAtom(s12);
		int q11Id = grounder.getAtom(q11);
		int q12Id = grounder.getAtom(q12);
		int q13Id = grounder.getAtom(q13);
		assignment.growForMaxAtomId(grounder.getMaxAtomId());
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
		String program = "n(a). n(b). n(c). n(d). n(e)." +
			"s(a,b). s(b,c). s(c,d). s(d,e)." +
			"{ q(X) } :- n(X)." +
			"p(X) :- q(X)." +
			"p(X) :- p(Y), s(Y,X)." +
			":- not p(c).";
		Program parsedProgram = parser.parse(program);
		NaiveGrounder grounder = new NaiveGrounder(parsedProgram);
		grounder.getNoGoods(null);
		ArrayAssignment assignment = new ArrayAssignment(grounder);
		Atom qa = parser.parse("q(a).").getFacts().get(0);
		Atom qb = parser.parse("q(b).").getFacts().get(0);
		Atom qc = parser.parse("q(c).").getFacts().get(0);
		Atom qd = parser.parse("q(d).").getFacts().get(0);
		Atom qe = parser.parse("q(e).").getFacts().get(0);
		int qaId = grounder.getAtom(qa);
		int qbId = grounder.getAtom(qb);
		int qcId = grounder.getAtom(qc);
		int qdId = grounder.getAtom(qd);
		int qeId = grounder.getAtom(qe);

		assignment.growForMaxAtomId(grounder.getMaxAtomId());
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
		int nqaId = grounder.getAtom(nqa);
		int nqbId = grounder.getAtom(nqb);
		int nqcId = grounder.getAtom(nqc);
		int nqdId = grounder.getAtom(nqd);
		int nqeId = grounder.getAtom(nqe);

		assignment.growForMaxAtomId(grounder.getMaxAtomId());
		assignment.assign(nqaId, ThriceTruth.TRUE);
		assignment.assign(nqbId, ThriceTruth.TRUE);
		assignment.assign(nqcId, ThriceTruth.TRUE);
		assignment.assign(nqdId, ThriceTruth.TRUE);
		assignment.assign(nqeId, ThriceTruth.TRUE);

		Atom pc = parser.parse("p(c).").getFacts().get(0);
		Set<Literal> reasons = grounder.justifyAtom(grounder.getAtom(pc), assignment);
		assertFalse(reasons.isEmpty());
	}

}