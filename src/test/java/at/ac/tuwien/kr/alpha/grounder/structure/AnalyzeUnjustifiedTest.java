package at.ac.tuwien.kr.alpha.grounder.structure;

import at.ac.tuwien.kr.alpha.common.Predicate;
import at.ac.tuwien.kr.alpha.common.Program;
import at.ac.tuwien.kr.alpha.common.atoms.Atom;
import at.ac.tuwien.kr.alpha.common.atoms.BasicAtom;
import at.ac.tuwien.kr.alpha.common.atoms.Literal;
import at.ac.tuwien.kr.alpha.common.terms.ConstantTerm;
import at.ac.tuwien.kr.alpha.grounder.AtomStore;
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
		AtomStore atomStore = grounder.getAtomStore();
		assignment.growForMaxAtomId(atomStore.getHighestAtomId());
		assignment.assign(atomStore.getAtomId(new BasicAtom(Predicate.getInstance("r", 0))), ThriceTruth.FALSE);
		assignment.assign(atomStore.getAtomId(new BasicAtom(Predicate.getInstance("nr", 0))), ThriceTruth.TRUE);
		BasicAtom p5 = new BasicAtom(Predicate.getInstance("p", 1), Collections.singletonList(ConstantTerm.getInstance(5)));
		assignment.assign(atomStore.getAtomId(p5), ThriceTruth.MBT);
		Set<Literal> reasons = grounder.analyzeUnjustified.analyze(grounder.getAtomStore().getAtomId(p5), assignment);
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
		AtomStore atomStore = grounder.getAtomStore();
		assignment.growForMaxAtomId(atomStore.getHighestAtomId());
		Atom p1 = parser.parse("p(1).").getFacts().get(0);
		Atom r2 = parser.parse("r(2).").getFacts().get(0);
		Atom s12 = parser.parse("s(1,2).").getFacts().get(0);
		Atom q11 = parser.parse("q(1,1).").getFacts().get(0);
		Atom q12 = parser.parse("q(1,2).").getFacts().get(0);
		Atom q13 = parser.parse("q(1,3).").getFacts().get(0);
		assignment.assign(atomStore.getAtomId(p1), ThriceTruth.MBT);
		assignment.assign(atomStore.getAtomId(r2), ThriceTruth.TRUE);
		assignment.assign(atomStore.getAtomId(s12), ThriceTruth.TRUE);
		assignment.assign(atomStore.getAtomId(q11), ThriceTruth.TRUE);
		assignment.assign(atomStore.getAtomId(q12), ThriceTruth.TRUE);
		assignment.assign(atomStore.getAtomId(q13), ThriceTruth.FALSE);

		Set<Literal> reasons = grounder.analyzeUnjustified.analyze(grounder.getAtomStore().getAtomId(p1), assignment);
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
		AtomStore atomStore = grounder.getAtomStore();
		assignment.growForMaxAtomId(atomStore.getHighestAtomId());
		Atom qa = parser.parse("q(a).").getFacts().get(0);
		Atom qb = parser.parse("q(b).").getFacts().get(0);
		Atom qc = parser.parse("q(c).").getFacts().get(0);
		Atom qd = parser.parse("q(d).").getFacts().get(0);
		Atom qe = parser.parse("q(e).").getFacts().get(0);

		assignment.assign(atomStore.getAtomId(qa), ThriceTruth.FALSE);
		assignment.assign(atomStore.getAtomId(qb), ThriceTruth.FALSE);
		assignment.assign(atomStore.getAtomId(qc), ThriceTruth.FALSE);
		assignment.assign(atomStore.getAtomId(qd), ThriceTruth.FALSE);
		assignment.assign(atomStore.getAtomId(qe), ThriceTruth.FALSE);

		Predicate nq = Predicate.getInstance("_nq", 2, true);
		Atom nqa = new BasicAtom(nq, Arrays.asList(ConstantTerm.getInstance("1"), ConstantTerm.getSymbolicInstance("a")));
		Atom nqb = new BasicAtom(nq, Arrays.asList(ConstantTerm.getInstance("1"), ConstantTerm.getSymbolicInstance("b")));
		Atom nqc = new BasicAtom(nq, Arrays.asList(ConstantTerm.getInstance("1"), ConstantTerm.getSymbolicInstance("c")));
		Atom nqd = new BasicAtom(nq, Arrays.asList(ConstantTerm.getInstance("1"), ConstantTerm.getSymbolicInstance("d")));
		Atom nqe = new BasicAtom(nq, Arrays.asList(ConstantTerm.getInstance("1"), ConstantTerm.getSymbolicInstance("e")));

		assignment.assign(atomStore.getAtomId(nqa), ThriceTruth.TRUE);
		assignment.assign(atomStore.getAtomId(nqb), ThriceTruth.TRUE);
		assignment.assign(atomStore.getAtomId(nqc), ThriceTruth.TRUE);
		assignment.assign(atomStore.getAtomId(nqd), ThriceTruth.TRUE);
		assignment.assign(atomStore.getAtomId(nqe), ThriceTruth.TRUE);

		Atom pc = parser.parse("p(c).").getFacts().get(0);
		Set<Literal> reasons = grounder.analyzeUnjustified.analyze(grounder.getAtomStore().getAtomId(pc), assignment);
		assertFalse(reasons.isEmpty());
	}

}