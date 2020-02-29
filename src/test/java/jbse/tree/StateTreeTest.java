package jbse.tree;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Collections;

import org.junit.Test;

import jbse.bc.ClassFileFactoryJavassist;
import jbse.bc.Classpath;
import jbse.bc.exc.InvalidClassFileFactoryClassException;
import jbse.common.exc.InvalidInputException;
import jbse.mem.State;
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
		final State _1_1 = new State(true, tree.getPreInitialHistoryPoint().startingInitial(), 100, 100, new Classpath(Paths.get("."), Paths.get("."), Collections.emptyList(), Collections.emptyList()), ClassFileFactoryJavassist.class, Collections.emptyMap(), Collections.emptyMap(), new SymbolFactory());
		_1_1.setPhasePostInitial();
		final State _1_2 = _1_1.clone();
		final State _1_3 = _1_1.clone();
		final State _1_4 = _1_1.clone();
		tree.possiblyAddBranchPoint(true, false, false, false);
		tree.addState(_1_4, 0, null);
		tree.addState(_1_3, 0, null);
		tree.addState(_1_2, 0, null);
		tree.addState(_1_1, 0, null);
		assertSame(_1_1, tree.nextState());
		assertSame(_1_2, tree.nextState());
		assertSame(_1_3, tree.nextState());
		assertSame(_1_4, tree.nextState());
		assertEquals(".1.1", _1_1.getBranchIdentifier().toString());
		assertEquals(".1.2", _1_2.getBranchIdentifier().toString());
		assertEquals(".1.3", _1_3.getBranchIdentifier().toString());
		assertEquals(".1.4", _1_4.getBranchIdentifier().toString());
	}
	
	@Test
	public void testAddState2() throws InvalidClassFileFactoryClassException, IOException, InvalidInputException {
		final StateTree tree = new StateTree(StateIdentificationMode.COMPACT, BreadthMode.MORE_THAN_ONE);
		final State _1_1 = new State(true, tree.getPreInitialHistoryPoint().startingInitial(), 100, 100, new Classpath(Paths.get("."), Paths.get("."), Collections.emptyList(), Collections.emptyList()), ClassFileFactoryJavassist.class, Collections.emptyMap(), Collections.emptyMap(), new SymbolFactory());
		_1_1.setPhasePostInitial();
		final State _1_2 = _1_1.clone();
		final State _1_3 = _1_1.clone();
		final State _1_4 = _1_1.clone();
		tree.possiblyAddBranchPoint(true, false, false, false);
		tree.addState(_1_4, 0, null);
		tree.addState(_1_3, 0, null);
		tree.addState(_1_2, 0, null);
		tree.addState(_1_1, 0, null);
		assertSame(_1_1, tree.nextState());
		assertSame(_1_2, tree.nextState());
		assertSame(_1_3, tree.nextState());
		assertEquals(".1.1", _1_1.getBranchIdentifier().toString());
		assertEquals(".1.2", _1_2.getBranchIdentifier().toString());
		assertEquals(".1.3", _1_3.getBranchIdentifier().toString());
		final State _1_3_1 = _1_3.clone();
		final State _1_3_2 = _1_3.clone();
		tree.possiblyAddBranchPoint(true, false, false, false);
		tree.addState(_1_3_2, 0, null);
		tree.addState(_1_3_1, 0, null);
		assertSame(_1_3_1, tree.nextState());
		assertSame(_1_3_2, tree.nextState());
		assertSame(_1_4, tree.nextState());
		assertEquals(".1.3.1", _1_3_1.getBranchIdentifier().toString());
		assertEquals(".1.3.2", _1_3_2.getBranchIdentifier().toString());
		assertEquals(".1.4", _1_4.getBranchIdentifier().toString());
	}
	
	@Test
	public void testGetStateAtBranch() throws InvalidClassFileFactoryClassException, IOException, InvalidInputException {
		final StateTree tree = new StateTree(StateIdentificationMode.COMPACT, BreadthMode.MORE_THAN_ONE);
		final State _1_1 = new State(true, tree.getPreInitialHistoryPoint().startingInitial(), 100, 100, new Classpath(Paths.get("."), Paths.get("."), Collections.emptyList(), Collections.emptyList()), ClassFileFactoryJavassist.class, Collections.emptyMap(), Collections.emptyMap(), new SymbolFactory());
		_1_1.setPhasePostInitial();
		final State _1_2 = _1_1.clone();
		final State _1_3 = _1_1.clone();
		final State _1_4 = _1_1.clone();
		tree.possiblyAddBranchPoint(true, false, false, false);
		tree.addState(_1_4, 0, null);
		tree.addState(_1_3, 0, null);
		tree.addState(_1_2, 0, null);
		tree.addState(_1_1, 0, null);
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
		tree.addState(_1_2_6, 0, null);
		tree.addState(_1_2_5, 0, null);
		tree.addState(_1_2_4, 0, null);
		tree.addState(_1_2_3, 0, null);
		tree.addState(_1_2_2, 0, null);
		tree.addState(_1_2_1, 0, null);
		tree.nextState(); //emits _1_2_1
		tree.nextState(); //emits _1_2_2
		tree.nextState(); //emits _1_2_3
		tree.nextState(); //emits _1_2_4
		final State _1_2_4_1 = _1_2_4.clone();
		final State _1_2_4_2 = _1_2_4.clone();
		tree.possiblyAddBranchPoint(true, false, false, false);
		tree.addState(_1_2_4_2, 0, null);
		tree.addState(_1_2_4_1, 0, null);
		
		final State statePicked = tree.getStateAtBranch(bp_1_2, 1);
		assertSame(_1_2_6, statePicked);
	}
}
