package at.ac.tuwien.kr.alpha.api.impl;

import java.util.function.Supplier;

import com.google.common.annotations.VisibleForTesting;

import at.ac.tuwien.kr.alpha.api.Alpha;
import at.ac.tuwien.kr.alpha.api.config.AggregateRewritingConfig;
import at.ac.tuwien.kr.alpha.api.config.GrounderHeuristicsConfiguration;
import at.ac.tuwien.kr.alpha.api.config.SystemConfig;
import at.ac.tuwien.kr.alpha.api.programs.InputProgram;
import at.ac.tuwien.kr.alpha.api.programs.NormalProgram;
import at.ac.tuwien.kr.alpha.api.programs.ProgramParser;
import at.ac.tuwien.kr.alpha.core.actions.ActionExecutionService;
import at.ac.tuwien.kr.alpha.core.actions.ActionExecutionServiceImpl;
import at.ac.tuwien.kr.alpha.core.actions.DefaultActionImplementationProvider;
import at.ac.tuwien.kr.alpha.core.grounder.GrounderFactory;
import at.ac.tuwien.kr.alpha.core.parser.aspcore2.ASPCore2ProgramParser;
import at.ac.tuwien.kr.alpha.core.parser.evolog.EvologProgramParser;
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

	static Supplier<ProgramTransformation<InputProgram, NormalProgram>> newProgramNormalizationFactory(Supplier<ProgramParser> parserFactory,
			AggregateRewritingConfig aggregateCfg) {
		// AggregateEncoderFactory depends on parser factory since stringtemplate-based aggregate encoders need to use the same parser that's used
		// for input programs.
		AggregateEncoderFactory aggregateEncoderFactory = new AggregateEncoderFactory(parserFactory,
				aggregateCfg.isUseSortingGridEncoding(), aggregateCfg.isSupportNegativeValuesInSums());

		// Factory for aggregate rewriting (depends on encoders provided by above factory).
		Supplier<AggregateRewriting> aggregateRewritingFactory = () -> new AggregateRewriting(aggregateEncoderFactory.newCountEqualsEncoder(),
				aggregateEncoderFactory.newCountLessOrEqualEncoder(), aggregateEncoderFactory.newSumEqualsEncoder(),
				aggregateEncoderFactory.newSumLessOrEqualEncoder(), aggregateEncoderFactory.newMinEncoder(), aggregateEncoderFactory.newMaxEncoder());

		// Factory for NormalizeProgramTransformation - needs a supplier for AggregateRewriting due to AggregateRewritings' dependency to encoder
		// factory.
		return () -> new NormalizeProgramTransformation(
				aggregateRewritingFactory);
	}

	static GrounderFactory newGrounderFactory(SystemConfig cfg) {
		GrounderHeuristicsConfiguration grounderHeuristicsCfg = GrounderHeuristicsConfiguration.getInstance(cfg.getGrounderToleranceConstraints(),
				cfg.getGrounderToleranceRules());
		grounderHeuristicsCfg.setAccumulatorEnabled(cfg.isGrounderAccumulatorEnabled());
		return new GrounderFactory(
				grounderHeuristicsCfg,
				cfg.isDebugInternalChecks());
	}

	static SolverFactory newSolverFactory(SystemConfig cfg) {
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
		return new SolverFactory(cfg.getSolverName(), cfg.getNogoodStoreName(), solverCfg);
	}

	// TODO lifetime of one ActionContext needs to be exactly runtime of one program!
	@VisibleForTesting
	public static Alpha newAlpha(SystemConfig cfg, ActionExecutionService actionContext) {

		// Parser factory - Supply correct parser dependent on the accepted input language.
		Supplier<ProgramParser> parserFactory = () -> cfg.isAcceptEvologPrograms() ? new EvologProgramParser() : new ASPCore2ProgramParser();
		// if (cfg.isAcceptEvologPrograms()) {

		// }
		Supplier<ProgramTransformation<InputProgram, NormalProgram>> programNormalizationFactory = newProgramNormalizationFactory(parserFactory,
				cfg.getAggregateRewritingConfig());

		// GrounderFactory - Since every grounder instance is only good for one program instance, we need a factory.
		GrounderFactory grounderFactory = newGrounderFactory(cfg);

		// SolverFactory - Same as for GrounderFactory, we need a new Solver for each program.
		SolverFactory solverFactory = newSolverFactory(cfg);

		// Now that all dependencies are taken care of, build new Alpha instance.
		return new AlphaImpl(parserFactory, programNormalizationFactory, grounderFactory, solverFactory, actionContext, cfg.isEvaluateStratifiedPart(),
				cfg.isSortAnswerSets());
	}

	// TODO action stuff should go into system config
	public static Alpha newAlpha(SystemConfig cfg) {
		return newAlpha(cfg, new ActionExecutionServiceImpl(new DefaultActionImplementationProvider()));
	}

	// Create Alpha instance with default config.
	public static Alpha newAlpha() {
		return newAlpha(new SystemConfig());
	}

}
