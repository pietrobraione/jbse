package jbse.apps;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import jbse.common.exc.UnexpectedInternalException;
import jbse.mem.Clause;
import jbse.mem.ClauseAssume;
import jbse.mem.ClauseAssumeAliases;
import jbse.mem.ClauseAssumeExpands;
import jbse.mem.ClauseAssumeNull;
import jbse.val.Any;
import jbse.val.Expression;
import jbse.val.FunctionApplication;
import jbse.val.NarrowingConversion;
import jbse.val.Primitive;
import jbse.val.PrimitiveSymbolic;
import jbse.val.PrimitiveVisitor;
import jbse.val.ReferenceSymbolic;
import jbse.val.Simplex;
import jbse.val.Term;
import jbse.val.WideningConversion;

public class Util {
	/** Constant for line separator. */
	public static final String LINE_SEP = System.getProperty("line.separator");
	
	/** Constant for path separator. */
	public static final String PATH_SEP = System.getProperty("file.separator");
	
	/** Constant for path list separator. */
	public static final String MULTI_PATH_SEP = ";";

	/** Constant for signature separator. */
	public static final String SIGNATURE_SEP = ":";

	/** Milliseconds in one second */
	private static final long ONE_SEC = 1000;

	/** Milliseconds in one minute */
	private static final long ONE_MIN = ONE_SEC * 60;

	/** Milliseconds in one hour */
	private static final long ONE_HOUR = ONE_MIN * 60;

	/**
	 * Formats a {@code long} value denoting a time interval into a more
	 * readable {@link String}.
	 * 
	 * @param elapsedTime
	 *            a {@code long} denoting a time interval in milliseconds.
	 * @return a {@link String} describing the interval in terms of hours,
	 *         minutes, seconds and milliseconds.
	 */
	public static String formatTime(long elapsedTime) {
		final long millisToPrint = ((elapsedTime % ONE_HOUR) % ONE_MIN) % ONE_SEC;
		final long secondsToPrint = ((elapsedTime % ONE_HOUR) % ONE_MIN) / ONE_SEC;
		final long minutesToPrint = (elapsedTime % ONE_HOUR) / ONE_MIN;
		final long hoursToPrint = elapsedTime / ONE_HOUR;
		
		String result = null;
		if (hoursToPrint > 0) {
			result = hoursToPrint + " hr";
		}
		if (minutesToPrint > 0) {
			result = (result == null ? "" : (result + " ")) + minutesToPrint + " min";
		}
		if (secondsToPrint > 0) {
			result = (result == null ? "" : (result + " ")) + secondsToPrint + " sec";
		}
		result = (result == null ? "" : (result + " ")) + millisToPrint + " msec";
		return result;
	}
	
	public static String formatTimePercent(long part, long tot) {
		final double timeRatio = (((double) part) / tot);
		final String timePercent = new DecimalFormat("##.##%").format(timeRatio);
		return timePercent;
	}
	
	private static final String SOURCE_FILE_EXTENSION = ".java";
	
	/**
	 * Returns a row in a source file.
	 * 
	 * @param className a {@link String}, the signature of a class.
	 * @param srcPath a {@link String}<code>[]</code>, the paths on the file system 
	 *        where the source files are found.
	 * @param pathSep a {@link String}, the separator used to build the path.
	 * @param row an {@code int}, the number of the row in the source.
	 * @return a {@link String}, the source code row, or {@code null} if the
	 *         source file is not found or some I/O error occurs.
	 */
	public static String getSrcFileRow(String className, List<String> srcPath, String pathSep, int row) {
		if (row == -1 || srcPath == null) {
			return null;
		}
		final String sourceFileNameRelative = className.split("\\$")[0] + SOURCE_FILE_EXTENSION;
		for (String s: srcPath) {
			final File f = new File(s);
			BufferedReader fr = null;
			try {
	            ZipFile zf = null;
				if (f.isDirectory()) {
					final String sourceFilePathName = (s.endsWith(pathSep) ? s : s.concat(pathSep)) + sourceFileNameRelative;
					fr = new BufferedReader(new FileReader(sourceFilePathName));
				} else if (f.isFile()) {
					//let's try if it is a zipped file
				    zf = new ZipFile(f);
				    final ZipEntry e = zf.getEntry(sourceFileNameRelative);
				    if (e == null) {
				        zf.close();
				        continue; //no file
				    }
				    final InputStream zipInput = zf.getInputStream(e);
				    fr = new BufferedReader(new InputStreamReader(zipInput));
				} else {
					continue; //does not exist
				}
				String l;
				for (int i = 1; ((l = fr.readLine()) != null) ; ++i) {
					if (i == row) {
						fr.close();
						return l;
					}
				}
			} catch (IOException e) {
				//does nothing
			} finally {
				try {
					if (fr != null) { 
						fr.close();
					}
				} catch (IOException e) {
					//does nothing
				}
			}
		}
		return null;
	}
		
