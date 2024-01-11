package at.ac.tuwien.kr.alpha.core.rules;

import java.util.LinkedHashSet;
import java.util.Set;

import at.ac.tuwien.kr.alpha.api.programs.literals.Literal;
import at.ac.tuwien.kr.alpha.api.rules.heads.NormalHead;

public final class CompiledRules {

	private CompiledRules() {
		throw new AssertionError("Cannot instantiate utility class!");
	}

	public static CompiledRule newCompiledRule(NormalHead head, Set<Literal> body) {
		return new CompiledRuleImpl(head, body);
	}

	public static CompiledRule newCompiledRule(NormalHead head, Literal... body) {
		Set<Literal> bodySet = new LinkedHashSet<>();
		for (Literal lit : body) {
			bodySet.add(lit);
		}
		return CompiledRules.newCompiledRule(head, bodySet);
	}

}
