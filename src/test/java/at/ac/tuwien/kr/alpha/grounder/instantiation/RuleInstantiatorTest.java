package at.ac.tuwien.kr.alpha.grounder.instantiation;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import at.ac.tuwien.kr.alpha.common.ComparisonOperator;
import at.ac.tuwien.kr.alpha.common.atoms.ComparisonAtom;
import at.ac.tuwien.kr.alpha.common.atoms.ComparisonLiteral;
import at.ac.tuwien.kr.alpha.common.atoms.Literal;
import at.ac.tuwien.kr.alpha.common.terms.ConstantTerm;
import at.ac.tuwien.kr.alpha.common.terms.VariableTerm;
import at.ac.tuwien.kr.alpha.grounder.Substitution;

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

	public void instantiateUnsatisfiedFixedInterpretationLiteral() {

	}

	public void instantiateEnumLiteral() {

	}

	public void cautiousInstantiateSatisfiedBasicLiteral() {

	}

	public void cautiousInstantiatUnsatisfiedBasicLiteral() {

	}
}
