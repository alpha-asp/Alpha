package at.ac.tuwien.kr.alpha;

import at.ac.tuwien.kr.alpha.common.AnswerSet;
import at.ac.tuwien.kr.alpha.common.AnswerSetBuilder;
import at.ac.tuwien.kr.alpha.common.Program;
import at.ac.tuwien.kr.alpha.common.Rule;
import at.ac.tuwien.kr.alpha.common.atoms.BasicAtom;
import at.ac.tuwien.kr.alpha.common.predicates.BasicPredicate;
import at.ac.tuwien.kr.alpha.common.predicates.ExternalEvaluable;
import at.ac.tuwien.kr.alpha.common.terms.ConstantTerm;
import org.junit.Test;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

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
		Set<AnswerSet> expected = new HashSet<>(Collections.singletonList(new AnswerSetBuilder().predicate("a").build()));
		assertEquals(expected, actual);
	}

	@Test(expected = IllegalArgumentException.class)
	public void withExternalTypeConflict() throws Exception {
		Alpha system = new Alpha();
		system.register(this.getClass().getMethod("isFoo", Integer.class));
		Set<AnswerSet> actual = system.solve("a :- &isFoo(\"adsfnfdsf\").").collect(Collectors.toSet());
		Set<AnswerSet> expected = new HashSet<>(Collections.singletonList(new AnswerSetBuilder().predicate("a").build()));
		assertEquals(expected, actual);
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
			Collections.singletonList(
				new BasicAtom(new ExternalEvaluable(this.getClass().getMethod("thinger", Thingy.class)), ConstantTerm.getInstance(thingy))
			)
		);

		Alpha system = new Alpha();

		system.setProgram(new Program(
			Collections.singletonList(rule),
			Collections.emptyList()
		));

		Set<AnswerSet> actual = system.solve().collect(Collectors.toSet());
		Set<AnswerSet> expected = new HashSet<>(Collections.singletonList(new AnswerSetBuilder().predicate("p").instance("x").build()));
		assertEquals(expected, actual);
	}

	@Test
	public void withExternalViaAnnotation() throws Exception {
		Alpha system = new Alpha();
		system.scan(this.getClass().getPackage().getName());
		Set<AnswerSet> actual = system.solve("a :- &isOne(1).").collect(Collectors.toSet());
		Set<AnswerSet> expected = new HashSet<>(Collections.singletonList(new AnswerSetBuilder().predicate("a").build()));
		assertEquals(expected, actual);
	}

	@Test
	public void withNativeExternal() throws Exception {
		Alpha system = new Alpha();
		system.register("isTwo", t -> t.getObject().toString().equals("2"));

		Set<AnswerSet> actual = system.solve("a :- &isTwo(2).").collect(Collectors.toSet());
		Set<AnswerSet> expected = new HashSet<>(Collections.singletonList(new AnswerSetBuilder().predicate("a").build()));
		assertEquals(expected, actual);
	}

	@Test
	public void withExternalInvocationCounted1() throws Exception {
		Alpha system = new Alpha();
		system.register(this.getClass().getMethod("isOne", int.class));
		int before = invocations;
		Set<AnswerSet> actual = system.solve("a :- &isOne(1), &isOne(1).").collect(Collectors.toSet());
		int after = invocations;

		assertEquals(1, after - before);

		Set<AnswerSet> expected = new HashSet<>(Collections.singletonList(new AnswerSetBuilder().predicate("a").build()));
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

		Set<AnswerSet> expected = new HashSet<>(Collections.singletonList(new AnswerSetBuilder().predicate("a").build()));
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

		Set<AnswerSet> expected = new HashSet<>(Collections.singletonList(new AnswerSetBuilder().predicate("a").build()));
		assertEquals(expected, actual);
	}

	@Test
	public void basicUsage() throws Exception {
		Alpha system = new Alpha();
		Set<AnswerSet> actual = system.solve("a.").collect(Collectors.toSet());
		Set<AnswerSet> expected = new HashSet<>(Collections.singletonList(new AnswerSetBuilder().predicate("a").build()));
		assertEquals(expected, actual);
	}
}