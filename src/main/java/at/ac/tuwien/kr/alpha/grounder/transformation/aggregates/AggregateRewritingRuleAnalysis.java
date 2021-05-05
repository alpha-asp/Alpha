package at.ac.tuwien.kr.alpha.grounder.transformation.aggregates;

import at.ac.tuwien.kr.alpha.common.atoms.AggregateAtom;
import at.ac.tuwien.kr.alpha.common.atoms.AggregateLiteral;
import at.ac.tuwien.kr.alpha.common.atoms.Literal;
import at.ac.tuwien.kr.alpha.common.rule.BasicRule;
import at.ac.tuwien.kr.alpha.common.rule.head.NormalHead;
import at.ac.tuwien.kr.alpha.common.terms.VariableTerm;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static at.ac.tuwien.kr.alpha.Util.oops;

/**
 * Analyses a rule and records occurring aggregates and for each aggregate its global variables and its dependencies on
 * other literals.
 *
 * Copyright (c) 2021, the Alpha Team.
 */ // Should be private, but needs to be visible to tests.
class AggregateRewritingRuleAnalysis {

	private final BasicRule rule;
	final Map<AggregateLiteral, Set<VariableTerm>> globalVariablesPerAggregate = new LinkedHashMap<>();
	final Map<AggregateLiteral, Set<Literal>> dependenciesPerAggregate = new LinkedHashMap<>();
	final List<AggregateLiteral> aggregatesInRule = new ArrayList<>();

	private AggregateRewritingRuleAnalysis(BasicRule rule) {
		this.rule = rule;
	}

	/**
	 * Analyze the given rule and record its global variables and dependencies.
	 * @param rule
	 * @return
	 */
	static AggregateRewritingRuleAnalysis analyzeRule(BasicRule rule) {
		AggregateRewritingRuleAnalysis ruleAnalysis = new AggregateRewritingRuleAnalysis(rule);
		ruleAnalysis.findGlobalVariablesPerAggregate();
		ruleAnalysis.computeDependencies();
		return ruleAnalysis;
	}

	private void computeDependencies() {
		// Treat special case where rule has only one aggregate.
		if (aggregatesInRule.size() == 1) {
			AggregateLiteral singleAggregateLiteral = aggregatesInRule.get(0);
			dependenciesPerAggregate.put(singleAggregateLiteral, bodyMinusAggregates());
			return;
		}

		Set<VariableTerm> variablesBoundByBodyMinusAggregates = bindingVariablesInLiterals(bodyMinusAggregates());
		// Check for special case with two aggregates binding the same variable, which is not guaranteed to yield correct results.
		// Note: this case can be handled once we have an order of evaluation from the safety check, which can then be considered below.
		checkForTwoAggregatesSolelyBindingOneVariable(variablesBoundByBodyMinusAggregates);

		// Rule has multiple aggregates: dependencies are the whole body except aggregates plus those aggregates solely binding a needed variable.
		for (AggregateLiteral aggregateLiteral : aggregatesInRule) {
			Set<VariableTerm> unboundGlobalVariables = new HashSet<>(globalVariablesPerAggregate.get(aggregateLiteral));
			unboundGlobalVariables.removeAll(variablesBoundByBodyMinusAggregates);
			Set<Literal> aggregateDependencies = bodyMinusAggregates();
			// Search other aggregates for those that bind some needed but unbound variables.
			for (AggregateLiteral otherAggregateLiteral : aggregatesInRule) {
				if (otherAggregateLiteral == aggregateLiteral) {
					continue;	// Skip aggregate itself.
				}
				// If no unbound variables remain, stop search.
				if (unboundGlobalVariables.isEmpty()) {
					break;
				}
				// If other aggregate binds a variable, that is needed for this one, add it to dependencies.
				if (!otherAggregateLiteral.getBindingVariables().isEmpty()) {
					if (otherAggregateLiteral.getBindingVariables().size() != 1) {
						throw oops("AggregateLiteral has more than one binding variable.");
					}
					VariableTerm boundVariable = otherAggregateLiteral.getBindingVariables().iterator().next();
					if (unboundGlobalVariables.contains(boundVariable)) {
						// Observe that due to the above special case check, only one other aggregate can bind boundVariable here.
						unboundGlobalVariables.remove(boundVariable);
						aggregateDependencies.add(otherAggregateLiteral);
					}
				}
			}
			if (!unboundGlobalVariables.isEmpty()) {
				throw oops("Encountered AggregateLiteral in rule whose variables are not fully bound by the remaining body.");
			}
			dependenciesPerAggregate.put(aggregateLiteral, aggregateDependencies);
		}
	}

