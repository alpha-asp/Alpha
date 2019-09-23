package at.ac.tuwien.kr.alpha.solver;

import at.ac.tuwien.kr.alpha.common.*;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static at.ac.tuwien.kr.alpha.common.NoGoodTest.fromOldLiterals;
import static org.junit.Assert.*;

public class LearnedNoGoodDeletionTest {

	private NoGoodStoreAlphaRoaming store;
	private LearnedNoGoodDeletion learnedNoGoodDeletion;

	public LearnedNoGoodDeletionTest() {
		AtomStore atomStore = new AtomStoreImpl();
		AtomStoreTest.fillAtomStore(atomStore, 200);
		WritableAssignment assignment = new TrailAssignment(atomStore);
		assignment.growForMaxAtomId();
		store = new NoGoodStoreAlphaRoaming(assignment);
		learnedNoGoodDeletion = store.getLearnedNoGoodDeletion();
	}

	@Before
	public void setUp() {
		store.clear();
		store.growForMaxAtomId(fromOldLiterals(200));
		assertNull(store.add(1, new NoGood(fromOldLiterals(1, -2))));
		assertNull(store.add(2, new NoGood(fromOldLiterals(2, -3))));
		assertNull(store.add(3, new NoGood(fromOldLiterals(2, -4, -5))));
		assertNull(store.add(4, new NoGood(fromOldLiterals(3, 4, 5))));
		assertNull(store.propagate());
	}

	@Test
	public void testDeletionRunningAfterConflicts() {
		for (int i = 0; i < LearnedNoGoodDeletion.RUN_AFTER_AT_LEAST; i++) {
			learnedNoGoodDeletion.increaseConflictCounter();
		}
		assertFalse(learnedNoGoodDeletion.needToRunNoGoodDeletion());
		learnedNoGoodDeletion.increaseConflictCounter();
		assertTrue(learnedNoGoodDeletion.needToRunNoGoodDeletion());
		learnedNoGoodDeletion.runNoGoodDeletion();
		assertFalse(learnedNoGoodDeletion.needToRunNoGoodDeletion());
	}

	@Test
	public void testDeletionRemovingWatches() {
		for (int i = 0; i < LearnedNoGoodDeletion.RUN_AFTER_AT_LEAST; i++) {
			learnedNoGoodDeletion.increaseConflictCounter();
		}
		assertFalse(learnedNoGoodDeletion.needToRunNoGoodDeletion());
		learnedNoGoodDeletion.increaseConflictCounter();
		assertTrue(learnedNoGoodDeletion.needToRunNoGoodDeletion());
		assertNull(store.add(4, NoGood.learnt(fromOldLiterals(10, 11, 12)), 3));
		assertNull(store.add(5, NoGood.learnt(fromOldLiterals(10, -13, -14)), 4));
		List<WatchedNoGood> watchedNoGoods = learnedNoGoodDeletion.inspectLearnedNoGoods();
		assertTrue(watchedNoGoods.size() >= 2);
		WatchedNoGood watchedNoGood = watchedNoGoods.get(0);
		for (int i = 0; i < 10; i++) {
			watchedNoGood.bumpActivity();
		}
		learnedNoGoodDeletion.runNoGoodDeletion();
		watchedNoGoods = learnedNoGoodDeletion.inspectLearnedNoGoods();
		assertTrue(watchedNoGoods.size() < 2);
	}
}