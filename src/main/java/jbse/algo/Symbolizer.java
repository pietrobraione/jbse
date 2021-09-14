package jbse.algo;

import static jbse.algo.UtilControlFlow.failExecution;
import static jbse.bc.ClassLoaders.CLASSLOADER_BOOT;
import static jbse.bc.Signatures.JAVA_ARRAYLIST;
import static jbse.bc.Signatures.JAVA_ARRAYLIST_INIT;
import static jbse.bc.Signatures.JAVA_CLASS;
import static jbse.bc.Signatures.JAVA_CLASSLOADER;
import static jbse.bc.Signatures.JAVA_CONCURRENTHASHMAP;
import static jbse.bc.Signatures.JAVA_HASHMAP;
import static jbse.bc.Signatures.JAVA_LINKEDHASHMAP;
import static jbse.bc.Signatures.JAVA_THREAD;
import static jbse.bc.Signatures.JBSE_JAVA_CONCURRENTMAP_ABSENTKEYS;
import static jbse.bc.Signatures.JBSE_JAVA_CONCURRENTMAP_ABSENTVALUES;
import static jbse.bc.Signatures.JBSE_JAVA_CONCURRENTMAP_INITIALMAP;
import static jbse.bc.Signatures.JBSE_JAVA_CONCURRENTMAP_ISINITIAL;
import static jbse.bc.Signatures.JBSE_JAVA_CONCURRENTMAP_NNODEEMPTY;
import static jbse.bc.Signatures.JBSE_JAVA_CONCURRENTMAP_NNODEEMPTY_INIT;
import static jbse.bc.Signatures.JBSE_JAVA_CONCURRENTMAP_NNODEPAIR_KEY;
import static jbse.bc.Signatures.JBSE_JAVA_CONCURRENTMAP_NNODEPAIR_NEXT;
import static jbse.bc.Signatures.JBSE_JAVA_CONCURRENTMAP_NNODEPAIR_VALUE;
import static jbse.bc.Signatures.JBSE_JAVA_CONCURRENTMAP_NUMNODES;
import static jbse.bc.Signatures.JBSE_JAVA_CONCURRENTMAP_ROOT;
import static jbse.bc.Signatures.JBSE_JAVA_CONCURRENTMAP_SIZE;
import static jbse.bc.Signatures.JBSE_JAVA_LINKEDMAP_ABSENTKEYS;
import static jbse.bc.Signatures.JBSE_JAVA_LINKEDMAP_ABSENTVALUES;
import static jbse.bc.Signatures.JBSE_JAVA_LINKEDMAP_INITIALMAP;
import static jbse.bc.Signatures.JBSE_JAVA_LINKEDMAP_ISINITIAL;
import static jbse.bc.Signatures.JBSE_JAVA_LINKEDMAP_NNODEEMPTY;
import static jbse.bc.Signatures.JBSE_JAVA_LINKEDMAP_NNODEEMPTY_INIT;
import static jbse.bc.Signatures.JBSE_JAVA_LINKEDMAP_NNODEPAIR_KEY;
import static jbse.bc.Signatures.JBSE_JAVA_LINKEDMAP_NNODEPAIR_NEXT;
import static jbse.bc.Signatures.JBSE_JAVA_LINKEDMAP_NNODEPAIR_VALUE;
import static jbse.bc.Signatures.JBSE_JAVA_LINKEDMAP_NUMNODES;
import static jbse.bc.Signatures.JBSE_JAVA_LINKEDMAP_ROOT;
import static jbse.bc.Signatures.JBSE_JAVA_LINKEDMAP_SIZE;
import static jbse.bc.Signatures.JBSE_JAVA_MAP_ABSENTKEYS;
import static jbse.bc.Signatures.JBSE_JAVA_MAP_ABSENTVALUES;
import static jbse.bc.Signatures.JBSE_JAVA_MAP_INITIALMAP;
import static jbse.bc.Signatures.JBSE_JAVA_MAP_ISINITIAL;
import static jbse.bc.Signatures.JBSE_JAVA_MAP_NNODEEMPTY;
import static jbse.bc.Signatures.JBSE_JAVA_MAP_NNODEEMPTY_INIT;
import static jbse.bc.Signatures.JBSE_JAVA_MAP_NNODEPAIR_KEY;
import static jbse.bc.Signatures.JBSE_JAVA_MAP_NNODEPAIR_NEXT;
import static jbse.bc.Signatures.JBSE_JAVA_MAP_NNODEPAIR_VALUE;
import static jbse.bc.Signatures.JBSE_JAVA_MAP_NUMNODES;
import static jbse.bc.Signatures.JBSE_JAVA_MAP_ROOT;
import static jbse.bc.Signatures.JBSE_JAVA_MAP_SIZE;
import static jbse.common.Type.isPrimitive;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import jbse.bc.ClassFile;
import jbse.bc.ClassHierarchy;
import jbse.bc.Signature;
import jbse.bc.exc.FieldNotFoundException;
import jbse.bc.exc.MethodCodeNotFoundException;
import jbse.bc.exc.MethodNotFoundException;
import jbse.bc.exc.NullMethodReceiverException;
import jbse.common.exc.InvalidInputException;
import jbse.common.exc.UnexpectedInternalException;
import jbse.mem.Array;
import jbse.mem.Instance;
import jbse.mem.Klass;
import jbse.mem.Objekt;
import jbse.mem.Slot;
import jbse.mem.State;
import jbse.mem.Variable;
import jbse.mem.Array.AccessOutcomeIn;
import jbse.mem.Array.AccessOutcomeInValue;
import jbse.mem.exc.CannotAssumeSymbolicObjectException;
import jbse.mem.exc.ContradictionException;
import jbse.mem.exc.HeapMemoryExhaustedException;
import jbse.mem.exc.InvalidProgramCounterException;
import jbse.mem.exc.InvalidSlotException;
import jbse.mem.exc.ThreadStackEmptyException;
import jbse.val.Any;
import jbse.val.Calculator;
import jbse.val.Expression;
import jbse.val.KlassPseudoReference;
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
import jbse.val.ReferenceSymbolicMemberMapKey;
import jbse.val.ReferenceSymbolicMemberMapValue;
import jbse.val.Simplex;
import jbse.val.Term;
import jbse.val.Value;
import jbse.val.WideningConversion;
import jbse.val.exc.InvalidTypeException;

