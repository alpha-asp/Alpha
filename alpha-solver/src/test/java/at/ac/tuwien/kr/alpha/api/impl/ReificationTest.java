package at.ac.tuwien.kr.alpha.api.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import at.ac.tuwien.kr.alpha.api.Alpha;
import at.ac.tuwien.kr.alpha.api.ComparisonOperator;
import at.ac.tuwien.kr.alpha.api.programs.Predicate;
import at.ac.tuwien.kr.alpha.api.programs.atoms.AtomQuery;
import at.ac.tuwien.kr.alpha.api.programs.atoms.BasicAtom;
import at.ac.tuwien.kr.alpha.api.programs.terms.ConstantTerm;
import at.ac.tuwien.kr.alpha.api.programs.terms.Term;
import at.ac.tuwien.kr.alpha.commons.Predicates;
import at.ac.tuwien.kr.alpha.commons.comparisons.ComparisonOperators;
import at.ac.tuwien.kr.alpha.commons.programs.atoms.Atoms;
import at.ac.tuwien.kr.alpha.commons.programs.terms.Terms;

/**
 * Integration tests for program reification, i.e. generating sets of facts
 * describing arbitrary ASP programs.
 */
public class ReificationTest {

	private final Alpha alpha = new AlphaImpl();

	private static final Map<ComparisonOperator, ConstantTerm<?>> CMP_OP_IDS;
	
	static {
		Map<ComparisonOperator, ConstantTerm<?>> operators = new HashMap<>();
		operators.put(ComparisonOperators.EQ, Terms.newSymbolicConstant("eq"));
		operators.put(ComparisonOperators.NE, Terms.newSymbolicConstant("ne"));
		operators.put(ComparisonOperators.LE, Terms.newSymbolicConstant("le"));
		operators.put(ComparisonOperators.LT, Terms.newSymbolicConstant("lt"));
		operators.put(ComparisonOperators.GE, Terms.newSymbolicConstant("ge"));
		operators.put(ComparisonOperators.GT, Terms.newSymbolicConstant("gt"));
		CMP_OP_IDS = Collections.unmodifiableMap(operators);
	}

