package at.ac.tuwien.kr.alpha.core.programs.transformation.aggregates;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;

import at.ac.tuwien.kr.alpha.api.programs.atoms.AggregateAtom.AggregateElement;
import at.ac.tuwien.kr.alpha.api.programs.atoms.AggregateAtom.AggregateFunctionSymbol;
import at.ac.tuwien.kr.alpha.api.programs.literals.AggregateLiteral;
import at.ac.tuwien.kr.alpha.api.programs.literals.Literal;
import at.ac.tuwien.kr.alpha.api.rules.Rule;
import at.ac.tuwien.kr.alpha.api.rules.heads.Head;
import at.ac.tuwien.kr.alpha.api.terms.ArithmeticOperator;
import at.ac.tuwien.kr.alpha.api.terms.Term;
import at.ac.tuwien.kr.alpha.api.terms.VariableTerm;
import at.ac.tuwien.kr.alpha.commons.Predicates;
import at.ac.tuwien.kr.alpha.commons.atoms.Atoms;
import at.ac.tuwien.kr.alpha.commons.comparisons.ComparisonOperators;
import at.ac.tuwien.kr.alpha.commons.literals.Literals;
import at.ac.tuwien.kr.alpha.commons.rules.heads.Heads;
import at.ac.tuwien.kr.alpha.commons.terms.Terms;
import at.ac.tuwien.kr.alpha.core.rules.BasicRule;

public class AggregateRewritingRuleAnalysisTest {

	//@formatter:off
	// BINDING_AGGREGATE_NO_GLOBALS := p(X) :- X = #max{N : q(N)}, X < 10, p(X, Y).
	private static final Rule<Head> BINDING_AGGREGATE_NO_GLOBALS = new BasicRule(
			Heads.newNormalHead(Atoms.newBasicAtom(Predicates.getPredicate("p", 1), Terms.newVariable("X"))),
			Atoms.newAggregateAtom(
					ComparisonOperators.EQ, 
					Terms.newVariable("X"), 
					AggregateFunctionSymbol.MAX, 
					Arrays.asList(
							Atoms.newAggregateElement(Arrays.asList(Terms.newVariable("N")), Arrays.asList(Atoms.newBasicAtom(Predicates.getPredicate("q", 1), Terms.newVariable("N")).toLiteral())))
					).toLiteral(),
			Atoms.newComparisonAtom(Terms.newVariable("X"), Terms.newConstant(10), ComparisonOperators.LT).toLiteral(),
			Atoms.newBasicAtom(Predicates.getPredicate("p", 2), Terms.newVariable("X"), Terms.newVariable("Y")).toLiteral());

	// NONBINDING_AGGREGATE_NO_GLOBALS_1 := p(X) :- X < #max{N : q(N)}, X < 10, p(X, Y).
	private static final Rule<Head> NONBINDING_AGGREGATE_NO_GLOBALS_1 = new BasicRule(
			Heads.newNormalHead(Atoms.newBasicAtom(Predicates.getPredicate("p", 1), Terms.newVariable("X"))),
			Atoms.newAggregateAtom(
					ComparisonOperators.LT, 
					Terms.newVariable("X"), 
					AggregateFunctionSymbol.MAX, 
					Arrays.asList(
							Atoms.newAggregateElement(Arrays.asList(Terms.newVariable("N")), Arrays.asList(Atoms.newBasicAtom(Predicates.getPredicate("q", 1), Terms.newVariable("N")).toLiteral())))
					).toLiteral(),
			Atoms.newComparisonAtom(Terms.newVariable("X"), Terms.newConstant(10), ComparisonOperators.LT).toLiteral(),
			Atoms.newBasicAtom(Predicates.getPredicate("p", 2), Terms.newVariable("X"), Terms.newVariable("Y")).toLiteral());
	
