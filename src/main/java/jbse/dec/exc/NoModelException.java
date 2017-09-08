package jbse.dec.exc;

/**
 * Exception thrown if the decision procedure was unablenot
 * to produce a model.
 * 
 * @author Pietro Braione
 *
 */
public class NoModelException extends DecisionException {

    /**
     * 
     */
    private static final long serialVersionUID = -1301198542555359673L;

    public NoModelException() {
        // TODO Auto-generated constructor stub
    }

    public NoModelException(String param) {
        super(param);
        // TODO Auto-generated constructor stub
    }

    public NoModelException(Exception e) {
        super(e);
        // TODO Auto-generated constructor stub
    }

}
