package jbse.val;

import java.util.Arrays;

import jbse.bc.ClassFile;
import jbse.common.Type;
import jbse.common.exc.UnexpectedInternalException;
import jbse.val.exc.InvalidTypeException;

/**
 * A {@link Reference} to a concrete array which may not yet 
 * have a corresponding {@link Array} instance in the 
 * {@link Heap}. A {@link ReferenceArrayImmaterial} is always stored 
 * in an {@link Array} created by a [multi]anewarray with symbolic 
 * length values. When the container {@link Array} is accessed, it returns 
 * a {@link ReferenceArrayImmaterial}, which is in turns used to lazily 
 * create concrete arrays in a layer. A {@link ReferenceArrayImmaterial} always
 * refers to objects created after the start of the symbolic execution. 
 * A {@link ReferenceArrayImmaterial} shall not escape to the operand stack!!!
 * 
 * @author Pietro Braione
 */
public final class ReferenceArrayImmaterial extends Reference {
    /** The type of the array. */
    private final ClassFile arrayType;

    /** The lengths of the array's dimensions. */
    private final Primitive[] arrayLength;

    public ReferenceArrayImmaterial(ClassFile arrayType, Primitive[] arrayLength) 
    throws InvalidTypeException {
        super();
        if (Type.getDeclaredNumberOfDimensions(arrayType.getClassName()) != arrayLength.length) {
            throw new InvalidTypeException("the number of declared dimensions of the array disagrees with the number of length values");
        }
        this.arrayType = arrayType;
        this.arrayLength = arrayLength.clone(); //safety copy
    }

    /**
     * Returns the type of the array this 
     * object models.
     * 
     * @return a {@code String}, an array type.
     */
    public ClassFile getArrayType() {
        return this.arrayType;
    }

    /** 
     * Gets the length of the array.
     * 
     * @return a {@link Primitive}, the length of the 
     *         array (i.e., of its first dimension). As an
     *         example, if {@code this} is an
     *         {@link ReferenceArrayImmaterial} satisfied by
     *         an array with dimensions {@code [foo][bar]},
     *         {@code this.getLength()} returns {@code foo}. 
     *           
     */
    public Primitive getLength() {
        return this.arrayLength[0];
    }

    /**
     * Gets a {@link ReferenceArrayImmaterial} for the members
     * of the array this object refers to.
     *  
     * @return an {@link ReferenceArrayImmaterial} for all the 
     *         members of the array whose shape is modeled by
     *         {@code this} object.
     *         As an example, if {@code this} is an
     *         {@link ReferenceArrayImmaterial} satisfied by
     *         an array with dimensions {@code [foo][bar]},
     *         {@code this.next()} returns {@code [bar]}.
     *         If {@code this} describes a monodimensional 
     *         array, the method returns 
     *         {@code null} (e.g., for the above example
     *         {@code this.getMember().getMember() == null}).
     *          
     */
    public ReferenceArrayImmaterial getMember() {
        if (this.arrayLength.length > 1) {
            Primitive[] newLength = new Primitive[this.arrayLength.length - 1];
            boolean first = true;
            int i = 0;
            for (Primitive p : this.arrayLength) {
                if (first) {
                    first = false;
                } else {
                    newLength[i] = p;
                }
                i++;
            }
            try {
                return new ReferenceArrayImmaterial(this.arrayType.getMemberClass(), newLength);
            } catch (InvalidTypeException e) {
                //this should never happen
                throw new UnexpectedInternalException(e);
            }
        } else {
            return null;
        }
    }
    
    @Override
    public void accept(ReferenceVisitor v) throws Exception {
    	v.visitReferenceArrayImmaterial(this);
    }

    @Override
    public boolean isSymbolic() {
        return false; 
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null) {
            return false;
        }
        if (this.getClass() != o.getClass()) {
            return false;
        }
        final ReferenceArrayImmaterial c = (ReferenceArrayImmaterial) o;
        if (c.arrayLength.length != this.arrayLength.length) {
            return false;
        }
        //heap position is unknown
        if (this.arrayType != c.arrayType) {
            return false;
        }
        return Arrays.equals(this.arrayLength, c.arrayLength);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Arrays.hashCode(this.arrayLength);
        result = prime * result +
            ((this.arrayType == null) ? 0 : this.arrayType.hashCode());
        return result;
    }

    @Override
    public String toString() {
        final StringBuilder buf = new StringBuilder();
        buf.append("{R[");
        boolean isFirst = true;
        for (Primitive p : this.arrayLength) {
            if (isFirst) {
                isFirst = false;
            } else {
                buf.append("][");
            }
            if (p == null) {
                buf.append("*");
            } else {
                buf.append(p.toString());
            }
        }
        buf.append("]}");
        return buf.toString();
    }
}
