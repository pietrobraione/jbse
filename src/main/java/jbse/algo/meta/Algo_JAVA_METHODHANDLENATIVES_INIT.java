package jbse.algo.meta;

import static jbse.algo.Util.exitFromAlgorithm;
import static jbse.algo.Util.failExecution;
import static jbse.algo.Util.throwNew;
import static jbse.algo.meta.Util.FAIL_JBSE;
import static jbse.algo.meta.Util.getInstance;
import static jbse.algo.meta.Util.getMemberNameFlagsMethod;
import static jbse.algo.meta.Util.INTERRUPT_SYMBOLIC_VALUE_NOT_ALLOWED_EXCEPTION;
import static jbse.algo.meta.Util.JVM_RECOGNIZED_FIELD_MODIFIERS;
import static jbse.algo.meta.Util.IS_FIELD;
import static jbse.algo.meta.Util.REFERENCE_KIND_SHIFT;
import static jbse.algo.meta.Util.REF_getField;
import static jbse.algo.meta.Util.REF_getStatic;
import static jbse.bc.Signatures.INTERNAL_ERROR;
import static jbse.bc.Signatures.JAVA_CONSTRUCTOR;
import static jbse.bc.Signatures.JAVA_CONSTRUCTOR_CLAZZ;
import static jbse.bc.Signatures.JAVA_CONSTRUCTOR_SLOT;
import static jbse.bc.Signatures.JAVA_FIELD;
import static jbse.bc.Signatures.JAVA_FIELD_CLAZZ;
import static jbse.bc.Signatures.JAVA_FIELD_NAME;
import static jbse.bc.Signatures.JAVA_FIELD_SLOT;
import static jbse.bc.Signatures.JAVA_FIELD_TYPE;
import static jbse.bc.Signatures.JAVA_METHOD;
import static jbse.bc.Signatures.JAVA_METHOD_CLAZZ;
import static jbse.bc.Signatures.JAVA_METHOD_SLOT;
import static jbse.bc.Signatures.JAVA_MEMBERNAME_CLAZZ;
import static jbse.bc.Signatures.JAVA_MEMBERNAME_FLAGS;
import static jbse.bc.Signatures.JAVA_MEMBERNAME_NAME;
import static jbse.bc.Signatures.JAVA_MEMBERNAME_TYPE;

import java.util.function.Supplier;

import jbse.algo.Algo_INVOKEMETA_Nonbranching;
import jbse.algo.InterruptException;
import jbse.algo.StrategyUpdate;
import jbse.algo.exc.SymbolicValueNotAllowedException;
import jbse.algo.meta.exc.UndefinedResultException;
import jbse.algo.meta.Util.ErrorAction;
import jbse.bc.ClassFile;
import jbse.bc.ClassHierarchy;
import jbse.bc.Signature;
import jbse.bc.exc.BadClassFileVersionException;
import jbse.bc.exc.ClassFileIllFormedException;
import jbse.bc.exc.ClassFileNotAccessibleException;
import jbse.bc.exc.ClassFileNotFoundException;
import jbse.bc.exc.FieldNotFoundException;
import jbse.bc.exc.IncompatibleClassFileException;
import jbse.bc.exc.MethodNotFoundException;
import jbse.bc.exc.RenameUnsupportedException;
import jbse.bc.exc.WrongClassNameException;
import jbse.common.exc.ClasspathException;
import jbse.common.exc.InvalidInputException;
import jbse.mem.Instance;
import jbse.mem.Instance_JAVA_CLASS;
import jbse.mem.State;
import jbse.mem.exc.ThreadStackEmptyException;
import jbse.tree.DecisionAlternative_NONE;
import jbse.val.Calculator;
import jbse.val.Reference;
import jbse.val.Simplex;

/**
 * Meta-level implementation of {@link java.lang.invoke.MethodHandleNatives#init(java.lang.invoke.MemberName, Object)}.
 * 
 * @author Pietro Braione
 */
public final class Algo_JAVA_METHODHANDLENATIVES_INIT extends Algo_INVOKEMETA_Nonbranching {
	private ClassFile cf_JAVA_FIELD; //set by cookMore
	private ClassFile cf_JAVA_METHOD; //set by cookMore
	private ClassFile cf_JAVA_CONSTRUCTOR; //set by cookMore

	@Override
	protected Supplier<Integer> numOperands() {
		return () -> 2;
	}

