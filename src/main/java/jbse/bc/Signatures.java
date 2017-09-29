package jbse.bc;

import static jbse.common.Type.ARRAYOF;
import static jbse.common.Type.BOOLEAN;
import static jbse.common.Type.CHAR;
import static jbse.common.Type.INT;
import static jbse.common.Type.LONG;
import static jbse.common.Type.REFERENCE;
import static jbse.common.Type.TYPEEND;
import static jbse.common.Type.VOID;

/**
 * This class declares the signatures for many of the 
 * standard JRE classes, interfaces, fields and methods.
 * 
 * @author Pietro Braione
 *
 */
public final class Signatures {
    //classes and interfaces
    public static final String JAVA_ABSTRACTCOLLECTION       = "java/util/AbstractCollection";
    public static final String JAVA_ABSTRACTLIST             = "java/util/AbstractList";
    public static final String JAVA_ABSTRACTMAP              = "java/util/AbstractMap";
    public static final String JAVA_ABSTRACTSET              = "java/util/AbstractSet";
    public static final String JAVA_ACCESSCONTROLLER         = "java/security/AccessController";
    public static final String JAVA_ARRAYLIST                = "java/util/ArrayList";
    public static final String JAVA_ATOMICREFERENCEFIELDUPDATER        = "java/util/concurrent/atomic/AtomicReferenceFieldUpdater";
    public static final String JAVA_ATOMICREFERENCEFIELDUPDATER_IMPL   = "java/util/concurrent/atomic/AtomicReferenceFieldUpdater$AtomicReferenceFieldUpdaterImpl";
    public static final String JAVA_ATOMICREFERENCEFIELDUPDATER_IMPL_1 = "java/util/concurrent/atomic/AtomicReferenceFieldUpdater$AtomicReferenceFieldUpdaterImpl$1";
    public static final String JAVA_BOOLEAN                  = "java/lang/Boolean";
    public static final String JAVA_BUFFEREDINPUTSTREAM      = "java/io/BufferedInputStream";
    public static final String JAVA_CLASS                    = "java/lang/Class";
    public static final String JAVA_CLASS_ATOMIC             = "java/lang/Class$Atomic";
    public static final String JAVA_CLASSLOADER              = "java/lang/ClassLoader";
    public static final String JAVA_CLONEABLE                = "java/lang/Cloneable";
    public static final String JAVA_COLLECTIONS              = "java/util/Collections";
    public static final String JAVA_COLLECTIONS_EMPTYLIST    = "java/util/Collections$EmptyList";
    public static final String JAVA_COLLECTIONS_EMPTYMAP     = "java/util/Collections$EmptyMap";
    public static final String JAVA_COLLECTIONS_EMPTYSET     = "java/util/Collections$EmptySet";
    public static final String JAVA_COLLECTIONS_SYNCHRONIZEDCOLLECTION       = "java/util/Collections$SynchronizedCollection";
    public static final String JAVA_COLLECTIONS_SYNCHRONIZEDSET              = "java/util/Collections$SynchronizedSet";
    public static final String JAVA_COLLECTIONS_UNMODIFIABLECOLLECTION       = "java/util/Collections$UnmodifiableCollection";
    public static final String JAVA_COLLECTIONS_UNMODIFIABLELIST             = "java/util/Collections$UnmodifiableList";
    public static final String JAVA_COLLECTIONS_UNMODIFIABLERANDOMACCESSLIST = "java/util/Collections$UnmodifiableRandomAccessList";
    public static final String JAVA_DICTIONARY               = "java/util/Dictionary";
    public static final String JAVA_DOUBLE                   = "java/lang/Double";
    public static final String JAVA_ENUM                     = "java/lang/Enum";
    public static final String JAVA_EXCEPTION                = "java/lang/Exception";
    public static final String JAVA_FIELD                    = "java/lang/reflect/Field";
    public static final String JAVA_FILEDESCRIPTOR           = "java/io/FileDescriptor";
    public static final String JAVA_FILEDESCRIPTOR_1         = "java/io/FileDescriptor$1";
    public static final String JAVA_FILEINPUTSTREAM          = "java/io/FileInputStream";
    public static final String JAVA_FILEOUTPUTSTREAM         = "java/io/FileOutputStream";
    public static final String JAVA_FILTERINPUTSTREAM        = "java/io/FilterInputStream";
    public static final String JAVA_FLOAT                    = "java/lang/Float";
    public static final String JAVA_IDENTITYHASHMAP          = "java/util/IdentityHashMap";
    public static final String JAVA_INPUTSTREAM              = "java/io/InputStream";
    public static final String JAVA_METHOD                   = "java/lang/reflect/Method";
    public static final String JAVA_MODIFIER                 = "java/lang/reflect/Modifier";
    public static final String JAVA_HASHMAP                  = "java/util/HashMap";
    public static final String JAVA_HASHMAP_NODE             = "java/util/HashMap$Node";
    public static final String JAVA_HASHSET                  = "java/util/HashSet";
    public static final String JAVA_HASHTABLE                = "java/util/Hashtable";
    public static final String JAVA_HASHTABLE_ENTRY          = "java/util/Hashtable$Entry";
    public static final String JAVA_HASHTABLE_ENTRYSET       = "java/util/Hashtable$EntrySet";
    public static final String JAVA_HASHTABLE_ENUMERATOR     = "java/util/Hashtable$Enumerator";
    public static final String JAVA_INTEGER                  = "java/lang/Integer";
    public static final String JAVA_INTEGER_INTEGERCACHE     = "java/lang/Integer$IntegerCache";
    public static final String JAVA_LINKEDLIST               = "java/util/LinkedList";
    public static final String JAVA_LINKEDLIST_ENTRY         = "java/util/LinkedList$Entry";
    public static final String JAVA_MATH                     = "java/lang/Math";
    public static final String JAVA_NUMBER                   = "java/lang/Number";
    public static final String JAVA_OBJECT                   = "java/lang/Object";
    public static final String JAVA_OBJECTS                  = "java/util/Objects";
    public static final String JAVA_OUTPUTSTREAM             = "java/io/OutputStream";
    public static final String JAVA_PRIVILEGEDACTION         = "java/security/PrivilegedAction";
    public static final String JAVA_PRIVILEGEDEXCEPTIONACTION = "java/security/PrivilegedExceptionAction";
    public static final String JAVA_PROPERTIES               = "java/util/Properties";
    public static final String JAVA_REFERENCEQUEUE           = "java/lang/ref/ReferenceQueue";
    public static final String JAVA_REFERENCEQUEUE_LOCK      = "java/lang/ref/ReferenceQueue$Lock";
    public static final String JAVA_REFERENCEQUEUE_NULL      = "java/lang/ref/ReferenceQueue$Null";
    public static final String JAVA_REFLECT_ARRAY            = "java/lang/reflect/Array";
    public static final String JAVA_RUNTIMEEXCEPTION         = "java/lang/RuntimeException";
    public static final String JAVA_SERIALIZABLE             = "java/io/Serializable";
    public static final String JAVA_STACK_TRACE_ELEMENT      = "java/lang/StackTraceElement";
    public static final String JAVA_STRING                   = "java/lang/String";
    public static final String JAVA_STRING_CASEINSCOMP       = "java/lang/String$CaseInsensitiveComparator";
    public static final String JAVA_SYSTEM                   = "java/lang/System";
    public static final String JAVA_THROWABLE                = "java/lang/Throwable";
    public static final String JAVA_THROWABLE_SENTINELHOLDER = "java/lang/Throwable$SentinelHolder";
    public static final String JAVA_TREESET                  = "java/util/TreeSet";
    public static final String JBSE_ANALYSIS                 = "jbse/meta/Analysis";
    public static final String SUN_REFLECTION                = "sun/reflect/Reflection";
    public static final String SUN_REFLECTIONFACTORY         = "sun/reflect/ReflectionFactory";
    public static final String SUN_REFLECTIONFACTORY_GETREFLECTIONFACTORYACTION = "sun/reflect/ReflectionFactory$GetReflectionFactoryAction";
    public static final String SUN_SHAREDSECRETS             = "sun/misc/SharedSecrets";
    public static final String SUN_UNSAFE                    = "sun/misc/Unsafe";
    public static final String SUN_VERSION                   = "sun/misc/Version";
    public static final String SUN_VM                        = "sun/misc/VM";

