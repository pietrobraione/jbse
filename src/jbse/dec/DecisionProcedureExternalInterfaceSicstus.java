package jbse.dec;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

import jbse.bc.ClassHierarchy;
import jbse.common.Type;
import jbse.common.exc.UnexpectedInternalException;
import jbse.dec.exc.ExternalProtocolInterfaceException;
import jbse.mem.Array;
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

import jdd.bdd.BDD;
import se.sics.prologbeans.Bindings;
import se.sics.prologbeans.IllegalCharacterSetException;
import se.sics.prologbeans.PrologSession;
import se.sics.prologbeans.QueryAnswer;

class DecisionProcedureExternalInterfaceSicstus extends DecisionProcedureExternalInterface {
	private static class SicstusServer implements Runnable {
	    private final static String SERVER_PL = 
        "[user].\n" +
        ":- module(server,[main/0,server_started_listener/0,server_shutdown/0,my_predicate/2]).\n" +
        ":- use_module(library(prologbeans)).\n" +
        ":- use_module(library(clpq)).\n"+
        ":- use_module(library(charsio), [read_from_chars/2]).\n" +
 
        // Register acceptable queries and start the server (using default port)
        "main :-\n" +
        "  register_query(do(C), my_predicate(C)),\n" +
        "  register_query(shutdown, server_shutdown),\n" +
        "  register_event_listener(server_started, server_started_listener),\n" +
        "  start([port(_Port)]),\n" +
        "  halt.\n"+

        // The listener for the start event emits the port number
        "server_started_listener :-\n" +
        "  get_server_property(port(Port)),\n"+
        "  format(user_error, 'port:~w~n', [Port]),\n"+
        "  flush_output(user_error).\n" + 
 
        // Shutting down the server
        "server_shutdown :- shutdown(now).\n" +
        
        // The query processor receives a list of characters
        // and converts them into an expression to be evaluated
        "my_predicate(Chars) :-\n" +
        "  read_from_chars(Chars, X),\n" +
        "  X.\n" +
        
        //starts
        ":- main.\n";

	    private Thread thread = null;
		private final String sicstusPath;
		private boolean ready = false;
		private int port = -1;
		private Process process = null;
		private Exception e = null;
		private BufferedWriter in;
		private BufferedReader err;

		public SicstusServer(String sicstusPath) {
			this.sicstusPath = sicstusPath;
	        this.thread = new Thread(this);
	        this.thread.start();
		}

		@Override
		public void run() {
			try {
			    synchronized(this) {
			        //launches sicstus
			        final String command = this.sicstusPath + "sicstus -f";
			        this.process = Runtime.getRuntime().exec(command);

			        //takes its stdin and stderr
			        this.in = new BufferedWriter(new OutputStreamWriter(this.process.getOutputStream()));
			        this.err = new BufferedReader(new InputStreamReader(this.process.getErrorStream()));

			        //sends the server code to sicstus
			        in.write(SERVER_PL); 
			        in.flush();

			        //scans stderr until it finds the port number and stores it 
			        String line;
			        while ((line = err.readLine()) != null) {
			            if (line.length() > 0 && line.startsWith("port:")) {
			                port = Integer.parseInt(line.substring(5)); // e.g, port:4711
			                break;
			            }
			        }
                }
			} catch (Exception e) {
                synchronized(this) {
                    this.e = e;
                    this.port = -2;
                }
			} finally {
				synchronized(this) {
					this.ready = true;
					notify();
				}
			}
		}

		public synchronized int getPort(int timeout) throws InterruptedException {
			if (!this.ready) {
				wait(timeout);
			}
			return port;
		}

		public synchronized Exception getException(int timeout) throws InterruptedException {
			if (!this.ready) {
				wait(timeout);
			}
			return this.e;
		}

		public synchronized void shutdown(int timeout) throws InterruptedException {
		    try {
		        if (this.in != null) {
		            this.in.close();
		        }
		    } catch (IOException e) {
		        //does nothing
		    }
            try {
                if (this.err != null) {
                    this.err.close();
                }
            } catch (IOException e) {
                //does nothing
            }
            try {
                if (this.process.getInputStream() != null) {
                    this.process.getInputStream().close();
                }
            } catch (IOException e) {
                //does nothing
            }
			if (!this.ready) {
				wait(timeout);
			}
			if (this.process != null) {
                this.process.waitFor();
                this.process.destroy();
			    this.process = null;
			}
			if (this.thread != null) {
			    this.thread.interrupt();
			    this.thread.join();
			    this.thread = null;
			}
		}
	}
	
