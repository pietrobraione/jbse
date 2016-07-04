package jbse.apps;

import static jbse.common.Type.getArrayMemberType;

import static jbse.common.Type.className;
import static jbse.common.Type.isArray;
import static jbse.common.Type.isReference;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import jbse.apps.Formatter;
import jbse.common.exc.UnexpectedInternalException;
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
import jbse.val.Primitive;
import jbse.val.PrimitiveSymbolic;
import jbse.val.PrimitiveVisitor;
import jbse.val.ReferenceSymbolic;
import jbse.val.Simplex;
import jbse.val.Symbolic;
import jbse.val.Term;
import jbse.val.WideningConversion;

/**
 * A {@link Formatter} used by Sushi (comparison with 
 * partial heap). 
 * 
 * @author Esther Turati
 * @author Pietro Braione
 */
public final class StateFormatterSushiPartialHeap implements Formatter {
    private final int methodNumber;
    private final Supplier<Long> traceCounterSupplier;
    private final Supplier<State> initialStateSupplier;
    private final Supplier<Map<PrimitiveSymbolic, Simplex>> modelSupplier;
    private StringBuilder output = new StringBuilder();
    private int testCounter = 0;
    
    public StateFormatterSushiPartialHeap(int methodNumber,
                                          Supplier<Long> traceCounterSupplier,
                                          Supplier<State> initialStateSupplier, 
                                          Supplier<Map<PrimitiveSymbolic, Simplex>> modelSupplier) {
        this.methodNumber = methodNumber;
        this.traceCounterSupplier = traceCounterSupplier;
        this.initialStateSupplier = initialStateSupplier;
        this.modelSupplier = modelSupplier;
    }

    public StateFormatterSushiPartialHeap(int methodNumber,
                                          Supplier<State> initialStateSupplier, 
                                          Supplier<Map<PrimitiveSymbolic, Simplex>> modelSupplier) {
        this(methodNumber, null, initialStateSupplier, modelSupplier);
    }

    @Override
    public void formatPrologue() {
        this.output.append(PROLOGUE);
        this.output.append('_');
        this.output.append(this.methodNumber);
        if (this.traceCounterSupplier != null) {
            this.output.append('_');
            this.output.append(this.traceCounterSupplier.get());
        }
        this.output.append(" {\n\n");
    }

    @Override
    public void formatState(State state) {
        new MethodUnderTest(this.output, this.initialStateSupplier.get(), state, this.modelSupplier.get(), this.testCounter);
        ++this.testCounter;
    }
    
    public void formatEpilogue() {
        this.output.append("}\n");
    }
    
    @Override
    public String emit() {
        return this.output.toString();
    }

    @Override
    public void cleanup() {
        this.output = new StringBuilder();
        this.testCounter = 0;
    }
    
    private static final String PROLOGUE =
        "import static sushi.compile.distance.Distance.distance;\n" +
        "\n" +
        "import sushi.compile.reflection.Allocator;\n" +
        "import sushi.compile.reflection.AccessibleObject;\n" +
        "import sushi.compile.reflection.ObjectField;\n" +
        "import sushi.logging.Level;\n" +
        "import sushi.logging.Logger;\n" +
        "\n" +
        "import java.util.HashSet;\n" +
        "\n" +
        "public class EvoSuiteWrapper";

    private static class MethodUnderTest {
        private static final String INDENT_1 = "    ";
        private static final String INDENT_2 = INDENT_1 + INDENT_1;
        private static final String INDENT_3 = INDENT_1 + INDENT_2;
        private final StringBuilder s;
        private final HashMap<Symbolic, String> symbolsToVariables = new HashMap<>();
        private final HashSet<String> evoSuiteInputVariables = new HashSet<>();
        private final HashSet<PrimitiveSymbolic> primitiveSymbolsDone = new HashSet<>();
        private boolean panic = false;
        private ClauseAssume clauseLength = null;
        
        MethodUnderTest(StringBuilder s, State initialState, State finalState, Map<PrimitiveSymbolic, Simplex> model, int testCounter) {
            this.s = s;
            makeVariables(finalState);
            appendMethodDeclaration(initialState, finalState, testCounter);
            appendInputsInitialization(finalState, model, testCounter);
            appendIfStatement(initialState, finalState, testCounter);
            appendMethodEnd(finalState, testCounter);
        }
        
