package jbse.algo.meta;

import static jbse.algo.Util.exitFromAlgorithm;
import static jbse.algo.Util.failExecution;
import static jbse.algo.Util.getMemberNameFlagsMethod;
import static jbse.algo.Util.IS_CONSTRUCTOR;
import static jbse.algo.Util.IS_FIELD;
import static jbse.algo.Util.IS_METHOD;
import static jbse.algo.Util.IS_TYPE;
import static jbse.algo.Util.JVM_RECOGNIZED_FIELD_MODIFIERS;
import static jbse.algo.Util.REFERENCE_KIND_SHIFT;
import static jbse.algo.Util.REF_getField;
import static jbse.algo.Util.REF_getStatic;
import static jbse.algo.Util.SEARCH_SUPERCLASSES;
import static jbse.algo.Util.SEARCH_INTERFACES;
import static jbse.algo.Util.throwNew;
import static jbse.algo.Util.valueString;
import static jbse.algo.meta.Util.FAIL_JBSE;
import static jbse.algo.meta.Util.INTERRUPT_SYMBOLIC_VALUE_NOT_ALLOWED_EXCEPTION;
import static jbse.algo.meta.Util.getArray;
import static jbse.algo.meta.Util.getInstance;
import static jbse.algo.meta.Util.getInteger;
import static jbse.bc.ClassLoaders.CLASSLOADER_BOOT;
import static jbse.bc.Signatures.JAVA_CLASS;
import static jbse.bc.Signatures.JAVA_MEMBERNAME;
import static jbse.bc.Signatures.JAVA_MEMBERNAME_CLAZZ;
import static jbse.bc.Signatures.JAVA_MEMBERNAME_FLAGS;
import static jbse.bc.Signatures.JAVA_MEMBERNAME_NAME;
import static jbse.bc.Signatures.JAVA_MEMBERNAME_TYPE;
import static jbse.bc.Signatures.JAVA_OBJECT;
import static jbse.bc.Signatures.JAVA_STRING;
import static jbse.bc.Signatures.OUT_OF_MEMORY_ERROR;
import static jbse.common.Type.isPrimitive;
import static jbse.common.Type.REFERENCE;
import static jbse.common.Type.toPrimitiveOrVoidCanonicalName;
import static jbse.common.Type.TYPEEND;

import java.util.ArrayList;
import java.util.function.Supplier;

import jbse.algo.Algo_INVOKEMETA_Nonbranching;
import jbse.algo.InterruptException;
import jbse.algo.StrategyUpdate;
import jbse.algo.exc.SymbolicValueNotAllowedException;
import jbse.bc.ClassFile;
import jbse.bc.ClassHierarchy;
import jbse.bc.Signature;
import jbse.bc.exc.FieldNotFoundException;
import jbse.bc.exc.MethodNotFoundException;
import jbse.common.exc.ClasspathException;
import jbse.common.exc.InvalidInputException;
import jbse.mem.Array;
import jbse.mem.Array.AccessOutcomeInValue;
import jbse.mem.Instance;
import jbse.mem.Instance_JAVA_CLASS;
import jbse.mem.State;
import jbse.mem.exc.FastArrayAccessNotAllowedException;
import jbse.mem.exc.FrozenStateException;
import jbse.mem.exc.HeapMemoryExhaustedException;
import jbse.tree.DecisionAlternative_NONE;
import jbse.val.Calculator;
import jbse.val.Null;
import jbse.val.ReferenceConcrete;
import jbse.val.Simplex;

/**
 * Meta-level implementation of {@link java.lang.invoke.MethodHandleNatives#getMembers(Class, String, String, int, Class, int, MemberName[])}.
 * 
 * @author Pietro Braione
 */
public final class Algo_JAVA_METHODHANDLENATIVES_GETMEMBERS extends Algo_INVOKEMETA_Nonbranching {
	private boolean justReturnMinusOne;
	private boolean justReturnZero;

	@Override
	protected Supplier<Integer> numOperands() {
		return () -> 7;
	}
	
	private static final String THIS_METHOD = "java.lang.invoke.MethodHandleNatives.getMembers";

