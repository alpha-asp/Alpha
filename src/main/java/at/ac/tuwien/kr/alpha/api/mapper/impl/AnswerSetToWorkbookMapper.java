/**
 * Copyright (c) 2020, the Alpha Team.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1) Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 *
 * 2) Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package at.ac.tuwien.kr.alpha.api.mapper.impl;

import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.tuwien.kr.alpha.api.mapper.AnswerSetToObjectMapper;
import at.ac.tuwien.kr.alpha.common.AnswerSet;
import at.ac.tuwien.kr.alpha.common.Predicate;
import at.ac.tuwien.kr.alpha.common.atoms.Atom;
import at.ac.tuwien.kr.alpha.common.atoms.BasicAtom;
import at.ac.tuwien.kr.alpha.common.terms.Term;

/**
 * Implementation of {@link AnswerSetToObjectMapper} that generates an office open xml workbook ("excel file") from a given answer set.
 * 
 * Copyright (c) 2020, the Alpha Team.
 */
public class AnswerSetToWorkbookMapper implements AnswerSetToObjectMapper<Workbook> {

	private static final Logger LOGGER = LoggerFactory.getLogger(AnswerSetToWorkbookMapper.class);

	/**
	 * Creates an xlsx workbook containing all the atoms from the given {@link AnswerSet} with one sheet per predicate. All predicates with arity 0 are listed
	 * in a special sheet called "flags". Caution, potential resource leak: note that the returned workbook needs to be closed by the caller once it has been
	 * processed (written to file etc).
	 */
	@Override
	public Workbook mapFromAnswerSet(AnswerSet answerSet) {
		LOGGER.debug("Start mapping answer set to workbook");
		Workbook workbook = new XSSFWorkbook();
		// first, create a worksheet for 0-arity predicates
		Sheet flags = workbook.createSheet("Flags");
		Sheet currentPredicateSheet;
		for (Predicate pred : answerSet.getPredicates()) {
			if (pred.getArity() == 0) {
				// 0-artiy predicate has no instances in answer set, create a dummy atom
				this.writeAtomToSheet(flags, new BasicAtom(pred));
			} else {
				currentPredicateSheet = workbook.createSheet(pred.getName() + "_" + pred.getArity());
				for (Atom atom : answerSet.getPredicateInstances(pred)) {
					this.writeAtomToSheet(currentPredicateSheet, atom);
				}
			}
		}
		return workbook;
	}

	private void writeAtomToSheet(Sheet sheet, Atom atom) {
		int rownum = -1;
		if (sheet.getLastRowNum() == 0 && sheet.getRow(0) == null) {
			// sheet is empty, start at row zero
			rownum = 0;
		} else {
			rownum = sheet.getLastRowNum() + 1;
		}
		Row atomRow = sheet.createRow(rownum);
		List<Term> terms = atom.getTerms();
		Cell currCell;
		if (terms.isEmpty()) {
			// 0-arity atom
			currCell = atomRow.createCell(0);
			currCell.setCellValue(atom.getPredicate().getName());
			sheet.autoSizeColumn(0);
		} else {
			for (int i = 0; i < terms.size(); i++) {
				currCell = atomRow.createCell(i);
				currCell.setCellValue(terms.get(i).toString());
				sheet.autoSizeColumn(i);
			}
		}
	}

}
