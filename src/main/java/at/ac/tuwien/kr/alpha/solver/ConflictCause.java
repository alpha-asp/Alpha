package at.ac.tuwien.kr.alpha.solver;

import at.ac.tuwien.kr.alpha.common.Assignment;
import at.ac.tuwien.kr.alpha.common.NoGood;

public class ConflictCause {
	private final NoGood violatedNoGood;
	private final Assignment.Entry violatedChoice;

	public ConflictCause(NoGood violatedNoGood) {
		this.violatedNoGood = violatedNoGood;
		this.violatedChoice = null;
	}

	public ConflictCause(Assignment.Entry violatedChoice) {
		this.violatedNoGood = null;
		this.violatedChoice = violatedChoice;
	}

	public Assignment.Entry getViolatedChoice() {
		return violatedChoice;
	}

	public NoGood getViolatedNoGood() {
		return violatedNoGood;
	}

	@Override
	public String toString() {
		if (violatedNoGood != null) {
			return violatedNoGood.toString();
		}
		if (violatedChoice != null) {
			return violatedChoice.toString();
		}

		return "null";
	}
}
