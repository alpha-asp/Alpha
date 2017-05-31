package at.ac.tuwien.kr.alpha;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import static at.ac.tuwien.kr.alpha.Main.main;
import static at.ac.tuwien.kr.alpha.Main.parseVisit;

public class MainTest {
	public static InputStream stream(String file) {
		return new ByteArrayInputStream(file.getBytes());
	}

	@Test
	@Ignore
	public void parseSimpleProgram() throws IOException {
		parseVisit(
			"p(X) :- q(X).\n" +
			"q(a).\n" +
			"q(b).\n"
		);
	}

	@Test
	public void parseProgramWithNegativeBody() throws IOException {
		parseVisit(
			"p(X) :- q(X), not q(a).\n" +
				"q(a).\n"
		);
	}

	@Test(expected = UnsupportedOperationException.class)
	@Ignore
	public void parseProgramWithFunction() throws IOException {
		parseVisit(
			"p(X) :- q(f(X)).\n" +
				"q(a).\n"
		);
	}

	@Test(expected = UnsupportedOperationException.class)
	@Ignore
	public void parseProgramWithDisjunctionInHead() throws IOException {
		parseVisit(
			"r(X) | q(X) :- q(X).\n" +
				"q(a).\n"
		);
	}

	@Test
	@Ignore
	public void testLargeInputProgram() {
		Logger root = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
		root.setLevel(Level.DEBUG);
		//main(new String[]{"-g", "naive", "-s", "default", "-n", "2", "-i", "./benchmarks/omiga/omiga-testcases/locstrat/locstrat-200.txt"});
		//main(new String[]{"-g", "naive", "-s", "default", "-n", "2", "-i", "./benchmarks/omiga/omiga-testcases/cutedge/cutedge-100-50.txt"});
		//main(new String[]{"-g", "naive", "-s", "default", "-n", "10", "-i", "./benchmarks/omiga/omiga-testcases/3col/3col-20-38.txt"});
		//main(new String[]{"-g", "naive", "-s", "naive", "-n", "10", "-i", "./benchmarks/omiga/omiga-testcases/reach/reach-1.txt"});
		main(new String[]{"-g", "naive", "-s", "default", "-n", "1", "-i", "./benchmarks/siemens/vehicle_normal_small.asp"});
	}

}