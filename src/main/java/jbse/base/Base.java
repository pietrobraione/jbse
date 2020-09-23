package jbse.base;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.security.AccessControlContext;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.HashMap;
import java.util.Properties;

import jbse.meta.annotations.MetaOverriddenBy;
import sun.misc.Unsafe;

/**
 * Some base-level overriding implementations of methods. 
 * 
 * @author Pietro Braione
 *
 */
public final class Base {
    //Properties to be set at the meta level
    private static final String JBSE_VERSION            = null;
    private static final String JBSE_NAME               = null;
    private static final String JAVA_EXT_DIRS           = null;
    private static final String SUN_BOOT_LIBRARY_PATH   = null;
    private static final String JAVA_LIBRARY_PATH       = null;
    private static final String JAVA_HOME               = null;
    private static final String SUN_BOOT_CLASS_PATH     = null;
    private static final String JAVA_CLASS_PATH         = null;
    private static final String JAVA_VERSION            = null;
    private static final String OS_NAME                 = null;
    private static final String OS_VERSION              = null;
    private static final String OS_ARCH                 = null;
    private static final String FILE_SEPARATOR          = null;
    private static final String PATH_SEPARATOR          = null;
    private static final String LINE_SEPARATOR          = null;
    private static final String USER_LANGUAGE           = null;
    private static final String USER_SCRIPT             = null;
    private static final String USER_COUNTRY            = null;
    private static final String USER_VARIANT            = null;
    private static final String FILE_ENCODING           = null;
    private static final String SUN_JNU_ENCODING        = null;
    private static final String SUN_STDOUT_ENCODING     = null;
    private static final String SUN_STDERR_ENCODING     = null;
    private static final String SUN_IO_UNICODE_ENCODING = null;
    private static final String SUN_CPU_ISALIST         = null;
    private static final String SUN_CPU_ENDIAN          = null;
    private static final String HTTP_PROXYHOST          = null;
    private static final String HTTP_PROXYPORT          = null;
    private static final String HTTPS_PROXYHOST         = null;
    private static final String HTTPS_PROXYPORT         = null;
    private static final String FTP_PROXYHOST           = null;
    private static final String FTP_PROXYPORT           = null;
    private static final String SOCKSPROXYHOST          = null;
    private static final String SOCKSPROXYPORT          = null;
    private static final String GOPHERPROXYSET          = null;
    private static final String GOPHERPROXYHOST         = null;
    private static final String GOPHERPROXYPORT         = null;
    private static final String HTTP_NONPROXYHOSTS      = null;
    private static final String FTP_NONPROXYHOSTS       = null;
    private static final String SOCKSNONPROXYHOSTS      = null;
    private static final String JAVA_AWT_PRINTERJOB     = null;
    private static final String SUN_ARCH_DATA_MODEL     = null;
    private static final String SUN_OS_PATCH_LEVEL      = null;
    private static final String JAVA_AWT_GRAPHICSENV    = null;
    private static final String SUN_JAVA2D_FONTPATH     = null;
    private static final String JAVA_IO_TMPDIR          = null;
    private static final String USER_NAME               = null;
    private static final String USER_HOME               = null;
    private static final String USER_TIMEZONE           = null;
    private static final String USER_DIR                = null;
    private static final String SUN_DESKTOP             = null;

    static {
        clinit();
    }

    /**
     * Sets the {@code private static final String} fields
     * of this class to the values (if exist) of the 
     * corresponding properties at the meta-level.
     */
    @MetaOverriddenBy("jbse/algo/meta/Algo_JBSE_BASE_CLINIT")
    private static native void clinit();
    
    /**
     * Overriding implementation of {@link java.security.AccessController#doPrivileged(PrivilegedExceptionAction)}.
     * @see java.security.AccessController#doPrivileged(PrivilegedExceptionAction)
     */
    private static Object base_JAVA_ACCESSCONTROLLER_DOPRIVILEGED_EXCEPTION(PrivilegedExceptionAction<?> action)
    throws PrivilegedActionException {
        //since JBSE does not enforce access control we just execute the action
        try {
            return action.run();
        } catch (RuntimeException e) {
            throw e; //runtime exceptions propagate
        } catch (Exception e) {
            throw new PrivilegedActionException(e); //not explicitly told, but this is the only sensible behavior
        }
    }

