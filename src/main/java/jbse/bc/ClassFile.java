package jbse.bc;

import static jbse.bc.Signatures.ASSERTIONDISABLED_NAME;
import static jbse.bc.Signatures.JAVA_OBJECT;
import static jbse.bc.Signatures.JAVA_STRING;
import static jbse.common.Type.REFERENCE;
import static jbse.common.Type.TYPEEND;
import static jbse.common.Type.className;
import static jbse.common.Type.isCat_1;
import static jbse.common.Type.splitParametersDescriptors;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import jbse.bc.exc.AttributeNotFoundException;
import jbse.bc.exc.ClassFileIllFormedException;
import jbse.bc.exc.FieldNotFoundException;
import jbse.bc.exc.InvalidIndexException;
import jbse.bc.exc.MethodCodeNotFoundException;
import jbse.bc.exc.MethodNotFoundException;
import jbse.bc.exc.RenameUnsupportedException;
import jbse.common.Type;
import jbse.common.exc.InvalidInputException;
import jbse.common.exc.UnexpectedInternalException;

/**
 * Abstract class for managing the information on a single 
 * class file.
 */
public abstract class ClassFile implements Comparable<ClassFile> {
    public static final int JAVA_1 = 45;
    public static final int JAVA_2 = 46;
    public static final int JAVA_3 = 47;
    public static final int JAVA_4 = 48;
    public static final int JAVA_5 = 49;
    public static final int JAVA_6 = 50;
    public static final int JAVA_7 = 51;
    public static final int JAVA_8 = 52;
    public static final int JAVA_9 = 53;
    
    protected static final String JAR_FILE_EXTENSION = ".jar";
    
    /**
     * Returns the content of the binary file for this class.
     * 
     * @return a {@code byte[]} if {@link #isDummy()}, otherwise
     *         it may be {@code null}.
     */
    public abstract byte[] getBinaryFileContent();

    /**
     * Returns the name of the source code file where this 
     * class has been declared.
     * 
     * @return the name of the source code file as a {@link String}, 
     *         or the empty {@link String} in case it has none
     *         (array classes, primitive classes, snippet classfiles).
     */
    public abstract String getSourceFile();
    
    /**
     * Gets the major version number of this class.
     * 
     * @return an {@code int}.
     */
    public abstract int getMajorVersion();
    
    /**
     * Gets the minor version number of this class.
     * 
     * @return an {@code int}.
     */
    public abstract int getMinorVersion();

    /**
     * Returns the name of the class.
     * 
     * @return the name of the class.
     */
    public abstract String getClassName();
    
    /**
     * Renames this class.
     * 
     * @param classNameNew a {@link String}, the new name
     *        for this class.
     * @throws RenameUnsupportedException if the class cannot
     *         be renamed.
     */
    public abstract void rename(String classNameNew) throws RenameUnsupportedException;
    
    /**
     * Returns the internal type name for 
     * this class.
     * 
     * @return a {@link String}, the internal 
     *         type name for this class.
     */
    public abstract String getInternalTypeName();
    
    /**
     * Returns the defining classloader identifier 
     * for this class.
     * 
     * @return an {@code int}.
     */
    public abstract int getDefiningClassLoader();

    /**
     * Returns the name of the package where this class has been declared.
     * 
     * @return the package name of this class as a {@link String}.
     */
    public abstract String getPackageName();
    
    /**
     * Returns the generic signature (type) of this class.
     * 
     * @return a {@link String} or {@code null} if the class has not a generic signature.
     */
    public abstract String getGenericSignatureType();
    
    /**
     * Returns the modifiers of the class.
     * The result must be equal to that returned
     * by {@link java.lang.Class#getModifiers()}.
     * 
     * @return an {@code int} codifying the modifiers.
     */
    public abstract int getModifiers();

    /**
     * Returns the access flags stored in the classfile. 
     * Note that for inner classes these flags may differ
     * from those returned by {@link java.lang.Class#getModifiers()}, 
     * but rather correspond to those returned by {@link sun.reflect.Reflection#getClassAccessFlags(Class)}.
     *  
     * @return an {@code int} codifying the access flags.
     */
    public abstract int getAccessFlags();

    /** 
     * Test whether the class is dummy, i.e., 
     * it is a lightweight wrapper for accessing
     * the content of a classfile but cannot be
     * used for other purposes because it is not
     * linked to other {@link ClassFile}s.
     * 
     * @return {@code true} iff the class is dummy.
     */
    public abstract boolean isDummy();
    
    /**
     * Test whether the class is an array class.
     * 
     * @return {@code true} iff the class is an array class.
     */
    public abstract boolean isArray();
    
    /**
     * Test whether the class is an enum class.
     * 
     * @return {@code true} iff the class is an enum class.
     */
    public abstract boolean isEnum();

    /**
     * Tests whether the class is primitive or void.
     * 
     * @return {@code true} iff the class is primitive or void.
     */
    public abstract boolean isPrimitiveOrVoid();

    /**
     * Equivalent to {@code !}{@link #isPrimitiveOrVoid()}{@code  && }{@code !}{@link #isArray()}.
     * 
     * @return {@code true} iff the class is the class for an object type.
     */
    public final boolean isReference() {
        return !isPrimitiveOrVoid() && !isArray();
    }

    /**
     * Test whether the class is an interface.
     * 
     * @return {@code true} iff the class is an interface.
     */
    public abstract boolean isInterface();

    /**
     * Tests whether the class is abstract.
     * 
     * @return {@code true} iff the class is abstract.
     */
    public abstract boolean isAbstract();

    /**
     * Tests whether the class is final.
     * 
     * @return {@code true} iff the class is final.
     */
    public abstract boolean isFinal();

    /**
     * Tests whether the class has public visibility.
     * 
     * @return {@code true} iff the class is public.
     */
    public abstract boolean isPublic();

    /**
     * Tests whether the class has protected visibility.
     * 
     * @return {@code true} iff the class has protected visibility.
     */
    public abstract boolean isProtected();

    /**
     * Tests whether the class has package visibility.
     * 
     * @return {@code true} iff the class has package visibility.
     */
    public abstract boolean isPackage();

    /**
     * Tests whether the class has private visibility.
     * 
     * @return {@code true} iff the class has protected visibility.
     */
    public abstract boolean isPrivate();

    /**
     * Tests whether the class is static.
     * 
     * @return {@code true} iff the class is static.
     */
    public abstract boolean isStatic();
    
    /**
     * Tests the {@code invokespecial} bytecode semantics required
     * for this class.
     *  
     * @return {@code true} if the ACC_SUPER flag of the class 
     * is set, i.e., if the class requires post-Java 1.1 semantics,
     * {@code false} if the class requires backward-compatible 
     * semantics (Java 1.0.2 and previous).
     */
    public abstract boolean isSuperInvoke();
    
    /**
     * Tests whether the class is local (JLS v8, section 14.3).
     * 
     * @return {@code true} iff the class is local.
     */
    public abstract boolean isLocal();
    
    /**
     * Tests whether the class is anonymous (JLS v8, section 15.9.5).
     * 
     * @return {@code true} iff the class is anonymous.
     */
    public abstract boolean isAnonymous();
    
