package jbse.tree;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;

import org.junit.Test;

import jbse.bc.ClassFileFactoryJavassist;
import jbse.bc.Classpath;
import jbse.bc.exc.InvalidClassFileFactoryClassException;
import jbse.common.exc.InvalidInputException;
import jbse.mem.State;
import jbse.rewr.CalculatorRewriting;
import jbse.tree.StateTree.BranchPoint;
import jbse.tree.StateTree.BreadthMode;
import jbse.tree.StateTree.StateIdentificationMode;
import jbse.val.SymbolFactory;

public class StateTreeTest {
	@Test
	public void testPossiblyAddBranchPoint() {
		final StateTree tree = new StateTree(StateIdentificationMode.COMPACT, BreadthMode.MORE_THAN_ONE);
		assertTrue(tree.possiblyAddBranchPoint(true, false, false, false));
	}
	
	@Test
	public void testAddState1() throws InvalidClassFileFactoryClassException, IOException, InvalidInputException {
		final StateTree tree = new StateTree(StateIdentificationMode.COMPACT, BreadthMode.MORE_THAN_ONE);
		final CalculatorRewriting calc = new CalculatorRewriting();
		final State _1_1 = new State(true, tree.getPreInitialHistoryPoint().startingInitial(), 100, 100, new Classpath(Paths.get("."), Collections.emptyList(), Collections.emptyList()), ClassFileFactoryJavassist.class, new HashMap<>(), calc, new SymbolFactory(calc));
		_1_1.setPhasePostInitial();
		final State _1_2 = _1_1.clone();
		final State _1_3 = _1_1.clone();
		final State _1_4 = _1_1.clone();
		tree.possiblyAddBranchPoint(true, false, false, false);
		tree.addState(_1_4);
		tree.addState(_1_3);
		tree.addState(_1_2);
		tree.addState(_1_1);
		assertSame(_1_1, tree.nextState());
		assertSame(_1_2, tree.nextState());
		assertSame(_1_3, tree.nextState());
		assertSame(_1_4, tree.nextState());
		assertEquals(".1.1", _1_1.getIdentifier().toString());
		assertEquals(".1.2", _1_2.getIdentifier().toString());
		assertEquals(".1.3", _1_3.getIdentifier().toString());
		assertEquals(".1.4", _1_4.getIdentifier().toString());
	}
	
	@Test
	public void testAddState2() throws InvalidClassFileFactoryClassException, IOException, InvalidInputException {
		final StateTree tree = new StateTree(StateIdentificationMode.COMPACT, BreadthMode.MORE_THAN_ONE);
		final CalculatorRewriting calc = new CalculatorRewriting();
		final State _1_1 = new State(true, tree.getPreInitialHistoryPoint().startingInitial(), 100, 100, new Classpath(Paths.get("."), Collections.emptyList(), Collections.emptyList()), ClassFileFactoryJavassist.class, new HashMap<>(), calc, new SymbolFactory(calc));
		_1_1.setPhasePostInitial();
		final State _1_2 = _1_1.clone();
		final State _1_3 = _1_1.clone();
		final State _1_4 = _1_1.clone();
		tree.possiblyAddBranchPoint(true, false, false, false);
		tree.addState(_1_4);
		tree.addState(_1_3);
		tree.addState(_1_2);
		tree.addState(_1_1);
		assertSame(_1_1, tree.nextState());
		assertSame(_1_2, tree.nextState());
		assertSame(_1_3, tree.nextState());
		assertEquals(".1.1", _1_1.getIdentifier().toString());
		assertEquals(".1.2", _1_2.getIdentifier().toString());
		assertEquals(".1.3", _1_3.getIdentifier().toString());
		final State _1_3_1 = _1_3.clone();
		final State _1_3_2 = _1_3.clone();
		tree.possiblyAddBranchPoint(true, false, false, false);
		tree.addState(_1_3_2);
		tree.addState(_1_3_1);
		assertSame(_1_3_1, tree.nextState());
		assertSame(_1_3_2, tree.nextState());
		assertSame(_1_4, tree.nextState());
		assertEquals(".1.3.1", _1_3_1.getIdentifier().toString());
		assertEquals(".1.3.2", _1_3_2.getIdentifier().toString());
		assertEquals(".1.4", _1_4.getIdentifier().toString());
	}
	
	@Test
	public void testGetStateAtBranch() throws InvalidClassFileFactoryClassException, IOException, InvalidInputException {
		final StateTree tree = new StateTree(StateIdentificationMode.COMPACT, BreadthMode.MORE_THAN_ONE);
		final CalculatorRewriting calc = new CalculatorRewriting();
		final State _1_1 = new State(true, tree.getPreInitialHistoryPoint().startingInitial(), 100, 100, new Classpath(Paths.get("."), Collections.emptyList(), Collections.emptyList()), ClassFileFactoryJavassist.class, new HashMap<>(), calc, new SymbolFactory(calc));
		_1_1.setPhasePostInitial();
		final State _1_2 = _1_1.clone();
		final State _1_3 = _1_1.clone();
		final State _1_4 = _1_1.clone();
		tree.possiblyAddBranchPoint(true, false, false, false);
		tree.addState(_1_4);
		tree.addState(_1_3);
		tree.addState(_1_2);
		tree.addState(_1_1);
		tree.nextState(); //emits _1_1
		tree.nextState(); //emits _1_2
		final State _1_2_1 = _1_2.clone();
		final State _1_2_2 = _1_2.clone();
		final State _1_2_3 = _1_2.clone();
		final State _1_2_4 = _1_2.clone();
		final State _1_2_5 = _1_2.clone();
		final State _1_2_6 = _1_2.clone();
		tree.possiblyAddBranchPoint(true, false, false, false);
		final BranchPoint bp_1_2 = tree.nextBranch();
		tree.addState(_1_2_6);
		tree.addState(_1_2_5);
		tree.addState(_1_2_4);
		tree.addState(_1_2_3);
		tree.addState(_1_2_2);
		tree.addState(_1_2_1);
		tree.nextState(); //emits _1_2_1
		tree.nextState(); //emits _1_2_2
		tree.nextState(); //emits _1_2_3
		tree.nextState(); //emits _1_2_4
		final State _1_2_4_1 = _1_2_4.clone();
		final State _1_2_4_2 = _1_2_4.clone();
		tree.possiblyAddBranchPoint(true, false, false, false);
		tree.addState(_1_2_4_2);
		tree.addState(_1_2_4_1);
		
		final State statePicked = tree.getStateAtBranch(bp_1_2, 1);
		assertSame(_1_2_6, statePicked);
	}
}
