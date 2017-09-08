package jbse.dec;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Map;

import jbse.bc.ClassHierarchy;
import jbse.common.Type;
import jbse.common.exc.UnexpectedInternalException;
import jbse.dec.exc.ExternalProtocolInterfaceException;
import jbse.dec.exc.NoModelException;
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
import jbse.val.exc.InvalidOperandException;
import jbse.val.exc.InvalidTypeException;

/**
 * {@link DecisionProcedureExternalInterface} to the CVC3 SMT solver.
 * 
 * @author Pietro Braione
 */
class DecisionProcedureExternalInterfaceCVC3 extends DecisionProcedureExternalInterface {
	private static final String LINE_SEP = System.getProperty("line.separator");
	private static final String PROMPT = "CVC> ";
	private static final String PROLOGUE = 
		"J_long : TYPE = SUBTYPE ( LAMBDA (x : INT) : x >= -9223372036854775808 AND x <= 9223372036854775807 ); " +
		"J_int : TYPE = SUBTYPE( LAMBDA (x : J_long) : x >= -2147483648 AND x <= 2147483647 ); " +
		"J_char : TYPE = SUBTYPE ( LAMBDA (x : J_int) : x >= 0 AND x <= 65535 ); " +
		"J_short : TYPE = SUBTYPE( LAMBDA (x : J_int) : x >= -32768 AND x <= 32767 ); " +
		"J_byte : TYPE = SUBTYPE( LAMBDA (x : J_short) : x >= -128 AND x <= 127 ); " +
		"J_boolean : TYPE = SUBTYPE ( LAMBDA (x : J_byte) : x >= 0 AND x <= 1 ); " +
		"J_double : TYPE = SUBTYPE ( LAMBDA (x : REAL) : x >= -((2 - 2^(-52)) * 2^1023) AND x <= ((2 - 2^(-52)) * 2^1023) ); " +
		"J_float : TYPE = SUBTYPE ( LAMBDA (x : J_double) : x >= -((2 - 2^(-23)) * 2^127) AND x <= ((2 - 2^(-23)) * 2^127) ); " + 
		"J2I : J_long -> J_int ; ASSERT FORALL (x : J_long): EXISTS (i : INT): (x - 2147483648) - 4294967296 * i + 2147483648 = J2I(x); " +
		"I2C : J_int -> J_char ; ASSERT FORALL (x : J_int): EXISTS (i : INT): x - 65536 * i = I2C(x); " +
		"I2S : J_int -> J_short ; ASSERT FORALL (x : J_int): EXISTS (i : INT): (x - 32768) - 65536 * i + 32768 = I2S(x); " +
		"I2B : J_int -> J_byte ; ASSERT FORALL (x : J_int): EXISTS (i : INT): (x - 128) - 256 * i + 128 = I2B(x); " +
		"D2J : J_double -> J_long ; ASSERT FORALL (x : J_double): (x < -9223372036854775808 AND D2J(x) = -9223372036854775808) OR (x > 9223372036854775807 AND D2J(x) = 9223372036854775807) OR (x >= -9223372036854775808 AND x <= 9223372036854775807 AND ((x >= 0 AND 0 <= x - D2J(x) AND x - D2J(x) < 1) OR (x < 0 AND 0 <= D2J(x) - x AND D2J(x) - x < 1)));" +
		"D2I : J_double -> J_int ; ASSERT FORALL (x : J_double): (x < -2147483648 AND D2I(x) = -2147483648) OR (x > 2147483647 AND D2I(x) = 2147483647) OR (x >= -2147483648 AND x <= 2147483647 AND ((x >= 0 AND 0 <= x - D2I(x) AND x - D2I(x) < 1) OR (x < 0 AND 0 <= D2I(x) - x AND D2I(x) - x < 1)));" +
		"D2F : J_double -> J_float ; ASSERT FORALL (x : J_double): (x < -((2 - 2^(-23)) * 2^127) AND D2F(x) = -((2 - 2^(-23)) * 2^127)) OR (x > ((2 - 2^(-23)) * 2^127) AND D2F(x) = ((2 - 2^(-23)) * 2^127)) OR (x >= -((2 - 2^(-23)) * 2^127) AND x <= ((2 - 2^(-23)) * 2^127) AND D2F(x) = x);" +
		"F2J : J_float -> J_long ; ASSERT FORALL (x : J_float): F2J(x) = D2J(x);" +
		"F2I : J_float -> J_int ; ASSERT FORALL (x : J_float): F2I(x) = D2I(x);" +
		LINE_SEP;
	private static final String PUSH = "PUSH;" + LINE_SEP;
	private static final String POP = "POP;" + LINE_SEP;
	
