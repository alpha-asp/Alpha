package at.ac.tuwien.kr.alpha.core.programs.transformation;

import at.ac.tuwien.kr.alpha.api.config.SystemConfig;
import at.ac.tuwien.kr.alpha.api.programs.ProgramParser;
import at.ac.tuwien.kr.alpha.core.parser.ProgramParserImpl;
import at.ac.tuwien.kr.alpha.core.programs.AnalyzedProgram;
import at.ac.tuwien.kr.alpha.core.programs.CompiledProgram;

import java.util.function.Function;

public class SimplePreprocessingTest {

    private final ProgramParser parser = new ProgramParserImpl();
    private final NormalizeProgramTransformation normalizer = new NormalizeProgramTransformation(SystemConfig.DEFAULT_AGGREGATE_REWRITING_CONFIG);
    private final SimplePreprocessing evaluator = new SimplePreprocessing();
    private final Function<String, CompiledProgram> parseAndEvaluate = (str) -> {
        return evaluator.apply(AnalyzedProgram.analyzeNormalProgram(normalizer.apply(parser.parse(str))));
    };
}
