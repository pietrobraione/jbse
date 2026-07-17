package jbse.apps;

import static jbse.apps.run.JAVA_MAP_Utils.isInitialMapField;
import static jbse.apps.run.JAVA_MAP_Utils.possiblyAdaptMapModelSymbols;
import static jbse.bc.Signatures.JAVA_CHARSEQUENCE;
import static jbse.bc.Signatures.JAVA_STRING;
import static jbse.bc.Signatures.JAVA_STRING_EQUALS;
import static jbse.bc.Signatures.JAVA_STRING_VALUE;
import static jbse.common.Type.BOOLEAN;
import static jbse.common.Type.CHAR;
import static jbse.common.Type.internalToBinaryTypeName;
import static jbse.common.Type.internalToCanonicalTypeName;
import static jbse.common.Type.REFERENCE;
import static jbse.common.Type.toPrimitiveOrVoidCanonicalName;
import static jbse.common.Type.TYPEEND;
import static jbse.val.Util.asStringLiteral;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import jbse.bc.ClassHierarchy;
import jbse.bc.Signature;
import jbse.common.Type;
import jbse.common.exc.UnexpectedInternalException;
import jbse.mem.Array;
import jbse.mem.Array.AccessOutcomeIn;
import jbse.mem.Array.AccessOutcomeInInitialArray;
import jbse.mem.Array.AccessOutcomeInValue;
import jbse.mem.Clause;
import jbse.mem.ClauseAssume;
import jbse.mem.ClauseAssumeAliases;
import jbse.mem.ClauseAssumeExpands;
import jbse.mem.ClauseAssumeNull;
import jbse.mem.ClauseAssumeReferenceSymbolic;
import jbse.mem.Instance;
import jbse.mem.Objekt;
import jbse.mem.State;
import jbse.mem.exc.FrozenStateException;
import jbse.rewr.CalculatorRewriting;
import jbse.val.Any;
import jbse.val.Calculator;
import jbse.val.Expression;
import jbse.val.NarrowingConversion;
import jbse.val.Operator;
import jbse.val.Primitive;
import jbse.val.PrimitiveSymbolicApply;
import jbse.val.PrimitiveSymbolicHashCode;
import jbse.val.PrimitiveSymbolicLocalVariable;
import jbse.val.PrimitiveSymbolicMemberArray;
import jbse.val.PrimitiveSymbolicMemberArrayLength;
import jbse.val.PrimitiveSymbolicMemberField;
import jbse.val.PrimitiveVisitor;
import jbse.val.Reference;
import jbse.val.ReferenceConcrete;
import jbse.val.ReferenceSymbolic;
import jbse.val.ReferenceSymbolicApply;
import jbse.val.Simplex;
import jbse.val.Symbolic;
import jbse.val.Term;
import jbse.val.Value;
import jbse.val.WideningConversion;
import jbse.val.exc.InvalidOperandException;
import jbse.val.exc.InvalidTypeException;

/**
 * A {@link FormatterSushi} based on comparing a test
 * against the path condition clauses of symbolic states.
 * 
 * @author Pietro Braione
 */
public final class StateFormatterSushiPathCondition implements FormatterSushi {
    private final long methodNumber;
    private final Supplier<Long> traceCounterSupplier;
    private final Supplier<State> initialStateSupplier;
    private final boolean shallRelaxLastExpansionClause;
    private HashMap<Long, String> stringLiterals = new HashMap<>();
    private TreeSet<Long> stringOthers = new TreeSet<>();
    private HashSet<String> forbiddenExpansions = new HashSet<>();
    private StringBuilder output = new StringBuilder();
    private int testCounter = 0;

    public StateFormatterSushiPathCondition(long methodNumber,
                                            Supplier<Long> traceCounterSupplier,
                                            Supplier<State> initialStateSupplier, 
                                            boolean shallRelaxLastExpansionClause) {
        this.methodNumber = methodNumber;
        this.traceCounterSupplier = traceCounterSupplier;
        this.initialStateSupplier = initialStateSupplier;
        this.shallRelaxLastExpansionClause = shallRelaxLastExpansionClause;
    }

    public StateFormatterSushiPathCondition(int methodNumber,
                                            Supplier<State> initialStateSupplier, 
                                            boolean shallRelaxLastExpansionClause) {
        this(methodNumber, null, initialStateSupplier, shallRelaxLastExpansionClause);
    }

    @Override
    public void setStringsConstant(Map<Long, String> stringLiterals) {
        this.stringLiterals = new HashMap<>(stringLiterals); //safety copy
    }
    
    @Override
    public void setStringsNonconstant(Set<Long> stringOthers) {
        this.stringOthers = new TreeSet<>(stringOthers); //safety copy
    }
    
    @Override
    public void setForbiddenExpansions(Set<String> forbiddenExpansions) {
    	this.forbiddenExpansions = new HashSet<>(forbiddenExpansions);
    }

    @Override
    public void formatPrologue() {
        try {
            //package declaration
            this.output.append("package ");
            final State initialState = this.initialStateSupplier.get();
            final String initialCurrentClassName = initialState.getStack().get(0).getMethodClass().getClassName();
            final String initialCurrentClassPackageName = initialCurrentClassName.substring(0, initialCurrentClassName.lastIndexOf('/')).replace('/', '.');
            this.output.append(initialCurrentClassPackageName);
            this.output.append(";\n\n");
            
            final long traceCounter = (this.traceCounterSupplier == null ? -1 : this.traceCounterSupplier.get());

            //imports and class declaration
            this.output.append(PROLOGUE_1);
            this.output.append('_');
            this.output.append(this.methodNumber);
            if (this.traceCounterSupplier != null) {
                this.output.append('_');
                this.output.append(traceCounter);
            }
            
            //constant declarations
            this.output.append(PROLOGUE_2);
            for (Map.Entry<Long, String> lit : this.stringLiterals.entrySet()) {
                this.output.append(INDENT_1);
                this.output.append("private static final String CONST_");
                this.output.append(lit.getKey());
                this.output.append(" = ");
                this.output.append(asStringLiteral(lit.getValue()));
                this.output.append(";\n");
            }

            //private members and constructor declaration
            this.output.append(PROLOGUE_3);
            this.output.append('_');
            this.output.append(this.methodNumber);
            if (this.traceCounterSupplier != null) {
                this.output.append('_');
                this.output.append(traceCounter);
            }		
            this.output.append("(ClassLoader classLoader) {\n");
            this.output.append(INDENT_2);
            this.output.append("this.classLoader = classLoader;\n");
            for (long heapPos : new TreeSet<Long>(this.stringLiterals.keySet())) {
                this.output.append(INDENT_2);
                this.output.append("this.finalHeap.put(");
                this.output.append(heapPos);
                this.output.append("L, CONST_");
                this.output.append(heapPos);
                this.output.append(");\n");
            }
            this.output.append(INDENT_1);
            this.output.append("}\n\n");
        } catch (FrozenStateException e) {
            this.output.delete(0, this.output.length());
        }
    }

