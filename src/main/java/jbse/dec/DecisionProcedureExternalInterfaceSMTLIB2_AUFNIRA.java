package jbse.dec;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Stack;

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

/**
 * {@link DecisionProcedureExternalInterface} to a generic SMTLIB 2 solver
 * that supports the AUFNIRA logic. 
 * 
 * @author Pietro Braione
 * @author Diego Piazza
 */
//TODO simplify implementation
class DecisionProcedureExternalInterfaceSMTLIB2_AUFNIRA extends DecisionProcedureExternalInterface {
    //commands
    private static final String PROLOGUE = 
        "(set-option :print-success true)\n" +
        "(set-option :interactive-mode true)\n" +
        "(set-option :produce-models true)\n" +
        "(set-logic AUFNIRA)\n" +
        "(define-fun round_to_zero ((x Real)) Int (ite (>= x 0.0) (to_int x) (- (to_int (- x)))))\n";
    private static final String PUSH_1 = "(push 1)\n";
    private static final String POP_BEGIN = "(pop ";
    private static final String POP_END = ")\n";
    private static final String POP_1 = "(pop 1)\n";
    private static final String CHECKSAT = "(check-sat)\n";
    private static final String GETVALUE_BEGIN = "(get-value (";
    private static final String GETVALUE_END = "))\n";
    private static final String EXIT = "(exit)\n";
    
    //answers
    private static final String SUCCESS = "success";
    private static final String SAT = "sat";
    private static final String UNSAT = "unsat";
    private static final String UNKNOWN = "unknown";
    
    //etc
    private static final String OTHER = "";

    private final CalculatorRewriting calc;
    private final ExpressionMangler m;
    private boolean working;
    private Process solver;
    private BufferedReader solverIn;
    private BufferedWriter solverOut;
    private String currentClausePositive;
    private String currentClauseNegative;
    private boolean hasCurrentClause;
    private SMTLIB2ExpressionVisitor v;
    private ArrayList<Integer> nSymPushed; 
    private int nSymCurrent;
    private int nTotalSymbols;

