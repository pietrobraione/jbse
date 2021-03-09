package jbse.mem;

import static org.junit.Assert.assertThat;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Test;

import jbse.bc.ClassFile;
import jbse.bc.ClassFileFactoryJavassist;
import jbse.bc.ClassHierarchy;
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
import jbse.mem.Array.AccessOutcome;
import jbse.mem.Array.AccessOutcomeInValue;
import jbse.mem.Array.AccessOutcomeOut;
import jbse.mem.exc.FastArrayAccessNotAllowedException;
import jbse.rewr.CalculatorRewriting;
import jbse.rewr.RewriterExpressionOrConversionOnSimplex;
import jbse.rewr.RewriterFunctionApplicationOnSimplex;
import jbse.rewr.RewriterNegationElimination;
import jbse.rewr.RewriterNormalize;
import jbse.rewr.RewriterPolynomials;
import jbse.rewr.RewriterZeroUnit;
import jbse.val.HistoryPoint;
import jbse.val.Primitive;
import jbse.val.Value;
import jbse.val.exc.InvalidOperandException;
import jbse.val.exc.InvalidTypeException;

public class ArrayTest {
	private CalculatorRewriting calc;
    private Classpath cp;
    private ClassHierarchy hier;
    private HistoryPoint hp;
	
	@Before
	public void before() throws InvalidClassFileFactoryClassException, InvalidInputException, IOException, 
	ClassFileNotFoundException, ClassFileIllFormedException, ClassFileNotAccessibleException, IncompatibleClassFileException, 
	BadClassFileVersionException, RenameUnsupportedException, WrongClassNameException {
		this.calc = new CalculatorRewriting();
		this.calc.addRewriter(new RewriterExpressionOrConversionOnSimplex());
        this.calc.addRewriter(new RewriterFunctionApplicationOnSimplex());
        this.calc.addRewriter(new RewriterZeroUnit());
        this.calc.addRewriter(new RewriterNegationElimination());
		this.calc.addRewriter(new RewriterPolynomials());
		this.calc.addRewriter(new RewriterNormalize());
		this.cp = new Classpath(Paths.get(".", "build", "classes"), Paths.get(System.getProperty("java.home", "")), 
		                        new ArrayList<>(Arrays.stream(System.getProperty("java.ext.dirs", "").split(File.pathSeparator)).map(s -> Paths.get(s)).collect(Collectors.toList())), 
		                        Collections.emptyList());
		this.hier = new ClassHierarchy(this.cp, ClassFileFactoryJavassist.class, Collections.emptyMap(), Collections.emptyMap());
		this.hier.loadCreateClass("java/lang/Object");
		this.hier.loadCreateClass("java/lang/Cloneable");
		this.hier.loadCreateClass("java/io/Serializable");
		this.hp = HistoryPoint.unknown();
	}

	@Test
	public void arrayConcreteBasicLenghTest() throws InvalidInputException, InvalidTypeException, ClassFileNotFoundException, 
	ClassFileIllFormedException, ClassFileNotAccessibleException, IncompatibleClassFileException, BadClassFileVersionException, RenameUnsupportedException, WrongClassNameException {
		final ClassFile cfArray = this.hier.loadCreateClass("[I");
		final ArrayImpl a = new ArrayImpl(this.calc, false, false, null, this.calc.valInt(0), cfArray, null, this.hp, false, 10);
		final Primitive length = a.getLength();
		assertThat(length, is(equalTo(this.calc.valInt(0))));
	}

	@Test
	public void arrayConcreteFastSetGetTest() throws InvalidInputException, InvalidTypeException, ClassFileNotFoundException, 
	ClassFileIllFormedException, ClassFileNotAccessibleException, IncompatibleClassFileException, BadClassFileVersionException, 
	RenameUnsupportedException, WrongClassNameException, FastArrayAccessNotAllowedException {
		final ClassFile cfArray = this.hier.loadCreateClass("[I");
		final ArrayImpl a = new ArrayImpl(this.calc, false, false, null, this.calc.valInt(3), cfArray, null, this.hp, false, 10);
		a.setFast(this.calc.valInt(1), this.calc.valInt(9));
		final Value zero = ((AccessOutcomeInValue) a.getFast(this.calc, this.calc.valInt(2))).getValue();
		final Value nine = ((AccessOutcomeInValue) a.getFast(this.calc, this.calc.valInt(1))).getValue();
		@SuppressWarnings("unused")
		final Object foo = ((AccessOutcomeOut) a.getFast(this.calc, this.calc.valInt(3)));
		assertThat(zero, is(equalTo(this.calc.valInt(0))));
		assertThat(nine, is(equalTo(this.calc.valInt(9))));
	}