public final class Symbolizer {
	private final Calculator calc;
	
	/**
	 * Constructor.
	 * 
	 * @param calc a {@link Calculator}. Must not be {@code null}.
	 * @throws InvalidInputException if {@code calc == null}. 
	 */
	public Symbolizer(Calculator calc) throws InvalidInputException {
		if (calc == null) {
			throw new InvalidInputException("Invoked Symbolizer(Calculator) with the parameter set to null.");
		}
		this.calc = calc;
	}

	/**
	 * Makes symbolic a {@link Klass} in a {@link State}, recursively 
	 * making symbolic all the objects its fields points to and augmenting the
	 * {@link State}'s path condition correspondingly.
	 * 
	 * @param state a {@link State}. It must not be {@code null}.
	 * @param classFile a {@link ClassFile}. It must not be {@code null}.
	 * @throws InvalidInputException if {@code state == null || classFile == null} or 
	 *         {@code state} is frozen or {@code state} does not
	 *         contain a {@link Klass} for {@code classFile}.
	 * @throws ContradictionException if any of the assumptions pushed on {@code state}'s
	 *         path condition generate a contradiction.
	 * @throws HeapMemoryExhaustedException if the heap is full.
	 * @throws ThreadStackEmptyException if the state's stack is empty.
	 */
	public void makeKlassSymbolic(State state, ClassFile classFile) throws InvalidInputException, ContradictionException, HeapMemoryExhaustedException, ThreadStackEmptyException {
		if (state == null || classFile == null) {
			throw new InvalidInputException("Invoked Symbolizer.makeKlassSymbolic(State, ClassFile) with one of the parameters set to null.");
		}
		
		//gets the Klass
		final Klass klass = state.getKlass(classFile);
		if (klass == null) {
			throw new InvalidInputException("Invoked Symbolizer.makeKlassSymbolic(State, ClassFile) with a State parameter for which no Klass exists corresponding to the ClassFile parameter.");
		}

		//creates the origin and the identity hash code
		final KlassPseudoReference origin = state.createSymbolKlassPseudoReference(state.getHistoryPoint(), classFile);

		//completes the transformation of klass recursively, 
		//by transforming all the references and the concrete 
		//objects they refer. Also, pushes all the assumptions
		//on the created symbolic references to state's path condition.
		makeObjektSymbolic(state, klass, origin);
	}

