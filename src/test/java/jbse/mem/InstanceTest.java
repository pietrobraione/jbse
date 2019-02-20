package jbse.mem;

import static jbse.bc.ClassLoaders.*;
import static org.junit.Assert.*;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;

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
import jbse.bc.exc.PleaseLoadClassException;
import jbse.bc.exc.WrongClassNameException;
import jbse.common.exc.InvalidInputException;
import jbse.rewr.CalculatorRewriting;
import jbse.val.Value;
import jbse.val.exc.InvalidTypeException;

public class InstanceTest {
    private ClassHierarchy hier;
    private CalculatorRewriting calc;

    @Before
    public void setUp() throws InvalidClassFileFactoryClassException, IOException, InvalidInputException {
        //environment
        final ArrayList<Path> userPath = new ArrayList<>();
        userPath.add(Paths.get("src/test/resources/jbse/bc/testdata"));
        final Classpath env = new Classpath(Paths.get(System.getProperty("java.home")), new ArrayList<>(), userPath);

        //class hierarchy
        this.hier = new ClassHierarchy(env, ClassFileFactoryJavassist.class, new HashMap<>());
        
        //calculator
        this.calc = new CalculatorRewriting();
    }

    @Test
    public void testInstanceGetFieldValue1() throws ClassFileNotFoundException, ClassFileIllFormedException, InvalidInputException, 
    BadClassFileVersionException, WrongClassNameException, IncompatibleClassFileException, ClassFileNotAccessibleException, 
    PleaseLoadClassException, InvalidTypeException {
        final String className = "tsafe/main/SimpleCalculator";
        final ClassFile classFile = this.hier.loadCreateClass(CLASSLOADER_APP, className, true);
        final int numOfStaticFields = this.hier.numOfStaticFields(classFile);
        final Signature[] fieldsSignatures = this.hier.getAllFields(classFile);
        final Instance i = new InstanceImpl(false, this.calc, classFile, null, null, numOfStaticFields, fieldsSignatures);
        final Signature sigMinLat = new Signature(className, "D", "minLat");
        final Value valMinLat = i.getFieldValue(sigMinLat);
        assertEquals(valMinLat, this.calc.valDouble(0));
    }

    @Test
    public void testInstanceGetFieldValue2() throws ClassFileNotFoundException, ClassFileIllFormedException, InvalidInputException, 
    BadClassFileVersionException, WrongClassNameException, IncompatibleClassFileException, ClassFileNotAccessibleException, 
    PleaseLoadClassException, InvalidTypeException {
        final String className = "tsafe/main/SimpleCalculator";
        final ClassFile classFile = this.hier.loadCreateClass(CLASSLOADER_APP, className, true);
        final int numOfStaticFields = this.hier.numOfStaticFields(classFile);
        final Signature[] fieldsSignatures = this.hier.getAllFields(classFile);
        final Instance i = new InstanceImpl(false, this.calc, classFile, null, null, numOfStaticFields, fieldsSignatures);
        final Signature sigMinLat = new Signature(className, "D", "minLat");
        final Value valMinLat = i.getFieldValue(sigMinLat);
        final Value valMinLat2 = i.getFieldValue("minLat", className);
        assertEquals(valMinLat, valMinLat2);
    }

    @Test
    public void testInstanceSetFieldValue() throws ClassFileNotFoundException, ClassFileIllFormedException, InvalidInputException, 
    BadClassFileVersionException, WrongClassNameException, IncompatibleClassFileException, ClassFileNotAccessibleException, 
    PleaseLoadClassException, InvalidTypeException {
        final String className = "tsafe/main/SimpleCalculator";
        final ClassFile classFile = this.hier.loadCreateClass(CLASSLOADER_APP, className, true);
        final int numOfStaticFields = this.hier.numOfStaticFields(classFile);
        final Signature[] fieldsSignatures = this.hier.getAllFields(classFile);
        final Instance i = new InstanceImpl(false, this.calc, classFile, null, null, numOfStaticFields, fieldsSignatures);
        final Signature sigMinLat = new Signature(className, "D", "minLat");
        i.setFieldValue(sigMinLat, this.calc.valDouble(1.0d));
        final Value valMinLat = i.getFieldValue("minLat", className);
        assertEquals(valMinLat, this.calc.valDouble(1.0d));
    }

    @Test
    public void testInstanceClone() throws ClassFileNotFoundException, ClassFileIllFormedException, InvalidInputException, 
    BadClassFileVersionException, WrongClassNameException, IncompatibleClassFileException, ClassFileNotAccessibleException, 
    PleaseLoadClassException, InvalidTypeException {
        final String className = "tsafe/main/SimpleCalculator";
        final ClassFile classFile = this.hier.loadCreateClass(CLASSLOADER_APP, className, true);
        final int numOfStaticFields = this.hier.numOfStaticFields(classFile);
        final Signature[] fieldsSignatures = this.hier.getAllFields(classFile);
        final Instance i = new InstanceImpl(false, this.calc, classFile, null, null, numOfStaticFields, fieldsSignatures);
        final Instance iClone = i.clone();
        final Signature sigMinLat = new Signature(className, "D", "minLat");
        i.setFieldValue(sigMinLat, this.calc.valDouble(1.0d));
        final Value valMinLatClone = iClone.getFieldValue("minLat", className);
        assertEquals(valMinLatClone, this.calc.valDouble(0));
    }
}
