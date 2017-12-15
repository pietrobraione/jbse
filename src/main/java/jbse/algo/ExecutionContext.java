package jbse.algo;

import static jbse.algo.Overrides.ALGO_INVOKEMETA_PURE;
import static jbse.algo.Overrides.ALGO_JAVA_CLASS_GETCOMPONENTTYPE;
import static jbse.algo.Overrides.ALGO_JAVA_CLASS_FORNAME0;
import static jbse.algo.Overrides.ALGO_JAVA_CLASS_GETDECLAREDCONSTRUCTORS0;
import static jbse.algo.Overrides.ALGO_JAVA_CLASS_GETDECLAREDFIELDS0;
import static jbse.algo.Overrides.ALGO_JAVA_CLASS_GETDECLARINGCLASS0;
import static jbse.algo.Overrides.ALGO_JAVA_CLASS_GETENCLOSINGMETHOD0;
import static jbse.algo.Overrides.ALGO_JAVA_CLASS_GETMODIFIERS;
import static jbse.algo.Overrides.ALGO_JAVA_CLASS_GETNAME0;
import static jbse.algo.Overrides.ALGO_JAVA_CLASS_GETPRIMITIVECLASS;
import static jbse.algo.Overrides.ALGO_JAVA_CLASS_GETSUPERCLASS;
import static jbse.algo.Overrides.ALGO_JAVA_CLASS_ISARRAY;
import static jbse.algo.Overrides.ALGO_JAVA_CLASS_ISASSIGNABLEFROM;
import static jbse.algo.Overrides.ALGO_JAVA_CLASS_ISINSTANCE;
import static jbse.algo.Overrides.ALGO_JAVA_CLASS_ISINTERFACE;
import static jbse.algo.Overrides.ALGO_JAVA_CLASS_ISPRIMITIVE;
import static jbse.algo.Overrides.ALGO_JAVA_CLASSLOADER_NATIVELIBRARY_LOAD;
import static jbse.algo.Overrides.ALGO_JAVA_METHODHANDLENATIVES_RESOLVE;
import static jbse.algo.Overrides.ALGO_JAVA_OBJECT_CLONE;
import static jbse.algo.Overrides.ALGO_JAVA_OBJECT_GETCLASS;
import static jbse.algo.Overrides.ALGO_JAVA_OBJECT_HASHCODE;
import static jbse.algo.Overrides.ALGO_JAVA_REFLECT_ARRAY_NEWARRAY;
import static jbse.algo.Overrides.ALGO_JAVA_STRING_HASHCODE;
import static jbse.algo.Overrides.ALGO_JAVA_STRING_INTERN;
import static jbse.algo.Overrides.ALGO_JAVA_STRINGBUILDER_APPEND;
import static jbse.algo.Overrides.ALGO_JAVA_SYSTEM_ARRAYCOPY;
import static jbse.algo.Overrides.ALGO_JAVA_SYSTEM_IDENTITYHASHCODE;
import static jbse.algo.Overrides.ALGO_JAVA_SYSTEM_MAPLIBRARYNAME;
import static jbse.algo.Overrides.ALGO_JAVA_SYSTEM_SETERR0;
import static jbse.algo.Overrides.ALGO_JAVA_SYSTEM_SETIN0;
import static jbse.algo.Overrides.ALGO_JAVA_SYSTEM_SETOUT0;
import static jbse.algo.Overrides.ALGO_JAVA_THREAD_CURRENTTHREAD;
import static jbse.algo.Overrides.ALGO_JAVA_THROWABLE_FILLINSTACKTRACE;
import static jbse.algo.Overrides.ALGO_JAVA_THROWABLE_GETSTACKTRACEDEPTH;
import static jbse.algo.Overrides.ALGO_JAVA_THROWABLE_GETSTACKTRACEELEMENT;
import static jbse.algo.Overrides.ALGO_JBSE_ANALYSIS_ANY;
import static jbse.algo.Overrides.ALGO_JBSE_ANALYSIS_ASSUMECLASSNOTINITIALIZED;
import static jbse.algo.Overrides.ALGO_JBSE_ANALYSIS_ENDGUIDANCE;
import static jbse.algo.Overrides.ALGO_JBSE_ANALYSIS_FAIL;
import static jbse.algo.Overrides.ALGO_JBSE_ANALYSIS_IGNORE;
import static jbse.algo.Overrides.ALGO_JBSE_ANALYSIS_ISRESOLVED;
import static jbse.algo.Overrides.ALGO_JBSE_ANALYSIS_ISSYMBOLIC;
import static jbse.algo.Overrides.ALGO_JBSE_ANALYSIS_SUCCEED;
import static jbse.algo.Overrides.ALGO_JBSE_ANALYSIS_SYMBOLNAME;
import static jbse.algo.Overrides.ALGO_noclass_REGISTERMETHODTYPE;
import static jbse.algo.Overrides.ALGO_noclass_STORELINKEDMETHODANDAPPENDIX;
import static jbse.algo.Overrides.ALGO_SUN_NATIVECONSTRUCTORACCESSORIMPL_NEWINSTANCE0;
import static jbse.algo.Overrides.ALGO_SUN_REFLECTION_GETCALLERCLASS;
import static jbse.algo.Overrides.ALGO_SUN_REFLECTION_GETCLASSACCESSFLAGS;
import static jbse.algo.Overrides.ALGO_SUN_UNSAFE_ALLOCATEMEMORY;
import static jbse.algo.Overrides.ALGO_SUN_UNSAFE_COMPAREANDSWAPINT;
import static jbse.algo.Overrides.ALGO_SUN_UNSAFE_COMPAREANDSWAPLONG;
import static jbse.algo.Overrides.ALGO_SUN_UNSAFE_COMPAREANDSWAPOBJECT;
import static jbse.algo.Overrides.ALGO_SUN_UNSAFE_DEFINEANONYMOUSCLASS;
import static jbse.algo.Overrides.ALGO_SUN_UNSAFE_ENSURECLASSINITIALIZED;
import static jbse.algo.Overrides.ALGO_SUN_UNSAFE_FREEMEMORY;
import static jbse.algo.Overrides.ALGO_SUN_UNSAFE_GETBYTE;
import static jbse.algo.Overrides.ALGO_SUN_UNSAFE_GETINTVOLATILE;
import static jbse.algo.Overrides.ALGO_SUN_UNSAFE_GETLONG;
import static jbse.algo.Overrides.ALGO_SUN_UNSAFE_GETOBJECTVOLATILE;
import static jbse.algo.Overrides.ALGO_SUN_UNSAFE_OBJECTFIELDOFFSET;
import static jbse.algo.Overrides.ALGO_SUN_UNSAFE_PUTLONG;
import static jbse.algo.Overrides.ALGO_SUN_UNSAFE_PUTOBJECTVOLATILE;
import static jbse.algo.Overrides.ALGO_SUN_UNSAFE_SHOULDBEINITIALIZED;
import static jbse.algo.Overrides.BASE_JAVA_ACCESSCONTROLLER_DOPRIVILEGED_EXCEPTION;
import static jbse.algo.Overrides.BASE_JAVA_ACCESSCONTROLLER_DOPRIVILEGED_NOEXCEPTION;
import static jbse.algo.Overrides.BASE_JAVA_ACCESSCONTROLLER_GETSTACKACCESSCONTROLCONTEXT;
import static jbse.algo.Overrides.BASE_JAVA_ATOMICLONG_VMSUPPORTSCS8;
import static jbse.algo.Overrides.BASE_JAVA_CLASS_DESIREDASSERTIONSTATUS0;
import static jbse.algo.Overrides.BASE_JAVA_CLASSLOADER_FINDBUILTINLIB;
import static jbse.algo.Overrides.BASE_JAVA_METHODHANDLENATIVES_GETCONSTANT;
import static jbse.algo.Overrides.BASE_JAVA_RUNTIME_AVAILABLEPROCESSORS;
import static jbse.algo.Overrides.BASE_JAVA_SYSTEM_INITPROPERTIES;
import static jbse.algo.Overrides.BASE_JAVA_THREAD_ISALIVE;
import static jbse.algo.Overrides.BASE_JBSE_ANALYSIS_ISRUNBYJBSE;
import static jbse.algo.Overrides.BASE_SUN_SIGNAL_FINDSIGNAL;
import static jbse.algo.Overrides.BASE_SUN_SIGNAL_HANDLE0;
import static jbse.algo.Overrides.BASE_SUN_UNSAFE_ADDRESSSIZE;
import static jbse.algo.Overrides.BASE_SUN_UNSAFE_ARRAYBASEOFFSET;
import static jbse.algo.Overrides.BASE_SUN_UNSAFE_ARRAYINDEXSCALE;

