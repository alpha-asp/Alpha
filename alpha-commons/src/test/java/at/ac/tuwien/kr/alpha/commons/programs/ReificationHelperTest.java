package at.ac.tuwien.kr.alpha.commons.programs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import at.ac.tuwien.kr.alpha.api.programs.atoms.BasicAtom;
import at.ac.tuwien.kr.alpha.api.terms.ArithmeticOperator;
import at.ac.tuwien.kr.alpha.api.terms.ArithmeticTerm;
import at.ac.tuwien.kr.alpha.api.terms.ConstantTerm;
import at.ac.tuwien.kr.alpha.api.terms.FunctionTerm;
import at.ac.tuwien.kr.alpha.api.terms.Term;
import at.ac.tuwien.kr.alpha.api.terms.VariableTerm;
import at.ac.tuwien.kr.alpha.commons.Predicates;
import at.ac.tuwien.kr.alpha.commons.atoms.Atoms;
import at.ac.tuwien.kr.alpha.commons.terms.Terms;
import at.ac.tuwien.kr.alpha.commons.util.IntIdGenerator;

public class ReificationHelperTest {

	private Supplier<ConstantTerm<?>> newIdGenerator() {
		return new Supplier<ConstantTerm<?>>() {

			IntIdGenerator idGen = new IntIdGenerator(0);

			@Override
			public ConstantTerm<?> get() {
				return Terms.newConstant(idGen.getNextId());
			}

		};
	}

	@Test
	public void reifySymbolicConstant() {
		ConstantTerm<String> constant = Terms.newSymbolicConstant("someConstant");
		Supplier<ConstantTerm<?>> idGen = newIdGenerator();
		ConstantTerm<?> reifiedId = idGen.get();
		Set<BasicAtom> reified = new ReificationHelper(idGen).reifyConstantTerm(reifiedId, constant);
		assertTrue(reified.contains(Atoms.newBasicAtom(Predicates.getPredicate("term_type", 2), reifiedId, Terms.newSymbolicConstant("constant"))));
		assertTrue(reified.contains(Atoms.newBasicAtom(Predicates.getPredicate("constantTerm_class", 2), reifiedId, Terms.newConstant("java.lang.String"))));
		assertTrue(reified.contains(Atoms.newBasicAtom(Predicates.getPredicate("constantTerm_symbolic", 2), reifiedId, Terms.newSymbolicConstant("true"))));
		assertTrue(reified.contains(Atoms.newBasicAtom(Predicates.getPredicate("constantTerm_value", 2), reifiedId, Terms.newConstant("someConstant"))));
	}

	@Test
	public void reifyStringConstant() {
		ConstantTerm<String> constant = Terms.newConstant("someString");
		Supplier<ConstantTerm<?>> idGen = newIdGenerator();
		ConstantTerm<?> reifiedId = idGen.get();
		Set<BasicAtom> reified = new ReificationHelper(idGen).reifyConstantTerm(reifiedId, constant);
		assertTrue(reified.contains(Atoms.newBasicAtom(Predicates.getPredicate("term_type", 2), reifiedId, Terms.newSymbolicConstant("constant"))));
		assertTrue(reified.contains(Atoms.newBasicAtom(Predicates.getPredicate("constantTerm_class", 2), reifiedId, Terms.newConstant("java.lang.String"))));
		assertTrue(reified.contains(Atoms.newBasicAtom(Predicates.getPredicate("constantTerm_symbolic", 2), reifiedId, Terms.newSymbolicConstant("false"))));
		assertTrue(reified.contains(Atoms.newBasicAtom(Predicates.getPredicate("constantTerm_value", 2), reifiedId, Terms.newConstant("someString"))));
	}

	@Test
	public void reifyIntegerConstant() {
		ConstantTerm<Integer> constant = Terms.newConstant(666);
		Supplier<ConstantTerm<?>> idGen = newIdGenerator();
		ConstantTerm<?> reifiedId = idGen.get();
		Set<BasicAtom> reified = new ReificationHelper(idGen).reifyConstantTerm(reifiedId, constant);
		assertTrue(reified.contains(Atoms.newBasicAtom(Predicates.getPredicate("term_type", 2), reifiedId, Terms.newSymbolicConstant("constant"))));
		assertTrue(reified.contains(Atoms.newBasicAtom(Predicates.getPredicate("constantTerm_class", 2), reifiedId, Terms.newConstant("java.lang.Integer"))));
		assertTrue(reified.contains(Atoms.newBasicAtom(Predicates.getPredicate("constantTerm_symbolic", 2), reifiedId, Terms.newSymbolicConstant("false"))));
		assertTrue(reified.contains(Atoms.newBasicAtom(Predicates.getPredicate("constantTerm_value", 2), reifiedId, Terms.newConstant(Integer.toString(666)))));
	}