	public static String formatClauses(Iterable<Clause> assumptions) {
		final StringBuilder buf = new StringBuilder();
        boolean firstDone = false;
        for (Clause c : assumptions) {
        	if (firstDone) {
        		buf.append(" && ");
        	} else {
        		firstDone = true;
        	}
        	buf.append(formatClause(c));
        }
        if (!firstDone) {
        	buf.append("true");        	
        }
        return buf.toString();
	}
	
	public static String formatClause(Clause c) {
    	if (c instanceof ClauseAssume) {
    		return formatClauseAssume((ClauseAssume) c);
    	} else if (c instanceof ClauseAssumeAliases) {
    		return formatClauseAssumeAliases((ClauseAssumeAliases) c);
    	} else if (c instanceof ClauseAssumeExpands) {
    		return formatClauseAssumeExpands((ClauseAssumeExpands) c);
    	} else if (c instanceof ClauseAssumeNull) {
    		return formatClauseAssumeNull((ClauseAssumeNull) c);
    	} else { //ClauseAssumeClassInitialized || ClauseAssumeClassNotInitialized
    		return c.toString();
    	}
	}
	
	public static String formatClauseAssume(ClauseAssume ca) {
		return formatPrimitive(ca.getCondition());
	}
	
	public static String formatClauseAssumeAliases(ClauseAssumeAliases ca) {
		final ReferenceSymbolic r = ca.getReference();
		return (r.getOrigin() + " == " + ca.getObjekt().getOrigin());
	}
	
	public static String formatClauseAssumeExpands(ClauseAssumeExpands ce) {
		final ReferenceSymbolic r = ce.getReference();
		return (r.getOrigin() + " == fresh " + ce.getObjekt().getType());
	}
	
	public static String formatClauseAssumeNull(ClauseAssumeNull cn) {
		final ReferenceSymbolic r = cn.getReference();
		return (r.getOrigin() + " == null");
	}
	
	public static String formatPrimitive(Primitive p) {
		final FormatDispatcher dispatcher = new FormatDispatcher();
		try {
			p.accept(dispatcher);
		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			throw new UnexpectedInternalException(e);
		}
		return dispatcher.retVal;
	}
	
	private static final class FormatDispatcher implements PrimitiveVisitor {
		String retVal;
		
		@Override public void visitAny(Any x) { this.retVal = formatAny(x); }
		@Override public void visitExpression(Expression e) { this.retVal = formatExpression(e); }
		@Override public void visitFunctionApplication(FunctionApplication x) { this.retVal = formatFunctionApplication(x); }
		@Override public void visitNarrowingConversion(NarrowingConversion x) { this.retVal = formatNarrowingConversion(x); }
		@Override public void visitPrimitiveSymbolic(PrimitiveSymbolic s) { this.retVal = formatPrimitiveSymbolic(s); }
		@Override public void visitSimplex(Simplex x) { this.retVal = formatSimplex(x); }
		@Override public void visitTerm(Term x) { this.retVal = formatTerm(x); }
		@Override public void visitWideningConversion(WideningConversion x) { this.retVal = formatWideningConversion(x); }
	};
	
	public static String formatAny(Any a) {
		return a.toString();
	}
	
	public static String formatExpression(Expression e) {
		final String secondOp = formatPrimitive(e.getSecondOperand());
		if (e.isUnary()) {
			return e.getOperator().toString() + " " + secondOp;
		} else {
			final String firstOp = formatPrimitive(e.getFirstOperand());
			return firstOp + " " + e.getOperator().toString() + " " + secondOp;
		}
	}
	
	public static String formatFunctionApplication(FunctionApplication f) {
		final StringBuilder buf = new StringBuilder();
		buf.append(f.getOperator() + "(");
		boolean first = true;
		for (Primitive p : f.getArgs()) {
			buf.append((first ? "" : ",") + formatPrimitive(p));
			first = false;
		}
		buf.append(")");
		return buf.toString();
	}
	
	public static String formatNarrowingConversion(NarrowingConversion c) {
		return "NARROW-"+ c.getType() + "(" + formatPrimitive(c.getArg()) + ")";
	}
	
	public static String formatPrimitiveSymbolic(PrimitiveSymbolic p) {
		return p.getOrigin().toString();
	}
	
	public static String formatSimplex(Simplex s) {
		return s.toString();
	}
	
	public static String formatTerm(Term t) {
		return t.toString();
	}
	
	public static String formatWideningConversion(WideningConversion c) {
		return "WIDEN-"+ c.getType() + "(" + formatPrimitive(c.getArg()) + ")";
	}
}
