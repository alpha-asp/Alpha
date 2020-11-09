package at.ac.tuwien.kr.alpha.grounder.transformation;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.stringtemplate.v4.ST;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import at.ac.tuwien.kr.alpha.common.ComparisonOperator;
import at.ac.tuwien.kr.alpha.common.Predicate;
import at.ac.tuwien.kr.alpha.common.atoms.AggregateAtom;
import at.ac.tuwien.kr.alpha.common.atoms.AggregateAtom.AggregateFunctionSymbol;
import at.ac.tuwien.kr.alpha.common.atoms.AggregateLiteral;
import at.ac.tuwien.kr.alpha.common.atoms.BasicAtom;
import at.ac.tuwien.kr.alpha.common.atoms.Literal;
import at.ac.tuwien.kr.alpha.common.rule.BasicRule;

public final class AggregateRewritingContext {

	private static final ST AGGREGATE_RESULT_TEMPLATE = new ST("<id>_result");

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
				String aggregateId = this.registerAggregateLiteral((AggregateLiteral) lit);
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
		String id = this.registerAggregateLiteral(lit);
		this.dependenciesById.put(id, dependencies);
		return id;
	}

	private String registerAggregateLiteral(AggregateLiteral lit) {
		AggregateAtom atom = lit.getAtom();
		String id = atom.getAggregatefunction().toString().toLowerCase() + "_" + (++this.idCounter);
		AggregateInfo info = new AggregateInfo(id, lit, this.buildAggregateOutputAtom(id, atom));
		this.aggregatesById.put(id, info);
		this.aggregateFunctionsToRewrite.putIfAbsent(new ImmutablePair<>(atom.getAggregatefunction(), atom.getLowerBoundOperator()), new HashSet<>());
		this.aggregateFunctionsToRewrite.get(new ImmutablePair<>(atom.getAggregatefunction(), atom.getLowerBoundOperator())).add(id);
		return id;
	}

	private BasicAtom buildAggregateOutputAtom(String aggregateId, AggregateAtom atom) {
		String outputPredicateName = new ST(AGGREGATE_RESULT_TEMPLATE).add("id", aggregateId).render();
		return new BasicAtom(Predicate.getInstance(outputPredicateName, 1), atom.getLowerBoundTerm());
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

		public AggregateInfo(String id, AggregateLiteral literal, BasicAtom outputAtom) {
			this.id = id;
			this.literal = literal;
			this.outputAtom = outputAtom;
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

	}

}
