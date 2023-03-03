package at.ac.tuwien.kr.alpha.api.config;

import at.ac.tuwien.kr.alpha.api.Alpha;
import at.ac.tuwien.kr.alpha.api.AnswerSet;
import at.ac.tuwien.kr.alpha.api.common.fixedinterpretations.PredicateInterpretation;
import at.ac.tuwien.kr.alpha.api.programs.Predicate;
import at.ac.tuwien.kr.alpha.api.programs.analysis.ComponentGraph;
import at.ac.tuwien.kr.alpha.api.programs.analysis.DependencyGraph;

import java.util.*;

/**
 * Config structure for ASP input supplied to {@link Alpha} instances.
 * 
 * Copyright (c) 2021, the Alpha Team.
 */
public class InputConfig {

	public static final java.util.function.Predicate<Predicate> DEFAULT_FILTER = p -> true;
	public static final boolean DEFAULT_LITERATE = false;
	public static final int DEFAULT_NUM_ANSWER_SETS = 0;
	public static final boolean DEFAULT_DEBUG_PREPROCESSING = false;
	public static final String DEFAULT_DEPGRAPH_TARGET_FILE = "depgraph.dot";
	public static final String DEFAULT_COMPGRAPH_TARGET_FILE = "compgraph.dot";
	public static final String DEFAULT_NORMALIZED_TARGET_FILE = "input.normalized.asp";
	public static final String DEFAULT_PREPROC_TARGET_FILE = "input.preproc.asp";
	public static final boolean DEFAULT_WRITE_XLSX = false;
	public static final String DEFAULT_XLSX_OUTFILE_PATH = "alphaAnswerSet"; // current directory, files named "alphaAnswerSet.{num}.{ext}"
	public static final boolean DEFAULT_REIFY_INPUT = false;
	public static final boolean DEFAULT_RUN_TESTS = false;

	private List<String> aspStrings = new ArrayList<>();
	private List<String> files = new ArrayList<>();
	private boolean literate = InputConfig.DEFAULT_LITERATE;
	private int numAnswerSets = InputConfig.DEFAULT_NUM_ANSWER_SETS;
	private Set<String> desiredPredicates = new HashSet<>();
	private boolean debugPreprocessing = InputConfig.DEFAULT_DEBUG_PREPROCESSING;
	private String depgraphPath = InputConfig.DEFAULT_DEPGRAPH_TARGET_FILE;
	private String compgraphPath = InputConfig.DEFAULT_COMPGRAPH_TARGET_FILE;
	private String normalizedPath = InputConfig.DEFAULT_NORMALIZED_TARGET_FILE;
	private String preprocessedPath = InputConfig.DEFAULT_PREPROC_TARGET_FILE;
	private boolean writeAnswerSetsAsXlsx = InputConfig.DEFAULT_WRITE_XLSX;
	private String answerSetFileOutputPath;
	private boolean reifyInput = InputConfig.DEFAULT_REIFY_INPUT;
	private boolean runTests = InputConfig.DEFAULT_RUN_TESTS;

	private Map<String, PredicateInterpretation> predicateMethods = new HashMap<>();
	
	public static InputConfig forString(String str) {
		InputConfig retVal = new InputConfig();
		retVal.aspStrings.add(str);
		return retVal;
	}

	public List<String> getAspStrings() {
		return this.aspStrings;
	}

	/**
	 * Sets a list of strings constituitng valid ASP code which togehter make up (part of) an APS program.
	 */
	public void setAspStrings(List<String> aspStrings) {
		this.aspStrings = aspStrings;
	}

	public boolean isLiterate() {
		return this.literate;
	}

	/**
	 * Sets whether the ASP code in this input config should be treated as literate (i.e. comment- and code lines "switched").
	 */
	public void setLiterate(boolean literate) {
		this.literate = literate;
	}

	public int getNumAnswerSets() {
		return this.numAnswerSets;
	}

	/**
	 * Sets the number of {@link AnswerSet}s Alpha should caluclate for this input. (set zero to calculate all answer sets)
	 */
	public void setNumAnswerSets(int numAnswerSets) {
		this.numAnswerSets = numAnswerSets;
	}

	public java.util.function.Predicate<Predicate> getFilter() {
		return this.desiredPredicates.isEmpty() ? InputConfig.DEFAULT_FILTER : p -> this.desiredPredicates.contains(p.getName());
	}

