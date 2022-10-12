package at.ac.tuwien.kr.alpha.commons.programs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import at.ac.tuwien.kr.alpha.api.programs.ASPCore2Program;
import at.ac.tuwien.kr.alpha.api.programs.InlineDirectives;
import at.ac.tuwien.kr.alpha.api.programs.atoms.AggregateAtom;
import at.ac.tuwien.kr.alpha.api.programs.atoms.AggregateAtom.AggregateElement;
import at.ac.tuwien.kr.alpha.api.programs.atoms.AggregateAtom.AggregateFunctionSymbol;
import at.ac.tuwien.kr.alpha.api.programs.atoms.Atom;
import at.ac.tuwien.kr.alpha.api.programs.atoms.BasicAtom;
import at.ac.tuwien.kr.alpha.api.programs.atoms.ComparisonAtom;
import at.ac.tuwien.kr.alpha.api.programs.atoms.ExternalAtom;
import at.ac.tuwien.kr.alpha.api.programs.literals.Literal;
import at.ac.tuwien.kr.alpha.api.programs.terms.ArithmeticOperator;
import at.ac.tuwien.kr.alpha.api.programs.terms.ArithmeticTerm;
import at.ac.tuwien.kr.alpha.api.programs.terms.ConstantTerm;
import at.ac.tuwien.kr.alpha.api.programs.terms.FunctionTerm;
import at.ac.tuwien.kr.alpha.api.programs.terms.Term;
import at.ac.tuwien.kr.alpha.api.programs.terms.VariableTerm;
import at.ac.tuwien.kr.alpha.commons.Predicates;
import at.ac.tuwien.kr.alpha.commons.comparisons.ComparisonOperators;
import at.ac.tuwien.kr.alpha.commons.programs.atoms.Atoms;
import at.ac.tuwien.kr.alpha.commons.programs.rules.Rules;
import at.ac.tuwien.kr.alpha.commons.programs.rules.heads.Heads;
import at.ac.tuwien.kr.alpha.commons.programs.terms.Terms;
import at.ac.tuwien.kr.alpha.commons.util.IdGenerator;
import at.ac.tuwien.kr.alpha.commons.util.IntIdGenerator;

public class ReifierTest {

	private IdGenerator<ConstantTerm<?>> newIdGenerator() {
		final IntIdGenerator idGen = new IntIdGenerator(0);
		return () -> Terms.newConstant(idGen.getNextId());
	}

	@Test
	public void reifySymbolicConstant() {
		ConstantTerm<String> constant = Terms.newSymbolicConstant("someConstant");
		IdGenerator<ConstantTerm<?>> idGen = newIdGenerator();
		ConstantTerm<?> reifiedId = idGen.getNextId();
		Set<BasicAtom> reified = new Reifier(idGen).reifyConstantTerm(reifiedId, constant);
		assertTrue(reified.contains(Atoms.newBasicAtom(Predicates.getPredicate("term_type", 2), reifiedId, Terms.newSymbolicConstant("constant"))));
		assertTrue(reified.contains(Atoms.newBasicAtom(Predicates.getPredicate("constantTerm_type", 2), reifiedId, Terms.newConstant("symbol"))));
		assertTrue(reified.contains(Atoms.newBasicAtom(Predicates.getPredicate("constantTerm_value", 2), reifiedId, Terms.newConstant("someConstant"))));
	}

	@Test
	public void reifyStringConstant() {
		ConstantTerm<String> constant = Terms.newConstant("someString");
		IdGenerator<ConstantTerm<?>> idGen = newIdGenerator();
		ConstantTerm<?> reifiedId = idGen.getNextId();
		Set<BasicAtom> reified = new Reifier(idGen).reifyConstantTerm(reifiedId, constant);
		assertTrue(reified.contains(Atoms.newBasicAtom(Predicates.getPredicate("term_type", 2), reifiedId, Terms.newSymbolicConstant("constant"))));
		assertTrue(reified.contains(Atoms.newBasicAtom(Predicates.getPredicate("constantTerm_type", 2), reifiedId, Terms.newConstant("string"))));
		assertTrue(reified.contains(Atoms.newBasicAtom(Predicates.getPredicate("constantTerm_value", 2), reifiedId, Terms.newConstant("someString"))));
	}

