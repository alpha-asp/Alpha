package at.ac.tuwien.kr.alpha.grounder.transformation;

import org.apache.commons.collections4.SetUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.stringtemplate.v4.ST;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import at.ac.tuwien.kr.alpha.common.ComparisonOperator;
import at.ac.tuwien.kr.alpha.common.Predicate;
import at.ac.tuwien.kr.alpha.common.atoms.AggregateAtom;
import at.ac.tuwien.kr.alpha.common.atoms.AggregateAtom.AggregateElement;
import at.ac.tuwien.kr.alpha.common.atoms.AggregateAtom.AggregateFunctionSymbol;
import at.ac.tuwien.kr.alpha.common.atoms.AggregateLiteral;
import at.ac.tuwien.kr.alpha.common.atoms.Atom;
import at.ac.tuwien.kr.alpha.common.atoms.BasicAtom;
import at.ac.tuwien.kr.alpha.common.atoms.Literal;
import at.ac.tuwien.kr.alpha.common.rule.BasicRule;
import at.ac.tuwien.kr.alpha.common.terms.ConstantTerm;
import at.ac.tuwien.kr.alpha.common.terms.FunctionTerm;
import at.ac.tuwien.kr.alpha.common.terms.Term;
import at.ac.tuwien.kr.alpha.common.terms.VariableTerm;

public final class AggregateRewritingContext {

	private static final ST AGGREGATE_RESULT_TEMPLATE = new ST("<id>_result");
	private static final ST AGGREGATE_ARGS_FUNCTION_SYMBOL = new ST("<id>_args");
	private static final ST AGGREGATE_ARGS_NOARGS_CONST = new ST("<id>_no_args");

	private int idCounter;
	private Map<String, AggregateInfo> aggregatesById = new HashMap<>();
	private Map<String, Set<Literal>> dependenciesById = new HashMap<>();
	private Map<ImmutablePair<AggregateFunctionSymbol, ComparisonOperator>, Set<String>> aggregateFunctionsToRewrite = new HashMap<>();
	// Since theoretically an aggregate literal could occur in several rules in different context,
	// we need to keep track of the rules aggregate literals occur in.
	private Map<BasicRule, Map<AggregateLiteral, String>> rulesWithAggregates = new HashMap<>();

	public AggregateRewritingContext() {
	}

	public AggregateRewritingContext(AggregateRewritingContext ctx) {
		this.idCounter = ctx.idCounter;
		this.aggregatesById = new HashMap<>(ctx.aggregatesById);
		this.dependenciesById = new HashMap<>();
		for (Map.Entry<String, Set<Literal>> entry : ctx.dependenciesById.entrySet()) {
			this.dependenciesById.put(entry.getKey(), new HashSet<>(entry.getValue()));
		}
		this.aggregateFunctionsToRewrite = new HashMap<>();
		for (Map.Entry<ImmutablePair<AggregateFunctionSymbol, ComparisonOperator>, Set<String>> entry : ctx.aggregateFunctionsToRewrite.entrySet()) {
			this.aggregateFunctionsToRewrite.put(entry.getKey(), new HashSet<>(entry.getValue()));
		}
		for (Map.Entry<BasicRule, Map<AggregateLiteral, String>> entry : ctx.rulesWithAggregates.entrySet()) {
			this.rulesWithAggregates.put(entry.getKey(), new HashMap<>(entry.getValue()));
		}
	}

