package at.ac.tuwien.kr.alpha.api.terms;

/**
 * A term representing the result of an evolog action (i.e. result of action function application).
 * Action result terms are function terms with symbol "success" or "error" depending on whether the corresponding action was sucessful.
 * There is always one argument which is either some term representing the actual function result or an error message, respectively.
 */
public interface ActionResultTerm<T extends Term> extends FunctionTerm {

	public static final String SUCCESS_SYMBOL = "success";
	public static final String ERROR_SYMBOL = "error";

	/**
	 * True if the action that generated this result was successful (i.e. executed normally).
	 */
	boolean isSuccess();

	/**
	 * True if the action that generated this result failed (i.e. threw an error in execution).
	 */
	boolean isError();

	/**
	 * Gets the actual value wrapped in this result.
	 * Either a term representing the action return value or a string term representing an error
	 * message.s
	 */
	T getValue();
}
