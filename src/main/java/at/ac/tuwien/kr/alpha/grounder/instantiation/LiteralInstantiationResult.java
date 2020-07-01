package at.ac.tuwien.kr.alpha.grounder.instantiation;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.ImmutablePair;

import at.ac.tuwien.kr.alpha.grounder.Substitution;

public class LiteralInstantiationResult {

	private final Type type;
	// use optional to ensure empty value is caught as early as possible
	private final Optional<List<ImmutablePair<Substitution, AssignmentStatus>>> substitutions;

	private LiteralInstantiationResult(Type type, Optional<List<ImmutablePair<Substitution, AssignmentStatus>>> substitutions) {
		this.type = type;
		this.substitutions = substitutions;
	}

	public static LiteralInstantiationResult continueBinding(List<ImmutablePair<Substitution, AssignmentStatus>> substitutions) {
		return new LiteralInstantiationResult(Type.CONTINUE, Optional.of(substitutions));
	}

	//@formatter:off
	public static LiteralInstantiationResult continueBindingWithTrueSubstitutions(List<Substitution> substitutions) {
		List<ImmutablePair<Substitution, AssignmentStatus>> substitutionsWithAssignment = substitutions.stream()
				.map((substitution) -> new ImmutablePair<>(substitution, AssignmentStatus.TRUE))
				.collect(Collectors.toList());
		return new LiteralInstantiationResult(Type.CONTINUE, Optional.of(substitutionsWithAssignment));
	}
	//@formatter:on

	public static LiteralInstantiationResult continueBinding(Substitution substitution, AssignmentStatus assignmentStatus) {
		return new LiteralInstantiationResult(Type.CONTINUE, Optional.of(Collections.singletonList(new ImmutablePair<>(substitution, assignmentStatus))));
	}

	public static LiteralInstantiationResult stopBinding() {
		return new LiteralInstantiationResult(Type.STOP_BINDING, Optional.empty());
	}

	public static LiteralInstantiationResult pushBack() {
		return new LiteralInstantiationResult(Type.PUSH_BACK, Optional.empty());
	}

	public static LiteralInstantiationResult maybePushBack() {
		return new LiteralInstantiationResult(Type.MAYBE_PUSH_BACK, Optional.empty());
	}

	public Type getType() {
		return this.type;
	}

	// unwrap the optional, if empty, exception will be thrown here
	public List<ImmutablePair<Substitution, AssignmentStatus>> getSubstitutions() {
		return this.substitutions.get();
	}

	public static enum Type {
		STOP_BINDING,
		CONTINUE,
		MAYBE_PUSH_BACK,
		PUSH_BACK;
	}
}