	public boolean registerRule(BasicRule rule) {
		// Keep track of aggregates in the rule so we get the dependencies right in rules with more than one aggregate.
		Map<String, Set<Literal>> dependeciesByAggregateId = new HashMap<>();
		Map<AggregateLiteral, String> idsByLiteral = new HashMap<>();
		// Do a first iteration to find and register all aggregates.
		boolean aggregateFound = false;
		for (Literal lit : rule.getBody()) {
			if (lit instanceof AggregateLiteral) {
				AggregateLiteral aggregateLiteral = (AggregateLiteral) lit;
				Set<Literal> bodyWithoutLit = SetUtils.difference(rule.getBody(), Collections.singleton(aggregateLiteral));
				Set<VariableTerm> globalVars = this.findGlobalVariablesForAggregate(aggregateLiteral, bodyWithoutLit);
				String aggregateId = this.internalRegisterAggregateLiteral((AggregateLiteral) lit, globalVars);
				dependeciesByAggregateId.put(aggregateId, new HashSet<>());
				idsByLiteral.put((AggregateLiteral) lit, aggregateId);
				aggregateFound = true;
			}
		}
		if (!aggregateFound) {
			return false;
		}
		// Now collect the dependencies (i.e. the rest of the rule body) for each aggregate literal.
		for (Literal lit : rule.getBody()) {
			if (lit instanceof AggregateLiteral) {
				// Add the rewritten literal as dependency to all aggregate literals other than lit itself.
				for (String id : dependeciesByAggregateId.keySet()) {
					if (id.equals(idsByLiteral.get((AggregateLiteral) lit))) {
						continue;
					}
					dependeciesByAggregateId.get(id).add(this.aggregatesById.get(id).outputAtom.toLiteral(!lit.isNegated()));
				}
			} else {
				// Add the literal as dependency to all aggregate literals
				for (String id : dependeciesByAggregateId.keySet()) {
					dependeciesByAggregateId.get(id).add(lit);
				}
			}
		}
		// Register the rule itself, along with aggregates occurring within it
		this.rulesWithAggregates.put(rule, idsByLiteral);
		// Register the dependencies of the respective aggregate literals
		this.dependenciesById.putAll(dependeciesByAggregateId);
		return true;
	}

	public String registerAggregateLiteral(AggregateLiteral lit, Set<Literal> dependencies) {
		String id = this.internalRegisterAggregateLiteral(lit, this.findGlobalVariablesForAggregate(lit, dependencies));
		this.dependenciesById.put(id, dependencies);
		return id;
	}

	// Note: Thanks to type erasure we need a name other than "registerAggregateLiteral" here.
	private String internalRegisterAggregateLiteral(AggregateLiteral lit, Set<VariableTerm> globalVariables) {
		AggregateAtom atom = lit.getAtom();
		String id = atom.getAggregatefunction().toString().toLowerCase() + "_" + (++this.idCounter);
		AggregateInfo info = new AggregateInfo(id, lit, globalVariables);
		this.aggregatesById.put(id, info);
		this.aggregateFunctionsToRewrite.putIfAbsent(new ImmutablePair<>(atom.getAggregatefunction(), atom.getLowerBoundOperator()), new HashSet<>());
		this.aggregateFunctionsToRewrite.get(new ImmutablePair<>(atom.getAggregatefunction(), atom.getLowerBoundOperator())).add(id);
		return id;
	}

	/**
	 * Calculates a set of global variables for the given {@link AggregateLiteral} based on a specific set of literals
	 * ("scope"). A variable is "global" if it occurs inside an {@link AggregateElement} of the given literal AND is a
	 * binding or non-binding variable of at least one literal of the scope set. Note that, since {@link AggregateLiteral}s
	 * cannot know their non-binding variables by themselves, this method only looks at the lower bound term of aggregate
	 * literals in the scope set (since everything inside the aggregate itself is either local to the aggregate or global
	 * via other literals). The "scope" for which global variables are calculated is typically the body of the rule the
	 * aggregate literal occurs in.
	 * 
	 * @param lit
	 * @param scope
	 * @return
	 */
	private Set<VariableTerm> findGlobalVariablesForAggregate(AggregateLiteral aggregate, Set<Literal> scope) {
		Set<VariableTerm> globalVars = new HashSet<>();
		Set<VariableTerm> varsInGlobalScope = new HashSet<>();
		for (Literal lit : scope) {
			if (lit instanceof AggregateLiteral) {
				// Only look at "left-hand" term here, by contract we only have literals without right-hand terms at this stage.
				AggregateLiteral otherAggregate = (AggregateLiteral) lit;
				if (otherAggregate.getAtom().getLowerBoundTerm() instanceof VariableTerm) {
					varsInGlobalScope.add((VariableTerm) otherAggregate.getAtom().getLowerBoundTerm());
				}
			} else {
				varsInGlobalScope.addAll(lit.getBindingVariables());
				varsInGlobalScope.addAll(lit.getNonBindingVariables());
			}
		}
		/*
		 * At this point we have collected all variables from the literals in the scope,
		 * now check which of these occur in the aggregate in question.
		 */
		AggregateAtom atom = aggregate.getAtom();
		for (VariableTerm var : atom.getAggregateVariables()) {
			if (varsInGlobalScope.contains(var)) {
				globalVars.add(var);
			}
		}
		return globalVars;
	}