	// NONBINDING_AGGREGATE_NO_GLOBALS_2 := p(X) :- X < #max{N : q(N)}, X < 10, X = 3 + Y, Y = Z + 4, r(S, Z).
	private static final Rule<Head> NONBINDING_AGGREGATE_NO_GLOBALS_2 = new BasicRule(
			Heads.newNormalHead(Atoms.newBasicAtom(Predicates.getPredicate("p", 1), Terms.newVariable("X"))),
			Atoms.newAggregateAtom(
					ComparisonOperators.LT, 
					Terms.newVariable("X"), 
					AggregateFunctionSymbol.MAX, 
					Arrays.asList(
							Atoms.newAggregateElement(Arrays.asList(Terms.newVariable("N")), Arrays.asList(Atoms.newBasicAtom(Predicates.getPredicate("q", 1), Terms.newVariable("N")).toLiteral())))
					).toLiteral(),
			Atoms.newComparisonAtom(Terms.newVariable("X"), Terms.newConstant(10), ComparisonOperators.LT).toLiteral(),
			Atoms.newComparisonAtom(Terms.newVariable("X"), Terms.newArithmeticTerm(Terms.newConstant(3), ArithmeticOperator.PLUS, Terms.newVariable("Y")), ComparisonOperators.EQ).toLiteral(),
			Atoms.newComparisonAtom(Terms.newVariable("Y"), Terms.newArithmeticTerm(Terms.newVariable("Z"), ArithmeticOperator.PLUS, Terms.newConstant(4)), ComparisonOperators.EQ).toLiteral(),
			Atoms.newBasicAtom(Predicates.getPredicate("r", 2), Terms.newVariable("S"), Terms.newVariable("Z")).toLiteral());
	
	// BINDING_AGGREGATES_NO_GLOBALS_INCLUDED := p :- not p(X), 3 < #max { Y : q(Y) }, X = #count { Y : q(Y) }.
	private static final Rule<Head> BINDING_AGGREGATES_NO_GLOBALS_INCLUDED = new BasicRule(
			Heads.newNormalHead(Atoms.newBasicAtom(Predicates.getPredicate("p", 0))),
			Atoms.newBasicAtom(Predicates.getPredicate("p", 1), Terms.newVariable("X")).toLiteral(false),
			Atoms.newAggregateAtom(
					ComparisonOperators.LT, 
					Terms.newConstant(3),
					AggregateFunctionSymbol.MAX, 
					Arrays.asList(
							Atoms.newAggregateElement(Arrays.asList(Terms.newVariable("Y")), Arrays.asList(Atoms.newBasicAtom(Predicates.getPredicate("q", 1), Terms.newVariable("Y")).toLiteral())))
					).toLiteral(),
			Atoms.newAggregateAtom(
					ComparisonOperators.EQ, 
					Terms.newVariable("X"),
					AggregateFunctionSymbol.COUNT, 
					Arrays.asList(
							Atoms.newAggregateElement(Arrays.asList(Terms.newVariable("Y")), Arrays.asList(Atoms.newBasicAtom(Predicates.getPredicate("q", 1), Terms.newVariable("Y")).toLiteral())))
					).toLiteral());
	
	/* BINDING_AGGREGATE_WITH_GLOBALS_1 := 
	 *     graph_vertex_degree(G, V, D) :-
	 *         graph(G),
	 *         graph_vertex(G, V),
	 *         D = #count{ VN : graph_edge(G, e(V, VN)); VN : graph_edge(G, e(VN, V)) }.
	 */
	private static final Rule<Head> BINDING_AGGREGATE_WITH_GLOBALS_1 = new BasicRule(
			Heads.newNormalHead(Atoms.newBasicAtom(Predicates.getPredicate("graph_vertex_degree", 3), Terms.newVariable("G"), Terms.newVariable("V"), Terms.newVariable("D"))),
			Atoms.newBasicAtom(Predicates.getPredicate("graph", 1), Terms.newVariable("G")).toLiteral(),
			Atoms.newBasicAtom(Predicates.getPredicate("graph_vertex", 2), Terms.newVariable("G"), Terms.newVariable("V")).toLiteral(),
			Atoms.newAggregateAtom(
					ComparisonOperators.EQ, 
					Terms.newVariable("D"),
					AggregateFunctionSymbol.COUNT, 
					Arrays.asList(
							Atoms.newAggregateElement(Arrays.asList(Terms.newVariable("VN")), Arrays.asList(Atoms.newBasicAtom(Predicates.getPredicate("graph_edge", 2), Terms.newVariable("G"), Terms.newFunctionTerm("e", Terms.newVariable("V"), Terms.newVariable("VN"))).toLiteral())),
							Atoms.newAggregateElement(Arrays.asList(Terms.newVariable("VN")), Arrays.asList(Atoms.newBasicAtom(Predicates.getPredicate("graph_edge", 2), Terms.newVariable("G"), Terms.newFunctionTerm("e", Terms.newVariable("VN"), Terms.newVariable("V"))).toLiteral())))
					).toLiteral());	
	
