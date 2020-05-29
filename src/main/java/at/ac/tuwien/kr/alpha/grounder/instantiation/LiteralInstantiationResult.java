package at.ac.tuwien.kr.alpha.grounder.instantiation;

import java.util.List;
import java.util.Optional;

import at.ac.tuwien.kr.alpha.grounder.Substitution;

public class LiteralInstantiationResult {

	private final Type type;
	// use optional to ensure empty value is caught as early as possible
	private final Optional<List<Substitution>> substitutions;

	private LiteralInstantiationResult(Type type, Optional<List<Substitution>> substitutions) {
		this.type = type;
		this.substitutions = substitutions;
	}

	public static LiteralInstantiationResult continueBinding(List<Substitution> substitutions) {
		return new LiteralInstantiationResult(Type.CONTINUE, Optional.of(substitutions));
	}

	public static LiteralInstantiationResult stopBinding() {
		return new LiteralInstantiationResult(Type.STOP_BINDING, Optional.empty());
	}

	public static LiteralInstantiationResult pushBack() {
		return new LiteralInstantiationResult(Type.PUSH_BACK, Optional.empty());
	}

	public Type getType() {
		return this.type;
	}

	// unwrap the optional, if empty, exception will be thrown here
	public List<Substitution> getSubstitutions() {
		return this.substitutions.get();
	}

	public static enum Type {
		STOP_BINDING,
		CONTINUE,
		PUSH_BACK;
	}
}