import static jbse.bc.Signatures.ARITHMETIC_EXCEPTION;
import static jbse.bc.Signatures.ARRAY_STORE_EXCEPTION;
import static jbse.bc.Signatures.CLASS_CAST_EXCEPTION;
import static jbse.bc.Signatures.ERROR;
import static jbse.bc.Signatures.ILLEGAL_ARGUMENT_EXCEPTION;
import static jbse.bc.Signatures.ILLEGAL_MONITOR_STATE_EXCEPTION;
import static jbse.bc.Signatures.JAVA_ABSTRACTCOLLECTION;
import static jbse.bc.Signatures.JAVA_ABSTRACTLIST;
import static jbse.bc.Signatures.JAVA_ABSTRACTMAP;
import static jbse.bc.Signatures.JAVA_ABSTRACTSET;
import static jbse.bc.Signatures.JAVA_ABSTRACTSTRINGBUILDER;
import static jbse.bc.Signatures.JAVA_ACCESSCONTROLCONTEXT;
import static jbse.bc.Signatures.JAVA_ACCESSCONTROLLER;
import static jbse.bc.Signatures.JAVA_ACCESSCONTROLLER_DOPRIVILEGED_EXCEPTION;
import static jbse.bc.Signatures.JAVA_ACCESSCONTROLLER_DOPRIVILEGED_NOEXCEPTION;
import static jbse.bc.Signatures.JAVA_ACCESSCONTROLLER_GETSTACKACCESSCONTROLCONTEXT;
import static jbse.bc.Signatures.JAVA_ACCESSIBLEOBJECT;
import static jbse.bc.Signatures.JAVA_ARRAYLIST;
import static jbse.bc.Signatures.JAVA_ARRAYS;
import static jbse.bc.Signatures.JAVA_ATOMICINTEGER;
import static jbse.bc.Signatures.JAVA_ATOMICLONG;
import static jbse.bc.Signatures.JAVA_ATOMICLONG_VMSUPPORTSCS8;
import static jbse.bc.Signatures.JAVA_ATOMICREFERENCEFIELDUPDATER;
import static jbse.bc.Signatures.JAVA_ATOMICREFERENCEFIELDUPDATER_IMPL;
import static jbse.bc.Signatures.JAVA_ATOMICREFERENCEFIELDUPDATER_IMPL_1;
import static jbse.bc.Signatures.JAVA_BASICPERMISSION;
import static jbse.bc.Signatures.JAVA_BITS;
import static jbse.bc.Signatures.JAVA_BITS_1;
import static jbse.bc.Signatures.JAVA_BOOLEAN;
import static jbse.bc.Signatures.JAVA_BOUNDMETHODHANDLE;
import static jbse.bc.Signatures.JAVA_BOUNDMETHODHANDLE_SPECIESDATA;
import static jbse.bc.Signatures.JAVA_BOUNDMETHODHANDLE_SPECIES_L;
import static jbse.bc.Signatures.JAVA_BUFFER;
import static jbse.bc.Signatures.JAVA_BUFFEREDINPUTSTREAM;
import static jbse.bc.Signatures.JAVA_BUFFEREDOUTPUTSTREAM;
import static jbse.bc.Signatures.JAVA_BUFFEREDWRITER;
import static jbse.bc.Signatures.JAVA_BYTE;
import static jbse.bc.Signatures.JAVA_BYTE_BYTECACHE;
import static jbse.bc.Signatures.JAVA_BYTEBUFFER;
import static jbse.bc.Signatures.JAVA_BYTEORDER;
import static jbse.bc.Signatures.JAVA_CHARACTER;
import static jbse.bc.Signatures.JAVA_CHARACTER_CHARACTERCACHE;
import static jbse.bc.Signatures.JAVA_CHARSET;
import static jbse.bc.Signatures.JAVA_CHARSET_EXTENDEDPROVIDERHOLDER;
import static jbse.bc.Signatures.JAVA_CHARSETENCODER;
import static jbse.bc.Signatures.JAVA_CHARSETPROVIDER;
import static jbse.bc.Signatures.JAVA_CLASS;
import static jbse.bc.Signatures.JAVA_CLASS_1;
import static jbse.bc.Signatures.JAVA_CLASS_3;
import static jbse.bc.Signatures.JAVA_CLASS_ATOMIC;
import static jbse.bc.Signatures.JAVA_CLASS_DESIREDASSERTIONSTATUS0;
import static jbse.bc.Signatures.JAVA_CLASS_FORNAME0;
import static jbse.bc.Signatures.JAVA_CLASS_GETCOMPONENTTYPE;
import static jbse.bc.Signatures.JAVA_CLASS_GETDECLAREDCONSTRUCTORS0;
import static jbse.bc.Signatures.JAVA_CLASS_GETDECLAREDFIELDS0;
import static jbse.bc.Signatures.JAVA_CLASS_GETDECLARINGCLASS0;
import static jbse.bc.Signatures.JAVA_CLASS_GETENCLOSINGMETHOD0;
import static jbse.bc.Signatures.JAVA_CLASS_GETMODIFIERS;
import static jbse.bc.Signatures.JAVA_CLASS_GETNAME0;
import static jbse.bc.Signatures.JAVA_CLASS_GETPRIMITIVECLASS;
import static jbse.bc.Signatures.JAVA_CLASS_GETSUPERCLASS;
import static jbse.bc.Signatures.JAVA_CLASS_ISARRAY;
import static jbse.bc.Signatures.JAVA_CLASS_ISASSIGNABLEFROM;
import static jbse.bc.Signatures.JAVA_CLASS_ISINSTANCE;
import static jbse.bc.Signatures.JAVA_CLASS_ISINTERFACE;
import static jbse.bc.Signatures.JAVA_CLASS_ISPRIMITIVE;
import static jbse.bc.Signatures.JAVA_CLASS_REFLECTIONDATA;
import static jbse.bc.Signatures.JAVA_CLASS_REGISTERNATIVES;
import static jbse.bc.Signatures.JAVA_CLASSLOADER;
import static jbse.bc.Signatures.JAVA_CLASSLOADER_FINDBUILTINLIB;
import static jbse.bc.Signatures.JAVA_CLASSLOADER_NATIVELIBRARY;
import static jbse.bc.Signatures.JAVA_CLASSLOADER_NATIVELIBRARY_LOAD;
import static jbse.bc.Signatures.JAVA_CLASSLOADER_PARALLELLOADERS;
import static jbse.bc.Signatures.JAVA_CLASSLOADER_REGISTERNATIVES;
import static jbse.bc.Signatures.JAVA_CLASSVALUE;
import static jbse.bc.Signatures.JAVA_CODESOURCE;
import static jbse.bc.Signatures.JAVA_CODINGERRORACTION;
import static jbse.bc.Signatures.JAVA_COLLECTIONS;
import static jbse.bc.Signatures.JAVA_COLLECTIONS_EMPTYLIST;
import static jbse.bc.Signatures.JAVA_COLLECTIONS_EMPTYMAP;
import static jbse.bc.Signatures.JAVA_COLLECTIONS_EMPTYSET;
import static jbse.bc.Signatures.JAVA_COLLECTIONS_SETFROMMAP;
import static jbse.bc.Signatures.JAVA_COLLECTIONS_SYNCHRONIZEDCOLLECTION;
import static jbse.bc.Signatures.JAVA_COLLECTIONS_SYNCHRONIZEDSET;
import static jbse.bc.Signatures.JAVA_COLLECTIONS_UNMODIFIABLECOLLECTION;
import static jbse.bc.Signatures.JAVA_COLLECTIONS_UNMODIFIABLELIST;
import static jbse.bc.Signatures.JAVA_COLLECTIONS_UNMODIFIABLERANDOMACCESSLIST;
import static jbse.bc.Signatures.JAVA_CONCURRENTHASHMAP;
import static jbse.bc.Signatures.JAVA_CONSTRUCTOR;
import static jbse.bc.Signatures.JAVA_DEFAULTFILESYSTEM;
import static jbse.bc.Signatures.JAVA_DICTIONARY;
import static jbse.bc.Signatures.JAVA_DIRECTMETHODHANDLE;
import static jbse.bc.Signatures.JAVA_DOUBLE;
import static jbse.bc.Signatures.JAVA_DOUBLE_DOUBLETORAWLONGBITS;
import static jbse.bc.Signatures.JAVA_DOUBLE_LONGBITSTODOUBLE;
import static jbse.bc.Signatures.JAVA_ENUM;
import static jbse.bc.Signatures.JAVA_EXCEPTION;
import static jbse.bc.Signatures.JAVA_EXECUTABLE;
import static jbse.bc.Signatures.JAVA_EXPIRINGCACHE;
import static jbse.bc.Signatures.JAVA_EXPIRINGCACHE_1;
import static jbse.bc.Signatures.JAVA_FIELD;
import static jbse.bc.Signatures.JAVA_FILE;
import static jbse.bc.Signatures.JAVA_FILEDESCRIPTOR;
import static jbse.bc.Signatures.JAVA_FILEDESCRIPTOR_INITIDS;
import static jbse.bc.Signatures.JAVA_FILEDESCRIPTOR_1;
import static jbse.bc.Signatures.JAVA_FILEINPUTSTREAM;
import static jbse.bc.Signatures.JAVA_FILEINPUTSTREAM_INITIDS;
import static jbse.bc.Signatures.JAVA_FILEOUTPUTSTREAM;
import static jbse.bc.Signatures.JAVA_FILEOUTPUTSTREAM_INITIDS;
import static jbse.bc.Signatures.JAVA_FILESYSTEM;
import static jbse.bc.Signatures.JAVA_FILTERINPUTSTREAM;
import static jbse.bc.Signatures.JAVA_FILTEROUTPUTSTREAM;
import static jbse.bc.Signatures.JAVA_FINALIZER;
import static jbse.bc.Signatures.JAVA_FINALIZER_FINALIZERTHREAD;
import static jbse.bc.Signatures.JAVA_FINALREFERENCE;
import static jbse.bc.Signatures.JAVA_FLOAT;
import static jbse.bc.Signatures.JAVA_FLOAT_FLOATTORAWINTBITS;
import static jbse.bc.Signatures.JAVA_HASHMAP;
import static jbse.bc.Signatures.JAVA_HASHMAP_NODE;
import static jbse.bc.Signatures.JAVA_HASHSET;
import static jbse.bc.Signatures.JAVA_HASHTABLE;
import static jbse.bc.Signatures.JAVA_HASHTABLE_ENTRY;
import static jbse.bc.Signatures.JAVA_HASHTABLE_ENTRYSET;
import static jbse.bc.Signatures.JAVA_HASHTABLE_ENUMERATOR;
import static jbse.bc.Signatures.JAVA_HEAPBYTEBUFFER;
import static jbse.bc.Signatures.JAVA_IDENTITYHASHMAP;
import static jbse.bc.Signatures.JAVA_INPUTSTREAM;
import static jbse.bc.Signatures.JAVA_INTEGER;
import static jbse.bc.Signatures.JAVA_INTEGER_INTEGERCACHE;
import static jbse.bc.Signatures.JAVA_INTERRUPTEDEXCEPTION;
import static jbse.bc.Signatures.JAVA_INVOKERBYTECODEGENERATOR;
import static jbse.bc.Signatures.JAVA_INVOKERBYTECODEGENERATOR_2;
import static jbse.bc.Signatures.JAVA_LAMBDAFORM;
import static jbse.bc.Signatures.JAVA_LAMBDAFORM_NAME;
import static jbse.bc.Signatures.JAVA_LINKEDHASHMAP;
import static jbse.bc.Signatures.JAVA_LINKEDLIST;
import static jbse.bc.Signatures.JAVA_LINKEDLIST_ENTRY;
import static jbse.bc.Signatures.JAVA_LONG;
import static jbse.bc.Signatures.JAVA_LONG_LONGCACHE;
import static jbse.bc.Signatures.JAVA_MATH;
import static jbse.bc.Signatures.JAVA_MEMBERNAME;
import static jbse.bc.Signatures.JAVA_MEMBERNAME_FACTORY;
import static jbse.bc.Signatures.JAVA_METHOD;
import static jbse.bc.Signatures.JAVA_METHODHANDLE;
import static jbse.bc.Signatures.JAVA_METHODHANDLEIMPL;
import static jbse.bc.Signatures.JAVA_METHODHANDLENATIVES;
import static jbse.bc.Signatures.JAVA_METHODHANDLENATIVES_GETCONSTANT;
import static jbse.bc.Signatures.JAVA_METHODHANDLENATIVES_REGISTERNATIVES;
import static jbse.bc.Signatures.JAVA_METHODHANDLENATIVES_RESOLVE;
import static jbse.bc.Signatures.JAVA_METHODHANDLES;
import static jbse.bc.Signatures.JAVA_METHODHANDLES_LOOKUP;
import static jbse.bc.Signatures.JAVA_METHODHANDLESTATICS;
import static jbse.bc.Signatures.JAVA_METHODTYPE;
import static jbse.bc.Signatures.JAVA_METHODTYPEFORM;
import static jbse.bc.Signatures.JAVA_MODIFIER;
import static jbse.bc.Signatures.JAVA_NUMBER;
import static jbse.bc.Signatures.JAVA_OBJECT;
import static jbse.bc.Signatures.JAVA_OBJECT_CLONE;
import static jbse.bc.Signatures.JAVA_OBJECT_GETCLASS;
import static jbse.bc.Signatures.JAVA_OBJECT_HASHCODE;
import static jbse.bc.Signatures.JAVA_OBJECT_NOTIFYALL;
import static jbse.bc.Signatures.JAVA_OBJECT_REGISTERNATIVES;
import static jbse.bc.Signatures.JAVA_OBJECTS;
import static jbse.bc.Signatures.JAVA_OUTPUTSTREAM;
import static jbse.bc.Signatures.JAVA_OUTPUTSTREAMWRITER;
import static jbse.bc.Signatures.JAVA_PERMISSION;
import static jbse.bc.Signatures.JAVA_PHANTOMREFERENCE;
import static jbse.bc.Signatures.JAVA_PRINTSTREAM;
import static jbse.bc.Signatures.JAVA_PROPERTIES;
import static jbse.bc.Signatures.JAVA_PROTECTIONDOMAIN;
import static jbse.bc.Signatures.JAVA_PROTECTIONDOMAIN_2;
import static jbse.bc.Signatures.JAVA_PROTECTIONDOMAIN_JAVASECURITYACCESSIMPL;
import static jbse.bc.Signatures.JAVA_PROTECTIONDOMAIN_KEY;
import static jbse.bc.Signatures.JAVA_REFERENCE;
import static jbse.bc.Signatures.JAVA_REFERENCE_1;
import static jbse.bc.Signatures.JAVA_REFERENCE_LOCK;
import static jbse.bc.Signatures.JAVA_REFERENCE_REFERENCEHANDLER;
import static jbse.bc.Signatures.JAVA_REFERENCEQUEUE;
import static jbse.bc.Signatures.JAVA_REFERENCEQUEUE_LOCK;
import static jbse.bc.Signatures.JAVA_REFERENCEQUEUE_NULL;
import static jbse.bc.Signatures.JAVA_REFLECT_ARRAY_NEWARRAY;
import static jbse.bc.Signatures.JAVA_REFLECTACCESS;
import static jbse.bc.Signatures.JAVA_REFLECTPERMISSION;
import static jbse.bc.Signatures.JAVA_RUNTIME;
import static jbse.bc.Signatures.JAVA_RUNTIME_AVAILABLEPROCESSORS;
import static jbse.bc.Signatures.JAVA_RUNTIMEEXCEPTION;
import static jbse.bc.Signatures.JAVA_RUNTIMEPERMISSION;
import static jbse.bc.Signatures.JAVA_SECURECLASSLOADER;
import static jbse.bc.Signatures.JAVA_SHORT;
import static jbse.bc.Signatures.JAVA_SHORT_SHORTCACHE;
import static jbse.bc.Signatures.JAVA_SIMPLEMETHODHANDLE;
import static jbse.bc.Signatures.JAVA_STACK;
import static jbse.bc.Signatures.JAVA_STRICTMATH_ACOS;
import static jbse.bc.Signatures.JAVA_STRICTMATH_ASIN;
import static jbse.bc.Signatures.JAVA_STRICTMATH_ATAN;
import static jbse.bc.Signatures.JAVA_STRICTMATH_ATAN2;
import static jbse.bc.Signatures.JAVA_STRICTMATH_CBRT;
import static jbse.bc.Signatures.JAVA_STRICTMATH_COS;
import static jbse.bc.Signatures.JAVA_STRICTMATH_COSH;
import static jbse.bc.Signatures.JAVA_STRICTMATH_EXP;
import static jbse.bc.Signatures.JAVA_STRICTMATH_EXPM1;
import static jbse.bc.Signatures.JAVA_STRICTMATH_HYPOT;
import static jbse.bc.Signatures.JAVA_STRICTMATH_IEEEREMAINDER;
import static jbse.bc.Signatures.JAVA_STRICTMATH_LOG;
import static jbse.bc.Signatures.JAVA_STRICTMATH_LOG10;
import static jbse.bc.Signatures.JAVA_STRICTMATH_LOG1P;
import static jbse.bc.Signatures.JAVA_STRICTMATH_POW;
import static jbse.bc.Signatures.JAVA_STRICTMATH_SIN;
import static jbse.bc.Signatures.JAVA_STRICTMATH_SINH;
import static jbse.bc.Signatures.JAVA_STRICTMATH_SQRT;
import static jbse.bc.Signatures.JAVA_STRICTMATH_TAN;
import static jbse.bc.Signatures.JAVA_STRICTMATH_TANH;
import static jbse.bc.Signatures.JAVA_STRING;
import static jbse.bc.Signatures.JAVA_STRING_CASEINSCOMP;
import static jbse.bc.Signatures.JAVA_STRING_HASHCODE;
import static jbse.bc.Signatures.JAVA_STRING_INTERN;
import static jbse.bc.Signatures.JAVA_STRINGBUILDER;
import static jbse.bc.Signatures.JAVA_STRINGBUILDER_APPEND_BOOLEAN;
import static jbse.bc.Signatures.JAVA_STRINGBUILDER_APPEND_CHAR;
import static jbse.bc.Signatures.JAVA_STRINGBUILDER_APPEND_DOUBLE;
import static jbse.bc.Signatures.JAVA_STRINGBUILDER_APPEND_FLOAT;
import static jbse.bc.Signatures.JAVA_STRINGBUILDER_APPEND_INT;
import static jbse.bc.Signatures.JAVA_STRINGBUILDER_APPEND_LONG;
import static jbse.bc.Signatures.JAVA_SYSTEM;
import static jbse.bc.Signatures.JAVA_SYSTEM_ARRAYCOPY;
import static jbse.bc.Signatures.JAVA_SYSTEM_IDENTITYHASHCODE;
import static jbse.bc.Signatures.JAVA_SYSTEM_INITPROPERTIES;
import static jbse.bc.Signatures.JAVA_SYSTEM_MAPLIBRARYNAME;
import static jbse.bc.Signatures.JAVA_SYSTEM_REGISTERNATIVES;
import static jbse.bc.Signatures.JAVA_SYSTEM_SETERR0;
import static jbse.bc.Signatures.JAVA_SYSTEM_SETIN0;
import static jbse.bc.Signatures.JAVA_SYSTEM_SETOUT0;
import static jbse.bc.Signatures.JAVA_SYSTEM_2;
import static jbse.bc.Signatures.JAVA_TERMINATOR;
import static jbse.bc.Signatures.JAVA_TERMINATOR_1;
import static jbse.bc.Signatures.JAVA_THREAD;
import static jbse.bc.Signatures.JAVA_THREAD_CURRENTTHREAD;
import static jbse.bc.Signatures.JAVA_THREAD_ISALIVE;
import static jbse.bc.Signatures.JAVA_THREAD_REGISTERNATIVES;
import static jbse.bc.Signatures.JAVA_THREAD_SETPRIORITY0;
import static jbse.bc.Signatures.JAVA_THREAD_START0;
import static jbse.bc.Signatures.JAVA_THREADGROUP;
import static jbse.bc.Signatures.JAVA_THREADLOCAL;
import static jbse.bc.Signatures.JAVA_THROWABLE;
import static jbse.bc.Signatures.JAVA_THROWABLE_FILLINSTACKTRACE;
import static jbse.bc.Signatures.JAVA_THROWABLE_GETSTACKTRACEDEPTH;
import static jbse.bc.Signatures.JAVA_THROWABLE_GETSTACKTRACEELEMENT;
import static jbse.bc.Signatures.JAVA_THROWABLE_SENTINELHOLDER;
import static jbse.bc.Signatures.JAVA_TREESET;
import static jbse.bc.Signatures.JAVA_UNIXFILESYSTEM;
import static jbse.bc.Signatures.JAVA_UNIXFILESYSTEM_INITIDS;
import static jbse.bc.Signatures.JAVA_URLCLASSLOADER;
import static jbse.bc.Signatures.JAVA_URLCLASSLOADER_7;
import static jbse.bc.Signatures.JAVA_URLSTREAMHANDLER;
import static jbse.bc.Signatures.JAVA_VECTOR;
import static jbse.bc.Signatures.JAVA_VOID;
import static jbse.bc.Signatures.JAVA_WEAKHASHMAP;
import static jbse.bc.Signatures.JAVA_WEAKHASHMAP_ENTRY;
import static jbse.bc.Signatures.JAVA_WEAKHASHMAP_KEYSET;
import static jbse.bc.Signatures.JAVA_WEAKREFERENCE;
import static jbse.bc.Signatures.JAVA_WINNTFILESYSTEM;
import static jbse.bc.Signatures.JAVA_WINNTFILESYSTEM_INITIDS;
import static jbse.bc.Signatures.JAVA_WRITER;
import static jbse.bc.Signatures.JBSE_ANALYSIS_ANY;
import static jbse.bc.Signatures.JBSE_ANALYSIS_ASSUMECLASSNOTINITIALIZED;
import static jbse.bc.Signatures.JBSE_ANALYSIS_ENDGUIDANCE;
import static jbse.bc.Signatures.JBSE_ANALYSIS_FAIL;
import static jbse.bc.Signatures.JBSE_ANALYSIS_IGNORE;
import static jbse.bc.Signatures.JBSE_ANALYSIS_ISRESOLVED;
import static jbse.bc.Signatures.JBSE_ANALYSIS_ISRUNBYJBSE;
import static jbse.bc.Signatures.JBSE_ANALYSIS_ISSYMBOLIC_BOOLEAN;
import static jbse.bc.Signatures.JBSE_ANALYSIS_ISSYMBOLIC_BYTE;
import static jbse.bc.Signatures.JBSE_ANALYSIS_ISSYMBOLIC_CHAR;
import static jbse.bc.Signatures.JBSE_ANALYSIS_ISSYMBOLIC_DOUBLE;
import static jbse.bc.Signatures.JBSE_ANALYSIS_ISSYMBOLIC_FLOAT;
import static jbse.bc.Signatures.JBSE_ANALYSIS_ISSYMBOLIC_INT;
import static jbse.bc.Signatures.JBSE_ANALYSIS_ISSYMBOLIC_LONG;
import static jbse.bc.Signatures.JBSE_ANALYSIS_ISSYMBOLIC_SHORT;
import static jbse.bc.Signatures.JBSE_ANALYSIS_SUCCEED;
import static jbse.bc.Signatures.JBSE_ANALYSIS_SYMBOLNAME_BOOLEAN;
import static jbse.bc.Signatures.JBSE_ANALYSIS_SYMBOLNAME_BYTE;
import static jbse.bc.Signatures.JBSE_ANALYSIS_SYMBOLNAME_CHAR;
import static jbse.bc.Signatures.JBSE_ANALYSIS_SYMBOLNAME_DOUBLE;
import static jbse.bc.Signatures.JBSE_ANALYSIS_SYMBOLNAME_FLOAT;
import static jbse.bc.Signatures.JBSE_ANALYSIS_SYMBOLNAME_INT;
import static jbse.bc.Signatures.JBSE_ANALYSIS_SYMBOLNAME_LONG;
import static jbse.bc.Signatures.JBSE_ANALYSIS_SYMBOLNAME_SHORT;
import static jbse.bc.Signatures.JBSE_BASE;
import static jbse.bc.Signatures.noclass_REGISTERMETHODTYPE;
import static jbse.bc.Signatures.noclass_STORELINKEDMETHODANDAPPENDIX;
import static jbse.bc.Signatures.NULL_POINTER_EXCEPTION;
import static jbse.bc.Signatures.OUT_OF_MEMORY_ERROR;
import static jbse.bc.Signatures.STACK_OVERFLOW_ERROR;
import static jbse.bc.Signatures.SUN_CLEANER;
import static jbse.bc.Signatures.SUN_CONSTRUCTORACCESSORIMPL;
import static jbse.bc.Signatures.SUN_DEBUG;
import static jbse.bc.Signatures.SUN_DELEGATINGCONSTRUCTORACCESSORIMPL;
import static jbse.bc.Signatures.SUN_FASTCHARSETPROVIDER;
import static jbse.bc.Signatures.SUN_GETPROPERTYACTION;
import static jbse.bc.Signatures.SUN_HANDLER;
import static jbse.bc.Signatures.SUN_LAUNCHER;
import static jbse.bc.Signatures.SUN_LAUNCHERHELPER;
import static jbse.bc.Signatures.SUN_LAUNCHER_EXTCLASSLOADER;
import static jbse.bc.Signatures.SUN_LAUNCHER_EXTCLASSLOADER_1;
import static jbse.bc.Signatures.SUN_LAUNCHER_FACTORY;
import static jbse.bc.Signatures.SUN_MAGICACCESSORIMPL;
import static jbse.bc.Signatures.SUN_NATIVECONSTRUCTORACCESSORIMPL;
import static jbse.bc.Signatures.SUN_NATIVECONSTRUCTORACCESSORIMPL_NEWINSTANCE0;
import static jbse.bc.Signatures.SUN_NATIVESIGNALHANDLER;
import static jbse.bc.Signatures.SUN_OSENVIRONMENT;
import static jbse.bc.Signatures.SUN_PREHASHEDMAP;
import static jbse.bc.Signatures.SUN_REFLECTION;
import static jbse.bc.Signatures.SUN_REFLECTIONFACTORY;
import static jbse.bc.Signatures.SUN_REFLECTIONFACTORY_1;
import static jbse.bc.Signatures.SUN_REFLECTIONFACTORY_GETREFLECTIONFACTORYACTION;
import static jbse.bc.Signatures.SUN_REFLECTION_GETCALLERCLASS;
import static jbse.bc.Signatures.SUN_REFLECTION_GETCLASSACCESSFLAGS;
import static jbse.bc.Signatures.SUN_REFLECTUTIL;
import static jbse.bc.Signatures.SUN_SHAREDSECRETS;
import static jbse.bc.Signatures.SUN_SIGNAL;
import static jbse.bc.Signatures.SUN_SIGNAL_FINDSIGNAL;
import static jbse.bc.Signatures.SUN_SIGNAL_HANDLE0;
import static jbse.bc.Signatures.SUN_SIGNALHANDLER;
import static jbse.bc.Signatures.SUN_STANDARDCHARSETS;
import static jbse.bc.Signatures.SUN_STANDARDCHARSETS_ALIASES;
import static jbse.bc.Signatures.SUN_STANDARDCHARSETS_CACHE;
import static jbse.bc.Signatures.SUN_STANDARDCHARSETS_CLASSES;
import static jbse.bc.Signatures.SUN_STREAMENCODER;
import static jbse.bc.Signatures.SUN_UNICODE;
import static jbse.bc.Signatures.SUN_UNSAFE;
import static jbse.bc.Signatures.SUN_UNSAFE_ADDRESSSIZE;
import static jbse.bc.Signatures.SUN_UNSAFE_ALLOCATEMEMORY;
import static jbse.bc.Signatures.SUN_UNSAFE_ARRAYBASEOFFSET;
import static jbse.bc.Signatures.SUN_UNSAFE_ARRAYINDEXSCALE;
import static jbse.bc.Signatures.SUN_UNSAFE_COMPAREANDSWAPINT;
import static jbse.bc.Signatures.SUN_UNSAFE_COMPAREANDSWAPLONG;
import static jbse.bc.Signatures.SUN_UNSAFE_COMPAREANDSWAPOBJECT;
import static jbse.bc.Signatures.SUN_UNSAFE_DEFINEANONYMOUSCLASS;
import static jbse.bc.Signatures.SUN_UNSAFE_ENSURECLASSINITIALIZED;
import static jbse.bc.Signatures.SUN_UNSAFE_FREEMEMORY;
import static jbse.bc.Signatures.SUN_UNSAFE_GETBYTE;
import static jbse.bc.Signatures.SUN_UNSAFE_GETINTVOLATILE;
import static jbse.bc.Signatures.SUN_UNSAFE_GETLONG;
import static jbse.bc.Signatures.SUN_UNSAFE_GETOBJECTVOLATILE;
import static jbse.bc.Signatures.SUN_UNSAFE_OBJECTFIELDOFFSET;
import static jbse.bc.Signatures.SUN_UNSAFE_PUTLONG;
import static jbse.bc.Signatures.SUN_UNSAFE_PUTOBJECTVOLATILE;
import static jbse.bc.Signatures.SUN_UNSAFE_REGISTERNATIVES;
import static jbse.bc.Signatures.SUN_UNSAFE_SHOULDBEINITIALIZED;
import static jbse.bc.Signatures.SUN_URLCLASSPATH;
import static jbse.bc.Signatures.SUN_UTF_8;
import static jbse.bc.Signatures.SUN_UTF_8_ENCODER;
import static jbse.bc.Signatures.SUN_VERIFYACCESS;
import static jbse.bc.Signatures.SUN_VERSION;
import static jbse.bc.Signatures.SUN_VM;
import static jbse.bc.Signatures.SUN_VM_INITIALIZE;
import static jbse.bc.Signatures.SUN_WRAPPER_FORMAT;
import static jbse.bc.Signatures.VIRTUAL_MACHINE_ERROR;
import static jbse.common.Type.binaryClassName;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import jbse.algo.exc.MetaUnsupportedException;
import jbse.bc.ClassFileFactory;
import jbse.bc.ClassHierarchy;
import jbse.bc.Classpath;
import jbse.bc.Signature;
import jbse.common.exc.UnexpectedInternalException;
import jbse.dec.DecisionProcedureAlgorithms;
import jbse.mem.State;
import jbse.rules.TriggerRulesRepo;
import jbse.tree.DecisionAlternative;
import jbse.tree.DecisionAlternativeComparators;
import jbse.tree.StateTree;
import jbse.tree.StateTree.BreadthMode;
import jbse.tree.StateTree.StateIdentificationMode;
import jbse.val.Calculator;
import jbse.val.ReferenceConcrete;

