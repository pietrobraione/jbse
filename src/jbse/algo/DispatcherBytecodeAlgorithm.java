package jbse.algo;

import static jbse.bc.Opcodes.*;

import jbse.algo.exc.NotYetImplementedException;
import jbse.bc.Dispatcher;
import jbse.common.Type;
import jbse.common.exc.UnexpectedInternalException;
import jbse.val.Operator;


/**
 * A {@link Dispatcher} returning the {@link Algorithm}s to process 
 * every bytecode of the symbolic execution. 
 * 
 * @author Pietro Braione
 *
 */
public class DispatcherBytecodeAlgorithm extends Dispatcher<Byte, Algorithm> {
    private Algo_INIT seInit = null;
    private Algo_NOTALLOWED algo_NOTALLOWED = null;
    private Algo_ACONST_NULL algo_ACONST_NULL = null;
    private Algo_XALOAD algo_XALOAD = null;
    private Algo_ANEWARRAY algo_ANEWARRAY = null;
    private Algo_ARRAYLENGTH algo_ARRAYLENGTH = null;
    private Algo_XASTORE algo_XASTORE = null;
    private Algo_ATHROW algo_ATHROW = null;
	private Algo_XBINOP algo_XBINOP = null;
    private Algo_BIPUSH algo_BIPUSH = null;
    private Algo_CHECKCAST algo_CHECKCAST = null;
    private Algo_XCONST_Y algo_XCONST_Y = null;
    private Algo_DUPX_Y algo_DUPX_Y = null;
    private Algo_DUPX algo_DUPX = null;
    private Algo_GETFIELD algo_GETFIELD = null;
    private Algo_GETSTATIC algo_GETSTATIC = null;
    private Algo_GOTOX algo_GOTOX = null;
    private Algo_IF_ACMPX_XNULL algo_IF_ACMPX_XNULL = null;
    private Algo_IFX algo_IFX = null;
    private Algo_IINC algo_IINC = null;
    private Algo_INSTANCEOF algo_INSTANCEOF = null;
    private Algo_INVOKEX algo_INVOKEX = null;
    private Algo_JSRX algo_JSRX = null;
    private Algo_LDCX_Y algo_LDC = null;
    private Algo_XLOAD algo_XLOAD = null;
    private Algo_MONITORX algo_MONITORX = null;
    private Algo_MULTIANEWARRAY algo_MULTIANEWARRAY = null;
    private Algo_X2Y algo_X2Y = null;
    private Algo_XCMPY algo_XCMPY = null;
    private Algo_XNEG algo_XNEG = null;
    private Algo_NEW algo_NEW = null;
    private Algo_NEWARRAY algo_NEWARRAY = null;
    private Algo_NOP algo_NOP = null;
    private Algo_POPX algo_POPX = null;
    private Algo_PUTFIELD algo_PUTFIELD = null;
    private Algo_PUTSTATIC algo_PUTSTATIC = null;
    private Algo_RET algo_RET = null;
    private Algo_XRETURN algo_XRETURN = null;
    private Algo_SIPUSH algo_SIPUSH = null;
    private Algo_XSTORE algo_XSTORE = null;
    private Algo_SWAP algo_SWAP = null;
    private Algo_XSWITCH algo_XSWITCH = null;
    private Algo_WIDE algo_WIDE = null;

	private class DispatchStrategy_NOP implements Dispatcher.DispatchStrategy<Algo_NOP> {
		public Algo_NOP doIt() {
			if (DispatcherBytecodeAlgorithm.this.algo_NOP == null) {
				DispatcherBytecodeAlgorithm.this.algo_NOP = new Algo_NOP();
			}
			return DispatcherBytecodeAlgorithm.this.algo_NOP;
		}
	}
	
	private class DispatchStrategy_XLOAD implements Dispatcher.DispatchStrategy<Algo_XLOAD> {
		private boolean hasIndex;
		private int index;
		public DispatchStrategy_XLOAD() { this.hasIndex = false; }
		public DispatchStrategy_XLOAD(int index) { this.hasIndex = true; this.index = index; }
		public Algo_XLOAD doIt() {
			if (DispatcherBytecodeAlgorithm.this.algo_XLOAD == null) {
				DispatcherBytecodeAlgorithm.this.algo_XLOAD = new Algo_XLOAD();
			}
			DispatcherBytecodeAlgorithm.this.algo_XLOAD.hasIndex = this.hasIndex;
			DispatcherBytecodeAlgorithm.this.algo_XLOAD.index = this.index;
			return DispatcherBytecodeAlgorithm.this.algo_XLOAD;
		}
	}
	
	private class DispatchStrategy_XSTORE implements Dispatcher.DispatchStrategy<Algo_XSTORE> {
		private boolean hasIndex;
		private int index;
		public DispatchStrategy_XSTORE() { this.hasIndex = false; }
		public DispatchStrategy_XSTORE(int index) { this.hasIndex = true; this.index = index; }
		public Algo_XSTORE doIt() {
			if (DispatcherBytecodeAlgorithm.this.algo_XSTORE == null) {
				DispatcherBytecodeAlgorithm.this.algo_XSTORE = new Algo_XSTORE();
			}
			DispatcherBytecodeAlgorithm.this.algo_XSTORE.hasIndex = this.hasIndex;
			DispatcherBytecodeAlgorithm.this.algo_XSTORE.index = this.index;
			return DispatcherBytecodeAlgorithm.this.algo_XSTORE;
		}
	}
	
	private class DispatchStrategy_BIPUSH implements Dispatcher.DispatchStrategy<Algo_BIPUSH> {
		public Algo_BIPUSH doIt() {
			if (DispatcherBytecodeAlgorithm.this.algo_BIPUSH == null) {
				DispatcherBytecodeAlgorithm.this.algo_BIPUSH = new Algo_BIPUSH();
			}
			return DispatcherBytecodeAlgorithm.this.algo_BIPUSH;
		}
	}

