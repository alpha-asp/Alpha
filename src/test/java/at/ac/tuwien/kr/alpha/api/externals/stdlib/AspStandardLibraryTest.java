package at.ac.tuwien.kr.alpha.api.externals.stdlib;

import java.util.List;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

import at.ac.tuwien.kr.alpha.common.terms.ConstantTerm;

public class AspStandardLibraryTest {

	@Test
	public void parseDateTime1() {
		Set<List<ConstantTerm<Integer>>> dtSubstitution = 
				AspStandardLibrary.stdlib_datetime_parse("20.05.2020 01:19:13", "dd.MM.yyyy HH:mm:ss");
		Assert.assertEquals(1, dtSubstitution.size());
		List<ConstantTerm<Integer>> dtTerms = dtSubstitution.stream().findFirst().get();
		Assert.assertEquals(6, dtTerms.size());
		Assert.assertEquals(ConstantTerm.getInstance(2020), dtTerms.get(0));
		Assert.assertEquals(ConstantTerm.getInstance(5), dtTerms.get(1));
		Assert.assertEquals(ConstantTerm.getInstance(20), dtTerms.get(2));
		Assert.assertEquals(ConstantTerm.getInstance(1), dtTerms.get(3));
		Assert.assertEquals(ConstantTerm.getInstance(19), dtTerms.get(4));
		Assert.assertEquals(ConstantTerm.getInstance(13), dtTerms.get(5));
	}

	@Test
	public void parseDateTime2() {
		Set<List<ConstantTerm<Integer>>> dtSubstitution = 
				AspStandardLibrary.stdlib_datetime_parse("07/2123/18 22/37/01", "MM/yyyy/dd HH/mm/ss");
		Assert.assertEquals(1, dtSubstitution.size());
		List<ConstantTerm<Integer>> dtTerms = dtSubstitution.stream().findFirst().get();
		Assert.assertEquals(6, dtTerms.size());
		Assert.assertEquals(ConstantTerm.getInstance(2123), dtTerms.get(0));
		Assert.assertEquals(ConstantTerm.getInstance(7), dtTerms.get(1));
		Assert.assertEquals(ConstantTerm.getInstance(18), dtTerms.get(2));
		Assert.assertEquals(ConstantTerm.getInstance(22), dtTerms.get(3));
		Assert.assertEquals(ConstantTerm.getInstance(37), dtTerms.get(4));
		Assert.assertEquals(ConstantTerm.getInstance(1), dtTerms.get(5));
	}

	@Test
	public void parseDateTime3() {
		Set<List<ConstantTerm<Integer>>> dtSubstitution = 
				AspStandardLibrary.stdlib_datetime_parse("\"03,12,2019\", \"11:00:00\"", "\"dd,MM,yyyy\", \"HH:mm:ss\"");
		Assert.assertEquals(1, dtSubstitution.size());
		List<ConstantTerm<Integer>> dtTerms = dtSubstitution.stream().findFirst().get();
		Assert.assertEquals(6, dtTerms.size());
		Assert.assertEquals(ConstantTerm.getInstance(2019), dtTerms.get(0));
		Assert.assertEquals(ConstantTerm.getInstance(12), dtTerms.get(1));
		Assert.assertEquals(ConstantTerm.getInstance(3), dtTerms.get(2));
		Assert.assertEquals(ConstantTerm.getInstance(11), dtTerms.get(3));
		Assert.assertEquals(ConstantTerm.getInstance(0), dtTerms.get(4));
		Assert.assertEquals(ConstantTerm.getInstance(0), dtTerms.get(5));
	}
	
	@Test
	public void datetimeBefore() {
		Assert.assertTrue(AspStandardLibrary.stdlib_datetime_is_before(1990, 2, 14, 15, 16, 17, 1990, 3, 1, 0, 59, 1));
		Assert.assertFalse(AspStandardLibrary.stdlib_datetime_is_before(2015, 5, 13, 12, 1, 33, 2003, 1, 1, 0, 0, 1));
		Assert.assertFalse(AspStandardLibrary.stdlib_datetime_is_before(2022, 2, 22, 22, 22, 22, 2022, 2, 22, 22, 22, 22));
	}

}