/**
 * Class containing an execution context, i.e., everything 
 * different from the symbolic execution state necessary to 
 * perform an execution step.
 * 
 * @author Pietro Braione
 */
public final class ExecutionContext {
    /** The maximum length of an array to be granted simple representation. Used during initialization. */
    public final int maxSimpleArrayLength;
    
    /** The maximum heap size expressed as maximum number of objects. Used during initialization. */
    public final long maxHeapSize;
    
    /** The {@link Classpath}. Used during initialization. */
    public final Classpath classpath;

    /** The {@link Signature} of the root (initial) method. Used during initialization. */
    public final Signature rootMethodSignature;

    /** The {@link Calculator}. Used during initialization. */
    public final Calculator calc;

    /** The class for the symbolic execution's {@link ClassFileFactory} 
     * (injected dependency). Used during initialization.
     */
    public final Class<? extends ClassFileFactory> classFileFactoryClass;

    /** 
     * Maps class names to the names of the subclasses that may be 
     * used to expand references. Used during initialization.
     */
    public final Map<String, Set<String>> expansionBackdoor;

    /** 
     * The initial {@link State} of symbolic execution. It is a prototype 
     * that will be cloned by its getter. 
     */
    private State initialState;

    /** The symbolic execution's {@link DecisionAlternativeComparators}. */
    private final DecisionAlternativeComparators comparators;