	/* BINDING_AGGREGATE_WITH_GLOBALS_2 := 
	 * graph_max_degree_vertices(G, DMAX, N) :-
	 *	   graph(G),
	 *	   DMAX = #max{ DV : graph_vertex_degree(G, V, DV)},
	 *     N = #count{ V : graph_vertex_degree(G, V, DMAX)}.
	 */
	private static final Rule<Head> BINDING_AGGREGATE_WITH_GLOBALS_2 = new BasicRule(
			Heads.newNormalHead(Atoms.newBasicAtom(Predicates.getPredicate("graph_max_degree_vertices", 3), Terms.newVariable("G"), Terms.newVariable("DMAX"), Terms.newVariable("N"))),
			Atoms.newBasicAtom(Predicates.getPredicate("graph", 1), Terms.newVariable("G")).toLiteral(),
			Atoms.newAggregateAtom(
					ComparisonOperators.EQ, 
					Terms.newVariable("DMAX"),
					AggregateFunctionSymbol.MAX, 
					Arrays.asList(
							Atoms.newAggregateElement(Arrays.asList(Terms.newVariable("DV")), Arrays.asList(Atoms.newBasicAtom(Predicates.getPredicate("graph_vertex_degree", 3), Terms.newVariable("G"), Terms.newVariable("V"), Terms.newVariable("DV")).toLiteral())))
					).toLiteral(),
			Atoms.newAggregateAtom(
					ComparisonOperators.EQ, 
					Terms.newVariable("N"),
					AggregateFunctionSymbol.COUNT, 
					Arrays.asList(
							Atoms.newAggregateElement(Arrays.asList(Terms.newVariable("V")), Arrays.asList(Atoms.newBasicAtom(Predicates.getPredicate("graph_vertex_degree", 3), Terms.newVariable("G"), Terms.newVariable("V"), Terms.newVariable("DMAX")).toLiteral())))
					).toLiteral()
			);	
	//@formatter:on

	@Test
	public void bindingAggregateNoGlobals() {
		AggregateRewritingRuleAnalysis analysis = AggregateRewritingRuleAnalysis.analyzeRuleDependencies(BINDING_AGGREGATE_NO_GLOBALS);
		assertEquals(1, analysis.globalVariablesPerAggregate.size());
		assertEquals(1, analysis.dependenciesPerAggregate.size());

		AggregateLiteral aggregate = new ArrayList<>(analysis.globalVariablesPerAggregate.keySet()).get(0);
		assertTrue(analysis.globalVariablesPerAggregate.get(aggregate).isEmpty());
		assertEquals(0, analysis.dependenciesPerAggregate.get(aggregate).size());
	}

	@Test
	public void nonBindingAggregateNoGlobals1() {
		AggregateRewritingRuleAnalysis analysis = AggregateRewritingRuleAnalysis.analyzeRuleDependencies(NONBINDING_AGGREGATE_NO_GLOBALS_1);
		assertEquals(1, analysis.globalVariablesPerAggregate.size());
		assertEquals(1, analysis.dependenciesPerAggregate.size());

		AggregateLiteral aggregate = new ArrayList<>(analysis.globalVariablesPerAggregate.keySet()).get(0);
		assertTrue(analysis.globalVariablesPerAggregate.get(aggregate).isEmpty());
		assertFalse(analysis.dependenciesPerAggregate.get(aggregate).isEmpty());

		Set<Literal> dependencies = analysis.dependenciesPerAggregate.get(aggregate);
		assertEquals(1, dependencies.size());

		Literal pXY = Literals.fromAtom(Atoms.newBasicAtom(Predicates.getPredicate("p", 2), Terms.newVariable("X"), Terms.newVariable("Y")), true);
		assertTrue(dependencies.contains(pXY));
	}

