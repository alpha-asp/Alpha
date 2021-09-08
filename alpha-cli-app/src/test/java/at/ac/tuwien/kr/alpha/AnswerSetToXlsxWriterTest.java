package at.ac.tuwien.kr.alpha;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.junit.jupiter.api.Test;

import at.ac.tuwien.kr.alpha.api.AnswerSet;
import at.ac.tuwien.kr.alpha.app.mappers.AnswerSetToWorkbookMapperTest;
import at.ac.tuwien.kr.alpha.commons.AnswerSetBuilder;

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
		assertEquals(1, generatedFiles.length);
		File answerSetFile = generatedFiles[0];
		assertEquals("alphaAnswerSet.0.xlsx", answerSetFile.getName());
		try (Workbook wb = WorkbookFactory.create(answerSetFile)) {
			AnswerSetToWorkbookMapperTest.assertWorkbookMatchesAnswerSet(wb, as);
		}
		// clean up
		answerSetFile.delete();
		tmpDirFile.delete();
	}

	@Test
	public void writeUnsatTest() throws IOException {
		Path tmpDir = Files.createTempDirectory("alpha-test-xlsx-unsat");
		AnswerSetToXlsxWriter.writeUnsatInfo(Paths.get(tmpDir.toString() + "/alphaAnswerSet.UNSAT.xlsx"));
		File tmpDirFile = tmpDir.toFile();
		File[] generatedFiles = tmpDirFile.listFiles();
		assertEquals(1, generatedFiles.length);
		File unsatFile = generatedFiles[0];
		assertEquals("alphaAnswerSet.UNSAT.xlsx", unsatFile.getName());
		try (Workbook wb = WorkbookFactory.create(unsatFile)) {
			Sheet unsatSheet = wb.getSheet("Unsatisfiable");
			assertNotNull(unsatSheet);
			Cell cell = unsatSheet.getRow(0).getCell(0);
			assertNotNull(cell);
			String cellValue = cell.getStringCellValue();
			assertEquals("Input is unsatisfiable - No answer sets!", cellValue);
		}
		// clean up
		unsatFile.delete();
		tmpDirFile.delete();
	}

}
