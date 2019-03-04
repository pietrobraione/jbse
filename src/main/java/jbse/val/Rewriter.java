package jbse.val;

import java.util.List;

import jbse.common.exc.InvalidInputException;
import jbse.common.exc.UnexpectedInternalException;
import jbse.val.exc.InvalidOperandException;
import jbse.val.exc.InvalidOperatorException;
import jbse.val.exc.InvalidTypeException;
import jbse.val.exc.NoResultException;

/**
 * An abstract term rewriter for {@link Primitive}s.
 * 
 * @author Pietro Braione
 */
public class Rewriter implements Cloneable {
	//TODO this implementation is not reentrant, i.e., a rewriter cannot recursively invoke itself. Because of this, the CalculatorRewriting.simplify method cannot invoke itself recursively. Fix this situation.
	
	private Primitive value;
	private RewriteVisitor visitor;
	
	public Rewriter() {
		clear();
		makeDispatcher();
	}
	
	private void makeDispatcher() {
		this.visitor = new RewriteVisitor();
	}
	
    public static Primitive applyRewriters(Primitive p, Rewriter...rewriters)
    throws NoResultException {
        Primitive retVal = p;
        for (Rewriter r : rewriters) {
        	retVal = r.rewrite(retVal);
        }
        return retVal;
    }
	
    public static Primitive applyRewriters(Primitive p, List<? extends Rewriter> rewriters)
    throws NoResultException {
        Primitive retVal = p;
        for (Rewriter r : rewriters) {
        	retVal = r.rewrite(retVal);
        }
        return retVal;
    }
    
	protected final Primitive rewrite(Primitive p) throws NoResultException {
		if (p == null) {
			throw new NoResultException();
		} else {
			clear();
			try {
				p.accept(this.visitor);
			} catch (NoResultException | RuntimeException e) {
				throw e;
			} catch (Exception e) {
				//this should never happen
				throw new UnexpectedInternalException(e);
			}
			return this.value;
		}
	}
	
	protected final void setResult(Primitive x) throws NoResultException {
		if (x == null) {
			throw new NoResultException();
		} else {
			this.value = x;
		}
	}
		
	protected final Primitive getResult() throws NoResultException {
		if (this.value == null) {
			throw new NoResultException();
		} else {
			return this.value;
		}
	}
		
	protected void rewriteAny(Any x) throws NoResultException {
		setResult(x);
	}
	
	protected void rewriteTerm(Term x) throws NoResultException {
		setResult(x);
	}
	
	protected void rewriteSimplex(Simplex x) throws NoResultException {
		setResult(x);
	}
	
	protected void rewritePrimitiveSymbolic(PrimitiveSymbolic x) 
	throws NoResultException {
		setResult(x);
	}
	
	protected void rewritePrimitiveSymbolicApply(PrimitiveSymbolicApply x) 
	throws NoResultException {
		final Value[] args = x.getArgs();
		for (int i = 0; i < args.length; i++) {
		    if (args[i] instanceof Primitive) {
			args[i] = rewrite((Primitive) args[i]);
		    }
		}
		final PrimitiveSymbolicApply result;
		try {
			result = new PrimitiveSymbolicApply(x.getType(), x.historyPoint(), x.getOperator(), args);
		} catch (InvalidTypeException | InvalidInputException e) {
			throw new NoResultException(e);
		}
		setResult(result);
	}

	protected void rewriteExpression(Expression x) 
	throws NoResultException {
		final Operator operator = x.getOperator();
		final Expression result;
		try {
			if (x.isUnary()) {
				final Primitive operand = rewrite(x.getOperand());
				result = Expression.makeExpressionUnary(operator, operand);
			} else {
				final Primitive firstOperand = rewrite(x.getFirstOperand());
				final Primitive secondOperand = rewrite(x.getSecondOperand());
				result = Expression.makeExpressionBinary(firstOperand, operator, secondOperand);
			}
		} catch (InvalidTypeException | InvalidOperandException e) {
			//rewriting of operands yielded bad results: fails
			throw new NoResultException(e);
		} catch (InvalidOperatorException e) {
			//this should never happen
			throw new UnexpectedInternalException(e);
		}
		setResult(result);
	}
	
	protected void rewriteWideningConversion(WideningConversion x) 
	throws NoResultException {
		final Primitive arg = rewrite(x.getArg());
		final WideningConversion result;
		try {
			result = WideningConversion.make(x.getType(), arg);
		} catch (InvalidOperandException | InvalidTypeException e) {
			//this should never happen
			throw new UnexpectedInternalException(e);
		}
		setResult(result);
	}

	protected void rewriteNarrowingConversion(NarrowingConversion x) 
	throws NoResultException {
		final Primitive arg = rewrite(x.getArg());
		final NarrowingConversion result;
		try {
			result = NarrowingConversion.make(x.getType(), arg);
		} catch (InvalidOperandException | InvalidTypeException e) {
			//this should never happen
			throw new UnexpectedInternalException(e);
		}
		setResult(result);
	}

	private void clear() {
		this.value = null;
	}
	
	/**
	 * Just redispatches.
	 * 
	 * @author Pietro Braione
	 *
	 */
	private class RewriteVisitor implements PrimitiveVisitor {
		@Override public void visitAny(Any x) throws NoResultException { Rewriter.this.rewriteAny(x); }

		@Override public void visitExpression(Expression e) throws NoResultException { Rewriter.this.rewriteExpression(e); }

		@Override public void visitPrimitiveSymbolicApply(PrimitiveSymbolicApply x) throws NoResultException { Rewriter.this.rewritePrimitiveSymbolicApply(x); }

		@Override public void visitPrimitiveSymbolicAtomic(PrimitiveSymbolicAtomic s) throws NoResultException { Rewriter.this.rewritePrimitiveSymbolic(s); }

		@Override public void visitSimplex(Simplex x) throws NoResultException { Rewriter.this.rewriteSimplex(x); }

		@Override public void visitTerm(Term x) throws NoResultException { Rewriter.this.rewriteTerm(x); }

		@Override public void visitWideningConversion(WideningConversion x) throws NoResultException { Rewriter.this.rewriteWideningConversion(x); }

		@Override public void visitNarrowingConversion(NarrowingConversion x) throws NoResultException { Rewriter.this.rewriteNarrowingConversion(x); }
	}
	
	
	@Override
	public Rewriter clone() {
		try {
			final Rewriter other = (Rewriter) super.clone();
			other.makeDispatcher(); //otherwise, it would redispatch to this object!
			return other;
		} catch (CloneNotSupportedException e) {
			//this should never happen
			throw new UnexpectedInternalException(e);
		}
	}
}
