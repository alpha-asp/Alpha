package at.ac.tuwien.kr.alpha.solver;

/**
 * Indicates the presence of a conflict and contains its reason in terms of a violated Antecedent.
 * Throughout the solver the absence of a conflict is indicated by ConflictCause = null.
 */
public class ConflictCause {
	// Note: directly replacing ConflictCause by Antecedent requires another indicator flag of whether a conflict occurred.
	private final Antecedent violatedNoGood;

	public ConflictCause(Antecedent violatedNoGood) {
		this.violatedNoGood = violatedNoGood;
	}

	public Antecedent getAntecedent() {
		return violatedNoGood;
	}

	@Override
	public String toString() {
		if (violatedNoGood != null) {
			return violatedNoGood.toString();
		}

		return "null";
	}
}