    /**
     * Overriding implementation of {@link java.security.AccessController#doPrivileged(PrivilegedExceptionAction, AccessControlContext)}.
     * @see java.security.AccessController#doPrivileged(PrivilegedExceptionAction, AccessControlContext)
     */
    private static Object base_JAVA_ACCESSCONTROLLER_DOPRIVILEGED_EXCEPTION(PrivilegedExceptionAction<?> action, AccessControlContext context)
    throws PrivilegedActionException {
        //since JBSE does not enforce access control we just execute the action
        try {
            return action.run();
        } catch (RuntimeException e) {
            throw e; //runtime exceptions propagate
        } catch (Exception e) {
            throw new PrivilegedActionException(e); //not explicitly told, but this is the only sensible behavior
        }
    }

    /**
     * Overriding implementation of {@link java.security.AccessController#doPrivileged(PrivilegedAction)}.
     * @see java.security.AccessController#doPrivileged(PrivilegedAction)
     */
    private static Object base_JAVA_ACCESSCONTROLLER_DOPRIVILEGED_NOEXCEPTION(PrivilegedAction<?> action)
    throws PrivilegedActionException {
        //since JBSE does not enforce access control we just execute the action
        return action.run();
    }

    /**
     * Overriding implementation of {@link java.security.AccessController#doPrivileged(PrivilegedExceptionAction, AccessControlContext)}.
     * @see java.security.AccessController#doPrivileged(PrivilegedExceptionAction, AccessControlContext)
     */
    private static Object base_JAVA_ACCESSCONTROLLER_DOPRIVILEGED_NOEXCEPTION(PrivilegedAction<?> action, AccessControlContext context)
    throws PrivilegedActionException {
        //since JBSE does not enforce access control we just execute the action
    	return action.run();
    }

    /**
     * Overriding implementation of {@link java.security.AccessController#getStackAccessControlContext()}.
     * @see java.security.AccessController#getStackAccessControlContext()
     */
    private static AccessControlContext base_JAVA_ACCESSCONTROLLER_GETSTACKACCESSCONTROLCONTEXT() {
        //JBSE does not (yet) check access control, so a dummy null context is returned signifying
        //privileged access (or so it seems).
        return null;
    }
    
    /**
     * Overriding implementation of {@link java.util.concurrent.atomic.AtomicLong#VMSupportsCS8()}.
     * @see java.util.concurrent.atomic.AtomicLong#VMSupportsCS8()
     */
    private static boolean base_JAVA_ATOMICLONG_VMSUPPORTSCS8() {
        //JBSE trivially supports atomic compare-and-swap for
        //all data types
        return true;
    }

    /**
     * Overriding implementation of {@link java.lang.Class#desiredAssertionStatus0(Class)}.
     * @see java.lang.Class#desiredAssertionStatus0(Class)
     */
    private static boolean base_JAVA_CLASS_DESIREDASSERTIONSTATUS0(Class<?> clazz) {
        return false; //no assertions, sorry
        //TODO should we give a way to control the assertion status, possibly handling Java assertions as JBSE assertions?
    }
    
    /**
     * Overriding implementation of {@link java.lang.ClassLoader#findBuiltinLib(String)}.
     * @see java.lang.ClassLoader#findBuiltinLib(String)
     */
    private static String base_JAVA_CLASSLOADER_FINDBUILTINLIB(String file) {
        //we assume that all the libraries are builtin and at the same path!!!
        return "/usr/lib/" + file;
        //TODO if the platform is Windows, then return a Windows path
    }
    
    //from java.lang.invoke.MethodHandleNatives.Constants
    private static final int GC_COUNT_GWT = 4;
    