    //exceptions
    public static final String ARITHMETIC_EXCEPTION                = "java/lang/ArithmeticException";
    public static final String ARRAY_INDEX_OUT_OF_BOUNDS_EXCEPTION = "java/lang/ArrayIndexOutOfBoundsException";
    public static final String ARRAY_STORE_EXCEPTION               = "java/lang/ArrayStoreException";
    public static final String CLASS_CAST_EXCEPTION                = "java/lang/ClassCastException";
    public static final String CLASS_NOT_FOUND_EXCEPTION           = "java/lang/ClassNotFoundException";
    public static final String ILLEGAL_ARGUMENT_EXCEPTION          = "java/lang/IllegalArgumentException";
    public static final String INDEX_OUT_OF_BOUNDS_EXCEPTION       = "java/lang/IndexOutOfBoundsException";
    public static final String NEGATIVE_ARRAY_SIZE_EXCEPTION       = "java/lang/NegativeArraySizeException";
    public static final String NULL_POINTER_EXCEPTION              = "java/lang/NullPointerException";
    
    //errors
    public static final String ABSTRACT_METHOD_ERROR               = "java/lang/AbstractMethodError";
    public static final String ILLEGAL_ACCESS_ERROR                = "java/lang/IllegalAccessError";
    public static final String INCOMPATIBLE_CLASS_CHANGE_ERROR     = "java/lang/IncompatibleClassChangeError";
    public static final String NO_CLASS_DEFINITION_FOUND_ERROR     = "java/lang/NoClassDefFoundError";
    public static final String NO_SUCH_FIELD_ERROR                 = "java/lang/NoSuchFieldError";
    public static final String NO_SUCH_METHOD_ERROR                = "java/lang/NoSuchMethodError";
    public static final String VERIFY_ERROR                        = "java/lang/VerifyError";
    
