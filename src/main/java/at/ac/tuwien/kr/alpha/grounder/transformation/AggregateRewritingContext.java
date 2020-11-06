package at.ac.tuwien.kr.alpha.grounder.transformation;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import at.ac.tuwien.kr.alpha.common.atoms.AggregateAtom;
import at.ac.tuwien.kr.alpha.common.atoms.AggregateAtom.AggregateFunctionSymbol;
import at.ac.tuwien.kr.alpha.common.atoms.AggregateLiteral;
import at.ac.tuwien.kr.alpha.common.atoms.BasicAtom;
import at.ac.tuwien.kr.alpha.common.atoms.Literal;
import at.ac.tuwien.kr.alpha.common.rule.BasicRule;
import at.ac.tuwien.kr.alpha.common.terms.ConstantTerm;

public class AggregateRewritingContext {

	private int idCounter;
	private Map<AggregateLiteral, String> aggregateIds = new HashMap<>();
	private Map<AggregateLiteral, BasicAtom> aggregateOutputAtoms = new HashMap<>();
	private Map<AggregateFunctionSymbol, Set<AggregateLiteral>> aggregateFunctionsToRewrite = new HashMap<>();

	public boolean registerRule(BasicRule rule) {
		for (Literal lit : rule.getBody()) {
			if (lit instanceof AggregateLiteral) {
				this.registerAggregateLiteral((AggregateLiteral) lit, rule);
				return true;
			}
		}
		return false;
	}

	public void registerAggregateLiteral(AggregateLiteral lit, BasicRule source) {
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
		return new BasicAtom(AggregateRewriting.AGGREGATE_RESULT, ConstantTerm.getSymbolicInstance(aggregateId), atom.getLowerBoundTerm());
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
