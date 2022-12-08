package jbse.bc;

import java.util.ArrayList;
import java.util.List;

/**
 * Class representing an exception table.
 * 
 * @author Pietro Braione
 * @author unknown
 */
public class ExceptionTable {
	/**
	 * Class representing an entry in the exception table. An entry
	 * memorizes the fact that, when an exception of some class is raised 
	 * by an instruction in a given program counter range (the try block), 
	 * then the control must be transferred to a given instruction (the catch
	 * block).
	 * 
	 * @author Pietro Braione
	 * @author unknown
	 */
	public static final class Entry {
	    public final int programCounterStart;
	    public final int programCounterEnd;
	    public final String type;
	    public final int programCounterHandler;

	    /**
	     * Constructor.
	     * 
	     * @param programCounterStart an {@code int}, the start of the try block.
	     * @param programCounterEnd an {@code int}, the end of the try block.
	     * @param type a {@link String}, the class name of a throwable class.
	     * @param programCounterHandler an {@code int}, the entry point of the catch block.
	     */
	    Entry(int programCounterStart, int programCounterEnd, String type, int programCounterHandler) {
	        this.programCounterStart = programCounterStart;
	        this.programCounterEnd = programCounterEnd;
	        this.programCounterHandler = programCounterHandler;
	        this.type = type;
	    }
	    
	    @Override
	    public String toString() {
	        return "<" + this.programCounterStart + " " + this.programCounterEnd + " " + this.programCounterHandler + " " + this.type + ">";
	    }
	}
	
	private ArrayList<Entry> entries;

    /**
     * Constructor, Initialize the structure that will contain the exception table.
     * 
     * @param length an {@code int}, the number of elements in exception table.
     */		
    public ExceptionTable(int length) {	
        entries = new ArrayList<Entry>(length);
    }

    /**
     * Returns the number of element of the exception table.
     * 
     * @return an {@code int}, the number of elements in the exception table.
     */
    public int getLength() {
        return entries.size();
    }

    /**
     * Seeks an entry in the table.
     * 
     * @param exceptionTypes a {@link List}{@code <}{@link String}{@code >}
     *        containing all the exception types we want to search.
     * @param programCounter an {@code int}, the current program counter.
     * @return an {@link Entry} or {@code null} if the
     *         exception table does not contain an entry matching
     *         one of the exception types at the program counter.
     */
    public Entry getEntry(List<String> exceptionTypes, int programCounter) {
        for (Entry entry : this.entries) {
            if (exceptionTypes.contains(entry.type) && 
                (programCounter >= entry.programCounterStart) && 
                (programCounter < entry.programCounterEnd)) {
                return entry;
            }
        }
        return null;
    }

    /**
     * Adds an entry to the exception table.
     * 
     * @param entry The {@link Entry} to add.
     */
    public void addEntry(Entry entry) {
        entries.add(entry);
    }
}