	private static final int TIMEOUT_INIT = 5000;
	private static final int TIMEOUT_SESSION = 0; //infinite;
	private static final int BDD_SIZE = 1000;
	private static final String INDEX_SICSTUS = "I";
	
	private final CalculatorRewriting calc;
    private final ExpressionMangler m;
	private final SicstusServer sicstusServer;
	private final PrologSession session;
	private boolean working;
	private Primitive assumptions;
	private Primitive currentClause = null;
	private boolean hasCurrentClause = false;

	public DecisionProcedureExternalInterfaceSicstus(CalculatorRewriting calc, String sicstusPath) 
	throws ExternalProtocolInterfaceException, IOException {
		this.calc = calc;
		this.m = new ExpressionMangler("X", "", calc); //a format for mangled names compatible with Sicstus
		this.sicstusServer = new SicstusServer(sicstusPath); 

		// Get the port from the SICStus process (and fail if port is an error value)
		int port;
		try {
			port = this.sicstusServer.getPort(TIMEOUT_INIT);
			if (port > 0) {
				this.session = new PrologSession();
				this.session.setPort(port);
				this.session.setTimeout(TIMEOUT_SESSION);
				this.working = true;
			} else {
				this.session = null;
				this.working = false;
				final Exception e = this.sicstusServer.getException(TIMEOUT_INIT);
				throw new ExternalProtocolInterfaceException(e);
			}
		} catch (InterruptedException e) {
			throw new ExternalProtocolInterfaceException(e);
		}
		this.assumptions = this.calc.valBoolean(true);
	}

	@Override
	public boolean isWorking() {
		return this.working;
	}

