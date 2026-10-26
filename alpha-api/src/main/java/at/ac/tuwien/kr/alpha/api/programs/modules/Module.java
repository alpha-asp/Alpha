package at.ac.tuwien.kr.alpha.api.programs.modules;

import at.ac.tuwien.kr.alpha.api.programs.InputProgram;
import at.ac.tuwien.kr.alpha.api.programs.Predicate;

import java.util.Set;

public interface Module {

	String getName();

	Predicate getInputSpec();

	Set<Predicate> getOutputSpec();

	InputProgram getImplementation();

}
