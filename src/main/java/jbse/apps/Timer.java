package jbse.apps;

/**
 * A timer (not explicitly started or stopped).
 * 
 * @author Pietro Braione
 *
 */
public interface Timer {
	/**
	 * Returns the measured time.
	 * 
	 * @return time in milliseconds.
	 */
	long getTime();
}
