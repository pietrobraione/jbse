package jbse.algo.meta;

import static jbse.algo.UtilControlFlow.exitFromAlgorithm;
import static jbse.algo.UtilControlFlow.failExecution;
import static jbse.algo.UtilControlFlow.throwNew;
import static jbse.bc.ClassLoaders.CLASSLOADER_APP;
import static jbse.bc.Signatures.JBSE_BASE;
import static jbse.bc.Signatures.JBSE_BASE_FILE_ENCODING;
import static jbse.bc.Signatures.JBSE_BASE_FILE_SEPARATOR;
import static jbse.bc.Signatures.JBSE_BASE_FTP_NONPROXYHOSTS;
import static jbse.bc.Signatures.JBSE_BASE_FTP_PROXYHOST;
import static jbse.bc.Signatures.JBSE_BASE_FTP_PROXYPORT;
import static jbse.bc.Signatures.JBSE_BASE_GOPHERPROXYHOST;
import static jbse.bc.Signatures.JBSE_BASE_GOPHERPROXYPORT;
import static jbse.bc.Signatures.JBSE_BASE_GOPHERPROXYSET;
import static jbse.bc.Signatures.JBSE_BASE_HTTP_NONPROXYHOSTS;
import static jbse.bc.Signatures.JBSE_BASE_HTTP_PROXYHOST;
import static jbse.bc.Signatures.JBSE_BASE_HTTP_PROXYPORT;
import static jbse.bc.Signatures.JBSE_BASE_HTTPS_PROXYHOST;
import static jbse.bc.Signatures.JBSE_BASE_HTTPS_PROXYPORT;
import static jbse.bc.Signatures.JBSE_BASE_JAVA_AWT_GRAPHICSENV;
import static jbse.bc.Signatures.JBSE_BASE_JAVA_AWT_PRINTERJOB;
import static jbse.bc.Signatures.JBSE_BASE_JAVA_CLASS_PATH;
import static jbse.bc.Signatures.JBSE_BASE_JAVA_EXT_DIRS;
import static jbse.bc.Signatures.JBSE_BASE_JAVA_HOME;
import static jbse.bc.Signatures.JBSE_BASE_JAVA_IO_TMPDIR;
import static jbse.bc.Signatures.JBSE_BASE_JAVA_LIBRARY_PATH;
import static jbse.bc.Signatures.JBSE_BASE_JAVA_VERSION;
import static jbse.bc.Signatures.JBSE_BASE_JBSE_NAME;
import static jbse.bc.Signatures.JBSE_BASE_JBSE_VERSION;
import static jbse.bc.Signatures.JBSE_BASE_LINE_SEPARATOR;
import static jbse.bc.Signatures.JBSE_BASE_OS_ARCH;
import static jbse.bc.Signatures.JBSE_BASE_OS_NAME;
import static jbse.bc.Signatures.JBSE_BASE_OS_VERSION;
import static jbse.bc.Signatures.JBSE_BASE_PATH_SEPARATOR;
import static jbse.bc.Signatures.JBSE_BASE_SOCKSNONPROXYHOSTS;
import static jbse.bc.Signatures.JBSE_BASE_SOCKSPROXYHOST;
import static jbse.bc.Signatures.JBSE_BASE_SOCKSPROXYPORT;
import static jbse.bc.Signatures.JBSE_BASE_SUN_ARCH_DATA_MODEL;
import static jbse.bc.Signatures.JBSE_BASE_SUN_BOOT_CLASS_PATH;
import static jbse.bc.Signatures.JBSE_BASE_SUN_BOOT_LIBRARY_PATH;
import static jbse.bc.Signatures.JBSE_BASE_SUN_CPU_ENDIAN;
import static jbse.bc.Signatures.JBSE_BASE_SUN_CPU_ISALIST;
import static jbse.bc.Signatures.JBSE_BASE_SUN_DESKTOP;
import static jbse.bc.Signatures.JBSE_BASE_SUN_IO_UNICODE_ENCODING;
import static jbse.bc.Signatures.JBSE_BASE_SUN_JAVA2D_FONTPATH;
import static jbse.bc.Signatures.JBSE_BASE_SUN_JNU_ENCODING;
import static jbse.bc.Signatures.JBSE_BASE_SUN_OS_PATCH_LEVEL;
import static jbse.bc.Signatures.JBSE_BASE_SUN_STDERR_ENCODING;
import static jbse.bc.Signatures.JBSE_BASE_SUN_STDOUT_ENCODING;
import static jbse.bc.Signatures.JBSE_BASE_USER_COUNTRY;
import static jbse.bc.Signatures.JBSE_BASE_USER_DIR;
import static jbse.bc.Signatures.JBSE_BASE_USER_HOME;
import static jbse.bc.Signatures.JBSE_BASE_USER_LANGUAGE;
import static jbse.bc.Signatures.JBSE_BASE_USER_NAME;
import static jbse.bc.Signatures.JBSE_BASE_USER_SCRIPT;
import static jbse.bc.Signatures.JBSE_BASE_USER_TIMEZONE;
import static jbse.bc.Signatures.JBSE_BASE_USER_VARIANT;
import static jbse.bc.Signatures.OUT_OF_MEMORY_ERROR;

