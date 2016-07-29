package jbse.dec;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.instanceOf;

import java.util.HashMap;
import java.util.TreeSet;

import jbse.bc.ClassFileFactoryJavassist;
import jbse.bc.ClassHierarchy;
import jbse.bc.Classpath;
import jbse.bc.exc.InvalidClassFileFactoryClassException;
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

import org.junit.Before;
import org.junit.Test;

public class DecisionProcedureTest {
	final CalculatorRewriting calc;
    final ClassHierarchy hier;
    final DecisionAlternativeComparators cmp;
	DecisionProcedureAlgorithms dec;
	
	public DecisionProcedureTest() throws InvalidClassFileFactoryClassException {
		this.calc = new CalculatorRewriting();
		this.calc.addRewriter(new RewriterOperationOnSimplex());
        this.hier = new ClassHierarchy(new Classpath(), ClassFileFactoryJavassist.class, new HashMap<>());
		this.cmp = new DecisionAlternativeComparators();
	}

	@Before
	public void setUp() throws DecisionException {
	    this.dec = 
	        new DecisionProcedureAlgorithms(
	            new DecisionProcedureSMTLIB2_AUFNIRA(new DecisionProcedureAlwSat(), this.calc, "/opt/local/bin/z3 -smt2 -in -t:10"), 
	            this.calc);
	}

	@Test
	public void testSimplifyDecision1() 
    throws InvalidInputException, DecisionException, InvalidOperandException, InvalidTypeException {
		// 2 > 4
		Primitive p = this.calc.valInt(2).gt(this.calc.valInt(4));
        TreeSet<DecisionAlternative_IFX> d = new TreeSet<>(this.cmp.get(DecisionAlternative_IFX.class));
        this.dec.decide_IFX(this.hier, p, d);

		//expected: {F_concrete}
		assertEquals(1, d.size());
		DecisionAlternative_IFX dai = d.first();
		assertTrue(dai.concrete());
		assertThat(dai, instanceOf(DecisionAlternative_IFX_False.class));
	}

	@Test
	public void testSimplifyDecision2() 
    throws InvalidInputException, DecisionException, InvalidOperandException, InvalidTypeException {
		// true |- (A > 0) && (A <= 1)
		Term A = this.calc.valTerm(Type.INT, "A");
		Expression e = (Expression) A.gt(this.calc.valInt(0));
		e = (Expression) e.and(A.le(this.calc.valInt(1)));
        TreeSet<DecisionAlternative_IFX> d = new TreeSet<>(this.cmp.get(DecisionAlternative_IFX.class));
        this.dec.decide_IFX(this.hier, e, d);

		//expected: {T_nonconcrete, F_nonconcrete}
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
		Simplex two = this.calc.valInt(2);
		Simplex five = this.calc.valInt(5);
		TreeSet<DecisionAlternative_XCMPY> d = new TreeSet<>(this.cmp.get(DecisionAlternative_XCMPY.class));
		this.dec.decide_XCMPY(this.hier, two, five, d);
		
        //expected {LT_concrete}
		assertEquals(1, d.size());
		DecisionAlternative_XCMPY dac = d.first();
		assertTrue(dac.concrete());
		assertThat(dac, instanceOf(DecisionAlternative_XCMPY_Lt.class));
	}

	@Test
	public void testSimplifyComparison2() 
    throws InvalidInputException, DecisionException, InvalidOperandException, InvalidTypeException {
		// A >= 0 |- 2 * A ? A
		Term A = this.calc.valTerm(Type.INT, "A");
		Expression Agezero = (Expression) A.ge(this.calc.valInt(0));
		Expression Atwice = (Expression) A.mul(this.calc.valInt(2));
		TreeSet<DecisionAlternative_XCMPY> d = new TreeSet<>(this.cmp.get(DecisionAlternative_XCMPY.class));
		this.dec.pushAssumption(new ClauseAssume(Agezero));
		this.dec.decide_XCMPY(this.hier, Atwice, A, d);
		
        //expected {GT_nonconcrete, EQ_nonconcrete}
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
		Term A = this.calc.valTerm(Type.INT, "A");
		Expression e = (Expression) A.ge(this.calc.valInt(0));
		e = (Expression) e.and(A.lt(this.calc.valInt(5)));
		e = (Expression) e.and(A.eq(this.calc.valInt(1)).not());
		e = (Expression) e.and(A.eq(this.calc.valInt(2)).not());
		e = (Expression) e.and(A.eq(this.calc.valInt(3)).not());

		//expected satisfiable (by A == 4)
		assertTrue(this.dec.isSat(this.hier, e));
	}

