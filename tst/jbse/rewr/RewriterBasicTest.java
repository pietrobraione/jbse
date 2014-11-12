package jbse.rewr;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import jbse.Type;
import jbse.exc.common.UnexpectedInternalException;
import jbse.exc.mem.InvalidTypeException;
import jbse.exc.rewr.NoResultException;
import jbse.mem.Primitive;
import jbse.mem.Term;

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
