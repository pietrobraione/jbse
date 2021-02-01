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
import static jbse.common.Type.internalClassName;
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
    public static final String ALGO_INVOKEMETA_METACIRCULAR                        = internalClassName(jbse.algo.Algo_INVOKEMETA_Metacircular.class.getName());
    
    //Overriding meta-level implementations of standard methods
    public static final String ALGO_JAVA_CLASS_FORNAME0                            = internalClassName(jbse.algo.meta.Algo_JAVA_CLASS_FORNAME0.class.getName());
    public static final String ALGO_JAVA_CLASS_GETCOMPONENTTYPE                    = internalClassName(jbse.algo.meta.Algo_JAVA_CLASS_GETCOMPONENTTYPE.class.getName());
    public static final String ALGO_JAVA_CLASS_GETCONSTANTPOOL                     = internalClassName(jbse.algo.meta.Algo_JAVA_CLASS_GETCONSTANTPOOL.class.getName());
    public static final String ALGO_JAVA_CLASS_GETDECLAREDCONSTRUCTORS0            = internalClassName(jbse.algo.meta.Algo_JAVA_CLASS_GETDECLAREDCONSTRUCTORS0.class.getName());
    public static final String ALGO_JAVA_CLASS_GETDECLAREDFIELDS0                  = internalClassName(jbse.algo.meta.Algo_JAVA_CLASS_GETDECLAREDFIELDS0.class.getName());
    public static final String ALGO_JAVA_CLASS_GETDECLAREDMETHODS0                 = internalClassName(jbse.algo.meta.Algo_JAVA_CLASS_GETDECLAREDMETHODS0.class.getName());
    public static final String ALGO_JAVA_CLASS_GETDECLARINGCLASS0                  = internalClassName(jbse.algo.meta.Algo_JAVA_CLASS_GETDECLARINGCLASS0.class.getName());
    public static final String ALGO_JAVA_CLASS_GETENCLOSINGMETHOD0                 = internalClassName(jbse.algo.meta.Algo_JAVA_CLASS_GETENCLOSINGMETHOD0.class.getName());
    public static final String ALGO_JAVA_CLASS_GETGENERICSIGNATURE0                = internalClassName(jbse.algo.meta.Algo_JAVA_CLASS_GETGENERICSIGNATURE0.class.getName());
    public static final String ALGO_JAVA_CLASS_GETINTERFACES0                      = internalClassName(jbse.algo.meta.Algo_JAVA_CLASS_GETINTERFACES0.class.getName());
    public static final String ALGO_JAVA_CLASS_GETMODIFIERS                        = internalClassName(jbse.algo.meta.Algo_JAVA_CLASS_GETMODIFIERS.class.getName());
    public static final String ALGO_JAVA_CLASS_GETNAME0                            = internalClassName(jbse.algo.meta.Algo_JAVA_CLASS_GETNAME0.class.getName());
    public static final String ALGO_JAVA_CLASS_GETPRIMITIVECLASS                   = internalClassName(jbse.algo.meta.Algo_JAVA_CLASS_GETPRIMITIVECLASS.class.getName());
    public static final String ALGO_JAVA_CLASS_GETPROTECTIONDOMAIN0                = internalClassName(jbse.algo.meta.Algo_JAVA_CLASS_GETPROTECTIONDOMAIN0.class.getName());
    public static final String ALGO_JAVA_CLASS_GETRAWANNOTATIONS                   = internalClassName(jbse.algo.meta.Algo_JAVA_CLASS_GETRAWANNOTATIONS.class.getName());
    public static final String ALGO_JAVA_CLASS_GETSIGNERS                          = internalClassName(jbse.algo.meta.Algo_JAVA_CLASS_GETSIGNERS.class.getName());
    public static final String ALGO_JAVA_CLASS_GETSUPERCLASS                       = internalClassName(jbse.algo.meta.Algo_JAVA_CLASS_GETSUPERCLASS.class.getName());
    public static final String ALGO_JAVA_CLASS_ISARRAY                             = internalClassName(jbse.algo.meta.Algo_JAVA_CLASS_ISARRAY.class.getName());
    public static final String ALGO_JAVA_CLASS_ISASSIGNABLEFROM                    = internalClassName(jbse.algo.meta.Algo_JAVA_CLASS_ISASSIGNABLEFROM.class.getName());
    public static final String ALGO_JAVA_CLASS_ISINSTANCE                          = internalClassName(jbse.algo.meta.Algo_JAVA_CLASS_ISINSTANCE.class.getName());
    public static final String ALGO_JAVA_CLASS_ISINTERFACE                         = internalClassName(jbse.algo.meta.Algo_JAVA_CLASS_ISINTERFACE.class.getName());
    public static final String ALGO_JAVA_CLASS_ISPRIMITIVE                         = internalClassName(jbse.algo.meta.Algo_JAVA_CLASS_ISPRIMITIVE.class.getName());
    public static final String ALGO_JAVA_CLASS_SETSIGNERS                          = internalClassName(jbse.algo.meta.Algo_JAVA_CLASS_SETSIGNERS.class.getName());
    public static final String ALGO_JAVA_CLASSLOADER_DEFINECLASS1                  = internalClassName(jbse.algo.meta.Algo_JAVA_CLASSLOADER_DEFINECLASS1.class.getName());
    public static final String ALGO_JAVA_CLASSLOADER_FINDBOOTSTRAPCLASS            = internalClassName(jbse.algo.meta.Algo_JAVA_CLASSLOADER_FINDBOOTSTRAPCLASS.class.getName());
    public static final String ALGO_JAVA_CLASSLOADER_FINDLOADEDCLASS0              = internalClassName(jbse.algo.meta.Algo_JAVA_CLASSLOADER_FINDLOADEDCLASS0.class.getName());
    public static final String ALGO_JAVA_CLASSLOADER_NATIVELIBRARY_LOAD            = internalClassName(jbse.algo.meta.Algo_JAVA_CLASSLOADER_NATIVELIBRARY_LOAD.class.getName());
    public static final String ALGO_JAVA_CRC32_UPDATEBYTES                         = internalClassName(jbse.algo.meta.Algo_JAVA_CRC32_UPDATEBYTES.class.getName());
    public static final String ALGO_JAVA_EXECUTABLE_GETPARAMETERS0                 = internalClassName(jbse.algo.meta.Algo_JAVA_EXECUTABLE_GETPARAMETERS0.class.getName());
    public static final String ALGO_JAVA_FILEINPUTSTREAM_AVAILABLE                 = internalClassName(jbse.algo.meta.Algo_JAVA_FILEINPUTSTREAM_AVAILABLE.class.getName());
    public static final String ALGO_JAVA_FILEINPUTSTREAM_CLOSE0                    = internalClassName(jbse.algo.meta.Algo_JAVA_FILEINPUTSTREAM_CLOSE0.class.getName());
    public static final String ALGO_JAVA_FILEINPUTSTREAM_OPEN0                     = internalClassName(jbse.algo.meta.Algo_JAVA_FILEINPUTSTREAM_OPEN0.class.getName());
    public static final String ALGO_JAVA_FILEINPUTSTREAM_READBYTES                 = internalClassName(jbse.algo.meta.Algo_JAVA_FILEINPUTSTREAM_READBYTES.class.getName());
    public static final String ALGO_JAVA_FILEOUTPUTSTREAM_OPEN0                    = internalClassName(jbse.algo.meta.Algo_JAVA_FILEOUTPUTSTREAM_OPEN0.class.getName());
    public static final String ALGO_JAVA_FILEOUTPUTSTREAM_WRITEBYTES               = internalClassName(jbse.algo.meta.Algo_JAVA_FILEOUTPUTSTREAM_WRITEBYTES.class.getName());
    public static final String ALGO_JAVA_INFLATER_END                              = internalClassName(jbse.algo.meta.Algo_JAVA_INFLATER_END.class.getName());
    public static final String ALGO_JAVA_INFLATER_GETADLER                         = internalClassName(jbse.algo.meta.Algo_JAVA_INFLATER_GETADLER.class.getName());
    public static final String ALGO_JAVA_INFLATER_INFLATEBYTES                     = internalClassName(jbse.algo.meta.Algo_JAVA_INFLATER_INFLATEBYTES.class.getName());
    public static final String ALGO_JAVA_INFLATER_INIT                             = internalClassName(jbse.algo.meta.Algo_JAVA_INFLATER_INIT.class.getName());
    public static final String ALGO_JAVA_INFLATER_RESET                            = internalClassName(jbse.algo.meta.Algo_JAVA_INFLATER_RESET.class.getName());
    public static final String ALGO_JAVA_INFLATER_SETDICTIONARY                    = internalClassName(jbse.algo.meta.Algo_JAVA_INFLATER_SETDICTIONARY.class.getName());
    public static final String ALGO_JAVA_JARFILE_GETMETAINFENTRYNAMES              = internalClassName(jbse.algo.meta.Algo_JAVA_JARFILE_GETMETAINFENTRYNAMES.class.getName());
    public static final String ALGO_JAVA_METHODHANDLENATIVES_GETMEMBERS            = internalClassName(jbse.algo.meta.Algo_JAVA_METHODHANDLENATIVES_GETMEMBERS.class.getName());
    public static final String ALGO_JAVA_METHODHANDLENATIVES_INIT                  = internalClassName(jbse.algo.meta.Algo_JAVA_METHODHANDLENATIVES_INIT.class.getName());
    public static final String ALGO_JAVA_METHODHANDLENATIVES_OBJECTFIELDOFFSET     = internalClassName(jbse.algo.meta.Algo_JAVA_METHODHANDLENATIVES_OBJECTFIELDOFFSET.class.getName());
    public static final String ALGO_JAVA_METHODHANDLENATIVES_RESOLVE               = internalClassName(jbse.algo.meta.Algo_JAVA_METHODHANDLENATIVES_RESOLVE.class.getName());
    public static final String ALGO_JAVA_METHODHANDLENATIVES_SETCALLSITETARGETNORMAL = internalClassName(jbse.algo.meta.Algo_JAVA_METHODHANDLENATIVES_SETCALLSITETARGETNORMAL.class.getName());
    public static final String ALGO_JAVA_METHODHANDLENATIVES_SETCALLSITETARGETVOLATILE = internalClassName(jbse.algo.meta.Algo_JAVA_METHODHANDLENATIVES_SETCALLSITETARGETVOLATILE.class.getName());
    public static final String ALGO_JAVA_METHODHANDLENATIVES_STATICFIELDOFFSET     = internalClassName(jbse.algo.meta.Algo_JAVA_METHODHANDLENATIVES_STATICFIELDOFFSET.class.getName());
    public static final String ALGO_JAVA_OBJECT_CLONE                              = internalClassName(jbse.algo.meta.Algo_JAVA_OBJECT_CLONE.class.getName());
    public static final String ALGO_JAVA_OBJECT_GETCLASS                           = internalClassName(jbse.algo.meta.Algo_JAVA_OBJECT_GETCLASS.class.getName());
    public static final String ALGO_JAVA_OBJECT_HASHCODE                           = internalClassName(jbse.algo.meta.Algo_JAVA_OBJECT_HASHCODE.class.getName());
    public static final String ALGO_JAVA_PACKAGE_GETSYSTEMPACKAGE0                 = internalClassName(jbse.algo.meta.Algo_JAVA_PACKAGE_GETSYSTEMPACKAGE0.class.getName());
    public static final String ALGO_JAVA_PROCESSENVIRONMENT_ENVIRON                = internalClassName(jbse.algo.meta.Algo_JAVA_PROCESSENVIRONMENT_ENVIRON.class.getName());
    public static final String ALGO_JAVA_RANDOMACCESSFILE_OPEN0                    = internalClassName(jbse.algo.meta.Algo_JAVA_RANDOMACCESSFILE_OPEN0.class.getName());
    public static final String ALGO_JAVA_REFLECT_ARRAY_NEWARRAY                    = internalClassName(jbse.algo.meta.Algo_JAVA_REFLECT_ARRAY_NEWARRAY.class.getName());
    public static final String ALGO_JAVA_STRING_HASHCODE                           = internalClassName(jbse.algo.meta.Algo_JAVA_STRING_HASHCODE.class.getName());
    public static final String ALGO_JAVA_STRING_INTERN                             = internalClassName(jbse.algo.meta.Algo_JAVA_STRING_INTERN.class.getName());
    public static final String ALGO_JAVA_STRINGBUILDER_APPEND                      = internalClassName(jbse.algo.meta.Algo_JAVA_STRINGBUILDER_APPEND.class.getName());
    public static final String ALGO_JAVA_SYSTEM_ARRAYCOPY                          = internalClassName(jbse.algo.meta.Algo_JAVA_SYSTEM_ARRAYCOPY.class.getName());
    public static final String ALGO_JAVA_SYSTEM_IDENTITYHASHCODE                   = internalClassName(jbse.algo.meta.Algo_JAVA_SYSTEM_IDENTITYHASHCODE.class.getName());
    public static final String ALGO_JAVA_SYSTEM_MAPLIBRARYNAME                     = internalClassName(jbse.algo.meta.Algo_JAVA_SYSTEM_MAPLIBRARYNAME.class.getName());
    public static final String ALGO_JAVA_SYSTEM_SETERR0                            = internalClassName(jbse.algo.meta.Algo_JAVA_SYSTEM_SETERR0.class.getName());
    public static final String ALGO_JAVA_SYSTEM_SETIN0                             = internalClassName(jbse.algo.meta.Algo_JAVA_SYSTEM_SETIN0.class.getName());
    public static final String ALGO_JAVA_SYSTEM_SETOUT0                            = internalClassName(jbse.algo.meta.Algo_JAVA_SYSTEM_SETOUT0.class.getName());
    public static final String ALGO_JAVA_THREAD_CURRENTTHREAD                      = internalClassName(jbse.algo.meta.Algo_JAVA_THREAD_CURRENTTHREAD.class.getName());
    public static final String ALGO_JAVA_THREAD_ISINTERRUPTED                      = internalClassName(jbse.algo.meta.Algo_JAVA_THREAD_ISINTERRUPTED.class.getName());
    public static final String ALGO_JAVA_THROWABLE_FILLINSTACKTRACE                = internalClassName(jbse.algo.meta.Algo_JAVA_THROWABLE_FILLINSTACKTRACE.class.getName());
    public static final String ALGO_JAVA_THROWABLE_GETSTACKTRACEDEPTH              = internalClassName(jbse.algo.meta.Algo_JAVA_THROWABLE_GETSTACKTRACEDEPTH.class.getName());
    public static final String ALGO_JAVA_THROWABLE_GETSTACKTRACEELEMENT            = internalClassName(jbse.algo.meta.Algo_JAVA_THROWABLE_GETSTACKTRACEELEMENT.class.getName());
    public static final String ALGO_JAVA_WINNTFILESYSTEM_CANONICALIZEWITHPREFIX0   = internalClassName(jbse.algo.meta.Algo_JAVA_WINNTFILESYSTEM_CANONICALIZEWITHPREFIX0.class.getName());
    public static final String ALGO_JAVA_XFILESYSTEM_CANONICALIZE0                 = internalClassName(jbse.algo.meta.Algo_JAVA_XFILESYSTEM_CANONICALIZE0.class.getName());
    public static final String ALGO_JAVA_XFILESYSTEM_CHECKACCESS                   = internalClassName(jbse.algo.meta.Algo_JAVA_XFILESYSTEM_CHECKACCESS.class.getName());
    public static final String ALGO_JAVA_XFILESYSTEM_GETBOOLEANATTRIBUTESX         = internalClassName(jbse.algo.meta.Algo_JAVA_XFILESYSTEM_GETBOOLEANATTRIBUTESX.class.getName());
    public static final String ALGO_JAVA_XFILESYSTEM_GETLASTMODIFIEDTIME           = internalClassName(jbse.algo.meta.Algo_JAVA_XFILESYSTEM_GETLASTMODIFIEDTIME.class.getName());
    public static final String ALGO_JAVA_XFILESYSTEM_GETLENGTH                     = internalClassName(jbse.algo.meta.Algo_JAVA_XFILESYSTEM_GETLENGTH.class.getName());
    public static final String ALGO_JAVA_XFILESYSTEM_LIST                          = internalClassName(jbse.algo.meta.Algo_JAVA_XFILESYSTEM_LIST.class.getName());
    public static final String ALGO_JAVA_ZIPFILE_FREEENTRY                         = internalClassName(jbse.algo.meta.Algo_JAVA_ZIPFILE_FREEENTRY.class.getName());
    public static final String ALGO_JAVA_ZIPFILE_GETENTRY                          = internalClassName(jbse.algo.meta.Algo_JAVA_ZIPFILE_GETENTRY.class.getName());
    public static final String ALGO_JAVA_ZIPFILE_GETENTRYBYTES                     = internalClassName(jbse.algo.meta.Algo_JAVA_ZIPFILE_GETENTRYBYTES.class.getName());
    public static final String ALGO_JAVA_ZIPFILE_GETENTRYCRC                       = internalClassName(jbse.algo.meta.Algo_JAVA_ZIPFILE_GETENTRYCRC.class.getName());
    public static final String ALGO_JAVA_ZIPFILE_GETENTRYCSIZE                     = internalClassName(jbse.algo.meta.Algo_JAVA_ZIPFILE_GETENTRYCSIZE.class.getName());
    public static final String ALGO_JAVA_ZIPFILE_GETENTRYFLAG                      = internalClassName(jbse.algo.meta.Algo_JAVA_ZIPFILE_GETENTRYFLAG.class.getName());
    public static final String ALGO_JAVA_ZIPFILE_GETENTRYMETHOD                    = internalClassName(jbse.algo.meta.Algo_JAVA_ZIPFILE_GETENTRYMETHOD.class.getName());
    public static final String ALGO_JAVA_ZIPFILE_GETENTRYSIZE                      = internalClassName(jbse.algo.meta.Algo_JAVA_ZIPFILE_GETENTRYSIZE.class.getName());
    public static final String ALGO_JAVA_ZIPFILE_GETENTRYTIME                      = internalClassName(jbse.algo.meta.Algo_JAVA_ZIPFILE_GETENTRYTIME.class.getName());
    public static final String ALGO_JAVA_ZIPFILE_GETNEXTENTRY                      = internalClassName(jbse.algo.meta.Algo_JAVA_ZIPFILE_GETNEXTENTRY.class.getName());
    public static final String ALGO_JAVA_ZIPFILE_GETTOTAL                          = internalClassName(jbse.algo.meta.Algo_JAVA_ZIPFILE_GETTOTAL.class.getName());
    public static final String ALGO_JAVA_ZIPFILE_OPEN                              = internalClassName(jbse.algo.meta.Algo_JAVA_ZIPFILE_OPEN.class.getName());
    public static final String ALGO_JAVA_ZIPFILE_READ                              = internalClassName(jbse.algo.meta.Algo_JAVA_ZIPFILE_READ.class.getName());
    public static final String ALGO_JAVA_ZIPFILE_STARTSWITHLOC                     = internalClassName(jbse.algo.meta.Algo_JAVA_ZIPFILE_STARTSWITHLOC.class.getName());
    public static final String ALGO_SUN_CONSTANTPOOL_GETUTF8AT0                    = internalClassName(jbse.algo.meta.Algo_SUN_CONSTANTPOOL_GETUTF8AT0.class.getName());
    public static final String ALGO_SUN_NATIVECONSTRUCTORACCESSORIMPL_NEWINSTANCE0 = internalClassName(jbse.algo.meta.Algo_SUN_NATIVECONSTRUCTORACCESSORIMPL_NEWINSTANCE0.class.getName());
    public static final String ALGO_SUN_NATIVEMETHODACCESSORIMPL_INVOKE0           = internalClassName(jbse.algo.meta.Algo_SUN_NATIVEMETHODACCESSORIMPL_INVOKE0.class.getName());
    public static final String ALGO_SUN_PERF_CREATELONG                            = internalClassName(jbse.algo.meta.Algo_SUN_PERF_CREATELONG.class.getName());
    public static final String ALGO_SUN_REFLECTION_GETCALLERCLASS                  = internalClassName(jbse.algo.meta.Algo_SUN_REFLECTION_GETCALLERCLASS.class.getName());
    public static final String ALGO_SUN_REFLECTION_GETCLASSACCESSFLAGS             = internalClassName(jbse.algo.meta.Algo_SUN_REFLECTION_GETCLASSACCESSFLAGS.class.getName());
    public static final String ALGO_SUN_UNIXNATIVEDISPATCHER_GETCWD                = internalClassName(jbse.algo.meta.Algo_SUN_UNIXNATIVEDISPATCHER_GETCWD.class.getName());
    public static final String ALGO_SUN_UNIXNATIVEDISPATCHER_INIT                  = internalClassName(jbse.algo.meta.Algo_SUN_UNIXNATIVEDISPATCHER_INIT.class.getName());
    public static final String ALGO_SUN_UNSAFE_ADDRESSSIZE                         = internalClassName(jbse.algo.meta.Algo_SUN_UNSAFE_ADDRESSSIZE.class.getName());
    public static final String ALGO_SUN_UNSAFE_ALLOCATEINSTANCE                    = internalClassName(jbse.algo.meta.Algo_SUN_UNSAFE_ALLOCATEINSTANCE.class.getName());
    public static final String ALGO_SUN_UNSAFE_ALLOCATEMEMORY                      = internalClassName(jbse.algo.meta.Algo_SUN_UNSAFE_ALLOCATEMEMORY.class.getName());
    public static final String ALGO_SUN_UNSAFE_COMPAREANDSWAPINT                   = internalClassName(jbse.algo.meta.Algo_SUN_UNSAFE_COMPAREANDSWAPINT.class.getName());
    public static final String ALGO_SUN_UNSAFE_COMPAREANDSWAPLONG                  = internalClassName(jbse.algo.meta.Algo_SUN_UNSAFE_COMPAREANDSWAPLONG.class.getName());
    public static final String ALGO_SUN_UNSAFE_COMPAREANDSWAPOBJECT                = internalClassName(jbse.algo.meta.Algo_SUN_UNSAFE_COMPAREANDSWAPOBJECT.class.getName());
    public static final String ALGO_SUN_UNSAFE_DEFINEANONYMOUSCLASS                = internalClassName(jbse.algo.meta.Algo_SUN_UNSAFE_DEFINEANONYMOUSCLASS.class.getName());
    public static final String ALGO_SUN_UNSAFE_DEFINECLASS                         = internalClassName(jbse.algo.meta.Algo_SUN_UNSAFE_DEFINECLASS.class.getName());
    public static final String ALGO_SUN_UNSAFE_ENSURECLASSINITIALIZED              = internalClassName(jbse.algo.meta.Algo_SUN_UNSAFE_ENSURECLASSINITIALIZED.class.getName());
    public static final String ALGO_SUN_UNSAFE_FREEMEMORY                          = internalClassName(jbse.algo.meta.Algo_SUN_UNSAFE_FREEMEMORY.class.getName());
    public static final String ALGO_SUN_UNSAFE_GETBYTE                             = internalClassName(jbse.algo.meta.Algo_SUN_UNSAFE_GETBYTE.class.getName());
    public static final String ALGO_SUN_UNSAFE_GETINT                              = internalClassName(jbse.algo.meta.Algo_SUN_UNSAFE_GETINT.class.getName());
    public static final String ALGO_SUN_UNSAFE_GETINT_O                            = internalClassName(jbse.algo.meta.Algo_SUN_UNSAFE_GETINT_O.class.getName());
    public static final String ALGO_SUN_UNSAFE_GETLONG                             = internalClassName(jbse.algo.meta.Algo_SUN_UNSAFE_GETLONG.class.getName());
    public static final String ALGO_SUN_UNSAFE_GETLONG_O                           = internalClassName(jbse.algo.meta.Algo_SUN_UNSAFE_GETLONG_O.class.getName());
    public static final String ALGO_SUN_UNSAFE_GETOBJECT_O                         = internalClassName(jbse.algo.meta.Algo_SUN_UNSAFE_GETOBJECT_O.class.getName());
    public static final String ALGO_SUN_UNSAFE_OBJECTFIELDOFFSET                   = internalClassName(jbse.algo.meta.Algo_SUN_UNSAFE_OBJECTFIELDOFFSET.class.getName());
    public static final String ALGO_SUN_UNSAFE_PAGESIZE                            = internalClassName(jbse.algo.meta.Algo_SUN_UNSAFE_PAGESIZE.class.getName());
    public static final String ALGO_SUN_UNSAFE_PUTINT                              = internalClassName(jbse.algo.meta.Algo_SUN_UNSAFE_PUTINT.class.getName());
    public static final String ALGO_SUN_UNSAFE_PUTINT_O                            = internalClassName(jbse.algo.meta.Algo_SUN_UNSAFE_PUTINT_O.class.getName());
    public static final String ALGO_SUN_UNSAFE_PUTLONG                             = internalClassName(jbse.algo.meta.Algo_SUN_UNSAFE_PUTLONG.class.getName());
    public static final String ALGO_SUN_UNSAFE_PUTLONG_O                           = internalClassName(jbse.algo.meta.Algo_SUN_UNSAFE_PUTLONG_O.class.getName());
    public static final String ALGO_SUN_UNSAFE_PUTOBJECT_O                         = internalClassName(jbse.algo.meta.Algo_SUN_UNSAFE_PUTOBJECT_O.class.getName());
    public static final String ALGO_SUN_UNSAFE_SHOULDBEINITIALIZED                 = internalClassName(jbse.algo.meta.Algo_SUN_UNSAFE_SHOULDBEINITIALIZED.class.getName());
    public static final String ALGO_SUN_UNSAFE_STATICFIELDBASE                     = internalClassName(jbse.algo.meta.Algo_SUN_UNSAFE_STATICFIELDBASE.class.getName());
    public static final String ALGO_SUN_UNSAFE_STATICFIELDOFFSET                   = internalClassName(jbse.algo.meta.Algo_SUN_UNSAFE_STATICFIELDOFFSET.class.getName());

    //Overriding meta-level implementations of jbse.meta.Analysis methods
    public static final String ALGO_JBSE_ANALYSIS_ANY                       = internalClassName(jbse.algo.meta.Algo_JBSE_ANALYSIS_ANY.class.getName());
    public static final String ALGO_JBSE_ANALYSIS_ASSUMECLASSNOTINITIALIZED = internalClassName(jbse.algo.meta.Algo_JBSE_ANALYSIS_ASSUMECLASSNOTINITIALIZED.class.getName());
    public static final String ALGO_JBSE_ANALYSIS_ENDGUIDANCE               = internalClassName(jbse.algo.meta.Algo_JBSE_ANALYSIS_ENDGUIDANCE.class.getName());
    public static final String ALGO_JBSE_ANALYSIS_FAIL                      = internalClassName(jbse.algo.meta.Algo_JBSE_ANALYSIS_FAIL.class.getName());
    public static final String ALGO_JBSE_ANALYSIS_IGNORE                    = internalClassName(jbse.algo.meta.Algo_JBSE_ANALYSIS_IGNORE.class.getName());
    public static final String ALGO_JBSE_ANALYSIS_ISRESOLVED                = internalClassName(jbse.algo.meta.Algo_JBSE_ANALYSIS_ISRESOLVED.class.getName());
    public static final String ALGO_JBSE_ANALYSIS_ISRESOLVEDBYALIAS         = internalClassName(jbse.algo.meta.Algo_JBSE_ANALYSIS_ISRESOLVEDBYALIAS.class.getName());
    public static final String ALGO_JBSE_ANALYSIS_ISRESOLVEDBYEXPANSION     = internalClassName(jbse.algo.meta.Algo_JBSE_ANALYSIS_ISRESOLVEDBYEXPANSION.class.getName());
    public static final String ALGO_JBSE_ANALYSIS_ISSYMBOLIC                = internalClassName(jbse.algo.meta.Algo_JBSE_ANALYSIS_ISSYMBOLIC.class.getName());
    public static final String ALGO_JBSE_ANALYSIS_SUCCEED                   = internalClassName(jbse.algo.meta.Algo_JBSE_ANALYSIS_SUCCEED.class.getName());
    public static final String ALGO_JBSE_ANALYSIS_SYMBOLNAME                = internalClassName(jbse.algo.meta.Algo_JBSE_ANALYSIS_SYMBOLNAME.class.getName());

    //Overriding meta-level implementations of jbse.base.Base methods
    public static final String ALGO_JBSE_BASE_CLINIT                        = internalClassName(jbse.algo.meta.Algo_JBSE_BASE_CLINIT.class.getName());
    public static final String ALGO_JBSE_BASE_MAKEKLASSSYMBOLIC_DO          = internalClassName(jbse.algo.meta.Algo_JBSE_BASE_MAKEKLASSSYMBOLIC_DO.class.getName());

    //Overriding meta-level implementations of jbse.base.JAVA_MAP and jbse.base.JAVA_CONCURRENTMAP methods
    public static final String ALGO_JBSE_JAVA_CONCURRENTMAP_ONKEYRESOLUTION0                 = internalClassName(jbse.algo.meta.Algo_JBSE_JAVA_CONCURRENTMAP_ONKEYRESOLUTION0.class.getName());
    public static final String ALGO_JBSE_JAVA_CONCURRENTMAP_REFINEONFRESHENTRYANDBRANCH      = internalClassName(jbse.algo.meta.Algo_JBSE_JAVA_CONCURRENTMAP_REFINEONFRESHENTRYANDBRANCH.class.getName());
    public static final String ALGO_JBSE_JAVA_CONCURRENTMAP_REFINEONKEYANDBRANCH             = internalClassName(jbse.algo.meta.Algo_JBSE_JAVA_CONCURRENTMAP_REFINEONKEYANDBRANCH.class.getName());
    public static final String ALGO_JBSE_JAVA_CONCURRENTMAP_REFINEONKEYCOMBINATIONSANDBRANCH = internalClassName(jbse.algo.meta.Algo_JBSE_JAVA_CONCURRENTMAP_REFINEONKEYCOMBINATIONSANDBRANCH.class.getName());
    public static final String ALGO_JBSE_JAVA_CONCURRENTMAP_REFINEONVALUEANDBRANCH           = internalClassName(jbse.algo.meta.Algo_JBSE_JAVA_CONCURRENTMAP_REFINEONVALUEANDBRANCH.class.getName());
    public static final String ALGO_JBSE_JAVA_LINKEDMAP_ONKEYRESOLUTION0                     = internalClassName(jbse.algo.meta.Algo_JBSE_JAVA_LINKEDMAP_ONKEYRESOLUTION0.class.getName());
    public static final String ALGO_JBSE_JAVA_LINKEDMAP_REFINEONFRESHENTRYANDBRANCH          = internalClassName(jbse.algo.meta.Algo_JBSE_JAVA_LINKEDMAP_REFINEONFRESHENTRYANDBRANCH.class.getName());
    public static final String ALGO_JBSE_JAVA_LINKEDMAP_REFINEONKEYANDBRANCH                 = internalClassName(jbse.algo.meta.Algo_JBSE_JAVA_LINKEDMAP_REFINEONKEYANDBRANCH.class.getName());
    public static final String ALGO_JBSE_JAVA_LINKEDMAP_REFINEONKEYCOMBINATIONSANDBRANCH     = internalClassName(jbse.algo.meta.Algo_JBSE_JAVA_LINKEDMAP_REFINEONKEYCOMBINATIONSANDBRANCH.class.getName());
    public static final String ALGO_JBSE_JAVA_LINKEDMAP_REFINEONVALUEANDBRANCH               = internalClassName(jbse.algo.meta.Algo_JBSE_JAVA_LINKEDMAP_REFINEONVALUEANDBRANCH.class.getName());
    public static final String ALGO_JBSE_JAVA_MAP_ONKEYRESOLUTION0                           = internalClassName(jbse.algo.meta.Algo_JBSE_JAVA_MAP_ONKEYRESOLUTION0.class.getName());
    public static final String ALGO_JBSE_JAVA_MAP_REFINEONFRESHENTRYANDBRANCH                = internalClassName(jbse.algo.meta.Algo_JBSE_JAVA_MAP_REFINEONFRESHENTRYANDBRANCH.class.getName());
    public static final String ALGO_JBSE_JAVA_MAP_REFINEONKEYANDBRANCH                       = internalClassName(jbse.algo.meta.Algo_JBSE_JAVA_MAP_REFINEONKEYANDBRANCH.class.getName());
    public static final String ALGO_JBSE_JAVA_MAP_REFINEONKEYCOMBINATIONSANDBRANCH           = internalClassName(jbse.algo.meta.Algo_JBSE_JAVA_MAP_REFINEONKEYCOMBINATIONSANDBRANCH.class.getName());
    public static final String ALGO_JBSE_JAVA_MAP_REFINEONVALUEANDBRANCH                     = internalClassName(jbse.algo.meta.Algo_JBSE_JAVA_MAP_REFINEONVALUEANDBRANCH.class.getName());
    public static final String ALGO_JBSE_JAVA_XMAP_MAKEINITIAL                               = internalClassName(jbse.algo.meta.Algo_JBSE_JAVA_XMAP_MAKEINITIAL.class.getName());
    public static final String ALGO_JBSE_JAVA_XMAP_METATHROWUNEXPECTEDINTERNALEXCEPTION      = internalClassName(jbse.algo.meta.Algo_JBSE_JAVA_XMAP_METATHROWUNEXPECTEDINTERNALEXCEPTION.class.getName());
    public static final String ALGO_JBSE_JAVA_XMAP_NOTIFYMETHODEXECUTION                     = internalClassName(jbse.algo.meta.Algo_JBSE_JAVA_XMAP_NOTIFYMETHODEXECUTION.class.getName());
    
    //Overriding meta-level implementations of JBSE classless (pseudo)methods
    public static final String ALGO_noclass_REGISTERLOADEDCLASS                   = internalClassName(jbse.algo.meta.Algo_noclass_REGISTERLOADEDCLASS.class.getName());
    public static final String ALGO_noclass_REGISTERMETHODHANDLE                  = internalClassName(jbse.algo.meta.Algo_noclass_REGISTERMETHODHANDLE.class.getName());
    public static final String ALGO_noclass_REGISTERMETHODTYPE                    = internalClassName(jbse.algo.meta.Algo_noclass_REGISTERMETHODTYPE.class.getName());
    public static final String ALGO_noclass_SETSTANDARDCLASSLOADERSREADY          = internalClassName(jbse.algo.meta.Algo_noclass_SETSTANDARDCLASSLOADERSREADY.class.getName());
    public static final String ALGO_noclass_STORELINKEDMETHODADAPTERANDAPPENDIX   = internalClassName(jbse.algo.meta.Algo_noclass_STORELINKEDMETHODADAPTERANDAPPENDIX.class.getName());
    public static final String ALGO_noclass_STORELINKEDCALLSITEADAPTERANDAPPENDIX = internalClassName(jbse.algo.meta.Algo_noclass_STORELINKEDCALLSITEADAPTERANDAPPENDIX.class.getName());
    
    //Overriding base-level implementation of standard methods
    private static final String JBSE_BASE = internalClassName(jbse.base.Base.class.getName());
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
