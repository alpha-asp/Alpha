package at.ac.tuwien.kr.alpha.grounder.transformation;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.function.Function;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.tuwien.kr.alpha.api.Alpha;
import at.ac.tuwien.kr.alpha.api.externals.Externals;
import at.ac.tuwien.kr.alpha.common.program.AbstractProgram;
import at.ac.tuwien.kr.alpha.common.program.InputProgram;
import at.ac.tuwien.kr.alpha.common.program.NormalProgram;

public class ProgramTransformationTest {

	private static final Logger LOGGER = LoggerFactory.getLogger(ProgramTransformationTest.class);

	private static final String TESTFILES_PATH = "/transform-test/";

	private Alpha alpha = new Alpha();

	private ChoiceHeadToNormal choiceToNormal = new ChoiceHeadToNormal();
	private IntervalTermToIntervalAtom intervalRewriting = new IntervalTermToIntervalAtom();

	@SuppressWarnings("resource")
	private static String readTestResource(String resource) throws IOException {
		InputStream is = ProgramTransformationTest.class.getResourceAsStream(TESTFILES_PATH + resource);
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		StringBuilder bld = new StringBuilder();
		String line;
		while ((line = br.readLine()) != null) {
			bld.append(line).append(System.lineSeparator());
		}
		br.close();
		return bld.toString();
	}

	private <I extends AbstractProgram<?>, O extends AbstractProgram<?>> void genericTransformationTest(ProgramTransformation<I, O> transform,
			Function<InputProgram, I> prepareFunc, String resourceSet) {
		try {
			String inputCode = ProgramTransformationTest.readTestResource(resourceSet + ".in");
			String expectedResult = ProgramTransformationTest.readTestResource(resourceSet + ".out");
			InputProgram inputProg = alpha.readProgramString(inputCode, Externals.scan(ProgramTransformationTest.class));
			I transformInput = prepareFunc.apply(inputProg);
			String beforeTransformProg = transformInput.toString();
			O transformedProg = transform.apply(transformInput);
			assertEquals(expectedResult, transformedProg.toString(), "Transformation result doesn't match expected result");
			assertEquals(beforeTransformProg, transformInput.toString(), "Transformation modified source program (breaks immutability!)");
		} catch (Exception ex) {
			LOGGER.error("Exception in test, nested exception: " + ex.getMessage(), ex);
			throw new RuntimeException(ex);
		}
	}

	@Test
	public void choiceHeadToNormalSimpleTest() {
		genericTransformationTest(choiceToNormal, Function.identity(), "choice-to-normal.1");
	}

	@Test
	public void intervalTermToIntervalAtomSimpleTest() {
		genericTransformationTest(intervalRewriting, NormalProgram::fromInputProgram, "interval.1");
	}

	@Test
	public void intervalTermToIntervalAtomExternalAtomTest() {
		genericTransformationTest(intervalRewriting, NormalProgram::fromInputProgram, "interval-external_atom");
	}

	@Test
	public void intervalTermToIntervalAtomComparisonAtomTest() {
		genericTransformationTest(intervalRewriting, NormalProgram::fromInputProgram, "interval-comparison_atom");
	}

	@SuppressWarnings("unused")
	@at.ac.tuwien.kr.alpha.api.externals.Predicate(name = "say_true")
	public static boolean sayTrue(int val) {
		// Dummy method so we can have an external in the transformation test.
		return true;
	}

}
