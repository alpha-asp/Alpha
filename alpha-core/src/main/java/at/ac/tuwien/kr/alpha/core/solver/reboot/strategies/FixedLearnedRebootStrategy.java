package at.ac.tuwien.kr.alpha.core.solver.reboot.strategies;

import at.ac.tuwien.kr.alpha.core.common.NoGood;

public class FixedLearnedRebootStrategy implements RebootStrategy {
	private final int breakpoint;
	private int learnedCount;

	public FixedLearnedRebootStrategy(int breakpoint) {
		this.breakpoint = breakpoint;
		this.learnedCount = 0;
	}

	@Override
	public void newEnumerationNoGood(NoGood noGood) {
		learnedCount++;
	}

	@Override
	public void newLearnedNoGood(NoGood noGood) {
		learnedCount++;
	}

	@Override
	public boolean isRebootScheduled() {
		return learnedCount >= breakpoint;
	}

	@Override
	public void rebootPerformed() {
		learnedCount = 0;
	}
}
