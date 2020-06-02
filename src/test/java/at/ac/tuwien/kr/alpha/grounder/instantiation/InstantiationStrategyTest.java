package at.ac.tuwien.kr.alpha.grounder.instantiation;

import org.junit.Assert;
import org.junit.Test;

import at.ac.tuwien.kr.alpha.common.Predicate;
import at.ac.tuwien.kr.alpha.common.atoms.BasicAtom;
import at.ac.tuwien.kr.alpha.common.atoms.BasicLiteral;
import at.ac.tuwien.kr.alpha.common.atoms.Literal;
import at.ac.tuwien.kr.alpha.common.terms.ConstantTerm;
import at.ac.tuwien.kr.alpha.grounder.WorkingMemory;

public class InstantiationStrategyTest {

	@Test
	public void cautiousInstantiationAcceptLiteral() {
		InstantiationStrategy strategy = new CautiousInstantiationStrategy();
		Predicate p = Predicate.getInstance("p", 1);
		Literal positiveAcceptedLiteral = new BasicLiteral(
				new BasicAtom(p, ConstantTerm.getSymbolicInstance("a")), true);
		WorkingMemory workingMemory = new WorkingMemory();
		workingMemory.initialize(p);
		workingMemory.addInstance(new BasicAtom(p, ConstantTerm.getSymbolicInstance("a")), true);
		InstanceStorageView storageView = new BasicInstanceStorageView(workingMemory);
		Assert.assertTrue(strategy.acceptSubstitutedLiteral(positiveAcceptedLiteral, storageView));
		Literal negativeAcceptedLiteral = new BasicLiteral(
				new BasicAtom(p, ConstantTerm.getSymbolicInstance("b")), false);
		Assert.assertTrue(strategy.acceptSubstitutedLiteral(negativeAcceptedLiteral, storageView));
	}

	@Test
	public void cautiousInstantiationRejectLiteral() {
		InstantiationStrategy strategy = new CautiousInstantiationStrategy();
		Predicate p = Predicate.getInstance("p", 1);
		Literal positiveRejectedLiteral = new BasicLiteral(
				new BasicAtom(p, ConstantTerm.getSymbolicInstance("b")), true);
		WorkingMemory workingMemory = new WorkingMemory();
		workingMemory.initialize(p);
		workingMemory.addInstance(new BasicAtom(p, ConstantTerm.getSymbolicInstance("a")), true);
		InstanceStorageView storageView = new BasicInstanceStorageView(workingMemory);
		Assert.assertFalse(strategy.acceptSubstitutedLiteral(positiveRejectedLiteral, storageView));
		Literal negativeRejectedLiteral = new BasicLiteral(
				new BasicAtom(p, ConstantTerm.getSymbolicInstance("a")), false);
		Assert.assertFalse(strategy.acceptSubstitutedLiteral(negativeRejectedLiteral, storageView));
	}

}
