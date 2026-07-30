package jbse.mem;

import jbse.bc.ClassFile;
import jbse.common.exc.InvalidInputException;

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
     * @param classFile a {@code ClassFile}.
     *        It must not be {@code null}.
     * @param k the symbolic or concrete initial {@link Klass} corresponding to 
     *        {@code classFile}. In the latter case, {@code k} is zeroed.
     *        It must not be {@code null}.  
     * @throws InvalidInputException if {@code classFile == null} or {@code k == null}.
     */
    public ClauseAssumeClassInitialized(ClassFile classFile, Klass k) 
    throws InvalidInputException {
        if (classFile == null || k == null) {
            throw new InvalidInputException("Tried to build a ClauseAssumeClassInitialized with null classFile or k parameter.");
        }
        this.classFile = classFile; 
        this.k = k.clone(); //safety copy
    }

    /**
     * Returns the classfile.
     * 
     * @return a {@link ClassFile}, the class assumed initialized.
     */
    public ClassFile getClassFile() { 
        return this.classFile;
    }
    
    Klass getKlass() { //used only by State for refinement
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
        result = prime * result + this.classFile.hashCode();
        result = prime * result + this.k.hashCode();
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
        final ClauseAssumeClassInitialized other = (ClauseAssumeClassInitialized) obj;
        if (!this.classFile.equals(other.classFile)) {
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