    /**
     * Overriding implementation of {@link java.lang.invoke.MethodHandleNatives#getConstant(int)}.
     * @see java.lang.invoke.MethodHandleNatives#getConstant(int)
     */
    private static int base_JAVA_METHODHANDLENATIVES_GETCONSTANT(int what) {
        if (what == GC_COUNT_GWT) {
            return 1;
        } else {
            return 0;
        }
    }
    
    /**
     * Overriding implementation of {@link java.lang.Runtime#availableProcessors()}.
     * @see java.lang.Runtime#availableProcessors()
     */
    private static int base_JAVA_RUNTIME_AVAILABLEPROCESSORS(Runtime _this) {
        return 1; //just one processor for JBSE, sorry
    }

    /**
     * Puts a key-value pair in a {@link Properties} object, 
     * if the value is not null, otherwise does nothing.
     * 
     * @param p the {@link Properties}.
     * @param key a {@link String}, the key.
     * @param value a {@link String}, the value.
     */
    private static void putSafe(Properties p, String key, String value) {
        if (value != null) {
            p.put(key, value);
        }
    }
    
    /**
     * Overriding implementation of {@link java.lang.System#initProperties(Properties)}.
     * @see java.lang.System#initProperties(Properties)
     */
    private static Properties base_JAVA_SYSTEM_INITPROPERTIES(Properties p) {
        //properties taken from openjdk 8, hotspot:src/share/vm/runtime/arguments.cpp
        putSafe(p, "java.vm.specification.name",    "Java Virtual Machine Specification");
        putSafe(p, "java.vm.version",               JBSE_VERSION);
        putSafe(p, "java.vm.name",                  JBSE_NAME);
        putSafe(p, "java.vm.info",                  "");
        putSafe(p, "java.ext.dirs",                 JAVA_EXT_DIRS);
        putSafe(p, "java.endorsed.dirs",            ""); //TODO currently unsupported, study and support
        putSafe(p, "sun.boot.library.path",         SUN_BOOT_LIBRARY_PATH);
        putSafe(p, "java.library.path",             JAVA_LIBRARY_PATH);
        putSafe(p, "java.home",                     JAVA_HOME);
        putSafe(p, "sun.boot.class.path",           SUN_BOOT_CLASS_PATH);
        putSafe(p, "java.class.path",               JAVA_CLASS_PATH);
        putSafe(p, "java.vm.specification.vendor",  "Oracle Corporation");
        putSafe(p, "java.vm.specification.version", "1.8");
        putSafe(p, "java.vm.vendor",                "JBSE project");
        
        //properties taken from openjdk 8, jdk:src/share/native/java/lang/System.c
        putSafe(p, "java.specification.version", "1.8");
        putSafe(p, "java.specification.name",    "Java Platform API Specification");
        putSafe(p, "java.specification.vendor",  "Oracle Corporation");
        putSafe(p, "java.version",               JAVA_VERSION);
        putSafe(p, "java.vendor",                "Oracle Corporation");
        putSafe(p, "java.vendor.url",            "http://java.oracle.com/");
        putSafe(p, "java.vendor.url.bug",        "http://bugreport.sun.com/bugreport/");
        putSafe(p, "java.class.version",         "52.0");
        putSafe(p, "os.name",                    OS_NAME);
        putSafe(p, "os.version",                 OS_VERSION);
        putSafe(p, "os.arch",                    OS_ARCH);
        putSafe(p, "file.separator",             FILE_SEPARATOR);
        putSafe(p, "path.separator",             PATH_SEPARATOR);
        putSafe(p, "line.separator",             LINE_SEPARATOR);
        putSafe(p, "user.language",              USER_LANGUAGE);
        putSafe(p, "user.script",                USER_SCRIPT);
        putSafe(p, "user.country",               USER_COUNTRY);
        putSafe(p, "user.variant",               USER_VARIANT);
        putSafe(p, "file.encoding",              FILE_ENCODING);
        putSafe(p, "sun.jnu.encoding",           SUN_JNU_ENCODING);
        putSafe(p, "sun.stdout.encoding",        SUN_STDOUT_ENCODING);
        putSafe(p, "sun.stderr.encoding",        SUN_STDERR_ENCODING);
        putSafe(p, "file.encoding.pkg",          "sun.io");
        putSafe(p, "sun.io.unicode.encoding",    SUN_IO_UNICODE_ENCODING);
        putSafe(p, "sun.cpu.isalist",            SUN_CPU_ISALIST);
        putSafe(p, "sun.cpu.endian",             SUN_CPU_ENDIAN);
        putSafe(p, "http.proxyHost",             HTTP_PROXYHOST);
        putSafe(p, "http.proxyPort",             HTTP_PROXYPORT);
        putSafe(p, "https.proxyHost",            HTTPS_PROXYHOST);
        putSafe(p, "https.proxyPort",            HTTPS_PROXYPORT);
        putSafe(p, "ftp.proxyHost",              FTP_PROXYHOST);
        putSafe(p, "ftp.proxyPort",              FTP_PROXYPORT);
        putSafe(p, "socksProxyHost",             SOCKSPROXYHOST);
        putSafe(p, "socksProxyPort",             SOCKSPROXYPORT);
        putSafe(p, "gopherProxySet",             GOPHERPROXYSET);
        putSafe(p, "gopherProxyHost",            GOPHERPROXYHOST);
        putSafe(p, "gopherProxyPort",            GOPHERPROXYPORT);
        putSafe(p, "http.nonProxyHosts",         HTTP_NONPROXYHOSTS);
        putSafe(p, "ftp.nonProxyHosts",          FTP_NONPROXYHOSTS);
        putSafe(p, "socksNonProxyHosts",         SOCKSNONPROXYHOSTS);
        putSafe(p, "java.awt.printerjob",        JAVA_AWT_PRINTERJOB);
        putSafe(p, "sun.arch.data.model",        SUN_ARCH_DATA_MODEL);
        putSafe(p, "sun.os.patch.level",         SUN_OS_PATCH_LEVEL);
        putSafe(p, "java.awt.graphicsenv",       JAVA_AWT_GRAPHICSENV);
        putSafe(p, "sun.java2d.fontpath",        SUN_JAVA2D_FONTPATH);
        putSafe(p, "java.io.tmpdir",             JAVA_IO_TMPDIR);
        putSafe(p, "user.name",                  USER_NAME);
        putSafe(p, "user.home",                  USER_HOME);
        putSafe(p, "user.timezone",              USER_TIMEZONE);
        putSafe(p, "user.dir",                   USER_DIR);
        putSafe(p, "sun.desktop",                SUN_DESKTOP);
        
        return p;
    }
    
