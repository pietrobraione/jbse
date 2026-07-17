package jbse.algo;

import static jbse.bc.Signatures.JAVA_ACCESSCONTROLCONTEXT;
import static jbse.bc.Signatures.JAVA_CLASS;
import static jbse.bc.Signatures.JAVA_CLASSLOADER;
import static jbse.bc.Signatures.JAVA_FIELD;
import static jbse.bc.Signatures.JAVA_OBJECT;
import static jbse.bc.Signatures.JAVA_PRIVILEGEDACTION;
import static jbse.bc.Signatures.JAVA_PRIVILEGEDEXCEPTIONACTION;
import static jbse.bc.Signatures.JAVA_PROPERTIES;
import static jbse.bc.Signatures.JAVA_RUNTIME;
import static jbse.bc.Signatures.JAVA_STRING;
import static jbse.bc.Signatures.JAVA_THREAD;
import static jbse.bc.Signatures.JAVA_URL;
import static jbse.bc.Signatures.SUN_UNSAFE;
import static jbse.common.Type.ARRAYOF;
import static jbse.common.Type.BOOLEAN;
import static jbse.common.Type.INT;
import static jbse.common.Type.binaryToInternalClassName;
import static jbse.common.Type.LONG;
import static jbse.common.Type.REFERENCE;
import static jbse.common.Type.TYPEEND;
import static jbse.common.Type.VOID;

import jbse.bc.Signature;

/**
 * This class defines class names and method signatures used as overriding
 * implementations.
 * 
 * @author Pietro Braione
 *
 */
public final class Overrides {
    public static final String ALGO_INVOKEMETA_METACIRCULAR                        = binaryToInternalClassName(jbse.algo.Algo_INVOKEMETA_Metacircular.class.getName());
    
