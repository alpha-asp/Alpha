package at.ac.tuwien.kr.alpha.grounder.transformation;

import org.stringtemplate.v4.ST;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import at.ac.tuwien.kr.alpha.common.Predicate;
import at.ac.tuwien.kr.alpha.common.atoms.AggregateAtom;
import at.ac.tuwien.kr.alpha.common.atoms.AggregateAtom.AggregateFunctionSymbol;
import at.ac.tuwien.kr.alpha.common.atoms.AggregateLiteral;
import at.ac.tuwien.kr.alpha.common.atoms.BasicAtom;
import at.ac.tuwien.kr.alpha.common.atoms.Literal;
import at.ac.tuwien.kr.alpha.common.rule.BasicRule;

public class AggregateRewritingContext {

	private static final ST AGGREGATE_RESULT_TEMPLATE = new ST("<id>_result");

	private int idCounter;
	private Map<AggregateLiteral, String> aggregateIds = new HashMap<>();
	private Map<AggregateLiteral, BasicAtom> aggregateOutputAtoms = new HashMap<>();
	private Map<AggregateFunctionSymbol, Set<AggregateLiteral>> aggregateFunctionsToRewrite = new HashMap<>();

	public AggregateRewritingContext() {
	}

	public AggregateRewritingContext(AggregateRewritingContext ctx) {
		this.idCounter = ctx.idCounter;
		this.aggregateIds = new HashMap<>(ctx.aggregateIds);
		this.aggregateOutputAtoms = new HashMap<>(ctx.aggregateOutputAtoms);
		this.aggregateFunctionsToRewrite = new HashMap<>();
		for (Map.Entry<AggregateFunctionSymbol, Set<AggregateLiteral>> entry : ctx.aggregateFunctionsToRewrite.entrySet()) {
			this.aggregateFunctionsToRewrite.put(entry.getKey(), new HashSet<>(entry.getValue()));
		}
	}

	public boolean registerRule(BasicRule rule) {
		for (Literal lit : rule.getBody()) {
			if (lit instanceof AggregateLiteral) {
				this.registerAggregateLiteral((AggregateLiteral) lit);
				return true;
			}
		}
		return false;
	}

	public void registerAggregateLiteral(AggregateLiteral lit) {
		if (this.aggregateIds.containsKey(lit)) {
			return;
		}
		AggregateAtom atom = lit.getAtom();
		String id = atom.getAggregatefunction().toString().toLowerCase() + "_" + (++this.idCounter);
		this.aggregateIds.put(lit, id);
		this.aggregateOutputAtoms.put(lit, this.buildAggregateOutputAtom(id, atom));
		this.aggregateFunctionsToRewrite.putIfAbsent(atom.getAggregatefunction(), new HashSet<>());
		this.aggregateFunctionsToRewrite.get(atom.getAggregatefunction()).add(lit);
	}

	private BasicAtom buildAggregateOutputAtom(String aggregateId, AggregateAtom atom) {
		String outputPredicateName = new ST(AGGREGATE_RESULT_TEMPLATE).add("id", aggregateId).render();
		return new BasicAtom(Predicate.getInstance(outputPredicateName, 1), atom.getLowerBoundTerm());
	}

	public String getAggregateId(AggregateLiteral lit) {
		return this.aggregateIds.get(lit);
	}

	public BasicAtom getAggregateOutputAtom(AggregateLiteral lit) {
		return this.aggregateOutputAtoms.get(lit);
	}

	public Set<AggregateLiteral> getLiteralsToRewrite() {
		return Collections.unmodifiableSet(this.aggregateIds.keySet());
	}

	public int numAggregatesToRewrite() {
		return this.idCounter;
	}

	public Map<AggregateFunctionSymbol, Set<AggregateLiteral>> getAggregateFunctionsToRewrite() {
		return Collections.unmodifiableMap(this.aggregateFunctionsToRewrite);
	}
}
