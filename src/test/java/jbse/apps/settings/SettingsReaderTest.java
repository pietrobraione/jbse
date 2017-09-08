package jbse.apps.settings;

//import static org.junit.Assert.*;

import java.io.FileNotFoundException;
import java.io.IOException;

import jbse.jvm.EngineParameters;

import org.junit.Test;

public class SettingsReaderTest {
	@Test
	public void testSimple() throws FileNotFoundException, ParseException, IOException {
		final SettingsReader r = new SettingsReader("src/test/resources/jbse/apps/settings/testdata/foo.jbse");
		final EngineParameters p = new EngineParameters();
		r.fillEngineParameters(p);
		
		//TODO assertEquals...
	}
}
