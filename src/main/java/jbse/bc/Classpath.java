package jbse.bc;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Class representing the classpath for the symbolic execution.
 * 
 * @author Pietro Braione
 */
public class Classpath implements Cloneable {
	private ArrayList<String> classPath; //nonfinal because of clone
	
	/**
	 * Constructor.
	 * 
	 * @param paths a varargs of {@link String}, 
	 *        the paths to be put in the classpath.
	 */
	public Classpath(String... paths) {
		this.classPath = new ArrayList<String>();
		for (String s : paths) {
			addClassPath(s);
		}
	}

	/**
	 * Adds a path to the classpath.
	 * 
	 * @param path a {@link String} representing a path in the executor's
	 *            classpath.
	 */
	private void addClassPath(String path) {
		if (path.endsWith(".jar")) {
			this.classPath.add(path);
		} else if (path.charAt(path.length() - 1) == '/') {
			this.classPath.add(path);
		} else {
			this.classPath.add(path + "/");
		}
	}

	/**
	 * Returns the paths in this classpath.
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
		
		o.classPath = (ArrayList<String>) this.classPath.clone();
		return o;
	}
}