	@Test
	public void reifyStringWithQuotes() {
		ConstantTerm<String> constant = Terms.newConstant("someStringWith\"Quotes\"");
		IdGenerator<ConstantTerm<?>> idGen = newIdGenerator();
		ConstantTerm<?> reifiedId = idGen.getNextId();
		Set<BasicAtom> reified = new Reifier(idGen).reifyConstantTerm(reifiedId, constant);
		assertTrue(reified.contains(Atoms.newBasicAtom(Predicates.getPredicate("term_type", 2), reifiedId, Terms.newSymbolicConstant("constant"))));
		assertTrue(reified.contains(Atoms.newBasicAtom(Predicates.getPredicate("constantTerm_type", 2), reifiedId, Terms.newConstant("string"))));
		assertTrue(reified.contains(Atoms.newBasicAtom(Predicates.getPredicate("constantTerm_value", 2), reifiedId, Terms.newConstant("someStringWith\\\"Quotes\\\""))));

	}

	@Test
	public void reifyIntegerConstant() {
		ConstantTerm<Integer> constant = Terms.newConstant(666);
		IdGenerator<ConstantTerm<?>> idGen = newIdGenerator();
		ConstantTerm<?> reifiedId = idGen.getNextId();
		Set<BasicAtom> reified = new Reifier(idGen).reifyConstantTerm(reifiedId, constant);
		assertTrue(reified.contains(Atoms.newBasicAtom(Predicates.getPredicate("term_type", 2), reifiedId, Terms.newSymbolicConstant("constant"))));
		assertTrue(reified.contains(Atoms.newBasicAtom(Predicates.getPredicate("constantTerm_type", 2), reifiedId, Terms.newConstant("integer"))));
		assertTrue(reified.contains(Atoms.newBasicAtom(Predicates.getPredicate("constantTerm_value", 2), reifiedId, Terms.newConstant(Integer.toString(666)))));
	}

	@Test
	public void reifyVariable() {
		VariableTerm var = Terms.newVariable("SOME_VAR");
		IdGenerator<ConstantTerm<?>> idGen = newIdGenerator();
		ConstantTerm<?> reifiedId = idGen.getNextId();
		Set<BasicAtom> reified = new Reifier(idGen).reifyVariableTerm(reifiedId, var);
		assertTrue(reified.contains(Atoms.newBasicAtom(Predicates.getPredicate("term_type", 2), reifiedId, Terms.newSymbolicConstant("variable"))));
		assertTrue(reified.contains(Atoms.newBasicAtom(Predicates.getPredicate("variableTerm_symbol", 2), reifiedId, Terms.newConstant("SOME_VAR"))));
	}

