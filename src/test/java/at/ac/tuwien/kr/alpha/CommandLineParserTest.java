package at.ac.tuwien.kr.alpha;

import java.util.Arrays;

import org.apache.commons.cli.ParseException;
import org.junit.Assert;
import org.junit.Test;

import at.ac.tuwien.kr.alpha.config.AlphaContext;
import at.ac.tuwien.kr.alpha.config.CommandLineParser;
import at.ac.tuwien.kr.alpha.config.InputConfig.InputSource;

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
		parser.parseCommandLine(new String[] { "-h" });
		Assert.assertTrue(!(bld.toString().isEmpty()));
	}

	@Test
	public void basicUsageWithFile() throws ParseException {
		CommandLineParser parser = new CommandLineParser("java-jar Alpha-bundled.jar", (msg) -> {});
		AlphaContext ctx = parser.parseCommandLine(new String[] { "-i", "someFile.asp", "-i", "someOtherFile.asp" });
		Assert.assertEquals(InputSource.FILE, ctx.getInputConfig().getSource());
		Assert.assertEquals(Arrays.asList(new String[] { "someFile.asp", "someOtherFile.asp" }), ctx.getInputConfig().getFiles());
	}

	@Test
	public void basicUsageWithString() throws ParseException {
		CommandLineParser parser = new CommandLineParser("java-jar Alpha-bundled.jar", (msg) -> {});
		AlphaContext ctx = parser.parseCommandLine(new String[] { "-str", "b :- a." });
		Assert.assertEquals(InputSource.STRING, ctx.getInputConfig().getSource());
		Assert.assertEquals("b :- a.", ctx.getInputConfig().getAspString());
	}

	@Test(expected = ParseException.class)
	public void invalidUsageNoInput() throws ParseException {
		CommandLineParser parser = new CommandLineParser("java-jar Alpha-bundled.jar", (msg) -> {});
		parser.parseCommandLine(new String[] {});
	}
	
	@Test(expected = ParseException.class)
	public void invalidUsageMoreThanOneInputSource() throws ParseException {
		CommandLineParser parser = new CommandLineParser("java-jar Alpha-bundled.jar", (msg) -> {});
		parser.parseCommandLine(new String[] {"-i", "a.b", "-i", "b.c", "-str", "aString."});
	}

}