	private class DispatchStrategy_SIPUSH implements Dispatcher.DispatchStrategy<Algo_SIPUSH> {
		public Algo_SIPUSH doIt() {
			if (DispatcherBytecodeAlgorithm.this.algo_SIPUSH == null) {
				DispatcherBytecodeAlgorithm.this.algo_SIPUSH = new Algo_SIPUSH();
			}
			return DispatcherBytecodeAlgorithm.this.algo_SIPUSH;
		}
	}

	private class DispatchStrategy_LDCX_Y implements Dispatcher.DispatchStrategy<Algo_LDCX_Y> {
		private boolean wide;
        private boolean cat1;
		public DispatchStrategy_LDCX_Y(boolean wide, boolean cat1) {
			this.wide = wide;
			this.cat1 = cat1;
		}
		public Algo_LDCX_Y doIt() {
			if (DispatcherBytecodeAlgorithm.this.algo_LDC == null) {
				DispatcherBytecodeAlgorithm.this.algo_LDC = new Algo_LDCX_Y();
			}
			DispatcherBytecodeAlgorithm.this.algo_LDC.wide = this.wide;
            DispatcherBytecodeAlgorithm.this.algo_LDC.cat1 = this.cat1;
			return DispatcherBytecodeAlgorithm.this.algo_LDC;
		}
	}

	private class DispatchStrategy_ACONST_NULL implements Dispatcher.DispatchStrategy<Algo_ACONST_NULL> {
		public Algo_ACONST_NULL doIt() {
			if (DispatcherBytecodeAlgorithm.this.algo_ACONST_NULL == null) {
				DispatcherBytecodeAlgorithm.this.algo_ACONST_NULL = new Algo_ACONST_NULL();
			}
			return DispatcherBytecodeAlgorithm.this.algo_ACONST_NULL;
		}
	}

	private class DispatchStrategy_XCONST_Y implements Dispatcher.DispatchStrategy<Algo_XCONST_Y> {
		private char type;
		private int value;
		public DispatchStrategy_XCONST_Y(char type, int value) {
			this.type = type;
			this.value = value;
		}
		public Algo_XCONST_Y doIt() {
			if (DispatcherBytecodeAlgorithm.this.algo_XCONST_Y == null) {
				DispatcherBytecodeAlgorithm.this.algo_XCONST_Y = new Algo_XCONST_Y();
			}
			DispatcherBytecodeAlgorithm.this.algo_XCONST_Y.type = this.type;
			DispatcherBytecodeAlgorithm.this.algo_XCONST_Y.value = this.value;
			return DispatcherBytecodeAlgorithm.this.algo_XCONST_Y;
		}
	}

	private class DispatchStrategy_XBINOP implements Dispatcher.DispatchStrategy<Algo_XBINOP> {
		private Operator op;
		public DispatchStrategy_XBINOP(Operator op) {
			this.op = op;
		}
		public Algo_XBINOP doIt() {
			if (DispatcherBytecodeAlgorithm.this.algo_XBINOP  == null) {
				DispatcherBytecodeAlgorithm.this.algo_XBINOP = new Algo_XBINOP();
			}
			DispatcherBytecodeAlgorithm.this.algo_XBINOP.op = op;
			return DispatcherBytecodeAlgorithm.this.algo_XBINOP;
		}
	}

	private class DispatchStrategy_XNEG implements Dispatcher.DispatchStrategy<Algo_XNEG> {
		public Algo_XNEG doIt() {
			if (DispatcherBytecodeAlgorithm.this.algo_XNEG == null) {
				DispatcherBytecodeAlgorithm.this.algo_XNEG = new Algo_XNEG();
			}
			return DispatcherBytecodeAlgorithm.this.algo_XNEG;
		}
	}

	private class DispatchStrategy_IINC implements Dispatcher.DispatchStrategy<Algo_IINC> {
		public Algo_IINC doIt() {
			if (DispatcherBytecodeAlgorithm.this.algo_IINC == null) {
				DispatcherBytecodeAlgorithm.this.algo_IINC = new Algo_IINC();
			}
			return DispatcherBytecodeAlgorithm.this.algo_IINC;
		}
	}

	private class DispatchStrategy_XCMPY implements Dispatcher.DispatchStrategy<Algo_XCMPY> {
		public Algo_XCMPY doIt() {
			if (DispatcherBytecodeAlgorithm.this.algo_XCMPY == null) {
				DispatcherBytecodeAlgorithm.this.algo_XCMPY = new Algo_XCMPY();
			}
			return DispatcherBytecodeAlgorithm.this.algo_XCMPY;
		}
	}

	private class DispatchStrategy_X2Y implements Dispatcher.DispatchStrategy<Algo_X2Y> {
		private char fromType;
		private char toType;
		public DispatchStrategy_X2Y(char fromType, char toType) {
			this.fromType = fromType;
			this.toType = toType;
		}
		public Algo_X2Y doIt() {
			if (DispatcherBytecodeAlgorithm.this.algo_X2Y == null) {
				DispatcherBytecodeAlgorithm.this.algo_X2Y = new Algo_X2Y();
			}
			DispatcherBytecodeAlgorithm.this.algo_X2Y.fromType = this.fromType;
			DispatcherBytecodeAlgorithm.this.algo_X2Y.toType = this.toType;
			return DispatcherBytecodeAlgorithm.this.algo_X2Y;
		}
	}

	private class DispatchStrategy_NEW implements Dispatcher.DispatchStrategy<Algo_NEW> {
		public Algo_NEW doIt() {
			if (DispatcherBytecodeAlgorithm.this.algo_NEW == null) {
				DispatcherBytecodeAlgorithm.this.algo_NEW = new Algo_NEW();
			}
			return DispatcherBytecodeAlgorithm.this.algo_NEW;
		}
	}

	private class DispatchStrategy_NEWARRAY implements Dispatcher.DispatchStrategy<Algo_NEWARRAY> {
		public Algo_NEWARRAY doIt() {
			if (DispatcherBytecodeAlgorithm.this.algo_NEWARRAY == null) {
				DispatcherBytecodeAlgorithm.this.algo_NEWARRAY = new Algo_NEWARRAY();
			}
			return DispatcherBytecodeAlgorithm.this.algo_NEWARRAY;
		}
	}

