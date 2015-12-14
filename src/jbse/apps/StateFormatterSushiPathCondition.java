package jbse.apps;

import static jbse.common.Type.className;
import static jbse.common.Type.isReference;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import jbse.apps.Formatter;
import jbse.common.Type;
import jbse.mem.Clause;
import jbse.mem.ClauseAssume;
import jbse.mem.ClauseAssumeAliases;
import jbse.mem.ClauseAssumeExpands;
import jbse.mem.ClauseAssumeNull;
import jbse.mem.ClauseAssumeReferenceSymbolic;
import jbse.mem.Objekt;
import jbse.mem.State;
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
import jbse.val.Symbolic;
import jbse.val.Term;
import jbse.val.WideningConversion;

/**
 * A {@link Formatter} used by Sushi.
 * 
 * @author Esther Turati
 * @author Pietro Braione
 */
public abstract class StateFormatterSushiPathCondition implements Formatter {
    protected String output = "";
    private Supplier<State> initialStateSupplier;
    private Supplier<Map<PrimitiveSymbolic, Simplex>> modelSupplier;
    private int testCounter = 0;
    private int bestTest = -1;
    private int bestPathConditionLength = -1;
    
    public StateFormatterSushiPathCondition(Supplier<State> initialStateSupplier, 
                                  Supplier<Map<PrimitiveSymbolic, Simplex>> modelSupplier) {
        this.initialStateSupplier = initialStateSupplier;
        this.modelSupplier = modelSupplier;
    }

    @Override
    public final void formatPrologue() {
        this.output = PROLOGUE;
    }

    @Override
    public final void formatState(State state) {
        final MethodUnderTest t = 
            new MethodUnderTest(this.initialStateSupplier.get(), state, this.modelSupplier.get(), this.testCounter);
        this.output = t.getText();
        if (this.bestTest == -1 || this.bestPathConditionLength > t.getPathConditionLength()) {
            this.bestTest = this.testCounter;
            this.bestPathConditionLength = t.getPathConditionLength();
        }
        ++this.testCounter;
    }
    
    public final void formatEpilogue() {
        this.output = "}\n//USE: test" + this.bestTest +"\n";
    }

    @Override
    public void cleanup() {
        this.output = "";        
    }
    
    private static final String PROLOGUE =
        "import static sushi.compile.path_condition_distance.DistanceBySimilarityWithPathCondition.distance;\n" +
        "\n" +
        "import static java.lang.Math.*;\n" +
        "\n" +
        "import sushi.compile.path_condition_distance.*;\n" +
        "import sushi.logging.Level;\n" +
        "import sushi.logging.Logger;\n" +
        "\n" +
        "import java.util.ArrayList;\n" +
        "import java.util.HashMap;\n" +
        "import java.util.List;\n" +
        "\n" +
        "public class EvoSuiteWrapper {\n" +
        "\n";

    private static class MethodUnderTest {
        private static final String INDENT_1 = "    ";
        private static final String INDENT_2 = INDENT_1 + INDENT_1;
        private static final String INDENT_3 = INDENT_1 + INDENT_2;
        private static final String INDENT_4 = INDENT_1 + INDENT_3;
        private final StringBuilder s = new StringBuilder();
        private final int pathConditionLength;
        private final HashMap<Symbolic, String> symbolsToVariables = new HashMap<>();
        private final ArrayList<String> evoSuiteInputVariables = new ArrayList<>();
        private boolean panic = false;
        
        MethodUnderTest(State initialState, State finalState, Map<PrimitiveSymbolic, Simplex> model, int testCounter) {
            this.pathConditionLength = finalState.getPathCondition().size();
            appendMethodDeclaration(finalState, testCounter);
            appendPathCondition(finalState, testCounter);
            appendIfStatement(initialState, finalState, testCounter);
            appendMethodEnd(finalState, testCounter);
        }
        
        String getText() {
            return this.s.toString();
        }
        
        int getPathConditionLength() {
            return this.pathConditionLength;
        }
        