    /** 
     * Costructor.
     */
    public DecisionProcedureExternalInterfaceSMTLIB2_AUFNIRA(CalculatorRewriting calc, String solverBinaryPath) 
    throws ExternalProtocolInterfaceException, IOException {
        this.calc = calc;
        this.m = new ExpressionMangler("X", "", this.calc);
        this.working = true;
        final ProcessBuilder pb = new ProcessBuilder(solverBinaryPath.split(" "));
        pb.redirectErrorStream(true);
        this.solver = pb.start();
        this.solverIn = new BufferedReader(new InputStreamReader(this.solver.getInputStream()));
        this.solverOut = new BufferedWriter(new OutputStreamWriter(this.solver.getOutputStream()));

        final String query = PROLOGUE + PUSH_1;
        sendAndCheckAnswer(query);
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
        this.hasCurrentClause = true;

        try {
            cond.accept(this.v);
            this.currentClausePositive = PUSH_1 + this.v.getQueryDeclarations() + "(assert " + this.v.getQueryAssertClause() + ")\n";
            cond.not().accept(this.v);
            this.currentClauseNegative = PUSH_1 + this.v.getQueryDeclarations() + "(assert " + this.v.getQueryAssertClause() + ")\n";
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
        this.hasCurrentClause = true;
        //does nothing, this decision procedure works only for numbers
        this.currentClausePositive = this.currentClauseNegative = null;
    }

    @Override
    public void sendClauseAssumeExpands(ReferenceSymbolic r, String className) 
    throws ExternalProtocolInterfaceException {
        if (this.hasCurrentClause) {
            throw new ExternalProtocolInterfaceException("Attempted to send a clause when a current clause already exists.");
        }
        this.hasCurrentClause = true;
        //does nothing, this decision procedure works only for numbers
        this.currentClausePositive = this.currentClauseNegative = null;
    }

    @Override
    public void sendClauseAssumeNull(ReferenceSymbolic r) 
    throws ExternalProtocolInterfaceException {
        if (this.hasCurrentClause) {
            throw new ExternalProtocolInterfaceException("Attempted to send a clause when a current clause already exists.");
        }
        this.hasCurrentClause = true;
        //does nothing, this decision procedure works only for numbers      
        this.currentClausePositive = this.currentClauseNegative = null;
    }

    @Override
    public void sendClauseAssumeClassInitialized(String className) 
    throws ExternalProtocolInterfaceException {
        if (this.hasCurrentClause) {
            throw new ExternalProtocolInterfaceException("Attempted to send a clause when a current clause already exists.");
        }
        this.hasCurrentClause = true;
        //does nothing, this decision procedure works only for numbers
        this.currentClausePositive = this.currentClauseNegative = null;
    }

    @Override
    public void sendClauseAssumeClassNotInitialized(String className) 
    throws ExternalProtocolInterfaceException {
        if (this.hasCurrentClause) {
            throw new ExternalProtocolInterfaceException("Attempted to send a clause when a current clause already exists.");
        }
        this.hasCurrentClause = true;
        //does nothing, this decision procedure works only for numbers
        this.currentClausePositive = this.currentClauseNegative = null;
    }

    @Override
    public void retractClause() throws ExternalProtocolInterfaceException {
        if (!this.hasCurrentClause) {
            throw new ExternalProtocolInterfaceException("Attempted to retract a clause with no current clause.");
        }
        this.hasCurrentClause = false;
        this.currentClausePositive = this.currentClauseNegative = null;
        forgetPushedDeclarations();
    }

    @Override
    public boolean checkSat(ClassHierarchy hier, boolean value) 
    throws ExternalProtocolInterfaceException, IOException {
        if (!this.hasCurrentClause) {
            throw new ExternalProtocolInterfaceException("Attempted to check satisfiability with no current clause.");
        }
        
        final String queryPush = (value ? this.currentClausePositive : this.currentClauseNegative);
        if (queryPush == null) {
            return true;
        }
        sendAndCheckAnswer(queryPush);
        final boolean isSat = sendAndCheckAnswerChecksat();
        sendAndCheckAnswer(POP_1);
        return isSat;
    }
    
    @Override
    public Map<PrimitiveSymbolic, Simplex> getModel() 
    throws NoModelException, ExternalProtocolInterfaceException, IOException {
        sendAndCheckAnswerChecksat(); //always need a checksat before reading a model
        final String smtlib2Model = sendAndCheckAnswerGetmodel();
        if (smtlib2Model == null || smtlib2Model.startsWith("(error")) {
            throw new NoModelException();
        }
        
        final HashMap<PrimitiveSymbolic, Simplex> model = new HashMap<>();
        String smtlib2Symbol = null;
        LinkedList<LinkedList<Object>> smtlib2ParseStack = new LinkedList<>();
        smtlib2ParseStack.push(new LinkedList<>());
        int nestingLevel = 0;
        boolean scanningSymbol = false;
        final String[] smtlib2ModelTokens = smtlib2Model.replace("(", " ( ").replace(")", " ) ").trim().split("\\s+"); //thanks for the idea Peter Norvig!
        for (String token : smtlib2ModelTokens) {
            final int prevNestingLevel = nestingLevel;
            if (token.equals("(")) {
                ++nestingLevel;
            } else if (token.equals(")")) {
                --nestingLevel;
            }
            if (prevNestingLevel == 1 && nestingLevel == 2) {
                scanningSymbol = true;
            } else if (nestingLevel == 2 && scanningSymbol) {
                smtlib2Symbol = token;
                scanningSymbol = false;
            } else if (nestingLevel >= 2 && !scanningSymbol) {
                if (token.equals("(")) {
                    smtlib2ParseStack.push(new LinkedList<>());
                } else if (token.equals(")")) {
                    final LinkedList<Object> list = smtlib2ParseStack.pop();
                    smtlib2ParseStack.peek().add(list);
                } else {
                    smtlib2ParseStack.peek().add(token);
                }
            } else if (prevNestingLevel == 2 && nestingLevel == 1) {
                final Primitive jbseSymbol = this.v.smtlib2VarsToJBSESymbols.get(smtlib2Symbol);
                if (jbseSymbol != null && jbseSymbol instanceof PrimitiveSymbolic) {
                    final Number value = smtlib2Interpret(smtlib2ParseStack.pop());
                    if (value == null) {
                        //unable to interpret the SMTLIB2 expression
                        throw new NoModelException(); //TODO possibly throw a different exception
                    } else {
                        model.put((PrimitiveSymbolic) jbseSymbol, (Simplex) calc.val_(value));
                    }
                }   
                smtlib2Symbol = null;
                smtlib2ParseStack = new LinkedList<>();
                smtlib2ParseStack.push(new LinkedList<>());
            }
        }
        return model;
    }
    
    private Number smtlib2Interpret(Object smtlib2ParsedExpression) {
        if (smtlib2ParsedExpression == null) {
            return null;
        }
        if (smtlib2ParsedExpression instanceof String) {
            // <constant>
            final String constant = (String) smtlib2ParsedExpression;
            try {
                final Long l = Long.parseLong(constant);
                return l;
            } catch (NumberFormatException e1) {
                try {
                    final Double d = Double.parseDouble(constant);
                    return d;
                } catch (NumberFormatException e2) {
                    return null;
                }
            }
        }
        if (smtlib2ParsedExpression instanceof LinkedList<?>) {
            // ( ... )
            final LinkedList<?> exprList = (LinkedList<?>) smtlib2ParsedExpression;
            if (exprList.isEmpty()) {
                // ()
                return null;
            }
            final Object head = exprList.pollFirst();
            if (exprList.isEmpty()) {
                // (<subexpression>)
                return smtlib2Interpret(head);
            } else if (head instanceof String) {
                final String headString = (String) head;
                final Number firstOperand, secondOperand;
                switch (headString) {
                case "+":
                    firstOperand = smtlib2Interpret(exprList.pollFirst());
                    if (exprList.isEmpty()) {
                        // (+ <firstOperand>)
                        return firstOperand;
                    } else {
                        // (+ <firstOperand> <secondOperand>)
                        secondOperand = smtlib2Interpret(exprList.pollFirst());
                        if (firstOperand == null || secondOperand == null) {
                            return null;
                        } else if (firstOperand instanceof Float || firstOperand instanceof Double ||
                        secondOperand instanceof Float || secondOperand instanceof Double) {
                            return firstOperand.doubleValue() + secondOperand.doubleValue();
                        } else {
                            return firstOperand.longValue() + secondOperand.longValue();
                        }
                    }
                case "-":
                    firstOperand = smtlib2Interpret(exprList.pollFirst());
                    if (exprList.isEmpty()) {
                        // (- <firstOperand>)
                        if (firstOperand == null) {
                            return null;
                        } else if (firstOperand instanceof Float || firstOperand instanceof Double) {
                            return - firstOperand.doubleValue();
                        } else {
                            return - firstOperand.longValue();
                        }
                    } else {
                        // (- <firstOperand> <secondOperand>)
                        secondOperand = smtlib2Interpret(exprList.pollFirst());
                        if (firstOperand == null || secondOperand == null) {
                            return null;
                        } else if (firstOperand instanceof Float || firstOperand instanceof Double ||
                        secondOperand instanceof Float || secondOperand instanceof Double) {
                            return firstOperand.doubleValue() - secondOperand.doubleValue();
                        } else {
                            return firstOperand.longValue() - secondOperand.longValue();
                        }
                    }
                case "*":
                    // (* <firstOperand> <secondOperand>)
                    firstOperand = smtlib2Interpret(exprList.pollFirst());
                    secondOperand = smtlib2Interpret(exprList.pollFirst());
                    if (firstOperand == null || secondOperand == null) {
                        return null;
                    } else if (firstOperand instanceof Float || firstOperand instanceof Double ||
                    secondOperand instanceof Float || secondOperand instanceof Double) {
                        return firstOperand.doubleValue() * secondOperand.doubleValue();
                    } else {
                        return firstOperand.longValue() * secondOperand.longValue();
                    }
                case "/":
                    // (/ <firstOperand> <secondOperand>)
                    firstOperand = smtlib2Interpret(exprList.pollFirst());
                    secondOperand = smtlib2Interpret(exprList.pollFirst());
                    if (firstOperand == null || secondOperand == null) {
                        return null;
                    } else if (firstOperand instanceof Float || firstOperand instanceof Double ||
                    secondOperand instanceof Float || secondOperand instanceof Double) {
                        return firstOperand.doubleValue() / secondOperand.doubleValue();
                    } else {
                        return firstOperand.longValue() / secondOperand.longValue();
                    }
                }
            }
        }
        return null;
    }

    @Override
    public void pushAssumption(boolean value) 
    throws ExternalProtocolInterfaceException, IOException {
        if (!this.hasCurrentClause) {
            throw new ExternalProtocolInterfaceException("attempted to push assumption with no current clause");
        }
        this.hasCurrentClause = false;
        rememberPushedDeclarations();
        
        String queryPush = (value ? this.currentClausePositive : this.currentClauseNegative);
        if (queryPush == null) {
            queryPush = PUSH_1; //TODO avoid empty pushes
        }
        sendAndCheckAnswer(queryPush);
    }

    @Override
    public void popAssumption() throws ExternalProtocolInterfaceException, IOException {
        forgetPoppedDeclarations();
        sendAndCheckAnswer(POP_1);
    }

    @Override
    public void clear() 
    throws ExternalProtocolInterfaceException, IOException {
        final int nToPop = (this.nSymPushed == null ? 0 : this.nSymPushed.size());
        if (nToPop > 0) {
            sendAndCheckAnswer(POP_BEGIN + nToPop + POP_END);
        }
        this.currentClausePositive = this.currentClauseNegative = null;
        this.hasCurrentClause = false;
        forgetAllDeclarations();
    }
    
    private void send(String query) throws IOException {
        //System.err.print("--->SMTLIB2: " + query); //TODO log differently!

        try {
            this.solverOut.write(query);
            this.solverOut.flush();
        } catch (IOException e) {
            this.working = false;
            throw e;
        }
    }
    
    private void sendAndCheckAnswer(String query) throws IOException, ExternalProtocolInterfaceException {
        send(query);
        for (int i = 0; i < query.length(); ++i) {
            if (query.charAt(i) == '\n') {
                final String answer = read();
                if (answer == null) {
                    this.working = false;
                    throw new IOException("failed read of solver answer. Query: " + query + ", failed at character " + i);
                }
                if (!answer.equals(SUCCESS)) {
                    this.working = false;
                    throw new ExternalProtocolInterfaceException("unexpected solver answer. Message: " + answer);
                }
            }
        }
    }
    
    private String read() throws IOException {
        final String answer;
        try {
            answer = this.solverIn.readLine();
        } catch (IOException e) {
            this.working = false;
            throw e;
        }
        if (answer == null) {
            this.working = false;
            throw new IOException("failed read of solver output, premature end of stream reached, process alive: " + this.solver.isAlive() + ", exit value: " + this.solver.exitValue());
        }

        //System.err.println("<---SMTLIB2: " + answer); //TODO log differently!
        return answer;
    }
    
    private boolean sendAndCheckAnswerChecksat() throws IOException, ExternalProtocolInterfaceException {
        send(CHECKSAT);
        final String answer = read();
        if (!answer.equals(SAT) && !answer.equals(UNSAT) && !answer.equals(UNKNOWN)) {
            this.working = false;
            throw new ExternalProtocolInterfaceException("unrecognized answer from solver when checking satisfiability. Message: " + answer);
        }
        return !answer.equals(UNSAT); //conservatively returns true if answer is unknown
    }
    
    private String sendAndCheckAnswerGetmodel() 
    throws IOException, ExternalProtocolInterfaceException {
        final StringBuilder query = new StringBuilder(GETVALUE_BEGIN);
        for (String symbol : this.v.smtlib2DeclaredSymbols) {
            query.append(symbol);
            query.append(' ');
        }
        query.append(GETVALUE_END);
        send(query.toString());
        //answer can be multiline, we count parentheses to
        //determine when the answer is over
        final StringBuilder retVal = new StringBuilder();
        int nestingLevel = 0;
        do {
            final String answer = read();
            retVal.append(answer);
            for (char c : answer.toCharArray()) {
                if (c == '(') {
                    ++nestingLevel;
                } else if (c == ')') {
                    --nestingLevel;
                }
            }
        } while (nestingLevel > 0);
        return retVal.toString();
    }
    
    private void rememberPushedDeclarations() {
        this.v.clearQueryDeclarations();
        this.nSymPushed.add(this.nSymCurrent);
        this.nSymCurrent = 0;
    }
    
    private void forgetPushedDeclarations() {
        this.v.clearQueryDeclarations();
        this.v.removeDeclaredSymbols(this.nSymCurrent);
        this.nSymCurrent = 0;
    }

    private void forgetPoppedDeclarations() {
        final int last = this.nSymPushed.size() - 1;
        this.v.removeDeclaredSymbols(this.nSymPushed.get(last));
        this.nSymPushed.remove(last);
    }

    private void forgetAllDeclarations() {
        this.v = new SMTLIB2ExpressionVisitor();
        this.nSymPushed = new ArrayList<>();
        this.nSymCurrent = 0;
        this.nTotalSymbols = 0;
    }

    /**
     * Returns the SMTLIB2 primitive operator which corresponds 
     * to a Java operator.
     * 
     */ 
    private static String toSMTLIB2Operator(Operator operator, Primitive firstOperand, Primitive secondOperand) {
        if (operator == Operator.ADD)      return "+";
        else if (operator == Operator.SUB) return "-";
        else if (operator == Operator.MUL) return "*";
        else if (operator == Operator.DIV) {
            if (Type.isPrimitiveIntegral(firstOperand.getType()) && Type.isPrimitiveIntegral(secondOperand.getType())) {
                return "div";
            } else {
                return "/";
            }
        }
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
    private static String toSMTLIB2Type(char type) {
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
     * Builds a SMTLIB2 string representing an expression.
     */
    private class SMTLIB2ExpressionVisitor implements PrimitiveVisitor {
        /** Is this a boolean expression? */
        private boolean isBooleanExpression = true;
        
        /** 
         * All the SMTLIB v2 symbols declared in 
         * the visited Primitive. 
         */
        private LinkedHashSet<String> smtlib2DeclaredSymbols = new LinkedHashSet<>();
        
        /** 
         * Remaps the SMTLIB v2 symbols to
         * their original JBSE primitives 
         */
        private HashMap<String, Primitive> smtlib2VarsToJBSESymbols = new HashMap<>();
        
        /** 
         * SMTLIB2 query for the declaration of the symbols.
         * This string contains the declarations that have
         * not yet been sent to the solver, to avoid double
         * declarations.
         */
        private StringBuilder queryDeclarations = new StringBuilder();
        
        /**
         * Clauses stored during the visit.
         */
        private Stack<String> clauseStack = new Stack<>();

        public String getQueryAssertClause() { return this.clauseStack.pop(); }

        public String getQueryDeclarations() { return this.queryDeclarations.toString(); }

        public void clearQueryDeclarations() {
            this.queryDeclarations = new StringBuilder();                       
        }

        void removeDeclaredSymbols(int nSymbolsToForget) {
            final ArrayList<String> symbolsToForget = new ArrayList<>();
            int n = nTotalSymbols - nSymbolsToForget;
            int c = 0;
            for (String s : this.smtlib2DeclaredSymbols) {
                if (c < n) {
                    ++c;
                } else {
                    this.smtlib2VarsToJBSESymbols.remove(s);
                    symbolsToForget.add(s);
                }
            }
            this.smtlib2DeclaredSymbols.removeAll(symbolsToForget);
            nTotalSymbols = nTotalSymbols - nSymbolsToForget;
        }

        public SMTLIB2ExpressionVisitor() { }

        public SMTLIB2ExpressionVisitor(SMTLIB2ExpressionVisitor v, boolean isBooleanExpression) {
            this.isBooleanExpression = isBooleanExpression;
            this.smtlib2DeclaredSymbols = v.smtlib2DeclaredSymbols;
            this.smtlib2VarsToJBSESymbols = v.smtlib2VarsToJBSESymbols;
            this.queryDeclarations = v.queryDeclarations;
            this.clauseStack = v.clauseStack;
        }

        @Override
        public void visitAny(Any x) throws ExternalProtocolInterfaceException {
            throw new ExternalProtocolInterfaceException("values of type Any should not reach the SMT solver");         
        }

        @Override
        public void visitExpression(Expression e) throws Exception {
            final Operator operation = e.getOperator();
            final Primitive firstOperand = e.getFirstOperand();
            final Primitive secondOperand = e.getSecondOperand();
            final String op = toSMTLIB2Operator(operation, firstOperand, secondOperand);
            final boolean isBooleanOperator = operation.acceptsBoolean();
            if (operation.returnsBoolean() == this.isBooleanExpression) {
                //operation well formed
                if (operation == Operator.NE) {
                    //1-NE is not a SMTLIB2 operator but can be translated to a combination of SMTLIB2 operators
                    firstOperand.accept(new SMTLIB2ExpressionVisitor(this, isBooleanOperator));
                    secondOperand.accept(new SMTLIB2ExpressionVisitor(this, isBooleanOperator));
                    final String secondOperandSMT = this.clauseStack.pop();
                    final String firstOperandSMT = this.clauseStack.pop();
                    this.clauseStack.push("(not (= " + firstOperandSMT + " " + secondOperandSMT + "))");
                } else if (op.equals(OTHER)) {
                    //2-Operator does not correspond to a SMTLIB2 operator
                    m.mangle(e).accept(this);
                } else {
                    //3-The operator correspond to a SMTLIB2 operator
                    final String clause;
                    if (e.isUnary()) {
                        e.getOperand().accept(new SMTLIB2ExpressionVisitor(this, isBooleanOperator));
                        clause = "("+ op +" "+ this.clauseStack.pop() + ")";
                    } else {
                        firstOperand.accept(new SMTLIB2ExpressionVisitor(this, isBooleanOperator));
                        secondOperand.accept(new SMTLIB2ExpressionVisitor(this, isBooleanOperator));
                        final String secondOperandSMT = this.clauseStack.pop();
                        final String firstOperandSMT = this.clauseStack.pop();
                        clause = "("+ op + " " + firstOperandSMT + " " + secondOperandSMT + ")";
                    }
                    this.clauseStack.push(clause);
                }
            } else {
                throw new UnexpectedInternalException("error while parsing expression (expected a boolean expression but it is not): " + e.toString());
            }
        }

        @Override
        public void visitFunctionApplication(FunctionApplication x) throws Exception {
            if (x.getType() == Type.BOOLEAN && !this.isBooleanExpression) {
                throw new UnexpectedInternalException("error while parsing expression (expected a boolean expression but it is not): " + x.toString());
            } else if (x.getType() != Type.BOOLEAN && this.isBooleanExpression) {
                throw new UnexpectedInternalException("error while parsing expression (expected a numeric expression but it is not): " + x.toString());
            }
            final String operator = x.getOperator().split(":")[2];
            final char type = x.getType();
            final StringBuilder clause = new StringBuilder();
            final StringBuilder smtlib2Signature = new StringBuilder();
            boolean builtIn = false;
            if (operator.equals(FunctionApplication.ABS)) {
                if (Type.isPrimitiveIntegral(x.getType())) {
                    builtIn = true;
                    clause.append("(abs ");
                    smtlib2Signature.append("abs ("); //useless, but we keep it
                } else {
                    clause.append("(absReals ");
                    smtlib2Signature.append("absReals (");
                }
            } else {
                clause.append("(" + operator + " ");
                smtlib2Signature.append(operator + " (");
            }
            for (Primitive p : x.getArgs()) {
                p.accept(new SMTLIB2ExpressionVisitor(this, false));
                clause.append(this.clauseStack.pop());
                clause.append(" ");
                final String smtlib2Type = toSMTLIB2Type(p.getType());
                smtlib2Signature.append(smtlib2Type);
                smtlib2Signature.append(" ");
            }
            clause.append(")");
            this.clauseStack.push(clause.toString());
            smtlib2Signature.append(") ");
            smtlib2Signature.append(toSMTLIB2Type(type));

            if (this.smtlib2DeclaredSymbols.contains(operator) || builtIn) {
                // does nothing
            } else {
                this.smtlib2DeclaredSymbols.add(operator);
                //not added to smtlib2VarsToJBSESymbols, sorry, no model for this
                this.queryDeclarations.append("(declare-fun " + smtlib2Signature + " )\n");
                nSymCurrent = nSymCurrent + 1;
                nTotalSymbols = nTotalSymbols + 1;
            }
        }

        @Override
        public void visitWideningConversion(WideningConversion x) throws Exception {
            if (x.getType() == Type.BOOLEAN && !this.isBooleanExpression) {
                throw new UnexpectedInternalException("error while parsing expression (expected a boolean expression but it is not): " + x.toString());
            } else if (x.getType() != Type.BOOLEAN && this.isBooleanExpression) {
                throw new UnexpectedInternalException("error while parsing expression (expected a numeric expression but it is not): " + x.toString());
            }
            final Primitive arg = x.getArg();
            arg.accept(new SMTLIB2ExpressionVisitor(this, false));
            if (Type.isPrimitiveIntegral(x.getType()) != Type.isPrimitiveIntegral(arg.getType())) {
                this.clauseStack.push("(to_real " + this.clauseStack.pop() + ")");
            }
        }   

        @Override
        public void visitNarrowingConversion(NarrowingConversion x) throws Exception {
            if (x.getType() == Type.BOOLEAN && !this.isBooleanExpression) {
                throw new UnexpectedInternalException("error while parsing expression (expected a boolean expression but it is not): " + x.toString());
            } else if (x.getType() != Type.BOOLEAN && this.isBooleanExpression) {
                throw new UnexpectedInternalException("error while parsing expression (expected a numeric expression but it is not): " + x.toString());
            }
            final Primitive arg = x.getArg();
            arg.accept(new SMTLIB2ExpressionVisitor(this, false));
            if (Type.isPrimitiveIntegral(x.getType()) != Type.isPrimitiveIntegral(arg.getType())) {
                this.clauseStack.push("(round_to_zero " + this.clauseStack.pop() + ")");
            }
        }

        @Override
        public void visitSimplex(Simplex x) {
            final Object obj = x.getActualValue();
            final char mytype = x.getType();
            if (mytype == Type.BYTE || mytype == Type.SHORT ||
                mytype == Type.INT || mytype == Type.LONG ||
                mytype == Type.CHAR) {
                if (obj instanceof Number && ((Number) obj).doubleValue() < 0) {
                    this.clauseStack.push("(- " + obj.toString().substring(1) + ")");
                } else {
                    this.clauseStack.push(obj.toString());
                }
            } else if (mytype == Type.FLOAT || mytype == Type.DOUBLE) {
                String value = obj.toString();
                
                //breaks value in its significand and exponent parts 
                final String significand;
                final String exponent;
                String[] parts = value.split("E|e");
                if (parts.length == 2) {                    
                    significand = parts[0];
                    exponent = parts[1];
                } else { //parts.length == 1
                    significand = value;
                    exponent = "0";
                }

                //encodes the base in SMTLIB2 format
                final String smtlib2Significand;
                if (significand.contains("-")) {
                    smtlib2Significand = String.format("(- %s)", significand.replace("-", ""));
                } else {
                    smtlib2Significand = significand;
                }
                
                //builds smtlib2Value
                final String smtlib2Value;
                if (Long.parseLong(exponent) == 0) {
                    smtlib2Value = smtlib2Significand;
                } else {
                    //smtlib2Multiplier == 10 to the power of abs(exponent)
                    final StringBuilder smtlib2Multiplier = new StringBuilder("1");
                    final long numZeros = Math.abs(Long.parseLong(exponent));
                    for (int i = 1; i <= numZeros ; ++i) {
                        smtlib2Multiplier.append('0');
                    }
                    //multiplies or divides smtlib2Significand by 
                    //smtlib2Multiplier based on the sign of exponent
                    final String smtlib2Operator = (exponent.contains("-") ? "/" : "*");                    
                    smtlib2Value = String.format("(%s %s %s)", smtlib2Operator, smtlib2Significand, smtlib2Multiplier.toString());                   
                }

                //pushes it
                this.clauseStack.push(smtlib2Value);
              } else if (mytype == Type.BOOLEAN) {
                if ((Boolean) obj) {
                    this.clauseStack.push(this.isBooleanExpression ? "true" : "1");
                } else {
                    this.clauseStack.push(this.isBooleanExpression ? "false" : "0");
                }
            }
        }

        @Override
        public void visitPrimitiveSymbolic(PrimitiveSymbolic s) {
            putSymbol(s);
        }

        @Override
        public void visitTerm(Term x) {
            putSymbol(x);
        }

        private void putSymbol(Primitive symbol) {
            final char type = symbol.getType();
            final String symbolToString = symbol.toString();
            final String smtlib2Variable = (symbolToString.charAt(0) == '{' ? 
                                            symbolToString.substring(1, symbolToString.length() - 1) :
                                            symbolToString);
            if (this.smtlib2DeclaredSymbols.contains(smtlib2Variable)) {
                // does nothing
            } else {
                this.smtlib2DeclaredSymbols.add(smtlib2Variable);
                this.smtlib2VarsToJBSESymbols.put(smtlib2Variable, symbol);
                this.queryDeclarations.append("(declare-fun " + smtlib2Variable + " () " + toSMTLIB2Type(type) + ")\n");
                ++nSymCurrent;
                ++nTotalSymbols;
            }
            this.clauseStack.push(smtlib2Variable);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void quit() 
    throws ExternalProtocolInterfaceException, IOException {
        this.working = false;
        send(EXIT);
        while (this.solverIn.readLine() != null) {
            //do nothing
        }
        this.solverOut.close();
        try {
            //we don't check the exit code because Z3 seems to 
            //always exit with code 1 when invoked from Java
            this.solver.waitFor();
        } catch (InterruptedException e) {
            throw new ExternalProtocolInterfaceException(e);
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