	@Test
	public void nonBindingAggregateNoGlobals2() {
		AggregateRewritingRuleAnalysis analysis = AggregateRewritingRuleAnalysis.analyzeRuleDependencies(NONBINDING_AGGREGATE_NO_GLOBALS_2);
		assertEquals(1, analysis.globalVariablesPerAggregate.size());
		assertEquals(1, analysis.dependenciesPerAggregate.size());

		AggregateLiteral aggregate = new ArrayList<>(analysis.globalVariablesPerAggregate.keySet()).get(0);
		assertTrue(analysis.globalVariablesPerAggregate.get(aggregate).isEmpty());
		assertFalse(analysis.dependenciesPerAggregate.get(aggregate).isEmpty());

		Set<Literal> dependencies = analysis.dependenciesPerAggregate.get(aggregate);
		assertEquals(3, dependencies.size());

		Literal threePlusY = Literals.fromAtom(
				Atoms.newComparisonAtom(Terms.newVariable("X"),
						Terms.newArithmeticTerm(Terms.newConstant(3), ArithmeticOperator.PLUS, Terms.newVariable("Y")),
						ComparisonOperators.EQ),
				true);
		assertTrue(dependencies.contains(threePlusY));

		Literal zPlusFour = Literals.fromAtom(
				Atoms.newComparisonAtom(Terms.newVariable("Y"),
						Terms.newArithmeticTerm(Terms.newVariable("Z"), ArithmeticOperator.PLUS, Terms.newConstant(4)),
						ComparisonOperators.EQ),
				true);
		assertTrue(dependencies.contains(zPlusFour));

		Literal rSZ = Literals.fromAtom(Atoms.newBasicAtom(Predicates.getPredicate("r", 2), Terms.newVariable("S"), Terms.newVariable("Z")), true);
		assertTrue(dependencies.contains(rSZ));
	}

	@Test
	public void bindingAggregateWithGlobals1() {
		AggregateRewritingRuleAnalysis analysis = AggregateRewritingRuleAnalysis.analyzeRuleDependencies(BINDING_AGGREGATE_WITH_GLOBALS_1);
		assertEquals(1, analysis.globalVariablesPerAggregate.size());
		assertEquals(1, analysis.dependenciesPerAggregate.size());

		AggregateLiteral aggregate = new ArrayList<>(analysis.globalVariablesPerAggregate.keySet()).get(0);
		assertFalse(analysis.globalVariablesPerAggregate.get(aggregate).isEmpty());
		assertFalse(analysis.dependenciesPerAggregate.get(aggregate).isEmpty());

		Set<VariableTerm> globalVars = analysis.globalVariablesPerAggregate.get(aggregate);
		assertTrue(globalVars.contains(Terms.newVariable("G")));
		assertTrue(globalVars.contains(Terms.newVariable("V")));

		Set<Literal> dependencies = analysis.dependenciesPerAggregate.get(aggregate);
		assertEquals(2, dependencies.size());

		Literal graph = Literals.fromAtom(Atoms.newBasicAtom(Predicates.getPredicate("graph", 1), Terms.newVariable("G")), true);
		assertTrue(dependencies.contains(graph));

		Literal graphVertex = Literals.fromAtom(
				Atoms.newBasicAtom(Predicates.getPredicate("graph_vertex", 2), Terms.newVariable("G"), Terms.newVariable("V")), true);
		assertTrue(dependencies.contains(graphVertex));
	}