	@Override
	protected void cookMore(State state) 
	throws ThreadStackEmptyException, InterruptException, UndefinedResultException, 
	SymbolicValueNotAllowedException, ClasspathException, InvalidInputException, 
	RenameUnsupportedException {
		final ErrorAction THROW_JAVA_INTERNAL_ERROR = msg -> { throwNew(state, this.ctx.getCalculator(), INTERNAL_ERROR); exitFromAlgorithm(); };

		//checks the first parameter (the MemberName)
		getInstance(state, this.data.operand(0), "java.lang.invoke.MethodHandleNatives.init", "MemberName self", FAIL_JBSE, THROW_JAVA_INTERNAL_ERROR, INTERRUPT_SYMBOLIC_VALUE_NOT_ALLOWED_EXCEPTION);

		//checks the second parameter (the Target)
		getInstance(state, this.data.operand(1), "java.lang.invoke.MethodHandleNatives.init", "Object ref", FAIL_JBSE, THROW_JAVA_INTERNAL_ERROR, INTERRUPT_SYMBOLIC_VALUE_NOT_ALLOWED_EXCEPTION);

		//gets some standard classes
		final ClassHierarchy hier = state.getClassHierarchy();
		try {
			this.cf_JAVA_FIELD = hier.loadCreateClass(JAVA_FIELD);
			this.cf_JAVA_METHOD = hier.loadCreateClass(JAVA_METHOD);
			this.cf_JAVA_CONSTRUCTOR = hier.loadCreateClass(JAVA_CONSTRUCTOR);
		} catch (InvalidInputException | ClassFileNotFoundException | ClassFileIllFormedException
				| ClassFileNotAccessibleException | IncompatibleClassFileException | BadClassFileVersionException
				| RenameUnsupportedException | WrongClassNameException e) {
			//this should never happen
			failExecution(e);
		} 
	}

