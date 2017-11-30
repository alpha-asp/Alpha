package at.ac.tuwien.kr.alpha.grounder;

import at.ac.tuwien.kr.alpha.common.terms.ConstantTerm;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

/**
 * Copyright (c) 2016, the Alpha Team.
 */
public class IndexedInstanceStorageTest {
	@Test
	public void testIndexedInstanceStorage() {
		IndexedInstanceStorage storage = new IndexedInstanceStorage("A test storage of arity 4", 4);
		storage.addIndexPosition(0);
		storage.addIndexPosition(2);
		ConstantTerm t0 = ConstantTerm.getInstance("0");
		ConstantTerm t1 = ConstantTerm.getInstance("1");
		ConstantTerm t2 = ConstantTerm.getInstance("2");
		ConstantTerm t3 = ConstantTerm.getInstance("3");
		ConstantTerm t4 = ConstantTerm.getInstance("4");
		ConstantTerm t5 = ConstantTerm.getInstance("5");

		Instance badInst1 = new Instance(t1, t1, t0);
		Instance badInst2 = new Instance(t5, t5, t5, t5, t5);

		try {
			storage.addInstance(badInst1);
			fail();
		} catch (Exception e) {
			assertTrue(e.getMessage().startsWith("Instance length does not match arity of IndexedInstanceStorage"));
		}

		try {
			storage.addInstance(badInst2);
			fail();
		} catch (Exception e) {
			assertTrue(e.getMessage().startsWith("Instance length does not match arity of IndexedInstanceStorage"));
		}

		Instance inst1 = new Instance(t1, t1, t1, t1);
		Instance inst2 = new Instance(t1, t2, t3, t4);
		Instance inst3 = new Instance(t4, t3, t3, t5);
		Instance inst4 = new Instance(t1, t2, t1, t1);
		Instance inst5 = new Instance(t5, t4, t3, t2);

		storage.addInstance(inst1);
		storage.addInstance(inst2);
		storage.addInstance(inst3);
		storage.addInstance(inst4);
		storage.addInstance(inst5);

		List<Instance> matching3 = storage.getInstancesMatchingAtPosition(t3, 2);
		assertEquals(matching3.size(), 3);
		assertTrue(matching3.contains(new Instance(t1, t2, t3, t4)));
		assertTrue(matching3.contains(new Instance(t4, t3, t3, t5)));
		assertTrue(matching3.contains(new Instance(t5, t4, t3, t2)));
		assertFalse(matching3.contains(new Instance(t1, t1, t1, t1)));

		List<Instance> matching1 = storage.getInstancesMatchingAtPosition(t2, 0);
		assertEquals(matching1.size(), 0);
	}

}