    @Override
    public void formatState(State state) {
        try {
            new MethodUnderTest(this.output, this.initialStateSupplier.get(), state, this.testCounter, this.stringOthers, this.forbiddenExpansions, this.shallRelaxLastExpansionClause);
        } catch (FrozenStateException e) {
            this.output.delete(0, this.output.length());
        }
        ++this.testCounter;
    }
    
    @Override
    public void formatEpilogue() {
        this.output.append("}\n"); //closes the class declaration
    }

    @Override
    public String emit() {
        return this.output.toString();
    }

    @Override
    public void cleanup() {
        this.output = new StringBuilder();
    }

    private static final String INDENT_1 = "    ";
    private static final String INDENT_2 = INDENT_1 + INDENT_1;
    private static final String INDENT_3 = INDENT_1 + INDENT_2;
    private static final String INDENT_4 = INDENT_1 + INDENT_3;
    private static final String INDENT_5 = INDENT_1 + INDENT_4;
    private static final String INDENT_6 = INDENT_1 + INDENT_5;
    private static final String PROLOGUE_1 =
    "import static sushi.compile.path_condition_distance.DistanceBySimilarityWithPathCondition.completeFinalHeap;\n" +
    "import static sushi.compile.path_condition_distance.DistanceBySimilarityWithPathCondition.distance;\n" +
    "\n" +
    "import static java.lang.Double.*;\n" +
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
    "public class EvoSuiteWrapper";
    private static final String PROLOGUE_2 = " {\n" +
    INDENT_1 + "private static final double SMALL_DISTANCE = 1;\n" +
    INDENT_1 + "private static final double BIG_DISTANCE = 1E300;\n" +
    "\n";
    private static final String PROLOGUE_3 = "\n" +
    INDENT_1 + "private final HashMap<Long, String> finalHeap = new HashMap<>();\n" +
    INDENT_1 + "private final ClassLoader classLoader;\n" +
    INDENT_1 + "private final SushiLibCache cache = new SushiLibCache();\n\n" +
    INDENT_1 + "public EvoSuiteWrapper";

    private static class MethodUnderTest {
        private static final Signature JAVA_STRING_CONTAINS = 
        new Signature(JAVA_STRING, "(" + REFERENCE + JAVA_CHARSEQUENCE + TYPEEND + ")" + BOOLEAN, "contains");
        private static final Signature JAVA_STRING_ENDSWITH = 
        new Signature(JAVA_STRING, "(" + REFERENCE + JAVA_STRING + TYPEEND + ")" + BOOLEAN, "endsWith");
        private static final Signature JAVA_STRING_STARTSWITH = 
        new Signature(JAVA_STRING, "(" + REFERENCE + JAVA_STRING + TYPEEND + ")" + BOOLEAN, "startsWith");

        private final StringBuilder s;
        private final HashMap<Symbolic, String> symbolsToVariables = new HashMap<>();
        private final HashMap<String, Symbolic> variablesToSymbols = new HashMap<>();
        private final ArrayList<String> inputVariables = new ArrayList<>();
        private final Calculator calc = new CalculatorRewriting(); //dummy
        private boolean panic = false;

        MethodUnderTest(StringBuilder s, State initialState, State finalState, int testCounter, Set<Long> stringsOther, Set<String> forbiddenExpansions, boolean shallRelaxLastExpansionClause) 
        throws FrozenStateException {
            this.s = s;
            appendMethodDeclaration(initialState, finalState, testCounter);
            appendStringsNonconstant(finalState, stringsOther);
            appendPathCondition(finalState, testCounter, forbiddenExpansions, shallRelaxLastExpansionClause);
            appendCandidateObjects(finalState, testCounter);
            appendFinalHeapCompletionStatement();
            appendIfStatement(testCounter);
            appendMethodEnd(finalState, testCounter);
        }

        private static String javaType(Symbolic symbol) {
            if (symbol instanceof Primitive) { //either PrimitiveSymbolic or Term (however, it should never be the case of a Term)
                final char type = ((Primitive) symbol).getType();
                return toPrimitiveOrVoidCanonicalName(type);
            } else if (symbol instanceof ReferenceSymbolic) {
                final String type = ((ReferenceSymbolic) symbol).getStaticType();
                return internalToCanonicalTypeName(type);
            } else {
                //this should never happen
                throw new RuntimeException("Reached unreachable branch while calculating the Java type of a symbol: Perhaps some type of symbol is not handled yet.");
            }
        }

        private void appendMethodDeclaration(State initialState, State finalState, int testCounter) {
            if (this.panic) {
                return;
            }

            final List<Symbolic> inputs;
            try {
                inputs = initialState.getStack().get(0).localVariables().values().stream()
                .filter(v -> v.getValue() instanceof Symbolic)
                .map(v -> (Symbolic) v.getValue())
                .collect(Collectors.toList());
            } catch (IndexOutOfBoundsException | FrozenStateException e) {
                throw new UnexpectedInternalException(e);
            }

            this.s.append(INDENT_1);
            this.s.append("public double test");
            this.s.append(testCounter);
            this.s.append("(");
            boolean firstDone = false;
            for (Symbolic symbol : inputs) {
                makeVariableFor(symbol);
                final String varName = getVariableFor(symbol);
                this.inputVariables.add(varName);
                if (firstDone) {
                    this.s.append(", ");
                } else {
                    firstDone = true;
                }
                final String type = javaType(symbol);
                this.s.append(type);
                this.s.append(' ');
                this.s.append(varName);
            }
            this.s.append(") throws Exception {\n");
            this.s.append(INDENT_2);
            this.s.append("//generated for state ");
            this.s.append(finalState.getBranchIdentifier());
            this.s.append('[');
            this.s.append(finalState.getSequenceNumber());
            this.s.append("]\n");
        }
        