    /**
     * Returns all the annotations of the class (raw classfile format).
     * 
     * @return a {@link byte}{@code []} containing all the annotations 
     *         of the class.
     */
    public abstract byte[] getClassAnnotationsRaw();

    /**
     * Returns the class for the member type
     * of an array class.
     * 
     * @return a {@link ClassFile}, or {@code null}
     * if {@code !}{@link #isArray()}.
     */
    public abstract ClassFile getMemberClass();

    /**
     * Returns the class for the i-th level of
     * membership type of a multiarray class.
     * 
     * @param level how many levels of nesting
     *        must be skipped. For {@code level == 0}
     *        the method returns {@code this}, 
     *        for {@code level == 1} the method
     *        returns {@link #getMemberClass()}, 
     *        when {@code level} is greater than
     *        the maximum level of nesting of the
     *        array type the method returns {@code null}.
     * @return
     */
    public final ClassFile getMemberClass(int level) {
        ClassFile retVal = this;
        for (int i = 0; i < level; ++i) {
            retVal = retVal.getMemberClass();
            if (retVal == null) {
                break;
            }
        }
        return retVal;
    }
    
    /**
     * Checks whether the class is anonymous 
     * (not in the sense of JLS v8, section 15.9.5 but 
     * in the sense of  
     * {@link sun.misc.Unsafe#defineAnonymousClass}).
     * 
     * @return {@code true} iff the class is anonymous.
     */
    public abstract boolean isAnonymousUnregistered();
    
    /**
     * Returns the host class name for an anonymous 
     * (not in the sense of JLS v8, section 15.9.5 but 
     * in the sense of  
     * {@link sun.misc.Unsafe#defineAnonymousClass}).
     * 
     * @return a {@code ClassFile}, the host class
     *         for this class, or {@code null} if 
     *         {@link #isAnonymousUnregistered()}{@code  == false}
     */
    public abstract ClassFile getHostClass();
    
    /**
     * If this class is nested (statically nested, inner, anonymous 
     * or local), returns the name of the class containing this class.
     * 
     * @return A {@link String}, the name of the class containing
     *         this class, or {@code null} in case
     *         this class is not nested.
     */
    public abstract String classContainer();
    
    /**
     * If this class is local or anonymous returns the {@link Signature} of the
     * enclosing method or constructor where the class is declared.
     *  
     * @return The {@link Signature} of the enclosing method or
     *         constructor, or {@code null} if this class is neither
     *         local nor anonymous. In the (quite unlikely) case the 
     *         class is local or anonymous, and it is not immediately 
     *         enclosed (JVMS v8, section 4.7.7) by a method or constructor,
     *         it will return a {@link Signature} {@code retVal} such that
     *         {@code retVal.}{@link Signature#getDescriptor() getDescriptor()}{@code  == null},
     *         {@code retVal.}{@link Signature#getName() getName()}{@code  == null}, and
     *         {@code retVal.}{@link Signature#getClassName() getClassName()} is the name
     *         of the enclosing class.
     * @throws ClassFileNotFoundException if the enclosing class does not 
     *         exist in the classpath.
     */
    public abstract Signature getEnclosingMethodOrConstructor();
    
    /**
     * Does a best-effort check of whether the class is pure. The
     * answer is sound, in that if the method returns {@code true}
     * the class is surely pure (but it may return {@code false} 
     * for some pure classes).
     * 
     * @return {@code true} iff all the static fields are private and
     *         final and have primitive or {@link String} type, and
     *         the class has no static constructor.
     */
    public final boolean isPure() {
        for (Signature sigMethod : getDeclaredMethods()) {
            if ("<clinit>".equals(sigMethod.getName())) {
                return false;
            }
        }
        for (Signature sigFieldStatic : getDeclaredFieldsStatic()) {
            try {
                //skips field $assertionDisabled
                if (ASSERTIONDISABLED_NAME.equals(sigFieldStatic.getName())) {
                    continue;
                }
                if (!isFieldFinal(sigFieldStatic)) {
                    return false;
                }
                if (!Type.isPrimitive(sigFieldStatic.getDescriptor())) {
                    final String className = className(sigFieldStatic.getDescriptor());
                    if (!JAVA_STRING.equals(className)) {
                        return false;
                    }
                }
            } catch (FieldNotFoundException e) {
                //this should never happen
                throw new UnexpectedInternalException("Classfile " + getClassName() + " cannot find declared static field " + sigFieldStatic.toString() + ".");
            }
        }
        return true;
    }

    /**
     * Returns the size of the constant pool.
     * Indices to the constant pool must range
     * from {@code 1} to {@code constantPoolSize() - 1}
     * inclusive.
     * 
     * @return a {@code int}.
     */
    public abstract int constantPoolSize();

    /**
     * Tests whether the class has a declaration for a method with a given 
     * signature. 
     * 
     * @param methodSignature the {@link Signature} of the method. 
     *        Only the name of the method and its descriptor 
     *        are considered (the signature's class name 
     *        is ignored).
     * @return {@code true} iff the class declares a 
     *         method with signature {@code methodSignature}.
     *         If {@code methodSignature}'s name is signature polymorphic in this 
     *         class the check must succeed for all the polymorphic variants of the
     *         signature and return {@code true}.
     */
    public abstract boolean hasMethodDeclaration(Signature methodSignature);

    /**
     * Tests whether the class has an implementation (i.e., a declaration 
     * plus bytecode) for a method with a given signature. 
     * 
     * @param methodSignature the {@link Signature} of the method. 
     *        Only the name of the method and its descriptor 
     *        are considered (the signature's class name 
     *        is ignored).
     * @return {@code true} if the class has an implementation for 
     *         the method with signature {@code methodSignature}, 
     *         {@code false} otherwise.
     *         If {@code methodSignature}'s name is signature polymorphic in this 
     *         class the check must succeed for all the polymorphic variants of the
     *         signature and return {@code false}.
     */
    public abstract boolean hasMethodImplementation(Signature methodSignature);
    
    /**
     * Tests whether the class has exactly one declaration for a signature polymorphic 
     * method with a given name. The check that the method is signature polymorphic is
     * done according to JVMS v8, section 2.9.
     * 
     * @param methodName a {@code String}, the method name
     * @return {@code true} iff the class declares exactly one
     *         method with name {@code methodName}, and the method
     *         is signature polymorphic.
     */
    public abstract boolean hasOneSignaturePolymorphicMethodDeclaration(String methodName);

    /**
     * Tests whether a method in the class is declared abstract.
     * 
     * @param methodSignature the signature of the method to be checked.
     * @return {@code true} iff the method is abstract.
     *         If {@code methodSignature}'s name is signature polymorphic in this 
     *         class the check must succeed for all the polymorphic variants of the
     *         signature and return the same result as for the {@code (Object...)Object}
     *         declared variant.
     * @throws MethodNotFoundException iff {@link #hasMethodDeclaration}{@code (methodSignature) == false}.
     */
    public abstract boolean isMethodAbstract(Signature methodSignature) throws MethodNotFoundException;