	//the next fields are used in the context of an invocation of makeObjektSymbolic
	private ClassFile cf_JAVA_ARRAYLIST;
	private ClassFile cf_JAVA_CLASS;
	private ClassFile cf_JAVA_CLASSLOADER;
	private ClassFile cf_JAVA_THREAD;
	private ClassFile cf_JAVA_CONCURRENTHASHMAP;
	private ClassFile cf_JAVA_HASHMAP;
	private ClassFile cf_JAVA_LINKEDHASHMAP;
	private ClassFile cf_JBSE_JAVA_CONCURRENTMAP_NNODEEMPTY;
	private ClassFile cf_JBSE_JAVA_MAP_NNODEEMPTY;
	private ClassFile cf_JBSE_JAVA_LINKEDMAP_NNODEEMPTY;
	private ArrayList<ReferenceSymbolic> assumeExpands;
	private ArrayList<Long> assumeExpandsTargets;
	private ArrayList<ReferenceSymbolic> assumeAliases;
	private ArrayList<ReferenceSymbolic> assumeAliasesTargets;
	private ArrayList<ReferenceSymbolic> assumeNull;

	/**
	 * Makes symbolic an {@link Objekt} in a {@link State}, recursively 
	 * making symbolic all the objects its fields points to and augmenting the
	 * {@link State}'s path condition correspondingly.
	 * 
	 * @param state a {@link State}. It must not be {@code null}.
	 * @param object the {@link Objekt} that must 
	 *        be transformed. It must not be {@code null}.
	 * @param origin a {@link ReferenceSymbolic}, the origin
	 *        of {@code object}. It must not be {@code null}.
	 * @throws InvalidInputException if {@code state == null || object == null || origin == null}, 
	 *         or if {@code state} has not loaded one of the classes {@code java.lang.Class}, 
	 *         {@code java.lang.ClassLoader} or {@code java.lang.Thread}, or {@code object} has a
	 *         field with a concrete reference to an object that cannot
	 *         be made symbolic (should never happen?).
	 * @throws ContradictionException if any of the assumptions pushed on {@code state}'s
	 *         path condition generate a contradiction.
	 * @throws HeapMemoryExhaustedException if the heap is full.
	 * @throws ThreadStackEmptyException if the state's stack is empty.
	 */
	private void makeObjektSymbolic(State state, Objekt object, ReferenceSymbolic origin) 
	throws InvalidInputException, ContradictionException, HeapMemoryExhaustedException, ThreadStackEmptyException {
		if (state == null || object == null || origin == null) {
			throw new InvalidInputException("Invoked Symbolizer.makeObjektSymbolic(State, Objekt, ReferenceSymbolic) with one of the parameters set to null.");
		}
		
		final ClassHierarchy hier = state.getClassHierarchy();
		this.cf_JAVA_ARRAYLIST = hier.getClassFileClassArray(CLASSLOADER_BOOT, JAVA_ARRAYLIST);
		this.cf_JAVA_CLASS = hier.getClassFileClassArray(CLASSLOADER_BOOT, JAVA_CLASS);
		this.cf_JAVA_CLASSLOADER = hier.getClassFileClassArray(CLASSLOADER_BOOT, JAVA_CLASSLOADER);
		this.cf_JAVA_THREAD = hier.getClassFileClassArray(CLASSLOADER_BOOT, JAVA_THREAD);
		if (this.cf_JAVA_ARRAYLIST == null || this.cf_JAVA_CLASS == null || this.cf_JAVA_CLASSLOADER == null || this.cf_JAVA_THREAD == null) {
			throw new InvalidInputException("Invoked Symbolizer.makeObjektSymbolic(State, Objekt, ReferenceSymbolic) with a State parameter where java.util.ArrayList or java.lang.Class or java.lang.ClassLoader or java.lang.Thread was not loaded.");
		}
		//in the next cases, null is allowed
		this.cf_JAVA_CONCURRENTHASHMAP = hier.getClassFileClassArray(CLASSLOADER_BOOT, JAVA_CONCURRENTHASHMAP);
		this.cf_JAVA_HASHMAP = hier.getClassFileClassArray(CLASSLOADER_BOOT, JAVA_HASHMAP);
		this.cf_JAVA_LINKEDHASHMAP = hier.getClassFileClassArray(CLASSLOADER_BOOT, JAVA_LINKEDHASHMAP);
		this.cf_JBSE_JAVA_CONCURRENTMAP_NNODEEMPTY = hier.getClassFileClassArray(CLASSLOADER_BOOT, JBSE_JAVA_CONCURRENTMAP_NNODEEMPTY);
		this.cf_JBSE_JAVA_MAP_NNODEEMPTY = hier.getClassFileClassArray(CLASSLOADER_BOOT, JBSE_JAVA_MAP_NNODEEMPTY);
		this.cf_JBSE_JAVA_LINKEDMAP_NNODEEMPTY = hier.getClassFileClassArray(CLASSLOADER_BOOT, JBSE_JAVA_LINKEDMAP_NNODEEMPTY);
		
		//initializes the lists of assumptions
		this.assumeExpands = new ArrayList<>();
		this.assumeExpandsTargets = new ArrayList<>();
		this.assumeAliases = new ArrayList<>();
		this.assumeAliasesTargets = new ArrayList<>();
		this.assumeNull = new ArrayList<>();

		//makes object symbolic and sets its identity hash code
		object.makeSymbolic(origin);
		object.setIdentityHashCode(state.createSymbolIdentityHashCode(object));
		
		//starts the recursive visit
		completeSymbolic(state, object, new HashSet<>());

		//pushes all the assumptions, first all expands assumptions, 
		//so we have a complete backbone, then aliases and nulls
		for (int i = 0; i < this.assumeExpands.size(); ++i) {
			state.assumeExpandsAlreadyPresent(this.assumeExpands.get(i), this.assumeExpandsTargets.get(i));
		}
		for (int i = 0; i < this.assumeAliases.size(); ++i) {
			state.assumeAliases(this.assumeAliases.get(i), this.assumeAliasesTargets.get(i));
		}
		for (int i = 0; i < this.assumeNull.size(); ++i) {
			state.assumeNull(this.assumeNull.get(i));
		}
	}
	
