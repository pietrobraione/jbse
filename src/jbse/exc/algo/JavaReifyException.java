package jbse.exc.algo;

public class JavaReifyException extends Exception {
	private String s;
	
	public JavaReifyException(String string) {
		this.s = string;
	}
	
	public String subject() {
		return s;
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 8053563472463491773L;

}
