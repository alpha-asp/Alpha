package at.ac.tuwien.kr.alpha.core.solver.reboot.strategies;

import at.ac.tuwien.kr.alpha.core.common.NoGood;

public class FixedLearnedRebootStrategy implements RebootStrategy {
	private int breakpoint;
	private int learnedCounter;

	public FixedLearnedRebootStrategy(int breakpoint) {
		this.breakpoint = breakpoint;
		this.learnedCounter = 0;
	}

	@Override
	public void newLearnedNoGood(NoGood noGood) {
		learnedCounter++;
	}

	@Override
	public boolean isRebootScheduled() {
		return learnedCounter >= breakpoint;
	}

	@Override
	public void rebootPerformed() {
		learnedCounter = 0;
	}
}
