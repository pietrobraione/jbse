package jbse.bc;

import java.util.ArrayList;
import java.util.List;

/**
 * Class that represent an exception table.
 */
public class ExceptionTable {
    private ArrayList<ExceptionTableEntry> exceptionTable;

    /**
     * Constructor, Initialize the structure that will contain the exception table.
     * 
     * @param length an {@code int}, the number of elements in exception table.
     */		
    public ExceptionTable(int length) {	
        exceptionTable = new ArrayList<ExceptionTableEntry>(length);
    }

    /**
     * Returns the number of element of the exception table.
     * 
     * @return an {@code int}, the number of elements in the exception table.
     */
    public int getLength() {
        return exceptionTable.size();
    }

    /**
     * Seeks an entry in the table.
     * 
     * @param exceptionTypes a {@link List}{@code <}{@link String}{@code >}
     *        containing all the exception types we want to search.
     * @param programCounter an {@code int}, the current program counter.
     * @return an {@link ExceptionTableEntry} or {@code null} if the
     *         exception table does not contain an entry matching
     *         one of the exception types at the program counter.
     */
    public ExceptionTableEntry getEntry(List<String> exceptionTypes, int programCounter) {
        for (ExceptionTableEntry entry : this.exceptionTable) {
            if (exceptionTypes.contains(entry.getType()) && 
                (programCounter >= entry.getProgramCounterStart()) && 
                (programCounter < entry.getProgramCounterEnd())) {
                return entry;
            }
        }
        return null;
    }

    /**
     * Adds an entry to the exception table.
     * 
     * @param entry The {@link ExceptionTableEntry} to add.
     */
    public void addEntry(ExceptionTableEntry entry) {
        exceptionTable.add(entry);
    }
}
