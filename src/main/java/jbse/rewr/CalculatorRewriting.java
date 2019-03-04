package jbse.rewr;

import static jbse.val.Rewriter.applyRewriters;

import java.util.ArrayList;

import jbse.common.exc.UnexpectedInternalException;
import jbse.val.Calculator;
import jbse.val.Primitive;
import jbse.val.Rewriter;
import jbse.val.exc.NoResultException;

/**
 * A {@link Calculator} that simplifies based on {@link Rewriter}s.
 * 
 * @author Pietro Braione
 */
public class CalculatorRewriting extends Calculator {
    private final ArrayList<RewriterCalculatorRewriting> rewriters = new ArrayList<>();

    /**
     * Constructor.
     */
    public CalculatorRewriting() {
        super();
    }
    
    /**
     * Copy constructor.
     * 
     * @param calc another CalculatorRewriting.
     */
    public CalculatorRewriting(CalculatorRewriting calc) {
    	for (RewriterCalculatorRewriting rewriter : calc.rewriters) {
    		final RewriterCalculatorRewriting rewriterNew = rewriter.clone();
    		rewriterNew.calc = this;
    		this.rewriters.add(rewriterNew);
    	}
    }
    
    /**
     * Adds a rewriter.
     * 
     * @param rewriter the {@link Rewriter} to add.
     */
    public void addRewriter(RewriterCalculatorRewriting rewriter) {
    	rewriter.calc = this;
        this.rewriters.add(rewriter);
    }

    @Override
    public Primitive simplify(Primitive p) {
    	try {
    		final Primitive retVal = applyRewriters(p, this.rewriters);
    		return retVal;
    	} catch (NoResultException e) {
    		//this should not happen
    		throw new UnexpectedInternalException(e);
    	}
    }
}
