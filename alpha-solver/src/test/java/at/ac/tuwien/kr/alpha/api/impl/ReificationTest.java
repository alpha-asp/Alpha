package at.ac.tuwien.kr.alpha.api.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import at.ac.tuwien.kr.alpha.api.Alpha;
import at.ac.tuwien.kr.alpha.api.programs.atoms.BasicAtom;
import at.ac.tuwien.kr.alpha.api.programs.terms.Term;
import at.ac.tuwien.kr.alpha.commons.programs.atoms.Atoms;
import at.ac.tuwien.kr.alpha.commons.programs.terms.Terms;

/**
 * Integration tests for program reification, i.e. generating sets of facts describing arbitrary ASP programs.
 */
public class ReificationTest {

	private final Alpha alpha = new AlphaImpl();

	@Test
	public void propositionalFact() {
		Set<BasicAtom> reified = alpha.reify(alpha.readProgramString("aRatherUselessFact."));
		List<BasicAtom> factAtomResult = reified.stream().filter(Atoms.query("fact", 1)).collect(Collectors.toList());
		assertEquals(1, factAtomResult.size());
		BasicAtom factAtom = factAtomResult.get(0);
		Term idTerm = factAtom.getTerms().get(0);
		assertTrue(reified.stream().filter(
				Atoms.query("atom_type", 2)
						.withTermEquals(0, idTerm)
						.withTermEquals(1, Terms.newSymbolicConstant("basic")))
				.findFirst()
				.isPresent());
		assertTrue(reified.stream().filter(
				Atoms.query("basicAtom_numTerms", 2)
						.withTermEquals(0, idTerm)
						.withTermEquals(1, Terms.newConstant(0)))
				.findFirst()
				.isPresent());
		Optional<BasicAtom> predicateAtomResult = reified.stream().filter(Atoms.query("basicAtom_predicate", 2)
				.withTermEquals(0, idTerm)).findFirst();
		assertTrue(predicateAtomResult.isPresent());
		Term predicateIdTerm = predicateAtomResult.get().getTerms().get(1);
		Optional<BasicAtom> predicateDescriptorResult = reified.stream().filter(Atoms.query("predicate", 3)
				.withTermEquals(0, predicateIdTerm)).findFirst();
		assertTrue(predicateDescriptorResult.isPresent());
		BasicAtom predicateDescriptor = predicateDescriptorResult.get();
		assertEquals(Terms.newConstant("aRatherUselessFact"), predicateDescriptor.getTerms().get(1));
		assertEquals(Terms.newConstant(0), predicateDescriptor.getTerms().get(2));
	}

	@Test
	public void simplePositiveRule() {
		Set<BasicAtom> reified = alpha.reify(alpha.readProgramString("p(X) :- q(X), r(X)."));
		List<BasicAtom> ruleDescriptorResult = reified.stream().filter(Atoms.query("rule", 1)).collect(Collectors.toList());
		assertEquals(1, ruleDescriptorResult.size());
		BasicAtom ruleDescriptor = ruleDescriptorResult.get(0);
		Term ruleId = ruleDescriptor.getTerms().get(0);
		List<BasicAtom> headDescriptorResult = reified.stream().filter(
				Atoms.query("rule_head", 2)
						.withTermEquals(0, ruleId))
				.collect(Collectors.toList());
		assertEquals(1, headDescriptorResult.size());
		BasicAtom headDescriptor = headDescriptorResult.get(0);
		Term headId = headDescriptor.getTerms().get(1);
		assertEquals(1, reified.stream().filter(
				Atoms.query("head_type", 2)
						.withTermEquals(0, headId)
						.withTermEquals(1, Terms.newSymbolicConstant("normal")))
				.collect(Collectors.toList()).size());
		List<BasicAtom> headAtomDescriptorResult = reified.stream().filter(
				Atoms.query("normalHead_atom", 2)
						.withTermEquals(0, headId))
				.collect(Collectors.toList());
		assertEquals(1, headAtomDescriptorResult.size());
		BasicAtom headAtomDescriptor = headAtomDescriptorResult.get(0);
		Term headAtomId = headAtomDescriptor.getTerms().get(1);
		Set<BasicAtom> atomTypeDescriptors = reified.stream().filter(
				Atoms.query("atom_type", 2)
						.withTermEquals(1, Terms.newSymbolicConstant("basic")))
				.collect(Collectors.toSet());
		assertEquals(3, atomTypeDescriptors.size());
		assertEquals(1, atomTypeDescriptors.stream().filter(Atoms.query("atom_type", 2).withTermEquals(0, headAtomId)).collect(Collectors.toList()).size());
		assertEquals(1, reified.stream().filter(
				Atoms.query("rule_numBodyLiterals", 2)
						.withTermEquals(0, ruleId)
						.withTermEquals(1, Terms.newConstant(2)))
				.collect(Collectors.toList()).size());
		Set<BasicAtom> bodyLiteralDescriptors = reified.stream().filter(
				Atoms.query("rule_bodyLiteral", 2)
						.withTermEquals(0, ruleId))
				.collect(Collectors.toSet());
		assertEquals(2, bodyLiteralDescriptors.size());
		for (BasicAtom bodyLiteralDescriptor : bodyLiteralDescriptors) {
			assertEquals(1, reified.stream().filter(
					Atoms.query("literal_polarity", 2)
							.withTermEquals(0, bodyLiteralDescriptor.getTerms().get(1))
							.withTermEquals(1, Terms.newSymbolicConstant("pos")))
					.collect(Collectors.toList()).size());
			assertEquals(1, reified.stream().filter(
					Atoms.query("literal_atom", 2)
							.withTermEquals(0, bodyLiteralDescriptor.getTerms().get(1)))
					.collect(Collectors.toList()).size());
		}
		assertEquals(3, reified.stream().filter(
				Atoms.query("term_type", 2)
						.withTermEquals(1, Terms.newSymbolicConstant("variable")))
				.collect(Collectors.toSet()).size());
		assertEquals(3, reified.stream().filter(
			Atoms.query("variableTerm_symbol", 2)
					.withTermEquals(1, Terms.newConstant("X")))
			.collect(Collectors.toSet()).size());
	}

}
