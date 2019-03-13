package jbse.rewr;

import jbse.val.PrimitiveSymbolicApply;
import jbse.val.Rewriter;
import jbse.val.exc.NoResultException;
import jbse.val.Primitive;

/**
 * A {@link Rewriter} which rewrites tan(atan(x)) to x.
 * 
 * @author Pietro Braione
 *
 */
public class RewriterArcTan extends RewriterCalculatorRewriting {

	public RewriterArcTan() { }
	
	@Override
	protected void rewritePrimitiveSymbolicApply(PrimitiveSymbolicApply x)
	throws NoResultException {
		final String operator = x.getOperator();
		if (operator == PrimitiveSymbolicApply.TAN) {
			final Primitive arg = (Primitive) x.getArgs()[0];
			if (arg instanceof PrimitiveSymbolicApply) {
				final PrimitiveSymbolicApply argFA = (PrimitiveSymbolicApply) arg;
				if (argFA.getOperator().equals(PrimitiveSymbolicApply.ATAN)) {
					setResult(this.calc.simplify((Primitive) argFA.getArgs()[0]));
					return;
				}
			}
		}
		setResult(x);
	}
}
