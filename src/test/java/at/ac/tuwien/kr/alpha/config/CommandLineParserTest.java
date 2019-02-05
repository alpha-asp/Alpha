package at.ac.tuwien.kr.alpha.config;

import java.util.Arrays;

import org.apache.commons.cli.ParseException;
import org.junit.Assert;
import org.junit.Test;

public class CommandLineParserTest {

	/**
	 * Tests that a help message is written to the consumer configured in the
	 * parser.
	 * 
	 * @throws ParseException shouldn't happen
	 */
	@Test
	public void help() throws ParseException {
		StringBuilder bld = new StringBuilder();
		CommandLineParser parser = new CommandLineParser("java-jar Alpha-bundled.jar", (msg) -> bld.append(msg));
		parser.parseCommandLine(new String[] {"-h"});
		Assert.assertTrue(!(bld.toString().isEmpty()));
	}

	@Test
	public void basicUsageWithFile() throws ParseException {
		CommandLineParser parser = new CommandLineParser("java-jar Alpha-bundled.jar", (msg) -> { });
		AlphaContext ctx = parser.parseCommandLine(new String[] {"-i", "someFile.asp", "-i", "someOtherFile.asp"});
		Assert.assertEquals(Arrays.asList(new String[] {"someFile.asp", "someOtherFile.asp"}), ctx.getInputConfig().getFiles());
	}

	@Test
	public void basicUsageWithString() throws ParseException {
		CommandLineParser parser = new CommandLineParser("java-jar Alpha-bundled.jar", (msg) -> { });
		AlphaContext ctx = parser.parseCommandLine(new String[] {"-str", "b :- a.", "-str", "c :- a, b."});
		Assert.assertEquals(Arrays.asList(new String[] {"b :- a.", "c :- a, b."}), ctx.getInputConfig().getAspStrings());
	}

	@Test(expected = ParseException.class)
	public void invalidUsageNoInput() throws ParseException {
		CommandLineParser parser = new CommandLineParser("java-jar Alpha-bundled.jar", (msg) -> { });
		parser.parseCommandLine(new String[] {});
	}

	@Test
	public void moreThanOneInputSource() throws ParseException {
		CommandLineParser parser = new CommandLineParser("java-jar Alpha-bundled.jar", (msg) -> { });
		parser.parseCommandLine(new String[] {"-i", "a.b", "-i", "b.c", "-str", "aString."});
	}

	@Test(expected = ParseException.class)
	public void invalidUsageMissingInputFlag() throws ParseException {
		CommandLineParser parser = new CommandLineParser("java-jar Alpha-bundled.jar", (msg) -> { });
		parser.parseCommandLine(new String[] {"-i", "a.b", "b.c"});
	}

	@Test
	public void numAnswerSets() throws ParseException {
		CommandLineParser parser = new CommandLineParser("java-jar Alpha-bundled.jar", (msg) -> { });
		AlphaContext ctx = parser.parseCommandLine(new String[] {"-str", "aString.", "-n", "00435"});
		Assert.assertEquals(435, ctx.getInputConfig().getNumAnswerSets());
	}
	
	@Test(expected = ParseException.class)
	public void noInputGiven() throws ParseException {
		CommandLineParser parser = new CommandLineParser("java -jar Alpha--bundled.jar", (msg) -> { });
		parser.parseCommandLine(new String[] {});
	}

}