	/**
	 * Completes the transformation of an {@link Objekt} 
	 * from concrete to symbolic by transforming
	 * all the concrete references in it into symbolic 
	 * references pointing to the same objects (which are 
	 * recursively made symbolic). Finally, pushes the 
	 * necessary assumptions about the created symbolic 
	 * references on the state's path condition.
	 * 
	 * @param state a {@link State}.
	 * @param object the {@link Objekt} that must 
	 *        be transformed. 
	 * @param origin a {@link ReferenceSymbolic}, the origin
	 *        of {@code object}.
	 * @throws InvalidInputException if {@code state} has not loaded one 
	 *         of the classes {@code java.lang.Class}, {@code java.lang.ClassLoader}
	 *         or {@code java.lang.Thread}, or {@code object} has a
	 *         field with a concrete reference to an object that cannot
	 *         be made symbolic (should never happen?).
	 * @throws HeapMemoryExhaustedException if the heap is full.
	 * @throws ThreadStackEmptyException if the state's stack is empty.
	 */
	private void completeSymbolic(State state, Objekt object, HashSet<Objekt> visited) 
	throws InvalidInputException, HeapMemoryExhaustedException, ThreadStackEmptyException {
		visited.add(object);
		try {
			if (object instanceof Instance &&
			object.getType().equals(this.cf_JAVA_CONCURRENTHASHMAP) &&
			this.cf_JAVA_CONCURRENTHASHMAP.hasFieldDeclaration(JBSE_JAVA_CONCURRENTMAP_INITIALMAP)) {
				//this object is a model java.util.concurrent.ConcurrentHashMap
				completeSymbolicMapModel(state, object, visited, 
			                             this.cf_JAVA_CONCURRENTHASHMAP, this.cf_JBSE_JAVA_CONCURRENTMAP_NNODEEMPTY, 
			                             JBSE_JAVA_CONCURRENTMAP_INITIALMAP, JBSE_JAVA_CONCURRENTMAP_ABSENTKEYS,
			                             JBSE_JAVA_CONCURRENTMAP_ABSENTVALUES, JBSE_JAVA_CONCURRENTMAP_ISINITIAL,
			                             JBSE_JAVA_CONCURRENTMAP_SIZE, JBSE_JAVA_CONCURRENTMAP_ROOT, 
			                             JBSE_JAVA_CONCURRENTMAP_NUMNODES, JBSE_JAVA_CONCURRENTMAP_NNODEEMPTY_INIT,
			                             JBSE_JAVA_CONCURRENTMAP_NNODEPAIR_KEY, JBSE_JAVA_CONCURRENTMAP_NNODEPAIR_VALUE,
			                             JBSE_JAVA_CONCURRENTMAP_NNODEPAIR_NEXT);
			} else if (object instanceof Instance &&
			object.getType().equals(this.cf_JAVA_HASHMAP) &&
			this.cf_JAVA_HASHMAP.hasFieldDeclaration(JBSE_JAVA_MAP_INITIALMAP)) {
				//this object is a model java.util.HashMap
				completeSymbolicMapModel(state, object, visited, 
				                         this.cf_JAVA_HASHMAP, this.cf_JBSE_JAVA_MAP_NNODEEMPTY, 
				                         JBSE_JAVA_MAP_INITIALMAP, JBSE_JAVA_MAP_ABSENTKEYS,
				                         JBSE_JAVA_MAP_ABSENTVALUES, JBSE_JAVA_MAP_ISINITIAL,
				                         JBSE_JAVA_MAP_SIZE, JBSE_JAVA_MAP_ROOT, 
				                         JBSE_JAVA_MAP_NUMNODES, JBSE_JAVA_MAP_NNODEEMPTY_INIT,
				                         JBSE_JAVA_MAP_NNODEPAIR_KEY, JBSE_JAVA_MAP_NNODEPAIR_VALUE,
				                         JBSE_JAVA_MAP_NNODEPAIR_NEXT);
			} else if (object instanceof Instance &&
			object.getType().equals(this.cf_JAVA_LINKEDHASHMAP) &&
			this.cf_JAVA_LINKEDHASHMAP.hasFieldDeclaration(JBSE_JAVA_LINKEDMAP_INITIALMAP)) {
				//this object is a model java.util.LinkedHashMap
				completeSymbolicMapModel(state, object, visited, 
				                         this.cf_JAVA_LINKEDHASHMAP, this.cf_JBSE_JAVA_LINKEDMAP_NNODEEMPTY, 
				                         JBSE_JAVA_LINKEDMAP_INITIALMAP, JBSE_JAVA_LINKEDMAP_ABSENTKEYS,
				                         JBSE_JAVA_LINKEDMAP_ABSENTVALUES, JBSE_JAVA_LINKEDMAP_ISINITIAL,
				                         JBSE_JAVA_LINKEDMAP_SIZE, JBSE_JAVA_LINKEDMAP_ROOT, 
				                         JBSE_JAVA_LINKEDMAP_NUMNODES, JBSE_JAVA_LINKEDMAP_NNODEEMPTY_INIT,
				                         JBSE_JAVA_LINKEDMAP_NNODEPAIR_KEY, JBSE_JAVA_LINKEDMAP_NNODEPAIR_VALUE,
				                         JBSE_JAVA_LINKEDMAP_NNODEPAIR_NEXT);
			} else if (object instanceof Instance || object instanceof Klass) {
				for (Map.Entry<Signature, Variable> entry : object.fields().entrySet()) {
					final Signature sig = entry.getKey();
					final Variable var = entry.getValue();
					
					//gets the old value
					final Value oldValue = var.getValue();
					
					//determines some features of the field
					final String fieldType = var.getType();
					final String fieldGenericSignatureType = var.getGenericSignatureType();
					final String fieldName = var.getName();
					final String fieldClass = sig.getClassName();
					
					if (!isPrimitive(fieldType)) {
						//makes the new value
						final ReferenceSymbolic newValue = (ReferenceSymbolic) state.createSymbolMemberField(fieldType, fieldGenericSignatureType, object.getOrigin(), fieldName, fieldClass);
	
						//sets the new value
						setNewValue(state, var, oldValue, newValue, visited);
					}
				}
			} else if (object instanceof Array) {
				final Array array = (Array) object;
				
				//adds a fresh object for the initial array
				final ClassFile arrayClass = array.getType();
				final long initialArrayHeapPosition = state.createArrayInitial(this.calc, arrayClass, array.getOrigin(), array.getLength());
				
				//sets the initial array
				final ReferenceConcrete initialArrayReference = new ReferenceConcrete(initialArrayHeapPosition);
				final Array initialArray = (Array) state.getObject(initialArrayReference);
				initialArray.cloneEntries(array, this.calc);
				
				//sets the array
				array.setEntriesBackingArray(this.calc, initialArrayReference);

				//if the array member type is a reference type, scans all the 
				//entries in the initial array and makes their values symbolic
				final ClassFile arrayMemberClass = array.getType().getMemberClass();
				if (!arrayMemberClass.isPrimitiveOrVoid()) {
					final String arrayMemberType = arrayMemberClass.getInternalTypeName();
					final String arrayMemberGenericSignatureType = arrayMemberClass.getGenericSignatureType();
					for (Iterator<? extends AccessOutcomeIn> it = initialArray.entries().iterator(); it.hasNext(); ) {
						final AccessOutcomeIn entry = it.next();
						if (entry instanceof AccessOutcomeInValue) {
							final AccessOutcomeInValue entryInValue = (AccessOutcomeInValue) entry;
							
							//gets the old value
							final Value oldValue = entryInValue.getValue();

							//determines the index
							final Primitive indexActual;
							{
								final IndexVisitor v = new IndexVisitor(array.getIndex());
								final Expression accessCondition = entryInValue.getAccessCondition();
								try {
									accessCondition.accept(v);
								} catch (UnexpectedInternalException e) {
									throw e;
								} catch (Exception e) {
									//this should never happen
									failExecution(e);
								}
								indexActual = v.indexActual;
							}

							//makes the new value
							final ReferenceSymbolic newValue = (ReferenceSymbolic) state.createSymbolMemberArray(arrayMemberType, arrayMemberGenericSignatureType, array.getOrigin(), indexActual);
							
							//sets the new value
							setNewValue(state, entryInValue, oldValue, newValue, visited);							
						}
					}
				}
			}
		} catch (InvalidTypeException | FieldNotFoundException | CannotAssumeSymbolicObjectException | NullMethodReceiverException | 
		MethodNotFoundException | MethodCodeNotFoundException | InvalidProgramCounterException | InvalidSlotException e) {
			//this should never happen
			throw new UnexpectedInternalException(e);
		}
	}
	
