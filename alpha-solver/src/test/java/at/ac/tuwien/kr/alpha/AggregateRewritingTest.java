package at.ac.tuwien.kr.alpha;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import at.ac.tuwien.kr.alpha.api.Alpha;
import at.ac.tuwien.kr.alpha.api.AnswerSet;
import at.ac.tuwien.kr.alpha.api.impl.AlphaFactory;
import at.ac.tuwien.kr.alpha.api.programs.InputProgram;
import at.ac.tuwien.kr.alpha.api.programs.Predicate;
import at.ac.tuwien.kr.alpha.commons.Predicates;
import at.ac.tuwien.kr.alpha.commons.atoms.Atoms;
import at.ac.tuwien.kr.alpha.commons.terms.Terms;

// TODO This is a functional test and should not be run with standard unit tests
public class AggregateRewritingTest {

	//@formatter:off
	// Smoke-test case for "X <= #count{...}" aggregate
	private static final String CNT_LE1_ASP = 
			"thing(75..76)."
			+ "candidate(2..4)."
			+ "cnt_le(N) :- N <= #count{X : thing(X)}, candidate(N).";
	// Smoke-test case for "X = #count{...}" aggregate
	private static final String CNT_EQ1_ASP =
			"thing(4..6)."
			+ "cnt_things(N) :- N = #count{X : thing(X)}.";
	// Smoke-test case for non-binding min aggregate
	private static final String MIN_GT1_ASP =
			"thing(4). thing(7). thing(13). thing(3). "
			+ "acceptable(8). acceptable(10). acceptable(5). "
			+ "greater_min_acceptable(T) :- thing(T), T > #min{ A : acceptable(A) }.";
	// Smoke-test case for "X = #sum{...}" aggregate
	private static final String SUM_EQ1_ASP =
			"thing(2). thing(4). thing(6)."
			+ "sum_things(S) :- S = #sum{K : thing(K)}.";
	// Smoke-test case for "X = #sum{...}" aggregate
	private static final String SUM_LE1_ASP =
			"thing(2). thing(4). thing(6). bound(11)."
			+ "bound_le_sum(B) :- B <= #sum{K : thing(K)}, bound(B).";

	// Basic ASP representation of a triangular undirected graph, used across multiple test cases
	private static final String TEST_GRAPH_ASP = 
			"graph(g1)."
			+ "graph_undirected(g1)."
			+ "graph_vertex(g1, 1)."
			+ "graph_vertex(g1, 2)."
			+ "graph_vertex(g1 ,3)."
			+ "graph_edge(g1, e(1, 2))."
			+ "graph_edge(g1, e(2, 3))."
			+ "graph_edge(g1, e(3, 1))."
			+ "graph_directed(G) :- graph(G), not graph_undirected(G)."
			+ "err_directedness(G) :- graph(G), graph_directed(G), graph_undirected(G)."
			+ "err_directedness(G) :- graph(G), not graph_directed(G), not graph_undirected(G)."
			+ ":- err_directedness(_)."
			+ "err_undirected_edges(G, V1, V2) :- graph_edge(G, e(V1, V2)), graph_edge(G, e(V2, V1))."
			+ ":- err_undirected_edges(_, _, _).";
	private static final String VERTEX_DEGREE_ASP = TEST_GRAPH_ASP
			+ "graph_vertex_degree(G, V, D) :-"
			+ "    graph(G),"
			+ "    graph_vertex(G, V),"
			+ "    D = #count{ VN : graph_edge(G, e(V, VN)); VN : graph_edge(G, e(VN, V)) }.";
	private static final String NUM_MAX_DEGREE_VERTICES_ASP = VERTEX_DEGREE_ASP
			+ "graph_max_degree_vertices(G, DMAX, N) :-"
			+ "	   graph(G),"
			+ "	   DMAX = #max{ DV : graph_vertex_degree(G, V, DV)},"
			+ "    N = #count{ V : graph_vertex_degree(G, V, DMAX)}.";
	private static final String COMPLEX_EQUALITY_WITH_GLOBALS =
			"p(1..10)."
			+ "q :- X = #count { Y : p( Y ) }, X = #count { Z : p( Z ) },"
			+ "	Y = #count { X : p( X ) }, 1 <= #count { X : p( X ) }, Z = #max { W : p( W ) }.";
	//@formatter:on

	// Use an alpha instance with default config for all test cases
	private final Alpha alpha = AlphaFactory.newAlpha();
	private final Function<String, List<AnswerSet>> solve = (asp) -> {
		InputProgram prog = alpha.readProgramString(asp);
		return alpha.solve(prog).collect(Collectors.toList());
	};

