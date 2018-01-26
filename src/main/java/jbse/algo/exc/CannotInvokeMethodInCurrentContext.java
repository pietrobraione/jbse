package jbse.algo.exc;

/**
 * Exception thrown whenever a native method invocation fails because it is not possible to 
 * invoke the method in the current invocation context. Currently it is thrown only when 
 * {@code sun.reflect.Reflection.getCallerClass} is invoked from the root frame.
 * 
 * @author Pietro Braione
 *
 */
public class CannotInvokeMethodInCurrentContext extends CannotInvokeNativeException {
    /**
     * 
     */
    private static final long serialVersionUID = -3947977828490707849L;

    public CannotInvokeMethodInCurrentContext(String s) { 
        super(s); 
    }
}
