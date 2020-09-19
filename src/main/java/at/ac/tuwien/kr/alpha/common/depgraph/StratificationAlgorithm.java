/**
 * Copyright (c) 2019, the Alpha Team.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1) Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 *
 * 2) Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package at.ac.tuwien.kr.alpha.common.depgraph;

import at.ac.tuwien.kr.alpha.common.depgraph.ComponentGraph.SCComponent;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * Helper class for finding stratifications on a given {@link ComponentGraph}.
 * 
 * Copyright (c) 2019, the Alpha Team.
 */
public class StratificationAlgorithm {

	private static final Logger LOGGER = LoggerFactory.getLogger(StratificationAlgorithm.class);

	private ComponentGraph componentGraph;

	private Map<Integer, Integer> strataByComponentId;
	private Map<Integer, Boolean> componentStratifyability;

	private Map<Integer, List<SCComponent>> strata;

	private StratificationAlgorithm(ComponentGraph cg) {
		componentGraph = cg;
		componentStratifyability = new HashMap<>();
		strataByComponentId = new HashMap<>();
		strata = new HashMap<>();
	}

	/**
	 * Calculates a stratification covering as much as possible (the maximal stratifiable part) of the given component graph, such that:
	 * <p>
	 * <ul>
	 * <li>components depending only on facts (i.e. components with no dependencies) are on the lowest stratum
	 * <li>every component within the stratification depends only on components that are also part of the stratification themselves
	 * </ul>
	 * <p>
	 * 
	 * @param cg the component graph to stratify
	 * @return a map representing a valid stratification with respect to the criteria above, where the key is the zero-based index of the stratum
	 */
	public static Map<Integer, List<SCComponent>> calculateStratification(ComponentGraph cg) {
		return new StratificationAlgorithm(cg).runStratification();
	}


	private Map<Integer, List<SCComponent>> runStratification() {
		LOGGER.debug("Initial call to stratify with entry points!");
		stratify(new HashSet<>(componentGraph.getEntryPoints()));
		return strata;
	}

	private void stratify(Set<SCComponent> currComponents) {
		Set<SCComponent> nextComps = new HashSet<>();
		LOGGER.debug("Starting stratify run - currComponents = {}", StringUtils.join(currComponents, ","));
		for (SCComponent comp : currComponents) {
			for (int nextComponentId : stratifyComponent(comp)) {
				nextComps.add(componentGraph.getComponents().get(nextComponentId));
			}
		}
		if (!nextComps.isEmpty()) {
			stratify(nextComps);
		} else {
			LOGGER.debug("Stratification finished - no more components to work off!");
		}
	}

	private Set<Integer> stratifyComponent(SCComponent comp) {
		Set<Integer> retVal = new HashSet<>();
		Map<Integer, Boolean> dependencies = comp.getDependencyIds();
		int stratum = 0;
		boolean canStratify = true;
		SCComponent dep;
		if (comp.hasNegativeCycle()) {
			// no need to check dependencies if we aren't stratifyable
			markUnstratifyable(comp);
			canStratify = false;
		} else if (getStratumFor(comp) != null) {
			// component already has a stratum, i.e. we reached it via a different path first.
			// no need to do anything further
			canStratify = false;
		} else {
			for (Entry<Integer, Boolean> depEntry : dependencies.entrySet()) {
				dep = componentGraph.getComponents().get(depEntry.getKey());
				if (getStratumFor(dep) == null) {
					// NOT breaking out of loop here, need to make sure unstratifyability is propagated
					canStratify = false;
					if (isUnstratifyable(dep)) {
						markUnstratifyable(comp);
					}
				} else {
					stratum = (getStratumFor(dep) > stratum) ? getStratumFor(dep) : stratum;
					if (depEntry.getValue().equals(false)) {
						stratum++;
					}
				}
			}
		}
		if (canStratify) {
			setStratumFor(comp, stratum);
		}
		if (canStratify || isUnstratifyable(comp)) {
			// set up dependent components for next step
			// also do this for unstratifyable components since we need to propagate unstratifyability
			// NOTE: this will lead to every node in the graph being explored
			retVal = comp.getDependentIds();
		}
		return retVal;
	}

	private boolean isUnstratifyable(SCComponent comp) {
		return Boolean.FALSE == componentStratifyability.get(comp.getId());
	}

	private void markUnstratifyable(SCComponent comp) {
		componentStratifyability.put(comp.getId(), false);
	}

	private void setStratumFor(SCComponent comp, int stratum) {
		strata.putIfAbsent(stratum, new ArrayList<>());
		strata.get(stratum).add(comp);
		strataByComponentId.put(comp.getId(), stratum);
	}

	private Integer getStratumFor(SCComponent comp) {
		return strataByComponentId.get(comp.getId());
	}

}
