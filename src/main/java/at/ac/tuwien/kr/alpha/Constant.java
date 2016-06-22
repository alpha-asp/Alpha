package at.ac.tuwien.kr.alpha;

import java.util.HashMap;

class Constant extends Term {
	private final String name;

	private static HashMap<String, Constant> cache = new HashMap<>();

	private Constant(String name) {
		this.name = name;
	}

	public static Constant getInstance(String name) {
		return cache.computeIfAbsent(name, Constant::new);
	}

	public String getName() {
		return name;
	}

	@Override
	public boolean equals(Object o) {
		return this == o || (o != null && getClass() == o.getClass() && name.equals(((Constant) o).name));
	}

	@Override
	public int hashCode() {
		return name.hashCode();
	}

	@Override
	public String toString() {
		return name;
	}
}
