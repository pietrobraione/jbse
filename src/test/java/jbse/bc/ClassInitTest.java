package jbse.bc;

import static org.junit.Assert.*;
import static jbse.bc.ClassLoaders.CLASSLOADER_APP;
import static jbse.bc.ClassLoaders.CLASSLOADER_BOOT;
import static jbse.bc.Signatures.JAVA_CLONEABLE;
import static jbse.bc.Signatures.JAVA_ENUM;
import static jbse.bc.Signatures.JAVA_SERIALIZABLE;
import static jbse.bc.Signatures.JAVA_STRING;
import static jbse.bc.Signatures.JBSE_BASE_MAKEKLASSSYMBOLIC;
import static org.hamcrest.core.Is.*;

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
import jbse.algo.Util;
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
import jbse.rewr.RewriterOperationOnSimplex;
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
        calc.addRewriter(new RewriterOperationOnSimplex());
        final DecisionProcedureAlgorithms dec = new DecisionProcedureAlgorithms(new DecisionProcedureClassInit(new DecisionProcedureAlwSat(calc), new ClassInitRulesRepo()));
        this.ctx = new ExecutionContext(null, true, 20, 20, true, cp, ClassFileFactoryJavassist.class, Collections.emptyMap(), Collections.emptyMap(), calc, new DecisionAlternativeComparators(), new Signature("hier/A", "()V", "a"), dec, null, null, new TriggerRulesRepo(), new ArrayList<String>());
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
            Util.ensureClassInitialized(this.state, this.ctx, cf_A);
            assertTrue(false); //should not arrive here
        } catch (InterruptException e) {
            //that's right, go on
        }
        assertThat(this.state.getStackSize(), is(20));
        final Iterator<Frame> itFrames = this.state.getStack().iterator();
        Frame f;
        f = itFrames.next(); assertThat(f.getMethodSignature(), is(this.ctx.rootMethodSignature)); 
        f = itFrames.next(); assertThat(f.getMethodSignature(), is(JBSE_BASE_MAKEKLASSSYMBOLIC)); 
        f = itFrames.next(); assertThat(f.getMethodSignature(), is(new Signature("hier/A", "()V", "<clinit>"))); 
        f = itFrames.next(); assertThat(f.getMethodSignature(), is(JBSE_BASE_MAKEKLASSSYMBOLIC)); 
        f = itFrames.next(); assertThat(f.getMethodSignature(), is(new Signature("hier/E", "()V", "<clinit>"))); 
        f = itFrames.next(); assertThat(f.getMethodSignature(), is(JBSE_BASE_MAKEKLASSSYMBOLIC)); 
        f = itFrames.next(); assertThat(f.getMethodSignature(), is(new Signature("hier/G", "()V", "<clinit>"))); 
        f = itFrames.next(); assertThat(f.getMethodSignature(), is(JBSE_BASE_MAKEKLASSSYMBOLIC)); 
        f = itFrames.next(); assertThat(f.getMethodSignature(), is(new Signature("hier/F", "()V", "<clinit>"))); 
        f = itFrames.next(); assertThat(f.getMethodSignature(), is(JBSE_BASE_MAKEKLASSSYMBOLIC)); 
        f = itFrames.next(); assertThat(f.getMethodSignature(), is(new Signature("hier/H", "()V", "<clinit>"))); 
        f = itFrames.next(); assertThat(f.getMethodSignature(), is(JBSE_BASE_MAKEKLASSSYMBOLIC)); 
        f = itFrames.next(); assertThat(f.getMethodSignature(), is(new Signature("hier/D", "()V", "<clinit>"))); 
        f = itFrames.next(); assertThat(f.getMethodSignature(), is(JBSE_BASE_MAKEKLASSSYMBOLIC)); 
        f = itFrames.next(); assertThat(f.getMethodSignature(), is(new Signature("hier/B", "()V", "<clinit>"))); 
        f = itFrames.next(); assertThat(f.getMethodSignature(), is(JBSE_BASE_MAKEKLASSSYMBOLIC)); 
        f = itFrames.next(); assertThat(f.getMethodSignature(), is(new Signature("hier/I", "()V", "<clinit>"))); 
        f = itFrames.next(); assertThat(f.getMethodSignature(), is(JBSE_BASE_MAKEKLASSSYMBOLIC)); 
        f = itFrames.next(); assertThat(f.getMethodSignature(), is(new Signature("hier/C", "()V", "<clinit>"))); 
        f = itFrames.next(); assertThat(f.getMethodSignature(), is(new Signature("java/lang/Object", "()V", "<clinit>"))); 
    }
}