    /**
     * Tests whether a method in the class is declared static.
     * 
     * @param methodSignature the {@link Signature} of the method to be checked.
     * @return {@code true} iff the method is static.
     *         If {@code methodSignature}'s name is signature polymorphic in this 
     *         class the check must succeed for all the polymorphic variants of the
     *         signature and return the same result as for the {@code (Object...)Object}
     *         declared variant.
     * @throws MethodNotFoundException iff {@link #hasMethodDeclaration}{@code (methodSignature) == false}.
     */
    public abstract boolean isMethodStatic(Signature methodSignature) throws MethodNotFoundException;

    /**
     * Tests whether a method in the class is declared public.
     * 
     * @param methodSignature the {@link Signature} of the method to be checked.
     * @return {@code true} iff the method is public.
     *         If {@code methodSignature}'s name is signature polymorphic in this 
     *         class the check must succeed for all the polymorphic variants of the
     *         signature and return the same result as for the {@code (Object...)Object}
     *         declared variant.
     * @throws MethodNotFoundException iff {@link #hasMethodDeclaration}{@code (methodSignature) == false}.
     */
    public abstract boolean isMethodPublic(Signature methodSignature) throws MethodNotFoundException;

    /**
     * Tests whether a method in the class is declared protected.
     * 
     * @param methodSignature the {@link Signature} of the method to be checked.
     * @return {@code true} if the method is protected.
     *         If {@code methodSignature}'s name is signature polymorphic in this 
     *         class the check must succeed for all the polymorphic variants of the
     *         signature and return the same result as for the {@code (Object...)Object}
     *         declared variant.
     * @throws MethodNotFoundException iff {@link #hasMethodDeclaration}{@code (methodSignature) == false}.
     */
    public abstract boolean isMethodProtected(Signature methodSignature) throws MethodNotFoundException;

    /**
     * Tests whether a method in the class is declared with package visibility.
     * 
     * @param methodSignature the {@link Signature} of the method to be checked.
     * @return {@code true} iff the method has package visibility.
     *         If {@code methodSignature}'s name is signature polymorphic in this 
     *         class the check must succeed for all the polymorphic variants of the
     *         signature and return the same result as for the {@code (Object...)Object}
     *         declared variant.
     * @throws MethodNotFoundException iff {@link #hasMethodDeclaration}{@code (methodSignature) == false}.
     */
    public abstract boolean isMethodPackage(Signature methodSignature) throws MethodNotFoundException;

    /**
     * Tests whether a method in the class is declared private.
     * 
     * @param methodSignature the {@link Signature} of the method to be checked.
     * @return {@code true} if the method is private.
     *         If {@code methodSignature}'s name is signature polymorphic in this 
     *         class the check must succeed for all the polymorphic variants of the
     *         signature and return the same result as for the {@code (Object...)Object}
     *         declared variant.
     * @throws MethodNotFoundException iff {@link #hasMethodDeclaration}{@code (methodSignature) == false}.
     */
    public abstract boolean isMethodPrivate(Signature methodSignature) throws MethodNotFoundException;

    /**
     * Tests whether a method in the class is declared native.
     * 
     * @param methodSignature the {@link Signature} of the method to be checked.
     * @return {@code true} iff the method is native.
     *         If {@code methodSignature}'s name is signature polymorphic in this 
     *         class the check must succeed for all the polymorphic variants of the
     *         signature and return the same result as for the {@code (Object...)Object}
     *         declared variant.
     * @throws MethodNotFoundException iff {@link #hasMethodDeclaration}{@code (methodSignature) == false}.
     */
    public abstract boolean isMethodNative(Signature methodSignature) throws MethodNotFoundException;

    /**
     * Tests whether a method in the class is declared varargs.
     * 
     * @param methodSignature the {@link Signature} of the method to be checked.
     * @return {@code true} iff the method is varargs.
     *         If {@code methodSignature}'s name is signature polymorphic in this 
     *         class the check must succeed for all the polymorphic variants of the
     *         signature and return the same result as for the {@code (Object...)Object}
     *         declared variant.
     * @throws MethodNotFoundException iff {@link #hasMethodDeclaration}{@code (methodSignature) == false}.
     */
    public abstract boolean isMethodVarargs(Signature methodSignature) throws MethodNotFoundException;

    /**
     * Tests whether a method in the class is final.
     * 
     * @param methodSignature the {@link Signature} of the method to be checked.
     * @return {@code true} iff the method is final.
     *         If {@code methodSignature}'s name is signature polymorphic in this 
     *         class the check must succeed for all the polymorphic variants of the
     *         signature and return the same result as for the {@code (Object...)Object}
     *         declared variant.
     * @throws MethodNotFoundException iff {@link #hasMethodDeclaration}{@code (methodSignature) == false}.
     */
    public abstract boolean isMethodFinal(Signature methodSignature) throws MethodNotFoundException;

    /**
     * Tests whether a method in the class is signature polymorphic
     * (see JVMS v8, section 2.9).
     * 
     * @param methodSignature the {@link Signature} of the method to be checked.
     * @return {@code true} iff the method is signature polymorphic.
     * @throws MethodNotFoundException iff {@link #hasMethodDeclaration}{@code (methodSignature) == false}.
     */
    public final boolean isMethodSignaturePolymorphic(Signature methodSignature) throws MethodNotFoundException {
        return hasOneSignaturePolymorphicMethodDeclaration(methodSignature.getName());
    }
    
    /**
     * Tests whether a method in the class is caller sensitive.
     * 
     * @param methodSignature the {@link Signature} of the method to be checked.
     * @return {@code true} iff the method is caller sensitive.
     *         If {@code methodSignature}'s name is signature polymorphic in this 
     *         class the check must succeed for all the polymorphic variants of the
     *         signature and return the same result as for the {@code (Object...)Object}
     *         declared variant.
     * @throws MethodNotFoundException iff {@link #hasMethodDeclaration}{@code (methodSignature) == false}.
     */
    public abstract boolean isMethodCallerSensitive(Signature methodSignature) throws MethodNotFoundException;
    
    /**
     * Returns the generic signature (type) of a method.
     * 
     * @param methodSignature the {@link Signature} of a method.
     * @return a {@link String} or {@code null} if the method has not a generic signature.
     *         If {@code methodSignature}'s name is signature polymorphic in this 
     *         class the method must succeed for all the polymorphic variants of the
     *         signature and return the unmodified generic signature of the 
     *         {@code (Object...)Object} declared variant.
     * @throws MethodNotFoundException iff {@link #hasMethodDeclaration}{@code (methodSignature) == false}.
     */
    public abstract String getMethodGenericSignatureType(Signature methodSignature) 
    throws MethodNotFoundException;

    /**
     * Returns the encoded modifiers of a method.
     * 
     * @param methodSignature the {@link Signature} of a method.
     * @return an {@code int}, the encoded modifiers.
     *         If {@code methodSignature}'s name is signature polymorphic in this 
     *         class the method must succeed for all the polymorphic variants of the
     *         signature and return the modifiers for the 
     *         {@code (Object...)Object} declared variant.
     * @throws MethodNotFoundException iff {@link #hasMethodDeclaration}{@code (methodSignature) == false}.
     */
    public abstract int getMethodModifiers(Signature methodSignature)
    throws MethodNotFoundException;

