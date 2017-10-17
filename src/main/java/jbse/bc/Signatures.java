package jbse.bc;

import static jbse.common.Type.ARRAYOF;
import static jbse.common.Type.BOOLEAN;
import static jbse.common.Type.BYTE;
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
 */
public final class Signatures {
    //classes and interfaces
    public static final String JAVA_ABSTRACTCOLLECTION       = "java/util/AbstractCollection";
    public static final String JAVA_ABSTRACTLIST             = "java/util/AbstractList";
    public static final String JAVA_ABSTRACTMAP              = "java/util/AbstractMap";
    public static final String JAVA_ABSTRACTSET              = "java/util/AbstractSet";
    public static final String JAVA_ABSTRACTSTRINGBUILDER    = "java/lang/AbstractStringBuilder";
    public static final String JAVA_ACCESSCONTROLCONTEXT     = "java/security/AccessControlContext";
    public static final String JAVA_ACCESSCONTROLLER         = "java/security/AccessController";
    public static final String JAVA_ACCESSIBLEOBJECT         = "java/lang/reflect/AccessibleObject";
    public static final String JAVA_ARRAYLIST                = "java/util/ArrayList";
    public static final String JAVA_ARRAYS                   = "java/util/Arrays";
    public static final String JAVA_ATOMICINTEGER            = "java/util/concurrent/atomic/AtomicInteger";
    public static final String JAVA_ATOMICREFERENCEFIELDUPDATER        = "java/util/concurrent/atomic/AtomicReferenceFieldUpdater";
    public static final String JAVA_ATOMICREFERENCEFIELDUPDATER_IMPL   = "java/util/concurrent/atomic/AtomicReferenceFieldUpdater$AtomicReferenceFieldUpdaterImpl";
    public static final String JAVA_ATOMICREFERENCEFIELDUPDATER_IMPL_1 = "java/util/concurrent/atomic/AtomicReferenceFieldUpdater$AtomicReferenceFieldUpdaterImpl$1";
    public static final String JAVA_BASICPERMISSION          = "java/security/BasicPermission";
    public static final String JAVA_BOOLEAN                  = "java/lang/Boolean";
    public static final String JAVA_BUFFEREDINPUTSTREAM      = "java/io/BufferedInputStream";
    public static final String JAVA_BUFFEREDOUTPUTSTREAM     = "java/io/BufferedOutputStream";
    public static final String JAVA_CHARSET                  = "java/nio/charset/Charset";
    public static final String JAVA_CHARSET_EXTENDEDPROVIDERHOLDER = "java/nio/charset/Charset$ExtendedProviderHolder";
    public static final String JAVA_CHARSETPROVIDER          = "java/nio/charset/spi/CharsetProvider";
    public static final String JAVA_CLASS                    = "java/lang/Class";
    public static final String JAVA_CLASS_3                  = "java/lang/Class$3";
    public static final String JAVA_CLASS_ATOMIC             = "java/lang/Class$Atomic";
    public static final String JAVA_CLASS_REFLECTIONDATA     = "java/lang/Class$ReflectionData";
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
    public static final String JAVA_CONSTRUCTOR              = "java/lang/reflect/Constructor";
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
    public static final String JAVA_FILTEROUTPUTSTREAM       = "java/io/FilterOutputStream";
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
    public static final String JAVA_INTERRUPTEDEXCEPTION     = "java/lang/InterruptedException";
    public static final String JAVA_LINKEDLIST               = "java/util/LinkedList";
    public static final String JAVA_LINKEDLIST_ENTRY         = "java/util/LinkedList$Entry";
    public static final String JAVA_MATH                     = "java/lang/Math";
    public static final String JAVA_NUMBER                   = "java/lang/Number";
    public static final String JAVA_OBJECT                   = "java/lang/Object";
    public static final String JAVA_OBJECTS                  = "java/util/Objects";
    public static final String JAVA_OUTPUTSTREAM             = "java/io/OutputStream";
    public static final String JAVA_OUTPUTSTREAMWRITER       = "java/io/OutputStreamWriter";
    public static final String JAVA_PERMISSION               = "java/security/Permission";
    public static final String JAVA_PHANTOMREFERENCE         = "java/lang/ref/PhantomReference";
    public static final String JAVA_PRINTSTREAM              = "java/io/PrintStream";
    public static final String JAVA_PRIVILEGEDACTION         = "java/security/PrivilegedAction";
    public static final String JAVA_PRIVILEGEDEXCEPTIONACTION = "java/security/PrivilegedExceptionAction";
    public static final String JAVA_PROPERTIES               = "java/util/Properties";
    public static final String JAVA_REFERENCE                = "java/lang/ref/Reference";
    public static final String JAVA_REFERENCE_1              = "java/lang/ref/Reference$1";
    public static final String JAVA_REFERENCE_LOCK           = "java/lang/ref/Reference$Lock";
    public static final String JAVA_REFERENCE_REFERENCEHANDLER = "java/lang/ref/Reference$ReferenceHandler";
    public static final String JAVA_REFERENCEQUEUE           = "java/lang/ref/ReferenceQueue";
    public static final String JAVA_REFERENCEQUEUE_LOCK      = "java/lang/ref/ReferenceQueue$Lock";
    public static final String JAVA_REFERENCEQUEUE_NULL      = "java/lang/ref/ReferenceQueue$Null";
    public static final String JAVA_REFLECT_ARRAY            = "java/lang/reflect/Array";
    public static final String JAVA_REFLECTACCESS            = "java/lang/reflect/ReflectAccess";
    public static final String JAVA_REFLECTPERMISSION        = "java/lang/reflect/ReflectPermission";
    public static final String JAVA_RUNTIMEEXCEPTION         = "java/lang/RuntimeException";
    public static final String JAVA_RUNTIMEPERMISSION        = "java/lang/RuntimePermission";
    public static final String JAVA_SERIALIZABLE             = "java/io/Serializable";
    public static final String JAVA_STACK_TRACE_ELEMENT      = "java/lang/StackTraceElement";
    public static final String JAVA_STRING                   = "java/lang/String";
    public static final String JAVA_STRING_CASEINSCOMP       = "java/lang/String$CaseInsensitiveComparator";
    public static final String JAVA_STRINGBUILDER            = "java/lang/StringBuilder";
    public static final String JAVA_SYSTEM                   = "java/lang/System";
    public static final String JAVA_THREAD                   = "java/lang/Thread";
    public static final String JAVA_THREADGROUP              = "java/lang/ThreadGroup";
    public static final String JAVA_THREADLOCAL              = "java/lang/ThreadLocal";
    public static final String JAVA_THROWABLE                = "java/lang/Throwable";
    public static final String JAVA_THROWABLE_SENTINELHOLDER = "java/lang/Throwable$SentinelHolder";
    public static final String JAVA_TREESET                  = "java/util/TreeSet";
    public static final String JAVA_WRITER                   = "java/io/Writer";
    public static final String JBSE_ANALYSIS                 = jbse.meta.Analysis.class.getCanonicalName().replace('.', '/');
    public static final String JBSE_BASE                     = jbse.base.Base.class.getCanonicalName().replace('.', '/');
    public static final String SUN_CLEANER                   = "sun/misc/Cleaner";
    public static final String SUN_FASTCHARSETPROVIDER       = "sun/nio/cs/FastCharsetProvider";
    public static final String SUN_GETPROPERTYACTION         = "sun/security/action/GetPropertyAction";
    public static final String SUN_PREHASHEDMAP              = "sun/util/PreHashedMap";
    public static final String SUN_REFLECTION                = "sun/reflect/Reflection";
    public static final String SUN_REFLECTIONFACTORY         = "sun/reflect/ReflectionFactory";
    public static final String SUN_REFLECTIONFACTORY_GETREFLECTIONFACTORYACTION = "sun/reflect/ReflectionFactory$GetReflectionFactoryAction";
    public static final String SUN_REFLECTUTIL               = "sun/reflect/misc/ReflectUtil";
    public static final String SUN_SHAREDSECRETS             = "sun/misc/SharedSecrets";
    public static final String SUN_STANDARDCHARSETS          = "sun/nio/cs/StandardCharsets";
    public static final String SUN_STANDARDCHARSETS_ALIASES  = "sun/nio/cs/StandardCharsets$Aliases";
    public static final String SUN_STANDARDCHARSETS_CACHE    = "sun/nio/cs/StandardCharsets$Cache";
    public static final String SUN_STANDARDCHARSETS_CLASSES  = "sun/nio/cs/StandardCharsets$Classes";
    public static final String SUN_STREAMENCODER             = "sun/nio/cs/StreamEncoder";
    public static final String SUN_UNICODE                   = "sun/nio/cs/Unicode";
    public static final String SUN_UNSAFE                    = "sun/misc/Unsafe";
    public static final String SUN_UTF_8                     = "sun/nio/cs/UTF_8";
    public static final String SUN_VERSION                   = "sun/misc/Version";
    public static final String SUN_VM                        = "sun/misc/VM";

