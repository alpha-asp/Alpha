package at.ac.tuwien.kr.alpha.common.depgraph;

import java.util.List;
import java.util.Map;

import at.ac.tuwien.kr.alpha.common.depgraph.ComponentGraph.SCComponent;

public class StratificationAnalysis {

	private List<SCComponent> componentHandlingOrder;
	private Map<Integer, List<SCComponent>> strata;

	public List<SCComponent> getComponentHandlingOrder() {
		return this.componentHandlingOrder;
	}

	public void setComponentHandlingOrder(List<SCComponent> componentHandlingOrder) {
		this.componentHandlingOrder = componentHandlingOrder;
	}

	public Map<Integer, List<SCComponent>> getStrata() {
		return this.strata;
	}

	public void setStrata(Map<Integer, List<SCComponent>> strata) {
		this.strata = strata;
	}

	public int getNumStratfiedComponents() {
		return this.componentHandlingOrder.size();
	}

}
