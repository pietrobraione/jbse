package jbse.mem;

import jbse.bc.ClassFile;
import jbse.common.exc.InvalidInputException;
import jbse.val.ReferenceConcrete;

/**
 * Class that represent an instance of an object with class {@code java.lang.Class} 
 * in the heap.
 */
public interface Instance_JAVA_CLASS extends Instance {
    /**
     * Returns the class this {@code Instance}
     * of {@code java.lang.Class} represents.
     * 
     * @return a {@link ClassFile}, the  
     * represented class.
     */
    ClassFile representedClass();
    
    /**
     * Sets the signers of this {@code Instance}
     * of {@code java.lang.Class}.
     * 
     * @param signers a {@link ReferenceConcrete} to
     *        a monodimensional array of objects.
     * @throws InvalidInputException if {@code signers == null}.
     */
    void setSigners(ReferenceConcrete signers) throws InvalidInputException;
    
    /**
     * Returns the signers of this {@code Instance}
     * of {@code java.lang.Class}.
     * 
     * @return the {@link ReferenceConcrete} to
     *        a monodimensional array of objects
     *        previously set with {@link #setSigners}, 
     *        or {@code null} if {@link #setSigners}
     *        was not invoked before.
     */
    ReferenceConcrete getSigners();
    
    Instance_JAVA_CLASS clone();
}
