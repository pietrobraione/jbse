package jbse.algo.meta;

import static jbse.algo.Util.ensureStringLiteral;
import static jbse.algo.Util.exitFromAlgorithm;
import static jbse.algo.Util.failExecution;
import static jbse.algo.Util.throwNew;
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
import static jbse.bc.Signatures.JBSE_BASE_LINE_SEPARATOR;
import static jbse.bc.Signatures.JBSE_BASE_OS_ARCH;
import static jbse.bc.Signatures.JBSE_BASE_OS_NAME;
import static jbse.bc.Signatures.JBSE_BASE_OS_VERSION;
import static jbse.bc.Signatures.JBSE_BASE_PATH_SEPARATOR;
import static jbse.bc.Signatures.JBSE_BASE_SOCKSNONPROXYHOSTS;
import static jbse.bc.Signatures.JBSE_BASE_SOCKSPROXYHOST;
import static jbse.bc.Signatures.JBSE_BASE_SOCKSPROXYPORT;
import static jbse.bc.Signatures.JBSE_BASE_SUN_CPU_ENDIAN;
import static jbse.bc.Signatures.JBSE_BASE_SUN_CPU_ISALIST;
import static jbse.bc.Signatures.JBSE_BASE_SUN_IO_UNICODE_ENCODING;
import static jbse.bc.Signatures.JBSE_BASE_SUN_JNU_ENCODING;
import static jbse.bc.Signatures.JBSE_BASE_SUN_STDERR_ENCODING;
import static jbse.bc.Signatures.JBSE_BASE_SUN_STDOUT_ENCODING;
import static jbse.bc.Signatures.JBSE_BASE_USER_COUNTRY;
import static jbse.bc.Signatures.JBSE_BASE_USER_LANGUAGE;
import static jbse.bc.Signatures.JBSE_BASE_USER_SCRIPT;
import static jbse.bc.Signatures.JBSE_BASE_USER_VARIANT;
import static jbse.bc.Signatures.OUT_OF_MEMORY_ERROR;

import java.util.function.Supplier;

import jbse.algo.Algo_INVOKEMETA_Nonbranching;
import jbse.algo.Algorithm;
import jbse.algo.ExecutionContext;
import jbse.algo.InterruptException;
import jbse.algo.exc.CannotManageStateException;
import jbse.bc.Signature;
import jbse.bc.exc.ClassFileIllFormedException;
import jbse.common.exc.ClasspathException;
import jbse.dec.exc.DecisionException;
import jbse.mem.Klass;
import jbse.mem.State;
import jbse.mem.exc.HeapMemoryExhaustedException;
import jbse.mem.exc.ThreadStackEmptyException;
import jbse.val.ReferenceConcrete;

/**
 * An {@link Algorithm} implementing the effect of a method call
 * which pushes the any value on the stack.
 * 
 * @author Pietro Braione
 *
 */
public final class Algo_JBSE_BASE_CLINIT extends Algo_INVOKEMETA_Nonbranching {
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

    @Override
    protected Supplier<Integer> numOperands() {
        return () -> 0;
    }

    @Override
    protected void cookMore(State state) throws ThreadStackEmptyException, DecisionException, ClasspathException,
    CannotManageStateException, InterruptException {
        try {
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
        } catch (ClassFileIllFormedException e) {
            throw new ClasspathException(e);
        }
    }

    private static void safeEnsureStringLiteral(State state, ExecutionContext ctx, String stringLit) 
    throws ClassFileIllFormedException, DecisionException, ClasspathException, InterruptException {
        if (stringLit != null) {
            try {
                ensureStringLiteral(state, ctx, stringLit);
            } catch (HeapMemoryExhaustedException e) {
                throwNew(state, OUT_OF_MEMORY_ERROR);
                exitFromAlgorithm();
            }
        }
    }


    @Override
    protected void update(State state) throws ThreadStackEmptyException {
        final Klass klassBase = state.getKlass(JBSE_BASE);
        if (klassBase == null) {
            //this should never happen
            failExecution("Found no klass " + JBSE_BASE);
        }

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
