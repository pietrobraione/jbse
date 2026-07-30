package jbse.mem;

import jbse.bc.ClassFile;
import jbse.common.exc.InvalidInputException;

/**
 * A path condition {@link Clause}, an assumption 
 * that some class is not initialized when the symbolic 
 * execution starts.
 *
 * @author Pietro Braione
 *
 */
public class ClauseAssumeClassNotInitialized implements Clause {
    private final ClassFile classFile;

    /**
     * Constructor.
     * 
     * @param classFile a {@code ClassFile}, the class assumed
     *         to be not initialized. It must not be {@code null}.
     * @throws InvalidInputException if {@code classFile == null}.
     */
    public ClauseAssumeClassNotInitialized(ClassFile classFile) 
    throws InvalidInputException {
    	if (classFile == null) {
    		throw new InvalidInputException("Tried to build a ClauseAssumeClassNotInitialized with null classFile parameter.");
    	}
        this.classFile = classFile; 
    }

    /**
     * Returns the not initialized class.
     * 
     * @return a {@link ClassFile}, the class assumed
     *         to be not initialized.
     */
    public ClassFile getClassFile() { 
    	return this.classFile; 
    }	

    @Override
    public void accept(ClauseVisitor v) throws Exception {
        v.visitClauseAssumeClassNotInitialized(this);
    }

    @Override
    public int hashCode() {
        final int prime = 1283;
        int result = 1;
        result = prime * result + ((this.classFile == null) ? 0 : this.classFile.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ClauseAssumeClassNotInitialized other = (ClauseAssumeClassNotInitialized) obj;
        if (this.classFile == null) {
            if (other.classFile != null) {
                return false;
            }
        } else if (!this.classFile.equals(other.classFile)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "!pre_init(" + this.classFile + ")";
    }


    @Override
    public ClauseAssumeClassNotInitialized clone() {
        final ClauseAssumeClassNotInitialized o;
        try {
            o = (ClauseAssumeClassNotInitialized) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new InternalError(e);
        }
        return o;
    }
}