    //Overriding meta-level implementations of standard methods
    public static final String ALGO_JAVA_CLASS_FORNAME0                            = binaryToInternalClassName(jbse.algo.meta.Algo_JAVA_CLASS_FORNAME0.class.getName());
    public static final String ALGO_JAVA_CLASS_GETCOMPONENTTYPE                    = binaryToInternalClassName(jbse.algo.meta.Algo_JAVA_CLASS_GETCOMPONENTTYPE.class.getName());
    public static final String ALGO_JAVA_CLASS_GETCONSTANTPOOL                     = binaryToInternalClassName(jbse.algo.meta.Algo_JAVA_CLASS_GETCONSTANTPOOL.class.getName());
    public static final String ALGO_JAVA_CLASS_GETDECLAREDCONSTRUCTORS0            = binaryToInternalClassName(jbse.algo.meta.Algo_JAVA_CLASS_GETDECLAREDCONSTRUCTORS0.class.getName());
    public static final String ALGO_JAVA_CLASS_GETDECLAREDFIELDS0                  = binaryToInternalClassName(jbse.algo.meta.Algo_JAVA_CLASS_GETDECLAREDFIELDS0.class.getName());
    public static final String ALGO_JAVA_CLASS_GETDECLAREDMETHODS0                 = binaryToInternalClassName(jbse.algo.meta.Algo_JAVA_CLASS_GETDECLAREDMETHODS0.class.getName());
    public static final String ALGO_JAVA_CLASS_GETDECLARINGCLASS0                  = binaryToInternalClassName(jbse.algo.meta.Algo_JAVA_CLASS_GETDECLARINGCLASS0.class.getName());
    public static final String ALGO_JAVA_CLASS_GETENCLOSINGMETHOD0                 = binaryToInternalClassName(jbse.algo.meta.Algo_JAVA_CLASS_GETENCLOSINGMETHOD0.class.getName());
    public static final String ALGO_JAVA_CLASS_GETGENERICSIGNATURE0                = binaryToInternalClassName(jbse.algo.meta.Algo_JAVA_CLASS_GETGENERICSIGNATURE0.class.getName());
    public static final String ALGO_JAVA_CLASS_GETINTERFACES0                      = binaryToInternalClassName(jbse.algo.meta.Algo_JAVA_CLASS_GETINTERFACES0.class.getName());
    public static final String ALGO_JAVA_CLASS_GETMODIFIERS                        = binaryToInternalClassName(jbse.algo.meta.Algo_JAVA_CLASS_GETMODIFIERS.class.getName());
    public static final String ALGO_JAVA_CLASS_GETNAME0                            = binaryToInternalClassName(jbse.algo.meta.Algo_JAVA_CLASS_GETNAME0.class.getName());
    public static final String ALGO_JAVA_CLASS_GETPRIMITIVECLASS                   = binaryToInternalClassName(jbse.algo.meta.Algo_JAVA_CLASS_GETPRIMITIVECLASS.class.getName());
    public static final String ALGO_JAVA_CLASS_GETPROTECTIONDOMAIN0                = binaryToInternalClassName(jbse.algo.meta.Algo_JAVA_CLASS_GETPROTECTIONDOMAIN0.class.getName());
    public static final String ALGO_JAVA_CLASS_GETRAWANNOTATIONS                   = binaryToInternalClassName(jbse.algo.meta.Algo_JAVA_CLASS_GETRAWANNOTATIONS.class.getName());
    public static final String ALGO_JAVA_CLASS_GETSIGNERS                          = binaryToInternalClassName(jbse.algo.meta.Algo_JAVA_CLASS_GETSIGNERS.class.getName());
    public static final String ALGO_JAVA_CLASS_GETSUPERCLASS                       = binaryToInternalClassName(jbse.algo.meta.Algo_JAVA_CLASS_GETSUPERCLASS.class.getName());
    public static final String ALGO_JAVA_CLASS_ISARRAY                             = binaryToInternalClassName(jbse.algo.meta.Algo_JAVA_CLASS_ISARRAY.class.getName());
    public static final String ALGO_JAVA_CLASS_ISASSIGNABLEFROM                    = binaryToInternalClassName(jbse.algo.meta.Algo_JAVA_CLASS_ISASSIGNABLEFROM.class.getName());
    public static final String ALGO_JAVA_CLASS_ISINSTANCE                          = binaryToInternalClassName(jbse.algo.meta.Algo_JAVA_CLASS_ISINSTANCE.class.getName());
    public static final String ALGO_JAVA_CLASS_ISINTERFACE                         = binaryToInternalClassName(jbse.algo.meta.Algo_JAVA_CLASS_ISINTERFACE.class.getName());
    public static final String ALGO_JAVA_CLASS_ISPRIMITIVE                         = binaryToInternalClassName(jbse.algo.meta.Algo_JAVA_CLASS_ISPRIMITIVE.class.getName());
    public static final String ALGO_JAVA_CLASS_SETSIGNERS                          = binaryToInternalClassName(jbse.algo.meta.Algo_JAVA_CLASS_SETSIGNERS.class.getName());
    public static final String ALGO_JAVA_CLASSLOADER_DEFINECLASS1                  = binaryToInternalClassName(jbse.algo.meta.Algo_JAVA_CLASSLOADER_DEFINECLASS1.class.getName());
    public static final String ALGO_JAVA_CLASSLOADER_FINDBOOTSTRAPCLASS            = binaryToInternalClassName(jbse.algo.meta.Algo_JAVA_CLASSLOADER_FINDBOOTSTRAPCLASS.class.getName());
    public static final String ALGO_JAVA_CLASSLOADER_FINDLOADEDCLASS0              = binaryToInternalClassName(jbse.algo.meta.Algo_JAVA_CLASSLOADER_FINDLOADEDCLASS0.class.getName());
    public static final String ALGO_JAVA_CLASSLOADER_NATIVELIBRARY_LOAD            = binaryToInternalClassName(jbse.algo.meta.Algo_JAVA_CLASSLOADER_NATIVELIBRARY_LOAD.class.getName());
    public static final String ALGO_JAVA_CRC32_UPDATEBYTES                         = binaryToInternalClassName(jbse.algo.meta.Algo_JAVA_CRC32_UPDATEBYTES.class.getName());
    public static final String ALGO_JAVA_EXECUTABLE_GETPARAMETERS0                 = binaryToInternalClassName(jbse.algo.meta.Algo_JAVA_EXECUTABLE_GETPARAMETERS0.class.getName());
    public static final String ALGO_JAVA_FILEINPUTSTREAM_AVAILABLE                 = binaryToInternalClassName(jbse.algo.meta.Algo_JAVA_FILEINPUTSTREAM_AVAILABLE.class.getName());
    public static final String ALGO_JAVA_FILEINPUTSTREAM_CLOSE0                    = binaryToInternalClassName(jbse.algo.meta.Algo_JAVA_FILEINPUTSTREAM_CLOSE0.class.getName());
    public static final String ALGO_JAVA_FILEINPUTSTREAM_OPEN0                     = binaryToInternalClassName(jbse.algo.meta.Algo_JAVA_FILEINPUTSTREAM_OPEN0.class.getName());
    public static final String ALGO_JAVA_FILEINPUTSTREAM_READBYTES                 = binaryToInternalClassName(jbse.algo.meta.Algo_JAVA_FILEINPUTSTREAM_READBYTES.class.getName());
    public static final String ALGO_JAVA_FILEOUTPUTSTREAM_OPEN0                    = binaryToInternalClassName(jbse.algo.meta.Algo_JAVA_FILEOUTPUTSTREAM_OPEN0.class.getName());
    public static final String ALGO_JAVA_FILEOUTPUTSTREAM_WRITEBYTES               = binaryToInternalClassName(jbse.algo.meta.Algo_JAVA_FILEOUTPUTSTREAM_WRITEBYTES.class.getName());
    public static final String ALGO_JAVA_INFLATER_END                              = binaryToInternalClassName(jbse.algo.meta.Algo_JAVA_INFLATER_END.class.getName());
    public static final String ALGO_JAVA_INFLATER_GETADLER                         = binaryToInternalClassName(jbse.algo.meta.Algo_JAVA_INFLATER_GETADLER.class.getName());
    public static final String ALGO_JAVA_INFLATER_INFLATEBYTES                     = binaryToInternalClassName(jbse.algo.meta.Algo_JAVA_INFLATER_INFLATEBYTES.class.getName());
    public static final String ALGO_JAVA_INFLATER_INIT                             = binaryToInternalClassName(jbse.algo.meta.Algo_JAVA_INFLATER_INIT.class.getName());
    public static final String ALGO_JAVA_INFLATER_RESET                            = binaryToInternalClassName(jbse.algo.meta.Algo_JAVA_INFLATER_RESET.class.getName());
    public static final String ALGO_JAVA_INFLATER_SETDICTIONARY                    = binaryToInternalClassName(jbse.algo.meta.Algo_JAVA_INFLATER_SETDICTIONARY.class.getName());
    public static final String ALGO_JAVA_JARFILE_GETMETAINFENTRYNAMES              = binaryToInternalClassName(jbse.algo.meta.Algo_JAVA_JARFILE_GETMETAINFENTRYNAMES.class.getName());
    public static final String ALGO_JAVA_METHODHANDLENATIVES_GETMEMBERS            = binaryToInternalClassName(jbse.algo.meta.Algo_JAVA_METHODHANDLENATIVES_GETMEMBERS.class.getName());
    public static final String ALGO_JAVA_METHODHANDLENATIVES_INIT                  = binaryToInternalClassName(jbse.algo.meta.Algo_JAVA_METHODHANDLENATIVES_INIT.class.getName());
    public static final String ALGO_JAVA_METHODHANDLENATIVES_OBJECTFIELDOFFSET     = binaryToInternalClassName(jbse.algo.meta.Algo_JAVA_METHODHANDLENATIVES_OBJECTFIELDOFFSET.class.getName());
    public static final String ALGO_JAVA_METHODHANDLENATIVES_RESOLVE               = binaryToInternalClassName(jbse.algo.meta.Algo_JAVA_METHODHANDLENATIVES_RESOLVE.class.getName());
    public static final String ALGO_JAVA_METHODHANDLENATIVES_SETCALLSITETARGETNORMAL = binaryToInternalClassName(jbse.algo.meta.Algo_JAVA_METHODHANDLENATIVES_SETCALLSITETARGETNORMAL.class.getName());
    public static final String ALGO_JAVA_METHODHANDLENATIVES_SETCALLSITETARGETVOLATILE = binaryToInternalClassName(jbse.algo.meta.Algo_JAVA_METHODHANDLENATIVES_SETCALLSITETARGETVOLATILE.class.getName());
    public static final String ALGO_JAVA_METHODHANDLENATIVES_STATICFIELDOFFSET     = binaryToInternalClassName(jbse.algo.meta.Algo_JAVA_METHODHANDLENATIVES_STATICFIELDOFFSET.class.getName());
    public static final String ALGO_JAVA_OBJECT_CLONE                              = binaryToInternalClassName(jbse.algo.meta.Algo_JAVA_OBJECT_CLONE.class.getName());
    public static final String ALGO_JAVA_OBJECT_GETCLASS                           = binaryToInternalClassName(jbse.algo.meta.Algo_JAVA_OBJECT_GETCLASS.class.getName());
    public static final String ALGO_JAVA_OBJECT_HASHCODE                           = binaryToInternalClassName(jbse.algo.meta.Algo_JAVA_OBJECT_HASHCODE.class.getName());
    public static final String ALGO_JAVA_PACKAGE_GETSYSTEMPACKAGE0                 = binaryToInternalClassName(jbse.algo.meta.Algo_JAVA_PACKAGE_GETSYSTEMPACKAGE0.class.getName());
    public static final String ALGO_JAVA_PROCESSENVIRONMENT_ENVIRON                = binaryToInternalClassName(jbse.algo.meta.Algo_JAVA_PROCESSENVIRONMENT_ENVIRON.class.getName());
    public static final String ALGO_JAVA_RANDOMACCESSFILE_OPEN0                    = binaryToInternalClassName(jbse.algo.meta.Algo_JAVA_RANDOMACCESSFILE_OPEN0.class.getName());
    public static final String ALGO_JAVA_REFLECT_ARRAY_NEWARRAY                    = binaryToInternalClassName(jbse.algo.meta.Algo_JAVA_REFLECT_ARRAY_NEWARRAY.class.getName());
    public static final String ALGO_JAVA_STRING_HASHCODE                           = binaryToInternalClassName(jbse.algo.meta.Algo_JAVA_STRING_HASHCODE.class.getName());
    public static final String ALGO_JAVA_STRING_INTERN                             = binaryToInternalClassName(jbse.algo.meta.Algo_JAVA_STRING_INTERN.class.getName());
    public static final String ALGO_JAVA_STRINGBUILDER_APPEND                      = binaryToInternalClassName(jbse.algo.meta.Algo_JAVA_STRINGBUILDER_APPEND.class.getName());
    public static final String ALGO_JAVA_SYSTEM_ARRAYCOPY                          = binaryToInternalClassName(jbse.algo.meta.Algo_JAVA_SYSTEM_ARRAYCOPY.class.getName());
    public static final String ALGO_JAVA_SYSTEM_IDENTITYHASHCODE                   = binaryToInternalClassName(jbse.algo.meta.Algo_JAVA_SYSTEM_IDENTITYHASHCODE.class.getName());
    public static final String ALGO_JAVA_SYSTEM_MAPLIBRARYNAME                     = binaryToInternalClassName(jbse.algo.meta.Algo_JAVA_SYSTEM_MAPLIBRARYNAME.class.getName());
    public static final String ALGO_JAVA_SYSTEM_SETERR0                            = binaryToInternalClassName(jbse.algo.meta.Algo_JAVA_SYSTEM_SETERR0.class.getName());
    public static final String ALGO_JAVA_SYSTEM_SETIN0                             = binaryToInternalClassName(jbse.algo.meta.Algo_JAVA_SYSTEM_SETIN0.class.getName());
    public static final String ALGO_JAVA_SYSTEM_SETOUT0                            = binaryToInternalClassName(jbse.algo.meta.Algo_JAVA_SYSTEM_SETOUT0.class.getName());
    public static final String ALGO_JAVA_THREAD_CURRENTTHREAD                      = binaryToInternalClassName(jbse.algo.meta.Algo_JAVA_THREAD_CURRENTTHREAD.class.getName());
    public static final String ALGO_JAVA_THREAD_ISINTERRUPTED                      = binaryToInternalClassName(jbse.algo.meta.Algo_JAVA_THREAD_ISINTERRUPTED.class.getName());
    public static final String ALGO_JAVA_THROWABLE_FILLINSTACKTRACE                = binaryToInternalClassName(jbse.algo.meta.Algo_JAVA_THROWABLE_FILLINSTACKTRACE.class.getName());
    public static final String ALGO_JAVA_THROWABLE_GETSTACKTRACEDEPTH              = binaryToInternalClassName(jbse.algo.meta.Algo_JAVA_THROWABLE_GETSTACKTRACEDEPTH.class.getName());
    public static final String ALGO_JAVA_THROWABLE_GETSTACKTRACEELEMENT            = binaryToInternalClassName(jbse.algo.meta.Algo_JAVA_THROWABLE_GETSTACKTRACEELEMENT.class.getName());
    public static final String ALGO_JAVA_WINNTFILESYSTEM_CANONICALIZEWITHPREFIX0   = binaryToInternalClassName(jbse.algo.meta.Algo_JAVA_WINNTFILESYSTEM_CANONICALIZEWITHPREFIX0.class.getName());
    public static final String ALGO_JAVA_XFILESYSTEM_CANONICALIZE0                 = binaryToInternalClassName(jbse.algo.meta.Algo_JAVA_XFILESYSTEM_CANONICALIZE0.class.getName());
    public static final String ALGO_JAVA_XFILESYSTEM_CHECKACCESS                   = binaryToInternalClassName(jbse.algo.meta.Algo_JAVA_XFILESYSTEM_CHECKACCESS.class.getName());
    public static final String ALGO_JAVA_XFILESYSTEM_GETBOOLEANATTRIBUTESX         = binaryToInternalClassName(jbse.algo.meta.Algo_JAVA_XFILESYSTEM_GETBOOLEANATTRIBUTESX.class.getName());
    public static final String ALGO_JAVA_XFILESYSTEM_GETLASTMODIFIEDTIME           = binaryToInternalClassName(jbse.algo.meta.Algo_JAVA_XFILESYSTEM_GETLASTMODIFIEDTIME.class.getName());
    public static final String ALGO_JAVA_XFILESYSTEM_GETLENGTH                     = binaryToInternalClassName(jbse.algo.meta.Algo_JAVA_XFILESYSTEM_GETLENGTH.class.getName());
    public static final String ALGO_JAVA_XFILESYSTEM_LIST                          = binaryToInternalClassName(jbse.algo.meta.Algo_JAVA_XFILESYSTEM_LIST.class.getName());
    public static final String ALGO_JAVA_ZIPFILE_FREEENTRY                         = binaryToInternalClassName(jbse.algo.meta.Algo_JAVA_ZIPFILE_FREEENTRY.class.getName());
    public static final String ALGO_JAVA_ZIPFILE_GETENTRY                          = binaryToInternalClassName(jbse.algo.meta.Algo_JAVA_ZIPFILE_GETENTRY.class.getName());
    public static final String ALGO_JAVA_ZIPFILE_GETENTRYBYTES                     = binaryToInternalClassName(jbse.algo.meta.Algo_JAVA_ZIPFILE_GETENTRYBYTES.class.getName());
    public static final String ALGO_JAVA_ZIPFILE_GETENTRYCRC                       = binaryToInternalClassName(jbse.algo.meta.Algo_JAVA_ZIPFILE_GETENTRYCRC.class.getName());
    public static final String ALGO_JAVA_ZIPFILE_GETENTRYCSIZE                     = binaryToInternalClassName(jbse.algo.meta.Algo_JAVA_ZIPFILE_GETENTRYCSIZE.class.getName());
    public static final String ALGO_JAVA_ZIPFILE_GETENTRYFLAG                      = binaryToInternalClassName(jbse.algo.meta.Algo_JAVA_ZIPFILE_GETENTRYFLAG.class.getName());
    public static final String ALGO_JAVA_ZIPFILE_GETENTRYMETHOD                    = binaryToInternalClassName(jbse.algo.meta.Algo_JAVA_ZIPFILE_GETENTRYMETHOD.class.getName());
    public static final String ALGO_JAVA_ZIPFILE_GETENTRYSIZE                      = binaryToInternalClassName(jbse.algo.meta.Algo_JAVA_ZIPFILE_GETENTRYSIZE.class.getName());
    public static final String ALGO_JAVA_ZIPFILE_GETENTRYTIME                      = binaryToInternalClassName(jbse.algo.meta.Algo_JAVA_ZIPFILE_GETENTRYTIME.class.getName());
    public static final String ALGO_JAVA_ZIPFILE_GETMANIFESTNUM                    = binaryToInternalClassName(jbse.algo.meta.Algo_JAVA_ZIPFILE_GETMANIFESTNUM.class.getName());
    public static final String ALGO_JAVA_ZIPFILE_GETNEXTENTRY                      = binaryToInternalClassName(jbse.algo.meta.Algo_JAVA_ZIPFILE_GETNEXTENTRY.class.getName());
    public static final String ALGO_JAVA_ZIPFILE_GETTOTAL                          = binaryToInternalClassName(jbse.algo.meta.Algo_JAVA_ZIPFILE_GETTOTAL.class.getName());
    public static final String ALGO_JAVA_ZIPFILE_OPEN                              = binaryToInternalClassName(jbse.algo.meta.Algo_JAVA_ZIPFILE_OPEN.class.getName());
    public static final String ALGO_JAVA_ZIPFILE_READ                              = binaryToInternalClassName(jbse.algo.meta.Algo_JAVA_ZIPFILE_READ.class.getName());
    public static final String ALGO_JAVA_ZIPFILE_STARTSWITHLOC                     = binaryToInternalClassName(jbse.algo.meta.Algo_JAVA_ZIPFILE_STARTSWITHLOC.class.getName());
    public static final String ALGO_SUN_CONSTANTPOOL_GETUTF8AT0                    = binaryToInternalClassName(jbse.algo.meta.Algo_SUN_CONSTANTPOOL_GETUTF8AT0.class.getName());
    public static final String ALGO_SUN_NATIVECONSTRUCTORACCESSORIMPL_NEWINSTANCE0 = binaryToInternalClassName(jbse.algo.meta.Algo_SUN_NATIVECONSTRUCTORACCESSORIMPL_NEWINSTANCE0.class.getName());
    public static final String ALGO_SUN_NATIVEMETHODACCESSORIMPL_INVOKE0           = binaryToInternalClassName(jbse.algo.meta.Algo_SUN_NATIVEMETHODACCESSORIMPL_INVOKE0.class.getName());
    public static final String ALGO_SUN_PERF_CREATELONG                            = binaryToInternalClassName(jbse.algo.meta.Algo_SUN_PERF_CREATELONG.class.getName());
    public static final String ALGO_SUN_REFLECTION_GETCALLERCLASS                  = binaryToInternalClassName(jbse.algo.meta.Algo_SUN_REFLECTION_GETCALLERCLASS.class.getName());
    public static final String ALGO_SUN_REFLECTION_GETCLASSACCESSFLAGS             = binaryToInternalClassName(jbse.algo.meta.Algo_SUN_REFLECTION_GETCLASSACCESSFLAGS.class.getName());
    public static final String ALGO_SUN_UNIXNATIVEDISPATCHER_GETCWD                = binaryToInternalClassName(jbse.algo.meta.Algo_SUN_UNIXNATIVEDISPATCHER_GETCWD.class.getName());
    public static final String ALGO_SUN_UNIXNATIVEDISPATCHER_INIT                  = binaryToInternalClassName(jbse.algo.meta.Algo_SUN_UNIXNATIVEDISPATCHER_INIT.class.getName());
    public static final String ALGO_SUN_UNSAFE_ADDRESSSIZE                         = binaryToInternalClassName(jbse.algo.meta.Algo_SUN_UNSAFE_ADDRESSSIZE.class.getName());
    public static final String ALGO_SUN_UNSAFE_ALLOCATEINSTANCE                    = binaryToInternalClassName(jbse.algo.meta.Algo_SUN_UNSAFE_ALLOCATEINSTANCE.class.getName());
    public static final String ALGO_SUN_UNSAFE_ALLOCATEMEMORY                      = binaryToInternalClassName(jbse.algo.meta.Algo_SUN_UNSAFE_ALLOCATEMEMORY.class.getName());
    public static final String ALGO_SUN_UNSAFE_COMPAREANDSWAPINT                   = binaryToInternalClassName(jbse.algo.meta.Algo_SUN_UNSAFE_COMPAREANDSWAPINT.class.getName());
    public static final String ALGO_SUN_UNSAFE_COMPAREANDSWAPLONG                  = binaryToInternalClassName(jbse.algo.meta.Algo_SUN_UNSAFE_COMPAREANDSWAPLONG.class.getName());
    public static final String ALGO_SUN_UNSAFE_COMPAREANDSWAPOBJECT                = binaryToInternalClassName(jbse.algo.meta.Algo_SUN_UNSAFE_COMPAREANDSWAPOBJECT.class.getName());
    public static final String ALGO_SUN_UNSAFE_DEFINEANONYMOUSCLASS                = binaryToInternalClassName(jbse.algo.meta.Algo_SUN_UNSAFE_DEFINEANONYMOUSCLASS.class.getName());
    public static final String ALGO_SUN_UNSAFE_DEFINECLASS                         = binaryToInternalClassName(jbse.algo.meta.Algo_SUN_UNSAFE_DEFINECLASS.class.getName());
    public static final String ALGO_SUN_UNSAFE_ENSURECLASSINITIALIZED              = binaryToInternalClassName(jbse.algo.meta.Algo_SUN_UNSAFE_ENSURECLASSINITIALIZED.class.getName());
    public static final String ALGO_SUN_UNSAFE_FREEMEMORY                          = binaryToInternalClassName(jbse.algo.meta.Algo_SUN_UNSAFE_FREEMEMORY.class.getName());
    public static final String ALGO_SUN_UNSAFE_GETBYTE                             = binaryToInternalClassName(jbse.algo.meta.Algo_SUN_UNSAFE_GETBYTE.class.getName());
    public static final String ALGO_SUN_UNSAFE_GETINT                              = binaryToInternalClassName(jbse.algo.meta.Algo_SUN_UNSAFE_GETINT.class.getName());
    public static final String ALGO_SUN_UNSAFE_GETINT_O                            = binaryToInternalClassName(jbse.algo.meta.Algo_SUN_UNSAFE_GETINT_O.class.getName());
    public static final String ALGO_SUN_UNSAFE_GETLONG                             = binaryToInternalClassName(jbse.algo.meta.Algo_SUN_UNSAFE_GETLONG.class.getName());
    public static final String ALGO_SUN_UNSAFE_GETLONG_O                           = binaryToInternalClassName(jbse.algo.meta.Algo_SUN_UNSAFE_GETLONG_O.class.getName());
    public static final String ALGO_SUN_UNSAFE_GETOBJECT_O                         = binaryToInternalClassName(jbse.algo.meta.Algo_SUN_UNSAFE_GETOBJECT_O.class.getName());
    public static final String ALGO_SUN_UNSAFE_OBJECTFIELDOFFSET                   = binaryToInternalClassName(jbse.algo.meta.Algo_SUN_UNSAFE_OBJECTFIELDOFFSET.class.getName());
    public static final String ALGO_SUN_UNSAFE_PAGESIZE                            = binaryToInternalClassName(jbse.algo.meta.Algo_SUN_UNSAFE_PAGESIZE.class.getName());
    public static final String ALGO_SUN_UNSAFE_PUTINT                              = binaryToInternalClassName(jbse.algo.meta.Algo_SUN_UNSAFE_PUTINT.class.getName());
    public static final String ALGO_SUN_UNSAFE_PUTINT_O                            = binaryToInternalClassName(jbse.algo.meta.Algo_SUN_UNSAFE_PUTINT_O.class.getName());
    public static final String ALGO_SUN_UNSAFE_PUTLONG                             = binaryToInternalClassName(jbse.algo.meta.Algo_SUN_UNSAFE_PUTLONG.class.getName());
    public static final String ALGO_SUN_UNSAFE_PUTLONG_O                           = binaryToInternalClassName(jbse.algo.meta.Algo_SUN_UNSAFE_PUTLONG_O.class.getName());
    public static final String ALGO_SUN_UNSAFE_PUTOBJECT_O                         = binaryToInternalClassName(jbse.algo.meta.Algo_SUN_UNSAFE_PUTOBJECT_O.class.getName());
    public static final String ALGO_SUN_UNSAFE_SHOULDBEINITIALIZED                 = binaryToInternalClassName(jbse.algo.meta.Algo_SUN_UNSAFE_SHOULDBEINITIALIZED.class.getName());
    public static final String ALGO_SUN_UNSAFE_STATICFIELDBASE                     = binaryToInternalClassName(jbse.algo.meta.Algo_SUN_UNSAFE_STATICFIELDBASE.class.getName());
    public static final String ALGO_SUN_UNSAFE_STATICFIELDOFFSET                   = binaryToInternalClassName(jbse.algo.meta.Algo_SUN_UNSAFE_STATICFIELDOFFSET.class.getName());
    public static final String ALGO_SUN_UNSAFE_STOREFENCE                          = binaryToInternalClassName(jbse.algo.meta.Algo_SUN_UNSAFE_STOREFENCE.class.getName());

