package at.ac.tuwien.kr.alpha.core.solver.reboot.strategies;

public class AnswerSetRebootStrategy implements RebootStrategy {
	private boolean reboot;

	public AnswerSetRebootStrategy() {
		reboot = false;
	}

	@Override
	public void answerSetFound() {
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
