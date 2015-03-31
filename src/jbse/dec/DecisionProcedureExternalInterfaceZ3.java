package jbse.dec;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Stack;

import jbse.common.Type;
import jbse.common.exc.UnexpectedInternalException;
import jbse.dec.exc.ExternalProtocolInterfaceException;
import jbse.mem.Objekt;
import jbse.rewr.CalculatorRewriting;
import jbse.val.Any;
import jbse.val.Expression;
import jbse.val.FunctionApplication;
import jbse.val.NarrowingConversion;
import jbse.val.Operator;
import jbse.val.Primitive;
import jbse.val.PrimitiveSymbolic;
import jbse.val.PrimitiveVisitor;
import jbse.val.ReferenceSymbolic;
import jbse.val.Simplex;
import jbse.val.Term;
import jbse.val.WideningConversion;

/**
 * {@link DecisionProcedureExternalInterface} to a generic SMTLIB 2 solver
 * that supports the AUFNIRA logic. 
 * 
 * @author Diego Piazza
 * @author Pietro Braione
 */
//TODO simplify implementation; make a general decision procedure for SMTLIB2-compatible solvers
public class DecisionProcedureExternalInterfaceZ3 extends DecisionProcedureExternalInterface {
	private static final String PROLOGUE = 
	    "(set-option :print-success true)\n" +
	    "(set-option :interactive-mode true)\n" +
	    "(set-logic AUFNIRA)\n" +
	    "(define-fun round_to_zero ((x Real)) Int (ite (>= x 0.0) (to_int x) (- (to_int (- x)))))";
	private static final String PUSH = "(push 1)";
	private static final String POP = "(pop 1)";
	private static final String CHECKSAT = "(check-sat)";
	private static final String OTHER = "";
	private static final String UNSAT = "unsat";
	
    private final ExpressionMangler m;
	private boolean working;
	private Process solver;
	private BufferedReader solverIn;
	private BufferedWriter solverOut;
	private String currentClausePositive;
	private String currentClauseNegative;
	private boolean hasCurrentClause;
	private Z3ExpressionVisitor v;
	private ArrayList<Integer> nSymPushed; 
	private int nSymCurrent;
	private int nTotalSym;

	/** 
	 * Costructor.
	 */
	public DecisionProcedureExternalInterfaceZ3(CalculatorRewriting calc, String Z3path) 
	throws ExternalProtocolInterfaceException, IOException {
		this.m = new ExpressionMangler("X", "", calc);
		this.working = true;
		this.solver = Runtime.getRuntime().exec(Z3path + "z3 -smt2 -in -t:10");
		this.solverIn = new BufferedReader (new InputStreamReader (this.solver.getInputStream()));
		this.solverOut = new BufferedWriter (new OutputStreamWriter (this.solver.getOutputStream()));
		this.solverOut.write(PROLOGUE);
		this.solverOut.write(PUSH);
		this.solverOut.flush();
		//TODO log differently!
		//System.err.println("--->Z3: " + PROLOGUE);
		//System.err.println("--->Z3: " + PUSH);
		clear();
	}
	
	@Override
	public boolean isWorking() {
		return this.working;
	}

	@Override
	public void sendClauseAssume(Primitive cond) 
	throws ExternalProtocolInterfaceException {
		if (this.hasCurrentClause) {
			throw new ExternalProtocolInterfaceException("Attempted to send a clause when a current clause already exists.");
		}
		if (cond == null || cond.getType() != Type.BOOLEAN) {
			throw new ExternalProtocolInterfaceException("Attempted to send an invalid clause.");
		}		
		try {
			cond.accept(this.v);
			this.currentClausePositive = PUSH + this.v.getDecl() + " (assert " + this.v.popClause() + ")";
			cond.not().accept(this.v);
			this.currentClauseNegative = PUSH + this.v.getDecl() + " (assert " + this.v.popClause() + ")";
			this.hasCurrentClause = true;
		} catch (ExternalProtocolInterfaceException | RuntimeException e) {
			throw e;
		} catch (Exception e) {
			//this should never happen
			this.working = false;
			throw new UnexpectedInternalException(e);
		}
	}

