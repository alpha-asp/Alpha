package at.ac.tuwien.kr.alpha.grounder.transformation.aggregates;

import at.ac.tuwien.kr.alpha.common.ComparisonOperator;
import at.ac.tuwien.kr.alpha.common.Predicate;
import at.ac.tuwien.kr.alpha.common.atoms.AggregateAtom;
import at.ac.tuwien.kr.alpha.common.atoms.AggregateAtom.AggregateFunctionSymbol;
import at.ac.tuwien.kr.alpha.common.atoms.AggregateLiteral;
import at.ac.tuwien.kr.alpha.common.atoms.Atom;
import at.ac.tuwien.kr.alpha.common.atoms.BasicAtom;
import at.ac.tuwien.kr.alpha.common.atoms.BasicLiteral;
import at.ac.tuwien.kr.alpha.common.atoms.Literal;
import at.ac.tuwien.kr.alpha.common.rule.BasicRule;
import at.ac.tuwien.kr.alpha.common.terms.ConstantTerm;
import at.ac.tuwien.kr.alpha.common.terms.FunctionTerm;
import at.ac.tuwien.kr.alpha.common.terms.Term;
import at.ac.tuwien.kr.alpha.common.terms.VariableTerm;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.stringtemplate.v4.ST;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class AggregateRewritingContext {

	private static final ST AGGREGATE_RESULT_TEMPLATE = new ST("<id>_result");
	private static final ST AGGREGATE_ARGS_FUNCTION_SYMBOL = new ST("<id>_args");
	private static final ST AGGREGATE_ARGS_NOARGS_CONST = new ST("<id>_no_args");

	private int idCounter;
	private Map<String, AggregateInfo> aggregatesById = new HashMap<>();
	private Map<String, Set<Literal>> dependenciesById = new HashMap<>();
	private Map<ImmutablePair<AggregateFunctionSymbol, ComparisonOperator>, Set<AggregateInfo>> aggregateFunctionsToRewrite = new LinkedHashMap<>();
	// Since theoretically an aggregate literal could occur in several rules in different context,
	// we need to keep track of the rules aggregate literals occur in.
	private Map<BasicRule, Map<AggregateLiteral, String>> rulesWithAggregates = new HashMap<>();

	public AggregateRewritingContext() {
		idCounter = 0;
	}

	/**
	 * Registers a rule that potentially contains one or more {@link AggregateLiteral}s with this context.
	 * In case aggregates are found in the rule, global variables and dependencies of the aggregate are calculated and the
	 * aggregate literal is stored in the rewriting context along with a reference to the rule it occurs in
	 * 
	 * @param rule
	 * @return true if the given rule contains one or more aggregate literals, false otherwise
	 */
	public boolean registerRule(BasicRule rule) {
		AggregateRewritingRuleAnalysis ruleAnalysis = AggregateRewritingRuleAnalysis.analyzeRule(rule);
		if (ruleAnalysis.aggregatesInRule.isEmpty()) {
			// Rule has no aggregates.
			return false;
		}
		Map<AggregateLiteral, String> idsPerLiteral = new HashMap<>();
		// Do initial registration of each aggregate literal and keep the ids.
		for (Map.Entry<AggregateLiteral, Set<VariableTerm>> entry : ruleAnalysis.globalVariablesPerAggregate.entrySet()) {
			String id = internalRegisterAggregateLiteral(entry.getKey(), entry.getValue());
			idsPerLiteral.put(entry.getKey(), id);
		}
		// Now go through dependencies and replace the actual aggregate literals with their rewritten versions
		for (Map.Entry<AggregateLiteral, Set<Literal>> entry : ruleAnalysis.dependenciesPerAggregate.entrySet()) {
			Set<Literal> rewrittenDependencies = new HashSet<>();
			for (Literal dependency : entry.getValue()) {
				if (dependency instanceof AggregateLiteral) {
					AggregateInfo dependencyInfo = getAggregateInfo(idsPerLiteral.get((AggregateLiteral) dependency));
					rewrittenDependencies.add(new BasicLiteral(dependencyInfo.getOutputAtom(), !dependency.isNegated()));
				} else {
					rewrittenDependencies.add(dependency);
				}
			}
			dependenciesById.put(idsPerLiteral.get(entry.getKey()), rewrittenDependencies);
		}
		rulesWithAggregates.put(rule, idsPerLiteral);
		return true;
	}

	// Note: Thanks to type erasure we need a name other than "registerAggregateLiteral" here.
	private String internalRegisterAggregateLiteral(AggregateLiteral lit, Set<VariableTerm> globalVariables) {
		AggregateAtom atom = lit.getAtom();
		String id = atom.getAggregatefunction().toString().toLowerCase() + "_" + (++idCounter);
		AggregateInfo info = new AggregateInfo(id, lit, globalVariables);
		aggregatesById.put(id, info);
		aggregateFunctionsToRewrite.putIfAbsent(new ImmutablePair<>(atom.getAggregatefunction(), atom.getLowerBoundOperator()), new LinkedHashSet<>());
		aggregateFunctionsToRewrite.get(new ImmutablePair<>(atom.getAggregatefunction(), atom.getLowerBoundOperator())).add(info);
		return id;
	}

	public AggregateInfo getAggregateInfo(String aggregateId) {
		return aggregatesById.get(aggregateId);
	}

	// Transforms (restricted) aggregate literals of format "VAR OP #AGG_FN{...}" into literals of format
	// "<result_predicate>(ARGS, VAR)" where ARGS is a function term wrapping the aggregate's global variables.
	List<BasicRule> rewriteRulesWithAggregates() {
		List<BasicRule> rewrittenRules = new ArrayList<>();
		for (BasicRule rule : rulesWithAggregates.keySet()) {
			List<Literal> rewrittenBody = new ArrayList<>();
			Map<AggregateLiteral, String> aggregatesInRule = rulesWithAggregates.get(rule);
			for (Literal lit : rule.getBody()) {
				if (lit instanceof AggregateLiteral) {
					String aggregateId = aggregatesInRule.get((AggregateLiteral) lit);
					AggregateInfo aggregateInfo = getAggregateInfo(aggregateId);
					rewrittenBody.add(new BasicLiteral(aggregateInfo.getOutputAtom(), !lit.isNegated()));
				} else {
					rewrittenBody.add(lit);
				}
			}
			rewrittenRules.add(new BasicRule(rule.getHead(), rewrittenBody));
		}
		return rewrittenRules;
	}

	public Set<Literal> getDependencies(String aggregateId) {
		return dependenciesById.get(aggregateId);
	}

	public Map<ImmutablePair<AggregateFunctionSymbol, ComparisonOperator>, Set<AggregateInfo>> getAggregateFunctionsToRewrite() {
		return Collections.unmodifiableMap(aggregateFunctionsToRewrite);
	}

	public static class AggregateInfo {
		private final String id;
		private final AggregateLiteral literal;
		private final BasicAtom outputAtom;
		private final Set<VariableTerm> globalVariables;
		private final Term aggregateArguments;

		AggregateInfo(String id, AggregateLiteral literal, Set<VariableTerm> globalVariables) {
			this.id = id;
			this.literal = literal;
			this.globalVariables = globalVariables;
			this.aggregateArguments = buildArguments();
			this.outputAtom = buildOutputAtom();
		}

		/**
		 * Builds a term representing the global variables of the {@link AggregateLiteral} represented by this
		 * {@link AggregateInfo}. For a literal with global variables "A, B, C", the argument term is "$aggregate_id_$args(A, B,
		 * C)". For a
		 * literal without global variables, the constant "$aggregate_id$_no_args" is returned.
		 */
		private Term buildArguments() {
			Term argumentTerm;
			if (globalVariables.isEmpty()) {
				argumentTerm = ConstantTerm.getSymbolicInstance(new ST(AGGREGATE_ARGS_NOARGS_CONST).add("id", id).render());
			} else {
				argumentTerm = FunctionTerm.getInstance(new ST(AGGREGATE_ARGS_FUNCTION_SYMBOL).add("id", id).render(),
						new ArrayList<>(globalVariables));
			}
			return argumentTerm;
		}

		/**
		 * Builds an {@link Atom} which is inserted into rule bodies instead of the respective {@link AggregateLiteral}.
		 * The atom is of structure "$aggregate_id$_result($argTerm$, $resultTerm$)", where aggregate_id is the unique id of the
		 * aggregate literal as generated by {@link AggregateRewritingContext}, arg_term contains the global variables of the
		 * aggregate literal in question (or a constant "$aggregate_id$_no_args" for a literal without global variables), and
		 * resultTerm is the
		 * "lowerBoundTerm" of the aggregate literal.
		 * 
		 * @return
		 */
		private BasicAtom buildOutputAtom() {
			String outputPredicateName = new ST(AGGREGATE_RESULT_TEMPLATE).add("id", id).render();
			Term argumentTerm = aggregateArguments;
			Term resultTerm = literal.getAtom().getLowerBoundTerm();
			return new BasicAtom(Predicate.getInstance(outputPredicateName, 2, true), argumentTerm, resultTerm);
		}

		public String getId() {
			return id;
		}

		public AggregateLiteral getLiteral() {
			return literal;
		}

		public BasicAtom getOutputAtom() {
			return outputAtom;
		}

		public Set<VariableTerm> getGlobalVariables() {
			return globalVariables;
		}

		public Term getAggregateArguments() {
			return aggregateArguments;
		}

	}

}
