package jbse.rewr;

import jbse.exc.common.UnexpectedInternalException;
import jbse.exc.rewr.NoResultException;
import jbse.mem.FunctionApplication;
import jbse.mem.Primitive;

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
	throws NoResultException, UnexpectedInternalException {
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
