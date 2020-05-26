package at.ac.tuwien.kr.alpha.grounder.instantiation;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import at.ac.tuwien.kr.alpha.common.ComparisonOperator;
import at.ac.tuwien.kr.alpha.common.atoms.ComparisonAtom;
import at.ac.tuwien.kr.alpha.common.atoms.ComparisonLiteral;
import at.ac.tuwien.kr.alpha.common.atoms.Literal;
import at.ac.tuwien.kr.alpha.common.terms.ConstantTerm;
import at.ac.tuwien.kr.alpha.common.terms.Term;
import at.ac.tuwien.kr.alpha.common.terms.VariableTerm;
import at.ac.tuwien.kr.alpha.grounder.Substitution;
import at.ac.tuwien.kr.alpha.grounder.atoms.EnumerationAtom;
import at.ac.tuwien.kr.alpha.grounder.atoms.EnumerationLiteral;

public class RuleInstantiatorTest {

	@Test
	public void instantiateSatisfiedFixedInterpretationLiteral() {
		ComparisonAtom equalsThree = new ComparisonAtom(ConstantTerm.getInstance(3), VariableTerm.getInstance("THREE"), ComparisonOperator.EQ);
		Literal lit = new ComparisonLiteral(equalsThree, true);
		Substitution substitution = new Substitution();
		RuleInstantiator instantiator = new RuleInstantiator(new CautiousInstantiationStrategy());
		List<Substitution> resultSubstitutions = instantiator.instantiateLiteral(lit, substitution, null);
		Assert.assertEquals(1, resultSubstitutions.size());
		Substitution extendedSubstitution = resultSubstitutions.get(0);
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
		RuleInstantiator instantiator = new RuleInstantiator(new CautiousInstantiationStrategy());
		List<Substitution> resultSubstitutions = instantiator.instantiateLiteral(lit, substitution, null);
		Assert.assertTrue(resultSubstitutions.isEmpty());
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
		RuleInstantiator instantiator = new RuleInstantiator(new CautiousInstantiationStrategy());
		List<Substitution> resultSubstitutions = instantiator.instantiateLiteral(lit, substitution, null);
		Assert.assertEquals(1, resultSubstitutions.size());
		Assert.assertTrue(resultSubstitutions.get(0).isVariableSet(indexTerm));
	}

	public void cautiousInstantiateSatisfiedBasicLiteral() {

	}

	public void cautiousInstantiateUnsatisfiedBasicLiteral() {

	}
}
