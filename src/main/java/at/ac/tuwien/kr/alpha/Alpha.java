package at.ac.tuwien.kr.alpha;

import at.ac.tuwien.kr.alpha.common.AnswerSet;
import at.ac.tuwien.kr.alpha.common.Program;
import at.ac.tuwien.kr.alpha.common.atoms.Atom;
import at.ac.tuwien.kr.alpha.common.atoms.BasicAtom;
import at.ac.tuwien.kr.alpha.common.predicates.*;
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
import java.util.*;
import java.util.stream.Stream;

public class Alpha {
	private final Map<String, FixedInterpretationPredicate> predicateMethods = new HashMap<>();

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
			String name = method.getAnnotation(Predicate.class).name();

			if (name.isEmpty()) {
				name = method.getName();
			}

			this.register(method, name);
		}
	}

	public void register(Method method, String name) {
		this.predicateMethods.put(name, new ExternalMethodPredicate(method));
	}

	public void registerBinding(Method method, String name) {
		this.predicateMethods.put(name, new ExternalBindingMethodPredicate(method));
	}

	public void registerBinding(Method method) {
		registerBinding(method, method.getName());
	}

	public void register(Method method) {
		register(method, method.getName());
	}

	public <T> void register(String name, java.util.function.Predicate<T> predicate) {
		this.predicateMethods.put(name, new ExternalPredicate<>(name, predicate));
	}

	public <T, U> void register(String name, java.util.function.BiPredicate<T, U> predicate) {
		this.predicateMethods.put(name, new ExternalBiPredicate<>(name, predicate));
	}

	public void setProgram(Program program) {
		this.program = program;
	}

	public <T extends Comparable<T>> void addFacts(Collection<T> c, String name) {
		if (c.isEmpty()) {
			return;
		}

		final List<Atom> atoms = new ArrayList<>();

		for (T it : c) {
			atoms.add(new BasicAtom(new at.ac.tuwien.kr.alpha.common.predicates.Predicate(name, 1), ConstantTerm.getInstance(it)));
		}

		final Program acc = new Program(Collections.emptyList(), atoms);

		if (this.program == null) {
			this.program = acc;
		} else {
			this.program.accumulate(acc);
		}
	}

	public <T extends Comparable<T>> void addFacts(Collection<T> c) {
		if (c.isEmpty()) {
			return;
		}

		T first = c.iterator().next();

		String simpleName = first.getClass().getSimpleName();
		addFacts(c, simpleName.substring(0, 1).toLowerCase() + simpleName.substring(1));
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
