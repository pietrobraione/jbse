package jbse.apps;

import static jbse.apps.Util.LINE_SEP;
import static jbse.apps.Util.PATH_SEP;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import jbse.bc.ClassHierarchy;
import jbse.common.Type;
import jbse.mem.Array;
import jbse.mem.Clause;
import jbse.mem.ClauseAssume;
import jbse.mem.ClauseAssumeReferenceSymbolic;
import jbse.mem.Frame;
import jbse.mem.Instance;
import jbse.mem.Klass;
import jbse.mem.Objekt;
import jbse.mem.State;
import jbse.mem.Variable;
import jbse.mem.exc.ThreadStackEmptyException;
import jbse.val.Expression;
import jbse.val.FunctionApplication;
import jbse.val.MemoryPath;
import jbse.val.NarrowingConversion;
import jbse.val.Primitive;
import jbse.val.PrimitiveSymbolic;
import jbse.val.ReferenceSymbolic;
import jbse.val.Simplex;
import jbse.val.Value;
import jbse.val.WideningConversion;

/**
 * A {@link Formatter} which produces a complex, fully 
 * descriptive rendition of a {@link State}.
 * 
 * @author Pietro Braione
 */
public class StateFormatterText implements Formatter {
	protected List<String> srcPath;
	protected String output = "";
	
	public StateFormatterText(List<String> srcPath) {
		this.srcPath = Collections.unmodifiableList(srcPath);
	}
	
	@Override
	public void formatState(State s) {
		this.output = formatState(s, this.srcPath, true, "\t", "");
	}
	
	@Override
	public String emit() {
	    return this.output;
	}

	@Override
	public void cleanup() {
		this.output = "";
	}
	
	private static String formatState(State state, List<String> srcPath, boolean breakLines, String indentTxt, String indentCurrent) {
		final String lineSep = (breakLines ? LINE_SEP : "");
		String s = state.getIdentifier() + "[" + state.getSequenceNumber() + "] " + lineSep;
        if (state.isStuck()) {
        	s += "Leaf state";
        	if (state.getStuckException() != null) {
        		s += ", raised exception: " + state.getStuckException().toString() + lineSep;
        	} else if (state.getStuckReturn() != null) {
        		s += ", returned value: " + state.getStuckReturn().toString() + lineSep;
        	} else {
        		s += lineSep;
        	}
        } else {
        	try {
        		s += "Method signature: " +  state.getCurrentMethodSignature() + lineSep;
        		s += "Program counter: " + state.getPC() + lineSep;
        		final BytecodeFormatter bfmt = new BytecodeFormatter();
        		s += "Next bytecode: " + bfmt.format(state) + lineSep; 
        		s += "Source line: " + sourceLine(state.getCurrentFrame(), srcPath) + lineSep;
			} catch (ThreadStackEmptyException e) {
				//the state is not stuck but it has no frames:
				//this case is not common but it can mean a state
				//not completely ready to run
			}
        }
        s += "Path condition: " + formatPathCondition(state, breakLines, indentTxt, indentCurrent + indentTxt) + lineSep;
        s += "Static store: {" + lineSep + formatStaticMethodArea(state, breakLines, indentTxt, indentCurrent + indentTxt) + lineSep + "}" + lineSep;
        s += "Heap: {" + lineSep + formatHeap(state, breakLines, indentTxt, indentCurrent + indentTxt) + lineSep + "}" + lineSep;
        if (state.getStackSize() > 0) {
        	s += "Stack: {" + lineSep + formatStack(state, srcPath, breakLines, indentTxt, indentCurrent + indentTxt) + lineSep + "}";
        }
        s += lineSep;
        return s;
	}
	private static String formatPathCondition(State s, boolean breakLines, String indentTxt, String indentCurrent) {
		final String lineSep = (breakLines ? LINE_SEP : "");
		String expression = "";
		String where = "";
    	boolean doneFirstExpression = false;
    	boolean doneFirstWhere = false;
		HashSet<String> doneSymbols = new HashSet<String>();
		for (Clause c : s.getPathCondition()) {
   			expression += (doneFirstExpression ? (" &&" + lineSep) : "") + indentCurrent;
   			doneFirstExpression = true;
    		if (c instanceof ClauseAssume) {
    			final Primitive cond = ((ClauseAssume) c).getCondition();
    			expression += formatValue(s, cond);
    			final String expressionFormatted = formatPrimitiveForPathCondition(cond, breakLines, indentTxt, indentCurrent, doneSymbols);
        		if (expressionFormatted.equals("")) {
        			//does nothing
        		} else {
        			where += (doneFirstWhere ? (" &&" + lineSep) : "") + indentCurrent + expressionFormatted;
        			doneFirstWhere = true;
        		}
    		} else if (c instanceof ClauseAssumeReferenceSymbolic) {
    			final ReferenceSymbolic ref = ((ClauseAssumeReferenceSymbolic) c).getReference(); 
    			expression += ref.toString() + " == ";
    			if (s.isNull(ref)) {
    				expression += "null";
    			} else {
    				final MemoryPath tgtOrigin = s.getObject(ref).getOrigin();
    				expression += "Object[" + s.getResolution(ref) + "] (" + (ref.getOrigin().equals(tgtOrigin) ? "fresh" : ("aliases " + tgtOrigin)) + ")";
    			}
    			final String referenceFormatted = formatReferenceForPathCondition(ref, doneSymbols); 
        		if (referenceFormatted.equals("")) {
        			//does nothing
        		} else {
        			where += (doneFirstWhere ? (" &&" + lineSep ) : "") + indentCurrent + referenceFormatted;
        			doneFirstWhere = true;
        		}
    		} else { //(c instanceof ClauseAssumeClassInitialized) || (c instanceof ClauseAssumeClassNotInitialized)
    			expression += c.toString();
    		}
		
		}
		return (expression.equals("") ? "" : (lineSep + expression)) + (where.equals("") ? "" : (lineSep + indentCurrent + "where:" + lineSep  + where));
	}
	
