package jbse.bc;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.NotDirectoryException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import jbse.common.Util;

/**
 * Class representing a classpath for symbolic execution.
 * 
 * @author Pietro Braione
 */
public class Classpath implements Cloneable {
    private final Path jbseLibPath;
    private final Path javaHome;
    private ArrayList<Path> bootClassPath; //nonfinal because of clone
    private ArrayList<Path> extClassPath; //nonfinal because of clone
    private ArrayList<Path> extDirs; //nonfinal because of clone
    private ArrayList<Path> userClassPath; //nonfinal because of clone
    private ArrayList<Path> classPath; //nonfinal because of clone

    /**
     * Constructor.
     * 
     * @param jbseLibPath a {@link Path}, the path of the JBSE library.
     * @param javaHome a {@link Path}, the Java home directory.
     * @param extDirs a {@link List}{@code <}{@link Path}{@code >}, 
     *        the extension directories. It must contain valid paths
     *        to directories. Only the jar files contained in these
     *        directories will be considered.
     * @param userPaths a {@link List}{@code <}{@link Path}{@code >},
     *        the user classpath. It must contain valid paths to directories
     *        or jar files.
     * @throws IOException if an I/O error occurs.
     */
    public Classpath(Path jbseLibPath, Path javaHome, List<Path> extDirs, List<Path> userPaths) throws IOException {
    	this.jbseLibPath = jbseLibPath.toAbsolutePath();
        this.javaHome = javaHome.toAbsolutePath();
        
        //bootstrap paths
        //taken from hotspot:/src/share/vm/runtime/os.cpp, lines 1194-1202
        this.bootClassPath = new ArrayList<>();
        addClassPath(this.bootClassPath, javaHome.resolve("lib").resolve("resources.jar"));
        addClassPath(this.bootClassPath, javaHome.resolve("lib").resolve("rt.jar"));
        addClassPath(this.bootClassPath, javaHome.resolve("lib").resolve("sunrsasign.jar"));
        addClassPath(this.bootClassPath, javaHome.resolve("lib").resolve("jsse.jar"));
        addClassPath(this.bootClassPath, javaHome.resolve("lib").resolve("jce.jar"));
        addClassPath(this.bootClassPath, javaHome.resolve("lib").resolve("charset.jar"));
        addClassPath(this.bootClassPath, javaHome.resolve("lib").resolve("jfr.jar"));
        addClassPath(this.bootClassPath, javaHome.resolve("classes"));
        addClassPath(this.bootClassPath, jbseLibPath);
        
        //extension paths and dirs
        this.extClassPath = new ArrayList<>();
        this.extDirs = new ArrayList<>();
        for (Path p : extDirs) {
        	addAllJars(this.extClassPath, p);
            addClassPath(this.extDirs, p);            
        }
        
        //user paths
        this.userClassPath = new ArrayList<>();
        for (Path s : userPaths) {
            addClassPath(this.userClassPath, s);
        }
        if (this.userClassPath.isEmpty()) {
            addClassPath(this.userClassPath, Paths.get(".").toAbsolutePath());
        }
        
        //the full classpath; this is an approximation of what Hotspot does
        this.classPath = new ArrayList<>();
        this.classPath.addAll(this.bootClassPath);
        this.classPath.addAll(this.extClassPath);
        this.classPath.addAll(this.userClassPath);
    }

	/**
     * Adds a path to a list of paths.
     * 
     * @param paths a {@link List}{@code <}{@link Path}{@code >},
     *        where {@code path} must be added.
     * @param path a {@link Path}, the path to be added. If {@code path}
     *        is not a path to a jar file or a directory, the method
     *        does nothing.
     */
    private void addClassPath(List<Path> paths, Path path) {
        if (Util.isJarFile(path)) {
            paths.add(path);
        } else if (Files.isDirectory(path)) {
            paths.add(path);
        } //else do nothing
    }
    
    /**
     * Adds to a list of paths all the paths to jar files 
     * contained in a directory.
     * 
     * @param paths a {@link List}{@code <}{@link Path}{@code >},
     *        where the jar files in {@code path} must be added.
     * @param path a {@link Path} to a directory. If {@code path} 
     *        is not a path to a directory, the method does nothing.
     * @throws IOException if an I/O error occurs.
     */
    private void addAllJars(List<Path> paths, Path path) throws IOException {
        try (final DirectoryStream<Path> stream = Files.newDirectoryStream(path, Util::isJarFile)) {
        	stream.forEach(jar -> addClassPath(paths, jar.toAbsolutePath()));
        } catch (NoSuchFileException | NotDirectoryException e) {
        	return; //do nothing, path is not a path to a directory
        }
    }
    
    /**
     * Returns the path of the JBSE library.
     * 
     * @return a {@link Path}.
     */
    public Path jbseLibPath() {
        return this.jbseLibPath;
    }
    
    /**
     * Returns the Java home directory.
     * 
     * @return a {@link Path}.
     */
    public Path javaHome() {
        return this.javaHome;
    }

    /**
     * Returns the paths in the bootstrap classpath.
     * 
     * @return an {@link Iterable}{@code <}{@link Path}{@code >}
     *         of all the paths in the bootstrap classpath.
     */
    public Iterable<Path> bootClassPath() {
        return Collections.unmodifiableCollection(this.bootClassPath);
    }
    
    /**
     * Returns the extensions directories.
     * 
     * @return an {@link Iterable}{@code <}{@link Path}{@code >}
     *         of all the extensions directories.
     */
    public Iterable<Path> extDirs() {
        return Collections.unmodifiableCollection(this.extDirs);
    }

    /**
     * Returns the paths in the extensions classpath.
     * 
     * @return an {@link Iterable}{@code <}{@link Path}{@code >}
     *         of all the paths in the extensions classpath.
     */
    public Iterable<Path> extClassPath() {
        return Collections.unmodifiableCollection(this.extClassPath);
    }

    /**
     * Returns the paths in the user classpath.
     * 
     * @return an {@link Iterable}{@code <}{@link Path}{@code >}
     *         of all the paths in the user classpath.
     */
    public Iterable<Path> userClassPath() {
        return Collections.unmodifiableCollection(this.userClassPath);
    }

    /**
     * Returns the paths in the full classpath.
     * 
     * @return an {@link Iterable}{@code <}{@link Path}{@code >}
     *         of all the paths in the classpath.
     */
    public Iterable<Path> classPath() {
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

        o.bootClassPath = (ArrayList<Path>) this.bootClassPath.clone();
        o.extClassPath = (ArrayList<Path>) this.extClassPath.clone();
        o.userClassPath = (ArrayList<Path>) this.userClassPath.clone();
        o.classPath = (ArrayList<Path>) this.classPath.clone();
        return o;
    }
}
