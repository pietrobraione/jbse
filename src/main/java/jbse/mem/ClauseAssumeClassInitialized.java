package jbse.mem;

import jbse.bc.ClassFile;

/**
 * A path condition {@link Clause}, an assumption 
 * that some class is already initialized when the symbolic 
 * execution starts.
 *
 * @author Pietro Braione
 *
 */
public class ClauseAssumeClassInitialized implements Clause {
    private final ClassFile classFile;
    private final Klass k;

    /**
     * Constructor.
     * 
     * @param classFile a {@code ClassFile}, class.
     *        It must not be {@code null}.
     * @param k the symbolic {@link Klass} corresponding to {@code classFile}
     *        (cached here for convenience).
     */
    ClauseAssumeClassInitialized(ClassFile classFile, Klass k) { 
        this.classFile = classFile; 
        this.k = k.clone(); //safety copy
    }

    /**
     * Returns the classfile.
     * 
     * @return a {@link ClassFile}, the class assumed initialized.
     */
    public ClassFile getClassFile() { return this.classFile; }	

    Klass getKlass() { 
        return this.k.clone(); //preserves the safety copy
    }

    @Override
    public void accept(ClauseVisitor v) throws Exception {
        v.visitClauseAssumeClassInitialized(this);
    }

    @Override
    public int hashCode() {
        final int prime = 89;
        int result = 1;
        result = prime * result
        + ((classFile == null) ? 0 : classFile.hashCode());
        result = prime * result + ((k == null) ? 0 : k.hashCode());
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
        ClauseAssumeClassInitialized other = (ClauseAssumeClassInitialized) obj;
        if (this.classFile == null) {
            if (other.classFile != null) {
                return false;
            }
        } else if (this.classFile != other.classFile) {
            return false;
        }
        if (this.k == null) {
            if (other.k != null) {
                return false;
            }
        } else if (!this.k.equals(other.k)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "pre_init(" + this.classFile.getClassName() + ")";
    }


    @Override
    public ClauseAssumeClassInitialized clone() {
        final ClauseAssumeClassInitialized o;
        try {
            o = (ClauseAssumeClassInitialized) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new InternalError(e);
        }
        return o;
    }
}