	private class DispatchStrategy_ANEWARRAY implements Dispatcher.DispatchStrategy<Algo_ANEWARRAY> {
		public Algo_ANEWARRAY doIt() {
			if (DispatcherBytecodeAlgorithm.this.algo_ANEWARRAY == null) {
				DispatcherBytecodeAlgorithm.this.algo_ANEWARRAY = new Algo_ANEWARRAY();
			}
			return DispatcherBytecodeAlgorithm.this.algo_ANEWARRAY;
		}
	}

	private class DispatchStrategy_MULTIANEWARRAY implements Dispatcher.DispatchStrategy<Algo_MULTIANEWARRAY> {
		public Algo_MULTIANEWARRAY doIt() {
			if (DispatcherBytecodeAlgorithm.this.algo_MULTIANEWARRAY == null) {
				DispatcherBytecodeAlgorithm.this.algo_MULTIANEWARRAY = new Algo_MULTIANEWARRAY();
			}
			return DispatcherBytecodeAlgorithm.this.algo_MULTIANEWARRAY;
		}
	}

	private class DispatchStrategy_GETFIELD implements Dispatcher.DispatchStrategy<Algo_GETFIELD> {
		public Algo_GETFIELD doIt() {
			if (DispatcherBytecodeAlgorithm.this.algo_GETFIELD == null) {
				DispatcherBytecodeAlgorithm.this.algo_GETFIELD = new Algo_GETFIELD();
			}
			return DispatcherBytecodeAlgorithm.this.algo_GETFIELD;
		}
	}

	private class DispatchStrategy_PUTFIELD implements Dispatcher.DispatchStrategy<Algo_PUTFIELD> {
		public Algo_PUTFIELD doIt() {
			if (DispatcherBytecodeAlgorithm.this.algo_PUTFIELD == null) {
				DispatcherBytecodeAlgorithm.this.algo_PUTFIELD = new Algo_PUTFIELD();
			}
			return DispatcherBytecodeAlgorithm.this.algo_PUTFIELD;
		}
	}

	private class DispatchStrategy_GETSTATIC implements Dispatcher.DispatchStrategy<Algo_GETSTATIC> {
		public Algo_GETSTATIC doIt() {
			if (DispatcherBytecodeAlgorithm.this.algo_GETSTATIC == null) {
				DispatcherBytecodeAlgorithm.this.algo_GETSTATIC = new Algo_GETSTATIC();
			}
			return DispatcherBytecodeAlgorithm.this.algo_GETSTATIC;
		}
	}

	private class DispatchStrategy_PUTSTATIC implements Dispatcher.DispatchStrategy<Algo_PUTSTATIC> {
		public Algo_PUTSTATIC doIt() {
			if (DispatcherBytecodeAlgorithm.this.algo_PUTSTATIC == null) {
				DispatcherBytecodeAlgorithm.this.algo_PUTSTATIC = new Algo_PUTSTATIC();
			}
			return DispatcherBytecodeAlgorithm.this.algo_PUTSTATIC;
		}
	}

	private class DispatchStrategy_XALOAD implements Dispatcher.DispatchStrategy<Algo_XALOAD> {
		public Algo_XALOAD doIt() {
			if (DispatcherBytecodeAlgorithm.this.algo_XALOAD == null) {
				DispatcherBytecodeAlgorithm.this.algo_XALOAD = new Algo_XALOAD();
			}
			return DispatcherBytecodeAlgorithm.this.algo_XALOAD;
		}
	}

	private class DispatchStrategy_XASTORE implements Dispatcher.DispatchStrategy<Algo_XASTORE> {
		public Algo_XASTORE doIt() {
			if (DispatcherBytecodeAlgorithm.this.algo_XASTORE == null) {
				DispatcherBytecodeAlgorithm.this.algo_XASTORE = new Algo_XASTORE();
			}
			return DispatcherBytecodeAlgorithm.this.algo_XASTORE;
		}
	}

	private class DispatchStrategy_ARRAYLENGTH implements Dispatcher.DispatchStrategy<Algo_ARRAYLENGTH> {
		public Algo_ARRAYLENGTH doIt() {
			if (DispatcherBytecodeAlgorithm.this.algo_ARRAYLENGTH == null) {
				DispatcherBytecodeAlgorithm.this.algo_ARRAYLENGTH = new Algo_ARRAYLENGTH();
			}
			return DispatcherBytecodeAlgorithm.this.algo_ARRAYLENGTH;
		}
	}

	private class DispatchStrategy_INSTANCEOF implements Dispatcher.DispatchStrategy<Algo_INSTANCEOF> {
		public Algo_INSTANCEOF doIt() {
			if (DispatcherBytecodeAlgorithm.this.algo_INSTANCEOF == null) {
				DispatcherBytecodeAlgorithm.this.algo_INSTANCEOF = new Algo_INSTANCEOF();
			}
			return DispatcherBytecodeAlgorithm.this.algo_INSTANCEOF;
		}
	}

	private class DispatchStrategy_CHECKCAST implements Dispatcher.DispatchStrategy<Algo_CHECKCAST> {
		public Algo_CHECKCAST doIt() {
			if (DispatcherBytecodeAlgorithm.this.algo_CHECKCAST == null) {
				DispatcherBytecodeAlgorithm.this.algo_CHECKCAST = new Algo_CHECKCAST();
			}
			return DispatcherBytecodeAlgorithm.this.algo_CHECKCAST;
		}
	}

	private class DispatchStrategy_POPX implements Dispatcher.DispatchStrategy<Algo_POPX> {
		private boolean cat1;
		public DispatchStrategy_POPX(boolean cat1) {
			this.cat1 = cat1;
		}
		public Algo_POPX doIt() {
			if (DispatcherBytecodeAlgorithm.this.algo_POPX == null) {
				DispatcherBytecodeAlgorithm.this.algo_POPX = new Algo_POPX();
			}
			DispatcherBytecodeAlgorithm.this.algo_POPX.cat1 = this.cat1;
			return DispatcherBytecodeAlgorithm.this.algo_POPX;
		}
	}