        private void appendPathCondition(State finalState, int testCounter, Set<String> forbiddenExpansions, boolean shallRelaxLastExpansionClause) 
        throws FrozenStateException {
            if (this.panic) {
                return;
            }
            this.s.append(INDENT_2);
            this.s.append("final ArrayList<ClauseSimilarityHandler> pathConditionHandler = new ArrayList<>();\n");
            this.s.append(INDENT_2);
            this.s.append("ValueCalculator valueCalculator;\n");
            final List<Clause> pathCondition = finalState.getPathCondition();
            final int pathConditionSize = pathCondition.size();
            int currentClause = 0;
            final ClassHierarchy hier = finalState.getClassHierarchy();
            for (Clause clause : pathCondition) {
                ++currentClause;
                if (shouldSkip(hier, clause)) {
                    continue;
                }
                if (clause instanceof ClauseAssumeExpands) {
                    this.s.append(INDENT_2);
                    this.s.append("// "); //comment
                    this.s.append(clause.toString());
                    this.s.append("\n");
                    final ClauseAssumeExpands clauseExpands = (ClauseAssumeExpands) clause;
                    final ReferenceSymbolic symbol = clauseExpands.getReference();
                    final long heapPosition = clauseExpands.getHeapPosition();
                    setWithNewObject(finalState, symbol, heapPosition, (shallRelaxLastExpansionClause && currentClause == pathConditionSize), forbiddenExpansions);
                } else if (clause instanceof ClauseAssumeNull) {
                    this.s.append(INDENT_2);
                    this.s.append("// "); //comment
                    this.s.append(clause.toString());
                    this.s.append("\n");
                    final ClauseAssumeNull clauseNull = (ClauseAssumeNull) clause;
                    final ReferenceSymbolic symbol = clauseNull.getReference();
                    setWithNull(symbol);
                } else if (clause instanceof ClauseAssumeAliases) {
                    this.s.append(INDENT_2);
                    this.s.append("// "); //comment
                    this.s.append(clause.toString());
                    this.s.append("\n");
                    final ClauseAssumeAliases clauseAliases = (ClauseAssumeAliases) clause;
                    final ReferenceSymbolic symbol = clauseAliases.getReference();
                    final long heapPosition = clauseAliases.getHeapPosition();
                    setWithAlias(finalState, symbol, heapPosition);
                } else if (clause instanceof ClauseAssume) {
                    this.s.append(INDENT_2);
                    this.s.append("// "); //comment
                    this.s.append(clause.toString());
                    this.s.append("\n");
                    final ClauseAssume clauseAssume = (ClauseAssume) clause;
                    final Primitive assumption = clauseAssume.getCondition();
                    if (isAssumptionOnBooleanApply(assumption)) {
                        setAssumptionOnBooleanApply(finalState, assumption);
                    } else {
                        setNumericAssumption(finalState, assumption);
                    }
                } //else, do nothing
            }
            this.s.append("\n");
        }

        private boolean shouldSkip(ClassHierarchy hier, Clause clause) {
            if (clause instanceof ClauseAssumeReferenceSymbolic) {
            	final ReferenceSymbolic ref = ((ClauseAssumeReferenceSymbolic) clause).getReference(); 
                //exclude all the clauses with shape ClauseAssumeReferenceSymbolic
                //if they refer to the resolution of a symbolic reference that is a 
                //function application
                if (ref instanceof ReferenceSymbolicApply) {
                    return true;
                }

                //exclude all the clauses with shape ClauseAssumeReferenceSymbolic
                //if they refer to the resolution of the field initialMap of HashMap-models, since
                //initialMap is an internal field of the models and does not exist in concrete hashMaps
                if (isInitialMapField(hier, ref)) {
                    return true;
                }
            }

            //all other clauses are accepted
            return false;
        }

        private void appendCandidateObjects(State finalState, int testCounter) {
            if (this.panic) {
                return;
            }
            this.s.append(INDENT_2);
            this.s.append("final HashMap<String, Object> candidateObjects = new HashMap<>();\n");
            for (String inputVariable : this.inputVariables) {
                this.s.append(INDENT_2);
                this.s.append("candidateObjects.put(\"");
                this.s.append(getPossiblyAdaptedOriginString(getSymbolFor(inputVariable)));
                this.s.append("\", ");
                this.s.append(inputVariable);
                this.s.append(");\n");
            }			
            this.s.append("\n");
        }

        private void appendStringsNonconstant(State finalState, Set<Long> stringsOther) throws FrozenStateException {
            if (this.panic) {
                return;
            }
            this.s.append(INDENT_2);
            this.s.append("final HashMap<Long, StringCalculator> stringCalculators = new HashMap<>();\n");
            for (long heapPosition : stringsOther) {
                final ReferenceConcrete refString = new ReferenceConcrete(heapPosition);
                final Instance instanceString = (Instance) finalState.getObject(refString);
                final Reference refValue = (Reference) instanceString.getFieldValue(JAVA_STRING_VALUE);
                final Array arrayValue = (Array) finalState.getObject(refValue);
                if (arrayValue != null) {
                    final List<Symbolic> symbols = symbolsInArrayOfChars(finalState, arrayValue);
                    this.s.append(INDENT_2);
                    this.s.append("stringCalculators.put(");
                    this.s.append(heapPosition);
                    this.s.append("L, new StringCalculator() {\n");
                    this.s.append(INDENT_3);
                    this.s.append("@Override public Iterable<String> getVariableOrigins() {\n");
                    this.s.append(INDENT_4);
                    this.s.append("ArrayList<String> retVal = new ArrayList<>();\n");       
                    for (Symbolic symbol: symbols) {
                        this.s.append(INDENT_4);
                        this.s.append("retVal.add(\"");
                        this.s.append(getPossiblyAdaptedOriginString(symbol));
                        this.s.append("\");\n");
                    }
                    this.s.append(INDENT_4);
                    this.s.append("return retVal;\n");       
                    this.s.append(INDENT_3);
                    this.s.append("}\n");
                    
                    //**//**//**
                    this.s.append(INDENT_3);
                    this.s.append("@Override public String getString(List<Object> variables) {\n");
                    for (int i = 0; i < symbols.size(); ++i) {
                        final Symbolic symbol = symbols.get(i);
                        makeVariableFor(symbol);
                        this.s.append(INDENT_4);
                        this.s.append("final ");
                        this.s.append(javaType(symbol));
                        this.s.append(" ");
                        this.s.append(getVariableFor(symbol));
                        this.s.append(" = (");
                        this.s.append(javaType(symbol));
                        this.s.append(") variables.get(");
                        this.s.append(i);
                        this.s.append(");\n");
                    }
                    this.s.append("\n");
                    final Evaluator evaluator = new Evaluator(arrayValue.getIndex());
                    final String arrayLength = evaluator.eval(arrayValue.getLength());
                    this.s.append(INDENT_4);
                    this.s.append("final char[] array_");
                    this.s.append(heapPosition);
                    this.s.append(" = new char[");
                    this.s.append(arrayLength);
                    this.s.append("];\n");
                    this.s.append(INDENT_4);
                    this.s.append("for (int i = 0; i < ");
                    this.s.append(arrayLength);
                    this.s.append("; ++i) {\n");
                    boolean firstDone = false;
                    for (Iterator<? extends AccessOutcomeIn> it = arrayValue.entries().iterator(); it.hasNext(); ) {
                        this.s.append(INDENT_5);
                        if (firstDone) {
                            this.s.append("} else ");
                        } else {
                            firstDone = true;
                        }
                        this.s.append("if (");
                        final AccessOutcomeIn entry = it.next();
                        this.s.append(evaluator.eval(entry.getAccessCondition()));
                        this.s.append(") {\n");
                        this.s.append(INDENT_6);
                        this.s.append("array_");
                        this.s.append(heapPosition);
                        this.s.append("[i] = ");
                        if (entry instanceof AccessOutcomeInValue) {
                            final Primitive entryValue = (Primitive) ((AccessOutcomeInValue) entry).getValue();
                            final Primitive charValue;
                            try {
                                charValue = this.calc.push(entryValue).to(CHAR).pop();
                            } catch (NoSuchElementException | InvalidTypeException | InvalidOperandException e) {
                                //this should never happen
                                throw new UnexpectedInternalException(e);
                            }
                            this.s.append(evaluator.eval(charValue));
                        } else {
                            final AccessOutcomeInInitialArray entryInitialArray = ((AccessOutcomeInInitialArray) entry);
                            final Array arrayInitial = (Array) finalState.getObject(entryInitialArray.getInitialArray());
                            this.s.append(getVariableFor(arrayInitial.getOrigin()));
                            this.s.append("[i + (");
                            this.s.append(evaluator.eval(entryInitialArray.getOffset()));
                            this.s.append(")]");
                        }
                        this.s.append(";\n");
                    }
                    if (arrayValue.entries().size() > 0) {
                    	this.s.append(INDENT_5);
                    	this.s.append("}\n");
                    }
                    this.s.append(INDENT_4);
                    this.s.append("}\n");
                    this.s.append(INDENT_4);
                    this.s.append("return new String(array_");
                    this.s.append(heapPosition);
                    this.s.append(");\n");
                    this.s.append(INDENT_3);
                    this.s.append("}\n");
                    this.s.append(INDENT_2);
                    this.s.append("});\n");                    
                }
            }
            this.s.append("\n");
        }

