package jbse.algo;

import static jbse.bc.Opcodes.*;

import jbse.algo.exc.UndefInstructionException;
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
    private SEInit seInit = null;
    private SEUnexpected seUnexpected = null;
    private SEAconst_null seAconst_null = null;
    private SEAload seAload = null;
    private SEAnewarray seAnewarray = null;
    private SEArrayLength seArrayLength = null;
    private SEAstore seAstore = null;
    private SEAthrow seAthrow = null;
	private SEBinOp seBinOp = null;
    private SEBipush seBipush = null;
    private SECheckcast seCheckcast = null;
    private SEConst seConst = null;
    private SEDup_n seDup_n = null;
    private SEDup seDup = null;
    private SEGetfield seGetfield = null;
    private SEGetstatic seGetstatic = null;
    private SEGoto seGoto = null;
    private SEIfacmp seIfacmp = null;
    private SEIfcond seIfcond = null;
    private SEIinc seIinc = null;
    private SEInstanceof seInstanceof = null;
    private SEInvokeSpecial seInvokeSpecial = null;
    private SEInvokeStatic seInvokeStatic = null;
    private SEInvokeVirtualInterface seInvokeVirtualInterface = null;
    private SEJsr seJsr = null;
    private SELdc seLdc = null;
    private SELoad seLoad = null;
    private SEMonitor seMonitor = null;
    private SEMultianewarray seMultianewarray = null;
    private SEN2n seN2n = null;
    private SENcmp seNcmp = null;
    private SENeg seNeg = null;
    private SENew seNew = null;
    private SENewarray seNewarray = null;
    private SENop seNop = null;
    private SEPop sePop = null;
    private SEPutfield sePutfield = null;
    private SEPutstatic sePutstatic = null;
    private SERet seRet = null;
    private SEReturn seReturn = null;
    private SESipush seSipush = null;
    private SEStore seStore = null;
    private SESwap seSwap = null;
    private SESwitch seSwitch = null;
    private SEWide seWide = null;

	private class DispatchStrategy_NOP implements Dispatcher.DispatchStrategy<SENop> {
		public SENop doIt() {
			if (DispatcherBytecodeAlgorithm.this.seNop == null) {
				DispatcherBytecodeAlgorithm.this.seNop = new SENop();
			}
			return DispatcherBytecodeAlgorithm.this.seNop;
		}
	}
	
	private class DispatchStrategy_XLOAD implements Dispatcher.DispatchStrategy<SELoad> {
		private boolean def;
		private int index;
		public DispatchStrategy_XLOAD() { this.def = false; }
		public DispatchStrategy_XLOAD(int index) { this.def = true; this.index = index; }
		public SELoad doIt() {
			if (DispatcherBytecodeAlgorithm.this.seLoad == null) {
				DispatcherBytecodeAlgorithm.this.seLoad = new SELoad();
			}
			DispatcherBytecodeAlgorithm.this.seLoad.def = this.def;
			DispatcherBytecodeAlgorithm.this.seLoad.index = this.index;
			return DispatcherBytecodeAlgorithm.this.seLoad;
		}
	}
	
	private class DispatchStrategy_XSTORE implements Dispatcher.DispatchStrategy<SEStore> {
		private boolean def;
		private int index;
		public DispatchStrategy_XSTORE() { this.def = false; }
		public DispatchStrategy_XSTORE(int index) { this.def = true; this.index = index; }
		public SEStore doIt() {
			if (DispatcherBytecodeAlgorithm.this.seStore == null) {
				DispatcherBytecodeAlgorithm.this.seStore = new SEStore();
			}
			DispatcherBytecodeAlgorithm.this.seStore.def = this.def;
			DispatcherBytecodeAlgorithm.this.seStore.index = this.index;
			return DispatcherBytecodeAlgorithm.this.seStore;
		}
	}
	
	private class DispatchStrategy_BIPUSH implements Dispatcher.DispatchStrategy<SEBipush> {
		public SEBipush doIt() {
			if (DispatcherBytecodeAlgorithm.this.seBipush == null) {
				DispatcherBytecodeAlgorithm.this.seBipush = new SEBipush();
			}
			return DispatcherBytecodeAlgorithm.this.seBipush;
		}
	}

	private class DispatchStrategy_SIPUSH implements Dispatcher.DispatchStrategy<SESipush> {
		public SESipush doIt() {
			if (DispatcherBytecodeAlgorithm.this.seSipush == null) {
				DispatcherBytecodeAlgorithm.this.seSipush = new SESipush();
			}
			return DispatcherBytecodeAlgorithm.this.seSipush;
		}
	}

	private class DispatchStrategy_LDCX_Y implements Dispatcher.DispatchStrategy<SELdc> {
		private boolean wide;
        private boolean cat1;
		public DispatchStrategy_LDCX_Y(boolean wide, boolean cat1) {
			this.wide = wide;
			this.cat1 = cat1;
		}
		public SELdc doIt() {
			if (DispatcherBytecodeAlgorithm.this.seLdc == null) {
				DispatcherBytecodeAlgorithm.this.seLdc = new SELdc();
			}
			DispatcherBytecodeAlgorithm.this.seLdc.wide = this.wide;
            DispatcherBytecodeAlgorithm.this.seLdc.cat1 = this.cat1;
			return DispatcherBytecodeAlgorithm.this.seLdc;
		}
	}

	private class DispatchStrategy_ACONST_NULL implements Dispatcher.DispatchStrategy<SEAconst_null> {
		public SEAconst_null doIt() {
			if (DispatcherBytecodeAlgorithm.this.seAconst_null == null) {
				DispatcherBytecodeAlgorithm.this.seAconst_null = new SEAconst_null();
			}
			return DispatcherBytecodeAlgorithm.this.seAconst_null;
		}
	}

	private class DispatchStrategy_XCONST_Y implements Dispatcher.DispatchStrategy<SEConst> {
		private char type;
		private int val;
		public DispatchStrategy_XCONST_Y(char type, int val) {
			this.type = type;
			this.val = val;
		}
		public SEConst doIt() {
			if (DispatcherBytecodeAlgorithm.this.seConst == null) {
				DispatcherBytecodeAlgorithm.this.seConst = new SEConst();
			}
			DispatcherBytecodeAlgorithm.this.seConst.type = this.type;
			DispatcherBytecodeAlgorithm.this.seConst.val = this.val;
			return DispatcherBytecodeAlgorithm.this.seConst;
		}
	}

	private class DispatchStrategy_XBINOP implements Dispatcher.DispatchStrategy<SEBinOp> {
		private Operator op;
		public DispatchStrategy_XBINOP(Operator op) {
			this.op = op;
		}
		public SEBinOp doIt() {
			if (DispatcherBytecodeAlgorithm.this.seBinOp  == null) {
				DispatcherBytecodeAlgorithm.this.seBinOp = new SEBinOp();
			}
			DispatcherBytecodeAlgorithm.this.seBinOp.op = op;
			return DispatcherBytecodeAlgorithm.this.seBinOp;
		}
	}

	private class DispatchStrategy_XNEG implements Dispatcher.DispatchStrategy<SENeg> {
		public SENeg doIt() {
			if (DispatcherBytecodeAlgorithm.this.seNeg == null) {
				DispatcherBytecodeAlgorithm.this.seNeg = new SENeg();
			}
			return DispatcherBytecodeAlgorithm.this.seNeg;
		}
	}

	private class DispatchStrategy_IINC implements Dispatcher.DispatchStrategy<SEIinc> {
		public SEIinc doIt() {
			if (DispatcherBytecodeAlgorithm.this.seIinc == null) {
				DispatcherBytecodeAlgorithm.this.seIinc = new SEIinc();
			}
			return DispatcherBytecodeAlgorithm.this.seIinc;
		}
	}

	private class DispatchStrategy_XCMPY implements Dispatcher.DispatchStrategy<SENcmp> {
		public SENcmp doIt() {
			if (DispatcherBytecodeAlgorithm.this.seNcmp == null) {
				DispatcherBytecodeAlgorithm.this.seNcmp = new SENcmp();
			}
			return DispatcherBytecodeAlgorithm.this.seNcmp;
		}
	}

	private class DispatchStrategy_X2Y implements Dispatcher.DispatchStrategy<SEN2n> {
		private char type;
		private char castType;
		public DispatchStrategy_X2Y(char type, char castType) {
			this.type = type;
			this.castType = castType;
		}
		public SEN2n doIt() {
			if (DispatcherBytecodeAlgorithm.this.seN2n == null) {
				DispatcherBytecodeAlgorithm.this.seN2n = new SEN2n();
			}
			DispatcherBytecodeAlgorithm.this.seN2n.type = this.type;
			DispatcherBytecodeAlgorithm.this.seN2n.castType = this.castType;
			return DispatcherBytecodeAlgorithm.this.seN2n;
		}
	}

	private class DispatchStrategy_NEW implements Dispatcher.DispatchStrategy<SENew> {
		public SENew doIt() {
			if (DispatcherBytecodeAlgorithm.this.seNew == null) {
				DispatcherBytecodeAlgorithm.this.seNew = new SENew();
			}
			return DispatcherBytecodeAlgorithm.this.seNew;
		}
	}

	private class DispatchStrategy_NEWARRAY implements Dispatcher.DispatchStrategy<SENewarray> {
		public SENewarray doIt() {
			if (DispatcherBytecodeAlgorithm.this.seNewarray == null) {
				DispatcherBytecodeAlgorithm.this.seNewarray = new SENewarray();
			}
			return DispatcherBytecodeAlgorithm.this.seNewarray;
		}
	}

	private class DispatchStrategy_ANEWARRAY implements Dispatcher.DispatchStrategy<SEAnewarray> {
		public SEAnewarray doIt() {
			if (DispatcherBytecodeAlgorithm.this.seAnewarray == null) {
				DispatcherBytecodeAlgorithm.this.seAnewarray = new SEAnewarray();
			}
			return DispatcherBytecodeAlgorithm.this.seAnewarray;
		}
	}

	private class DispatchStrategy_MULTIANEWARRAY implements Dispatcher.DispatchStrategy<SEMultianewarray> {
		public SEMultianewarray doIt() {
			if (DispatcherBytecodeAlgorithm.this.seMultianewarray == null) {
				DispatcherBytecodeAlgorithm.this.seMultianewarray = new SEMultianewarray();
			}
			return DispatcherBytecodeAlgorithm.this.seMultianewarray;
		}
	}

	private class DispatchStrategy_GETFIELD implements Dispatcher.DispatchStrategy<SEGetfield> {
		public SEGetfield doIt() {
			if (DispatcherBytecodeAlgorithm.this.seGetfield == null) {
				DispatcherBytecodeAlgorithm.this.seGetfield = new SEGetfield();
			}
			return DispatcherBytecodeAlgorithm.this.seGetfield;
		}
	}

	private class DispatchStrategy_PUTFIELD implements Dispatcher.DispatchStrategy<SEPutfield> {
		public SEPutfield doIt() {
			if (DispatcherBytecodeAlgorithm.this.sePutfield == null) {
				DispatcherBytecodeAlgorithm.this.sePutfield = new SEPutfield();
			}
			return DispatcherBytecodeAlgorithm.this.sePutfield;
		}
	}

	private class DispatchStrategy_GETSTATIC implements Dispatcher.DispatchStrategy<SEGetstatic> {
		public SEGetstatic doIt() {
			if (DispatcherBytecodeAlgorithm.this.seGetstatic == null) {
				DispatcherBytecodeAlgorithm.this.seGetstatic = new SEGetstatic();
			}
			return DispatcherBytecodeAlgorithm.this.seGetstatic;
		}
	}

	private class DispatchStrategy_PUTSTATIC implements Dispatcher.DispatchStrategy<SEPutstatic> {
		public SEPutstatic doIt() {
			if (DispatcherBytecodeAlgorithm.this.sePutstatic == null) {
				DispatcherBytecodeAlgorithm.this.sePutstatic = new SEPutstatic();
			}
			return DispatcherBytecodeAlgorithm.this.sePutstatic;
		}
	}

	private class DispatchStrategy_XALOAD implements Dispatcher.DispatchStrategy<SEAload> {
		public SEAload doIt() {
			if (DispatcherBytecodeAlgorithm.this.seAload == null) {
				DispatcherBytecodeAlgorithm.this.seAload = new SEAload();
			}
			return DispatcherBytecodeAlgorithm.this.seAload;
		}
	}

	private class DispatchStrategy_XASTORE implements Dispatcher.DispatchStrategy<SEAstore> {
		public SEAstore doIt() {
			if (DispatcherBytecodeAlgorithm.this.seAstore == null) {
				DispatcherBytecodeAlgorithm.this.seAstore = new SEAstore();
			}
			return DispatcherBytecodeAlgorithm.this.seAstore;
		}
	}

	private class DispatchStrategy_ARRAYLENGTH implements Dispatcher.DispatchStrategy<SEArrayLength> {
		public SEArrayLength doIt() {
			if (DispatcherBytecodeAlgorithm.this.seArrayLength == null) {
				DispatcherBytecodeAlgorithm.this.seArrayLength = new SEArrayLength();
			}
			return DispatcherBytecodeAlgorithm.this.seArrayLength;
		}
	}

	private class DispatchStrategy_INSTANCEOF implements Dispatcher.DispatchStrategy<SEInstanceof> {
		public SEInstanceof doIt() {
			if (DispatcherBytecodeAlgorithm.this.seInstanceof == null) {
				DispatcherBytecodeAlgorithm.this.seInstanceof = new SEInstanceof();
			}
			return DispatcherBytecodeAlgorithm.this.seInstanceof;
		}
	}

	private class DispatchStrategy_CHECKCAST implements Dispatcher.DispatchStrategy<SECheckcast> {
		public SECheckcast doIt() {
			if (DispatcherBytecodeAlgorithm.this.seCheckcast == null) {
				DispatcherBytecodeAlgorithm.this.seCheckcast = new SECheckcast();
			}
			return DispatcherBytecodeAlgorithm.this.seCheckcast;
		}
	}

	private class DispatchStrategy_POPX implements Dispatcher.DispatchStrategy<SEPop> {
		private boolean def;
		public DispatchStrategy_POPX(boolean def) {
			this.def = def;
		}
		public SEPop doIt() {
			if (DispatcherBytecodeAlgorithm.this.sePop == null) {
				DispatcherBytecodeAlgorithm.this.sePop = new SEPop();
			}
			DispatcherBytecodeAlgorithm.this.sePop.def = this.def;
			return DispatcherBytecodeAlgorithm.this.sePop;
		}
	}

	private class DispatchStrategy_DUPX implements Dispatcher.DispatchStrategy<SEDup> {
		private boolean cat_1;
		public DispatchStrategy_DUPX(boolean cat_1) {
			this.cat_1 = cat_1;
		}
		public SEDup doIt() {
			if (DispatcherBytecodeAlgorithm.this.seDup == null) {
				DispatcherBytecodeAlgorithm.this.seDup = new SEDup();
			}
			DispatcherBytecodeAlgorithm.this.seDup.cat_1 = this.cat_1;
			return DispatcherBytecodeAlgorithm.this.seDup;
		}
	}

	private class DispatchStrategy_DUPX_Y implements Dispatcher.DispatchStrategy<SEDup_n> {
		private boolean cat_1;
		private boolean x_1;
		public DispatchStrategy_DUPX_Y(boolean cat_1, boolean x_1) {
			this.cat_1 = cat_1;
			this.x_1 = x_1;
		}
		public SEDup_n doIt() {
			if (DispatcherBytecodeAlgorithm.this.seDup_n == null) {
				DispatcherBytecodeAlgorithm.this.seDup_n = new SEDup_n();
			}
			DispatcherBytecodeAlgorithm.this.seDup_n.cat_1 = this.cat_1;
			DispatcherBytecodeAlgorithm.this.seDup_n.x_1 = this.x_1;
			return DispatcherBytecodeAlgorithm.this.seDup_n;
		}
	}

	private class DispatchStrategy_SWAP implements Dispatcher.DispatchStrategy<SESwap> {
		public SESwap doIt() {
			if (DispatcherBytecodeAlgorithm.this.seSwap == null) {
				DispatcherBytecodeAlgorithm.this.seSwap = new SESwap();
			}
			return DispatcherBytecodeAlgorithm.this.seSwap;
		}
	}

	private class DispatchStrategy_IFX implements Dispatcher.DispatchStrategy<SEIfcond> {
		private boolean def;
		private Operator type;
		public DispatchStrategy_IFX(boolean def, Operator type) {
			this.def = def;
			this.type = type;
		}
		public SEIfcond doIt() {
			if (DispatcherBytecodeAlgorithm.this.seIfcond == null) {
				DispatcherBytecodeAlgorithm.this.seIfcond = new SEIfcond();
			}
			DispatcherBytecodeAlgorithm.this.seIfcond.def = this.def;
			DispatcherBytecodeAlgorithm.this.seIfcond.operator = this.type;
			return DispatcherBytecodeAlgorithm.this.seIfcond;
		}
	}

	private class DispatchStrategy_IF_ACMPX_XNULL implements Dispatcher.DispatchStrategy<SEIfacmp> {
		private boolean compareWithNull;
		private boolean eq;
		public DispatchStrategy_IF_ACMPX_XNULL(boolean compareWithNull, boolean eq) {
			this.compareWithNull = compareWithNull;
			this.eq = eq;
		}
		public SEIfacmp doIt() {
			if (DispatcherBytecodeAlgorithm.this.seIfacmp == null) {
				DispatcherBytecodeAlgorithm.this.seIfacmp = new SEIfacmp();
			}
			DispatcherBytecodeAlgorithm.this.seIfacmp.compareWithNull = this.compareWithNull;
			DispatcherBytecodeAlgorithm.this.seIfacmp.eq = this.eq;
			return DispatcherBytecodeAlgorithm.this.seIfacmp;
		}
	}

	private class DispatchStrategy_XSWITCH implements Dispatcher.DispatchStrategy<SESwitch> {
		private boolean isTableSwitch;
		public DispatchStrategy_XSWITCH(boolean isTableSwitch) {
			this.isTableSwitch = isTableSwitch;
		}
		public SESwitch doIt() {
			if (DispatcherBytecodeAlgorithm.this.seSwitch == null) {
				DispatcherBytecodeAlgorithm.this.seSwitch = new SESwitch();
			}
			DispatcherBytecodeAlgorithm.this.seSwitch.isTableSwitch = this.isTableSwitch;
			return DispatcherBytecodeAlgorithm.this.seSwitch;
		}
	}

	private class DispatchStrategy_GOTOX implements Dispatcher.DispatchStrategy<SEGoto> {
		private boolean def;
		public DispatchStrategy_GOTOX(boolean def) {
			this.def = def;
		}
		public SEGoto doIt() {
			if (DispatcherBytecodeAlgorithm.this.seGoto == null) {
				DispatcherBytecodeAlgorithm.this.seGoto = new SEGoto();
			}
			DispatcherBytecodeAlgorithm.this.seGoto.def = this.def;
			return DispatcherBytecodeAlgorithm.this.seGoto;
		}
	}

	private class DispatchStrategy_JSRX implements Dispatcher.DispatchStrategy<SEJsr> {
		private boolean def;
		public DispatchStrategy_JSRX(boolean def) {
			this.def = def;
		}
		public SEJsr doIt() {
			if (DispatcherBytecodeAlgorithm.this.seJsr == null) {
				DispatcherBytecodeAlgorithm.this.seJsr = new SEJsr();
			}
			DispatcherBytecodeAlgorithm.this.seJsr.def = this.def;
			return DispatcherBytecodeAlgorithm.this.seJsr;
		}
	}

	private class DispatchStrategy_RET implements Dispatcher.DispatchStrategy<SERet> {
		public SERet doIt() {
			if (DispatcherBytecodeAlgorithm.this.seRet == null) {
				DispatcherBytecodeAlgorithm.this.seRet = new SERet();
			}
			return DispatcherBytecodeAlgorithm.this.seRet;
		}
	}

	private class DispatchStrategy_INVOKEVIRTUALINTERFACE implements Dispatcher.DispatchStrategy<SEInvokeVirtualInterface> {
		private boolean isInterface;
		public DispatchStrategy_INVOKEVIRTUALINTERFACE(boolean isInterface) {
			this.isInterface = isInterface;
		}
		public SEInvokeVirtualInterface doIt() {
			if (DispatcherBytecodeAlgorithm.this.seInvokeVirtualInterface == null) {
				DispatcherBytecodeAlgorithm.this.seInvokeVirtualInterface = new SEInvokeVirtualInterface();
			}
			DispatcherBytecodeAlgorithm.this.seInvokeVirtualInterface.isInterface = this.isInterface;
			return DispatcherBytecodeAlgorithm.this.seInvokeVirtualInterface;
		}
	}

	private class DispatchStrategy_INVOKESPECIAL implements Dispatcher.DispatchStrategy<SEInvokeSpecial> {
		public SEInvokeSpecial doIt() {
			if (DispatcherBytecodeAlgorithm.this.seInvokeSpecial == null) {
				DispatcherBytecodeAlgorithm.this.seInvokeSpecial = new SEInvokeSpecial();
			}
			return DispatcherBytecodeAlgorithm.this.seInvokeSpecial;
		}
	}

	private class DispatchStrategy_INVOKESTATIC implements Dispatcher.DispatchStrategy<SEInvokeStatic> {
		public SEInvokeStatic doIt() {
			if (DispatcherBytecodeAlgorithm.this.seInvokeStatic == null) {
				DispatcherBytecodeAlgorithm.this.seInvokeStatic = new SEInvokeStatic();
			}
			return DispatcherBytecodeAlgorithm.this.seInvokeStatic;
		}
	}

	private class DispatchStrategy_XRETURN implements Dispatcher.DispatchStrategy<SEReturn> {
		private boolean def;
		public DispatchStrategy_XRETURN(boolean def) {
			this.def = def;
		}
		public SEReturn doIt() {
			if (DispatcherBytecodeAlgorithm.this.seReturn == null) {
				DispatcherBytecodeAlgorithm.this.seReturn = new SEReturn();
			}
			DispatcherBytecodeAlgorithm.this.seReturn.def = this.def;
			return DispatcherBytecodeAlgorithm.this.seReturn;
		}
	}

	private class DispatchStrategy_ATHROW implements Dispatcher.DispatchStrategy<SEAthrow> {
		public SEAthrow doIt() {
			if (DispatcherBytecodeAlgorithm.this.seAthrow == null) {
				DispatcherBytecodeAlgorithm.this.seAthrow = new SEAthrow();
			}
			return DispatcherBytecodeAlgorithm.this.seAthrow;
		}
	}

	private class DispatchStrategy_WIDE implements Dispatcher.DispatchStrategy<SEWide> {
		public SEWide doIt() {
			if (DispatcherBytecodeAlgorithm.this.seWide == null) {
				DispatcherBytecodeAlgorithm.this.seWide = new SEWide();
			}
			return DispatcherBytecodeAlgorithm.this.seWide;
		}
	}

	private class DispatchStrategy_MONITORX implements Dispatcher.DispatchStrategy<SEMonitor> {
		public SEMonitor doIt() {
			if (DispatcherBytecodeAlgorithm.this.seMonitor == null) {
				DispatcherBytecodeAlgorithm.this.seMonitor = new SEMonitor();
			}
			return DispatcherBytecodeAlgorithm.this.seMonitor;
		}
	}

	private class DispatchStrategy_UNEXPECTED implements Dispatcher.DispatchStrategy<SEUnexpected> {
		public SEUnexpected doIt() {
			if (DispatcherBytecodeAlgorithm.this.seUnexpected == null) {
				DispatcherBytecodeAlgorithm.this.seUnexpected = new SEUnexpected();
			}
			return DispatcherBytecodeAlgorithm.this.seUnexpected;
		}
	}

	private static class DispatchStrategy_UNDEFINED implements Dispatcher.DispatchStrategy<Algorithm> {
		private String bcName;
		public DispatchStrategy_UNDEFINED(String bcName) { this.bcName = bcName; }
		public Algorithm doIt() throws UndefInstructionException {
        	throw new UndefInstructionException(this.bcName);
		}
	}

	private static class DispatchStrategy_INTERNALERROR implements Dispatcher.DispatchStrategy<Algorithm> {
		public Algorithm doIt() {
        	throw new UnexpectedInternalException("this bytecode does not exist");
		}
	}

	public DispatcherBytecodeAlgorithm() {
		//implemented bytecodes (sometimes with limited support)
        this
		.setDispatchStrategy(OP_NOP,             new DispatchStrategy_NOP())
		.setDispatchStrategy(OP_ALOAD,           new DispatchStrategy_XLOAD())
		.setDispatchStrategy(OP_DLOAD,           new DispatchStrategy_XLOAD())
		.setDispatchStrategy(OP_FLOAD,           new DispatchStrategy_XLOAD())
		.setDispatchStrategy(OP_ILOAD,           new DispatchStrategy_XLOAD())
		.setDispatchStrategy(OP_LLOAD,           new DispatchStrategy_XLOAD())
		.setDispatchStrategy(OP_ALOAD_0,         new DispatchStrategy_XLOAD(0))
		.setDispatchStrategy(OP_DLOAD_0,         new DispatchStrategy_XLOAD(0))
		.setDispatchStrategy(OP_FLOAD_0,         new DispatchStrategy_XLOAD(0))
		.setDispatchStrategy(OP_ILOAD_0,         new DispatchStrategy_XLOAD(0))
		.setDispatchStrategy(OP_LLOAD_0,         new DispatchStrategy_XLOAD(0))
		.setDispatchStrategy(OP_ALOAD_1,         new DispatchStrategy_XLOAD(1))
		.setDispatchStrategy(OP_DLOAD_1,         new DispatchStrategy_XLOAD(1))
		.setDispatchStrategy(OP_FLOAD_1,         new DispatchStrategy_XLOAD(1))
		.setDispatchStrategy(OP_ILOAD_1,         new DispatchStrategy_XLOAD(1))
		.setDispatchStrategy(OP_LLOAD_1,         new DispatchStrategy_XLOAD(1))
		.setDispatchStrategy(OP_ALOAD_2,         new DispatchStrategy_XLOAD(2))
		.setDispatchStrategy(OP_DLOAD_2,         new DispatchStrategy_XLOAD(2))
		.setDispatchStrategy(OP_FLOAD_2,         new DispatchStrategy_XLOAD(2))
		.setDispatchStrategy(OP_ILOAD_2,         new DispatchStrategy_XLOAD(2))
		.setDispatchStrategy(OP_LLOAD_2,         new DispatchStrategy_XLOAD(2))
		.setDispatchStrategy(OP_ALOAD_3,         new DispatchStrategy_XLOAD(3))
		.setDispatchStrategy(OP_DLOAD_3,         new DispatchStrategy_XLOAD(3))
		.setDispatchStrategy(OP_FLOAD_3,         new DispatchStrategy_XLOAD(3))
		.setDispatchStrategy(OP_ILOAD_3,         new DispatchStrategy_XLOAD(3))
		.setDispatchStrategy(OP_LLOAD_3,         new DispatchStrategy_XLOAD(3))
		.setDispatchStrategy(OP_ASTORE,          new DispatchStrategy_XSTORE())
		.setDispatchStrategy(OP_DSTORE,          new DispatchStrategy_XSTORE())
		.setDispatchStrategy(OP_FSTORE,          new DispatchStrategy_XSTORE())
		.setDispatchStrategy(OP_ISTORE,          new DispatchStrategy_XSTORE())
		.setDispatchStrategy(OP_LSTORE,          new DispatchStrategy_XSTORE())
		.setDispatchStrategy(OP_ASTORE_0,        new DispatchStrategy_XSTORE(0))
		.setDispatchStrategy(OP_DSTORE_0,        new DispatchStrategy_XSTORE(0))
		.setDispatchStrategy(OP_FSTORE_0,        new DispatchStrategy_XSTORE(0))
		.setDispatchStrategy(OP_ISTORE_0,        new DispatchStrategy_XSTORE(0))
		.setDispatchStrategy(OP_LSTORE_0,        new DispatchStrategy_XSTORE(0))
		.setDispatchStrategy(OP_ASTORE_1,        new DispatchStrategy_XSTORE(1))
		.setDispatchStrategy(OP_DSTORE_1,        new DispatchStrategy_XSTORE(1))
		.setDispatchStrategy(OP_FSTORE_1,        new DispatchStrategy_XSTORE(1))
		.setDispatchStrategy(OP_ISTORE_1,        new DispatchStrategy_XSTORE(1))
		.setDispatchStrategy(OP_LSTORE_1,        new DispatchStrategy_XSTORE(1))
		.setDispatchStrategy(OP_ASTORE_2,        new DispatchStrategy_XSTORE(2))
		.setDispatchStrategy(OP_DSTORE_2,        new DispatchStrategy_XSTORE(2))
		.setDispatchStrategy(OP_FSTORE_2,        new DispatchStrategy_XSTORE(2))
		.setDispatchStrategy(OP_ISTORE_2,        new DispatchStrategy_XSTORE(2))
		.setDispatchStrategy(OP_LSTORE_2,        new DispatchStrategy_XSTORE(2))
		.setDispatchStrategy(OP_ASTORE_3,        new DispatchStrategy_XSTORE(3))
		.setDispatchStrategy(OP_DSTORE_3,        new DispatchStrategy_XSTORE(3))
		.setDispatchStrategy(OP_FSTORE_3,        new DispatchStrategy_XSTORE(3))
		.setDispatchStrategy(OP_ISTORE_3,        new DispatchStrategy_XSTORE(3))
		.setDispatchStrategy(OP_LSTORE_3,        new DispatchStrategy_XSTORE(3))
		.setDispatchStrategy(OP_BIPUSH,          new DispatchStrategy_BIPUSH())
		.setDispatchStrategy(OP_SIPUSH,          new DispatchStrategy_SIPUSH())
		.setDispatchStrategy(OP_LDC,             new DispatchStrategy_LDCX_Y(false, true))
		.setDispatchStrategy(OP_LDC_W,           new DispatchStrategy_LDCX_Y(true, true))
		.setDispatchStrategy(OP_LDC2_W,          new DispatchStrategy_LDCX_Y(true, false))
		.setDispatchStrategy(OP_ACONST_NULL,     new DispatchStrategy_ACONST_NULL())
		.setDispatchStrategy(OP_DCONST_0,        new DispatchStrategy_XCONST_Y(Type.DOUBLE, 0))
		.setDispatchStrategy(OP_DCONST_1,        new DispatchStrategy_XCONST_Y(Type.DOUBLE, 1))
		.setDispatchStrategy(OP_FCONST_0,        new DispatchStrategy_XCONST_Y(Type.FLOAT, 0))
		.setDispatchStrategy(OP_FCONST_1,        new DispatchStrategy_XCONST_Y(Type.FLOAT, 1))
		.setDispatchStrategy(OP_FCONST_2,        new DispatchStrategy_XCONST_Y(Type.FLOAT, 2))
		.setDispatchStrategy(OP_ICONST_M1,       new DispatchStrategy_XCONST_Y(Type.INT, -1))
		.setDispatchStrategy(OP_ICONST_0,        new DispatchStrategy_XCONST_Y(Type.INT, 0))
		.setDispatchStrategy(OP_ICONST_1,        new DispatchStrategy_XCONST_Y(Type.INT, 1))
		.setDispatchStrategy(OP_ICONST_2,        new DispatchStrategy_XCONST_Y(Type.INT, 2))
		.setDispatchStrategy(OP_ICONST_3,        new DispatchStrategy_XCONST_Y(Type.INT, 3))
		.setDispatchStrategy(OP_ICONST_4,        new DispatchStrategy_XCONST_Y(Type.INT, 4))
		.setDispatchStrategy(OP_ICONST_5,        new DispatchStrategy_XCONST_Y(Type.INT, 5))
		.setDispatchStrategy(OP_LCONST_0,        new DispatchStrategy_XCONST_Y(Type.LONG, 0))
		.setDispatchStrategy(OP_LCONST_1,        new DispatchStrategy_XCONST_Y(Type.LONG, 1))
		.setDispatchStrategy(OP_DADD,            new DispatchStrategy_XBINOP(Operator.ADD))
		.setDispatchStrategy(OP_FADD,            new DispatchStrategy_XBINOP(Operator.ADD))
		.setDispatchStrategy(OP_IADD,            new DispatchStrategy_XBINOP(Operator.ADD))
		.setDispatchStrategy(OP_LADD,            new DispatchStrategy_XBINOP(Operator.ADD))
		.setDispatchStrategy(OP_DSUB,            new DispatchStrategy_XBINOP(Operator.SUB))
		.setDispatchStrategy(OP_FSUB,            new DispatchStrategy_XBINOP(Operator.SUB))
		.setDispatchStrategy(OP_ISUB,            new DispatchStrategy_XBINOP(Operator.SUB))
		.setDispatchStrategy(OP_LSUB,            new DispatchStrategy_XBINOP(Operator.SUB))
		.setDispatchStrategy(OP_DMUL,            new DispatchStrategy_XBINOP(Operator.MUL))
		.setDispatchStrategy(OP_FMUL,            new DispatchStrategy_XBINOP(Operator.MUL))
		.setDispatchStrategy(OP_IMUL,            new DispatchStrategy_XBINOP(Operator.MUL))
		.setDispatchStrategy(OP_LMUL,            new DispatchStrategy_XBINOP(Operator.MUL))
		.setDispatchStrategy(OP_DDIV,            new DispatchStrategy_XBINOP(Operator.DIV))
		.setDispatchStrategy(OP_FDIV,            new DispatchStrategy_XBINOP(Operator.DIV))
		.setDispatchStrategy(OP_IDIV,            new DispatchStrategy_XBINOP(Operator.DIV))
		.setDispatchStrategy(OP_LDIV,            new DispatchStrategy_XBINOP(Operator.DIV))
		.setDispatchStrategy(OP_DREM,            new DispatchStrategy_XBINOP(Operator.REM))
		.setDispatchStrategy(OP_FREM,            new DispatchStrategy_XBINOP(Operator.REM))
		.setDispatchStrategy(OP_IREM,            new DispatchStrategy_XBINOP(Operator.REM))
		.setDispatchStrategy(OP_LREM,            new DispatchStrategy_XBINOP(Operator.REM))
		.setDispatchStrategy(OP_DNEG,            new DispatchStrategy_XNEG())
		.setDispatchStrategy(OP_FNEG,            new DispatchStrategy_XNEG())
		.setDispatchStrategy(OP_INEG,            new DispatchStrategy_XNEG())
		.setDispatchStrategy(OP_LNEG,            new DispatchStrategy_XNEG())
		.setDispatchStrategy(OP_ISHL,            new DispatchStrategy_XBINOP(Operator.SHL))
		.setDispatchStrategy(OP_LSHL,            new DispatchStrategy_XBINOP(Operator.SHL))
		.setDispatchStrategy(OP_ISHR,            new DispatchStrategy_XBINOP(Operator.SHR))
		.setDispatchStrategy(OP_LSHR,            new DispatchStrategy_XBINOP(Operator.SHR))
		.setDispatchStrategy(OP_IUSHR,           new DispatchStrategy_XBINOP(Operator.USHR))
		.setDispatchStrategy(OP_LUSHR,           new DispatchStrategy_XBINOP(Operator.USHR))  
		.setDispatchStrategy(OP_IOR,             new DispatchStrategy_XBINOP(Operator.ORBW))
		.setDispatchStrategy(OP_LOR,             new DispatchStrategy_XBINOP(Operator.ORBW))
		.setDispatchStrategy(OP_IAND,            new DispatchStrategy_XBINOP(Operator.ANDBW))
		.setDispatchStrategy(OP_LAND,            new DispatchStrategy_XBINOP(Operator.ANDBW))
		.setDispatchStrategy(OP_IXOR,            new DispatchStrategy_XBINOP(Operator.XORBW))
		.setDispatchStrategy(OP_LXOR,            new DispatchStrategy_XBINOP(Operator.XORBW))
		.setDispatchStrategy(OP_IINC,            new DispatchStrategy_IINC())
		.setDispatchStrategy(OP_DCMPG,           new DispatchStrategy_XCMPY())
		.setDispatchStrategy(OP_DCMPL,           new DispatchStrategy_XCMPY())
		.setDispatchStrategy(OP_FCMPG,           new DispatchStrategy_XCMPY())
		.setDispatchStrategy(OP_FCMPL,           new DispatchStrategy_XCMPY())
		.setDispatchStrategy(OP_LCMP,            new DispatchStrategy_XCMPY())
		.setDispatchStrategy(OP_I2D,             new DispatchStrategy_X2Y(Type.INT, Type.DOUBLE))
		.setDispatchStrategy(OP_I2F,             new DispatchStrategy_X2Y(Type.INT, Type.FLOAT))
		.setDispatchStrategy(OP_I2L,             new DispatchStrategy_X2Y(Type.INT, Type.LONG))
		.setDispatchStrategy(OP_L2D,             new DispatchStrategy_X2Y(Type.LONG, Type.DOUBLE))
		.setDispatchStrategy(OP_L2F,             new DispatchStrategy_X2Y(Type.LONG, Type.FLOAT))
		.setDispatchStrategy(OP_F2D,             new DispatchStrategy_X2Y(Type.FLOAT, Type.DOUBLE))
		.setDispatchStrategy(OP_D2F,             new DispatchStrategy_X2Y(Type.DOUBLE, Type.FLOAT))
		.setDispatchStrategy(OP_D2I,             new DispatchStrategy_X2Y(Type.DOUBLE, Type.INT))
		.setDispatchStrategy(OP_D2L,             new DispatchStrategy_X2Y(Type.DOUBLE, Type.LONG))
		.setDispatchStrategy(OP_F2I,             new DispatchStrategy_X2Y(Type.FLOAT, Type.INT))
		.setDispatchStrategy(OP_F2L,             new DispatchStrategy_X2Y(Type.FLOAT, Type.LONG))
		.setDispatchStrategy(OP_I2B,             new DispatchStrategy_X2Y(Type.INT, Type.BYTE))
		.setDispatchStrategy(OP_I2C,             new DispatchStrategy_X2Y(Type.INT, Type.CHAR))
		.setDispatchStrategy(OP_I2S,             new DispatchStrategy_X2Y(Type.INT, Type.SHORT))
		.setDispatchStrategy(OP_L2I,             new DispatchStrategy_X2Y(Type.LONG, Type.INT))
		.setDispatchStrategy(OP_NEW,             new DispatchStrategy_NEW())
		.setDispatchStrategy(OP_NEWARRAY,        new DispatchStrategy_NEWARRAY())
		.setDispatchStrategy(OP_ANEWARRAY,       new DispatchStrategy_ANEWARRAY())
		.setDispatchStrategy(OP_MULTIANEWARRAY,  new DispatchStrategy_MULTIANEWARRAY())
		.setDispatchStrategy(OP_GETFIELD,        new DispatchStrategy_GETFIELD())
		.setDispatchStrategy(OP_PUTFIELD,        new DispatchStrategy_PUTFIELD())
		.setDispatchStrategy(OP_GETSTATIC,       new DispatchStrategy_GETSTATIC())
		.setDispatchStrategy(OP_PUTSTATIC,       new DispatchStrategy_PUTSTATIC())
		.setDispatchStrategy(OP_AALOAD,          new DispatchStrategy_XALOAD())
		.setDispatchStrategy(OP_CALOAD,          new DispatchStrategy_XALOAD())
		.setDispatchStrategy(OP_BALOAD,          new DispatchStrategy_XALOAD())
		.setDispatchStrategy(OP_DALOAD,          new DispatchStrategy_XALOAD())
		.setDispatchStrategy(OP_FALOAD,          new DispatchStrategy_XALOAD())
		.setDispatchStrategy(OP_IALOAD,          new DispatchStrategy_XALOAD())
		.setDispatchStrategy(OP_LALOAD,          new DispatchStrategy_XALOAD())
		.setDispatchStrategy(OP_SALOAD,          new DispatchStrategy_XALOAD())
		.setDispatchStrategy(OP_AASTORE,         new DispatchStrategy_XASTORE())
		.setDispatchStrategy(OP_CASTORE,         new DispatchStrategy_XASTORE())
		.setDispatchStrategy(OP_BASTORE,         new DispatchStrategy_XASTORE())
		.setDispatchStrategy(OP_DASTORE,         new DispatchStrategy_XASTORE())
		.setDispatchStrategy(OP_FASTORE,         new DispatchStrategy_XASTORE())
		.setDispatchStrategy(OP_IASTORE,         new DispatchStrategy_XASTORE())
		.setDispatchStrategy(OP_LASTORE,         new DispatchStrategy_XASTORE())
		.setDispatchStrategy(OP_SASTORE,         new DispatchStrategy_XASTORE())
		.setDispatchStrategy(OP_ARRAYLENGTH,     new DispatchStrategy_ARRAYLENGTH())
		.setDispatchStrategy(OP_INSTANCEOF,      new DispatchStrategy_INSTANCEOF())
		.setDispatchStrategy(OP_CHECKCAST,       new DispatchStrategy_CHECKCAST())
		.setDispatchStrategy(OP_POP,             new DispatchStrategy_POPX(true))
		.setDispatchStrategy(OP_POP2,            new DispatchStrategy_POPX(false))
		.setDispatchStrategy(OP_DUP,             new DispatchStrategy_DUPX(true))
		.setDispatchStrategy(OP_DUP2,            new DispatchStrategy_DUPX(false))
		.setDispatchStrategy(OP_DUP_X1,          new DispatchStrategy_DUPX_Y(true, true))
		.setDispatchStrategy(OP_DUP2_X1,         new DispatchStrategy_DUPX_Y(false, true))
		.setDispatchStrategy(OP_DUP_X2,          new DispatchStrategy_DUPX_Y(true, false))
		.setDispatchStrategy(OP_DUP2_X2,         new DispatchStrategy_DUPX_Y(false, false))
		.setDispatchStrategy(OP_SWAP,            new DispatchStrategy_SWAP())
		.setDispatchStrategy(OP_IFEQ,            new DispatchStrategy_IFX(true, Operator.EQ))
		.setDispatchStrategy(OP_IFGE,            new DispatchStrategy_IFX(true, Operator.GE))
		.setDispatchStrategy(OP_IFGT,            new DispatchStrategy_IFX(true, Operator.GT))
		.setDispatchStrategy(OP_IFLE,            new DispatchStrategy_IFX(true, Operator.LE))
		.setDispatchStrategy(OP_IFLT,            new DispatchStrategy_IFX(true, Operator.LT))
		.setDispatchStrategy(OP_IFNE,            new DispatchStrategy_IFX(true, Operator.NE))
		.setDispatchStrategy(OP_IFNONNULL,       new DispatchStrategy_IF_ACMPX_XNULL(true, false))
		.setDispatchStrategy(OP_IFNULL,          new DispatchStrategy_IF_ACMPX_XNULL(true, true))
		.setDispatchStrategy(OP_IF_ICMPEQ,       new DispatchStrategy_IFX(false, Operator.EQ))
		.setDispatchStrategy(OP_IF_ICMPGE,       new DispatchStrategy_IFX(false, Operator.GE))
		.setDispatchStrategy(OP_IF_ICMPGT,       new DispatchStrategy_IFX(false, Operator.GT))
		.setDispatchStrategy(OP_IF_ICMPLE,       new DispatchStrategy_IFX(false, Operator.LE))
		.setDispatchStrategy(OP_IF_ICMPLT,       new DispatchStrategy_IFX(false, Operator.LT))
		.setDispatchStrategy(OP_IF_ICMPNE,       new DispatchStrategy_IFX(false, Operator.NE))
		.setDispatchStrategy(OP_IF_ACMPEQ,       new DispatchStrategy_IF_ACMPX_XNULL(false, true))
		.setDispatchStrategy(OP_IF_ACMPNE,       new DispatchStrategy_IF_ACMPX_XNULL(false, false))
		.setDispatchStrategy(OP_TABLESWITCH,     new DispatchStrategy_XSWITCH(true))
		.setDispatchStrategy(OP_LOOKUPSWITCH,    new DispatchStrategy_XSWITCH(false))
		.setDispatchStrategy(OP_GOTO,            new DispatchStrategy_GOTOX(true))
		.setDispatchStrategy(OP_GOTO_W,          new DispatchStrategy_GOTOX(false))
		.setDispatchStrategy(OP_JSR,             new DispatchStrategy_JSRX(true))
		.setDispatchStrategy(OP_JSR_W,           new DispatchStrategy_JSRX(false))
		.setDispatchStrategy(OP_RET,             new DispatchStrategy_RET())
		.setDispatchStrategy(OP_INVOKEINTERFACE, new DispatchStrategy_INVOKEVIRTUALINTERFACE(true))
		.setDispatchStrategy(OP_INVOKEVIRTUAL,   new DispatchStrategy_INVOKEVIRTUALINTERFACE(false))
		.setDispatchStrategy(OP_INVOKESPECIAL,   new DispatchStrategy_INVOKESPECIAL())
		.setDispatchStrategy(OP_INVOKESTATIC,    new DispatchStrategy_INVOKESTATIC())
		.setDispatchStrategy(OP_RETURN,          new DispatchStrategy_XRETURN(true))
		.setDispatchStrategy(OP_ARETURN,         new DispatchStrategy_XRETURN(false))
		.setDispatchStrategy(OP_DRETURN,         new DispatchStrategy_XRETURN(false))
		.setDispatchStrategy(OP_FRETURN,         new DispatchStrategy_XRETURN(false))
		.setDispatchStrategy(OP_IRETURN,         new DispatchStrategy_XRETURN(false))
		.setDispatchStrategy(OP_LRETURN,         new DispatchStrategy_XRETURN(false))
		.setDispatchStrategy(OP_ATHROW,          new DispatchStrategy_ATHROW())
		.setDispatchStrategy(OP_WIDE,            new DispatchStrategy_WIDE())
		.setDispatchStrategy(OP_MONITORENTER,    new DispatchStrategy_MONITORX())
		.setDispatchStrategy(OP_MONITOREXIT,     new DispatchStrategy_MONITORX())
		.setDispatchStrategy(OP_BREAKPOINT,      new DispatchStrategy_NOP())
		.setDispatchStrategy(OP_IMPDEP1,         new DispatchStrategy_NOP())
		.setDispatchStrategy(OP_IMPDEP2,         new DispatchStrategy_NOP())
		;
	
        //this should never appear in a classfile
        DispatchStrategy_UNEXPECTED s = new DispatchStrategy_UNEXPECTED();
        this
		.setDispatchStrategy(OP_ANEWARRAY_QUICK,           s)
		.setDispatchStrategy(OP_CHECKCAST_QUICK,           s)
		.setDispatchStrategy(OP_GETSTATIC_QUICK,           s)
		.setDispatchStrategy(OP_GETSTATIC2_QUICK,          s)
		.setDispatchStrategy(OP_GETFIELD_QUICK,            s)
		.setDispatchStrategy(OP_GETFIELD_QUICK_W,          s)
		.setDispatchStrategy(OP_GETFIELD2_QUICK,           s)
		.setDispatchStrategy(OP_INSTANCEOF_QUICK,          s)
		.setDispatchStrategy(OP_INVOKEINTERFACE_QUICK,     s)
		.setDispatchStrategy(OP_INVOKENONVIRTUAL_QUICK,    s)
		.setDispatchStrategy(OP_INVOKESTATIC_QUICK,        s)
		.setDispatchStrategy(OP_INVOKESUPER_QUICK,         s)
		.setDispatchStrategy(OP_INVOKEVIRTUAL_QUICK,       s)
		.setDispatchStrategy(OP_INVOKEVIRTUAL_QUICK_W,     s)
		.setDispatchStrategy(OP_INVOKEVIRTUALOBJECT_QUICK, s)
		.setDispatchStrategy(OP_LDC_QUICK,                 s)
		.setDispatchStrategy(OP_LDC_W_QUICK,               s)
		.setDispatchStrategy(OP_LDC2_W_QUICK,              s)
		.setDispatchStrategy(OP_MULTIANEWARRAY_QUICK,      s)
		.setDispatchStrategy(OP_NEW_QUICK,                 s)
		.setDispatchStrategy(OP_PUTFIELD_QUICK,            s)
		.setDispatchStrategy(OP_PUTFIELD_QUICK_W,          s)
		.setDispatchStrategy(OP_PUTFIELD2_QUICK,           s)
		.setDispatchStrategy(OP_PUTSTATIC_QUICK,           s)
		.setDispatchStrategy(OP_PUTSTATIC2_QUICK,          s)
        ;

        //not (yet) implemented
        this 
		.setDispatchStrategy(OP_INVOKEDYNAMIC,   new DispatchStrategy_UNDEFINED("INVOKEDYNAMIC"));
	
        //all 2^8 values should be mapped to a dispatch strategy
        this.setDispatchNonexistentStrategy(new DispatchStrategy_INTERNALERROR());
    }
	
    public SEInit select() {
		if (this.seInit == null) {
			this.seInit = new SEInit();
		}
		return this.seInit;
    }
		
	@Override
	public Algorithm select(Byte bytecode) 
	throws UndefInstructionException {
		final Algorithm retVal;
        try {
            retVal = super.select(bytecode);
        } catch (UndefInstructionException | RuntimeException e) {
            throw e;
        } catch (Exception e) {
            //this should never happen
            throw new UnexpectedInternalException(e);
        }
		return retVal;
	}
}
