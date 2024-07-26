package at.ac.tuwien.kr.alpha.core.programs.transformation;

import at.ac.tuwien.kr.alpha.api.Alpha;
import at.ac.tuwien.kr.alpha.api.AnswerSet;
import at.ac.tuwien.kr.alpha.api.common.fixedinterpretations.PredicateInterpretation;
import at.ac.tuwien.kr.alpha.api.programs.Predicate;
import at.ac.tuwien.kr.alpha.api.programs.NormalProgram;
import at.ac.tuwien.kr.alpha.api.programs.atoms.Atom;
import at.ac.tuwien.kr.alpha.api.programs.atoms.BasicAtom;
import at.ac.tuwien.kr.alpha.api.programs.atoms.ExternalAtom;
import at.ac.tuwien.kr.alpha.api.programs.atoms.ModuleAtom;
import at.ac.tuwien.kr.alpha.api.programs.literals.Literal;
import at.ac.tuwien.kr.alpha.api.programs.literals.ModuleLiteral;
import at.ac.tuwien.kr.alpha.api.programs.modules.Module;
import at.ac.tuwien.kr.alpha.api.programs.rules.NormalRule;
import at.ac.tuwien.kr.alpha.api.programs.rules.Rule;
import at.ac.tuwien.kr.alpha.api.programs.rules.heads.NormalHead;
import at.ac.tuwien.kr.alpha.api.programs.terms.FunctionTerm;
import at.ac.tuwien.kr.alpha.api.programs.terms.Term;
import at.ac.tuwien.kr.alpha.commons.programs.Programs;
import at.ac.tuwien.kr.alpha.commons.programs.atoms.Atoms;
import at.ac.tuwien.kr.alpha.commons.programs.rules.Rules;
import at.ac.tuwien.kr.alpha.commons.programs.terms.Terms;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.collections4.SetUtils;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Program transformation that translates {@link at.ac.tuwien.kr.alpha.api.programs.literals.ModuleLiteral}s into
 * {@link at.ac.tuwien.kr.alpha.api.programs.literals.ExternalLiteral}s by constructing {@link at.ac.tuwien.kr.alpha.api.programs.atoms.ExternalAtom}s
 * which solve the ASP implementation of the module with the given inputs.
 */
public class ModuleLinker extends ProgramTransformation<NormalProgram, NormalProgram> {

	// Note: References to a standard library of modules that are always available for linking should be member variables of a linker.

	private final Alpha moduleRunner;

	public ModuleLinker(Alpha moduleRunner) {
		this.moduleRunner = moduleRunner;
	}


	@Override
	public NormalProgram apply(NormalProgram inputProgram) {
		Map<String, Module> moduleTable = inputProgram.getModules().stream().collect(Collectors.toMap(Module::getName, Function.identity()));
		List<NormalRule> transformedRules = inputProgram.getRules().stream()
				.map(rule -> containsModuleAtom(rule) ? linkModuleAtoms(rule, moduleTable) : rule)
				.collect(Collectors.toList());
		return null;
	}

	private NormalRule linkModuleAtoms(NormalRule rule, Map<String, Module> moduleTable) {
		NormalHead newHead = rule.getHead();
		Set<Literal> newBody = rule.getBody().stream()
				.map(literal -> {
					if (literal instanceof ModuleLiteral) {
						ModuleLiteral moduleLiteral = (ModuleLiteral) literal;
						return translateModuleAtom(moduleLiteral.getAtom(), moduleTable).toLiteral(!moduleLiteral.isNegated());
					} else {
						return literal;
					}
				})
				.collect(Collectors.toSet());
		return Rules.newNormalRule(newHead, newBody);
	}

	private ExternalAtom translateModuleAtom(ModuleAtom atom, Map<String, Module> moduleTable) {
		if (!moduleTable.containsKey(atom.getModuleName())) {
			throw new IllegalArgumentException("Module " + atom.getModuleName() + " not found in module table.");
		}
		Module definition = moduleTable.get(atom.getModuleName());
		// verify inputs
		Predicate inputSpec = definition.getInputSpec();
		if (atom.getInput().size() != inputSpec.getArity()) {
			throw new IllegalArgumentException("Module " + atom.getModuleName() + " expects " + inputSpec.getArity() + " inputs, but " + atom.getInput().size() + " were given.");
		}
		NormalProgram normalizedImplementation = moduleRunner.normalizeProgram(definition.getImplementation());
		// verify outputs
		Set<Predicate> outputSpec = definition.getOutputSpec();
		Set<Predicate> expectedOutputPredicates;
		if (outputSpec.isEmpty()) {
			expectedOutputPredicates = calculateOutputPredicates(normalizedImplementation);
		} else {
			expectedOutputPredicates = outputSpec;
		}
		if (atom.getOutput().size() != expectedOutputPredicates.size()) {
			throw new IllegalArgumentException("Module " + atom.getModuleName() + " expects " + outputSpec.size() + " outputs, but " + atom.getOutput().size() + " were given.");
		}
		// create the actual interpretation
		PredicateInterpretation interpretation = terms -> {
			BasicAtom inputAtom = Atoms.newBasicAtom(inputSpec, terms);
			NormalProgram program = Programs.newNormalProgram(normalizedImplementation.getRules(),
					ListUtils.union(List.of(inputAtom), normalizedImplementation.getFacts()), normalizedImplementation.getInlineDirectives());
			java.util.function.Predicate<Predicate> filter = outputSpec.isEmpty() ? p -> true : outputSpec::contains;
			Stream<AnswerSet> answerSets = moduleRunner.solve(program, filter);
			if (atom.getInstantiationMode().requestedAnswerSets().isPresent()) {
				answerSets = answerSets.limit(atom.getInstantiationMode().requestedAnswerSets().get());
			}
			return answerSets.map(as -> answerSetToTerms(as, expectedOutputPredicates)).collect(Collectors.toSet());
		};
		return Atoms.newExternalAtom(atom.getPredicate(), interpretation, atom.getInput(), atom.getOutput());
	}

	private static boolean containsModuleAtom(NormalRule rule) {
		return rule.getBody().stream().anyMatch(literal -> literal instanceof ModuleLiteral);
	}

	private static Set<Predicate> calculateOutputPredicates(NormalProgram program) {
		return SetUtils.union(program.getFacts().stream().map(Atom::getPredicate).collect(Collectors.toSet()),
				program.getRules().stream()
						.filter(java.util.function.Predicate.not(Rule::isConstraint))
						.map(Rule::getHead).map(NormalHead::getAtom).map(Atom::getPredicate)
						.collect(Collectors.toSet()));
	}

	private static List<Term> answerSetToTerms(AnswerSet answerSet, Set<Predicate> moduleOutputSpec) {
		List<Term> terms = new ArrayList<>();
		for (Predicate predicate : moduleOutputSpec) {
			if (!answerSet.getPredicates().contains(predicate)) {
				terms.add(Terms.EMPTY_LIST);
			} else {
				terms.add(Terms.asListTerm(answerSet.getPredicateInstances(predicate).stream()
						.map(Atoms::toFunctionTerm).collect(Collectors.toList())));
			}
		}
		return terms;
	}


}