    //Overriding meta-level implementations of jbse.meta.Analysis methods
    public static final String ALGO_JBSE_ANALYSIS_ANY                       = binaryToInternalClassName(jbse.algo.meta.Algo_JBSE_ANALYSIS_ANY.class.getName());
    public static final String ALGO_JBSE_ANALYSIS_ASSUMECLASSNOTINITIALIZED = binaryToInternalClassName(jbse.algo.meta.Algo_JBSE_ANALYSIS_ASSUMECLASSNOTINITIALIZED.class.getName());
    public static final String ALGO_JBSE_ANALYSIS_ENDGUIDANCE               = binaryToInternalClassName(jbse.algo.meta.Algo_JBSE_ANALYSIS_ENDGUIDANCE.class.getName());
    public static final String ALGO_JBSE_ANALYSIS_FAIL                      = binaryToInternalClassName(jbse.algo.meta.Algo_JBSE_ANALYSIS_FAIL.class.getName());
    public static final String ALGO_JBSE_ANALYSIS_IGNORE                    = binaryToInternalClassName(jbse.algo.meta.Algo_JBSE_ANALYSIS_IGNORE.class.getName());
    public static final String ALGO_JBSE_ANALYSIS_ISRESOLVED                = binaryToInternalClassName(jbse.algo.meta.Algo_JBSE_ANALYSIS_ISRESOLVED.class.getName());
    public static final String ALGO_JBSE_ANALYSIS_ISRESOLVEDBYALIAS         = binaryToInternalClassName(jbse.algo.meta.Algo_JBSE_ANALYSIS_ISRESOLVEDBYALIAS.class.getName());
    public static final String ALGO_JBSE_ANALYSIS_ISRESOLVEDBYEXPANSION     = binaryToInternalClassName(jbse.algo.meta.Algo_JBSE_ANALYSIS_ISRESOLVEDBYEXPANSION.class.getName());
    public static final String ALGO_JBSE_ANALYSIS_ISSYMBOLIC                = binaryToInternalClassName(jbse.algo.meta.Algo_JBSE_ANALYSIS_ISSYMBOLIC.class.getName());
    public static final String ALGO_JBSE_ANALYSIS_SUCCEED                   = binaryToInternalClassName(jbse.algo.meta.Algo_JBSE_ANALYSIS_SUCCEED.class.getName());
    public static final String ALGO_JBSE_ANALYSIS_SYMBOLNAME                = binaryToInternalClassName(jbse.algo.meta.Algo_JBSE_ANALYSIS_SYMBOLNAME.class.getName());

