package at.ac.tuwien.kr.alpha.commons.programs.modules;

import at.ac.tuwien.kr.alpha.api.programs.InputProgram;
import at.ac.tuwien.kr.alpha.api.programs.Predicate;
import at.ac.tuwien.kr.alpha.api.programs.modules.Module;

import java.util.Set;

public final class Modules {

	private Modules() {
		throw new AssertionError("Cannot instantiate utility class!");
	}

	public static Module newModule(final String name, final Predicate inputSpec, final Set<Predicate> outputSpec, final InputProgram implementation) {
		return new ModuleImpl(name, inputSpec, outputSpec, implementation);
	}

}