        private void appendMethodDeclaration(State initialState, State finalState, int testCounter) {
            if (this.panic) {
                return;
            }
            this.s.append(INDENT_1);
            this.s.append("public double test");
            this.s.append(testCounter);
            this.s.append("(");
            final List<Symbolic> inputs;
            try {
                inputs = initialState.getStack().get(0).localVariables().values().stream()
                         .filter((v) -> v.getValue() instanceof Symbolic)
                         .map((v) -> (Symbolic) v.getValue())
                         .collect(Collectors.toList());
            } catch (IndexOutOfBoundsException e) {
                throw new UnexpectedInternalException(e);
            }            
            boolean firstDone = false;
            for (Symbolic symbol : inputs) {
                //the values of the symbolic primitives are found by the
                //solver, so EvoSuite must build only inputs of reference
                //type
                makeVariableFor(symbol);
                if (symbol instanceof ReferenceSymbolic) {
                    final String varName = getVariableFor(symbol);
                    this.evoSuiteInputVariables.add(varName);
                    if (firstDone) {
                        this.s.append(", ");
                    } else {
                        firstDone = true;
                    }
                    final String type = ((ReferenceSymbolic) symbol).getStaticType();
                    final String className = javaClass(type);
                    this.s.append(className);
                    this.s.append(' ');
                    this.s.append(varName);
                    this.s.append("_actual");
                }
            }
            this.s.append(") {\n");
            this.s.append(INDENT_2);
            this.s.append("//generated for state ");
            this.s.append(finalState.getIdentifier());
            this.s.append('[');
            this.s.append(finalState.getSequenceNumber());
            this.s.append("]\n");
        }
        
        private void appendInputsInitialization(State finalState, Map<PrimitiveSymbolic, Simplex> model, int testCounter) {
            if (this.panic) {
                return;
            }
            this.s.append(INDENT_2);
            this.s.append("final HashSet<ObjectField> initializedObjectFields = new HashSet<>();\n");
            this.s.append(INDENT_2);
            this.s.append("final Allocator alloc = Allocator.I();\n");
            final Collection<Clause> pathCondition = finalState.getPathCondition();
            for (Iterator<Clause> iterator = pathCondition.iterator(); iterator.hasNext(); ) {
                final Clause clause = iterator.next();
                this.s.append(INDENT_2);
                if (clause instanceof ClauseAssumeExpands) {
                    final ClauseAssumeExpands clauseExpands = (ClauseAssumeExpands) clause;
                    final Symbolic symbol = clauseExpands.getReference();
                    final long heapPosition = clauseExpands.getHeapPosition();
                    setWithNewObject(finalState, symbol, heapPosition, iterator, model);
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
                    if (model == null) {
                        this.panic = true;
                        return;
                    }
                    final ClauseAssume clauseAssume = (ClauseAssume) clause;
                    final Primitive assumption = clauseAssume.getCondition();
                    setNumericAssumption(assumption, model);
                } else {
                    this.s.append(';');
                }
                this.s.append(" // "); //comment
                this.s.append(clause.toString());
                if (this.clauseLength != null) {
                    this.s.append(", ");
                    this.s.append(this.clauseLength.toString());
                    this.clauseLength = null;
                }
                this.s.append('\n');
            }
        }
        
