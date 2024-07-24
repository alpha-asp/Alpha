package at.ac.tuwien.kr.alpha.commons.programs.modules;

import at.ac.tuwien.kr.alpha.api.programs.InputProgram;
import at.ac.tuwien.kr.alpha.api.programs.Predicate;
import at.ac.tuwien.kr.alpha.api.programs.modules.Module;

import java.util.Set;

class ModuleImpl implements Module {

	private final String name;
	private final Set<Predicate> inputSpec;
	private final Set<Predicate> outputSpec;
	private final InputProgram implementation;

	ModuleImpl(String name, Set<Predicate> inputSpec, Set<Predicate> outputSpec, InputProgram implementation) {
		this.name = name;
		this.inputSpec = inputSpec;
		this.outputSpec = outputSpec;
		this.implementation = implementation;
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public Set<Predicate> getInputSpec() {
		return this.inputSpec;
	}

	@Override
	public Set<Predicate> getOutputSpec() {
		return this.outputSpec;
	}

	@Override
	public InputProgram getImplementation() {
		return this.implementation;
	}

}