	private final CalculatorRewriting calc;
    private final ExpressionMangler m;
	private boolean working;
	private Process cvc3;
	private BufferedReader cvc3In;
	private BufferedWriter cvc3Out;
	private boolean hasCurrentClause;
	private Primitive currentClause;
	private Primitive context;

	public DecisionProcedureExternalInterfaceCVC3(CalculatorRewriting calc, String path) 
	throws IOException, ExternalProtocolInterfaceException {
		this.calc = calc;
		this.m = new ExpressionMangler("X", "", calc);
		this.working = true;
		try {
			this.cvc3 = Runtime.getRuntime().exec(path + "cvc3 +interactive -stimeout 10"); 
			this.cvc3In = new BufferedReader(new InputStreamReader(this.cvc3.getInputStream()));
			this.cvc3Out = new BufferedWriter(new OutputStreamWriter(this.cvc3.getOutputStream()));
			this.readPrompt();
			this.cvc3Out.write(PROLOGUE);
			this.cvc3Out.flush();
			this.readPrompt();
			this.clear();
		} catch (IOException e) {
			this.working = false;
			throw e;
		}
	}

	private void readPrompt() throws IOException {
		final char[] promptArray = PROMPT.toCharArray();
		final int promptSize = promptArray.length;
		int i = 0;
		while (true) {
			int c = this.cvc3In.read();
			if (c == promptArray[i]) {
				++i;
				if (i == promptSize) {
					return;
				}
			} else {
				i = 0;
			}
		}
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
		this.hasCurrentClause = true;
		this.currentClause = cond;
	}

	@Override
	public void sendClauseAssumeAliases(ReferenceSymbolic r, long heapPos, Objekt o)
	throws ExternalProtocolInterfaceException {
		if (this.hasCurrentClause) {
			throw new ExternalProtocolInterfaceException("Attempted to send a clause when a current clause already exists.");
		}
		//does nothing, this decision procedure works only for numbers
		this.hasCurrentClause = true;
	}

	@Override
	public void sendClauseAssumeExpands(ReferenceSymbolic r, String className) 
	throws ExternalProtocolInterfaceException {
		if (this.hasCurrentClause) {
			throw new ExternalProtocolInterfaceException("Attempted to send a clause when a current clause already exists.");
		}
		//does nothing, this decision procedure works only for numbers
		this.hasCurrentClause = true;
	}

	@Override
	public void sendClauseAssumeNull(ReferenceSymbolic r) 
	throws ExternalProtocolInterfaceException {
		if (this.hasCurrentClause) {
			throw new ExternalProtocolInterfaceException("Attempted to send a clause when a current clause already exists.");
		}
		//does nothing, this decision procedure works only for numbers		
		this.hasCurrentClause = true;
	}

	@Override
	public void sendClauseAssumeClassInitialized(String className) 
	throws ExternalProtocolInterfaceException {
		if (this.hasCurrentClause) {
			throw new ExternalProtocolInterfaceException("Attempted to send a clause when a current clause already exists.");
		}
		//does nothing, this decision procedure works only for numbers
		this.hasCurrentClause = true;
	}

	@Override
	public void sendClauseAssumeClassNotInitialized(String className) 
	throws ExternalProtocolInterfaceException {
		if (this.hasCurrentClause) {
			throw new ExternalProtocolInterfaceException("Attempted to send a clause when a current clause already exists.");
		}
		//does nothing, this decision procedure works only for numbers
		this.hasCurrentClause = true;
	}

