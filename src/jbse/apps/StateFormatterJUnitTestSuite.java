package jbse.apps;

import static jbse.common.Type.className;
import static jbse.common.Type.isReference;
import static jbse.common.Type.splitParametersDescriptors;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.TreeSet;
import java.util.function.Supplier;

import jbse.common.exc.UnexpectedInternalException;
import jbse.mem.Clause;
import jbse.mem.ClauseAssume;
import jbse.mem.ClauseAssumeAliases;
import jbse.mem.ClauseAssumeExpands;
import jbse.mem.ClauseAssumeNull;
import jbse.mem.Objekt;
import jbse.mem.State;
import jbse.mem.Variable;
import jbse.mem.exc.ThreadStackEmptyException;
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
 * A {@link Formatter} that emits a JUnit test suite, with 
 * test cases covering the symbolic states.
 * 
 * @author Esther Turati
 * @author Pietro Braione
 */
public abstract class StateFormatterJUnitTestSuite implements Formatter {
    protected String output = "";
    private Supplier<State> initialStateSupplier;
    private Supplier<Map<PrimitiveSymbolic, Simplex>> modelSupplier;
    private int testCounter = 0;
    
    public StateFormatterJUnitTestSuite(Supplier<State> initialStateSupplier, 
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
        final JUnitTestCase t = 
            new JUnitTestCase(this.initialStateSupplier.get(), state, this.modelSupplier.get(), this.testCounter++);
        this.output = t.get();
    }
    
    public final void formatEpilogue() {
        this.output = "}\n";
    }

    @Override
    public void cleanup() {
        this.output = "";        
    }
    
    private static final String PROLOGUE =
        "import static org.junit.Assert.*;\n" +
        "\n" +
        "import java.lang.reflect.Field;" +
        "\n" +
        "import org.junit.Test;\n" +
        "\n" +
        "public class TestSuite {\n" +
        "    private static class AccessibleObject {\n" +
        "        private final Object target;\n" +
        "        AccessibleObject(Object o) {\n" +
        "            target = o;\n" +
        "        }\n"+
        "        void set(String fieldName, Object value) {\n" +
        "            try {\n" +
        "                final Field p = target.getClass().getDeclaredField(fieldName);\n" +
        "                p.setAccessible(true);\n" +
        "                p.set(target, value);\n" +
        "            } catch (IllegalArgumentException | IllegalAccessException\n" +
        "                | NoSuchFieldException | SecurityException e) {\n" +
        "                throw new RuntimeException(e);\n" +
        "            }\n" +
        "        }\n" +
        "        AccessibleObject get(String fieldName) {\n" +
        "            try {\n" +
        "                Field p = target.getClass().getDeclaredField(fieldName);\n" +
        "                p.setAccessible(true);\n" +
        "                return new AccessibleObject(p.get(target));\n" +
        "            } catch (IllegalArgumentException | IllegalAccessException\n" +
        "                | NoSuchFieldException | SecurityException e) {\n" +
        "                throw new RuntimeException(e);\n" +
        "            }\n" +
        "        }\n" +
        "        Object getValue() {\n" +
        "            return target;\n" +
        "        }\n" +
        "    }\n" +
        "\n";

    private static class JUnitTestCase {
        private static final String INDENT = "        ";
        private final StringBuilder s = new StringBuilder(); 
        private final HashMap<String, String> symboslToVariables = new HashMap<>();
        