        private void appendMethodDeclaration(State finalState, int testCounter) {
            if (this.panic) {
                return;
            }
            this.s.append(INDENT_1);
            this.s.append("public void test");
            this.s.append(testCounter);
            this.s.append("(");
            makeVariables(finalState);
            boolean firstDone = false;
            for (Symbolic symbol : this.symbolsToVariables.keySet()) {
                final String varName = getVariableFor(symbol);
                if (isEvoSuiteInput(varName)) {
                    this.evoSuiteInputVariables.add(varName);
                    if (firstDone) {
                        this.s.append(", ");
                    } else {
                        firstDone = true;
                    }
                    final String type;
                    if (symbol instanceof ReferenceSymbolic) {
                        type = javaClass(((ReferenceSymbolic) symbol).getStaticType(), true);
                    } else {
                        type = javaPrimitiveType(((PrimitiveSymbolic) symbol).getType());
                    }
                    this.s.append(type);
                    this.s.append(' ');
                    this.s.append(varName);
                    }
            }
            this.s.append(") throws Exception {\n");
            this.s.append(INDENT_2);
            this.s.append("//generated for state ");
            this.s.append(finalState.getIdentifier());
            this.s.append('[');
            this.s.append(finalState.getSequenceNumber());
            this.s.append("]\n");
        }
        
        private void appendPathCondition(State finalState, int testCounter) {
            if (this.panic) {
                return;
            }
            this.s.append(INDENT_2);
            this.s.append("final ArrayList<ClauseSimilarityHandler> pathConditionHandler = new ArrayList<>();\n");
            this.s.append(INDENT_2);
            this.s.append("ValueCalculator valueCalculator;\n");
            final Collection<Clause> pathCondition = finalState.getPathCondition();
            for (Iterator<Clause> iterator = pathCondition.iterator(); iterator.hasNext(); ) {
                final Clause clause = iterator.next();
                this.s.append(INDENT_2);
                this.s.append("// "); //comment
                this.s.append(clause.toString());
                this.s.append("\n");
                if (clause instanceof ClauseAssumeExpands) {
                    final ClauseAssumeExpands clauseExpands = (ClauseAssumeExpands) clause;
                    final Symbolic symbol = clauseExpands.getReference();
                    final long heapPosition = clauseExpands.getHeapPosition();
                    setWithNewObject(finalState, symbol, heapPosition);
                } else if (clause instanceof ClauseAssumeNull) {
                    final ClauseAssumeNull clauseNull = (ClauseAssumeNull) clause;
                    final ReferenceSymbolic symbol = clauseNull.getReference();
                    setWithNull(symbol);
                } else if (clause instanceof ClauseAssumeAliases) {
                    final ClauseAssumeAliases clauseAliases = (ClauseAssumeAliases) clause;
                    final Symbolic symbol = clauseAliases.getReference();
                    final long heapPosition = clauseAliases.getHeapPosition();
                    setWithAlias(finalState, symbol, heapPosition);
                } else if (clause instanceof ClauseAssume) {
                    final ClauseAssume clauseAssume = (ClauseAssume) clause;
                    final Primitive assumption = clauseAssume.getCondition();
                    setNumericAssumption(assumption);
                } else {
                    this.s.append(INDENT_2);
                    this.s.append(';');
                    this.s.append('\n');
                }
            }
            this.s.append("\n");
        }
        
        private void appendIfStatement(State initialState, State finalState, int testCounter) {
            if (this.panic) {
                return;
            }
            this.s.append(INDENT_2);
            this.s.append("final HashMap<String, Object> candidateObjects = new HashMap<>();\n");
            for (String inputVariable : this.evoSuiteInputVariables) {
                this.s.append(INDENT_2);
                this.s.append("candidateObjects.put(\"");
                this.s.append(generateOriginFromVarName(inputVariable));
                this.s.append("\", ");
                this.s.append(inputVariable);
                this.s.append(");\n");
            }
            this.s.append('\n');
            this.s.append(INDENT_2);
            this.s.append("if (distance(pathConditionHandler, candidateObjects) == 0.0d)\n");
            this.s.append(INDENT_3);
            this.s.append("System.out.println(\"test");
            this.s.append(testCounter);
            this.s.append(" 0 distance\");\n");
        }
        
        private void appendMethodEnd(State finalState, int testCounter) {
            if (this.panic) {
                this.s.delete(0, s.length());
                this.s.append(INDENT_1);
                this.s.append("//Unable to generate test case ");
                this.s.append(testCounter);
                this.s.append(" for state ");
                this.s.append(finalState.getIdentifier());
                this.s.append('[');
                this.s.append(finalState.getSequenceNumber());
                this.s.append("]\n");
            } else {
                this.s.append(INDENT_1);
                this.s.append("}\n");
            }
        }
        
