package at.ac.tuwien.kr.alpha.core.solver.reboot.strategies;

import at.ac.tuwien.kr.alpha.core.common.NoGood;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FixedLearnedRebootStrategyTest {
	@Test
	public void testStrategySchedulesAfterLearnedNogoods() {
		RebootStrategy strategy = new FixedLearnedRebootStrategy(2);
		strategy.newLearnedNoGood(new NoGood());
		strategy.newLearnedNoGood(new NoGood());
		assertTrue(strategy.isRebootScheduled());
	}

	@Test
	public void testStrategySchedulesAfterEnumerationNogoods() {
		RebootStrategy strategy = new FixedLearnedRebootStrategy(2);
		strategy.newEnumerationNoGood(new NoGood());
		strategy.newEnumerationNoGood(new NoGood());
		assertTrue(strategy.isRebootScheduled());
	}

	@Test
	public void testStrategySchedulesAfterMixedNogoods() {
		RebootStrategy strategy = new FixedLearnedRebootStrategy(2);
		strategy.newLearnedNoGood(new NoGood());
		strategy.newEnumerationNoGood(new NoGood());
		assertTrue(strategy.isRebootScheduled());
	}

	@Test
	public void testStrategySchedulesAfterNogoodBundle() {
		RebootStrategy strategy = new FixedLearnedRebootStrategy(2);
		Collection<NoGood> noGoods = List.of(new NoGood(), new NoGood());
		strategy.newLearnedNoGoods(noGoods);
		assertTrue(strategy.isRebootScheduled());
	}

	@Test
	public void testStrategyResetsAfterReboot() {
		RebootStrategy strategy = new FixedLearnedRebootStrategy(1);
		strategy.newLearnedNoGood(new NoGood());
		assertTrue(strategy.isRebootScheduled());
		strategy.rebootPerformed();
		assertFalse(strategy.isRebootScheduled());
	}

	@Test
	public void testStrategyDoesNotScheduleImmediately() {
		RebootStrategy strategy = new FixedLearnedRebootStrategy(2);
		assertFalse(strategy.isRebootScheduled());
	}

	@Test
	public void testStrategyDoesNotScheduleAfterSmallerBundle() {
		RebootStrategy strategy = new FixedLearnedRebootStrategy(3);
		Collection<NoGood> noGoods = List.of(new NoGood(), new NoGood());
		strategy.newLearnedNoGoods(noGoods);
		assertFalse(strategy.isRebootScheduled());
	}

	@Test
	public void testStrategyDoesNotScheduleAfterSingleEnumerationNogood() {
		RebootStrategy strategy = new FixedLearnedRebootStrategy(2);
		strategy.newEnumerationNoGood(new NoGood());
		assertFalse(strategy.isRebootScheduled());
	}

	@Test
	public void testStrategyDoesNotScheduleAfterStaticNogoods() {
		RebootStrategy strategy = new FixedLearnedRebootStrategy(2);
		strategy.newNoGood(new NoGood());
		strategy.newNoGood(new NoGood());
		assertFalse(strategy.isRebootScheduled());
	}

	@Test
	public void testStrategyDoesNotScheduleAfterStaticNogoodBundle() {
		RebootStrategy strategy = new FixedLearnedRebootStrategy(2);
		strategy.newNoGoods(List.of(new NoGood(), new NoGood()));
		assertFalse(strategy.isRebootScheduled());
	}

	@Test
	public void testStrategyDoesNotScheduleAfterJustificationNogoods() {
		RebootStrategy strategy = new FixedLearnedRebootStrategy(2);
		strategy.newJustificationNoGood(new NoGood());
		strategy.newJustificationNoGood(new NoGood());
		assertFalse(strategy.isRebootScheduled());
	}

	@Test
	public void testStrategyDoesNotScheduleAfterUnrelatedEvents() {
		RebootStrategy strategy = new FixedLearnedRebootStrategy(1);

		strategy.nextIteration();
		assertFalse(strategy.isRebootScheduled());

		strategy.decisionMade();
		assertFalse(strategy.isRebootScheduled());

		strategy.conflictEncountered();
		assertFalse(strategy.isRebootScheduled());

		strategy.backtrackJustified();
		assertFalse(strategy.isRebootScheduled());

		strategy.answerSetFound();
		assertFalse(strategy.isRebootScheduled());
	}
}
