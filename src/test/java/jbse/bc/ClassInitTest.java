package jbse.bc;

import static org.junit.Assert.*;
import static jbse.bc.ClassLoaders.CLASSLOADER_APP;
import static jbse.bc.ClassLoaders.CLASSLOADER_BOOT;
import static jbse.bc.Signatures.JAVA_CLONEABLE;
import static jbse.bc.Signatures.JAVA_ENUM;
import static jbse.bc.Signatures.JAVA_SERIALIZABLE;
import static jbse.bc.Signatures.JAVA_STRING;
import static jbse.bc.Signatures.JBSE_BASE_MAKEKLASSSYMBOLIC;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

import org.junit.Before;
import org.junit.Test;

import jbse.algo.ExecutionContext;
import jbse.algo.InterruptException;
import jbse.algo.UtilClassInitialization;
import jbse.bc.exc.BadClassFileVersionException;
import jbse.bc.exc.ClassFileIllFormedException;
import jbse.bc.exc.ClassFileNotAccessibleException;
import jbse.bc.exc.ClassFileNotFoundException;
import jbse.bc.exc.IncompatibleClassFileException;
import jbse.bc.exc.InvalidClassFileFactoryClassException;
import jbse.bc.exc.MethodCodeNotFoundException;
import jbse.bc.exc.MethodNotFoundException;
import jbse.bc.exc.PleaseLoadClassException;
import jbse.bc.exc.RenameUnsupportedException;
import jbse.bc.exc.WrongClassNameException;
import jbse.common.exc.ClasspathException;
import jbse.common.exc.InvalidInputException;
import jbse.dec.DecisionProcedureAlgorithms;
import jbse.dec.DecisionProcedureAlwSat;
import jbse.dec.DecisionProcedureClassInit;
import jbse.dec.exc.DecisionException;
import jbse.mem.Frame;
import jbse.mem.State;
import jbse.mem.exc.CannotAssumeSymbolicObjectException;
import jbse.mem.exc.ContradictionException;
import jbse.mem.exc.HeapMemoryExhaustedException;
import jbse.rewr.CalculatorRewriting;
import jbse.rewr.RewriterNegationElimination;
import jbse.rewr.RewriterExpressionOrConversionOnSimplex;
import jbse.rewr.RewriterFunctionApplicationOnSimplex;
import jbse.rewr.RewriterZeroUnit;
import jbse.rules.ClassInitRulesRepo;
import jbse.rules.TriggerRulesRepo;
import jbse.tree.DecisionAlternativeComparators;

public class ClassInitTest {
    private State state;
    private ExecutionContext ctx;

    @Before
    public void setUp() throws InvalidClassFileFactoryClassException, InvalidInputException, ClassFileNotFoundException, ClassFileIllFormedException, ClassFileNotAccessibleException, IncompatibleClassFileException, PleaseLoadClassException, BadClassFileVersionException, WrongClassNameException, IOException, RenameUnsupportedException {
        final ArrayList<Path> userPaths = new ArrayList<>();
        userPaths.add(Paths.get("src/test/resources/jbse/bc/testdata"));
        final Classpath cp = new Classpath(Paths.get("build/classes/java/main"), Paths.get(System.getProperty("java.home", "")), Collections.emptyList(), userPaths);
        final CalculatorRewriting calc = new CalculatorRewriting();
        calc.addRewriter(new RewriterExpressionOrConversionOnSimplex()); //indispensable
        calc.addRewriter(new RewriterFunctionApplicationOnSimplex()); //indispensable
		calc.addRewriter(new RewriterZeroUnit()); //indispensable
		calc.addRewriter(new RewriterNegationElimination()); //indispensable?
        final DecisionProcedureAlgorithms dec = new DecisionProcedureAlgorithms(new DecisionProcedureClassInit(new DecisionProcedureAlwSat(calc), new ClassInitRulesRepo()));
        this.ctx = new ExecutionContext(null, true, 20, 20, cp, ClassFileFactoryJavassist.class, Collections.emptyMap(), Collections.emptyMap(), true, calc, new DecisionAlternativeComparators(), new Signature("hier/A", "()V", "a"), dec, null, null, new TriggerRulesRepo(), new ArrayList<String>());
        this.state = this.ctx.createStateVirginPreInitial();
        this.state.getClassHierarchy().loadCreateClass(CLASSLOADER_BOOT, JAVA_CLONEABLE, true); //necessary when creating string literals
        this.state.getClassHierarchy().loadCreateClass(CLASSLOADER_BOOT, JAVA_SERIALIZABLE, true); //necessary when creating string literals
        this.state.getClassHierarchy().loadCreateClass(CLASSLOADER_BOOT, JAVA_STRING, true); //necessary when creating string literals
        this.state.getClassHierarchy().loadCreateClass(CLASSLOADER_BOOT, JAVA_ENUM, true); //necessary when checking if a class has a pure initializer
    }
    