import java.util.function.Supplier;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import jbse.JBSE;
import jbse.algo.Algo_INVOKEMETA_Nonbranching;
import jbse.algo.Algorithm;
import jbse.algo.ExecutionContext;
import jbse.algo.InterruptException;
import jbse.algo.StrategyUpdate;
import jbse.bc.ClassFile;
import jbse.bc.Classpath;
import jbse.bc.Signature;
import jbse.bc.exc.BadClassFileVersionException;
import jbse.bc.exc.ClassFileIllFormedException;
import jbse.bc.exc.ClassFileNotAccessibleException;
import jbse.bc.exc.ClassFileNotFoundException;
import jbse.bc.exc.IncompatibleClassFileException;
import jbse.bc.exc.PleaseLoadClassException;
import jbse.bc.exc.RenameUnsupportedException;
import jbse.bc.exc.WrongClassNameException;
import jbse.common.exc.ClasspathException;
import jbse.common.exc.InvalidInputException;
import jbse.mem.Klass;
import jbse.mem.State;
import jbse.mem.exc.HeapMemoryExhaustedException;
import jbse.tree.DecisionAlternative_NONE;
import jbse.val.ReferenceConcrete;

/**
 * An {@link Algorithm} implementing the effect of a method call
 * which pushes the any value on the stack.
 * 
 * @author Pietro Braione
 *
 */
