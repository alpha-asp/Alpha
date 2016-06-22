package at.ac.tuwien.kr.alpha;

class Rule {
	private final Atom head;
	private final Atom[] bodyPos;
	private final Atom[] bodyNeg;

	public Rule(Atom head, Atom[] bodyPos, Atom[] bodyNeg) {
		this.head = head;
		this.bodyPos = bodyPos;
		this.bodyNeg = bodyNeg;
	}

	public boolean isConstraint() {
		return head == null;
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