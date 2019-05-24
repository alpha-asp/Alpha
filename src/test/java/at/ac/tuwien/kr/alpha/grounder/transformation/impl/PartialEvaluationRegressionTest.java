package at.ac.tuwien.kr.alpha.grounder.transformation.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import at.ac.tuwien.kr.alpha.Alpha;
import at.ac.tuwien.kr.alpha.common.Predicate;
import at.ac.tuwien.kr.alpha.common.atoms.BasicAtom;
import at.ac.tuwien.kr.alpha.common.program.impl.InputProgram;
import at.ac.tuwien.kr.alpha.common.program.impl.InternalProgram;
import at.ac.tuwien.kr.alpha.common.terms.ConstantTerm;

@RunWith(Parameterized.class)
public class PartialEvaluationRegressionTest {

	//private static final Logger LOGGER = LoggerFactory.getLogger(PartialEvaluationRegressionTest.class);

	private static final String BASIC_TEST_ASP = "a. b:- a.";
	private static final String BASIC_MULTI_INSTANCE_ASP = "p(a). p(b). q(X) :- p(X).";

	@Parameters(name = "Run {index}: aspString={0}, verifier={1}")
	public static Iterable<Object[]> params() {
		List<ImmutablePair<String, Consumer<InternalProgram>>> testCases = new ArrayList<>();
		List<Object[]> paramList = new ArrayList<>();
		testCases.add(new ImmutablePair<>(BASIC_TEST_ASP, PartialEvaluationRegressionTest::verifyBasic));
		testCases.add(new ImmutablePair<>(BASIC_MULTI_INSTANCE_ASP, PartialEvaluationRegressionTest::verifyBasicMultiInstance));

		testCases.forEach((pair) -> paramList.add(new Object[] {pair.left, pair.right}));
		return paramList;
	}

	private String aspString;
	private Consumer<InternalProgram> verifier;

	public PartialEvaluationRegressionTest(String aspString, Consumer<InternalProgram> verifier) {
		this.aspString = aspString;
		this.verifier = verifier;
	}

	@Test
	public void evaluateSimpleTest() {
		String aspStr = this.aspString;
		Alpha system = new Alpha();
		InputProgram prg = system.readProgramString(aspStr);
		InternalProgram internalProgram = system.performProgramPreprocessing(prg);
		InternalProgram evaluated = new PartialEvaluation().apply(internalProgram);
		this.verifier.accept(evaluated);
	}

	private static void verifyBasic(InternalProgram evaluated) {
		Assert.assertTrue(evaluated.getFacts().contains(new BasicAtom(Predicate.getInstance("a", 0))));
		Assert.assertTrue(evaluated.getFacts().contains(new BasicAtom(Predicate.getInstance("b", 0))));
		Assert.assertEquals(2, evaluated.getFacts().size());
	}

	private static void verifyBasicMultiInstance(InternalProgram evaluated) {
		BasicAtom qOfA = new BasicAtom(Predicate.getInstance("q", 1), ConstantTerm.getSymbolicInstance("a"));
		BasicAtom qOfB = new BasicAtom(Predicate.getInstance("q", 1), ConstantTerm.getSymbolicInstance("b"));
		Assert.assertTrue(evaluated.getFacts().contains(qOfA));
		Assert.assertTrue(evaluated.getFacts().contains(qOfB));
	}

}
