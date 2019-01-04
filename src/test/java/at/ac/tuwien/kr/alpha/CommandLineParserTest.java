package at.ac.tuwien.kr.alpha;

import org.apache.commons.cli.ParseException;
import org.junit.Test;

public class CommandLineParserTest {

	@Test
	public void smokeTest() throws ParseException {
		CommandLineParser parser = new CommandLineParser();
		parser.parseCommandLine(new String[] { "-i", "somefile", "-n", "13", "-g", "naive" });
	}

}
