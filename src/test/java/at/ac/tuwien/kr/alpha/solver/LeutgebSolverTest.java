package at.ac.tuwien.kr.alpha.solver;

import at.ac.tuwien.kr.alpha.common.AnswerSet;
import at.ac.tuwien.kr.alpha.common.Predicate;
import at.ac.tuwien.kr.alpha.grounder.DummyGrounder;
import at.ac.tuwien.kr.alpha.grounder.Grounder;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class LeutgebSolverTest {
	private static final java.util.function.Predicate<Predicate> ALWAYS_TRUE = x -> true;
	private Grounder grounder;

	@Before
	public void instantiateGrounder() {
		grounder = new DummyGrounder();
	}

	@Test
	@Ignore("not implemented yet")
	public void returnsAnswerSet() {
		final Solver solver = new LeutgebSolver(grounder, ALWAYS_TRUE);
		final List<AnswerSet> recorder = new ArrayList<>(1);
		assertTrue(solver.spliterator().tryAdvance(recorder::add));
		assertEquals(1, recorder.size());
	}
}
