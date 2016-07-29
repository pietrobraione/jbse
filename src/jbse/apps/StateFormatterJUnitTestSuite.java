package jbse.apps;

import static jbse.common.Type.getArrayMemberType;
import static jbse.common.Type.className;
import static jbse.common.Type.isArray;
import static jbse.common.Type.isReference;
import static jbse.common.Type.isPrimitive;
import static jbse.common.Type.isPrimitiveFloating;
import static jbse.common.Type.isPrimitiveIntegral;
import static jbse.common.Type.splitParametersDescriptors;
import static jbse.common.Type.splitReturnValueDescriptor;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Supplier;

import jbse.common.Type;
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
import jbse.val.Reference;
import jbse.val.ReferenceSymbolic;
import jbse.val.Simplex;
import jbse.val.Symbolic;
import jbse.val.Term;
import jbse.val.Value;
import jbse.val.WideningConversion;

/**
 * A {@link Formatter} that emits a JUnit test suite, with 
 * test cases covering the symbolic states.
 * 
 * @author Esther Turati
 * @author Pietro Braione
 */
public final class StateFormatterJUnitTestSuite implements Formatter {
    private final Supplier<State> initialStateSupplier;
    private final Supplier<Map<PrimitiveSymbolic, Simplex>> modelSupplier;
    private StringBuilder output = new StringBuilder();
    private int testCounter = 0;
    
    public StateFormatterJUnitTestSuite(Supplier<State> initialStateSupplier, 
                                        Supplier<Map<PrimitiveSymbolic, Simplex>> modelSupplier) {
        this.initialStateSupplier = initialStateSupplier;
        this.modelSupplier = modelSupplier;
    }

    @Override
    public void formatPrologue() {
        this.output.append(PROLOGUE);
    }

    @Override
    public void formatState(State state) {
        new JUnitTestCase(this.output, this.initialStateSupplier.get(), state, this.modelSupplier.get(), this.testCounter++);
    }
    
