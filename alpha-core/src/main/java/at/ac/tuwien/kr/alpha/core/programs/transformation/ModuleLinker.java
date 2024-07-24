package at.ac.tuwien.kr.alpha.core.programs.transformation;

import at.ac.tuwien.kr.alpha.api.Alpha;
import at.ac.tuwien.kr.alpha.api.programs.NormalProgram;
import at.ac.tuwien.kr.alpha.api.programs.atoms.ExternalAtom;
import at.ac.tuwien.kr.alpha.api.programs.atoms.ModuleAtom;
import at.ac.tuwien.kr.alpha.api.programs.literals.Literal;
import at.ac.tuwien.kr.alpha.api.programs.literals.ModuleLiteral;
import at.ac.tuwien.kr.alpha.api.programs.modules.Module;
import at.ac.tuwien.kr.alpha.api.programs.rules.NormalRule;
import at.ac.tuwien.kr.alpha.api.programs.rules.heads.NormalHead;
import at.ac.tuwien.kr.alpha.commons.programs.rules.Rules;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

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

	private ExternalAtom translateModuleAtom(ModuleAtom moduleAtom, Map<String, Module> moduleTable) {
		if (!moduleTable.containsKey(moduleAtom.getModuleName())) {
			throw new IllegalArgumentException("Module " + moduleAtom.getModuleName() + " not found in module table.");
		}
		Module implementationModule = moduleTable.get(moduleAtom.getModuleName());
		//implementationModule.
		return null;
	}

	private static boolean containsModuleAtom(NormalRule rule) {
		return rule.getBody().stream().anyMatch(literal -> literal instanceof ModuleLiteral);
	}

}
