package jbse.rewr;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import jbse.common.Type;
import jbse.common.exc.UnexpectedInternalException;
import jbse.rewr.exc.NoResultException;
import jbse.val.HistoryPoint;
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
	
	HistoryPoint hist;
	CalculatorRewriting calc;
	
	@Before
	public void before() {
		this.hist = HistoryPoint.unknown();
		this.calc = new CalculatorRewriting();
		this.calc.addRewriter(new RewriterBasic());
	}

	@Test
	public void testBasic() throws Exception {
		final Primitive p_post = this.calc.applyFunctionPrimitive(Type.INT, this.hist, "foo", this.calc.valTerm(Type.DOUBLE, "BAZ"));
		assertEquals(this.calc.applyFunctionPrimitive(Type.INT, this.hist, "foo", this.calc.valTerm(Type.DOUBLE, "FOO")), p_post);
	}

}
