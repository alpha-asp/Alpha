package at.ac.tuwien.kr.alpha.core.solver.reboot.strategies;

import at.ac.tuwien.kr.alpha.core.common.NoGood;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AnswerSetRebootStrategyTest {
	@Test
	public void testStrategySchedulesAfterAnswerSet() {
		RebootStrategy strategy = new AnswerSetRebootStrategy();
		strategy.answerSetFound();
		assertTrue(strategy.isRebootScheduled());
	}

	@Test
	public void testStrategyResetsAfterReboot() {
		RebootStrategy strategy = new AnswerSetRebootStrategy();
		strategy.answerSetFound();
		assertTrue(strategy.isRebootScheduled());
		strategy.rebootPerformed();
		assertFalse(strategy.isRebootScheduled());
	}

	@Test
	public void testStrategyDoesNotScheduleImmediately() {
		RebootStrategy strategy = new AnswerSetRebootStrategy();
		assertFalse(strategy.isRebootScheduled());
	}

	@Test
	public void testStrategyDoesNotScheduleAfterIteration() {
		RebootStrategy strategy = new AnswerSetRebootStrategy();
		strategy.nextIteration();
		assertFalse(strategy.isRebootScheduled());
	}

	@Test
	public void testStrategyDoesNotScheduleAfterUnrelatedEvents() {
		RebootStrategy strategy = new AnswerSetRebootStrategy();

		strategy.nextIteration();
		assertFalse(strategy.isRebootScheduled());

		strategy.decisionMade();
		assertFalse(strategy.isRebootScheduled());

		strategy.conflictEncountered();
		assertFalse(strategy.isRebootScheduled());

		strategy.backtrackJustified();
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