	@Test
	public void reifyArithmeticTerm() {
		Term arithmeticTerm = Terms.newArithmeticTerm(Terms.newVariable("VAR"), ArithmeticOperator.PLUS, Terms.newConstant(2));
		IdGenerator<ConstantTerm<?>> idGen = newIdGenerator();
		ConstantTerm<?> reifiedId = idGen.getNextId();
		Set<BasicAtom> reified = new Reifier(idGen).reifyArithmeticTerm(reifiedId, (ArithmeticTerm) arithmeticTerm);
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
		IdGenerator<ConstantTerm<?>> idGen = newIdGenerator();
		ConstantTerm<?> reifiedId = idGen.getNextId();
		Set<BasicAtom> reified = new Reifier(idGen).reifyFunctionTerm(reifiedId, funcTerm);
		assertTrue(reified.contains(Atoms.newBasicAtom(Predicates.getPredicate("term_type", 2), reifiedId, Terms.newSymbolicConstant("function"))));
		assertTrue(reified.contains(Atoms.newBasicAtom(Predicates.getPredicate("functionTerm_symbol", 2), reifiedId, Terms.newConstant("f"))));
		assertTrue(reified.contains(Atoms.newBasicAtom(Predicates.getPredicate("functionTerm_numArguments", 2), reifiedId, Terms.newConstant(1))));
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

		IdGenerator<ConstantTerm<?>> idGen = newIdGenerator();
		Reifier reificationHelper = new Reifier(idGen);

		ConstantTerm<?> constId = idGen.getNextId();
		Set<BasicAtom> reifiedConst = reificationHelper.reifyTerm(constId, constTerm);
		assertTrue(reifiedConst.contains(Atoms.newBasicAtom(Predicates.getPredicate("term_type", 2), constId, Terms.newSymbolicConstant("constant"))));

		ConstantTerm<?> varId = idGen.getNextId();
		Set<BasicAtom> reifiedVar = reificationHelper.reifyTerm(varId, varTerm);
		assertTrue(reifiedVar.contains(Atoms.newBasicAtom(Predicates.getPredicate("term_type", 2), varId, Terms.newSymbolicConstant("variable"))));

		ConstantTerm<?> calcId = idGen.getNextId();
		Set<BasicAtom> reifiedCalc = reificationHelper.reifyTerm(calcId, arithmeticTerm);
		assertTrue(reifiedCalc.contains(Atoms.newBasicAtom(Predicates.getPredicate("term_type", 2), calcId, Terms.newSymbolicConstant("arithmetic"))));

		ConstantTerm<?> funcId = idGen.getNextId();
		Set<BasicAtom> reifiedFunc = reificationHelper.reifyTerm(funcId, funcTerm);
		assertTrue(reifiedFunc.contains(Atoms.newBasicAtom(Predicates.getPredicate("term_type", 2), funcId, Terms.newSymbolicConstant("function"))));
	}

