package at.ac.tuwien.kr.alpha.common;

import org.junit.Assert;
import org.junit.Test;

public class AnswerSetFormatterTest {

	@Test
	public void basicFormatterWithSeparator() {
		AnswerSetFormatter<String> fmt = new BasicAnswerSetFormatter(" bla ");
		AnswerSet as = new AnswerSetBuilder().predicate("p").instance("a").predicate("q").instance("b").build();
		String formatted = fmt.format(as);
		Assert.assertEquals("{ p(\"a\") bla q(\"b\") }", formatted);
	}

}