public final class Algo_JBSE_BASE_CLINIT extends Algo_INVOKEMETA_Nonbranching {
    private static final String JBSE_NAME               = JBSE.NAME + " (" + JBSE.ACRONYM + ")";
    private static final String JBSE_VERSION            = JBSE.VERSION;
    private static       String JAVA_HOME               = null;
    private static       String SUN_BOOT_CLASS_PATH     = null;
    private static       String JAVA_EXT_DIRS           = null;
    private static       String JAVA_CLASS_PATH         = null;
    private static       String SUN_BOOT_LIBRARY_PATH   = null;
    private static       String JAVA_LIBRARY_PATH       = null;
    private static final String JAVA_VERSION            = System.getProperty("java.version");
    private static final String OS_NAME                 = System.getProperty("os.name");
    private static final String OS_VERSION              = System.getProperty("os.version");
    private static final String OS_ARCH                 = System.getProperty("os.arch");
    private static final String FILE_SEPARATOR          = System.getProperty("file.separator");
    private static final String PATH_SEPARATOR          = System.getProperty("path.separator");
    private static final String LINE_SEPARATOR          = System.getProperty("line.separator");
    private static final String USER_LANGUAGE           = System.getProperty("user.language");
    private static final String USER_SCRIPT             = System.getProperty("user.script");
    private static final String USER_COUNTRY            = System.getProperty("user.country");
    private static final String USER_VARIANT            = System.getProperty("user.variant");
    private static final String FILE_ENCODING           = System.getProperty("file.encoding");
    private static final String SUN_JNU_ENCODING        = System.getProperty("sun.jnu.encoding");
    private static final String SUN_STDOUT_ENCODING     = System.getProperty("sun.stdout.encoding");
    private static final String SUN_STDERR_ENCODING     = System.getProperty("sun.stderr.encoding");
    private static final String SUN_IO_UNICODE_ENCODING = System.getProperty("sun.io.unicode.encoding");
    private static final String SUN_CPU_ISALIST         = System.getProperty("sun.cpu.isalist");
    private static final String SUN_CPU_ENDIAN          = System.getProperty("sun.cpu.endian");
    private static final String HTTP_PROXYHOST          = System.getProperty("http.proxyHost");
    private static final String HTTP_PROXYPORT          = System.getProperty("http.proxyPort");
    private static final String HTTPS_PROXYHOST         = System.getProperty("https.proxyHost");
    private static final String HTTPS_PROXYPORT         = System.getProperty("https.proxyPort");
    private static final String FTP_PROXYHOST           = System.getProperty("ftp.proxyHost");
    private static final String FTP_PROXYPORT           = System.getProperty("ftp.proxyPort");
    private static final String SOCKSPROXYHOST          = System.getProperty("socksProxyHost");
    private static final String SOCKSPROXYPORT          = System.getProperty("socksProxyPort");
    private static final String GOPHERPROXYSET          = System.getProperty("gopherProxySet");
    private static final String GOPHERPROXYHOST         = System.getProperty("gopherProxyHost");
    private static final String GOPHERPROXYPORT         = System.getProperty("gopherProxyPort");
    private static final String HTTP_NONPROXYHOSTS      = System.getProperty("http.nonProxyHosts");
    private static final String FTP_NONPROXYHOSTS       = System.getProperty("ftp.nonProxyHosts");
    private static final String SOCKSNONPROXYHOSTS      = System.getProperty("socksNonProxyHosts");
    private static final String JAVA_AWT_PRINTERJOB     = System.getProperty("java.awt.printerjob");
    private static final String SUN_ARCH_DATA_MODEL     = System.getProperty("sun.arch.data.model");
    private static final String SUN_OS_PATCH_LEVEL      = System.getProperty("sun.os.patch.level");
    private static final String JAVA_AWT_GRAPHICSENV    = System.getProperty("java.awt.graphicsenv");
    private static final String SUN_JAVA2D_FONTPATH     = System.getProperty("sun.java2d.fontpath");
    private static final String JAVA_IO_TMPDIR          = System.getProperty("java.io.tmpdir");
    private static final String USER_NAME               = System.getProperty("user.name");
    private static final String USER_HOME               = System.getProperty("user.home");
    private static final String USER_TIMEZONE           = System.getProperty("user.timezone");
    private static final String USER_DIR                = System.getProperty("user.dir");
    private static final String SUN_DESKTOP             = System.getProperty("sun.desktop");

    @Override
    protected Supplier<Integer> numOperands() {
        return () -> 0;
    }