    /** The {@link DispatcherBytecodeAlgorithm}. */
    public final DispatcherBytecodeAlgorithm dispatcher = new DispatcherBytecodeAlgorithm();

    /** 
     * The {@link DispatcherMeta} for handling methods with 
     * meta-level implementation. 
     */
    public final DispatcherMeta dispatcherMeta = new DispatcherMeta();

    /** Maps method signatures to their base-level overrides. */
    public final HashMap<Signature, Signature> baseOverrides = new HashMap<>();

    /** The symbolic execution's {@link DecisionProcedureAlgorithms}. */
    public final DecisionProcedureAlgorithms decisionProcedure;

    /** The symbolic execution's {@link StateTree}. */
    public final StateTree stateTree;

    /** 
     * The {@link TriggerManager} that handles reference resolution events
     * and executes triggers. 
     */
    public final TriggerManager triggerManager;
    
    /** A {@link ReferenceConcrete} to the main thread group created at init time. */
    private ReferenceConcrete mainThreadGroup;
    
    /** A {@link ReferenceConcrete} to the main thread created at init time. */
    private ReferenceConcrete mainThread;

    /**
     * Constructor.
     * 
     * @param initialState the initial {@code State}, or {@code null} if no
     *        initial state. Warning: all the remaining parameters
     *        must be coherent with it, if not {@code null} (e.g., 
     *        {@code calc} must be the calculator used to create  
     *        {@code initialState}). It will not be modified, but
     *        it shall not be modified externally.
     * @param maxSimpleArrayLength the maximum length an array may have
     *        to be granted simple representation.
     * @param maxHeapSize a {@code long}, the maximum size of the
     *        heap expressed as maximum number of objects it can store.
     * @param classpath a {@link Classpath} object, containing 
     *        information about the classpath of the symbolic execution.
     * @param rootMethodSignature the {@link Signature} of the root method
     *        of the symbolic execution.
     * @param calc a {@link Calculator}.
     * @param decisionProcedure a {@link DecisionProcedureAlgorithms}.
     * @param stateIdentificationMode a {@link StateIdentificationMode}.
     * @param breadthMode a {@link BreadthMode}.
     * @param classFileFactoryClass a {@link Class}{@code <? extends }{@link ClassFileFactory}{@code >}
     *        that will be instantiated by the engine to retrieve classfiles. It must 
     *        provide a parameterless public constructor.
     * @param expansionBackdoor a 
     *        {@link Map}{@code <}{@link String}{@code , }{@link Set}{@code <}{@link String}{@code >>}
     *        associating class names to sets of names of their subclasses. It 
     *        is used in place of the class hierarchy to perform reference expansion.
     * @param rulesTrigger a {@link TriggerRulesRepo}.
     * @param comparators a {@link DecisionAlternativeComparators} which
     *        will be used to establish the order of exploration
     *        for sibling branches.
     * @param nativeInvoker a {@link NativeInvoker} which will be used
     *        to execute native methods.
     */
    public ExecutionContext(State initialState,
                            int maxSimpleArrayLength,
                            long maxHeapSize,
                            Classpath classpath,
                            Signature rootMethodSignature,
                            Calculator calc, 
                            DecisionProcedureAlgorithms decisionProcedure,
                            StateIdentificationMode stateIdentificationMode,
                            BreadthMode breadthMode,
                            Class<? extends ClassFileFactory> classFileFactoryClass, 
                            Map<String, Set<String>> expansionBackdoor,
                            TriggerRulesRepo rulesTrigger,
                            DecisionAlternativeComparators comparators) {
        this.initialState = initialState;
        this.maxSimpleArrayLength = maxSimpleArrayLength;
        this.maxHeapSize = maxHeapSize;
        this.classpath = classpath;
        this.rootMethodSignature = rootMethodSignature;
        this.calc = calc;
        this.decisionProcedure = decisionProcedure;
        this.stateTree = new StateTree(stateIdentificationMode, breadthMode);
        this.classFileFactoryClass = classFileFactoryClass;
        this.expansionBackdoor = new HashMap<>(expansionBackdoor);      //safety copy
        this.triggerManager = new TriggerManager(rulesTrigger.clone()); //safety copy
        this.comparators = comparators;

        //defaults
        try {
            //JRE methods
            addBaseOverridden(JAVA_ACCESSCONTROLLER_DOPRIVILEGED_EXCEPTION,       BASE_JAVA_ACCESSCONTROLLER_DOPRIVILEGED_EXCEPTION);
            addBaseOverridden(JAVA_ACCESSCONTROLLER_DOPRIVILEGED_NOEXCEPTION,     BASE_JAVA_ACCESSCONTROLLER_DOPRIVILEGED_NOEXCEPTION);
            addBaseOverridden(JAVA_ACCESSCONTROLLER_GETSTACKACCESSCONTROLCONTEXT, BASE_JAVA_ACCESSCONTROLLER_GETSTACKACCESSCONTROLCONTEXT);
            addBaseOverridden(JAVA_ATOMICLONG_VMSUPPORTSCS8,                      BASE_JAVA_ATOMICLONG_VMSUPPORTSCS8);
            addBaseOverridden(JAVA_CLASS_DESIREDASSERTIONSTATUS0,                 BASE_JAVA_CLASS_DESIREDASSERTIONSTATUS0);
            addMetaOverridden(JAVA_CLASS_FORNAME0,                                ALGO_JAVA_CLASS_FORNAME0);
            addMetaOverridden(JAVA_CLASS_GETCOMPONENTTYPE,                        ALGO_JAVA_CLASS_GETCOMPONENTTYPE);
            addMetaOverridden(JAVA_CLASS_GETDECLAREDCONSTRUCTORS0,                ALGO_JAVA_CLASS_GETDECLAREDCONSTRUCTORS0);
            addMetaOverridden(JAVA_CLASS_GETDECLAREDFIELDS0,                      ALGO_JAVA_CLASS_GETDECLAREDFIELDS0);
            addMetaOverridden(JAVA_CLASS_GETDECLARINGCLASS0,                      ALGO_JAVA_CLASS_GETDECLARINGCLASS0);
            addMetaOverridden(JAVA_CLASS_GETENCLOSINGMETHOD0,                     ALGO_JAVA_CLASS_GETENCLOSINGMETHOD0);
            addMetaOverridden(JAVA_CLASS_GETMODIFIERS,                            ALGO_JAVA_CLASS_GETMODIFIERS);
            addMetaOverridden(JAVA_CLASS_GETNAME0,                                ALGO_JAVA_CLASS_GETNAME0);
            addMetaOverridden(JAVA_CLASS_GETPRIMITIVECLASS,                       ALGO_JAVA_CLASS_GETPRIMITIVECLASS);
            addMetaOverridden(JAVA_CLASS_GETSUPERCLASS,                           ALGO_JAVA_CLASS_GETSUPERCLASS);
            addMetaOverridden(JAVA_CLASS_ISARRAY,                                 ALGO_JAVA_CLASS_ISARRAY);
            addMetaOverridden(JAVA_CLASS_ISASSIGNABLEFROM,                        ALGO_JAVA_CLASS_ISASSIGNABLEFROM);
            addMetaOverridden(JAVA_CLASS_ISINSTANCE,                              ALGO_JAVA_CLASS_ISINSTANCE);
            addMetaOverridden(JAVA_CLASS_ISINTERFACE,                             ALGO_JAVA_CLASS_ISINTERFACE);
            addMetaOverridden(JAVA_CLASS_ISPRIMITIVE,                             ALGO_JAVA_CLASS_ISPRIMITIVE);
            addMetaOverridden(JAVA_CLASS_REGISTERNATIVES,                         ALGO_INVOKEMETA_PURE);
            addBaseOverridden(JAVA_CLASSLOADER_FINDBUILTINLIB,                    BASE_JAVA_CLASSLOADER_FINDBUILTINLIB);
            addMetaOverridden(JAVA_CLASSLOADER_NATIVELIBRARY_LOAD,                ALGO_JAVA_CLASSLOADER_NATIVELIBRARY_LOAD);
            addMetaOverridden(JAVA_CLASSLOADER_REGISTERNATIVES,                   ALGO_INVOKEMETA_PURE);
            addMetaOverridden(JAVA_DOUBLE_DOUBLETORAWLONGBITS,                    ALGO_INVOKEMETA_PURE);
            addMetaOverridden(JAVA_DOUBLE_LONGBITSTODOUBLE,                       ALGO_INVOKEMETA_PURE);
            addMetaOverridden(JAVA_FILEDESCRIPTOR_INITIDS,                        ALGO_INVOKEMETA_PURE);
            addMetaOverridden(JAVA_FILEINPUTSTREAM_INITIDS,                       ALGO_INVOKEMETA_PURE);
            addMetaOverridden(JAVA_FILEOUTPUTSTREAM_INITIDS,                      ALGO_INVOKEMETA_PURE);
            addMetaOverridden(JAVA_FLOAT_FLOATTORAWINTBITS,                       ALGO_INVOKEMETA_PURE);
            addBaseOverridden(JAVA_METHODHANDLENATIVES_GETCONSTANT,               BASE_JAVA_METHODHANDLENATIVES_GETCONSTANT);
            addMetaOverridden(JAVA_METHODHANDLENATIVES_REGISTERNATIVES,           ALGO_INVOKEMETA_PURE);
            addMetaOverridden(JAVA_METHODHANDLENATIVES_RESOLVE,                   ALGO_JAVA_METHODHANDLENATIVES_RESOLVE);
            addMetaOverridden(JAVA_OBJECT_CLONE,                                  ALGO_JAVA_OBJECT_CLONE);
            addMetaOverridden(JAVA_OBJECT_GETCLASS,                               ALGO_JAVA_OBJECT_GETCLASS);
            addMetaOverridden(JAVA_OBJECT_HASHCODE,                               ALGO_JAVA_OBJECT_HASHCODE);
            addMetaOverridden(JAVA_OBJECT_NOTIFYALL,                              ALGO_INVOKEMETA_PURE);
            addMetaOverridden(JAVA_OBJECT_REGISTERNATIVES,                        ALGO_INVOKEMETA_PURE);
            addMetaOverridden(JAVA_REFLECT_ARRAY_NEWARRAY,                        ALGO_JAVA_REFLECT_ARRAY_NEWARRAY);
            addBaseOverridden(JAVA_RUNTIME_AVAILABLEPROCESSORS,                   BASE_JAVA_RUNTIME_AVAILABLEPROCESSORS);
            addMetaOverridden(JAVA_STRICTMATH_ACOS,                               ALGO_INVOKEMETA_PURE);
            addMetaOverridden(JAVA_STRICTMATH_ASIN,                               ALGO_INVOKEMETA_PURE);
            addMetaOverridden(JAVA_STRICTMATH_ATAN,                               ALGO_INVOKEMETA_PURE);
            addMetaOverridden(JAVA_STRICTMATH_ATAN2,                              ALGO_INVOKEMETA_PURE);
            addMetaOverridden(JAVA_STRICTMATH_CBRT,                               ALGO_INVOKEMETA_PURE);
            addMetaOverridden(JAVA_STRICTMATH_COS,                                ALGO_INVOKEMETA_PURE);
            addMetaOverridden(JAVA_STRICTMATH_COSH,                               ALGO_INVOKEMETA_PURE);
            addMetaOverridden(JAVA_STRICTMATH_EXP,                                ALGO_INVOKEMETA_PURE);
            addMetaOverridden(JAVA_STRICTMATH_EXPM1,                              ALGO_INVOKEMETA_PURE);
            addMetaOverridden(JAVA_STRICTMATH_HYPOT,                              ALGO_INVOKEMETA_PURE);
            addMetaOverridden(JAVA_STRICTMATH_IEEEREMAINDER,                      ALGO_INVOKEMETA_PURE);
            addMetaOverridden(JAVA_STRICTMATH_LOG,                                ALGO_INVOKEMETA_PURE);
            addMetaOverridden(JAVA_STRICTMATH_LOG10,                              ALGO_INVOKEMETA_PURE);
            addMetaOverridden(JAVA_STRICTMATH_LOG1P,                              ALGO_INVOKEMETA_PURE);
            addMetaOverridden(JAVA_STRICTMATH_POW,                                ALGO_INVOKEMETA_PURE);
            addMetaOverridden(JAVA_STRICTMATH_SIN,                                ALGO_INVOKEMETA_PURE);
            addMetaOverridden(JAVA_STRICTMATH_SINH,                               ALGO_INVOKEMETA_PURE);
            addMetaOverridden(JAVA_STRICTMATH_SQRT,                               ALGO_INVOKEMETA_PURE);
            addMetaOverridden(JAVA_STRICTMATH_TAN,                                ALGO_INVOKEMETA_PURE);
            addMetaOverridden(JAVA_STRICTMATH_TANH,                               ALGO_INVOKEMETA_PURE);
            addMetaOverridden(JAVA_STRING_HASHCODE,                               ALGO_JAVA_STRING_HASHCODE);
            addMetaOverridden(JAVA_STRING_INTERN,                                 ALGO_JAVA_STRING_INTERN);
            addMetaOverridden(JAVA_STRINGBUILDER_APPEND_BOOLEAN,                  ALGO_JAVA_STRINGBUILDER_APPEND);
            addMetaOverridden(JAVA_STRINGBUILDER_APPEND_CHAR,                     ALGO_JAVA_STRINGBUILDER_APPEND);
            addMetaOverridden(JAVA_STRINGBUILDER_APPEND_DOUBLE,                   ALGO_JAVA_STRINGBUILDER_APPEND);
            addMetaOverridden(JAVA_STRINGBUILDER_APPEND_FLOAT,                    ALGO_JAVA_STRINGBUILDER_APPEND);
            addMetaOverridden(JAVA_STRINGBUILDER_APPEND_INT,                      ALGO_JAVA_STRINGBUILDER_APPEND);
            addMetaOverridden(JAVA_STRINGBUILDER_APPEND_LONG,                     ALGO_JAVA_STRINGBUILDER_APPEND);
            addMetaOverridden(JAVA_SYSTEM_ARRAYCOPY,                              ALGO_JAVA_SYSTEM_ARRAYCOPY);
            addBaseOverridden(JAVA_SYSTEM_INITPROPERTIES,                         BASE_JAVA_SYSTEM_INITPROPERTIES);
            addMetaOverridden(JAVA_SYSTEM_IDENTITYHASHCODE,                       ALGO_JAVA_SYSTEM_IDENTITYHASHCODE);
            addMetaOverridden(JAVA_SYSTEM_MAPLIBRARYNAME,                         ALGO_JAVA_SYSTEM_MAPLIBRARYNAME);
            addMetaOverridden(JAVA_SYSTEM_REGISTERNATIVES,                        ALGO_INVOKEMETA_PURE);
            addMetaOverridden(JAVA_SYSTEM_SETERR0,                                ALGO_JAVA_SYSTEM_SETERR0);
            addMetaOverridden(JAVA_SYSTEM_SETIN0,                                 ALGO_JAVA_SYSTEM_SETIN0);
            addMetaOverridden(JAVA_SYSTEM_SETOUT0,                                ALGO_JAVA_SYSTEM_SETOUT0);
            addMetaOverridden(JAVA_THREAD_CURRENTTHREAD,                          ALGO_JAVA_THREAD_CURRENTTHREAD);
            addBaseOverridden(JAVA_THREAD_ISALIVE,                                BASE_JAVA_THREAD_ISALIVE);
            addMetaOverridden(JAVA_THREAD_REGISTERNATIVES,                        ALGO_INVOKEMETA_PURE);
            addMetaOverridden(JAVA_THREAD_SETPRIORITY0,                           ALGO_INVOKEMETA_PURE);
            addMetaOverridden(JAVA_THREAD_START0,                                 ALGO_INVOKEMETA_PURE);
            addMetaOverridden(JAVA_THROWABLE_FILLINSTACKTRACE,                    ALGO_JAVA_THROWABLE_FILLINSTACKTRACE);
            addMetaOverridden(JAVA_THROWABLE_GETSTACKTRACEDEPTH,                  ALGO_JAVA_THROWABLE_GETSTACKTRACEDEPTH);
            addMetaOverridden(JAVA_THROWABLE_GETSTACKTRACEELEMENT,                ALGO_JAVA_THROWABLE_GETSTACKTRACEELEMENT);
            addMetaOverridden(JAVA_UNIXFILESYSTEM_INITIDS,                        ALGO_INVOKEMETA_PURE);
            addMetaOverridden(JAVA_WINNTFILESYSTEM_INITIDS,                       ALGO_INVOKEMETA_PURE);
            addMetaOverridden(SUN_NATIVECONSTRUCTORACCESSORIMPL_NEWINSTANCE0,     ALGO_SUN_NATIVECONSTRUCTORACCESSORIMPL_NEWINSTANCE0);
            addMetaOverridden(SUN_REFLECTION_GETCALLERCLASS,                      ALGO_SUN_REFLECTION_GETCALLERCLASS);
            addMetaOverridden(SUN_REFLECTION_GETCLASSACCESSFLAGS,                 ALGO_SUN_REFLECTION_GETCLASSACCESSFLAGS);
            addBaseOverridden(SUN_SIGNAL_FINDSIGNAL,                              BASE_SUN_SIGNAL_FINDSIGNAL);
            addBaseOverridden(SUN_SIGNAL_HANDLE0,                                 BASE_SUN_SIGNAL_HANDLE0);
            addBaseOverridden(SUN_UNSAFE_ADDRESSSIZE,                             BASE_SUN_UNSAFE_ADDRESSSIZE);
            addMetaOverridden(SUN_UNSAFE_ALLOCATEMEMORY,                          ALGO_SUN_UNSAFE_ALLOCATEMEMORY);
            addBaseOverridden(SUN_UNSAFE_ARRAYBASEOFFSET,                         BASE_SUN_UNSAFE_ARRAYBASEOFFSET);
            addBaseOverridden(SUN_UNSAFE_ARRAYINDEXSCALE,                         BASE_SUN_UNSAFE_ARRAYINDEXSCALE);
            addMetaOverridden(SUN_UNSAFE_COMPAREANDSWAPINT,                       ALGO_SUN_UNSAFE_COMPAREANDSWAPINT);
            addMetaOverridden(SUN_UNSAFE_COMPAREANDSWAPLONG,                      ALGO_SUN_UNSAFE_COMPAREANDSWAPLONG);
            addMetaOverridden(SUN_UNSAFE_COMPAREANDSWAPOBJECT,                    ALGO_SUN_UNSAFE_COMPAREANDSWAPOBJECT);
            addMetaOverridden(SUN_UNSAFE_DEFINEANONYMOUSCLASS,                    ALGO_SUN_UNSAFE_DEFINEANONYMOUSCLASS);
            addMetaOverridden(SUN_UNSAFE_ENSURECLASSINITIALIZED,                  ALGO_SUN_UNSAFE_ENSURECLASSINITIALIZED);
            addMetaOverridden(SUN_UNSAFE_FREEMEMORY,                              ALGO_SUN_UNSAFE_FREEMEMORY);
            addMetaOverridden(SUN_UNSAFE_GETBYTE,                                 ALGO_SUN_UNSAFE_GETBYTE);
            addMetaOverridden(SUN_UNSAFE_GETINTVOLATILE,                          ALGO_SUN_UNSAFE_GETINTVOLATILE);
            addMetaOverridden(SUN_UNSAFE_GETLONG,                                 ALGO_SUN_UNSAFE_GETLONG);
            addMetaOverridden(SUN_UNSAFE_GETOBJECTVOLATILE,                       ALGO_SUN_UNSAFE_GETOBJECTVOLATILE);
            addMetaOverridden(SUN_UNSAFE_OBJECTFIELDOFFSET,                       ALGO_SUN_UNSAFE_OBJECTFIELDOFFSET);
            addMetaOverridden(SUN_UNSAFE_PUTLONG,                                 ALGO_SUN_UNSAFE_PUTLONG);
            addMetaOverridden(SUN_UNSAFE_PUTOBJECTVOLATILE,                       ALGO_SUN_UNSAFE_PUTOBJECTVOLATILE);
            addMetaOverridden(SUN_UNSAFE_REGISTERNATIVES,                         ALGO_INVOKEMETA_PURE);
            addMetaOverridden(SUN_UNSAFE_SHOULDBEINITIALIZED,                     ALGO_SUN_UNSAFE_SHOULDBEINITIALIZED);
            addMetaOverridden(SUN_VM_INITIALIZE,                                  ALGO_INVOKEMETA_PURE);

            //jbse.meta.Analysis methods
            addMetaOverridden(JBSE_ANALYSIS_ANY,                       ALGO_JBSE_ANALYSIS_ANY);
            addMetaOverridden(JBSE_ANALYSIS_ASSUMECLASSNOTINITIALIZED, ALGO_JBSE_ANALYSIS_ASSUMECLASSNOTINITIALIZED);
            addMetaOverridden(JBSE_ANALYSIS_ENDGUIDANCE,               ALGO_JBSE_ANALYSIS_ENDGUIDANCE);
            addMetaOverridden(JBSE_ANALYSIS_FAIL,                      ALGO_JBSE_ANALYSIS_FAIL);
            addMetaOverridden(JBSE_ANALYSIS_IGNORE,                    ALGO_JBSE_ANALYSIS_IGNORE);
            addMetaOverridden(JBSE_ANALYSIS_ISRESOLVED,                ALGO_JBSE_ANALYSIS_ISRESOLVED);
            addBaseOverridden(JBSE_ANALYSIS_ISRUNBYJBSE,               BASE_JBSE_ANALYSIS_ISRUNBYJBSE);
            addMetaOverridden(JBSE_ANALYSIS_ISSYMBOLIC_BOOLEAN,        ALGO_JBSE_ANALYSIS_ISSYMBOLIC);
            addMetaOverridden(JBSE_ANALYSIS_ISSYMBOLIC_BYTE,           ALGO_JBSE_ANALYSIS_ISSYMBOLIC);
            addMetaOverridden(JBSE_ANALYSIS_ISSYMBOLIC_CHAR,           ALGO_JBSE_ANALYSIS_ISSYMBOLIC);
            addMetaOverridden(JBSE_ANALYSIS_ISSYMBOLIC_DOUBLE,         ALGO_JBSE_ANALYSIS_ISSYMBOLIC);
            addMetaOverridden(JBSE_ANALYSIS_ISSYMBOLIC_FLOAT,          ALGO_JBSE_ANALYSIS_ISSYMBOLIC);
            addMetaOverridden(JBSE_ANALYSIS_ISSYMBOLIC_INT,            ALGO_JBSE_ANALYSIS_ISSYMBOLIC);
            addMetaOverridden(JBSE_ANALYSIS_ISSYMBOLIC_LONG,           ALGO_JBSE_ANALYSIS_ISSYMBOLIC);
            addMetaOverridden(JBSE_ANALYSIS_ISSYMBOLIC_SHORT,          ALGO_JBSE_ANALYSIS_ISSYMBOLIC);
            addMetaOverridden(JBSE_ANALYSIS_SUCCEED,                   ALGO_JBSE_ANALYSIS_SUCCEED);
            addMetaOverridden(JBSE_ANALYSIS_SYMBOLNAME_BOOLEAN,        ALGO_JBSE_ANALYSIS_SYMBOLNAME);
            addMetaOverridden(JBSE_ANALYSIS_SYMBOLNAME_BYTE,           ALGO_JBSE_ANALYSIS_SYMBOLNAME);
            addMetaOverridden(JBSE_ANALYSIS_SYMBOLNAME_CHAR,           ALGO_JBSE_ANALYSIS_SYMBOLNAME);
            addMetaOverridden(JBSE_ANALYSIS_SYMBOLNAME_DOUBLE,         ALGO_JBSE_ANALYSIS_SYMBOLNAME);
            addMetaOverridden(JBSE_ANALYSIS_SYMBOLNAME_FLOAT,          ALGO_JBSE_ANALYSIS_SYMBOLNAME);
            addMetaOverridden(JBSE_ANALYSIS_SYMBOLNAME_INT,            ALGO_JBSE_ANALYSIS_SYMBOLNAME);
            addMetaOverridden(JBSE_ANALYSIS_SYMBOLNAME_LONG,           ALGO_JBSE_ANALYSIS_SYMBOLNAME);
            addMetaOverridden(JBSE_ANALYSIS_SYMBOLNAME_SHORT,          ALGO_JBSE_ANALYSIS_SYMBOLNAME);
            
            //jbse classless (pseudo)methods
            addMetaOverridden(noclass_REGISTERMETHODTYPE,           ALGO_noclass_REGISTERMETHODTYPE);
            addMetaOverridden(noclass_STORELINKEDMETHODANDAPPENDIX, ALGO_noclass_STORELINKEDMETHODANDAPPENDIX);
        } catch (MetaUnsupportedException e) {
            throw new UnexpectedInternalException(e);
        }
    }