    //exceptions
    public static final String ARITHMETIC_EXCEPTION                = "java/lang/ArithmeticException";
    public static final String ARRAY_INDEX_OUT_OF_BOUNDS_EXCEPTION = "java/lang/ArrayIndexOutOfBoundsException";
    public static final String ARRAY_STORE_EXCEPTION               = "java/lang/ArrayStoreException";
    public static final String CLASS_CAST_EXCEPTION                = "java/lang/ClassCastException";
    public static final String CLASS_NOT_FOUND_EXCEPTION           = "java/lang/ClassNotFoundException";
    public static final String CLONE_NOT_SUPPORTED_EXCEPTION       = "java/lang/CloneNotSupportedException";
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
    public static final Signature JAVA_ACCESSCONTROLLER_GETSTACKACCESSCONTROLCONTEXT =
        new Signature(JAVA_ACCESSCONTROLLER, "()" + REFERENCE + JAVA_ACCESSCONTROLCONTEXT + TYPEEND, "getStackAccessControlContext");
    public static final Signature JAVA_CLASS_DESIREDASSERTIONSTATUS0 =
        new Signature(JAVA_CLASS, "(" + REFERENCE + JAVA_CLASS + TYPEEND + ")" + BOOLEAN, "desiredAssertionStatus0");
    public static final Signature JAVA_CLASS_FORNAME0 =
        new Signature(JAVA_CLASS, "(" + REFERENCE + JAVA_STRING + TYPEEND + BOOLEAN + REFERENCE + JAVA_CLASSLOADER + TYPEEND + REFERENCE + JAVA_CLASS + TYPEEND + ")" + REFERENCE + JAVA_CLASS + TYPEEND, "forName0");
    public static final Signature JAVA_CLASS_GETCOMPONENTTYPE =
        new Signature(JAVA_CLASS, "()" + REFERENCE + JAVA_CLASS + TYPEEND, "getComponentType");
    public static final Signature JAVA_CLASS_GETDECLAREDCONSTRUCTORS0 =
        new Signature(JAVA_CLASS, "(" + BOOLEAN + ")" + ARRAYOF + REFERENCE + JAVA_CONSTRUCTOR + TYPEEND, "getDeclaredConstructors0");
    public static final Signature JAVA_CLASS_GETDECLAREDFIELDS0 =
        new Signature(JAVA_CLASS, "(" + BOOLEAN + ")" + ARRAYOF + REFERENCE + JAVA_FIELD + TYPEEND, "getDeclaredFields0");
    public static final Signature JAVA_CLASS_GETPRIMITIVECLASS =
        new Signature(JAVA_CLASS, "(" + REFERENCE + JAVA_STRING + TYPEEND + ")" + 
                      REFERENCE + JAVA_CLASS + TYPEEND, "getPrimitiveClass");
    public static final Signature JAVA_CLASS_ISASSIGNABLEFROM =
        new Signature(JAVA_CLASS, "(" + REFERENCE + JAVA_CLASS + TYPEEND + ")" + BOOLEAN, "isAssignableFrom");
    public static final Signature JAVA_CLASS_ISINSTANCE =
        new Signature(JAVA_CLASS, "(" + REFERENCE + JAVA_OBJECT + TYPEEND + ")" + BOOLEAN, "isInstance");
    public static final Signature JAVA_CLASS_ISINTERFACE =
        new Signature(JAVA_CLASS, "()" + BOOLEAN, "isInterface");
    public static final Signature JAVA_CLASS_ISPRIMITIVE =
        new Signature(JAVA_CLASS, "()" + BOOLEAN, "isPrimitive");
    public static final Signature JAVA_METHOD_INVOKE =
        new Signature(JAVA_METHOD, 
                     "(" + REFERENCE + JAVA_OBJECT + TYPEEND + ARRAYOF + REFERENCE + JAVA_OBJECT + TYPEEND + ")" + REFERENCE + JAVA_OBJECT + TYPEEND, 
                     "invoke");
    public static final Signature JAVA_OBJECT_CLONE =
        new Signature(JAVA_OBJECT, "()" + REFERENCE + JAVA_OBJECT + TYPEEND, "clone");
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
        new Signature(JAVA_SYSTEM, 
                      "(" + REFERENCE + JAVA_OBJECT + TYPEEND + INT + REFERENCE + JAVA_OBJECT + TYPEEND + INT + INT + ")" + VOID, 
                      "arraycopy");
    public static final Signature JAVA_SYSTEM_CLINIT = new Signature(JAVA_SYSTEM, "()" + VOID, "<clinit>");
    public static final Signature JAVA_SYSTEM_IDENTITYHASHCODE =
        new Signature(JAVA_SYSTEM, "(" + REFERENCE + JAVA_OBJECT + TYPEEND + ")" + INT, "identityHashCode");
    public static final Signature JAVA_SYSTEM_INITIALIZESYSTEMCLASS =
        new Signature(JAVA_SYSTEM, "()" + VOID, "initializeSystemClass");
    public static final Signature JAVA_SYSTEM_INITPROPERTIES =
        new Signature(JAVA_SYSTEM, "(" + REFERENCE + JAVA_PROPERTIES + TYPEEND + ")" + REFERENCE + JAVA_PROPERTIES + TYPEEND, "initProperties");
    public static final Signature JAVA_THREAD_CURRENTTHREAD =
        new Signature(JAVA_THREAD, "()" + REFERENCE + JAVA_THREAD + TYPEEND, "currentThread");
    public static final Signature JAVA_THREAD_INIT =
        new Signature(JAVA_THREAD, "(" + REFERENCE + JAVA_THREADGROUP + TYPEEND + REFERENCE + JAVA_STRING + TYPEEND + ")" + VOID, "<init>");
    public static final Signature JAVA_THREAD_ISALIVE =
        new Signature(JAVA_THREAD, "()" + BOOLEAN, "isAlive");
    public static final Signature JAVA_THREADGROUP_INIT_1 =
        new Signature(JAVA_THREADGROUP, "()" + VOID, "<init>");
    public static final Signature JAVA_THREADGROUP_INIT_2 =
        new Signature(JAVA_THREADGROUP, "(" + REFERENCE + JAVA_THREADGROUP + TYPEEND + REFERENCE + JAVA_STRING + TYPEEND + ")" + VOID, "<init>");
    public static final Signature JAVA_THROWABLE_FILLINSTACKTRACE =
        new Signature(JAVA_THROWABLE, "(" + INT + ")" + REFERENCE + JAVA_THROWABLE + TYPEEND, "fillInStackTrace");
    public static final Signature JAVA_THROWABLE_GETSTACKTRACEDEPTH = 
        new Signature(JAVA_THROWABLE, "()" + INT, "getStackTraceDepth");
    public static final Signature JAVA_THROWABLE_GETSTACKTRACEELEMENT = 
        new Signature(JAVA_THROWABLE, "(" + INT + ")" + REFERENCE + JAVA_STACK_TRACE_ELEMENT + TYPEEND, "getStackTraceElement");
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
    public static final Signature SUN_UNSAFE_COMPAREANDSWAPINT = 
        new Signature(SUN_UNSAFE, "(" + REFERENCE + JAVA_OBJECT + TYPEEND + LONG + INT + INT + ")" + BOOLEAN, "compareAndSwapInt");
    public static final Signature SUN_UNSAFE_COMPAREANDSWAPOBJECT = 
        new Signature(SUN_UNSAFE, 
                      "(" + REFERENCE + JAVA_OBJECT + TYPEEND + LONG + REFERENCE + JAVA_OBJECT + TYPEEND + REFERENCE + JAVA_OBJECT + TYPEEND + ")" + BOOLEAN, 
                      "compareAndSwapObject");
    public static final Signature SUN_UNSAFE_GETINTVOLATILE = 
        new Signature(SUN_UNSAFE, "(" + REFERENCE + JAVA_OBJECT + TYPEEND + LONG + ")" + INT, "getIntVolatile");
    public static final Signature SUN_UNSAFE_OBJECTFIELDOFFSET = 
        new Signature(SUN_UNSAFE, "(" + REFERENCE + JAVA_FIELD + TYPEEND + ")" + LONG, "objectFieldOffset");
    