	private static String formatReferenceForPathCondition(ReferenceSymbolic r, HashSet<String> done) {
		if (done.contains(r.toString())) {
			return "";
		} else {
			return r.toString() + " == " + r.getOrigin();
		}
	}
	
	private static String formatExpressionForPathCondition(Expression e, boolean breakLines, String indentTxt, String indentCurrent, HashSet<String> done) {
		final Primitive firstOp = e.getFirstOperand();
		final Primitive secondOp = e.getSecondOperand();
		String retVal = "";
        if (firstOp != null) {
        	retVal = formatPrimitiveForPathCondition(firstOp, breakLines, indentTxt, indentCurrent, done);
        }
        final String secondOpFormatted = formatPrimitiveForPathCondition(secondOp, breakLines, indentTxt, indentCurrent, done);
        if (retVal.equals("") || secondOpFormatted.equals("")) {
    		//does nothing
        } else {
    		final String lineSep = (breakLines ? LINE_SEP : "");
       		retVal += " &&" + lineSep + indentCurrent;
        }
        retVal += secondOpFormatted;
        return retVal;
	}
	
	private static String formatPrimitiveForPathCondition(Primitive p, boolean breakLines, String indentTxt, String indentCurrent, HashSet<String> done) {
		if (p instanceof Expression) {
			return formatExpressionForPathCondition((Expression) p, breakLines, indentTxt, indentCurrent, done);
		} else if (p instanceof PrimitiveSymbolic) {
			if (done.contains(p.toString())) {
				return "";
			} else {
				done.add(p.toString());
				return p.toString() + " == " + ((PrimitiveSymbolic) p).getOrigin();
			}
		} else if (p instanceof FunctionApplication) {
			return formatFunctionApplicationForPathCondition((FunctionApplication) p, breakLines, indentTxt, indentCurrent, done);
		} else if (p instanceof WideningConversion) {
			final WideningConversion pWiden = (WideningConversion) p;
			return formatPrimitiveForPathCondition(pWiden.getArg(), breakLines, indentTxt, indentCurrent, done);
		} else if (p instanceof NarrowingConversion) {
			final NarrowingConversion pNarrow = (NarrowingConversion) p;
			return formatPrimitiveForPathCondition(pNarrow.getArg(), breakLines, indentTxt, indentCurrent, done);
		} else { //(p instanceof Any || p instanceof Simplex)
			return "";
		}
	}