        JUnitTestCase(State initialState, State finalState, Map<PrimitiveSymbolic, Simplex> model, int testCounter) {
            //method declaration
            this.s.append("    @Test\n");
            this.s.append("    public void testCase");
            this.s.append(testCounter);
            this.s.append("() {\n");
            this.s.append("        //test case for state ");
            this.s.append(finalState.getIdentifier());
            this.s.append('[');
            this.s.append(finalState.getSequenceNumber());
            this.s.append("]\n");
            
            //inputs initialization
            final Collection<Clause> pathCondition = finalState.getPathCondition();
            for (Clause clause : pathCondition) {
                this.s.append(INDENT); //indent
                if (clause instanceof ClauseAssumeExpands) {
                    final ClauseAssumeExpands clauseExpands = (ClauseAssumeExpands) clause;
                    final Symbolic symbol = clauseExpands.getReference();
                    final long heapPosition = clauseExpands.getHeapPosition();
                    setWithNewObject(finalState, symbol, heapPosition);
                } else if (clause instanceof ClauseAssumeNull) { // == null
                    final ClauseAssumeNull clauseNull = (ClauseAssumeNull) clause;
                    final ReferenceSymbolic symbol = clauseNull.getReference();
                    makeVariableFor(symbol);
                    final String var = getVariableFor(symbol);
                    if (hasMemberAccessor(var)) {
                        setByReflection(var, "null");
                    } else {
                        final String type = javaType(symbol.getStaticType());
                        this.s.append(type);
                        this.s.append(' ');
                        this.s.append(var);
                        this.s.append(" = null;");
                    }
                } else if (clause instanceof ClauseAssumeAliases) {
                    final ClauseAssumeAliases clauseAliases = (ClauseAssumeAliases) clause;
                    final Symbolic symbol = clauseAliases.getReference();
                    makeVariableFor(symbol);
                    final String var = getVariableFor(symbol);
                    final long heapPosition = clauseAliases.getHeapPosition();
                    final String value = getValueByReflection(getOriginOfObjectInHeap(finalState, heapPosition));
                    if (hasMemberAccessor(var)) {
                        setByReflection(var, value);
                    } else {
                        final String type = javaType(getClassOfObjectInHeap(finalState, heapPosition));
                        this.s.append(type);
                        this.s.append(' '); 
                        this.s.append(var); 
                        this.s.append(" = "); 
                        this.s.append(value);
                        this.s.append(';'); 
                    }
                } else if (clause instanceof ClauseAssume) {
                    if (model == null) {
                        //panic: no model
                        this.s.delete(0, s.length());
                        this.s.append("    //Unable to generate test case ");
                        this.s.append(testCounter);
                        this.s.append(" for state ");
                        this.s.append(finalState.getIdentifier());
                        this.s.append('[');
                        this.s.append(finalState.getSequenceNumber());
                        this.s.append("] (no numeric solution from the solver)\n");
                        return;
                    }
                    final ClauseAssume clauseAssume = (ClauseAssume) clause;
                    final Primitive p = clauseAssume.getCondition();
                    this.s.append(primitiveSymbolAssignments(p, model));
                } else {
                    this.s.append(';');
                }
                this.s.append(" // "); //comment
                this.s.append(clause.toString());
                this.s.append('\n');
            
            }
            
            //test method invocation
            this.s.append(INDENT);
            try {
                final String methodName = initialState.getRootMethodSignature().getName();
                if ("this".equals(initialState.getRootFrame().getLocalVariableDeclaredName(0))) {
                    this.s.append("__ROOT_this.");
                    this.s.append(methodName);
                } else {
                    this.s.append(methodName);
                }
                this.s.append('(');
                final Map<Integer, Variable> lva = initialState.getRootFrame().localVariables();
                final TreeSet<Integer> slots = new TreeSet<>(lva.keySet());
                final int numParams = splitParametersDescriptors(initialState.getRootMethodSignature().getDescriptor()).length;
                int currentParam = 1;
                for (int slot : slots) {
                    final Variable lv = lva.get(slot);
                    if ("this".equals(lv.getName())) {
                        continue;
                    }
                    if (currentParam > numParams) {
                        break;
                    }
                    if (currentParam > 1) {
                        s.append(", ");
                    }
                    final String variable = "__ROOT_" + lv.getName();
                    if (this.symboslToVariables.containsValue(variable)) {
                        this.s.append(variable);
                    } else if (jbse.common.Type.isPrimitiveIntegral(lv.getType().charAt(0))) {
                        this.s.append('0');
                    } else if (jbse.common.Type.isPrimitiveFloating(lv.getType().charAt(0))) {
                        this.s.append("0.0f");
                    } else {
                        this.s.append("null");
                    }
                    ++currentParam;
                }
                this.s.append(");\n");
            } catch (ThreadStackEmptyException e) {
                //TODO
                throw new RuntimeException(e);
            }
            
            //end of the method
            this.s.append("    }\n");
        }
        
