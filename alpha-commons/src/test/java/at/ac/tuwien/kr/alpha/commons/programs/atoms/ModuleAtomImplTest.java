package at.ac.tuwien.kr.alpha.commons.programs.atoms;

import java.util.List;

import at.ac.tuwien.kr.alpha.api.programs.atoms.ModuleAtom;
import at.ac.tuwien.kr.alpha.commons.programs.terms.Terms;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ModuleAtomImplTest {

	@Test
	public void withTerms() {
		ModuleAtom moduleAtom = new ModuleAtomImpl("someModule",
				List.of(Terms.newVariable("X"), Terms.newVariable("Y")),
				List.of(Terms.newVariable("Z")), ModuleAtom.ModuleInstantiationMode.ALL);
		ModuleAtom newModuleAtom = moduleAtom.withTerms(List.of(Terms.newConstant(1), Terms.newConstant(2), Terms.newConstant(3)));
		// Check correct construction of original atom (also check that withTerms didn't modify the original atom)
		assertEquals(moduleAtom.getInput().size(), 2);
		assertEquals(moduleAtom.getOutput().size(), 1);
		assertEquals(moduleAtom.getModuleName(), "someModule");
		assertEquals(moduleAtom.getInstantiationMode(), ModuleAtom.ModuleInstantiationMode.ALL);
		// Check terms of new atom
		assertEquals(newModuleAtom.getInput().size(), 2);
		assertEquals(newModuleAtom.getOutput().size(), 1);
		assertEquals(newModuleAtom.getModuleName(), "someModule");
		assertEquals(newModuleAtom.getInstantiationMode(), ModuleAtom.ModuleInstantiationMode.ALL);
		assertEquals(List.of(Terms.newConstant(1), Terms.newConstant(2)), newModuleAtom.getInput());
		assertEquals(List.of(Terms.newConstant(3)), newModuleAtom.getOutput());
	}

	@Test
	public void withTermsNewTermsTooLong() {
		ModuleAtom moduleAtom = new ModuleAtomImpl("someModule",
				List.of(Terms.newVariable("X"), Terms.newVariable("Y")),
				List.of(Terms.newVariable("Z")), ModuleAtom.ModuleInstantiationMode.ALL);
		assertThrows(IllegalArgumentException.class,
				() -> moduleAtom.withTerms(
						List.of(Terms.newConstant(1), Terms.newConstant(2),
								Terms.newConstant(3), Terms.newConstant(4))));
	}

	@Test
	public void withTermsNewTermsTooShort() {
		ModuleAtom moduleAtom = new ModuleAtomImpl("someModule",
				List.of(Terms.newVariable("X"), Terms.newVariable("Y")),
				List.of(Terms.newVariable("Z")), ModuleAtom.ModuleInstantiationMode.ALL);
		assertThrows(IllegalArgumentException.class,
				() -> moduleAtom.withTerms(List.of(Terms.newConstant(1))));
	}

	@Test
	public void moduleAtomsEqual() {
		ModuleAtom m1 = new ModuleAtomImpl("someModule",
				List.of(Terms.newVariable("X"), Terms.newVariable("Y")),
				List.of(Terms.newVariable("Z")), ModuleAtom.ModuleInstantiationMode.ALL);
		ModuleAtom m2 = new ModuleAtomImpl("someModule",
				List.of(Terms.newVariable("X"), Terms.newVariable("Y")),
				List.of(Terms.newVariable("Z")), ModuleAtom.ModuleInstantiationMode.ALL);
		assertEquals(m1, m2);
		assertEquals(m1.hashCode(), m2.hashCode());

		ModuleAtom m3 = new ModuleAtomImpl("someModule",
				List.of(Terms.newVariable("X"), Terms.newVariable("Y")),
				List.of(Terms.newVariable("Z")), ModuleAtom.ModuleInstantiationMode.forNumAnswerSets(3));
		ModuleAtom m4 = new ModuleAtomImpl("someModule",
				List.of(Terms.newVariable("X"), Terms.newVariable("Y")),
				List.of(Terms.newVariable("Z")), ModuleAtom.ModuleInstantiationMode.forNumAnswerSets(3));
		assertNotEquals(m1, m3);
		assertEquals(m3, m4);
	}

}