	@Test
	public void testSimplify2() 
	throws InvalidInputException, DecisionException, InvalidOperandException, InvalidTypeException {
		// true |- ((A >= 0) || (A < -3)) && (A == -1)  
		Term A = this.calc.valTerm(Type.INT, "A");
		Primitive e = this.calc.valBoolean(true);
		e = e.and(A.ge(this.calc.valInt(0)));
		e = (Expression) e.or(A.lt(this.calc.valInt(-3)));
		e = (Expression) e.and(A.eq(this.calc.valInt(-1)));

		//expected unsatisfiable
		assertFalse(this.dec.isSat(this.hier, (Expression) e));
	}

	@Test
	public void testSimplify3() 
	throws InvalidInputException, DecisionException, InvalidOperandException, InvalidTypeException {
		// true |- (X && !Y) || (!X && Y)

		//boolean terms are emulated with satisfiable independent clauses
		Term A = this.calc.valTerm(Type.INT, "A");
		Term B = this.calc.valTerm(Type.INT, "B");
		Primitive X = A.ge(this.calc.valInt(0));
		Primitive Y = B.ge(this.calc.valInt(0));
		Primitive e0 = this.calc.valBoolean(true);
		e0 = e0.and(X);
		e0 = e0.and(Y.not());
		Primitive e1 = this.calc.valBoolean(true);
		e1 = e1.and(X.not());
		e1 = e1.and(Y);
		Primitive e = e0.or(e1);

		//expected satisfiable
		assertTrue(this.dec.isSat(this.hier, (Expression) e));
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
		Term V12 = this.calc.valTerm(Type.INT, "V12");
		//Term V14 = this.calc.valTerm(Type.INT, "V14");
		Term V15 = this.calc.valTerm(Type.INT, "V15");
		//Term V20 = this.calc.valTerm(Type.INT, "V20");
		Term X0 = this.calc.valTerm(Type.INT, "X0");
		Simplex zero = this.calc.valInt(0);
		//Simplex bottom = this.calc.valInt(-2147483648);
		Simplex top = this.calc.valInt(65536);
		Simplex eight = this.calc.valInt(8);
		Simplex two = this.calc.valInt(2);

		Primitive e = this.calc.valBoolean(true);
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

		//this.dec.pushClause(e);

		//Expression ee = (Expression) X0.ne(zero);

		assertTrue(this.dec.isSat(this.hier, (Expression) e));
	}

	@Test
	public void testAssumption2() 
	throws InvalidInputException, DecisionException, InvalidOperandException, InvalidTypeException {
		//assume:
		//0 > B &&
		//-B <= C &&
		//B + 10.0 * A = 0 
		Term A = this.calc.valTerm(Type.INT, "A");
		Term B = this.calc.valTerm(Type.INT, "B");
		Term C = this.calc.valTerm(Type.INT, "C");
		Simplex zero = this.calc.valInt(0);
		Simplex ten = this.calc.valInt(10);

		Primitive e = this.calc.valBoolean(true);
		e = e.and(zero.gt(B));
		e = e.and(B.neg().le(C));
		e = e.and(B.add(A.mul(ten)).eq(zero));

		assertTrue(this.dec.isSat(this.hier, (Expression) e));
		//shows a past bug: the Sicstus server first simplifies the expression with the clpqr solver, 
		//which simplifies the third constraint as B - 1/10*C <= 0 , then reuses the simplified constraint
		//to feed the integer solver. The latter apparently solves 1/10 as 0, yielding an unsatisfiable set 
		//of constraints.
	}