        String get() {
            return s.toString();
        }   
        
        private void setWithNewObject(State finalState, Symbolic symbol, long heapPosition) {        
            makeVariableFor(symbol);
            final String var = getVariableFor(symbol);
            final String type = javaType(getClassOfObjectInHeap(finalState, heapPosition));
            if (hasMemberAccessor(var)){
                setByReflection(var, "new " + type + "()");
            } else {
                this.s.append(type);
                this.s.append(' ');
                this.s.append(var);
                this.s.append(" = new ");
                this.s.append(type);
                this.s.append("();");
            }
        }

        private String javaType(String s){
            if (s == null) {
                return null;
            }
            final String a = s.replace('/', '.').replace('$', '.');
            return (isReference(a) ? className(a) : a);
        }
        
        
        private String generateName(String name) {
            return name.replace("{ROOT}:", "__ROOT_");
        }
        
        private void makeVariableFor(Symbolic symbol) {
            final String value = symbol.getValue(); 
            final String origin = symbol.getOrigin();
            if (!this.symboslToVariables.containsKey(value)) {
                this.symboslToVariables.put(value, generateName(origin));
            }
        }
        
        private String getVariableFor(Symbolic symbol) {
            final String value = symbol.getValue(); 
            return this.symboslToVariables.get(value);
        }
        
        private static String getClassOfObjectInHeap(State finalState, long num) {
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
        
        private String getValueByReflection(String accessExpression) {
            if (hasMemberAccessor(accessExpression)) {
                final String container = "new AccessibleObject(" + accessExpression.substring(0, accessExpression.indexOf('.')) + ")";
                final String accessExpressionWithGetters = replaceAccessorsWithGetters(container, accessExpression);
                return accessExpressionWithGetters + ".getValue()" ;
            } else {
                return accessExpression;
            }
        }
        
        private void setByReflection(String accessExpression, String value) {
            if (hasMemberAccessor(accessExpression)) {
                final String container = "new AccessibleObject(" + accessExpression.substring(0, accessExpression.indexOf('.')) + ")";
                final String accessExpressionWithGetters = replaceAccessorsWithGetters(container, accessExpression.substring(0, accessExpression.lastIndexOf('.')));
                final String fieldToSet = accessExpression.substring(accessExpression.lastIndexOf('.') + 1);
                this.s.append(accessExpressionWithGetters);
                this.s.append(".set(\"");
                this.s.append(fieldToSet);
                this.s.append("\", ");
                this.s.append(value);
                this.s.append(");");
            } else {
                this.s.append(accessExpression);
            }
        }
        
        private String primitiveSymbolAssignments(Primitive e, Map<PrimitiveSymbolic, Simplex> model) {
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
            
            final StringBuilder s = new StringBuilder();
            for (PrimitiveSymbolic symbol : symbols) {
                if (getVariableFor(symbol) == null) { //not yet done
                    final Simplex value = model.get(symbol);
                    if (value == null) {
                        //this should never happen
                        throw new UnexpectedInternalException("No value found in model for symbol " + symbol.toString() + ".");
                    } else {
                        setWithNumericValue(symbol, value);
                    }
                    s.append(' ');
                }
            }
            return s.toString();
        }
        
        private void setWithNumericValue(PrimitiveSymbolic symbol, Simplex value) {
            final boolean variableNotYetCreated = (getVariableFor(symbol) == null);
            if (variableNotYetCreated) {
                makeVariableFor(symbol);
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
}
