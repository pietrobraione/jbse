package jbse.algo;

import static jbse.algo.Util.throwNew;
import static jbse.algo.Util.ensureClassInstance;
import static jbse.algo.Util.ensureStringLiteral;
import static jbse.algo.Util.throwVerifyError;
import static jbse.bc.Signatures.ILLEGAL_ACCESS_ERROR;
import static jbse.bc.Signatures.NO_CLASS_DEFINITION_FOUND_ERROR;

import jbse.algo.exc.InterruptException;
import jbse.bc.ClassFile;
import jbse.bc.ConstantPoolClass;
import jbse.bc.ConstantPoolPrimitive;
import jbse.bc.ConstantPoolString;
import jbse.bc.ConstantPoolValue;
import jbse.bc.exc.BadClassFileException;
import jbse.bc.exc.ClassFileNotAccessibleException;
import jbse.bc.exc.ClassFileNotFoundException;
import jbse.bc.exc.InvalidIndexException;
import jbse.common.Type;
import jbse.common.Util;
import jbse.common.exc.ClasspathException;
import jbse.common.exc.UnexpectedInternalException;
import jbse.dec.exc.DecisionException;
import jbse.mem.State;
import jbse.mem.exc.InvalidProgramCounterException;
import jbse.mem.exc.ThreadStackEmptyException;
import jbse.val.Value;

final class Algo_LDCX_Y implements Algorithm {
	boolean wide;
	boolean cat1;

	public Algo_LDCX_Y() { }
	
	@Override
	public void exec(State state, ExecutionContext ctx) 
	throws ClasspathException, DecisionException, 
	ThreadStackEmptyException, InterruptException {
		final int index;
		try {
			final byte tmp1 = state.getInstruction(1);
			if (this.wide) {
                final byte tmp2 = state.getInstruction(2);
                index = Util.byteCat(tmp1, tmp2);
			} else {
                index = Util.byteCat((byte) 0, tmp1);
			}
		} catch (InvalidProgramCounterException e) {
            throwVerifyError(state);
			return;
		}

		//gets the current class and pushes the value stored 
		//at index
		final String currentClassName = state.getCurrentMethodSignature().getClassName();
		final ClassFile cf;
		try {
			cf = state.getClassHierarchy().getClassFile(currentClassName);
		} catch (BadClassFileException e) {
			//this should never happen
			throw new UnexpectedInternalException(e);
		}
		
        final Value val;
        try {
            final ConstantPoolValue cpv = cf.getValueFromConstantPool(index);
            if (cpv instanceof ConstantPoolPrimitive) {
                val = state.getCalculator().val_(cpv.getValue());
                if (this.cat1 != Type.isCat_1(val.getType())) {
                    throwVerifyError(state);
                    return;
                }
            } else if (cpv instanceof ConstantPoolString) {
                final String stringLit = ((ConstantPoolString) cpv).getValue();
                ensureStringLiteral(state, stringLit, ctx.decisionProcedure);
                val = state.referenceToStringLiteral(stringLit);
            } else { // cpv instanceof ConstantPoolClass
                final String classSignature = ((ConstantPoolClass) cpv).getValue();
                ensureClassInstance(state, classSignature, ctx.decisionProcedure);
                val = state.referenceToClassInstance(classSignature);
            }
        } catch (InvalidIndexException e) {
            throwVerifyError(state);
            return;
        } catch (ClassFileNotFoundException e) {
            throwNew(state, NO_CLASS_DEFINITION_FOUND_ERROR);
            return;
        } catch (ClassFileNotAccessibleException e) {
            throwNew(state, ILLEGAL_ACCESS_ERROR);
            return;
        } catch (BadClassFileException e) {
            throwVerifyError(state);
            return;
        }

		//pushes the value on the operand stack
		state.push(val);
		
		//increments the program counter
		try {
			if (this.wide) {
                state.incPC(3);
			} else {
                state.incPC(2);
			}
		} catch (InvalidProgramCounterException e) {
            throwVerifyError(state);
		}
	} 
}
