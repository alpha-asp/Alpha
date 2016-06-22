package at.ac.tuwien.kr.alpha;

import java.util.Arrays;

class Function extends Term {
	private final String name;
	private final Term[] arguments;

	public Function(String name, Term[] arguments) {
		this.name = name;
		this.arguments = arguments;
	}

	@Override
	public String toString() {
		return name + Arrays.toString(arguments);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		Function function = (Function) o;

		if (!name.equals(function.name)) {
			return false;
		}
		return Arrays.equals(arguments, function.arguments);
	}

	@Override
	public int hashCode() {
		int result = name.hashCode();
		result = 31 * result + Arrays.hashCode(arguments);
		return result;
	}
}