	@Override
	protected void cookMore(State state) throws SymbolicValueNotAllowedException, FrozenStateException, 
	InterruptException, ClasspathException {
		//checks and gets the parameters, exits when trivial results
		this.justReturnMinusOne = false;
		getInstance(state, this.data.operand(0), THIS_METHOD, "Class defc", FAIL_JBSE, (msg) -> { this.justReturnMinusOne = true; }, INTERRUPT_SYMBOLIC_VALUE_NOT_ALLOWED_EXCEPTION);
		getInstance(state, this.data.operand(4), THIS_METHOD, "Class caller", FAIL_JBSE, (msg) -> { this.justReturnMinusOne = true; }, INTERRUPT_SYMBOLIC_VALUE_NOT_ALLOWED_EXCEPTION);
		getArray(state, this.data.operand(6), THIS_METHOD, "MemberName[] results", FAIL_JBSE, (msg) -> { this.justReturnMinusOne = true; }, INTERRUPT_SYMBOLIC_VALUE_NOT_ALLOWED_EXCEPTION, INTERRUPT_SYMBOLIC_VALUE_NOT_ALLOWED_EXCEPTION);
		if (this.justReturnMinusOne) {
			return;
		}
		this.justReturnZero = false;
		getInstance(state, this.data.operand(1), THIS_METHOD, "String matchName", FAIL_JBSE, (msg) -> { this.justReturnZero = true; }, INTERRUPT_SYMBOLIC_VALUE_NOT_ALLOWED_EXCEPTION);
		getInstance(state, this.data.operand(2), THIS_METHOD, "String matchSig", FAIL_JBSE, (msg) -> { this.justReturnZero = true; }, INTERRUPT_SYMBOLIC_VALUE_NOT_ALLOWED_EXCEPTION);
		if (this.justReturnZero) {
			return;
		}
		getInteger(state, this.data.operand(3), THIS_METHOD, "int matchFlags", FAIL_JBSE, FAIL_JBSE, INTERRUPT_SYMBOLIC_VALUE_NOT_ALLOWED_EXCEPTION);
		getInteger(state, this.data.operand(5), THIS_METHOD, "int skip", FAIL_JBSE, FAIL_JBSE, INTERRUPT_SYMBOLIC_VALUE_NOT_ALLOWED_EXCEPTION);
	}
	
