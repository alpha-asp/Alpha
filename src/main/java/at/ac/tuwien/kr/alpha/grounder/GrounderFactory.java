package at.ac.tuwien.kr.alpha.grounder;

import at.ac.tuwien.kr.alpha.common.Predicate;
import at.ac.tuwien.kr.alpha.grounder.bridges.Bridge;

public final class GrounderFactory {
	public static Grounder getInstance(String name, java.util.function.Predicate<Predicate> filter, Bridge... bridges) {
		switch (name.toLowerCase()) {
			case "naive": return new NaiveGrounder(filter, bridges);
		}
		throw new IllegalArgumentException("Unknown grounder requested.");
	}
}