    @Test
    public void test1() throws InvalidInputException, ClassFileNotFoundException, ClassFileIllFormedException, ClassFileNotAccessibleException, IncompatibleClassFileException, PleaseLoadClassException, BadClassFileVersionException, WrongClassNameException, DecisionException, ClasspathException, HeapMemoryExhaustedException, InterruptException, ContradictionException, MethodNotFoundException, MethodCodeNotFoundException, CannotAssumeSymbolicObjectException, RenameUnsupportedException {
        final ClassFile cf_A = this.state.getClassHierarchy().loadCreateClass(CLASSLOADER_APP, "hier/A", true);
        this.state.pushFrameSymbolic(cf_A, this.ctx.rootMethodSignature); //or initialization will fail
        try {
            UtilClassInitialization.ensureClassInitialized(this.state, this.ctx, cf_A);
            assertTrue(false); //should not arrive here
        } catch (InterruptException e) {
            //that's right, go on
        }
        assertEquals(20, this.state.getStackSize());
        final Iterator<Frame> itFrames = this.state.getStack().iterator();
        Frame f;
        f = itFrames.next(); assertEquals(this.ctx.rootMethodSignature, f.getMethodSignature()); 
        f = itFrames.next(); assertEquals(JBSE_BASE_MAKEKLASSSYMBOLIC, f.getMethodSignature()); 
        f = itFrames.next(); assertEquals(new Signature("hier/A", "()V", "<clinit>"), f.getMethodSignature()); 
        f = itFrames.next(); assertEquals(JBSE_BASE_MAKEKLASSSYMBOLIC, f.getMethodSignature()); 
        f = itFrames.next(); assertEquals(new Signature("hier/E", "()V", "<clinit>"), f.getMethodSignature()); 
        f = itFrames.next(); assertEquals(JBSE_BASE_MAKEKLASSSYMBOLIC, f.getMethodSignature()); 
        f = itFrames.next(); assertEquals(new Signature("hier/G", "()V", "<clinit>"), f.getMethodSignature()); 
        f = itFrames.next(); assertEquals(JBSE_BASE_MAKEKLASSSYMBOLIC, f.getMethodSignature()); 
        f = itFrames.next(); assertEquals(new Signature("hier/F", "()V", "<clinit>"), f.getMethodSignature()); 
        f = itFrames.next(); assertEquals(JBSE_BASE_MAKEKLASSSYMBOLIC, f.getMethodSignature()); 
        f = itFrames.next(); assertEquals(new Signature("hier/H", "()V", "<clinit>"), f.getMethodSignature()); 
        f = itFrames.next(); assertEquals(JBSE_BASE_MAKEKLASSSYMBOLIC, f.getMethodSignature()); 
        f = itFrames.next(); assertEquals(new Signature("hier/D", "()V", "<clinit>"), f.getMethodSignature()); 
        f = itFrames.next(); assertEquals(JBSE_BASE_MAKEKLASSSYMBOLIC, f.getMethodSignature()); 
        f = itFrames.next(); assertEquals(new Signature("hier/B", "()V", "<clinit>"), f.getMethodSignature()); 
        f = itFrames.next(); assertEquals(JBSE_BASE_MAKEKLASSSYMBOLIC, f.getMethodSignature()); 
        f = itFrames.next(); assertEquals(new Signature("hier/I", "()V", "<clinit>"), f.getMethodSignature()); 
        f = itFrames.next(); assertEquals(JBSE_BASE_MAKEKLASSSYMBOLIC, f.getMethodSignature()); 
        f = itFrames.next(); assertEquals(new Signature("hier/C", "()V", "<clinit>"), f.getMethodSignature()); 
        f = itFrames.next(); assertEquals(new Signature("java/lang/Object", "()V", "<clinit>"), f.getMethodSignature()); 
    }
}
