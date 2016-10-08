package at.ac.tuwien.kr.alpha;

import at.ac.tuwien.kr.alpha.common.AnswerSet;
import at.ac.tuwien.kr.alpha.common.BasicAnswerSet;
import at.ac.tuwien.kr.alpha.grounder.ChoiceGrounder;
import at.ac.tuwien.kr.alpha.grounder.DummyGrounder;
import at.ac.tuwien.kr.alpha.grounder.Grounder;
import at.ac.tuwien.kr.alpha.solver.NaiveSolver;
import at.ac.tuwien.kr.alpha.solver.SolverFactory;
import org.junit.Ignore;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import static at.ac.tuwien.kr.alpha.Main.main;
import static at.ac.tuwien.kr.alpha.Main.parseVisit;
import static org.junit.Assert.assertEquals;

public class MainTest {
	public static InputStream stream(String file) {
		return new ByteArrayInputStream(file.getBytes());
	}

	@Test
	@Ignore
	public void parseSimpleProgram() throws IOException {
		parseVisit(stream(
			"p(X) :- q(X).\n" +
			"q(a).\n" +
			"q(b).\n"
		));
	}

	@Test
	public void parseProgramWithNegativeBody() throws IOException {
		parseVisit(stream(
			"p(X) :- q(X), not q(a).\n" +
				"q(a).\n"
		));
	}

	@Test(expected = UnsupportedOperationException.class)
	@Ignore
	public void parseProgramWithFunction() throws IOException {
		parseVisit(stream(
			"p(X) :- q(f(X)).\n" +
				"q(a).\n"
		));
	}

	@Test(expected = UnsupportedOperationException.class)
	@Ignore
	public void parseProgramWithDisjunctionInHead() throws IOException {
		parseVisit(stream(
			"r(X) | q(X) :- q(X).\n" +
				"q(a).\n"
		));
	}

	@Test
	public void testDummyGrounderAndSolver() {
		Grounder grounder = new DummyGrounder();
		NaiveSolver solver = (NaiveSolver)SolverFactory.getInstance("naive", grounder, p -> true);

		AnswerSet expected = new BasicAnswerSet.Builder()
			.predicate("a")
			.predicate("b")
			.predicate("_br1")
			.predicate("c")
			.build();

		int answerSetCount = 0;
		while (true) {
			AnswerSet as = solver.computeNextAnswerSet();
			if (as == null) {
				break;
			}
			answerSetCount++;
			// Adapting the printing of answer sets requires adaption of the below assertion.
			assertEquals(expected, as);
		}
		assertEquals("Program has one answer set.", 1, answerSetCount);
		//System.out.println("Found " + answerSetCount + " Answer Set(s), there are no more answer sets.");
	}

	@Test
	public void testGrounderChoiceAndSolver() {

		Grounder grounder = new ChoiceGrounder();
		NaiveSolver solver = (NaiveSolver)SolverFactory.getInstance("naive", grounder, p -> true);

		int answerSetCount = 0;
		while (true) {
			AnswerSet as = solver.computeNextAnswerSet();
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
	@Ignore
	public void testLargeInputProgram() {
		main(new String[]{"-g", "naive", "-s", "naive", "-n", "10", "-i", "./benchmarks/omiga/omiga-testcases/locstrat/locstrat-200.txt"});
	}

}