    /**
     * Returns all the annotations of a method (raw classfile format).
     * 
     * @param methodSignature the {@link Signature} of a method.
     * @return a {@link byte}{@code []} containing all the annotations of the method.
     *         If {@code methodSignature}'s name is signature polymorphic in this 
     *         class the method must succeed for all the polymorphic variants of the
     *         signature and return the annotations for the 
     *         {@code (Object...)Object} declared variant.
     * @throws MethodNotFoundException iff {@link #hasMethodDeclaration}{@code (methodSignature) == false}.
     */
    public abstract byte[] getMethodAnnotationsRaw(Signature methodSignature) 
    throws MethodNotFoundException;

    /**
     * Returns all the annotations of a method that are available on the current classpath.
     * 
     * @param methodSignature the {@link Signature} of a method.
     * @return an {@link String}{@code []} containing all the names of the annotations of the method (just the class name).
     *         If {@code methodSignature}'s name is signature polymorphic in this 
     *         class the method must succeed for all the polymorphic variants of the
     *         signature and return the annotation names for the 
     *         {@code (Object...)Object} declared variant.
     * @throws MethodNotFoundException iff {@link #hasMethodDeclaration}{@code (methodSignature) == false}.
     */
    public abstract String[] getMethodAvailableAnnotations(Signature methodSignature) 
    throws MethodNotFoundException;

    /**
     * Gets the value of an annotation parameter on a method annotation, in the case
     * the parameter's value is a {@link String}.
     * 
     * @param methodSignature the {@link Signature} of the method. Only the name and 
     *        descriptor are considered.
     * @param annotation a {@code String}, the name of the annotation to look for.
     * @param parameter a {@code String}, the name of the annotation's parameter to look for.
     * @return a {@link String}, the value of parameter {@code parameter} for 
     *         annotation {@code annotation}, or {@code null} if {@code annotation}
     *         is not present, or is present and {@code parameter} is not present, 
     *         or {@code parameter} is present and has default value or is not a {@link String}.
     *         If {@code methodSignature}'s name is signature polymorphic in this 
     *         class the method must succeed for all the polymorphic variants of the
     *         signature and return the same annotation parameter for the 
     *         {@code (Object...)Object} declared variant.
     * @throws MethodNotFoundException if {@code classFileMethod} does not contain a 
     *         method with name and descriptor as {@code methodSignature}.
     */
    public abstract String getMethodAnnotationParameterValueString(Signature methodSignature, String annotation, String parameter) 
    throws MethodNotFoundException;
    
    /**
     * Given the signature of a method, returns the information on its 
     * parameters according to the associated {@code MethodParameters_attribute}. 
     * 
     * @param methodSignature a method's {@link Signature}.
     * @return a {@link ParameterInfo}{@code []}. The order of the parameters 
     *         is the same as that in {@code method signature}. If the method
     *         has an implicit {@code this} parameter, its {@link ParameterInfo}
     *         has position {@code 0}. If {@code methodSignature} has no associated 
     *         {@code MethodParameters_attribute} this method returns {@code null}.
     * @throws MethodNotFoundException iff the method does not exist in the class.
     */
    public abstract ParameterInfo[] getMethodParameters(Signature methodSignature)
    throws MethodNotFoundException;

    /**
     * Returns the list of the exception that a method declares to throw.
     * 
     * @param methodSignature the {@link Signature} of a method.
     * @return a {@link String}{@code []} containing the class names of the exceptions that the 
     *         method declares to throw.
     *         If {@code methodSignature}'s name is signature polymorphic in this 
     *         class the method must succeed for all the polymorphic variants of the
     *         signature and return the exceptions for the 
     *         {@code (Object...)Object} declared variant.
     * @throws MethodNotFoundException iff {@link #hasMethodDeclaration}{@code (methodSignature) == false}.
     */
    public abstract String[] getMethodThrownExceptions(Signature methodSignature)
    throws MethodNotFoundException;

    /**
     * Given the signature of a method, returns its exception table.
     * 
     * @param MethodSignature the {@link Signature} of a method.
     * @return the {@link ExceptionTable} of the method (empty in the case 
     *         the method has no exception handler).
     *         If {@code methodSignature}'s name is signature polymorphic in this 
     *         class the method must succeed for all the polymorphic variants of the
     *         signature and return the exception table for the 
     *         {@code (Object...)Object} declared variant.
     * @throws MethodNotFoundException iff {@link #hasMethodDeclaration}{@code (methodSignature) == false}.
     * @throws MethodCodeNotFoundException iff the method has not the Code attribute.
     * @throws InvalidIndexException iff the exception type field in a row of the exception table 
     *         does not contain the index of a valid CONSTANT_Class in the class constant pool.
     */
    public abstract ExceptionTable getExceptionTable(Signature methodSignature) 
    throws MethodNotFoundException, MethodCodeNotFoundException, InvalidIndexException;

    /**
     * Given the signature of a method, returns a local variable table for that method.
     * 
     * @param methodSignature the structure that contains the signature of a method.
     * @return a {@link LocalVariableTable} for the method containing the local variable table
     *         of the method. If no LocalVariableTable attribute is found, the method returns 
     *         a default {@link LocalVariableTable} that is built from {@code methodSignature}.
     *         If {@code methodSignature}'s name is signature polymorphic in this 
     *         class the method must succeed for all the polymorphic variants of the
     *         signature and return the same result as for the 
     *         {@code (Object...)Object} declared variant.
     * @throws MethodNotFoundException iff {@link #hasMethodDeclaration}{@code (methodSignature) == false}.
     * @throws MethodCodeNotFoundException iff the method has not the Code attribute.
     */
    public abstract LocalVariableTable getLocalVariableTable(Signature methodSignature) 
    throws MethodNotFoundException, MethodCodeNotFoundException;    

    /**
     * Given the signature of a method, returns a local variable type table for that method.
     * 
     * @param methodSignature the structure that contains the signature of a method.
     * @return a {@link LocalVariableTable} for the method containing the local variable type table
     *         of the method. If no LocalVariableTypeTable attribute is found, the method returns
     *         an empty {@link LocalVariableTable}.
     *         If {@code methodSignature}'s name is signature polymorphic in this 
     *         class the method must succeed for all the polymorphic variants of the
     *         signature and return the same result as for the 
     *         {@code (Object...)Object} declared variant.
     * @throws MethodNotFoundException iff {@link #hasMethodDeclaration}{@code (methodSignature) == false}.
     * @throws MethodCodeNotFoundException iff the method has not the Code attribute.
     */
    public abstract LocalVariableTable getLocalVariableTypeTable(Signature methodSignature) 
    throws MethodNotFoundException, MethodCodeNotFoundException;    

    /**
     * Returns the length of the local variable table of a method.
     * 
     * @param methodSignature a method's {@link Signature}.
     * @return a {@code int} representing the length in bytes of the 
     *         local variable table of the method with signature 
     *         {@code methodSignature}, in the case such method exists 
     *         in the class and has code.
     *         If {@code methodSignature}'s name is signature polymorphic in this 
     *         class the method must succeed for all the polymorphic variants of the
     *         signature and return the same result as for the 
     *         {@code (Object...)Object} declared variant.
     * @throws MethodNotFoundException iff the method does not exist in the class.
     * @throws MethodCodeNotFoundException iff the method has not the 
     *         Code attribute.
     */
    public abstract int getLocalVariableTableLength(Signature methodSignature) 
    throws MethodNotFoundException, MethodCodeNotFoundException;
    
