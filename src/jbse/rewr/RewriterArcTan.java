package jbse.rewr;

import jbse.rewr.exc.NoResultException;
import jbse.val.FunctionApplication;
import jbse.val.Primitive;

/**
 * A {@link Rewriter} which rewrites tan(atan(x)) to x.
 * 
 * @author Pietro Braione
 *
 */
public class RewriterArcTan extends Rewriter {

	public RewriterArcTan() { }
	
	@Override
	protected void rewriteFunctionApplication(FunctionApplication x)
	throws NoResultException {
		final String operator = x.getOperator();
		if (operator == FunctionApplication.TAN) {
			final Primitive arg = x.getArgs()[0];
			if (arg instanceof FunctionApplication) {
				final FunctionApplication argFA = (FunctionApplication) arg;
				if (argFA.getOperator().equals(FunctionApplication.ATAN)) {
					setResult(calc.applyRewriters(argFA.getArgs()[0]));
					return;
				}
			}
		}
		super.rewriteFunctionApplication(x);
	}
}
