package at.ac.tuwien.kr.alpha.grounder.transformation.aggregates;

import org.apache.commons.collections4.SetUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.stringtemplate.v4.ST;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import at.ac.tuwien.kr.alpha.common.ComparisonOperator;
import at.ac.tuwien.kr.alpha.common.Predicate;
import at.ac.tuwien.kr.alpha.common.atoms.AggregateAtom;
import at.ac.tuwien.kr.alpha.common.atoms.AggregateAtom.AggregateElement;
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

public final class AggregateRewritingContext {

	private static final ST AGGREGATE_RESULT_TEMPLATE = new ST("<id>_result");
	private static final ST AGGREGATE_ARGS_FUNCTION_SYMBOL = new ST("<id>_args");
	private static final ST AGGREGATE_ARGS_NOARGS_CONST = new ST("<id>_no_args");

	private final AtomicInteger idCounter;
	private Map<String, AggregateInfo> aggregatesById = new HashMap<>();
	private Map<String, Set<Literal>> dependenciesById = new HashMap<>();
	private Map<ImmutablePair<AggregateFunctionSymbol, ComparisonOperator>, Set<String>> aggregateFunctionsToRewrite = new HashMap<>();
	// Since theoretically an aggregate literal could occur in several rules in different context,
	// we need to keep track of the rules aggregate literals occur in.
	private Map<BasicRule, Map<AggregateLiteral, String>> rulesWithAggregates = new HashMap<>();

	public AggregateRewritingContext() {
		this.idCounter = new AtomicInteger(0);
	}

