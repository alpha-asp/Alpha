package at.ac.tuwien.kr.alpha.common.depgraph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.tuwien.kr.alpha.common.depgraph.ComponentGraph.SCComponent;

public class StratificationHelper {

	private static final Logger LOGGER = LoggerFactory.getLogger(StratificationHelper.class);

	private ComponentGraph componentGraph;

	private Map<Integer, Integer> strataByComponentId;
	private Map<Integer, Boolean> componentStratifyability;

	private Map<Integer, List<SCComponent>> strata;

	private void reset() {
		this.componentGraph = null;
		this.componentStratifyability = new HashMap<>();
		this.strataByComponentId = new HashMap<>();
		this.strata = new HashMap<>();
	}

	public Map<Integer, List<SCComponent>> calculateStratification(ComponentGraph cg) {
		this.reset();
		this.componentGraph = cg;
		LOGGER.debug("Initial call to stratify with entry points!");
		this.stratify(new HashSet<>(this.componentGraph.getEntryPoints()));
		return this.strata;
	}

	private void stratify(Set<SCComponent> currComponents) {
		Set<SCComponent> nextComps = new HashSet<>();
		LOGGER.debug("Starting stratify run - currComponents = {}", StringUtils.join(currComponents, ","));
		for (SCComponent comp : currComponents) {
			for (int nextComponentId : this.stratifyComponent(comp)) {
				nextComps.add(this.componentGraph.getComponents().get(nextComponentId));
			}
		}
		if (!nextComps.isEmpty()) {
			this.stratify(nextComps);
		} else {
			LOGGER.debug("Stratification finished - no more components to work off!");
		}
	}

	private Set<Integer> stratifyComponent(SCComponent comp) {
		Set<Integer> retVal = new HashSet<>();
		Map<Integer, Boolean> dependencies = comp.getDependencyIds();
		int stratum = 0;
		boolean canStratify = true;
		SCComponent dep = null;
		if (comp.hasNegativeCycle()) {
			// no need to check dependencies if we aren't stratifyable
			this.markUnstratifyable(comp);
			canStratify = false;
		} else if (this.getStratumFor(comp) != null) {
			// component already has a stratum, i.e. we reached it via a different path first.
			// no need to do anything further
			canStratify = false;
		} else {
			for (Entry<Integer, Boolean> depEntry : dependencies.entrySet()) {
				dep = this.componentGraph.getComponents().get(depEntry.getKey());
				if (this.getStratumFor(dep) == null) {
					// NOT breaking out of loop here, need to make sure unstratifyability is propagated
					canStratify = false;
					if (this.isUnstratifyable(dep)) {
						this.markUnstratifyable(comp);
					}
				} else {
					stratum = (this.getStratumFor(dep) > stratum) ? this.getStratumFor(dep) : stratum;
					if (depEntry.getValue().equals(false)) {
						stratum++;
					}
				}
			}
		}
		if (canStratify) {
			this.setStratumFor(comp, stratum);
		}
		if (canStratify || this.isUnstratifyable(comp)) {
			// set up dependent components for next step
			// also do this for unstratifyable components since we need to propagate unstratifyability
			// NOTE: this will lead to every node in the graph being explored
			retVal = comp.getDependentIds();
		}
		return retVal;
	}

	private boolean isUnstratifyable(SCComponent comp) {
		return Boolean.FALSE.equals(this.componentStratifyability.get(comp.getId()));
	}

	private void markUnstratifyable(SCComponent comp) {
		this.componentStratifyability.put(comp.getId(), false);
	}

	private void setStratumFor(SCComponent comp, int stratum) {
		this.strata.putIfAbsent(stratum, new ArrayList<>());
		this.strata.get(stratum).add(comp);
		this.strataByComponentId.put(comp.getId(), stratum);
	}

	private Integer getStratumFor(SCComponent comp) {
		return this.strataByComponentId.get(comp.getId());
	}

}