    //methods
    public static final Signature JAVA_ACCESSCONTROLLER_DOPRIVILEGED_EXCEPTION =
            new Signature(JAVA_ACCESSCONTROLLER, "(" + REFERENCE + JAVA_PRIVILEGEDEXCEPTIONACTION + TYPEEND + ")" + REFERENCE + JAVA_OBJECT + TYPEEND, "doPrivileged");
    public static final Signature JAVA_ACCESSCONTROLLER_DOPRIVILEGED_NOEXCEPTION =
            new Signature(JAVA_ACCESSCONTROLLER, "(" + REFERENCE + JAVA_PRIVILEGEDACTION + TYPEEND + ")" + REFERENCE + JAVA_OBJECT + TYPEEND, "doPrivileged");
    public static final Signature JAVA_CLASS_DESIREDASSERTIONSTATUS0 =
        new Signature(JAVA_CLASS, "(" + REFERENCE + JAVA_CLASS + TYPEEND + ")" + BOOLEAN, "desiredAssertionStatus0");
    public static final Signature JAVA_CLASS_GETCOMPONENTTYPE =
        new Signature(JAVA_CLASS, "()" + REFERENCE + JAVA_CLASS + TYPEEND, "getComponentType");
    public static final Signature JAVA_CLASS_GETDECLAREDFIELDS0 =
            new Signature(JAVA_CLASS, "(" + BOOLEAN + ")" + ARRAYOF + REFERENCE + JAVA_FIELD + TYPEEND, "getDeclaredFields0");
    public static final Signature JAVA_CLASS_GETPRIMITIVECLASS =
        new Signature(JAVA_CLASS, "(" + REFERENCE + JAVA_STRING + TYPEEND + ")" + 
                      REFERENCE + JAVA_CLASS + TYPEEND, "getPrimitiveClass");
    public static final Signature JAVA_CLASS_ISINSTANCE =
        new Signature(JAVA_CLASS, "(" + REFERENCE + JAVA_OBJECT + TYPEEND + ")" + BOOLEAN, "isInstance");
    public static final Signature JAVA_METHOD_INVOKE =
            new Signature(JAVA_METHOD, 
                         "(" + REFERENCE + JAVA_OBJECT + TYPEEND + ARRAYOF + REFERENCE + JAVA_OBJECT + TYPEEND + ")" + REFERENCE + JAVA_OBJECT + TYPEEND, 
                         "invoke");
    public static final Signature JAVA_OBJECT_GETCLASS =
        new Signature(JAVA_OBJECT, "()" + REFERENCE + JAVA_CLASS + TYPEEND, "getClass");
    public static final Signature JAVA_OBJECT_HASHCODE =
        new Signature(JAVA_OBJECT, "()" + INT, "hashCode");
    public static final Signature JAVA_REFLECT_ARRAY_NEWARRAY =
        new Signature(JAVA_REFLECT_ARRAY, "(" + REFERENCE + JAVA_CLASS + TYPEEND + INT + ")" + REFERENCE + JAVA_OBJECT + TYPEEND, "newArray");
    public static final Signature JAVA_STRING_HASHCODE =
        new Signature(JAVA_STRING, "()" + INT, "hashCode");
    public static final Signature JAVA_STRING_INTERN =
        new Signature(JAVA_STRING, "()" + REFERENCE + JAVA_STRING + TYPEEND, "intern");
    public static final Signature JAVA_SYSTEM_ARRAYCOPY =
        new Signature(JAVA_SYSTEM, "(" + REFERENCE + JAVA_OBJECT + TYPEEND + INT + 
                                         REFERENCE + JAVA_OBJECT + TYPEEND + INT + INT + ")" + VOID, 
                      "arraycopy");
    public static final Signature JAVA_SYSTEM_CLINIT = new Signature(JAVA_SYSTEM, "()" + VOID, "<clinit>");
    public static final Signature JAVA_SYSTEM_IDENTITYHASHCODE =
        new Signature(JAVA_SYSTEM, "(" + REFERENCE + JAVA_OBJECT + TYPEEND + ")" + INT, "identityHashCode");
    public static final Signature JAVA_SYSTEM_INITIALIZESYSTEMCLASS =
        new Signature(JAVA_SYSTEM, "()" + VOID, "initializeSystemClass");
    public static final Signature JAVA_SYSTEM_INITPROPERTIES =
        new Signature(JAVA_SYSTEM, "(" + REFERENCE + JAVA_PROPERTIES + TYPEEND + ")" + 
                                       REFERENCE + JAVA_PROPERTIES + TYPEEND, "initProperties");
    public static final Signature JAVA_THROWABLE_FILLINSTACKTRACE =
        new Signature(JAVA_THROWABLE, "(" + INT + ")" + REFERENCE + JAVA_THROWABLE + TYPEEND, "fillInStackTrace");
    public static final Signature JAVA_THROWABLE_GETSTACKTRACEDEPTH = 
        new Signature(JAVA_THROWABLE, "()" + INT, "getStackTraceDepth");
    public static final Signature JAVA_THROWABLE_GETSTACKTRACEELEMENT = 
        new Signature(JAVA_THROWABLE, "(" + INT + ")" + REFERENCE + JAVA_STACK_TRACE_ELEMENT + TYPEEND, 
                      "getStackTraceElement");
    public static final Signature JBSE_ANALYSIS_ANY = 
        new Signature(JBSE_ANALYSIS, "()" + BOOLEAN, "any");
    public static final Signature JBSE_ANALYSIS_ENDGUIDANCE = 
        new Signature(JBSE_ANALYSIS, "()" + VOID, "endGuidance");
    public static final Signature JBSE_ANALYSIS_FAIL = 
        new Signature(JBSE_ANALYSIS, "()" + VOID, "fail");
    public static final Signature JBSE_ANALYSIS_IGNORE = 
        new Signature(JBSE_ANALYSIS, "()" + VOID, "ignore");
    public static final Signature JBSE_ANALYSIS_ISRESOLVED = 
        new Signature(JBSE_ANALYSIS, "(" + REFERENCE + JAVA_OBJECT + TYPEEND + REFERENCE + JAVA_STRING + TYPEEND + ")" + BOOLEAN, "isResolved");
    public static final Signature JBSE_ANALYSIS_ISRUNBYJBSE = 
        new Signature(JBSE_ANALYSIS, "()" + BOOLEAN, "isRunByJBSE");
    public static final Signature JBSE_ANALYSIS_SUCCEED = 
        new Signature(JBSE_ANALYSIS, "()" + VOID, "succeed");
    public static final Signature JBSE_ANALYSIS_ASSUMECLASSNOTINITIALIZED = 
        new Signature(JBSE_ANALYSIS, "(" + REFERENCE + JAVA_STRING + TYPEEND + ")" + VOID, "assumeClassNotInitialized");
    public static final Signature SUN_REFLECTION_GETCALLERCLASS = 
        new Signature(SUN_REFLECTION, "()" + REFERENCE + JAVA_CLASS + TYPEEND, "getCallerClass");
    public static final Signature SUN_UNSAFE_ADDRESSSIZE = 
        new Signature(SUN_UNSAFE, "()" + INT, "addressSize");
    public static final Signature SUN_UNSAFE_ARRAYBASEOFFSET = 
        new Signature(SUN_UNSAFE, "(" + REFERENCE + JAVA_CLASS + TYPEEND + ")" + INT, "arrayBaseOffset");
    public static final Signature SUN_UNSAFE_ARRAYINDEXSCALE = 
        new Signature(SUN_UNSAFE, "(" + REFERENCE + JAVA_CLASS + TYPEEND + ")" + INT, "arrayIndexScale");
    public static final Signature SUN_UNSAFE_OBJECTFIELDOFFSET = 
            new Signature(SUN_UNSAFE, "(" + REFERENCE + JAVA_FIELD + TYPEEND + ")" + LONG, "objectFieldOffset");
    