    public static class ParameterInfo {
    	public final String name;
    	public final int accessFlags;
    	
    	public ParameterInfo(String name, int accessFlags) {
    		this.name = name;
    		this.accessFlags = accessFlags;
    	}
    }
    
    /**
     * Returns a method's {@link LineNumberTable}. 
     * 
     * @param methodSignature a method's {@link Signature}.
     * @return the {@link LineNumberTable} for the method with signature {@code methodSignature}.
     *         If {@code methodSignature}'s name is signature polymorphic in this 
     *         class the method must succeed for all the polymorphic variants of the
     *         signature and return the {@link LineNumberTable} for the 
     *         {@code (Object...)Object} declared variant.
     * @throws MethodNotFoundException iff {@link #hasMethodDeclaration}{@code (methodSignature) == false}.
     * @throws MethodCodeNotFoundException iff the method has not the Code attribute.
     */
    public abstract LineNumberTable getLineNumberTable(Signature methodSignature) 
    throws MethodNotFoundException, MethodCodeNotFoundException;    

    /**
     * Returns the length of the bytecode of a method.
     * 
     * @param methodSignature a {@link Signature}.
     * @return a {@code int} representing the length in bytes of the 
     *         bytecode of the method with signature 
     *         {@code methodSignature}, in the case such method exists 
     *         in the class and has code.
     *         If {@code methodSignature}'s name is signature polymorphic in this 
     *         class the method must succeed for all the polymorphic variants of the
     *         signature and return the same result as for the 
     *         {@code (Object...)Object} declared variant.
     * @throws MethodNotFoundException iff the method does not exist in the class.
     * @throws MethodCodeNotFoundException iff the method has not the 
     *         Code attribute.
     */
    public abstract int getCodeLength(Signature methodSignature) 
    throws MethodNotFoundException, MethodCodeNotFoundException;

    /**
     * Creates a default local variable table from a method's signature.
     * 
     * @param methodSignature the {@link Signature} of a method.
     * @return a {@link LocalVariableTable} containing entries 
     *         inferred from the method's parameters.
     * @throws MethodNotFoundException iff {@link #hasMethodDeclaration}{@code (methodSignature) == false}.
     * @throws MethodCodeNotFoundException iff the method has not the Code attribute.
     */
    protected final LocalVariableTable defaultLocalVariableTable(Signature methodSignature) 
    throws MethodNotFoundException, MethodCodeNotFoundException {
        //if no LocalVariableTable attribute is found, tries to create the local 
        //variable table from information on the method's signature
        boolean isStatic = isMethodStatic(methodSignature);
        final String[] parDescList = splitParametersDescriptors(methodSignature.getDescriptor());
        final LocalVariableTable lvt = new LocalVariableTable(getLocalVariableTableLength(methodSignature));
        int i = 0;
        short slot = 0;
        if (!isStatic) {
            lvt.addRow(slot, REFERENCE + this.getClassName() + TYPEEND, 
                         "this", 0, this.getCodeLength(methodSignature));
            ++i; ++slot;
        }
        for (String descriptor : parDescList) {
            lvt.addRow(slot, descriptor, 
                         "__PARAM[" + i + "]", 0, this.getCodeLength(methodSignature));
            ++i; ++slot;
            if (!isCat_1(descriptor.charAt(0))) {
                ++slot;
            }
        }
        return lvt;
    }

    /**
     * Creates the default line number table to be returned in 
     * the case a method has not the LineNumberTable attribute.
     * 
     * @return the default (empty) {@link LineNumberTable}.
     */
    protected final LineNumberTable defaultLineNumberTable() {
        return new LineNumberTable(0);
    }

    /**
     * Given a CONSTANT_Methodref index in the constant pool, returns the array 
     * of byte code; it is equivalent to 
     * {@code getMethodCodeBySignature(getMethodSignature(methodRef))}.
     * 
     * @param methodRef the CONSTANT_Methodref of searched method
     * @return a {@code byte[]} containing the method's byte code.
     * @throws InvalidIndexException iff {@code methodRef} is not the index of a valid 
     *         CONSTANT_MethodRef in the class constant pool.
     * @throws MethodNotFoundException iff 
     * {@link #hasMethodDeclaration}{@code (}{@link #getMethodSignature}{@code (methodRef)) == false}.
     * @throws MethodCodeNotFoundException iff the method has not the Code attribute.
     */
    public final byte[] getMethodCodeByMethodRef(int methodRef) 
    throws InvalidIndexException, MethodNotFoundException, MethodCodeNotFoundException {
        return getMethodCodeBySignature(getMethodSignature(methodRef));
    }

    /**
     * Given the signature of a method, returns the bytecode of 
     * the method. 
     * 
     * @param methodSignature the {@link Signature} of a method.
     * @return a {@code byte[]} containing the bytecode.
     *         If {@code methodSignature}'s name is signature polymorphic in this 
     *         class the method must succeed for all the polymorphic variants of the
     *         signature and return the same result as for the 
     *         {@code (Object...)Object} declared variant.
     * @throws MethodNotFoundException iff {@link #hasMethodDeclaration}{@code (methodSignature) == false}.
     * @throws MethodCodeNotFoundException iff the method has not the Code attribute.
     */
    public abstract byte[] getMethodCodeBySignature(Signature methodSignature) 
    throws MethodNotFoundException, MethodCodeNotFoundException;

    /**
     * Returns a constant pool value (only for primitive, String, Uf8, Class, MethodType, 
     * or MethodHandle constants).
     * 
     * @param index an {@code int}, a constant pool index.
     * @return a {@link ConstantPoolValue} for the value contained in the constant pool 
     *         at the index, if such value is a primitive, String, Utf8 or Class.
     * @throws InvalidIndexException iff the constant pool has less entries than {@code index}, or
     *         {@code index} does not refer to a CONSTANT_Integer, CONSTANT_Long, CONSTANT_Float,
     *         CONSTANT_Double, CONSTANT_Utf8, CONSTANT_String, CONSTANT_Class, CONSTANT_MethodType, 
     *         CONSTANT_MethodHandle.
     * @throws ClassFileIllFormedException if the class file is ill formed.
     */
    public abstract ConstantPoolValue getValueFromConstantPool(int index) 
    throws InvalidIndexException, ClassFileIllFormedException;

    /**
     * Checks whether the class declares a field.
     * 
     * @param fieldSignature the {@link Signature} of a field. The 
     * class of the signature is ignored.
     * @return {@code true} iff the class declares a field with  
     * {@code fieldSignature}'s name and type.
     */
    public abstract boolean hasFieldDeclaration(Signature fieldSignature);

    /**
     * Tests whether a field in the class is declared final.
     * 
     * @param fieldSignature the {@link Signature} of the field to be checked.
     * @return {@code true} iff the field is final.
     * @throws FieldNotFoundException iff {@link #hasFieldDeclaration}{@code (fieldSignature) == false}.
     */
    public abstract boolean isFieldFinal(Signature fieldSignature) throws FieldNotFoundException;

