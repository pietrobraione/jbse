package jbse.algo.exc;

/**
 * Exception thrown whenever a native method invocation fails because it is not possible to 
 * access reflectively to a method and perform a metacircular method invocation.
 * 
 * @author Pietro Braione
 *
 */
public class CannotAccessImplementationReflectively extends CannotInvokeNativeException {
    /**
     * 
     */
    private static final long serialVersionUID = -2537489831129866617L;

    public CannotAccessImplementationReflectively(Throwable e) { 
        super(e); 
    }
}
