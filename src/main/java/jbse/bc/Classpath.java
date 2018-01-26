package jbse.bc;

import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Class representing a classpath for symbolic execution.
 * 
 * @author Pietro Braione
 */
public class Classpath implements Cloneable {
    private final String javaHome;
    private ArrayList<String> bootClassPath; //nonfinal because of clone
    private ArrayList<String> extClassPath; //nonfinal because of clone
    private ArrayList<String> userClassPath; //nonfinal because of clone
    private ArrayList<String> classPath; //nonfinal because of clone

    /**
     * Constructor.
     * 
     * @param javaHome a {@link String}, the Java home directory.
     * @param extPaths a {@link List}{@code <}{@link String}{@code >}, 
     *        the extension directories. It must contain valid paths
     *        to directories. Only the jar files contained in these
     *        directories will be considered.
     * @param userPaths a {@link List}{@code <}{@link String}{@code >},
     *        the user classpath. It must contain valid paths to directories
     *        or jar files.
     */
    public Classpath(String javaHome, List<String> extPaths, List<String> userPaths) {
        this.javaHome = javaHome;
        
        //bootstrap paths
        //taken from hotspot:/src/share/vm/runtime/os.cpp, lines 1194-1202
        this.bootClassPath = new ArrayList<>();
        addClassPath(this.bootClassPath, Paths.get(javaHome, "lib", "resources.jar").toString());
        addClassPath(this.bootClassPath, Paths.get(javaHome, "lib", "rt.jar").toString());
        addClassPath(this.bootClassPath, Paths.get(javaHome, "lib", "sunrsasign.jar").toString());
        addClassPath(this.bootClassPath, Paths.get(javaHome, "lib", "jsse.jar").toString());
        addClassPath(this.bootClassPath, Paths.get(javaHome, "lib", "jce.jar").toString());
        addClassPath(this.bootClassPath, Paths.get(javaHome, "lib", "charset.jar").toString());
        addClassPath(this.bootClassPath, Paths.get(javaHome, "lib", "jfr.jar").toString());
        addClassPath(this.bootClassPath, Paths.get(javaHome, "classes").toString());
        
        //extension paths
        this.extClassPath = new ArrayList<>();
        for (String dir : extPaths) {
            final File f = new File(dir);
            final File[] jars = f.listFiles((dir2, name) -> name.endsWith(".jar"));
            if (jars == null) {
                //not a directory
                continue;
            }
            for (File jar : jars) {
                addClassPath(this.extClassPath, jar.getAbsolutePath());
            }
        }
        
        //user paths
        this.userClassPath = new ArrayList<>();
        for (String s : userPaths) {
            addClassPath(this.userClassPath, s);
        }
        if (this.userClassPath.isEmpty()) {
            addClassPath(this.userClassPath, Paths.get(".").toAbsolutePath().toString());
        }
        
        //the full classpath; this is an approximation of what Hotspot does
        this.classPath = new ArrayList<>();
        this.classPath.addAll(this.bootClassPath);
        this.classPath.addAll(this.extClassPath);
        this.classPath.addAll(this.userClassPath);
    }

    /**
     * Adds a path to a classpath.
     * 
     * @param paths a {@link List}{@code <}{@link String}{@code >},
     *        the classpath where {@code path} must be added.
     * @param path a {@link String}, the path to be added.
     */
    private void addClassPath(List<String> paths, String path) {
        if (path.endsWith(".jar")) {
            paths.add(path);
        } else if (path.charAt(path.length() - 1) == '/') {
            paths.add(path);
        } else {
            paths.add(path + "/");
        }
    }
    
    /**
     * Returns the Java home directory.
     * 
     * @return a {@link String}.
     */
    public String javaHome() {
        return this.javaHome;
    }

    /**
     * Returns the paths in the bootstrap classpath.
     * 
     * @return an {@link Iterable}{@code <}{@link String}{@code >}
     *         of all the paths in the bootstrap classpath.
     */
    public Iterable<String> bootClassPath() {
        return Collections.unmodifiableCollection(this.bootClassPath);
    }

    /**
     * Returns the paths in the extension classpath.
     * 
     * @return an {@link Iterable}{@code <}{@link String}{@code >}
     *         of all the paths in the extension classpath.
     */
    public Iterable<String> extClassPath() {
        return Collections.unmodifiableCollection(this.extClassPath);
    }

    /**
     * Returns the paths in the user classpath.
     * 
     * @return an {@link Iterable}{@code <}{@link String}{@code >}
     *         of all the paths in the user classpath.
     */
    public Iterable<String> userClassPath() {
        return Collections.unmodifiableCollection(this.userClassPath);
    }

    /**
     * Returns the paths in the full classpath.
     * 
     * @return an {@link Iterable}{@code <}{@link String}{@code >}
     *         of all the paths in the classpath.
     */
    public Iterable<String> classPath() {
        return Collections.unmodifiableCollection(this.classPath);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Classpath clone() {
        final Classpath o;

        try {
            o = (Classpath) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new InternalError(e); 
        }

        o.bootClassPath = (ArrayList<String>) this.bootClassPath.clone();
        o.extClassPath = (ArrayList<String>) this.extClassPath.clone();
        o.userClassPath = (ArrayList<String>) this.userClassPath.clone();
        o.classPath = (ArrayList<String>) this.classPath.clone();
        return o;
    }
}
