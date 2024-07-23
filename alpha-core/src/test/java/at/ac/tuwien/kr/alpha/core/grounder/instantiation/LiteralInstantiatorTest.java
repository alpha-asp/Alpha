package at.ac.tuwien.kr.alpha.core.grounder.instantiation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.List;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.junit.jupiter.api.Test;

import at.ac.tuwien.kr.alpha.api.grounder.Substitution;
import at.ac.tuwien.kr.alpha.api.programs.Predicate;
import at.ac.tuwien.kr.alpha.api.programs.atoms.ComparisonAtom;
import at.ac.tuwien.kr.alpha.api.programs.literals.Literal;
import at.ac.tuwien.kr.alpha.api.programs.terms.VariableTerm;
import at.ac.tuwien.kr.alpha.commons.Predicates;
import at.ac.tuwien.kr.alpha.commons.comparisons.ComparisonOperators;
import at.ac.tuwien.kr.alpha.commons.programs.atoms.Atoms;
import at.ac.tuwien.kr.alpha.commons.programs.literals.Literals;
import at.ac.tuwien.kr.alpha.commons.programs.terms.Terms;
import at.ac.tuwien.kr.alpha.commons.substitutions.BasicSubstitution;
import at.ac.tuwien.kr.alpha.core.grounder.WorkingMemory;
import at.ac.tuwien.kr.alpha.core.programs.atoms.EnumerationAtom;
import at.ac.tuwien.kr.alpha.core.programs.atoms.EnumerationLiteral;

public class LiteralInstantiatorTest {

	@Test
	public void instantiateSatisfiedFixedInterpretationLiteral() {
		ComparisonAtom equalsThree = Atoms.newComparisonAtom(Terms.newConstant(3), Terms.newVariable("THREE"), ComparisonOperators.EQ);
		Literal lit = Literals.fromAtom(equalsThree, true);
		Substitution substitution = new BasicSubstitution();
		LiteralInstantiator instantiator = new LiteralInstantiator(new WorkingMemoryBasedInstantiationStrategy(null));
		LiteralInstantiationResult result = instantiator.instantiateLiteral(lit, substitution);
		assertEquals(LiteralInstantiationResult.Type.CONTINUE, result.getType());
		List<ImmutablePair<Substitution, AssignmentStatus>> resultSubstitutions = result.getSubstitutions();
		assertEquals(1, resultSubstitutions.size());
		assertEquals(AssignmentStatus.TRUE, resultSubstitutions.get(0).right);
		Substitution extendedSubstitution = resultSubstitutions.get(0).left;
		assertTrue(extendedSubstitution.isVariableSet(Terms.newVariable("THREE")));
		assertEquals(Terms.newConstant(3), extendedSubstitution.eval(Terms.newVariable("THREE")));
	}

	@Test
	public void instantiateUnsatisfiedFixedInterpretationLiteral() {
		ComparisonAtom fiveEqualsThree = Atoms.newComparisonAtom(Terms.newVariable("FIVE"), Terms.newVariable("THREE"), ComparisonOperators.EQ);
		Literal lit = Literals.fromAtom(fiveEqualsThree, true);
		Substitution substitution = new BasicSubstitution();
		substitution.put(Terms.newVariable("FIVE"), Terms.newConstant(5));
		substitution.put(Terms.newVariable("THREE"), Terms.newConstant(3));
		LiteralInstantiator instantiator = new LiteralInstantiator(new WorkingMemoryBasedInstantiationStrategy(null));
		LiteralInstantiationResult result = instantiator.instantiateLiteral(lit, substitution);
		assertEquals(LiteralInstantiationResult.Type.STOP_BINDING, result.getType());
	}

	@Test
	public void instantiateEnumLiteral() {
		VariableTerm enumTerm = Terms.newVariable("E");
		VariableTerm idTerm = Terms.newVariable("X");
		VariableTerm indexTerm = Terms.newVariable("I");
		EnumerationAtom enumAtom = new EnumerationAtom(enumTerm, idTerm, indexTerm);
		EnumerationLiteral lit = new EnumerationLiteral(enumAtom);
		Substitution substitution = new BasicSubstitution();
		substitution.put(enumTerm, Terms.newSymbolicConstant("enum1"));
		substitution.put(idTerm, Terms.newSymbolicConstant("someElement"));
		LiteralInstantiator instantiator = new LiteralInstantiator(new WorkingMemoryBasedInstantiationStrategy(null));
		LiteralInstantiationResult result = instantiator.instantiateLiteral(lit, substitution);
		assertEquals(LiteralInstantiationResult.Type.CONTINUE, result.getType());
		List<ImmutablePair<Substitution, AssignmentStatus>> resultSubstitutions = result.getSubstitutions();
		assertEquals(1, resultSubstitutions.size());
		assertEquals(AssignmentStatus.TRUE, resultSubstitutions.get(0).right);
		assertTrue(resultSubstitutions.get(0).left.isVariableSet(indexTerm));
	}