	private void completeSymbolicMapModel(State state, Objekt object, HashSet<Objekt> visited, 
	                                      ClassFile cfMapModel, ClassFile cfMapModel_NNODEEMPTY, 
	                                      Signature fldMapModel_INITIALMAP, Signature fldMapModel_ABSENTKEYS,
	                                      Signature fldMapModel_ABSENTVALUES, Signature fldMapModel_ISINITIAL,
	                                      Signature fldMapModel_SIZE, Signature fldMapModel_ROOT, 
	                                      Signature fldMapModel_NUMNODES, Signature mthMapModel_NNODEEMPTY_INIT,
	                                      Signature fldMapModel_NNODEPAIR_KEY, Signature fldMapModel_NNODEPAIR_VALUE,
	                                      Signature fldMapModel_NNODEPAIR_NEXT) 
	                                      throws InvalidTypeException, InvalidInputException, FieldNotFoundException, 
	                                      CannotAssumeSymbolicObjectException, HeapMemoryExhaustedException, NullMethodReceiverException, 
	                                      MethodNotFoundException, MethodCodeNotFoundException, InvalidSlotException, 
	                                      InvalidProgramCounterException, ThreadStackEmptyException {
		//makes the initial map's origin
		final ReferenceSymbolic initialMapOrigin = (ReferenceSymbolic) state.createSymbolMemberField(fldMapModel_INITIALMAP.getDescriptor(), cfMapModel.getFieldGenericSignatureType(fldMapModel_INITIALMAP), object.getOrigin(), fldMapModel_INITIALMAP.getName(), fldMapModel_INITIALMAP.getClassName());

		//adds a fresh object for the initial map
		final long initialMapHeapPosition = state.createObjectSymbolic(this.calc, cfMapModel, initialMapOrigin);

		//assumes that the initial map is resolved by expansion
		this.assumeExpands.add(initialMapOrigin);
		this.assumeExpandsTargets.add(initialMapHeapPosition);

		//makes the initial map initial
		final Instance initialMap = (Instance) state.getObject(initialMapOrigin);
		initialMap.makeInitial();

		//sets some fields of the initial map
		initialMap.setFieldValue(fldMapModel_ISINITIAL, this.calc.valBoolean(true));
		final ReferenceConcrete refAbsentKeys = state.createInstance(this.calc, this.cf_JAVA_ARRAYLIST);
		initialMap.setFieldValue(fldMapModel_ABSENTKEYS, refAbsentKeys);
		final ReferenceConcrete refAbsentValues = state.createInstance(this.calc, this.cf_JAVA_ARRAYLIST);
		initialMap.setFieldValue(fldMapModel_ABSENTVALUES, refAbsentValues);
		final Value initialMapSize = object.getFieldValue(fldMapModel_SIZE);
		initialMap.setFieldValue(fldMapModel_SIZE, initialMapSize);
		final Value initialMapRoot = object.getFieldValue(fldMapModel_ROOT);
		initialMap.setFieldValue(fldMapModel_ROOT, initialMapRoot);
		final Value initialMapNumNodes = object.getFieldValue(fldMapModel_NUMNODES);
		initialMap.setFieldValue(fldMapModel_NUMNODES, initialMapNumNodes);

		//set the fields of the object
		object.setFieldValue(fldMapModel_ISINITIAL, this.calc.valBoolean(false));
		final ReferenceConcrete refNNodeEmpty = state.createInstance(this.calc, cfMapModel_NNODEEMPTY);
		object.setFieldValue(fldMapModel_ROOT, refNNodeEmpty);
		object.setFieldValue(fldMapModel_NUMNODES, this.calc.valInt(0));

		//creates the frames for the created objects' constructors
		state.pushFrame(this.calc, this.cf_JAVA_ARRAYLIST, JAVA_ARRAYLIST_INIT, false, 0, refAbsentKeys);
		state.pushFrame(this.calc, this.cf_JAVA_ARRAYLIST, JAVA_ARRAYLIST_INIT, false, 0, refAbsentValues);
		state.pushFrame(this.calc, cfMapModel_NNODEEMPTY, mthMapModel_NNODEEMPTY_INIT, false, 0, refNNodeEmpty);

		//scans all the entries in the initial map and makes their key/values symbolic
		Reference r = (Reference) initialMapRoot;
		Instance i = (Instance) state.getObject(r);
		while (!i.getType().equals(cfMapModel_NNODEEMPTY)) {
			//gets the old key/value pair
			final Reference oldKey = (Reference) i.getFieldValue(fldMapModel_NNODEPAIR_KEY);
			final Reference oldValue = (Reference) i.getFieldValue(fldMapModel_NNODEPAIR_VALUE);

			//makes the new key/value pair
			final ReferenceSymbolicMemberMapKey newKey = state.createSymbolMemberMapKeyHistoryPointContainer(initialMapOrigin, null);
			final ReferenceSymbolicMemberMapValue newValue = state.createSymbolMemberMapValueKeyHistoryPointContainer(initialMapOrigin, newKey);
			newKey.setAssociatedValue(newValue);

			//sets the new key/value pair
			setNewValue(state, i.fields().get(fldMapModel_NNODEPAIR_KEY), oldKey, newKey, visited);
			setNewValue(state, i.fields().get(fldMapModel_NNODEPAIR_VALUE), oldValue, newValue, visited);

			//moves to the next entry
			r = (Reference) i.getFieldValue(fldMapModel_NNODEPAIR_NEXT);
			i = (Instance) state.getObject(r);
		}
	}
	
