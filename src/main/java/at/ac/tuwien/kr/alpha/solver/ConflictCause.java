package at.ac.tuwien.kr.alpha.solver;

import at.ac.tuwien.kr.alpha.common.Assignment;
import at.ac.tuwien.kr.alpha.common.NoGood;

public class ConflictCause {
	private final NoGood violatedNoGood;
	private final Assignment.Entry violatedGuess;

	public ConflictCause(NoGood violatedNoGood) {
		this.violatedNoGood = violatedNoGood;
		this.violatedGuess = null;
	}

	public ConflictCause(Assignment.Entry violatedGuess) {
		this.violatedNoGood = null;
		this.violatedGuess = violatedGuess;
	}

	public Assignment.Entry getViolatedGuess() {
		return violatedGuess;
	}

	public NoGood getViolatedNoGood() {
		return violatedNoGood;
	}

	@Override
	public String toString() {
		if (violatedNoGood != null) {
			return violatedNoGood.toString();
		}
		if (violatedGuess != null) {
			return violatedGuess.toString();
		}

		return "null";
	}
}
