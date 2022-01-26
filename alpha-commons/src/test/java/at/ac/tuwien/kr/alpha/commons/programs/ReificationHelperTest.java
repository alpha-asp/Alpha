package at.ac.tuwien.kr.alpha.commons.programs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import at.ac.tuwien.kr.alpha.api.programs.atoms.AggregateAtom;
import at.ac.tuwien.kr.alpha.api.programs.atoms.AggregateAtom.AggregateElement;
import at.ac.tuwien.kr.alpha.api.programs.atoms.AggregateAtom.AggregateFunctionSymbol;
import at.ac.tuwien.kr.alpha.api.programs.atoms.Atom;
import at.ac.tuwien.kr.alpha.api.programs.atoms.BasicAtom;
import at.ac.tuwien.kr.alpha.api.programs.atoms.ComparisonAtom;
import at.ac.tuwien.kr.alpha.api.programs.atoms.ExternalAtom;
import at.ac.tuwien.kr.alpha.api.programs.literals.Literal;
import at.ac.tuwien.kr.alpha.api.terms.ArithmeticOperator;
import at.ac.tuwien.kr.alpha.api.terms.ArithmeticTerm;
import at.ac.tuwien.kr.alpha.api.terms.ConstantTerm;
import at.ac.tuwien.kr.alpha.api.terms.FunctionTerm;
import at.ac.tuwien.kr.alpha.api.terms.Term;
import at.ac.tuwien.kr.alpha.api.terms.VariableTerm;
import at.ac.tuwien.kr.alpha.commons.Predicates;
import at.ac.tuwien.kr.alpha.commons.atoms.Atoms;
import at.ac.tuwien.kr.alpha.commons.comparisons.ComparisonOperators;
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
		assertTrue(reified.contains(Atoms.newBasicAtom(Predicates.getPredicate("constantTerm_type", 2), reifiedId, Terms.newConstant("symbol"))));
		assertTrue(reified.contains(Atoms.newBasicAtom(Predicates.getPredicate("constantTerm_value", 2), reifiedId, Terms.newConstant("someConstant"))));
	}

	@Test
	public void reifyStringConstant() {
		ConstantTerm<String> constant = Terms.newConstant("someString");
		Supplier<ConstantTerm<?>> idGen = newIdGenerator();
		ConstantTerm<?> reifiedId = idGen.get();
		Set<BasicAtom> reified = new ReificationHelper(idGen).reifyConstantTerm(reifiedId, constant);
		assertTrue(reified.contains(Atoms.newBasicAtom(Predicates.getPredicate("term_type", 2), reifiedId, Terms.newSymbolicConstant("constant"))));
		assertTrue(reified.contains(Atoms.newBasicAtom(Predicates.getPredicate("constantTerm_type", 2), reifiedId, Terms.newConstant("string"))));
		assertTrue(reified.contains(Atoms.newBasicAtom(Predicates.getPredicate("constantTerm_value", 2), reifiedId, Terms.newConstant("someString"))));
	}

	@Test
	public void reifyStringWithQuotes() {
		ConstantTerm<String> constant = Terms.newConstant("someStringWith\"Quotes\"");
		Supplier<ConstantTerm<?>> idGen = newIdGenerator();
		ConstantTerm<?> reifiedId = idGen.get();
		Set<BasicAtom> reified = new ReificationHelper(idGen).reifyConstantTerm(reifiedId, constant);
		assertTrue(reified.contains(Atoms.newBasicAtom(Predicates.getPredicate("term_type", 2), reifiedId, Terms.newSymbolicConstant("constant"))));
		assertTrue(reified.contains(Atoms.newBasicAtom(Predicates.getPredicate("constantTerm_type", 2), reifiedId, Terms.newConstant("string"))));
		assertTrue(reified.contains(Atoms.newBasicAtom(Predicates.getPredicate("constantTerm_value", 2), reifiedId, Terms.newConstant("someStringWith\\\"Quotes\\\""))));

	}

	@Test
	public void reifyIntegerConstant() {
		ConstantTerm<Integer> constant = Terms.newConstant(666);
		Supplier<ConstantTerm<?>> idGen = newIdGenerator();
		ConstantTerm<?> reifiedId = idGen.get();
		Set<BasicAtom> reified = new ReificationHelper(idGen).reifyConstantTerm(reifiedId, constant);
		assertTrue(reified.contains(Atoms.newBasicAtom(Predicates.getPredicate("term_type", 2), reifiedId, Terms.newSymbolicConstant("constant"))));
		assertTrue(reified.contains(Atoms.newBasicAtom(Predicates.getPredicate("constantTerm_type", 2), reifiedId, Terms.newConstant("integer"))));
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

	@Test
	public void reifyBasicAtom() {
		BasicAtom atom = Atoms.newBasicAtom(Predicates.getPredicate("p", 2), Terms.newVariable("X"), Terms.newVariable("Y"));
		Supplier<ConstantTerm<?>> idGen = newIdGenerator();
		ConstantTerm<?> reifiedId = idGen.get();
		Set<BasicAtom> reified = new ReificationHelper(idGen).reifyBasicAtom(reifiedId, atom);
		assertTrue(reified.contains(Atoms.newBasicAtom(Predicates.getPredicate("atom_type", 2), reifiedId, Terms.newSymbolicConstant("basic"))));
		assertEquals(2,
				reified.stream()
						.filter(
								(a) -> a.getPredicate().equals(Predicates.getPredicate("basicAtom_term", 3)))
						.collect(Collectors.toList())
						.size());
	}

	@Test
	public void reifyComparisonAtom() {
		ComparisonAtom atom = Atoms.newComparisonAtom(Terms.newConstant(5), Terms.newVariable("X"), ComparisonOperators.LE);
		Supplier<ConstantTerm<?>> idGen = newIdGenerator();
		ConstantTerm<?> reifiedId = idGen.get();
		Set<BasicAtom> reified = new ReificationHelper(idGen).reifyComparisonAtom(reifiedId, atom);
		assertTrue(reified.contains(Atoms.newBasicAtom(Predicates.getPredicate("atom_type", 2), reifiedId, Terms.newSymbolicConstant("comparison"))));
		assertEquals(1,
				reified.stream()
						.filter(
								(a) -> a.getPredicate().equals(Predicates.getPredicate("comparisonAtom_leftTerm", 2)))
						.collect(Collectors.toList())
						.size());
		assertEquals(1,
				reified.stream()
						.filter(
								(a) -> a.getPredicate().equals(Predicates.getPredicate("comparisonAtom_rightTerm", 2)))
						.collect(Collectors.toList())
						.size());
		assertTrue(reified.contains(Atoms.newBasicAtom(Predicates.getPredicate("comparisonAtom_operator", 2), reifiedId, Terms.newSymbolicConstant("le"))));
	}

	@Test
	public void reifyAggregateElement() {
		List<Term> elementTerms = new ArrayList<>();
		elementTerms.add(Terms.newVariable("X"));
		List<Literal> elementLiterals = new ArrayList<>();
		elementLiterals.add(Atoms.newBasicAtom(Predicates.getPredicate("dom", 1), Terms.newVariable("X")).toLiteral());
		AggregateElement element = Atoms.newAggregateElement(elementTerms, elementLiterals);
		Supplier<ConstantTerm<?>> idGen = newIdGenerator();
		ConstantTerm<?> reifiedId = idGen.get();
		Set<BasicAtom> reified = new ReificationHelper(idGen).reifyAggregateElement(reifiedId, element);
		assertEquals(1,
				reified.stream()
						.filter(
								(a) -> a.getPredicate().equals(Predicates.getPredicate("aggregateElement_term", 3)))
						.collect(Collectors.toList())
						.size());
		assertEquals(1,
				reified.stream()
						.filter(
								(a) -> a.getPredicate().equals(Predicates.getPredicate("aggregateElement_literal", 2)))
						.collect(Collectors.toList())
						.size());
	}

	@Test
	public void reifyAggregateAtom() {
		List<Term> element1Terms = new ArrayList<>();
		element1Terms.add(Terms.newVariable("X"));
		List<Literal> element1Literals = new ArrayList<>();
		element1Literals.add(Atoms.newBasicAtom(Predicates.getPredicate("dom1", 1), Terms.newVariable("X")).toLiteral());
		AggregateElement element1 = Atoms.newAggregateElement(element1Terms, element1Literals);

		List<Term> element2Terms = new ArrayList<>();
		element1Terms.add(Terms.newVariable("Y"));
		List<Literal> element2Literals = new ArrayList<>();
		element1Literals.add(Atoms.newBasicAtom(Predicates.getPredicate("dom2", 1), Terms.newVariable("Y")).toLiteral());
		AggregateElement element2 = Atoms.newAggregateElement(element2Terms, element2Literals);

		List<AggregateElement> elements = new ArrayList<>();
		elements.add(element1);
		elements.add(element2);
		AggregateAtom atom = Atoms.newAggregateAtom(ComparisonOperators.EQ, Terms.newVariable("X"), AggregateFunctionSymbol.COUNT, elements);

		Supplier<ConstantTerm<?>> idGen = newIdGenerator();
		ConstantTerm<?> reifiedId = idGen.get();
		Set<BasicAtom> reified = new ReificationHelper(idGen).reifyAggregateAtom(reifiedId, atom);

		assertTrue(reified.contains(Atoms.newBasicAtom(Predicates.getPredicate("atom_type", 2), reifiedId, Terms.newSymbolicConstant("aggregate"))));
		assertTrue(reified
				.contains(Atoms.newBasicAtom(Predicates.getPredicate("aggregateAtom_aggregateFunction", 2), reifiedId, Terms.newSymbolicConstant("count"))));
		assertEquals(2,
				reified.stream()
						.filter(
								(a) -> a.getPredicate().equals(Predicates.getPredicate("aggregateAtom_aggregateElement", 2)))
						.collect(Collectors.toList())
						.size());
	}

	@Test
	public void reifyExternalAtom() {
		List<Term> extInput = new ArrayList<>();
		List<Term> extOutput = new ArrayList<>();
		extInput.add(Terms.newVariable("I"));
		extOutput.add(Terms.newVariable("O"));
		ExternalAtom atom = Atoms.newExternalAtom(Predicates.getPredicate("ext", 2), (trms) -> Collections.emptySet(), extInput, extOutput);

		Supplier<ConstantTerm<?>> idGen = newIdGenerator();
		ConstantTerm<?> reifiedId = idGen.get();
		Set<BasicAtom> reified = new ReificationHelper(idGen).reifyExternalAtom(reifiedId, atom);

		assertTrue(reified.contains(Atoms.newBasicAtom(Predicates.getPredicate("atom_type", 2), reifiedId, Terms.newSymbolicConstant("external"))));
		assertEquals(1,
				reified.stream()
						.filter(
								(a) -> a.getPredicate().equals(Predicates.getPredicate("externalAtom_name", 2)))
						.collect(Collectors.toList())
						.size());
		assertEquals(1,
				reified.stream()
						.filter(
								(a) -> a.getPredicate().equals(Predicates.getPredicate("externalAtom_inputTerm", 3)))
						.collect(Collectors.toList())
						.size());
		assertEquals(1,
				reified.stream()
						.filter(
								(a) -> a.getPredicate().equals(Predicates.getPredicate("externalAtom_outputTerm", 3)))
						.collect(Collectors.toList())
						.size());
	}

	@Test
	public void reifyAtom() {
		Atom basicAtom = Atoms.newBasicAtom(Predicates.getPredicate("p", 2), Terms.newVariable("X"), Terms.newVariable("Y"));
		
		Atom cmpAtom = Atoms.newComparisonAtom(Terms.newConstant(5), Terms.newVariable("X"), ComparisonOperators.LE);
		
		List<Term> element1Terms = new ArrayList<>();
		element1Terms.add(Terms.newVariable("X"));
		List<Literal> element1Literals = new ArrayList<>();
		element1Literals.add(Atoms.newBasicAtom(Predicates.getPredicate("dom1", 1), Terms.newVariable("X")).toLiteral());
		AggregateElement element1 = Atoms.newAggregateElement(element1Terms, element1Literals);
		List<Term> element2Terms = new ArrayList<>();
		element1Terms.add(Terms.newVariable("Y"));
		List<Literal> element2Literals = new ArrayList<>();
		element1Literals.add(Atoms.newBasicAtom(Predicates.getPredicate("dom2", 1), Terms.newVariable("Y")).toLiteral());
		AggregateElement element2 = Atoms.newAggregateElement(element2Terms, element2Literals);
		List<AggregateElement> elements = new ArrayList<>();
		elements.add(element1);
		elements.add(element2);
		Atom aggAtom = Atoms.newAggregateAtom(ComparisonOperators.EQ, Terms.newVariable("X"), AggregateFunctionSymbol.COUNT, elements);

		List<Term> extInput = new ArrayList<>();
		List<Term> extOutput = new ArrayList<>();
		extInput.add(Terms.newVariable("I"));
		extOutput.add(Terms.newVariable("O"));
		ExternalAtom extAtom = Atoms.newExternalAtom(Predicates.getPredicate("ext", 2), (trms) -> Collections.emptySet(), extInput, extOutput);

		Supplier<ConstantTerm<?>> idGen = newIdGenerator();
		
		ConstantTerm<?> reifiedBasicAtomId = idGen.get();
		Set<BasicAtom> reifiedBasicAtom = new ReificationHelper(idGen).reifyAtom(reifiedBasicAtomId, basicAtom);
		assertTrue(reifiedBasicAtom.contains(Atoms.newBasicAtom(Predicates.getPredicate("atom_type", 2), reifiedBasicAtomId, Terms.newSymbolicConstant("basic"))));

		ConstantTerm<?> reifiedCmpAtomId = idGen.get();
		Set<BasicAtom> reifiedCmpAtom = new ReificationHelper(idGen).reifyAtom(reifiedCmpAtomId, cmpAtom);
		assertTrue(reifiedCmpAtom.contains(Atoms.newBasicAtom(Predicates.getPredicate("atom_type", 2), reifiedCmpAtomId, Terms.newSymbolicConstant("comparison"))));

		ConstantTerm<?> reifiedAggAtomId = idGen.get();
		Set<BasicAtom> reifiedAggAtom = new ReificationHelper(idGen).reifyAtom(reifiedAggAtomId, aggAtom);
		assertTrue(reifiedAggAtom.contains(Atoms.newBasicAtom(Predicates.getPredicate("atom_type", 2), reifiedAggAtomId, Terms.newSymbolicConstant("aggregate"))));

		ConstantTerm<?> reifiedExtAtomId = idGen.get();
		Set<BasicAtom> reifiedExtAtom = new ReificationHelper(idGen).reifyAtom(reifiedExtAtomId, extAtom);
		assertTrue(reifiedExtAtom.contains(Atoms.newBasicAtom(Predicates.getPredicate("atom_type", 2), reifiedExtAtomId, Terms.newSymbolicConstant("external"))));
	}

}
