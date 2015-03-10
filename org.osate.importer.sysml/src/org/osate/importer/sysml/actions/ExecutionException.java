package org.osate.importer.sysml.actions;

/**
 * Root exception of the package.
 *
 * @author cedric dumoulin
 *
 */
public class ExecutionException extends Exception {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Constructor.
	 *
	 */
	public ExecutionException() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * Constructor.
	 *
	 * @param message
	 */
	public ExecutionException(String message) {
		super(message);
		// TODO Auto-generated constructor stub
	}

	/**
	 * Constructor.
	 *
	 * @param cause
	 */
	public ExecutionException(Throwable cause) {
		super(cause);
		// TODO Auto-generated constructor stub
	}

	/**
	 * Constructor.
	 *
	 * @param message
	 * @param cause
	 */
	public ExecutionException(String message, Throwable cause) {
		super(message, cause);
		// TODO Auto-generated constructor stub
	}

}