    //fields
    public static final Signature JAVA_ACCESSIBLEOBJECT_OVERRIDE = 
        new Signature(JAVA_ACCESSIBLEOBJECT, "" + BOOLEAN, "override");
    public static final Signature JAVA_CLASS_CLASSLOADER = 
        new Signature(JAVA_CLASS, "" + REFERENCE + JAVA_CLASSLOADER + TYPEEND, "classLoader");
    public static final Signature JAVA_CLASS_NAME = 
        new Signature(JAVA_CLASS, "" + REFERENCE + JAVA_STRING + TYPEEND, "name");
    public static final Signature JAVA_CONSTRUCTOR_ANNOTATIONS = 
        new Signature(JAVA_CONSTRUCTOR, "" + ARRAYOF + BYTE, "annotations");
    public static final Signature JAVA_CONSTRUCTOR_CLAZZ = 
        new Signature(JAVA_CONSTRUCTOR, "" + REFERENCE + JAVA_CLASS + TYPEEND, "clazz");
    public static final Signature JAVA_CONSTRUCTOR_EXCEPTIONTYPES = 
        new Signature(JAVA_CONSTRUCTOR, "" + ARRAYOF + REFERENCE + JAVA_CLASS + TYPEEND, "exceptionTypes");
    public static final Signature JAVA_CONSTRUCTOR_MODIFIERS = 
        new Signature(JAVA_CONSTRUCTOR, "" + INT, "modifiers");
    public static final Signature JAVA_CONSTRUCTOR_PARAMETERTYPES = 
        new Signature(JAVA_CONSTRUCTOR, "" + ARRAYOF + REFERENCE + JAVA_CLASS + TYPEEND, "parameterTypes");
    public static final Signature JAVA_CONSTRUCTOR_SIGNATURE = 
        new Signature(JAVA_CONSTRUCTOR, "" + REFERENCE + JAVA_STRING + TYPEEND, "signature");
    public static final Signature JAVA_CONSTRUCTOR_SLOT = 
        new Signature(JAVA_CONSTRUCTOR, "" + INT, "slot");
    public static final Signature JAVA_FIELD_ANNOTATIONS = 
        new Signature(JAVA_FIELD, "" + ARRAYOF + BYTE, "annotations");
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
    public static final Signature JAVA_THREAD_PRIORITY = 
        new Signature(JAVA_THREAD, "" + INT, "priority");
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
    public static final Signature JBSE_BASE_FILE_ENCODING = 
        new Signature(JBSE_BASE, "" + REFERENCE + JAVA_STRING + TYPEEND, "FILE_ENCODING");
    public static final Signature JBSE_BASE_FILE_SEPARATOR = 
        new Signature(JBSE_BASE, "" + REFERENCE + JAVA_STRING + TYPEEND, "FILE_SEPARATOR");
    public static final Signature JBSE_BASE_FTP_NONPROXYHOSTS = 
        new Signature(JBSE_BASE, "" + REFERENCE + JAVA_STRING + TYPEEND, "FTP_NONPROXYHOSTS");
    public static final Signature JBSE_BASE_FTP_PROXYHOST = 
        new Signature(JBSE_BASE, "" + REFERENCE + JAVA_STRING + TYPEEND, "FTP_PROXYHOST");
    public static final Signature JBSE_BASE_FTP_PROXYPORT = 
        new Signature(JBSE_BASE, "" + REFERENCE + JAVA_STRING + TYPEEND, "FTP_PROXYPORT");
    public static final Signature JBSE_BASE_GOPHERPROXYHOST = 
        new Signature(JBSE_BASE, "" + REFERENCE + JAVA_STRING + TYPEEND, "GOPHERPROXYHOST");
    public static final Signature JBSE_BASE_GOPHERPROXYPORT = 
        new Signature(JBSE_BASE, "" + REFERENCE + JAVA_STRING + TYPEEND, "GOPHERPROXYPORT");
    public static final Signature JBSE_BASE_GOPHERPROXYSET = 
        new Signature(JBSE_BASE, "" + REFERENCE + JAVA_STRING + TYPEEND, "GOPHERPROXYSET");
    public static final Signature JBSE_BASE_HTTP_NONPROXYHOSTS = 
        new Signature(JBSE_BASE, "" + REFERENCE + JAVA_STRING + TYPEEND, "HTTP_NONPROXYHOSTS");
    public static final Signature JBSE_BASE_HTTP_PROXYHOST = 
        new Signature(JBSE_BASE, "" + REFERENCE + JAVA_STRING + TYPEEND, "HTTP_PROXYHOST");
    public static final Signature JBSE_BASE_HTTP_PROXYPORT = 
        new Signature(JBSE_BASE, "" + REFERENCE + JAVA_STRING + TYPEEND, "HTTP_PROXYPORT");
    public static final Signature JBSE_BASE_HTTPS_PROXYHOST = 
        new Signature(JBSE_BASE, "" + REFERENCE + JAVA_STRING + TYPEEND, "HTTPS_PROXYHOST");
    public static final Signature JBSE_BASE_HTTPS_PROXYPORT = 
        new Signature(JBSE_BASE, "" + REFERENCE + JAVA_STRING + TYPEEND, "HTTPS_PROXYPORT");
    public static final Signature JBSE_BASE_LINE_SEPARATOR = 
        new Signature(JBSE_BASE, "" + REFERENCE + JAVA_STRING + TYPEEND, "LINE_SEPARATOR");
    public static final Signature JBSE_BASE_OS_ARCH = 
        new Signature(JBSE_BASE, "" + REFERENCE + JAVA_STRING + TYPEEND, "OS_ARCH");
    public static final Signature JBSE_BASE_OS_NAME = 
        new Signature(JBSE_BASE, "" + REFERENCE + JAVA_STRING + TYPEEND, "OS_NAME");
    public static final Signature JBSE_BASE_OS_VERSION = 
        new Signature(JBSE_BASE, "" + REFERENCE + JAVA_STRING + TYPEEND, "OS_VERSION");
    public static final Signature JBSE_BASE_PATH_SEPARATOR = 
        new Signature(JBSE_BASE, "" + REFERENCE + JAVA_STRING + TYPEEND, "PATH_SEPARATOR");
    public static final Signature JBSE_BASE_SOCKSNONPROXYHOSTS = 
        new Signature(JBSE_BASE, "" + REFERENCE + JAVA_STRING + TYPEEND, "SOCKSNONPROXYHOSTS");
    public static final Signature JBSE_BASE_SOCKSPROXYHOST = 
        new Signature(JBSE_BASE, "" + REFERENCE + JAVA_STRING + TYPEEND, "SOCKSPROXYHOST");
    public static final Signature JBSE_BASE_SOCKSPROXYPORT = 
        new Signature(JBSE_BASE, "" + REFERENCE + JAVA_STRING + TYPEEND, "SOCKSPROXYPORT");
    public static final Signature JBSE_BASE_USER_COUNTRY = 
        new Signature(JBSE_BASE, "" + REFERENCE + JAVA_STRING + TYPEEND, "USER_COUNTRY");
    public static final Signature JBSE_BASE_SUN_CPU_ENDIAN = 
        new Signature(JBSE_BASE, "" + REFERENCE + JAVA_STRING + TYPEEND, "SUN_CPU_ENDIAN");
    public static final Signature JBSE_BASE_SUN_CPU_ISALIST = 
        new Signature(JBSE_BASE, "" + REFERENCE + JAVA_STRING + TYPEEND, "SUN_CPU_ISALIST");
    public static final Signature JBSE_BASE_SUN_IO_UNICODE_ENCODING = 
        new Signature(JBSE_BASE, "" + REFERENCE + JAVA_STRING + TYPEEND, "SUN_IO_UNICODE_ENCODING");
    public static final Signature JBSE_BASE_SUN_JNU_ENCODING = 
        new Signature(JBSE_BASE, "" + REFERENCE + JAVA_STRING + TYPEEND, "SUN_JNU_ENCODING");
    public static final Signature JBSE_BASE_SUN_STDERR_ENCODING = 
        new Signature(JBSE_BASE, "" + REFERENCE + JAVA_STRING + TYPEEND, "SUN_STDERR_ENCODING");
    public static final Signature JBSE_BASE_SUN_STDOUT_ENCODING = 
        new Signature(JBSE_BASE, "" + REFERENCE + JAVA_STRING + TYPEEND, "SUN_STDOUT_ENCODING");
    public static final Signature JBSE_BASE_USER_LANGUAGE = 
        new Signature(JBSE_BASE, "" + REFERENCE + JAVA_STRING + TYPEEND, "USER_LANGUAGE");
    public static final Signature JBSE_BASE_USER_SCRIPT = 
        new Signature(JBSE_BASE, "" + REFERENCE + JAVA_STRING + TYPEEND, "USER_SCRIPT");
    public static final Signature JBSE_BASE_USER_VARIANT = 
        new Signature(JBSE_BASE, "" + REFERENCE + JAVA_STRING + TYPEEND, "USER_VARIANT");

    /**
     * Do not instantiate it! 
     */
    private Signatures() {
        //intentionally empty
    }

}
