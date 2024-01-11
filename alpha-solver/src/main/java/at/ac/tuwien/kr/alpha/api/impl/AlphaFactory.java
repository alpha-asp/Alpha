package at.ac.tuwien.kr.alpha.api.impl;

import java.util.function.Supplier;

import at.ac.tuwien.kr.alpha.api.Alpha;
import at.ac.tuwien.kr.alpha.api.config.AggregateRewritingConfig;
import at.ac.tuwien.kr.alpha.api.config.GrounderHeuristicsConfiguration;
import at.ac.tuwien.kr.alpha.api.config.SystemConfig;
import at.ac.tuwien.kr.alpha.api.programs.InputProgram;
import at.ac.tuwien.kr.alpha.api.programs.NormalProgram;
import at.ac.tuwien.kr.alpha.api.programs.ProgramParser;
import at.ac.tuwien.kr.alpha.core.actions.ActionExecutionServiceImpl;
import at.ac.tuwien.kr.alpha.core.actions.ActionImplementationProvider;
import at.ac.tuwien.kr.alpha.core.actions.DefaultActionImplementationProvider;
import at.ac.tuwien.kr.alpha.core.grounder.GrounderFactory;
import at.ac.tuwien.kr.alpha.core.parser.aspcore2.ASPCore2ProgramParser;
import at.ac.tuwien.kr.alpha.core.parser.evolog.EvologProgramParser;
import at.ac.tuwien.kr.alpha.core.programs.transformation.ArithmeticTermTransformer;
import at.ac.tuwien.kr.alpha.core.programs.transformation.ChoiceHeadNormalizer;
import at.ac.tuwien.kr.alpha.core.programs.transformation.EnumerationTransformer;
import at.ac.tuwien.kr.alpha.core.programs.transformation.IntervalTermTransformer;
import at.ac.tuwien.kr.alpha.core.programs.transformation.ProgramNormalizer;
import at.ac.tuwien.kr.alpha.core.programs.transformation.ProgramTransformer;
import at.ac.tuwien.kr.alpha.core.programs.transformation.StratifiedEvaluation;
import at.ac.tuwien.kr.alpha.core.programs.transformation.VariableEqualityTransformer;
import at.ac.tuwien.kr.alpha.core.programs.transformation.aggregates.AggregateTransformer;
import at.ac.tuwien.kr.alpha.core.programs.transformation.aggregates.encoders.AggregateEncoders;
import at.ac.tuwien.kr.alpha.core.solver.SolverConfig;
import at.ac.tuwien.kr.alpha.core.solver.SolverFactory;
import at.ac.tuwien.kr.alpha.core.solver.heuristics.HeuristicsConfiguration;

public class AlphaFactory {

	protected ProgramTransformer<InputProgram, NormalProgram> newProgramNormalizer(ProgramParser parser,
			AggregateRewritingConfig aggregateCfg) {
		return new ProgramNormalizer(
				new VariableEqualityTransformer(),
				new ChoiceHeadNormalizer(),
				new AggregateTransformer(
						AggregateEncoders.newCountEqualsEncoder(parser),
						AggregateEncoders.newCountLessOrEqualEncoder(parser, aggregateCfg.isUseSortingGridEncoding()),
						AggregateEncoders.newSumEqualsEncoder(parser, aggregateCfg.isSupportNegativeValuesInSums()),
						AggregateEncoders.newSumLessOrEqualEncoder(parser, aggregateCfg.isSupportNegativeValuesInSums()),
						AggregateEncoders.newMinEncoder(parser),
						AggregateEncoders.newMaxEncoder(parser)),
				new EnumerationTransformer(),
				new IntervalTermTransformer(),
				new ArithmeticTermTransformer());
	}

	protected Supplier<StratifiedEvaluation> newStratifiedEvaluationFactory(ActionImplementationProvider actionImplementationProvider,
			boolean generateActionWitnesses) {
		return () -> new StratifiedEvaluation(new ActionExecutionServiceImpl(actionImplementationProvider), generateActionWitnesses);
	}

	protected GrounderFactory newGrounderFactory(SystemConfig cfg) {
		GrounderHeuristicsConfiguration grounderHeuristicsCfg = GrounderHeuristicsConfiguration.getInstance(cfg.getGrounderToleranceConstraints(),
				cfg.getGrounderToleranceRules());
		grounderHeuristicsCfg.setAccumulatorEnabled(cfg.isGrounderAccumulatorEnabled());
		return new GrounderFactory(
				grounderHeuristicsCfg,
				cfg.isDebugInternalChecks());
	}

	protected SolverFactory newSolverFactory(SystemConfig cfg) {
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

	protected ActionImplementationProvider newActionImplementationProvider() {
		return new DefaultActionImplementationProvider();
	}

	public Alpha newAlpha(SystemConfig cfg) {
		ActionImplementationProvider actionImplementationProvider = newActionImplementationProvider();
		ProgramParser parser;
		if (cfg.isAcceptEvologPrograms()) {
			parser = new EvologProgramParser(actionImplementationProvider); // TODO need to give stdin/stdout definitions to parser (pass in implementation
																			// provider)
		} else {
			parser = new ASPCore2ProgramParser();
		}
		ProgramTransformer<InputProgram, NormalProgram> programNormalizer = newProgramNormalizer(parser,
				cfg.getAggregateRewritingConfig());

		// Stratified evaluation factory - since every instance of stratified evaluation is only good for one program, we need a factory.
		Supplier<StratifiedEvaluation> stratifiedEvaluationFactory = newStratifiedEvaluationFactory(actionImplementationProvider, cfg.isDebugInternalChecks());

		// GrounderFactory - Since every grounder instance is only good for one program instance, we need a factory.
		GrounderFactory grounderFactory = newGrounderFactory(cfg);

		// SolverFactory - Same as for GrounderFactory, we need a new Solver for each program.
		SolverFactory solverFactory = newSolverFactory(cfg);

		// Now that all dependencies are taken care of, build new Alpha instance.
		return new AlphaImpl(parser, programNormalizer, stratifiedEvaluationFactory, grounderFactory, solverFactory, cfg.isSortAnswerSets());
	}

	// Create Alpha instance with default config.
	public Alpha newAlpha() {
		return newAlpha(new SystemConfig());
	}

}