        private List<Symbolic> symbolsInArrayOfChars(State finalState, Array a) {
            final ArrayList<Symbolic> symbols = new ArrayList<>();
            final PrimitiveVisitor v = new PrimitiveVisitor() {

                @Override
                public void visitAny(Any x) { }

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
                public void visitPrimitiveSymbolicApply(PrimitiveSymbolicApply x) throws Exception {
                    if (symbols.contains(x)) {
                        return; //surely its args have been processed
                    }
                    symbols.add(x);
                }

                @Override
                public void visitSimplex(Simplex x) throws Exception { }

                @Override
                public void visitTerm(Term x) throws Exception { }

                @Override
                public void visitNarrowingConversion(NarrowingConversion x) throws Exception {
                    x.getArg().accept(this);
                }

                @Override
                public void visitWideningConversion(WideningConversion x) throws Exception {
                    x.getArg().accept(this);
                }

				@Override
				public void visitPrimitiveSymbolicHashCode(PrimitiveSymbolicHashCode x) throws Exception {
                    if (symbols.contains(x)) {
                        return;
                    }
                    symbols.add(x);
				}

				@Override
				public void visitPrimitiveSymbolicLocalVariable(PrimitiveSymbolicLocalVariable x) throws Exception {
                    if (symbols.contains(x)) {
                        return;
                    }
                    symbols.add(x);
				}

				@Override
				public void visitPrimitiveSymbolicMemberArray(PrimitiveSymbolicMemberArray x) throws Exception {
                    if (symbols.contains(x)) {
                        return;
                    }
                    symbols.add(x);
				}

				@Override
				public void visitPrimitiveSymbolicMemberArrayLength(PrimitiveSymbolicMemberArrayLength x)
				throws Exception {
                    if (symbols.contains(x)) {
                        return;
                    }
                    symbols.add(x);
				}

				@Override
				public void visitPrimitiveSymbolicMemberField(PrimitiveSymbolicMemberField x) throws Exception {
                    if (symbols.contains(x)) {
                        return;
                    }
                    symbols.add(x);
				}
            };
            
            try {
                a.getLength().accept(v);
                for (Iterator<? extends AccessOutcomeIn> it = a.entries().iterator(); it.hasNext(); ) {
                    final AccessOutcomeIn entry = it.next();
                    entry.getAccessCondition().accept(v);
                    if (entry instanceof AccessOutcomeInValue) {
                        //it is an array of characters, so the value is primitive
                        ((Primitive) ((AccessOutcomeInValue) entry).getValue()).accept(v);
                    } else { //entry instanceof AccessOutcomeInInitialArray
                        final AccessOutcomeInInitialArray entryInitialArray = (AccessOutcomeInInitialArray) entry;
                        entryInitialArray.getOffset().accept(v);
                        final ReferenceSymbolic initialArrayOrigin = ((Array) finalState.getObject(entryInitialArray.getInitialArray())).getOrigin();
                        symbols.add(initialArrayOrigin);
                    }
                }
            } catch (Exception exc) {
                //this should never happen
                throw new AssertionError(exc);
            }
            return symbols;
        }

        
        private final class Evaluator implements PrimitiveVisitor {
            private final Term arrayIndex;
            private String result = null;
            
            Evaluator(Term arrayIndex) {
                this.arrayIndex = arrayIndex;
            }
            
            public String eval(Primitive p) {
                try {
                    p.accept(this);
                } catch (RuntimeException e) {
                    throw e;
                } catch (Exception e) {
                    //never happens
                    throw new AssertionError(e);
                }
                return this.result;
            }

            @Override
            public void visitAny(Any x) throws Exception {
                this.result = "";
            }

            @Override
            public void visitExpression(Expression e) throws Exception {
                if (e.isUnary()) {
                    e.getOperand().accept(this);
                    final String operand = this.result;
                    this.result = "(" + e.getOperator() + " " + operand + ")";
                } else {
                    e.getFirstOperand().accept(this);
                    final String operandFirst = this.result;
                    e.getSecondOperand().accept(this);
                    final String operandSecond = this.result;
                    this.result = "(" + operandFirst + " " + e.getOperator() + " " + operandSecond + ")";
                }
            }

            @Override
            public void visitPrimitiveSymbolicApply(PrimitiveSymbolicApply x) throws Exception {
                this.result = ""; //currently unsupported
                
            }