    @Override
    protected void cookMore(State state) 
    throws InterruptException, ClasspathException, InvalidInputException {
        final Classpath cp = state.getClasspath();
        
        //initializes the paths
        JAVA_HOME             = cp.javaHome().toString();
        SUN_BOOT_CLASS_PATH   = String.join(PATH_SEPARATOR, stream(cp.bootClassPath()).map(Object::toString).toArray(String[]::new));
        JAVA_EXT_DIRS         = String.join(PATH_SEPARATOR, stream(cp.extDirs()).map(Object::toString).toArray(String[]::new));
        JAVA_CLASS_PATH       = String.join(PATH_SEPARATOR, stream(cp.classPath()).map(Object::toString).toArray(String[]::new));
        SUN_BOOT_LIBRARY_PATH = JAVA_HOME + FILE_SEPARATOR + "lib";
        JAVA_LIBRARY_PATH     = JAVA_EXT_DIRS; //TODO this is an approximation, the real case is much more complex and os-dependent
        
        //creates the literals
        safeEnsureStringLiteral(state, this.ctx, JBSE_NAME);
        safeEnsureStringLiteral(state, this.ctx, JBSE_VERSION);
        safeEnsureStringLiteral(state, this.ctx, JAVA_EXT_DIRS);
        safeEnsureStringLiteral(state, this.ctx, SUN_BOOT_LIBRARY_PATH);
        safeEnsureStringLiteral(state, this.ctx, JAVA_LIBRARY_PATH);
        safeEnsureStringLiteral(state, this.ctx, JAVA_HOME);
        safeEnsureStringLiteral(state, this.ctx, SUN_BOOT_CLASS_PATH);
        safeEnsureStringLiteral(state, this.ctx, JAVA_CLASS_PATH);        
        safeEnsureStringLiteral(state, this.ctx, JAVA_VERSION);
        safeEnsureStringLiteral(state, this.ctx, OS_NAME);
        safeEnsureStringLiteral(state, this.ctx, OS_VERSION);
        safeEnsureStringLiteral(state, this.ctx, OS_ARCH);
        safeEnsureStringLiteral(state, this.ctx, FILE_SEPARATOR);
        safeEnsureStringLiteral(state, this.ctx, PATH_SEPARATOR);
        safeEnsureStringLiteral(state, this.ctx, LINE_SEPARATOR);
        safeEnsureStringLiteral(state, this.ctx, USER_LANGUAGE);
        safeEnsureStringLiteral(state, this.ctx, USER_SCRIPT);
        safeEnsureStringLiteral(state, this.ctx, USER_COUNTRY);
        safeEnsureStringLiteral(state, this.ctx, USER_VARIANT);
        safeEnsureStringLiteral(state, this.ctx, FILE_ENCODING);
        safeEnsureStringLiteral(state, this.ctx, SUN_JNU_ENCODING);
        safeEnsureStringLiteral(state, this.ctx, SUN_STDOUT_ENCODING);
        safeEnsureStringLiteral(state, this.ctx, SUN_STDERR_ENCODING);
        safeEnsureStringLiteral(state, this.ctx, SUN_IO_UNICODE_ENCODING);
        safeEnsureStringLiteral(state, this.ctx, SUN_CPU_ISALIST);
        safeEnsureStringLiteral(state, this.ctx, SUN_CPU_ENDIAN);
        safeEnsureStringLiteral(state, this.ctx, HTTP_PROXYHOST);
        safeEnsureStringLiteral(state, this.ctx, HTTP_PROXYPORT);
        safeEnsureStringLiteral(state, this.ctx, HTTPS_PROXYHOST);
        safeEnsureStringLiteral(state, this.ctx, HTTPS_PROXYPORT);
        safeEnsureStringLiteral(state, this.ctx, FTP_PROXYHOST);
        safeEnsureStringLiteral(state, this.ctx, FTP_PROXYPORT);
        safeEnsureStringLiteral(state, this.ctx, SOCKSPROXYHOST);
        safeEnsureStringLiteral(state, this.ctx, SOCKSPROXYPORT);
        safeEnsureStringLiteral(state, this.ctx, GOPHERPROXYSET);
        safeEnsureStringLiteral(state, this.ctx, GOPHERPROXYHOST);
        safeEnsureStringLiteral(state, this.ctx, GOPHERPROXYPORT);
        safeEnsureStringLiteral(state, this.ctx, HTTP_NONPROXYHOSTS);
        safeEnsureStringLiteral(state, this.ctx, FTP_NONPROXYHOSTS);
        safeEnsureStringLiteral(state, this.ctx, SOCKSNONPROXYHOSTS);
        safeEnsureStringLiteral(state, this.ctx, JAVA_AWT_PRINTERJOB);
        safeEnsureStringLiteral(state, this.ctx, SUN_ARCH_DATA_MODEL);
        safeEnsureStringLiteral(state, this.ctx, SUN_OS_PATCH_LEVEL);
        safeEnsureStringLiteral(state, this.ctx, JAVA_AWT_GRAPHICSENV);
        safeEnsureStringLiteral(state, this.ctx, SUN_JAVA2D_FONTPATH);
        safeEnsureStringLiteral(state, this.ctx, JAVA_IO_TMPDIR);
        safeEnsureStringLiteral(state, this.ctx, USER_NAME);
        safeEnsureStringLiteral(state, this.ctx, USER_HOME);
        safeEnsureStringLiteral(state, this.ctx, USER_TIMEZONE);
        safeEnsureStringLiteral(state, this.ctx, USER_DIR);
        safeEnsureStringLiteral(state, this.ctx, SUN_DESKTOP);
    }

    /**
     * Converts an iterable to a stream.
     * See <a href="https://stackoverflow.com/a/23177907/450589">https://stackoverflow.com/a/23177907/450589</a>.
     * @param it an {@link Iterable}{@code <T>}.
     * @return a {@link Stream}{@code <T>} for {@code it}.
     */
    private static <T> Stream<T> stream(Iterable<T> it) {
        return StreamSupport.stream(it.spliterator(), false);
    }
    

