package jbse.rewr;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import jbse.common.Type;
import jbse.common.exc.UnexpectedInternalException;
import jbse.rewr.exc.NoResultException;
import jbse.val.Primitive;
import jbse.val.Term;
import jbse.val.exc.InvalidTypeException;

public class RewriterBasicTest {
	private static class RewriterBasic extends Rewriter {
		public RewriterBasic() { }
		@Override
		protected void rewriteTerm(Term x) throws NoResultException {
			try {
				super.rewriteTerm(this.calc.valTerm(x.getType(), "FOO"));
			} catch (InvalidTypeException e) {
				//should not happen
				throw new UnexpectedInternalException(e);
			}
		}
	}
	
	@Test
	public void testBasic() throws Exception {
		CalculatorRewriting calc = new CalculatorRewriting();
		calc.addRewriter(new RewriterBasic());
		
		final Primitive p_post = calc.applyFunction(Type.INT, "foo", calc.valTerm(Type.DOUBLE, "BAZ"));
		assertEquals(calc.applyFunction(Type.INT, "foo", calc.valTerm(Type.DOUBLE, "FOO")), p_post);
	}

}