	//Boundary value for integers (regression bug of the Sicstus server)
	@Test
	public void testBoundary1() 
	throws InvalidInputException, DecisionException, InvalidOperandException, InvalidTypeException {
		Expression e = (Expression) this.calc.valTerm(Type.INT, "A").eq(this.calc.valInt(Integer.MIN_VALUE));
		assertTrue(this.dec.isSat(this.hier, e));
	}
	
	//Other boundary value for integers
	@Test
	public void testBoundary2() 
	throws InvalidInputException, DecisionException, InvalidOperandException, InvalidTypeException {
		Expression e = (Expression) this.calc.valTerm(Type.INT, "A").eq(this.calc.valInt(Integer.MAX_VALUE));
		assertTrue(this.dec.isSat(this.hier, e));
	}
	
	//Test floats
	@Test
	public void testType1() 
	throws InvalidInputException, DecisionException, InvalidOperandException, InvalidTypeException {
		Expression e = (Expression) this.calc.valTerm(Type.FLOAT, "A").gt(this.calc.valInt(0)).and(this.calc.valTerm(Type.FLOAT, "A").lt(this.calc.valInt(1)));
		assertTrue(this.dec.isSat(this.hier, e));
	}
	
	//Test ints
	@Test
	public void testType2() 
	throws InvalidInputException, DecisionException, InvalidOperandException, InvalidTypeException {
		Expression e = (Expression) this.calc.valTerm(Type.INT, "A").gt(this.calc.valInt(0)).and(this.calc.valTerm(Type.FLOAT, "A").lt(this.calc.valInt(1)));
		assertFalse(this.dec.isSat(this.hier, e));
	}
    
    //Test integer division (Sicstus bug)
    @Test
    public void testIDiv() 
    throws InvalidInputException, DecisionException, InvalidOperandException, InvalidTypeException {
        // 0 <= A < B && A >= B / 2 && B = 1 (for integer division A == 0 is a solution, no solution for real division).
        final Term A = this.calc.valTerm(Type.INT, "A");
        final Term B = this.calc.valTerm(Type.INT, "B");
        final Expression e = (Expression) A.ge(this.calc.valInt(0)).and(A.lt(B)).and(A.ge(B.div(this.calc.valInt(2)))).and(B.eq(this.calc.valInt(1)));
        assertTrue(this.dec.isSat(this.hier, e));
    }
	
	//Old Sicstus bug
	@Test
	public void testOther1() 
	throws InvalidInputException, DecisionException, InvalidOperandException, InvalidTypeException {
		//true |- A/B + (C - C) / D < E  
		Term A = this.calc.valTerm(Type.INT, "A");
		Term B = this.calc.valTerm(Type.INT, "B");
		Term C = this.calc.valTerm(Type.INT, "C");
		Term D = this.calc.valTerm(Type.INT, "D");
		Term E = this.calc.valTerm(Type.INT, "E");
		
		Primitive e = this.calc.valBoolean(true);
		e = e.and(A.div(B).add(C.sub(C).div(D)).lt(E));
		
		//expected satisfiable
		assertTrue(this.dec.isSat(this.hier, (Expression) e));
	}
	
	@Test
	public void testOther2() 
	throws InvalidInputException, DecisionException, InvalidOperandException, InvalidTypeException {
		//true |- 2 - (3 + A) <= A
		Simplex two = this.calc.valInt(2);
		Simplex three = this.calc.valInt(3);
		Term A = this.calc.valTerm(Type.INT, "A");
		
		Primitive e = this.calc.valBoolean(true);
		e = e.and(two.sub(three.add(A)).le(A));
		
		//expected satisfiable
		assertTrue(this.dec.isSat(this.hier, (Expression) e));
	}
	
	@Test
	public void testOther3() 
	throws InvalidInputException, DecisionException, InvalidOperandException, InvalidTypeException {
		Simplex two = this.calc.valInt(2);
		Term A = this.calc.valTerm(Type.INT, "A");
		
		Primitive e = this.calc.valBoolean(true);
		e = e.and(A.div(two).lt(two));
		
		//expected satisfiable
		assertTrue(this.dec.isSat(this.hier, (Expression) e));
	}	
}
