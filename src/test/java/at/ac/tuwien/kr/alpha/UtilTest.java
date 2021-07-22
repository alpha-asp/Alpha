package at.ac.tuwien.kr.alpha;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

public class UtilTest {
	
	@Test
	public void oops() throws Exception {
		assertThrows(RuntimeException.class, () -> {
			throw Util.oops("Ha", new UnsupportedOperationException("Ho"));
		});
	}

	@Test
	public void oops1() throws Exception {
		assertThrows(RuntimeException.class, () -> {
			throw Util.oops("Ha");
		});
	}

	@Test
	public void oops2() throws Exception {
		assertThrows(RuntimeException.class, () -> {
			throw Util.oops();
		});
	}
}