        private void makeVariables(State finalState) {
            final Collection<Clause> pathCondition = finalState.getPathCondition();
            for (Clause clause : pathCondition) {
                if (clause instanceof ClauseAssumeReferenceSymbolic) {
                    final ClauseAssumeReferenceSymbolic clauseRef = (ClauseAssumeReferenceSymbolic) clause;
                    final ReferenceSymbolic s = clauseRef.getReference();
                    makeVariableFor(s);
                } else if (clause instanceof ClauseAssume) {
                    final ClauseAssume clausePrim = (ClauseAssume) clause;
                    final List<PrimitiveSymbolic> symbols = symbolsIn(clausePrim.getCondition());
                    for (PrimitiveSymbolic s : symbols) {
                        makeVariableFor(s);
                    }
                } //else do nothing
            }
        }
        
        private boolean isEvoSuiteInput(String varName) {
            return !hasArrayAccessor(varName) && !hasMemberAccessor(varName);
        }
        
        private void setWithNewObject(State finalState, Symbolic symbol, long heapPosition) {
            final String expansionClass = javaClass(getTypeOfObjectInHeap(finalState, heapPosition), false);
            this.s.append(INDENT_2);
            this.s.append("pathConditionHandler.add(new SimilarityWithRefToFreshObject(\"");
            this.s.append(symbol.getOrigin());
            this.s.append("\", Class.forName(\"");
            this.s.append(expansionClass); //TODO arrays
            this.s.append("\")));\n");
        }
        
        private void setWithNull(ReferenceSymbolic symbol) {
            this.s.append(INDENT_2);
            this.s.append("pathConditionHandler.add(new SimilarityWithRefToNull(\"");
            this.s.append(symbol.getOrigin());
            this.s.append("\"));\n");
        }
        
        private void setWithAlias(State finalState, Symbolic symbol, long heapPosition) {
            final String target = getOriginOfObjectInHeap(finalState, heapPosition);
            this.s.append(INDENT_2);
            this.s.append("pathConditionHandler.add(new SimilarityWithRefToAlias(\"");
            this.s.append(symbol.getOrigin());
            this.s.append("\", \"");
            this.s.append(target);
            this.s.append("\"));\n");
        }
        
        private String javaPrimitiveType(char type) {
            if (type == Type.BOOLEAN) {
                return "boolean";
            } else if (type == Type.BYTE) {
                return "byte";
            } else if (type == Type.CHAR) {
                return "char";
            } else if (type == Type.DOUBLE) {
                return "double";
            } else if (type == Type.FLOAT) {
                return "float";
            } else if (type == Type.INT) {
                return "int";
            } else if (type == Type.LONG) {
                return "long";
            } else if (type == Type.SHORT) {
                return "short";
            } else {
                return null;
            }
        }

        private String javaClass(String type, boolean forDeclaration) {
            if (type == null) {
                return null;
            }
            final String a = type.replace('/', '.');
            final String b = (isReference(a) ? className(a) : a);
            final String s = (forDeclaration ? b.replace('$', '.') : b);
            
            if (forDeclaration) {
                final char[] tmp = s.toCharArray();
                int arrayNestingLevel = 0;
                boolean hasReference = false;
                int start = 0;
                for (int i = 0; i < tmp.length ; ++i) {
                    if (tmp[i] == '[') {
                        ++arrayNestingLevel;
                    } else if (tmp[i] == 'L') {
                        hasReference = true;
                    } else {
                        start = i;
                        break;
                    }
                }
                final StringBuilder retVal = new StringBuilder(s.substring(start, (hasReference ? tmp.length - 1 : tmp.length)));
                for (int k = 1; k <= arrayNestingLevel; ++k) {
                    retVal.append("[]");
                }
                return retVal.toString();
            } else {
                return s;
            }
        }
                
        private String generateVarNameFromOrigin(String name) {
            return name.replace("{ROOT}:", "__ROOT_");
        }
        
        private String generateOriginFromVarName(String name) {
            return name.replace("__ROOT_", "{ROOT}:");
        }
        
        private void makeVariableFor(Symbolic symbol) {
            final String origin = symbol.getOrigin().toString();
            if (!this.symbolsToVariables.containsKey(symbol)) {
                this.symbolsToVariables.put(symbol, generateVarNameFromOrigin(origin));
            }
        }
        
        private String getVariableFor(Symbolic symbol) {
            return this.symbolsToVariables.get(symbol);
        }
        