    /**
     * Used as overriding implementation of several {@code registerNatives()}, 
     * {@code initIDs()}, and other native static initialization methods.
     */
    private static void doNothing() {
        return;
    }
    
    /**
     * Overriding implementation of {@link java.lang.Object#notify()} and
     * {@link java.lang.Object#notifyAll()}.
     * @see java.lang.Object#notify()
     * @see java.lang.Object#notifyAll()
     */
    private static void base_JAVA_OBJECT_NOTIFY(Object _this) {
        //no concurrency
        return;
    }
    
    /**
     * Overriding implementation of {@link java.lang.Object#wait(long)}.
     * @see java.lang.Object#wait(long)
     */
    private static void base_JAVA_OBJECT_WAIT(Object _this, long timeout) {
        //no concurrency
        return;
    }
    
    /**
     * Overriding implementation of {@link java.lang.Thread#isAlive()}.
     * @see java.lang.Thread#isAlive()
     */
    private static boolean base_JAVA_THREAD_ISALIVE(Thread _this) {
        //in JBSE there is only one thread alive, the current thread
        return (_this == Thread.currentThread());
    }
    
    /**
     * Overriding implementation of {@link jbse.meta.Analysis#isRunByJBSE()}.
     * @see jbse.meta.Analysis#isRunByJBSE()
     */
    private static boolean base_JBSE_ANALYSIS_ISRUNBYJBSE() {
        return true;
    }

