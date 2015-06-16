package jbse.dec;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.instanceOf;

import java.util.TreeSet;

import jbse.common.Type;
import jbse.dec.exc.DecisionException;
import jbse.dec.exc.InvalidInputException;
import jbse.mem.ClauseAssume;
import jbse.rewr.CalculatorRewriting;
import jbse.rewr.RewriterOperationOnSimplex;
import jbse.tree.DecisionAlternative_XCMPY_Eq;
import jbse.tree.DecisionAlternative_XCMPY_Gt;
import jbse.tree.DecisionAlternative_XCMPY_Lt;
import jbse.tree.DecisionAlternative_IFX;
import jbse.tree.DecisionAlternative_XCMPY;
import jbse.tree.DecisionAlternativeComparators;
import jbse.tree.DecisionAlternative_IFX_False;
import jbse.tree.DecisionAlternative_IFX_True;
import jbse.val.Expression;
import jbse.val.Primitive;
import jbse.val.Simplex;
import jbse.val.Term;
import jbse.val.exc.InvalidOperandException;
import jbse.val.exc.InvalidTypeException;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class DecisionProcedureTest {

	static CalculatorRewriting calc;
	static DecisionProcedureAlgorithms dec;
	static DecisionAlternativeComparators cmp;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		calc = new CalculatorRewriting();
		calc.addRewriter(new RewriterOperationOnSimplex());
		dec = new DecisionProcedureAlgorithms(
				new DecisionProcedureSicstus(new DecisionProcedureAlwSat(), calc, "/usr/local/bin/"), calc);
		cmp = new DecisionAlternativeComparators();
	}

	@AfterClass
	public static void tearDownBeforeClass() throws Exception {
		dec.close();
	}

	@After
	public void tearDown() throws Exception {
		dec.clearAssumptions();
	}


	@Test
	public void testSimplifyDecision1() 
    throws InvalidInputException, DecisionException, InvalidOperandException, InvalidTypeException {
		// 2 > 4
		Primitive p = calc.valInt(2).gt(calc.valInt(4));

		//expected: {F_concrete}
		TreeSet<DecisionAlternative_IFX> d = new TreeSet<>(cmp.get(DecisionAlternative_IFX.class));
		dec.decide_IFX(null, p, d);
		assertEquals(1, d.size());
		DecisionAlternative_IFX dai = d.first();
		assertTrue(dai.concrete());
		assertThat(dai, instanceOf(DecisionAlternative_IFX_False.class));
	}

	@Test
	public void testSimplifyDecision2() 
    throws InvalidInputException, DecisionException, InvalidOperandException, InvalidTypeException {
		// true |- (A > 0) && (A <= 1)
		Term A = calc.valTerm(Type.INT, "A");
		Expression e = (Expression) A.gt(calc.valInt(0));
		e = (Expression) e.and(A.le(calc.valInt(1)));

		//expected: {T_nonconcrete, F_nonconcrete}
		TreeSet<DecisionAlternative_IFX> d = new TreeSet<>(cmp.get(DecisionAlternative_IFX.class));
		dec.decide_IFX(null, e, d);
		assertEquals(2, d.size());
		DecisionAlternative_IFX dai1 = d.first();
		d.remove(dai1);
		DecisionAlternative_IFX dai2 = d.first();
		assertFalse(dai1.concrete());
		assertFalse(dai2.concrete());
		assertThat(dai1, instanceOf(DecisionAlternative_IFX_True.class));
		assertThat(dai2, instanceOf(DecisionAlternative_IFX_False.class));
	}

	@Test
	public void testSimplifyComparison1() 
	throws InvalidInputException, DecisionException {
		// true |- 2 ? 5
		Simplex two = calc.valInt(2);
		Simplex five = calc.valInt(5);

		//expected {LT_concrete}
		TreeSet<DecisionAlternative_XCMPY> d = new TreeSet<>(cmp.get(DecisionAlternative_XCMPY.class));
		dec.decide_XCMPY(null, two, five, d);
		assertEquals(1, d.size());
		DecisionAlternative_XCMPY dac = d.first();
		assertTrue(dac.concrete());
		assertThat(dac, instanceOf(DecisionAlternative_XCMPY_Lt.class));
	}

	@Test
	public void testSimplifyComparison2() 
    throws InvalidInputException, DecisionException, InvalidOperandException, InvalidTypeException {
		// A >= 0 |- 2 * A ? A
		Term A = calc.valTerm(Type.INT, "A");
		Expression Agezero = (Expression) A.ge(calc.valInt(0));
		Expression Atwice = (Expression) A.mul(calc.valInt(2));

		//expected {GT_nonconcrete, EQ_nonconcrete}
		TreeSet<DecisionAlternative_XCMPY> d = new TreeSet<>(cmp.get(DecisionAlternative_XCMPY.class));
		dec.pushAssumption(new ClauseAssume(Agezero));
		dec.decide_XCMPY(null, Atwice, A, d);
		assertEquals(2, d.size());
		DecisionAlternative_XCMPY dac1 = d.first();
		d.remove(dac1);
		DecisionAlternative_XCMPY dac2 = d.first();
		assertFalse(dac1.concrete());
		assertFalse(dac2.concrete());
		assertThat(dac1, instanceOf(DecisionAlternative_XCMPY_Gt.class));
		assertThat(dac2, instanceOf(DecisionAlternative_XCMPY_Eq.class));
	}

	@Test
	public void testSimplify1() 
	throws InvalidInputException, DecisionException, InvalidOperandException, InvalidTypeException {
		// true |- (A >= 0) && (A < 5) && !(A == 1) && !(A == 2) && !(A == 3)
		Term A = calc.valTerm(Type.INT, "A");
		Expression e = (Expression) A.ge(calc.valInt(0));
		e = (Expression) e.and(A.lt(calc.valInt(5)));
		e = (Expression) e.and(A.eq(calc.valInt(1)).not());
		e = (Expression) e.and(A.eq(calc.valInt(2)).not());
		e = (Expression) e.and(A.eq(calc.valInt(3)).not());

		//expected satisfiable (by A == 4)
		assertTrue(dec.isSat(null, e));
	}

	@Test
	public void testSimplify2() 
	throws InvalidInputException, DecisionException, InvalidOperandException, InvalidTypeException {
		// true |- ((A >= 0) || (A < -3)) && (A == -1)  
		Term A = calc.valTerm(Type.INT, "A");
		Primitive e = calc.valBoolean(true);
		e = e.and(A.ge(calc.valInt(0)));
		e = (Expression) e.or(A.lt(calc.valInt(-3)));
		e = (Expression) e.and(A.eq(calc.valInt(-1)));

		//expected unsatisfiable
		assertFalse(dec.isSat(null, (Expression) e));
	}

	@Test
	public void testSimplify3() 
	throws InvalidInputException, DecisionException, InvalidOperandException, InvalidTypeException {
		// true |- (X && !Y) || (!X && Y)

		//boolean terms are emulated with satisfiable independent clauses
		Term A = calc.valTerm(Type.INT, "A");
		Term B = calc.valTerm(Type.INT, "B");
		Primitive X = A.ge(calc.valInt(0));
		Primitive Y = B.ge(calc.valInt(0));
		Primitive e0 = calc.valBoolean(true);
		e0 = e0.and(X);
		e0 = e0.and(Y.not());
		Primitive e1 = calc.valBoolean(true);
		e1 = e1.and(X.not());
		e1 = e1.and(Y);
		Primitive e = e0.or(e1);

		//expected satisfiable
		assertTrue(dec.isSat(null, (Expression) e));
	}

	@Test
	public void testAssumption1() 
	throws InvalidInputException, DecisionException, InvalidOperandException, InvalidTypeException {
		//assume:
		//(0 > {V12}) && 
		//     ({V14} >= 0) && 
		//     (0 < {V14}) && 
		//     (0 <= {V15}) && 
		//     ({V20} >= 0) && 
		//     (0 < {V20})) && 
		//     ({V12} != -2147483648) && 
		//(- {V12} <= {V15}) && 
		//(- {V12} < 65536) && 
		//(- {V12} - ({X0} * 8.0 + {X0} * 2.0)) == 0)
		Term V12 = calc.valTerm(Type.INT, "V12");
		//Term V14 = calc.valTerm(Type.INT, "V14");
		Term V15 = calc.valTerm(Type.INT, "V15");
		//Term V20 = calc.valTerm(Type.INT, "V20");
		Term X0 = calc.valTerm(Type.INT, "X0");
		Simplex zero = calc.valInt(0);
		//Simplex bottom = calc.valInt(-2147483648);
		Simplex top = calc.valInt(65536);
		Simplex eight = calc.valInt(8);
		Simplex two = calc.valInt(2);

		Primitive e = calc.valBoolean(true);
		e = e.and(zero.gt(V12));
		//e = e.and(V14.ge(zero));
		//e = e.and(zero.lt(V14));
		//e = e.and(zero.le(V15));
		//e = e.and(V20.ge(zero));
		//e = e.and(zero.lt(V20));
		//e = e.and(V12.ne(bottom));
		e = e.and(V12.neg().le(V15));
		e = e.and(V12.neg().lt(top));
		e = e.and(V12.neg().sub(X0.mul(eight).add(X0.mul(two))).eq(zero));

		//dec.pushClause(e);

		//Expression ee = (Expression) X0.ne(zero);

		assertTrue(dec.isSat(null, (Expression) e));
	}

	@Test
	public void testAssumption2() 
	throws InvalidInputException, DecisionException, InvalidOperandException, InvalidTypeException {
		//assume:
		//0 > B &&
		//-B <= C &&
		//B + 10.0 * A = 0 
		Term A = calc.valTerm(Type.INT, "A");
		Term B = calc.valTerm(Type.INT, "B");
		Term C = calc.valTerm(Type.INT, "C");
		Simplex zero = calc.valInt(0);
		Simplex ten = calc.valInt(10);

		Primitive e = calc.valBoolean(true);
		e = e.and(zero.gt(B));
		e = e.and(B.neg().le(C));
		e = e.and(B.add(A.mul(ten)).eq(zero));

		assertTrue(dec.isSat(null, (Expression) e));
		//shows a past bug: the Sicstus server first simplifies the expression with the clpqr solver, 
		//which simplifies the third constraint as B - 1/10*C <= 0 , then reuses the simplified constraint
		//to feed the integer solver. The latter apparently solves 1/10 as 0, yielding an unsatisfiable set 
		//of constraints.
	}

	//Boundary value for integers (regression bug of the Sicstus server)
	@Test
	public void testBoundary1() 
	throws InvalidInputException, DecisionException, InvalidOperandException, InvalidTypeException {
		Expression e = (Expression) calc.valTerm(Type.INT, "A").eq(calc.valInt(Integer.MIN_VALUE));
		assertTrue(dec.isSat(null, e));
	}
	
	//Other boundary value for integers
	@Test
	public void testBoundary2() 
	throws InvalidInputException, DecisionException, InvalidOperandException, InvalidTypeException {
		Expression e = (Expression) calc.valTerm(Type.INT, "A").eq(calc.valInt(Integer.MAX_VALUE));
		assertTrue(dec.isSat(null, e));
	}
	
	//Test floats
	@Test
	public void testType1() 
	throws InvalidInputException, DecisionException, InvalidOperandException, InvalidTypeException {
		Expression e = (Expression) calc.valTerm(Type.FLOAT, "A").gt(calc.valInt(0)).and(calc.valTerm(Type.FLOAT, "A").lt(calc.valInt(1)));
		assertTrue(dec.isSat(null, e));
	}
	
	//Test ints
	@Test
	public void testType2() 
	throws InvalidInputException, DecisionException, InvalidOperandException, InvalidTypeException {
		Expression e = (Expression) calc.valTerm(Type.INT, "A").gt(calc.valInt(0)).and(calc.valTerm(Type.FLOAT, "A").lt(calc.valInt(1)));
		assertFalse(dec.isSat(null, e));
	}
    
    //Test integer division (Sicstus bug)
    @Test
    public void testIDiv() 
    throws InvalidInputException, DecisionException, InvalidOperandException, InvalidTypeException {
        // 0 <= A < B && A >= B / 2 && B = 1 (for integer division A == 0 is a solution, no solution for real division).
        final Term A = calc.valTerm(Type.INT, "A");
        final Term B = calc.valTerm(Type.INT, "B");
        final Expression e = (Expression) A.ge(calc.valInt(0)).and(A.lt(B)).and(A.ge(B.div(calc.valInt(2)))).and(B.eq(calc.valInt(1)));
        assertTrue(dec.isSat(null, e));
    }
	
	//Old Sicstus bug
	@Test
	public void testOther1() 
	throws InvalidInputException, DecisionException, InvalidOperandException, InvalidTypeException {
		//true |- A/B + (C - C) / D < E  
		Term A = calc.valTerm(Type.INT, "A");
		Term B = calc.valTerm(Type.INT, "B");
		Term C = calc.valTerm(Type.INT, "C");
		Term D = calc.valTerm(Type.INT, "D");
		Term E = calc.valTerm(Type.INT, "E");
		
		Primitive e = calc.valBoolean(true);
		e = e.and(A.div(B).add(C.sub(C).div(D)).lt(E));
		
		//expected satisfiable
		assertTrue(dec.isSat(null, (Expression) e));
	}
	
	@Test
	public void testOther2() 
	throws InvalidInputException, DecisionException, InvalidOperandException, InvalidTypeException {
		//true |- 2 - (3 + A) <= A
		Simplex two = calc.valInt(2);
		Simplex three = calc.valInt(3);
		Term A = calc.valTerm(Type.INT, "A");
		
		Primitive e = calc.valBoolean(true);
		e = e.and(two.sub(three.add(A)).le(A));
		
		//expected satisfiable
		assertTrue(dec.isSat(null, (Expression) e));
	}
	
	@Test
	public void testOther3() 
	throws InvalidInputException, DecisionException, InvalidOperandException, InvalidTypeException {
		Simplex two = calc.valInt(2);
		Term A = calc.valTerm(Type.INT, "A");
		
		Primitive e = calc.valBoolean(true);
		e = e.and(A.div(two).lt(two));
		
		//expected satisfiable
		assertTrue(dec.isSat(null, (Expression) e));
	}	
}
