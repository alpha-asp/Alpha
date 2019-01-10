package at.ac.tuwien.kr.alpha.config;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import at.ac.tuwien.kr.alpha.common.Predicate;
import at.ac.tuwien.kr.alpha.common.fixedinterpretations.PredicateInterpretation;

public class InputConfig {

	public static final java.util.function.Predicate<Predicate> DEFAULT_FILTER = p -> true;
	public static final boolean DEFAULT_LITERATE = false;
	public static final int DEFAULT_NUM_ANSWER_SETS = 0;
	public static final boolean DEFAULT_WRITE_DEPENDENCY_GRAPH = false;
	public static final String DEFAULT_DEPGRAPH_PATH = "dep-graph.dot";

	public enum InputSource {
		STRING, FILE;
	}

	private InputSource source;
	private String aspString;
	private List<String> files = new ArrayList<>();
	private boolean literate = InputConfig.DEFAULT_LITERATE;
	private int numAnswerSets = InputConfig.DEFAULT_NUM_ANSWER_SETS;
	private java.util.function.Predicate<Predicate> filter = InputConfig.DEFAULT_FILTER;
	private Map<String, PredicateInterpretation> predicateMethods = new HashMap<>();
	private boolean writeDependencyGraph = InputConfig.DEFAULT_WRITE_DEPENDENCY_GRAPH;
	private OutputStream depGraphTarget;

	public static InputConfig forString(String str) {
		InputConfig retVal = new InputConfig();
		retVal.setSource(InputSource.STRING);
		retVal.setAspString(str);
		return retVal;
	}

	public InputSource getSource() {
		return this.source;
	}

	public void setSource(InputSource source) {
		this.source = source;
	}

	public String getAspString() {
		return this.aspString;
	}

	public void setAspString(String aspString) {
		this.aspString = aspString;
	}

	public boolean isLiterate() {
		return this.literate;
	}

	public void setLiterate(boolean literate) {
		this.literate = literate;
	}

	public int getNumAnswerSets() {
		return this.numAnswerSets;
	}

	public void setNumAnswerSets(int numAnswerSets) {
		this.numAnswerSets = numAnswerSets;
	}

	public java.util.function.Predicate<Predicate> getFilter() {
		return this.filter;
	}

	public void setFilter(java.util.function.Predicate<Predicate> filter) {
		this.filter = filter;
	}

	public Map<String, PredicateInterpretation> getPredicateMethods() {
		return this.predicateMethods;
	}

	public void setPredicateMethods(Map<String, PredicateInterpretation> predicateMethods) {
		this.predicateMethods = predicateMethods;
	}

	public void addPredicateMethod(String name, PredicateInterpretation interpretation) {
		this.predicateMethods.put(name, interpretation);
	}

	public List<String> getFiles() {
		return this.files;
	}

	public void setFiles(List<String> files) {
		this.files = files;
	}

	public boolean isWriteDependencyGraph() {
		return this.writeDependencyGraph;
	}

	public void setWriteDependencyGraph(boolean writeDependencyGraph) {
		this.writeDependencyGraph = writeDependencyGraph;
	}

	public OutputStream getDepGraphTarget() {
		return this.depGraphTarget;
	}

	public void setDepGraphTarget(OutputStream depGraphTarget) {
		this.depGraphTarget = depGraphTarget;
	}

}
