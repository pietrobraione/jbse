package jbse.exc.bc;

// ("java/lang/NoSuchMethodError");
public class MethodNotFoundException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -964367016588053873L;
	
	public MethodNotFoundException(String methodSignature) {
		super(methodSignature);
	}

}