	@Test
	public void countLeSortingGridSimple() {
		List<AnswerSet> answerSets = solve.apply(CNT_LE1_ASP);
		assertEquals(1, answerSets.size());
		AnswerSet answerSet = answerSets.get(0);
		Predicate thing = Predicates.getPredicate("thing", 1);
		Predicate candidate = Predicates.getPredicate("candidate", 1);
		Predicate cntLe = Predicates.getPredicate("cnt_le", 1);

		// System.out.println(new SimpleAnswerSetFormatter("\n").format(answerSet));

		assertTrue(answerSet.getPredicateInstances(thing).contains(Atoms.newBasicAtom(thing, Terms.newConstant(75))));
		assertTrue(answerSet.getPredicateInstances(thing).contains(Atoms.newBasicAtom(thing, Terms.newConstant(76))));

		assertTrue(answerSet.getPredicateInstances(candidate).contains(Atoms.newBasicAtom(candidate, Terms.newConstant(2))));
		assertTrue(answerSet.getPredicateInstances(candidate).contains(Atoms.newBasicAtom(candidate, Terms.newConstant(3))));
		assertTrue(answerSet.getPredicateInstances(candidate).contains(Atoms.newBasicAtom(candidate, Terms.newConstant(4))));

		assertTrue(answerSet.getPredicates().contains(cntLe));
		assertTrue(answerSet.getPredicateInstances(cntLe).contains(Atoms.newBasicAtom(cntLe, Terms.newConstant(2))));
	}

	@Test
	public void countEqSimple() {
		List<AnswerSet> answerSets = solve.apply(CNT_EQ1_ASP);
		assertEquals(1, answerSets.size());
		AnswerSet answerSet = answerSets.get(0);
		Predicate thing = Predicates.getPredicate("thing", 1);
		Predicate cntThings = Predicates.getPredicate("cnt_things", 1);

		// System.out.println(new SimpleAnswerSetFormatter("\n").format(answerSet));

		assertTrue(answerSet.getPredicateInstances(thing).contains(Atoms.newBasicAtom(thing, Terms.newConstant(4))));
		assertTrue(answerSet.getPredicateInstances(thing).contains(Atoms.newBasicAtom(thing, Terms.newConstant(5))));
		assertTrue(answerSet.getPredicateInstances(thing).contains(Atoms.newBasicAtom(thing, Terms.newConstant(6))));

		assertTrue(answerSet.getPredicates().contains(cntThings));
		assertTrue(answerSet.getPredicateInstances(cntThings).contains(Atoms.newBasicAtom(cntThings, Terms.newConstant(3))));
	}

	@Test
	public void countLeCountingGridSimple() {
		List<AnswerSet> answerSets = solve.apply(CNT_LE1_ASP);
		assertEquals(1, answerSets.size());
		AnswerSet answerSet = answerSets.get(0);
		Predicate thing = Predicates.getPredicate("thing", 1);
		Predicate candidate = Predicates.getPredicate("candidate", 1);
		Predicate cntLe = Predicates.getPredicate("cnt_le", 1);

		// System.out.println(new SimpleAnswerSetFormatter("\n").format(answerSet));

		assertTrue(answerSet.getPredicateInstances(thing).contains(Atoms.newBasicAtom(thing, Terms.newConstant(75))));
		assertTrue(answerSet.getPredicateInstances(thing).contains(Atoms.newBasicAtom(thing, Terms.newConstant(76))));

		assertTrue(answerSet.getPredicateInstances(candidate).contains(Atoms.newBasicAtom(candidate, Terms.newConstant(2))));
		assertTrue(answerSet.getPredicateInstances(candidate).contains(Atoms.newBasicAtom(candidate, Terms.newConstant(3))));
		assertTrue(answerSet.getPredicateInstances(candidate).contains(Atoms.newBasicAtom(candidate, Terms.newConstant(4))));

		assertTrue(answerSet.getPredicates().contains(cntLe));
		assertTrue(answerSet.getPredicateInstances(cntLe).contains(Atoms.newBasicAtom(cntLe, Terms.newConstant(2))));
	}

