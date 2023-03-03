package at.ac.tuwien.kr.alpha.commons.util;

public class TestExecutionException extends Exception {

	public TestExecutionException(String msg) {
		super(msg);
	}

	public TestExecutionException(String msg, Throwable t) {
		super(msg, t);
	}

}
