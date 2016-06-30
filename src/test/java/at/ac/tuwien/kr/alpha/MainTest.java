package at.ac.tuwien.kr.alpha;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

public class MainTest {
	private InputStream stream(String file) {
		return new ByteArrayInputStream(file.getBytes());
	}

	@Test
	public void parseSimpleProgram() throws IOException {
		Main.parse(stream(
			"p(X) :- q(X).\n" +
			"q(a).\n" +
			"q(b).\n"
		));
	}

	@Test
	public void parseProgramWithNegativeBody() throws IOException {
		Main.parse(stream(
			"p(X) :- q(X), not q(a).\n" +
				"q(a).\n"
		));
	}

	@Test(expected = UnsupportedOperationException.class)
	public void parseProgramWithFunction() throws IOException {
		Main.parse(stream(
			"p(X) :- q(f(X)).\n" +
				"q(a).\n"
		));
	}

	@Test(expected = UnsupportedOperationException.class)
	public void parseProgramWithDisjunctionInHead() throws IOException {
		Main.parse(stream(
			"r(X) | q(X) :- q(X).\n" +
				"q(a).\n"
		));
	}
}