    private static void safeEnsureStringLiteral(State state, ExecutionContext ctx, String stringLit) 
    throws InterruptException, ClasspathException, InvalidInputException {
        if (stringLit != null) {
            try {
                state.ensureStringLiteral(ctx.getCalculator(), stringLit);
            } catch (HeapMemoryExhaustedException e) {
                throwNew(state, ctx.getCalculator(), OUT_OF_MEMORY_ERROR);
                exitFromAlgorithm();
            }
        }
    }

    @Override
    protected StrategyUpdate<DecisionAlternative_NONE> updater() {
        return (state, alt) -> {
            //gets the Klass for jbse.base.Base
            Klass klassBase = null; //to keep the compiler happy
            try {
                final ClassFile  cf_JBSE_BASE = state.getClassHierarchy().loadCreateClass(CLASSLOADER_APP, JBSE_BASE, true);
                klassBase = state.getKlass(cf_JBSE_BASE);
                if (klassBase == null) {
                    //this should never happen
                    failExecution("Found no Klass for " + JBSE_BASE);
                }
            } catch (ClassFileNotFoundException | ClassFileIllFormedException | BadClassFileVersionException |
                     RenameUnsupportedException | WrongClassNameException | IncompatibleClassFileException | 
                     ClassFileNotAccessibleException | PleaseLoadClassException e) {
                //this should never happen
                failExecution(e);
            }

            //inits its static fields
            safeSetStringValue(state, klassBase, JBSE_BASE_JBSE_NAME,               JBSE_NAME);
            safeSetStringValue(state, klassBase, JBSE_BASE_JBSE_VERSION,            JBSE_VERSION);
            safeSetStringValue(state, klassBase, JBSE_BASE_JAVA_EXT_DIRS,           JAVA_EXT_DIRS);
            safeSetStringValue(state, klassBase, JBSE_BASE_SUN_BOOT_LIBRARY_PATH,   SUN_BOOT_LIBRARY_PATH);
            safeSetStringValue(state, klassBase, JBSE_BASE_JAVA_LIBRARY_PATH,       JAVA_LIBRARY_PATH);
            safeSetStringValue(state, klassBase, JBSE_BASE_JAVA_HOME,               JAVA_HOME);
            safeSetStringValue(state, klassBase, JBSE_BASE_SUN_BOOT_CLASS_PATH,     SUN_BOOT_CLASS_PATH);
            safeSetStringValue(state, klassBase, JBSE_BASE_JAVA_CLASS_PATH,         JAVA_CLASS_PATH);
            safeSetStringValue(state, klassBase, JBSE_BASE_JAVA_VERSION,            JAVA_VERSION);
            safeSetStringValue(state, klassBase, JBSE_BASE_OS_NAME,                 OS_NAME);
            safeSetStringValue(state, klassBase, JBSE_BASE_OS_VERSION,              OS_VERSION);
            safeSetStringValue(state, klassBase, JBSE_BASE_OS_ARCH,                 OS_ARCH);
            safeSetStringValue(state, klassBase, JBSE_BASE_FILE_SEPARATOR,          FILE_SEPARATOR);
            safeSetStringValue(state, klassBase, JBSE_BASE_PATH_SEPARATOR,          PATH_SEPARATOR);
            safeSetStringValue(state, klassBase, JBSE_BASE_LINE_SEPARATOR,          LINE_SEPARATOR);
            safeSetStringValue(state, klassBase, JBSE_BASE_USER_LANGUAGE,           USER_LANGUAGE);
            safeSetStringValue(state, klassBase, JBSE_BASE_USER_SCRIPT,             USER_SCRIPT);
            safeSetStringValue(state, klassBase, JBSE_BASE_USER_COUNTRY,            USER_COUNTRY);
            safeSetStringValue(state, klassBase, JBSE_BASE_USER_VARIANT,            USER_VARIANT);
            safeSetStringValue(state, klassBase, JBSE_BASE_FILE_ENCODING,           FILE_ENCODING);
            safeSetStringValue(state, klassBase, JBSE_BASE_SUN_JNU_ENCODING,        SUN_JNU_ENCODING);
            safeSetStringValue(state, klassBase, JBSE_BASE_SUN_STDOUT_ENCODING,     SUN_STDOUT_ENCODING);
            safeSetStringValue(state, klassBase, JBSE_BASE_SUN_STDERR_ENCODING,     SUN_STDERR_ENCODING);
            safeSetStringValue(state, klassBase, JBSE_BASE_SUN_IO_UNICODE_ENCODING, SUN_IO_UNICODE_ENCODING);
            safeSetStringValue(state, klassBase, JBSE_BASE_SUN_CPU_ISALIST,         SUN_CPU_ISALIST);
            safeSetStringValue(state, klassBase, JBSE_BASE_SUN_CPU_ENDIAN,          SUN_CPU_ENDIAN);
            safeSetStringValue(state, klassBase, JBSE_BASE_HTTP_PROXYHOST,          HTTP_PROXYHOST);
            safeSetStringValue(state, klassBase, JBSE_BASE_HTTP_PROXYPORT,          HTTP_PROXYPORT);
            safeSetStringValue(state, klassBase, JBSE_BASE_HTTPS_PROXYHOST,         HTTP_PROXYHOST);
            safeSetStringValue(state, klassBase, JBSE_BASE_HTTPS_PROXYPORT,         HTTP_PROXYPORT);
            safeSetStringValue(state, klassBase, JBSE_BASE_FTP_PROXYHOST,           FTP_PROXYHOST);
            safeSetStringValue(state, klassBase, JBSE_BASE_FTP_PROXYPORT,           FTP_PROXYPORT);
            safeSetStringValue(state, klassBase, JBSE_BASE_SOCKSPROXYHOST,          SOCKSPROXYHOST);
            safeSetStringValue(state, klassBase, JBSE_BASE_SOCKSPROXYPORT,          SOCKSPROXYPORT);
            safeSetStringValue(state, klassBase, JBSE_BASE_GOPHERPROXYSET,          GOPHERPROXYSET);
            safeSetStringValue(state, klassBase, JBSE_BASE_GOPHERPROXYHOST,         GOPHERPROXYHOST);
            safeSetStringValue(state, klassBase, JBSE_BASE_GOPHERPROXYPORT,         GOPHERPROXYPORT);
            safeSetStringValue(state, klassBase, JBSE_BASE_HTTP_NONPROXYHOSTS,      HTTP_NONPROXYHOSTS);
            safeSetStringValue(state, klassBase, JBSE_BASE_FTP_NONPROXYHOSTS,       FTP_NONPROXYHOSTS);
            safeSetStringValue(state, klassBase, JBSE_BASE_SOCKSNONPROXYHOSTS,      SOCKSNONPROXYHOSTS);
            safeSetStringValue(state, klassBase, JBSE_BASE_JAVA_AWT_PRINTERJOB,     JAVA_AWT_PRINTERJOB);
            safeSetStringValue(state, klassBase, JBSE_BASE_SUN_ARCH_DATA_MODEL,     SUN_ARCH_DATA_MODEL);
            safeSetStringValue(state, klassBase, JBSE_BASE_SUN_OS_PATCH_LEVEL,      SUN_OS_PATCH_LEVEL);
            safeSetStringValue(state, klassBase, JBSE_BASE_JAVA_AWT_GRAPHICSENV,    JAVA_AWT_GRAPHICSENV);
            safeSetStringValue(state, klassBase, JBSE_BASE_SUN_JAVA2D_FONTPATH,     SUN_JAVA2D_FONTPATH);
            safeSetStringValue(state, klassBase, JBSE_BASE_JAVA_IO_TMPDIR,          JAVA_IO_TMPDIR);
            safeSetStringValue(state, klassBase, JBSE_BASE_USER_NAME,               USER_NAME);
            safeSetStringValue(state, klassBase, JBSE_BASE_USER_HOME,               USER_HOME);
            safeSetStringValue(state, klassBase, JBSE_BASE_USER_TIMEZONE,           USER_TIMEZONE);
            safeSetStringValue(state, klassBase, JBSE_BASE_USER_DIR,                USER_DIR);
            safeSetStringValue(state, klassBase, JBSE_BASE_SUN_DESKTOP,             SUN_DESKTOP);
        };
    }

    private static void safeSetStringValue(State state, Klass k, Signature field, String property) {
        if (property != null) {
            final ReferenceConcrete refPropertyValue = state.referenceToStringLiteral(property);
            if (refPropertyValue == null) {
                failExecution("Failed to create a string literal");
            }
            k.setFieldValue(field, refPropertyValue);
        }
    }
}
