package at.ac.tuwien.kr.alpha;

class Rule {
	private final Atom head;
	private final Atom[] bodyPos;
	private final Atom[] bodyNeg;

	private final String[] variables;

	public Rule(String[] variables, Atom head, Atom[] bodyPos, Atom[] bodyNeg) {
		this.variables = variables;
		this.head = head;
		this.bodyPos = bodyPos;
		this.bodyNeg = bodyNeg;
	}

	public boolean isConstraint() {
		return head == null;
	}

	public String getVariable(int index) {
		return variables[index];
	}

	public Atom[] getBodyPositive() {
		return bodyPos;
	}

	public Atom[] getBodyNegative() {
		return bodyNeg;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		if (head != null) {
			sb.append(head);
			sb.append(" ");
		}
		sb.append(":- ");
		if (bodyPos != null) {
			sb.append(bodyPos);
		}
		if (bodyPos != null && bodyNeg != null) {
			sb.append(", ");
		}
		if (bodyNeg != null) {
			sb.append("not");
			sb.append(bodyNeg);
		}
		sb.append(".");
		return sb.toString();
	}
}