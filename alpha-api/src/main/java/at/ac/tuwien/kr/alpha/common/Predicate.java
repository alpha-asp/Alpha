package at.ac.tuwien.kr.alpha.common;

public interface Predicate extends Comparable<Predicate> {
	String getName();

	int getArity();
}
