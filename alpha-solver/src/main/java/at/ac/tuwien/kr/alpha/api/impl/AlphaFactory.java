package at.ac.tuwien.kr.alpha.api.impl;

import java.util.function.Supplier;

import at.ac.tuwien.kr.alpha.api.Alpha;
import at.ac.tuwien.kr.alpha.api.config.GrounderHeuristicsConfiguration;
import at.ac.tuwien.kr.alpha.api.config.SystemConfig;
import at.ac.tuwien.kr.alpha.api.programs.InputProgram;
import at.ac.tuwien.kr.alpha.api.programs.NormalProgram;
import at.ac.tuwien.kr.alpha.api.programs.ProgramParser;
import at.ac.tuwien.kr.alpha.core.grounder.GrounderFactory;
import at.ac.tuwien.kr.alpha.core.parser.ProgramParserImpl;
import at.ac.tuwien.kr.alpha.core.programs.transformation.NormalizeProgramTransformation;
import at.ac.tuwien.kr.alpha.core.programs.transformation.ProgramTransformation;
import at.ac.tuwien.kr.alpha.core.programs.transformation.aggregates.AggregateRewriting;
import at.ac.tuwien.kr.alpha.core.programs.transformation.aggregates.encoders.AggregateEncoderFactory;
import at.ac.tuwien.kr.alpha.core.solver.SolverConfig;
import at.ac.tuwien.kr.alpha.core.solver.SolverFactory;
import at.ac.tuwien.kr.alpha.core.solver.heuristics.HeuristicsConfiguration;

public final class AlphaFactory {

	private AlphaFactory() {
		throw new AssertionError("Cannot instantiate utility class!");
	}

	public static Alpha newAlpha(SystemConfig cfg) {
		Supplier<ProgramParser> parserFactory = () -> new ProgramParserImpl();

		// AggregateEncoderFactory depends on parser factory since stringtemplate-based aggregate encoders need to use the same parser that's used
		// for input programs.
		AggregateEncoderFactory aggregateEncoderFactory = new AggregateEncoderFactory(parserFactory,
				cfg.getAggregateRewritingConfig().isUseSortingGridEncoding(), cfg.getAggregateRewritingConfig().isSupportNegativeValuesInSums());

		// Factory for aggregate rewriting (depends on encoders provided by above factory).
		Supplier<AggregateRewriting> aggregateRewritingFactory = () -> new AggregateRewriting(aggregateEncoderFactory.newCountEqualsEncoder(),
				aggregateEncoderFactory.newCountLessOrEqualEncoder(), aggregateEncoderFactory.newSumEqualsEncoder(),
				aggregateEncoderFactory.newSumLessOrEqualEncoder(), aggregateEncoderFactory.newMinEncoder(), aggregateEncoderFactory.newMaxEncoder());

		// Factory for NormalizeProgramTransformation - needs a supplier for AggregateRewriting due to AggregateRewritings' dependency to encoder
		// factory.
		Supplier<ProgramTransformation<InputProgram, NormalProgram>> programNormalizationFactory = () -> new NormalizeProgramTransformation(
				aggregateRewritingFactory);

		// GrounderFactory - Since every grounder instance is only good for one program instance, we need a factory.
		GrounderHeuristicsConfiguration grounderHeuristicsCfg = GrounderHeuristicsConfiguration.getInstance(cfg.getGrounderToleranceConstraints(),
				cfg.getGrounderToleranceRules());
		grounderHeuristicsCfg.setAccumulatorEnabled(cfg.isGrounderAccumulatorEnabled());
		GrounderFactory grounderFactory = new GrounderFactory(
				grounderHeuristicsCfg,
				cfg.isDebugInternalChecks());

		// SolverFactory - Same as for GrounderFactory, we need a new Solver for each program.
		SolverConfig solverCfg = new SolverConfig();
		solverCfg.setDisableJustifications(cfg.isDisableJustificationSearch());
		solverCfg.setDisableNogoodDeletion(cfg.isDisableNoGoodDeletion());
		solverCfg.setEnableDebugChecks(cfg.isDebugInternalChecks());
		solverCfg.setRandomSeed(cfg.getSeed());
		solverCfg.setHeuristicsConfiguration(
				HeuristicsConfiguration.builder()
						.setHeuristic(cfg.getBranchingHeuristic())
						.setMomsStrategy(cfg.getMomsStrategy())
						.setReplayChoices(cfg.getReplayChoices())
						.build());
		SolverFactory solverFactory = new SolverFactory(cfg.getSolverName(), cfg.getNogoodStoreName(), solverCfg);

		// Now that all dependencies are taken care of, build new Alpha instance.
		return new AlphaImpl(parserFactory, programNormalizationFactory, grounderFactory, solverFactory, cfg.isEvaluateStratifiedPart(),
				cfg.isSortAnswerSets());
	}

	// Create Alpha instance with default config.
	public static Alpha newAlpha() {
		return newAlpha(new SystemConfig());
	}

}