	private static String formatFunctionApplicationForPathCondition(FunctionApplication a, boolean breakLines, String indentTxt, String indentCurrent, HashSet<String> done) {
		String retVal = "";
		boolean first = true;
		for (Primitive p : a.getArgs()) {
			String argFormatted = formatPrimitiveForPathCondition(p, breakLines, indentTxt, indentCurrent, done);
			if (argFormatted.equals("")) {
				//does nothing
			} else { 
	    		final String lineSep = (breakLines ? LINE_SEP : "");
				retVal += (first ? "" : " &&" + lineSep + indentCurrent) + argFormatted;
				first = false;
			}
		}
		return retVal;
	}

	private static String formatHeap(State s, boolean breakLines, String indentTxt, String indentCurrent) {
		final String lineSep = (breakLines ? LINE_SEP : "");
		final Map<Long, Objekt> h = s.getHeap();
		final int heapSize = h.size();
        String tmpRet = indentCurrent;
        int j = 0;
        for (Map.Entry<Long, Objekt> e : h.entrySet()) {
        	Objekt o = e.getValue();
            tmpRet += "Object[" + e.getKey() + "]: " + "{" + lineSep;
            tmpRet += formatObject(s, o, breakLines, indentTxt, indentCurrent + indentTxt) + lineSep;
            tmpRet += indentCurrent + "}";
            if (j < heapSize - 1) {
            	tmpRet += lineSep + indentCurrent;
            }
            j++;
        }
        return(tmpRet);
	}

	private static String formatStaticMethodArea(State state, boolean breakLines, String indentTxt, String indentCurrent) {
		final String lineSep = (breakLines ? LINE_SEP : "");
		final Map<String, Klass> a = state.getStaticMethodArea();
        String retVal = indentCurrent;
        boolean doneFirst = false;
        for (Map.Entry<String, Klass> ee : a.entrySet()) {
            final Klass k = ee.getValue();
            if (k.getFieldSignatures().size() > 0) {
                if (doneFirst) {
                    retVal += lineSep + indentCurrent;
                }
                doneFirst = true;
                final String c = ee.getKey();
                retVal += "Class[" + c + "]: " + "{" + lineSep;
                retVal += formatObject(state, k, breakLines, indentTxt, indentCurrent + indentTxt) + lineSep;
                retVal += indentCurrent + "}";
        	}
        }
        return retVal;
	}

	private static String formatObject(State s, Objekt o, boolean breakLines, String indentTxt, String indentCurrent) {
		final String lineSep = (breakLines ? LINE_SEP : "");
		String str = "";
		if (o.getOrigin() != null) {
			str = indentCurrent + "Origin: " + o.getOrigin() + lineSep;
		}
		//explicit dispatch on type
		if (o instanceof Array) {
			final Array a = (Array) o;
			str += formatArray(s, a, breakLines, indentTxt, indentCurrent);
		} else if (o instanceof Instance) {
			final Instance i = (Instance) o;
			str += formatInstance(s, i, breakLines, indentTxt, indentCurrent);
		} else if (o instanceof Klass) {
            final Klass k = (Klass) o;
            str += formatKlass(s, k, breakLines, indentTxt, indentCurrent);
		}
		return str;
	}
	