	@Test
	public void reifyVariable() {
		VariableTerm var = Terms.newVariable("SOME_VAR");
		Supplier<ConstantTerm<?>> idGen = newIdGenerator();
		ConstantTerm<?> reifiedId = idGen.get();
		Set<BasicAtom> reified = new ReificationHelper(idGen).reifyVariableTerm(reifiedId, var);
		assertTrue(reified.contains(Atoms.newBasicAtom(Predicates.getPredicate("term_type", 2), reifiedId, Terms.newSymbolicConstant("variable"))));
		assertTrue(reified.contains(Atoms.newBasicAtom(Predicates.getPredicate("variableTerm_symbol", 2), reifiedId, Terms.newConstant("SOME_VAR"))));
	}

	@Test
	public void reifyArithmeticTerm() {
		Term arithmeticTerm = Terms.newArithmeticTerm(Terms.newVariable("VAR"), ArithmeticOperator.PLUS, Terms.newConstant(2));
		Supplier<ConstantTerm<?>> idGen = newIdGenerator();
		ConstantTerm<?> reifiedId = idGen.get();
		Set<BasicAtom> reified = new ReificationHelper(idGen).reifyArithmeticTerm(reifiedId, (ArithmeticTerm) arithmeticTerm);
		assertTrue(reified.contains(Atoms.newBasicAtom(Predicates.getPredicate("term_type", 2), reifiedId, Terms.newSymbolicConstant("arithmetic"))));
		assertTrue(reified.contains(
				Atoms.newBasicAtom(Predicates.getPredicate("arithmeticTerm_operator", 2), reifiedId, Terms.newConstant(ArithmeticOperator.PLUS.toString()))));
		assertTrue(reified.stream()
				.filter((atom) -> atom.getPredicate().equals(Predicates.getPredicate("arithmeticTerm_leftTerm", 2)))
				.findFirst()
				.isPresent());
		assertTrue(reified.stream()
				.filter((atom) -> atom.getPredicate().equals(Predicates.getPredicate("arithmeticTerm_rightTerm", 2)))
				.findFirst()
				.isPresent());
	}

	@Test
	public void reifyFunctionTerm() {
		FunctionTerm funcTerm = Terms.newFunctionTerm("f", Terms.newConstant(1));
		Supplier<ConstantTerm<?>> idGen = newIdGenerator();
		ConstantTerm<?> reifiedId = idGen.get();
		Set<BasicAtom> reified = new ReificationHelper(idGen).reifyFunctionTerm(reifiedId, funcTerm);
		assertTrue(reified.contains(Atoms.newBasicAtom(Predicates.getPredicate("term_type", 2), reifiedId, Terms.newSymbolicConstant("function"))));
		assertTrue(reified.contains(Atoms.newBasicAtom(Predicates.getPredicate("functionTerm_symbol", 2), reifiedId, Terms.newConstant("f"))));
		assertEquals(1, reified.stream()
				.filter((atom) -> atom.getPredicate().equals(Predicates.getPredicate("functionTerm_argumentTerm", 3)))
				.collect(Collectors.toList()).size());
	}

	@Test
	public void reifyTerm() {
		Term constTerm = Terms.newConstant("bla");
		Term varTerm = Terms.newVariable("VAR");
		Term arithmeticTerm = Terms.newArithmeticTerm(Terms.newVariable("VAR"), ArithmeticOperator.PLUS, Terms.newConstant(2));
		Term funcTerm = Terms.newFunctionTerm("f", Terms.newConstant(1));

		Supplier<ConstantTerm<?>> idGen = newIdGenerator();
		ReificationHelper reificationHelper = new ReificationHelper(idGen);

		ConstantTerm<?> constId = idGen.get();
		Set<BasicAtom> reifiedConst = reificationHelper.reifyTerm(constId, constTerm);
		assertTrue(reifiedConst.contains(Atoms.newBasicAtom(Predicates.getPredicate("term_type", 2), constId, Terms.newSymbolicConstant("constant"))));

		ConstantTerm<?> varId = idGen.get();
		Set<BasicAtom> reifiedVar = reificationHelper.reifyTerm(varId, varTerm);
		assertTrue(reifiedVar.contains(Atoms.newBasicAtom(Predicates.getPredicate("term_type", 2), varId, Terms.newSymbolicConstant("variable"))));

		ConstantTerm<?> calcId = idGen.get();
		Set<BasicAtom> reifiedCalc = reificationHelper.reifyTerm(calcId, arithmeticTerm);
		assertTrue(reifiedCalc.contains(Atoms.newBasicAtom(Predicates.getPredicate("term_type", 2), calcId, Terms.newSymbolicConstant("arithmetic"))));

		ConstantTerm<?> funcId = idGen.get();
		Set<BasicAtom> reifiedFunc = reificationHelper.reifyTerm(funcId, funcTerm);
		assertTrue(reifiedFunc.contains(Atoms.newBasicAtom(Predicates.getPredicate("term_type", 2), funcId, Terms.newSymbolicConstant("function"))));
	}

}
