/**
 * Copyright (c) 2020, the Alpha Team.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1) Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 *
 * 2) Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
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