    /**
     * Overriding implementation of {@link sun.misc.Signal#findSignal(String)}.
     * @see sun.misc.Signal#findSignal(String)
     */
    private static int base_SUN_SIGNAL_FINDSIGNAL(String signal) {
        //we use the standard POSIX signal numbers for the three
        //signals used in the standard library
        switch (signal) {
        case "HUP":
            return 1;
        case "INT":
            return 2;
        case "TERM":
            return 15;
        default:
            return -1;
        }
        //TODO more signals?
    }
    
    private static final HashMap<Integer, Long> SIG_HANDLERS = new HashMap<>();
    
    /**
     * Overriding implementation of {@link sun.misc.Signal#handle0(int, long)}.
     * @see sun.misc.Signal#handle0(int, long)
     */
    private static long base_SUN_SIGNAL_HANDLE0(int signal, long nativeHandler) {
        //does nothing: JBSE does not handle signals!
        final Long oldHandler = SIG_HANDLERS.put(signal, nativeHandler);
        if (oldHandler == null) {
            return 1; //handler 1 == ignore the signal
        } else {
            return oldHandler.longValue();
        }
    }

    /**
     * Overriding implementation of {@link sun.misc.Unsafe#arrayBaseOffset(Class)}.
     * @see sun.misc.Unsafe#arrayBaseOffset(Class)
     */
    private static int base_SUN_UNSAFE_ARRAYBASEOFFSET(Unsafe _this, Class<?> arrayClass) {
        //JBSE raw array offsets are plain array indices, so base is zero
        return 0; 
    }

    /**
     * Overriding implementation of {@link sun.misc.Unsafe#arrayIndexScale(Class)}.
     * @see sun.misc.Unsafe#arrayIndexScale(Class)
     */
    private static int base_SUN_UNSAFE_ARRAYINDEXSCALE(Unsafe _this, Class<?> arrayClass) {
        //JBSE raw array offsets are plain array indices, so scale is one
        return 1; 
    }
    
    /**
     * Overriding implementation of {@link sun.misc.Unsafe#fullFence()}.
     * @see sun.misc.Unsafe#fullFence()
     */
    private static void base_SUN_UNSAFE_FULLFENCE(Unsafe _this) {
        //no concurrency in JBSE
        return; 
    }
    
    /**
     * Overriding implementation of {@link sun.misc.Unsafe#park(boolean, long)}.
     * @see sun.misc.Unsafe#park(boolean, long)
     */
    private static void base_SUN_UNSAFE_PARK(Unsafe _this, boolean isAbsolute, long time) {
        //no threads in JBSE
        return; 
    }
    
    /**
     * Overriding implementation of {@link sun.misc.Unsafe#unpark(Object)}.
     * @see sun.misc.Unsafe#unpark(Object)
     */
    private static void base_SUN_UNSAFE_UNPARK(Unsafe _this, Object thread) {
        //no threads in JBSE
        return; 
    }
    
    /**
     * Overriding implementation of {@link sun.misc.URLClassPath#getLookupCacheURLs(ClassLoader)}.
     * @see sun.misc.URLClassPath#getLookupCacheURLs(ClassLoader)
     */
    private static URL[] base_SUN_URLCLASSPATH_GETLOOKUPCACHEURLS(ClassLoader loader) {
        //no caches, sorry
        return null;
    }
    
    private static byte foo_B() throws Exception { return (byte) 0; }
    
    private static char foo_C() throws Exception { return '\u0000'; }
    
    private static double foo_D() throws Exception { return 0.0d; }
    
    private static float foo_F() throws Exception { return 0.0f; }
    
    private static int foo_I() throws Exception { return 0; }
    
    private static long foo_J() throws Exception { return 0L; }
    
    private static Object foo_L() throws Exception { return null; }
    
    private static short foo_S() throws Exception { return (short) 0; }
    
    private static void foo_V() throws Exception { }
    
    private static boolean foo_Z() throws Exception { return false; }
    
