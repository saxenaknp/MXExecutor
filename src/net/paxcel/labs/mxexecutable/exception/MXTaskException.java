package net.paxcel.labs.mxexecutable.exception;

/**
 * Exception which is used in different scenarios.
 * 
 * @author Kuldeep
 * 
 */
public class MXTaskException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2497010680044301888L;

	public MXTaskException() {
		this("");
	}

	public MXTaskException(String errorMessage) {
		super(errorMessage, null);
	}

	public MXTaskException(String errorMessage, Throwable throwable) {
		super(errorMessage, throwable);
	}

	public String toString() {
		return this.getMessage();
	}

}