	@Test
	public void arraySymbolicLengthAccessAffectedEntries() throws InvalidInputException, InvalidTypeException, ClassFileNotFoundException, 
	ClassFileIllFormedException, ClassFileNotAccessibleException, IncompatibleClassFileException, BadClassFileVersionException, 
	RenameUnsupportedException, WrongClassNameException, FastArrayAccessNotAllowedException {
		final ClassFile cfArray = this.hier.loadCreateClass("[I");
		final ArrayImpl a = new ArrayImpl(this.calc, false, false, null, this.calc.valTerm('I', "A"), cfArray, null, this.hp, false, 10);
		final Iterator<? extends AccessOutcome> affected = a.entriesPossiblyAffectedByAccess(this.calc, this.calc.valInt(1), this.calc.valInt(10));
		int size = 0; 
		AccessOutcome _outcome = null;
		while (affected.hasNext()) {
			++size; 
			_outcome = affected.next();
		}
		assertThat(size, is(equalTo(1)));
		final AccessOutcomeInValue outcome = (AccessOutcomeInValue) _outcome;
		assertThat(outcome.getValue(), is(equalTo(this.calc.valInt(0))));
		assertThat(outcome.getAccessCondition(), is(equalTo(a.inRange(this.calc, a.getIndex()))));
	}

	@Test
	public void arraySymbolicLengthAccessSetting() throws InvalidInputException, InvalidTypeException, ClassFileNotFoundException, 
	ClassFileIllFormedException, ClassFileNotAccessibleException, IncompatibleClassFileException, BadClassFileVersionException, 
	RenameUnsupportedException, WrongClassNameException, FastArrayAccessNotAllowedException, NoSuchElementException, InvalidOperandException {
		final ClassFile cfArray = this.hier.loadCreateClass("[I");
		final ArrayImpl a = new ArrayImpl(this.calc, false, false, null, this.calc.valTerm('I', "A"), cfArray, null, this.hp, false, 10);
		final Iterator<? extends AccessOutcome> affected = a.entriesPossiblyAffectedByAccess(this.calc, this.calc.valInt(1), this.calc.valInt(10));
		final AccessOutcomeInValue outcome = (AccessOutcomeInValue) affected.next();
		outcome.excludeIndexFromAccessCondition(this.calc, this.calc.valInt(1));
		a.set(this.calc, this.calc.valInt(1), this.calc.valInt(10));
		final Collection<AccessOutcome> getOutcomes = a.get(this.calc, this.calc.valInt(1));
		assertThat(getOutcomes.size(), is(equalTo(2))); //{(0<={I}<A && {I}==1) -> 10, ({I}<0 || {I}>=A) -> out_of_range}
		int outcomeIn = 0;
		int outcomeOut = 0;
		for (AccessOutcome getOutcome : getOutcomes) {
			if (getOutcome instanceof AccessOutcomeInValue) {
				final AccessOutcomeInValue outIn = (AccessOutcomeInValue) getOutcome;
				if (outIn.getValue().equals(this.calc.valInt(10)) && 
				outIn.getAccessCondition().equals(this.calc.push(a.inRange(this.calc, a.getIndex())).and(this.calc.push(a.getIndex()).eq(this.calc.valInt(1)).pop()).pop())) {
					++outcomeIn;
				}
			} else if (getOutcome instanceof AccessOutcomeOut) {
				final AccessOutcomeOut outOut = (AccessOutcomeOut) getOutcome;
				if (outOut.getAccessCondition().equals(this.calc.push(a.getIndex()).lt(this.calc.valInt(0)).or(this.calc.push(a.getIndex()).ge(this.calc.valTerm('I', "A")).pop()).pop())) {
					++outcomeOut;
				}
			}
		}
		assertThat(outcomeIn, is(equalTo(1))); //(0<={I}<A && {I}==1) -> 10
		assertThat(outcomeOut, is(equalTo(1))); //({I}<0 || {I}>=A) -> out_of_range
	}
}
