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
import static jbse.common.Type.toPrimitiveOrVoidCanonicalName;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Supplier;

import jbse.bc.Signature;
import jbse.common.Type;
import jbse.common.exc.InvalidInputException;
import jbse.common.exc.UnexpectedInternalException;
import jbse.mem.Clause;
import jbse.mem.ClauseAssume;
import jbse.mem.ClauseAssumeAliases;
import jbse.mem.ClauseAssumeExpands;
import jbse.mem.ClauseAssumeNull;
import jbse.mem.Objekt;
import jbse.mem.State;
import jbse.mem.Variable;
import jbse.mem.exc.FrozenStateException;
import jbse.mem.exc.ThreadStackEmptyException;
import jbse.val.Any;
import jbse.val.Expression;
import jbse.val.PrimitiveSymbolicApply;
import jbse.val.PrimitiveSymbolicHashCode;
import jbse.val.PrimitiveSymbolicLocalVariable;
import jbse.val.PrimitiveSymbolicMemberArray;
import jbse.val.PrimitiveSymbolicMemberArrayLength;
import jbse.val.PrimitiveSymbolicMemberField;
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
    private final StringBuilder output = new StringBuilder();
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
        this.output.delete(0, this.output.length());
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
        "        void setValue(String fieldName, Object value) {\n" +
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
        private final HashMap<String, String> symbolsToVariables = new HashMap<>();  //TODO can't we just use the symbol as key?
        private boolean panic = false;
        private ClauseAssume clauseLength = null;

        JUnitTestCase(StringBuilder s, State initialState, State finalState, Map<PrimitiveSymbolic, Simplex> model, int testCounter) {
            this.s = new StringBuilder();
            try { 
	            appendMethodDeclaration(finalState, testCounter);
	            appendInputsInitialization(finalState, model, testCounter);
	            appendInvocationOfMethodUnderTest(initialState, finalState);
	            appendAssert(initialState, finalState);
	            appendMethodEnd(finalState, testCounter);
            } catch (InvalidInputException e) {
            	this.s.delete(0, this.s.length());
                this.s.append("    //Unable to generate test case ");
                this.s.append(testCounter);
                this.s.append(" for state ");
                this.s.append(finalState.getBranchIdentifier());
                this.s.append('[');
                this.s.append(finalState.getSequenceNumber());
                this.s.append("] (raised InvalidInputException");
                if (e.getMessage() != null) {
                    this.s.append(", message: ");
                    this.s.append(e.getMessage());
                }
                this.s.append(")\n");
            }
            s.append(this.s);
        }

        private void appendMethodDeclaration(State finalState, int testCounter) 
        throws InvalidInputException {
            if (this.panic) {
                return;
            }
            final Reference exception = finalState.getStuckException();
            if (exception == null) {
                this.s.append("    @Test\n");
            } else {
                this.s.append("    @Test(expected=");
                this.s.append(javaClass(finalState.getObject(exception).getType().getClassName()));
                this.s.append(".class)\n");
            }
            this.s.append("    public void test");
            this.s.append(testCounter);
            this.s.append("() {\n");
            this.s.append("        //test case for state ");
            this.s.append(finalState.getBranchIdentifier());
            this.s.append('[');
            this.s.append(finalState.getSequenceNumber());
            this.s.append("]\n");
        }

        private void appendInputsInitialization(State finalState, Map<PrimitiveSymbolic, Simplex> model, int testCounter) 
        throws InvalidInputException {
            if (this.panic) {
                return;
            }
            this.s.append(INDENT);
            this.s.append("this.nullObjectFields = new HashSet<>();\n");
            final Collection<Clause> pathCondition = finalState.getPathCondition();
            for (Iterator<Clause> iterator = pathCondition.iterator(); iterator.hasNext(); ) {
                final Clause clause = iterator.next();
                final boolean clausePrinted;
                if (clause instanceof ClauseAssumeExpands) {
                	clausePrinted = true;
                    this.s.append(INDENT);
                    final ClauseAssumeExpands clauseExpands = (ClauseAssumeExpands) clause;
                    final Symbolic symbol = clauseExpands.getReference();
                    final long heapPosition = clauseExpands.getHeapPosition();
                    appendSetWithNewObject(symbol, finalState, heapPosition, iterator, model);
                } else if (clause instanceof ClauseAssumeNull) {
                	clausePrinted = true;
                    this.s.append(INDENT);
                    final ClauseAssumeNull clauseNull = (ClauseAssumeNull) clause;
                    final ReferenceSymbolic symbol = clauseNull.getReference();
                    appendSetWithNull(symbol);
                } else if (clause instanceof ClauseAssumeAliases) {
                	clausePrinted = true;
                    this.s.append(INDENT);
                    final ClauseAssumeAliases clauseAliases = (ClauseAssumeAliases) clause;
                    final Symbolic symbol = clauseAliases.getReference();
                    final long heapPosition = clauseAliases.getHeapPosition();
                    appendSetWithAlias(symbol, finalState, heapPosition);
                } else if (clause instanceof ClauseAssume) {
                	clausePrinted = true;
                    this.s.append(INDENT);
                    final ClauseAssume clauseAssume = (ClauseAssume) clause;
                    final Primitive p = clauseAssume.getCondition();
                    final Set<PrimitiveSymbolic> symbols = primitiveSymbolsIn(p);
                    appendSetWithNumericValues(symbols, model);
                } else {
                	//clause to skip
                    clausePrinted = false;
                }
                if (this.panic) {
                	//we panicked: we cannot do anything
                	//else but return immediately from
                	//this method (this.s will be reset
                	//anyways)
                	return;
                }
                if (clausePrinted) {
                	//we append a comment that describes the clause
                	this.s.append(" // "); 
                	this.s.append(clause.toString());
                	if (this.clauseLength != null) {
                		//it is possible that we printed two clauses, in
                		//the second clause is a clause describing the
                		//initial assumption on an array's length; we
                		//add also this clause's description to the comment
                		this.s.append(", ");
                		this.s.append(this.clauseLength.toString());
                		this.clauseLength = null; //reset for next iteration
                	}
                	this.s.append('\n');
                }
            }
        }

        private void appendInvocationOfMethodUnderTest(State initialState, State finalState) 
        throws InvalidInputException {
            if (this.panic) {
                return;
            }
            final Value returnedValue = finalState.getStuckReturn();
            final boolean mustCheckReturnedValue = 
                (returnedValue != null)  && (isPrimitive(returnedValue.getType()) || returnedValue instanceof Symbolic);
            this.s.append(INDENT);
            try {
            	final Signature rootMethodSignature = initialState.getRootMethodSignature();
                if (mustCheckReturnedValue) {
                    final char methodReturnType = splitReturnValueDescriptor(rootMethodSignature.getDescriptor()).charAt(0);
                    if (methodReturnType == Type.CHAR) {
                        this.s.append("char");
                    } else if (methodReturnType == Type.BOOLEAN) {
                        this.s.append("boolean");
                    } else if (isPrimitiveIntegral(methodReturnType)) {
                        this.s.append("long");
                    } else if (isPrimitiveFloating(methodReturnType)) {
                        this.s.append("double");
                    } else {
                        final Reference returnedRef = (Reference) returnedValue;
                        if (finalState.isNull(returnedRef)) {
                            this.s.append("java.lang.Object");
                        } else {
                            this.s.append(javaClass(finalState.getObject(returnedRef).getType().getClassName()));
                        }
                    }
                    this.s.append(" __returnedValue = ");
                }
                final String methodName = rootMethodSignature.getName();
                if ("this".equals(initialState.getRootFrame().getLocalVariableDeclaredName(0))) {
                    this.s.append("__ROOT_this.");
                    this.s.append(methodName);
                } else {
                    this.s.append(methodName);
                }
                this.s.append('(');
                final Map<Integer, Variable> lva = initialState.getRootFrame().localVariables();
                final TreeSet<Integer> slots = new TreeSet<>(lva.keySet());
                final int numParamsExcludedThis = splitParametersDescriptors(rootMethodSignature.getDescriptor()).length;
                int currentParam = 1;
                for (int slot : slots) {
                    final Variable lv = lva.get(slot);
                    if ("this".equals(lv.getName())) {
                        continue;
                    }
                    if (currentParam > numParamsExcludedThis) {
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

        private void appendAssert(State initialState, State finalState) throws InvalidInputException {
            if (this.panic) {
                return;
            }
            final Value returnedValue = finalState.getStuckReturn();
            final boolean mustCheckReturnedValue = 
            (returnedValue != null)  && (isPrimitive(returnedValue.getType()) || returnedValue instanceof Symbolic);
            if (mustCheckReturnedValue) {
                this.s.append(INDENT);
                this.s.append("assertTrue(__returnedValue == ");
                final char methodReturnType;
                try {
                    methodReturnType = splitReturnValueDescriptor(initialState.getRootMethodSignature().getDescriptor()).charAt(0);
                } catch (ThreadStackEmptyException e) {
                    //this should never happen
                    throw new UnexpectedInternalException(e);
                }
                if (methodReturnType == Type.BOOLEAN) {
                    if (returnedValue instanceof Simplex) {
                        final Simplex returnedValueSimplex = (Simplex) returnedValue;
                        this.s.append(returnedValueSimplex.isZeroOne(true) ? "false" : "true");
                    } else {
                        this.s.append(returnedValue.toString());
                    }
                } else if (isPrimitive(methodReturnType)) {
                    if (returnedValue instanceof Simplex) {
                        if (methodReturnType == Type.BYTE) {
                            this.s.append("(byte) "); 
                        } else if (methodReturnType == Type.CHAR) {
                            this.s.append("(char) ");
                        } else if (methodReturnType == Type.SHORT) {
                            this.s.append("(short) "); 
                        } //else, no cast is necessary
                    }
                    this.s.append(returnedValue.toString());
                } else {
                    final Reference returnedRef = (Reference) returnedValue;
                    if (finalState.isNull(returnedRef)) {
                        this.s.append("null");
                    } else {
                    	final String returnedRefOriginString = finalState.getObject(returnedRef).getOrigin().asOriginString();
                    	appendGetValue(returnedRefOriginString);
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
                this.s.append(finalState.getBranchIdentifier());
                this.s.append('[');
                this.s.append(finalState.getSequenceNumber());
                this.s.append("] (no model - aka numeric solution to path condition - from the solver)\n");
            } else {
                this.s.append("    }\n");
            }
        }

        private void appendSetWithNewObject(Symbolic symbol, State finalState, long heapPosition, 
                                            Iterator<Clause> iterator, Map<PrimitiveSymbolic, Simplex> model) 
        throws InvalidInputException {        
            makeVariableFor(symbol);
            final String var = getVariableFor(symbol);
            final String type = getTypeOfObjectInHeap(finalState, heapPosition); //the type of the new object
            final PrimitiveSymbolic symbolLength; //if the new object is an array, here we store the symbol for its length
            final Simplex valueLength; //if the new object is an array, here we store the concrete value for its length
            final String createNewObjectJavaExpression; //the Java expression that creates the new object
            if (isArray(type)) {
                //the next clause in the path condition predicates on the array length 
                this.clauseLength = (ClauseAssume) iterator.next(); //we store the clause just for generating a comment in code
                symbolLength = getArrayLength(this.clauseLength);
            	//we need a concrete primitive value for the
            	//array length: if we do not have a model we 
                //panic
                if (model == null) {
                    this.panic = true;
                    return;
                }
                valueLength = model.get(symbolLength);
                if (valueLength == null) {
                	//we REALLY need a concrete primitive value
                	//for the array length
                    throw new InvalidInputException("No value found in model for symbol " + symbolLength.toString() + ", that is the length of an array.");
                }
                createNewObjectJavaExpression = "(" + javaClass(type) + ") newArray(\"" + javaType(getArrayMemberType(type)) + "\", " + valueLength.toString() + ")";
            } else {
            	symbolLength = null;
            	valueLength = null;
                createNewObjectJavaExpression = "(" + javaClass(type) + ") newInstance(\"" + javaType(type) + "\")";
            }
            appendSetValue(javaClass(type), var, createNewObjectJavaExpression);
            
            //if we have read the clause for the array length
            //we must also emit the symbols in this clause
            //(we are lucky, it is just one)
            if (this.clauseLength != null) {
            	appendSetWithNumericValue(symbolLength, valueLength);
            }
        }

        private void appendSetWithNull(ReferenceSymbolic symbol) 
        throws InvalidInputException {
            makeVariableFor(symbol);
            final String var = getVariableFor(symbol);
            appendSetValue(javaClass(symbol.getStaticType()), var, "null");
            
            if (hasMemberAccessor(var)) {
            	//also adds var to the set of the null object fields
                final int splitPoint = var.lastIndexOf('.');
                this.s.append("this.nullObjectFields.add(new ObjectField(");
                appendGetValue(var.substring(0, splitPoint));
                this.s.append(", \"");
                this.s.append(var.substring(splitPoint + 1));
                this.s.append("\"));");
            }
        }

        private void appendSetWithAlias(Symbolic symbol, State finalState, long heapPosition) 
        throws InvalidInputException {
            makeVariableFor(symbol);
            final String var = getVariableFor(symbol);
            final Symbolic ref = getReferenceExpansion(finalState, heapPosition);
            makeVariableFor(ref);
            final String val = getVariableFor(ref);
            final String javaType = javaClass(getTypeOfObjectInHeap(finalState, heapPosition));
            appendSetValue(javaType, var, val);
        }
        
        private void appendSetWithNumericValues(Set<PrimitiveSymbolic> symbols, Map<PrimitiveSymbolic, Simplex> model) 
        throws InvalidInputException {
            for (PrimitiveSymbolic symbol : symbols) {
            	//we append a variable initialization only if the symbol has not yet been processed;
            	//we detect this by checking whether we have already created a variable for the symbol
                if (getVariableFor(symbol) == null) {  
                	//we need a concrete primitive value for the
                	//symbol: if we do not have a model we panic
                    if (model == null) {
                        this.panic = true;
                        return;
                    }
                    final Simplex value = model.get(symbol);
                    if (value == null) {
                    	//This can happen when the symbol is contained in a mangled
                    	//subexpression, i.e., a subexpression that cannot be 
                    	//represented by the solver; in this case we may have
                    	//a model value for the whole mangled subexpression, but not for 
                    	//the symbols it contains, thus we are stuck. Our (non-)solution 
                    	//is to do nothing, i.e., just to skip this symbol and hope 
                    	//for the best. (alternatively, we may just panic...)
                    	continue;
                    }
                    appendSetWithNumericValue(symbol, value);
                }
            }
        }

        private void appendSetWithNumericValue(PrimitiveSymbolic symbol, Simplex value) 
        throws InvalidInputException {
        	makeVariableFor(symbol); //if the variable exists already, this method does nothing
            final String var = getVariableFor(symbol);
            final String val;
        	final char symbolType = symbol.getType();
            if (symbolType == Type.BOOLEAN) {
            	val = "(" + value.toString() + " != 0)";
            } else if (symbolType == Type.BYTE) {
            	val = "(byte) " + value.toString();
            } else if (symbolType == Type.CHAR) {
            	val = "(char) " + value.toString();
            } else if (symbolType == Type.SHORT) {
            	val = "(short) " + value.toString();
            } else { 
            	//it is a JVM numeric type: no cast is necessary
            	val = value.toString();
            }
        	final String javaType = toPrimitiveOrVoidCanonicalName(symbolType);

            //appends the initialization statement for the variable
            appendSetValue(javaType, var, val);
        }

        private void appendGetValue(String accessExpression) {
            if (hasMemberAccessor(accessExpression)) {
            	final String accessExpressionRoot = accessExpression.substring(0, accessExpression.indexOf('.'));
                final String container = "new AccessibleObject(" + accessExpressionRoot + ")";
                final String accessExpressionWithGetters = replaceAccessorsWithGetters(container, accessExpression);
                this.s.append(accessExpressionWithGetters);
                this.s.append(".getValue()");
             } else { //hasArrayAccessor(accessExpression) or not
                this.s.append(accessExpression);
            }
        }

        private void appendSetValue(String javaType, String accessExpression, String value) {
        	if (hasMemberAccessor(accessExpression)) {
        		final String accessExpressionRoot = accessExpression.substring(0, accessExpression.indexOf('.'));
        		final String container = "new AccessibleObject(" + accessExpressionRoot + ")";
        		final String accessExpressionField = accessExpression.substring(0, accessExpression.lastIndexOf('.'));
        		final String accessExpressionFieldWithGetters = replaceAccessorsWithGetters(container, accessExpressionField);
        		final String fieldToSet = accessExpression.substring(accessExpression.lastIndexOf('.') + 1);
        		this.s.append(accessExpressionFieldWithGetters);
        		this.s.append(".setValue(\"");
        		this.s.append(fieldToSet);
        		this.s.append("\", ");
        		this.s.append(value);
        		this.s.append(");");
        	} else if (hasArrayAccessor(accessExpression)) { 
                this.s.append(accessExpression);
                this.s.append(" = "); 
                appendGetValue(value);
                this.s.append(';'); 
        	} else { //no accessor: it is a variable, declare it and initialize it
                this.s.append(javaType);
                this.s.append(' '); 
                this.s.append(accessExpression); 
                this.s.append(" = "); 
                appendGetValue(value);
                this.s.append(';'); 
        	}
        }

        /**
         * Creates a variable for a symbol, if has not already
         * been created before.
         * 
         * @param symbol a {@link Symbolic}. It must not be {@code null}.
         * @throws InvalidInputException if {@code symbol == null} or
         *         if the variable fpr {@code symbol} was already
         *         created before (usually a symptom of the fact
         *         that we are processing a same symbol twice). 
         */
        private void makeVariableFor(Symbolic symbol) throws InvalidInputException {
        	if (symbol == null) {
            	throw new InvalidInputException("Invoked StateFormatterJUnitTestSuite.makeVariableFor with a null symbol parameter.");
        	}
            final String key = symbol.getValue(); //TODO can't we just use the symbol as key?
            if (this.symbolsToVariables.containsKey(key)) {
            	throw new InvalidInputException("Invoked StateFormatterJUnitTestSuite.makeVariableFor with a symbol parameter for which a variable was already created.");
            }
            this.symbolsToVariables.put(key, generateName(symbol.asOriginString()));
        }

        /**
         * Gets the variable for a symbol.
         * 
         * @param symbol a {@link Symbolic}. It must not be {@code null}.
         * @return a {@link String}, the Java name for
         *         a variable, or {@code null} if the variable for 
         *         {@code symbol} has not been previously created
         *         (by invoking {@link #makeVariableFor(Symbolic)}) 
         * @throws InvalidInputException if {@code symbol == null}. 
         */
        private String getVariableFor(Symbolic symbol) throws InvalidInputException {
        	if (symbol == null) {
            	throw new InvalidInputException("Invoked StateFormatterJUnitTestSuite.getVariableFor with a null symbol parameter.");
        	}
            final String key = symbol.getValue();  //TODO can't we just use the symbol as key?
            return this.symbolsToVariables.get(key);
        }
        
        //some private static utility methods for types

        private static String javaType(String type){
            if (type == null) {
                return null;
            }
            final String a = type.replace('/', '.');
            return (isReference(a) ? className(a) : a);
        }

        private static String javaClass(String type){
            if (type == null) {
                return null;
            }
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

        //some private static utility methods for origins and variable names

        private static String generateName(String originString) {
            return originString.replace("{ROOT}:", "__ROOT_");
        }
        
        private static boolean hasMemberAccessor(String s) {
            return (s.indexOf('.') != -1);
        }

        private static boolean hasArrayAccessor(String s) {
            return (s.indexOf('[') != -1);
        }

        private static String replaceAccessorsWithGetters(String container, String accessExpression) {
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

        //some private static utility methods for accessing JBSE data structures
        
        private static PrimitiveSymbolic getArrayLength(ClauseAssume clause) 
        throws InvalidInputException {
            //the clause must have shape {length} >= 0 - i.e., it
            //must have just one symbol, the length; we extract it
            final Set<PrimitiveSymbolic> symbols = primitiveSymbolsIn(clause.getCondition());
            if (symbols.size() != 1) {
            	throw new InvalidInputException("Invoke StateFormatterJUnitTestSuite.arrayLength with a clause that does not seem a clause for the initial assumption of an array's length: " + clause.getCondition() + ".");
            }
            final PrimitiveSymbolic symbolLength = symbols.iterator().next();
            return symbolLength;
        }
        
        private static ReferenceSymbolic getReferenceExpansion(State state, long heapPos) 
        throws InvalidInputException {
        	//TODO extract this code and share with DecisionProcedureAlgorithms.getPossibleAliases

        	//finds the origin (reference) that expands to heapPos
        	//by scanning the path condition
        	final Collection<Clause> pathCondition = state.getPathCondition();
        	ReferenceSymbolic referenceExpands = null;
        	for (Clause clause : pathCondition) {
        		if (clause instanceof ClauseAssumeExpands) {
        			final ClauseAssumeExpands clauseExpands = (ClauseAssumeExpands) clause;
        			final long heapPosCurrent = clauseExpands.getHeapPosition();
        			if (heapPosCurrent == heapPos) {
        				referenceExpands = clauseExpands.getReference();
        				break;
        			}
        		}
        	}
        	
        	//if we didn't find anything, the state is ill-formed
            if (referenceExpands == null) {
            	throw new InvalidInputException("No symbolic reference in state's path condition that expands to heap position " + heapPos + ".");
            }

        	//returns the reference
        	return referenceExpands;
        }

        private static String getTypeOfObjectInHeap(State state, long num) 
        throws FrozenStateException {
            final Map<Long, Objekt> heap = state.getHeap();
            final Objekt o = heap.get(num);
            return o.getType().getClassName();
        }
        
    	/**
    	 * Scavenges a {@link Primitive} for all the symbols
    	 * with primitive type in it.
    	 * 
    	 * @param e a {@link Primitive}.
    	 * @return a {@link Set}{@code <}{@link PrimitiveSymbolic}{@code >}
    	 *         containing all the symbols with primtive type that are 
    	 *         subterms of {@code e}.
    	 * @throws InvalidInputException if {@code e} has a reference,
    	 *         either symbolic or concrete, among its subterms
    	 *         (we are not able to process this kind of expressions).
    	 */
        private static Set<PrimitiveSymbolic> primitiveSymbolsIn(Primitive e) 
        throws InvalidInputException {
            final HashSet<PrimitiveSymbolic> retVal = new HashSet<>();
            
    		//a PrimitiveVisitor that fills retVal with 
    		//primitive symbol subterms
            PrimitiveVisitor v = new PrimitiveVisitor() {
                @Override
                public void visitNarrowingConversion(NarrowingConversion x) throws Exception {
                    x.getArg().accept(this);
                }

                @Override
                public void visitWideningConversion(WideningConversion x) throws Exception {
                    x.getArg().accept(this);
                }

                @Override
                public void visitTerm(Term x) throws Exception { }

                @Override
                public void visitSimplex(Simplex x) throws Exception { }

                @Override
                public void visitPrimitiveSymbolicApply(PrimitiveSymbolicApply x) throws Exception {
                    for (Value v : x.getArgs()) {
                        if (v instanceof Primitive) {
                            ((Primitive) v).accept(this);
                        } else {
                        	//error: we found a reference,
                        	//either symbolic or concrete
                        	throw new InvalidInputException("StateFormatterJUnitTestSuite.primitiveSymbolsIn: found a subterm with reference type, this formatter currently cannot manage them.");
                        }
                    }
                    //TODO shall we put also x?
                }

				@Override
				public void visitPrimitiveSymbolicHashCode(PrimitiveSymbolicHashCode x) {
                    retVal.add(x);
				}

				@Override
				public void visitPrimitiveSymbolicLocalVariable(PrimitiveSymbolicLocalVariable x) {
                    retVal.add(x);
				}

				@Override
				public void visitPrimitiveSymbolicMemberArray(PrimitiveSymbolicMemberArray x) {
                    retVal.add(x);
				}

				@Override
				public void visitPrimitiveSymbolicMemberArrayLength(PrimitiveSymbolicMemberArrayLength x) {
                    retVal.add(x);
				}

				@Override
				public void visitPrimitiveSymbolicMemberField(PrimitiveSymbolicMemberField x) {
                    retVal.add(x);
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

           //visits
           try {
        	   e.accept(v);
           } catch (InvalidInputException exc) {
        	   throw exc;
           } catch (Exception exc) {
        	   //this should never happen
        	   throw new AssertionError(exc);
           }

           return retVal;
        }
    }
}
