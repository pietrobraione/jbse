package jbse.base;

import java.util.Properties;

public final class Base {
	public static final Properties base_JAVA_SYSTEM_INITPROPERTIES(Properties p) {
		p.put("java.version", "1.8.0");
		p.put("java.vendor", "JBSE project");
		p.put("java.vendor.url", "http://pietrobraione.github.io/jbse/");
		//TODO more properties
		return p;
	}
}
