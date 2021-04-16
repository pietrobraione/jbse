package jbse.bc;

import static org.junit.Assert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.hamcrest.core.IsNull.nullValue;

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
        assertThat(cf.getClassName(), is("java/lang/Object"));
        assertThat(cf.getSuperclass(), is(nullValue()));
        assertThat(cf.getPackageName(), is("java/lang"));
        assertThat(cf.getSourceFile(), is("Object.java"));
        assertThat(cf.isArray(), is(false));
        assertThat(cf.isPrimitiveOrVoid(), is(false));
        assertThat(cf.isReference(), is(true));
        assertThat(cf.isPackage(), is(false));
        assertThat(cf.isPrivate(), is(false));
        assertThat(cf.isProtected(), is(false));
        assertThat(cf.isPublic(), is(true));
        assertThat(cf.isAnonymous(), is(false));
        assertThat(cf.isAnonymousUnregistered(), is(false));
    }
    
    @Test
    public void testLoadClassLoadedAllSuperclasses() throws InvalidClassFileFactoryClassException, InvalidInputException, 
    ClassFileNotFoundException, ClassFileIllFormedException, ClassFileNotAccessibleException, 
    IncompatibleClassFileException, BadClassFileVersionException, RenameUnsupportedException, WrongClassNameException {
        final ClassHierarchy hier = new ClassHierarchy(cp, ClassFileFactoryJavassist.class, Collections.emptyMap(), Collections.emptyMap());
        hier.loadCreateClass("java/util/ArrayList");
        assertThat(hier.getClassFileClassArray(0, "java/util/ArrayList"), is(notNullValue()));
        assertThat(hier.getClassFileClassArray(0, "java/util/AbstractList"), is(notNullValue()));
        assertThat(hier.getClassFileClassArray(0, "java/util/AbstractCollection"), is(notNullValue()));
        assertThat(hier.getClassFileClassArray(0, "java/lang/Object"), is(notNullValue()));
        assertThat(hier.getClassFileClassArray(0, "java/util/List"), is(notNullValue()));
        assertThat(hier.getClassFileClassArray(0, "java/util/Collection"), is(notNullValue()));
        assertThat(hier.getClassFileClassArray(0, "java/lang/Iterable"), is(notNullValue()));
        assertThat(hier.getClassFileClassArray(0, "java/util/RandomAccess"), is(notNullValue()));
        assertThat(hier.getClassFileClassArray(0, "java/lang/Cloneable"), is(notNullValue()));
        assertThat(hier.getClassFileClassArray(0, "java/io/Serializable"), is(notNullValue()));
    }
    
    @Test
    public void testLoadClassPrimitiveFloat() throws InvalidClassFileFactoryClassException, InvalidInputException, 
    ClassFileNotFoundException, ClassFileIllFormedException, ClassFileNotAccessibleException, 
    IncompatibleClassFileException, BadClassFileVersionException, RenameUnsupportedException, WrongClassNameException {
        final ClassHierarchy hier = new ClassHierarchy(cp, ClassFileFactoryJavassist.class, Collections.emptyMap(), Collections.emptyMap());
        final ClassFile cf = hier.getClassFilePrimitiveOrVoid("float");
        assertThat(cf.getClassName(), is("float"));
        assertThat(cf.getDeclaredConstructors().length, is(0));
        assertThat(cf.getDeclaredFields().length, is(0));
        assertThat(cf.getDeclaredMethods().length, is(0));
        assertThat(cf.getPackageName(), is(""));
        assertThat(cf.getSourceFile(), is(""));
        assertThat(cf.isArray(), is(false));
        assertThat(cf.isPrimitiveOrVoid(), is(true));
        assertThat(cf.isReference(), is(false));
        assertThat(cf.isPackage(), is(false));
        assertThat(cf.isPrivate(), is(false));
        assertThat(cf.isProtected(), is(false));
        assertThat(cf.isPublic(), is(true));
        assertThat(cf.isAnonymous(), is(false));
        assertThat(cf.isAnonymousUnregistered(), is(false));
    }
}