    /**
     * Sets the initial state. To be invoked whenever 
     * the engine parameters object provided through the 
     * constructor does not have an initial state.
     * 
     * @param initialState a {@link State}. The method
     *        stores in this execution contest a safety 
     *        copy of it.
     */
    public void setInitialState(State initialState) {
        this.initialState = initialState.clone();
    }

    /**
     * Returns the initial state.
     * 
     * @return a {@link State}, a clone of the initial state
     *         of the symbolic execution.
     */
    public State getInitialState() {
        return (this.initialState == null ? null : this.initialState.clone());
    }

    /**
     * Allows to customize the behavior of the invocations to a method 
     * by specifying another method that implements it.
     * 
     * @param methodSignature the {@link Signature} of a method.
     * @param delegateMethodSignature the {@link Signature} of another method
     *        that will be executed in place of the method with signature
     *        {@code methodSignature}.
     */
    public void addBaseOverridden(Signature methodSignature, Signature delegateMethodSignature) {
        this.baseOverrides.put(methodSignature, delegateMethodSignature);
    }

    /**
     * Determines whether a method has a base-level overriding implementation.
     * 
     * @param methodSignature the {@link Signature} of a method.
     * @return {@code true} iff an overriding base-level method for it was added
     *         by invoking {@link #addBaseOverridden(Signature, Signature)}.
     */
    public boolean isMethodBaseLevelOverridden(Signature methodSignature) {
        return this.baseOverrides.containsKey(methodSignature);
    }

