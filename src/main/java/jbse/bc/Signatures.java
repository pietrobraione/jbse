package jbse.bc;

import static jbse.common.Type.ARRAYOF;
import static jbse.common.Type.BOOLEAN;
import static jbse.common.Type.BYTE;
import static jbse.common.Type.CHAR;
import static jbse.common.Type.DOUBLE;
import static jbse.common.Type.FLOAT;
import static jbse.common.Type.INT;
import static jbse.common.Type.internalClassName;
import static jbse.common.Type.LONG;
import static jbse.common.Type.REFERENCE;
import static jbse.common.Type.SHORT;
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
    public static final String JAVA_ANNOTATEDELEMENT         = "java/lang/reflect/AnnotatedElement";
    public static final String JAVA_ARRAYLIST                = "java/util/ArrayList";
    public static final String JAVA_ARRAYS                   = "java/util/Arrays";
    public static final String JAVA_ATOMICINTEGER            = "java/util/concurrent/atomic/AtomicInteger";
    public static final String JAVA_ATOMICLONG               = "java/util/concurrent/atomic/AtomicLong";
    public static final String JAVA_ATOMICREFERENCEFIELDUPDATER        = "java/util/concurrent/atomic/AtomicReferenceFieldUpdater";
    public static final String JAVA_ATOMICREFERENCEFIELDUPDATER_IMPL   = "java/util/concurrent/atomic/AtomicReferenceFieldUpdater$AtomicReferenceFieldUpdaterImpl";
    public static final String JAVA_ATOMICREFERENCEFIELDUPDATER_IMPL_1 = "java/util/concurrent/atomic/AtomicReferenceFieldUpdater$AtomicReferenceFieldUpdaterImpl$1";
    public static final String JAVA_BASICPERMISSION          = "java/security/BasicPermission";
    public static final String JAVA_BITS                     = "java/nio/Bits";
    public static final String JAVA_BITS_1                   = "java/nio/Bits$1";
    public static final String JAVA_BOOLEAN                  = "java/lang/Boolean";
    public static final String JAVA_BOUNDMETHODHANDLE        = "java/lang/invoke/BoundMethodHandle";
    public static final String JAVA_BOUNDMETHODHANDLE_SPECIESDATA = "java/lang/invoke/BoundMethodHandle$SpeciesData";
    public static final String JAVA_BOUNDMETHODHANDLE_SPECIES_L   = "java/lang/invoke/BoundMethodHandle$Species_L";
    public static final String JAVA_BUFFER                   = "java/nio/Buffer";
    public static final String JAVA_BUFFEREDINPUTSTREAM      = "java/io/BufferedInputStream";
    public static final String JAVA_BUFFEREDOUTPUTSTREAM     = "java/io/BufferedOutputStream";
    public static final String JAVA_BUFFEREDWRITER           = "java/io/BufferedWriter";
    public static final String JAVA_BYTE                     = "java/lang/Byte";
    public static final String JAVA_BYTE_BYTECACHE           = "java/lang/Byte$ByteCache";
    public static final String JAVA_BYTEBUFFER               = "java/nio/ByteBuffer";
    public static final String JAVA_BYTEORDER                = "java/nio/ByteOrder";
    public static final String JAVA_CHARACTER                = "java/lang/Character";
    public static final String JAVA_CHARACTER_CHARACTERCACHE = "java/lang/Character$CharacterCache";
    public static final String JAVA_CHARSEQUENCE             = "java/lang/CharSequence";
    public static final String JAVA_CHARSET                  = "java/nio/charset/Charset";
    public static final String JAVA_CHARSET_EXTENDEDPROVIDERHOLDER = "java/nio/charset/Charset$ExtendedProviderHolder";
    public static final String JAVA_CHARSETENCODER           = "java/nio/charset/CharsetEncoder";
    public static final String JAVA_CHARSETPROVIDER          = "java/nio/charset/spi/CharsetProvider";
    public static final String JAVA_CLASS                    = "java/lang/Class";
    public static final String JAVA_CLASS_1                  = "java/lang/Class$1";
    public static final String JAVA_CLASS_3                  = "java/lang/Class$3";
    public static final String JAVA_CLASS_ATOMIC             = "java/lang/Class$Atomic";
    public static final String JAVA_CLASS_REFLECTIONDATA     = "java/lang/Class$ReflectionData";
    public static final String JAVA_CLASSLOADER              = "java/lang/ClassLoader";
    public static final String JAVA_CLASSLOADER_NATIVELIBRARY   = "java/lang/ClassLoader$NativeLibrary";
    public static final String JAVA_CLASSLOADER_PARALLELLOADERS = "java/lang/ClassLoader$ParallelLoaders";
    public static final String JAVA_CLASSVALUE               = "java/lang/ClassValue";
    public static final String JAVA_CLONEABLE                = "java/lang/Cloneable";
    public static final String JAVA_CODESOURCE               = "java/security/CodeSource";
    public static final String JAVA_CODINGERRORACTION        = "java/nio/charset/CodingErrorAction";
    public static final String JAVA_COLLECTIONS              = "java/util/Collections";
    public static final String JAVA_COLLECTIONS_EMPTYLIST    = "java/util/Collections$EmptyList";
    public static final String JAVA_COLLECTIONS_EMPTYMAP     = "java/util/Collections$EmptyMap";
    public static final String JAVA_COLLECTIONS_EMPTYSET     = "java/util/Collections$EmptySet";
    public static final String JAVA_COLLECTIONS_SETFROMMAP   = "java/util/Collections$SetFromMap";
    public static final String JAVA_COLLECTIONS_SYNCHRONIZEDCOLLECTION       = "java/util/Collections$SynchronizedCollection";
    public static final String JAVA_COLLECTIONS_SYNCHRONIZEDSET              = "java/util/Collections$SynchronizedSet";
    public static final String JAVA_COLLECTIONS_UNMODIFIABLECOLLECTION       = "java/util/Collections$UnmodifiableCollection";
    public static final String JAVA_COLLECTIONS_UNMODIFIABLELIST             = "java/util/Collections$UnmodifiableList";
    public static final String JAVA_COLLECTIONS_UNMODIFIABLERANDOMACCESSLIST = "java/util/Collections$UnmodifiableRandomAccessList";
    public static final String JAVA_COMPARABLE               = "java/lang/Comparable";
    public static final String JAVA_CONCURRENTHASHMAP        = "java/util/concurrent/ConcurrentHashMap";
    public static final String JAVA_CONSTRUCTOR              = "java/lang/reflect/Constructor";
    public static final String JAVA_DEFAULTFILESYSTEM        = "java/io/DefaultFileSystem";
    public static final String JAVA_DICTIONARY               = "java/util/Dictionary";
    public static final String JAVA_DIRECTMETHODHANDLE       = "java/lang/invoke/DirectMethodHandle";
    public static final String JAVA_DOUBLE                   = "java/lang/Double";
    public static final String JAVA_ENUM                     = "java/lang/Enum";
    public static final String JAVA_EXECUTABLE               = "java/lang/reflect/Executable";
    public static final String JAVA_EXPIRINGCACHE            = "java/io/ExpiringCache";
    public static final String JAVA_EXPIRINGCACHE_1          = "java/io/ExpiringCache$1";
    public static final String JAVA_FIELD                    = "java/lang/reflect/Field";
    public static final String JAVA_FILE                     = "java/io/File";
    public static final String JAVA_FILEDESCRIPTOR           = "java/io/FileDescriptor";
    public static final String JAVA_FILEDESCRIPTOR_1         = "java/io/FileDescriptor$1";
    public static final String JAVA_FILEINPUTSTREAM          = "java/io/FileInputStream";
    public static final String JAVA_FILEOUTPUTSTREAM         = "java/io/FileOutputStream";
    public static final String JAVA_FILESYSTEM               = "java/io/FileSystem";
    public static final String JAVA_FILTERINPUTSTREAM        = "java/io/FilterInputStream";
    public static final String JAVA_FILTEROUTPUTSTREAM       = "java/io/FilterOutputStream";
    public static final String JAVA_FINALIZER                = "java/lang/ref/Finalizer";
    public static final String JAVA_FINALIZER_FINALIZERTHREAD = "java/lang/ref/Finalizer$FinalizerThread";
    public static final String JAVA_FINALREFERENCE           = "java/lang/ref/FinalReference";
    public static final String JAVA_FLOAT                    = "java/lang/Float";
    public static final String JAVA_GENERICDECLARATION       = "java/lang/reflect/GenericDeclaration";
    public static final String JAVA_HASHMAP                  = "java/util/HashMap";
    public static final String JAVA_HASHMAP_NODE             = "java/util/HashMap$Node";
    public static final String JAVA_HASHSET                  = "java/util/HashSet";
    public static final String JAVA_HASHTABLE                = "java/util/Hashtable";
    public static final String JAVA_HASHTABLE_ENTRY          = "java/util/Hashtable$Entry";
    public static final String JAVA_HASHTABLE_ENTRYSET       = "java/util/Hashtable$EntrySet";
    public static final String JAVA_HASHTABLE_ENUMERATOR     = "java/util/Hashtable$Enumerator";
    public static final String JAVA_HEAPBYTEBUFFER           = "java/nio/HeapByteBuffer";
    public static final String JAVA_IDENTITYHASHMAP          = "java/util/IdentityHashMap";
    public static final String JAVA_INPUTSTREAM              = "java/io/InputStream";
    public static final String JAVA_INTEGER                  = "java/lang/Integer";
    public static final String JAVA_INTEGER_INTEGERCACHE     = "java/lang/Integer$IntegerCache";
    public static final String JAVA_INTERRUPTEDEXCEPTION     = "java/lang/InterruptedException";
    public static final String JAVA_INVOKERBYTECODEGENERATOR = "java/lang/invoke/InvokerBytecodeGenerator";
    public static final String JAVA_INVOKERBYTECODEGENERATOR_2 = "java/lang/invoke/InvokerBytecodeGenerator$2";
    public static final String JAVA_LAMBDAFORM               = "java/lang/invoke/LambdaForm";
    public static final String JAVA_LAMBDAFORM_NAME          = "java/lang/invoke/LambdaForm$Name";
    public static final String JAVA_LINKEDHASHMAP            = "java/util/LinkedHashMap";
    public static final String JAVA_LINKEDLIST               = "java/util/LinkedList";
    public static final String JAVA_LINKEDLIST_ENTRY         = "java/util/LinkedList$Entry";
    public static final String JAVA_LONG                     = "java/lang/Long";
    public static final String JAVA_LONG_LONGCACHE           = "java/lang/Long$LongCache";
    public static final String JAVA_MATH                     = "java/lang/Math";
    public static final String JAVA_MEMBER                   = "java/lang/reflect/Member";
    public static final String JAVA_MEMBERNAME               = "java/lang/invoke/MemberName";
    public static final String JAVA_MEMBERNAME_FACTORY       = "java/lang/invoke/MemberName$Factory";
    public static final String JAVA_METHOD                   = "java/lang/reflect/Method";
    public static final String JAVA_METHODHANDLE             = "java/lang/invoke/MethodHandle";
    public static final String JAVA_METHODHANDLEIMPL         = "java/lang/invoke/MethodHandleImpl";
    public static final String JAVA_METHODHANDLENATIVES      = "java/lang/invoke/MethodHandleNatives";
    public static final String JAVA_METHODHANDLES            = "java/lang/invoke/MethodHandles";
    public static final String JAVA_METHODHANDLES_LOOKUP     = "java/lang/invoke/MethodHandles$Lookup";
    public static final String JAVA_METHODHANDLESTATICS      = "java/lang/invoke/MethodHandleStatics";
    public static final String JAVA_METHODTYPE               = "java/lang/invoke/MethodType";
    public static final String JAVA_METHODTYPEFORM           = "java/lang/invoke/MethodTypeForm";
    public static final String JAVA_MODIFIER                 = "java/lang/reflect/Modifier";
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
    public static final String JAVA_PROTECTIONDOMAIN         = "java/security/ProtectionDomain";
    public static final String JAVA_PROTECTIONDOMAIN_2       = "java/security/ProtectionDomain$2";
    public static final String JAVA_PROTECTIONDOMAIN_JAVASECURITYACCESSIMPL = "java/security/ProtectionDomain$JavaSecurityAccessImpl";
    public static final String JAVA_PROTECTIONDOMAIN_KEY     = "java/security/ProtectionDomain$Key";
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
    public static final String JAVA_RUNNABLE                 = "java/lang/Runnable";
    public static final String JAVA_RUNTIME                  = "java/lang/Runtime";
    public static final String JAVA_RUNTIMEPERMISSION        = "java/lang/RuntimePermission";
    public static final String JAVA_SECURECLASSLOADER        = "java/security/SecureClassLoader";
    public static final String JAVA_SERIALIZABLE             = "java/io/Serializable";
    public static final String JAVA_SHORT                    = "java/lang/Short";
    public static final String JAVA_SHORT_SHORTCACHE         = "java/lang/Short$ShortCache";
    public static final String JAVA_SIMPLEMETHODHANDLE       = "java/lang/invoke/SimpleMethodHandle";
    public static final String JAVA_STACK                    = "java/util/Stack";
    public static final String JAVA_STACKTRACEELEMENT        = "java/lang/StackTraceElement";
    public static final String JAVA_STRICTMATH               = "java/lang/StrictMath";
    public static final String JAVA_STRING                   = "java/lang/String";
    public static final String JAVA_STRING_CASEINSCOMP       = "java/lang/String$CaseInsensitiveComparator";
    public static final String JAVA_STRINGBUILDER            = "java/lang/StringBuilder";
    public static final String JAVA_SYSTEM                   = "java/lang/System";
    public static final String JAVA_SYSTEM_2                 = "java/lang/System$2";
    public static final String JAVA_TERMINATOR               = "java/lang/Terminator";
    public static final String JAVA_TERMINATOR_1             = "java/lang/Terminator$1";
    public static final String JAVA_THREAD                   = "java/lang/Thread";
    public static final String JAVA_THREAD_UNCAUGHTEXCEPTIONHANDLER = "java/lang/Thread$UncaughtExceptionHandler";
    public static final String JAVA_THREADGROUP              = "java/lang/ThreadGroup";
    public static final String JAVA_THREADLOCAL              = "java/lang/ThreadLocal";
    public static final String JAVA_THROWABLE                = "java/lang/Throwable";
    public static final String JAVA_THROWABLE_SENTINELHOLDER = "java/lang/Throwable$SentinelHolder";
    public static final String JAVA_TREESET                  = "java/util/TreeSet";
    public static final String JAVA_TYPE                     = "java/lang/reflect/Type";
    public static final String JAVA_UNIXFILESYSTEM           = "java/io/UnixFileSystem";
    public static final String JAVA_URL                      = "java/net/URL";
    public static final String JAVA_URLCLASSLOADER           = "java/net/URLClassLoader";
    public static final String JAVA_URLCLASSLOADER_7         = "java/net/URLClassLoader$7";
    public static final String JAVA_URLSTREAMHANDLER         = "java/net/URLStreamHandler";
    public static final String JAVA_VECTOR                   = "java/util/Vector";
    public static final String JAVA_VOID                     = "java/lang/Void";
    public static final String JAVA_WEAKHASHMAP              = "java/util/WeakHashMap";
    public static final String JAVA_WEAKHASHMAP_ENTRY        = "java/util/WeakHashMap$Entry";
    public static final String JAVA_WEAKHASHMAP_KEYSET       = "java/util/WeakHashMap$KeySet";
    public static final String JAVA_WEAKREFERENCE            = "java/lang/ref/WeakReference";
    public static final String JAVA_WINNTFILESYSTEM          = "java/io/WinNTFileSystem";
    public static final String JAVA_WRITER                   = "java/io/Writer";
    public static final String JBSE_ANALYSIS                 = internalClassName(jbse.meta.Analysis.class.getCanonicalName());
    public static final String JBSE_BASE                     = internalClassName(jbse.base.Base.class.getCanonicalName());
    public static final String SUN_CALLERSENSITIVE           = "sun/reflect/CallerSensitive";
    public static final String SUN_CLEANER                   = "sun/misc/Cleaner";
    public static final String SUN_CONSTRUCTORACCESSORIMPL   = "sun/reflect/ConstructorAccessorImpl";
    public static final String SUN_DEBUG                     = "sun/security/util/Debug";
    public static final String SUN_DELEGATINGCONSTRUCTORACCESSORIMPL = "sun/reflect/DelegatingConstructorAccessorImpl";
    public static final String SUN_FASTCHARSETPROVIDER       = "sun/nio/cs/FastCharsetProvider";
    public static final String SUN_GETPROPERTYACTION         = "sun/security/action/GetPropertyAction";
    public static final String SUN_HANDLER                   = "sun/net/www/protocol/jar/Handler";
    public static final String SUN_LAUNCHER                  = "sun/misc/Launcher";
    public static final String SUN_LAUNCHERHELPER            = "sun/launcher/LauncherHelper";
    public static final String SUN_LAUNCHER_EXTCLASSLOADER   = "sun/misc/Launcher$ExtClassLoader";
    public static final String SUN_LAUNCHER_EXTCLASSLOADER_1 = "sun/misc/Launcher$ExtClassLoader$1";
    public static final String SUN_LAUNCHER_FACTORY          = "sun/misc/Launcher$Factory";
    public static final String SUN_MAGICACCESSORIMPL         = "sun/reflect/MagicAccessorImpl";
    public static final String SUN_METAINDEX                 = "sun/misc/MetaIndex";
    public static final String SUN_NATIVECONSTRUCTORACCESSORIMPL = "sun/reflect/NativeConstructorAccessorImpl";
    public static final String SUN_NATIVESIGNALHANDLER       = "sun/misc/NativeSignalHandler";
    public static final String SUN_OSENVIRONMENT             = "sun/misc/OSEnvironment";
    public static final String SUN_PREHASHEDMAP              = "sun/util/PreHashedMap";
    public static final String SUN_REFLECTION                = "sun/reflect/Reflection";
    public static final String SUN_REFLECTIONFACTORY         = "sun/reflect/ReflectionFactory";
    public static final String SUN_REFLECTIONFACTORY_1       = "sun/reflect/ReflectionFactory$1";
    public static final String SUN_REFLECTIONFACTORY_GETREFLECTIONFACTORYACTION = "sun/reflect/ReflectionFactory$GetReflectionFactoryAction";
    public static final String SUN_REFLECTUTIL               = "sun/reflect/misc/ReflectUtil";
    public static final String SUN_SHAREDSECRETS             = "sun/misc/SharedSecrets";
    public static final String SUN_SIGNAL                    = "sun/misc/Signal";
    public static final String SUN_SIGNALHANDLER             = "sun/misc/SignalHandler";
    public static final String SUN_STANDARDCHARSETS          = "sun/nio/cs/StandardCharsets";
    public static final String SUN_STANDARDCHARSETS_ALIASES  = "sun/nio/cs/StandardCharsets$Aliases";
    public static final String SUN_STANDARDCHARSETS_CACHE    = "sun/nio/cs/StandardCharsets$Cache";
    public static final String SUN_STANDARDCHARSETS_CLASSES  = "sun/nio/cs/StandardCharsets$Classes";
    public static final String SUN_STREAMENCODER             = "sun/nio/cs/StreamEncoder";
    public static final String SUN_UNICODE                   = "sun/nio/cs/Unicode";
    public static final String SUN_UNSAFE                    = "sun/misc/Unsafe";
    public static final String SUN_URLCLASSPATH              = "sun/misc/URLClassPath";
    public static final String SUN_UTF_8                     = "sun/nio/cs/UTF_8";
    public static final String SUN_UTF_8_ENCODER             = "sun/nio/cs/UTF_8$Encoder";
    public static final String SUN_VERIFYACCESS              = "sun/invoke/util/VerifyAccess";
    public static final String SUN_VERSION                   = "sun/misc/Version";
    public static final String SUN_VM                        = "sun/misc/VM";
    public static final String SUN_WRAPPER_FORMAT            = "sun/invoke/util/Wrapper$Format";

    //exceptions
    public static final String ARITHMETIC_EXCEPTION                = "java/lang/ArithmeticException";
    public static final String ARRAY_INDEX_OUT_OF_BOUNDS_EXCEPTION = "java/lang/ArrayIndexOutOfBoundsException";
    public static final String ARRAY_STORE_EXCEPTION               = "java/lang/ArrayStoreException";
    public static final String CLASS_CAST_EXCEPTION                = "java/lang/ClassCastException";
    public static final String CLASS_NOT_FOUND_EXCEPTION           = "java/lang/ClassNotFoundException";
    public static final String CLONE_NOT_SUPPORTED_EXCEPTION       = "java/lang/CloneNotSupportedException";
    public static final String EXCEPTION                           = "java/lang/Exception";
    public static final String EXCEPTIONININITIALIZERERROR         = "java/lang/ExceptionInInitializerError";
    public static final String ILLEGAL_ARGUMENT_EXCEPTION          = "java/lang/IllegalArgumentException";
    public static final String ILLEGAL_MONITOR_STATE_EXCEPTION     = "java/lang/IllegalMonitorStateException";
    public static final String INDEX_OUT_OF_BOUNDS_EXCEPTION       = "java/lang/IndexOutOfBoundsException";
    public static final String INSTANTIATION_EXCEPTION             = "java/lang/InstantiationException";
    public static final String INTERNAL_ERROR                      = "java/lang/InternalError";
    public static final String NEGATIVE_ARRAY_SIZE_EXCEPTION       = "java/lang/NegativeArraySizeException";
    public static final String NULL_POINTER_EXCEPTION              = "java/lang/NullPointerException";
    public static final String RUNTIME_EXCEPTION                   = "java/lang/RuntimeException";
    
    //errors
    public static final String ABSTRACT_METHOD_ERROR               = "java/lang/AbstractMethodError";
    public static final String CLASS_FORMAT_ERROR                  = "java/lang/ClassFormatError";
    public static final String ERROR                               = "java/lang/Error";
    public static final String ILLEGAL_ACCESS_ERROR                = "java/lang/IllegalAccessError";
    public static final String INCOMPATIBLE_CLASS_CHANGE_ERROR     = "java/lang/IncompatibleClassChangeError";
    public static final String LINKAGE_ERROR                       = "java/lang/LinkageError";
    public static final String NO_CLASS_DEFINITION_FOUND_ERROR     = "java/lang/NoClassDefFoundError";
    public static final String NO_SUCH_FIELD_ERROR                 = "java/lang/NoSuchFieldError";
    public static final String NO_SUCH_METHOD_ERROR                = "java/lang/NoSuchMethodError";
    public static final String OUT_OF_MEMORY_ERROR                 = "java/lang/OutOfMemoryError";
    public static final String STACK_OVERFLOW_ERROR                = "java/lang/StackOverflowError";
    public static final String VERIFY_ERROR                        = "java/lang/VerifyError";
    public static final String VIRTUAL_MACHINE_ERROR               = "java/lang/VirtualMachineError";
    
    //descriptors (for signature polymorphic methods)
    public static final String SIGNATURE_POLYMORPHIC_DESCRIPTOR = "(" + ARRAYOF + REFERENCE + JAVA_OBJECT + TYPEEND + ")" + REFERENCE + JAVA_OBJECT + TYPEEND;
    
    //methods (empty signature)
    public static final Signature EMPTY_METHOD_SIGNATURE           = new Signature(null, null, null);
    
    //methods (signature polymorphic)
    public static final Signature JAVA_METHODHANDLE_INVOKE          = new Signature(JAVA_METHODHANDLE, null, "invoke");
    public static final Signature JAVA_METHODHANDLE_INVOKEBASIC     = new Signature(JAVA_METHODHANDLE, null, "invokeBasic");
    public static final Signature JAVA_METHODHANDLE_INVOKEEXACT     = new Signature(JAVA_METHODHANDLE, null, "invokeExact");
    public static final Signature JAVA_METHODHANDLE_LINKTOINTERFACE = new Signature(JAVA_METHODHANDLE, null, "linkToInterface");
    public static final Signature JAVA_METHODHANDLE_LINKTOSPECIAL   = new Signature(JAVA_METHODHANDLE, null, "linkToSpecial");
    public static final Signature JAVA_METHODHANDLE_LINKTOSTATIC    = new Signature(JAVA_METHODHANDLE, null, "linkToStatic");
    public static final Signature JAVA_METHODHANDLE_LINKTOVIRTUAL   = new Signature(JAVA_METHODHANDLE, null, "linkToVirtual");
    
    
    //methods
    public static final Signature JAVA_ACCESSCONTROLLER_DOPRIVILEGED_EXCEPTION_1 =
        new Signature(JAVA_ACCESSCONTROLLER, "(" + REFERENCE + JAVA_PRIVILEGEDEXCEPTIONACTION + TYPEEND + ")" + REFERENCE + JAVA_OBJECT + TYPEEND, "doPrivileged");
    public static final Signature JAVA_ACCESSCONTROLLER_DOPRIVILEGED_EXCEPTION_2 =
    new Signature(JAVA_ACCESSCONTROLLER, "(" + REFERENCE + JAVA_PRIVILEGEDEXCEPTIONACTION + TYPEEND + REFERENCE + JAVA_ACCESSCONTROLCONTEXT + TYPEEND + ")" + REFERENCE + JAVA_OBJECT + TYPEEND, "doPrivileged");
    public static final Signature JAVA_ACCESSCONTROLLER_DOPRIVILEGED_NOEXCEPTION =
        new Signature(JAVA_ACCESSCONTROLLER, "(" + REFERENCE + JAVA_PRIVILEGEDACTION + TYPEEND + ")" + REFERENCE + JAVA_OBJECT + TYPEEND, "doPrivileged");
    public static final Signature JAVA_ACCESSCONTROLLER_GETSTACKACCESSCONTROLCONTEXT =
        new Signature(JAVA_ACCESSCONTROLLER, "()" + REFERENCE + JAVA_ACCESSCONTROLCONTEXT + TYPEEND, "getStackAccessControlContext");
    public static final Signature JAVA_ATOMICLONG_VMSUPPORTSCS8 =
        new Signature(JAVA_ATOMICLONG, "()" + BOOLEAN, "VMSupportsCS8");
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
    public static final Signature JAVA_CLASS_GETDECLARINGCLASS0 =
        new Signature(JAVA_CLASS, "()" + REFERENCE + JAVA_CLASS + TYPEEND, "getDeclaringClass0");
    public static final Signature JAVA_CLASS_GETENCLOSINGMETHOD0 =
        new Signature(JAVA_CLASS, "()" + ARRAYOF + REFERENCE + JAVA_OBJECT + TYPEEND, "getEnclosingMethod0");
    public static final Signature JAVA_CLASS_GETMODIFIERS =
        new Signature(JAVA_CLASS, "()" + INT, "getModifiers");
    public static final Signature JAVA_CLASS_GETNAME0 =
        new Signature(JAVA_CLASS, "()" + REFERENCE + JAVA_STRING + TYPEEND, "getName0");
    public static final Signature JAVA_CLASS_GETPRIMITIVECLASS =
        new Signature(JAVA_CLASS, "(" + REFERENCE + JAVA_STRING + TYPEEND + ")" + REFERENCE + JAVA_CLASS + TYPEEND, "getPrimitiveClass");
    public static final Signature JAVA_CLASS_GETSUPERCLASS =
        new Signature(JAVA_CLASS, "()" + REFERENCE + JAVA_CLASS + TYPEEND, "getSuperclass");
    public static final Signature JAVA_CLASS_ISARRAY =
        new Signature(JAVA_CLASS, "()" + BOOLEAN, "isArray");
    public static final Signature JAVA_CLASS_ISASSIGNABLEFROM =
        new Signature(JAVA_CLASS, "(" + REFERENCE + JAVA_CLASS + TYPEEND + ")" + BOOLEAN, "isAssignableFrom");
    public static final Signature JAVA_CLASS_ISINSTANCE =
        new Signature(JAVA_CLASS, "(" + REFERENCE + JAVA_OBJECT + TYPEEND + ")" + BOOLEAN, "isInstance");
    public static final Signature JAVA_CLASS_ISINTERFACE =
        new Signature(JAVA_CLASS, "()" + BOOLEAN, "isInterface");
    public static final Signature JAVA_CLASS_ISPRIMITIVE =
        new Signature(JAVA_CLASS, "()" + BOOLEAN, "isPrimitive");
    public static final Signature JAVA_CLASS_REGISTERNATIVES =
        new Signature(JAVA_CLASS, "()" + VOID, "registerNatives");
    public static final Signature JAVA_CLASSLOADER_FINDBOOTSTRAPCLASS =
        new Signature(JAVA_CLASSLOADER, "(" + REFERENCE + JAVA_STRING + TYPEEND + ")" + REFERENCE + JAVA_CLASS + TYPEEND, "findBootstrapClass");
    public static final Signature JAVA_CLASSLOADER_FINDBUILTINLIB =
        new Signature(JAVA_CLASSLOADER, "(" + REFERENCE + JAVA_STRING + TYPEEND + ")" + REFERENCE + JAVA_STRING + TYPEEND, "findBuiltinLib");
    public static final Signature JAVA_CLASSLOADER_FINDLOADEDCLASS0 =
        new Signature(JAVA_CLASSLOADER, "(" + REFERENCE + JAVA_STRING + TYPEEND + ")" + REFERENCE + JAVA_CLASS + TYPEEND, "findLoadedClass0");
    public static final Signature JAVA_CLASSLOADER_GETSYSTEMCLASSLOADER =
        new Signature(JAVA_CLASSLOADER, "()" + REFERENCE + JAVA_CLASSLOADER + TYPEEND, "getSystemClassLoader");
    public static final Signature JAVA_CLASSLOADER_LOADCLASS =
        new Signature(JAVA_CLASSLOADER, "(" + REFERENCE + JAVA_STRING + TYPEEND + ")" + REFERENCE + JAVA_CLASS + TYPEEND, "loadClass");
    public static final Signature JAVA_CLASSLOADER_NATIVELIBRARY_LOAD =
        new Signature(JAVA_CLASSLOADER_NATIVELIBRARY, "(" + REFERENCE + JAVA_STRING + TYPEEND + BOOLEAN + ")" + VOID, "load");
    public static final Signature JAVA_CLASSLOADER_REGISTERNATIVES =
        new Signature(JAVA_CLASSLOADER, "()" + VOID, "registerNatives");
    public static final Signature JAVA_DOUBLE_DOUBLETORAWLONGBITS =
        new Signature(JAVA_DOUBLE, "(" + DOUBLE + ")" + LONG, "doubleToRawLongBits");
    public static final Signature JAVA_DOUBLE_LONGBITSTODOUBLE =
        new Signature(JAVA_DOUBLE, "(" + LONG + ")" + DOUBLE, "longBitsToDouble");
    public static final Signature JAVA_FILEDESCRIPTOR_INITIDS =
        new Signature(JAVA_FILEDESCRIPTOR, "()" + VOID, "initIDs");
    public static final Signature JAVA_FILEINPUTSTREAM_INITIDS =
        new Signature(JAVA_FILEINPUTSTREAM, "()" + VOID, "initIDs");
    public static final Signature JAVA_FILEOUTPUTSTREAM_INITIDS =
        new Signature(JAVA_FILEOUTPUTSTREAM, "()" + VOID, "initIDs");
    public static final Signature JAVA_FLOAT_FLOATTORAWINTBITS =
        new Signature(JAVA_FLOAT, "(" + FLOAT + ")" + INT, "floatToRawIntBits");
    public static final Signature JAVA_MEMBERNAME_GETTYPE =
        new Signature(JAVA_MEMBERNAME, "()" + REFERENCE + JAVA_OBJECT + TYPEEND, "getType");
    public static final Signature JAVA_METHOD_INVOKE =
        new Signature(JAVA_METHOD, 
                     "(" + REFERENCE + JAVA_OBJECT + TYPEEND + ARRAYOF + REFERENCE + JAVA_OBJECT + TYPEEND + ")" + REFERENCE + JAVA_OBJECT + TYPEEND, 
                     "invoke");
    public static final Signature JAVA_METHODHANDLENATIVES_FINDMETHODHANDLETYPE =
        new Signature(JAVA_METHODHANDLENATIVES, 
                      "(" + REFERENCE + JAVA_CLASS + TYPEEND + ARRAYOF + REFERENCE + JAVA_CLASS + TYPEEND + ")" + REFERENCE + JAVA_METHODTYPE + TYPEEND, 
                      "findMethodHandleType");
    public static final Signature JAVA_METHODHANDLENATIVES_GETCONSTANT =
        new Signature(JAVA_METHODHANDLENATIVES, "(" + INT + ")" + INT, "getConstant");
    public static final Signature JAVA_METHODHANDLENATIVES_LINKMETHOD =
        new Signature(JAVA_METHODHANDLENATIVES, 
                      "(" + REFERENCE + JAVA_CLASS + TYPEEND + INT + REFERENCE + JAVA_CLASS + TYPEEND + REFERENCE + JAVA_STRING + TYPEEND + 
                      REFERENCE + JAVA_OBJECT + TYPEEND + ARRAYOF + REFERENCE + JAVA_OBJECT + TYPEEND + ")" + REFERENCE + JAVA_MEMBERNAME + TYPEEND,
                      "linkMethod");
    public static final Signature JAVA_METHODHANDLENATIVES_REGISTERNATIVES =
        new Signature(JAVA_METHODHANDLENATIVES, "()" + VOID, "registerNatives");
    public static final Signature JAVA_METHODHANDLENATIVES_RESOLVE =
        new Signature(JAVA_METHODHANDLENATIVES, 
                      "(" + REFERENCE + JAVA_MEMBERNAME + TYPEEND + REFERENCE + JAVA_CLASS + TYPEEND + ")" + REFERENCE + JAVA_MEMBERNAME + TYPEEND, 
                      "resolve");
    public static final Signature JAVA_METHODTYPE_TOMETHODDESCRIPTORSTRING =
        new Signature(JAVA_METHODTYPE, "()" + REFERENCE + JAVA_STRING + TYPEEND, "toMethodDescriptorString");
    public static final Signature JAVA_OBJECT_CLONE =
        new Signature(JAVA_OBJECT, "()" + REFERENCE + JAVA_OBJECT + TYPEEND, "clone");
    public static final Signature JAVA_OBJECT_GETCLASS =
        new Signature(JAVA_OBJECT, "()" + REFERENCE + JAVA_CLASS + TYPEEND, "getClass");
    public static final Signature JAVA_OBJECT_HASHCODE =
        new Signature(JAVA_OBJECT, "()" + INT, "hashCode");
    public static final Signature JAVA_OBJECT_NOTIFYALL =
        new Signature(JAVA_OBJECT, "()" + VOID, "notifyAll");
    public static final Signature JAVA_OBJECT_REGISTERNATIVES =
        new Signature(JAVA_OBJECT, "()" + VOID, "registerNatives");
    public static final Signature JAVA_REFLECT_ARRAY_NEWARRAY =
        new Signature(JAVA_REFLECT_ARRAY, "(" + REFERENCE + JAVA_CLASS + TYPEEND + INT + ")" + REFERENCE + JAVA_OBJECT + TYPEEND, "newArray");
    public static final Signature JAVA_RUNTIME_AVAILABLEPROCESSORS = 
        new Signature(JAVA_RUNTIME, "()" + INT, "availableProcessors");
    public static final Signature JAVA_STRICTMATH_ACOS = 
        new Signature(JAVA_STRICTMATH, "(" + DOUBLE + ")" + DOUBLE, "acos");
    public static final Signature JAVA_STRICTMATH_ASIN = 
        new Signature(JAVA_STRICTMATH, "(" + DOUBLE + ")" + DOUBLE, "asin");
    public static final Signature JAVA_STRICTMATH_ATAN = 
        new Signature(JAVA_STRICTMATH, "(" + DOUBLE + ")" + DOUBLE, "atan");
    public static final Signature JAVA_STRICTMATH_ATAN2 = 
        new Signature(JAVA_STRICTMATH, "(" + DOUBLE + DOUBLE + ")" + DOUBLE, "atan");
    public static final Signature JAVA_STRICTMATH_CBRT = 
        new Signature(JAVA_STRICTMATH, "(" + DOUBLE + ")" + DOUBLE, "cbrt");
    public static final Signature JAVA_STRICTMATH_COS = 
        new Signature(JAVA_STRICTMATH, "(" + DOUBLE + ")" + DOUBLE, "cos");
    public static final Signature JAVA_STRICTMATH_COSH = 
        new Signature(JAVA_STRICTMATH, "(" + DOUBLE + ")" + DOUBLE, "cosh");
    public static final Signature JAVA_STRICTMATH_EXP = 
        new Signature(JAVA_STRICTMATH, "(" + DOUBLE + ")" + DOUBLE, "exp");
    public static final Signature JAVA_STRICTMATH_EXPM1 = 
        new Signature(JAVA_STRICTMATH, "(" + DOUBLE + ")" + DOUBLE, "expm1");
    public static final Signature JAVA_STRICTMATH_HYPOT = 
        new Signature(JAVA_STRICTMATH, "(" + DOUBLE + DOUBLE + ")" + DOUBLE, "hypot");
    public static final Signature JAVA_STRICTMATH_IEEEREMAINDER = 
        new Signature(JAVA_STRICTMATH, "(" + DOUBLE + DOUBLE + ")" + DOUBLE, "IEEEremainder");
    public static final Signature JAVA_STRICTMATH_LOG = 
        new Signature(JAVA_STRICTMATH, "(" + DOUBLE + ")" + DOUBLE, "log");
    public static final Signature JAVA_STRICTMATH_LOG10 = 
        new Signature(JAVA_STRICTMATH, "(" + DOUBLE + ")" + DOUBLE, "log10");
    public static final Signature JAVA_STRICTMATH_LOG1P = 
        new Signature(JAVA_STRICTMATH, "(" + DOUBLE + ")" + DOUBLE, "log1p");
    public static final Signature JAVA_STRICTMATH_POW = 
        new Signature(JAVA_STRICTMATH, "(" + DOUBLE + DOUBLE + ")" + DOUBLE, "pow");
    public static final Signature JAVA_STRICTMATH_SIN = 
        new Signature(JAVA_STRICTMATH, "(" + DOUBLE + ")" + DOUBLE, "sin");
    public static final Signature JAVA_STRICTMATH_SINH = 
        new Signature(JAVA_STRICTMATH, "(" + DOUBLE + ")" + DOUBLE, "sinh");
    public static final Signature JAVA_STRICTMATH_SQRT = 
        new Signature(JAVA_STRICTMATH, "(" + DOUBLE + ")" + DOUBLE, "sqrt");
    public static final Signature JAVA_STRICTMATH_TAN = 
        new Signature(JAVA_STRICTMATH, "(" + DOUBLE + ")" + DOUBLE, "tan");
    public static final Signature JAVA_STRICTMATH_TANH = 
        new Signature(JAVA_STRICTMATH, "(" + DOUBLE + ")" + DOUBLE, "tanh");
    public static final Signature JAVA_STRING_HASHCODE = 
        new Signature(JAVA_STRING, "()" + INT, "hashCode");
    public static final Signature JAVA_STRING_INTERN =
        new Signature(JAVA_STRING, "()" + REFERENCE + JAVA_STRING + TYPEEND, "intern");
    public static final Signature JAVA_STRINGBUILDER_APPEND_BOOLEAN =
        new Signature(JAVA_STRINGBUILDER, "(" + BOOLEAN + ")" + REFERENCE + JAVA_STRINGBUILDER + TYPEEND, "append");
    public static final Signature JAVA_STRINGBUILDER_APPEND_CHAR =
        new Signature(JAVA_STRINGBUILDER, "(" + CHAR + ")" + REFERENCE + JAVA_STRINGBUILDER + TYPEEND, "append");
    public static final Signature JAVA_STRINGBUILDER_APPEND_DOUBLE =
        new Signature(JAVA_STRINGBUILDER, "(" + DOUBLE + ")" + REFERENCE + JAVA_STRINGBUILDER + TYPEEND, "append");
    public static final Signature JAVA_STRINGBUILDER_APPEND_FLOAT =
        new Signature(JAVA_STRINGBUILDER, "(" + FLOAT + ")" + REFERENCE + JAVA_STRINGBUILDER + TYPEEND, "append");
    public static final Signature JAVA_STRINGBUILDER_APPEND_INT =
        new Signature(JAVA_STRINGBUILDER, "(" + INT + ")" + REFERENCE + JAVA_STRINGBUILDER + TYPEEND, "append");
    public static final Signature JAVA_STRINGBUILDER_APPEND_LONG =
        new Signature(JAVA_STRINGBUILDER, "(" + LONG + ")" + REFERENCE + JAVA_STRINGBUILDER + TYPEEND, "append");
    public static final Signature JAVA_STRINGBUILDER_APPEND_STRING =
        new Signature(JAVA_STRINGBUILDER, "(" + REFERENCE + JAVA_STRING + TYPEEND + ")" + REFERENCE + JAVA_STRINGBUILDER + TYPEEND, "append");
    public static final Signature JAVA_SYSTEM_ARRAYCOPY =
        new Signature(JAVA_SYSTEM, 
                      "(" + REFERENCE + JAVA_OBJECT + TYPEEND + INT + REFERENCE + JAVA_OBJECT + TYPEEND + INT + INT + ")" + VOID, 
                      "arraycopy");
    public static final Signature JAVA_SYSTEM_CLINIT = 
        new Signature(JAVA_SYSTEM, "()" + VOID, "<clinit>");
    public static final Signature JAVA_SYSTEM_IDENTITYHASHCODE =
        new Signature(JAVA_SYSTEM, "(" + REFERENCE + JAVA_OBJECT + TYPEEND + ")" + INT, "identityHashCode");
    public static final Signature JAVA_SYSTEM_INITIALIZESYSTEMCLASS =
        new Signature(JAVA_SYSTEM, "()" + VOID, "initializeSystemClass");
    public static final Signature JAVA_SYSTEM_INITPROPERTIES =
        new Signature(JAVA_SYSTEM, "(" + REFERENCE + JAVA_PROPERTIES + TYPEEND + ")" + REFERENCE + JAVA_PROPERTIES + TYPEEND, "initProperties");
    public static final Signature JAVA_SYSTEM_MAPLIBRARYNAME =
        new Signature(JAVA_SYSTEM, "(" + REFERENCE + JAVA_STRING + TYPEEND + ")" + REFERENCE + JAVA_STRING + TYPEEND, "mapLibraryName");
    public static final Signature JAVA_SYSTEM_NANOTIME =
        new Signature(JAVA_SYSTEM, "()" + LONG, "nanoTime");
    public static final Signature JAVA_SYSTEM_REGISTERNATIVES =
        new Signature(JAVA_SYSTEM, "()" + VOID, "registerNatives");
    public static final Signature JAVA_SYSTEM_SETERR0 =
        new Signature(JAVA_SYSTEM, "(" + REFERENCE + JAVA_PRINTSTREAM + TYPEEND + ")" + VOID, "setErr0");
    public static final Signature JAVA_SYSTEM_SETIN0 =
        new Signature(JAVA_SYSTEM, "(" + REFERENCE + JAVA_INPUTSTREAM + TYPEEND + ")" + VOID, "setIn0");
    public static final Signature JAVA_SYSTEM_SETOUT0 =
        new Signature(JAVA_SYSTEM, "(" + REFERENCE + JAVA_PRINTSTREAM + TYPEEND + ")" + VOID, "setOut0");
    public static final Signature JAVA_THREAD_CURRENTTHREAD =
        new Signature(JAVA_THREAD, "()" + REFERENCE + JAVA_THREAD + TYPEEND, "currentThread");
    public static final Signature JAVA_THREAD_INIT =
        new Signature(JAVA_THREAD, "(" + REFERENCE + JAVA_THREADGROUP + TYPEEND + REFERENCE + JAVA_STRING + TYPEEND + ")" + VOID, "<init>");
    public static final Signature JAVA_THREAD_ISALIVE =
        new Signature(JAVA_THREAD, "()" + BOOLEAN, "isAlive");
    public static final Signature JAVA_THREAD_REGISTERNATIVES =
        new Signature(JAVA_THREAD, "()" + VOID, "registerNatives");
    public static final Signature JAVA_THREAD_SETPRIORITY0 =
        new Signature(JAVA_THREAD, "(" + INT + ")" + VOID, "setPriority0");
    public static final Signature JAVA_THREAD_START0 =
        new Signature(JAVA_THREAD, "()" + VOID, "start0");
    public static final Signature JAVA_THREADGROUP_INIT_1 =
        new Signature(JAVA_THREADGROUP, "()" + VOID, "<init>");
    public static final Signature JAVA_THREADGROUP_INIT_2 =
        new Signature(JAVA_THREADGROUP, "(" + REFERENCE + JAVA_THREADGROUP + TYPEEND + REFERENCE + JAVA_STRING + TYPEEND + ")" + VOID, "<init>");
    public static final Signature JAVA_THROWABLE_FILLINSTACKTRACE =
        new Signature(JAVA_THROWABLE, "(" + INT + ")" + REFERENCE + JAVA_THROWABLE + TYPEEND, "fillInStackTrace");
    public static final Signature JAVA_THROWABLE_GETSTACKTRACEDEPTH = 
        new Signature(JAVA_THROWABLE, "()" + INT, "getStackTraceDepth");
    public static final Signature JAVA_THROWABLE_GETSTACKTRACEELEMENT = 
        new Signature(JAVA_THROWABLE, "(" + INT + ")" + REFERENCE + JAVA_STACKTRACEELEMENT + TYPEEND, "getStackTraceElement");
    public static final Signature JAVA_UNIXFILESYSTEM_GETBOOLEANATTRIBUTES0 = 
        new Signature(JAVA_UNIXFILESYSTEM, "(" + REFERENCE + JAVA_FILE + TYPEEND + ")" + INT, "getBooleanAttributes0");
    public static final Signature JAVA_UNIXFILESYSTEM_INITIDS = 
        new Signature(JAVA_UNIXFILESYSTEM, "()" + VOID, "initIDs");
    public static final Signature JAVA_WINNTFILESYSTEM_GETBOOLEANATTRIBUTES = 
        new Signature(JAVA_WINNTFILESYSTEM, "(" + REFERENCE + JAVA_FILE + TYPEEND + ")" + INT, "getBooleanAttributes");
    public static final Signature JAVA_WINNTFILESYSTEM_INITIDS = 
        new Signature(JAVA_WINNTFILESYSTEM, "()" + VOID, "initIDs");
    public static final Signature JBSE_ANALYSIS_ANY = 
        new Signature(JBSE_ANALYSIS, "()" + BOOLEAN, "any");
    public static final Signature JBSE_ANALYSIS_ASSUMECLASSNOTINITIALIZED = 
        new Signature(JBSE_ANALYSIS, "(" + REFERENCE + JAVA_STRING + TYPEEND + ")" + VOID, "assumeClassNotInitialized");
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
    public static final Signature JBSE_ANALYSIS_ISSYMBOLIC_BOOLEAN = 
        new Signature(JBSE_ANALYSIS, "(" + BOOLEAN + ")" + BOOLEAN, "isSymbolic");
    public static final Signature JBSE_ANALYSIS_ISSYMBOLIC_BYTE = 
        new Signature(JBSE_ANALYSIS, "(" + BYTE + ")" + BOOLEAN, "isSymbolic");
    public static final Signature JBSE_ANALYSIS_ISSYMBOLIC_CHAR = 
        new Signature(JBSE_ANALYSIS, "(" + CHAR + ")" + BOOLEAN, "isSymbolic");
    public static final Signature JBSE_ANALYSIS_ISSYMBOLIC_DOUBLE = 
        new Signature(JBSE_ANALYSIS, "(" + DOUBLE + ")" + BOOLEAN, "isSymbolic");
    public static final Signature JBSE_ANALYSIS_ISSYMBOLIC_FLOAT = 
        new Signature(JBSE_ANALYSIS, "(" + FLOAT + ")" + BOOLEAN, "isSymbolic");
    public static final Signature JBSE_ANALYSIS_ISSYMBOLIC_INT = 
        new Signature(JBSE_ANALYSIS, "(" + INT + ")" + BOOLEAN, "isSymbolic");
    public static final Signature JBSE_ANALYSIS_ISSYMBOLIC_LONG = 
        new Signature(JBSE_ANALYSIS, "(" + LONG + ")" + BOOLEAN, "isSymbolic");
    public static final Signature JBSE_ANALYSIS_ISSYMBOLIC_SHORT = 
        new Signature(JBSE_ANALYSIS, "(" + SHORT + ")" + BOOLEAN, "isSymbolic");
    public static final Signature JBSE_ANALYSIS_SUCCEED = 
        new Signature(JBSE_ANALYSIS, "()" + VOID, "succeed");
    public static final Signature JBSE_ANALYSIS_SYMBOLNAME_BOOLEAN = 
        new Signature(JBSE_ANALYSIS, "(" + BOOLEAN + ")" + REFERENCE + JAVA_STRING + TYPEEND, "symbolName");
    public static final Signature JBSE_ANALYSIS_SYMBOLNAME_BYTE = 
        new Signature(JBSE_ANALYSIS, "(" + BYTE + ")" + REFERENCE + JAVA_STRING + TYPEEND, "symbolName");
    public static final Signature JBSE_ANALYSIS_SYMBOLNAME_CHAR = 
        new Signature(JBSE_ANALYSIS, "(" + CHAR + ")" + REFERENCE + JAVA_STRING + TYPEEND, "symbolName");
    public static final Signature JBSE_ANALYSIS_SYMBOLNAME_DOUBLE = 
        new Signature(JBSE_ANALYSIS, "(" + DOUBLE + ")" + REFERENCE + JAVA_STRING + TYPEEND, "symbolName");
    public static final Signature JBSE_ANALYSIS_SYMBOLNAME_FLOAT = 
        new Signature(JBSE_ANALYSIS, "(" + FLOAT + ")" + REFERENCE + JAVA_STRING + TYPEEND, "symbolName");
    public static final Signature JBSE_ANALYSIS_SYMBOLNAME_INT = 
        new Signature(JBSE_ANALYSIS, "(" + INT + ")" + REFERENCE + JAVA_STRING + TYPEEND, "symbolName");
    public static final Signature JBSE_ANALYSIS_SYMBOLNAME_LONG = 
        new Signature(JBSE_ANALYSIS, "(" + LONG + ")" + REFERENCE + JAVA_STRING + TYPEEND, "symbolName");
    public static final Signature JBSE_ANALYSIS_SYMBOLNAME_SHORT = 
        new Signature(JBSE_ANALYSIS, "(" + SHORT + ")" + REFERENCE + JAVA_STRING + TYPEEND, "symbolName");
    public static final Signature JBSE_BASE_BOXEXCEPTIONININITIALIZERERROR = 
        new Signature(JBSE_BASE, "()" + VOID, "boxExceptionInInitializerError");
    public static final Signature JBSE_BASE_BOXINVOCATIONTARGETEXCEPTION = 
        new Signature(JBSE_BASE, "()" + VOID, "boxInvocationTargetException");
    public static final Signature SUN_NATIVECONSTRUCTORACCESSORIMPL_NEWINSTANCE0 = 
        new Signature(SUN_NATIVECONSTRUCTORACCESSORIMPL, 
                      "(" + REFERENCE + JAVA_CONSTRUCTOR + TYPEEND + ARRAYOF + REFERENCE + JAVA_OBJECT + TYPEEND + ")" + REFERENCE + JAVA_OBJECT + TYPEEND, 
                      "newInstance0");
    public static final Signature SUN_REFLECTION_GETCALLERCLASS = 
        new Signature(SUN_REFLECTION, "()" + REFERENCE + JAVA_CLASS + TYPEEND, "getCallerClass");
    public static final Signature SUN_REFLECTION_GETCLASSACCESSFLAGS = 
        new Signature(SUN_REFLECTION, "(" + REFERENCE + JAVA_CLASS + TYPEEND + ")" + INT, "getClassAccessFlags");
    public static final Signature SUN_SIGNAL_FINDSIGNAL = 
        new Signature(SUN_SIGNAL, "(" + REFERENCE + JAVA_STRING + TYPEEND + ")" + INT, "findSignal");
    public static final Signature SUN_SIGNAL_HANDLE0 = 
        new Signature(SUN_SIGNAL, "(" + INT + LONG + ")" + LONG, "handle0");
    public static final Signature SUN_UNSAFE_ADDRESSSIZE = 
        new Signature(SUN_UNSAFE, "()" + INT, "addressSize");
    public static final Signature SUN_UNSAFE_ALLOCATEMEMORY = 
        new Signature(SUN_UNSAFE, "(" + LONG + ")" + LONG, "allocateMemory");
    public static final Signature SUN_UNSAFE_ARRAYBASEOFFSET = 
        new Signature(SUN_UNSAFE, "(" + REFERENCE + JAVA_CLASS + TYPEEND + ")" + INT, "arrayBaseOffset");
    public static final Signature SUN_UNSAFE_ARRAYINDEXSCALE = 
        new Signature(SUN_UNSAFE, "(" + REFERENCE + JAVA_CLASS + TYPEEND + ")" + INT, "arrayIndexScale");
    public static final Signature SUN_UNSAFE_COMPAREANDSWAPINT = 
        new Signature(SUN_UNSAFE, "(" + REFERENCE + JAVA_OBJECT + TYPEEND + LONG + INT + INT + ")" + BOOLEAN, "compareAndSwapInt");
    public static final Signature SUN_UNSAFE_COMPAREANDSWAPLONG = 
        new Signature(SUN_UNSAFE, "(" + REFERENCE + JAVA_OBJECT + TYPEEND + LONG + LONG + LONG + ")" + BOOLEAN, "compareAndSwapLong");
    public static final Signature SUN_UNSAFE_COMPAREANDSWAPOBJECT = 
        new Signature(SUN_UNSAFE, 
                      "(" + REFERENCE + JAVA_OBJECT + TYPEEND + LONG + REFERENCE + JAVA_OBJECT + TYPEEND + REFERENCE + JAVA_OBJECT + TYPEEND + ")" + BOOLEAN, 
                      "compareAndSwapObject");
    public static final Signature SUN_UNSAFE_DEFINEANONYMOUSCLASS = 
        new Signature(SUN_UNSAFE, 
                      "(" + REFERENCE + JAVA_CLASS + TYPEEND + ARRAYOF + BYTE + ARRAYOF + REFERENCE + JAVA_OBJECT + TYPEEND + ")" + REFERENCE + JAVA_CLASS + TYPEEND, 
                      "defineAnonymousClass");
    public static final Signature SUN_UNSAFE_FREEMEMORY = 
        new Signature(SUN_UNSAFE, "(" + LONG + ")" + VOID, "freeMemory");
    public static final Signature SUN_UNSAFE_ENSURECLASSINITIALIZED = 
        new Signature(SUN_UNSAFE, "(" + REFERENCE + JAVA_CLASS + TYPEEND + ")" + VOID, "ensureClassInitialized");
    public static final Signature SUN_UNSAFE_GETBYTE = 
        new Signature(SUN_UNSAFE, "(" + LONG + ")" + BYTE, "getByte");
    public static final Signature SUN_UNSAFE_GETINTVOLATILE = 
        new Signature(SUN_UNSAFE, "(" + REFERENCE + JAVA_OBJECT + TYPEEND + LONG + ")" + INT, "getIntVolatile");
    public static final Signature SUN_UNSAFE_GETLONG = 
        new Signature(SUN_UNSAFE, "(" + LONG + ")" + LONG, "getLong");
    public static final Signature SUN_UNSAFE_GETOBJECTVOLATILE = 
        new Signature(SUN_UNSAFE, "(" + REFERENCE + JAVA_OBJECT + TYPEEND + LONG + ")" + REFERENCE + JAVA_OBJECT + TYPEEND, "getObjectVolatile");
    public static final Signature SUN_UNSAFE_OBJECTFIELDOFFSET = 
        new Signature(SUN_UNSAFE, "(" + REFERENCE + JAVA_FIELD + TYPEEND + ")" + LONG, "objectFieldOffset");
    public static final Signature SUN_UNSAFE_PUTLONG = 
        new Signature(SUN_UNSAFE, "(" + LONG + LONG + ")" + VOID, "putLong");
    public static final Signature SUN_UNSAFE_PUTOBJECTVOLATILE = 
        new Signature(SUN_UNSAFE, "(" + REFERENCE + JAVA_OBJECT + TYPEEND + LONG + REFERENCE + JAVA_OBJECT + TYPEEND + ")" + VOID, "putObjectVolatile");
    public static final Signature SUN_UNSAFE_REGISTERNATIVES =
        new Signature(SUN_UNSAFE, "()" + VOID, "registerNatives");
    public static final Signature SUN_UNSAFE_SHOULDBEINITIALIZED =
        new Signature(SUN_UNSAFE, "(" + REFERENCE + JAVA_CLASS + TYPEEND + ")" + BOOLEAN, "shouldBeInitialized");
    public static final Signature SUN_URLCLASSPATH_GETLOOKUPCACHEURLS =
        new Signature(SUN_URLCLASSPATH, "(" + REFERENCE + JAVA_CLASSLOADER + TYPEEND + ")" + ARRAYOF + REFERENCE + JAVA_URL + TYPEEND, "getLookupCacheURLs");
    public static final Signature SUN_VM_INITIALIZE = 
        new Signature(SUN_VM, "()" + VOID, "initialize");
    public static final Signature noclass_REGISTERLOADEDCLASS =
        new Signature(null, "(" + INT + REFERENCE + JAVA_CLASSLOADER + TYPEEND + ")" + VOID, "registerLoadedClass");
    public static final Signature noclass_REGISTERMETHODTYPE =
        new Signature(null, "(" + REFERENCE + JAVA_STRING + TYPEEND + REFERENCE + JAVA_METHODTYPE + TYPEEND + ")" + VOID, "registerMethodType");
    public static final Signature noclass_STORELINKEDMETHODANDAPPENDIX =
        new Signature(null, "(" + REFERENCE + JAVA_STRING + TYPEEND + REFERENCE + JAVA_METHODTYPE + TYPEEND + ARRAYOF + REFERENCE + JAVA_OBJECT + TYPEEND + REFERENCE + JAVA_MEMBERNAME + TYPEEND + ")" + VOID, "storeLinkedMethodAndAppendix");
    public static final Signature noclass_SETSTANDARDCLASSLOADERSREADY =
        new Signature(null, "()" + VOID, "setStandardClassLoadersReady");
    
    //fields
    public static final Signature JAVA_ACCESSIBLEOBJECT_OVERRIDE = 
        new Signature(JAVA_ACCESSIBLEOBJECT, "" + BOOLEAN, "override");
    public static final Signature JAVA_BOOLEAN_VALUE = 
        new Signature(JAVA_BOOLEAN, "" + BOOLEAN, "value");
    public static final Signature JAVA_BYTE_VALUE = 
        new Signature(JAVA_BYTE, "" + BYTE, "value");
    public static final Signature JAVA_CHARACTER_VALUE = 
        new Signature(JAVA_CHARACTER, "" + CHAR, "value");
    public static final Signature JAVA_CLASS_CLASSLOADER = 
        new Signature(JAVA_CLASS, "" + REFERENCE + JAVA_CLASSLOADER + TYPEEND, "classLoader");
    public static final Signature JAVA_CLASS_NAME = 
        new Signature(JAVA_CLASS, "" + REFERENCE + JAVA_STRING + TYPEEND, "name");
    public static final Signature JAVA_CLASSLOADER_NATIVELIBRARY_ISBUILTIN = 
        new Signature(JAVA_CLASSLOADER_NATIVELIBRARY, "" + BOOLEAN, "isBuiltin");
    public static final Signature JAVA_CLASSLOADER_NATIVELIBRARY_LOADED = 
        new Signature(JAVA_CLASSLOADER_NATIVELIBRARY, "" + BOOLEAN, "loaded");
    public static final Signature JAVA_CLASSLOADER_NATIVELIBRARY_NAME = 
        new Signature(JAVA_CLASSLOADER_NATIVELIBRARY, "" + REFERENCE + JAVA_STRING + TYPEEND, "name");
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
    public static final Signature JAVA_DOUBLE_VALUE = 
        new Signature(JAVA_DOUBLE, "" + DOUBLE, "value");
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
    public static final Signature JAVA_FILE_PATH = 
        new Signature(JAVA_FILE, "" + REFERENCE + JAVA_STRING + TYPEEND, "path");
    public static final Signature JAVA_FLOAT_VALUE = 
        new Signature(JAVA_FLOAT, "" + FLOAT, "value");
    public static final Signature JAVA_INTEGER_VALUE = 
        new Signature(JAVA_INTEGER, "" + INT, "value");
    public static final Signature JAVA_LONG_VALUE = 
        new Signature(JAVA_LONG, "" + LONG, "value");
    public static final Signature JAVA_MEMBERNAME_CLAZZ = 
        new Signature(JAVA_MEMBERNAME, "" + REFERENCE + JAVA_CLASS + TYPEEND, "clazz");
    public static final Signature JAVA_MEMBERNAME_FLAGS = 
        new Signature(JAVA_MEMBERNAME, "" + INT, "flags");
    public static final Signature JAVA_MEMBERNAME_NAME = 
        new Signature(JAVA_MEMBERNAME, "" + REFERENCE + JAVA_STRING + TYPEEND, "name");
    public static final Signature JAVA_MEMBERNAME_TYPE = 
        new Signature(JAVA_MEMBERNAME, "" + REFERENCE + JAVA_OBJECT + TYPEEND, "type");
    public static final Signature JAVA_METHODTYPE_METHODDESCRIPTOR = 
        new Signature(JAVA_METHODTYPE, "" + REFERENCE + JAVA_STRING + TYPEEND, "methodDescriptor");
    public static final Signature JAVA_SHORT_VALUE = 
        new Signature(JAVA_SHORT, "" + SHORT, "value");
    public static final Signature JAVA_STACKTRACEELEMENT_DECLARINGCLASS = 
        new Signature(JAVA_STACKTRACEELEMENT, "" + REFERENCE + JAVA_STRING + TYPEEND, "declaringClass");
    public static final Signature JAVA_STACKTRACEELEMENT_FILENAME = 
        new Signature(JAVA_STACKTRACEELEMENT, "" + REFERENCE + JAVA_STRING + TYPEEND, "fileName");
    public static final Signature JAVA_STACKTRACEELEMENT_LINENUMBER = 
        new Signature(JAVA_STACKTRACEELEMENT, "" + INT, "lineNumber");
    public static final Signature JAVA_STACKTRACEELEMENT_METHODNAME = 
        new Signature(JAVA_STACKTRACEELEMENT, "" + REFERENCE + JAVA_STRING + TYPEEND, "methodName");
    public static final Signature JAVA_STRING_HASH = 
        new Signature(JAVA_STRING, "" + INT, "hash");
    public static final Signature JAVA_STRING_VALUE = 
        new Signature(JAVA_STRING, "" + ARRAYOF + CHAR, "value");
    public static final Signature JAVA_SYSTEM_ERR = 
        new Signature(JAVA_SYSTEM, "" + REFERENCE + JAVA_PRINTSTREAM + TYPEEND, "err");
    public static final Signature JAVA_SYSTEM_IN = 
        new Signature(JAVA_SYSTEM, "" + REFERENCE + JAVA_INPUTSTREAM + TYPEEND, "in");
    public static final Signature JAVA_SYSTEM_OUT = 
        new Signature(JAVA_SYSTEM, "" + REFERENCE + JAVA_PRINTSTREAM + TYPEEND, "out");
    public static final Signature JAVA_THREAD_PRIORITY = 
        new Signature(JAVA_THREAD, "" + INT, "priority");
    public static final Signature JAVA_THROWABLE_BACKTRACE = 
        new Signature(JAVA_THROWABLE, "" + REFERENCE + JAVA_OBJECT + TYPEEND, "backtrace");
    public static final Signature JAVA_THROWABLE_STACKTRACE = 
        new Signature(JAVA_THROWABLE, "" + ARRAYOF + REFERENCE + JAVA_STACKTRACEELEMENT + TYPEEND, "stackTrace");
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
    public static final Signature JBSE_BASE_JAVA_CLASS_PATH = 
        new Signature(JBSE_BASE, "" + REFERENCE + JAVA_STRING + TYPEEND, "JAVA_CLASS_PATH");
    public static final Signature JBSE_BASE_JAVA_EXT_DIRS = 
        new Signature(JBSE_BASE, "" + REFERENCE + JAVA_STRING + TYPEEND, "JAVA_EXT_DIRS");
    public static final Signature JBSE_BASE_JAVA_HOME = 
        new Signature(JBSE_BASE, "" + REFERENCE + JAVA_STRING + TYPEEND, "JAVA_HOME");
    public static final Signature JBSE_BASE_JAVA_LIBRARY_PATH = 
        new Signature(JBSE_BASE, "" + REFERENCE + JAVA_STRING + TYPEEND, "JAVA_LIBRARY_PATH");
    public static final Signature JBSE_BASE_JBSE_NAME = 
        new Signature(JBSE_BASE, "" + REFERENCE + JAVA_STRING + TYPEEND, "JBSE_NAME");
    public static final Signature JBSE_BASE_JBSE_VERSION = 
        new Signature(JBSE_BASE, "" + REFERENCE + JAVA_STRING + TYPEEND, "JBSE_VERSION");
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
    public static final Signature JBSE_BASE_SUN_BOOT_CLASS_PATH = 
        new Signature(JBSE_BASE, "" + REFERENCE + JAVA_STRING + TYPEEND, "SUN_BOOT_CLASS_PATH");
    public static final Signature JBSE_BASE_SUN_BOOT_LIBRARY_PATH = 
        new Signature(JBSE_BASE, "" + REFERENCE + JAVA_STRING + TYPEEND, "SUN_BOOT_LIBRARY_PATH");
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
    public static final Signature JBSE_BASE_USER_COUNTRY = 
        new Signature(JBSE_BASE, "" + REFERENCE + JAVA_STRING + TYPEEND, "USER_COUNTRY");
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
