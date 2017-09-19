package jbse.bc;

import java.util.ArrayList;
import java.util.List;

/**
 * Class that represent an exception table.
 */
public class ExceptionTable {
	private ArrayList<ExceptionTableEntry> exTable;

	/**
	 *Constructor, Initialize the structure that will contain the exception table
	 *@param length number of element of exception table
	 */		
	public ExceptionTable(int length) {	
		exTable = new ArrayList<ExceptionTableEntry>(length);
	}
	
	/**
	 *Returns the number of element of the exception table
	 *@return int the number of element of the exception table
	 */
	public int getLength() {
		return exTable.size();
	}
	
	/**
	 * Seeks an entry in the table.
	 * 
	 * @param excTypes a {@link List}{@code <}{@link String}{@code >}
	 *        containing all the exception types we want to search.
	 * @param PC an {@code int}, the current program counter.
	 * @return an {@link ExceptionTableEntry} or {@code null} if the
	 *         exception table does not contain an entry matching
	 *         one of the exception types at the program counter.
	 */
	public ExceptionTableEntry getEntry(List<String> excTypes, int PC) {
		for (ExceptionTableEntry tmpEntry : this.exTable) {
			if (excTypes.contains(tmpEntry.getType()) && 
					(PC >= tmpEntry.getStartPC()) && 
					(PC < tmpEntry.getEndPC())) {
				return tmpEntry;
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
		exTable.add(entry);
	}
	
}