    /**
     * Tests whether a field in the class is declared public.
     * 
     * @param fieldSignature the {@link Signature} of the field to be checked.
     * @return {@code true} iff the field is public.
     * @throws FieldNotFoundException iff {@link #hasFieldDeclaration}{@code (fieldSignature) == false}.
     */
    public abstract boolean isFieldPublic(Signature fieldSignature) throws FieldNotFoundException;

    /**
     * Tests whether a field in the class is declared protected.
     * 
     * @param fieldSignature the {@link Signature} of the field to be checked.
     * @return {@code true} iff the field is protected.
     * @throws FieldNotFoundException iff {@link #hasFieldDeclaration}{@code (fieldSignature) == false}.
     */
    public abstract boolean isFieldProtected(Signature fieldSignature) throws FieldNotFoundException;	

    /**
     * Tests whether a field in the class is declared with package visibility.
     * 
     * @param fieldSignature the {@link Signature} of the field to be checked.
     * @return {@code true} iff the field has package visibility.
     * @throws FieldNotFoundException iff {@link #hasFieldDeclaration}{@code (fieldSignature) == false}.
     */
    public abstract boolean isFieldPackage(Signature fieldSignature) throws FieldNotFoundException;

    /**
     * Tests whether a field in the class is declared private.
     * 
     * @param fieldSignature the {@link Signature} of the field to be checked.
     * @return {@code true} iff the field is private.
     * @throws FieldNotFoundException iff {@link #hasFieldDeclaration}{@code (fieldSignature) == false}.
     */
    public abstract boolean isFieldPrivate(Signature fieldSignature) throws FieldNotFoundException;

    /**
     * Tests whether a field in the class is declared static.
     * 
     * @param fieldSignature the {@link Signature} of the field to be checked. 
     *        The class of the signature is ignored.
     * @return {@code true} iff the field is static.
     * @throws FieldNotFoundException iff {@link #hasFieldDeclaration}{@code (fieldSignature) == false}.
     */
    public abstract boolean isFieldStatic(Signature fieldSignature) throws FieldNotFoundException;

    /**
     * Tests whether a field in the class has a ConstantValue attribute.
     * 
     * @param fieldSignature the {@link Signature} of the field to be checked.
     * @return {@code true} iff the field is constant.
     * @throws FieldNotFoundException iff {@link #hasFieldDeclaration}{@code (fieldSignature) == false}.
     */
    public abstract boolean hasFieldConstantValue(Signature fieldSignature) throws FieldNotFoundException;

    /**
     * Tests whether a field in the class is both final and has a ConstantValue attribute.
     * 
     * @param fieldSignature the {@link Signature} of the field to be checked.
     * @return {@code true} iff the field is both final and constant.
     * @throws FieldNotFoundException iff {@link #hasFieldDeclaration}{@code (fieldSignature) == false}.
     */
    public final boolean isFieldConstant(Signature fieldSignature) 
    throws FieldNotFoundException {
        return isFieldFinal(fieldSignature) && hasFieldConstantValue(fieldSignature);
    }

    /**
     * Returns the generic signature (type) of a field.
     * 
     * @param fieldSignature the {@link Signature} of a field.
     * @return a {@link String} or {@code null} if the field has not a generic signature.
     * @throws FieldNotFoundException iff {@link #hasFieldDeclaration}{@code (fieldSignature) == false}.
     */
    public abstract String getFieldGenericSignatureType(Signature fieldSignature) 
    throws FieldNotFoundException;

    /**
     * Returns the encoded modifiers of a field.
     * 
     * @param fieldSignature the {@link Signature} of a field.
     * @return an {@code int}, the encoded modifiers.
     * @throws FieldNotFoundException iff {@link #hasFieldDeclaration}{@code (fieldSignature) == false}.
     */
    public abstract int getFieldModifiers(Signature fieldSignature) 
    throws FieldNotFoundException;

    /**
     * Returns all the annotations of a field (raw classfile format).
     * 
     * @param fieldSignature the {@link Signature} of a field.
     * @return a {@link byte}{@code []} containing all the annotations of the field.
     * @throws FieldNotFoundException iff {@link #hasFieldDeclaration}{@code (fieldSignature) == false}.
     */
    public abstract byte[] getFieldAnnotationsRaw(Signature fieldSignature) 
    throws FieldNotFoundException;

    /**
     * Returns the value of a constant field.
     * 
     * @param fieldSignature the {@link Signature} of the field.
     * @return its value as an {@link Object}.
     * @throws FieldNotFoundException iff {@link #hasFieldDeclaration}{@code (fieldSignature) == false}.
     * @throws AttributeNotFoundException iff {@link #hasFieldConstantValue}{@code (fieldSignature) == false}.
     * @throws InvalidIndexException iff the access to the constant pool fails.
     * @throws ClassFileIllFormedException iff the class file is ill-formed.
     */
    public final ConstantPoolValue fieldConstantValue(Signature fieldSignature) 
    throws FieldNotFoundException, AttributeNotFoundException, InvalidIndexException, 
    ClassFileIllFormedException {
        final int index = fieldConstantValueIndex(fieldSignature);
        final ConstantPoolValue retVal = getValueFromConstantPool(index);
        return retVal;
    }

    /**
     * Returns the index in the constant pool where the value of a constant field is found.
     * 
     * @param fieldSignature the {@link Signature} of the field.
     * @return the constant pool index of its value as an {@code int}.
     * @throws FieldNotFoundException iff {@link #hasFieldDeclaration}{@code (fieldSignature) == false}.
     * @throws AttributeNotFoundException iff {@link #hasFieldConstantValue}{@code (fieldSignature) == false}.
     */
    public abstract int fieldConstantValueIndex(Signature fieldSignature) 
    throws FieldNotFoundException, AttributeNotFoundException;

    /**
     * Gets all the nonstatic (instance) fields declared by this class 
     * (not by its superclasses).
     *  
     * @return an array of {@link Signature}, one for each 
     *         nonstatic field declared in the class. The order
     *         of the {@link Signature}s in the returned array
     *         does not change across multiple invocations of 
     *         this method. 
     */
    public abstract Signature[] getDeclaredFieldsNonStatic();

    /**
     * Gets all the static (class) fields declared by this class 
     * (not by its superclasses).
     *  
     * @return an array of {@link Signature}s, one for each 
     *         static field declared in the class. The order
     *         of the {@link Signature}s in the returned array
     *         does not change across multiple invocations of 
     *         this method.  
     */
    public abstract Signature[] getDeclaredFieldsStatic();

    /**
     * Returns the number of static fields of this class.
     * 
     * @return an {@code int}, the number of static fields
     *         declared by this class.
     */
    public final int numOfStaticFields() {
        return getDeclaredFieldsStatic().length;
    }
    
    /**
     * Returns the number of nonstatic fields of this class.
     * 
     * @return an {@code int}, the number of nonstatic fields
     *         declared by this class.
     */
    public final int numOfNonStaticFields() {
        return getDeclaredFieldsNonStatic().length;
    }
    
