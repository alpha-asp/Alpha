package at.ac.tuwien.kr.alpha;

import at.ac.tuwien.kr.alpha.common.AnswerSet;
import at.ac.tuwien.kr.alpha.grounder.*;
import at.ac.tuwien.kr.alpha.grounder.parser.ParsedConstant;
import at.ac.tuwien.kr.alpha.grounder.parser.ParsedFunctionTerm;
import at.ac.tuwien.kr.alpha.grounder.parser.ParsedProgram;
import at.ac.tuwien.kr.alpha.solver.Solver;
import at.ac.tuwien.kr.alpha.solver.SolverFactory;
import org.antlr.v4.runtime.RecognitionException;
import org.junit.Ignore;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static org.junit.Assert.*;

public class MainTest {
	private InputStream stream(String file) {
		return new ByteArrayInputStream(file.getBytes());
	}

	@Test
	@Ignore
	public void parseSimpleProgram() throws IOException {
		Main.parseVisit(stream(
			"p(X) :- q(X).\n" +
			"q(a).\n" +
			"q(b).\n"
		));
	}

	@Test
	public void parseProgramWithNegativeBody() throws IOException {
		Main.parseVisit(stream(
			"p(X) :- q(X), not q(a).\n" +
				"q(a).\n"
		));
	}

	@Test(expected = UnsupportedOperationException.class)
	@Ignore
	public void parseProgramWithFunction() throws IOException {
		Main.parseVisit(stream(
			"p(X) :- q(f(X)).\n" +
				"q(a).\n"
		));
	}

	@Test(expected = UnsupportedOperationException.class)
	@Ignore
	public void parseProgramWithDisjunctionInHead() throws IOException {
		Main.parseVisit(stream(
			"r(X) | q(X) :- q(X).\n" +
				"q(a).\n"
		));
	}

	@Test
	public void parseFact() throws IOException {
		ParsedProgram parsedProgram = Main.parseVisit(stream("p(a,b)."));

		assertEquals("Program contains one fact.", 1, parsedProgram.facts.size());
		assertEquals("Predicate name of fact is p.", "p", parsedProgram.facts.get(0).fact.predicate);
		assertEquals("Fact has two terms.", 2, parsedProgram.facts.get(0).fact.arity);
		assertEquals("First term is a.", "a", ((ParsedConstant)parsedProgram.facts.get(0).fact.terms.get(0)).content);
		assertEquals("Second term is b.", "b", ((ParsedConstant)parsedProgram.facts.get(0).fact.terms.get(1)).content);
	}

	@Test
	public void parseFactWithFunctionTerms() throws IOException {
		ParsedProgram parsedProgram = Main.parseVisit(stream("p(f(a),g(h(Y)))."));

		assertEquals("Program contains one fact.", 1, parsedProgram.facts.size());
		assertEquals("Predicate name of fact is p.", "p", parsedProgram.facts.get(0).fact.predicate);
		assertEquals("Fact has two terms.", 2, parsedProgram.facts.get(0).fact.arity);
		assertEquals("First term is function term f.", "f", ((ParsedFunctionTerm)parsedProgram.facts.get(0).fact.terms.get(0)).functionName);
		assertEquals("Second term is function term g.", "g", ((ParsedFunctionTerm)parsedProgram.facts.get(0).fact.terms.get(1)).functionName);
	}

	@Test
	public void parseSmallProgram() throws IOException {
		ParsedProgram parsedProgram = Main.parseVisit(stream("a :- b, not d.\n" +
				"c(X) :- p(X,a,_), q(Xaa,xaa). :- f(Y)."));

		assertEquals("Program contains two rules.", 2, parsedProgram.rules.size());
		assertEquals("Program contains one constraint.", 1, parsedProgram.constraints.size());
	}

	@Test(expected = RecognitionException.class)
	public void parseBadSyntax() throws IOException {
		Main.parseVisit(stream("Wrong Syntax."));
	}

	@Test
	public void testDummyGrounderAndSolver() {

		Grounder grounder = GrounderFactory.getInstance("dummy", null);
		Solver solver = SolverFactory.getInstance("dummy", grounder, p -> true);

		int answerSetCount = 0;
		while (true) {
			AnswerSet as = solver.get();
			if (as == null) {
				break;
			}
			answerSetCount++;
			// Adapting the printing of answer sets requires adaption of the below assertion.
			assertEquals("Answer set is { a, b, _br1, c }.", "{ a, b, _br1, c }", as.toString());
		}
		assertEquals("Program has one answer set.", 1, answerSetCount);
		//System.out.println("Found " + answerSetCount + " Answer Set(s), there are no more answer sets.");
	}

	@Test
	public void testGrounderChoiceAndSolver() {

		Grounder grounder = new GrounderChoiceTest();
		Solver solver = SolverFactory.getInstance("dummy", grounder, p -> true);

		int answerSetCount = 0;
		while (true) {
			AnswerSet as = solver.get();
			if (as == null) {
				break;
			}
			answerSetCount++;
			System.out.println("AS " + answerSetCount + ": " + as.toString());
			// Adapting the printing of answer sets requires adaption of the below assertion.
			//assertEquals("Answer set is { a, b, _br1, c }.", "{ a, b, _br1, c }", as.toString());
		}
		assertEquals("Program has two answer sets.", 2, answerSetCount);
		//System.out.println("Found " + answerSetCount + " Answer Set(s), there are no more answer sets.");
	}

	@Test
	public void testIndexedInstanceStorage() {
		IndexedInstanceStorage storage = new IndexedInstanceStorage("A test storage of arity 4", 4);
		storage.addIndexPosition(0);
		storage.addIndexPosition(2);

		Instance badInst1 = new Instance(new int[]{1, 1, 0});
		Instance badInst2 = new Instance(new int[]{5, 5, 5, 5, 5});

		try {
			storage.addInstance(badInst1);
			fail();
		} catch (Exception e) {
			assertTrue(e.getMessage().startsWith("Instance length does not match arity of IndexedInstanceStorage"));
		}

		try {
			storage.addInstance(badInst2);
			fail();
		} catch (Exception e) {
			assertTrue(e.getMessage().startsWith("Instance length does not match arity of IndexedInstanceStorage"));
		}

		Instance inst1 = new Instance(new int[]{1, 1, 1, 1});
		Instance inst2 = new Instance(new int[]{1, 2, 3, 4});
		Instance inst3 = new Instance(new int[]{4, 3, 3, 5});
		Instance inst4 = new Instance(new int[]{1, 2, 1, 1});
		Instance inst5 = new Instance(new int[]{5, 4, 3, 2});

		storage.addInstance(inst1);
		storage.addInstance(inst2);
		storage.addInstance(inst3);
		storage.addInstance(inst4);
		storage.addInstance(inst5);

		List<Instance> matching3 = storage.getInstancesMatchingAtPosition(3, 2);
		assertEquals(matching3.size(), 3);
		assertTrue(matching3.contains(new Instance(new int[]{1, 2, 3, 4})));
		assertTrue(matching3.contains(new Instance(new int[]{4, 3, 3, 5})));
		assertTrue(matching3.contains(new Instance(new int[]{5, 4, 3, 2})));
		assertFalse(matching3.contains(new Instance(new int[]{1, 1, 1, 1})));

		List<Instance> matching1 = storage.getInstancesMatchingAtPosition(2, 0);
		assertEquals(matching1, null);
	}
}