    //Overriding meta-level implementations of jbse.base.Base methods
    public static final String ALGO_JBSE_BASE_CLINIT                        = binaryToInternalClassName(jbse.algo.meta.Algo_JBSE_BASE_CLINIT.class.getName());
    public static final String ALGO_JBSE_BASE_MAKEKLASSSYMBOLIC_DO          = binaryToInternalClassName(jbse.algo.meta.Algo_JBSE_BASE_MAKEKLASSSYMBOLIC_DO.class.getName());

    //Overriding meta-level implementations of jbse.base.JAVA_MAP and jbse.base.JAVA_CONCURRENTMAP methods
    public static final String ALGO_JBSE_JAVA_CONCURRENTMAP_ONKEYRESOLUTION0                 = binaryToInternalClassName(jbse.algo.meta.Algo_JBSE_JAVA_CONCURRENTMAP_ONKEYRESOLUTION0.class.getName());
    public static final String ALGO_JBSE_JAVA_CONCURRENTMAP_REFINEONFRESHENTRYANDBRANCH      = binaryToInternalClassName(jbse.algo.meta.Algo_JBSE_JAVA_CONCURRENTMAP_REFINEONFRESHENTRYANDBRANCH.class.getName());
    public static final String ALGO_JBSE_JAVA_CONCURRENTMAP_REFINEONKEYANDBRANCH             = binaryToInternalClassName(jbse.algo.meta.Algo_JBSE_JAVA_CONCURRENTMAP_REFINEONKEYANDBRANCH.class.getName());
    public static final String ALGO_JBSE_JAVA_CONCURRENTMAP_REFINEONKEYCOMBINATIONSANDBRANCH = binaryToInternalClassName(jbse.algo.meta.Algo_JBSE_JAVA_CONCURRENTMAP_REFINEONKEYCOMBINATIONSANDBRANCH.class.getName());
    public static final String ALGO_JBSE_JAVA_CONCURRENTMAP_REFINEONVALUEANDBRANCH           = binaryToInternalClassName(jbse.algo.meta.Algo_JBSE_JAVA_CONCURRENTMAP_REFINEONVALUEANDBRANCH.class.getName());
    public static final String ALGO_JBSE_JAVA_LINKEDMAP_ONKEYRESOLUTION0                     = binaryToInternalClassName(jbse.algo.meta.Algo_JBSE_JAVA_LINKEDMAP_ONKEYRESOLUTION0.class.getName());
    public static final String ALGO_JBSE_JAVA_LINKEDMAP_REFINEONFRESHENTRYANDBRANCH          = binaryToInternalClassName(jbse.algo.meta.Algo_JBSE_JAVA_LINKEDMAP_REFINEONFRESHENTRYANDBRANCH.class.getName());
    public static final String ALGO_JBSE_JAVA_LINKEDMAP_REFINEONKEYANDBRANCH                 = binaryToInternalClassName(jbse.algo.meta.Algo_JBSE_JAVA_LINKEDMAP_REFINEONKEYANDBRANCH.class.getName());
    public static final String ALGO_JBSE_JAVA_LINKEDMAP_REFINEONKEYCOMBINATIONSANDBRANCH     = binaryToInternalClassName(jbse.algo.meta.Algo_JBSE_JAVA_LINKEDMAP_REFINEONKEYCOMBINATIONSANDBRANCH.class.getName());
    public static final String ALGO_JBSE_JAVA_LINKEDMAP_REFINEONVALUEANDBRANCH               = binaryToInternalClassName(jbse.algo.meta.Algo_JBSE_JAVA_LINKEDMAP_REFINEONVALUEANDBRANCH.class.getName());
    public static final String ALGO_JBSE_JAVA_MAP_ONKEYRESOLUTION0                           = binaryToInternalClassName(jbse.algo.meta.Algo_JBSE_JAVA_MAP_ONKEYRESOLUTION0.class.getName());
    public static final String ALGO_JBSE_JAVA_MAP_REFINEONFRESHENTRYANDBRANCH                = binaryToInternalClassName(jbse.algo.meta.Algo_JBSE_JAVA_MAP_REFINEONFRESHENTRYANDBRANCH.class.getName());
    public static final String ALGO_JBSE_JAVA_MAP_REFINEONKEYANDBRANCH                       = binaryToInternalClassName(jbse.algo.meta.Algo_JBSE_JAVA_MAP_REFINEONKEYANDBRANCH.class.getName());
    public static final String ALGO_JBSE_JAVA_MAP_REFINEONKEYCOMBINATIONSANDBRANCH           = binaryToInternalClassName(jbse.algo.meta.Algo_JBSE_JAVA_MAP_REFINEONKEYCOMBINATIONSANDBRANCH.class.getName());
    public static final String ALGO_JBSE_JAVA_MAP_REFINEONVALUEANDBRANCH                     = binaryToInternalClassName(jbse.algo.meta.Algo_JBSE_JAVA_MAP_REFINEONVALUEANDBRANCH.class.getName());
    public static final String ALGO_JBSE_JAVA_XMAP_MAKEINITIAL                               = binaryToInternalClassName(jbse.algo.meta.Algo_JBSE_JAVA_XMAP_MAKEINITIAL.class.getName());
    public static final String ALGO_JBSE_JAVA_XMAP_METATHROWUNEXPECTEDINTERNALEXCEPTION      = binaryToInternalClassName(jbse.algo.meta.Algo_JBSE_JAVA_XMAP_METATHROWUNEXPECTEDINTERNALEXCEPTION.class.getName());
    public static final String ALGO_JBSE_JAVA_XMAP_NOTIFYMETHODEXECUTION                     = binaryToInternalClassName(jbse.algo.meta.Algo_JBSE_JAVA_XMAP_NOTIFYMETHODEXECUTION.class.getName());
    
