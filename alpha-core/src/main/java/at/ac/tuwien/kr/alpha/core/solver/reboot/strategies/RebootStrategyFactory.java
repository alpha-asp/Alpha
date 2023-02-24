package at.ac.tuwien.kr.alpha.core.solver.reboot.strategies;

import at.ac.tuwien.kr.alpha.api.config.SystemConfig;

public final class RebootStrategyFactory {
	public static RebootStrategy getRebootStrategy(SystemConfig config) {
		switch (config.getRebootStrategy()) {
			case FIXED:
				return new FixedLearnedRebootStrategy(config.getRebootStrategyIterations());
			case GEOM:
				return new GeometricLearnedRebootStrategy(config.getRebootStrategyBase(), config.getRebootStrategyFactor());
			case LUBY:
				return new LubyLearnedRebootStrategy(config.getRebootStrategyFactor());
			case ASSIGN:
				return new CompleteAssignmentRebootStrategy();
			case ANSWER:
				return new AnswerSetRebootStrategy();
		}
		throw new IllegalArgumentException("Unknown reboot strategy requested.");
	}
}
