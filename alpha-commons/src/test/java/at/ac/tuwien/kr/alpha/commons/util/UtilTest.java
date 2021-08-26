package at.ac.tuwien.kr.alpha.commons.util;

import org.junit.Test;

import at.ac.tuwien.kr.alpha.commons.util.Util;

public class UtilTest {
	@Test(expected = RuntimeException.class)
	public void oops() throws Exception {
		throw Util.oops("Ha", new UnsupportedOperationException("Ho"));
	}

	@Test(expected = RuntimeException.class)
	public void oops1() throws Exception {
		throw Util.oops("Ha");
	}

	@Test(expected = RuntimeException.class)
	public void oops2() throws Exception {
		throw Util.oops();
	}
}