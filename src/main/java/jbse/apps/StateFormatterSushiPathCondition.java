package jbse.apps;

import static jbse.apps.run.JAVA_MAP_Utils.isInitialMapField;
import static jbse.apps.run.JAVA_MAP_Utils.possiblyAdaptMapModelSymbols;
import static jbse.bc.Signatures.JAVA_CHARSEQUENCE;
import static jbse.bc.Signatures.JAVA_STRING;
import static jbse.bc.Signatures.JAVA_STRING_EQUALS;
import static jbse.bc.Signatures.JAVA_STRING_VALUE;
import static jbse.common.Type.BOOLEAN;
import static jbse.common.Type.CHAR;
import static jbse.common.Type.internalToBinaryClassName;
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
import jbse.common.exc.InvalidInputException;
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
import jbse.mem.exc.ThreadStackEmptyException;
import jbse.rewr.CalculatorRewriting;
import jbse.val.Any;
import jbse.val.Calculator;
import jbse.val.Expression;
import jbse.val.NarrowingConversion;
import jbse.val.Operator;
import jbse.val.Primitive;
import jbse.val.PrimitiveSymbolic;
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
	//passed by constructor
	private final String javaPackageName;
	private final long identifier;
	private final Supplier<State> initialStateSupplier;
	private final boolean shallRelaxLastExpansionClause;

	//passed by setters, here we initialize them with default values
	private HashMap<Long, String> stringConstants = new HashMap<>();
	private TreeSet<Long> stringNonconstants = new TreeSet<>();
	private HashSet<String> forbiddenExpansions = new HashSet<>();

	//used to calculate the output
	private final HashMap<Symbolic, String> symbolsToVariables = new HashMap<>();
	private final HashMap<String, Symbolic> variablesToSymbols = new HashMap<>();
	private final ArrayList<String> inputVariables = new ArrayList<>();
	private final Calculator calc = new CalculatorRewriting(); //dummy
	private final StringBuilder output = new StringBuilder();
	private boolean notYetFormattedFinalState = true;
	private boolean failed = false;

	public StateFormatterSushiPathCondition(String packageName, long identifier,
	                                        Supplier<State> initialStateSupplier, 
	                                        boolean shallRelaxLastExpansionClause) {
		this.identifier = identifier;
		this.javaPackageName = packageName.replace('/', '.');
		this.initialStateSupplier = initialStateSupplier;
		this.shallRelaxLastExpansionClause = shallRelaxLastExpansionClause;
	}

	@Override
	public void setStringConstants(Map<Long, String> stringConstants) {
		this.stringConstants = new HashMap<>(stringConstants); //safety copy
	}

	@Override
	public void setStringNonconstants(Set<Long> stringNonconstants) {
		this.stringNonconstants = new TreeSet<>(stringNonconstants); //safety copy
	}

	@Override
	public void setForbiddenExpansions(Set<String> forbiddenExpansions) {
		this.forbiddenExpansions = new HashSet<>(forbiddenExpansions); //safety copy
	}

	@Override
	public void formatPrologue() {
		try {
			//package declaration
			this.output.append("package ");
			this.output.append(this.javaPackageName);
			this.output.append(";\n\n");

			//imports and class declaration
			this.output.append(PROLOGUE_1);
			this.output.append(this.identifier);

			//private static final string constants declarations
			this.output.append(PROLOGUE_2);
			for (Map.Entry<Long, String> lit : this.stringConstants.entrySet()) {
				this.output.append(INDENT_1);
				this.output.append("private static final String CONST_");
				this.output.append(lit.getKey());
				this.output.append(" = ");
				this.output.append(asStringLiteral(lit.getValue()));
				this.output.append(";\n");
			}
			if (!this.stringConstants.isEmpty()) {
				this.output.append('\n');
			}

			//private members and constructor declaration
			this.output.append(PROLOGUE_3);
			this.output.append(this.identifier);
			this.output.append("(ClassLoader classLoader) {\n");
			this.output.append(INDENT_2);
			this.output.append("this.classLoader = classLoader;\n");
			for (long heapPos : new TreeSet<Long>(this.stringConstants.keySet())) {
				this.output.append(INDENT_2);
				this.output.append("this.constants.put(");
				this.output.append(heapPos);
				this.output.append("L, CONST_");
				this.output.append(heapPos);
				this.output.append(");\n");
			}
			//the constructor body is not yet complete...
			//continues in formatState
		} catch (Exception e) {
			failFormatPrologue(e);
		}
	}

	@Override
	public void formatState(State state) {
		if (this.failed) {
			return;
		}

		final State initialState = this.initialStateSupplier.get();
		final State finalState = state; //just for the sake of clarity
		try {
			if (this.notYetFormattedFinalState) {
				appendPathConditionHandlers(initialState, finalState);
				appendMethodDeclaration(initialState, finalState);
				appendGetBackbone();
				appendRoots(initialState);
				appendStringCalculators(initialState, finalState);
				appendRefreshBackbone();
				appendDistanceCalculation();
				appendMethodAndClassEnd();
				this.notYetFormattedFinalState = false;
			} else {
				//error: we may format just one state
				//with this formatter (for exactly one
				//path condition)
				failFormatState(finalState);
			}
		} catch (Exception e) {
			failFormatState(finalState, e);
		}
	}

	@Override
	public String emit() {
		return this.output.toString();
	}

	@Override
	public void cleanup() {
		this.symbolsToVariables.clear();
		this.variablesToSymbols.clear();
		this.inputVariables.clear();
		this.output.delete(0, this.output.length());
		this.notYetFormattedFinalState = true;
	}

	private static final String INDENT_1 = "    ";
	private static final String INDENT_2 = INDENT_1 + INDENT_1;
	private static final String INDENT_3 = INDENT_1 + INDENT_2;
	private static final String INDENT_4 = INDENT_1 + INDENT_3;
	private static final String INDENT_5 = INDENT_1 + INDENT_4;
	private static final String INDENT_6 = INDENT_1 + INDENT_5;
	private static final String INDENT_7 = INDENT_1 + INDENT_6;
	private static final String PROLOGUE_1 =
	"import static sushi.compile.distance.StringDistanceFunctions.*;\n" +
	"import static sushi.compile.path_condition_distance.DistanceBySimilarityWithPathCondition.distance;\n" +
	"import static sushi.compile.path_condition_distance.DistanceBySimilarityWithPathCondition.getBackbone;\n" +
	"import static sushi.compile.path_condition_distance.DistanceBySimilarityWithPathCondition.makeBackbone;\n" +
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
	"public class PathConditionEvaluator_";
	private static final String PROLOGUE_2 = " {\n" +
	INDENT_1 + "private static final double SMALL_DISTANCE = 1.0d;\n" +
	INDENT_1 + "private static final double BIG_DISTANCE = 1E300;\n" +
	"\n";
	private static final String PROLOGUE_3 =
	INDENT_1 + "//fields\n\n" +
	INDENT_1 + "/** the EvoSuite instrumenting {@link ClassLoader} */\n" +
	INDENT_1 + "private final ClassLoader classLoader;\n" +
	INDENT_1 + "/** String constants from the symbolic state */\n" +
	INDENT_1 + "private final HashMap<Long, String> constants = new HashMap<>();\n" +
	INDENT_1 + "private final ArrayList<ClauseSimilarityHandler> pathConditionHandlers = new ArrayList<>();\n\n" +
	INDENT_1 + "/**\n" +
	INDENT_1 + " * Constructor.\n" +
	INDENT_1 + " *\n" +
	INDENT_1 + " * @param classLoader the EvoSuite instrumenting {@link ClassLoader}, used by\n" + 
	INDENT_1 + " *        EvoSuite to load/instrument the classes during the test execution.\n" + 
	INDENT_1 + " *        This path condition evaluators shall receive it from EvoSuite.\n" +
	INDENT_1 + " */\n" +
	INDENT_1 + "public PathConditionEvaluator_";

	private static final Signature JAVA_STRING_CONTAINS = 
	new Signature(JAVA_STRING, "(" + REFERENCE + JAVA_CHARSEQUENCE + TYPEEND + ")" + BOOLEAN, "contains");
	private static final Signature JAVA_STRING_ENDSWITH = 
	new Signature(JAVA_STRING, "(" + REFERENCE + JAVA_STRING + TYPEEND + ")" + BOOLEAN, "endsWith");
	private static final Signature JAVA_STRING_STARTSWITH = 
	new Signature(JAVA_STRING, "(" + REFERENCE + JAVA_STRING + TYPEEND + ")" + BOOLEAN, "startsWith");

	private static String javaType(Symbolic symbol) {
		if (symbol instanceof Primitive) { //either PrimitiveSymbolic or Term (however, it should never be the case of a Term)
			final char type = ((Primitive) symbol).getType();
			return toPrimitiveOrVoidCanonicalName(type);
		} else if (symbol instanceof ReferenceSymbolic) {
			final String type = ((ReferenceSymbolic) symbol).getStaticType();
			return internalToCanonicalTypeName(type);
		} else {
			//this should never happen
			throw new UnexpectedInternalException("Reached unreachable branch while calculating the Java type of a symbol: Perhaps some type of symbol is not handled yet.");
		}
	}


	private void appendPathConditionHandlers(State initialState, State finalState) 
	throws InvalidInputException {
		//we are still in the constructor of the path condition
		//evaluator class. First, we declare a temporary variable
		//for ValueCalculators
		this.output.append(INDENT_2);
		this.output.append("ValueCalculator valueCalculator;\n");
		
		//scans the path condition and for each clause
		//(excluded the irrelevant ones) creates a suitable
		//ClauseSimilarityHandler
		final List<Clause> pathCondition = finalState.getPathCondition();
		final int pathConditionSize = pathCondition.size();
		int currentClauseCounter = 0;
		final ClassHierarchy hier = finalState.getClassHierarchy();
		for (Clause clause : pathCondition) {
			++currentClauseCounter;
			if (shouldSkip(hier, clause)) {
				continue;
			}
			if (clause instanceof ClauseAssumeExpands) {
				//appends a comment before the clause similarity handler
				this.output.append(INDENT_2);
				this.output.append("// "); //comment
				this.output.append(clause.toString());
				this.output.append("\n");
				//puts the clause similarity handler
				final ClauseAssumeExpands clauseExpands = (ClauseAssumeExpands) clause;
				final ReferenceSymbolic symbol = clauseExpands.getReference();
				final long heapPosition = clauseExpands.getHeapPosition();
				final boolean relax = this.shallRelaxLastExpansionClause && currentClauseCounter == pathConditionSize;
				appendNewSimilarityWithRefToFreshObject(initialState, finalState, symbol, heapPosition, relax);
			} else if (clause instanceof ClauseAssumeNull) {
				this.output.append(INDENT_2);
				this.output.append("// "); //comment
				this.output.append(clause.toString());
				this.output.append("\n");
				final ClauseAssumeNull clauseNull = (ClauseAssumeNull) clause;
				final ReferenceSymbolic symbol = clauseNull.getReference();
				appendNewSimilarityWithRefToNull(initialState, symbol);
			} else if (clause instanceof ClauseAssumeAliases) {
				this.output.append(INDENT_2);
				this.output.append("// "); //comment
				this.output.append(clause.toString());
				this.output.append("\n");
				final ClauseAssumeAliases clauseAliases = (ClauseAssumeAliases) clause;
				final ReferenceSymbolic symbol = clauseAliases.getReference();
				final long heapPosition = clauseAliases.getHeapPosition();
				appendNewSimilarityWithRefToAlias(initialState, finalState, symbol, heapPosition);
			} else if (clause instanceof ClauseAssume) {
				this.output.append(INDENT_2);
				this.output.append("// "); //comment
				this.output.append(clause.toString());
				this.output.append("\n");
				final ClauseAssume clauseAssume = (ClauseAssume) clause;
				final Primitive assumption = clauseAssume.getCondition();
				if (isAssumptionOnBooleanApply(assumption)) {
					appendNewValueCalculatorBooleanApply(initialState, finalState, assumption);
				} else {
					appendNewValueCalculatorOthers(initialState, finalState, assumption);
				}
				appendNewSimilarityWithNumericExpression();
			} //else, do nothing
		}
		this.output.append("\n");
		this.output.append(INDENT_1);
		this.output.append("}\n\n"); //this closes the constructor body
	}

	/**
	 * Determines whether a path condition clause 
	 * should be skipped.
	 * 
	 * @param hier a {@link ClassHierarchy}
	 * @param clause the {@link Clause} to analyze.
	 * @return a {@code boolean}, {@code true} iff
	 *         the clause must be skipped.
	 */
	private static boolean shouldSkip(ClassHierarchy hier, Clause clause) {
		if (clause instanceof ClauseAssumeReferenceSymbolic) {
			final ReferenceSymbolic ref = ((ClauseAssumeReferenceSymbolic) clause).getReference(); 
			//exclude all the ClauseAssumeReferenceSymbolic
			//that refer to the resolution of a symbolic 
			//reference that is a function application
			if (ref instanceof ReferenceSymbolicApply) {
				return true;
			}

			//exclude all the ClauseAssumeReferenceSymbolic
			//that refer to the resolution of the field 
			//initialMap of HashMap models, because 
			//initialMap is an internal field of the symbolic 
			//execution models and does not exist in the 
			//concrete, standard HashMaps
			if (isInitialMapField(hier, ref)) {
				return true;
			}
		}

		//all other clauses are accepted
		return false;
	}

	private void appendMethodDeclaration(State initialState, State finalState) 
	throws InvalidInputException {
		//builds the list of the symbols corresponding
		//to the inputs of the method under test
		final List<Symbolic> inputs;
		try {
			inputs = initialState.getStack().get(0).localVariables().values().stream()
			.filter(v -> v.getValue() instanceof Symbolic)
			.map(v -> (Symbolic) v.getValue())
			.collect(Collectors.toList());
		} catch (IndexOutOfBoundsException | FrozenStateException e) {
			throw new UnexpectedInternalException(e);
		}

		//appends the method declaration and a comment
		this.output.append(INDENT_1);
		this.output.append("public double distance(");
		boolean firstDone = false;
		for (Symbolic symbol : inputs) {
			final String javaType = javaType(symbol);
			makeVariableFor(symbol, initialState);
			final String varName = getVariableFor(symbol);
			this.inputVariables.add(varName);
			if (firstDone) {
				this.output.append(", ");
			} else {
				firstDone = true;
			}
			this.output.append(javaType);
			this.output.append(' ');
			this.output.append(varName);
		}
		this.output.append(") throws Exception {\n");
		this.output.append(INDENT_2);
		this.output.append("//generated for state ");
		this.output.append(finalState.getBranchIdentifier());
		this.output.append('[');
		this.output.append(finalState.getSequenceNumber());
		this.output.append("]\n");
	}

	private void appendGetBackbone() {
		this.output.append(INDENT_2);
		this.output.append("//tries to retrieve the backbone\n");
		this.output.append(INDENT_2);
		this.output.append("CandidateBackbone backbone = getBackbone();\n");
		this.output.append(INDENT_2);
		this.output.append("if (backbone == null) {\n"); //starts an if statement
		this.output.append(INDENT_3);
		this.output.append("//if if fails, makes it\n");
		this.output.append(INDENT_3);
		this.output.append("backbone = makeBackbone(this.classLoader);\n");
		this.output.append(INDENT_2);
		this.output.append("//note that this new backbone is in reset state, thus\n");
		this.output.append(INDENT_2);
		this.output.append("//it must be refreshed before it can be used to\n");
		this.output.append(INDENT_2);
		this.output.append("//calculate distances\n");
		this.output.append(INDENT_2);
		this.output.append("}\n;"); //closes the if statement
		this.output.append(INDENT_2);
		this.output.append("//checks if the backbone is reset, and in the case\n");
		this.output.append(INDENT_2);
		this.output.append("//refreshes it\n");
		this.output.append(INDENT_2);
		this.output.append("if (backbone.isReset()) {\n"); //starts an if statement
		//now we need to create the roots and the 
		//stringCalculators: this is done in the appendRoots and
		//appendStringCalculators method. The last if statement
		//will be completed in the appendRefreshBackbone method.
	}

	private void appendRoots(State initialState) {
		this.output.append(INDENT_3);
		this.output.append("final HashMap<String, Object> roots = new HashMap<>();\n");
		for (String inputVariable : this.inputVariables) {
			this.output.append(INDENT_3);
			this.output.append("roots.put(\"");
			this.output.append(getPathString(getSymbolFor(inputVariable), initialState));
			this.output.append("\", ");
			this.output.append(inputVariable);
			this.output.append(");\n");
		}			
		this.output.append("\n");
	}

	private void appendStringCalculators(State initialState, State finalState) 
	throws InvalidInputException {
		this.output.append(INDENT_3);
		this.output.append("final HashMap<Long, StringCalculator> stringCalculators = new HashMap<>();\n");
		for (long heapPosition : this.stringNonconstants) {
			final ReferenceConcrete refString = new ReferenceConcrete(heapPosition);
			final Instance instanceString = (Instance) finalState.getObject(refString);
			final Reference refValue = (Reference) instanceString.getFieldValue(JAVA_STRING_VALUE);
			final Array array = (Array) finalState.getObject(refValue);
			if (array == null) {
				continue; //TODO when does this happen?
			}
			final Set<Symbolic> symbols = symbolsInArrayOfChars(array, finalState);
			this.output.append(INDENT_3);
			this.output.append("stringCalculators.put(");
			this.output.append(heapPosition);
			
			//here starts the declaration of a new StringCalculator
			this.output.append("L, new StringCalculator() {\n");
			this.output.append(INDENT_4);

			//declares the StringCalculator.getVariablesPathStrings method 
			this.output.append("@Override public Iterable<String> getVariablesPathStrings() {\n");
			this.output.append(INDENT_5);
			this.output.append("final ArrayList<String> retVal = new ArrayList<>();\n");       
			for (Symbolic symbol: symbols) {
				this.output.append(INDENT_5);
				this.output.append("retVal.add(\"");
				this.output.append(getPathString(symbol, initialState));
				this.output.append("\");\n");
			}
			this.output.append(INDENT_5);
			this.output.append("return retVal;\n");       
			this.output.append(INDENT_4);
			this.output.append("}\n"); //closes the getVariablesPathStrings method

			//declares the StringCalculator.computeString method:
			this.output.append(INDENT_4);
			this.output.append("@Override public String computeString(List<Object> variablesValues) {\n");
			//1- puts all the variablesValues into 
			//local variables in the method 
			int i = 0; //counter
			for (Symbolic symbol : symbols) {
				makeVariableFor(symbol, initialState);
				this.output.append(INDENT_5);
				this.output.append("final ");
				this.output.append(javaType(symbol));
				this.output.append(" ");
				this.output.append(getVariableFor(symbol));
				this.output.append(" = (");
				this.output.append(javaType(symbol));
				this.output.append(") variablesValues.get(");
				this.output.append(i);
				this.output.append(");\n");
				++i;
			}
			this.output.append("\n");
			//2- declares a variable char[] arrayTmp,
			//an array of characters, that has the
			//same length as the symbolic array. To
			//translate the (symbolic) length of the
			//array we use an ArrayAppender.
			this.output.append(INDENT_5);
			this.output.append("final char[] arrayTmp = new char[");
			final ArrayAppender appender = new ArrayAppender(array.getIndex());
			appender.append(this.output, array.getLength());
			this.output.append("];\n");
			//3- for all the positions i in arrayTmp, scans 
			//the entries in the symbolic array and
			//creates an if-elseif statement that updates
			//arrayTmp[i] with the correct concrete value
			this.output.append(INDENT_5);
			this.output.append("for (int i = 0; i < "); //
			appender.append(this.output, array.getLength());
			this.output.append("; ++i) {\n");
			boolean firstDone = false;
			for (Iterator<? extends AccessOutcomeIn> it = array.entries().iterator(); it.hasNext(); ) {
				this.output.append(INDENT_6);
				if (firstDone) {
					this.output.append("} else ");
				} else {
					firstDone = true;
				}
				this.output.append("if (");
				final AccessOutcomeIn entry = it.next();
				appender.append(this.output, entry.getAccessCondition());
				this.output.append(") {\n");
				this.output.append(INDENT_7);
				this.output.append("arrayTmp[i] = ");
				if (entry instanceof AccessOutcomeInValue) {
					final Primitive entryValue = (Primitive) ((AccessOutcomeInValue) entry).getValue();
					final Primitive charValue;
					try {
						charValue = this.calc.push(entryValue).to(CHAR).pop();
					} catch (NoSuchElementException | InvalidTypeException | InvalidOperandException e) {
						//this should never happen
						throw new UnexpectedInternalException(e);
					}
					appender.append(this.output, charValue);
				} else {
					final AccessOutcomeInInitialArray entryInitialArray = ((AccessOutcomeInInitialArray) entry);
					final Array arrayInitial = (Array) finalState.getObject(entryInitialArray.getInitialArray());
					this.output.append(getVariableFor(arrayInitial.getOrigin()));
					this.output.append("[i + (");
					appender.append(this.output, entryInitialArray.getOffset());
					this.output.append(")]");
				}
				this.output.append(";\n");
			}
			if (array.entries().size() > 0) {
				this.output.append(INDENT_6);
				this.output.append("}\n"); //closes the if-else if statement
			}
			//well, perhaps it never happens that
			//a symbolic array has no entries:
			//however, if this happens, there 
			//is no if-elseif statement inside 
			//the for loop, so we do not put its closure
			this.output.append(INDENT_5);
			this.output.append("}\n"); //closes the for loop
			this.output.append(INDENT_5);
			//4- returns a new String backed by arrayTmp
			this.output.append("return new String(arrayTmp);\n");
			this.output.append(INDENT_4);
			this.output.append("}\n"); //closes the computeString method
			this.output.append(INDENT_3);
			this.output.append("});\n"); //closes the StringCalculator anonymous class declaration and the statement that contains it                  
		}
		this.output.append("\n");
	}

	/**
	 * Scavenges an {@link Array} (of chars) for all the symbols
	 * in it.
	 * 
	 * @param a the {@link Array} to scavenge.
	 * @param finalState the {@link State} whose heap contains {@code a}.
	 * @return a {@link List}{@code <}{@link Symbolic}{@code >}.
	 */
	private static Set<Symbolic> symbolsInArrayOfChars(Array a, State finalState) {
		final HashSet<Symbolic> retVal = new HashSet<>();
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
			public void visitPrimitiveSymbolicApply(PrimitiveSymbolicApply x) {
				if (retVal.contains(x)) {
					return; //surely its args have been processed
				}
				retVal.add(x);
			}

			@Override
			public void visitSimplex(Simplex x) { }

			@Override
			public void visitTerm(Term x) { }

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
				if (retVal.contains(x)) {
					return;
				}
				retVal.add(x);
			}

			@Override
			public void visitPrimitiveSymbolicLocalVariable(PrimitiveSymbolicLocalVariable x) {
				if (retVal.contains(x)) {
					return;
				}
				retVal.add(x);
			}

			@Override
			public void visitPrimitiveSymbolicMemberArray(PrimitiveSymbolicMemberArray x) {
				if (retVal.contains(x)) {
					return;
				}
				retVal.add(x);
			}

			@Override
			public void visitPrimitiveSymbolicMemberArrayLength(PrimitiveSymbolicMemberArrayLength x) {
				if (retVal.contains(x)) {
					return;
				}
				retVal.add(x);
			}

			@Override
			public void visitPrimitiveSymbolicMemberField(PrimitiveSymbolicMemberField x) {
				if (retVal.contains(x)) {
					return;
				}
				retVal.add(x);
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
					retVal.add(initialArrayOrigin);
				}
			}
		} catch (Exception exc) {
			//this should never happen
			throw new UnexpectedInternalException(exc);
		}
		return retVal;
	}


	private final class ArrayAppender implements PrimitiveVisitor {
		private final Term arrayIndex;

		ArrayAppender(Term arrayIndex) {
			this.arrayIndex = arrayIndex;
		}
		
		private StringBuilder output = null;
		
		public void append(StringBuilder output, Primitive p) 
		throws InvalidInputException {
			if (output == null || p == null) {
				throw new InvalidInputException("StateFormatterSushiPathCondition.ArrayEvaluator.append: invoked with null parameter.");
			}
			this.output = output;
			
			//fills this.b
			try {
				p.accept(this);
			} catch (InvalidInputException e) {
				throw e;
			} catch (Exception e) {
				//this should never happen
				throw new AssertionError(e);
			}
		}

		@Override
		public void visitAny(Any x) { }

		@Override
		public void visitExpression(Expression e) throws Exception {
			this.output.append('(');
			if (e.isUnary()) {
				this.output.append(e.getOperator());
				this.output.append(' ');
				e.getOperand().accept(this); //appends the operand
			} else {
				e.getFirstOperand().accept(this); //appends the first operand
				this.output.append(' ');
				this.output.append(e.getOperator());
				this.output.append(' ');
				e.getSecondOperand().accept(this); //appends the second operand
			}
			this.output.append(')');
		}

		@Override
		public void visitPrimitiveSymbolicApply(PrimitiveSymbolicApply x)
		throws InvalidInputException {
			throw new InvalidInputException("StateFormatterSushiPathCondition.ArrayEvaluator.append: cannot handle a subterm of class PrimitiveSymbolicApply (currently unsupported by this formatter).");
		}

		@Override
		public void visitSimplex(Simplex x) {
			this.output.append(x.toString());
		}

		@Override
		public void visitTerm(Term x) throws InvalidInputException {
			if (x == this.arrayIndex) {
				this.output.append('i');
			} else {
				throw new InvalidInputException("StateFormatterSushiPathCondition.ArrayEvaluator.append: cannot handle a subterm of class Term, unless it is the symbol for the array index.");
			}
		}

		@Override
		public void visitNarrowingConversion(NarrowingConversion x) 
		throws Exception {
			this.output.append("((");
			this.output.append(toPrimitiveOrVoidCanonicalName(x.getType()));
			this.output.append(") ");
			x.getArg().accept(this); //appends the arg
			this.output.append(')');
		}

		@Override
		public void visitWideningConversion(WideningConversion x) throws Exception {
			x.getArg().accept(this); //just appends the arg
		}

		@Override
		public void visitPrimitiveSymbolicHashCode(PrimitiveSymbolicHashCode x)  
		throws InvalidInputException {
			this.output.append(getVariableFor(x));
		}

		@Override
		public void visitPrimitiveSymbolicLocalVariable(PrimitiveSymbolicLocalVariable x) 
		throws InvalidInputException {
			this.output.append(getVariableFor(x));
		}

		@Override
		public void visitPrimitiveSymbolicMemberArray(PrimitiveSymbolicMemberArray x) 
		throws InvalidInputException {
			this.output.append(getVariableFor(x));
		}

		@Override
		public void visitPrimitiveSymbolicMemberArrayLength(PrimitiveSymbolicMemberArrayLength x) 
		throws InvalidInputException {
			this.output.append(getVariableFor(x));
		}

		@Override
		public void visitPrimitiveSymbolicMemberField(PrimitiveSymbolicMemberField x) 
		throws InvalidInputException {
			this.output.append(getVariableFor(x));
		}            
	}

	private void appendRefreshBackbone() {
		//now that we have declared the roots and the stringCalculators
		//we can refresh the backbone, and conclude the if statement 
		//that we started in the appendGetBackbone method
		this.output.append(INDENT_3);
		this.output.append("backbone.refresh(this.constants, roots, stringCalculators);\n");
		this.output.append(INDENT_2);		
		this.output.append("}\n"); //closes the if statement
		this.output.append("//now the backbone can be used\n");
	}

	private void appendDistanceCalculation() {
		this.output.append(INDENT_2);
		this.output.append("final double d = distance(pathConditionHandlers, backbone);\n");
		this.output.append(INDENT_2);
		this.output.append("if (d == 0.0d) { //perhaps we should thing a better check than equality between floating point numbers...\n");
		this.output.append(INDENT_3);
		this.output.append("System.out.println(\"PathConditionEvaluator_");
		this.output.append(this.identifier);
		this.output.append(": 0 distance\");\n");
		this.output.append(INDENT_2);
		this.output.append("}\n");
		this.output.append(INDENT_2);
		this.output.append("return d;\n");
	}

	private void appendMethodAndClassEnd() {
		this.output.append(INDENT_1);
		this.output.append("}\n"); //this closes the method
		this.output.append("}\n"); //this closes the class declaration
	}

	private void failFormatPrologue(Exception e) {
		this.failed = true;
		this.output.delete(0, this.output.length());
		this.output.append("//Unable to generate prologue: exception thrown, class ");
		this.output.append(e.getClass().getName());
		final String message = e.getMessage();
		if (message != null) {
			this.output.append(", message: ");
			this.output.append(e.getMessage());
		}
		this.output.append("\n");
	}

	private void failFormatState(State finalState) {
		this.failed = true;
		this.output.delete(0, this.output.length());
		this.output.append("//Unable to generate distance calculator method for state ");
		this.output.append(finalState.getBranchIdentifier());
		this.output.append('[');
		this.output.append(finalState.getSequenceNumber());
		this.output.append("]: attempted to format more than one state, this formatter can format exactly one final state\n");
	}

	private void failFormatState(State finalState, Exception e) {
		this.failed = true;
		this.output.delete(0, this.output.length());
		this.output.append("//Unable to generate distance calculator method for state ");
		this.output.append(finalState.getBranchIdentifier());
		this.output.append('[');
		this.output.append(finalState.getSequenceNumber());
		this.output.append("]: exception thrown, class ");
		this.output.append(e.getClass().getName());
		final String message = e.getMessage();
		if (message != null) {
			this.output.append(", message: ");
			this.output.append(e.getMessage());
		}
		this.output.append("\n");
	}

	private void appendNewSimilarityWithRefToFreshObject(State initialState, State finalState, ReferenceSymbolic symbol, long heapPosition, boolean relax) 
	throws FrozenStateException {
		this.output.append(INDENT_2);
		this.output.append("this.pathConditionHandlers.add(new SimilarityWithRefToFreshObject(\"");
		this.output.append(getPathString(symbol, initialState));
		if (relax) {
			//invokes the constructor of SimilarityWithRefToFreshObject
			//for forbidden expansions: adds all the forbidden expansion 
			//classes as arguments
			this.output.append("\", \"\""); //adds the foo string parameter
			for (String forbiddenExpansionClass : this.forbiddenExpansions) {
				this.output.append(", PathConditionEvaluator_");
				this.output.append(this.identifier);
				this.output.append(".this.classLoader.loadClass(\"");
				this.output.append(internalToBinaryClassName(forbiddenExpansionClass));
				this.output.append("\")");
			}
			this.output.append("));\n");
		} else {
			//invokes the constructor of SimilarityWithRefToFreshObject
			//for allowed expansion: adds the only allowed expansion
			//to arguments
			final String expansionClass = getClassNameOfObjectInHeap(finalState, heapPosition);
			this.output.append("\", PathConditionEvaluator_");
			this.output.append(this.identifier);
			this.output.append(".this.classLoader.loadClass(\"");
			this.output.append(internalToBinaryClassName(expansionClass));
			this.output.append("\")));\n");
		}
	}

	private void appendNewSimilarityWithRefToNull(State initialState, ReferenceSymbolic symbol) {
		this.output.append(INDENT_2);
		this.output.append("this.pathConditionHandlers.add(new SimilarityWithRefToNull(\"");
		this.output.append(getPathString(symbol, initialState));
		this.output.append("\"));\n");
	}

	private void appendNewSimilarityWithRefToAlias(State initialState, State finalState, ReferenceSymbolic symbol, long heapPosition) 
	throws InvalidInputException {
		final String target = getOriginStringOfObjectInHeap(initialState, finalState, heapPosition);
		this.output.append(INDENT_2);
		this.output.append("this.pathConditionHandlers.add(new SimilarityWithRefToAlias(\"");
		this.output.append(getPathString(symbol, initialState));
		this.output.append("\", \"");
		this.output.append(target);
		this.output.append("\"));\n");
	}
	
	//this must be invoked after invoking one of the methods 
	//that append the creation of a new ValueCalculator
	private void appendNewSimilarityWithNumericExpression() {
		this.output.append(INDENT_2);
		this.output.append("this.pathConditionHandlers.add(new SimilarityWithNumericExpression(valueCalculator));\n");
		this.output.append(INDENT_2);
		this.output.append("valueCalculator = null; //just for safety, so the next similarity handler cannot mistakenly use it\n");
	}

	private int varCounter = 0;
	private String generateVariableNameFromOriginString(String originString) {
		//we are faking! we do not generate the variable
		//name from originString, we use a counter
		return "V" + this.varCounter++;
	}

	private void makeVariableFor(Symbolic symbol, State initialState) 
	throws InvalidInputException {
		if (symbol == null || initialState == null) {
			throw new InvalidInputException("StateFormatterSushiPathCondition.getVariableFor: invoked with null parameter.");
		}
		if (this.symbolsToVariables.containsKey(symbol)) {
			return; //does nothing
		}
		
		final String originString = getPathString(symbol, initialState);
		final String variableName = generateVariableNameFromOriginString(originString);
		this.symbolsToVariables.put(symbol, variableName);
		this.variablesToSymbols.put(variableName, symbol);
	}

	private String getVariableFor(Symbolic symbol) 
	throws InvalidInputException {
		if (symbol == null) {
			throw new InvalidInputException("StateFormatterSushiPathCondition.getVariableFor: invoked with null parameter.");
		}
		if (this.symbolsToVariables.containsKey(symbol)) {
			return this.symbolsToVariables.get(symbol);
		} else {
			throw new InvalidInputException("StateFormatterSushiPathCondition.getVariableFor: no variable for symbol " + symbol.toString() + ".");
		}
	}
	
	private String getVariableFor(ReferenceConcrete referenceConcrete) 
	throws InvalidInputException {
		if (referenceConcrete == null) {
			throw new InvalidInputException("StateFormatterSushiPathCondition.getVariableFor: invoked with null parameter.");
		}
		//much simpler, we do not store these variable names
		return "S" + referenceConcrete.getHeapPosition();
	}

	private Symbolic getSymbolFor(String varName) {
		return this.variablesToSymbols.get(varName);
	}

	private static String getClassNameOfRootObject(State initialState) 
	throws ThreadStackEmptyException {
		return initialState.getRootClass().getClassName();
	}

	private static String getClassNameOfObjectInHeap(State finalState, long num) 
	throws FrozenStateException {
		final Map<Long, Objekt> heap = finalState.getHeap();
		final Objekt o = heap.get(num);
		return o.getType().getClassName();
	}

	private String getOriginStringOfObjectInHeap(State initialState, State finalState, long heapPos) 
	throws InvalidInputException {
		final Collection<Clause> pathCondition = finalState.getPathCondition();
		for (Clause clause : pathCondition) {
			if (clause instanceof ClauseAssumeExpands) {
				final ClauseAssumeExpands clauseExpands = (ClauseAssumeExpands) clause;
				final long heapPosCurrent = clauseExpands.getHeapPosition();
				if (heapPosCurrent == heapPos) {
					return getPathString(clauseExpands.getReference(), initialState);
				}
			}
		}
		throw new InvalidInputException("StateFormatterSushiPathCondition.getOriginStringOfObjectInHeap: cannot find an expansion for heap position " + heapPos + ".");
	}

	//these kinds of assumption have shape:
	//(WIDEN-I(java/lang/String:(...)Z:<METHOD>(...)@...)) != (0)
	//or == (0), != (1), == (1), where <METHOD> is one of:
	//equals, contains, endsWith, startsWith.
	private static boolean isAssumptionOnBooleanApply(Primitive assumption) {
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
				JAVA_STRING_CONTAINS.toString().equals(applyOperator) ||
				JAVA_STRING_ENDSWITH.toString().equals(applyOperator) ||
				JAVA_STRING_STARTSWITH.toString().equals(applyOperator);
			} else {
				return false;
			}
		} else {
			return false;
		}
	}

	//This method currently supports only four boolean test methods, 
	//that operate all on string pairs. Therefore we can assume the
	//arguments of the underlying PrimitiveSymbolicApply
	//object are all (symbolic or concrete) references 
	//to strings. Additionally, we assume that all the 
	//arguments that are concrete references to strings 
	//must refer to constants (stored in this.constants).
	private void appendNewValueCalculatorBooleanApply(State initialState, State finalState, Primitive assumption) 
	throws InvalidInputException {
		//gets the arguments and the operator
		//of the nested symbolic apply from the
		//symbolic assumption, moreover
		//determines whether the result of 
		//such function application is compared
		//against zero or one
		final Value[] applyArgs; 
		final String applyOperator;
		final boolean isAssumptionAnEqualityToOne;
		{
			final Expression assumptionExpression = (Expression) assumption;
			final Primitive assumptionFirstOperand = assumptionExpression.getFirstOperand();
			final Primitive assumptionSecondOperand = assumptionExpression.getSecondOperand();
			final WideningConversion wideningOperand = (WideningConversion) ((assumptionFirstOperand instanceof WideningConversion) ? assumptionFirstOperand : assumptionSecondOperand);
			final PrimitiveSymbolicApply apply = (PrimitiveSymbolicApply) wideningOperand.getArg();
			applyArgs = apply.getArgs();
			applyOperator = apply.getOperator();
			final Operator assumptionOperator = assumptionExpression.getOperator();
			final Simplex simplexOperand = (Simplex) ((assumptionFirstOperand instanceof Simplex) ? assumptionFirstOperand : assumptionSecondOperand);
			final boolean simplexOperandIsZero = simplexOperand.isZeroOne(true);
			isAssumptionAnEqualityToOne = (assumptionOperator == Operator.NE && simplexOperandIsZero) || (assumptionOperator == Operator.EQ && !simplexOperandIsZero);
		}

		//divides the arguments of the symbolic apply
		//into concrete and symbolic; contextually, 
		//checks that have all reference type
		final ArrayList<ReferenceSymbolic> applyArgsReferenceSymbolic = new ArrayList<>();
		final ArrayList<ReferenceConcrete> applyArgsReferenceConcrete = new ArrayList<>();
		for (Value arg : applyArgs) {
			if (arg instanceof ReferenceSymbolic) {
				final ReferenceSymbolic argReferenceSymbolic = (ReferenceSymbolic) arg;
				if (!applyArgsReferenceSymbolic.contains(argReferenceSymbolic)) {
					applyArgsReferenceSymbolic.add(argReferenceSymbolic);
				}
			} else if (arg instanceof ReferenceConcrete)  {
				final ReferenceConcrete argReferenceConcrete = (ReferenceConcrete) arg;
				if (!applyArgsReferenceConcrete.contains(argReferenceConcrete)) {
					applyArgsReferenceConcrete.add((ReferenceConcrete) arg);
				}
			} else {
				throw new InvalidInputException("Cannot handle a function application of operator " + applyOperator + " to an argument that is not a reference (currently we are not able to manage other kinds of arguments).");
			}
		}
		//since all the args are references, it is not necessary to
		//scavenge further the assumption: all the symbols are there

		//declares a ValueCalculator anonymous class and object
		this.output.append(INDENT_2);
		this.output.append("valueCalculator = new ValueCalculator() {\n");

		//declares the ValueCalculator.getVariablesPathStrings method
		this.output.append(INDENT_3);
		this.output.append("@Override public Iterable<String> getVariablesPathStrings() {\n");
		this.output.append(INDENT_4);
		this.output.append("final ArrayList<String> retVal = new ArrayList<>();\n");       
		for (ReferenceSymbolic symbol: applyArgsReferenceSymbolic) { //only for symbolic args!
			this.output.append(INDENT_4);
			this.output.append("retVal.add(\"");
			this.output.append(getPathString(symbol, initialState));
			this.output.append("\");\n");
		}
		this.output.append(INDENT_4);
		this.output.append("return retVal;\n");       
		this.output.append(INDENT_3);
		this.output.append("}\n"); //closes ValueCalculator.getVariablesPathStrings

		//declares the ValueCalculator.getConstantsHeapPositions method
		this.output.append(INDENT_3);
		this.output.append("@Override public Iterable<Long> getConstantsHeapPositions() {\n");
		this.output.append(INDENT_4);
		this.output.append("final ArrayList<Long> retVal = new ArrayList<>();\n");       
		for (ReferenceConcrete referenceConcrete: applyArgsReferenceConcrete) { //only for concrete args!
			this.output.append(INDENT_4);
			this.output.append("retVal.add(Long.valueOf(");
			this.output.append(referenceConcrete.getHeapPosition());
			this.output.append("));\n");
		}
		this.output.append(INDENT_4);
		this.output.append("return retVal;\n");       
		this.output.append(INDENT_3);
		this.output.append("}\n"); //closes ValueCalculator.getConstantsHeapPositions

		//declares the ValueCalculator.computeDistance method
		this.output.append(INDENT_3);
		this.output.append("@Override public double computeDistance(List<Object> variablesValues, List<Object> constantsValues) {\n");
		//1- puts all the variablesValues into 
		//local variables in the method 
		int i = 0; //counter
		for (ReferenceSymbolic symbol: applyArgsReferenceSymbolic) {
			makeVariableFor(symbol, initialState);
			this.output.append(INDENT_4);
			this.output.append("final ");
			this.output.append(javaType(symbol));
			this.output.append(' ');
			this.output.append(getVariableFor(symbol));
			this.output.append(" = (");
			this.output.append(javaType(symbol));
			this.output.append(") variableValues.get(");
			this.output.append(i);
			this.output.append(");\n");
			++i;
		}
		//2- puts all the constantsValues into 
		//local variables in the method
		i = 0; //resets counter
		for (ReferenceConcrete referenceConcrete: applyArgsReferenceConcrete) {
			this.output.append(INDENT_4);
			this.output.append("final String ");
			this.output.append(getVariableFor(referenceConcrete));
			this.output.append(" = (String) constantsValues.get(");
			this.output.append(i);
			this.output.append(");\n");
			++i;
		}
		
		//returns the distance
		this.output.append(INDENT_4);
		this.output.append("return ");
		if (JAVA_STRING_EQUALS.toString().equals(applyOperator)) {
			appendJavaStringComparisonDistanceExpression(finalState, applyArgs, isAssumptionAnEqualityToOne, "equals", "distanceEditLevenshtein");
		} else if (JAVA_STRING_CONTAINS.toString().equals(applyOperator)) {
			appendJavaStringComparisonDistanceExpression(finalState, applyArgs, isAssumptionAnEqualityToOne, "contains", "distanceContainment");
		} else if (JAVA_STRING_ENDSWITH.toString().equals(applyOperator)) {
			appendJavaStringComparisonDistanceExpression(finalState, applyArgs, isAssumptionAnEqualityToOne, "endsWith", "distanceSuffix");
		} else if (JAVA_STRING_STARTSWITH.toString().equals(applyOperator)) {
			appendJavaStringComparisonDistanceExpression(finalState, applyArgs, isAssumptionAnEqualityToOne, "startsWith", "distancePrefix");
		} else {
			//this should never happen
			throw new UnexpectedInternalException("Unexpected function application of " + applyOperator + " for which a fitness function does not exist.");
		}
		this.output.append(";\n");
		this.output.append(INDENT_3);
		this.output.append("}\n"); //closes ValueCalculator.computeDistance
		this.output.append(INDENT_2);
		this.output.append("};\n"); //closes the ValueCalculator anonymous class declaration
	}

	private void appendNewValueCalculatorOthers(State initialState, State finalState, Primitive assumption) 
	throws InvalidInputException {
		//gets all the (primitive) symbols in the assumption
		final Set<PrimitiveSymbolic> symbols = primitiveSymbolsIn(assumption);

		//declares a ValueCalculator anonymous class and object
		this.output.append(INDENT_2);
		this.output.append("valueCalculator = new ValueCalculator() {\n");
		
		//declares the ValueCalculator.getVariablesPathStrings method
		this.output.append(INDENT_3);
		this.output.append("@Override public Iterable<String> getVariablesPathStrings() {\n");
		this.output.append(INDENT_4);
		this.output.append("final ArrayList<String> retVal = new ArrayList<>();\n");       
		for (Symbolic symbol: symbols) {
			this.output.append(INDENT_4);
			this.output.append("retVal.add(\"");
			this.output.append(getPathString(symbol, initialState));
			this.output.append("\");\n");
		}
		this.output.append(INDENT_4);
		this.output.append("return retVal;\n");       
		this.output.append(INDENT_3);
		this.output.append("}\n"); //closes ValueCalculator.getVariablesPathStrings

		//declares the ValueCalculator.getConstantsHeapPositions method
		this.output.append(INDENT_3);
		this.output.append("@Override public Iterable<Long> getConstantsHeapPositions() {\n");
		this.output.append(INDENT_4);
		this.output.append("final ArrayList<String> retVal = new ArrayList<>();\n");       
		this.output.append(INDENT_4);
		this.output.append("return retVal;\n"); //no constants used 
		this.output.append(INDENT_3);
		this.output.append("}\n"); //closes ValueCalculator.getConstantsHeapPositions

		//declares the ValueCalculator.computeDistance method
		this.output.append(INDENT_3);
		this.output.append("@Override public double computeDistance(List<Object> variablesValues, List<Object> constantsValues) {\n");
		//1- puts all the variablesValues into 
		//local variables in the method 
		int i = 0; //counter
		for (PrimitiveSymbolic symbol : symbols) {
			makeVariableFor(symbol, initialState);
			this.output.append(INDENT_4);
			this.output.append("final ");
			this.output.append(javaType(symbol));
			this.output.append(' ');
			this.output.append(getVariableFor(symbol));
			this.output.append(" = (");
			this.output.append(javaType(symbol));
			this.output.append(") variableValues.get(");
			this.output.append(i);
			this.output.append(");\n");
			++i;
		}
		//2- returns the distance
		this.output.append(INDENT_4);
		this.output.append("return ");
		appendJavaDistanceExpression(initialState, assumption);
		this.output.append(";\n");
		this.output.append(INDENT_3);
		this.output.append("}\n"); //closes ValueCalculator.computeDistance
		this.output.append(INDENT_2);
		this.output.append("};\n"); //closes the ValueCalculator anonymous class declaration
	}

	private String getPathString(Symbolic symbol, State initialState) {
		String retVal = possiblyAdaptMapModelSymbols(symbol.asOriginString());
		if (retVal.contains("{ROOT}:")) {
			//we need to add the class of the parameter
			//to the origin string, because the CandidateBacbone
			//stores the origins of all the methods under test,
			//so we have to distinguish all the different 
			//{ROOT}:... parameters of different methods
			//that may have same name
			try {
				final String replacement = getClassNameOfRootObject(initialState) + ":{ROOT}:"; 
				retVal = retVal.replaceAll("\\{ROOT\\}:", replacement);
			} catch (ThreadStackEmptyException e) {
				//this should never happen
				throw new UnexpectedInternalException(e);
			}
		}
		return retVal;
	}

	/**
	 * Scavenges a numeric assumption for all the symbols
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
		final PrimitiveVisitor v = new PrimitiveVisitor() {
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
				retVal.add(x); //TODO why do we need to add it?
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

	private static Operator dual(Operator op) {
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

	private void appendJavaStringComparisonDistanceExpression(State state, Value[] applyArgs, boolean isAssumptionAnEqualityToOne, String methodStringComparisonName, String methodStringDistanceName) 
	throws InvalidInputException {
		//just two args in the current implementation:
		//gets the corresponding variable names; note that we do not
		//call makeVariableFor, it is assumed that all the variables
		//have been created before, an assumption that fits the use of
		//this method (it is used to produce the final distance 
		//calculation expression of a ValueCalculator, so in theory
		//all the relevant variables should have been created before,
		//or there is something that is not working)
		final String firstArgVariable;
		if (applyArgs[0] instanceof ReferenceSymbolic) {
			firstArgVariable = getVariableFor((ReferenceSymbolic) applyArgs[0]);
		} else {
			firstArgVariable = "S" + ((ReferenceConcrete) applyArgs[0]).getHeapPosition();
		}
		final String secondArgVariable;
		if (applyArgs[1] instanceof ReferenceSymbolic) {
			secondArgVariable = getVariableFor((ReferenceSymbolic) applyArgs[1]);
		} else {
			secondArgVariable = "S" + ((ReferenceConcrete) applyArgs[1]).getHeapPosition();
		}

		//builds the expression
		this.output.append("((");
		//first case: both arguments are null
		this.output.append(firstArgVariable);
		this.output.append(" == null && ");
		this.output.append(secondArgVariable);
		this.output.append(" == null) ? ");
		if (isAssumptionAnEqualityToOne) {
			this.output.append('0');
		} else {
			this.output.append('1');
		}
		this.output.append(" : ((");
		//second case: one arguments is null and the other is not
		this.output.append(firstArgVariable);
		this.output.append(" == null && ");
		this.output.append(secondArgVariable);
		this.output.append(" != null) || (");
		this.output.append(firstArgVariable);
		this.output.append(" != null && ");
		this.output.append(secondArgVariable);
		this.output.append(" == null)) ? ");
		if (isAssumptionAnEqualityToOne) {
			this.output.append('1');
		} else {
			this.output.append('0');
		}
		this.output.append(" : ");
		//third case: the direct comparison of the args succeeds
		this.output.append(firstArgVariable);
		this.output.append('.');
		this.output.append(methodStringComparisonName);
		this.output.append('(');
		this.output.append(secondArgVariable);
		this.output.append(") ? ");
		if (isAssumptionAnEqualityToOne) {
			this.output.append('0');
		} else {
			this.output.append('1');
		}
		this.output.append(" : ");
		//fourth and last case: direct comparison of the args fails
		if (isAssumptionAnEqualityToOne) {
			//calculate the string distance
			this.output.append(methodStringDistanceName);
			this.output.append('(');
			this.output.append(firstArgVariable);
			this.output.append(", ");
			this.output.append(secondArgVariable);
			this.output.append(')');
		} else {
			//in the case isEqualityToOne, since
			//direct comparison of the args has
			//failed, we are happy with distance 1
			this.output.append('1');
		}
		this.output.append(')');
	}

	private void appendJavaDistanceExpression(State initialState, Primitive assumption) 
	throws InvalidInputException {
		//first pass: eliminate negation
		final ArrayList<Primitive> assumptionWithNoNegation = new ArrayList<>(); //we use only element at position 0 as it were a reference to a String variable            
		final PrimitiveVisitor negationEliminator = new PrimitiveVisitor() {
			@Override
			public void visitAny(Any x) throws InvalidInputException {
				throw new InvalidInputException("StateFormatterSushiPathCondition.appendJavaDistanceExpression: cannot process a value of class Any.");
			}

			@Override
			public void visitExpression(Expression e) throws Exception {
				if (e.getOperator().equals(Operator.NOT)) {
					final Primitive operand = e.getOperand();
					if (operand instanceof Simplex) {
						//true or false
						assumptionWithNoNegation.add(StateFormatterSushiPathCondition.this.calc.push(operand).not().pop());
					} else if (operand instanceof Expression) {
						final Expression operandExp = (Expression) operand;
						final Operator operator = operandExp.getOperator();
						if (operator.equals(Operator.NOT)) {
							//double negation
							operandExp.getOperand().accept(this);
						} else if (operator.equals(Operator.AND) || operator.equals(Operator.OR)) {
							StateFormatterSushiPathCondition.this.calc.push(operandExp.getFirstOperand()).not().pop().accept(this);
							final Primitive first = assumptionWithNoNegation.remove(0);
							StateFormatterSushiPathCondition.this.calc.push(operandExp.getSecondOperand()).not().pop().accept(this);
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
			public void visitSimplex(Simplex x) {
				assumptionWithNoNegation.add(x);
			}

			@Override
			public void visitTerm(Term x) {
				assumptionWithNoNegation.add(x);
			}

			@Override
			public void visitNarrowingConversion(NarrowingConversion x) {
				assumptionWithNoNegation.add(x);
			}

			@Override
			public void visitWideningConversion(WideningConversion x) {
				assumptionWithNoNegation.add(x);
			}

			@Override
			public void visitPrimitiveSymbolicHashCode(PrimitiveSymbolicHashCode x) {
				assumptionWithNoNegation.add(x);
			}

			@Override
			public void visitPrimitiveSymbolicLocalVariable(PrimitiveSymbolicLocalVariable x) {
				assumptionWithNoNegation.add(x);
			}

			@Override
			public void visitPrimitiveSymbolicMemberArray(PrimitiveSymbolicMemberArray x) {
				assumptionWithNoNegation.add(x);
			}

			@Override
			public void visitPrimitiveSymbolicMemberArrayLength(PrimitiveSymbolicMemberArrayLength x) {
				assumptionWithNoNegation.add(x);
			}

			@Override
			public void visitPrimitiveSymbolicMemberField(PrimitiveSymbolicMemberField x) {
				assumptionWithNoNegation.add(x);
			}
		};
		try {
			assumption.accept(negationEliminator);
		} catch (InvalidInputException exc) {
			throw exc;
		} catch (Exception exc) {
			//this should never happen
			throw new AssertionError(exc);
		}

		//second pass: build the expression; note that we do not
		//add the variable names, it is assumed that all the variables
		//have been created before, an assumption that fits the use of
		//this method (it is used to produce the final distance 
		//calculation expression of a ValueCalculator, so in theory
		//all the relevant variables should have been created before,
		//or there is something that is not working)
		final PrimitiveVisitor translator = new PrimitiveVisitor() {
			final StringBuilder output = StateFormatterSushiPathCondition.this.output;

			@Override
			public void visitWideningConversion(WideningConversion x) 
			throws Exception {
				final char argType = x.getArg().getType();
				final char type = x.getType();
				if (argType == Type.BOOLEAN && type == Type.INT) {
					//operand stack widening of booleans
					this.output.append("((");
					x.getArg().accept(this); //appends the arg
					this.output.append(") == false ? 0 : 1)");
				}
			}

			@Override
			public void visitTerm(Term x) {
				this.output.append(x.toString());
			}

			@Override
			public void visitSimplex(Simplex x) {
				this.output.append(x.toString());
			}

			@Override
			public void visitNarrowingConversion(NarrowingConversion x)
			throws Exception {
				this.output.append('(');
				this.output.append(toPrimitiveOrVoidCanonicalName(x.getType()));
				this.output.append(") (");
				x.getArg().accept(this); //appends the arg
				this.output.append(')');
			}

			@Override
			public void visitPrimitiveSymbolicApply(PrimitiveSymbolicApply x) 
			throws InvalidInputException {
				this.output.append(getVariableFor(x));
			}

			@Override
			public void visitExpression(Expression e) 
			throws Exception {
				final Operator op = e.getOperator();
				if (e.isUnary()) {
					this.output.append(op == Operator.NEG ? "-" : op.toString());
					this.output.append('(');
					e.getOperand().accept(this);
					this.output.append(')');
				} else { 
					if (op.equals(Operator.EQ) ||
					op.equals(Operator.GT) ||
					op.equals(Operator.LT) ||
					op.equals(Operator.GE) ||
					op.equals(Operator.LE)) {
						this.output.append('(');
						e.getFirstOperand().accept(this);
						this.output.append(") ");
						this.output.append(op.toString());
						this.output.append(" (");
						e.getSecondOperand().accept(this);
						this.output.append(") ? 0 : isNaN((");
						e.getFirstOperand().accept(this);
						this.output.append(") - (");
						e.getSecondOperand().accept(this);
						this.output.append(")) ? BIG_DISTANCE : SMALL_DISTANCE + abs((");
						e.getFirstOperand().accept(this);
						this.output.append(") - (");
						e.getSecondOperand().accept(this);
						this.output.append("))");
					} else if (op.equals(Operator.NE)) {
						this.output.append('(');
						e.getFirstOperand().accept(this);
						this.output.append(") ");
						this.output.append(op.toString());
						this.output.append(" (");
						e.getSecondOperand().accept(this);
						this.output.append(") ? 0 : isNaN((");
						e.getFirstOperand().accept(this);
						this.output.append(") - (");
						e.getSecondOperand().accept(this);
						this.output.append(")) ? BIG_DISTANCE : SMALL_DISTANCE");
					} else {
						this.output.append('(');
						e.getFirstOperand().accept(this);
						this.output.append(") ");
						if (op.equals(Operator.AND)) {
							this.output.append('+');
						} else if (op.equals(Operator.OR)) {
							this.output.append('*');
						} else {
							this.output.append(op.toString());
						}
						this.output.append(" (");
						e.getSecondOperand().accept(this);
						this.output.append(')');
					}
				}
			}

			@Override
			public void visitAny(Any x) throws InvalidInputException {
				throw new InvalidInputException("StateFormatterSushiPathCondition.appendJavaDistanceExpression: cannot process a value of class Any.");
			}

			@Override
			public void visitPrimitiveSymbolicHashCode(PrimitiveSymbolicHashCode x) 
			throws InvalidInputException {
				this.output.append(getVariableFor(x));
			}

			@Override
			public void visitPrimitiveSymbolicLocalVariable(PrimitiveSymbolicLocalVariable x) 
			throws InvalidInputException {
				this.output.append(getVariableFor(x));
			}

			@Override
			public void visitPrimitiveSymbolicMemberArray(PrimitiveSymbolicMemberArray x)  
			throws InvalidInputException {
				this.output.append(getVariableFor(x));
			}

			@Override
			public void visitPrimitiveSymbolicMemberArrayLength(PrimitiveSymbolicMemberArrayLength x)  
			throws InvalidInputException {
				this.output.append(getVariableFor(x));
			}

			@Override
			public void visitPrimitiveSymbolicMemberField(PrimitiveSymbolicMemberField x)  
			throws InvalidInputException {
				this.output.append(getVariableFor(x));
			}
		};
		try {
			assumptionWithNoNegation.get(0).accept(translator); //from the first pass
		} catch (InvalidInputException exc) {
			throw exc;
		} catch (Exception exc) {
			//this should never happen
			throw new AssertionError(exc);
		}
	}
}
