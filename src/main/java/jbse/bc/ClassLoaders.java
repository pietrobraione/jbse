package jbse.bc;

/**
 * Constants related to class loading.
 * 
 * @author Pietro Braione
 */
public interface ClassLoaders {
    /** The identifier for no classloader. Only anonymous classes may have no classloader. */
    int CLASSLOADER_NONE = -1;
    
    /** The identifier for the bootstrap classloader. */
    int CLASSLOADER_BOOT = 0;
    
    /** The identifier for the extension classloader. */
    int CLASSLOADER_EXT  = 1;
    
    /** The identifier for the application classloader. */
    int CLASSLOADER_APP  = 2;
}
