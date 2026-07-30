package jbse.apps.run;

import static jbse.bc.ClassLoaders.CLASSLOADER_APP;
import static jbse.bc.Signatures.JAVA_MAP;
import static jbse.bc.Signatures.JAVA_MAP_CONTAINSKEY;
import static jbse.bc.Signatures.JAVA_MAP_GET;

import jbse.bc.ClassFile;
import jbse.bc.ClassHierarchy;
import jbse.bc.exc.BadClassFileVersionException;
import jbse.bc.exc.ClassFileIllFormedException;
import jbse.bc.exc.ClassFileNotAccessibleException;
import jbse.bc.exc.ClassFileNotFoundException;
import jbse.bc.exc.IncompatibleClassFileException;
import jbse.bc.exc.PleaseLoadClassException;
import jbse.bc.exc.RenameUnsupportedException;
import jbse.bc.exc.WrongClassNameException;
import jbse.common.exc.InvalidInputException;
import jbse.common.exc.UnexpectedInternalException;
import jbse.mem.Clause;
import jbse.mem.ClauseAssume;
import jbse.mem.ClauseAssumeAliases;
import jbse.mem.ClauseAssumeNull;
import jbse.mem.ClauseAssumeReferenceSymbolic;
import jbse.val.Expression;
import jbse.val.Operator;
import jbse.val.Primitive;
import jbse.val.PrimitiveSymbolicMemberField;
import jbse.val.ReferenceSymbolic;
import jbse.val.SymbolicApply;
import jbse.val.SymbolicMemberField;
import jbse.val.Value;

public final class JAVA_MAP_Utils {
	public final static String INITIAL_MAP_FIELD_NAME = "initialMap";
	public final static String GET_SIGIL = "::GET(";

	public static boolean isInitialMapField(ClassHierarchy hier, Value value) {
		if (!(value instanceof SymbolicMemberField)) {
			return false;
		}
		final SymbolicMemberField originMemberField = (SymbolicMemberField) value;
		final ClassFile originMemberClass;
		try {
			originMemberClass = hier.loadCreateClass(CLASSLOADER_APP, originMemberField.getFieldClass(), true);
		} catch (InvalidInputException | ClassFileNotFoundException | ClassFileIllFormedException |
		         ClassFileNotAccessibleException | IncompatibleClassFileException | PleaseLoadClassException |
		         BadClassFileVersionException | RenameUnsupportedException | WrongClassNameException e) {
			//this should never happen
			throw new UnexpectedInternalException(e);
		}
		if (originMemberField.getFieldName().equals(INITIAL_MAP_FIELD_NAME) 
			&& classImplementsJavaUtilMap(originMemberClass)) {
			return true;
		} else {
			return false;
		}
	}

	public static boolean classImplementsJavaUtilMap(ClassFile clazz) {
		for (ClassFile interf: clazz.superinterfaces()) {
			if (JAVA_MAP.equals(interf.getClassName())) {
				return true;
			}	
		}
		return false;
	}

	public static boolean isSymbolicApplyOnInitialMap(ClassHierarchy hier, Value value) {
		if (!(value instanceof SymbolicApply)) {
			return false;
		}
		final SymbolicApply symbolicApply = (SymbolicApply) value;
		final Value[] args = symbolicApply.getArgs();
		if (args.length > 0 && isInitialMapField(hier, args[0])) {
			if (!symbolicApply.getOperator().equals(JAVA_MAP_CONTAINSKEY.toString())) {
				throw new UnexpectedInternalException("Path condition refers to unexpected symbolicApply on a symbolic map: " + symbolicApply.getOperator());
			}
			return true;
		} else {
			return false;
		}				
	}

	public static String possiblyAdaptMapModelSymbols(String originString) {
		final String initialMapFieldRegex = "\\.[^\\.]*Map:" + INITIAL_MAP_FIELD_NAME;
		final String originStringNoInitialMap;
		if (originString.matches(".*" + initialMapFieldRegex + ".*")) {
			originStringNoInitialMap = originString.replaceAll(initialMapFieldRegex, "");
		} else {
			originStringNoInitialMap = originString;
		}
		final String retVal;
		if (originStringNoInitialMap.contains(GET_SIGIL)) {
			final String mapRef = originStringNoInitialMap.substring(0, originStringNoInitialMap.indexOf(GET_SIGIL));
			String keyRef = originStringNoInitialMap.substring(originStringNoInitialMap.indexOf(GET_SIGIL) + GET_SIGIL.length());
			keyRef = keyRef.substring(0, keyRef.indexOf(')'));
			retVal = "<" + JAVA_MAP_GET.toString() + "@" + mapRef + "," + keyRef + ">";
		} else {
			retVal = originStringNoInitialMap;
		}
		return retVal;
	}

	//only tardis
	public static boolean mapModelAssumptionViolated(ClassHierarchy hier, Clause clause) {
		if (clause instanceof ClauseAssumeReferenceSymbolic) {
			//the initialMap field of a model map must only expand
			final ReferenceSymbolic ref = ((ClauseAssumeReferenceSymbolic) clause).getReference(); 
			if (isInitialMapField(hier, ref) && (clause instanceof ClauseAssumeAliases || clause instanceof ClauseAssumeNull)) {
				return true;
			} 
		} else if (clause instanceof ClauseAssume) {
			//the size of an initialMap must be always be greater or equal to zero
			final Expression cond = (Expression) ((ClauseAssume) clause).getCondition(); 
			final Primitive firstOp = cond.getFirstOperand();
			if (cond.getOperator().equals(Operator.LT) && firstOp instanceof PrimitiveSymbolicMemberField) {
				final PrimitiveSymbolicMemberField field = (PrimitiveSymbolicMemberField) firstOp;
				if (isInitialMapField(hier, field.getContainer()) && "size".equals(field.getFieldName())) {
					return true;
				}
			}
		}
		return false;
	}
	
    /**
     * Do not instantiate it! 
     */
	private JAVA_MAP_Utils() {
        //intentionally empty
	}
}
