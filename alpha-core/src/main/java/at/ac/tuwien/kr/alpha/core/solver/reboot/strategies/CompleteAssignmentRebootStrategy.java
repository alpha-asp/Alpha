package at.ac.tuwien.kr.alpha.core.solver.reboot.strategies;

public class CompleteAssignmentRebootStrategy implements RebootStrategy {
	private boolean reboot;

	public CompleteAssignmentRebootStrategy() {
		reboot = false;
	}

	@Override
	public void answerSetFound() {
		reboot = true;
	}

	@Override
	public void backtrackJustified() {
		reboot = true;
	}

	@Override
	public boolean isRebootScheduled() {
		return reboot;
	}

	@Override
	public void rebootPerformed() {
		reboot = false;
	}
}