    /**
     * Helper method for {@link sun.reflect.NativeMethodAccessorImpl#invoke0(Method, Object, Object[])}.
     * Boxes the exceptions that are raised by the execution of a method 
     * into an {@link InvocationTargetException} and rethrows them. Also, 
     * boxes the return value (of primitive type byte) from the execution of 
     * the method and returns it.
     */
    private static Object boxInvocationTargetExceptionAndReturn_B() 
    throws InvocationTargetException {
        try {
            final byte b = foo_B(); //skip this
            return Byte.valueOf(b);
        } catch (Exception e) {
            throw new InvocationTargetException(e);
        }
    }
    
    /**
     * Helper method for {@link sun.reflect.NativeMethodAccessorImpl#invoke0(Method, Object, Object[])}.
     * Boxes the exceptions that are raised by the execution of a method 
     * into an {@link InvocationTargetException} and rethrows them. Also, 
     * boxes the return value (of primitive type char) from the execution of 
     * the method and returns it.
     */
    private static Object boxInvocationTargetExceptionAndReturn_C() 
    throws InvocationTargetException {
        try {
            final char c = foo_C(); //skip this
            return Character.valueOf(c);
        } catch (Exception e) {
            throw new InvocationTargetException(e);
        }
    }
    
    /**
     * Helper method for {@link sun.reflect.NativeMethodAccessorImpl#invoke0(Method, Object, Object[])}.
     * Boxes the exceptions that are raised by the execution of a method 
     * into an {@link InvocationTargetException} and rethrows them. Also, 
     * boxes the return value (of primitive type double) from the execution of 
     * the method and returns it.
     */
    private static Object boxInvocationTargetExceptionAndReturn_D() 
    throws InvocationTargetException {
        try {
            final double d = foo_D(); //skip this
            return Double.valueOf(d);
        } catch (Exception e) {
            throw new InvocationTargetException(e);
        }
    }
    
    /**
     * Helper method for {@link sun.reflect.NativeMethodAccessorImpl#invoke0(Method, Object, Object[])}.
     * Boxes the exceptions that are raised by the execution of a method 
     * into an {@link InvocationTargetException} and rethrows them. Also, 
     * boxes the return value (of primitive type float) from the execution of 
     * the method and returns it.
     */
    private static Object boxInvocationTargetExceptionAndReturn_F() 
    throws InvocationTargetException {
        try {
            final float f = foo_F(); //skip this
            return Float.valueOf(f);
        } catch (Exception e) {
            throw new InvocationTargetException(e);
        }
    }
    
    /**
     * Helper method for {@link sun.reflect.NativeMethodAccessorImpl#invoke0(Method, Object, Object[])}.
     * Boxes the exceptions that are raised by the execution of a method 
     * into an {@link InvocationTargetException} and rethrows them. Also, 
     * boxes the return value (of primitive type int) from the execution of 
     * the method and returns it.
     */
    private static Object boxInvocationTargetExceptionAndReturn_I() 
    throws InvocationTargetException {
        try {
            final int i = foo_I(); //skip this
            return Integer.valueOf(i);
        } catch (Exception e) {
            throw new InvocationTargetException(e);
        }
    }
    
    /**
     * Helper method for {@link sun.reflect.NativeMethodAccessorImpl#invoke0(Method, Object, Object[])}.
     * Boxes the exceptions that are raised by the execution of a method 
     * into an {@link InvocationTargetException} and rethrows them. Also, 
     * boxes the return value (of primitive type long) from the execution of 
     * the method and returns it.
     */
    private static Object boxInvocationTargetExceptionAndReturn_J() 
    throws InvocationTargetException {
        try {
            final long j = foo_J(); //skip this
            return Long.valueOf(j);
        } catch (Exception e) {
            throw new InvocationTargetException(e);
        }
    }
    