            @Override
            public void visitSimplex(Simplex x) throws Exception {
                this.result = x.toString();
            }

            @Override
            public void visitTerm(Term x) throws Exception {
                if (x == this.arrayIndex) {
                    this.result = "i";
                } else {
                    this.result = "";
                }
            }

            @Override
            public void visitNarrowingConversion(NarrowingConversion x) throws Exception {
                x.getArg().accept(this);
                final String arg = this.result;
                this.result = "((" + toPrimitiveOrVoidCanonicalName(x.getType()) + ") " + arg + ")";
            }

            @Override
            public void visitWideningConversion(WideningConversion x) throws Exception {
                x.getArg().accept(this);
            }

			@Override
			public void visitPrimitiveSymbolicHashCode(PrimitiveSymbolicHashCode x) throws Exception {
                this.result = getVariableFor(x);
			}

			@Override
			public void visitPrimitiveSymbolicLocalVariable(PrimitiveSymbolicLocalVariable x) throws Exception {
                this.result = getVariableFor(x);
			}

			@Override
			public void visitPrimitiveSymbolicMemberArray(PrimitiveSymbolicMemberArray x) throws Exception {
                this.result = getVariableFor(x);
			}

			@Override
			public void visitPrimitiveSymbolicMemberArrayLength(PrimitiveSymbolicMemberArrayLength x) throws Exception {
                this.result = getVariableFor(x);
			}

			@Override
			public void visitPrimitiveSymbolicMemberField(PrimitiveSymbolicMemberField x) throws Exception {
                this.result = getVariableFor(x);
			}            
        }
        
        private void appendFinalHeapCompletionStatement() {
            this.s.append(INDENT_2);
            this.s.append("completeFinalHeap(stringCalculators, candidateObjects, this.finalHeap, this.classLoader, this.cache);\n");
            this.s.append("\n");
        }

        private void appendIfStatement(int testCounter) {
            this.s.append(INDENT_2);
            this.s.append("double d = distance(pathConditionHandler, candidateObjects, this.finalHeap, this.classLoader, this.cache);\n");
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
                this.s.append(finalState.getBranchIdentifier());
                this.s.append('[');
                this.s.append(finalState.getSequenceNumber());
                this.s.append("]\n");
            } else {
                this.s.append(INDENT_1);
                this.s.append("}\n");
            }
        }

        private void setWithNewObject(State finalState, ReferenceSymbolic symbol, long heapPosition, boolean relax, Set<String> forbiddenExpansions) 
        throws FrozenStateException {
            this.s.append(INDENT_2);
            this.s.append("pathConditionHandler.add(new SimilarityWithRefToFreshObject(\"");
            this.s.append(getPossiblyAdaptedOriginString(symbol));
            if (relax) {
                this.s.append("\", \"\"");
                for (String forbiddenExpansionClass : forbiddenExpansions) {
                    this.s.append(", Class.forName(\"");
                    this.s.append(internalToBinaryTypeName(forbiddenExpansionClass)); //TODO arrays
                    this.s.append("\")");
                }
                this.s.append("));\n");
            } else {
                final String expansionClass = internalToBinaryTypeName(getTypeOfObjectInHeap(finalState, heapPosition));
                this.s.append("\", Class.forName(\"");
                this.s.append(expansionClass); //TODO arrays
                this.s.append("\")));\n");
            }
        }

        private void setWithNull(ReferenceSymbolic symbol) {
            this.s.append(INDENT_2);
            this.s.append("pathConditionHandler.add(new SimilarityWithRefToNull(\"");
            this.s.append(getPossiblyAdaptedOriginString(symbol));
            this.s.append("\"));\n");
        }

        private void setWithAlias(State finalState, ReferenceSymbolic symbol, long heapPosition) {
            final String target = getOriginStringOfObjectInHeap(finalState, heapPosition);
            this.s.append(INDENT_2);
            this.s.append("pathConditionHandler.add(new SimilarityWithRefToAlias(\"");
            this.s.append(getPossiblyAdaptedOriginString(symbol));
            this.s.append("\", \"");
            this.s.append(target);
            this.s.append("\"));\n");
        }

        private int varCounter = 0;
        private String generateVarNameFromOrigin(String name) {
            return "V" + this.varCounter++;
        }

        private void makeVariableFor(Symbolic symbol) {
            if (!this.symbolsToVariables.containsKey(symbol)) {
                final String origin = getPossiblyAdaptedOriginString(symbol);
                final String varName = generateVarNameFromOrigin(origin);
                this.symbolsToVariables.put(symbol, varName);
                this.variablesToSymbols.put(varName, symbol);
            }
        }

        private String getVariableFor(Symbolic symbol) {
            return this.symbolsToVariables.get(symbol);
        }

        private Symbolic getSymbolFor(String varName) {
            return this.variablesToSymbols.get(varName);
        }

        private static String getTypeOfObjectInHeap(State finalState, long num) 
        throws FrozenStateException {
            final Map<Long, Objekt> heap = finalState.getHeap();
            final Objekt o = heap.get(num);
            return o.getType().getInternalTypeName();
        }

        private String getOriginStringOfObjectInHeap(State finalState, long heapPos) {
            final Collection<Clause> path = finalState.getPathCondition();
            for (Clause clause : path) {
                if (clause instanceof ClauseAssumeExpands) {
                    final ClauseAssumeExpands clauseExpands = (ClauseAssumeExpands) clause;
                    final long heapPosCurrent = clauseExpands.getHeapPosition();
                    if (heapPosCurrent == heapPos) {
                        return getPossiblyAdaptedOriginString(clauseExpands.getReference());
                    }
                }
            }
            return null;
        }
        
        private boolean isAssumptionOnBooleanApply(Primitive assumption) {
            if (!(assumption instanceof Expression)) {
                return false;
            }
            final Expression assumptionExpression = (Expression) assumption;
            if (assumptionExpression.isUnary()) {
                return false;
            }
            if (assumptionExpression.getOperator() != Operator.EQ && assumptionExpression.getOperator() != Operator.NE) {
                return false;
            }
            final Primitive firstOperand = assumptionExpression.getFirstOperand();
            final Primitive secondOperand = assumptionExpression.getSecondOperand();
            final Simplex simplexOperand;
            final Primitive otherOperand;
            if (firstOperand instanceof Simplex) {
                simplexOperand = (Simplex) firstOperand;
                otherOperand = secondOperand;
            } else if (secondOperand instanceof Simplex) {
                simplexOperand = (Simplex) secondOperand;
                otherOperand = firstOperand;
            } else {
                return false;
            }
            if (simplexOperand.getType() != Type.INT || (!simplexOperand.isZeroOne(true) && !simplexOperand.isZeroOne(false))) {
                return false;
            }
            if (otherOperand instanceof WideningConversion) {
                final WideningConversion wideningOperand = (WideningConversion) otherOperand;
                if (wideningOperand.getArg() instanceof PrimitiveSymbolicApply) {
                    final PrimitiveSymbolicApply apply = (PrimitiveSymbolicApply) wideningOperand.getArg();
                    final String applyOperator = apply.getOperator();
                    return 
                    JAVA_STRING_EQUALS.toString().equals(applyOperator) || 
                    JAVA_STRING_CONTAINS.toString().equals(applyOperator);
                } else {
                    return false;
                }
            } else {
                return false;
            }
        }
        