	@Override
	public void retractClause() 
	throws ExternalProtocolInterfaceException {
		if (!this.hasCurrentClause) {
			throw new ExternalProtocolInterfaceException("Attempted to retract clause with no current clause.");
		}
		this.hasCurrentClause = false;
		this.currentClause = this.calc.valBoolean(true);
	}

	private static final String OTHER = "";

	private static String CVC3Op(Operator operator) {
		if (operator == Operator.ADD)      return "+";
		else if (operator == Operator.SUB) return "-";
		else if (operator == Operator.MUL) return "*";
		else if (operator == Operator.DIV) return "/";
		else if (operator == Operator.NEG) return "-";
		else if (operator == Operator.LT)  return "<";
		else if (operator == Operator.LE)  return "<=";
		else if (operator == Operator.EQ)  return "=";
		else if (operator == Operator.NE)  return "/=";
		else if (operator == Operator.GE)  return ">=";
		else if (operator == Operator.GT)  return ">";
		else if (operator == Operator.AND) return "AND";
		else if (operator == Operator.OR)  return "OR";
		else if (operator == Operator.NOT) return "NOT";
		else return OTHER;
	}

	private static String CVC3PrimitiveType(char type) {
		if (type == Type.BYTE) {
			return "J_byte"; 
		} else if (type == Type.SHORT) {
			return "J_short"; 
		} else if (type == Type.INT) {
			return "J_int";
		} else if (type == Type.LONG) {
			return "J_long";
		} else if (type == Type.FLOAT) {
			return "J_float";
		} else if (type == Type.DOUBLE) {
			return "J_double";
		} else if (type == Type.BOOLEAN) {
			return "J_boolean";
		} else if (type == Type.CHAR) {
			return "J_char";
		} else {
			//this should be unreachable!
			return OTHER;
		}
	}

	private static String CVC3Double(String s) {
		String[] parts = s.split("E");
		String[] partsMantissa = parts[0].split("\\."); 
		long exponent;
		if (s.length() == 2) {
			exponent = Long.parseLong(parts[1]);
		} else {
			exponent = 0L;
		}
		if (partsMantissa.length == 2) {
			exponent -= partsMantissa[1].length();
		}
		String retVal = "(";
		retVal += partsMantissa[0] + (partsMantissa.length == 2 ? partsMantissa[1] : "");
		retVal += ((exponent == 0) ? "" : "* (10 ^ (" + exponent + "))");
		retVal += ")";
		return retVal;
	}


	/**
	 * A {@link PrimitiveVisitor} which builds a CVC3 string 
	 * representing the expression.
	 * 
	 * @author Pietro Braione
	 */
	private class CVC3ExpressionVisitor implements PrimitiveVisitor {
		private boolean isBooleanExpression = true;
		private HashSet<String> declaredVars = new HashSet<String>();
		private StringBuilder decl = new StringBuilder();
		private ArrayDeque<String> clauseStack = new ArrayDeque<String>();

		public String popClause() { return this.clauseStack.pop(); }
		public String getDecl() { return this.decl.toString(); }

		public CVC3ExpressionVisitor() { }
		public CVC3ExpressionVisitor(CVC3ExpressionVisitor v, boolean isBooleanExpression) {
			this.isBooleanExpression = isBooleanExpression;
			this.declaredVars = v.declaredVars;
			this.decl = v.decl;
			this.clauseStack = v.clauseStack;
		}

		@Override
		public void visitFunctionApplication(FunctionApplication x) throws Exception {
			final String fun = x.getOperator();

			if (fun.equals(FunctionApplication.POW)) {
				for (Primitive p : x.getArgs()) { //they are two! 
					p.accept(new CVC3ExpressionVisitor(this, false));
				}
				this.clauseStack.push("( (" + this.clauseStack.pop() + ") ^ (" + this.clauseStack.pop() + ") )");
			} else {
				//the whole application is treated as an uninterpreted 
				//symbolic value
				char mytype = x.getType();
				if (Type.isPrimitive(mytype)) {
					m.mangle(x).accept(this);
				} else {
					throw new ExternalProtocolInterfaceException("wrong function return type");
				}
			}
		}	
		