    //fields
    public static final Signature JAVA_CLASS_CLASSLOADER = 
            new Signature(JAVA_CLASS, "" + REFERENCE + JAVA_CLASSLOADER + TYPEEND, "classLoader");
    public static final Signature JAVA_CLASS_NAME = 
        new Signature(JAVA_CLASS, "" + REFERENCE + JAVA_STRING + TYPEEND, "name");
    public static final Signature JAVA_FIELD_CLAZZ = 
        new Signature(JAVA_FIELD, "" + REFERENCE + JAVA_CLASS + TYPEEND, "clazz");
    public static final Signature JAVA_FIELD_MODIFIERS = 
            new Signature(JAVA_FIELD, "" + INT, "modifiers");
    public static final Signature JAVA_FIELD_NAME = 
            new Signature(JAVA_FIELD, "" + REFERENCE + JAVA_STRING + TYPEEND, "name");
    public static final Signature JAVA_FIELD_SIGNATURE = 
            new Signature(JAVA_FIELD, "" + REFERENCE + JAVA_STRING + TYPEEND, "signature");
    public static final Signature JAVA_FIELD_SLOT = 
            new Signature(JAVA_FIELD, "" + INT, "slot");
    public static final Signature JAVA_FIELD_TYPE = 
        new Signature(JAVA_FIELD, "" + REFERENCE + JAVA_CLASS + TYPEEND, "type");
    public static final Signature JAVA_STRING_HASH = 
        new Signature(JAVA_STRING, "" + INT, "hash");
    public static final Signature JAVA_STRING_VALUE = 
        new Signature(JAVA_STRING, "" + ARRAYOF + CHAR, "value");
    public static final Signature JAVA_THROWABLE_BACKTRACE = 
        new Signature(JAVA_THROWABLE, "" + REFERENCE + JAVA_OBJECT + TYPEEND, "backtrace");
    public static final Signature JAVA_THROWABLE_STACKTRACE = 
        new Signature(JAVA_THROWABLE, "" + ARRAYOF + REFERENCE + JAVA_STACK_TRACE_ELEMENT + TYPEEND, "stackTrace");
    public static final Signature JAVA_STACK_TRACE_ELEMENT_DECLARINGCLASS = 
        new Signature(JAVA_STACK_TRACE_ELEMENT, "" + REFERENCE + JAVA_STRING + TYPEEND, "declaringClass");
    public static final Signature JAVA_STACK_TRACE_ELEMENT_FILENAME = 
        new Signature(JAVA_STACK_TRACE_ELEMENT, "" + REFERENCE + JAVA_STRING + TYPEEND, "fileName");
    public static final Signature JAVA_STACK_TRACE_ELEMENT_LINENUMBER = 
        new Signature(JAVA_STACK_TRACE_ELEMENT, "" + INT, "lineNumber");
    public static final Signature JAVA_STACK_TRACE_ELEMENT_METHODNAME = 
        new Signature(JAVA_STACK_TRACE_ELEMENT, "" + REFERENCE + JAVA_STRING + TYPEEND, "methodName");

    /**
     * Do not instantiate it! 
     */
    private Signatures() {
        //intentionally empty
    }

}
