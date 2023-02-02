package at.ac.tuwien.kr.alpha.commons.programs;

import at.ac.tuwien.kr.alpha.api.programs.ASPCore2Program;
import at.ac.tuwien.kr.alpha.api.programs.InlineDirectives;
import at.ac.tuwien.kr.alpha.api.programs.NormalProgram;
import at.ac.tuwien.kr.alpha.api.programs.atoms.Atom;
import at.ac.tuwien.kr.alpha.api.programs.rules.NormalRule;
import at.ac.tuwien.kr.alpha.api.programs.rules.Rule;
import at.ac.tuwien.kr.alpha.api.programs.rules.heads.Head;
import at.ac.tuwien.kr.alpha.commons.programs.rules.Rules;
import at.ac.tuwien.kr.alpha.commons.programs.rules.WeakConstraint;

import java.util.ArrayList;
import java.util.List;

public final class Programs {
	
	private Programs() {
		throw new AssertionError("Cannot instantiate utility class!");
	}

	public static ASPCore2Program emptyProgram() {
		return ASPCore2ProgramImpl.EMPTY;
	}

	public static ASPCore2Program newASPCore2Program(List<Rule<Head>> rules, List<Atom> facts, InlineDirectives inlineDirectives, boolean containsWeakConstraints) {
		return new ASPCore2ProgramImpl(rules, facts, inlineDirectives, containsWeakConstraints);
	}

	public static ASPCore2ProgramBuilder builder() {
		return new ASPCore2ProgramBuilder();
	}

	public static ASPCore2ProgramBuilder builder(ASPCore2Program program) {
		return new ASPCore2ProgramBuilder(program);
	}

	public static NormalProgram newNormalProgram(List<NormalRule> rules, List<Atom> facts, InlineDirectives inlineDirectives, boolean containsWeakConstraints) {
		return new NormalProgramImpl(rules, facts, inlineDirectives, containsWeakConstraints);
	}

	public static NormalProgram toNormalProgram(ASPCore2Program inputProgram) {
		List<NormalRule> normalRules = new ArrayList<>();
		boolean containsWeakConstraints = false;
		for (Rule<Head> r : inputProgram.getRules()) {
			normalRules.add(Rules.toNormalRule(r));
			containsWeakConstraints |= r instanceof WeakConstraint;
		}
		return new NormalProgramImpl(normalRules, inputProgram.getFacts(), inputProgram.getInlineDirectives(), containsWeakConstraints);
	}

	public static InlineDirectives newInlineDirectives() {
		return new InlineDirectivesImpl();
	}

	/**
	 * Builder for more complex program construction scenarios, ensuring that an {@link AspCore2ProgramImpl} is immutable
	 */
	public static class ASPCore2ProgramBuilder {

		private List<Rule<Head>> rules = new ArrayList<>();
		private List<Atom> facts = new ArrayList<>();
		private InlineDirectives inlineDirectives = new InlineDirectivesImpl();
		private boolean containsWeakConstraints;

		public ASPCore2ProgramBuilder(ASPCore2Program prog) {
			this.addRules(prog.getRules());
			this.addFacts(prog.getFacts());
			this.addInlineDirectives(prog.getInlineDirectives());
			this.containsWeakConstraints = prog.containsWeakConstraints();
		}

		public ASPCore2ProgramBuilder() {

		}

		public ASPCore2ProgramBuilder addRules(List<Rule<Head>> rules) {
			this.rules.addAll(rules);
			return this;
		}

		public ASPCore2ProgramBuilder addRule(Rule<Head> r) {
			this.rules.add(r);
			return this;
		}

		public ASPCore2ProgramBuilder addFacts(List<Atom> facts) {
			this.facts.addAll(facts);
			return this;
		}

		public ASPCore2ProgramBuilder addFact(Atom fact) {
			this.facts.add(fact);
			return this;
		}

		public ASPCore2ProgramBuilder addInlineDirectives(InlineDirectives inlineDirectives) {
			this.inlineDirectives.accumulate(inlineDirectives);
			return this;
		}

		public ASPCore2ProgramBuilder accumulate(ASPCore2Program prog) {
			return this.addRules(prog.getRules()).addFacts(prog.getFacts()).addInlineDirectives(prog.getInlineDirectives());
		}

		public ASPCore2Program build() {
			return Programs.newASPCore2Program(this.rules, this.facts, this.inlineDirectives, containsWeakConstraints);
		}

	}

}