	@Test
	public void workingMemoryBasedVerifyPositiveGroundLiteralSatisfied() {
		Predicate p = Predicates.getPredicate("p", 2);
		WorkingMemory workingMemory = new WorkingMemory();
		workingMemory.initialize(p);
		workingMemory.addInstance(Atoms.newBasicAtom(p, Terms.newSymbolicConstant("x"), Terms.newSymbolicConstant("y")), true);
		VariableTerm x = Terms.newVariable("X");
		VariableTerm y = Terms.newVariable("Y");
		Literal lit = Literals.fromAtom(Atoms.newBasicAtom(p, x, y), true);
		Substitution substitution = new BasicSubstitution();
		substitution.put(x, Terms.newSymbolicConstant("x"));
		substitution.put(y, Terms.newSymbolicConstant("y"));
		LiteralInstantiator instantiator = new LiteralInstantiator(new WorkingMemoryBasedInstantiationStrategy(workingMemory));
		LiteralInstantiationResult result = instantiator.instantiateLiteral(lit, substitution);
		assertEquals(LiteralInstantiationResult.Type.CONTINUE, result.getType());
		List<ImmutablePair<Substitution, AssignmentStatus>> substitutions = result.getSubstitutions();
		assertEquals(1, substitutions.size());
		assertEquals(AssignmentStatus.TRUE, substitutions.get(0).right);
		Substitution resultSubstitution = substitutions.get(0).left;
		// With the given input substitution, lit is ground and satisfied -
		// we expect the instantiator to verify that.
		assertEquals(substitution, resultSubstitution);
	}

	@Test
	public void workingMemoryBasedVerifyPositiveGroundLiteralUnsatisfied() {
		Predicate p = Predicates.getPredicate("p", 2);
		WorkingMemory workingMemory = new WorkingMemory();
		workingMemory.initialize(p);
		VariableTerm x = Terms.newVariable("X");
		VariableTerm y = Terms.newVariable("Y");
		Literal lit = Literals.fromAtom(Atoms.newBasicAtom(p, x, y), true);
		Substitution substitution = new BasicSubstitution();
		substitution.put(x, Terms.newSymbolicConstant("x"));
		substitution.put(y, Terms.newSymbolicConstant("y"));
		LiteralInstantiator instantiator = new LiteralInstantiator(new WorkingMemoryBasedInstantiationStrategy(workingMemory));
		LiteralInstantiationResult result = instantiator.instantiateLiteral(lit, substitution);
		// With the given input substitution, lit is ground, but not satisfied -
		// we expect the instantiator to verify that and return an empty list of
		// substitutions.
		assertEquals(LiteralInstantiationResult.Type.STOP_BINDING, result.getType());
	}

	@Test
	public void workingMemoryBasedInstantiatePositiveBasicLiteral() {
		Predicate p = Predicates.getPredicate("p", 2);
		WorkingMemory workingMemory = new WorkingMemory();
		workingMemory.initialize(p);
		workingMemory.addInstance(Atoms.newBasicAtom(p, Terms.newSymbolicConstant("x"), Terms.newSymbolicConstant("y")), true);
		workingMemory.addInstance(Atoms.newBasicAtom(p, Terms.newSymbolicConstant("x"), Terms.newSymbolicConstant("z")), true);
		VariableTerm x = Terms.newVariable("X");
		VariableTerm y = Terms.newVariable("Y");
		Literal lit = Literals.fromAtom(Atoms.newBasicAtom(p, x, y), true);
		Substitution substitution = new BasicSubstitution();
		substitution.put(x, Terms.newSymbolicConstant("x"));
		LiteralInstantiator instantiator = new LiteralInstantiator(new WorkingMemoryBasedInstantiationStrategy(workingMemory));
		LiteralInstantiationResult result = instantiator.instantiateLiteral(lit, substitution);
		assertEquals(LiteralInstantiationResult.Type.CONTINUE, result.getType());
		List<ImmutablePair<Substitution, AssignmentStatus>> substitutions = result.getSubstitutions();
		assertEquals(2, substitutions.size());
		boolean ySubstituted = false;
		boolean zSubstituted = false;
		for (ImmutablePair<Substitution, AssignmentStatus> resultSubstitution : substitutions) {
			assertTrue(resultSubstitution.left.isVariableSet(y));
			assertEquals(AssignmentStatus.TRUE, resultSubstitution.right);
			if (resultSubstitution.left.eval(y).equals(Terms.newSymbolicConstant("y"))) {
				ySubstituted = true;
			} else if (resultSubstitution.left.eval(y).equals(Terms.newSymbolicConstant("z"))) {
				zSubstituted = true;
			} else {
				fail("Invalid substitution for variable Y");
			}
		}
		assertTrue(ySubstituted && zSubstituted);
	}
}
