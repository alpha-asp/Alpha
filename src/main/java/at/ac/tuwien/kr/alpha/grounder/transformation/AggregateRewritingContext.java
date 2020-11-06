package at.ac.tuwien.kr.alpha.grounder.transformation;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import at.ac.tuwien.kr.alpha.common.atoms.AggregateAtom.AggregateFunctionSymbol;
import at.ac.tuwien.kr.alpha.common.atoms.BasicAtom;
import at.ac.tuwien.kr.alpha.common.atoms.Literal;
import at.ac.tuwien.kr.alpha.common.atoms.RestrictedAggregateAtom;
import at.ac.tuwien.kr.alpha.common.atoms.RestrictedAggregateLiteral;
import at.ac.tuwien.kr.alpha.common.rule.BasicRule;
import at.ac.tuwien.kr.alpha.common.terms.ConstantTerm;

public class AggregateRewritingContext {

	private int idCounter;
	private Map<RestrictedAggregateLiteral, String> aggregateIds = new HashMap<>();
	private Map<RestrictedAggregateLiteral, BasicAtom> aggregateOutputAtoms = new HashMap<>();
	private Map<AggregateFunctionSymbol, Set<RestrictedAggregateLiteral>> aggregateFunctionsToRewrite = new HashMap<>();

	public boolean registerRule(BasicRule rule) {
		for (Literal lit : rule.getBody()) {
			if (lit instanceof RestrictedAggregateLiteral) {
				this.registerAggregateLiteral((RestrictedAggregateLiteral) lit, rule);
				return true;
			}
		}
		return false;
	}

	public void registerAggregateLiteral(RestrictedAggregateLiteral lit, BasicRule source) {
		if (this.aggregateIds.containsKey(lit)) {
			return;
		}
		RestrictedAggregateAtom atom = lit.getAtom();
		String id = atom.getAggregatefunction().toString().toLowerCase() + "_" + (++this.idCounter);
		this.aggregateIds.put(lit, id);
		this.aggregateOutputAtoms.put(lit, this.buildAggregateOutputAtom(id, atom));
		this.aggregateFunctionsToRewrite.putIfAbsent(atom.getAggregatefunction(), new HashSet<>());
		this.aggregateFunctionsToRewrite.get(atom.getAggregatefunction()).add(lit);
	}

	private BasicAtom buildAggregateOutputAtom(String aggregateId, RestrictedAggregateAtom atom) {
		return new BasicAtom(AggregateRewriting.AGGREGATE_RESULT, ConstantTerm.getSymbolicInstance(aggregateId), atom.getLowerBoundTerm());
	}

	public String getAggregateId(RestrictedAggregateLiteral lit) {
		return this.aggregateIds.get(lit);
	}

	public BasicAtom getAggregateOutputAtom(RestrictedAggregateLiteral lit) {
		return this.aggregateOutputAtoms.get(lit);
	}

	public Set<RestrictedAggregateLiteral> getLiteralsToRewrite() {
		return Collections.unmodifiableSet(this.aggregateIds.keySet());
	}

	public int numAggregatesToRewrite() {
		return this.idCounter;
	}

	public Map<AggregateFunctionSymbol, Set<RestrictedAggregateLiteral>> getAggregateFunctionsToRewrite() {
		return Collections.unmodifiableMap(this.aggregateFunctionsToRewrite);
	}
}
