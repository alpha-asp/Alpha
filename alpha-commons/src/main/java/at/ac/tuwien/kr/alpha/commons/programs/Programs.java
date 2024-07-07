package at.ac.tuwien.kr.alpha.commons.programs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import at.ac.tuwien.kr.alpha.api.programs.InputProgram;
import at.ac.tuwien.kr.alpha.api.programs.InlineDirectives;
import at.ac.tuwien.kr.alpha.api.programs.NormalProgram;
import at.ac.tuwien.kr.alpha.api.programs.atoms.Atom;
import at.ac.tuwien.kr.alpha.api.programs.rules.NormalRule;
import at.ac.tuwien.kr.alpha.api.programs.rules.Rule;
import at.ac.tuwien.kr.alpha.api.programs.rules.heads.Head;
import at.ac.tuwien.kr.alpha.api.programs.tests.TestCase;
import at.ac.tuwien.kr.alpha.commons.programs.rules.Rules;

public final class Programs {
	
	private Programs() {
		throw new AssertionError("Cannot instantiate utility class!");
	}

	public static InputProgram emptyProgram() {
		return InputProgramImpl.EMPTY;
	}

	// TODO rename method
	public static InputProgram newInputProgram(List<Rule<Head>> rules, List<Atom> facts, InlineDirectives inlineDirectives, List<TestCase> testCases) {
		return new InputProgramImpl(rules, facts, inlineDirectives, testCases);
	}

	// TODO rename method
	public static InputProgram newInputProgram(List<Rule<Head>> rules, List<Atom> facts, InlineDirectives inlineDirectives) {
		return new InputProgramImpl(rules, facts, inlineDirectives, Collections.emptyList());
	}

	public static InputProgramBuilder builder() {
		return new InputProgramBuilder();
	}

	public static InputProgramBuilder builder(InputProgram program) {
		return new InputProgramBuilder(program);
	}

	public static NormalProgram newNormalProgram(List<NormalRule> rules, List<Atom> facts, InlineDirectives inlineDirectives) {
		return new NormalProgramImpl(rules, facts, inlineDirectives);
	}

	public static NormalProgram toNormalProgram(InputProgram inputProgram) {
		List<NormalRule> normalRules = new ArrayList<>();
		for (Rule<Head> r : inputProgram.getRules()) {
			normalRules.add(Rules.toNormalRule(r));
		}
		return new NormalProgramImpl(normalRules, inputProgram.getFacts(), inputProgram.getInlineDirectives());
	}

	public static InlineDirectives newInlineDirectives() {
		return new InlineDirectivesImpl();
	}

	/**
	 * Builder for more complex program construction scenarios, ensuring that an {@link InputProgramImpl} is immutable
	 */
	// TODO maybe rename
	public static class InputProgramBuilder {

		private List<Rule<Head>> rules = new ArrayList<>();
		private List<Atom> facts = new ArrayList<>();
		private InlineDirectives inlineDirectives = new InlineDirectivesImpl();

		private List<TestCase> testCases = new ArrayList<>();

		public InputProgramBuilder(InputProgram prog) {
			this.addRules(prog.getRules());
			this.addFacts(prog.getFacts());
			this.addInlineDirectives(prog.getInlineDirectives());
			this.addTestCases(prog.getTestCases());
		}

		public InputProgramBuilder() {

		}

		public InputProgramBuilder addRules(List<Rule<Head>> rules) {
			this.rules.addAll(rules);
			return this;
		}

		public InputProgramBuilder addRule(Rule<Head> r) {
			this.rules.add(r);
			return this;
		}

		public InputProgramBuilder addFacts(List<Atom> facts) {
			this.facts.addAll(facts);
			return this;
		}

		public InputProgramBuilder addFact(Atom fact) {
			this.facts.add(fact);
			return this;
		}

		public InputProgramBuilder addInlineDirectives(InlineDirectives inlineDirectives) {
			this.inlineDirectives.accumulate(inlineDirectives);
			return this;
		}

		public InputProgramBuilder addTestCase(TestCase testCase) {
			this.testCases.add(testCase);
			return this;
		}

		public InputProgramBuilder addTestCases(List<TestCase> testCases) {
			this.testCases.addAll(testCases);
			return this;
		}

		public InputProgramBuilder accumulate(InputProgram prog) {
			return this.addRules(prog.getRules()).addFacts(prog.getFacts()).addInlineDirectives(prog.getInlineDirectives()).addTestCases(prog.getTestCases());
		}

		public InputProgram build() {
			return Programs.newInputProgram(this.rules, this.facts, this.inlineDirectives, this.testCases);
		}

	}

}