        //TODO this code assumes all reference args point to strings
        private void setAssumptionOnBooleanApply(State state, Primitive assumption) {
            final Value[] applyArgs;
            final String applyOperator;
            final boolean shallBeEqual;
            {
                final Expression assumptionExpression = (Expression) assumption;
                final Primitive firstOperand = assumptionExpression.getFirstOperand();
                final Primitive secondOperand = assumptionExpression.getSecondOperand();
                final WideningConversion wideningOperand = (WideningConversion) ((firstOperand instanceof WideningConversion) ? firstOperand : secondOperand);
                final PrimitiveSymbolicApply apply = (PrimitiveSymbolicApply) wideningOperand.getArg();
                applyArgs = apply.getArgs();
                applyOperator = apply.getOperator();
                final Simplex simplexOperand = (Simplex) ((firstOperand instanceof Simplex) ? firstOperand : secondOperand);
                final boolean isZero = simplexOperand.isZeroOne(true);
                final Operator operator = assumptionExpression.getOperator();
                shallBeEqual = (isZero && operator == Operator.NE) || (!isZero && operator == Operator.EQ);
            }
            
            final ArrayList<ReferenceSymbolic> symbolicStrings = new ArrayList<>();
            final ArrayList<ReferenceConcrete> concreteStrings = new ArrayList<>();
            for (Value arg : applyArgs) {
                if (arg instanceof ReferenceSymbolic) {
                	final ReferenceSymbolic argSymbolic = (ReferenceSymbolic) arg;
                	if (!symbolicStrings.contains(argSymbolic)) {
                		symbolicStrings.add(argSymbolic);
                	}
                } else if (arg instanceof ReferenceConcrete)  {
                	final ReferenceConcrete argConcrete = (ReferenceConcrete) arg;
                	if (!concreteStrings.contains(argConcrete)) {
                		concreteStrings.add((ReferenceConcrete) arg);
                	}
                }
            }
            
            this.s.append(INDENT_2);
            this.s.append("valueCalculator = new ValueCalculator() {\n");
            this.s.append(INDENT_3);
            this.s.append("@Override public Iterable<String> getVariableOrigins() {\n");
            this.s.append(INDENT_4);
            this.s.append("ArrayList<String> retVal = new ArrayList<>();\n");       
            for (ReferenceSymbolic symbol: symbolicStrings) {
                this.s.append(INDENT_4);
                this.s.append("retVal.add(\"");
                this.s.append(getPossiblyAdaptedOriginString(symbol));
                this.s.append("\");\n");
            }
            this.s.append(INDENT_4);
            this.s.append("return retVal;\n");       
            this.s.append(INDENT_3);
            this.s.append("}\n");       
            this.s.append(INDENT_3);
            this.s.append("@Override public double calculate(List<Object> variables) {\n");
            for (int i = 0; i < symbolicStrings.size(); ++i) {
                final ReferenceSymbolic symbolicString = symbolicStrings.get(i);
                makeVariableFor(symbolicString);
                this.s.append(INDENT_4);
                this.s.append("final String ");
                this.s.append(getVariableFor(symbolicString));
                this.s.append(" = (String) variables.get(");
                this.s.append(i);
                this.s.append(");\n");
            }
            for (int i = 0; i < concreteStrings.size(); ++i) {
                final ReferenceConcrete concreteString = concreteStrings.get(i);
                this.s.append(INDENT_4);
                this.s.append("final String S");
                this.s.append(concreteString.getHeapPosition());
                this.s.append(" = (String) finalHeap.get(");
                this.s.append(concreteString.getHeapPosition());
                this.s.append("L);\n");
            }
            this.s.append(INDENT_4);
            this.s.append("return ");
            if (JAVA_STRING_EQUALS.toString().equals(applyOperator)) {
                this.s.append(javaStringComparisonAssumptionCheck(state, applyArgs, shallBeEqual, "equals", "distanceEditLevenshtein"));
            } else if (JAVA_STRING_CONTAINS.toString().equals(applyOperator)) {
                this.s.append(javaStringComparisonAssumptionCheck(state, applyArgs, shallBeEqual, "contains", "distanceContainment"));
            } else if (JAVA_STRING_ENDSWITH.toString().equals(applyOperator)) {
                this.s.append(javaStringComparisonAssumptionCheck(state, applyArgs, shallBeEqual, "endsWith", "distanceSuffix"));
            } else if (JAVA_STRING_STARTSWITH.toString().equals(applyOperator)) {
                this.s.append(javaStringComparisonAssumptionCheck(state, applyArgs, shallBeEqual, "startsWith", "distancePrefix"));
            } else {
                //this should never happen
                throw new AssertionError("Unexpected function application of " + applyOperator + " for which a fitness function does not exist.");
            }
            this.s.append(";\n");
            this.s.append(INDENT_3);
            this.s.append("}\n");
            this.s.append(INDENT_2);
            this.s.append("};\n");
            this.s.append(INDENT_2);
            this.s.append("pathConditionHandler.add(new SimilarityWithNumericExpression(valueCalculator));\n");
        }

