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
    public static final String JAVA_ABSTRACTPIPELINE         = "java/util/stream/AbstractPipeline";
    public static final String JAVA_ACCESSCONTROLCONTEXT     = "java/security/AccessControlContext";
    public static final String JAVA_ACCESSCONTROLLER         = "java/security/AccessController";
    public static final String JAVA_ACCESSIBLEOBJECT         = "java/lang/reflect/AccessibleObject";
    public static final String JAVA_ANNOTATEDELEMENT         = "java/lang/reflect/AnnotatedElement";
    public static final String JAVA_ARRAYDEQUE               = "java/util/ArrayDeque";
    public static final String JAVA_ARRAYLIST                = "java/util/ArrayList";
    public static final String JAVA_ARRAYS_LEGACYMERGESORT   = "java/util/Arrays$LegacyMergeSort";
    public static final String JAVA_ATOMICLONG               = "java/util/concurrent/atomic/AtomicLong";
    public static final String JAVA_ATTRIBUTES_NAME          = "java/util/jar/Attributes$Name";
    public static final String JAVA_BITS                     = "java/nio/Bits";
    public static final String JAVA_BOOLEAN                  = "java/lang/Boolean";
    public static final String JAVA_BOUNDMETHODHANDLE        = "java/lang/invoke/BoundMethodHandle";
    public static final String JAVA_BOUNDMETHODHANDLE_FACTORY = "java/lang/invoke/BoundMethodHandle$Factory";
    public static final String JAVA_BOUNDMETHODHANDLE_SPECIESDATA = "java/lang/invoke/BoundMethodHandle$SpeciesData";
    public static final String JAVA_BOUNDMETHODHANDLE_SPECIES_L   = "java/lang/invoke/BoundMethodHandle$Species_L";
    public static final String JAVA_BUFFEREDIMAGE            = "java/awt/image/BufferedImage";
    public static final String JAVA_BYTE                     = "java/lang/Byte";
    public static final String JAVA_BYTE_BYTECACHE           = "java/lang/Byte$ByteCache";
    public static final String JAVA_BYTEBUFFER               = "java/nio/ByteBuffer";
    public static final String JAVA_CALLSITE                 = "java/lang/invoke/CallSite";
    public static final String JAVA_CHARACTER                = "java/lang/Character";
    public static final String JAVA_CHARACTER_CHARACTERCACHE = "java/lang/Character$CharacterCache";
    public static final String JAVA_CHARSEQUENCE             = "java/lang/CharSequence";
    public static final String JAVA_CHARSET_EXTENDEDPROVIDERHOLDER = "java/nio/charset/Charset$ExtendedProviderHolder";
    public static final String JAVA_CHARSETENCODER           = "java/nio/charset/CharsetEncoder";
    public static final String JAVA_CLASS                    = "java/lang/Class";
    public static final String JAVA_CLASSLOADER              = "java/lang/ClassLoader";
    public static final String JAVA_CLASSLOADER_NATIVELIBRARY   = "java/lang/ClassLoader$NativeLibrary";
    public static final String JAVA_CLASSVALUE_CLASSVALUEMAP = "java/lang/ClassValue$ClassValueMap";
    public static final String JAVA_CLONEABLE                = "java/lang/Cloneable";
    public static final String JAVA_COLLECTIONS_COPIESLIST   = "java/util/Collections$CopiesList";
    public static final String JAVA_COLLECTORS               = "java/util/stream/Collectors";
    public static final String JAVA_COLORMODEL               = "java/awt/image/ColorModel";
    public static final String JAVA_COMPARABLE               = "java/lang/Comparable";
    public static final String JAVA_CONCURRENTHASHMAP        = "java/util/concurrent/ConcurrentHashMap";
    public static final String JAVA_CONSTRUCTOR              = "java/lang/reflect/Constructor";
    public static final String JAVA_CRC32                    = "java/util/zip/CRC32";
    public static final String JAVA_DEFLATER                 = "java/util/zip/Deflater";
    public static final String JAVA_DELEGATINGMETHODHANDLE   = "java/lang/invoke/DelegatingMethodHandle";
    public static final String JAVA_DIRECTBYTEBUFFER         = "java/nio/DirectByteBuffer";
    public static final String JAVA_DIRECTLONGBUFFERU        = "java/nio/DirectLongBufferU";
    public static final String JAVA_DIRECTMETHODHANDLE       = "java/lang/invoke/DirectMethodHandle";
    public static final String JAVA_DIRECTMETHODHANDLE_CONSTRUCTOR = "java/lang/invoke/DirectMethodHandle$Constructor";
    public static final String JAVA_DIRECTMETHODHANDLE_ENSUREINITIALIZED = "java/lang/invoke/DirectMethodHandle$EnsureInitialized";
    public static final String JAVA_DIRECTMETHODHANDLE_INTERFACE = "java/lang/invoke/DirectMethodHandle$Interface";
    public static final String JAVA_DIRECTMETHODHANDLE_LAZY  = "java/lang/invoke/DirectMethodHandle$Lazy";
    public static final String JAVA_DOUBLE                   = "java/lang/Double";
    public static final String JAVA_ENUM                     = "java/lang/Enum";
    public static final String JAVA_ENUMMAP                  = "java/util/EnumMap";
    public static final String JAVA_ENUMSET                  = "java/util/EnumSet";
    public static final String JAVA_EXECUTABLE               = "java/lang/reflect/Executable";
    public static final String JAVA_FIELD                    = "java/lang/reflect/Field";
    public static final String JAVA_FILE                     = "java/io/File";
    public static final String JAVA_FILEDESCRIPTOR           = "java/io/FileDescriptor";
    public static final String JAVA_FILEINPUTSTREAM          = "java/io/FileInputStream";
    public static final String JAVA_FILEOUTPUTSTREAM         = "java/io/FileOutputStream";
    public static final String JAVA_FILEPERMISSION           = "java/io/FilePermission";
    public static final String JAVA_FINALIZER                = "java/lang/ref/Finalizer";
    public static final String JAVA_FINALREFERENCE           = "java/lang/ref/FinalReference";
    public static final String JAVA_FLOAT                    = "java/lang/Float";
    public static final String JAVA_FONT                     = "java/awt/Font";
    public static final String JAVA_GENERICDECLARATION       = "java/lang/reflect/GenericDeclaration";
    public static final String JAVA_HASHMAP                  = "java/util/HashMap";
    public static final String JAVA_HASHSET                  = "java/util/HashSet";
    public static final String JAVA_IDENTITYHASHMAP          = "java/util/IdentityHashMap";
    public static final String JAVA_INETADDRESS              = "java/net/InetAddress";
    public static final String JAVA_INNERCLASSLAMBDAMETAFACTORY = "java/lang/invoke/InnerClassLambdaMetafactory";
    public static final String JAVA_INPUTSTREAM              = "java/io/InputStream";
    public static final String JAVA_INFLATER                 = "java/util/zip/Inflater";
    public static final String JAVA_INFOFROMMEMBERNAME       = "java/lang/invoke/InfoFromMemberName";
    public static final String JAVA_INTEGER                  = "java/lang/Integer";
    public static final String JAVA_INTEGER_INTEGERCACHE     = "java/lang/Integer$IntegerCache";
    public static final String JAVA_INVOKERBYTECODEGENERATOR = "java/lang/invoke/InvokerBytecodeGenerator";
    public static final String JAVA_INVOKERBYTECODEGENERATOR_2 = "java/lang/invoke/InvokerBytecodeGenerator$2";
    public static final String JAVA_INVOKERS                 = "java/lang/invoke/Invokers";
    public static final String JAVA_JARFILE                  = "java/util/jar/JarFile";
    public static final String JAVA_JARVERIFIER              = "java/util/jar/JarVerifier";
    public static final String JAVA_LAMBDAFORM               = "java/lang/invoke/LambdaForm";
    public static final String JAVA_LAMBDAFORMBUFFER         = "java/lang/invoke/LambdaFormBuffer";
    public static final String JAVA_LAMBDAFORMEDITOR         = "java/lang/invoke/LambdaFormEditor";
    public static final String JAVA_LAMBDAFORMEDITOR_TRANSFORM = "java/lang/invoke/LambdaFormEditor$Transform";
    public static final String JAVA_LAMBDAFORM_NAME          = "java/lang/invoke/LambdaForm$Name";
    public static final String JAVA_LAMBDAFORM_NAMEDFUNCTION = "java/lang/invoke/LambdaForm$NamedFunction";
    public static final String JAVA_LAMBDAMETAFACTORY        = "java/lang/invoke/LambdaMetafactory";
    public static final String JAVA_LINKEDHASHMAP            = "java/util/LinkedHashMap";
    public static final String JAVA_LINKEDLIST               = "java/util/LinkedList";
    public static final String JAVA_LINKEDLIST_ENTRY         = "java/util/LinkedList$Entry";
    public static final String JAVA_LOCALE_1                 = "java/util/Locale$1";
    public static final String JAVA_LONG                     = "java/lang/Long";
    public static final String JAVA_LONG_LONGCACHE           = "java/lang/Long$LongCache";
    public static final String JAVA_MAP                      = "java/util/Map";
    public static final String JAVA_MAPPEDBYTEBUFFER         = "java/nio/MappedByteBuffer";
    public static final String JAVA_MEMBER                   = "java/lang/reflect/Member";
    public static final String JAVA_MEMBERNAME               = "java/lang/invoke/MemberName";
    public static final String JAVA_METHOD                   = "java/lang/reflect/Method";
    public static final String JAVA_METHODHANDLE             = "java/lang/invoke/MethodHandle";
    public static final String JAVA_METHODHANDLEIMPL_ARRAYACCESSOR = "java/lang/invoke/MethodHandleImpl$ArrayAccessor";
    public static final String JAVA_METHODHANDLEIMPL_COUNTINGWRAPPER = "java/lang/invoke/MethodHandleImpl$CountingWrapper";
    public static final String JAVA_METHODHANDLEIMPL_ASVARARGSCOLLECTOR = "java/lang/invoke/MethodHandleImpl$AsVarargsCollector";
    public static final String JAVA_METHODHANDLEIMPL_LAZY    = "java/lang/invoke/MethodHandleImpl$Lazy";
    public static final String JAVA_METHODHANDLENATIVES      = "java/lang/invoke/MethodHandleNatives";
    public static final String JAVA_METHODHANDLES            = "java/lang/invoke/MethodHandles";
    public static final String JAVA_METHODHANDLES_1          = "java/lang/invoke/MethodHandles$1";
    public static final String JAVA_METHODHANDLES_LOOKUP     = "java/lang/invoke/MethodHandles$Lookup";
    public static final String JAVA_METHODTYPE               = "java/lang/invoke/MethodType";
    public static final String JAVA_METHODTYPEFORM           = "java/lang/invoke/MethodTypeForm";
    public static final String JAVA_NETWORKINTERFACE         = "java/net/NetworkInterface";
    public static final String JAVA_OBJECT                   = "java/lang/Object";
    public static final String JAVA_OPTIONAL                 = "java/util/Optional";
    public static final String JAVA_PACKAGE                  = "java/lang/Package";
    public static final String JAVA_PARAMETER                = "java/lang/reflect/Parameter";
    public static final String JAVA_PATTERN                  = "java/util/regex/Pattern";
    public static final String JAVA_PLAINDATAGRAMSOCKETIMPL  = "java/net/PlainDatagramSocketImpl";
    public static final String JAVA_PLAINSOCKETIMPL          = "java/net/PlainSocketImpl";
    public static final String JAVA_PRINTSTREAM              = "java/io/PrintStream";
    public static final String JAVA_PRIVILEGEDACTION         = "java/security/PrivilegedAction";
    public static final String JAVA_PRIVILEGEDEXCEPTIONACTION = "java/security/PrivilegedExceptionAction";
    public static final String JAVA_PROCESSENVIRONMENT       = "java/lang/ProcessEnvironment";
    public static final String JAVA_PROPERTIES               = "java/util/Properties";
    public static final String JAVA_PROTECTIONDOMAIN         = "java/security/ProtectionDomain";
    public static final String JAVA_RANDOMACCESSFILE         = "java/io/RandomAccessFile";
    public static final String JAVA_REFERENCE                = "java/lang/ref/Reference";
    public static final String JAVA_REFERENCEPIPELINE_STATEFULOP  = "java/util/stream/ReferencePipeline$StatefulOp";
    public static final String JAVA_REFERENCEPIPELINE_STATELESSOP = "java/util/stream/ReferencePipeline$StatelessOp";
    public static final String JAVA_REFLECT_ARRAY            = "java/lang/reflect/Array";
    public static final String JAVA_RUNNABLE                 = "java/lang/Runnable";
    public static final String JAVA_RUNTIME                  = "java/lang/Runtime";
    public static final String JAVA_SERIALIZABLE             = "java/io/Serializable";
    public static final String JAVA_SHORT                    = "java/lang/Short";
    public static final String JAVA_SHORT_SHORTCACHE         = "java/lang/Short$ShortCache";
    public static final String JAVA_SIMPLEMETHODHANDLE       = "java/lang/invoke/SimpleMethodHandle";
    public static final String JAVA_STACKTRACEELEMENT        = "java/lang/StackTraceElement";
    public static final String JAVA_STANDARDCHARSETS         = "java/nio/charset/StandardCharsets";
    public static final String JAVA_STRICTMATH               = "java/lang/StrictMath";
    public static final String JAVA_STRING                   = "java/lang/String";
    public static final String JAVA_STRINGBUILDER            = "java/lang/StringBuilder";
    public static final String JAVA_STRINGCODING             = "java/lang/StringCoding";
    public static final String JAVA_SYSTEM                   = "java/lang/System";
    public static final String JAVA_THREAD                   = "java/lang/Thread";
    public static final String JAVA_THREAD_UNCAUGHTEXCEPTIONHANDLER = "java/lang/Thread$UncaughtExceptionHandler";
    public static final String JAVA_THREADGROUP              = "java/lang/ThreadGroup";
    public static final String JAVA_THROWABLE                = "java/lang/Throwable";
    public static final String JAVA_THROWABLE_SENTINELHOLDER = "java/lang/Throwable$SentinelHolder";
    public static final String JAVA_TIMSORT                  = "java/util/TimSort";
    public static final String JAVA_TREESET                  = "java/util/TreeSet";
    public static final String JAVA_TYPE                     = "java/lang/reflect/Type";
    public static final String JAVA_UNIXFILESYSTEM           = "java/io/UnixFileSystem";
    public static final String JAVA_URI                      = "java/net/URI";
    public static final String JAVA_URL                      = "java/net/URL";
    public static final String JAVA_VOID                     = "java/lang/Void";
    public static final String JAVA_WINNTFILESYSTEM          = "java/io/WinNTFileSystem";
    public static final String JAVA_ZIPFILE                  = "java/util/zip/ZipFile";
    public static final String JAVA_ZSTREAMREF               = "java/util/zip/ZStreamRef";
    public static final String JBSE_ANALYSIS                 = internalClassName(jbse.meta.Analysis.class.getName());
    public static final String JBSE_BASE                     = internalClassName(jbse.base.Base.class.getName());
    public static final String JBSE_JAVA_MAP                 = internalClassName(jbse.base.JAVA_MAP.class.getName());
    public static final String JDK_FRAME                     = "jdk/internal/org/objectweb/asm/Frame";
    public static final String JDK_TYPE                      = "jdk/internal/org/objectweb/asm/Type";
    public static final String SUN_ASCIICASEINSENSITIVECOMPARATOR = "sun/misc/ASCIICaseInsensitiveComparator";
    public static final String SUN_CALLERSENSITIVE           = "sun/reflect/CallerSensitive";
    public static final String SUN_CONSTANTPOOL              = "sun/reflect/ConstantPool";
    public static final String SUN_EXTENSIONDEPENDENCY       = "sun/misc/ExtensionDependency";
    public static final String SUN_JARINDEX                  = "sun/misc/JarIndex";
    public static final String SUN_LOCALEPROVIDERADAPTER     = "sun/util/locale/provider/LocaleProviderAdapter";
    public static final String SUN_LOCALEPROVIDERADAPTER_1   = "sun/util/locale/provider/LocaleProviderAdapter$1";
    public static final String SUN_NATIVECONSTRUCTORACCESSORIMPL = "sun/reflect/NativeConstructorAccessorImpl";
    public static final String SUN_NATIVEMETHODACCESSORIMPL  = "sun/reflect/NativeMethodAccessorImpl";
    public static final String SUN_PERF                      = "sun/misc/Perf";
    public static final String SUN_PERFCOUNTER               = "sun/misc/PerfCounter";
    public static final String SUN_PERFCOUNTER_CORECOUNTERS  = "sun/misc/PerfCounter$CoreCounters";
    public static final String SUN_REFLECTION                = "sun/reflect/Reflection";
    public static final String SUN_SECURITYCONSTANTS         = "sun/security/util/SecurityConstants";
    public static final String SUN_SIGNAL                    = "sun/misc/Signal";
    public static final String SUN_UNIXNATIVEDISPATCHER      = "sun/nio/fs/UnixNativeDispatcher";
    public static final String SUN_UNIXPATH                  = "sun/nio/fs/UnixPath";
    public static final String SUN_UNSAFE                    = "sun/misc/Unsafe";
    public static final String SUN_UNSAFEFIELDACCESSORIMPL   = "sun/reflect/UnsafeFieldAccessorImpl";
    public static final String SUN_URLCLASSPATH              = "sun/misc/URLClassPath";
    public static final String SUN_URLCLASSPATH_JARLOADER    = "sun/misc/URLClassPath$JarLoader";
    public static final String SUN_UTIL                      = "sun/nio/fs/Util";
    public static final String SUN_VALUECONVERSIONS          = "sun/invoke/util/ValueConversions";
    public static final String SUN_VALUECONVERSIONS_1        = "sun/invoke/util/ValueConversions$1";
    public static final String SUN_VERIFYACCESS              = "sun/invoke/util/VerifyAccess";
    public static final String SUN_VERIFYTYPE                = "sun/invoke/util/VerifyType";
    public static final String SUN_VM                        = "sun/misc/VM";
    public static final String SUN_WIN32ERRORMODE            = "sun/io/Win32ErrorMode";
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
    public static final String FILE_NOT_FOUND_EXCEPTION            = "java/io/FileNotFoundException";
    public static final String ILLEGAL_ARGUMENT_EXCEPTION          = "java/lang/IllegalArgumentException";
    public static final String ILLEGAL_MONITOR_STATE_EXCEPTION     = "java/lang/IllegalMonitorStateException";
    public static final String INDEX_OUT_OF_BOUNDS_EXCEPTION       = "java/lang/IndexOutOfBoundsException";
    public static final String INSTANTIATION_EXCEPTION             = "java/lang/InstantiationException";
    public static final String IO_EXCEPTION                        = "java/io/IOException";
    public static final String NEGATIVE_ARRAY_SIZE_EXCEPTION       = "java/lang/NegativeArraySizeException";
    public static final String NULL_POINTER_EXCEPTION              = "java/lang/NullPointerException";
    public static final String RUNTIME_EXCEPTION                   = "java/lang/RuntimeException";
    
    //errors
    public static final String ABSTRACT_METHOD_ERROR               = "java/lang/AbstractMethodError";
    public static final String CLASS_FORMAT_ERROR                  = "java/lang/ClassFormatError";
    public static final String ERROR                               = "java/lang/Error";
    public static final String ILLEGAL_ACCESS_ERROR                = "java/lang/IllegalAccessError";
    public static final String INCOMPATIBLE_CLASS_CHANGE_ERROR     = "java/lang/IncompatibleClassChangeError";
    public static final String INTERNAL_ERROR                      = "java/lang/InternalError";
    public static final String LINKAGE_ERROR                       = "java/lang/LinkageError";
    public static final String NO_CLASS_DEFINITION_FOUND_ERROR     = "java/lang/NoClassDefFoundError";
    public static final String NO_SUCH_FIELD_ERROR                 = "java/lang/NoSuchFieldError";
    public static final String NO_SUCH_METHOD_ERROR                = "java/lang/NoSuchMethodError";
    public static final String OUT_OF_MEMORY_ERROR                 = "java/lang/OutOfMemoryError";
    public static final String STACK_OVERFLOW_ERROR                = "java/lang/StackOverflowError";
    public static final String UNSUPPORTED_CLASS_VERSION_ERROR     = "java/lang/UnsupportedClassVersionError";
    public static final String VERIFY_ERROR                        = "java/lang/VerifyError";
    public static final String VIRTUAL_MACHINE_ERROR               = "java/lang/VirtualMachineError";
    
    //method descriptors (for signature polymorphic methods)
    public static final String SIGNATURE_POLYMORPHIC_DESCRIPTOR     = "(" + ARRAYOF + REFERENCE + JAVA_OBJECT + TYPEEND + ")" + REFERENCE + JAVA_OBJECT + TYPEEND;
    
    //methods (empty signature)
    public static final Signature EMPTY_METHOD_SIGNATURE            = new Signature(null, null, null);
    
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
    public static final Signature JAVA_ACCESSCONTROLLER_DOPRIVILEGED_NOEXCEPTION_1 =
        new Signature(JAVA_ACCESSCONTROLLER, "(" + REFERENCE + JAVA_PRIVILEGEDACTION + TYPEEND + ")" + REFERENCE + JAVA_OBJECT + TYPEEND, "doPrivileged");
    public static final Signature JAVA_ACCESSCONTROLLER_DOPRIVILEGED_NOEXCEPTION_2 =
            new Signature(JAVA_ACCESSCONTROLLER, "(" + REFERENCE + JAVA_PRIVILEGEDACTION + TYPEEND + REFERENCE + JAVA_ACCESSCONTROLCONTEXT + TYPEEND + ")" + REFERENCE + JAVA_OBJECT + TYPEEND, "doPrivileged");
    public static final Signature JAVA_ACCESSCONTROLLER_GETSTACKACCESSCONTROLCONTEXT =
        new Signature(JAVA_ACCESSCONTROLLER, "()" + REFERENCE + JAVA_ACCESSCONTROLCONTEXT + TYPEEND, "getStackAccessControlContext");
    public static final Signature JAVA_ATOMICLONG_VMSUPPORTSCS8 =
        new Signature(JAVA_ATOMICLONG, "()" + BOOLEAN, "VMSupportsCS8");
    public static final Signature JAVA_BUFFEREDIMAGE_INITIDS =
    		new Signature(JAVA_BUFFEREDIMAGE, "()" + VOID, "initIDs");
    public static final Signature JAVA_CLASS_DESIREDASSERTIONSTATUS0 =
        new Signature(JAVA_CLASS, "(" + REFERENCE + JAVA_CLASS + TYPEEND + ")" + BOOLEAN, "desiredAssertionStatus0");
    public static final Signature JAVA_CLASS_FORNAME0 =
        new Signature(JAVA_CLASS, "(" + REFERENCE + JAVA_STRING + TYPEEND + BOOLEAN + REFERENCE + JAVA_CLASSLOADER + TYPEEND + REFERENCE + JAVA_CLASS + TYPEEND + ")" + REFERENCE + JAVA_CLASS + TYPEEND, "forName0");
    public static final Signature JAVA_CLASS_GETCOMPONENTTYPE =
        new Signature(JAVA_CLASS, "()" + REFERENCE + JAVA_CLASS + TYPEEND, "getComponentType");
    public static final Signature JAVA_CLASS_GETCONSTANTPOOL =
        new Signature(JAVA_CLASS, "()" + REFERENCE + SUN_CONSTANTPOOL + TYPEEND, "getConstantPool");
    public static final Signature JAVA_CLASS_GETDECLAREDCONSTRUCTORS0 =
        new Signature(JAVA_CLASS, "(" + BOOLEAN + ")" + ARRAYOF + REFERENCE + JAVA_CONSTRUCTOR + TYPEEND, "getDeclaredConstructors0");
    public static final Signature JAVA_CLASS_GETDECLAREDFIELDS0 =
        new Signature(JAVA_CLASS, "(" + BOOLEAN + ")" + ARRAYOF + REFERENCE + JAVA_FIELD + TYPEEND, "getDeclaredFields0");
    public static final Signature JAVA_CLASS_GETDECLAREDMETHODS0 =
        new Signature(JAVA_CLASS, "(" + BOOLEAN + ")" + ARRAYOF + REFERENCE + JAVA_METHOD + TYPEEND, "getDeclaredMethods0");
    public static final Signature JAVA_CLASS_GETDECLARINGCLASS0 =
        new Signature(JAVA_CLASS, "()" + REFERENCE + JAVA_CLASS + TYPEEND, "getDeclaringClass0");
    public static final Signature JAVA_CLASS_GETENCLOSINGMETHOD0 =
        new Signature(JAVA_CLASS, "()" + ARRAYOF + REFERENCE + JAVA_OBJECT + TYPEEND, "getEnclosingMethod0");
    public static final Signature JAVA_CLASS_GETGENERICSIGNATURE0 =
        new Signature(JAVA_CLASS, "()" + REFERENCE + JAVA_STRING + TYPEEND, "getGenericSignature0");
    public static final Signature JAVA_CLASS_GETINTERFACES0 =
        new Signature(JAVA_CLASS, "()" + ARRAYOF + REFERENCE + JAVA_CLASS + TYPEEND, "getInterfaces0");
    public static final Signature JAVA_CLASS_GETMODIFIERS =
        new Signature(JAVA_CLASS, "()" + INT, "getModifiers");
    public static final Signature JAVA_CLASS_GETNAME0 =
        new Signature(JAVA_CLASS, "()" + REFERENCE + JAVA_STRING + TYPEEND, "getName0");
    public static final Signature JAVA_CLASS_GETPRIMITIVECLASS =
        new Signature(JAVA_CLASS, "(" + REFERENCE + JAVA_STRING + TYPEEND + ")" + REFERENCE + JAVA_CLASS + TYPEEND, "getPrimitiveClass");
    public static final Signature JAVA_CLASS_GETPROTECTIONDOMAIN0 =
        new Signature(JAVA_CLASS, "()" + REFERENCE + JAVA_PROTECTIONDOMAIN + TYPEEND, "getProtectionDomain0");
    public static final Signature JAVA_CLASS_GETRAWANNOTATIONS =
        new Signature(JAVA_CLASS, "()" + ARRAYOF + BYTE, "getRawAnnotations");
    public static final Signature JAVA_CLASS_GETSIGNERS =
        new Signature(JAVA_CLASS, "()" + ARRAYOF + REFERENCE + JAVA_OBJECT + TYPEEND, "getSigners");
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
    public static final Signature JAVA_CLASS_SETSIGNERS =
        new Signature(JAVA_CLASS, "(" + ARRAYOF + REFERENCE + JAVA_OBJECT + TYPEEND + ")" + VOID, "setSigners");
    public static final Signature JAVA_CLASSLOADER_DEFINECLASS1 =
        new Signature(JAVA_CLASSLOADER, "(" + REFERENCE + JAVA_STRING + TYPEEND + ARRAYOF + BYTE + INT + INT + REFERENCE + JAVA_PROTECTIONDOMAIN + TYPEEND + REFERENCE + JAVA_STRING + TYPEEND + ")" + REFERENCE + JAVA_CLASS + TYPEEND, "defineClass1");
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
    public static final Signature JAVA_COLORMODEL_INITIDS =
    	new Signature(JAVA_COLORMODEL, "()" + VOID, "initIDs");
    public static final Signature JAVA_CRC32_UPDATE =
        new Signature(JAVA_CRC32, "(" + INT + INT + ")" + INT, "update");
    public static final Signature JAVA_CRC32_UPDATEBYTES =
        new Signature(JAVA_CRC32, "(" + INT + ARRAYOF + BYTE + INT + INT +")" + INT, "updateBytes");
    public static final Signature JAVA_DEFLATER_INITIDS =
        new Signature(JAVA_DEFLATER, "()" + VOID, "initIDs");
    public static final Signature JAVA_DIRECTBYTEBUFFER_INIT =
        new Signature(JAVA_DIRECTBYTEBUFFER, "(" + LONG + INT + ")" + VOID, "<init>");
    public static final Signature JAVA_DOUBLE_DOUBLETORAWLONGBITS =
        new Signature(JAVA_DOUBLE, "(" + DOUBLE + ")" + LONG, "doubleToRawLongBits");
    public static final Signature JAVA_DOUBLE_LONGBITSTODOUBLE =
        new Signature(JAVA_DOUBLE, "(" + LONG + ")" + DOUBLE, "longBitsToDouble");
    public static final Signature JAVA_EXECUTABLE_GETPARAMETERS0 =
        new Signature(JAVA_EXECUTABLE, "()" + ARRAYOF + REFERENCE + JAVA_PARAMETER + TYPEEND, "getParameters0");
    public static final Signature JAVA_FILEDESCRIPTOR_INITIDS =
        new Signature(JAVA_FILEDESCRIPTOR, "()" + VOID, "initIDs");
    public static final Signature JAVA_FILEDESCRIPTOR_SET =
        new Signature(JAVA_FILEDESCRIPTOR, "(" + INT + ")" + LONG, "set");
    public static final Signature JAVA_FILEINPUTSTREAM_AVAILABLE =
        new Signature(JAVA_FILEINPUTSTREAM, "()" + INT, "available");
    public static final Signature JAVA_FILEINPUTSTREAM_CLOSE0 =
        new Signature(JAVA_FILEINPUTSTREAM, "()" + VOID, "close0");
    public static final Signature JAVA_FILEINPUTSTREAM_INITIDS =
        new Signature(JAVA_FILEINPUTSTREAM, "()" + VOID, "initIDs");
    public static final Signature JAVA_FILEINPUTSTREAM_OPEN0 =
        new Signature(JAVA_FILEINPUTSTREAM, "(" + REFERENCE + JAVA_STRING + TYPEEND + ")" + VOID, "open0");
    public static final Signature JAVA_FILEINPUTSTREAM_READBYTES =
        new Signature(JAVA_FILEINPUTSTREAM, "(" + ARRAYOF + BYTE + INT + INT + ")" + INT, "readBytes");
    public static final Signature JAVA_FILEOUTPUTSTREAM_INITIDS =
        new Signature(JAVA_FILEOUTPUTSTREAM, "()" + VOID, "initIDs");
    public static final Signature JAVA_FILEOUTPUTSTREAM_OPEN0 =
    	new Signature(JAVA_FILEOUTPUTSTREAM, "(" + REFERENCE + JAVA_STRING + TYPEEND + BOOLEAN + ")" + VOID, "open0");
    public static final Signature JAVA_FILEOUTPUTSTREAM_WRITEBYTES =
        new Signature(JAVA_FILEOUTPUTSTREAM, "(" + ARRAYOF + BYTE + INT + INT + BOOLEAN + ")" + VOID, "writeBytes");
    public static final Signature JAVA_FLOAT_FLOATTORAWINTBITS =
        new Signature(JAVA_FLOAT, "(" + FLOAT + ")" + INT, "floatToRawIntBits");
    public static final Signature JAVA_FLOAT_INTBITSTOFLOAT =
    	new Signature(JAVA_FLOAT, "(" + INT + ")" + FLOAT, "intBitsToFloat");
    public static final Signature JAVA_FONT_INITIDS =
    	new Signature(JAVA_FONT, "()" + VOID, "initIDs");
    public static final Signature JAVA_INETADDRESS_INIT=
    	new Signature(JAVA_INETADDRESS, "()" + VOID, "init");
    public static final Signature JAVA_INFLATER_END =
        new Signature(JAVA_INFLATER, "(" + LONG + ")" + VOID, "end");
    public static final Signature JAVA_INFLATER_GETADLER =
        new Signature(JAVA_INFLATER, "(" + LONG + ")" + INT, "getAdler");
    public static final Signature JAVA_INFLATER_INFLATEBYTES =
        new Signature(JAVA_INFLATER, "(" + LONG + ARRAYOF + BYTE + INT + INT + ")" + INT, "inflateBytes");
    public static final Signature JAVA_INFLATER_INIT =
        new Signature(JAVA_INFLATER, "(" + BOOLEAN + ")" + LONG, "init");
    public static final Signature JAVA_INFLATER_INITIDS =
        new Signature(JAVA_INFLATER, "()" + VOID, "initIDs");
    public static final Signature JAVA_INFLATER_RESET =
        new Signature(JAVA_INFLATER, "(" + LONG + ")" + VOID, "reset");
    public static final Signature JAVA_INFLATER_SETDICTIONARY =
        new Signature(JAVA_INFLATER, "(" + LONG + ARRAYOF + BYTE + INT + INT + ")" + VOID, "setDictionary");
    public static final Signature JAVA_JARFILE_GETMETAINFENTRYNAMES =
        new Signature(JAVA_JARFILE, "()" + ARRAYOF + REFERENCE + JAVA_STRING + TYPEEND, "getMetaInfEntryNames");
    public static final Signature JAVA_LINKEDHASHMAP_CONTAINSVALUE =
		new Signature(JAVA_LINKEDHASHMAP, "(" + REFERENCE + JAVA_OBJECT + TYPEEND + ")" + BOOLEAN, "containsValue");
    public static final Signature JAVA_MAP_CONTAINSKEY =
        new Signature(JAVA_MAP, "(" + REFERENCE + JAVA_OBJECT + TYPEEND + ")" + BOOLEAN, "containsKey");
    public static final Signature JAVA_MAP_CONTAINSVALUE =
    	new Signature(JAVA_MAP, "(" + REFERENCE + JAVA_OBJECT + TYPEEND + ")" + BOOLEAN, "containsValue");
    public static final Signature JAVA_MAP_GET =
        new Signature(JAVA_MAP, "(" + REFERENCE + JAVA_OBJECT + TYPEEND + ")" + REFERENCE + JAVA_OBJECT + TYPEEND, "get");
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
    public static final Signature JAVA_METHODHANDLENATIVES_GETMEMBERS =
        new Signature(JAVA_METHODHANDLENATIVES, 
        		      "(" + REFERENCE + JAVA_CLASS + TYPEEND + REFERENCE + JAVA_STRING + TYPEEND + 
        		       REFERENCE + JAVA_STRING + TYPEEND + INT + REFERENCE + JAVA_CLASS + TYPEEND + 
        		       INT + ARRAYOF +  REFERENCE + JAVA_MEMBERNAME + TYPEEND + ")" + INT, "getMembers");
    public static final Signature JAVA_METHODHANDLENATIVES_INIT =
        new Signature(JAVA_METHODHANDLENATIVES, 
                      "(" + REFERENCE + JAVA_MEMBERNAME + TYPEEND + REFERENCE + JAVA_OBJECT + TYPEEND + ")" + VOID, 
                      "init");
    public static final Signature JAVA_METHODHANDLENATIVES_LINKCALLSITE =
        new Signature(JAVA_METHODHANDLENATIVES, 
                          "(" + REFERENCE + JAVA_OBJECT + TYPEEND + REFERENCE + JAVA_OBJECT + TYPEEND + REFERENCE + JAVA_OBJECT + TYPEEND + 
                          REFERENCE + JAVA_OBJECT + TYPEEND + REFERENCE + JAVA_OBJECT + TYPEEND + ARRAYOF + REFERENCE + JAVA_OBJECT + TYPEEND + ")" + REFERENCE + JAVA_MEMBERNAME + TYPEEND,
                          "linkCallSite");
    public static final Signature JAVA_METHODHANDLENATIVES_LINKMETHOD =
        new Signature(JAVA_METHODHANDLENATIVES, 
                      "(" + REFERENCE + JAVA_CLASS + TYPEEND + INT + REFERENCE + JAVA_CLASS + TYPEEND + REFERENCE + JAVA_STRING + TYPEEND + 
                      REFERENCE + JAVA_OBJECT + TYPEEND + ARRAYOF + REFERENCE + JAVA_OBJECT + TYPEEND + ")" + REFERENCE + JAVA_MEMBERNAME + TYPEEND,
                      "linkMethod");
    public static final Signature JAVA_METHODHANDLENATIVES_LINKMETHODHANDLECONSTANT =
            new Signature(JAVA_METHODHANDLENATIVES, 
                          "(" + REFERENCE + JAVA_CLASS + TYPEEND + INT + REFERENCE + JAVA_CLASS + TYPEEND + REFERENCE + JAVA_STRING + TYPEEND + 
                          REFERENCE + JAVA_OBJECT + TYPEEND + ")" + REFERENCE + JAVA_METHODHANDLE + TYPEEND,
                          "linkMethodHandleConstant");
    public static final Signature JAVA_METHODHANDLENATIVES_OBJECTFIELDOFFSET =
        new Signature(JAVA_METHODHANDLENATIVES, "(" + REFERENCE + JAVA_MEMBERNAME + TYPEEND + ")" + LONG, "objectFieldOffset");
    public static final Signature JAVA_METHODHANDLENATIVES_REGISTERNATIVES =
        new Signature(JAVA_METHODHANDLENATIVES, "()" + VOID, "registerNatives");
    public static final Signature JAVA_METHODHANDLENATIVES_RESOLVE =
        new Signature(JAVA_METHODHANDLENATIVES, 
                      "(" + REFERENCE + JAVA_MEMBERNAME + TYPEEND + REFERENCE + JAVA_CLASS + TYPEEND + ")" + REFERENCE + JAVA_MEMBERNAME + TYPEEND, 
                      "resolve");
    public static final Signature JAVA_METHODHANDLENATIVES_SETCALLSITETARGETNORMAL =
        new Signature(JAVA_METHODHANDLENATIVES, "(" + REFERENCE + JAVA_CALLSITE + TYPEEND + REFERENCE + JAVA_METHODHANDLE + TYPEEND + ")" + VOID, "setCallSiteTargetNormal");
    public static final Signature JAVA_METHODHANDLENATIVES_SETCALLSITETARGETVOLATILE =
        new Signature(JAVA_METHODHANDLENATIVES, "(" + REFERENCE + JAVA_CALLSITE + TYPEEND + REFERENCE + JAVA_METHODHANDLE + TYPEEND + ")" + VOID, "setCallSiteTargetVolatile");
    public static final Signature JAVA_METHODHANDLENATIVES_STATICFIELDOFFSET =
        new Signature(JAVA_METHODHANDLENATIVES, "(" + REFERENCE + JAVA_MEMBERNAME + TYPEEND + ")" + LONG, "staticFieldOffset");
    public static final Signature JAVA_METHODTYPE_TOMETHODDESCRIPTORSTRING =
        new Signature(JAVA_METHODTYPE, "()" + REFERENCE + JAVA_STRING + TYPEEND, "toMethodDescriptorString");
    public static final Signature JAVA_NETWORKINTERFACE_INIT=
    	new Signature(JAVA_NETWORKINTERFACE, "()" + VOID, "init");
    public static final Signature JAVA_OBJECT_CLONE =
        new Signature(JAVA_OBJECT, "()" + REFERENCE + JAVA_OBJECT + TYPEEND, "clone");
    public static final Signature JAVA_OBJECT_EQUALS =
        new Signature(JAVA_OBJECT, "(" + REFERENCE + JAVA_OBJECT + TYPEEND + ")" + BOOLEAN, "equals");
    public static final Signature JAVA_OBJECT_GETCLASS =
        new Signature(JAVA_OBJECT, "()" + REFERENCE + JAVA_CLASS + TYPEEND, "getClass");
    public static final Signature JAVA_OBJECT_HASHCODE =
        new Signature(JAVA_OBJECT, "()" + INT, "hashCode");
    public static final Signature JAVA_OBJECT_NOTIFY =
        new Signature(JAVA_OBJECT, "()" + VOID, "notify");
    public static final Signature JAVA_OBJECT_NOTIFYALL =
        new Signature(JAVA_OBJECT, "()" + VOID, "notifyAll");
    public static final Signature JAVA_OBJECT_REGISTERNATIVES =
        new Signature(JAVA_OBJECT, "()" + VOID, "registerNatives");
    public static final Signature JAVA_OBJECT_WAIT =
        new Signature(JAVA_OBJECT, "(" + LONG + ")" + VOID, "wait");
    public static final Signature JAVA_PACKAGE_GETSYSTEMPACKAGE0 =
        new Signature(JAVA_PACKAGE, "(" + REFERENCE + JAVA_STRING + TYPEEND + ")" + REFERENCE + JAVA_STRING + TYPEEND, "getSystemPackage0");
    public static final Signature JAVA_PLAINDATAGRAMSOCKETIMPL_INIT =
        new Signature(JAVA_PLAINDATAGRAMSOCKETIMPL, "()" + VOID, "init");
    public static final Signature JAVA_PLAINSOCKETIMPL_INITPROTO =
        new Signature(JAVA_PLAINSOCKETIMPL, "()" + VOID, "initProto");
    public static final Signature JAVA_PROCESSENVIRONMENT_ENVIRON =
        new Signature(JAVA_PROCESSENVIRONMENT, "()" + ARRAYOF + ARRAYOF + BYTE, "environ");
    public static final Signature JAVA_RANDOMACCESSFILE_FD = 
    	new Signature(JAVA_RANDOMACCESSFILE, "" + REFERENCE + JAVA_FILEDESCRIPTOR + TYPEEND, "fd");
    public static final Signature JAVA_RANDOMACCESSFILE_INITIDS =
        new Signature(JAVA_RANDOMACCESSFILE, "()" + VOID, "initIDs");
    public static final Signature JAVA_RANDOMACCESSFILE_OPEN0 =
    	new Signature(JAVA_RANDOMACCESSFILE, "(" + REFERENCE + JAVA_STRING + TYPEEND + INT + ")" + VOID, "open0");
    public static final Signature JAVA_REFLECT_ARRAY_NEWARRAY =
        new Signature(JAVA_REFLECT_ARRAY, "(" + REFERENCE + JAVA_CLASS + TYPEEND + INT + ")" + REFERENCE + JAVA_OBJECT + TYPEEND, "newArray");
    public static final Signature JAVA_RUNTIME_AVAILABLEPROCESSORS = 
        new Signature(JAVA_RUNTIME, "()" + INT, "availableProcessors");
    public static final Signature JAVA_STRICTMATH_ABS_DOUBLE = 
        new Signature(JAVA_STRICTMATH, "(" + DOUBLE + ")" + DOUBLE, "abs");
    public static final Signature JAVA_STRICTMATH_ABS_FLOAT = 
        new Signature(JAVA_STRICTMATH, "(" + FLOAT + ")" + FLOAT, "abs");
    public static final Signature JAVA_STRICTMATH_ABS_INT = 
        new Signature(JAVA_STRICTMATH, "(" + INT + ")" + INT, "abs");
    public static final Signature JAVA_STRICTMATH_ABS_LONG = 
        new Signature(JAVA_STRICTMATH, "(" + LONG + ")" + LONG, "abs");
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
    public static final Signature JAVA_STRICTMATH_MAX_DOUBLE = 
        new Signature(JAVA_STRICTMATH, "(" + DOUBLE + DOUBLE + ")" + DOUBLE, "max");
    public static final Signature JAVA_STRICTMATH_MAX_FLOAT = 
        new Signature(JAVA_STRICTMATH, "(" + FLOAT + FLOAT + ")" + FLOAT, "max");
    public static final Signature JAVA_STRICTMATH_MAX_INT = 
        new Signature(JAVA_STRICTMATH, "(" + INT + INT + ")" + INT, "max");
    public static final Signature JAVA_STRICTMATH_MAX_LONG = 
        new Signature(JAVA_STRICTMATH, "(" + LONG + LONG + ")" + LONG, "max");
    public static final Signature JAVA_STRICTMATH_MIN_DOUBLE = 
        new Signature(JAVA_STRICTMATH, "(" + DOUBLE + DOUBLE + ")" + DOUBLE, "min");
    public static final Signature JAVA_STRICTMATH_MIN_FLOAT = 
        new Signature(JAVA_STRICTMATH, "(" + FLOAT + FLOAT + ")" + FLOAT, "min");
    public static final Signature JAVA_STRICTMATH_MIN_INT = 
        new Signature(JAVA_STRICTMATH, "(" + INT + INT + ")" + INT, "min");
    public static final Signature JAVA_STRICTMATH_MIN_LONG = 
        new Signature(JAVA_STRICTMATH, "(" + LONG + LONG + ")" + LONG, "min");
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
    public static final Signature JAVA_STRING_EQUALS = 
        new Signature(JAVA_STRING, "(" + REFERENCE + JAVA_OBJECT + TYPEEND + ")" + BOOLEAN, "equals");
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
    public static final Signature JAVA_SYSTEM_CURRENTTIMEMILLIS = 
        new Signature(JAVA_SYSTEM, "()" + LONG, "currentTimeMillis");
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
    public static final Signature JAVA_THREAD_ISINTERRUPTED =
        new Signature(JAVA_THREAD, "(" + BOOLEAN + ")" + BOOLEAN, "isInterrupted");
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
    public static final Signature JAVA_UNIXFILESYSTEM_CANONICALIZE0 = 
        new Signature(JAVA_UNIXFILESYSTEM, "(" + REFERENCE + JAVA_STRING + TYPEEND + ")" + REFERENCE + JAVA_STRING + TYPEEND, "canonicalize0");
    public static final Signature JAVA_UNIXFILESYSTEM_CHECKACCESS = 
        new Signature(JAVA_UNIXFILESYSTEM, "(" + REFERENCE + JAVA_FILE + TYPEEND + INT + ")" + BOOLEAN, "checkAccess");
    public static final Signature JAVA_UNIXFILESYSTEM_GETBOOLEANATTRIBUTES0 = 
        new Signature(JAVA_UNIXFILESYSTEM, "(" + REFERENCE + JAVA_FILE + TYPEEND + ")" + INT, "getBooleanAttributes0");
    public static final Signature JAVA_UNIXFILESYSTEM_GETLASTMODIFIEDTIME = 
        new Signature(JAVA_UNIXFILESYSTEM, "(" + REFERENCE + JAVA_FILE + TYPEEND + ")" + LONG, "getLastModifiedTime");
    public static final Signature JAVA_UNIXFILESYSTEM_GETLENGTH = 
        new Signature(JAVA_UNIXFILESYSTEM, "(" + REFERENCE + JAVA_FILE + TYPEEND + ")" + LONG, "getLength");
    public static final Signature JAVA_UNIXFILESYSTEM_INITIDS = 
        new Signature(JAVA_UNIXFILESYSTEM, "()" + VOID, "initIDs");
    public static final Signature JAVA_UNIXFILESYSTEM_LIST = 
        new Signature(JAVA_UNIXFILESYSTEM, "(" + REFERENCE + JAVA_FILE + TYPEEND + ")" + ARRAYOF + REFERENCE + JAVA_STRING + TYPEEND, "list");
    public static final Signature JAVA_WINNTFILESYSTEM_CANONICALIZE0 = 
        new Signature(JAVA_WINNTFILESYSTEM, "(" + REFERENCE + JAVA_STRING + TYPEEND + ")" + REFERENCE + JAVA_STRING + TYPEEND, "canonicalize0");
    public static final Signature JAVA_WINNTFILESYSTEM_CANONICALIZEWITHPREFIX0 = 
            new Signature(JAVA_WINNTFILESYSTEM, "(" + REFERENCE + JAVA_STRING + TYPEEND + REFERENCE + JAVA_STRING + TYPEEND + ")" + REFERENCE + JAVA_STRING + TYPEEND, "canonicalizeWithPrefix0");
    public static final Signature JAVA_WINNTFILESYSTEM_CHECKACCESS = 
        new Signature(JAVA_WINNTFILESYSTEM, "(" + REFERENCE + JAVA_FILE + TYPEEND + INT + ")" + BOOLEAN, "checkAccess");
    public static final Signature JAVA_WINNTFILESYSTEM_GETBOOLEANATTRIBUTES = 
        new Signature(JAVA_WINNTFILESYSTEM, "(" + REFERENCE + JAVA_FILE + TYPEEND + ")" + INT, "getBooleanAttributes");
    public static final Signature JAVA_WINNTFILESYSTEM_GETLASTMODIFIEDTIME = 
        new Signature(JAVA_WINNTFILESYSTEM, "(" + REFERENCE + JAVA_FILE + TYPEEND + ")" + LONG, "getLastModifiedTime");
    public static final Signature JAVA_WINNTFILESYSTEM_GETLENGTH = 
        new Signature(JAVA_WINNTFILESYSTEM, "(" + REFERENCE + JAVA_FILE + TYPEEND + ")" + LONG, "getLength");
    public static final Signature JAVA_WINNTFILESYSTEM_INITIDS = 
        new Signature(JAVA_WINNTFILESYSTEM, "()" + VOID, "initIDs");
    public static final Signature JAVA_WINNTFILESYSTEM_LIST = 
        new Signature(JAVA_WINNTFILESYSTEM, "(" + REFERENCE + JAVA_FILE + TYPEEND + ")" + ARRAYOF + REFERENCE + JAVA_STRING + TYPEEND, "list");
    public static final Signature JAVA_ZIPFILE_FREEENTRY = 
        new Signature(JAVA_ZIPFILE, "(" + LONG + LONG + ")" + VOID, "freeEntry");
    public static final Signature JAVA_ZIPFILE_GETENTRY = 
        new Signature(JAVA_ZIPFILE, "(" + LONG + ARRAYOF + BYTE + BOOLEAN + ")" + LONG, "getEntry");
    public static final Signature JAVA_ZIPFILE_GETENTRYBYTES = 
        new Signature(JAVA_ZIPFILE, "(" + LONG + INT + ")" + ARRAYOF + BYTE, "getEntryBytes");
    public static final Signature JAVA_ZIPFILE_GETENTRYCRC = 
        new Signature(JAVA_ZIPFILE, "(" + LONG + ")" + LONG, "getEntryCrc");
    public static final Signature JAVA_ZIPFILE_GETENTRYCSIZE = 
        new Signature(JAVA_ZIPFILE, "(" + LONG + ")" + LONG, "getEntryCSize");
    public static final Signature JAVA_ZIPFILE_GETENTRYFLAG = 
        new Signature(JAVA_ZIPFILE, "(" + LONG + ")" + INT, "getEntryFlag");
    public static final Signature JAVA_ZIPFILE_GETENTRYMETHOD = 
        new Signature(JAVA_ZIPFILE, "(" + LONG + ")" + INT, "getEntryMethod");
    public static final Signature JAVA_ZIPFILE_GETENTRYSIZE = 
        new Signature(JAVA_ZIPFILE, "(" + LONG + ")" + LONG, "getEntrySize");
    public static final Signature JAVA_ZIPFILE_GETENTRYTIME = 
        new Signature(JAVA_ZIPFILE, "(" + LONG + ")" + LONG, "getEntryTime");
    public static final Signature JAVA_ZIPFILE_GETNEXTENTRY = 
        new Signature(JAVA_ZIPFILE, "(" + LONG + INT + ")" + LONG, "getNextEntry");
    public static final Signature JAVA_ZIPFILE_GETTOTAL = 
        new Signature(JAVA_ZIPFILE, "(" + LONG + ")" + INT, "getTotal");
    public static final Signature JAVA_ZIPFILE_INITIDS = 
        new Signature(JAVA_ZIPFILE, "()" + VOID, "initIDs");
    public static final Signature JAVA_ZIPFILE_OPEN = 
        new Signature(JAVA_ZIPFILE, "(" + REFERENCE + JAVA_STRING + TYPEEND + INT + LONG + BOOLEAN + ")" + LONG, "open");
    public static final Signature JAVA_ZIPFILE_READ = 
        new Signature(JAVA_ZIPFILE, "(" + LONG + LONG + LONG + ARRAYOF + BYTE + INT + INT + ")" + INT, "read");
    public static final Signature JAVA_ZIPFILE_STARTSWITHLOC = 
        new Signature(JAVA_ZIPFILE, "(" + LONG + ")" + BOOLEAN, "startsWithLOC");
    public static final Signature JBSE_ANALYSIS_ANY = 
        new Signature(JBSE_ANALYSIS, "()" + BOOLEAN, "any");
    public static final Signature JBSE_ANALYSIS_ASSUMECLASSNOTINITIALIZED = 
        new Signature(JBSE_ANALYSIS, "(" + REFERENCE + JAVA_CLASS + TYPEEND + ")" + VOID, "assumeClassNotInitialized");
    public static final Signature JBSE_ANALYSIS_ENDGUIDANCE = 
        new Signature(JBSE_ANALYSIS, "()" + VOID, "endGuidance");
    public static final Signature JBSE_ANALYSIS_FAIL = 
        new Signature(JBSE_ANALYSIS, "()" + VOID, "fail");
    public static final Signature JBSE_ANALYSIS_IGNORE = 
        new Signature(JBSE_ANALYSIS, "()" + VOID, "ignore");
    public static final Signature JBSE_ANALYSIS_ISRESOLVED = 
        new Signature(JBSE_ANALYSIS, "(" + REFERENCE + JAVA_OBJECT + TYPEEND + REFERENCE + JAVA_STRING + TYPEEND + ")" + BOOLEAN, "isResolved");
    public static final Signature JBSE_ANALYSIS_ISRESOLVEDBYALIAS = 
        new Signature(JBSE_ANALYSIS, "(" + REFERENCE + JAVA_OBJECT + TYPEEND + ")" + BOOLEAN, "isResolvedByAlias");
    public static final Signature JBSE_ANALYSIS_ISRESOLVEDBYEXPANSION = 
        new Signature(JBSE_ANALYSIS, "(" + REFERENCE + JAVA_OBJECT + TYPEEND + ")" + BOOLEAN, "isResolvedByExpansion");
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
    public static final Signature JBSE_ANALYSIS_ISSYMBOLIC_OBJECT = 
        new Signature(JBSE_ANALYSIS, "(" + REFERENCE + JAVA_OBJECT + TYPEEND + ")" + BOOLEAN, "isSymbolic");
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
    public static final Signature JBSE_ANALYSIS_SYMBOLNAME_OBJECT = 
        new Signature(JBSE_ANALYSIS, "(" + REFERENCE + JAVA_OBJECT + TYPEEND + ")" + REFERENCE + JAVA_STRING + TYPEEND, "symbolName");
    public static final Signature JBSE_ANALYSIS_SYMBOLNAME_SHORT = 
        new Signature(JBSE_ANALYSIS, "(" + SHORT + ")" + REFERENCE + JAVA_STRING + TYPEEND, "symbolName");
    public static final Signature JBSE_BASE_BOXEXCEPTIONININITIALIZERERROR = 
        new Signature(JBSE_BASE, "()" + VOID, "boxExceptionInInitializerError");
    public static final Signature JBSE_BASE_BOXINVOCATIONTARGETEXCEPTIONANDRETURN_B = 
        new Signature(JBSE_BASE, "()" + REFERENCE + JAVA_OBJECT + TYPEEND, "boxInvocationTargetExceptionAndReturn_B");
    public static final Signature JBSE_BASE_BOXINVOCATIONTARGETEXCEPTIONANDRETURN_C = 
        new Signature(JBSE_BASE, "()" + REFERENCE + JAVA_OBJECT + TYPEEND, "boxInvocationTargetExceptionAndReturn_C");
    public static final Signature JBSE_BASE_BOXINVOCATIONTARGETEXCEPTIONANDRETURN_D = 
        new Signature(JBSE_BASE, "()" + REFERENCE + JAVA_OBJECT + TYPEEND, "boxInvocationTargetExceptionAndReturn_D");
    public static final Signature JBSE_BASE_BOXINVOCATIONTARGETEXCEPTIONANDRETURN_F = 
        new Signature(JBSE_BASE, "()" + REFERENCE + JAVA_OBJECT + TYPEEND, "boxInvocationTargetExceptionAndReturn_F");
    public static final Signature JBSE_BASE_BOXINVOCATIONTARGETEXCEPTIONANDRETURN_I = 
        new Signature(JBSE_BASE, "()" + REFERENCE + JAVA_OBJECT + TYPEEND, "boxInvocationTargetExceptionAndReturn_I");
    public static final Signature JBSE_BASE_BOXINVOCATIONTARGETEXCEPTIONANDRETURN_J = 
        new Signature(JBSE_BASE, "()" + REFERENCE + JAVA_OBJECT + TYPEEND, "boxInvocationTargetExceptionAndReturn_J");
    public static final Signature JBSE_BASE_BOXINVOCATIONTARGETEXCEPTIONANDRETURN_L = 
        new Signature(JBSE_BASE, "()" + REFERENCE + JAVA_OBJECT + TYPEEND, "boxInvocationTargetExceptionAndReturn_L");
    public static final Signature JBSE_BASE_BOXINVOCATIONTARGETEXCEPTIONANDRETURN_NULL = 
        new Signature(JBSE_BASE, "()" + REFERENCE + JAVA_OBJECT + TYPEEND, "boxInvocationTargetExceptionAndReturn_null");
    public static final Signature JBSE_BASE_BOXINVOCATIONTARGETEXCEPTIONANDRETURN_S = 
        new Signature(JBSE_BASE, "()" + REFERENCE + JAVA_OBJECT + TYPEEND, "boxInvocationTargetExceptionAndReturn_S");
    public static final Signature JBSE_BASE_BOXINVOCATIONTARGETEXCEPTIONANDRETURN_V = 
        new Signature(JBSE_BASE, "()" + VOID, "boxInvocationTargetExceptionAndReturn_V");
    public static final Signature JBSE_BASE_BOXINVOCATIONTARGETEXCEPTIONANDRETURN_Z = 
        new Signature(JBSE_BASE, "()" + REFERENCE + JAVA_OBJECT + TYPEEND, "boxInvocationTargetExceptionAndReturn_Z");
    public static final Signature JBSE_BASE_MAKEKLASSSYMBOLIC = 
        new Signature(JBSE_BASE, "(" + INT + REFERENCE + JAVA_STRING + TYPEEND +")" + VOID, "makeKlassSymbolic");
    public static final Signature JBSE_BASE_MAKEKLASSSYMBOLIC_DO = 
        new Signature(JBSE_BASE, "(" + INT + REFERENCE + JAVA_STRING + TYPEEND +")" + VOID, "makeKlassSymbolic_do");
    public static final Signature JBSE_JAVA_CONCURRENTMAP_INITSYMBOLIC = 
		new Signature(JAVA_CONCURRENTHASHMAP, "(" + REFERENCE + JAVA_CONCURRENTHASHMAP + TYPEEND + ")" + VOID, "initSymbolic");
    public static final Signature JBSE_JAVA_CONCURRENTMAP_MAKEINITIAL = 
        new Signature(JAVA_CONCURRENTHASHMAP, "()" + VOID, "makeInitial");
    public static final Signature JBSE_JAVA_CONCURRENTMAP_METATHROWUNEXPECTEDINTERNALEXCEPTION = 
        new Signature(JAVA_CONCURRENTHASHMAP, "(" + REFERENCE + JAVA_STRING + TYPEEND + ")" + VOID, "metaThrowUnexpectedInternalException");
    public static final Signature JBSE_JAVA_CONCURRENTMAP_NOTIFYMETHODEXECUTION = 
        new Signature(JAVA_CONCURRENTHASHMAP, "()" + VOID, "notifyMethodExecution");
    public static final Signature JBSE_JAVA_CONCURRENTMAP_ONKEYRESOLUTION = 
		new Signature(JAVA_CONCURRENTHASHMAP, "(" + REFERENCE + JAVA_OBJECT + TYPEEND + ")" + VOID, "onKeyResolution");
    public static final Signature JBSE_JAVA_CONCURRENTMAP_ONKEYRESOLUTION0 = 
    	new Signature(JAVA_CONCURRENTHASHMAP, "(" + REFERENCE + JAVA_OBJECT + TYPEEND + ")" + VOID, "onKeyResolution0");
    public static final Signature JBSE_JAVA_CONCURRENTMAP_ONKEYRESOLUTIONCOMPLETE = 
    	new Signature(JAVA_CONCURRENTHASHMAP, "(" + REFERENCE + JAVA_CONCURRENTHASHMAP + TYPEEND + REFERENCE + JAVA_OBJECT + TYPEEND + ")" + VOID, "onKeyResolutionComplete");
    public static final Signature JBSE_JAVA_CONCURRENTMAP_REFINEIN = 
        new Signature(JAVA_CONCURRENTHASHMAP, "(" + REFERENCE + JAVA_OBJECT + TYPEEND + REFERENCE + JAVA_OBJECT + TYPEEND + ")" + VOID, "refineIn");
    public static final Signature JBSE_JAVA_CONCURRENTMAP_REFINEMAPCOMPLETE = 
		new Signature(JAVA_CONCURRENTHASHMAP, "()" + VOID, "refineMapComplete");
    public static final Signature JBSE_JAVA_CONCURRENTMAP_REFINEONFRESHENTRYANDBRANCH = 
		new Signature(JAVA_CONCURRENTHASHMAP, "()" + VOID, "refineOnFreshEntryAndBranch");
    public static final Signature JBSE_JAVA_CONCURRENTMAP_REFINEONKEYANDBRANCH = 
        new Signature(JAVA_CONCURRENTHASHMAP, "(" + REFERENCE + JAVA_OBJECT + TYPEEND + ")" + VOID, "refineOnKeyAndBranch");
    public static final Signature JBSE_JAVA_CONCURRENTMAP_REFINEONKEYCOMBINATIONSANDBRANCH = 
        new Signature(JAVA_CONCURRENTHASHMAP, "(" + ARRAYOF + REFERENCE + JAVA_OBJECT + TYPEEND + ")" + VOID, "refineOnKeyCombinationsAndBranch");
    public static final Signature JBSE_JAVA_CONCURRENTMAP_REFINEONVALUEANDBRANCH = 
		new Signature(JAVA_CONCURRENTHASHMAP, "(" + REFERENCE + JAVA_OBJECT + TYPEEND + ")" + VOID, "refineOnValueAndBranch");
    public static final Signature JBSE_JAVA_CONCURRENTMAP_REFINEOUTKEY = 
        new Signature(JAVA_CONCURRENTHASHMAP, "(" + REFERENCE + JAVA_OBJECT + TYPEEND + ")" + VOID, "refineOutKey");
    public static final Signature JBSE_JAVA_CONCURRENTMAP_REFINEOUTVALUE = 
		new Signature(JAVA_CONCURRENTHASHMAP, "(" + REFERENCE + JAVA_OBJECT + TYPEEND + ")" + VOID, "refineOutValue");
    public static final Signature JBSE_JAVA_LINKEDMAP_INITSYMBOLIC = 
		new Signature(JAVA_LINKEDHASHMAP, "(" + REFERENCE + JAVA_LINKEDHASHMAP + TYPEEND + ")" + VOID, "initSymbolic");
    public static final Signature JBSE_JAVA_LINKEDMAP_MAKEINITIAL = 
    	new Signature(JAVA_LINKEDHASHMAP, "()" + VOID, "makeInitial");
    public static final Signature JBSE_JAVA_LINKEDMAP_METATHROWUNEXPECTEDINTERNALEXCEPTION = 
    	new Signature(JAVA_LINKEDHASHMAP, "(" + REFERENCE + JAVA_STRING + TYPEEND + ")" + VOID, "metaThrowUnexpectedInternalException");
    public static final Signature JBSE_JAVA_LINKEDMAP_NOTIFYMETHODEXECUTION = 
    	new Signature(JAVA_LINKEDHASHMAP, "()" + VOID, "notifyMethodExecution");
    public static final Signature JBSE_JAVA_LINKEDMAP_ONKEYRESOLUTION = 
		new Signature(JAVA_LINKEDHASHMAP, "(" + REFERENCE + JAVA_OBJECT + TYPEEND + ")" + VOID, "onKeyResolution");
    public static final Signature JBSE_JAVA_LINKEDMAP_ONKEYRESOLUTION0 = 
		new Signature(JAVA_LINKEDHASHMAP, "(" + REFERENCE + JAVA_OBJECT + TYPEEND + ")" + VOID, "onKeyResolution0");
    public static final Signature JBSE_JAVA_LINKEDMAP_ONKEYRESOLUTIONCOMPLETE = 
		new Signature(JAVA_LINKEDHASHMAP, "(" + REFERENCE + JAVA_LINKEDHASHMAP + TYPEEND + REFERENCE + JAVA_OBJECT + TYPEEND + ")" + VOID, "onKeyResolutionComplete");
    public static final Signature JBSE_JAVA_LINKEDMAP_REFINEIN = 
    	new Signature(JAVA_LINKEDHASHMAP, "(" + REFERENCE + JAVA_OBJECT + TYPEEND + REFERENCE + JAVA_OBJECT + TYPEEND + ")" + VOID, "refineIn");
    public static final Signature JBSE_JAVA_LINKEDMAP_REFINEMAPCOMPLETE = 
    	new Signature(JAVA_LINKEDHASHMAP, "()" + VOID, "refineMapComplete");
    public static final Signature JBSE_JAVA_LINKEDMAP_REFINEONFRESHENTRYANDBRANCH = 
    	new Signature(JAVA_LINKEDHASHMAP, "()" + VOID, "refineOnFreshEntryAndBranch");
    public static final Signature JBSE_JAVA_LINKEDMAP_REFINEONKEYANDBRANCH = 
    	new Signature(JAVA_LINKEDHASHMAP, "(" + REFERENCE + JAVA_OBJECT + TYPEEND + ")" + VOID, "refineOnKeyAndBranch");
    public static final Signature JBSE_JAVA_LINKEDMAP_REFINEONKEYCOMBINATIONSANDBRANCH = 
    	new Signature(JAVA_LINKEDHASHMAP, "(" + ARRAYOF + REFERENCE + JAVA_OBJECT + TYPEEND + ")" + VOID, "refineOnKeyCombinationsAndBranch");
    public static final Signature JBSE_JAVA_LINKEDMAP_REFINEONVALUEANDBRANCH = 
    	new Signature(JAVA_LINKEDHASHMAP, "(" + REFERENCE + JAVA_OBJECT + TYPEEND + ")" + VOID, "refineOnValueAndBranch");
    public static final Signature JBSE_JAVA_LINKEDMAP_REFINEOUTKEY = 
    	new Signature(JAVA_LINKEDHASHMAP, "(" + REFERENCE + JAVA_OBJECT + TYPEEND + ")" + VOID, "refineOutKey");
    public static final Signature JBSE_JAVA_LINKEDMAP_REFINEOUTVALUE = 
    	new Signature(JAVA_LINKEDHASHMAP, "(" + REFERENCE + JAVA_OBJECT + TYPEEND + ")" + VOID, "refineOutValue");
    public static final Signature JBSE_JAVA_MAP_INITSYMBOLIC = 
    	new Signature(JAVA_HASHMAP, "(" + REFERENCE + JAVA_HASHMAP + TYPEEND + ")" + VOID, "initSymbolic");
    public static final Signature JBSE_JAVA_MAP_MAKEINITIAL = 
    	new Signature(JAVA_HASHMAP, "()" + VOID, "makeInitial");
    public static final Signature JBSE_JAVA_MAP_METATHROWUNEXPECTEDINTERNALEXCEPTION = 
    	new Signature(JAVA_HASHMAP, "(" + REFERENCE + JAVA_STRING + TYPEEND + ")" + VOID, "metaThrowUnexpectedInternalException");
    public static final Signature JBSE_JAVA_MAP_NOTIFYMETHODEXECUTION = 
		new Signature(JAVA_HASHMAP, "()" + VOID, "notifyMethodExecution");
    public static final Signature JBSE_JAVA_MAP_ONKEYRESOLUTION = 
		new Signature(JAVA_HASHMAP, "(" + REFERENCE + JAVA_OBJECT + TYPEEND + ")" + VOID, "onKeyResolution");
    public static final Signature JBSE_JAVA_MAP_ONKEYRESOLUTION0 = 
    	new Signature(JAVA_HASHMAP, "(" + REFERENCE + JAVA_OBJECT + TYPEEND + ")" + VOID, "onKeyResolution0");
    public static final Signature JBSE_JAVA_MAP_ONKEYRESOLUTIONCOMPLETE = 
		new Signature(JAVA_HASHMAP, "(" + REFERENCE + JAVA_HASHMAP + TYPEEND + REFERENCE + JAVA_OBJECT + TYPEEND + ")" + VOID, "onKeyResolutionComplete");
    public static final Signature JBSE_JAVA_MAP_REFINEIN = 
    	new Signature(JAVA_HASHMAP, "(" + REFERENCE + JAVA_OBJECT + TYPEEND + REFERENCE + JAVA_OBJECT + TYPEEND + ")" + VOID, "refineIn");
    public static final Signature JBSE_JAVA_MAP_REFINEMAPCOMPLETE = 
		new Signature(JAVA_HASHMAP, "()" + VOID, "refineMapComplete");
    public static final Signature JBSE_JAVA_MAP_REFINEONFRESHENTRYANDBRANCH = 
    	new Signature(JAVA_HASHMAP, "()" + VOID, "refineOnFreshEntryAndBranch");
    public static final Signature JBSE_JAVA_MAP_REFINEONKEYANDBRANCH = 
        new Signature(JAVA_HASHMAP, "(" + REFERENCE + JAVA_OBJECT + TYPEEND + ")" + VOID, "refineOnKeyAndBranch");
    public static final Signature JBSE_JAVA_MAP_REFINEONKEYCOMBINATIONSANDBRANCH = 
    	new Signature(JAVA_HASHMAP, "(" + ARRAYOF + REFERENCE + JAVA_OBJECT + TYPEEND + ")" + VOID, "refineOnKeyCombinationsAndBranch");
    public static final Signature JBSE_JAVA_MAP_REFINEONVALUEANDBRANCH = 
    	new Signature(JAVA_HASHMAP, "(" + REFERENCE + JAVA_OBJECT + TYPEEND + ")" + VOID, "refineOnValueAndBranch");
    public static final Signature JBSE_JAVA_MAP_REFINEOUTKEY = 
    	new Signature(JAVA_HASHMAP, "(" + REFERENCE + JAVA_OBJECT + TYPEEND + ")" + VOID, "refineOutKey");
    public static final Signature JBSE_JAVA_MAP_REFINEOUTVALUE = 
		new Signature(JAVA_HASHMAP, "(" + REFERENCE + JAVA_OBJECT + TYPEEND + ")" + VOID, "refineOutValue");
    public static final Signature SUN_CONSTANTPOOL_GETUTF8AT0 = 
        new Signature(SUN_CONSTANTPOOL, 
                      "(" + REFERENCE + JAVA_OBJECT + TYPEEND + INT + ")" + REFERENCE + JAVA_STRING + TYPEEND, 
                      "getUTF8At0");
    public static final Signature SUN_NATIVECONSTRUCTORACCESSORIMPL_NEWINSTANCE0 = 
        new Signature(SUN_NATIVECONSTRUCTORACCESSORIMPL, 
                      "(" + REFERENCE + JAVA_CONSTRUCTOR + TYPEEND + ARRAYOF + REFERENCE + JAVA_OBJECT + TYPEEND + ")" + REFERENCE + JAVA_OBJECT + TYPEEND, 
                      "newInstance0");
    public static final Signature SUN_NATIVEMETHODACCESSORIMPL_INVOKE0 = 
        new Signature(SUN_NATIVEMETHODACCESSORIMPL, 
                      "(" + REFERENCE + JAVA_METHOD + TYPEEND + REFERENCE + JAVA_OBJECT + TYPEEND + ARRAYOF + REFERENCE + JAVA_OBJECT + TYPEEND + ")" + REFERENCE + JAVA_OBJECT + TYPEEND, 
                      "invoke0");
    public static final Signature SUN_PERF_CREATELONG =
        new Signature(SUN_PERF, "(" + REFERENCE + JAVA_STRING + TYPEEND + INT + INT + LONG + ")" + REFERENCE + JAVA_BYTEBUFFER + TYPEEND, "createLong");
    public static final Signature SUN_PERF_REGISTERNATIVES =
        new Signature(SUN_PERF, "()" + VOID, "registerNatives");
    public static final Signature SUN_REFLECTION_GETCALLERCLASS = 
        new Signature(SUN_REFLECTION, "()" + REFERENCE + JAVA_CLASS + TYPEEND, "getCallerClass");
    public static final Signature SUN_REFLECTION_GETCLASSACCESSFLAGS = 
        new Signature(SUN_REFLECTION, "(" + REFERENCE + JAVA_CLASS + TYPEEND + ")" + INT, "getClassAccessFlags");
    public static final Signature SUN_SIGNAL_FINDSIGNAL = 
        new Signature(SUN_SIGNAL, "(" + REFERENCE + JAVA_STRING + TYPEEND + ")" + INT, "findSignal");
    public static final Signature SUN_SIGNAL_HANDLE0 = 
        new Signature(SUN_SIGNAL, "(" + INT + LONG + ")" + LONG, "handle0");
    public static final Signature SUN_UNIXNATIVEDISPATCHER_GETCWD = 
        new Signature(SUN_UNIXNATIVEDISPATCHER, "()" + ARRAYOF + BYTE, "getcwd");
    public static final Signature SUN_UNIXNATIVEDISPATCHER_INIT = 
        new Signature(SUN_UNIXNATIVEDISPATCHER, "()" + INT, "init");
    public static final Signature SUN_UNSAFE_ADDRESSSIZE = 
        new Signature(SUN_UNSAFE, "()" + INT, "addressSize");
    public static final Signature SUN_UNSAFE_ALLOCATEINSTANCE = 
        new Signature(SUN_UNSAFE, "(" + REFERENCE + JAVA_CLASS + TYPEEND + ")" + REFERENCE + JAVA_OBJECT + TYPEEND, "allocateInstance");
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
    public static final Signature SUN_UNSAFE_DEFINECLASS = 
        new Signature(SUN_UNSAFE, 
                      "(" + REFERENCE + JAVA_STRING + TYPEEND + ARRAYOF + BYTE + INT + INT + REFERENCE + JAVA_CLASSLOADER + TYPEEND + REFERENCE + JAVA_PROTECTIONDOMAIN + TYPEEND + ")" + REFERENCE + JAVA_CLASS + TYPEEND, 
                      "defineClass");
    public static final Signature SUN_UNSAFE_FREEMEMORY = 
        new Signature(SUN_UNSAFE, "(" + LONG + ")" + VOID, "freeMemory");
    public static final Signature SUN_UNSAFE_ENSURECLASSINITIALIZED = 
        new Signature(SUN_UNSAFE, "(" + REFERENCE + JAVA_CLASS + TYPEEND + ")" + VOID, "ensureClassInitialized");
    public static final Signature SUN_UNSAFE_FULLFENCE = 
        new Signature(SUN_UNSAFE, "()" + VOID, "fullFence");
    public static final Signature SUN_UNSAFE_GETBYTE = 
        new Signature(SUN_UNSAFE, "(" + LONG + ")" + BYTE, "getByte");
    public static final Signature SUN_UNSAFE_GETINT = 
    	new Signature(SUN_UNSAFE, "(" + LONG + ")" + INT, "getInt");
    public static final Signature SUN_UNSAFE_GETINT_O = 
        new Signature(SUN_UNSAFE, "(" + REFERENCE + JAVA_OBJECT + TYPEEND + LONG + ")" + INT, "getInt");
    public static final Signature SUN_UNSAFE_GETINTVOLATILE = 
        new Signature(SUN_UNSAFE, "(" + REFERENCE + JAVA_OBJECT + TYPEEND + LONG + ")" + INT, "getIntVolatile");
    public static final Signature SUN_UNSAFE_GETLONG = 
        new Signature(SUN_UNSAFE, "(" + LONG + ")" + LONG, "getLong");
    public static final Signature SUN_UNSAFE_GETLONG_O =
        new Signature(SUN_UNSAFE, "(" + REFERENCE + JAVA_OBJECT + TYPEEND + LONG + ")" + LONG, "getLong");
    public static final Signature SUN_UNSAFE_GETLONGVOLATILE =
    	new Signature(SUN_UNSAFE, "(" + REFERENCE + JAVA_OBJECT + TYPEEND + LONG + ")" + LONG, "getLongVolatile");
    public static final Signature SUN_UNSAFE_GETOBJECT = 
        new Signature(SUN_UNSAFE, "(" + REFERENCE + JAVA_OBJECT + TYPEEND + LONG + ")" + REFERENCE + JAVA_OBJECT + TYPEEND, "getObject");
    public static final Signature SUN_UNSAFE_PUTINT = 
        new Signature(SUN_UNSAFE, "(" + LONG + INT + ")" + VOID, "putInt");
    public static final Signature SUN_UNSAFE_GETOBJECTVOLATILE = 
        new Signature(SUN_UNSAFE, "(" + REFERENCE + JAVA_OBJECT + TYPEEND + LONG + ")" + REFERENCE + JAVA_OBJECT + TYPEEND, "getObjectVolatile");
    public static final Signature SUN_UNSAFE_OBJECTFIELDOFFSET = 
        new Signature(SUN_UNSAFE, "(" + REFERENCE + JAVA_FIELD + TYPEEND + ")" + LONG, "objectFieldOffset");
    public static final Signature SUN_UNSAFE_PAGESIZE = 
        new Signature(SUN_UNSAFE, "()" + INT, "pageSize");
    public static final Signature SUN_UNSAFE_PARK = 
        new Signature(SUN_UNSAFE, "(" + BOOLEAN + LONG + ")" + VOID, "park");
    public static final Signature SUN_UNSAFE_PUTINT_O = 
        new Signature(SUN_UNSAFE, "(" + REFERENCE + JAVA_OBJECT + TYPEEND + LONG + INT + ")" + VOID, "putInt");
    public static final Signature SUN_UNSAFE_PUTINTVOLATILE = 
        new Signature(SUN_UNSAFE, "(" + REFERENCE + JAVA_OBJECT + TYPEEND + LONG + INT + ")" + VOID, "putIntVolatile");
    public static final Signature SUN_UNSAFE_PUTLONG = 
        new Signature(SUN_UNSAFE, "(" + LONG + LONG + ")" + VOID, "putLong");
    public static final Signature SUN_UNSAFE_PUTLONG_O = 
    	new Signature(SUN_UNSAFE, "(" + REFERENCE + JAVA_OBJECT + TYPEEND + LONG + LONG + ")" + VOID, "putLong");
    public static final Signature SUN_UNSAFE_PUTLONGVOLATILE = 
    	new Signature(SUN_UNSAFE, "(" + REFERENCE + JAVA_OBJECT + TYPEEND + LONG + LONG + ")" + VOID, "putLongVolatile");
    public static final Signature SUN_UNSAFE_PUTOBJECT = 
        new Signature(SUN_UNSAFE, "(" + REFERENCE + JAVA_OBJECT + TYPEEND + LONG + REFERENCE + JAVA_OBJECT + TYPEEND + ")" + VOID, "putObject");
    public static final Signature SUN_UNSAFE_PUTOBJECTVOLATILE = 
        new Signature(SUN_UNSAFE, "(" + REFERENCE + JAVA_OBJECT + TYPEEND + LONG + REFERENCE + JAVA_OBJECT + TYPEEND + ")" + VOID, "putObjectVolatile");
    public static final Signature SUN_UNSAFE_PUTORDEREDINT = 
        new Signature(SUN_UNSAFE, "(" + REFERENCE + JAVA_OBJECT + TYPEEND + LONG + INT + ")" + VOID, "putOrderedInt");
    public static final Signature SUN_UNSAFE_PUTORDEREDOBJECT = 
        new Signature(SUN_UNSAFE, "(" + REFERENCE + JAVA_OBJECT + TYPEEND + LONG + REFERENCE + JAVA_OBJECT + TYPEEND + ")" + VOID, "putOrderedObject");
    public static final Signature SUN_UNSAFE_REGISTERNATIVES =
        new Signature(SUN_UNSAFE, "()" + VOID, "registerNatives");
    public static final Signature SUN_UNSAFE_SHOULDBEINITIALIZED =
        new Signature(SUN_UNSAFE, "(" + REFERENCE + JAVA_CLASS + TYPEEND + ")" + BOOLEAN, "shouldBeInitialized");
    public static final Signature SUN_UNSAFE_STATICFIELDBASE = 
        new Signature(SUN_UNSAFE, "(" + REFERENCE + JAVA_FIELD + TYPEEND + ")" + REFERENCE + JAVA_OBJECT + TYPEEND, "staticFieldBase");
    public static final Signature SUN_UNSAFE_STATICFIELDOFFSET = 
        new Signature(SUN_UNSAFE, "(" + REFERENCE + JAVA_FIELD + TYPEEND + ")" + LONG, "staticFieldOffset");
    public static final Signature SUN_UNSAFE_UNPARK = 
        new Signature(SUN_UNSAFE, "(" + REFERENCE + JAVA_OBJECT + TYPEEND + ")" + VOID, "unpark");
    public static final Signature SUN_URLCLASSPATH_GETLOOKUPCACHEURLS =
        new Signature(SUN_URLCLASSPATH, "(" + REFERENCE + JAVA_CLASSLOADER + TYPEEND + ")" + ARRAYOF + REFERENCE + JAVA_URL + TYPEEND, "getLookupCacheURLs");
    public static final Signature SUN_VM_INITIALIZE = 
        new Signature(SUN_VM, "()" + VOID, "initialize");
    public static final Signature SUN_WIN32ERRORMODE_SETERRORMODE = 
        new Signature(SUN_WIN32ERRORMODE, "(" + LONG + ")" + LONG, "setErrorMode");
    public static final Signature noclass_REGISTERLOADEDCLASS =
        new Signature(null, "(" + INT + REFERENCE + JAVA_CLASSLOADER + TYPEEND + ")" + VOID, "registerLoadedClass");
    public static final Signature noclass_REGISTERMETHODHANDLE =
        new Signature(null, "(" + REFERENCE + JAVA_METHODHANDLE + TYPEEND + INT + REFERENCE + JAVA_CLASS + TYPEEND + 
            REFERENCE + JAVA_STRING + TYPEEND + REFERENCE + JAVA_OBJECT + TYPEEND + ")" + VOID, "registerMethodHandle");
    public static final Signature noclass_REGISTERMETHODTYPE =
        new Signature(null, "(" + REFERENCE + JAVA_METHODTYPE + TYPEEND + REFERENCE + JAVA_OBJECT + TYPEEND + ")" + VOID, "registerMethodType");
    public static final Signature noclass_SETSTANDARDCLASSLOADERSREADY =
        new Signature(null, "()" + VOID, "setStandardClassLoadersReady");
    public static final Signature noclass_STORELINKEDCALLSITEADAPTERANDAPPENDIX =
        new Signature(null, "(" + REFERENCE + JAVA_MEMBERNAME + TYPEEND + ARRAYOF + REFERENCE + JAVA_OBJECT + TYPEEND + REFERENCE + JAVA_CLASS + TYPEEND + 
        	REFERENCE + JAVA_OBJECT + TYPEEND + REFERENCE + JAVA_OBJECT + TYPEEND + REFERENCE + JAVA_OBJECT + TYPEEND + ")" + VOID, "storeLinkedCallSiteAdapterAndAppendix");
    public static final Signature noclass_STORELINKEDMETHODADAPTERANDAPPENDIX =
        new Signature(null, "(" + REFERENCE + JAVA_STRING + TYPEEND + REFERENCE + JAVA_METHODTYPE + TYPEEND + ARRAYOF + REFERENCE + JAVA_OBJECT + TYPEEND + REFERENCE + JAVA_MEMBERNAME + TYPEEND + ")" + VOID, "storeLinkedMethodAdapterAndAppendix");
    
    //field names
    public static final String ASSERTIONDISABLED_NAME = "$assertionsDisabled";
    
    //fields
    public static final Signature JAVA_ACCESSIBLEOBJECT_OVERRIDE = 
        new Signature(JAVA_ACCESSIBLEOBJECT, "" + BOOLEAN, "override");
    public static final Signature JAVA_BOOLEAN_FALSE = 
        new Signature(JAVA_BOOLEAN, "" + REFERENCE + JAVA_BOOLEAN + TYPEEND, "FALSE");
    public static final Signature JAVA_BOOLEAN_TRUE = 
        new Signature(JAVA_BOOLEAN, "" + REFERENCE + JAVA_BOOLEAN + TYPEEND, "TRUE");
    public static final Signature JAVA_BOOLEAN_VALUE = 
        new Signature(JAVA_BOOLEAN, "" + BOOLEAN, "value");
    public static final Signature JAVA_BYTE_BYTECACHE_CACHE = 
            new Signature(JAVA_BYTE_BYTECACHE, "" + ARRAYOF + REFERENCE + JAVA_BYTE + TYPEEND, "cache");
    public static final Signature JAVA_BYTE_VALUE = 
        new Signature(JAVA_BYTE, "" + BYTE, "value");
    public static final Signature JAVA_CALLSITE_TARGET = 
        new Signature(JAVA_CALLSITE, "" + REFERENCE + JAVA_METHODHANDLE + TYPEEND, "target");
    public static final Signature JAVA_CHARACTER_CHARACTERCACHE_CACHE = 
        new Signature(JAVA_CHARACTER_CHARACTERCACHE, "" + ARRAYOF + REFERENCE + JAVA_CHARACTER + TYPEEND, "cache");
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
    public static final Signature JAVA_FILEDESCRIPTOR_FD = 
        new Signature(JAVA_FILEDESCRIPTOR, "" + INT, "fd");
    public static final Signature JAVA_FILEDESCRIPTOR_HANDLE = 
            new Signature(JAVA_FILEDESCRIPTOR, "" + LONG, "handle");
    public static final Signature JAVA_FILEINPUTSTREAM_FD = 
        new Signature(JAVA_FILEINPUTSTREAM, "" + REFERENCE + JAVA_FILEDESCRIPTOR + TYPEEND, "fd");
    public static final Signature JAVA_FILEOUTPUTSTREAM_FD = 
        new Signature(JAVA_FILEOUTPUTSTREAM, "" + REFERENCE + JAVA_FILEDESCRIPTOR + TYPEEND, "fd");
    public static final Signature JAVA_FLOAT_VALUE = 
        new Signature(JAVA_FLOAT, "" + FLOAT, "value");
    public static final Signature JAVA_INFLATER_BUF = 
        new Signature(JAVA_INFLATER, "" + ARRAYOF + BYTE, "buf");
    public static final Signature JAVA_INFLATER_BYTESREAD = 
        new Signature(JAVA_INFLATER, "" + LONG, "bytesRead");
    public static final Signature JAVA_INFLATER_BYTESWRITTEN = 
        new Signature(JAVA_INFLATER, "" + LONG, "bytesWritten");
    public static final Signature JAVA_INFLATER_FINISHED = 
        new Signature(JAVA_INFLATER, "" + BOOLEAN, "finished");
    public static final Signature JAVA_INFLATER_LEN = 
        new Signature(JAVA_INFLATER, "" + INT, "len");
    public static final Signature JAVA_INFLATER_NEEDDICT = 
        new Signature(JAVA_INFLATER, "" + BOOLEAN, "needDict");
    public static final Signature JAVA_INFLATER_OFF = 
        new Signature(JAVA_INFLATER, "" + INT, "off");
    public static final Signature JAVA_INFLATER_ZSREF = 
        new Signature(JAVA_INFLATER, "" + REFERENCE + JAVA_ZSTREAMREF + TYPEEND, "zsRef");
    public static final Signature JAVA_INTEGER_INTEGERCACHE_CACHE = 
        new Signature(JAVA_INTEGER_INTEGERCACHE, "" + ARRAYOF + REFERENCE + JAVA_INTEGER + TYPEEND, "cache");
    public static final Signature JAVA_INTEGER_INTEGERCACHE_HIGH = 
        new Signature(JAVA_INTEGER_INTEGERCACHE, "" + INT, "high");
    public static final Signature JAVA_INTEGER_INTEGERCACHE_LOW = 
        new Signature(JAVA_INTEGER_INTEGERCACHE, "" + INT, "low");
    public static final Signature JAVA_INTEGER_VALUE = 
        new Signature(JAVA_INTEGER, "" + INT, "value");
    public static final Signature JAVA_LAMBDAFORM_VMENTRY = 
    	new Signature(JAVA_LAMBDAFORM, "" + REFERENCE + JAVA_MEMBERNAME + TYPEEND, "vmentry");
    public static final Signature JAVA_LONG_LONGCACHE_CACHE = 
        new Signature(JAVA_LONG_LONGCACHE, "" + ARRAYOF + REFERENCE + JAVA_LONG + TYPEEND, "cache");
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
    public static final Signature JAVA_METHOD_ANNOTATIONS = 
        new Signature(JAVA_METHOD, "" + ARRAYOF + BYTE, "annotations");
    public static final Signature JAVA_METHOD_CLAZZ = 
        new Signature(JAVA_METHOD, "" + REFERENCE + JAVA_CLASS + TYPEEND, "clazz");
    public static final Signature JAVA_METHOD_EXCEPTIONTYPES = 
        new Signature(JAVA_METHOD, "" + ARRAYOF + REFERENCE + JAVA_CLASS + TYPEEND, "exceptionTypes");
    public static final Signature JAVA_METHOD_MODIFIERS = 
        new Signature(JAVA_METHOD, "" + INT, "modifiers");
    public static final Signature JAVA_METHOD_NAME = 
        new Signature(JAVA_METHOD, "" + REFERENCE + JAVA_STRING + TYPEEND, "name");
    public static final Signature JAVA_METHOD_PARAMETERTYPES = 
        new Signature(JAVA_METHOD, "" + ARRAYOF + REFERENCE + JAVA_CLASS + TYPEEND, "parameterTypes");
    public static final Signature JAVA_METHOD_RETURNTYPE = 
        new Signature(JAVA_METHOD, "" + REFERENCE + JAVA_CLASS + TYPEEND, "returnType");
    public static final Signature JAVA_METHOD_SIGNATURE = 
        new Signature(JAVA_METHOD, "" + REFERENCE + JAVA_STRING + TYPEEND, "signature");
    public static final Signature JAVA_METHOD_SLOT = 
        new Signature(JAVA_METHOD, "" + INT, "slot");
    public static final Signature JAVA_METHODHANDLE_FORM = 
        new Signature(JAVA_METHODHANDLE, "" + REFERENCE + JAVA_LAMBDAFORM + TYPEEND, "form");
    public static final Signature JAVA_METHODTYPE_METHODDESCRIPTOR = 
        new Signature(JAVA_METHODTYPE, "" + REFERENCE + JAVA_STRING + TYPEEND, "methodDescriptor");
    public static final Signature JAVA_PARAMETER_EXECUTABLE =
        new Signature(JAVA_PARAMETER, "" + REFERENCE + JAVA_EXECUTABLE + TYPEEND, "executable");
    public static final Signature JAVA_PARAMETER_INDEX =
        new Signature(JAVA_PARAMETER, "" + INT, "index");
    public static final Signature JAVA_PARAMETER_MODIFIERS =
        new Signature(JAVA_PARAMETER, "" + INT, "modifiers");
    public static final Signature JAVA_PARAMETER_NAME =
        new Signature(JAVA_PARAMETER, "" + REFERENCE + JAVA_STRING + TYPEEND, "name");
    public static final Signature JAVA_SHORT_SHORTCACHE_CACHE = 
        new Signature(JAVA_SHORT_SHORTCACHE, "" + ARRAYOF + REFERENCE + JAVA_SHORT + TYPEEND, "cache");
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
    public static final Signature JAVA_ZIPFILE_JZFILE = 
        new Signature(JAVA_ZIPFILE, "" + LONG, "jzfile");
    public static final Signature JAVA_ZSTREAMREF_ADDRESS = 
        new Signature(JAVA_ZSTREAMREF, "" + LONG, "address");
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
    public static final Signature JBSE_BASE_JAVA_AWT_GRAPHICSENV = 
        new Signature(JBSE_BASE, "" + REFERENCE + JAVA_STRING + TYPEEND, "JAVA_AWT_GRAPHICSENV");
    public static final Signature JBSE_BASE_JAVA_AWT_PRINTERJOB = 
        new Signature(JBSE_BASE, "" + REFERENCE + JAVA_STRING + TYPEEND, "JAVA_AWT_PRINTERJOB");
    public static final Signature JBSE_BASE_JAVA_CLASS_PATH = 
        new Signature(JBSE_BASE, "" + REFERENCE + JAVA_STRING + TYPEEND, "JAVA_CLASS_PATH");
    public static final Signature JBSE_BASE_JAVA_EXT_DIRS = 
        new Signature(JBSE_BASE, "" + REFERENCE + JAVA_STRING + TYPEEND, "JAVA_EXT_DIRS");
    public static final Signature JBSE_BASE_JAVA_HOME = 
        new Signature(JBSE_BASE, "" + REFERENCE + JAVA_STRING + TYPEEND, "JAVA_HOME");
    public static final Signature JBSE_BASE_JAVA_IO_TMPDIR = 
        new Signature(JBSE_BASE, "" + REFERENCE + JAVA_STRING + TYPEEND, "JAVA_IO_TMPDIR");
    public static final Signature JBSE_BASE_JAVA_LIBRARY_PATH = 
        new Signature(JBSE_BASE, "" + REFERENCE + JAVA_STRING + TYPEEND, "JAVA_LIBRARY_PATH");
    public static final Signature JBSE_BASE_JAVA_VERSION = 
        new Signature(JBSE_BASE, "" + REFERENCE + JAVA_STRING + TYPEEND, "JAVA_VERSION");
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
    public static final Signature JBSE_BASE_SUN_ARCH_DATA_MODEL = 
        new Signature(JBSE_BASE, "" + REFERENCE + JAVA_STRING + TYPEEND, "SUN_ARCH_DATA_MODEL");
    public static final Signature JBSE_BASE_SUN_BOOT_CLASS_PATH = 
        new Signature(JBSE_BASE, "" + REFERENCE + JAVA_STRING + TYPEEND, "SUN_BOOT_CLASS_PATH");
    public static final Signature JBSE_BASE_SUN_BOOT_LIBRARY_PATH = 
        new Signature(JBSE_BASE, "" + REFERENCE + JAVA_STRING + TYPEEND, "SUN_BOOT_LIBRARY_PATH");
    public static final Signature JBSE_BASE_SUN_CPU_ENDIAN = 
        new Signature(JBSE_BASE, "" + REFERENCE + JAVA_STRING + TYPEEND, "SUN_CPU_ENDIAN");
    public static final Signature JBSE_BASE_SUN_CPU_ISALIST = 
        new Signature(JBSE_BASE, "" + REFERENCE + JAVA_STRING + TYPEEND, "SUN_CPU_ISALIST");
    public static final Signature JBSE_BASE_SUN_DESKTOP = 
        new Signature(JBSE_BASE, "" + REFERENCE + JAVA_STRING + TYPEEND, "SUN_DESKTOP");
    public static final Signature JBSE_BASE_SUN_IO_UNICODE_ENCODING = 
        new Signature(JBSE_BASE, "" + REFERENCE + JAVA_STRING + TYPEEND, "SUN_IO_UNICODE_ENCODING");
    public static final Signature JBSE_BASE_SUN_JAVA2D_FONTPATH = 
        new Signature(JBSE_BASE, "" + REFERENCE + JAVA_STRING + TYPEEND, "SUN_JAVA2D_FONTPATH");
    public static final Signature JBSE_BASE_SUN_JNU_ENCODING = 
        new Signature(JBSE_BASE, "" + REFERENCE + JAVA_STRING + TYPEEND, "SUN_JNU_ENCODING");
    public static final Signature JBSE_BASE_SUN_OS_PATCH_LEVEL = 
        new Signature(JBSE_BASE, "" + REFERENCE + JAVA_STRING + TYPEEND, "SUN_OS_PATCH_LEVEL");
    public static final Signature JBSE_BASE_SUN_STDERR_ENCODING = 
        new Signature(JBSE_BASE, "" + REFERENCE + JAVA_STRING + TYPEEND, "SUN_STDERR_ENCODING");
    public static final Signature JBSE_BASE_SUN_STDOUT_ENCODING = 
        new Signature(JBSE_BASE, "" + REFERENCE + JAVA_STRING + TYPEEND, "SUN_STDOUT_ENCODING");
    public static final Signature JBSE_BASE_USER_COUNTRY = 
        new Signature(JBSE_BASE, "" + REFERENCE + JAVA_STRING + TYPEEND, "USER_COUNTRY");
    public static final Signature JBSE_BASE_USER_DIR = 
        new Signature(JBSE_BASE, "" + REFERENCE + JAVA_STRING + TYPEEND, "USER_DIR");
    public static final Signature JBSE_BASE_USER_HOME = 
        new Signature(JBSE_BASE, "" + REFERENCE + JAVA_STRING + TYPEEND, "USER_HOME");
    public static final Signature JBSE_BASE_USER_LANGUAGE = 
        new Signature(JBSE_BASE, "" + REFERENCE + JAVA_STRING + TYPEEND, "USER_LANGUAGE");
    public static final Signature JBSE_BASE_USER_NAME = 
        new Signature(JBSE_BASE, "" + REFERENCE + JAVA_STRING + TYPEEND, "USER_NAME");
    public static final Signature JBSE_BASE_USER_SCRIPT = 
        new Signature(JBSE_BASE, "" + REFERENCE + JAVA_STRING + TYPEEND, "USER_SCRIPT");
    public static final Signature JBSE_BASE_USER_TIMEZONE = 
        new Signature(JBSE_BASE, "" + REFERENCE + JAVA_STRING + TYPEEND, "USER_TIMEZONE");
    public static final Signature JBSE_BASE_USER_VARIANT = 
        new Signature(JBSE_BASE, "" + REFERENCE + JAVA_STRING + TYPEEND, "USER_VARIANT");
    public static final Signature SUN_CONSTANTPOOL_CONSTANTPOOLOOP =
        new Signature(SUN_CONSTANTPOOL, "" + REFERENCE + JAVA_OBJECT + TYPEEND, "constantPoolOop");

    /**
     * Do not instantiate it! 
     */
    private Signatures() {
        //intentionally empty
    }
}
