package jbse.dec;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.hamcrest.CoreMatchers.instanceOf;

import java.util.ArrayList;
import java.util.TreeSet;

import jbse.common.Type;
import jbse.common.exc.InvalidInputException;
import jbse.dec.exc.DecisionException;
import jbse.mem.ClauseAssume;
import jbse.mem.exc.ContradictionException;
import jbse.rewr.CalculatorRewriting;
import jbse.rewr.RewriterNegationElimination;
import jbse.rewr.RewriterExpressionOrConversionOnSimplex;
import jbse.rewr.RewriterFunctionApplicationOnSimplex;
import jbse.rewr.RewriterZeroUnit;
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
	private static final String SWITCH_CHAR = System.getProperty("os.name").toLowerCase().contains("windows") ? "/" : "-";
	private static final ArrayList<String> Z3_COMMAND_LINE = new ArrayList<>();
	private static final ArrayList<String> CVC4_COMMAND_LINE = new ArrayList<>();

	//BEGIN TO PATCH
	private static final ArrayList<String> COMMAND_LINE = Z3_COMMAND_LINE;

	//Settings for Docker
	private static final String SMT_SOLVER_PATH = "/Users/Andrew/Desktop/programming/java/cs591-23/z3-4.8.14-x64-osx-10.16/bin/z3";

	//Pietro's local settings
	//private static final String SMT_SOLVER_PATH = "/usr/local/bin/z3";

	//Giovanni's local settings
	//private static final String SMT_SOLVER_PATH = "/Users/denaro/Desktop/RTools/Z3/z3-4.3.2.d548c51a984e-x64-osx-10.8.5/bin/z3";
	//END TO PATCH

	static {
		Z3_COMMAND_LINE.add(SMT_SOLVER_PATH);
		Z3_COMMAND_LINE.add(SWITCH_CHAR + "smt2");
		Z3_COMMAND_LINE.add(SWITCH_CHAR + "in");
		Z3_COMMAND_LINE.add(SWITCH_CHAR + "t:10");
		CVC4_COMMAND_LINE.add(SMT_SOLVER_PATH);
		CVC4_COMMAND_LINE.add("--lang=smt2");
		CVC4_COMMAND_LINE.add("--output-lang=smt2");
		CVC4_COMMAND_LINE.add("--no-interactive");
		CVC4_COMMAND_LINE.add("--incremental");
		CVC4_COMMAND_LINE.add("--tlimit-per=10000");
	}

	CalculatorRewriting calc;
	DecisionAlternativeComparators cmp;
	DecisionProcedureAlgorithms dec;

	@Before
	public void setUp() throws DecisionException, InvalidInputException {
		this.calc = new CalculatorRewriting();
		this.calc.addRewriter(new RewriterExpressionOrConversionOnSimplex());
		this.calc.addRewriter(new RewriterFunctionApplicationOnSimplex());
		this.calc.addRewriter(new RewriterZeroUnit());
		this.calc.addRewriter(new RewriterNegationElimination());
		this.cmp = new DecisionAlternativeComparators();
		this.dec = 
		new DecisionProcedureAlgorithms(
		                                new DecisionProcedureSMTLIB2_AUFNIRA(
		                                                                     new DecisionProcedureAlwSat(this.calc), COMMAND_LINE));
	}

	@Test
	public void testSimplifyDecision1() 
	throws InvalidInputException, DecisionException, InvalidOperandException, InvalidTypeException {
		// 2 > 4
		Primitive p = this.calc.pushInt(2).gt(this.calc.valInt(4)).pop();
		TreeSet<DecisionAlternative_IFX> d = new TreeSet<>(this.cmp.get(DecisionAlternative_IFX.class));
		this.dec.decide_IFX(p, d);

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
		Expression e = (Expression) this.calc.push(A).gt(this.calc.valInt(0)).pop();
		e = (Expression) this.calc.push(e).and(this.calc.push(A).le(this.calc.valInt(1)).pop()).pop();
		TreeSet<DecisionAlternative_IFX> d = new TreeSet<>(this.cmp.get(DecisionAlternative_IFX.class));
		this.dec.decide_IFX(e, d);

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
		this.dec.decide_XCMPY(two, five, d);

		//expected {LT_concrete}
		assertEquals(1, d.size());
		DecisionAlternative_XCMPY dac = d.first();
		assertTrue(dac.concrete());
		assertThat(dac, instanceOf(DecisionAlternative_XCMPY_Lt.class));
	}

	@Test
	public void testSimplifyComparison2() 
	throws InvalidInputException, DecisionException, InvalidOperandException, InvalidTypeException, ContradictionException {
		// A >= 0 |- 2 * A ? A
		Term A = this.calc.valTerm(Type.INT, "A");
		Expression Agezero = (Expression) this.calc.push(A).ge(this.calc.valInt(0)).pop();
		Expression Atwice = (Expression) this.calc.push(A).mul(this.calc.valInt(2)).pop();
		TreeSet<DecisionAlternative_XCMPY> d = new TreeSet<>(this.cmp.get(DecisionAlternative_XCMPY.class));
		this.dec.pushAssumption(new ClauseAssume(Agezero));
		this.dec.decide_XCMPY(Atwice, A, d);

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
		Expression e = (Expression) this.calc.push(A).ge(this.calc.valInt(0)).pop();
		e = (Expression) this.calc.push(e).and(this.calc.push(A).lt(this.calc.valInt(5)).pop()).pop();
		e = (Expression) this.calc.push(e).and(this.calc.push(A).eq(this.calc.valInt(1)).not().pop()).pop();
		e = (Expression) this.calc.push(e).and(this.calc.push(A).eq(this.calc.valInt(2)).not().pop()).pop();
		e = (Expression) this.calc.push(e).and(this.calc.push(A).eq(this.calc.valInt(3)).not().pop()).pop();

		//expected satisfiable (by A == 4)
		assertTrue(this.dec.isSat(e));
	}

	@Test
	public void testSimplify2() 
	throws InvalidInputException, DecisionException, InvalidOperandException, InvalidTypeException {
		// true |- ((A >= 0) || (A < -3)) && (A == -1)  
		Term A = this.calc.valTerm(Type.INT, "A");
		Primitive e = this.calc.valBoolean(true);
		e = this.calc.push(e).and(this.calc.push(A).ge(this.calc.valInt(0)).pop()).pop();
		e = (Expression) this.calc.push(e).or(this.calc.push(A).lt(this.calc.valInt(-3)).pop()).pop();
		e = (Expression) this.calc.push(e).and(this.calc.push(A).eq(this.calc.valInt(-1)).pop()).pop();

		//expected unsatisfiable
		assertFalse(this.dec.isSat((Expression) e));
	}

	@Test
	public void testSimplify3() 
	throws InvalidInputException, DecisionException, InvalidOperandException, InvalidTypeException {
		// true |- (X && !Y) || (!X && Y)

		//boolean terms are emulated with satisfiable independent clauses
		Term A = this.calc.valTerm(Type.INT, "A");
		Term B = this.calc.valTerm(Type.INT, "B");
		Primitive X = this.calc.push(A).ge(this.calc.valInt(0)).pop();
		Primitive Y = this.calc.push(B).ge(this.calc.valInt(0)).pop();
		Primitive e0 = this.calc.valBoolean(true);
		e0 = this.calc.push(e0).and(X).pop();
		e0 = this.calc.push(e0).and(this.calc.push(Y).not().pop()).pop();
		Primitive e1 = this.calc.valBoolean(true);
		e1 = this.calc.push(e1).and(this.calc.push(X).not().pop()).pop();
		e1 = this.calc.push(e1).and(Y).pop();
		Primitive e = this.calc.push(e0).or(e1).pop();

		//expected satisfiable
		assertTrue(this.dec.isSat((Expression) e));
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
		e = this.calc.push(e).and(this.calc.push(zero).gt(V12).pop()).pop();
		//e = e.and(V14.ge(zero));
		//e = e.and(zero.lt(V14));
		//e = e.and(zero.le(V15));
		//e = e.and(V20.ge(zero));
		//e = e.and(zero.lt(V20));
		//e = e.and(V12.ne(bottom));
		e = this.calc.push(e).and(this.calc.push(V12).neg().le(V15).pop()).pop();
		e = this.calc.push(e).and(this.calc.push(V12).neg().lt(top).pop()).pop();
		e = this.calc.push(e).and(this.calc.push(V12).neg().sub(this.calc.push(X0).mul(eight).add(this.calc.push(X0).mul(two).pop()).pop()).eq(zero).pop()).pop();

		//this.dec.pushClause(e);

		//Expression ee = (Expression) X0.ne(zero);

		assertTrue(this.dec.isSat((Expression) e));
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
		e = this.calc.push(e).and(this.calc.push(zero).gt(B).pop()).pop();
		e = this.calc.push(e).and(this.calc.push(B).neg().le(C).pop()).pop();
		e = this.calc.push(e).and(this.calc.push(B).add(this.calc.push(A).mul(ten).pop()).eq(zero).pop()).pop();

		assertTrue(this.dec.isSat((Expression) e));
		//shows a past bug: the Sicstus server first simplifies the expression with the clpqr solver, 
		//which simplifies the third constraint as B - 1/10*C <= 0 , then reuses the simplified constraint
		//to feed the integer solver. The latter apparently solves 1/10 as 0, yielding an unsatisfiable set 
		//of constraints.
	}

	//Boundary value for integers (regression bug of the Sicstus server)
	@Test
	public void testBoundary1() 
	throws InvalidInputException, DecisionException, InvalidOperandException, InvalidTypeException {
		Expression e = (Expression) this.calc.pushTerm(Type.INT, "A").eq(this.calc.valInt(Integer.MIN_VALUE)).pop();
		assertTrue(this.dec.isSat(e));
	}

	//Other boundary value for integers
	@Test
	public void testBoundary2() 
	throws InvalidInputException, DecisionException, InvalidOperandException, InvalidTypeException {
		Expression e = (Expression) this.calc.pushTerm(Type.INT, "A").eq(this.calc.valInt(Integer.MAX_VALUE)).pop();
		assertTrue(this.dec.isSat(e));
	}

	//Test floats
	@Test
	public void testType1() 
	throws InvalidInputException, DecisionException, InvalidOperandException, InvalidTypeException {
		Expression e = (Expression) this.calc.pushTerm(Type.FLOAT, "A").gt(this.calc.valInt(0)).and(this.calc.pushTerm(Type.FLOAT, "A").lt(this.calc.valInt(1)).pop()).pop();
		assertTrue(this.dec.isSat(e));
	}

	//Test ints
	@Test
	public void testType2() 
	throws InvalidInputException, DecisionException, InvalidOperandException, InvalidTypeException {
		Expression e = (Expression) this.calc.pushTerm(Type.INT, "A").gt(this.calc.valInt(0)).and(this.calc.pushTerm(Type.FLOAT, "A").lt(this.calc.valInt(1)).pop()).pop();
		assertFalse(this.dec.isSat(e));
	}

	//Test integer division (Sicstus bug)
	@Test
	public void testIDiv() 
	throws InvalidInputException, DecisionException, InvalidOperandException, InvalidTypeException {
		// 0 <= A < B && A >= B / 2 && B = 1 (for integer division A == 0 is a solution, no solution for real division).
		final Term A = this.calc.valTerm(Type.INT, "A");
		final Term B = this.calc.valTerm(Type.INT, "B");
		final Expression e = (Expression) this.calc.push(A).ge(this.calc.valInt(0)).and(this.calc.push(A).lt(B).pop()).and(this.calc.push(A).ge(this.calc.push(B).div(this.calc.valInt(2)).pop()).pop()).and(this.calc.push(B).eq(this.calc.valInt(1)).pop()).pop();
		assertTrue(this.dec.isSat(e));
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
		e = this.calc.push(e).and(this.calc.push(A).div(B).add(this.calc.push(C).sub(C).div(D).pop()).lt(E).pop()).pop();

		//expected satisfiable
		assertTrue(this.dec.isSat((Expression) e));
	}

	@Test
	public void testOther2() 
	throws InvalidInputException, DecisionException, InvalidOperandException, InvalidTypeException {
		//true |- 2 - (3 + A) <= A
		Simplex two = this.calc.valInt(2);
		Simplex three = this.calc.valInt(3);
		Term A = this.calc.valTerm(Type.INT, "A");

		Primitive e = this.calc.valBoolean(true);
		e = this.calc.push(e).and(this.calc.push(two).sub(this.calc.push(three).add(A).pop()).le(A).pop()).pop();

		//expected satisfiable
		assertTrue(this.dec.isSat((Expression) e));
	}

	@Test
	public void testOther3() 
	throws InvalidInputException, DecisionException, InvalidOperandException, InvalidTypeException {
		//true |- A / 2 < 2
		Simplex two = this.calc.valInt(2);
		Term A = this.calc.valTerm(Type.INT, "A");

		Primitive e = this.calc.valBoolean(true);
		e = this.calc.push(e).and(this.calc.push(A).div(two).lt(two).pop()).pop();

		//expected satisfiable
		assertTrue(this.dec.isSat((Expression) e));
	}	
}
