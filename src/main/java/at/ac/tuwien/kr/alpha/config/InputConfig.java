package at.ac.tuwien.kr.alpha.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import at.ac.tuwien.kr.alpha.common.Predicate;
import at.ac.tuwien.kr.alpha.common.fixedinterpretations.PredicateInterpretation;

public class InputConfig {

	public static final java.util.function.Predicate<Predicate> DEFAULT_FILTER = p -> true;
	public static final boolean DEFAULT_LITERATE = false;
	public static final int DEFAULT_NUM_ANSWER_SETS = 0;
	public static final boolean DEFAULT_WRITE_DEPENDENCY_GRAPH = false;
	public static final String DEFAULT_DEPGRAPH_TARGET = "depgraph.dot";
	public static final boolean DEFAULT_WRITE_COMPONENT_GRAPH = false;
	public static final String DEFAULT_COMPGRAPH_TARGET = "compgraph.dot";
	public static final boolean DEFAULT_WRITE_PREPROCESSED_PROG = false;
	public static final String DEFAULT_PREPROC_TARGET = "input.preproc.asp";
        public static final boolean DEFAULT_WRITE_XLSX = false;
	public static final String DEFAULT_OUTFILE_PATH = "alphaAnswerSet"; // current directory, files named "alphaAnswerSet.{num}.{ext}"

	private List<String> aspStrings = new ArrayList<>();
	private List<String> files = new ArrayList<>();
	private boolean literate = InputConfig.DEFAULT_LITERATE;
	private int numAnswerSets = InputConfig.DEFAULT_NUM_ANSWER_SETS;
	private Set<String> desiredPredicates = new HashSet<>();
	private Map<String, PredicateInterpretation> predicateMethods = new HashMap<>();
	private boolean writeDependencyGraph = InputConfig.DEFAULT_WRITE_DEPENDENCY_GRAPH;
	private String depgraphPath = InputConfig.DEFAULT_DEPGRAPH_TARGET;
	private boolean writeComponentGraph = InputConfig.DEFAULT_WRITE_COMPONENT_GRAPH;
	private String compgraphPath = InputConfig.DEFAULT_COMPGRAPH_TARGET;
	private boolean writePreprocessed = InputConfig.DEFAULT_WRITE_PREPROCESSED_PROG;
	private String preprocessedPath = InputConfig.DEFAULT_PREPROC_TARGET;
        private boolean writeAnswerSetsAsXlsx = InputConfig.DEFAULT_WRITE_XLSX;
	private String answerSetFileOutputPath;

	public static InputConfig forString(String str) {
		InputConfig retVal = new InputConfig();
		retVal.aspStrings.add(str);
		return retVal;
	}

	public List<String> getAspStrings() {
		return this.aspStrings;
	}

	public void setAspStrings(List<String> aspStrings) {
		this.aspStrings = aspStrings;
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
		return this.desiredPredicates.isEmpty() ? InputConfig.DEFAULT_FILTER : p -> this.desiredPredicates.contains(p.getName());
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

	public Set<String> getDesiredPredicates() {
		return this.desiredPredicates;
	}

	public void setDesiredPredicates(Set<String> desiredPredicates) {
		this.desiredPredicates = desiredPredicates;
	}

	public boolean isWriteDependencyGraph() {
		return this.writeDependencyGraph;
	}

	public void setWriteDependencyGraph(boolean writeDependencyGraph) {
		this.writeDependencyGraph = writeDependencyGraph;
	}

	public boolean isWriteComponentGraph() {
		return this.writeComponentGraph;
	}

	public void setWriteComponentGraph(boolean writeComponentGraph) {
		this.writeComponentGraph = writeComponentGraph;
	}

	public boolean isWritePreprocessed() {
		return this.writePreprocessed;
	}

	public void setWritePreprocessed(boolean writePreprocessed) {
		this.writePreprocessed = writePreprocessed;
	}

	public String getDepgraphPath() {
		return this.depgraphPath;
	}

	public void setDepgraphPath(String depgraphPath) {
		this.depgraphPath = depgraphPath;
	}

	public String getCompgraphPath() {
		return this.compgraphPath;
	}

	public void setCompgraphPath(String compgraphPath) {
		this.compgraphPath = compgraphPath;
	}

	public String getPreprocessedPath() {
		return this.preprocessedPath;
	}

	public void setPreprocessedPath(String preprocessedPath) {
		this.preprocessedPath = preprocessedPath;
	}
	public boolean isWriteAnswerSetsAsXlsx() {
		return this.writeAnswerSetsAsXlsx;
	}

	public void setWriteAnswerSetsAsXlsx(boolean writeAnswerSetsAsXslx) {
		this.writeAnswerSetsAsXlsx = writeAnswerSetsAsXslx;
	}

	public String getAnswerSetFileOutputPath() {
		return this.answerSetFileOutputPath;
	}

	public void setAnswerSetFileOutputPath(String answerSetFileOutputPath) {
		this.answerSetFileOutputPath = answerSetFileOutputPath;
	}

}
