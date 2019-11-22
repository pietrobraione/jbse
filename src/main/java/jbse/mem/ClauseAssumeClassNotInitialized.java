package jbse.mem;

import jbse.bc.ClassFile;

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
     * @param className a {@code String}, the name of the class.
     *        It must not be {@code null}.
     */
    public ClauseAssumeClassNotInitialized(ClassFile className) { 
        this.classFile = className; 
    }

    /**
     * Returns the class name.
     * 
     * @return a {@link String}, the name of the class assumed initialized.
     */
    public ClassFile getClassFile() { return this.classFile; }	

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
