package at.ac.tuwien.kr.alpha.api.externals.stdlib;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.tuwien.kr.alpha.api.Alpha;
import at.ac.tuwien.kr.alpha.common.AnswerSet;
import at.ac.tuwien.kr.alpha.common.Predicate;
import at.ac.tuwien.kr.alpha.common.atoms.Atom;
import at.ac.tuwien.kr.alpha.common.program.InputProgram;
import at.ac.tuwien.kr.alpha.common.terms.ConstantTerm;
import at.ac.tuwien.kr.alpha.config.InputConfig;

public class AspStandardLibraryTest {

	private static final Logger LOGGER = LoggerFactory.getLogger(AspStandardLibrary.class);

	//@formatter:off
	private static final String STRINGSTUFF_ASP =
			"string(\"bla\")."
			+ "string(\"blubb\")."
			+ "string(\"foo\")."
			+ "string(\"bar\")."
			+ "{ strcat(S1, S2) } :- string(S1), string(S2)."
			+ "resultstring(SCAT) :- strcat(S1, S2), &stdlib_string_concat[S1, S2](SCAT)."
			+ ":- resultstring(S), &stdlib_string_length[S](LEN), LEN != 6."
			+ "containsFoo(S) :- resultstring(S), &stdlib_string_matches_regex[S, \".*foo.*\"]."
			+ ":- resultstring(S), not containsFoo(S)."
			+ "has_resultstring :- resultstring(_)."
			+ ":- not has_resultstring.";
	
	// same as stringstuff asp, but without the "containsFoo" intermediate predicate
	private static final String NEGATED_EXTERNAL_ASP =
			"string(\"bla\")."
			+ "string(\"blubb\")."
			+ "string(\"foo\")."
			+ "string(\"bar\")."
			+ "{ strcat(S1, S2) } :- string(S1), string(S2)."
			+ "resultstring(SCAT) :- strcat(S1, S2), &stdlib_string_concat[S1, S2](SCAT)."
			+ ":- resultstring(S), &stdlib_string_length[S](LEN), LEN != 6."
			+ ":- resultstring(S), not &stdlib_string_matches_regex[S, \".*foo.*\"]."
			+ "has_resultstring :- resultstring(_)."
			+ ":- not has_resultstring.";
	//@formatter:on

	@Test
	public void parseDateTime1() {
		Set<List<ConstantTerm<Integer>>> dtSubstitution = AspStandardLibrary.datetimeParse("20.05.2020 01:19:13", "dd.MM.yyyy HH:mm:ss");
		assertEquals(1, dtSubstitution.size());
		List<ConstantTerm<Integer>> dtTerms = dtSubstitution.stream().findFirst().get();
		assertEquals(6, dtTerms.size());
		assertEquals(ConstantTerm.getInstance(2020), dtTerms.get(0));
		assertEquals(ConstantTerm.getInstance(5), dtTerms.get(1));
		assertEquals(ConstantTerm.getInstance(20), dtTerms.get(2));
		assertEquals(ConstantTerm.getInstance(1), dtTerms.get(3));
		assertEquals(ConstantTerm.getInstance(19), dtTerms.get(4));
		assertEquals(ConstantTerm.getInstance(13), dtTerms.get(5));
	}

	@Test
	public void parseDateTime2() {
		Set<List<ConstantTerm<Integer>>> dtSubstitution = AspStandardLibrary.datetimeParse("07/2123/18 22/37/01", "MM/yyyy/dd HH/mm/ss");
		assertEquals(1, dtSubstitution.size());
		List<ConstantTerm<Integer>> dtTerms = dtSubstitution.stream().findFirst().get();
		assertEquals(6, dtTerms.size());
		assertEquals(ConstantTerm.getInstance(2123), dtTerms.get(0));
		assertEquals(ConstantTerm.getInstance(7), dtTerms.get(1));
		assertEquals(ConstantTerm.getInstance(18), dtTerms.get(2));
		assertEquals(ConstantTerm.getInstance(22), dtTerms.get(3));
		assertEquals(ConstantTerm.getInstance(37), dtTerms.get(4));
		assertEquals(ConstantTerm.getInstance(1), dtTerms.get(5));
	}

	@Test
	public void parseDateTime3() {
		Set<List<ConstantTerm<Integer>>> dtSubstitution = AspStandardLibrary.datetimeParse("\"03,12,2019\", \"11:00:00\"",
				"\"dd,MM,yyyy\", \"HH:mm:ss\"");
		assertEquals(1, dtSubstitution.size());
		List<ConstantTerm<Integer>> dtTerms = dtSubstitution.stream().findFirst().get();
		assertEquals(6, dtTerms.size());
		assertEquals(ConstantTerm.getInstance(2019), dtTerms.get(0));
		assertEquals(ConstantTerm.getInstance(12), dtTerms.get(1));
		assertEquals(ConstantTerm.getInstance(3), dtTerms.get(2));
		assertEquals(ConstantTerm.getInstance(11), dtTerms.get(3));
		assertEquals(ConstantTerm.getInstance(0), dtTerms.get(4));
		assertEquals(ConstantTerm.getInstance(0), dtTerms.get(5));
	}