	@Override
	protected StrategyUpdate<DecisionAlternative_NONE> updater() {
		return (state, alt) -> {
			final Calculator calc = this.ctx.getCalculator();
			if (this.justReturnMinusOne) {
	            state.pushOperand(calc.valInt(-1));
			} else if (this.justReturnZero) {
	            state.pushOperand(calc.valInt(0));
			} else {
				//checks and gets the parameters, exits when trivial results
				final Instance_JAVA_CLASS instanceDefc = (Instance_JAVA_CLASS) getInstance(state, this.data.operand(0), THIS_METHOD, "Class defc", FAIL_JBSE, FAIL_JBSE, FAIL_JBSE);
				final ClassFile k = instanceDefc.representedClass();
				final Instance instanceName = getInstance(state, this.data.operand(1), THIS_METHOD, "String matchName", FAIL_JBSE, FAIL_JBSE, FAIL_JBSE);
				final Instance instanceSig = getInstance(state, this.data.operand(2), THIS_METHOD, "String matchSig", FAIL_JBSE, FAIL_JBSE, FAIL_JBSE);
				String name = valueString(state, instanceName);
				final String sig = valueString(state, instanceSig);
				final Simplex simplexMatchFlags = (Simplex) getInteger(state, this.data.operand(3), THIS_METHOD, "int matchFlags", FAIL_JBSE, FAIL_JBSE, FAIL_JBSE);
				final Simplex simplexSkip = (Simplex) getInteger(state, this.data.operand(5), THIS_METHOD, "int skip", FAIL_JBSE, FAIL_JBSE, FAIL_JBSE);
				final Array arrayResults = getArray(state, this.data.operand(6), THIS_METHOD, "MemberName[] results", FAIL_JBSE, FAIL_JBSE, FAIL_JBSE, FAIL_JBSE);

				//code taken from MethodHandles::find_MemberNames
				
				int rfill = 0;
				int rlimit = ((Integer) ((Simplex) arrayResults.getLength()).getActualValue()).intValue();
				int rskip = ((Integer) simplexSkip.getActualValue()).intValue();
				int overflow = 0;
				int overflow_limit = Math.max(1000, rlimit);
				int match_flags = ((Integer) simplexMatchFlags.getActualValue()).intValue();
				boolean search_superc = ((match_flags & SEARCH_SUPERCLASSES) != 0);
				boolean search_intfc  = ((match_flags & SEARCH_INTERFACES)   != 0);
				boolean local_only = !(search_superc || search_intfc);
				boolean classes_only = !search_intfc;
				if (sig.charAt(0) == '(') {
					match_flags &= ~(IS_FIELD | IS_TYPE);
				} else {
					match_flags &= ~(IS_CONSTRUCTOR | IS_METHOD);
				}
				
				if ((match_flags & IS_TYPE) != 0) {
					// NYI, and Core Reflection works quite well for this query
				}

				if ((match_flags & IS_FIELD) != 0) {
					for (SignatureAndHolder sigAndHolderField: getAllFields(k, local_only, classes_only)) {
						if (name != null && !name.equals(sigAndHolderField.signature.getName())) {
							continue;
						}
						if (sig != null && !sig.equals(sigAndHolderField.signature.getDescriptor())) {
							continue;
						}
						//passed the filters
						if (rskip > 0) {
							--rskip;
						} else if (rfill < rlimit) {
							try {
								final Instance result = (Instance) ((AccessOutcomeInValue) arrayResults.getFast(calc, calc.valInt(rfill++))).getValue();
			                    final ClassHierarchy hier = state.getClassHierarchy();
			                    final ClassFile classFileMemberName = hier.getClassFileClassArray(CLASSLOADER_BOOT, JAVA_MEMBERNAME); //surely loaded
								if (!classFileMemberName.equals(result.getType())) {
						            state.pushOperand(calc.valInt(-99));
						            return;
								}
								//clazz
			                    state.ensureInstance_JAVA_CLASS(calc, sigAndHolderField.holder);
			                    final ReferenceConcrete fieldHolder = state.referenceToInstance_JAVA_CLASS(sigAndHolderField.holder);
			                    result.setFieldValue(JAVA_MEMBERNAME_CLAZZ, fieldHolder);
			                    //flags
			                    int flags = (short) (((short) sigAndHolderField.holder.getFieldModifiers(sigAndHolderField.signature)) & JVM_RECOGNIZED_FIELD_MODIFIERS);
			                    flags |= IS_FIELD | ((sigAndHolderField.holder.isFieldStatic(sigAndHolderField.signature) ? REF_getStatic : REF_getField) << REFERENCE_KIND_SHIFT);
			                    result.setFieldValue(JAVA_MEMBERNAME_FLAGS, calc.valInt(flags));
			                    //name
			                    state.ensureStringLiteral(calc, sigAndHolderField.signature.getName());
			                    final ReferenceConcrete fieldName = state.referenceToStringLiteral(sigAndHolderField.signature.getName());
			                    result.setFieldValue(JAVA_MEMBERNAME_NAME, fieldName);
			                    //type
			                    final ReferenceConcrete fieldType = referenceToType(state, calc, sigAndHolderField.signature.getDescriptor());
			                    result.setFieldValue(JAVA_MEMBERNAME_TYPE, fieldType);
							} catch (HeapMemoryExhaustedException e) {
				                throwNew(state, this.ctx.getCalculator(), OUT_OF_MEMORY_ERROR);
				                exitFromAlgorithm();
							} catch (FastArrayAccessNotAllowedException | FieldNotFoundException e) {
								//this should never happen
								failExecution(e);
							}
						} else if (++overflow >= overflow_limit) {
							match_flags = 0;
							break;
						}
					}
				}
				
				if ((match_flags & (IS_METHOD | IS_CONSTRUCTOR)) != 0) {
					String init_name = "<init>";
					String clinit_name = "<clinit>";
					if (clinit_name.equals(name)) {
						clinit_name = null; //hack for exposing <clinit>
					}
					boolean negate_name_test = false;
					if ((match_flags & IS_METHOD) == 0) {
						if (name == null) {
							name = init_name;
						} else if (!init_name.equals(name)) {
				            state.pushOperand(calc.valInt(0));
				            return;
						}
					} else if ((match_flags & IS_CONSTRUCTOR) == 0) {
						if (name == null) {
							name = init_name;
							negate_name_test = true;
						} else if (name.equals(init_name)) {
				            state.pushOperand(calc.valInt(0));
				            return;
						}
					} else {
						//caller will accept either sort; no need to adjust name
					}
					for (SignatureAndHolder sigAndHolderMethod: getAllMethods(k, local_only, classes_only)) {
						final String m_name = sigAndHolderMethod.signature.getName();
						if (clinit_name.equals(m_name)) {
							continue;
						}
						if (name != null && (!name.equals(m_name) ^ negate_name_test)) {
							continue;
						}
						if (sig != null && sig.equals(sigAndHolderMethod.signature.getDescriptor())) {
							continue;
						}
						//passed the filters
						if (rskip > 0) {
							--rskip;
						} else if (rfill < rlimit) {
							try {
								final Instance result = (Instance) ((AccessOutcomeInValue) arrayResults.getFast(calc, calc.valInt(rfill++))).getValue();
			                    final ClassHierarchy hier = state.getClassHierarchy();
			                    final ClassFile classFileMemberName = hier.getClassFileClassArray(CLASSLOADER_BOOT, JAVA_MEMBERNAME);
								if (!classFileMemberName.equals(result.getType())) {
						            state.pushOperand(calc.valInt(-99));
						            return;
								}
								//clazz
			                    //method MethodHandles::init_method_MemberName sets this field to
								//the method holder unless the method has a miranda slot (i.e., it 
								//is an abstract interface method or a default method). Since JBSE 
								//does not use miranda slots we always set clazz to the method holder
			                    state.ensureInstance_JAVA_CLASS(calc, sigAndHolderMethod.holder);
			                    final ReferenceConcrete fieldHolder = state.referenceToInstance_JAVA_CLASS(sigAndHolderMethod.holder);
			                    result.setFieldValue(JAVA_MEMBERNAME_CLAZZ, fieldHolder);
								//flags
			                    //determines the flags based on the kind of invocation, 
			                    //see hotspot:/src/share/vm/prims/methodHandles.cpp line 176 
			                    //method MethodHandles::init_method_MemberName
								int flags = getMemberNameFlagsMethod(sigAndHolderMethod.holder, sigAndHolderMethod.signature);
								result.setFieldValue(JAVA_MEMBERNAME_FLAGS, calc.valInt(flags));
								//name
			                    state.ensureStringLiteral(calc, sigAndHolderMethod.signature.getName());
			                    final ReferenceConcrete methName = state.referenceToStringLiteral(sigAndHolderMethod.signature.getName());
			                    result.setFieldValue(JAVA_MEMBERNAME_NAME, methName);
								//type
								//this is taken from the native implementation of the method
								//MemberName.expand(), that in turns calls MethodHandles::expand_MemberName,
								//see hotspot:/src/share/vm/prims/methodHandles.cpp line 774-775
			                    state.ensureStringLiteral(calc, sigAndHolderMethod.signature.getDescriptor());
			                    final ReferenceConcrete methDescriptor = state.referenceToStringLiteral(sigAndHolderMethod.signature.getDescriptor());
			                    result.setFieldValue(JAVA_MEMBERNAME_TYPE, methDescriptor);
							} catch (HeapMemoryExhaustedException e) {
				                throwNew(state, this.ctx.getCalculator(), OUT_OF_MEMORY_ERROR);
				                exitFromAlgorithm();
							} catch (FastArrayAccessNotAllowedException | MethodNotFoundException e) {
								//this should never happen
								failExecution(e);
							}
						} else if (++overflow >= overflow_limit) {
							match_flags = 0;
							break;
						}
					}
				}
				state.pushOperand(calc.valInt(rfill + overflow));
			}
		};
	}
	