    /**
     * Helper method for {@link sun.reflect.NativeMethodAccessorImpl#invoke0(Method, Object, Object[])}.
     * Boxes the exceptions that are raised by the execution of a method 
     * into an {@link InvocationTargetException} and rethrows them. Also, 
     * takes the return value (object or array) from the execution of 
     * the method and returns it.
     */
    private static Object boxInvocationTargetExceptionAndReturn_L() 
    throws InvocationTargetException {
        try {
            final Object l = foo_L(); //skip this
            return l;
        } catch (Exception e) {
            throw new InvocationTargetException(e);
        }
    }
    
    /**
     * Helper method for {@link sun.reflect.NativeMethodAccessorImpl#newInstance0(Constructor, Object[])}
     * when it has void return type.
     * Boxes the exceptions that are raised by the execution of a method 
     * into an {@link InvocationTargetException} and rethrows them. Also, 
     * returns {@code null} to comply with the fact that {@code newInstance0} 
     * has {@code Object} as a return type. 
     */
    private static Object boxInvocationTargetExceptionAndReturn_null() 
    throws InvocationTargetException {
        try {
            foo_V(); //skip this
            return null;
        } catch (Exception e) {
            throw new InvocationTargetException(e);
        }
    }

    /**
     * Helper method for {@link sun.reflect.NativeMethodAccessorImpl#invoke0(Method, Object, Object[])}.
     * Boxes the exceptions that are raised by the execution of a method 
     * into an {@link InvocationTargetException} and rethrows them. Also, 
     * boxes the return value (of primitive type short) from the execution of 
     * the method and returns it.
     */
    private static Object boxInvocationTargetExceptionAndReturn_S() 
    throws InvocationTargetException {
        try {
            final short s = foo_S(); //skip this
            return Short.valueOf(s);
        } catch (Exception e) {
            throw new InvocationTargetException(e);
        }
    }
    
    /**
     * Helper method for {@link sun.reflect.NativeConstructorAccessorImpl#newInstance0(Constructor, Object[])}.
     * Boxes the exceptions that are raised by the execution of a constructor 
     * into an {@link InvocationTargetException} and rethrows them.
     */
    private static void boxInvocationTargetExceptionAndReturn_V() 
    throws InvocationTargetException {
        try {
            foo_V(); //skip this
            return;
        } catch (Exception e) {
            throw new InvocationTargetException(e);
        }
    }
    
    /**
     * Helper method for {@link sun.reflect.NativeMethodAccessorImpl#invoke0(Method, Object, Object[])}.
     * Boxes the exceptions that are raised by the execution of a method 
     * into an {@link InvocationTargetException} and rethrows them. Also, 
     * boxes the return value (of primitive type boolean) from the execution of 
     * the method and returns it.
     */
    private static Object boxInvocationTargetExceptionAndReturn_Z() 
    throws InvocationTargetException {
        try {
            final boolean z = foo_Z(); //skip this
            return Boolean.valueOf(z);
        } catch (Exception e) {
            throw new InvocationTargetException(e);
        }
    }
    
    /**
     * Helper method for {@link java.lang.Class#forName0(String, boolean, ClassLoader, Class)}.
     * Just boxes the exceptions that are raised by the execution of a constructor 
     * into an {@link InvocationTargetException} and rethrows them.
     */
    private static void boxExceptionInInitializerError()
    throws ExceptionInInitializerError {
        try {
            foo_V(); //does nothing, it's just to force the compiler to generate the catch block
        } catch (Exception e) {
            throw new ExceptionInInitializerError(e);
        }
    }
    
    /**
     * Helper method for class initialization. When a Klass 
     * that is assumed to be pre-initialized is initialized by
     * running the static initializer, it contains concrete 
     * references and has no origin. This method makes it 
     * symbolic.
     * 
     * @param definingClassLoader an {@code int}, the number of the defining
     *        classloader of the class. 
     * @param className a {@link String}, the name of the class 
     *        in internal format.
     */
    private static void makeKlassSymbolic(int definingClassLoader, String className) {
    	makeKlassSymbolic_do(definingClassLoader, className); //just redispatch to the meta implementation
    }
    
    private static native void makeKlassSymbolic_do(int definingClassLoader, String className);

    private Base() {
        //do not instantiate!
        throw new AssertionError();
    }
}