	private class DispatchStrategy_DUPX implements Dispatcher.DispatchStrategy<Algo_DUPX> {
		private boolean cat1;
		public DispatchStrategy_DUPX(boolean cat1) {
			this.cat1 = cat1;
		}
		public Algo_DUPX doIt() {
			if (DispatcherBytecodeAlgorithm.this.algo_DUPX == null) {
				DispatcherBytecodeAlgorithm.this.algo_DUPX = new Algo_DUPX();
			}
			DispatcherBytecodeAlgorithm.this.algo_DUPX.cat1 = this.cat1;
			return DispatcherBytecodeAlgorithm.this.algo_DUPX;
		}
	}

	private class DispatchStrategy_DUPX_Y implements Dispatcher.DispatchStrategy<Algo_DUPX_Y> {
		private boolean cat1;
		private boolean x1;
		public DispatchStrategy_DUPX_Y(boolean cat1, boolean x1) {
			this.cat1 = cat1;
			this.x1 = x1;
		}
		public Algo_DUPX_Y doIt() {
			if (DispatcherBytecodeAlgorithm.this.algo_DUPX_Y == null) {
				DispatcherBytecodeAlgorithm.this.algo_DUPX_Y = new Algo_DUPX_Y();
			}
			DispatcherBytecodeAlgorithm.this.algo_DUPX_Y.cat1 = this.cat1;
			DispatcherBytecodeAlgorithm.this.algo_DUPX_Y.x1 = this.x1;
			return DispatcherBytecodeAlgorithm.this.algo_DUPX_Y;
		}
	}

	private class DispatchStrategy_SWAP implements Dispatcher.DispatchStrategy<Algo_SWAP> {
		public Algo_SWAP doIt() {
			if (DispatcherBytecodeAlgorithm.this.algo_SWAP == null) {
				DispatcherBytecodeAlgorithm.this.algo_SWAP = new Algo_SWAP();
			}
			return DispatcherBytecodeAlgorithm.this.algo_SWAP;
		}
	}

	private class DispatchStrategy_IFX implements Dispatcher.DispatchStrategy<Algo_IFX> {
		private boolean compareWithZero;
		private Operator operator;
		public DispatchStrategy_IFX(boolean compareWithZero, Operator operator) {
			this.compareWithZero = compareWithZero;
			this.operator = operator;
		}
		public Algo_IFX doIt() {
			if (DispatcherBytecodeAlgorithm.this.algo_IFX == null) {
				DispatcherBytecodeAlgorithm.this.algo_IFX = new Algo_IFX();
			}
			DispatcherBytecodeAlgorithm.this.algo_IFX.compareWithZero = this.compareWithZero;
			DispatcherBytecodeAlgorithm.this.algo_IFX.operator = this.operator;
			return DispatcherBytecodeAlgorithm.this.algo_IFX;
		}
	}

	private class DispatchStrategy_IF_ACMPX_XNULL implements Dispatcher.DispatchStrategy<Algo_IF_ACMPX_XNULL> {
		private boolean compareWithNull;
		private boolean eq;
		public DispatchStrategy_IF_ACMPX_XNULL(boolean compareWithNull, boolean eq) {
			this.compareWithNull = compareWithNull;
			this.eq = eq;
		}
		public Algo_IF_ACMPX_XNULL doIt() {
			if (DispatcherBytecodeAlgorithm.this.algo_IF_ACMPX_XNULL == null) {
				DispatcherBytecodeAlgorithm.this.algo_IF_ACMPX_XNULL = new Algo_IF_ACMPX_XNULL();
			}
			DispatcherBytecodeAlgorithm.this.algo_IF_ACMPX_XNULL.compareWithNull = this.compareWithNull;
			DispatcherBytecodeAlgorithm.this.algo_IF_ACMPX_XNULL.eq = this.eq;
			return DispatcherBytecodeAlgorithm.this.algo_IF_ACMPX_XNULL;
		}
	}

	private class DispatchStrategy_XSWITCH implements Dispatcher.DispatchStrategy<Algo_XSWITCH> {
		private boolean isTableSwitch;
		public DispatchStrategy_XSWITCH(boolean isTableSwitch) {
			this.isTableSwitch = isTableSwitch;
		}
		public Algo_XSWITCH doIt() {
			if (DispatcherBytecodeAlgorithm.this.algo_XSWITCH == null) {
				DispatcherBytecodeAlgorithm.this.algo_XSWITCH = new Algo_XSWITCH();
			}
			DispatcherBytecodeAlgorithm.this.algo_XSWITCH.isTableSwitch = this.isTableSwitch;
			return DispatcherBytecodeAlgorithm.this.algo_XSWITCH;
		}
	}

	private class DispatchStrategy_GOTOX implements Dispatcher.DispatchStrategy<Algo_GOTOX> {
		private boolean wide;
		public DispatchStrategy_GOTOX(boolean wide) {
			this.wide = wide;
		}
		public Algo_GOTOX doIt() {
			if (DispatcherBytecodeAlgorithm.this.algo_GOTOX == null) {
				DispatcherBytecodeAlgorithm.this.algo_GOTOX = new Algo_GOTOX();
			}
			DispatcherBytecodeAlgorithm.this.algo_GOTOX.wide = this.wide;
			return DispatcherBytecodeAlgorithm.this.algo_GOTOX;
		}
	}

	private class DispatchStrategy_JSRX implements Dispatcher.DispatchStrategy<Algo_JSRX> {
		private boolean wide;
		public DispatchStrategy_JSRX(boolean wide) {
			this.wide = wide;
		}
		public Algo_JSRX doIt() {
			if (DispatcherBytecodeAlgorithm.this.algo_JSRX == null) {
				DispatcherBytecodeAlgorithm.this.algo_JSRX = new Algo_JSRX();
			}
			DispatcherBytecodeAlgorithm.this.algo_JSRX.wide = this.wide;
			return DispatcherBytecodeAlgorithm.this.algo_JSRX;
		}
	}

