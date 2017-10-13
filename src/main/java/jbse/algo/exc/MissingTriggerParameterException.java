package jbse.algo.exc;

/**
 * Exception raised whenever no object in the heap matches 
 * the parameter part of a trigger rule. 
 *  
 * @author Pietro Braione
 *
 */
public class MissingTriggerParameterException extends CannotManageStateException {

    /**
     * 
     */
    private static final long serialVersionUID = -5478561178707984607L;

    public MissingTriggerParameterException(String param) {
        super(param);
    }
}
