package at.ac.tuwien.kr.alpha;

import org.junit.Ignore;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import static at.ac.tuwien.kr.alpha.Main.main;
import static at.ac.tuwien.kr.alpha.Main.parseVisit;
import static at.ac.tuwien.kr.alpha.Util.stream;

public class MainTest {
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
	@Ignore
	public void testLargeInputProgram() {
		//main(new String[]{"-g", "naive", "-s", "default", "-n", "10", "-i", "./benchmarks/omiga/omiga-testcases/locstrat/locstrat-200.txt"});

		main(new String[]{"-g", "naive", "-s", "default", "-n", "10", "-i", "./benchmarks/omiga/omiga-testcases/reach/reach-4.txt"});
	}

}