	public AggregateInfo getAggregateInfo(String aggregateId) {
		return this.aggregatesById.get(aggregateId);
	}

	public Set<BasicRule> getRulesToRewrite() {
		return Collections.unmodifiableSet(this.rulesWithAggregates.keySet());
	}

	public Map<AggregateLiteral, String> getAggregatesInRule(BasicRule rule) {
		return Collections.unmodifiableMap(this.rulesWithAggregates.get(rule));
	}

	public Set<Literal> getDependencies(String aggregateId) {
		return this.dependenciesById.get(aggregateId);
	}

	public int numAggregatesToRewrite() {
		return this.idCounter;
	}

	public Map<ImmutablePair<AggregateFunctionSymbol, ComparisonOperator>, Set<String>> getAggregateFunctionsToRewrite() {
		return Collections.unmodifiableMap(this.aggregateFunctionsToRewrite);
	}

	public static class AggregateInfo {
		private final String id;
		private final AggregateLiteral literal;
		private final BasicAtom outputAtom;
		private final Set<VariableTerm> globalVariables;
		private final Term aggregateArguments;

		public AggregateInfo(String id, AggregateLiteral literal, Set<VariableTerm> globalVariables) {
			this.id = id;
			this.literal = literal;
			this.globalVariables = globalVariables;
			this.aggregateArguments = this.buildArguments();
			this.outputAtom = this.buildOutputAtom();
		}

		/**
		 * Builds a term representing the global variables of the {@link AggregateLiteral} represented by this
		 * {@link AggregateInfo}. For a literal with global variables "A, B, C", the argument term is "$aggregate_id_$args(A, B,
		 * C)". For a
		 * literal without global variables, the constant "$aggregate_id$_no_args" is returned.
		 */
		private Term buildArguments() {
			Term argumentTerm;
			if (this.globalVariables.isEmpty()) {
				argumentTerm = ConstantTerm.getSymbolicInstance(new ST(AGGREGATE_ARGS_NOARGS_CONST).add("id", this.id).render());
			} else {
				argumentTerm = FunctionTerm.getInstance(new ST(AGGREGATE_ARGS_FUNCTION_SYMBOL).add("id", this.id).render(),
						new ArrayList<>(this.globalVariables));
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
			String outputPredicateName = new ST(AGGREGATE_RESULT_TEMPLATE).add("id", this.id).render();
			Term argumentTerm = this.aggregateArguments;
			Term resultTerm = this.literal.getAtom().getLowerBoundTerm();
			return new BasicAtom(Predicate.getInstance(outputPredicateName, 2, true), argumentTerm, resultTerm);
		}

		public String getId() {
			return this.id;
		}

		public AggregateLiteral getLiteral() {
			return this.literal;
		}

		public BasicAtom getOutputAtom() {
			return this.outputAtom;
		}

		public Set<VariableTerm> getGlobalVariables() {
			return this.globalVariables;
		}

		public Term getAggregateArguments() {
			return this.aggregateArguments;
		}

	}

}
