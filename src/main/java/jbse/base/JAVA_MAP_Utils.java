package jbse.base;

import static jbse.common.Type.binaryClassName;

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
import jbse.val.Symbolic;
import jbse.val.SymbolicApply;
import jbse.val.SymbolicMemberField;
import jbse.val.Value;

public class JAVA_MAP_Utils {
	public final static String INITIAL_MAP_FIELD_NAME = "initialMap";
	
	public static boolean isInitialMapField(Value value) {
		if (!(value instanceof SymbolicMemberField)) {
			return false;
		}
		SymbolicMemberField originMemberField = (SymbolicMemberField) value;
		if (binaryClassName(originMemberField.getFieldClass()).equals("java.util.HashMap") 
				&& originMemberField.getFieldName().equals(INITIAL_MAP_FIELD_NAME)) {
			return true;
		} else {
			return false;
		}
	}
	
	
	public static boolean isSymbolicApplyOnInitialMap(Value value) {
		if (!(value instanceof SymbolicApply)) {
			return false;
		}
		SymbolicApply symbolicApply = (SymbolicApply) value;
		jbse.val.Value[] args = symbolicApply.getArgs();
		if (args.length > 0 && JAVA_MAP_Utils.isInitialMapField(args[0])) {
			if (!symbolicApply.getOperator().equals("java/util/Map:(Ljava/lang/Object;)Z:containsKey")) {
				throw new UnexpectedInternalException("Path condition refers to unexpected symbolicApply on a symbolic map: " + symbolicApply.getOperator());
			}
			return true;
		} else {
			return false;
		}				
	}
	
	public static boolean assumptionViolated(Clause clause) {
		if (clause instanceof ClauseAssumeReferenceSymbolic) {
			ReferenceSymbolic ref = ((ClauseAssumeReferenceSymbolic) clause).getReference(); 
			if (isInitialMapField(ref) && (clause instanceof ClauseAssumeAliases || clause instanceof ClauseAssumeNull)) {
				return true;
			} 
		} else if (clause instanceof ClauseAssume) {
			Expression cond = (Expression) ((ClauseAssume) clause).getCondition(); 
			Primitive firstOp = cond.getFirstOperand();
			if (cond.getOperator().equals(Operator.LT) && firstOp instanceof PrimitiveSymbolicMemberField) {
				PrimitiveSymbolicMemberField field = (PrimitiveSymbolicMemberField) firstOp;
				if (isInitialMapField(field.getContainer()) && "size".equals(field.getFieldName())) {
					return true;
				}
			}
		}
		return false;
	}

	public static String possiblyAdaptMapModelSymbols(String origin) {
		if (origin.contains(".java/util/HashMap:" + INITIAL_MAP_FIELD_NAME)) {
			origin = origin.replace(".java/util/HashMap:" + INITIAL_MAP_FIELD_NAME, "");
		}
		if (origin.contains("::GET(")) {
			String mapRef = origin.substring(0, origin.indexOf("::GET("));
			String keyRef = origin.substring(origin.indexOf("::GET(") + 6);
			keyRef = keyRef.substring(0, keyRef.indexOf(')'));
			origin = "<java/util/Map:(Ljava/lang/Object;)Ljava/lang/Object;:get@" + mapRef + "," + keyRef + ">";
		}
		return origin;
	}	
}
