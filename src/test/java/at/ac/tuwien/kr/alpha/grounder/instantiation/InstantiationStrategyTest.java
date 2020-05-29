package at.ac.tuwien.kr.alpha.grounder.instantiation;

import org.junit.Assert;
import org.junit.Test;

import at.ac.tuwien.kr.alpha.common.Predicate;
import at.ac.tuwien.kr.alpha.common.atoms.BasicAtom;
import at.ac.tuwien.kr.alpha.common.atoms.BasicLiteral;
import at.ac.tuwien.kr.alpha.common.atoms.Literal;
import at.ac.tuwien.kr.alpha.common.terms.ConstantTerm;
import at.ac.tuwien.kr.alpha.grounder.IndexedInstanceStorage;
import at.ac.tuwien.kr.alpha.grounder.Instance;

public class InstantiationStrategyTest {

	@Test
	public void cautiousInstantiationAcceptLiteral() {
		InstantiationStrategy strategy = new CautiousInstantiationStrategy();
		Predicate p = Predicate.getInstance("p", 1);
		Literal positiveAcceptedLiteral = new BasicLiteral(
				new BasicAtom(p, ConstantTerm.getSymbolicInstance("a")), true);
		IndexedInstanceStorage instances = new IndexedInstanceStorage(p, true);
		instances.addInstance(new Instance(ConstantTerm.getSymbolicInstance("a")));
		InstanceStorageView storageView = new BasicInstanceStorageView(instances);
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
		IndexedInstanceStorage instances = new IndexedInstanceStorage(p, true);
		instances.addInstance(new Instance(ConstantTerm.getSymbolicInstance("a")));
		InstanceStorageView storageView = new BasicInstanceStorageView(instances);
		Assert.assertFalse(strategy.acceptSubstitutedLiteral(positiveRejectedLiteral, storageView));
		Literal negativeRejectedLiteral = new BasicLiteral(
				new BasicAtom(p, ConstantTerm.getSymbolicInstance("a")), false);
		Assert.assertFalse(strategy.acceptSubstitutedLiteral(negativeRejectedLiteral, storageView));
	}

}
