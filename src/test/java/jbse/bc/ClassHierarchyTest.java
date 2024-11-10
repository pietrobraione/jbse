package jbse.bc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Test;

import jbse.bc.exc.BadClassFileVersionException;
import jbse.bc.exc.ClassFileIllFormedException;
import jbse.bc.exc.ClassFileNotAccessibleException;
import jbse.bc.exc.ClassFileNotFoundException;
import jbse.bc.exc.IncompatibleClassFileException;
import jbse.bc.exc.InvalidClassFileFactoryClassException;
import jbse.bc.exc.RenameUnsupportedException;
import jbse.bc.exc.WrongClassNameException;
import jbse.common.exc.InvalidInputException;

public class ClassHierarchyTest {
    private Classpath cp;

    @Before
    public void setUp() throws IOException {
        this.cp = new Classpath(Paths.get(".", "build", "classes"), Paths.get(System.getProperty("java.home", "")), 
                                new ArrayList<>(Arrays.stream(System.getProperty("java.ext.dirs", "").split(File.pathSeparator)).map(s -> Paths.get(s)).collect(Collectors.toList())), 
                                Collections.emptyList());
    }
    
    @Test
    public void testLoadClass() throws InvalidClassFileFactoryClassException, InvalidInputException, 
    ClassFileNotFoundException, ClassFileIllFormedException, ClassFileNotAccessibleException, 
    IncompatibleClassFileException, BadClassFileVersionException, RenameUnsupportedException, WrongClassNameException {
        final ClassHierarchy hier = new ClassHierarchy(cp, ClassFileFactoryJavassist.class, Collections.emptyMap(), Collections.emptyMap());
        final ClassFile cf = hier.loadCreateClass("java/lang/Object");
        assertEquals("java/lang/Object", cf.getClassName());
        assertEquals(null, cf.getSuperclass());
        assertEquals("java/lang", cf.getPackageName());
        assertEquals("Object.java", cf.getSourceFile());
        assertEquals(false, cf.isArray());        
        assertEquals(false, cf.isPrimitiveOrVoid());
        assertEquals(true, cf.isReference());
        assertEquals(false, cf.isPackage());
        assertEquals(false, cf.isPrivate());
        assertEquals(false, cf.isProtected());
        assertEquals(true, cf.isPublic());
        assertEquals(false, cf.isAnonymous());
        assertEquals(false, cf.isAnonymousUnregistered());
    }
    
    @Test
    public void testLoadClassLoadedAllSuperclasses() throws InvalidClassFileFactoryClassException, InvalidInputException, 
    ClassFileNotFoundException, ClassFileIllFormedException, ClassFileNotAccessibleException, 
    IncompatibleClassFileException, BadClassFileVersionException, RenameUnsupportedException, WrongClassNameException {
        final ClassHierarchy hier = new ClassHierarchy(cp, ClassFileFactoryJavassist.class, Collections.emptyMap(), Collections.emptyMap());
        hier.loadCreateClass("java/util/ArrayList");
        assertNotNull(hier.getClassFileClassArray(0, "java/util/ArrayList"));
        assertNotNull(hier.getClassFileClassArray(0, "java/util/AbstractList"));
        assertNotNull(hier.getClassFileClassArray(0, "java/util/AbstractCollection"));
        assertNotNull(hier.getClassFileClassArray(0, "java/lang/Object"));
        assertNotNull(hier.getClassFileClassArray(0, "java/util/List"));
        assertNotNull(hier.getClassFileClassArray(0, "java/util/Collection"));
        assertNotNull(hier.getClassFileClassArray(0, "java/lang/Iterable"));
        assertNotNull(hier.getClassFileClassArray(0, "java/util/RandomAccess"));
        assertNotNull(hier.getClassFileClassArray(0, "java/lang/Cloneable"));
        assertNotNull(hier.getClassFileClassArray(0, "java/io/Serializable"));
    }
    
    @Test
    public void testLoadClassPrimitiveFloat() throws InvalidClassFileFactoryClassException, InvalidInputException, 
    ClassFileNotFoundException, ClassFileIllFormedException, ClassFileNotAccessibleException, 
    IncompatibleClassFileException, BadClassFileVersionException, RenameUnsupportedException, WrongClassNameException {
        final ClassHierarchy hier = new ClassHierarchy(cp, ClassFileFactoryJavassist.class, Collections.emptyMap(), Collections.emptyMap());
        final ClassFile cf = hier.getClassFilePrimitiveOrVoid("float");
        assertEquals("float", cf.getClassName());
        assertEquals(0, cf.getDeclaredConstructors().length);
        assertEquals(0, cf.getDeclaredFields().length);
        assertEquals(0, cf.getDeclaredMethods().length);
        assertEquals("", cf.getPackageName());
        assertEquals("", cf.getSourceFile());
        assertEquals(false, cf.isArray());
        assertEquals(true, cf.isPrimitiveOrVoid());
        assertEquals(false, cf.isReference());
        assertEquals(false, cf.isPackage());
        assertEquals(false, cf.isPrivate());
        assertEquals(false, cf.isProtected());
        assertEquals(true, cf.isPublic());
        assertEquals(false, cf.isAnonymous());
        assertEquals(false, cf.isAnonymousUnregistered());
    }
}
