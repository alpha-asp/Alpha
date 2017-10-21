package at.ac.tuwien.kr.alpha;

import at.ac.tuwien.kr.alpha.common.AnswerSet;
import at.ac.tuwien.kr.alpha.common.AnswerSetBuilder;
import at.ac.tuwien.kr.alpha.common.Program;
import at.ac.tuwien.kr.alpha.common.Rule;
import at.ac.tuwien.kr.alpha.common.atoms.BasicAtom;
import at.ac.tuwien.kr.alpha.common.atoms.ExternalAtom;
import at.ac.tuwien.kr.alpha.common.predicates.BasicPredicate;
import at.ac.tuwien.kr.alpha.common.predicates.ExternalSimpleFixedEvaluable;
import at.ac.tuwien.kr.alpha.common.terms.ConstantTerm;
import org.junit.Test;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;

public class AlphaTest {
	private static int invocations;

	@Predicate
	public static boolean isOne(int term) {
		invocations++;
		return term == 1;
	}

	@Predicate
	public static boolean isFoo(Integer a) {
		return a == 0xF00;
	}

	@Predicate
	public static boolean thinger(Thingy thingy) {
		return true;
	}

	@Test
	public void withExternal() throws Exception {
		Alpha system = new Alpha();
		system.register(this.getClass().getMethod("isOne", int.class));
		Set<AnswerSet> actual = system.solve("a :- &isOne(1).").collect(Collectors.toSet());
		Set<AnswerSet> expected = new HashSet<>(singletonList(new AnswerSetBuilder().predicate("a").build()));
		assertEquals(expected, actual);
	}

	@Test
	public void addsFacts() throws Exception {
		Alpha system = new Alpha();
		Thingy a = new Thingy();
		Thingy b = new Thingy();
		List<Thingy> things = Arrays.asList(a, b);
		system.addFacts(things);
		Set<AnswerSet> actual = system.solve().collect(Collectors.toSet());
		Set<AnswerSet> expected = new HashSet<>(singletonList(new AnswerSetBuilder().predicate("thingy").instance(a).instance(b).build()));
		assertEquals(expected, actual);
	}

	@Test(expected = IllegalArgumentException.class)
	public void withExternalTypeConflict() throws Exception {
		Alpha system = new Alpha();
		system.register(this.getClass().getMethod("isFoo", Integer.class));
		Set<AnswerSet> actual = system.solve("a :- &isFoo(\"adsfnfdsf\").").collect(Collectors.toSet());
		Set<AnswerSet> expected = new HashSet<>(singletonList(new AnswerSetBuilder().predicate("a").build()));
		assertEquals(expected, actual);
	}

	@Test
	public void smallGraph() throws Exception {
		Alpha system = new Alpha();
		system.register("connected", (Integer a, Integer b) -> {
			return (a == 1 && b == 2) || (b == 2 || b == 3);
		});

		Set<AnswerSet> answerSets = system.solve("node(1). node(2). node(3). a :- &connected(1,2).").collect(Collectors.toSet());
	}

	@Test(expected = IllegalArgumentException.class)
	public void smallGraphWithWrongType() throws Exception {
		Alpha system = new Alpha();
		system.register("connected", (Integer a, Integer b) -> {
			return (a == 1 && b == 2) || (b == 2 || b == 3);
		});

		system.solve("a :- &connected(\"hello\",2).").collect(Collectors.toSet());
	}

	private static class Thingy implements Comparable<Thingy> {
		@Override
		public String toString() {
			return "thingy";
		}

		@Override
		public int compareTo(Thingy o) {
			return 0;
		}
	}

	private static class SubThingy extends Thingy {}

	@Test
	public void withExternalSubtype() throws Exception {
		SubThingy thingy = new SubThingy();

		Rule rule = new Rule(
			new BasicAtom(new BasicPredicate("p", 1), ConstantTerm.getInstance("x")),
			singletonList(
				new ExternalAtom(
					new ExternalSimpleFixedEvaluable(this.getClass().getMethod("thinger", Thingy.class)),
					singletonList(ConstantTerm.getInstance(thingy)),
					emptyList(),
					false
				)
			)
		);

		Alpha system = new Alpha();

		system.setProgram(new Program(
			singletonList(rule),
			emptyList()
		));

		Set<AnswerSet> actual = system.solve().collect(Collectors.toSet());
		Set<AnswerSet> expected = new HashSet<>(singletonList(new AnswerSetBuilder().predicate("p").instance("x").build()));
		assertEquals(expected, actual);
	}

	@Test
	public void withExternalViaAnnotation() throws Exception {
		Alpha system = new Alpha();
		system.scan(this.getClass().getPackage().getName());
		Set<AnswerSet> actual = system.solve("a :- &isOne(1).").collect(Collectors.toSet());
		Set<AnswerSet> expected = new HashSet<>(singletonList(new AnswerSetBuilder().predicate("a").build()));
		assertEquals(expected, actual);
	}

	@Test
	public void withNativeExternal() throws Exception {
		Alpha system = new Alpha();
		system.register("isTwo", (Integer t) -> t == 2);

		Set<AnswerSet> actual = system.solve("a :- &isTwo(2).").collect(Collectors.toSet());
		Set<AnswerSet> expected = new HashSet<>(singletonList(new AnswerSetBuilder().predicate("a").build()));
		assertEquals(expected, actual);
	}

	@Test
	public void withExternalInvocationCounted1() throws Exception {
		Alpha system = new Alpha();
		system.register(this.getClass().getMethod("isOne", int.class));
		int before = invocations;
		Set<AnswerSet> actual = system.solve("a :- &isOne(1), &isOne(1).").collect(Collectors.toSet());
		int after = invocations;

		assertEquals(2, after - before);

		Set<AnswerSet> expected = new HashSet<>(singletonList(new AnswerSetBuilder().predicate("a").build()));
		assertEquals(expected, actual);
	}

	@Test
	public void withExternalInvocationCounted2() throws Exception {
		Alpha system = new Alpha();
		system.register(this.getClass().getMethod("isOne", int.class));
		int before = invocations;
		Set<AnswerSet> actual = system.solve("a. b :- &isOne(1), &isOne(2).").collect(Collectors.toSet());
		int after = invocations;

		assertEquals(2, after - before);

		Set<AnswerSet> expected = new HashSet<>(singletonList(new AnswerSetBuilder().predicate("a").build()));
		assertEquals(expected, actual);
	}

	@Test
	public void withExternalInvocationCounted3() throws Exception {
		Alpha system = new Alpha();
		system.register(this.getClass().getMethod("isOne", int.class));
		int before = invocations;
		Set<AnswerSet> actual = system.solve("a :- &isOne(1), not &isOne(2).").collect(Collectors.toSet());
		int after = invocations;

		assertEquals(1, after - before);

		Set<AnswerSet> expected = new HashSet<>(singletonList(new AnswerSetBuilder().predicate("a").build()));
		assertEquals(expected, actual);
	}

	@Test
	public void basicUsage() throws Exception {
		Alpha system = new Alpha();
		Set<AnswerSet> actual = system.solve("a.").collect(Collectors.toSet());
		Set<AnswerSet> expected = new HashSet<>(singletonList(new AnswerSetBuilder().predicate("a").build()));
		assertEquals(expected, actual);
	}
}