    /**
     * Gets all the fields declared by this class 
     * (not by its superclasses), both static and not.
     *  
     * @return an array of {@link Signature}s, one for each 
     *         field declared in the class. The order
     *         of the {@link Signature}s in the returned array
     *         does not change across multiple invocations of 
     *         this method. The returned array is equal to 
     *         {@link #getDeclaredFieldsStatic()} concatenated
     *         to {@link #getDeclaredFieldsNonStatic()}.
     */
    public abstract Signature[] getDeclaredFields();

    /**
     * Returns all the fields known to an object of 
     * this class. 
     * 
     * @return a {@link Signature}{@code []}. It will contain all the 
     *         {@link Signature}s of the class' static fields, followed
     *         by all the {@link Signature}s of the class' object (nonstatic) 
     *         fields, followed by all the {@link Signature}s of the object 
     *         fields of the superclass, the superclass' superclass, etc.
     */	
    public final Signature[] getObjectFields() {
    	final ArrayList<Signature> signatures = new ArrayList<>(0);
    	boolean isStartClass = true;
    	for (ClassFile c : superclasses()) {
    		if (isStartClass) {
    			signatures.addAll(Arrays.asList(c.getDeclaredFieldsStatic()));
    			isStartClass = false;
    		}
    		final Signature[] fields = c.getDeclaredFieldsNonStatic();
    		signatures.addAll(Arrays.asList(fields));
    	}
        final Signature[] retVal = signatures.toArray(SIGNATURE_ARRAY);
        return retVal;
    }
    
    /**
     * Gets the offset of a field.
     * 
     * @param fieldSignature a {@link Signature}.
     * @return a nonnegative {@code int}, the offset of the field
     *         with signature {@code sig}, or {@code -1} if an 
     *         instance of this class has no field with signature
     *         {@code fieldSignature}.
     */
    public final int getFieldOffset(Signature fieldSignature) {
        final Signature[] allFields = getObjectFields();
        for (int _ofst = 0; _ofst < allFields.length; ++_ofst) {
            if (allFields[allFields.length - 1 - _ofst].equals(fieldSignature)) {
            	return _ofst;
            }
        }
        return -1;
    }

    private static final Signature[] SIGNATURE_ARRAY = new Signature[0];

    /**
     * Given an index of the constant pool of CONSTANT_FieldRef type, returns the signature of the field.
     * 
     * @param fieldRef a CONSTANT_Fieldref value in the constant pool.
     * @return the {@link Signature} of a field.
     * @throws InvalidIndexException iff {@code fieldRef} is not the index of a valid CONSTANT_FieldRef
     *         in the class constant pool.
     */
    public abstract Signature getFieldSignature(int fieldRef) throws InvalidIndexException;

    /**
     * Gets all the constructors declared by this class.
     *  
     * @return a {@link Signature}{@code []}, one for each 
     *         constructor (i.e., methods with name {@code <init>}) 
     *         declared in the class. The order
     *         of the {@link Signature}s in the returned array
     *         does not change across multiple invocations of 
     *         this method. 
     */
    public abstract Signature[] getDeclaredConstructors();

    /**
     * Returns all the signatures of the methods declared in the class, 
     * except all the constructors.
     * 
     * @return a {@link Signature}{@code []}, one for each method
     *         declared in the class, except the methods returned
     *         by {@link #getDeclaredConstructors()}. Note that this
     *         method also returns the static initializer (with name
     *         {@code <clinit>}) if it is present.
     */
    public abstract Signature[] getDeclaredMethods();

    /**
     * Given an index of the constant pool of CONSTANT_MethodRef type, returns the signature of the Method
     * @param methodRef a CONSTANT_Methodref of searched field
     * @return the {@link Signature} of a method.
     * @throws InvalidIndexException iff {@code methodRef} is not the index of a valid CONSTANT_MethodRef
     *         in the class constant pool.
     */
    public abstract Signature getMethodSignature(int methodRef) throws InvalidIndexException;

    /**
     * Given an index of the constant pool of CONSTANT_InterfaceMethodRef type, returns the signature of the Method
     * @param methodRef a CONSTANT_InferfaceMethodref of searched field
     * @return the {@link Signature} of a method.
     * @throws InvalidIndexException iff {@code methodRef} is not the index of a valid CONSTANT_InterfaceMethodRef
     *         in the class constant pool.
     */
    public abstract Signature getInterfaceMethodSignature(int methodRef) throws InvalidIndexException;

    /**
     * Given an index of the constant pool of CONSTANT_Class type, returns the signature 
     * of the class.
     * 
     * @param classRef the CONSTANT_Class of searched class.
     * @return a {@link String}, the name of a class.
     * @throws InvalidIndexException iff {@code classRef} is not the index of a valid CONSTANT_Class
     *         in the class constant pool.
     */
    public abstract String getClassSignature(int classRef) throws InvalidIndexException;
    
    /**
     * Given an index of the constant pool of CONSTANT_InvokeDynamic type, returns the 
     * call site specifier.
     * 
     * @param callSiteSpecifierIndex the CONSTANT_InvokeDynamic index in the constant pool.
     * @return a {@link CallSiteSpecifier}.
     * @throws InvalidIndexException iff {@code callSiteSpecifierIndex} is not the index of 
     *         a valid CONSTANT_InvokeDynamic in the class constant pool.
     * @throws ClassFileIllFormedException  iff the class file is ill-formed.
     */
    public abstract CallSiteSpecifier getCallSiteSpecifier(int callSiteSpecifierIndex) throws InvalidIndexException, ClassFileIllFormedException;
    
    /**
     * Returns the superclass.
     * 
     * @return the {@link ClassFile} for this {@link ClassFile}'s
     * superclass, or {@code null}
     * in the case the class has no superclass ({@code java.lang.Object}, 
     * interfaces, primitive classes, snippet classfiles).
     */
    public abstract ClassFile getSuperclass();

    /**
     * Returns the name of the superclass.
     * 
     * @return the name of the superclass as a {@link String}, or {@code null}
     * in the case the class has no superclass ({@code java.lang.Object}, 
     * interfaces, primitive classes, snippet classfiles).
     */
    public abstract String getSuperclassName();

    /**
     * Returns the list of the superinterfaces.
     * 
     * @return an immutable {@link List}{@code <}{@link ClassFile}{@code >} 
     *         containing all the superinterfaces of this 
     *         class (empty if none).
     */
    public abstract List<ClassFile> getSuperInterfaces();

    /**
     * Returns the list of the names of the superinterfaces.
     * 
     * @return an immutable {@link List}{@code <}{@link String}{@code >} 
     *         containing all the names of the superinterfaces of this 
     *         class (empty if none).
     */
    public abstract List<String> getSuperInterfaceNames();