		@Override
		public void visitExpression(Expression e) throws Exception {
			final Operator operator = e.getOperator();
			final String operatorCVC3 = CVC3Op(operator);
			final boolean isBooleanOperator = operator.acceptsBoolean();

			if (operator.returnsBoolean() == this.isBooleanExpression) {
				//the expression is well-formed.

				//1 - The operator does not correspond to a CVC3 operator, 
				//    but it can be implemented in terms of these: builds a 
				//    suitable expression and visits it
				if (operator == Operator.REM) {
					final Primitive firstOp = e.getFirstOperand();
					final Primitive secondOp = e.getSecondOperand();
					final Primitive val = firstOp.sub(firstOp.div(secondOp).mul(secondOp));
					val.accept(this);
				} else if (operator == Operator.SHL) {
					final Primitive firstOp = e.getFirstOperand();
					final Primitive secondOp = e.getSecondOperand();
					final Primitive val = firstOp.mul(calc.applyFunction(firstOp.getType(), FunctionApplication.POW, calc.valInt(2), secondOp));
					val.accept(this);
				} else if (operator == Operator.SHR) {
					Primitive firstOp = e.getFirstOperand();
					Primitive secondOp = e.getSecondOperand();
					Primitive val = firstOp.div(calc.applyFunction(firstOp.getType(), FunctionApplication.POW, calc.valInt(2), secondOp));
					val.accept(new CVC3ExpressionVisitor(this, false));
					//2 - the operator is not supported: mangles the subexpressions into 
					//    a symbolic value and sends it (unsupported operators are:
					//    Operator.USHR, Operator.ANDBW, Operator.ORBW, Operator.XOR)
				} else if (operatorCVC3.equals(OTHER)) {
					m.mangle(e).accept(this);
					//3 - The operator corresponds to a CVC3 operator:
					//    builds the expression
				} else { 
					if (e.isUnary()) {
						e.getOperand().accept(new CVC3ExpressionVisitor(this, isBooleanOperator));
						this.clauseStack.push("( " + operatorCVC3 + this.clauseStack.pop() + " )");
					} else {
						e.getSecondOperand().accept(new CVC3ExpressionVisitor(this, isBooleanOperator));
						e.getFirstOperand().accept(new CVC3ExpressionVisitor(this, isBooleanOperator));
						this.clauseStack.push("( (" + this.clauseStack.pop() + ")" + operatorCVC3 + "(" + this.clauseStack.pop() + ") )");
					}
				}
			} else {
				//the expression is ill-formed
				throw new UnexpectedInternalException("error while parsing an expression for CVC3: " + e.toString());
			}
		}

		@Override
		public void visitNarrowingConversion(NarrowingConversion x)
		throws Exception {
			final Primitive arg = x.getArg();
			final char from = arg.getType();
			final char to = x.getType();
			arg.accept(this);
			this.clauseStack.push("(" + from + "2" + to + "(" + this.clauseStack.pop() + ") )");
		}

		@Override
		public void visitWideningConversion(WideningConversion x)
		throws Exception {
			x.getArg().accept(this); //ints are automatically injected into reals 
		}

		@Override
		public void visitPrimitiveSymbolic(PrimitiveSymbolic s) {
			this.putSymbol(s.getType(), s.toString());
		}

		@Override
		public void visitTerm(Term x) {
			this.putSymbol(x.getType(), x.toString());
		}

		private void putSymbol(char type, String symbol) {
			String cvc3VarName = (symbol.charAt(0) == '{' ? symbol.substring(1, symbol.length() - 1) : symbol);
			if (this.declaredVars.contains(cvc3VarName)) {
				//does nothing
			} else {
				this.declaredVars.add(cvc3VarName);
				this.decl.append(cvc3VarName + " : " + CVC3PrimitiveType(type) + "; ");
			}
			this.clauseStack.push(cvc3VarName);
		}