	private void checkForTwoAggregatesSolelyBindingOneVariable(Set<VariableTerm> variablesBoundByBodyMinusAggregates) {
		if (aggregatesInRule.size() < 2) {
			// Only if at least 2 aggregates are present, uncovered case can be hit.
			return;
		}
		Set<VariableTerm> variablesBoundByAggregates = new HashSet<>();
		for (AggregateLiteral aggregateLiteral : aggregatesInRule) {
			if (!aggregateLiteral.getBindingVariables().isEmpty()) {
				VariableTerm boundVariable = aggregateLiteral.getBindingVariables().iterator().next();
				if (!variablesBoundByBodyMinusAggregates.contains(boundVariable)) {
					// Variable is solely bound by aggregate(s).
					if (variablesBoundByAggregates.contains(boundVariable)) {
						throw new UnsupportedOperationException("Rule with multiple aggregates that bind the same variable encountered." +
							"This case is not supported yet, try rewriting the rule: " + rule.toString());
					} else {
						variablesBoundByAggregates.add(boundVariable);
					}
				}
			}
		}
	}

	/**
	 * Computes all the body literals of the rule except aggregate literals.
	 * @return a new set containing all body literals of the rule except aggregate literals.
	 */
	private Set<Literal> bodyMinusAggregates() {
		Set<Literal> bodyMinus = new LinkedHashSet<>();
		for (Literal literal : rule.getBody()) {
			if (literal instanceof AggregateLiteral) {
				continue;
			}
			bodyMinus.add(literal);
		}
		return bodyMinus;
	}

	/**
	 * Computes the binding variables of a set of given literals (considered to be body literals in a rule).
	 * @param literals the set of (body) literals.
	 * @return the set of variables bound by the given literals.
	 */
	private Set<VariableTerm> bindingVariablesInLiterals(Set<Literal> literals) {
		Set<VariableTerm> bindingVariables = new LinkedHashSet<>();
		for (Literal literal : literals) {
			bindingVariables.addAll(literal.getBindingVariables());
		}
		return bindingVariables;
	}

	private void findGlobalVariablesPerAggregate() {
		// First, compute all global variables, that is all variables occurring in a rule except those occurring
		// inside aggregate elements.
		Set<VariableTerm> globalVariables = new HashSet<>();
		if (!rule.isConstraint()) {
			NormalHead head = (NormalHead)rule.getHead();	// Head must be normal at this point.
			globalVariables.addAll(head.getAtom().getOccurringVariables());
		}
		for (Literal literal : rule.getBody()) {
			if (literal instanceof AggregateLiteral) {
				aggregatesInRule.add((AggregateLiteral) literal);
				AggregateAtom aggregateAtom = (AggregateAtom) literal.getAtom();
				// All variables in the bounds of an aggregate are also global variables.
				// Note that at this point, only lower bounds appear in aggregates.
				globalVariables.addAll(aggregateAtom.getLowerBoundTerm().getOccurringVariables());
			} else {
				globalVariables.addAll(literal.getOccurringVariables());
			}
		}
		// Second, compute for each aggregate those of its variables that are global.
		for (AggregateLiteral aggregateLiteral : aggregatesInRule) {
			Set<VariableTerm> globalVariablesInAggregate = new HashSet<>();
			for (VariableTerm aggregateVariable : aggregateLiteral.getAtom().getAggregateVariables()) {
				if (globalVariables.contains(aggregateVariable)) {
					globalVariablesInAggregate.add(aggregateVariable);
				}
			}
			globalVariablesPerAggregate.put(aggregateLiteral, globalVariablesInAggregate);
		}

	}

}