        private void setNumericAssumption(State state, Primitive assumption) {
            final List<Symbolic> symbols = symbolsInNumericAssumption(assumption);
            this.s.append(INDENT_2);
            this.s.append("valueCalculator = new ValueCalculator() {\n");
            this.s.append(INDENT_3);
            this.s.append("@Override public Iterable<String> getVariableOrigins() {\n");
            this.s.append(INDENT_4);
            this.s.append("ArrayList<String> retVal = new ArrayList<>();\n");       
            for (Symbolic symbol: symbols) {
                this.s.append(INDENT_4);
                this.s.append("retVal.add(\"");
                this.s.append(getPossiblyAdaptedOriginString(symbol));
                this.s.append("\");\n");
            }
            this.s.append(INDENT_4);
            this.s.append("return retVal;\n");       
            this.s.append(INDENT_3);
            this.s.append("}\n");       
            this.s.append(INDENT_3);
            this.s.append("@Override public double calculate(List<Object> variables) {\n");
            for (int i = 0; i < symbols.size(); ++i) {
                final Symbolic symbol = symbols.get(i);
                makeVariableFor(symbol);
                this.s.append(INDENT_4);
                this.s.append("final ");
                this.s.append(javaType(symbol));
                this.s.append(" ");
                this.s.append(getVariableFor(symbol));
                this.s.append(" = (");
                this.s.append(javaType(symbol));
                this.s.append(") variables.get(");
                this.s.append(i);
                this.s.append(");\n");
            }
            this.s.append(INDENT_4);
            this.s.append("return ");
            this.s.append(javaAssumptionCheck(state, assumption));
            this.s.append(";\n");
            this.s.append(INDENT_3);
            this.s.append("}\n");
            this.s.append(INDENT_2);
            this.s.append("};\n");
            this.s.append(INDENT_2);
            this.s.append("pathConditionHandler.add(new SimilarityWithNumericExpression(valueCalculator));\n");
        }

        private String getPossiblyAdaptedOriginString(Symbolic symbol) {
            final String retVal = possiblyAdaptMapModelSymbols(symbol.asOriginString());
            return retVal;
        }

        private List<Symbolic> symbolsInNumericAssumption(Primitive e) {
            final ArrayList<Symbolic> symbols = new ArrayList<>();
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
                public void visitNarrowingConversion(NarrowingConversion x) throws Exception {
                    x.getArg().accept(this);
                }

                @Override
                public void visitPrimitiveSymbolicApply(PrimitiveSymbolicApply x) throws Exception {
                    if (symbols.contains(x)) {
                        return; //surely its args have been processed
                    }
                    symbols.add(x);
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

				@Override
				public void visitPrimitiveSymbolicHashCode(PrimitiveSymbolicHashCode x) {
                    if (symbols.contains(x)) {
                        return;
                    }
                    symbols.add(x);
				}

				@Override
				public void visitPrimitiveSymbolicLocalVariable(PrimitiveSymbolicLocalVariable x) {
                    if (symbols.contains(x)) {
                        return;
                    }
                    symbols.add(x);
				}

				@Override
				public void visitPrimitiveSymbolicMemberArray(PrimitiveSymbolicMemberArray x) {
                    if (symbols.contains(x)) {
                        return;
                    }
                    symbols.add(x);
				}

				@Override
				public void visitPrimitiveSymbolicMemberArrayLength(PrimitiveSymbolicMemberArrayLength x) {
                    if (symbols.contains(x)) {
                        return;
                    }
                    symbols.add(x);
				}

				@Override
				public void visitPrimitiveSymbolicMemberField(PrimitiveSymbolicMemberField x) {
                    if (symbols.contains(x)) {
                        return;
                    }
                    symbols.add(x);
				}
            };

            try {
                e.accept(v);
            } catch (Exception exc) {
                //this should never happen
                throw new AssertionError(exc);
            }
            return symbols;
        }

        private Operator dual(Operator op) {
            switch (op) {
            case AND:
                return Operator.OR;
            case OR:
                return Operator.AND;
            case GT:
                return Operator.LE;
            case GE:
                return Operator.LT;
            case LT:
                return Operator.GE;
            case LE:
                return Operator.GT;
            case EQ:
                return Operator.NE;
            case NE:
                return Operator.EQ;
            default:
                return null;
            }
        }
        
        private String javaStringComparisonAssumptionCheck(State state, Value[] applyArgs, boolean shallBeEqual, String javaMethod, String distanceFunction) {
            final String firstArg;
            if (applyArgs[0] instanceof ReferenceSymbolic) {
                firstArg = getVariableFor((ReferenceSymbolic) applyArgs[0]);
            } else {
                firstArg = "S" + ((ReferenceConcrete) applyArgs[0]).getHeapPosition();
            }
            final String secondArg;
            if (applyArgs[1] instanceof ReferenceSymbolic) {
                secondArg = getVariableFor((ReferenceSymbolic) applyArgs[1]);
            } else {
                secondArg = "S" + ((ReferenceConcrete) applyArgs[1]).getHeapPosition();
            }
            
            final StringBuilder retVal = new StringBuilder();
            retVal.append("((");
            retVal.append(firstArg);
            retVal.append(" == null && ");
            retVal.append(secondArg);
            retVal.append(" == null) ? ");
            if (shallBeEqual) {
                retVal.append("0");
            } else {
                retVal.append("1");
            }
            retVal.append(" : ((");
            retVal.append(firstArg);
            retVal.append(" == null && ");
            retVal.append(secondArg);
            retVal.append(" != null) || (");
            retVal.append(firstArg);
            retVal.append(" != null && ");
            retVal.append(secondArg);
            retVal.append(" == null)) ? ");
            if (shallBeEqual) {
                retVal.append("1");
            } else {
                retVal.append("0");
            }
            retVal.append(" : ");
            retVal.append(firstArg);
            retVal.append("." + javaMethod + "(");
            retVal.append(secondArg);
            retVal.append(") ? ");
            if (shallBeEqual) {
                retVal.append("0");
            } else {
                retVal.append("1");
            }
            retVal.append(" : ");
            if (shallBeEqual) {
                retVal.append("sushi.compile.distance.StringDistanceFunctions." + distanceFunction + "(");
                retVal.append(firstArg);
                retVal.append(", ");
                retVal.append(secondArg);
                retVal.append(")");
            } else {
                retVal.append("1");
            }
            retVal.append(")");
            return retVal.toString();
        }
        
