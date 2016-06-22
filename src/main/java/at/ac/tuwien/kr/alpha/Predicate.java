package at.ac.tuwien.kr.alpha;

import java.io.Serializable;
import java.util.HashMap;

class Predicate implements Serializable {
	private static final long serialVersionUID = 1L;

	private static HashMap<String, Predicate> cache = new HashMap<>();

	private final String name;
	private final int arity;

	private Predicate(String name, int arity) {
		this.name = name;
		this.arity = arity;
	}

	public static Predicate getInstance(String name, int arity) {
		return cache.computeIfAbsent(toString(name, arity), k -> new Predicate(name, arity));
	}

	private static String toString(String name, int arity) {
		return name + "/" + arity;
	}

	public int getArity() {
		return arity;
	}

	public String getName() {
		return name;
	}

	@Override
	public String toString() {
		return toString(name, arity);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		Predicate predicate = (Predicate) o;

		return arity == predicate.arity && name.equals(predicate.name);
	}

	@Override
	public int hashCode() {
		return 31 * name.hashCode() + arity;
	}
}