	@Override
	public void sendClauseAssumeAliases(ReferenceSymbolic r, long heapPos, Objekt o) 
	throws ExternalProtocolInterfaceException {
		if (this.hasCurrentClause) {
			this.working = false;
			throw new ExternalProtocolInterfaceException("Attempted to send a clause when a current clause already exists.");
		}
		//does nothing, this decision procedure works only for numbers
		this.currentClausePositive = this.currentClauseNegative = null;
		this.hasCurrentClause = true;
	}

	@Override
	public void sendClauseAssumeExpands(ReferenceSymbolic r, String className) 
	throws ExternalProtocolInterfaceException {
		if (this.hasCurrentClause) {
			throw new ExternalProtocolInterfaceException("Attempted to send a clause when a current clause already exists.");
		}
		//does nothing, this decision procedure works only for numbers
		this.currentClausePositive = this.currentClauseNegative = null;
		this.hasCurrentClause = true;
	}

	@Override
	public void sendClauseAssumeNull(ReferenceSymbolic r) 
	throws ExternalProtocolInterfaceException {
		if (this.hasCurrentClause) {
			throw new ExternalProtocolInterfaceException("Attempted to send a clause when a current clause already exists.");
		}
		//does nothing, this decision procedure works only for numbers		
		this.currentClausePositive = this.currentClauseNegative = null;
		this.hasCurrentClause = true;
	}

	@Override
	public void sendClauseAssumeClassInitialized(String className) 
	throws ExternalProtocolInterfaceException {
		if (this.hasCurrentClause) {
			throw new ExternalProtocolInterfaceException("Attempted to send a clause when a current clause already exists.");
		}
		//does nothing, this decision procedure works only for numbers
		this.currentClausePositive = this.currentClauseNegative = null;
		this.hasCurrentClause = true;
	}

	@Override
	public void sendClauseAssumeClassNotInitialized(String className) 
	throws ExternalProtocolInterfaceException {
		if (this.hasCurrentClause) {
			throw new ExternalProtocolInterfaceException("Attempted to send a clause when a current clause already exists.");
		}
		//does nothing, this decision procedure works only for numbers
		this.currentClausePositive = this.currentClauseNegative = null;
		this.hasCurrentClause = true;
	}

