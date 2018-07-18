package jbse.mem;


import jbse.bc.ClassFile;

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
    
    Instance_JAVA_CLASS clone();
}