		@Override
		public void visitSimplex(Simplex x) {
			Object obj = x.getActualValue();
			if (working) {
				char mytype = x.getType();
				if (mytype == Type.BYTE ||
						mytype == Type.SHORT ||
						mytype == Type.INT ||
						mytype == Type.LONG ||
						mytype == Type.CHAR) {
					this.clauseStack.push(obj.toString());
				} else if (mytype == Type.FLOAT ||
						mytype == Type.DOUBLE) {
					this.clauseStack.push(CVC3Double(obj.toString()));
				} else if (mytype == Type.BOOLEAN) {
					if ((Boolean) obj) {
						if (this.isBooleanExpression) {
							this.clauseStack.push("TRUE");
						} else {
							this.clauseStack.push("1");
						}
					} else {
						if (this.isBooleanExpression) {
							this.clauseStack.push("FALSE");
						} else {
							this.clauseStack.push("0");
						}
					}
				}
			}
		}

		@Override
		public void visitAny(Any x) 
		throws ExternalProtocolInterfaceException {
			throw new ExternalProtocolInterfaceException("values of type Any should not reach CVC3");
		}
	}

	@Override
	public boolean checkSat(ClassHierarchy hier, boolean value) 		
	throws ExternalProtocolInterfaceException, IOException {
		if (!this.hasCurrentClause) {
			throw new ExternalProtocolInterfaceException("Attempted to check entailment with no current clause.");
		}
		//visits the context and the current expression and
		//builds the query
		final CVC3ExpressionVisitor v = new CVC3ExpressionVisitor();
		try {
			this.context.accept(v);
			if (value) {
				this.currentClause.accept(v);
			} else {
				this.currentClause.not().accept(v);
			}
		} catch (ExternalProtocolInterfaceException | IOException | RuntimeException e) {
			throw e;
		} catch (Exception e) {
			//this should never happen
			throw new UnexpectedInternalException(e);
		}
		final String decl = v.getDecl();
		final String cnd = v.popClause();
		final String ctx = v.popClause();
		final String query = decl + "ASSERT " + ctx + "; QUERY " + cnd + ";" + LINE_SEP;

		try {
	        //queries CVC3
			this.cvc3Out.write(PUSH);
			this.cvc3Out.flush();
			System.err.println(PUSH);
			this.readPrompt();
			this.cvc3Out.write(query);
			this.cvc3Out.flush();
			System.err.println(query);
	        final String ans = this.cvc3In.readLine();
			if (ans == null) {
				throw new IOException("failed read of CVC3 output");
			}
			System.err.println(ans);
			this.readPrompt();
			this.cvc3Out.write(POP);
			this.cvc3Out.flush();
			System.err.println(POP);
			this.readPrompt();

			//returns the result
			return ans.equals("Valid.");
        } catch (IOException e) {
            this.working = false;
            throw e;
        }
	}
    
    @Override
    public Map<PrimitiveSymbolic, Simplex> getModel() 
    throws NoModelException, ExternalProtocolInterfaceException, IOException {
        throw new NoModelException();
    }

	@Override
	public void pushAssumption(boolean value) 
	throws ExternalProtocolInterfaceException, IOException {
		if (!this.hasCurrentClause) {
			throw new ExternalProtocolInterfaceException("Attempted to push assumption with no current clause.");
		}
		try {
			if (value) {
				this.context = this.context.and(this.currentClause);
			} else {
				this.context = this.context.and(this.currentClause.not());
			}
		} catch (InvalidOperandException | InvalidTypeException e) {
			//this should never happen
			throw new UnexpectedInternalException(e);
		}
	}

	@Override
	public void clear() throws ExternalProtocolInterfaceException {
		this.hasCurrentClause = false;
		this.currentClause = this.calc.valBoolean(true);
		this.context = this.calc.valBoolean(true);
	}

	@Override
	public void quit() 
	throws ExternalProtocolInterfaceException, IOException {
		this.working = false;
		this.cvc3Out.close();
		try {
			if (this.cvc3.waitFor() != 0) {
				throw new ExternalProtocolInterfaceException("the CVC3 process ended with an error code");
			}
		} catch (InterruptedException e) {
			throw new ExternalProtocolInterfaceException(e);
		}
	}

	@Override
	public void fail() {
		this.working = false;
		this.cvc3.destroy();
	}
}