        private void appendIfStatement(State initialState, State finalState, int testCounter) {
            if (this.panic) {
                return;
            }
            this.s.append(INDENT_2);
            this.s.append("double d = distance(initializedObjectFields");
            for (String varName : this.evoSuiteInputVariables) {
                this.s.append(", ");
                this.s.append(varName);
                this.s.append(", ");
                this.s.append(varName);
                this.s.append("_actual");
            }
            this.s.append(");\n");
            this.s.append(INDENT_2);
            this.s.append("if (d == 0.0d)\n");
            this.s.append(INDENT_3);
            this.s.append("System.out.println(\"test");
            this.s.append(testCounter);
            this.s.append(" 0 distance\");\n");
            this.s.append(INDENT_2);
            this.s.append("return d;\n");
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
                this.s.append("] (no numeric solution from the solver)\n");
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
                    final Set<PrimitiveSymbolic> symbols = symbolsIn(clausePrim.getCondition());
                    for (PrimitiveSymbolic s : symbols) {
                        makeVariableFor(s);
                    }
                } //else do nothing
            }
        }
        
        private void setWithNewObject(State finalState, Symbolic symbol, long heapPosition, 
                                      Iterator<Clause> iterator, Map<PrimitiveSymbolic, Simplex> model) {        
            final String var = getVariableFor(symbol);
            final String type = getTypeOfObjectInHeap(finalState, heapPosition);
            final String instantiationStmt;
            if (isArray(type)) {
                //the next clause predicates on the array length 
                this.clauseLength = (ClauseAssume) iterator.next();
                final Simplex length = arrayLength(this.clauseLength, model);
                instantiationStmt = "alloc.newArray(\"" + javaType(getArrayMemberType(type)) + "\", " + length.toString() + ")";
            } else {
                instantiationStmt = "alloc.newInstance(\"" + javaType(type) + "\")";
            }
            if (hasMemberAccessor(var)){
                setByReflection(var, instantiationStmt);
            } else if (hasArrayAccessor(var)) {
                this.s.append(var);
                this.s.append(" = ");
                this.s.append(instantiationStmt);
                this.s.append(";");
            } else {
                this.s.append("java.lang.Object ");
                this.s.append(var);
                this.s.append(" = ");
                this.s.append(instantiationStmt);
                this.s.append(";");
            }
            if (hasMemberAccessor(var)) {
                final int splitPoint = var.lastIndexOf('.');
                this.s.append("initializedObjectFields.add(new ObjectField(");
                this.s.append(getValue(var.substring(0, splitPoint)));
                this.s.append(", \"");
                this.s.append(var.substring(splitPoint + 1));
                this.s.append("\"));");
            }
        }
        
        private void setWithNull(ReferenceSymbolic symbol) {
            final String var = getVariableFor(symbol);
            if (hasMemberAccessor(var)) {
                setByReflection(var, "null");
            } else if (hasArrayAccessor(var)) {
                this.s.append(var);
                this.s.append(" = null;");
            } else {
                this.s.append("java.lang.Object ");
                this.s.append(var);
                this.s.append(" = null;");
            }
            if (hasMemberAccessor(var)) {
                final int splitPoint = var.lastIndexOf('.');
                this.s.append("initializedObjectFields.add(new ObjectField(");
                this.s.append(getValue(var.substring(0, splitPoint)));
                this.s.append(", \"");
                this.s.append(var.substring(splitPoint + 1));
                this.s.append("\"));");
            }
        }
        
        private void setWithAlias(State finalState, Symbolic symbol, long heapPosition) {
            final String var = getVariableFor(symbol);
            final String value = getValue(getOriginOfObjectInHeap(finalState, heapPosition));
            if (hasMemberAccessor(var)) {
                setByReflection(var, value);
            } else if (hasArrayAccessor(var)) {
                this.s.append(var);
                this.s.append(" = "); 
                this.s.append(value);
                this.s.append(';'); 
            } else {
                this.s.append("java.lang.Object ");
                this.s.append(var); 
                this.s.append(" = "); 
                this.s.append(value);
                this.s.append(';'); 
            }
            if (hasMemberAccessor(var)) {
                final int splitPoint = var.lastIndexOf('.');
                this.s.append("initializedObjectFields.add(new ObjectField(");
                this.s.append(getValue(var.substring(0, splitPoint)));
                this.s.append(", \"");
                this.s.append(var.substring(splitPoint + 1));
                this.s.append("\"));");
            }
        }
        
        private Simplex arrayLength(ClauseAssume clause, Map<PrimitiveSymbolic, Simplex> model) {
            //the clause has shape {length} >= 0 - i.e., it has just
            //one symbol, the length
            final Set<PrimitiveSymbolic> symbols = symbolsIn(clause.getCondition());
            final PrimitiveSymbolic symbol = symbols.iterator().next();
            makeVariableFor(symbol); //so it remembers that the symbol has been processed
            final Simplex value = model.get(symbol);
            if (value == null) {
                //this should never happen
                throw new UnexpectedInternalException("No value found in model for symbol " + symbol.toString() + ".");
            }
            return value;
        }

        private String javaType(String s){
            if (s == null) {
                return null;
            }
            final String a = s.replace('/', '.');
            return (isReference(a) ? className(a) : a);
        }

        private String javaClass(String type){
            final String s = javaType(type).replace('$', '.');
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
        }
                
        private String generateName(String name) {
            return name.replace("{ROOT}:", "__ROOT_");
        }
        
        private void makeVariableFor(Symbolic symbol) {
            final String origin = symbol.getOrigin().toString();
            if (!this.symbolsToVariables.containsKey(symbol)) {
                this.symbolsToVariables.put(symbol, generateName(origin));
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
                    if (heapPosCurrent == heapPos){
                        return getVariableFor(clauseExpands.getReference());
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

        private String replaceAccessorsWithGetters(String container, String accessExpression) {
            String a = container;
            String s = accessExpression;    
            if (hasMemberAccessor(s)) {
                s = s.substring(s.indexOf('.') + 1);
            } else { 
                return a;
            }

            while (s != null && s.length() > 0) {
                if (hasMemberAccessor(s)){
                    int i = s.indexOf('.');
                    a = a + ".get(\"" + s.substring(0, i) + "\")";
                    s = s.substring(i + 1);
                } else {
                    a = a + ".get(\"" + s + "\")";
                    s = null;
                }            
            }
            return a;
        }
        
        private String getValue(String accessExpression) {
            if (hasMemberAccessor(accessExpression)) {
                final String container = "new AccessibleObject(" + accessExpression.substring(0, accessExpression.indexOf('.')) + ")";
                final String accessExpressionWithGetters = replaceAccessorsWithGetters(container, accessExpression);
                return accessExpressionWithGetters + ".getValue()" ;
            } else {
                return accessExpression;
            }
        }
        
        private void setByReflection(String accessExpression, String value) {
            final String container = "new AccessibleObject(" + accessExpression.substring(0, accessExpression.indexOf('.')) + ")";
            final String accessExpressionWithGetters = replaceAccessorsWithGetters(container, accessExpression.substring(0, accessExpression.lastIndexOf('.')));
            final String fieldToSet = accessExpression.substring(accessExpression.lastIndexOf('.') + 1);
            this.s.append(accessExpressionWithGetters);
            this.s.append(".set(\"");
            this.s.append(fieldToSet);
            this.s.append("\", ");
            this.s.append(value);
            this.s.append(");");
        }
        
        private void setNumericAssumption(Primitive assumption, Map<PrimitiveSymbolic, Simplex> model) {
            final Set<PrimitiveSymbolic> symbols = symbolsIn(assumption);
            for (PrimitiveSymbolic symbol : symbols) {
                if (!this.primitiveSymbolsDone.contains(symbol)) { //not yet done
                    final Simplex value = model.get(symbol);
                    if (value == null) {
                        //this should never happen
                        throw new UnexpectedInternalException("No value found in model for symbol " + symbol.toString() + ".");
                    }
                    setWithNumericValue(symbol, value);
                }
            }
            for (PrimitiveSymbolic symbol : symbols) {
                if (!this.primitiveSymbolsDone.contains(symbol)) { //not yet done
                    final String var = getVariableFor(symbol);
                    if (hasMemberAccessor(var)) {
                        final int splitPoint = var.lastIndexOf('.');
                        this.s.append("initializedObjectFields.add(new ObjectField(");
                        this.s.append(getValue(var.substring(0, splitPoint)));
                        this.s.append(", \"");
                        this.s.append(var.substring(splitPoint + 1));
                        this.s.append("\"));");
                    }
                    this.primitiveSymbolsDone.add(symbol);
                }
            }
        }
        
        private Set<PrimitiveSymbolic> symbolsIn(Primitive e) {
            final HashSet<PrimitiveSymbolic> symbols = new HashSet<>();
            PrimitiveVisitor v = new PrimitiveVisitor() {
                
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
        
        private void setWithNumericValue(PrimitiveSymbolic symbol, Simplex value) {
            final boolean variableNotYetCreated = (getVariableFor(symbol) == null);
            if (variableNotYetCreated) {
                makeVariableFor(symbol);
            }
            final String var = getVariableFor(symbol);
            if (hasMemberAccessor(var)) {
                setByReflection(var, value.toString()); 
            } else {
                this.s.append("int ");
                this.s.append(var);
                this.s.append(" = " + value.toString() + ";");
            }
        }
    }
}