	void setNewValue(State state, Slot slot, Value oldValue, ReferenceSymbolic newValue, HashSet<Objekt> visited) 
	throws InvalidInputException, InvalidTypeException, HeapMemoryExhaustedException, ThreadStackEmptyException {
		if (oldValue instanceof ReferenceConcrete) {
			final ReferenceConcrete valueRef = (ReferenceConcrete) oldValue;
			if (state.isNull(valueRef)) {
				//sets the slot
				slot.setValue(newValue);

				//records the resolution
				this.assumeNull.add(newValue);
			} else {
				//it is an expansion/alias, unless it is an instance of a 
				//class of which cannot be created symbolic objects
				//(java.lang.Class, java.lang.ClassLoader, java.lang.Thread):
				//In such case we leave the field's value concrete, as it is
				//the best approximation we can do.
				final Objekt o = state.getObject(valueRef); 
				final ClassFile oClass = o.getType();
				if (o instanceof Klass || 
				(!oClass.equals(this.cf_JAVA_CLASS) && !oClass.isSubclass(this.cf_JAVA_CLASSLOADER) && !oClass.isSubclass(this.cf_JAVA_THREAD))) {
					//sets the slot
					slot.setValue(newValue);

					if (o.isSymbolic()) {
						//records the resolution by alias
						this.assumeAliases.add(newValue);
						this.assumeAliasesTargets.add(o.getOrigin());
					} else {
						//records the resolution by expansion
						this.assumeExpands.add(newValue);
						this.assumeExpandsTargets.add(valueRef.getHeapPosition());
					}

					//determines if the referred object must also
					//become symbolic
					if (!visited.contains(o) && !o.isSymbolic()) {
						//makes o symbolic
						o.makeSymbolic(newValue);
						o.setIdentityHashCode(state.createSymbolIdentityHashCode(o));
						//Here we suppose that it is enough even if o is an array;
						//This means that o will not be backed by an initial array.
						//It should however not be a problem, since o, being the
						//transformation of a concrete object into a symbolic one,
						//does not need to be further refined.

						//completes transformation of o recursively
						completeSymbolic(state, o, visited);
					}
				}
			}
		} else if (oldValue instanceof ReferenceSymbolic) {
			//sets the slot
			slot.setValue(newValue);

			//calculates the assumption
			final ReferenceSymbolic ref = (ReferenceSymbolic) oldValue;
			if (state.isNull(ref)) {
				//it is resolved by null
				this.assumeNull.add(newValue);
			} else {
				//it is resolved by alias
				//we assert that ref must be resolved
				//(I really can't figure out a situation where it
				//can happen that a symbolic reference stored in
				//a static field by a <clinit> method is not 
				//initialized)
				final Objekt o = state.getObject(ref);
				if (o == null) {
					failExecution("Unexpected unresolved symbolic reference contained in a statically initialized object (while trying to transform it into a symbolic object).");
				}
				
				//we record the resolution by alias
				this.assumeAliases.add(newValue);
				this.assumeAliasesTargets.add(o.getOrigin());

				//the object is symbolic, thus it must not be made symbolic
				//(it already is) nor recursively explored
			}
		} //else, do nothing
	}