	@Test
	public void countEqGlobalVars() {
		List<AnswerSet> answerSets = solve.apply(VERTEX_DEGREE_ASP);
		assertEquals(1, answerSets.size());
		AnswerSet answerSet = answerSets.get(0);
		Predicate vertexDegree = Predicates.getPredicate("graph_vertex_degree", 3);

		// System.out.println(new SimpleAnswerSetFormatter("\n").format(answerSet));

		assertTrue(answerSet.getPredicates().contains(vertexDegree));
		assertTrue(answerSet.getPredicateInstances(vertexDegree)
				.contains(Atoms.newBasicAtom(vertexDegree, Terms.newSymbolicConstant("g1"), Terms.newConstant(1), Terms.newConstant(2))));
		assertTrue(answerSet.getPredicateInstances(vertexDegree)
				.contains(Atoms.newBasicAtom(vertexDegree, Terms.newSymbolicConstant("g1"), Terms.newConstant(2), Terms.newConstant(2))));
		assertTrue(answerSet.getPredicateInstances(vertexDegree)
				.contains(Atoms.newBasicAtom(vertexDegree, Terms.newSymbolicConstant("g1"), Terms.newConstant(3), Terms.newConstant(2))));
	}

	@Test
	// Test "count eq" and "max eq" together with global vars
	public void graphVerticesOfMaxDegree() {
		List<AnswerSet> answerSets = solve.apply(NUM_MAX_DEGREE_VERTICES_ASP);
		assertEquals(1, answerSets.size());
		AnswerSet answerSet = answerSets.get(0);
		Predicate maxDegreeVertices = Predicates.getPredicate("graph_max_degree_vertices", 3);

		// System.out.println(new SimpleAnswerSetFormatter("\n").format(answerSet));

		assertTrue(answerSet.getPredicates().contains(maxDegreeVertices));
		assertTrue(answerSet.getPredicateInstances(maxDegreeVertices)
				.contains(Atoms.newBasicAtom(maxDegreeVertices, Terms.newSymbolicConstant("g1"), Terms.newConstant(2), Terms.newConstant(3))));
	}

	@Test
	public void greaterMin() {
		List<AnswerSet> answerSets = solve.apply(MIN_GT1_ASP);
		assertEquals(1, answerSets.size());
		AnswerSet answerSet = answerSets.get(0);
		Predicate greaterMin = Predicates.getPredicate("greater_min_acceptable", 1);

		// System.out.println(new SimpleAnswerSetFormatter("\n").format(answerSet));

		assertTrue(answerSet.getPredicates().contains(greaterMin));
		assertTrue(answerSet.getPredicateInstances(greaterMin).contains(Atoms.newBasicAtom(greaterMin, Terms.newConstant(7))));
		assertTrue(answerSet.getPredicateInstances(greaterMin).contains(Atoms.newBasicAtom(greaterMin, Terms.newConstant(13))));
	}

	@Test
	public void sumEquals1() {
		List<AnswerSet> answerSets = solve.apply(SUM_EQ1_ASP);
		assertEquals(1, answerSets.size());
		AnswerSet answerSet = answerSets.get(0);
		Predicate sumThings = Predicates.getPredicate("sum_things", 1);

		// System.out.println(new SimpleAnswerSetFormatter("\n").format(answerSet));

		assertTrue(answerSet.getPredicates().contains(sumThings));
		assertEquals(1, answerSet.getPredicateInstances(sumThings).size());
		assertTrue(answerSet.getPredicateInstances(sumThings).contains(Atoms.newBasicAtom(sumThings, Terms.newConstant(12))));
	}

	@Test
	public void sumLessOrEqual1() {
		List<AnswerSet> answerSets = solve.apply(SUM_LE1_ASP);
		assertEquals(1, answerSets.size());
		AnswerSet answerSet = answerSets.get(0);
		Predicate boundLe = Predicates.getPredicate("bound_le_sum", 1);

		// System.out.println(new SimpleAnswerSetFormatter("\n").format(answerSet));

		assertTrue(answerSet.getPredicates().contains(boundLe));
		assertTrue(answerSet.getPredicateInstances(boundLe).contains(Atoms.newBasicAtom(boundLe, Terms.newConstant(11))));
	}

	@Test
	@Disabled("Open issue, as dependency analysis includes cyclic output-dependency, which it should not.")
	public void setComplexEqualityWithGlobals() {
		List<AnswerSet> answerSets = solve.apply(COMPLEX_EQUALITY_WITH_GLOBALS);
		assertEquals(1, answerSets.size());
		AnswerSet answerSet = answerSets.get(0);
		Predicate q = Predicates.getPredicate("q", 0);

		// System.out.println(new SimpleAnswerSetFormatter("\n").format(answerSet));

		assertTrue(answerSet.getPredicates().contains(q));
		assertTrue(answerSet.getPredicateInstances(q).contains(Atoms.newBasicAtom(q)));
	}

}
