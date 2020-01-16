package jbse;

/**
 * General information about this project.
 *
 * @author Pietro Braione
 */
public final class JBSE {
    /** The name of this project. */
    public static final String NAME = "Java Bytecode Symbolic Executor";

    /** The acronym of this project. */
    public static final String ACRONYM = getAcronym();

    /** The vendor of this project. */
    public static final String VENDOR = getVendor();

    /** The current version of this project. */
    public static final String VERSION = getVersion();

    /** The license under which this project is distributed. */
    public static final String LICENSE = "GNU GPL v.3.0 or greater";

    /**
     * Returns the acronym of this application, as resulting
     * from the containing jar file.
     * 
     * @return a {@link String} or {@code null} if this 
     *         class is not packaged in a jar file.
     */
    private static String getAcronym() {
        return JBSE.class.getPackage().getImplementationTitle();
    }

    /**
     * Returns the vendor of this application, as resulting
     * from the containing jar file.
     * 
     * @return a {@link String} or {@code null} if this 
     *         class is not packaged in a jar file.
     */
    private static String getVendor() {
        return JBSE.class.getPackage().getImplementationVendor();
    }

    /**
     * Returns the version of this application, as resulting
     * from the containing jar file.
     * 
     * @return a {@link String} or {@code null} if this 
     *         class is not packaged in a jar file.
     */
    private static String getVersion() {
        return JBSE.class.getPackage().getImplementationVersion();
    }

    /**
    /**
     * Do not instantiate me!
     */
    private JBSE() {
        //nothing to do
    }
}