	class IndexVisitor implements PrimitiveVisitor {
		private final Term indexFormal;
		private Primitive indexActual = null;

		public IndexVisitor(Term indexFormal) {
			this.indexFormal = indexFormal;
		}

		@Override
		public void visitAny(Any x) {
			throw new UnexpectedInternalException("Found Any value as a clause of an array entry access condition.");
		}

		@Override
		public void visitExpression(Expression e) throws Exception {
			if (e.getOperator() == Operator.AND) {
				e.getFirstOperand().accept(this);
				if (this.indexActual == null) {
					e.getSecondOperand().accept(this);
				}
			} else if (e.getOperator() == Operator.EQ && 
			e.getFirstOperand().equals(this.indexFormal)) {
				this.indexActual = e.getSecondOperand();
			} //else, do nothing
		}

		@Override
		public void visitPrimitiveSymbolicApply(PrimitiveSymbolicApply x) {
			throw new UnexpectedInternalException("Found PrimitiveSymbolicApply value as a clause of an array entry access condition.");
		}

		@Override
		public void visitSimplex(Simplex x) {
			//do nothing (this handles the unlikely case where true is a clause of the array access expression)
		}

		@Override
		public void visitTerm(Term x) {
			throw new UnexpectedInternalException("Found Term value as a clause of an array entry access condition.");
		}