	private class DispatchStrategy_RET implements Dispatcher.DispatchStrategy<Algo_RET> {
		public Algo_RET doIt() {
			if (DispatcherBytecodeAlgorithm.this.algo_RET == null) {
				DispatcherBytecodeAlgorithm.this.algo_RET = new Algo_RET();
			}
			return DispatcherBytecodeAlgorithm.this.algo_RET;
		}
	}

    private class DispatchStrategy_INVOKEX implements Dispatcher.DispatchStrategy<Algo_INVOKEX> {
        private boolean isInterface;
        private boolean isSpecial;
        private boolean isStatic;
        public DispatchStrategy_INVOKEX(boolean isInterface, boolean isSpecial, boolean isStatic) {
            this.isInterface = isInterface;
            this.isSpecial = isSpecial;
            this.isStatic = isStatic;
        }
        public Algo_INVOKEX doIt() {
            if (DispatcherBytecodeAlgorithm.this.algo_INVOKEX == null) {
                DispatcherBytecodeAlgorithm.this.algo_INVOKEX = new Algo_INVOKEX();
            }
            DispatcherBytecodeAlgorithm.this.algo_INVOKEX.isInterface = this.isInterface;
            DispatcherBytecodeAlgorithm.this.algo_INVOKEX.isSpecial = this.isSpecial;
            DispatcherBytecodeAlgorithm.this.algo_INVOKEX.isStatic = this.isStatic;
            return DispatcherBytecodeAlgorithm.this.algo_INVOKEX;
        }
    }

	private class DispatchStrategy_XRETURN implements Dispatcher.DispatchStrategy<Algo_XRETURN> {
		private boolean returnVoid;
		public DispatchStrategy_XRETURN(boolean returnVoid) {
			this.returnVoid = returnVoid;
		}
		public Algo_XRETURN doIt() {
			if (DispatcherBytecodeAlgorithm.this.algo_XRETURN == null) {
				DispatcherBytecodeAlgorithm.this.algo_XRETURN = new Algo_XRETURN();
			}
			DispatcherBytecodeAlgorithm.this.algo_XRETURN.returnVoid = this.returnVoid;
			return DispatcherBytecodeAlgorithm.this.algo_XRETURN;
		}
	}

	private class DispatchStrategy_ATHROW implements Dispatcher.DispatchStrategy<Algo_ATHROW> {
		public Algo_ATHROW doIt() {
			if (DispatcherBytecodeAlgorithm.this.algo_ATHROW == null) {
				DispatcherBytecodeAlgorithm.this.algo_ATHROW = new Algo_ATHROW();
			}
			return DispatcherBytecodeAlgorithm.this.algo_ATHROW;
		}
	}

	private class DispatchStrategy_WIDE implements Dispatcher.DispatchStrategy<Algo_WIDE> {
		public Algo_WIDE doIt() {
			if (DispatcherBytecodeAlgorithm.this.algo_WIDE == null) {
				DispatcherBytecodeAlgorithm.this.algo_WIDE = new Algo_WIDE();
			}
			return DispatcherBytecodeAlgorithm.this.algo_WIDE;
		}
	}

	private class DispatchStrategy_MONITORX implements Dispatcher.DispatchStrategy<Algo_MONITORX> {
		public Algo_MONITORX doIt() {
			if (DispatcherBytecodeAlgorithm.this.algo_MONITORX == null) {
				DispatcherBytecodeAlgorithm.this.algo_MONITORX = new Algo_MONITORX();
			}
			return DispatcherBytecodeAlgorithm.this.algo_MONITORX;
		}
	}

	private class DispatchStrategy_NOTALLOWED implements Dispatcher.DispatchStrategy<Algo_NOTALLOWED> {
		public Algo_NOTALLOWED doIt() {
			if (DispatcherBytecodeAlgorithm.this.algo_NOTALLOWED == null) {
				DispatcherBytecodeAlgorithm.this.algo_NOTALLOWED = new Algo_NOTALLOWED();
			}
			return DispatcherBytecodeAlgorithm.this.algo_NOTALLOWED;
		}
	}

	private static class DispatchStrategy_NOTYETIMPLEMENTED implements Dispatcher.DispatchStrategy<Algorithm> {
		private String bcName;
		public DispatchStrategy_NOTYETIMPLEMENTED(String bcName) { this.bcName = bcName; }
		public Algorithm doIt() throws NotYetImplementedException {
        	throw new NotYetImplementedException(this.bcName);
		}
	}

