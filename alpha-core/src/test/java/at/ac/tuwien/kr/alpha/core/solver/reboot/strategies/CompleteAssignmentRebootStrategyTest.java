package at.ac.tuwien.kr.alpha.core.solver.reboot.strategies;

import at.ac.tuwien.kr.alpha.core.common.NoGood;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CompleteAssignmentRebootStrategyTest {
	@Test
	public void testStrategySchedulesAfterAnswerSet() {
		RebootStrategy strategy = new CompleteAssignmentRebootStrategy();
		strategy.answerSetFound();
		assertTrue(strategy.isRebootScheduled());
	}

	@Test
	public void testStrategySchedulesAfterJustification() {
		RebootStrategy strategy = new CompleteAssignmentRebootStrategy();
		strategy.backtrackJustified();
		assertTrue(strategy.isRebootScheduled());
	}

	@Test
	public void testStrategyResetsAfterReboot() {
		RebootStrategy strategy = new CompleteAssignmentRebootStrategy();
		strategy.answerSetFound();
		assertTrue(strategy.isRebootScheduled());
		strategy.rebootPerformed();
		assertFalse(strategy.isRebootScheduled());
	}

	@Test
	public void testStrategyDoesNoScheduleImmediately() {
		RebootStrategy strategy = new CompleteAssignmentRebootStrategy();
		assertFalse(strategy.isRebootScheduled());
	}

	@Test
	public void testStrategyDoesNotScheduleAfterUnrelatedEvents() {
		RebootStrategy strategy = new CompleteAssignmentRebootStrategy();

		strategy.nextIteration();
		assertFalse(strategy.isRebootScheduled());

		strategy.decisionMade();
		assertFalse(strategy.isRebootScheduled());

		strategy.conflictEncountered();
		assertFalse(strategy.isRebootScheduled());

		strategy.newNoGood(new NoGood());
		assertFalse(strategy.isRebootScheduled());

		strategy.newLearnedNoGood(new NoGood());
		assertFalse(strategy.isRebootScheduled());

		strategy.newEnumerationNoGood(new NoGood());
		assertFalse(strategy.isRebootScheduled());

		strategy.newJustificationNoGood(new NoGood());
		assertFalse(strategy.isRebootScheduled());
	}
}
