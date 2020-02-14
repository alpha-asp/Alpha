package at.ac.tuwien.kr.alpha;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.Assert;
import org.junit.Test;

import at.ac.tuwien.kr.alpha.common.AnswerSet;
import at.ac.tuwien.kr.alpha.common.AnswerSetBuilder;

public class AnswerSetToXlsxWriterTest {

	@Test
	public void writeAnswerSetFilesTest() throws IOException {
		AnswerSet as = new AnswerSetBuilder().predicate("bla").instance("blubb", "blubb").instance("foo", "bar").predicate("foo").instance("bar")
				.instance("baz").predicate("complex").instance(Integer.valueOf(1), Integer.valueOf(2), Integer.valueOf(3)).build();
		Path tmpDir = Files.createTempDirectory("alpha-test-xlsx-output");
		AnswerSetToXlsxWriter writer = new AnswerSetToXlsxWriter(tmpDir.toString() + "/alphaAnswerSet");
		writer.accept(0, as);
		File tmpDirFile = tmpDir.toFile();
		File[] generatedFiles = tmpDirFile.listFiles();
		Assert.assertEquals(generatedFiles.length, 1);
		File answerSetFile = generatedFiles[0];
		Assert.assertEquals("alphaAnswerSet.0.xlsx", answerSetFile.getName());
		// clean up
		answerSetFile.delete();
		tmpDirFile.delete();
	}

}