	@Test
	public void reifyBasicAtom() {
		BasicAtom atom = Atoms.newBasicAtom(Predicates.getPredicate("p", 2), Terms.newVariable("X"), Terms.newVariable("Y"));
		IdGenerator<ConstantTerm<?>> idGen = newIdGenerator();
		ConstantTerm<?> reifiedId = idGen.getNextId();
		Set<BasicAtom> reified = new Reifier(idGen).reifyBasicAtom(reifiedId, atom);
		assertTrue(reified.contains(Atoms.newBasicAtom(Predicates.getPredicate("atom_type", 2), reifiedId, Terms.newSymbolicConstant("basic"))));
		assertTrue(reified.contains(Atoms.newBasicAtom(Predicates.getPredicate("basicAtom_numTerms", 2), reifiedId, Terms.newConstant(2))));		
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
		IdGenerator<ConstantTerm<?>> idGen = newIdGenerator();
		ConstantTerm<?> reifiedId = idGen.getNextId();
		Set<BasicAtom> reified = new Reifier(idGen).reifyComparisonAtom(reifiedId, atom);
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
		IdGenerator<ConstantTerm<?>> idGen = newIdGenerator();
		ConstantTerm<?> reifiedId = idGen.getNextId();
		Set<BasicAtom> reified = new Reifier(idGen).reifyAggregateElement(reifiedId, element);
		assertTrue(reified.contains(Atoms.newBasicAtom(Predicates.getPredicate("aggregateElement_numTerms", 2), reifiedId, Terms.newConstant(1))));
		assertEquals(1,
				reified.stream()
						.filter(
								(a) -> a.getPredicate().equals(Predicates.getPredicate("aggregateElement_term", 3)))
						.collect(Collectors.toList())
						.size());
		assertTrue(reified.contains(Atoms.newBasicAtom(Predicates.getPredicate("aggregateElement_numLiterals", 2), reifiedId, Terms.newConstant(1))));
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

		IdGenerator<ConstantTerm<?>> idGen = newIdGenerator();
		ConstantTerm<?> reifiedId = idGen.getNextId();
		Set<BasicAtom> reified = new Reifier(idGen).reifyAggregateAtom(reifiedId, atom);

		assertTrue(reified.contains(Atoms.newBasicAtom(Predicates.getPredicate("atom_type", 2), reifiedId, Terms.newSymbolicConstant("aggregate"))));
		assertTrue(reified
				.contains(Atoms.newBasicAtom(Predicates.getPredicate("aggregateAtom_aggregateFunction", 2), reifiedId, Terms.newSymbolicConstant("count"))));
		assertTrue(reified.contains(Atoms.newBasicAtom(Predicates.getPredicate("aggregateAtom_numAggregateElements", 2), reifiedId, Terms.newConstant(2))));				
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

		IdGenerator<ConstantTerm<?>> idGen = newIdGenerator();
		ConstantTerm<?> reifiedId = idGen.getNextId();
		Set<BasicAtom> reified = new Reifier(idGen).reifyExternalAtom(reifiedId, atom);

		assertTrue(reified.contains(Atoms.newBasicAtom(Predicates.getPredicate("atom_type", 2), reifiedId, Terms.newSymbolicConstant("external"))));
		assertTrue(reified.contains(Atoms.newBasicAtom(Predicates.getPredicate("externalAtom_numInputTerms", 2), reifiedId, Terms.newConstant(1))));
		assertTrue(reified.contains(Atoms.newBasicAtom(Predicates.getPredicate("externalAtom_numOutputTerms", 2), reifiedId, Terms.newConstant(1))));
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

		IdGenerator<ConstantTerm<?>> idGen = newIdGenerator();
		
		ConstantTerm<?> reifiedBasicAtomId = idGen.getNextId();
		Set<BasicAtom> reifiedBasicAtom = new Reifier(idGen).reifyAtom(reifiedBasicAtomId, basicAtom);
		assertTrue(reifiedBasicAtom.contains(Atoms.newBasicAtom(Predicates.getPredicate("atom_type", 2), reifiedBasicAtomId, Terms.newSymbolicConstant("basic"))));

		ConstantTerm<?> reifiedCmpAtomId = idGen.getNextId();
		Set<BasicAtom> reifiedCmpAtom = new Reifier(idGen).reifyAtom(reifiedCmpAtomId, cmpAtom);
		assertTrue(reifiedCmpAtom.contains(Atoms.newBasicAtom(Predicates.getPredicate("atom_type", 2), reifiedCmpAtomId, Terms.newSymbolicConstant("comparison"))));

		ConstantTerm<?> reifiedAggAtomId = idGen.getNextId();
		Set<BasicAtom> reifiedAggAtom = new Reifier(idGen).reifyAtom(reifiedAggAtomId, aggAtom);
		assertTrue(reifiedAggAtom.contains(Atoms.newBasicAtom(Predicates.getPredicate("atom_type", 2), reifiedAggAtomId, Terms.newSymbolicConstant("aggregate"))));

		ConstantTerm<?> reifiedExtAtomId = idGen.getNextId();
		Set<BasicAtom> reifiedExtAtom = new Reifier(idGen).reifyAtom(reifiedExtAtomId, extAtom);
		assertTrue(reifiedExtAtom.contains(Atoms.newBasicAtom(Predicates.getPredicate("atom_type", 2), reifiedExtAtomId, Terms.newSymbolicConstant("external"))));
	}

	@Test
	public void reifyDirectives() {
		InlineDirectives directives = Programs.newInlineDirectives();
		directives.addDirective(InlineDirectives.DIRECTIVE.enum_predicate_is, "bla");
		ASPCore2Program program = Programs.builder()
			.addRule(Rules.newRule(
				Heads.newNormalHead(Atoms.newBasicAtom(Predicates.getPredicate("a", 0))), 
				Atoms.newBasicAtom(Predicates.getPredicate("b", 0)).toLiteral()))
			.addInlineDirectives(directives)
			.build();
		Set<BasicAtom> reified = new Reifier(newIdGenerator()).reifyProgram(program);
		assertTrue(reified.contains(Atoms.newBasicAtom(Reifier.INLINE_DIRECTIVE, Terms.newConstant("enum_predicate_is"), Terms.newConstant("bla"))));
	}
}