	@Override
	public void retractClause() throws ExternalProtocolInterfaceException {
		if (!this.hasCurrentClause) {
			throw new ExternalProtocolInterfaceException("Attempted to retract clause with no current predicate.");
		}
		this.currentClausePositive = this.currentClauseNegative = null;
		this.hasCurrentClause = false;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean checkSat(boolean value) 
	throws ExternalProtocolInterfaceException, IOException {
		if (!this.hasCurrentClause) {
			throw new ExternalProtocolInterfaceException("Attempted to check entailment with no current predicate.");
		}
		final String query = (value ? this.currentClausePositive : this.currentClauseNegative) + " " + CHECKSAT;
		try {
			this.solverOut.write(query + '\n');
			this.solverOut.flush();
		} catch (IOException e) {
			this.working = false;
			throw e;
		}
		//TODO log differently!
		//System.err.println("--- CHECKSAT: ");
		//System.err.println("--->Z3: " + query);

		final String result = this.solverIn.readLine();
		if (result == null) {
			this.working = false;
			throw new IOException("failed read of Z3 output");
		}
		//TODO log differently!
		//System.err.println("<---Z3: " + result);

		this.solverOut.write(POP);
		this.solverOut.flush();
		//TODO log differently!
		//System.err.println("--->Z3: " + POP);

		return !result.equals(UNSAT); //conservatively returns true if z3 answers that does not know the answer
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void pushAssumption(boolean value) 
	throws ExternalProtocolInterfaceException, IOException {
		if (!this.hasCurrentClause) {
			throw new ExternalProtocolInterfaceException("attempted to check entailment with no current predicate");
		}
		this.hasCurrentClause = false;
		if (this.currentClausePositive == null || this.currentClauseNegative == null) {
			return;
		}
		final String query = (value ? this.currentClausePositive : this.currentClauseNegative);
		try {
			this.solverOut.write(query);
			this.solverOut.flush();
		} catch (IOException e) {
			this.working = false;
			throw e;
		}		
		rememberPushedDeclarations();
		//TODO log differently!
		//System.err.println("--- PUSH_ASSUMPTION:");
		//System.err.println("--->Z3: " + query);
	}

	@Override
	public void popAssumption() throws IOException {
		try {
			this.solverOut.write(POP);
			this.solverOut.flush();
		} catch (IOException e) {
			this.working = false;
			throw e;
		}
		forgetPoppedDeclarations();
		//TODO log differently!
		//System.err.println("--- POP_ASSUMPTION:");
		//System.err.println("--->Z3: " + POP);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void clear() 
	throws ExternalProtocolInterfaceException, IOException {
		//TODO log differently!
		//System.err.println("--- CLEAR:");
		final int nToPop = (this.nSymPushed == null ? 0 : this.nSymPushed.size());
		try {
			if (nToPop > 0) {
				//TODO log differently!
				//System.err.println("--->Z3: (pop " + nToPop + ")");
				this.solverOut.write("(pop " + nToPop + ")");
			}
			this.solverOut.flush();
		} catch (IOException e) {
			this.working = false;
			throw e;
		}
		this.currentClausePositive = this.currentClauseNegative = null;
		this.hasCurrentClause = false;
		forgetAllDeclarations();
	}
	
	private void rememberPushedDeclarations() {
		this.v.clearDecl(); //reinitialize decl
		this.nSymPushed.add(this.nSymCurrent);
		this.nSymCurrent = 0;
	}
	
	private void forgetPoppedDeclarations() {
		final int last = this.nSymPushed.size() - 1;
		this.v.removeDeclaredSymbols(this.nSymPushed.get(last));
		this.nSymPushed.remove(last);
	}
	
	private void forgetAllDeclarations() {
		this.v = new Z3ExpressionVisitor();
		this.nSymPushed = new ArrayList<Integer>();
		this.nSymCurrent = 0;
		this.nTotalSym = 0;
	}
	
	/**
	 * Returns the SMTLIB2 primitive operator which corresponds 
	 * to a Java operator.
	 * 
	 */	
	private static String toSMTLIB2(Operator operator) {
		if (operator == Operator.ADD)      return "+";
		else if (operator == Operator.SUB) return "-";
		else if (operator == Operator.MUL) return "*";
		else if (operator == Operator.DIV) return "/";
		else if (operator == Operator.REM) return "mod";
		else if (operator == Operator.NEG) return "-";
		else if (operator == Operator.LT)  return "<";
		else if (operator == Operator.LE)  return "<=";
		else if (operator == Operator.EQ)  return "=";
		else if (operator == Operator.GE)  return ">=";
		else if (operator == Operator.GT)  return ">";
		else if (operator == Operator.AND) return "and";
		else if (operator == Operator.OR)  return "or";
		else if (operator == Operator.NOT) return "not";
		else return OTHER;
	}
	
	/**
	 * returns z3 primitive type which corresponds to a java type
	 * 
	 */
	private static String Z3PrimitiveType(char type) {
		if (type == Type.BYTE) {
			return "Int"; 
		} 
		else if (type == Type.SHORT) {
			return "Int"; 
		}
		else if (type == Type.INT) {
			return "Int";
		}
		else if (type == Type.LONG) {
			return "Int";
		} 
		else if (type == Type.CHAR) {
			return "Int";
		}
		else if (type == Type.FLOAT) {
			return "Real";
		}
		else if (type == Type.DOUBLE) {
			return "Real";
		}
		else if (type == Type.BOOLEAN) {
			return "Int";
		}
		else {
			//this should be unreachable!
			return OTHER;
		}
	}
	
	/**
	 * Builds a Z3 string representing the expression.
	 */
	private class Z3ExpressionVisitor implements PrimitiveVisitor {
		private boolean isBooleanExpression = true;
		private LinkedHashSet<String> declaredSymbols = new LinkedHashSet<String>();
		private StringBuilder decl = new StringBuilder();
		private Stack<String> clauseStack = new Stack<String>();
		
		public String popClause() { return this.clauseStack.pop(); }
		
		public String getDecl() { return this.decl.toString(); }
		
		public void clearDecl() {
			this.decl = new StringBuilder();                       
		}
		
		protected void removeDeclaredSymbols(int nVars) {
			final ArrayList<String> symToDel = new ArrayList<>();
			int n = nTotalSym - nVars;
			int c = 0;
			for (String s : this.declaredSymbols) {
				if (c < n){
				   ++c;
				} else {
					symToDel.add(s);
				}
			}
			declaredSymbols.removeAll(symToDel);
			nTotalSym = nTotalSym - nVars;
		}
		
		public Z3ExpressionVisitor() { }
		
		public Z3ExpressionVisitor(Z3ExpressionVisitor v, boolean isBooleanExpression) {
			this.isBooleanExpression = isBooleanExpression;
			this.declaredSymbols = v.declaredSymbols;
			this.decl = v.decl;
			this.clauseStack = v.clauseStack;
		}
		
		@Override
		public void visitAny(Any x) throws ExternalProtocolInterfaceException {
			throw new ExternalProtocolInterfaceException("values of type Any should not reach Z3");			
		}
		
		@Override
		public void visitExpression(Expression e) throws Exception {
			final Operator operation = e.getOperator();
			final String op = toSMTLIB2(operation);
			final boolean isBooleanOperator = operation.acceptsBoolean();
			if (operation.returnsBoolean() == this.isBooleanExpression) {
				//operation well formed
				if (operation == Operator.NE) {
					//1-NE is not a z3 operator but can be translated to a combination of Z3 operators
					e.getFirstOperand().accept(new Z3ExpressionVisitor(this, isBooleanOperator));
					e.getSecondOperand().accept(new Z3ExpressionVisitor(this, isBooleanOperator));
					final String secondOperand = this.clauseStack.pop();
					final String firstOperand = this.clauseStack.pop();
					this.clauseStack.push("(not (= " + firstOperand + " " + secondOperand + "))");
				} else if (op.equals(OTHER)) {
					//2-Operator does not correspond to a z3 operator
					m.mangle(e).accept(this);
				} else {
					//3-The operator correspond to a z3 operator
					final String clause;
					if (e.isUnary()) {
						e.getOperand().accept(new Z3ExpressionVisitor (this , isBooleanOperator));
						clause = "("+ op +" "+ this.clauseStack.pop() + ")";
					} else {
						e.getFirstOperand().accept(new Z3ExpressionVisitor (this , isBooleanOperator));
						e.getSecondOperand().accept(new Z3ExpressionVisitor (this, isBooleanOperator));
						final String secondOperand = this.clauseStack.pop();
						final String firstOperand = this.clauseStack.pop();
						clause = "("+ op + " " + firstOperand + " " + secondOperand + ")";
					}
					this.clauseStack.push(clause);
				}
			} else {
				throw new UnexpectedInternalException("error while parsing an expression for Z3: " + e.toString());
			}
		}

		@Override
		public void visitFunctionApplication(FunctionApplication x) throws Exception {
			final String operator = x.getOperator();
			final char type = x.getType();
			final StringBuilder clause = new StringBuilder();
			final StringBuilder z3Signature = new StringBuilder();
			boolean builtIn = false;
			if (operator.equals(FunctionApplication.ABS)) {
				if (Type.isPrimitiveIntegral(x.getType())) {
					builtIn = true;
					clause.append("(abs ");
					z3Signature.append("abs ("); //useless, but we keep it
				} else {
					clause.append("(absReals ");
					z3Signature.append("absReals (");
				}
			} else {
				clause.append("(" + operator + " ");
				z3Signature.append(operator + " (");
			}
			for (Primitive p : x.getArgs()) {
				p.accept(new Z3ExpressionVisitor(this, false));
				clause.append(this.clauseStack.pop());
				clause.append(" ");
				final String z3Type = Z3PrimitiveType(p.getType());
				z3Signature.append(z3Type);
				z3Signature.append(" ");
			}
			clause.append(")");
			this.clauseStack.push(clause.toString());
			z3Signature.append(") ");
			z3Signature.append(Z3PrimitiveType(type));

			if (this.declaredSymbols.contains(operator) || builtIn) {
				// does nothing
			} else {
				this.declaredSymbols.add(operator);
				this.decl.append(" (declare-fun " + z3Signature + " )");
				nSymCurrent = nSymCurrent + 1;
				nTotalSym = nTotalSym + 1;
			}
		}
		
		@Override
		public void visitWideningConversion(WideningConversion x) throws Exception {
			final Primitive arg = x.getArg();
			arg.accept(new Z3ExpressionVisitor(this, false));
			if (Type.isPrimitiveIntegral(x.getType()) != Type.isPrimitiveIntegral(arg.getType())) {
				this.clauseStack.push("(to_real " + this.clauseStack.pop() + ")");
			}
		}	

		@Override
		public void visitNarrowingConversion(NarrowingConversion x) throws Exception {
			final Primitive arg = x.getArg();
			arg.accept(new Z3ExpressionVisitor(this, false));
			if (Type.isPrimitiveIntegral(x.getType()) != Type.isPrimitiveIntegral(arg.getType())) {
				this.clauseStack.push("(round_to_zero " + this.clauseStack.pop() + ")");
			}
		}

		@Override
		public void visitSimplex(Simplex x) {
			final Object obj = x.getActualValue();
			final char mytype = x.getType();
			if (mytype == Type.BYTE ||
					mytype == Type.SHORT ||
					mytype == Type.INT ||
					mytype == Type.LONG ||
					mytype == Type.CHAR ||
					mytype == Type.FLOAT ||
					mytype == Type.DOUBLE) {
				if (obj instanceof Number && ((Number) obj).doubleValue() < 0) {
					this.clauseStack.push("(- " + obj.toString().substring(1) + ")");
				} else {
					this.clauseStack.push(obj.toString());
				}
			}  else if (mytype == Type.BOOLEAN) {
				if ((Boolean) obj) {
					this.clauseStack.push("1");
				} else {
					this.clauseStack.push("0");
				}
			}
		}
		
		@Override
		public void visitPrimitiveSymbolic(PrimitiveSymbolic s) {
			final char type = s.getType();
			this.putSymbol(type, s.toString());
		}

		@Override
		public void visitTerm(Term x) {
			final char type = x.getType();
			this.putSymbol(type, x.toString());
		}
		
		private void putSymbol(char type, String symbol) {
			final String z3VarName = symbol.substring(1, symbol.length() -1);
			if (this.declaredSymbols.contains(z3VarName)) {
				// does nothing
			} else {
				this.declaredSymbols.add(z3VarName);
				this.decl.append(" (declare-fun " + z3VarName + " () " + Z3PrimitiveType(type) + ")");
				nSymCurrent = nSymCurrent + 1;
				nTotalSym = nTotalSym + 1;
			}
			this.clauseStack.push(z3VarName);
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void quit() 
	throws ExternalProtocolInterfaceException, IOException {
		this.working = false;
		this.solverOut.close();
		try {
			if (this.solver.waitFor() != 0) {
				throw new ExternalProtocolInterfaceException();
			}
		} catch (InterruptedException e) {
			throw new ExternalProtocolInterfaceException();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void fail() {
		this.working = false;
		this.solver.destroy();
	}
}