	@Test
	public void propositionalFact() {
		Set<BasicAtom> reified = alpha.reify(alpha.readProgramString("aRatherUselessFact."));
		List<BasicAtom> factAtomResult = reified.stream().filter(Atoms.query("fact", 1)).collect(Collectors.toList());
		assertEquals(1, factAtomResult.size());
		BasicAtom factAtom = factAtomResult.get(0);
		Term idTerm = factAtom.getTerms().get(0);
		assertTrue(reified.stream().anyMatch(
				Atoms.query("atom_type", 2)
						.withTermEquals(0, idTerm)
						.withTermEquals(1, Terms.newSymbolicConstant("basic"))));
		assertTrue(reified.stream().anyMatch(
				Atoms.query("basicAtom_numTerms", 2)
						.withTermEquals(0, idTerm)
						.withTermEquals(1, Terms.newConstant(0))));
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
	public void positiveRule() {
		Set<BasicAtom> reified = alpha.reify(alpha.readProgramString("p(X) :- q(X), r(X)."));
		List<BasicAtom> ruleDescriptorResult = reified.stream().filter(Atoms.query("rule", 1))
				.collect(Collectors.toList());
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
				.count());
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
		assertEquals(1, atomTypeDescriptors.stream().filter(Atoms.query("atom_type", 2).withTermEquals(0, headAtomId))
				.collect(Collectors.toList()).size());
		assertEquals(1, reified.stream().filter(
				Atoms.query("rule_numBodyLiterals", 2)
						.withTermEquals(0, ruleId)
						.withTermEquals(1, Terms.newConstant(2)))
				.count());
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
					.count());
			assertEquals(1, reified.stream().filter(
					Atoms.query("literal_atom", 2)
							.withTermEquals(0, bodyLiteralDescriptor.getTerms().get(1)))
					.count());
		}
		assertEquals(3, reified.stream().filter(
				Atoms.query("term_type", 2)
						.withTermEquals(1, Terms.newSymbolicConstant("variable")))
				.count());
		assertEquals(3, reified.stream().filter(
				Atoms.query("variableTerm_symbol", 2)
						.withTermEquals(1, Terms.newConstant("X")))
				.count());
	}

	@Test
	public void ruleWithNegativeLiteral() {
		Set<BasicAtom> reified = alpha.reify(alpha.readProgramString("p(X) :- q(X), not r(X)."));
		List<ConstantTerm<?>> qIds = findLiteralIdsForPredicate(Predicates.getPredicate("q", 1), Terms.newConstant(0),
				reified);
		assertEquals(1, qIds.size());
		assertEquals(1,
				reified.stream().filter(
						Atoms.query("literal_polarity", 2)
								.withTermEquals(0, qIds.get(0))
								.withTermEquals(1, Terms.newSymbolicConstant("pos")))
						.count());
		List<ConstantTerm<?>> rIds = findLiteralIdsForPredicate(Predicates.getPredicate("r", 1), Terms.newConstant(0),
				reified);
		assertEquals(1, rIds.size());
		assertEquals(1,
				reified.stream().filter(
						Atoms.query("literal_polarity", 2)
								.withTermEquals(0, rIds.get(0))
								.withTermEquals(1, Terms.newSymbolicConstant("neg")))
						.count());
	}

	@Test
	public void comparisonAtom() {
		Set<BasicAtom> reified = alpha.reify(alpha.readProgramString("p(X) :- X = 42."));
		List<ConstantTerm<?>> eqLitIds = findLiteralIdsForComparisonOperator(ComparisonOperators.EQ,  Terms.newConstant(0), reified);
		assertEquals(1, eqLitIds.size());
	}

	@Test
	public void aggregateAtom() {
		Set<BasicAtom> reified = alpha.reify(alpha.readProgramString("aggregateTrue :- X < #count{X : p(X)} < Y, X = 1, Y = 3."));
		List<ConstantTerm<?>> countLitIds = findLiteralIdsForAggregate(Terms.newSymbolicConstant("count"), Terms.newConstant(0), reified);
		assertEquals(1, countLitIds.size());
	}

	@Test
	public void externalAtom() {
		Set<BasicAtom> reified = alpha.reify(alpha.readProgramString("foo :- &stdlib_string_concat[\"foo\", \"bar\"](FOOBAR)."));
		List<ConstantTerm<?>> strcatLitIds = findLiteralIdsForExternal(Terms.newConstant("stdlib_string_concat"), Terms.newConstant(0), reified);
		assertEquals(1, strcatLitIds.size());
	}

	private static List<ConstantTerm<?>> findLiteralIdsForPredicate(Predicate predicate, ConstantTerm<?> reifiedRuleId,
			Set<BasicAtom> reifiedProgram) {
		ConstantTerm<?> predicateId = findPredicateId(predicate, reifiedProgram);
		return ruleBodyLiteralIdStream(reifiedRuleId, reifiedProgram)
				.filter((literalId) -> {
					List<ConstantTerm<?>> atomIdResult = reifiedProgram.stream()
							.filter(Atoms.query("literal_atom", 2).withTermEquals(0, literalId))
							.map((literalToAtomDescriptor) -> (ConstantTerm<?>) literalToAtomDescriptor.getTerms()
									.get(1))
							.collect(Collectors.toList());
					if (atomIdResult.isEmpty()) {
						return false;
					}
					ConstantTerm<?> atomId = atomIdResult.get(0);
					List<ConstantTerm<?>> predicateIdOfBasicAtomResult = reifiedProgram.stream()
							.filter(Atoms.query("basicAtom_predicate", 2).withTermEquals(0, atomId))
							.map((atomToPredicateDescriptor) -> (ConstantTerm<?>) atomToPredicateDescriptor.getTerms()
									.get(1))
							.collect(Collectors.toList());
					return predicateIdOfBasicAtomResult.isEmpty() ? false
							: predicateIdOfBasicAtomResult.get(0).equals(predicateId);
				})
				.collect(Collectors.toList());
	}

	private static List<ConstantTerm<?>> findLiteralIdsForAggregate(ConstantTerm<String> aggregateFunction, ConstantTerm<?> reifiedRuleId, Set<BasicAtom> reifiedProgram) {
		return ruleBodyLiteralIdStream(reifiedRuleId, reifiedProgram)
			.filter((literalId) -> {
				List<ConstantTerm<?>> atomIdResult = reifiedProgram.stream()
							.filter(Atoms.query("literal_atom", 2).withTermEquals(0, literalId))
							.map((literalToAtomDescriptor) -> (ConstantTerm<?>) literalToAtomDescriptor.getTerms()
									.get(1))
							.collect(Collectors.toList());
					if (atomIdResult.isEmpty()) {
						return false;
					}
					ConstantTerm<?> atomId = atomIdResult.get(0);
					return reifiedProgram.stream()
						.anyMatch(
							Atoms.query("aggregateAtom_aggregateFunction", 2)
								.withTermEquals(0, atomId)
								.withTermEquals(1, aggregateFunction));
			}).collect(Collectors.toList());
	}

	private static List<ConstantTerm<?>> findLiteralIdsForExternal(ConstantTerm<String> methodName, ConstantTerm<?> reifiedRuleId, Set<BasicAtom> reifiedProgram) {
		return ruleBodyLiteralIdStream(reifiedRuleId, reifiedProgram)
			.filter((literalId) -> {
				List<ConstantTerm<?>> atomIdResult = reifiedProgram.stream()
							.filter(Atoms.query("literal_atom", 2).withTermEquals(0, literalId))
							.map((literalToAtomDescriptor) -> (ConstantTerm<?>) literalToAtomDescriptor.getTerms()
									.get(1))
							.collect(Collectors.toList());
					if (atomIdResult.isEmpty()) {
						return false;
					}
					ConstantTerm<?> atomId = atomIdResult.get(0);
					return reifiedProgram.stream()
						.anyMatch(
							Atoms.query("externalAtom_name", 2)
								.withTermEquals(0, atomId)
								.withTermEquals(1, methodName));
			}).collect(Collectors.toList());
	}

	private static List<ConstantTerm<?>> findLiteralIdsForComparisonOperator(ComparisonOperator operator,
			ConstantTerm<?> reifiedRuleId, Set<BasicAtom> reifiedProgram) {
		return ruleBodyLiteralIdStream(reifiedRuleId, reifiedProgram)
				.filter((literalId) -> {
					List<ConstantTerm<?>> atomIdResult = reifiedProgram.stream()
							.filter(Atoms.query("literal_atom", 2).withTermEquals(0, literalId))
							.map((literalToAtomDescriptor) -> (ConstantTerm<?>) literalToAtomDescriptor.getTerms()
									.get(1))
							.collect(Collectors.toList());
					if (atomIdResult.isEmpty()) {
						return false;
					}
					ConstantTerm<?> atomId = atomIdResult.get(0);
					return reifiedProgram.stream()
						.anyMatch(
							Atoms.query("comparisonAtom_operator", 2)
								.withTermEquals(0, atomId)
								.withTermEquals(1, CMP_OP_IDS.get(operator)));
				}).collect(Collectors.toList());
	}

	private static ConstantTerm<?> findPredicateId(Predicate predicate, Set<BasicAtom> reifiedProgram) {
		List<BasicAtom> reifiedPredicateResult = reifiedProgram.stream()
				.filter(queryForReifiedPredicate(predicate))
				.collect(Collectors.toList());
		if (reifiedPredicateResult.size() > 1) {
			Assertions.fail("Expected only one atom when querying for reified predicate!");
		}
		return (ConstantTerm<?>) reifiedPredicateResult.get(0).getTerms().get(0);
	}

	private static AtomQuery queryForReifiedPredicate(Predicate predicate) {
		return Atoms.query("predicate", 3)
				.withTermEquals(1, Terms.newConstant(predicate.getName()))
				.withTermEquals(2, Terms.newConstant(predicate.getArity()));
	}

	private static Stream<ConstantTerm<?>> ruleBodyLiteralIdStream(ConstantTerm<?> ruleId,
			Set<BasicAtom> reifiedProgram) {
		return reifiedProgram.stream()
				.filter(Atoms.query("rule_bodyLiteral", 2).withTermEquals(0, ruleId))
				// After filtering we have atoms describing body literals - extract the second
				// term which is the id of the literal
				.map((bodyLiteralDescriptor) -> (ConstantTerm<?>) bodyLiteralDescriptor.getTerms().get(1));
	}

}