	@Test
	public void datetimeBefore() {
		assertTrue(AspStandardLibrary.datetimeIsBefore(1990, 2, 14, 15, 16, 17, 1990, 3, 1, 0, 59, 1));
		assertFalse(AspStandardLibrary.datetimeIsBefore(2015, 5, 13, 12, 1, 33, 2003, 1, 1, 0, 0, 1));
		assertFalse(AspStandardLibrary.datetimeIsBefore(2022, 2, 22, 22, 22, 22, 2022, 2, 22, 22, 22, 22));
	}
	
	@Test
	public void datetimeEqual() {
		assertTrue(AspStandardLibrary.datetimeIsEqual(1990, 2, 14, 15, 16, 17, 1990, 2, 14, 15, 16, 17));
		assertFalse(AspStandardLibrary.datetimeIsEqual(2015, 5, 13, 12, 1, 33, 2003, 1, 1, 0, 0, 1));
	}	

	@Test
	public void datetimeBeforeOrEqual() {
		assertTrue(AspStandardLibrary.datetimeIsBeforeOrEqual(1990, 2, 14, 15, 16, 17, 1990, 3, 1, 0, 59, 1));
		assertFalse(AspStandardLibrary.datetimeIsBeforeOrEqual(2015, 5, 13, 12, 1, 33, 2003, 1, 1, 0, 0, 1));
		assertTrue(AspStandardLibrary.datetimeIsBeforeOrEqual(2022, 2, 22, 22, 22, 22, 2022, 2, 22, 22, 22, 22));
		
	}
	
	@Test
	public void matchesRegex() {
		assertTrue(AspStandardLibrary.stringMatchesRegex("Blaaaaa Blubbb!!", "Bla+ Blub+!!"));
		assertFalse(AspStandardLibrary.stringMatchesRegex("Foobar", "Bla+ Blub+!!"));
	}

	@Test
	public void stringLength() {
		Set<List<ConstantTerm<Integer>>> result = AspStandardLibrary.stringLength("A String of length 21");
		assertEquals(1, result.size());
		List<ConstantTerm<Integer>> lengthTerms = result.stream().findFirst().get();
		assertEquals(1, lengthTerms.size());
		ConstantTerm<Integer> lenTerm = lengthTerms.get(0);
		assertEquals(ConstantTerm.getInstance(21), lenTerm);
	}

	@Test
	public void stringConcat() {
		Set<List<ConstantTerm<String>>> result = AspStandardLibrary.stringConcat("Foo", "bar");
		assertEquals(1, result.size());
		List<ConstantTerm<String>> concatTerms = result.stream().findFirst().get();
		assertEquals(1, concatTerms.size());
		ConstantTerm<String> concat = concatTerms.get(0);
		assertEquals(ConstantTerm.getInstance("Foobar"), concat);
	}

	@Test
	@SuppressWarnings("unchecked")
	public void programWithStringStuff() throws IOException {
		Alpha alpha = new Alpha();
		InputProgram prog = alpha.readProgram(InputConfig.forString(STRINGSTUFF_ASP));
		Set<AnswerSet> answerSets = alpha.solve(prog).collect(Collectors.toSet());
		// Verify every result string has length 6 and contains "foo"
		for (AnswerSet as : answerSets) {
			for (Atom atom : as.getPredicateInstances(Predicate.getInstance("resultstring", 1))) {
				String resultstring = ((ConstantTerm<String>) atom.getTerms().get(0)).getObject();
				assertEquals(6, resultstring.length());
				assertTrue(resultstring.contains("foo"));
			}
		}
	}

	@Test
	@SuppressWarnings("unchecked")
	public void negatedExternal() throws IOException {
		Alpha alpha = new Alpha();
		InputProgram prog = alpha.readProgram(InputConfig.forString(NEGATED_EXTERNAL_ASP));
		Set<AnswerSet> answerSets = alpha.solve(prog).collect(Collectors.toSet());
		assertEquals(31, answerSets.size());
		// Verify every result string has length 6 and contains "foo"
		for (AnswerSet as : answerSets) {
			for (Atom atom : as.getPredicateInstances(Predicate.getInstance("resultstring", 1))) {
				String resultstring = ((ConstantTerm<String>) atom.getTerms().get(0)).getObject();
				LOGGER.debug("ResultString is {}", resultstring);
				assertEquals(6, resultstring.length());
				assertTrue(resultstring.contains("foo"));
			}
		}
	}

}