		@Override
		public void visitNarrowingConversion(NarrowingConversion x) {
			throw new UnexpectedInternalException("Found NarrowingConversion value as a clause of an array entry access condition.");
		}

		@Override
		public void visitWideningConversion(WideningConversion x) {
			throw new UnexpectedInternalException("Found WideningConversion value as a clause of an array entry access condition.");
		}

		@Override
		public void visitPrimitiveSymbolicHashCode(PrimitiveSymbolicHashCode x) throws Exception {
			throw new UnexpectedInternalException("Found PrimitiveSymbolicHashCode value as a clause of an array entry access condition.");
		}

		@Override
		public void visitPrimitiveSymbolicLocalVariable(PrimitiveSymbolicLocalVariable x) throws Exception {
			throw new UnexpectedInternalException("Found PrimitiveSymbolicLocalVariable value as a clause of an array entry access condition.");
		}

		@Override
		public void visitPrimitiveSymbolicMemberArray(PrimitiveSymbolicMemberArray x) throws Exception {
			throw new UnexpectedInternalException("Found PrimitiveSymbolicMemberArray value as a clause of an array entry access condition.");
		}

		@Override
		public void visitPrimitiveSymbolicMemberArrayLength(PrimitiveSymbolicMemberArrayLength x) throws Exception {
			throw new UnexpectedInternalException("Found PrimitiveSymbolicMemberArrayLength value as a clause of an array entry access condition.");
		}

		@Override
		public void visitPrimitiveSymbolicMemberField(PrimitiveSymbolicMemberField x) throws Exception {
			throw new UnexpectedInternalException("Found PrimitiveSymbolicMemberField value as a clause of an array entry access condition.");
		}	
	}
}