	private static String formatArray(State s, Array a, boolean breakLines, String indentTxt, String indentCurrent) {
		final String lineSep = (breakLines ? LINE_SEP : "");
		String str = "";
		str += indentCurrent + "Type: " + a.getType();
		str += lineSep + indentCurrent + "Length: " + a.getLength(); 
		str += lineSep + indentCurrent + "Items: {";
		final String indentOld = indentCurrent;
		if (!a.hasSimpleRep()) {
			indentCurrent += indentTxt;
		}
		//if it is an array of chars, then it prints it in a string style
		boolean printAsString = (Type.getArrayMemberType(a.getType()).equals("" + Type.CHAR));
		if (printAsString) {
			for (Array.AccessOutcomeIn e : a.values()) {
				printAsString = printAsString && (e.getValue() instanceof Simplex);
			}
		}
		if (printAsString) {
			str += "\"";
		}
		boolean skipComma = true;
		boolean hasUnknownValues = false;
		final StringBuilder buf = new StringBuilder();
		for (Array.AccessOutcomeIn e : a.values()) {
			if (a.hasSimpleRep()) {
			    buf.append(skipComma ? "" : ", ");
			    buf.append(formatArrayEntry(s, e, false));
				if (!printAsString) {
					skipComma = false;
				}
			} else {
				final String entryFormatted = formatArrayEntry(s, e, true);
				if (entryFormatted == null) {
					hasUnknownValues = true;
				} else {
				    buf.append(lineSep);
				    buf.append(indentCurrent);
				    buf.append(entryFormatted);
				}
			}
		}
		str += buf.toString();
		if (printAsString) {
			str += "\"";
		}
		if (!a.hasSimpleRep()) {
			if (hasUnknownValues) {
				str += lineSep + indentCurrent + "(no assumption on other values)";
			}
			str += lineSep;
			indentCurrent = indentOld;
			str += indentCurrent;
		}
		str += "}";
		return str;
	}
	
	private static String formatArrayEntry(State s, Array.AccessOutcomeIn e, boolean showExpression) {
		if (e.getValue() == null) {
			return null;
		}
		final String exp;
		if (e.getAccessCondition() == null) {
			exp = "true";
		} else {
			exp = formatValue(s, e.getAccessCondition());
		}
		final String val = formatValue(s, e.getValue());
		return (showExpression ? (exp + " -> ") : "") + val;	
	}
	
	private static String formatInstance(State s, Instance i, boolean breakLines, String indentTxt, String indentCurrent) {
		final StringBuilder buf = new StringBuilder();
		buf.append(indentCurrent);
		buf.append("Class: ");
		buf.append(i.getType());
        int z = 0;
		final String lineSep = (breakLines ? LINE_SEP : "");
        for (Map.Entry<String, Variable> e : i.fields().entrySet()) {
            buf.append(lineSep);
            buf.append(indentCurrent);
            buf.append("Field[");
            buf.append(z);
            buf.append("]: ");
            buf.append(formatVariable(s, e.getValue()));
            ++z;
        }
        return buf.toString();
	}
    
    private static String formatKlass(State s, Klass k, boolean breakLines, String indentTxt, String indentCurrent) {
        final StringBuilder buf = new StringBuilder();
        int z = 0;
        final String lineSep = (breakLines ? LINE_SEP : "");
        for (Map.Entry<String, Variable> e : k.fields().entrySet()) {
            if (z > 0) {
                buf.append(lineSep);
            }
            buf.append(indentCurrent);
            buf.append("Field[");
            buf.append(z);
            buf.append("]: ");
            buf.append(formatVariable(s, e.getValue()));
            ++z;
        }
        return buf.toString();
    }
	
	
	private static String formatVariable(State s, Variable v) {
        String tmp;
        Value val = v.getValue(); 
        if (val == null) {
            tmp = "ERROR: no value has been assigned to this variable.";
        } else {
            tmp = formatValue(s, val) + " " + formatType(val);
        }
        return ("Name: " + v.getName() + ", Type: " + v.getType() + ", Value: " + tmp);
	}
	
	private static String formatValue(State s, Value val) {
        String tmp = val.toString();
        if (val instanceof ReferenceSymbolic) {
        	ReferenceSymbolic ref = (ReferenceSymbolic) val;
        	if (s.resolved(ref)) {
        		if (s.isNull(ref)) {
        			tmp += " == null";
        		} else {
        			tmp += " == Object[" + s.getResolution(ref) + "]";
        		}
        	}
        }
        return tmp;
	}
	
	private static String formatType(Value val) {
    	return  "(type: " + val.getType() + ")";		
	}
	