	@Override
	protected StrategyUpdate<DecisionAlternative_NONE> updater() {
		return (state, alt) -> {
			try {
				final Calculator calc = this.ctx.getCalculator();

				//gets the first parameter (the MemberName)
				final Instance memberNameObject = getInstance(state, this.data.operand(0), "java.lang.invoke.MethodHandleNatives.init", "MemberName self", FAIL_JBSE, FAIL_JBSE, FAIL_JBSE);

				//gets the second parameter (the Target)
				final Instance targetObject = getInstance(state, this.data.operand(1), "java.lang.invoke.MethodHandleNatives.init", "Object ref", FAIL_JBSE, FAIL_JBSE, FAIL_JBSE);

				//gets the class of the target
				final ClassFile targetClass = targetObject.getType();

				if (this.cf_JAVA_FIELD.equals(targetClass)) {
					//gets clazz, the class where the field is declared (the field holder)
					final Reference fieldClazz = (Reference) targetObject.getFieldValue(JAVA_FIELD_CLAZZ);
					if (state.isNull(fieldClazz)) {
						return; //nothing to do
					}
					final Instance_JAVA_CLASS fieldClazzInstance = (Instance_JAVA_CLASS) state.getObject(fieldClazz);
					final ClassFile cf_fieldClazz = fieldClazzInstance.representedClass();
					if (cf_fieldClazz.isArray() || cf_fieldClazz.isPrimitiveOrVoid()) {
						return; //nothing to do
					}

					//gets the slot to determine the signature of the field. This is to comply 
					//with the Hotspot code of init_field_MemberName where it is stated that the
					//name and type of a java.lang.Field object can be lazily computed and thus 
					//can be null (although this should not be the case in JBSE). See for reference
					//the comment in MethodHandles::init_field_MemberName at 
					//hotspot:/src/share/vm/prims/methodHandles.cpp
					final int fieldSlot = ((Integer) ((Simplex) targetObject.getFieldValue(JAVA_FIELD_SLOT)).getActualValue()).intValue();
					final Signature[] declaredFields = cf_fieldClazz.getDeclaredFields();
					if (fieldSlot < 0 || fieldSlot >= declaredFields.length) {
						return; //TODO crash???
					}
					final Signature fieldSignature = declaredFields[fieldSlot];

					//sets memberNameObject's clazz field with the reference to the field holder class
					memberNameObject.setFieldValue(JAVA_MEMBERNAME_CLAZZ, fieldClazz);

					//gets the flags and sets memberNameObject's flags field with them
					int flags = (short) (((short) cf_fieldClazz.getFieldModifiers(fieldSignature)) & JVM_RECOGNIZED_FIELD_MODIFIERS);
					flags |= IS_FIELD | ((cf_fieldClazz.isFieldStatic(fieldSignature) ? REF_getStatic : REF_getField) << REFERENCE_KIND_SHIFT);
					memberNameObject.setFieldValue(JAVA_MEMBERNAME_FLAGS, calc.valInt(flags));

					//gets the field type and name fields, and if they are not null
					//sets memberNameObject's type and name fields with them
					final Reference fieldType = (Reference) targetObject.getFieldValue(JAVA_FIELD_TYPE);
					if (!state.isNull(fieldType)) {
						memberNameObject.setFieldValue(JAVA_MEMBERNAME_TYPE, fieldType);
					}
					final Reference fieldName = (Reference) targetObject.getFieldValue(JAVA_FIELD_NAME);
					if (!state.isNull(fieldName)) {
						memberNameObject.setFieldValue(JAVA_MEMBERNAME_NAME, fieldName);
					}
				} else if (this.cf_JAVA_METHOD.equals(targetClass)) {
					//gets clazz, the class where the method is declared (the method holder)
					final Reference methodClazz = (Reference) targetObject.getFieldValue(JAVA_METHOD_CLAZZ);
					if (state.isNull(methodClazz)) {
						return; //nothing to do
					}
					final Instance_JAVA_CLASS methodClazzInstance = (Instance_JAVA_CLASS) state.getObject(methodClazz);
					final ClassFile cf_methodClazz = methodClazzInstance.representedClass();
					if (cf_methodClazz.isArray() || cf_methodClazz.isPrimitiveOrVoid()) {
						return; //nothing to do
					}

					//gets the slot and the signature, see above for comments
					final int methodSlot = ((Integer) ((Simplex) targetObject.getFieldValue(JAVA_METHOD_SLOT)).getActualValue()).intValue();
					final Signature[] declaredMethods = cf_methodClazz.getDeclaredMethods();
					if (methodSlot < 0 || methodSlot >= declaredMethods.length) {
						return; //TODO crash???
					}
					final Signature methodSignature = declaredMethods[methodSlot];

					//exits if the method is signature polymorphic
					if (cf_methodClazz.isMethodSignaturePolymorphic(methodSignature)) {
						return;
					}

					//sets memberNameObject's clazz field with the reference to the method holder class
					memberNameObject.setFieldValue(JAVA_MEMBERNAME_CLAZZ, methodClazz);

					//gets the flags and sets memberNameObject's flags field with them
					final int flags = getMemberNameFlagsMethod(cf_methodClazz, methodSignature);
					memberNameObject.setFieldValue(JAVA_MEMBERNAME_FLAGS, calc.valInt(flags));

				} else if (this.cf_JAVA_CONSTRUCTOR.equals(targetClass)) {
					//gets clazz, the class where the constructor is declared (the method holder)
					final Reference constructorClazz = (Reference) targetObject.getFieldValue(JAVA_CONSTRUCTOR_CLAZZ);
					if (state.isNull(constructorClazz)) {
						return; //nothing to do
					}
					final Instance_JAVA_CLASS constructorClazzInstance = (Instance_JAVA_CLASS) state.getObject(constructorClazz);
					final ClassFile cf_constructorClazz = constructorClazzInstance.representedClass();
					if (cf_constructorClazz.isArray() || cf_constructorClazz.isPrimitiveOrVoid()) {
						return; //nothing to do
					}

					//gets the slot and the signature, see above for comments
					final int constructorSlot = ((Integer) ((Simplex) targetObject.getFieldValue(JAVA_CONSTRUCTOR_SLOT)).getActualValue()).intValue();
					final Signature[] declaredConstructors = cf_constructorClazz.getDeclaredConstructors();
					if (constructorSlot < 0 || constructorSlot >= declaredConstructors.length) {
						return; //TODO crash???
					}
					final Signature constructorSignature = declaredConstructors[constructorSlot];

					//sets memberNameObject's clazz field with the reference to the method holder class
					memberNameObject.setFieldValue(JAVA_MEMBERNAME_CLAZZ, constructorClazz);

					//gets the flags and sets memberNameObject's flags field with them
					final int flags = getMemberNameFlagsMethod(cf_constructorClazz, constructorSignature);
					memberNameObject.setFieldValue(JAVA_MEMBERNAME_FLAGS, calc.valInt(flags));
				} //else, does nothing
			} catch (FieldNotFoundException | MethodNotFoundException e) {
				//this should never happen
				failExecution(e);
			}
		};
	}
}