        private String javaAssumptionCheck(State state, Primitive assumption) {
            //first pass: Eliminate negation
            final ArrayList<Primitive> assumptionWithNoNegation = new ArrayList<>(); //we use only one element as it were a reference to a String variable            
            final PrimitiveVisitor negationEliminator = new PrimitiveVisitor() {
                @Override
                public void visitAny(Any x) throws Exception {
                    assumptionWithNoNegation.add(x);
                }

                @Override
                public void visitExpression(Expression e) throws Exception {
                    if (e.getOperator().equals(Operator.NOT)) {
                        final Primitive operand = e.getOperand();
                        if (operand instanceof Simplex) {
                            //true or false
                            assumptionWithNoNegation.add(MethodUnderTest.this.calc.push(operand).not().pop());
                        } else if (operand instanceof Expression) {
                            final Expression operandExp = (Expression) operand;
                            final Operator operator = operandExp.getOperator();
                            if (operator.equals(Operator.NOT)) {
                                //double negation
                                operandExp.getOperand().accept(this);
                            } else if (operator.equals(Operator.AND) || operator.equals(Operator.OR)) {
                                MethodUnderTest.this.calc.push(operandExp.getFirstOperand()).not().pop().accept(this);
                                final Primitive first = assumptionWithNoNegation.remove(0);
                                MethodUnderTest.this.calc.push(operandExp.getSecondOperand()).not().pop().accept(this);
                                final Primitive second = assumptionWithNoNegation.remove(0);
                                assumptionWithNoNegation.add(Expression.makeExpressionBinary(first, dual(operator), second));
                            } else if (operator.equals(Operator.GT) || operator.equals(Operator.GE) ||
                            operator.equals(Operator.LT) || operator.equals(Operator.LE) ||
                            operator.equals(Operator.EQ) || operator.equals(Operator.NE)) {
                                assumptionWithNoNegation.add(Expression.makeExpressionBinary(operandExp.getFirstOperand(), dual(operator), operandExp.getSecondOperand()));
                            } else {
                                //can't do anything for this expression
                                assumptionWithNoNegation.add(e);
                            }
                        } else {
                            //can't do anything for this expression
                            assumptionWithNoNegation.add(e);
                        }
                    } else if (e.isUnary()) {
                        //in this case the operator can only be NEG
                        assumptionWithNoNegation.add(e);
                    } else {
                        //binary operator
                        final Operator operator = e.getOperator();
                        e.getFirstOperand().accept(this);
                        final Primitive first = assumptionWithNoNegation.remove(0);
                        e.getSecondOperand().accept(this);
                        final Primitive second = assumptionWithNoNegation.remove(0);
                        assumptionWithNoNegation.add(Expression.makeExpressionBinary(first, operator, second));
                    }
                }

                @Override
                public void visitPrimitiveSymbolicApply(PrimitiveSymbolicApply x) throws Exception {
                    final ArrayList<Value> newArgs = new ArrayList<>(); 
                    for (Value arg : x.getArgs()) {
                        if (arg instanceof Primitive) {
                            ((Primitive) arg).accept(this);
                            newArgs.add(assumptionWithNoNegation.remove(0));
                        } else {
                            newArgs.add(arg);
                        }
                    }
                    assumptionWithNoNegation.add(new PrimitiveSymbolicApply(x.getType(), x.historyPoint(), x.getOperator(), newArgs.toArray(new Value[0])));
                }

                @Override
                public void visitSimplex(Simplex x) throws Exception {
                    assumptionWithNoNegation.add(x);
                }

                @Override
                public void visitTerm(Term x) throws Exception {
                    assumptionWithNoNegation.add(x);
                }

                @Override
                public void visitNarrowingConversion(NarrowingConversion x) throws Exception {
                    assumptionWithNoNegation.add(x);
                }

                @Override
                public void visitWideningConversion(WideningConversion x) throws Exception {
                    assumptionWithNoNegation.add(x);
                }

				@Override
				public void visitPrimitiveSymbolicHashCode(PrimitiveSymbolicHashCode x) throws Exception {
                    assumptionWithNoNegation.add(x);
				}

				@Override
				public void visitPrimitiveSymbolicLocalVariable(PrimitiveSymbolicLocalVariable x) throws Exception {
                    assumptionWithNoNegation.add(x);
				}

				@Override
				public void visitPrimitiveSymbolicMemberArray(PrimitiveSymbolicMemberArray x) throws Exception {
                    assumptionWithNoNegation.add(x);
				}

				@Override
				public void visitPrimitiveSymbolicMemberArrayLength(PrimitiveSymbolicMemberArrayLength x)
				throws Exception {
                    assumptionWithNoNegation.add(x);
				}

				@Override
				public void visitPrimitiveSymbolicMemberField(PrimitiveSymbolicMemberField x) throws Exception {
                    assumptionWithNoNegation.add(x);
				}
            };
            try {
                assumption.accept(negationEliminator);
            } catch (Exception exc) {
                //this may happen if Any appears in assumption
                throw new RuntimeException(exc);
            }

            //second pass: translate
            final ArrayList<String> translation = new ArrayList<String>(); //we use only one element as it were a reference to a String variable            
            final PrimitiveVisitor translator = new PrimitiveVisitor() {
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
                    translation.add(x.toString());
                }

                @Override
                public void visitNarrowingConversion(NarrowingConversion x)
                throws Exception {
                    x.getArg().accept(this);
                    final String arg = translation.remove(0);
                    final StringBuilder b = new StringBuilder();
                    b.append("(");
                    b.append(toPrimitiveOrVoidCanonicalName(x.getType()));
                    b.append(") (");
                    b.append(arg);
                    b.append(")");
                    translation.add(b.toString());
                }

                @Override
                public void visitPrimitiveSymbolicApply(PrimitiveSymbolicApply x)
                throws Exception {
                    makeVariableFor(x);
                    translation.add(getVariableFor(x));
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
                            b.append(") ? 0 : isNaN((");
                            b.append(firstArg);
                            b.append(") - (");
                            b.append(secondArg);
                            b.append(")) ? BIG_DISTANCE : SMALL_DISTANCE + abs((");
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
                            b.append(") ? 0 : isNaN((");
                            b.append(firstArg);
                            b.append(") - (");
                            b.append(secondArg);
                            b.append(")) ? BIG_DISTANCE : SMALL_DISTANCE");
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

				@Override
				public void visitPrimitiveSymbolicHashCode(PrimitiveSymbolicHashCode x) {
                    makeVariableFor(x);
                    translation.add(getVariableFor(x));
				}

				@Override
				public void visitPrimitiveSymbolicLocalVariable(PrimitiveSymbolicLocalVariable x) {
                    makeVariableFor(x);
                    translation.add(getVariableFor(x));
				}

				@Override
				public void visitPrimitiveSymbolicMemberArray(PrimitiveSymbolicMemberArray x) {
                    makeVariableFor(x);
                    translation.add(getVariableFor(x));
				}

				@Override
				public void visitPrimitiveSymbolicMemberArrayLength(PrimitiveSymbolicMemberArrayLength x) {
                    makeVariableFor(x);
                    translation.add(getVariableFor(x));
				}

				@Override
				public void visitPrimitiveSymbolicMemberField(PrimitiveSymbolicMemberField x) {
                    makeVariableFor(x);
                    translation.add(getVariableFor(x));
				}
            };
            try {
                assumptionWithNoNegation.get(0).accept(translator);
            } catch (Exception exc) {
                //this may happen if Any appears in assumption
                throw new RuntimeException(exc);
            }

            return translation.get(0);
        }
    }
}