	private static String formatStack(State s, List<String> srcPath, boolean breakLines, String indentTxt, String indentCurrent) {
		final String lineSep = (breakLines ? LINE_SEP : "");
    	final Iterable<Frame> stack = s.getStack();
    	final int size = s.getStackSize();
        String tmpRet = indentCurrent;
        int j = 0;
        for (Frame f : stack) {
            tmpRet += "Frame[" + j + "]: {" + lineSep + formatFrame(s, f, srcPath, breakLines, indentTxt, indentCurrent + indentTxt) + lineSep;
            tmpRet += indentCurrent + "}";
            if (j < size - 1) 
            	tmpRet += lineSep + indentCurrent;
            j++;
        }
        return tmpRet;
	}

	private static String formatFrame(State s, Frame f, List<String> srcPath, boolean breakLines, String indentTxt, String indentCurrent) {
		final String lineSep = (breakLines ? LINE_SEP : "");
		String tmp = "";
        tmp += indentCurrent + "Method signature: " + f.getCurrentMethodSignature().toString() + lineSep;
        tmp += indentCurrent + "Program counter: " + f.getProgramCounter() + lineSep;
        tmp += indentCurrent + "Program counter after return: "; 
        tmp += ((f.getReturnProgramCounter() == Frame.UNKNOWN_PC) ? "<UNKNOWN>" : f.getReturnProgramCounter()) + lineSep;
		final ClassHierarchy hier = s.getClassHierarchy();
    	final BytecodeFormatter bfmt = new BytecodeFormatter();
        tmp += indentCurrent + "Next bytecode: " + bfmt.format(f, hier) + lineSep; 
        tmp += indentCurrent + "Source line: " + sourceLine(f, srcPath) + lineSep;
        tmp += indentCurrent + "Operand Stack: {"+ lineSep + formatOperandStack(s, f, breakLines, indentTxt, indentCurrent + indentTxt) + lineSep + indentCurrent + "}" + lineSep;
        tmp += indentCurrent + "Local Variables: {" + lineSep + formatLocalVariables(s, f, breakLines, indentTxt, indentCurrent + indentTxt) + lineSep + indentCurrent + "}";
        return tmp;
	}
	
	private static String formatOperandStack(State s, Frame f, boolean breakLines, String indentTxt, String indentCurrent) {
        final StringBuilder buf = new StringBuilder();
        buf.append(indentCurrent);
        final String lineSep = (breakLines ? LINE_SEP : "");
        int i = 0;
        final int last = f.values().size() - 1;
        for (Value v : f.values()) {
            buf.append("Operand[");
            buf.append(i);
            buf.append("]: ");
            buf.append(formatValue(s, v));
            buf.append(" ");
            buf.append(formatType(v));
            if (i < last)  {
                buf.append(lineSep);
                buf.append(indentCurrent);
            }
            ++i;
        }
        return buf.toString();
	}
	
	private static String formatLocalVariables(State s, Frame f, boolean breakLines, String indentTxt, String indentCurrent) {
        final StringBuilder buf = new StringBuilder();
        buf.append(indentCurrent);
        boolean isFirst = true;
        final Map<Integer, Variable> lva = f.localVariables();
        final TreeSet<Integer> slots = new TreeSet<>(lva.keySet());
        final String lineSep = (breakLines ? LINE_SEP : "");
       	for (int i : slots) {
            if (isFirst) {
                isFirst = false;
            } else {
                buf.append(lineSep);
                buf.append(indentCurrent);
            }
            buf.append("Variable[");
            buf.append(i);
            buf.append("]: ");
            buf.append(formatVariable(s, lva.get(i)));
        }
        return buf.toString();
	}
	
	private static String sourceLine(Frame f, List<String> srcPath) {
    	String retVal = "";
    	int sourceRow = f.getSourceRow();
    	if (sourceRow == -1) { 
    		retVal += "<UNKNOWN>";
    	} else { 
    		retVal += "(" + sourceRow + "): ";
    		String row = null;
    		if (srcPath != null) {
    			row = Util.getSrcFileRow(f.getCurrentMethodSignature().getClassName(), srcPath, PATH_SEP, sourceRow);
    		}
    		retVal += (row == null ? "<UNKNOWN>" : row);
    	}
    	return retVal;
	}
}