	private static class SignatureAndHolder {
		public final Signature signature;
		public final ClassFile holder;
		public SignatureAndHolder(Signature signature, ClassFile holder) {
			this.signature = signature;
			this.holder = holder;
		}
	}
	
	private static ArrayList<SignatureAndHolder> getAllFields(ClassFile k, boolean local_only, boolean classes_only) {
		final ArrayList<SignatureAndHolder> retVal = new ArrayList<>();
		addAllFields(retVal, k);
		if (!local_only) {
			for (ClassFile k2 : k.superclasses()) {
				addAllFields(retVal, k2);
			}
			if (!classes_only) {
				for (ClassFile k3 : k.superinterfaces()) {
					addAllFields(retVal, k3);
				}
			}
		}
		return retVal;
	}
	
	private static void addAllFields(ArrayList<SignatureAndHolder> retVal, ClassFile k) {
		final Signature[] fieldsStatic = k.getDeclaredFieldsStatic();
		for (Signature s : fieldsStatic) {
			retVal.add(new SignatureAndHolder(s, k));
		}
		final Signature[] fieldsNonStatic = k.getDeclaredFieldsNonStatic();
		for (Signature s : fieldsNonStatic) {
			retVal.add(new SignatureAndHolder(s, k));
		}
	}
	
	private static ReferenceConcrete referenceToType(State state, Calculator calc, String descriptor) 
	throws FrozenStateException, InvalidInputException, HeapMemoryExhaustedException {
		if (isPrimitive(descriptor)) {
			return state.referenceToInstance_JAVA_CLASS_primitiveOrVoid(toPrimitiveOrVoidCanonicalName(descriptor));
		} else if (descriptor.equals(REFERENCE + JAVA_OBJECT + TYPEEND)) {
			final ClassFile cf_JAVA_OBJECT = state.getClassHierarchy().getClassFileClassArray(CLASSLOADER_BOOT, JAVA_OBJECT); //surely loaded
			state.ensureInstance_JAVA_CLASS(calc, cf_JAVA_OBJECT);
			return state.referenceToInstance_JAVA_CLASS(cf_JAVA_OBJECT);
		} else if (descriptor.equals(REFERENCE + JAVA_CLASS + TYPEEND)) {
			final ClassFile cf_JAVA_CLASS = state.getClassHierarchy().getClassFileClassArray(CLASSLOADER_BOOT, JAVA_CLASS); //surely loaded
			state.ensureInstance_JAVA_CLASS(calc, cf_JAVA_CLASS);
			return state.referenceToInstance_JAVA_CLASS(cf_JAVA_CLASS);
		} else if (descriptor.equals(REFERENCE + JAVA_STRING + TYPEEND)) {
			final ClassFile cf_JAVA_STRING = state.getClassHierarchy().getClassFileClassArray(CLASSLOADER_BOOT, JAVA_STRING); //surely loaded
			state.ensureInstance_JAVA_CLASS(calc, cf_JAVA_STRING);
			return state.referenceToInstance_JAVA_CLASS(cf_JAVA_STRING);
		} else {
			return Null.getInstance(); //the reference can be lazily reified
		}
	}
	
	private static ArrayList<SignatureAndHolder> getAllMethods(ClassFile k, boolean local_only, boolean classes_only) {
		final ArrayList<SignatureAndHolder> retVal = new ArrayList<>();
		addAllMethods(retVal, k);
		if (!local_only) {
			for (ClassFile k2 : k.superclasses()) {
				addAllMethods(retVal, k2);
			}
			if (!classes_only) {
				for (ClassFile k3 : k.superinterfaces()) {
					addAllMethods(retVal, k3);
				}
			}
		}
		return retVal;
	}
	
	private static void addAllMethods(ArrayList<SignatureAndHolder> retVal, ClassFile k) {
		final Signature[] constructors = k.getDeclaredConstructors();
		for (Signature s : constructors) {
			retVal.add(new SignatureAndHolder(s, k));
		}
		final Signature[] nonConstructors = k.getDeclaredMethods();
		for (Signature s : nonConstructors) {
			retVal.add(new SignatureAndHolder(s, k));
		}
	}	
}
