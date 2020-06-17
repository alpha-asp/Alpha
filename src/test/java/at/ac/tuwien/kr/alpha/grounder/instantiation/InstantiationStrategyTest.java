package at.ac.tuwien.kr.alpha.grounder.instantiation;

import org.junit.Assert;
import org.junit.Test;

import at.ac.tuwien.kr.alpha.common.Predicate;
import at.ac.tuwien.kr.alpha.common.atoms.BasicAtom;
import at.ac.tuwien.kr.alpha.common.atoms.BasicLiteral;
import at.ac.tuwien.kr.alpha.common.atoms.Literal;
import at.ac.tuwien.kr.alpha.common.terms.ConstantTerm;
import at.ac.tuwien.kr.alpha.grounder.WorkingMemory;
import at.ac.tuwien.kr.alpha.grounder.instantiation.DefaultLazyGroundingInstantiationStrategy.AssignmentStatus;

public class InstantiationStrategyTest {

	@Test
	public void cautiousInstantiationAcceptLiteral() {
		Predicate p = Predicate.getInstance("p", 1);
		WorkingMemory workingMemory = new WorkingMemory();
		workingMemory.initialize(p);
		workingMemory.addInstance(new BasicAtom(p, ConstantTerm.getSymbolicInstance("a")), true);
		InstantiationStrategy strategy = new CautiousInstantiationStrategy(workingMemory);
		Literal positiveAcceptedLiteral = new BasicLiteral(
				new BasicAtom(p, ConstantTerm.getSymbolicInstance("a")), true);
		Assert.assertEquals(AssignmentStatus.TRUE, strategy.getTruthForGroundLiteral(positiveAcceptedLiteral));
		Literal negativeAcceptedLiteral = new BasicLiteral(
				new BasicAtom(p, ConstantTerm.getSymbolicInstance("b")), false);
		Assert.assertEquals(AssignmentStatus.TRUE, strategy.getTruthForGroundLiteral(negativeAcceptedLiteral));
	}

	@Test
	public void cautiousInstantiationRejectLiteral() {
		Predicate p = Predicate.getInstance("p", 1);
		WorkingMemory workingMemory = new WorkingMemory();
		workingMemory.initialize(p);
		workingMemory.addInstance(new BasicAtom(p, ConstantTerm.getSymbolicInstance("a")), true);
		InstantiationStrategy strategy = new CautiousInstantiationStrategy(workingMemory);
		Literal positiveRejectedLiteral = new BasicLiteral(
				new BasicAtom(p, ConstantTerm.getSymbolicInstance("b")), true);
		Assert.assertEquals(AssignmentStatus.FALSE, strategy.getTruthForGroundLiteral(positiveRejectedLiteral));
		Literal negativeRejectedLiteral = new BasicLiteral(
				new BasicAtom(p, ConstantTerm.getSymbolicInstance("a")), false);
		Assert.assertEquals(AssignmentStatus.FALSE, strategy.getTruthForGroundLiteral(negativeRejectedLiteral));
	}

}