	public Map<String, PredicateInterpretation> getPredicateMethods() {
		return this.predicateMethods;
	}

	/**
	 * Adds annotated java methods against which external atoms in the ASP input should be resolved.
	 */
	public void addPredicateMethods(Map<String, PredicateInterpretation> predicateMethods) {
		for (Map.Entry<String, PredicateInterpretation> entry : predicateMethods.entrySet()) {
			if (this.predicateMethods.containsKey(entry.getKey())) {
				throw new IllegalArgumentException("Input config already contains a predicate interpretation with name " + entry.getKey());
			}
			this.predicateMethods.put(entry.getKey(), entry.getValue());
		}
	}

	public void addPredicateMethod(String name, PredicateInterpretation interpretation) {
		this.predicateMethods.put(name, interpretation);
	}

	public List<String> getFiles() {
		return this.files;
	}

	/**
	 * Sets a list of files containing ASP code that make up (part of) the ASP program represented by this input config.
	 */
	public void setFiles(List<String> files) {
		this.files = files;
	}

	public Set<String> getDesiredPredicates() {
		return this.desiredPredicates;
	}

	/**
	 * Sets a set of predicates against which answer sets for this input should be filtered.
	 */
	public void setDesiredPredicates(Set<String> desiredPredicates) {
		this.desiredPredicates = desiredPredicates;
	}

	public String getDepgraphPath() {
		return this.depgraphPath;
	}

	/**
	 * Sets a path to which the {@link DependencyGraph} of the input program should be written. Note that
	 * {@link InputConfig#setDebugPreprocessing(boolean)} has to be active for this to take effect.
	 */
	public void setDepgraphPath(String depgraphPath) {
		this.depgraphPath = depgraphPath;
	}

	public String getCompgraphPath() {
		return this.compgraphPath;
	}

	/**
	 * Sets a path to which the {@link ComponentGraph} of the input program should be written. Note that
	 * {@link InputConfig#setDebugPreprocessing(boolean)} has to be active for this to take effect.
	 */
	public void setCompgraphPath(String compgraphPath) {
		this.compgraphPath = compgraphPath;
	}

	public String getPreprocessedPath() {
		return this.preprocessedPath;
	}

	/**
	 * Sets a path to which the preprocessed (i.e. normalized and up-front evaluated) version of the input program should be written. Note that
	 * {@link InputConfig#setDebugPreprocessing(boolean)} has to be active for this to take effect.
	 */
	public void setPreprocessedPath(String preprocessedPath) {
		this.preprocessedPath = preprocessedPath;
	}

	public boolean isWriteAnswerSetsAsXlsx() {
		return this.writeAnswerSetsAsXlsx;
	}

	/**
	 * If set to true, answer sets for this input will be written into an xslx worksheet rather than as plain-text console output.
	 */
	public void setWriteAnswerSetsAsXlsx(boolean writeAnswerSetsAsXslx) {
		this.writeAnswerSetsAsXlsx = writeAnswerSetsAsXslx;
	}

	public String getAnswerSetFileOutputPath() {
		return this.answerSetFileOutputPath;
	}

	/**
	 * If {@link InputConfig#setWriteAnswerSetsAsXlsx(boolean)} is set, answer sets for this input will be written into an xslx worksheet stored
	 * to the path set here.
	 */
	public void setAnswerSetFileOutputPath(String answerSetFileOutputPath) {
		this.answerSetFileOutputPath = answerSetFileOutputPath;
	}

	public String getNormalizedPath() {
		return this.normalizedPath;
	}

	/**
	 * Sets a path to which the normalized version of the input program should be written. Note that
	 * {@link InputConfig#setDebugPreprocessing(boolean)} has to be active for this to take effect.
	 */
	public void setNormalizedPath(String normalizedPath) {
		this.normalizedPath = normalizedPath;
	}

	public boolean isDebugPreprocessing() {
		return this.debugPreprocessing;
	}

	/**
	 * Enables collection of debug data during program preprocessing.
	 */
	public void setDebugPreprocessing(boolean debugPreprocessing) {
		this.debugPreprocessing = debugPreprocessing;
	}

	public boolean isReifyInput() {
		return reifyInput;
	}

	public void setReifyInput(boolean reifyInput) {
		this.reifyInput = reifyInput;
	}

	public boolean isRunTests() {
		return runTests;
	}

	public void setRunTests(boolean runTests) {
		this.runTests = runTests;
	}

}