    /**
     * Returns the signature of a base-level override implementation 
     * of a method. 
     * 
     * @param methodSignature the {@link Signature} of a method.
     * @return  the {@link Signature} of the method that overrides
     *          the one with signature {@code methodSignature} and
     *          that was previously set by invoking {@link #addBaseOverridden(Signature, Signature)}.
     */
    public Signature getBaseOverride(Signature methodSignature) {
        return this.baseOverrides.get(methodSignature);
    }

    /**
     * Allows to customize the behavior of the invocations to a method 
     * by specifying an {@link Algorithm} that implements its semantics.
     * 
     * @param methodSignature the {@link Signature} of a method. 
     * @param metaDelegateClassName a class name as a {@link String}, 
     *        indicating a class (that must be in the meta-level classpath, 
     *        must have a default constructor, must implement {@link Algorithm})
     *        of an algorithm that implements at the meta-level the 
     *        semantics of the invocations to the method with signature 
     *        {@code methodSignature}. 
     * @throws MetaUnsupportedException if the class indicated in 
     *         {@code metaDelegateClassName} does not exist, or cannot be loaded 
     *         or instantiated for any reason (misses from the meta-level classpath, 
     *         has insufficient visibility, does not extend {@link Algorithm}...).
     */
    public void addMetaOverridden(Signature methodSignature, String metaDelegateClassName) 
    throws MetaUnsupportedException {
        try {
            @SuppressWarnings("unchecked")
            final Class<? extends Algo_INVOKEMETA<?, ?, ?, ?>> metaDelegateClass = 
                (Class<? extends Algo_INVOKEMETA<?, ?, ?, ?>>) 
                ClassLoader.getSystemClassLoader().loadClass(binaryClassName(metaDelegateClassName)).asSubclass(Algo_INVOKEMETA.class);
            this.dispatcherMeta.loadAlgoMetaOverridden(methodSignature, metaDelegateClass);
        } catch (ClassNotFoundException e) {
            throw new MetaUnsupportedException("meta-level implementation class " + metaDelegateClassName + " not found");
        } catch (ClassCastException e) {
            throw new MetaUnsupportedException("meta-level implementation class " + metaDelegateClassName + " does not implement " + Algorithm.class);
        }
    }

