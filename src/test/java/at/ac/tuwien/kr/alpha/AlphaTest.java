package at.ac.tuwien.kr.alpha;

import at.ac.tuwien.kr.alpha.common.AnswerSet;
import at.ac.tuwien.kr.alpha.common.AnswerSetBuilder;
import at.ac.tuwien.kr.alpha.common.Program;
import at.ac.tuwien.kr.alpha.common.Rule;
import at.ac.tuwien.kr.alpha.common.atoms.BasicAtom;
import at.ac.tuwien.kr.alpha.common.atoms.ExternalAtom;
import at.ac.tuwien.kr.alpha.common.predicates.ExternalMethodPredicate;
import at.ac.tuwien.kr.alpha.common.predicates.Predicate;
import at.ac.tuwien.kr.alpha.common.terms.ConstantTerm;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;
import static java.util.Collections.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class AlphaTest {
	private static int invocations;

	@at.ac.tuwien.kr.alpha.Predicate
	public static boolean isOne(int term) {
		invocations++;
		return term == 1;
	}

	@at.ac.tuwien.kr.alpha.Predicate
	public static boolean isFoo(Integer a) {
		return a == 0xF00;
	}

	@at.ac.tuwien.kr.alpha.Predicate
	public static boolean thinger(Thingy thingy) {
		return true;
	}

	@Test
	void withExternal() throws Exception {
		Alpha system = new Alpha();
		system.register(this.getClass().getMethod("isOne", int.class));
		Set<AnswerSet> actual = system.solve("a :- &isOne[1].").collect(Collectors.toSet());
		Set<AnswerSet> expected = new HashSet<>(singletonList(new AnswerSetBuilder().predicate("a").build()));
		assertEquals(expected, actual);
	}

	@Test
	void addsFacts() throws Exception {
		Alpha system = new Alpha();
		Thingy a = new Thingy();
		Thingy b = new Thingy();
		List<Thingy> things = asList(a, b);
		system.addFacts(things);
		Set<AnswerSet> actual = system.solve().collect(Collectors.toSet());
		Set<AnswerSet> expected = new HashSet<>(singletonList(new AnswerSetBuilder().predicate("thingy").instance(a).instance(b).build()));
		assertEquals(expected, actual);
	}

	@Test
	void withExternalTypeConflict() throws Exception {
		assertThrows(IllegalArgumentException.class, () -> {
			Alpha system = new Alpha();
			system.register(this.getClass().getMethod("isFoo", Integer.class));
			Set<AnswerSet> actual = system.solve("a :- &isFoo[\"adsfnfdsf\"].").collect(Collectors.toSet());
			Set<AnswerSet> expected = new HashSet<>(singletonList(new AnswerSetBuilder().predicate("a").build()));
			assertEquals(expected, actual);
		});
	}

	@Test
	void smallGraph() throws IOException {
		Alpha system = new Alpha();
		system.register("connected", (Integer a, Integer b) -> (a == 1 && b == 2) || (b == 2 || b == 3));

		Set<AnswerSet> answerSets = system.solve("node(1). node(2). node(3). a :- &connected[1,2].").collect(Collectors.toSet());
	}

	@Test
	void smallGraphWithWrongType() {
		assertThrows(IllegalArgumentException.class, () -> {
			Alpha system = new Alpha();
			system.register("connected", (Integer a, Integer b) -> (a == 1 && b == 2) || (b == 2 || b == 3));

			system.solve("a :- &connected[\"hello\",2].").collect(Collectors.toSet());
		});
	}

	public static Set<List<ConstantTerm<Integer>>> neighbors(int node) {
		if (node == 1) {
			return new HashSet<>(asList(
				singletonList(ConstantTerm.getInstance(2)),
				singletonList(ConstantTerm.getInstance(3))
			));
		}
		return emptySet();
	}

	public static Set<List<ConstantTerm<Integer>>> coolNode(int node) {
		if (node == 1) {
			return singleton(emptyList());
		}
		return emptySet();
	}

	@Test
	void smallGraphNoNeighbors() throws Exception {
		Alpha system = new Alpha();
		system.registerBinding(this.getClass().getMethod("neighbors", int.class));

		Set<AnswerSet> expected = AnswerSetsParser.parse("{ noNeighbors(2) }");
		Set<AnswerSet> actual = system.solve("noNeighbors(2) :- not &neighbors[2].").collect(Collectors.toSet());
		assertEquals(expected, actual);
	}

	@Test
	void smallGraphCoolNode() throws Exception {
		Alpha system = new Alpha();
		system.registerBinding(this.getClass().getMethod("coolNode", int.class));

		Set<AnswerSet> actual = system.solve("node(1..2). in(X) :- node(X), &coolNode[X].").collect(Collectors.toSet());
		Set<AnswerSet> expected = AnswerSetsParser.parse("{ in(1), node(1), node(2) }");
		assertEquals(expected, actual);
	}

	@Test
	void smallGraphSingleNeighbor() throws Exception {
		Alpha system = new Alpha();
		system.registerBinding(this.getClass().getMethod("neighbors", int.class));

		Set<AnswerSet> expected = AnswerSetsParser.parse("{ in(1,2), in(1,3), node(1), node(2), node(3) }");
		Set<AnswerSet> actual = system.solve("node(1..3). in(1,X) :- &neighbors[1](X), node(X).").collect(Collectors.toSet());
		assertEquals(expected, actual);
	}

	@Test
	void smallGraphSingleNeighborNoTerm() throws Exception {
		Alpha system = new Alpha();
		system.registerBinding(this.getClass().getMethod("neighbors", int.class));

		Set<AnswerSet> expected = AnswerSetsParser.parse("{ success }");
		Set<AnswerSet> actual = system.solve("success :- &neighbors[1], not &neighbors[2].").collect(Collectors.toSet());
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
	void withExternalSubtype() throws Exception {
		SubThingy thingy = new SubThingy();

		Rule rule = new Rule(
			new BasicAtom(new Predicate("p", 1), ConstantTerm.getInstance("x")),
			singletonList(
				new ExternalAtom(
					new ExternalMethodPredicate(this.getClass().getMethod("thinger", Thingy.class)),
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
	void withExternalViaAnnotation() throws Exception {
		Alpha system = new Alpha();
		system.scan(this.getClass().getPackage().getName());
		Set<AnswerSet> actual = system.solve("a :- &isOne[1].").collect(Collectors.toSet());
		Set<AnswerSet> expected = new HashSet<>(singletonList(new AnswerSetBuilder().predicate("a").build()));
		assertEquals(expected, actual);
	}

	@Test
	void withNativeExternal() throws Exception {
		Alpha system = new Alpha();
		system.register("isTwo", (Integer t) -> t == 2);

		Set<AnswerSet> actual = system.solve("a :- &isTwo[2].").collect(Collectors.toSet());
		Set<AnswerSet> expected = new HashSet<>(singletonList(new AnswerSetBuilder().predicate("a").build()));
		assertEquals(expected, actual);
	}

	@Test
	void withExternalInvocationCounted1() throws Exception {
		Alpha system = new Alpha();
		system.register(this.getClass().getMethod("isOne", int.class));
		int before = invocations;
		Set<AnswerSet> actual = system.solve("a :- &isOne[1], &isOne[1].").collect(Collectors.toSet());
		int after = invocations;

		assertEquals(2, after - before);

		Set<AnswerSet> expected = new HashSet<>(singletonList(new AnswerSetBuilder().predicate("a").build()));
		assertEquals(expected, actual);
	}

	@Test
	void withExternalInvocationCounted2() throws Exception {
		Alpha system = new Alpha();
		system.register(this.getClass().getMethod("isOne", int.class));
		int before = invocations;
		Set<AnswerSet> actual = system.solve("a. b :- &isOne[1], &isOne[2].").collect(Collectors.toSet());
		int after = invocations;

		assertEquals(2, after - before);

		Set<AnswerSet> expected = new HashSet<>(singletonList(new AnswerSetBuilder().predicate("a").build()));
		assertEquals(expected, actual);
	}

	@Test
	void withExternalInvocationCounted3() throws Exception {
		Alpha system = new Alpha();
		system.register(this.getClass().getMethod("isOne", int.class));
		int before = invocations;
		Set<AnswerSet> actual = system.solve("a :- &isOne[1], not &isOne[2].").collect(Collectors.toSet());
		int after = invocations;

		assertEquals(1, after - before);

		Set<AnswerSet> expected = new HashSet<>(singletonList(new AnswerSetBuilder().predicate("a").build()));
		assertEquals(expected, actual);
	}

	@Test
	void basicUsage() throws Exception {
		Alpha system = new Alpha();
		Set<AnswerSet> actual = system.solve("a.").collect(Collectors.toSet());
		Set<AnswerSet> expected = new HashSet<>(singletonList(new AnswerSetBuilder().predicate("a").build()));
		assertEquals(expected, actual);
	}
}