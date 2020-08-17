package at.ac.tuwien.kr.alpha.grounder.instantiation;

import at.ac.tuwien.kr.alpha.common.ComparisonOperator;
import at.ac.tuwien.kr.alpha.common.Predicate;
import at.ac.tuwien.kr.alpha.common.atoms.BasicAtom;
import at.ac.tuwien.kr.alpha.common.atoms.BasicLiteral;
import at.ac.tuwien.kr.alpha.common.atoms.ComparisonAtom;
import at.ac.tuwien.kr.alpha.common.atoms.ComparisonLiteral;
import at.ac.tuwien.kr.alpha.common.atoms.Literal;
import at.ac.tuwien.kr.alpha.common.terms.ConstantTerm;
import at.ac.tuwien.kr.alpha.common.terms.Term;
import at.ac.tuwien.kr.alpha.common.terms.VariableTerm;
import at.ac.tuwien.kr.alpha.grounder.Substitution;
import at.ac.tuwien.kr.alpha.grounder.WorkingMemory;
import at.ac.tuwien.kr.alpha.grounder.atoms.EnumerationAtom;
import at.ac.tuwien.kr.alpha.grounder.atoms.EnumerationLiteral;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class LiteralInstantiatorTest {

	@Test
	public void instantiateSatisfiedFixedInterpretationLiteral() {
		ComparisonAtom equalsThree = new ComparisonAtom(ConstantTerm.getInstance(3), VariableTerm.getInstance("THREE"), ComparisonOperator.EQ);
		Literal lit = new ComparisonLiteral(equalsThree, true);
		Substitution substitution = new Substitution();
		LiteralInstantiator instantiator = new LiteralInstantiator(new WorkingMemoryBasedInstantiationStrategy(null));
		LiteralInstantiationResult result = instantiator.instantiateLiteral(lit, substitution);
		Assert.assertEquals(LiteralInstantiationResult.Type.CONTINUE, result.getType());
		List<ImmutablePair<Substitution, AssignmentStatus>> resultSubstitutions = result.getSubstitutions();
		Assert.assertEquals(1, resultSubstitutions.size());
		Assert.assertEquals(AssignmentStatus.TRUE, resultSubstitutions.get(0).right);
		Substitution extendedSubstitution = resultSubstitutions.get(0).left;
		Assert.assertTrue(extendedSubstitution.isVariableSet(VariableTerm.getInstance("THREE")));
		Assert.assertEquals(ConstantTerm.getInstance(3), extendedSubstitution.eval(VariableTerm.getInstance("THREE")));
	}

	@Test
	public void instantiateUnsatisfiedFixedInterpretationLiteral() {
		ComparisonAtom fiveEqualsThree = new ComparisonAtom(VariableTerm.getInstance("FIVE"), VariableTerm.getInstance("THREE"), ComparisonOperator.EQ);
		Literal lit = new ComparisonLiteral(fiveEqualsThree, true);
		Substitution substitution = new Substitution();
		substitution.put(VariableTerm.getInstance("FIVE"), ConstantTerm.getInstance(5));
		substitution.put(VariableTerm.getInstance("THREE"), ConstantTerm.getInstance(3));
		LiteralInstantiator instantiator = new LiteralInstantiator(new WorkingMemoryBasedInstantiationStrategy(null));
		LiteralInstantiationResult result = instantiator.instantiateLiteral(lit, substitution);
		Assert.assertEquals(LiteralInstantiationResult.Type.STOP_BINDING, result.getType());
	}

	@Test
	public void instantiateEnumLiteral() {
		VariableTerm enumTerm = VariableTerm.getInstance("E");
		VariableTerm idTerm = VariableTerm.getInstance("X");
		VariableTerm indexTerm = VariableTerm.getInstance("I");
		List<Term> termList = new ArrayList<>();
		termList.add(enumTerm);
		termList.add(idTerm);
		termList.add(indexTerm);
		EnumerationAtom enumAtom = new EnumerationAtom(termList);
		EnumerationLiteral lit = new EnumerationLiteral(enumAtom);
		Substitution substitution = new Substitution();
		substitution.put(enumTerm, ConstantTerm.getSymbolicInstance("enum1"));
		substitution.put(idTerm, ConstantTerm.getSymbolicInstance("someElement"));
		LiteralInstantiator instantiator = new LiteralInstantiator(new WorkingMemoryBasedInstantiationStrategy(null));
		LiteralInstantiationResult result = instantiator.instantiateLiteral(lit, substitution);
		Assert.assertEquals(LiteralInstantiationResult.Type.CONTINUE, result.getType());
		List<ImmutablePair<Substitution, AssignmentStatus>> resultSubstitutions = result.getSubstitutions();
		Assert.assertEquals(1, resultSubstitutions.size());
		Assert.assertEquals(AssignmentStatus.TRUE, resultSubstitutions.get(0).right);
		Assert.assertTrue(resultSubstitutions.get(0).left.isVariableSet(indexTerm));
	}

	@Test
	public void workingMemoryBasedVerifyPositiveGroundLiteralSatisfied() {
		Predicate p = Predicate.getInstance("p", 2);
		WorkingMemory workingMemory = new WorkingMemory();
		workingMemory.initialize(p);
		workingMemory.addInstance(new BasicAtom(p, ConstantTerm.getSymbolicInstance("x"), ConstantTerm.getSymbolicInstance("y")), true);
		VariableTerm x = VariableTerm.getInstance("X");
		VariableTerm y = VariableTerm.getInstance("Y");
		Literal lit = new BasicLiteral(new BasicAtom(p, x, y), true);
		Substitution substitution = new Substitution();
		substitution.put(x, ConstantTerm.getSymbolicInstance("x"));
		substitution.put(y, ConstantTerm.getSymbolicInstance("y"));
		LiteralInstantiator instantiator = new LiteralInstantiator(new WorkingMemoryBasedInstantiationStrategy(workingMemory));
		LiteralInstantiationResult result = instantiator.instantiateLiteral(lit, substitution);
		Assert.assertEquals(LiteralInstantiationResult.Type.CONTINUE, result.getType());
		List<ImmutablePair<Substitution, AssignmentStatus>> substitutions = result.getSubstitutions();
		Assert.assertEquals(1, substitutions.size());
		Assert.assertEquals(AssignmentStatus.TRUE, substitutions.get(0).right);
		Substitution resultSubstitution = substitutions.get(0).left;
		// With the given input substitution, lit is ground and satisfied -
		// we expect the instantiator to verify that.
		Assert.assertEquals(substitution, resultSubstitution);
	}

	@Test
	public void workingMemoryBasedVerifyPositiveGroundLiteralUnsatisfied() {
		Predicate p = Predicate.getInstance("p", 2);
		WorkingMemory workingMemory = new WorkingMemory();
		workingMemory.initialize(p);
		VariableTerm x = VariableTerm.getInstance("X");
		VariableTerm y = VariableTerm.getInstance("Y");
		Literal lit = new BasicLiteral(new BasicAtom(p, x, y), true);
		Substitution substitution = new Substitution();
		substitution.put(x, ConstantTerm.getSymbolicInstance("x"));
		substitution.put(y, ConstantTerm.getSymbolicInstance("y"));
		LiteralInstantiator instantiator = new LiteralInstantiator(new WorkingMemoryBasedInstantiationStrategy(workingMemory));
		LiteralInstantiationResult result = instantiator.instantiateLiteral(lit, substitution);
		// With the given input substitution, lit is ground, but not satisfied -
		// we expect the instantiator to verify that and return an empty list of
		// substitutions.
		Assert.assertEquals(LiteralInstantiationResult.Type.STOP_BINDING, result.getType());
	}

	@Test
	public void workingMemoryBasedInstantiatePositiveBasicLiteral() {
		Predicate p = Predicate.getInstance("p", 2);
		WorkingMemory workingMemory = new WorkingMemory();
		workingMemory.initialize(p);
		workingMemory.addInstance(new BasicAtom(p, ConstantTerm.getSymbolicInstance("x"), ConstantTerm.getSymbolicInstance("y")), true);
		workingMemory.addInstance(new BasicAtom(p, ConstantTerm.getSymbolicInstance("x"), ConstantTerm.getSymbolicInstance("z")), true);
		VariableTerm x = VariableTerm.getInstance("X");
		VariableTerm y = VariableTerm.getInstance("Y");
		Literal lit = new BasicLiteral(new BasicAtom(p, x, y), true);
		Substitution substitution = new Substitution();
		substitution.put(x, ConstantTerm.getSymbolicInstance("x"));
		LiteralInstantiator instantiator = new LiteralInstantiator(new WorkingMemoryBasedInstantiationStrategy(workingMemory));
		LiteralInstantiationResult result = instantiator.instantiateLiteral(lit, substitution);
		Assert.assertEquals(LiteralInstantiationResult.Type.CONTINUE, result.getType());
		List<ImmutablePair<Substitution, AssignmentStatus>> substitutions = result.getSubstitutions();
		Assert.assertEquals(2, substitutions.size());
		boolean ySubstituted = false;
		boolean zSubstituted = false;
		for (ImmutablePair<Substitution, AssignmentStatus> resultSubstitution : substitutions) {
			Assert.assertTrue(resultSubstitution.left.isVariableSet(y));
			Assert.assertEquals(AssignmentStatus.TRUE, resultSubstitution.right);
			if (resultSubstitution.left.eval(y).equals(ConstantTerm.getSymbolicInstance("y"))) {
				ySubstituted = true;
			} else if (resultSubstitution.left.eval(y).equals(ConstantTerm.getSymbolicInstance("z"))) {
				zSubstituted = true;
			} else {
				Assert.fail("Invalid substitution for variable Y");
			}
		}
		Assert.assertTrue(ySubstituted && zSubstituted);
	}
}
