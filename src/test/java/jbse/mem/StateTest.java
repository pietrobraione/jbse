package jbse.mem;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Test;

import jbse.bc.ClassFileFactoryJavassist;
import jbse.bc.Classpath;
import jbse.bc.exc.BadClassFileVersionException;
import jbse.bc.exc.ClassFileIllFormedException;
import jbse.bc.exc.ClassFileNotAccessibleException;
import jbse.bc.exc.ClassFileNotFoundException;
import jbse.bc.exc.IncompatibleClassFileException;
import jbse.bc.exc.InvalidClassFileFactoryClassException;
import jbse.bc.exc.RenameUnsupportedException;
import jbse.bc.exc.WrongClassNameException;
import jbse.common.exc.InvalidInputException;
import jbse.mem.State.Phase;
import jbse.val.HistoryPoint;
import jbse.val.SymbolFactory;

public class StateTest {
    private Classpath cp;

	@Before
	public void before() throws InvalidClassFileFactoryClassException, InvalidInputException, IOException, 
	ClassFileNotFoundException, ClassFileIllFormedException, ClassFileNotAccessibleException, IncompatibleClassFileException, 
	BadClassFileVersionException, RenameUnsupportedException, WrongClassNameException {
		this.cp = new Classpath(Paths.get(".", "build", "classes"), Paths.get(System.getProperty("java.home", "")), 
		                        new ArrayList<>(Arrays.stream(System.getProperty("java.ext.dirs", "").split(File.pathSeparator)).map(s -> Paths.get(s)).collect(Collectors.toList())), 
		                        Collections.emptyList());
	}
    
	@Test
	public void testFreshStateBasicChecks() throws InvalidClassFileFactoryClassException, InvalidInputException {
		final State s = new State(true, HistoryPoint.unknown(), 10, 100, this.cp, ClassFileFactoryJavassist.class, Collections.emptyMap(), Collections.emptyMap(), new SymbolFactory());
		assertEquals(true, s.bypassStandardLoading());
		assertEquals(HistoryPoint.unknown(), s.getHistoryPoint());
		assertEquals(Phase.PRE_INITIAL, s.phase());
		assertEquals(0, s.getStackSize());
		assertEquals(false, s.isStuck());
		assertEquals(null, s.getStuckException());
		assertEquals(null, s.getStuckReturn());
	}
}
