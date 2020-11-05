package at.ac.tuwien.kr.alpha.grounder.transformation;

public class AggregateRewritingConfig {

	private final boolean useSortingCircuitEncoding;

	// TODO should be builder
	public AggregateRewritingConfig(boolean useSortingCircuit) {
		this.useSortingCircuitEncoding = useSortingCircuit;
	}

	public boolean isUseSortingCircuitEncoding() {
		return this.useSortingCircuitEncoding;
	}

}
