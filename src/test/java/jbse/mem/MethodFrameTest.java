package jbse.mem;

import static jbse.bc.ClassLoaders.CLASSLOADER_APP;
import static org.junit.Assert.*;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;

import org.junit.Before;
import org.junit.Test;

import jbse.bc.ClassFile;
import jbse.bc.ClassFileFactoryJavassist;
import jbse.bc.ClassHierarchy;
import jbse.bc.Classpath;
import jbse.bc.Signature;
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
import jbse.common.exc.InvalidInputException;
import jbse.mem.exc.InvalidSlotException;
import jbse.val.Null;
import jbse.val.ReferenceConcrete;
import jbse.val.Value;

public class MethodFrameTest {
    private ClassHierarchy hier;

    @Before
    public void setUp() throws InvalidClassFileFactoryClassException, IOException, InvalidInputException {
        //environment
        final ArrayList<Path> userPath = new ArrayList<>();
        userPath.add(Paths.get("src/test/resources/jbse/bc/testdata"));
        final Classpath env = new Classpath(Paths.get("."), Paths.get(System.getProperty("java.home", "")), Collections.emptyList(), userPath);

        //class hierarchy
        this.hier = new ClassHierarchy(env, ClassFileFactoryJavassist.class, Collections.emptyMap(), Collections.emptyMap());
    }

    @Test
    public void testFrameCurrentMethodSignature() throws ClassFileNotFoundException, ClassFileIllFormedException, 
    InvalidInputException, BadClassFileVersionException, WrongClassNameException, IncompatibleClassFileException, 
    ClassFileNotAccessibleException, PleaseLoadClassException, MethodNotFoundException, MethodCodeNotFoundException, 
    RenameUnsupportedException {
        final String className = "tsafe/engine/TsafeEngine";
        final ClassFile cf = this.hier.loadCreateClass(CLASSLOADER_APP, className, true);
        final Signature sigMethod = new Signature(className, "()V", "start");
        final MethodFrame f = new MethodFrame(sigMethod, cf);
        assertEquals(f.getMethodSignature(), sigMethod);
    }

    @Test
    public void testFrameLocalVariables1() throws ClassFileNotFoundException, ClassFileIllFormedException, 
    InvalidInputException, BadClassFileVersionException, WrongClassNameException, IncompatibleClassFileException, 
    ClassFileNotAccessibleException, PleaseLoadClassException, MethodNotFoundException, MethodCodeNotFoundException, 
    InvalidSlotException, RenameUnsupportedException {
        final String className = "tsafe/engine/TsafeEngine";
        final ClassFile cf = this.hier.loadCreateClass(CLASSLOADER_APP, className, true);
        final Signature sigMethod = new Signature(className, "()V", "start");
        final MethodFrame f = new MethodFrame(sigMethod, cf);
        f.setArgs(Null.getInstance());
        final Value valThis = f.getLocalVariableValue(0);
        assertEquals(valThis, Null.getInstance());
    }

    @Test
    public void testFrameLocalVariables2() throws ClassFileNotFoundException, ClassFileIllFormedException, 
    InvalidInputException, BadClassFileVersionException, WrongClassNameException, IncompatibleClassFileException, 
    ClassFileNotAccessibleException, PleaseLoadClassException, MethodNotFoundException, MethodCodeNotFoundException, 
    InvalidSlotException, RenameUnsupportedException {
        final String className = "tsafe/engine/TsafeEngine";
        final ClassFile cf = this.hier.loadCreateClass(CLASSLOADER_APP, className, true);
        final Signature sigMethod = new Signature(className, "()V", "start");
        final MethodFrame f = new MethodFrame(sigMethod, cf);
        f.setArgs(Null.getInstance());
        final Value valThis = f.getLocalVariableValue("this");
        assertEquals(valThis, Null.getInstance());
    }

    @Test
    public void testFrameClone() throws ClassFileNotFoundException, ClassFileIllFormedException, 
    InvalidInputException, BadClassFileVersionException, WrongClassNameException, IncompatibleClassFileException, 
    ClassFileNotAccessibleException, PleaseLoadClassException, MethodNotFoundException, MethodCodeNotFoundException, 
    InvalidSlotException, RenameUnsupportedException {
        final String className = "tsafe/engine/TsafeEngine";
        final ClassFile cf = this.hier.loadCreateClass(CLASSLOADER_APP, className, true);
        final Signature sigMethod = new Signature(className, "()V", "start");
        final MethodFrame f = new MethodFrame(sigMethod, cf);
        f.setArgs(Null.getInstance());
        final MethodFrame fClone = f.clone();
        f.setLocalVariableValue(0, 0, new ReferenceConcrete(5));
        final Value valThisClone = fClone.getLocalVariableValue(0);
        assertEquals(valThisClone, Null.getInstance());
    }
}
