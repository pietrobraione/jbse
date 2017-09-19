package jbse.apps;

import java.io.IOException;
import java.io.PrintStream;

public class IO {
	/**
	 * Reads a line from {@link System}<code>.in</code>, displaying the 
	 * interaction on a number of {@link PrintStream}s.
	 *  
	 * @param ps a {@link PrintStream}<code>[]</code>; the state 
	 *           will be displayed on all the {@link PrintStream}s 
	 *           in <code>ps</code>.
	 * @param prompt a {@link String}</code> which will be displayed 
	 *               as input prompt.
	 * @return a {@link String} representing the input line, 
	 *         without the trailing end line, or <code>null</code> 
	 *         if some {@link IOException} arises during 
	 *         input scanning.
	 */
	public static String readln(PrintStream[] ps, String prompt) {
		if (ps != null && prompt != null) {
			IO.print(ps, prompt);
		}
		String retVal = IO.readln();
		if (ps != null) {
			for (PrintStream p : ps) {
				/* TODO this avoids duplication of input on System.out, but is not much robust. 
				 * Find a way to turn off echo on System.out when scanning input and remove the guard.
				 */
				if (p != null && p != System.out) { 
					p.println(retVal);
				}
			}
			if (retVal == null) { 
				retVal = "";
			}
		}

		return retVal;
	}

	/**
	 * Reads a line from {@link System}<code>.in</code>; as a side effect, 
	 * the interaction is displayed on {@link System}<code>.out</code>.
	 * 
	 * @return a {@link String}, the read line.
	 */
	private static String readln() { 
		String result = "";
		boolean done = false;

		while (!done) {
			int charAsInt = -1; //To keep the compiler happy
			try {
				charAsInt = System.in.read(); 
			} catch(IOException e) {
				result = null;
				break;
			} 

			char nextChar = (char) charAsInt;
			if (nextChar == '\n') {
				done = true;
			} else if (nextChar == '\r') {
				; //discard
			} else { 
				result += nextChar;
			}
		}

		return result;
	}

	/**
	 * Prints a end-of-line terminated text to a number of 
	 * {@link PrintStream}s.
	 * @param ps a {@link PrintStream}<code>[]</code>; the line of 
	 *           text will be printed on all the streams in it 
	 *           (behaves correctly even if <code>ps</code> or 
	 *           one of its member is <code>null</code>).
	 * @param txt a {@link String}, the text to be printed (the
	 *        method will print <code>txt</code> plus the end-of-line
	 *        character).
	 */
	public static void println(PrintStream[] ps, String txt) {
		if (ps != null) {
			for (PrintStream p : ps) {
				if (p != null) {
					p.println(txt);
				}
			}
		}
	}

	/**
	 * Prints the end-of-line character to a number of 
	 * {@link PrintStream}s.
	 * @param ps a {@link PrintStream}<code>[]</code>; the line of 
	 *           text will be printed on all the streams in it 
	 *           (behaves correctly even if <code>ps</code> or 
	 *           one of its member is <code>null</code>).
	 */
	public static void println(PrintStream[] ps) {
		if (ps != null) {
			for (PrintStream p : ps) {
				if (p != null) {
					p.println();
				}
			}
		}
	}

	/**
	 * Prints a {@link Throwable}'s stack trace to a number of 
	 * {@link PrintStream}s.
	 * @param ps a {@link PrintStream}<code>[]</code>; the line of 
	 *           text will be printed on all the streams in it 
	 *           (behaves correctly even if <code>ps</code> or 
	 *           one of its member is <code>null</code>).
	 * @param e a {@link Throwable} whose stack trace will be printed.
	 */
	public static void printException(PrintStream[] ps, Throwable e) {
		if (ps != null) {
			for (PrintStream p : ps) {
				if (p != null) {
					e.printStackTrace(p);
				}
			}
		}
	}

	/**
	 * Prints a {@link String} to a number of {@link PrintStream}s.
	 * @param ps a {@link PrintStream}<code>[]</code>; the line of 
	 *           text will be printed on all the streams in it 
	 *           (behaves correctly even if <code>ps</code> or 
	 *           one of its member is <code>null</code>).
	 * @param txt a {@link String}, the text to be printed.
	 */
	public static void print(PrintStream[] ps, String txt) {
		if (ps != null) {
			for (PrintStream p : ps) {
				if (p != null) {
					p.print(txt);
				}
			}
		}
	}
}
