package at.ac.tuwien.kr.alpha.api.answersets;

import org.junit.Assert;
import org.junit.Test;

import at.ac.tuwien.kr.alpha.common.Predicate;
import at.ac.tuwien.kr.alpha.common.atoms.Atom;
import at.ac.tuwien.kr.alpha.common.atoms.BasicAtom;
import at.ac.tuwien.kr.alpha.common.terms.ConstantTerm;

public class AtomQueryTest {

	@Test
	public void singleAtomMatchPredicate() {
		Predicate matchingPredicate = Predicate.getInstance("this_one", 2);
		Predicate otherPredicate = Predicate.getInstance("that_one", 3);
		Atom matchingAtom = new BasicAtom(matchingPredicate, ConstantTerm.getInstance("bla"), ConstantTerm.getInstance("blubb"));
		Atom nonMatchingAtom = new BasicAtom(otherPredicate, ConstantTerm.getInstance("foo"), ConstantTerm.getInstance("bar"), ConstantTerm.getInstance("baz"));
		AtomQuery query = AtomQuery.forPredicate(matchingPredicate);
		Assert.assertTrue(query.test(matchingAtom));
		Assert.assertFalse(query.test(nonMatchingAtom));
	}

	@Test
	public void singleAtomMatchString() {
		Predicate p = Predicate.getInstance("p", 2);
		Atom matchingAtom = new BasicAtom(p, ConstantTerm.getInstance("a string constant"), ConstantTerm.getInstance("blaaa"));
		Atom nonMatchingAtom = new BasicAtom(p, ConstantTerm.getInstance("another string constant"), ConstantTerm.getInstance("blubb"));
		AtomQuery query = AtomQuery.forPredicate(p).withStringEquals(0, "a string constant");
		Assert.assertTrue(query.test(matchingAtom));
		Assert.assertFalse(query.test(nonMatchingAtom));
	}

	@Test
	public void singleAtomMatchConstantSymbol() {
		Predicate p = Predicate.getInstance("p", 1);
		Atom matchingAtom = new BasicAtom(p, ConstantTerm.getSymbolicInstance("symbol")); // plain constant in ASP
		Atom nonMatchingAtom = new BasicAtom(p, ConstantTerm.getInstance("symbol")); // quoted string in ASP
		AtomQuery query = AtomQuery.forPredicate(p).withConstantSymbolEquals(0, "symbol");
		Assert.assertTrue(query.test(matchingAtom));
		Assert.assertFalse(query.test(nonMatchingAtom));
	}

}