        private static String getTypeOfObjectInHeap(State finalState, long num) {
            final Map<Long, Objekt> heap = finalState.getHeap();
            final Objekt o = heap.get(num);
            return o.getType();
        }
        
        private String getOriginOfObjectInHeap(State finalState, long heapPos){
            final Collection<Clause> path = finalState.getPathCondition();
            for (Clause clause : path) {
                if (clause instanceof ClauseAssumeExpands) { // == Obj fresh
                    final ClauseAssumeExpands clauseExpands = (ClauseAssumeExpands) clause;
                    final long heapPosCurrent = clauseExpands.getHeapPosition();
                    if (heapPosCurrent == heapPos) {
                        return generateOriginFromVarName(getVariableFor(clauseExpands.getReference()));
                    }
                }
            }
            return null;
        }
        
        private boolean hasMemberAccessor(String s) {
            return (s.indexOf('.') != -1);
        }

        private boolean hasArrayAccessor(String s) {
            return (s.indexOf('[') != -1);
        }

        private void setNumericAssumption(Primitive assumption) {
            final List<PrimitiveSymbolic> symbols = symbolsIn(assumption);
            this.s.append(INDENT_2);
            this.s.append("valueCalculator = new ValueCalculator() {\n");
            this.s.append(INDENT_3);
            this.s.append("@Override public Iterable<String> getVariableOrigins() {\n");
            this.s.append(INDENT_4);
            this.s.append("ArrayList<String> retVal = new ArrayList<>();\n");       
            for (PrimitiveSymbolic symbol: symbols) {
                this.s.append(INDENT_4);
                this.s.append("retVal.add(\"");
                this.s.append(symbol.getOrigin());
                this.s.append("\");\n");
            }
            this.s.append(INDENT_4);
            this.s.append("return retVal;\n");       
            this.s.append(INDENT_3);
            this.s.append("}\n");       
            this.s.append(INDENT_3);
            this.s.append("@Override public double calculate(List<Object> variables) {\n");
            for (int i = 0; i < symbols.size(); ++i) {
                final PrimitiveSymbolic symbol = symbols.get(i); 
                this.s.append(INDENT_4);
                this.s.append("final ");
                this.s.append(javaPrimitiveType(symbol.getType()));
                this.s.append(" ");
                this.s.append(javaVariable(symbol));
                this.s.append(" = (");
                this.s.append(javaPrimitiveType(symbol.getType()));
                this.s.append(") variables.get(");
                this.s.append(i);
                this.s.append(");\n");
            }
            this.s.append(INDENT_4);
            this.s.append("return ");
            this.s.append(javaAssumptionCheck(assumption));
            this.s.append(";\n");
            this.s.append(INDENT_3);
            this.s.append("}\n");
            this.s.append(INDENT_2);
            this.s.append("};\n");
            this.s.append(INDENT_2);
            this.s.append("pathConditionHandler.add(new SimilarityWithNumericExpression(valueCalculator));\n");
        }
        
        private List<PrimitiveSymbolic> symbolsIn(Primitive e) {
            final ArrayList<PrimitiveSymbolic> symbols = new ArrayList<>();
            final PrimitiveVisitor v = new PrimitiveVisitor() {
                
                @Override
                public void visitWideningConversion(WideningConversion x) throws Exception {
                    x.getArg().accept(this);
                }
                
                @Override
                public void visitTerm(Term x) throws Exception { }
                
                @Override
                public void visitSimplex(Simplex x) throws Exception { }
                
                @Override
                public void visitPrimitiveSymbolic(PrimitiveSymbolic s) {
                    if (symbols.contains(s)) {
                        return;
                    }
                    symbols.add(s);
                }
                
                @Override
                public void visitNarrowingConversion(NarrowingConversion x) throws Exception {
                    x.getArg().accept(this);
                }
                
                @Override
                public void visitFunctionApplication(FunctionApplication x) throws Exception {
                    for (Primitive p : x.getArgs()) {
                        p.accept(this);
                    }
                }
                
                @Override
                public void visitExpression(Expression e) throws Exception {
                    if (e.isUnary()) {
                        e.getOperand().accept(this);
                    } else {
                        e.getFirstOperand().accept(this);
                        e.getSecondOperand().accept(this);
                    }
                }
                
                @Override
                public void visitAny(Any x) { }
            };
            
            try {
                e.accept(v);
            } catch (Exception exc) {
                //this should never happen
                throw new AssertionError(exc);
            }
            return symbols;
        }
        
