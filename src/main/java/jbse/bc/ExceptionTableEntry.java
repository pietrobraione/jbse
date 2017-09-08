package jbse.bc;

/**
 *Class that represent an entry of the exception table
 */
public class ExceptionTableEntry {
    private int[] exEntry;
    private String exType;

    public ExceptionTableEntry(int startPC, int endPC, int PCHandle, String type)
	{
		exEntry=new int[3];
		exEntry[0]=startPC;
		exEntry[1]=endPC;
		exEntry[2]=PCHandle;
		exType=type;
	}
	/**
	 *Return the Program Counter of the start of try
	 *@return int The Program Counter of the start of try
	 */ 
	public int getStartPC()
	{
		return(exEntry[0]);
	}
	/**
	 *Return the Program Counter of the end of try
	 *@return int The Program Counter of the end of try
	 */ 
	public int getEndPC()
	{
		return(exEntry[1]);
	}
	/**
	 *Return the Program Counter of the start of catch
	 *@return int The Program Counter of the start of catch
	 */ 
	public int getPCHandle()
	{
		return(exEntry[2]);
	}
	/**
	 *Return the type of exception
	 *@return String The type of exception
	 */ 
	public String getType()
	{
		return(exType);
	}
	/**
	 *Set the Program Counter of the start of try
	 *@param startPc The Program Counter of the start of try
	 */ 
	public void setStartPC(int startPC)
	{
		exEntry[0]=startPC;
	}
	/**
	 *Set the Program Counter of the end of try
	 *@param endPC The Program Counter of the end of try
	 */ 
	public void setEndPC(int endPC)
	{
		exEntry[1]=endPC;
	}
	/**
	 *Set the Program Counter of the start of catch
	 *@param pcHandle The Program Counter of the start of catch
	 */ 
	public void setPCHandle(int pcHandle)
	{
		exEntry[2]=pcHandle;
	}
	/**
	 *Set the type of exception
	 *@param String The type of exception
	 */ 
	public void setType(String type)
	{
		exType=type;
	}
}