package jbse.apps;

import static jbse.apps.Util.formatPrimitive;

import jbse.common.exc.UnexpectedInternalException;
import jbse.mem.State;
import jbse.mem.exc.ThreadStackEmptyException;
import jbse.val.Primitive;
import jbse.val.Reference;
import jbse.val.Value;

/**
 * A {@link Formatter} which yields a one-line text rendition of 
 * the current {@link State}, including only the current identification
 * of the state in its trace, the current method and the current 
 * statement/bytecode. Useful to print execution traces.
 * 
 * @author Pietro Braione
 *
 */
public class StateFormatterTrace implements Formatter {
	/** 
	 * The {@link String} used by {@link StateFormatterTrace#formatState(State)} to
	 * indicate a stuck {@link State}. 
	 */
	protected String leaf = "LEAF";

	/** The {@link String} used by {@link StateFormatterTrace#formatState(State)} to separates fields. */
	protected String fieldSep = " ";
	
	/** Here the result of {@link StateFormatterTrace#formatState(State)}. */
	protected String output;
	
	private BytecodeFormatter bcf = new BytecodeFormatter();

	public void formatState(State s) {
		this.output = s.getIdentifier() + "[" + s.getSequenceNumber() + "]" + this.fieldSep 
		                  + s.getDepth() + "," + s.getCount() + this.fieldSep;
        if (s.isStuck()) {
        	this.output += this.leaf;
        	if (s.getStuckException() != null) {
        		this.output += this.fieldSep + "exception" + this.fieldSep + formatReturn(s, s.getStuckException());
        	} else if (s.getStuckReturn() != null) {
        		this.output += this.fieldSep + "return" + this.fieldSep + formatReturn(s, s.getStuckReturn());
        	}
        } else {
        	try {
        		this.output += s.getCurrentMethodSignature() + this.fieldSep + s.getSourceRow() + this.fieldSep 
        				+ s.getPC() + this.fieldSep + bcf.format(s);
			} catch (ThreadStackEmptyException e) {
				//the state is not stuck but it has no frames:
				//this case is not common but it can mean a state
				//not completely ready to run
			}
        }
        this.output += "\n";
	}
	
	private String formatReturn(State s, Value v) {
		if (v instanceof Primitive) {
			return formatPrimitive((Primitive) v);
		} else if (v instanceof Reference) {
		    if (s.isNull((Reference) v)) {
		        return "null";
		    } else {
		        return s.getObject((Reference) v).getType();
		    }
		} else {
			throw new UnexpectedInternalException("Unexpected value " + v + " returned.");
		}
	}
	
	@Override
	public final String emit() {
	    return this.output;
	}
	
	@Override
	public final void cleanup() {
		this.output = "";
	}
}
