package jbse.base;

import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.Properties;
import sun.misc.Unsafe;

/**
 * Some base-level overriding implementations of methods. 
 * 
 * @author Pietro Braione
 *
 */
public final class Base {
	/**
	 * Overriding implementation of {@link java.lang.System#initProperties(Properties)}.
	 * @see java.lang.System#initProperties(Properties)
	 */
	public static final Properties base_JAVA_SYSTEM_INITPROPERTIES(Properties p) {
		p.put("java.vendor", "JBSE project");
		p.put("java.vendor.url", "http://pietrobraione.github.io/jbse/");
		//TODO more properties
		return p;
	}
	
	/**
	 * Overriding implementation of {@link java.security.AccessController#doPrivileged(PrivilegedExceptionAction)}.
	 * @see java.security.AccessController#doPrivileged(PrivilegedExceptionAction)
	 */
	public static final Object base_JAVA_ACCESSCONTROLLER_DOPRIVILEGED_EXCEPTION(PrivilegedExceptionAction<?> action)
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
	public static final Object base_JAVA_ACCESSCONTROLLER_DOPRIVILEGED_NOEXCEPTION(PrivilegedAction<?> action)
	throws PrivilegedActionException {
		//since JBSE does not enforce access control we just execute the action
		return action.run();
	}
	
	/**
	 * Overriding implementation of {@link sun.misc.Unsafe#addressSize()}.
	 * @see sun.misc.Unsafe#addressSize()
	 */
	public static final int base_SUN_UNSAFE_ADDRESSSIZE(Unsafe _this) {
		//JBSE offers no raw access to its data structures, so we return a dummy value
		return 8; //can be either 4 or 8, we choose 8 
	}
	
	/**
	 * Overriding implementation of {@link sun.misc.Unsafe#arrayBaseOffset(Class)}.
	 * @see sun.misc.Unsafe#arrayBaseOffset(Class)
	 */
	public static final int base_SUN_UNSAFE_ARRAYBASEOFFSET(Unsafe _this, Class<?> arrayClass) {
		//JBSE offers no raw access to its data structures, so we return a dummy value
		return 0; 
	}
	
	/**
	 * Overriding implementation of {@link sun.misc.Unsafe#arrayIndexScale(Class)}.
	 * @see sun.misc.Unsafe#arrayIndexScale(Class)
	 */
	public static final int base_SUN_UNSAFE_ARRAYINDEXSCALE(Unsafe _this, Class<?> arrayClass) {
		//JBSE offers no raw access to its data structures, so we return a dummy value
		return 1; 
	}
	
	private Base() {
		//do not instantiate!
		throw new AssertionError();
	}
}