    //Overriding meta-level implementations of JBSE classless (pseudo)methods
    public static final String ALGO_noclass_REGISTERLOADEDCLASS                   = binaryToInternalClassName(jbse.algo.meta.Algo_noclass_REGISTERLOADEDCLASS.class.getName());
    public static final String ALGO_noclass_REGISTERMETHODHANDLE                  = binaryToInternalClassName(jbse.algo.meta.Algo_noclass_REGISTERMETHODHANDLE.class.getName());
    public static final String ALGO_noclass_REGISTERMETHODTYPE                    = binaryToInternalClassName(jbse.algo.meta.Algo_noclass_REGISTERMETHODTYPE.class.getName());
    public static final String ALGO_noclass_SETSTANDARDCLASSLOADERSREADY          = binaryToInternalClassName(jbse.algo.meta.Algo_noclass_SETSTANDARDCLASSLOADERSREADY.class.getName());
    public static final String ALGO_noclass_STORELINKEDMETHODADAPTERANDAPPENDIX   = binaryToInternalClassName(jbse.algo.meta.Algo_noclass_STORELINKEDMETHODADAPTERANDAPPENDIX.class.getName());
    public static final String ALGO_noclass_STORELINKEDCALLSITEADAPTERANDAPPENDIX = binaryToInternalClassName(jbse.algo.meta.Algo_noclass_STORELINKEDCALLSITEADAPTERANDAPPENDIX.class.getName());
    