        private String javaVariable(PrimitiveSymbolic symbol) {
            return symbol.toString().replaceAll("[\\{\\}]", "");
        }

        private String javaAssumptionCheck(Primitive assumption) {
            final ArrayList<String> translation = new ArrayList<String>(); //we use only one element as it were a reference to a String variable
            final PrimitiveVisitor v = new PrimitiveVisitor() {
                
                @Override
                public void visitWideningConversion(WideningConversion x) throws Exception {
                    x.getArg().accept(this);
                    final char argType = x.getArg().getType();
                    final char type = x.getType();
                    if (argType == Type.BOOLEAN && type == Type.INT) {
                        //operand stack widening of booleans
                        final String arg = translation.remove(0);
                        translation.add("((" + arg + ") == false ? 0 : 1)");
                    }
                }
                
                @Override
                public void visitTerm(Term x) {
                    translation.add(x.toString());
                }
                
                @Override
                public void visitSimplex(Simplex x) {
                    translation.add(x.getActualValue().toString());
                }
                
                @Override
                public void visitPrimitiveSymbolic(PrimitiveSymbolic s) {
                    translation.add(javaVariable(s));
                }
                
                @Override
                public void visitNarrowingConversion(NarrowingConversion x)
                throws Exception {
                    x.getArg().accept(this);
                    final String arg = translation.remove(0);
                    final StringBuilder b = new StringBuilder();
                    b.append("(");
                    b.append(javaPrimitiveType(x.getType()));
                    b.append(") (");
                    b.append(arg);
                    b.append(")");
                    translation.add(b.toString());
                }
                
                @Override
                public void visitFunctionApplication(FunctionApplication x)
                throws Exception {
                    final StringBuilder b = new StringBuilder();
                    b.append(x.getOperator());
                    b.append("(");
                    boolean firstDone = false;
                    for (Primitive p : x.getArgs()) {
                        if (firstDone) {
                            b.append(", ");
                        } else {
                            firstDone = true;
                        }
                        p.accept(this);
                        final String arg = translation.remove(0);
                        b.append(arg);
                    }
                    b.append(")");
                    translation.add(b.toString());
                }
                
                @Override
                public void visitExpression(Expression e) throws Exception {
                    final StringBuilder b = new StringBuilder();
                    final Operator op = e.getOperator();
                    if (e.isUnary()) {
                        e.getOperand().accept(this);
                        final String arg = translation.remove(0);
                        b.append(op == Operator.NEG ? "-" : op.toString());
                        b.append("(");
                        b.append(arg);
                        b.append(")");
                    } else { 
                        e.getFirstOperand().accept(this);
                        final String firstArg = translation.remove(0);
                        e.getSecondOperand().accept(this);
                        final String secondArg = translation.remove(0);
                        if (op.equals(Operator.EQ) ||
                            op.equals(Operator.GT) ||
                            op.equals(Operator.LT) ||
                            op.equals(Operator.GE) ||
                            op.equals(Operator.LE)) {
                            b.append("(");
                            b.append(firstArg);
                            b.append(") ");
                            b.append(op.toString());
                            b.append(" (");
                            b.append(secondArg);
                            b.append(") ? 0 : abs((");
                            b.append(firstArg);
                            b.append(") - (");
                            b.append(secondArg);
                            b.append("))");
                        } else if (op.equals(Operator.NE)) {
                            b.append("(");
                            b.append(firstArg);
                            b.append(") ");
                            b.append(op.toString());
                            b.append(" (");
                            b.append(secondArg);
                            b.append(") ? 0 : 1");
                        } else {
                            b.append("(");
                            b.append(firstArg);
                            b.append(") ");
                            if (op.equals(Operator.AND)) {
                                b.append("+");
                            } else if (op.equals(Operator.OR)) {
                                b.append("*");
                            } else {
                                b.append(op.toString());
                            }
                            b.append(" (");
                            b.append(secondArg);
                            b.append(")");
                        }
                    }
                    translation.add(b.toString());
                }
                
                @Override
                public void visitAny(Any x) throws Exception {
                    throw new Exception();
                }
            };
            
            try {
                assumption.accept(v);
            } catch (Exception exc) {
                //this may happen if Any appears in assumption
                throw new RuntimeException(exc);
            }
            return translation.remove(0);
        }
    }
}
