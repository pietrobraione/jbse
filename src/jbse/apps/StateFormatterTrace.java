package jbse.apps;

import static jbse.apps.Util.formatPrimitive;
import jbse.exc.common.UnexpectedInternalException;
import jbse.exc.mem.ThreadStackEmptyException;
import jbse.mem.Primitive;
import jbse.mem.Reference;
import jbse.mem.State;
import jbse.mem.Value;

/**
 * A {@link StateFormatter} which yields a one-line text rendition of 
 * the current {@link State}, including only the current identification
 * of the state in its trace, the current method and the current 
 * statement/bytecode. Useful to print execution traces.
 * 
 * @author Pietro Braione
 *
 */
public abstract class StateFormatterTrace implements StateFormatter {
	/** 
	 * The {@link String} used by {@link StateFormatterTrace#format(State)} to
	 * indicate a stuck {@link State}. 
	 */
	protected String leaf = "LEAF";

	/** The {@link String} used by {@link StateFormatterTrace#format(State)} to separates fields. */
	protected String fieldSep = " ";
	
	/** Here the result of {@link StateFormatterTrace#format(State)}. */
	protected String formatOutput;
	
	private BytecodeFormatter bcf = new BytecodeFormatter();

	public void format(State s) {
		this.formatOutput = s.getIdentifier() + "[" + s.getSequenceNumber() + "]" + fieldSep + s.getDepth()+","+s.getCount() + fieldSep;
        if (s.isStuck()) {
        	this.formatOutput += leaf;
        	if (s.getStuckException() != null) {
        		this.formatOutput += fieldSep + "exception" + fieldSep + formatReturn(s, s.getStuckException());
        	} else if (s.getStuckReturn() != null) {
        		this.formatOutput += fieldSep + "return" + fieldSep + formatReturn(s, s.getStuckReturn());
        	}
        } else {
        	try {
        		this.formatOutput += s.getCurrentMethodSignature() + fieldSep + s.getSourceRow() + fieldSep 
        				+ s.getPC() + fieldSep + bcf.format(s);
			} catch (ThreadStackEmptyException e) {
				//the state is not stuck but it has no frames:
				//this case is not common but it can mean a state
				//not completely ready to run
			}
        }
	}
	
	private String formatReturn(State s, Value v) {
		if (v instanceof Primitive) {
			return formatPrimitive((Primitive) v);
		} else if (v instanceof Reference) {
			return s.getObject((Reference) v).getType();
		} else {
			throw new UnexpectedInternalException("Unexpected value " + v + " returned.");
		}
	}
	
	@Override
	public void cleanup() {
		this.formatOutput = "";
	}
}
