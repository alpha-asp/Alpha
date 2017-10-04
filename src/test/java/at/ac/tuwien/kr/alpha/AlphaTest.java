package at.ac.tuwien.kr.alpha;

import at.ac.tuwien.kr.alpha.common.AnswerSet;
import at.ac.tuwien.kr.alpha.common.BasicAnswerSet;
import at.ac.tuwien.kr.alpha.common.terms.ConstantTerm;
import org.junit.Test;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;

public class AlphaTest {
	private static int invocations = 0;

	@Predicate
	public static boolean isOne(String term) {
		invocations++;
		return term.equals("1");
	}

	@Test
	public void withExternal() throws Exception {
		Alpha system = new Alpha();
		system.register(this.getClass().getMethod("isOne", String.class));
		Set<AnswerSet> actual = system.solve("a :- &isOne(1).").collect(Collectors.toSet());
		Set<AnswerSet> expected = new HashSet<>(Collections.singletonList(new BasicAnswerSet.Builder().predicate("a").build()));
		assertEquals(expected, actual);
	}

	@Test
	public void withExternalViaAnnotation() throws Exception {
		Alpha system = new Alpha();
		system.scan(this.getClass().getPackage().getName());
		Set<AnswerSet> actual = system.solve("a :- &isOne(1).").collect(Collectors.toSet());
		Set<AnswerSet> expected = new HashSet<>(Collections.singletonList(new BasicAnswerSet.Builder().predicate("a").build()));
		assertEquals(expected, actual);
	}

	@Test
	public void withNativeExternal() throws Exception {
		Alpha system = new Alpha();
		system.register("isTwo", t -> t.getObject().toString().equals("2"));

		Set<AnswerSet> actual = system.solve("a :- &isTwo(2).").collect(Collectors.toSet());
		Set<AnswerSet> expected = new HashSet<>(Collections.singletonList(new BasicAnswerSet.Builder().predicate("a").build()));
		assertEquals(expected, actual);
	}

	@Test
	public void withExternalInvocationCounted1() throws Exception {
		Alpha system = new Alpha();
		system.register(this.getClass().getMethod("isOne", ConstantTerm.class));
		int before = invocations;
		Set<AnswerSet> actual = system.solve("a :- &isOne(1), &isOne(1).").collect(Collectors.toSet());
		int after = invocations;

		assertEquals(1, after - before);

		Set<AnswerSet> expected = new HashSet<>(Collections.singletonList(new BasicAnswerSet.Builder().predicate("a").build()));
		assertEquals(expected, actual);
	}

	@Test
	public void withExternalInvocationCounted2() throws Exception {
		Alpha system = new Alpha();
		system.register(this.getClass().getMethod("isOne", ConstantTerm.class));
		int before = invocations;
		Set<AnswerSet> actual = system.solve("a. b :- &isOne(1), &isOne(2).").collect(Collectors.toSet());
		int after = invocations;

		assertEquals(2, after - before);

		Set<AnswerSet> expected = new HashSet<>(Collections.singletonList(new BasicAnswerSet.Builder().predicate("a").build()));
		assertEquals(expected, actual);
	}

	@Test
	public void withExternalInvocationCounted3() throws Exception {
		Alpha system = new Alpha();
		system.register(this.getClass().getMethod("isOne", ConstantTerm.class));
		int before = invocations;
		Set<AnswerSet> actual = system.solve("a :- &isOne(1), not &isOne(2).").collect(Collectors.toSet());
		int after = invocations;

		assertEquals(1, after - before);

		Set<AnswerSet> expected = new HashSet<>(Collections.singletonList(new BasicAnswerSet.Builder().predicate("a").build()));
		assertEquals(expected, actual);
	}

	@Test
	public void basicUsage() throws Exception {
		Alpha system = new Alpha();
		Set<AnswerSet> actual = system.solve("a.").collect(Collectors.toSet());
		Set<AnswerSet> expected = new HashSet<>(Collections.singletonList(new BasicAnswerSet.Builder().predicate("a").build()));
		assertEquals(expected, actual);
	}
}