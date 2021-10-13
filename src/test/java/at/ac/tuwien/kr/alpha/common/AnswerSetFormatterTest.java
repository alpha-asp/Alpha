package at.ac.tuwien.kr.alpha.common;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class AnswerSetFormatterTest {

	@Test
	public void basicFormatterWithSeparator() {
		AnswerSetFormatter<String> fmt = new SimpleAnswerSetFormatter(" bla ");
		AnswerSet as = new AnswerSetBuilder().predicate("p").instance("a").predicate("q").instance("b").build();
		String formatted = fmt.format(as);
		assertEquals("{ p(\"a\") bla q(\"b\") }", formatted);
	}

}