	public DispatcherBytecodeAlgorithm() {
		//implemented bytecodes (sometimes with limited support)
		setCase(OP_NOP,             new DispatchStrategy_NOP());
        setCase(OP_ACONST_NULL,     new DispatchStrategy_ACONST_NULL());
        setCase(OP_ICONST_M1,       new DispatchStrategy_XCONST_Y(Type.INT, -1));
        setCase(OP_ICONST_0,        new DispatchStrategy_XCONST_Y(Type.INT, 0));
        setCase(OP_ICONST_1,        new DispatchStrategy_XCONST_Y(Type.INT, 1));
        setCase(OP_ICONST_2,        new DispatchStrategy_XCONST_Y(Type.INT, 2));
        setCase(OP_ICONST_3,        new DispatchStrategy_XCONST_Y(Type.INT, 3));
        setCase(OP_ICONST_4,        new DispatchStrategy_XCONST_Y(Type.INT, 4));
        setCase(OP_ICONST_5,        new DispatchStrategy_XCONST_Y(Type.INT, 5));
        setCase(OP_LCONST_0,        new DispatchStrategy_XCONST_Y(Type.LONG, 0));
        setCase(OP_LCONST_1,        new DispatchStrategy_XCONST_Y(Type.LONG, 1));
        setCase(OP_FCONST_0,        new DispatchStrategy_XCONST_Y(Type.FLOAT, 0));
        setCase(OP_FCONST_1,        new DispatchStrategy_XCONST_Y(Type.FLOAT, 1));
        setCase(OP_FCONST_2,        new DispatchStrategy_XCONST_Y(Type.FLOAT, 2));
        setCase(OP_DCONST_0,        new DispatchStrategy_XCONST_Y(Type.DOUBLE, 0));
        setCase(OP_DCONST_1,        new DispatchStrategy_XCONST_Y(Type.DOUBLE, 1));
        setCase(OP_BIPUSH,          new DispatchStrategy_BIPUSH());
        setCase(OP_SIPUSH,          new DispatchStrategy_SIPUSH());
        setCase(OP_LDC,             new DispatchStrategy_LDCX_Y(false, true));
        setCase(OP_LDC_W,           new DispatchStrategy_LDCX_Y(true, true));
        setCase(OP_LDC2_W,          new DispatchStrategy_LDCX_Y(true, false));
		setCase(OP_ILOAD,           new DispatchStrategy_XLOAD());
		setCase(OP_LLOAD,           new DispatchStrategy_XLOAD());
        setCase(OP_FLOAD,           new DispatchStrategy_XLOAD());
        setCase(OP_DLOAD,           new DispatchStrategy_XLOAD());
        setCase(OP_ALOAD,           new DispatchStrategy_XLOAD());
        setCase(OP_ILOAD_0,         new DispatchStrategy_XLOAD(0));
        setCase(OP_ILOAD_1,         new DispatchStrategy_XLOAD(1));
        setCase(OP_ILOAD_2,         new DispatchStrategy_XLOAD(2));
        setCase(OP_ILOAD_3,         new DispatchStrategy_XLOAD(3));
        setCase(OP_LLOAD_0,         new DispatchStrategy_XLOAD(0));
        setCase(OP_LLOAD_1,         new DispatchStrategy_XLOAD(1));
        setCase(OP_LLOAD_2,         new DispatchStrategy_XLOAD(2));
        setCase(OP_LLOAD_3,         new DispatchStrategy_XLOAD(3));
		setCase(OP_FLOAD_0,         new DispatchStrategy_XLOAD(0));
        setCase(OP_FLOAD_1,         new DispatchStrategy_XLOAD(1));
        setCase(OP_FLOAD_2,         new DispatchStrategy_XLOAD(2));
        setCase(OP_FLOAD_3,         new DispatchStrategy_XLOAD(3));
        setCase(OP_DLOAD_0,         new DispatchStrategy_XLOAD(0));
		setCase(OP_DLOAD_1,         new DispatchStrategy_XLOAD(1));
		setCase(OP_DLOAD_2,         new DispatchStrategy_XLOAD(2));
        setCase(OP_DLOAD_3,         new DispatchStrategy_XLOAD(3));
        setCase(OP_ALOAD_0,         new DispatchStrategy_XLOAD(0));
        setCase(OP_ALOAD_1,         new DispatchStrategy_XLOAD(1));
        setCase(OP_ALOAD_2,         new DispatchStrategy_XLOAD(2));
		setCase(OP_ALOAD_3,         new DispatchStrategy_XLOAD(3));
        setCase(OP_IALOAD,          new DispatchStrategy_XALOAD());
        setCase(OP_LALOAD,          new DispatchStrategy_XALOAD());
        setCase(OP_FALOAD,          new DispatchStrategy_XALOAD());
        setCase(OP_DALOAD,          new DispatchStrategy_XALOAD());
        setCase(OP_AALOAD,          new DispatchStrategy_XALOAD());
        setCase(OP_BALOAD,          new DispatchStrategy_XALOAD());
        setCase(OP_CALOAD,          new DispatchStrategy_XALOAD());
        setCase(OP_SALOAD,          new DispatchStrategy_XALOAD());
        setCase(OP_ISTORE,          new DispatchStrategy_XSTORE());
        setCase(OP_LSTORE,          new DispatchStrategy_XSTORE());
        setCase(OP_FSTORE,          new DispatchStrategy_XSTORE());
		setCase(OP_DSTORE,          new DispatchStrategy_XSTORE());
        setCase(OP_ASTORE,          new DispatchStrategy_XSTORE());
        setCase(OP_ISTORE_0,        new DispatchStrategy_XSTORE(0));
        setCase(OP_ISTORE_1,        new DispatchStrategy_XSTORE(1));
        setCase(OP_ISTORE_2,        new DispatchStrategy_XSTORE(2));
        setCase(OP_ISTORE_3,        new DispatchStrategy_XSTORE(3));
		setCase(OP_LSTORE_0,        new DispatchStrategy_XSTORE(0));
        setCase(OP_LSTORE_1,        new DispatchStrategy_XSTORE(1));
        setCase(OP_LSTORE_2,        new DispatchStrategy_XSTORE(2));
        setCase(OP_LSTORE_3,        new DispatchStrategy_XSTORE(3));
        setCase(OP_FSTORE_0,        new DispatchStrategy_XSTORE(0));
        setCase(OP_FSTORE_1,        new DispatchStrategy_XSTORE(1));
        setCase(OP_FSTORE_2,        new DispatchStrategy_XSTORE(2));
        setCase(OP_FSTORE_3,        new DispatchStrategy_XSTORE(3));
        setCase(OP_DSTORE_0,        new DispatchStrategy_XSTORE(0));
        setCase(OP_DSTORE_1,        new DispatchStrategy_XSTORE(1));
        setCase(OP_DSTORE_2,        new DispatchStrategy_XSTORE(2));
        setCase(OP_DSTORE_3,        new DispatchStrategy_XSTORE(3));
        setCase(OP_ASTORE_0,        new DispatchStrategy_XSTORE(0));
		setCase(OP_ASTORE_1,        new DispatchStrategy_XSTORE(1));
		setCase(OP_ASTORE_2,        new DispatchStrategy_XSTORE(2));
		setCase(OP_ASTORE_3,        new DispatchStrategy_XSTORE(3));
        setCase(OP_IASTORE,         new DispatchStrategy_XASTORE());
        setCase(OP_LASTORE,         new DispatchStrategy_XASTORE());
        setCase(OP_FASTORE,         new DispatchStrategy_XASTORE());
        setCase(OP_DASTORE,         new DispatchStrategy_XASTORE());
        setCase(OP_AASTORE,         new DispatchStrategy_XASTORE());
        setCase(OP_BASTORE,         new DispatchStrategy_XASTORE());
        setCase(OP_CASTORE,         new DispatchStrategy_XASTORE());
        setCase(OP_SASTORE,         new DispatchStrategy_XASTORE());
        setCase(OP_POP,             new DispatchStrategy_POPX(true));
        setCase(OP_POP2,            new DispatchStrategy_POPX(false));
        setCase(OP_DUP,             new DispatchStrategy_DUPX(true));
        setCase(OP_DUP_X1,          new DispatchStrategy_DUPX_Y(true, true));
        setCase(OP_DUP_X2,          new DispatchStrategy_DUPX_Y(true, false));
        setCase(OP_DUP2,            new DispatchStrategy_DUPX(false));
        setCase(OP_DUP2_X1,         new DispatchStrategy_DUPX_Y(false, true));
        setCase(OP_DUP2_X2,         new DispatchStrategy_DUPX_Y(false, false));
        setCase(OP_SWAP,            new DispatchStrategy_SWAP());
        setCase(OP_IADD,            new DispatchStrategy_XBINOP(Operator.ADD));
        setCase(OP_LADD,            new DispatchStrategy_XBINOP(Operator.ADD));
        setCase(OP_FADD,            new DispatchStrategy_XBINOP(Operator.ADD));
		setCase(OP_DADD,            new DispatchStrategy_XBINOP(Operator.ADD));
        setCase(OP_ISUB,            new DispatchStrategy_XBINOP(Operator.SUB));
        setCase(OP_LSUB,            new DispatchStrategy_XBINOP(Operator.SUB));
        setCase(OP_FSUB,            new DispatchStrategy_XBINOP(Operator.SUB));
		setCase(OP_DSUB,            new DispatchStrategy_XBINOP(Operator.SUB));
        setCase(OP_IMUL,            new DispatchStrategy_XBINOP(Operator.MUL));
        setCase(OP_LMUL,            new DispatchStrategy_XBINOP(Operator.MUL));
		setCase(OP_FMUL,            new DispatchStrategy_XBINOP(Operator.MUL));
        setCase(OP_DMUL,            new DispatchStrategy_XBINOP(Operator.MUL));
        setCase(OP_IDIV,            new DispatchStrategy_XBINOP(Operator.DIV));
        setCase(OP_LDIV,            new DispatchStrategy_XBINOP(Operator.DIV));
		setCase(OP_FDIV,            new DispatchStrategy_XBINOP(Operator.DIV));
        setCase(OP_DDIV,            new DispatchStrategy_XBINOP(Operator.DIV));
        setCase(OP_IREM,            new DispatchStrategy_XBINOP(Operator.REM));
        setCase(OP_LREM,            new DispatchStrategy_XBINOP(Operator.REM));
        setCase(OP_FREM,            new DispatchStrategy_XBINOP(Operator.REM));
		setCase(OP_DREM,            new DispatchStrategy_XBINOP(Operator.REM));
		setCase(OP_INEG,            new DispatchStrategy_XNEG());
        setCase(OP_LNEG,            new DispatchStrategy_XNEG());
        setCase(OP_FNEG,            new DispatchStrategy_XNEG());
		setCase(OP_DNEG,            new DispatchStrategy_XNEG());
        setCase(OP_ISHL,            new DispatchStrategy_XBINOP(Operator.SHL));
		setCase(OP_LSHL,            new DispatchStrategy_XBINOP(Operator.SHL));
		setCase(OP_ISHR,            new DispatchStrategy_XBINOP(Operator.SHR));
		setCase(OP_LSHR,            new DispatchStrategy_XBINOP(Operator.SHR));
		setCase(OP_IUSHR,           new DispatchStrategy_XBINOP(Operator.USHR));
		setCase(OP_LUSHR,           new DispatchStrategy_XBINOP(Operator.USHR));
        setCase(OP_IAND,            new DispatchStrategy_XBINOP(Operator.ANDBW));
        setCase(OP_LAND,            new DispatchStrategy_XBINOP(Operator.ANDBW));
		setCase(OP_IOR,             new DispatchStrategy_XBINOP(Operator.ORBW));
		setCase(OP_LOR,             new DispatchStrategy_XBINOP(Operator.ORBW));
		setCase(OP_IXOR,            new DispatchStrategy_XBINOP(Operator.XORBW));
		setCase(OP_LXOR,            new DispatchStrategy_XBINOP(Operator.XORBW));
		setCase(OP_IINC,            new DispatchStrategy_IINC());
        setCase(OP_I2L,             new DispatchStrategy_X2Y(Type.INT, Type.LONG));
        setCase(OP_I2F,             new DispatchStrategy_X2Y(Type.INT, Type.FLOAT));
		setCase(OP_I2D,             new DispatchStrategy_X2Y(Type.INT, Type.DOUBLE));
        setCase(OP_L2I,             new DispatchStrategy_X2Y(Type.LONG, Type.INT));
		setCase(OP_L2F,             new DispatchStrategy_X2Y(Type.LONG, Type.FLOAT));
        setCase(OP_L2D,             new DispatchStrategy_X2Y(Type.LONG, Type.DOUBLE));
        setCase(OP_F2I,             new DispatchStrategy_X2Y(Type.FLOAT, Type.INT));
        setCase(OP_F2L,             new DispatchStrategy_X2Y(Type.FLOAT, Type.LONG));
		setCase(OP_F2D,             new DispatchStrategy_X2Y(Type.FLOAT, Type.DOUBLE));
        setCase(OP_D2I,             new DispatchStrategy_X2Y(Type.DOUBLE, Type.INT));
        setCase(OP_D2L,             new DispatchStrategy_X2Y(Type.DOUBLE, Type.LONG));
		setCase(OP_D2F,             new DispatchStrategy_X2Y(Type.DOUBLE, Type.FLOAT));
		setCase(OP_I2B,             new DispatchStrategy_X2Y(Type.INT, Type.BYTE));
		setCase(OP_I2C,             new DispatchStrategy_X2Y(Type.INT, Type.CHAR));
		setCase(OP_I2S,             new DispatchStrategy_X2Y(Type.INT, Type.SHORT));
        setCase(OP_LCMP,            new DispatchStrategy_XCMPY());
        setCase(OP_FCMPL,           new DispatchStrategy_XCMPY());
        setCase(OP_FCMPG,           new DispatchStrategy_XCMPY());
        setCase(OP_DCMPL,           new DispatchStrategy_XCMPY());
        setCase(OP_DCMPG,           new DispatchStrategy_XCMPY());
		setCase(OP_IFEQ,            new DispatchStrategy_IFX(true, Operator.EQ));
        setCase(OP_IFNE,            new DispatchStrategy_IFX(true, Operator.NE));
        setCase(OP_IFLT,            new DispatchStrategy_IFX(true, Operator.LT));
		setCase(OP_IFGE,            new DispatchStrategy_IFX(true, Operator.GE));
		setCase(OP_IFGT,            new DispatchStrategy_IFX(true, Operator.GT));
        setCase(OP_IFLE,            new DispatchStrategy_IFX(true, Operator.LE));
		setCase(OP_IF_ICMPEQ,       new DispatchStrategy_IFX(false, Operator.EQ));
        setCase(OP_IF_ICMPNE,       new DispatchStrategy_IFX(false, Operator.NE));
        setCase(OP_IF_ICMPLT,       new DispatchStrategy_IFX(false, Operator.LT));
		setCase(OP_IF_ICMPGE,       new DispatchStrategy_IFX(false, Operator.GE));
		setCase(OP_IF_ICMPGT,       new DispatchStrategy_IFX(false, Operator.GT));
		setCase(OP_IF_ICMPLE,       new DispatchStrategy_IFX(false, Operator.LE));
		setCase(OP_IF_ACMPEQ,       new DispatchStrategy_IF_ACMPX_XNULL(false, true));
		setCase(OP_IF_ACMPNE,       new DispatchStrategy_IF_ACMPX_XNULL(false, false));
		setCase(OP_GOTO,            new DispatchStrategy_GOTOX(false));
		setCase(OP_JSR,             new DispatchStrategy_JSRX(false));
		setCase(OP_RET,             new DispatchStrategy_RET());
        setCase(OP_TABLESWITCH,     new DispatchStrategy_XSWITCH(true));
        setCase(OP_LOOKUPSWITCH,    new DispatchStrategy_XSWITCH(false));
        setCase(OP_IRETURN,         new DispatchStrategy_XRETURN(false));
        setCase(OP_LRETURN,         new DispatchStrategy_XRETURN(false));
        setCase(OP_FRETURN,         new DispatchStrategy_XRETURN(false));
        setCase(OP_DRETURN,         new DispatchStrategy_XRETURN(false));
		setCase(OP_ARETURN,         new DispatchStrategy_XRETURN(false));
        setCase(OP_RETURN,          new DispatchStrategy_XRETURN(true));
        setCase(OP_GETSTATIC,       new DispatchStrategy_GETSTATIC());
        setCase(OP_PUTSTATIC,       new DispatchStrategy_PUTSTATIC());
        setCase(OP_GETFIELD,        new DispatchStrategy_GETFIELD());
        setCase(OP_PUTFIELD,        new DispatchStrategy_PUTFIELD());
        setCase(OP_INVOKEVIRTUAL,   new DispatchStrategy_INVOKEX(false, false, false));
        setCase(OP_INVOKESPECIAL,   new DispatchStrategy_INVOKEX(false, true, false));
        setCase(OP_INVOKESTATIC,    new DispatchStrategy_INVOKEX(false, false, true));
        setCase(OP_INVOKEINTERFACE, new DispatchStrategy_INVOKEX(true, false, false));
        setCase(OP_INVOKEDYNAMIC,   new DispatchStrategy_NOTYETIMPLEMENTED("INVOKEDYNAMIC"));
        setCase(OP_NEW,             new DispatchStrategy_NEW());
        setCase(OP_NEWARRAY,        new DispatchStrategy_NEWARRAY());
        setCase(OP_ANEWARRAY,       new DispatchStrategy_ANEWARRAY());
        setCase(OP_ARRAYLENGTH,     new DispatchStrategy_ARRAYLENGTH());
        setCase(OP_ATHROW,          new DispatchStrategy_ATHROW());
        setCase(OP_CHECKCAST,       new DispatchStrategy_CHECKCAST());
        setCase(OP_INSTANCEOF,      new DispatchStrategy_INSTANCEOF());
        setCase(OP_MONITORENTER,    new DispatchStrategy_MONITORX());
        setCase(OP_MONITOREXIT,     new DispatchStrategy_MONITORX());
		setCase(OP_WIDE,            new DispatchStrategy_WIDE());
        setCase(OP_MULTIANEWARRAY,  new DispatchStrategy_MULTIANEWARRAY());
        setCase(OP_IFNULL,          new DispatchStrategy_IF_ACMPX_XNULL(true, true));
        setCase(OP_IFNONNULL,       new DispatchStrategy_IF_ACMPX_XNULL(true, false));
        setCase(OP_GOTO_W,          new DispatchStrategy_GOTOX(true));
        setCase(OP_JSR_W,           new DispatchStrategy_JSRX(true));
        setCase(OP_BREAKPOINT,      new DispatchStrategy_NOP());
		setCase(OP_IMPDEP1,         new DispatchStrategy_NOP());
		setCase(OP_IMPDEP2,         new DispatchStrategy_NOP());
	
        //the remaining bytecodes should never appear in a classfile
        setDefault(new DispatchStrategy_NOTALLOWED());
    }
	
    public Algo_INIT select() {
		if (this.seInit == null) {
			this.seInit = new Algo_INIT();
		}
		return this.seInit;
    }
		
	@Override
	public Algorithm select(Byte bytecode) 
	throws NotYetImplementedException {
		final Algorithm retVal;
        try {
            retVal = super.select(bytecode);
        } catch (NotYetImplementedException | RuntimeException e) {
            throw e;
        } catch (Exception e) {
            //this should never happen
            throw new UnexpectedInternalException(e);
        }
		return retVal;
	}
}
