package jbse.bc;

/**
 * Class that represent an entry in the exception table. An entry
 * memorizes the fact that, when an exception of some class is raised 
 * by an instruction in a given program counter range (the try block), 
 * then the control must be transferred to a given instruction (the catch
 * block).
 * 
 * @author Pietro Braione
 * @author unknown
 */
public final class ExceptionTableEntry {
    private final int programCounterStart;
    private final int programCounterEnd;
    private final int programCounterHandler;
    private final String type;

    /**
     * Constructor.
     * 
     * @param programCounterStart an {@code int}, the start of the try block.
     * @param programCounterEnd an {@code int}, the end of the try block.
     * @param programCounterHandler an {@code int}, the entry point of the catch block.
     * @param type a {@link String}, the class name of a throwable class.
     */
    ExceptionTableEntry(int programCounterStart, int programCounterEnd, int programCounterHandler, String type) {
        this.programCounterStart = programCounterStart;
        this.programCounterEnd = programCounterEnd;
        this.programCounterHandler = programCounterHandler;
        this.type = type;
    }
    
    /**
     * Returns the program counter of the start of the try block
     * referred by this entry.
     * 
     * @return an {@code int}.
     */ 
    public int getProgramCounterStart() {
        return this.programCounterStart;
    }
    
    /**
     * Returns the program counter of the end of the try block
     * referred by this entry.
     * 
     * @return an {@code int}.
     */ 
    public int getProgramCounterEnd() {
        return this.programCounterEnd;
    }
    
    /**
     * Returns the program counter of the start of the catch block
     * referred by this entry.
     * 
     * @return an {@code int}.
     */ 
    public int getProgramCounterHandler() {
        return this.programCounterHandler;
    }
    
    /**
     * Returns the type of the throwable this entry refers to.
     * 
     * @return a {@link String}, the name of a throwable class.
     */ 
    public String getType() {
        return this.type;
    }

    @Override
    public String toString() {
        return "<" + this.programCounterStart + " " + this.programCounterEnd + " " + this.programCounterHandler + " " + this.type + ">";
    }
}