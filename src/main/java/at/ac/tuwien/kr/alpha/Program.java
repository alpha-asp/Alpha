package at.ac.tuwien.kr.alpha;

class Program {
	private final String[] constants;
	private final String[] predicates;
	private final Rule[] rules;

	Program(String[] constants, String[] predicates, Rule[] rules) {
		this.constants = constants;
		this.predicates = predicates;
		this.rules = rules;
	}

	public String getPredicate(int index) {
		return predicates[index];
	}

	public String getConstant(int index) {
		return constants[index];
	}

	public Rule[] getRules() {
		return rules;
	}
}