	/**
	 * Not a real copy constructor - see {@link AggregateRewritingContext#createChildContext(AggregateRewritingContext)}.
	 * Creates a copy of the given contexts data structures, but uses the same id counter.
	 * 
	 * @param ctx
	 */
	private AggregateRewritingContext(AggregateRewritingContext ctx) {
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

	/**
	 * Creates a new {@link AggregateRewritingContext} that is a copy of the given context, but uses the same id counter.
	 * 
	 * @param ctx
	 * @return
	 */
	public static AggregateRewritingContext createChildContext(AggregateRewritingContext ctx) {
		return new AggregateRewritingContext(ctx);
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
		RuleAnalysis ruleAnalysis = RuleAnalysis.analyzeRule(rule);
		if (ruleAnalysis.globalVariablesPerAggregate.isEmpty()) {
			// no aggregates found
			return false;
		}
		Map<AggregateLiteral, String> idsPerLiteral = new HashMap<>();
		// Do initial registration of each aggregate literal and keep the ids.
		for (Map.Entry<AggregateLiteral, Set<VariableTerm>> entry : ruleAnalysis.globalVariablesPerAggregate.entrySet()) {
			String id = this.internalRegisterAggregateLiteral(entry.getKey(), entry.getValue());
			idsPerLiteral.put(entry.getKey(), id);
		}
		// Now go through dependencies and replace the actual aggregate literals with their rewritten versions
		for (Map.Entry<AggregateLiteral, Set<Literal>> entry : ruleAnalysis.dependenciesPerAggregate.entrySet()) {
			Set<Literal> rewrittenDependencies = new HashSet<>();
			for (Literal dependency : entry.getValue()) {
				if (dependency instanceof AggregateLiteral) {
					AggregateInfo dependencyInfo = this.getAggregateInfo(idsPerLiteral.get((AggregateLiteral) dependency));
					rewrittenDependencies.add(new BasicLiteral(dependencyInfo.getOutputAtom(), !dependency.isNegated()));
				} else {
					rewrittenDependencies.add(dependency);
				}
			}
			this.dependenciesById.put(idsPerLiteral.get(entry.getKey()), rewrittenDependencies);
		}
		this.rulesWithAggregates.put(rule, idsPerLiteral);
		return true;
	}

	/**
	 * Registers an {@link AggregateLiteral} to this rewriting context. Callers are responsibly for making sure the
	 * dependency set (i.e. all literals binding non-binding variables of the aggregate) is complete and correct.
	 * 
	 * @param lit
	 * @param dependencies
	 * @return the unique id assigned to the given literal (within this context and all child contexts)
	 */
	public String registerAggregateLiteral(AggregateLiteral lit, Set<Literal> dependencies) {
		String id = this.internalRegisterAggregateLiteral(lit, RuleAnalysis.findGlobalVariablesForAggregate(lit, dependencies));
		this.dependenciesById.put(id, dependencies);
		return id;
	}

	// Note: Thanks to type erasure we need a name other than "registerAggregateLiteral" here.
	private String internalRegisterAggregateLiteral(AggregateLiteral lit, Set<VariableTerm> globalVariables) {
		AggregateAtom atom = lit.getAtom();
		String id = atom.getAggregatefunction().toString().toLowerCase() + "_" + (this.idCounter.incrementAndGet());
		AggregateInfo info = new AggregateInfo(id, lit, globalVariables);
		this.aggregatesById.put(id, info);
		this.aggregateFunctionsToRewrite.putIfAbsent(new ImmutablePair<>(atom.getAggregatefunction(), atom.getLowerBoundOperator()), new HashSet<>());
		this.aggregateFunctionsToRewrite.get(new ImmutablePair<>(atom.getAggregatefunction(), atom.getLowerBoundOperator())).add(id);
		return id;
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
		return this.idCounter.get();
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

	// Should be private, but needs to be visible to tests.
	static class RuleAnalysis {

		final BasicRule rule;
		final Map<AggregateLiteral, Set<VariableTerm>> globalVariablesPerAggregate;
		final Map<AggregateLiteral, Set<Literal>> dependenciesPerAggregate;

		private RuleAnalysis(BasicRule rule, Map<AggregateLiteral, Set<VariableTerm>> globalVariablesPerAggregate,
				Map<AggregateLiteral, Set<Literal>> dependenciesPerAggregate) {
			this.rule = rule;
			this.globalVariablesPerAggregate = globalVariablesPerAggregate;
			this.dependenciesPerAggregate = dependenciesPerAggregate;
		}

		static RuleAnalysis analyzeRule(BasicRule rule) {
			Map<AggregateLiteral, Set<VariableTerm>> globalVarsPerAggregate = findGlobalVariablesForAggregates(rule);
			Map<AggregateLiteral, Set<Literal>> dependenciesPerAggregate = new HashMap<>();
			for (AggregateLiteral lit : globalVarsPerAggregate.keySet()) {
				Set<VariableTerm> nonBindingVars = new HashSet<>(globalVarsPerAggregate.get(lit));
				Term leftHandTerm = lit.getAtom().getLowerBoundTerm();
				if (lit.getBindingVariables().isEmpty() && leftHandTerm instanceof VariableTerm) {
					/*
					 * If the "left-hand" term LT of the literal is a variable and not binding, it has to be non-binding,
					 * i.e. the aggregate literal depends on the literals binding LT.
					 */
					nonBindingVars.add((VariableTerm) leftHandTerm);
				}
				Set<Literal> dependencies = new HashSet<>();
				Set<Literal> bodyWithoutLit = SetUtils.difference(rule.getBody(), Collections.singleton(lit));
				findBindingLiterals(nonBindingVars, new HashSet<>(), dependencies, bodyWithoutLit, globalVarsPerAggregate);
				dependenciesPerAggregate.put(lit, dependencies);
			}
			return new RuleAnalysis(rule, globalVarsPerAggregate, dependenciesPerAggregate);
		}

		/**
		 * Recursively looks for literals in <code>searchScope</code> that bind the variables in the set
		 * <code>varsToBind</code>, i.e. any literal lit that has any variable var in question in its
		 * <code>bindingVariables</code> (i.e. lit assigns a value to var). Found binding literals are added to the set
		 * <code>boundSoFar</code>. If a literal has any of the desired variables as a binding variable, but also has other
		 * non-binding variables, the literals binding these are added to the set of desired variables for the next recursive
		 * call. Since {@link AggregateLiteral}s cannot report their non-binding variables by themselves, this method also needs
		 * a map of all aggregate literals and their global variables within the search scope.
		 */
		// Note: This algorithm has potentially exponential time complexity. Tuning potential definitely exists, but
		// performance optimization seems non-trivial.
		private static void findBindingLiterals(Set<VariableTerm> varsToBind, Set<VariableTerm> varsBoundSoFar, Set<Literal> foundSoFar,
				Set<Literal> searchScope,
				Map<AggregateLiteral, Set<VariableTerm>> aggregatesWithGlobalVars) {
			int newlyBoundVars = 0;
			Set<VariableTerm> furtherVarsToBind = new HashSet<>();
			for (VariableTerm varToBind : varsToBind) {
				for (Literal lit : searchScope) {
					Set<VariableTerm> bindingVars = lit.getBindingVariables();
					Set<VariableTerm> nonBindingVars = (lit instanceof AggregateLiteral) ? aggregatesWithGlobalVars.get((AggregateLiteral) lit)
							: lit.getNonBindingVariables();
					if (bindingVars.contains(varToBind)) {
						varsBoundSoFar.add(varToBind);
						foundSoFar.add(lit);
						newlyBoundVars++;
						for (VariableTerm nonBindingVar : nonBindingVars) {
							if (!varsBoundSoFar.contains(nonBindingVar)) {
								furtherVarsToBind.add(nonBindingVar);
							}
						}
					}
				}
			}
			if (newlyBoundVars == 0 && !varsToBind.isEmpty()) {
				// Sanity check to prevent endless recursions: If because of weird cyclic dependencies we end up with unbound variables,
				// but couldn't find any binding literals in the last run, it seems we're running in circles. Better to give up
				// screaming than producing a stack overflow ;-)
				throw new IllegalStateException("Couldn't find any literals binding variables: " + varsToBind + " in search scope " + searchScope);
			}
			if (!furtherVarsToBind.isEmpty()) {
				// As long as we find variables we still need to bind, repeat with the new set of variables to bind.
				findBindingLiterals(furtherVarsToBind, varsBoundSoFar, foundSoFar, searchScope, aggregatesWithGlobalVars);
			}
		}

		/**
		 * Calculates the set of global variables of each {@link AggregateLiteral} in the given rule.
		 * 
		 * @param rule
		 * @return
		 */
		// NOTE: This method has quadratic complexity - With appropriate data structures for bookkeeping it should be possible
		// to cut this down to linear time.
		private static Map<AggregateLiteral, Set<VariableTerm>> findGlobalVariablesForAggregates(BasicRule rule) {
			Map<AggregateLiteral, Set<VariableTerm>> globalVarsPerAggregate = new HashMap<>();
			for (Literal lit : rule.getBody()) {
				if (lit instanceof AggregateLiteral) {
					AggregateLiteral aggLit = (AggregateLiteral) lit;
					Set<Literal> bodyWithoutLit = SetUtils.difference(rule.getBody(), Collections.singleton(aggLit));
					globalVarsPerAggregate.put(aggLit, findGlobalVariablesForAggregate(aggLit, bodyWithoutLit));
				}
			}
			return globalVarsPerAggregate;
		}

		/**
		 * nullCalculates a set of global variables for the given {@link AggregateLiteral} based on a specific set of literals
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
		private static Set<VariableTerm> findGlobalVariablesForAggregate(AggregateLiteral aggregate, Set<Literal> scope) {
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

	}

}