	@Test
	public void bindingAggregateWithGlobals2() {
		AggregateRewritingRuleAnalysis analysis = AggregateRewritingRuleAnalysis.analyzeRuleDependencies(BINDING_AGGREGATE_WITH_GLOBALS_2);
		assertEquals(2, analysis.globalVariablesPerAggregate.size());
		assertEquals(2, analysis.dependenciesPerAggregate.size());

		// Verify correct analysis of max aggregate
		List<Term> vertexDegreeTerms = Collections.singletonList(Terms.newVariable("DV"));
		Literal vertexDegreeLiteral = Literals.fromAtom(Atoms.newBasicAtom(Predicates.getPredicate("graph_vertex_degree", 3), Terms.newVariable("G"),
				Terms.newVariable("V"), Terms.newVariable("DV")), true);
		List<Literal> vertexDegreeLiterals = Collections.singletonList(vertexDegreeLiteral);
		AggregateElement vertexDegree = Atoms.newAggregateElement(vertexDegreeTerms, vertexDegreeLiterals);
		AggregateLiteral maxAggregate = Literals.fromAtom(
				Atoms.newAggregateAtom(ComparisonOperators.EQ, Terms.newVariable("DMAX"), AggregateFunctionSymbol.MAX,
						Collections.singletonList(vertexDegree)),
				true);
		assertTrue(analysis.globalVariablesPerAggregate.containsKey(maxAggregate));

		Set<VariableTerm> maxAggrGlobalVars = analysis.globalVariablesPerAggregate.get(maxAggregate);
		assertEquals(1, maxAggrGlobalVars.size());
		assertTrue(maxAggrGlobalVars.contains(Terms.newVariable("G")));

		assertTrue(analysis.dependenciesPerAggregate.containsKey(maxAggregate));
		Set<Literal> maxAggrDependencies = analysis.dependenciesPerAggregate.get(maxAggregate);
		assertEquals(1, maxAggrDependencies.size());
		Literal graph = Literals.fromAtom(Atoms.newBasicAtom(Predicates.getPredicate("graph", 1), Terms.newVariable("G")), true);
		assertTrue(maxAggrDependencies.contains(graph));

		// Verify correct analysis of count aggregate
		List<Term> maxVertexDegreeTerms = Collections.singletonList(Terms.newVariable("V"));
		Literal maxVertexDegreeLiteral = Literals.fromAtom(Atoms.newBasicAtom(Predicates.getPredicate("graph_vertex_degree", 3), Terms.newVariable("G"),
				Terms.newVariable("V"), Terms.newVariable("DMAX")), true);
		List<Literal> maxVertexDegreeLiterals = Collections.singletonList(maxVertexDegreeLiteral);
		AggregateElement maxVertexDegree = Atoms.newAggregateElement(maxVertexDegreeTerms, maxVertexDegreeLiterals);
		AggregateLiteral countAggregate = Literals.fromAtom(
				Atoms.newAggregateAtom(ComparisonOperators.EQ, Terms.newVariable("N"), AggregateFunctionSymbol.COUNT,
						Collections.singletonList(maxVertexDegree)),
				true);
		assertTrue(analysis.globalVariablesPerAggregate.containsKey(countAggregate));
		Set<VariableTerm> cntAggrGlobalVars = analysis.globalVariablesPerAggregate.get(countAggregate);
		assertEquals(2, cntAggrGlobalVars.size());
		assertTrue(cntAggrGlobalVars.contains(Terms.newVariable("G")));
		assertTrue(cntAggrGlobalVars.contains(Terms.newVariable("DMAX")));

		assertTrue(analysis.dependenciesPerAggregate.containsKey(countAggregate));
		Set<Literal> cntAggrDependencies = analysis.dependenciesPerAggregate.get(countAggregate);
		assertEquals(2, cntAggrDependencies.size());
		assertTrue(cntAggrDependencies.contains(graph));
		assertTrue(cntAggrDependencies.contains(maxAggregate));
	}

	@Test
	public void bindingAggregateGlobalsNotIncluded() {
		AggregateRewritingRuleAnalysis analysis = AggregateRewritingRuleAnalysis.analyzeRuleDependencies(BINDING_AGGREGATES_NO_GLOBALS_INCLUDED);
		assertEquals(2, analysis.globalVariablesPerAggregate.size());
		assertEquals(2, analysis.dependenciesPerAggregate.size());

		// Check that the #max aggregate does not include "not p(X)" as its dependency.
		for (Map.Entry<AggregateLiteral, Set<Literal>> aggregateDependencies : analysis.dependenciesPerAggregate.entrySet()) {
			if (aggregateDependencies.getKey().getAtom().getAggregateFunction() == AggregateFunctionSymbol.MAX) {
				for (Literal dependency : aggregateDependencies.getValue()) {
					assertFalse(dependency.isNegated());
				}
			}
		}
	}

}
