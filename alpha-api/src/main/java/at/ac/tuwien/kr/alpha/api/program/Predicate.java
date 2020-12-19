package at.ac.tuwien.kr.alpha.api.program;

public interface Predicate extends Comparable<Predicate> {
	String getName();

	int getArity();
}
