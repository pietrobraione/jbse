package jbse.algo;

import static jbse.algo.Util.ARITHMETIC_EXCEPTION;
import jbse.Type;
import jbse.Util;
import jbse.exc.common.UnexpectedInternalException;
import jbse.exc.mem.InvalidOperandException;
import jbse.exc.mem.InvalidProgramCounterException;
import jbse.exc.mem.InvalidTypeException;
import jbse.exc.mem.OperandStackEmptyException;
import jbse.exc.mem.ThreadStackEmptyException;
import jbse.jvm.ExecutionContext;
import jbse.mem.Operator;
import jbse.mem.Primitive;
import jbse.mem.Simplex;
import jbse.mem.State;

public class SEBinOp implements Algorithm {
	Operator op;
	
	@Override
    public void exec(State state, ExecutionContext ctx) 
    throws ThreadStackEmptyException, OperandStackEmptyException {
    	final Primitive val2 = (Primitive) state.pop();
    	final Primitive val1 = (Primitive) state.pop();
    	
        try {
        	switch (op) {
        	case ADD:
        		state.push(val1.add(val2));
        		break;
        	case SUB:
        		state.push(val1.sub(val2));
        		break;
        	case MUL:
        		state.push(val1.mul(val2));
        		break;
        	case DIV:
        	case REM:
        		if (Type.isPrimitiveIntegralOpStack(val2.getType())) {
        			//TODO this works only for concrete values, add the case for symbolic ones with multiple state generation
        			if (val2 instanceof Simplex) {
        				Simplex op0_S = (Simplex) val2;
        				if (op0_S.isZeroOne(true)) {
        					state.createThrowableAndThrowIt(ARITHMETIC_EXCEPTION);
        					return;
        				}
        			}
        		}
        		state.push(op == Operator.DIV ? val1.div(val2) : val1.rem(val2));
        		break;
        	case SHL:
        		state.push(val1.shl(val2));
        		break;
        	case SHR:
        		state.push(val1.shr(val2));
        		break;
        	case USHR:
        		state.push(val1.ushr(val2));
        		break;
        	case ORBW:
        		state.push(val1.orBitwise(val2));
        		break;
        	case ANDBW:
        		state.push(val1.andBitwise(val2));
        		break;
        	case XORBW:
        		state.push(val1.xorBitwise(val2));
        		break;
        	default:
        		throw new UnexpectedInternalException();
        	}
		} catch (InvalidOperandException | InvalidTypeException e) {
			state.createThrowableAndThrowIt(Util.VERIFY_ERROR);
			return;
		}

    	try {
			state.incPC();
		} catch (InvalidProgramCounterException e) {
			state.createThrowableAndThrowIt(Util.VERIFY_ERROR);
		}
    }
}
