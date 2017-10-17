package jbse.bc;

/**
 * Class that represent an entry in the exception table. An entry
 * memorizes the fact that, when an exception of some class is raised 
 * by an instruction in a given program counter range (the try block), 
 * then the control must be transferred to a given instruction (the catch
 * block). 
 */
public class ExceptionTableEntry {
    private int[] exEntry;
    private String exType;

    /**
     * Constructor.
     * 
     * @param start an {@code int}, the start of the try block.
     * @param end an {@code int}, the end of the try block.
     * @param handle an {@code int}, the entry point of the catch block.
     * @param type a {@link String}, the class name of a throwable class.
     */
    public ExceptionTableEntry(int start, int end, int handle, String type) {
        this.exEntry = new int[3];
        this.exEntry[0] = start;
        this.exEntry[1] = end;
        this.exEntry[2] = handle;
        this.exType = type;
    }
    
    /**
     * Returns the program counter of the start of the try block
     * referred by this entry.
     * 
     * @return an {@code int}.
     */ 
    public int getStartPC() {
        return this.exEntry[0];
    }
    
    /**
     * Returns the program counter of the end of the try block
     * referred by this entry.
     * 
     * @return an {@code int}.
     */ 
    public int getEndPC() {
        return this.exEntry[1];
    }
    
    /**
     * Returns the program counter of the start of the catch block
     * referred by this entry.
     * 
     * @return an {@code int}.
     */ 
    public int getPCHandle() {
        return this.exEntry[2];
    }
    
    /**
     * Returns the type of the throwable this entry refers to.
     * 
     * @return a {@link String}, the name of a throwable class.
     */ 
    public String getType() {
        return this.exType;
    }
}