    /**
     * Checks whether this class (nonstrictly) extends/implements another one.
     * 
     * @param sup a {@link ClassFile}.
     * @return {@code true} if {@code this.}{@link #equals(Object) equals}{@code (sup)}, or {@code this} 
     *         extends {@code sup}, or {@code this} implements {@code sup}, 
     *         {@code false} otherwise.
     * @throws InvalidInputException if {@code sup == null}.
     */
    public boolean isSubclass(ClassFile sup) throws InvalidInputException {
    	if (sup == null) {
    		throw new InvalidInputException("Invoked ClassFile.isSubclass with null parameter.");
    	}
    	if (JAVA_OBJECT.equals(sup.getClassName())) {
    	    return true; //every class/interface is subclass of java.lang.Object
    	} else if (isArray() && sup.isArray()) {
            final ClassFile subMember = getMemberClass(); 
            final ClassFile supMember = sup.getMemberClass();
            if (subMember.isPrimitiveOrVoid() && supMember.isPrimitiveOrVoid()) {
                return (subMember.equals(supMember));
            } else if (subMember.isReference() && supMember.isReference()) {
                return subMember.isSubclass(supMember);
            } else if (subMember.isArray() && supMember.isArray()) {
                return subMember.isSubclass(supMember);
            } else {
                return false;
            }
        } else {
            for (ClassFile f : superclasses()) { 
                if (sup.equals(f)) {
                    return true;
                } 
            }
            for (ClassFile f : superinterfaces()) {
                if (sup.equals(f)) {
                    return true;
                }
            }
            return false;
        }
    }
    
    /**
     * Produces all the superclasses of this class.
     * 
     * @return an {@link Iterable}{@code <}{@link ClassFile}{@code >} containing 
     *         all the superclasses of {@code this} (included).
     */
    public Iterable<ClassFile> superclasses() {
        return new IterableSuperclasses(this);
    }

    /**
     * Produces all the superinterfaces of this class.
     * 
     * @return an {@link Iterable}{@code <}{@link ClassFile}{@code >} containing 
     *         all the superinterfaces of {@code startClassName} (included if
     *         it is an interface). A same superinterface is not iterated
     *         more than once even if the class inherits it more than once. 
     */
    public Iterable<ClassFile> superinterfaces() {
        return new IterableSuperinterfaces(this);
    }

    /**
     * {@link Iterable}{@code <}{@link ClassFile}{@code >} for upwardly 
     * scanning a class hierarchy.
     *  
     * @author Pietro Braione
     */
    private class IterableSuperclasses implements Iterable<ClassFile> {
        private ClassFile startClassName;

        /**
         * Constructor.
         * 
         * @param startClass The {@link ClassFile} of the 
         *        class from where the iteration is started. 
         */
        public IterableSuperclasses(ClassFile startClass) {
            this.startClassName = startClass;
        }

        public Iterator<ClassFile> iterator() {
            return new MyIterator(this.startClassName);
        }        

        /**
         * {@link Iterator}{@code <}{@link ClassFile}{@code >} for
         * upwardly scanning a class hierarchy.
         * 
         * @author Pietro Braione
         */
        private class MyIterator implements Iterator<ClassFile> {
            private ClassFile nextClass;

            public MyIterator(ClassFile startClass) {
                this.nextClass = startClass;
            }

            public boolean hasNext() {
                return (this.nextClass != null);
            }

            public ClassFile next() {
                //ensures the method precondition
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }

                //stores the return value
                final ClassFile retval = this.nextClass;

                //gets the classfile of the superclass
                this.nextClass = retval.getSuperclass();

                //returns
                return retval;
            }

            public void remove() {
                throw new UnsupportedOperationException();
            }
        }
    }

    /**
     * {@link Iterable}{@code <}{@link ClassFile}{@code >} 
     * for upwardly scanning an interface hierarchy.
     *  
     * @author Pietro Braione
     */
    private class IterableSuperinterfaces implements Iterable<ClassFile> {
        private ClassFile startClass;

        /**
         * Constructor.
         * 
         * @param startClassName 
         *        The name of the class from where the iteration is started. 
         *        Note that the first call to {@code hasNext()} 
         *        will return {@code true} iff {@code startClassName != null} and 
         *        {@code startClassName} exists in the environment 
         *        defined by {@link Classpath}{@code .this.env}, and it is an 
         *        interface.
         */
        public IterableSuperinterfaces(ClassFile startClass) {
            this.startClass = startClass;
        }

        public Iterator<ClassFile> iterator() {
            return new MyIterator(this.startClass);
        }        

        /**
         * {@link Iterator}{@code <}{@link ClassFile}{@code >} for
         * upwardly scanning the superinterfaces of a class/interface. 
         * For the sake of simplicity it scans in breadth-first 
         * order. It does not visit a same interface twice. 
         * 
         * @author Pietro Braione
         */
        private class MyIterator implements Iterator<ClassFile> {
            private final LinkedList<ClassFile> nextClassFiles;
            private final HashSet<ClassFile> visitedClassFiles;

            public MyIterator(ClassFile startClass) {
                this.visitedClassFiles = new HashSet<>();
                this.nextClassFiles = new LinkedList<>();
                if (startClass == null) {
                    return; //keeps the iterator empty
                }
                if (startClass.isInterface()) {
                    this.nextClassFiles.add(startClass);
                } else { //is not interface and is not ClassFileBad
                    for (ClassFile cfSuper : startClass.superclasses()) {
                        this.nextClassFiles.addAll(nonVisitedImmediateSuperinterfaces(cfSuper));
                    }
                }
            }

            public boolean hasNext() {
                return !(this.nextClassFiles.isEmpty());
            }

            public ClassFile next() {
                //ensures the method precondition
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }

                //gets the next interface into the return value
                //and updates the iteration state
                final ClassFile retVal = this.nextClassFiles.removeFirst(); 
                this.visitedClassFiles.add(retVal);
                this.nextClassFiles.addAll(nonVisitedImmediateSuperinterfaces(retVal));

                //returns the result
                return retVal;
            }

            public void remove() {
                throw new UnsupportedOperationException();
            }

            private List<ClassFile> nonVisitedImmediateSuperinterfaces(ClassFile base) {
                return base.getSuperInterfaces().stream()
                       .filter(cf -> !this.visitedClassFiles.contains(cf))
                       .collect(Collectors.toList());
            }
        }
    }
    
    protected final String indexOutOfRangeMessage(int index) {
        return "index " + index + " not in constant pool of class " + getClassName();
    }

    protected final String entryInvalidMessage(int index) {
        return "index " + index + " did not correspond to a valid CONST_value entry in the constant pool of class " + getClassName();
    }

    @Override
    public int compareTo(ClassFile other) {
        final int compareNames = getClassName().compareTo(other.getClassName());
        if (compareNames == 0) {
            final int d = other.getDefiningClassLoader() - getDefiningClassLoader();
            return (d < 0 ? -1 : (d == 0 ? 0 : 1));
        } else {
            return compareNames;
        }
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null) {
            return false;
        }
        if (!(o instanceof ClassFile)) { //not same class because of wrapped classfiles
            return false; 
        }
        final ClassFile ocf = (ClassFile) o;
        return getClassName().equals(ocf.getClassName()) && getDefiningClassLoader() == ocf.getDefiningClassLoader();
    }
    
    @Override
    public final String toString() {
        return "(" + getDefiningClassLoader() + "," + getClassName() + ")";
    }

    @Override
    public final int hashCode() {
        final int prime = 6719;
        int tmpHashCode = 1;
        tmpHashCode = prime * tmpHashCode + getClassName().hashCode();
        tmpHashCode = prime * tmpHashCode + getDefiningClassLoader();

        return tmpHashCode;
    }
}