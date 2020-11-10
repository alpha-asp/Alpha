package at.ac.tuwien.kr.alpha.grounder.transformation;

import org.junit.Assert;
import org.junit.Test;

import java.util.List;
import java.util.stream.Collectors;

import at.ac.tuwien.kr.alpha.api.Alpha;
import at.ac.tuwien.kr.alpha.common.AnswerSet;
import at.ac.tuwien.kr.alpha.common.Predicate;
import at.ac.tuwien.kr.alpha.common.atoms.BasicAtom;
import at.ac.tuwien.kr.alpha.common.program.InputProgram;
import at.ac.tuwien.kr.alpha.common.program.NormalProgram;
import at.ac.tuwien.kr.alpha.common.terms.ConstantTerm;

public class AggregateRewritingTest {

	//@formatter:off	
	private static final String CNT_LE1_ASP = 
			"thing(75..76)."
			+ "candidate(2..4)."
			+ "cnt_le(N) :- N <= #count{X : thing(X)}, candidate(N).";
	private static final String CNT_EQ1_ASP =
			"thing(4..6)."
			+ "cnt_things(N) :- N = #count{X : thing(X)}.";
	//@formatter:on

	@Test
	public void countLeSimple() {
		Alpha alpha = new Alpha();
		InputProgram input = alpha.readProgramString(CNT_LE1_ASP);
		NormalProgram normalized = alpha.normalizeProgram(input);
		List<AnswerSet> answerSets = alpha.solve(normalized, (p) -> true).collect(Collectors.toList());
		Assert.assertEquals(1, answerSets.size());
		AnswerSet answerSet = answerSets.get(0);
		Predicate thing = Predicate.getInstance("thing", 1);
		Predicate candidate = Predicate.getInstance("candidate", 1);
		Predicate cntLe = Predicate.getInstance("cnt_le", 1);

		Assert.assertTrue(answerSet.getPredicateInstances(thing).contains(new BasicAtom(thing, ConstantTerm.getInstance(75))));
		Assert.assertTrue(answerSet.getPredicateInstances(thing).contains(new BasicAtom(thing, ConstantTerm.getInstance(76))));

		Assert.assertTrue(answerSet.getPredicateInstances(candidate).contains(new BasicAtom(candidate, ConstantTerm.getInstance(2))));
		Assert.assertTrue(answerSet.getPredicateInstances(candidate).contains(new BasicAtom(candidate, ConstantTerm.getInstance(3))));
		Assert.assertTrue(answerSet.getPredicateInstances(candidate).contains(new BasicAtom(candidate, ConstantTerm.getInstance(4))));

		Assert.assertTrue(answerSet.getPredicates().contains(cntLe));
		Assert.assertTrue(answerSet.getPredicateInstances(cntLe).contains(new BasicAtom(cntLe, ConstantTerm.getInstance(2))));
	}

	@Test
	public void countEqSimple() {
		Alpha alpha = new Alpha();
		alpha.getConfig().setEvaluateStratifiedPart(false);
		InputProgram input = alpha.readProgramString(CNT_EQ1_ASP);
		NormalProgram normalized = alpha.normalizeProgram(input);
		//System.out.println(normalized);
		List<AnswerSet> answerSets = alpha.solve(normalized, (p) -> true).collect(Collectors.toList());
		Assert.assertEquals(1, answerSets.size());
		AnswerSet answerSet = answerSets.get(0);
		Predicate thing = Predicate.getInstance("thing", 1);
		Predicate cntThings = Predicate.getInstance("cnt_things", 1);

		//System.out.println(new SimpleAnswerSetFormatter("\n").format(answerSet));
		
		Assert.assertTrue(answerSet.getPredicateInstances(thing).contains(new BasicAtom(thing, ConstantTerm.getInstance(4))));
		Assert.assertTrue(answerSet.getPredicateInstances(thing).contains(new BasicAtom(thing, ConstantTerm.getInstance(5))));
		Assert.assertTrue(answerSet.getPredicateInstances(thing).contains(new BasicAtom(thing, ConstantTerm.getInstance(6))));

		Assert.assertTrue(answerSet.getPredicates().contains(cntThings));
		Assert.assertTrue(answerSet.getPredicateInstances(cntThings).contains(new BasicAtom(cntThings, ConstantTerm.getInstance(3))));
	}

}
