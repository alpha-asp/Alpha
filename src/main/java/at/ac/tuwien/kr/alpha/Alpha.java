package at.ac.tuwien.kr.alpha;

import at.ac.tuwien.kr.alpha.common.AnswerSet;
import at.ac.tuwien.kr.alpha.common.Program;
import at.ac.tuwien.kr.alpha.common.predicates.ExternalEvaluable;
import at.ac.tuwien.kr.alpha.common.predicates.ExternalNativePredicate;
import at.ac.tuwien.kr.alpha.common.terms.ConstantTerm;
import at.ac.tuwien.kr.alpha.grounder.Grounder;
import at.ac.tuwien.kr.alpha.grounder.GrounderFactory;
import at.ac.tuwien.kr.alpha.grounder.parser.ProgramParser;
import at.ac.tuwien.kr.alpha.solver.Solver;
import at.ac.tuwien.kr.alpha.solver.SolverFactory;
import at.ac.tuwien.kr.alpha.solver.heuristics.BranchingHeuristicFactory;
import org.reflections.Reflections;
import org.reflections.scanners.MethodAnnotationsScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.stream.Stream;

public class Alpha {
	private final Map<String, at.ac.tuwien.kr.alpha.common.predicates.Predicate> predicateMethods = new HashMap<>();

	private final String grounderName;
	private final String solverName;
	private final String storeName;

	private Solver solver;

	private Program program;

	public long getSeed() {
		return seed;
	}

	public void setSeed(long seed) {
		this.seed = seed;
	}

	public void setDeterministic() {
		setSeed(0);
	}

	private long seed;

	public Alpha(String grounderName, String solverName, String storeName) {
		this.grounderName = grounderName;
		this.solverName = solverName;
		this.storeName = storeName;
	}

	public Alpha(String grounderName, String solverName) {
		this(grounderName, solverName, "alpharoaming");
	}

	public Alpha(String grounderName) {
		this(grounderName, "default");
	}

	public Alpha() {
		this("naive");
	}

	public void scan(String base) {
		Reflections reflections = new Reflections(new ConfigurationBuilder()
			.setUrls(ClasspathHelper.forPackage(base))
			.setScanners(new MethodAnnotationsScanner())
		);

		Set<Method> predicateMethods = reflections.getMethodsAnnotatedWith(Predicate.class);

		for (Method method : predicateMethods) {
			this.register(method);
		}
	}

	public void register(Method method) {
		this.predicateMethods.put(method.getName(), new ExternalEvaluable(method));
	}

	public void register(String name, java.util.function.Predicate<ConstantTerm> predicate) {
		this.predicateMethods.put(name, new ExternalNativePredicate(name, predicate));
	}

	public void setProgram(Program program) {
		this.program = program;
	}

	public Stream<AnswerSet> solve(String program) throws IOException {
		if (solver != null) {
			// TODO: Maybe cache the result and instantly return here without throwing.
			throw new IllegalStateException("This system has already been used.");
		}

		ProgramParser parser = new ProgramParser(predicateMethods);

		setProgram(parser.parse(program));

		// Obtain grounder instance and feed it with parsedProgram.
		return solve();
	}

	public Stream<AnswerSet> solve() {
		Grounder grounder = GrounderFactory.getInstance(grounderName, program);
		Solver solver = SolverFactory.getInstance(solverName, storeName, grounder, new Random(seed), BranchingHeuristicFactory.Heuristic.NAIVE, false);
		return solver.stream();
	}
}
