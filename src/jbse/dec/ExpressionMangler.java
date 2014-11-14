package jbse.dec;

import java.util.ArrayList;
import java.util.HashMap;

import jbse.common.exc.UnexpectedInternalException;
import jbse.rewr.CalculatorRewriting;
import jbse.val.Expression;
import jbse.val.Primitive;
import jbse.val.Term;
import jbse.val.exc.InvalidTypeException;

/**
 * A class for renaming {@link Expression}s into 
 * {@link Term}s. It bookkeeps every mangled 
 * {@link Expression}, so if the same {@link Expression}
 * is mangled twice it generates the same {@link Term}
 * (up to equality).
 * 
 * @author Pietro Braione
 */
public class ExpressionMangler {
	private ArrayList<Primitive> demangling = new ArrayList<Primitive>();
	private HashMap<Primitive, Term> mangling = new HashMap<Primitive, Term>();
	private int symId = 0;
	private String pre, post;
	private CalculatorRewriting calc;
	
	public ExpressionMangler(String pre, String post, CalculatorRewriting calc) {
		this.pre = pre; this.post = post; this.calc = calc;
	}
	
	/**
	 * Sets the format of the mangled term, to
	 * enhance compatibility with external 
	 * decision procedures. The mangled 
	 * identifier for a term will be 
	 * {@code pre} + an_int + {@code post}.
	 *  
	 * @param pre a {@link String}.
	 * @param post a {@link String}
	 */
	public void setFormat(String pre, String post) {
		this.pre = pre; this.post = post;
	} 

	/**
	 * Mangles a {@link Primitive}.
	 * 
	 * @param p the {@link Primitive} to mangle.
	 * @return a {@link Term}; if another {@link Primitive}
	 *         {@code q} was mangled before by this mangler, 
	 *         and {@code p.toString().equals(q.toString())}, then
	 *         {@code mangle(p).equals(mangle(q))}. 
	 */
	public Term mangle(Primitive p) {
    	Term retVal = this.mangling.get(p);
    	if (retVal == null) {
    		final int nextId = Integer.valueOf(this.symId);
    		symId++;
    		try {
				retVal = this.calc.valTerm(p.getType(), this.pre + nextId + this.post);
			} catch (InvalidTypeException e) {
				//this should never happen
				throw new UnexpectedInternalException(e);
			}
    		this.mangling.put(p, retVal);
    		this.demangling.add(p);
    	}
    	return retVal;
    }
	
	/**
	 * Demangles a {@link Term}.
	 * 
	 * @param t the {@link Term} to demangle.
	 * @return the {@link Primitive} {@code p} such that 
	 *         a previous call to {@link #mangle(Primitive)} 
	 *         returned {@code t}, or {@code null} in case 
	 *         {@code t} was not produced by a previous call to 
	 *         {@link #mangle(Primitive)}.
	 */
	public Primitive demangle(Term t) {
		final String tString = t.getValue();
		final String idString = tString.substring(this.pre.length(), tString.length() - this.post.length());
		final int id;
		try {
			id = Integer.parseInt(idString);
		} catch (NumberFormatException e) {
			return null; //not demangling a mangled term
		}
		return this.demangling.get(id);
	}
}