    /**
     * Allows to customize the behavior of the invocations to a method 
     * by treating all the invocations of a given method as returning 
     * the application of an uninterpreted symbolic function
     * with no side effect.
     * 
     * @param methodSignature the {@link Signature} of a method. 
     * @param functionName the name of the uninterpreted symbolic function
     *        whose application to the invocation parameter is 
     *        the result of all the invocations to {@code className.methodName}.
     */
    public void addUninterpreted(Signature methodSignature, String functionName) { 
        this.dispatcherMeta.loadAlgoUninterpreted(methodSignature, functionName);
    }

    /**
     * Determines whether a class has a pure static initializer, where with
     * "pure" we mean that its effect is independent on when the initializer
     * is executed.
     * 
     * @param classHierarchy a {@link ClassHierarchy}.
     * @param className the name of the class.
     * @return {@code true} iff the class has a pure static initializer.
     */
    public boolean hasClassAPureInitializer(ClassHierarchy hier, String className) {
        return 
        (
        className.equals(ARITHMETIC_EXCEPTION) ||
        className.equals(ARRAY_STORE_EXCEPTION) ||
        className.equals(CLASS_CAST_EXCEPTION) ||
        className.equals(ERROR) ||
        className.equals(ILLEGAL_ARGUMENT_EXCEPTION) ||
        className.equals(ILLEGAL_MONITOR_STATE_EXCEPTION) ||
        className.equals(JAVA_ABSTRACTCOLLECTION) ||
        className.equals(JAVA_ABSTRACTLIST) ||
        className.equals(JAVA_ABSTRACTMAP) ||
        className.equals(JAVA_ABSTRACTSET) ||
        className.equals(JAVA_ABSTRACTSTRINGBUILDER) ||
        className.equals(JAVA_ACCESSCONTROLCONTEXT) || //not really, but its static fields are lazily initialized so it is as it were
        className.equals(JAVA_ACCESSCONTROLLER) || 
        className.equals(JAVA_ACCESSIBLEOBJECT) || 
        className.equals(JAVA_ARRAYLIST) || 
        className.equals(JAVA_ARRAYS) || 
        className.equals(JAVA_ATOMICINTEGER) || 
        className.equals(JAVA_ATOMICLONG) || 
        className.equals(JAVA_ATOMICREFERENCEFIELDUPDATER) || 
        className.equals(JAVA_ATOMICREFERENCEFIELDUPDATER_IMPL) || 
        className.equals(JAVA_ATOMICREFERENCEFIELDUPDATER_IMPL_1) || 
        className.equals(JAVA_BASICPERMISSION) ||
        className.equals(JAVA_BITS) || //not for maxMemory and memoryLimitSet, all the others seem to be ok (final or lazily intialized)
        className.equals(JAVA_BITS_1) ||
        className.equals(JAVA_BOOLEAN) ||
        className.equals(JAVA_BOUNDMETHODHANDLE) || //necessary for method handles
        className.equals(JAVA_BOUNDMETHODHANDLE_SPECIESDATA) || //necessary for method handles
        className.equals(JAVA_BOUNDMETHODHANDLE_SPECIES_L) || //necessary for method handles
        className.equals(JAVA_BUFFER) ||
        className.equals(JAVA_BUFFEREDINPUTSTREAM) ||
        className.equals(JAVA_BUFFEREDOUTPUTSTREAM) ||
        className.equals(JAVA_BUFFEREDWRITER) ||
        className.equals(JAVA_BYTE) ||
        className.equals(JAVA_BYTE_BYTECACHE) ||
        className.equals(JAVA_BYTEBUFFER) ||
        className.equals(JAVA_BYTEORDER) ||
        className.equals(JAVA_CHARACTER) ||
        className.equals(JAVA_CHARACTER_CHARACTERCACHE) ||
        className.equals(JAVA_CHARSET) || //not really, but most static values seem to be just caches, so we treat it as it were
        className.equals(JAVA_CHARSET_EXTENDEDPROVIDERHOLDER) ||
        className.equals(JAVA_CHARSETENCODER) || //one field is not final, but it is never reassigned
        className.equals(JAVA_CHARSETPROVIDER) ||
        className.equals(JAVA_CLASS) || 
        className.equals(JAVA_CLASS_1) || 
        className.equals(JAVA_CLASS_3) || 
        className.equals(JAVA_CLASS_ATOMIC) || 
        className.equals(JAVA_CLASS_REFLECTIONDATA) || 
        className.equals(JAVA_CLASSLOADER) || //not really, but static fields are either lazily initialized and not modified, or are not modified; plus the collections of loaded libraries etc. will be assumed to be empty at the start of symbolic execution 
        className.equals(JAVA_CLASSLOADER_NATIVELIBRARY) || 
        className.equals(JAVA_CLASSLOADER_PARALLELLOADERS) || //necessary for initialization (although it isn't) 
        className.equals(JAVA_CLASSVALUE) || //not really, but the only critical field is nextHashCode, that is used to generate hash codes for the class instances that are later used in internal table, so it is as it were 
        className.equals(JAVA_CODESOURCE) ||
        className.equals(JAVA_CODINGERRORACTION) ||
        className.equals(JAVA_COLLECTIONS) ||
        className.equals(JAVA_COLLECTIONS_EMPTYLIST) ||
        className.equals(JAVA_COLLECTIONS_EMPTYMAP) ||
        className.equals(JAVA_COLLECTIONS_EMPTYSET) ||
        className.equals(JAVA_COLLECTIONS_SETFROMMAP) || 
        className.equals(JAVA_COLLECTIONS_SYNCHRONIZEDCOLLECTION) || 
        className.equals(JAVA_COLLECTIONS_SYNCHRONIZEDSET) ||
        className.equals(JAVA_COLLECTIONS_UNMODIFIABLECOLLECTION) ||
        className.equals(JAVA_COLLECTIONS_UNMODIFIABLELIST) ||
        className.equals(JAVA_COLLECTIONS_UNMODIFIABLERANDOMACCESSLIST) ||
        className.equals(JAVA_CONCURRENTHASHMAP) || //only RESIZE_STAMP_BITS is not final, but it is never modified
        className.equals(JAVA_CONSTRUCTOR) ||
        className.equals(JAVA_DEFAULTFILESYSTEM) ||
        className.equals(JAVA_DICTIONARY) ||
        className.equals(JAVA_DIRECTMETHODHANDLE) || //wouldn't manage method handles otherwise
        className.equals(JAVA_DOUBLE) ||
        className.equals(JAVA_EXCEPTION) ||
        className.equals(JAVA_EXECUTABLE) ||
        className.equals(JAVA_EXPIRINGCACHE) ||
        className.equals(JAVA_EXPIRINGCACHE_1) ||
        className.equals(JAVA_FIELD) ||
        className.equals(JAVA_FILE) || 
        className.equals(JAVA_FILEDESCRIPTOR) || 
        className.equals(JAVA_FILEDESCRIPTOR_1) || 
        className.equals(JAVA_FILEINPUTSTREAM) || 
        className.equals(JAVA_FILEOUTPUTSTREAM) || 
        className.equals(JAVA_FILESYSTEM) || // the useCanonCaches and useCanonPrefixCache are not final but are never overwritten after initialization (note that they are not private)
        className.equals(JAVA_FILTERINPUTSTREAM) || 
        className.equals(JAVA_FILTEROUTPUTSTREAM) || 
        className.equals(JAVA_FINALIZER) || 
        className.equals(JAVA_FINALIZER_FINALIZERTHREAD) || //not at all, it statically initializes a single finalizer thread (being JBSE single-threaded it is as it had no effect) 
        className.equals(JAVA_FINALREFERENCE) || 
        className.equals(JAVA_FLOAT) || 
        className.equals(JAVA_HASHMAP) || 
        className.equals(JAVA_HASHMAP_NODE) || 
        className.equals(JAVA_HASHSET) || 
        className.equals(JAVA_HASHTABLE) || 
        className.equals(JAVA_HASHTABLE_ENTRY) || 
        className.equals(JAVA_HASHTABLE_ENTRYSET) ||
        className.equals(JAVA_HASHTABLE_ENUMERATOR) || 
        className.equals(JAVA_HEAPBYTEBUFFER) || 
        className.equals(JAVA_IDENTITYHASHMAP) || 
        className.equals(JAVA_INPUTSTREAM) ||
        className.equals(JAVA_INTEGER) || 
        className.equals(JAVA_INTEGER_INTEGERCACHE) || 
        className.equals(JAVA_INTERRUPTEDEXCEPTION) || 
        className.equals(JAVA_INVOKERBYTECODEGENERATOR) || //the only nonfinal static field STATICALLY_INVOCABLE_PACKAGES is never modified
        className.equals(JAVA_INVOKERBYTECODEGENERATOR_2) ||
        className.equals(JAVA_LAMBDAFORM) || 
        className.equals(JAVA_LAMBDAFORM_NAME) || 
        className.equals(JAVA_LINKEDHASHMAP) || 
        className.equals(JAVA_LINKEDLIST) || 
        className.equals(JAVA_LINKEDLIST_ENTRY) ||
        className.equals(JAVA_LONG) || 
        className.equals(JAVA_LONG_LONGCACHE) || 
        className.equals(JAVA_MATH) || 
        className.equals(JAVA_MEMBERNAME) || 
        className.equals(JAVA_MEMBERNAME_FACTORY) || //its only mutable static member ALLOWED_FLAGS is never changed
        className.equals(JAVA_METHOD) ||
        className.equals(JAVA_METHODHANDLE) ||
        className.equals(JAVA_METHODHANDLEIMPL) || //the critical members (TYPED_COLLECTORS, FILL_ARRAY_TO_RIGHT and FAKE_METHOD_HANDLE_INVOKE) are lazily initialized caches
        className.equals(JAVA_METHODHANDLENATIVES) || 
        className.equals(JAVA_METHODHANDLES) || //not really, but can be considered as it were (all final except ZERO_MHS and IDENTITY_MHS that are caches) 
        className.equals(JAVA_METHODHANDLES_LOOKUP) || //not really, but can be considered as it were (all final including PUBLIC_LOOKUP and IMPL_LOOKUP that are instances of Lookup - that is immutable - and except LOOKASIDE_TABLE, that seems to be a sort of cache) 
        className.equals(JAVA_METHODHANDLESTATICS) || 
        className.equals(JAVA_METHODTYPE) || //not really, but can be considered as it were (all final except internTable and objectOnlyTypes that are caches) 
        className.equals(JAVA_METHODTYPEFORM) || 
        className.equals(JAVA_MODIFIER) || //not really, but used as it were (it bootstraps sun.misc.SharedSecrets on init)
        className.equals(JAVA_NUMBER) || 
        className.equals(JAVA_OBJECT) ||
        className.equals(JAVA_OBJECTS) ||
        className.equals(JAVA_OUTPUTSTREAM) ||
        className.equals(JAVA_OUTPUTSTREAMWRITER) ||
        className.equals(JAVA_PERMISSION) ||
        className.equals(JAVA_PHANTOMREFERENCE) ||
        className.equals(JAVA_PRINTSTREAM) ||
        className.equals(JAVA_PROPERTIES) ||
        className.equals(JAVA_PROTECTIONDOMAIN) || //its static initializer sets up stuff in SharedSecrets
        className.equals(JAVA_PROTECTIONDOMAIN_2) ||
        className.equals(JAVA_PROTECTIONDOMAIN_JAVASECURITYACCESSIMPL) ||
        className.equals(JAVA_PROTECTIONDOMAIN_KEY) ||
        className.equals(JAVA_REFERENCE) ||  //not really, but the lock field is effectively final and the discovered field is managed by the garbage collector, that JBSE has not
        className.equals(JAVA_REFERENCE_1) ||
        className.equals(JAVA_REFERENCE_LOCK) ||
        className.equals(JAVA_REFERENCE_REFERENCEHANDLER) ||
        className.equals(JAVA_REFERENCEQUEUE) ||  //not really, but used as it were
        className.equals(JAVA_REFERENCEQUEUE_LOCK) ||
        className.equals(JAVA_REFERENCEQUEUE_NULL) ||
        className.equals(JAVA_REFLECTACCESS) ||
        className.equals(JAVA_REFLECTPERMISSION) ||
        className.equals(JAVA_RUNTIME) ||
        className.equals(JAVA_RUNTIMEEXCEPTION) ||
        className.equals(JAVA_RUNTIMEPERMISSION) ||
        className.equals(JAVA_SECURECLASSLOADER) || //its static initializer registers this class as a parallel capable class loader
        className.equals(JAVA_SHORT) || 
        className.equals(JAVA_SHORT_SHORTCACHE) || 
        className.equals(JAVA_SIMPLEMETHODHANDLE) || //necessary for method handles
        className.equals(JAVA_STACK) || 
        className.equals(JAVA_STRING) || 
        className.equals(JAVA_STRING_CASEINSCOMP) ||
        className.equals(JAVA_STRINGBUILDER) || 
        className.equals(JAVA_SYSTEM) || 
        className.equals(JAVA_SYSTEM_2) || 
        className.equals(JAVA_TERMINATOR) || //not really, but its only static field is private and lazily initialized
        className.equals(JAVA_TERMINATOR_1) ||
        className.equals(JAVA_TREESET) ||
        className.equals(JAVA_THREAD) || //not really, but it's not a terrible approximation considering it as it were (and also initializes many static final members)
        className.equals(JAVA_THREADGROUP) ||
        className.equals(JAVA_THREADLOCAL) || //not really, but the only static member generates sequences of hash codes, so it can be treated as it were
        className.equals(JAVA_THROWABLE) || 
        className.equals(JAVA_THROWABLE_SENTINELHOLDER) ||
        className.equals(JAVA_UNIXFILESYSTEM) || 
        className.equals(JAVA_URLCLASSLOADER) || //its static initializer registers stuff into SharedSecrets
        className.equals(JAVA_URLCLASSLOADER_7) ||
        className.equals(JAVA_URLSTREAMHANDLER) || //it isn't; necessary to init SharedSecrets
        className.equals(JAVA_VECTOR) || 
        className.equals(JAVA_VOID) || 
        className.equals(JAVA_WEAKHASHMAP) ||
        className.equals(JAVA_WEAKHASHMAP_ENTRY) ||
        className.equals(JAVA_WEAKHASHMAP_KEYSET) ||
        className.equals(JAVA_WEAKREFERENCE) ||
        className.equals(JAVA_WINNTFILESYSTEM) || //not really, but the only static member driveDirCache is a cache
        className.equals(JAVA_WRITER) || 
        className.equals(JBSE_BASE) ||
        className.equals(NULL_POINTER_EXCEPTION) ||
        className.equals(OUT_OF_MEMORY_ERROR) ||
        className.equals(STACK_OVERFLOW_ERROR) ||
        className.equals(SUN_CLEANER) ||  //not really, but we don't care very much about finalization since JBSE never garbage-collects, thus we treat it as it were
        className.equals(SUN_CONSTRUCTORACCESSORIMPL) ||        
        className.equals(SUN_DEBUG) || //its static nonfinal member args is effectively final        
        className.equals(SUN_DELEGATINGCONSTRUCTORACCESSORIMPL) ||        
        className.equals(SUN_FASTCHARSETPROVIDER) ||        
        className.equals(SUN_GETPROPERTYACTION) ||        
        className.equals(SUN_HANDLER) ||        
        className.equals(SUN_LAUNCHER) || //necessary to JVM bootstrap
        className.equals(SUN_LAUNCHERHELPER) || //necessary to JVM bootstrap (is it really?)
        className.equals(SUN_LAUNCHER_EXTCLASSLOADER) || //its static initializer registers this class as a parallel capable class loader
        className.equals(SUN_LAUNCHER_EXTCLASSLOADER_1) ||
        className.equals(SUN_LAUNCHER_FACTORY) || //not really, but its only static field is effectively final
        className.equals(SUN_MAGICACCESSORIMPL) ||
        className.equals(SUN_NATIVECONSTRUCTORACCESSORIMPL) ||
        className.equals(SUN_NATIVESIGNALHANDLER) ||
        className.equals(SUN_OSENVIRONMENT) ||
        className.equals(SUN_PREHASHEDMAP) ||
        className.equals(SUN_REFLECTION) ||  //not really, but at a first approximation we consider it as it were (it is loaded by System bootstrapping on init)
        className.equals(SUN_REFLECTIONFACTORY) ||  //not really, but at a first approximation we consider it as it were (it is loaded by System bootstrapping on init)
        className.equals(SUN_REFLECTIONFACTORY_1) ||
        className.equals(SUN_REFLECTIONFACTORY_GETREFLECTIONFACTORYACTION) ||
        className.equals(SUN_REFLECTUTIL) ||
        className.equals(SUN_SHAREDSECRETS) ||  //not really, but at a first approximation we consider it as it were (it is loaded by System bootstrapping on init); its members are initialized by many other classes, so beware
        className.equals(SUN_SIGNAL) || //not really, but we will assume it is; its static member are not final but are treated as if they were 
        className.equals(SUN_SIGNALHANDLER) || 
        className.equals(SUN_STANDARDCHARSETS) ||
        className.equals(SUN_STANDARDCHARSETS_ALIASES) ||
        className.equals(SUN_STANDARDCHARSETS_CACHE) ||
        className.equals(SUN_STANDARDCHARSETS_CLASSES) ||
        className.equals(SUN_STREAMENCODER) ||
        className.equals(SUN_UNICODE) ||
        className.equals(SUN_UNSAFE) ||
        className.equals(SUN_URLCLASSPATH) || //necessary to JVM bootstrap
        className.equals(SUN_UTF_8) ||
        className.equals(SUN_UTF_8_ENCODER) ||
        className.equals(SUN_VERIFYACCESS) ||
        className.equals(SUN_VERSION) ||
        className.equals(SUN_VM) ||
        className.equals(SUN_WRAPPER_FORMAT) ||
        className.equals(VIRTUAL_MACHINE_ERROR) ||
        hier.isSubclass(className, JAVA_ENUM));
    }

    public <R extends DecisionAlternative> 
    SortedSet<R> mkDecisionResultSet(Class<R> superclassDecisionAlternatives) {
        final Comparator<R> comparator = this.comparators.get(superclassDecisionAlternatives);
        final TreeSet<R> retVal = new TreeSet<>(comparator);
        return retVal;
    }
    
    public void setMainThreadGroup(ReferenceConcrete mainThreadGroup) {
        this.mainThreadGroup = mainThreadGroup;
    }
    
    public ReferenceConcrete getMainThreadGroup() {
        return this.mainThreadGroup;
    }
    
    public void setMainThread(ReferenceConcrete mainThread) {
        this.mainThread = mainThread;
    }
    
    public ReferenceConcrete getMainThread() {
        return this.mainThread;
    }
}