	@Override
	public void sendClauseAssume(Primitive cond) throws ExternalProtocolInterfaceException {
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
	public void sendClauseAssumeAliases(ReferenceSymbolic r, long heapPos, Objekt o) throws ExternalProtocolInterfaceException {
		if (this.hasCurrentClause) {
			throw new ExternalProtocolInterfaceException("Attempted to send a clause when a current clause already exists.");
		}
		//does nothing, this decision procedure works only for numbers
		this.hasCurrentClause = true;
        this.currentClause = null;
	}

	@Override
	public void sendClauseAssumeExpands(ReferenceSymbolic r, String className) throws ExternalProtocolInterfaceException {
		if (this.hasCurrentClause) {
			throw new ExternalProtocolInterfaceException("Attempted to send a clause when a current clause already exists.");
		}
		//does nothing, this decision procedure works only for numbers
		this.hasCurrentClause = true;
		this.currentClause = null;
	}

	@Override
	public void sendClauseAssumeNull(ReferenceSymbolic r) throws ExternalProtocolInterfaceException {
		if (this.hasCurrentClause) {
			throw new ExternalProtocolInterfaceException("Attempted to send a clause when a current clause already exists.");
		}
		//does nothing, this decision procedure works only for numbers		
		this.hasCurrentClause = true;
        this.currentClause = null;
	}

	@Override
	public void sendClauseAssumeClassInitialized(String className) throws ExternalProtocolInterfaceException {
		if (this.hasCurrentClause) {
			throw new ExternalProtocolInterfaceException("Attempted to send a clause when a current clause already exists.");
		}
		//does nothing, this decision procedure works only for numbers
		this.hasCurrentClause = true;
        this.currentClause = null;
	}

	@Override
	public void sendClauseAssumeClassNotInitialized(String className) throws ExternalProtocolInterfaceException {
		if (this.hasCurrentClause) {
			throw new ExternalProtocolInterfaceException("Attempted to send a clause when a current clause already exists.");
		}
		//does nothing, this decision procedure works only for numbers
		this.hasCurrentClause = true;
        this.currentClause = null;
	}

	@Override
	public void retractClause() throws ExternalProtocolInterfaceException {
		if (!this.hasCurrentClause) {
			throw new ExternalProtocolInterfaceException("Attempted to retract clause with no current clause.");
		}
        this.hasCurrentClause = false;
		this.currentClause = null;
	}

	@Override
	public boolean checkSat(ClassHierarchy hier, boolean value)
	throws ExternalProtocolInterfaceException, IOException {
		if (!this.hasCurrentClause) {
			throw new ExternalProtocolInterfaceException("Attempted to check entailment with no current clause.");
		}
		//builds the predicate to check and parses it
		//TODO if this.condition == null (clause outside theory) we may possibly return true
		final Primitive conditionTmp = (this.currentClause == null ? this.calc.valBoolean(true) : this.currentClause);
		final Primitive predicateToCheck;
		try {
			predicateToCheck = this.assumptions.and(value ? conditionTmp : conditionTmp.not());
		} catch (InvalidOperandException | InvalidTypeException e) {
			//this should never happen
			throw new UnexpectedInternalException(e);
		}

		final SicstusParser parser = new SicstusParser(predicateToCheck);

		//gets the "true" and "false" bdd constants
		final int bddFalse = parser.bdd.getZero();
		final int bddTrue = parser.bdd.getOne();
		
		//iterates through the bdd satisfying 
		//solutions and checks the associated
		//predicate via Sicstus, until there
		//are no more
		int predicate = parser.predicate;
		int sol = parser.bdd.oneSat(predicate);
		parser.bdd.ref(sol);  //TODO is it necessary?
		while (sol != bddFalse && sol != bddTrue) {
			int[] solDIMACS = toDIMACS(oneSatWithDontCare(parser.bdd, predicate));
			if (hasSolution(solDIMACS, parser.atomicPredicatesPositive, parser.atomicPredicatesNegative, parser.getIntegerVariables())) {
				return true;
			}
			final int predicateNew = parser.bdd.and(predicate, parser.bdd.not(sol));
			parser.bdd.ref(predicateNew);
			parser.bdd.deref(predicate);
            parser.bdd.deref(sol);
			predicate = predicateNew;
			sol = parser.bdd.oneSat(predicate);
	        parser.bdd.ref(sol);
		}
		if (sol == bddFalse) {
			return false;
		} else { //(sol == bddTrue)
			return true;
		}
	}

	/**
	 * Fixes a bug in {@link BDD#oneSat(int, int[])}
	 * (does not set don't care to -1 in result).
	 * 
	 * @param bdd a {@link BDD}
	 * @param predicate an {@code int}, a predicate (node)
	 *        in {@code bdd}
	 * @return a satisfying assignment.
	 * @see BDD#oneSat(int, int[]) 
	 */
	private int[] oneSatWithDontCare(BDD bdd, int predicate) {
		final int[] retVal = new int[bdd.numberOfVariables()];
		Arrays.fill(retVal, -1);
		final int bddFalse = bdd.getZero();
		final int bddTrue = bdd.getOne();
		int t = predicate;
		while (t != bddFalse && t != bddTrue) {
			final int lo = bdd.getLow(t);
			final int hi = bdd.getHigh(t);
			if (lo == bddFalse) {
				retVal[bdd.getVar(t)] = 1;
				t = hi;
			} else {
				retVal[bdd.getVar(t)] = 0;
				t = lo;
			}
		}
		return retVal;
	}
	
	/**
	 * Converts a JDD solution to a DIMACS-compatible
	 * format which is more suitable for querying sicstus. 
	 * 
	 * @param solJDD a solution as returned by JDD,
	 *        i.e., a {@code int[]} where the i-th member
	 *        represents the three-valued truth value (1 = true, 0 = 
	 *        false, other = don't care) of the i-th variable
	 *        in the bdd.
	 * @return a solution in DIMACS format, i.e., a {@code int[]} 
	 *         whose members have value either k or -k if 
	 *         the k-th variable has, respectively, true or 
	 *         false value. Variables whose truth value 
	 *         is don't care are not added to the solution.
	 *         Variable count starts from 1.
	 */
	private int[] toDIMACS(int[] solJDD) {
		int size = solJDD.length;
		for (int i : solJDD) {
			if (i != 0 && i != 1) {
				--size;
			}
		}
		int[] retVal = new int[size];
		int var = 1;
		int pos = 0;
		for (int i = 0; i < solJDD.length; ++i) {
			if (solJDD[i] == 1) {
				retVal[pos] = var;
				++pos;
			} else if (solJDD[i] == 0) {
				retVal[pos] = -var;
				++pos;
			} //ignore DONTCARE
			++var;
		}
		return retVal;
	}

	/**
	 * Queries sicstus to determine whether a bdd solution is satisfiable
	 * in the clp(q) theory. 
	 * 
	 * @param model an assignment (in DIMACS format) of the truth value of the atomic predicates in {@code atomicPredicatesPositive}.
	 * @param atomicPredicatesPositive an {@link ArrayList}{@code <}{@link String}{@code >}, containing a list of atomic predicates
	 *        in sicstus clp(q) format.
	 * @param atomicPredicatesNegative an {@link ArrayList}{@code <}{@link String}{@code >}, containing a list of atomic predicates
	 *        in sicstus clp(q) format, where the i-th element is the negation of the i-th element in {@code atomicPredicatesPositive}.
	 * @param integerVariables a {@link String} containing the list of the variables mentioned in the atomic predicates contained
	 *        in {@code atomicPredicatesPositive} (and thus {@code atomicPredicatesNegative}) that have integer type.
	 * @return {@code true} iff the logical-and of all the constraints in {@code atomicPredicatesPositive} at indices {@code i - 1} for {@code i > 0 && 
	 *         i == model[k]}, and all the constraints in {@code atomicPredicatesPositive} at indices {@code -j - 1} for  && {@code j < 0 && 
	 *         j == model[w]}, for some k and w, has a solution.
	 * @throws IOException
	 */
	private boolean hasSolution(int[] model, ArrayList<String> atomicPredicatesPositive, ArrayList<String> atomicPredicatesNegative, String integerVariables) 
	throws IOException {
		try {
			final StringBuilder query = new StringBuilder("{");
			boolean firstDone = false;
			for (int v : model) {
				if (firstDone) {
					query.append(",");
				} else {
					firstDone = true;
				}
				query.append((v > 0 ? atomicPredicatesPositive.get(v - 1) : atomicPredicatesNegative.get((-v) - 1)));
			}
			query.append("}, bb_inf([" + integerVariables + "],0,_Inf).");
			final Bindings bindings = new Bindings().bind("C", query.toString());
			final QueryAnswer answer = this.session.executeQuery("do(C)", bindings);
			if (answer.isError()) {
				fail();
				throw new UnexpectedInternalException(answer.toString());
			}
			final boolean sat = !answer.queryFailed();
			return sat;
		} catch (IllegalCharacterSetException e) {
			fail();
			throw new UnexpectedInternalException(e);
		}
	}

	@Override
	public void pushAssumption(boolean value)
	throws ExternalProtocolInterfaceException, IOException {
		if (!this.hasCurrentClause) {
			throw new ExternalProtocolInterfaceException("Attempted to push assumption with no current clause.");
		}
		this.hasCurrentClause = false;
		if (this.currentClause == null) {
			return;
		}
		try {
			if (value) {
				this.assumptions = this.assumptions.and(this.currentClause);
			} else {
				this.assumptions = this.assumptions.and(this.currentClause.not());
			}
		} catch (InvalidOperandException | InvalidTypeException e) {
			//this should never happen
			throw new UnexpectedInternalException(e);
		}
	}

	@Override
	public void clear() {
		this.assumptions = this.calc.valBoolean(true);
        this.hasCurrentClause = false;
		this.currentClause = null;
	}

	@Override
	public void quit() throws ExternalProtocolInterfaceException {
		try {
            this.session.executeQuery("shutdown");
			this.sicstusServer.shutdown(TIMEOUT_INIT);
		} catch (InterruptedException | IOException | IllegalCharacterSetException e) {
			throw new ExternalProtocolInterfaceException(e);
		}
	}

	@Override
	public void fail() {
		try {
		    this.session.executeQuery("shutdown");
			this.sicstusServer.shutdown(TIMEOUT_INIT);
		} catch (InterruptedException | IOException | IllegalCharacterSetException e) {
			//nothing
		}
	}
	
	/**
	 * Parses a {@link Primitive} and yields its boolean structure and
	 * the list of its atomic predicates (and their negations).
	 * 
	 * @author Pietro Braione
	 *
	 */
	private class SicstusParser implements PrimitiveVisitor {
		final private SicstusParserAtomicPredicates parserAtoms = new SicstusParserAtomicPredicates();
		final BDD bdd = new BDD(BDD_SIZE, BDD_SIZE);
		int predicate;
		final ArrayList<String> atomicPredicatesPositive = new ArrayList<String>();
		final ArrayList<String> atomicPredicatesNegative = new ArrayList<String>();
		
		public SicstusParser(Primitive p) throws ExternalProtocolInterfaceException { 
			try {
				p.accept(this);
			} catch (ExternalProtocolInterfaceException | RuntimeException e) {
				throw e;
			} catch (Exception e) {
				//this should never happen
				throw new UnexpectedInternalException(e);
			}
			
			//now translates mangled terms; we exploit the fact that
			//arraylists can be grown while scanned
			
			//first narrowing...
			while (!this.parserAtoms.narrowedValues.isEmpty()) {
				final ArrayList<Term> narrowedValues = this.parserAtoms.narrowedValues;
				this.parserAtoms.narrowedValues = new ArrayList<Term>(); //TODO put this reset in parserAtoms!
				for (Term t : narrowedValues) {
                    final int predicatePrev = this.predicate;
					try {
	                    final NarrowingConversion q = (NarrowingConversion) m.demangle(t);
	                    final Primitive arg = q.getArg();
	                    final char argType = arg.getType();
						final Primitive minusOne = calc.valInt(-1).to(argType);
						final Primitive zero = calc.valInt(0).to(argType);
						final Primitive one = calc.valInt(1).to(argType);
						final Primitive tWidened = t.to(argType);
						final Primitive constraintPos = arg.ge(zero).and(tWidened.le(arg)).and(arg.lt(tWidened.add(one)));
						final Primitive constraintNeg = arg.lt(zero).and(tWidened.add(minusOne).lt(arg)).and(arg.lt(tWidened));
						final Primitive constraint = constraintPos.or(constraintNeg);
						constraint.accept(this);
					} catch (ExternalProtocolInterfaceException | RuntimeException e) {
						throw e;
					} catch (Exception e) {
						//this should never happen
						throw new UnexpectedInternalException(e);
					}
					final int predicateNew = this.bdd.and(predicatePrev, this.predicate);
					this.bdd.ref(predicateNew);
                    this.bdd.deref(predicatePrev);
                    this.bdd.deref(this.predicate);
					this.predicate = predicateNew;
				}
			}
			
			//...then integer divisions
            while (!this.parserAtoms.divValues.isEmpty()) {
                final ArrayList<Term> divValues = this.parserAtoms.divValues;
                this.parserAtoms.divValues = new ArrayList<Term>(); //TODO put this reset in parserAtoms!
                for (Term t : divValues) {
                    final int predicatePrev = this.predicate;
                    try {
                        final Expression q = (Expression) m.demangle(t);
                        final char expType = q.getType();
                        final Primitive firstOperand = q.getFirstOperand();
                        final Primitive secondOperand = q.getSecondOperand();
                        final Primitive zero = calc.valInt(0).to(expType);
                        final Primitive one = calc.valInt(1).to(Type.DOUBLE);
                        final Primitive qWidened = firstOperand.widen(Type.DOUBLE).div(secondOperand.widen(Type.DOUBLE));
                        final Primitive constraintPos = firstOperand.mul(secondOperand).ge(zero).and(qWidened.sub(one).lt(t)).and(t.le(qWidened));
                        final Primitive constraintNeg = firstOperand.mul(secondOperand).lt(zero).and(qWidened.le(t)).and(t.lt(qWidened.add(one)));
                        final Primitive constraint = constraintPos.or(constraintNeg);
                        constraint.accept(this);
                    } catch (ExternalProtocolInterfaceException | RuntimeException e) {
                        throw e;
                    } catch (Exception e) {
                        //this should never happen
                        throw new UnexpectedInternalException(e);
                    }
                    final int predicateNew = this.bdd.and(predicatePrev, this.predicate);
                    this.bdd.ref(predicateNew);
                    this.bdd.deref(predicatePrev);
                    this.bdd.deref(this.predicate);
                    this.predicate = predicateNew;
                }
            }
		}
		
		public String getIntegerVariables() {
			return this.parserAtoms.integerVariables.toString(); //not stored here
		}
		
		@Override
		public void visitAny(Any x) throws ExternalProtocolInterfaceException {
			if (working) {
				throw new ExternalProtocolInterfaceException("Wrong symbol");
			}
		}

		@Override
		public void visitExpression(Expression e) throws Exception {
			if (working) {
				if (e.getType() == Type.BOOLEAN) {
					final Operator op = e.getOperator();
					switch (op) {
					case AND:
					case OR:
						e.getFirstOperand().accept(this);
						final int first = this.predicate;
						e.getSecondOperand().accept(this);
						final int second = this.predicate;
						if (op == Operator.AND) {
							this.predicate = this.bdd.and(first, second);
						} else {
							this.predicate = this.bdd.or(first, second);
						}
						this.bdd.ref(this.predicate);
						this.bdd.deref(first);
                        this.bdd.deref(second);
						break;
					case NOT:
						e.getOperand().accept(this);
						final int operand = this.predicate;
						this.predicate = this.bdd.not(operand);
						this.bdd.ref(this.predicate);
						this.bdd.deref(operand);
						break;
                    case NE:
                        final Primitive firstOp = e.getFirstOperand();
                        final Primitive secondOp = e.getSecondOperand();
                        //sicstus clp(q) handles < and > better than =\=
                        //so we translate A != B as (A < B) || (A > B);
                        //since it is unclear the advantage in the 
                        //noninteger case, we only do it for integers,
                        //except for the easy case var != const.
                        //Note that this trick might stress a lot the 
                        //boolean part as it makes 
                        if (Type.isPrimitiveIntegral(firstOp.getType()) && 
                            Type.isPrimitiveIntegral(secondOp.getType()) && 
                            !( (firstOp instanceof PrimitiveSymbolic && secondOp instanceof Simplex) ||
                               (firstOp instanceof Simplex && secondOp instanceof PrimitiveSymbolic))) {
                            final Primitive val = firstOp.lt(secondOp).or(firstOp.gt(secondOp));
                            val.accept(this);
                            break;
                        } //else fall through
					default:
						storeAtomicPredicate(e);
						break;
					}
				} else {
					throw new ExternalProtocolInterfaceException("wrong expression type " + e.getType());
				}
			}
		}
		
		private void storeAtomicPredicate(Primitive p) 
		throws ExternalProtocolInterfaceException {
			try {
				p.accept(this.parserAtoms);
			} catch (ExternalProtocolInterfaceException | RuntimeException e) {
				throw e;
			} catch (Exception e) {
				//this should never happen
				throw new UnexpectedInternalException(e);
			}
			this.atomicPredicatesPositive.add(parserAtoms.termPositive);
			this.atomicPredicatesNegative.add(parserAtoms.termNegative);
			this.predicate = this.bdd.createVar();
            this.bdd.ref(this.predicate);
		}

		@Override
		public void visitFunctionApplication(FunctionApplication x) 
		throws ExternalProtocolInterfaceException {
			if (working) {
				if (x.getType() == Type.BOOLEAN) {
					storeAtomicPredicate(x);
				} else {
					throw new ExternalProtocolInterfaceException("wrong function application type " + x.getType());
				}
			}
		}

		@Override
		public void visitWideningConversion(WideningConversion x) 
		throws ExternalProtocolInterfaceException {
			if (working) {
				if (x.getType() == Type.BOOLEAN) {
					storeAtomicPredicate(x);
				} else {
					throw new ExternalProtocolInterfaceException("wrong conversion type " + x.getType());
				}
			}
		}

		@Override
		public void visitNarrowingConversion(NarrowingConversion x) 
		throws ExternalProtocolInterfaceException {
			if (x.getType() == Type.BOOLEAN) {
				storeAtomicPredicate(x);
			} else {
				throw new ExternalProtocolInterfaceException("wrong conversion type " + x.getType());
			}
		}

		@Override
		public void visitPrimitiveSymbolic(PrimitiveSymbolic s)
		throws ExternalProtocolInterfaceException {
	        if (working) {
	            if (s.getType() == Type.BOOLEAN) {
	            	storeAtomicPredicate(s);
	            } else {
	                throw new ExternalProtocolInterfaceException("wrong symbol type " + s.getType());
	            }
	        }
		}

		@Override
		public void visitSimplex(Simplex x) 
		throws ExternalProtocolInterfaceException {
			if (x.getType() == Type.BOOLEAN) {
				boolean val = (Boolean) x.getActualValue();
				this.predicate = (val ? this.bdd.getOne() : this.bdd.getZero());
			} else {
				throw new ExternalProtocolInterfaceException("wrong simplex type " + x.getType());
			}
		}

		@Override
		public void visitTerm(Term x) 
		throws ExternalProtocolInterfaceException {
	        if (working) {
	            if (Type.isPrimitive(x.getType())) {
	            	storeAtomicPredicate(x);
	            } else {
	                throw new ExternalProtocolInterfaceException("wrong term type " + x.getType());
	            }
	        }
		}
	}
	
	/**
	 * Returns the Sicstus clp(q) operator which corresponds to 
	 * a Java operator.
	 * 
	 * @param operator a {@link Operator}.
	 * @return a {@link String} identifying the corresponding 
	 *         Sicstus clp(q) expression for {@code operator},
	 *         or {@code null} if the latter has no correspondance.
	 */
	private static String sicstusOperator(Operator operator) {
		switch (operator) {
		case ADD: 
			return "+";
		case SUB:
			return "-";
		case MUL:
			return "*";
		case DIV:
			return "/";
		case NEG:
			return "-";
		case EQ:
			return "=";
		case NE:
			return "=\\=";
		case LE :
			return "=<";
		case LT:
			return "<";
		case GE:
			return ">=";
		case GT:
			return ">";
		default:
			return null;	
		}
	}
	
	/**
	 * Returns the Sicstus clp(q) operator which corresponds to 
	 * the negation of a relational Java operator.
	 * 
	 * @param operator a {@link Operator}.
	 * @return a {@link String} identifying the corresponding 
	 *         Sicstus clp(q) expression for the negation of 
	 *         {@code operator}, or {@code null} if the latter 
	 *         is not a relational operator.
	 */
	private static String sicstusOperatorNegated(Operator operator) {
		switch (operator) {
		case EQ:
			return sicstusOperator(Operator.NE);
		case NE:
			return sicstusOperator(Operator.EQ);
		case LE :
			return sicstusOperator(Operator.GT);
		case LT:
			return sicstusOperator(Operator.GE);
		case GE:
			return sicstusOperator(Operator.LT);
		case GT:
			return sicstusOperator(Operator.LE);
		default:
			return null;
		}
	}
	
	/**
	 * Returns the Sicstus clp(q) function corresponding to a Java one.
	 * .
	 * @param function a {@link String}, the name of a Java mathematical 
	 *        function.
	 * @return a {@link String}, the corresponding Sicstus 
	 *         clp(q) function, or {@code null} if none exists.
	 */
	private static String sicstusFunction(String function) {
		if (function.equals(FunctionApplication.ABS)) {
			return "abs";
		} else if (function.equals(FunctionApplication.SIN)) {
			return "sin";
		} else if (function.equals(FunctionApplication.COS)) {
			return "cos";
		} else if (function.equals(FunctionApplication.TAN)) {
			return "tan";
		} else if (function.equals(FunctionApplication.POW)) {
			return "pow";
		}
		//else if (function.equals(FunctionApplication.EXP)) { return "exp"; }
		else if (function.equals(FunctionApplication.MIN)) {
			return "min";
		} else if (function.equals(FunctionApplication.MAX)) {
			return "max";
		} else {
			return null;
		}
	}

	/**
	 * Parses an atomic predicate, and translates it into a sicstus
	 * clp(q) expression (and its negation). Moreover, it stores the list 
	 * of the variables with integral type in the expression, and it mangles 
	 * the narrowing, floating-to-integral conversion subexpressions 
	 * and keeping the list of all the produced terms. It is meant
	 * to be used more than once in the context of a same {@link SicstusParser} 
	 * scan. This way, it will gradually grow the lists of all the integer variables
	 * for all the subexpression of the parsed expression. On contrary, the list of 
	 * all the terms obtained by mangling narrowing conversions must be reset 
	 * whenever the owner {@link SicstusParser} elaborates it.  
	 * This class should have been declared as an inner class of {@link SicstusParser}, 
	 * but it's here to offload the latter. 
	 * 
	 * @author Pietro Braione
	 *
	 */
	private class SicstusParserAtomicPredicates implements PrimitiveVisitor {
		final StringBuilder integerVariables = new StringBuilder();
		final HashSet<Primitive> integerVariablesDone = new HashSet<Primitive>();
		ArrayList<Term> narrowedValues = new ArrayList<Term>();
        ArrayList<Term> divValues = new ArrayList<Term>();
        String termPositive = "";
        String termNegative = "";
		
		public SicstusParserAtomicPredicates() { }

		@Override
		public void visitAny(Any x) {
			throw new UnexpectedInternalException("the 'any' value should not arrive to Sicstus");
		}

		@Override
		public void visitExpression(Expression e) throws Exception {
            final Primitive firstOp = e.getFirstOperand();
            final Primitive secondOp = e.getSecondOperand();
			final Operator operator = e.getOperator();
			final String operatorString = sicstusOperator(operator);
			if (operatorString == null) { 
				//operator unsupported by sicstus: some of them can 
				//be translated into combinations of supported operators
	            if (operator == Operator.REM) {
	            	final Primitive val = firstOp.sub(firstOp.div(secondOp).mul(secondOp));
	            	val.accept(this);
	            } else if (operator == Operator.SHL) {
	                final Primitive[] args = new Primitive[2];
	                args[0] = calc.valInt(2);
	                args[1] = secondOp;
	                final Primitive val = firstOp.mul(calc.applyFunction(firstOp.getType(), FunctionApplication.POW, args));
	                val.accept(this);
	            } else if (operator == Operator.SHR) {
	                final Primitive[] args = new Primitive[2];
	                args[0] = calc.valInt(2);
	                args[1] = secondOp;
	                final Primitive val = firstOp.div(calc.applyFunction(firstOp.getType(), FunctionApplication.POW, args).to(firstOp.getType()));
	                val.accept(this);
	            } else {
		            //completely unsupported operator: mangles e
		            //into a symbolic value (unsupported operators currently are
		           	//Operator.USHR, Operator.ANDBW, Operator.ORBW, Operator.XOR)
	            	m.mangle(e).accept(this);
	            }
			} else if (operator == Operator.DIV && 
			           Type.isPrimitiveIntegral(firstOp.getType()) && 
			           Type.isPrimitiveIntegral(secondOp.getType())) {
			    //need some equations for the integer division
                final Term t = m.mangle(e);
                this.divValues.add(t); //will be demangled/translated later
                t.accept(this);
			} else if (e.isUnary()) {
				e.getOperand().accept(this);
				final String operandString = this.termPositive;
				this.termPositive = "(" + operatorString + " " + operandString  + ")";
			} else { //e is binary
				e.getFirstOperand().accept(this);
				final String firstString = this.termPositive;
				e.getSecondOperand().accept(this);
				final String secondString = this.termPositive;
				this.termPositive = "(" + firstString + " " + operatorString + " " + secondString + ")";
				final String operatorStringNegated = sicstusOperatorNegated(operator);
				if (operatorStringNegated == null) {
					this.termNegative = null;
				} else {
					this.termNegative = "(" + firstString + " " + operatorStringNegated + " " + secondString + ")";
				}
			}
		}

		@Override
		public void visitFunctionApplication(FunctionApplication x)
		throws Exception {
			final String funString = sicstusFunction(x.getOperator());
			if (funString == null) {
				//if the function cannot be interpreted by Sicstus, 
				//the whole application is treated as an uninterpreted 
				//symbolic value
                if (Type.isPrimitive(x.getType())) {
                	m.mangle(x).accept(this);
                } else {
                    throw new ExternalProtocolInterfaceException("Wrong function return type");
                }
			} else {
				final StringBuilder resultBuffer = new StringBuilder();
				boolean firstDone = false;
				for (Primitive p : x.getArgs()) {
					p.accept(this);
					if (firstDone) {
						resultBuffer.append(",");
					} else {
						firstDone = true;
					}
					resultBuffer.append(this.termPositive);
				}
				this.termPositive = funString + "(" + resultBuffer.toString() + ")";
				this.termNegative = null;
			}
		}

		@Override
		public void visitNarrowingConversion(NarrowingConversion x)
		throws Exception  {
        	final Primitive arg = x.getArg();
			if (Type.isPrimitiveIntegral(x.getType()) == Type.isPrimitiveIntegral(arg.getType())) {
	        	arg.accept(this); //from and to types are injected in the same sicstus type
			} else { //from type is floating, to type is integral
				final Term t = m.mangle(x);
				this.narrowedValues.add(t); //we will demangle/translate it later
				t.accept(this);
			}
		}

		@Override
		public void visitWideningConversion(WideningConversion x)
		throws Exception  {
        	final Primitive arg = x.getArg();
        	arg.accept(this);
		}

		@Override
		public void visitPrimitiveSymbolic(PrimitiveSymbolic s) {
			this.termPositive = "S" + s.getId();
			this.termNegative = null;
			updateIntegerVariables(s);
		}

		@Override
		public void visitSimplex(Simplex x) {
			this.termPositive = x.toString();
			this.termNegative = null;
		}

		@Override
		public void visitTerm(Term x) {
			this.termPositive = x.toString();
			if (this.termPositive.equals(Array.INDEX_ID)) {
				this.termPositive = INDEX_SICSTUS;
			}
			this.termNegative = null;
			updateIntegerVariables(x);
		}
		
		private void updateIntegerVariables(Primitive p) {
			if (Type.isPrimitiveIntegral(p.getType()) && ! this.integerVariablesDone.contains(p)) {
				if (this.integerVariablesDone.size() > 0) {
					this.integerVariables.append(",");
				}
				this.integerVariables.append(this.termPositive);
				this.integerVariablesDone.add(p);
			}
		}
	}
}