    @Override
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
    }
    
    private static final String PROLOGUE =
        "import static java.lang.System.identityHashCode;\n" +
        "import static org.junit.Assert.*;\n" +
        "\n" +
        "import java.lang.reflect.Array;\n" +
        "import java.lang.reflect.Field;\n" +
        "import java.util.HashSet;\n" +
        "import sun.misc.Unsafe;\n" +
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
        "                final Field p = target.getClass().getDeclaredField(fieldName);\n" +
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
        "\n" +
        "    private static final Unsafe UNSAFE; //ugly!\n" +
        "\n" +
        "    static {\n" +
        "        final Field uns;\n" +
        "        try {\n" +
        "            uns = Unsafe.class.getDeclaredField(\"theUnsafe\");\n" +
        "            uns.setAccessible(true);\n" +
        "            UNSAFE = (Unsafe) uns.get(null);\n" +
        "        } catch (NoSuchFieldException e) {\n" +
        "            throw new RuntimeException(e);\n" +
        "        } catch (IllegalAccessException e) {\n" +
        "            throw new RuntimeException(e);\n" +
        "        }\n" +
        "    }\n" +
        "\n" +
        "    private static Object newInstance(String type) {\n" +
        "        try {\n"+
        "            final Class<?> clazz = Class.forName(type);\n" +
        "            return clazz.cast(UNSAFE.allocateInstance(clazz));\n" +
        "        } catch (ClassNotFoundException e) {\n" +
        "            throw new RuntimeException(e);\n" +
        "        } catch (InstantiationException e) {\n" +
        "            throw new RuntimeException(e);\n" +
        "        }\n" +
        "    }\n" +
        "\n" +
        "    private static Object newArray(String memberType, int length) {\n" +
        "        try {\n" +
        "            final Class<?> clazz = Class.forName(memberType);\n" +
        "            return Array.newInstance(clazz, length);\n" +
        "        } catch (ClassNotFoundException e) {\n" +
        "            throw new RuntimeException(e);\n" +
        "        }\n" +
        "    }\n" +
        "\n" +
        "    public class ObjectField {\n" +
        "        private final Object obj;\n" +
        "        private final Field fld;\n" +
        "        public ObjectField(Object obj, String fldName) {\n" +
        "            this.obj = obj;\n" +
        "            try {\n" + 
        "                this.fld = obj.getClass().getDeclaredField(fldName);\n" +
        "            } catch (NoSuchFieldException | SecurityException e) {\n" +
        "                throw new RuntimeException(e);\n" +
        "            }\n" +
        "        }\n" +
        "        @Override\n" +
        "        public int hashCode() {\n" +
        "            final int prime = 31;\n" +
        "            int result = 1;\n" +
        "            result = prime * result + ((fld == null) ? 0 : fld.hashCode());\n" +
        "            result = prime * result + ((obj == null) ? 0 : identityHashCode(obj));\n" +
        "            return result;\n" +
        "        }\n" +
        "        @Override\n" +
        "        public boolean equals(Object obj) {\n" +
        "            if (this == obj) {\n" +
        "                return true;\n" +
        "            }\n" +
        "            if (obj == null) {\n" +
        "                return false;\n" +
        "            }\n" +
        "            if (getClass() != obj.getClass()) {\n" +
        "                return false;\n" +
        "            }\n" +
        "            final ObjectField other = (ObjectField) obj;\n" +
        "            if (this.fld == null) {\n" +
        "                if (other.fld != null) {\n" +
        "                    return false;\n" +
        "                }\n" +
        "            } else if (!fld.equals(other.fld)) {\n" +
        "                return false;\n" +
        "            }\n" +
        "            if (this.obj == null) {\n" +
        "                if (other.obj != null) {\n" +
        "                    return false;\n" +
        "                }\n" +
        "            } else if (this.obj != other.obj) {\n" +
        "                return false;\n" +
        "            }\n" +
        "            return true;\n" +
        "        }\n" +
        "    }\n" +
        "\n" +
        "    public HashSet<ObjectField> nullObjectFields;\n" +
        "\n";

    private static class JUnitTestCase {
        private static final String INDENT = "        ";
        private final StringBuilder s; 
        private final HashMap<String, String> symbolsToVariables = new HashMap<>();
        private boolean panic = false;
        private ClauseAssume clauseLength = null;
        
        JUnitTestCase(StringBuilder s, State initialState, State finalState, Map<PrimitiveSymbolic, Simplex> model, int testCounter) {
            this.s = s;
            appendMethodDeclaration(finalState, testCounter);
            appendInputsInitialization(finalState, model, testCounter);
            appendInvocationOfMethodUnderTest(initialState, finalState);
            appendAssert(initialState, finalState);
            appendMethodEnd(finalState, testCounter);
        }
        
        private void appendMethodDeclaration(State finalState, int testCounter) {
            if (this.panic) {
                return;
            }
            final Reference exception = finalState.getStuckException();
            if (exception == null) {
                this.s.append("    @Test\n");
            } else {
                this.s.append("    @Test(expected=");
                this.s.append(javaClass(finalState.getObject(exception).getType()));
                this.s.append(".class)\n");
            }
            this.s.append("    public void test");
            this.s.append(testCounter);
            this.s.append("() {\n");
            this.s.append("        //test case for state ");
            this.s.append(finalState.getIdentifier());
            this.s.append('[');
            this.s.append(finalState.getSequenceNumber());
            this.s.append("]\n");
        }
        
        private void appendInputsInitialization(State finalState, Map<PrimitiveSymbolic, Simplex> model, int testCounter) {
            if (this.panic) {
                return;
            }
            this.s.append(INDENT);
            this.s.append("this.nullObjectFields = new HashSet<>();\n");
            final Collection<Clause> pathCondition = finalState.getPathCondition();
            for (Iterator<Clause> iterator = pathCondition.iterator(); iterator.hasNext(); ) {
                final Clause clause = iterator.next();
                this.s.append(INDENT);
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
                    final Primitive p = clauseAssume.getCondition();
                    this.s.append(primitiveSymbolAssignments(p, model));
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
        
        private void appendInvocationOfMethodUnderTest(State initialState, State finalState) {
            if (this.panic) {
                return;
            }
            final Value returnedValue = finalState.getStuckReturn();
            final boolean mustCheckReturnedValue = 
                (returnedValue != null)  && (isPrimitive(returnedValue.getType()) || returnedValue instanceof Symbolic);
            this.s.append(INDENT);
            try {
                if (mustCheckReturnedValue) {
                    final char returnType = splitReturnValueDescriptor(initialState.getRootMethodSignature().getDescriptor()).charAt(0);
                    if (returnType == Type.CHAR) {
                        this.s.append("char");
                    } else if (returnType == Type.BOOLEAN) {
                        this.s.append("boolean");
                    } else if (isPrimitiveIntegral(returnType)) {
                        this.s.append("long");
                    } else if (isPrimitiveFloating(returnType)) {
                        this.s.append("double");
                    } else {
                        final Reference returnedRef = (Reference) returnedValue;
                        if (finalState.isNull(returnedRef)) {
                            this.s.append("java.lang.Object");
                        } else {
                            this.s.append(javaClass(finalState.getObject(returnedRef).getType()));
                        }
                    }
                    this.s.append(" __returnedValue = ");
                }
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
                    if (this.symbolsToVariables.containsValue(variable)) {
                        this.s.append(variable);
                    } else if (isPrimitiveIntegral(lv.getType().charAt(0))) {
                        this.s.append('0');
                    } else if (isPrimitiveFloating(lv.getType().charAt(0))) {
                        this.s.append("0.0f");
                    } else {
                        this.s.append("null");
                    }
                    ++currentParam;
                }
                this.s.append(");\n");
            } catch (ThreadStackEmptyException e) {
                //this should never happen
                throw new UnexpectedInternalException(e);
            }
        }
        
        private void appendAssert(State initialState, State finalState) {
            if (this.panic) {
                return;
            }
            final Value returnedValue = finalState.getStuckReturn();
            final boolean mustCheckReturnedValue = 
                (returnedValue != null)  && (isPrimitive(returnedValue.getType()) || returnedValue instanceof Symbolic);
            if (mustCheckReturnedValue) {
                this.s.append(INDENT);
                this.s.append("assertTrue(__returnedValue == ");
                final char returnType;
                try {
                    returnType = splitReturnValueDescriptor(initialState.getRootMethodSignature().getDescriptor()).charAt(0);
                } catch (ThreadStackEmptyException e) {
                    //this should never happen
                    throw new UnexpectedInternalException(e);
                }
                if (returnType == Type.BOOLEAN) {
                    if (returnedValue instanceof Simplex) {
                        final Simplex returnedValueSimplex = (Simplex) returnedValue;
                        this.s.append(returnedValueSimplex.isZeroOne(true) ? "false" : "true");
                    } else {
                        this.s.append(returnedValue.toString());
                    }
                } else if (isPrimitive(returnType)) {
                    if (returnedValue instanceof Simplex) {
                        if (returnType == Type.BYTE) {
                            this.s.append("(byte) "); 
                        } else if (returnType == Type.CHAR) {
                            this.s.append("(char) ");
                        } else if (returnType == Type.SHORT) {
                            this.s.append("(short) "); 
                        }
                    }
                    this.s.append(returnedValue.toString());
                } else {
                    final Reference returnedRef = (Reference) returnedValue;
                    if (finalState.isNull(returnedRef)) {
                        this.s.append("null");
                    } else {
                        final String var = generateName(finalState.getObject(returnedRef).getOrigin().toString());
                        if (hasMemberAccessor(var)) {
                            this.s.append(getValue(var));
                        } else {
                            this.s.append(var);
                        }
                    }
                }
                this.s.append(");\n");
            }
        }
        
        private void appendMethodEnd(State finalState, int testCounter) {
            if (this.panic) {
                this.s.delete(0, s.length());
                this.s.append("    //Unable to generate test case ");
                this.s.append(testCounter);
                this.s.append(" for state ");
                this.s.append(finalState.getIdentifier());
                this.s.append('[');
                this.s.append(finalState.getSequenceNumber());
                this.s.append("] (no numeric solution from the solver)\n");
            } else {
                this.s.append("    }\n");
            }
        }
        
        private void setWithNewObject(State finalState, Symbolic symbol, long heapPosition, 
                                      Iterator<Clause> iterator, Map<PrimitiveSymbolic, Simplex> model) {        
            makeVariableFor(symbol);
            final String var = getVariableFor(symbol);
            final String type = getTypeOfObjectInHeap(finalState, heapPosition);
            final String instantiationStmt;
            if (isArray(type)) {
                //the next clause predicates on the array length 
                this.clauseLength = (ClauseAssume) iterator.next();
                final Simplex length = arrayLength(this.clauseLength, model);
                instantiationStmt = "newArray(\"" + javaType(getArrayMemberType(type)) + "\", " + length.toString() + ")";
            } else {
                instantiationStmt = "newInstance(\"" + javaType(type) + "\")";
            }
            final String className = javaClass(type);
            if (hasMemberAccessor(var)){
                setByReflection(var, instantiationStmt);
            } else if (hasArrayAccessor(var)) {
                this.s.append(var);
                this.s.append(" = (");
                this.s.append(className);
                this.s.append(") ");
                this.s.append(instantiationStmt);
                this.s.append(";");
            } else {
                this.s.append(className);
                this.s.append(' ');
                this.s.append(var);
                this.s.append(" = (");
                this.s.append(className);
                this.s.append(") ");
                this.s.append(instantiationStmt);
                this.s.append(";");
            }
        }
        
        private void setWithNull(ReferenceSymbolic symbol) {
            makeVariableFor(symbol);
            final String var = getVariableFor(symbol);
            if (hasMemberAccessor(var)) {
                setByReflection(var, "null");
            } else if (hasArrayAccessor(var)) {
                this.s.append(var);
                this.s.append(" = null;");
            } else {
                final String type = javaClass(symbol.getStaticType());
                this.s.append(type);
                this.s.append(' ');
                this.s.append(var);
                this.s.append(" = null;");
            }
            if (hasMemberAccessor(var)) {
                final int splitPoint = var.lastIndexOf('.');
                this.s.append("this.nullObjectFields.add(new ObjectField(");
                this.s.append(getValue(var.substring(0, splitPoint)));
                this.s.append(", \"");
                this.s.append(var.substring(splitPoint + 1));
                this.s.append("\"));");
            }
        }
        
        private void setWithAlias(State finalState, Symbolic symbol, long heapPosition) {
            makeVariableFor(symbol);
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
                final String type = javaClass(getTypeOfObjectInHeap(finalState, heapPosition));
                this.s.append(type);
                this.s.append(' '); 
                this.s.append(var); 
                this.s.append(" = "); 
                this.s.append(value);
                this.s.append(';'); 
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
            final String value = symbol.getValue(); 
            final String origin = symbol.getOrigin().toString();
            if (!this.symbolsToVariables.containsKey(value)) {
                this.symbolsToVariables.put(value, generateName(origin));
            }
        }
        
        private String getVariableFor(Symbolic symbol) {
            final String value = symbol.getValue(); 
            return this.symbolsToVariables.get(value);
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
        
        private String primitiveSymbolAssignments(Primitive e, Map<PrimitiveSymbolic, Simplex> model) {
            final Set<PrimitiveSymbolic> symbols = symbolsIn(e);
            final StringBuilder s = new StringBuilder();
            for (PrimitiveSymbolic symbol : symbols) {
                if (getVariableFor(symbol) == null) { //not yet done
                    final Simplex value = model.get(symbol);
                    if (value == null) {
                        //this should never happen
                        throw new UnexpectedInternalException("No value found in model for symbol " + symbol.toString() + ".");
                    }
                    setWithNumericValue(symbol, value);
                    s.append(' ');
                }
            }
            return s.toString();
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
                if (symbol.getType() == Type.BOOLEAN) {
                    setByReflection(var, "(" + value.toString() + " != 0)");
                } else if (symbol.getType() == Type.BYTE) {
                    setByReflection(var, "(byte) " + value.toString());
                } else if (symbol.getType() == Type.CHAR) {
                    setByReflection(var, "(char) " + value.toString());
                } else if (symbol.getType() == Type.SHORT) {
                    setByReflection(var, "(short) " + value.toString());
                } else {
                    setByReflection(var, value.toString());
                }
            } else {
                //TODO floating point values
                this.s.append("long ");
                this.s.append(var);
                this.s.append(" = " + value.toString() + ";");
            }
        }
    }
}