    //Overriding base-level implementation of standard methods
    private static final String JBSE_BASE = binaryToInternalClassName(jbse.base.Base.class.getName());
    public static final Signature BASE_DONOTHING = new Signature(JBSE_BASE, "()" + VOID, "doNothing");
    public static final Signature BASE_JAVA_ACCESSCONTROLLER_DOPRIVILEGED_EXCEPTION_1 = 
        new Signature(JBSE_BASE, 
                      "(" + REFERENCE + JAVA_PRIVILEGEDEXCEPTIONACTION + TYPEEND + ")" + REFERENCE + JAVA_OBJECT + TYPEEND, 
                      "base_JAVA_ACCESSCONTROLLER_DOPRIVILEGED_EXCEPTION");
    public static final Signature BASE_JAVA_ACCESSCONTROLLER_DOPRIVILEGED_EXCEPTION_2 = 
        new Signature(JBSE_BASE, 
                      "(" + REFERENCE + JAVA_PRIVILEGEDEXCEPTIONACTION + TYPEEND + REFERENCE + JAVA_ACCESSCONTROLCONTEXT + TYPEEND + ")" + REFERENCE + JAVA_OBJECT + TYPEEND, 
                      "base_JAVA_ACCESSCONTROLLER_DOPRIVILEGED_EXCEPTION");
    public static final Signature BASE_JAVA_ACCESSCONTROLLER_DOPRIVILEGED_NOEXCEPTION_1 = 
        new Signature(JBSE_BASE, 
                      "(" + REFERENCE + JAVA_PRIVILEGEDACTION + TYPEEND + ")" + REFERENCE + JAVA_OBJECT + TYPEEND, 
                      "base_JAVA_ACCESSCONTROLLER_DOPRIVILEGED_NOEXCEPTION");
    public static final Signature BASE_JAVA_ACCESSCONTROLLER_DOPRIVILEGED_NOEXCEPTION_2 = 
        new Signature(JBSE_BASE, 
                      "(" + REFERENCE + JAVA_PRIVILEGEDACTION + TYPEEND + REFERENCE + JAVA_ACCESSCONTROLCONTEXT + TYPEEND + ")" + REFERENCE + JAVA_OBJECT + TYPEEND, 
                      "base_JAVA_ACCESSCONTROLLER_DOPRIVILEGED_NOEXCEPTION");
    public static final Signature BASE_JAVA_ACCESSCONTROLLER_GETSTACKACCESSCONTROLCONTEXT = 
        new Signature(JBSE_BASE, 
                      "()" + REFERENCE + JAVA_ACCESSCONTROLCONTEXT + TYPEEND, 
                      "base_JAVA_ACCESSCONTROLLER_GETSTACKACCESSCONTROLCONTEXT");
    public static final Signature BASE_JAVA_ATOMICLONG_VMSUPPORTSCS8 = 
        new Signature(JBSE_BASE, 
                      "()" + BOOLEAN, 
                      "base_JAVA_ATOMICLONG_VMSUPPORTSCS8");
    public static final Signature BASE_JAVA_CLASS_DESIREDASSERTIONSTATUS0 = 
        new Signature(JBSE_BASE, 
                      "(" + REFERENCE + JAVA_CLASS + TYPEEND + ")" + BOOLEAN, 
                      "base_JAVA_CLASS_DESIREDASSERTIONSTATUS0");
    public static final Signature BASE_JAVA_CLASSLOADER_FINDBUILTINLIB = 
        new Signature(JBSE_BASE, 
                      "(" + REFERENCE + JAVA_STRING + TYPEEND + ")" + REFERENCE + JAVA_STRING + TYPEEND, 
                      "base_JAVA_CLASSLOADER_FINDBUILTINLIB");
    public static final Signature BASE_JAVA_METHODHANDLENATIVES_GETCONSTANT = 
        new Signature(JBSE_BASE, 
                      "(" + INT + ")" + INT, 
                      "base_JAVA_METHODHANDLENATIVES_GETCONSTANT");
    public static final Signature BASE_JAVA_OBJECT_NOTIFY = 
        new Signature(JBSE_BASE, 
                      "(" + REFERENCE + JAVA_OBJECT + TYPEEND + ")" + VOID, 
                      "base_JAVA_OBJECT_NOTIFY");
    public static final Signature BASE_JAVA_OBJECT_WAIT = 
        new Signature(JBSE_BASE, 
                      "(" + REFERENCE + JAVA_OBJECT + TYPEEND + LONG + ")" + VOID, 
                      "base_JAVA_OBJECT_WAIT");
    public static final Signature BASE_JAVA_RUNTIME_AVAILABLEPROCESSORS = 
        new Signature(JBSE_BASE, 
                      "(" + REFERENCE + JAVA_RUNTIME + TYPEEND + ")" + INT, 
                      "base_JAVA_RUNTIME_AVAILABLEPROCESSORS");
    public static final Signature BASE_JAVA_SYSTEM_INITPROPERTIES =
        new Signature(JBSE_BASE, 
                      "(" + REFERENCE + JAVA_PROPERTIES + TYPEEND + ")" + REFERENCE + JAVA_PROPERTIES + TYPEEND, 
                      "base_JAVA_SYSTEM_INITPROPERTIES");
    public static final Signature BASE_JAVA_THREAD_ISALIVE =
        new Signature(JBSE_BASE, 
                      "(" + REFERENCE + JAVA_THREAD + TYPEEND + ")" + BOOLEAN, 
                      "base_JAVA_THREAD_ISALIVE");
    public static final Signature BASE_JBSE_ANALYSIS_ISRUNBYJBSE =
        new Signature(JBSE_BASE, 
                      "()" + BOOLEAN, 
                      "base_JBSE_ANALYSIS_ISRUNBYJBSE");
    public static final Signature BASE_SUN_SIGNAL_FINDSIGNAL =
        new Signature(JBSE_BASE, 
                      "(" + REFERENCE + JAVA_STRING + TYPEEND + ")" + INT, 
                      "base_SUN_SIGNAL_FINDSIGNAL");
    public static final Signature BASE_SUN_SIGNAL_HANDLE0 =
        new Signature(JBSE_BASE, 
                      "(" + INT + LONG + ")" + LONG, 
                      "base_SUN_SIGNAL_HANDLE0");
    public static final Signature BASE_SUN_UNSAFE_ARRAYBASEOFFSET =
        new Signature(JBSE_BASE, 
                      "(" + REFERENCE + SUN_UNSAFE + TYPEEND + REFERENCE + JAVA_CLASS + TYPEEND + ")" + INT, 
                      "base_SUN_UNSAFE_ARRAYBASEOFFSET");
    public static final Signature BASE_SUN_UNSAFE_ARRAYINDEXSCALE =
        new Signature(JBSE_BASE, 
                      "(" + REFERENCE + SUN_UNSAFE + TYPEEND + REFERENCE + JAVA_CLASS + TYPEEND + ")" + INT, 
                      "base_SUN_UNSAFE_ARRAYINDEXSCALE");
    public static final Signature BASE_SUN_UNSAFE_FULLFENCE =
        new Signature(JBSE_BASE, 
                      "(" + REFERENCE + SUN_UNSAFE + TYPEEND + ")" + VOID, 
                      "base_SUN_UNSAFE_FULLFENCE");
    public static final Signature BASE_SUN_UNSAFE_OBJECTFIELDOFFSET =
        new Signature(JBSE_BASE, 
                      "(" + REFERENCE + SUN_UNSAFE + TYPEEND + REFERENCE + JAVA_FIELD + TYPEEND + ")" + LONG, 
                      "base_SUN_UNSAFE_OBJECTFIELDOFFSET");
    public static final Signature BASE_SUN_UNSAFE_PARK =
        new Signature(JBSE_BASE, 
                      "(" + REFERENCE + SUN_UNSAFE + TYPEEND + BOOLEAN + LONG + ")" + VOID, 
                      "base_SUN_UNSAFE_PARK");
    public static final Signature BASE_SUN_UNSAFE_UNPARK =
        new Signature(JBSE_BASE, 
                     "(" + REFERENCE + SUN_UNSAFE + TYPEEND + REFERENCE + JAVA_OBJECT + TYPEEND + ")" + VOID, 
                     "base_SUN_UNSAFE_UNPARK");
    public static final Signature BASE_SUN_URLCLASSPATH_GETLOOKUPCACHEURLS =
        new Signature(JBSE_BASE, 
                      "(" + REFERENCE + JAVA_CLASSLOADER + TYPEEND + ")" + ARRAYOF + REFERENCE + JAVA_URL + TYPEEND, 
                      "base_SUN_URLCLASSPATH_GETLOOKUPCACHEURLS");
}
