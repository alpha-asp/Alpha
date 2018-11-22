/**
 * Copyright (c) 2017-2018, the Alpha Team.
 * All rights reserved.
 * 
 * Additional changes made by Siemens.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1) Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 
 * 2) Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package at.ac.tuwien.kr.alpha;

import at.ac.tuwien.kr.alpha.common.AnswerSet;
import at.ac.tuwien.kr.alpha.common.AtomStore;
import at.ac.tuwien.kr.alpha.common.AtomStoreImpl;
import at.ac.tuwien.kr.alpha.common.Program;
import at.ac.tuwien.kr.alpha.common.atoms.Atom;
import at.ac.tuwien.kr.alpha.common.atoms.BasicAtom;
import at.ac.tuwien.kr.alpha.common.fixedinterpretations.*;
import at.ac.tuwien.kr.alpha.common.terms.ConstantTerm;
import at.ac.tuwien.kr.alpha.grounder.Grounder;
import at.ac.tuwien.kr.alpha.grounder.GrounderFactory;
import at.ac.tuwien.kr.alpha.grounder.parser.InlineDirectives;
import at.ac.tuwien.kr.alpha.grounder.parser.ProgramParser;
import at.ac.tuwien.kr.alpha.solver.Solver;
import at.ac.tuwien.kr.alpha.solver.SolverFactory;
import at.ac.tuwien.kr.alpha.solver.heuristics.BranchingHeuristicFactory;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.reflections.Reflections;
import org.reflections.scanners.MethodAnnotationsScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;

import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Stream;

public class Alpha {
	private final Map<String, PredicateInterpretation> predicateMethods = new HashMap<>();

	private final String grounderName;
	private final String solverName;
	private final String storeName;
	private final boolean debug;

	private Solver solver;

	private Program program;

	private long seed;

	public Alpha(String grounderName, String solverName, String storeName, boolean debug) {
		this.grounderName = grounderName;
		this.solverName = solverName;
		this.storeName = storeName;
		this.debug = debug;
	}

	public Alpha(String grounderName, String solverName, String storeName) {
		this(grounderName, solverName, storeName, false);
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

	public long getSeed() {
		return seed;
	}

	public void setSeed(long seed) {
		this.seed = seed;
	}

	public void setDeterministic() {
		setSeed(0);
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
		if (method.getReturnType().equals(boolean.class)) {
			this.predicateMethods.put(name, new MethodPredicateInterpretation(method));
			return;
		}

		if (method.getGenericReturnType().getTypeName().startsWith(PredicateInterpretation.EVALUATE_RETURN_TYPE_NAME_PREFIX)) {
			this.predicateMethods.put(name, new BindingMethodPredicateInterpretation(method));
			return;
		}

		throw new IllegalArgumentException("Passed method has unexpected return type. Should be either boolean or start with " + PredicateInterpretation.EVALUATE_RETURN_TYPE_NAME_PREFIX + ".");
	}

	public void register(Method method) {
		register(method, method.getName());
	}

	public <T> void register(String name, java.util.function.Predicate<T> predicate) {
		this.predicateMethods.put(name, new UnaryPredicateInterpretation<>(predicate));
	}

	public void register(String name, java.util.function.IntPredicate predicate) {
		this.predicateMethods.put(name, new IntPredicateInterpretation(predicate));
	}

	public void register(String name, java.util.function.LongPredicate predicate) {
		this.predicateMethods.put(name, new LongPredicateInterpretation(predicate));
	}

	public <T, U> void register(String name, java.util.function.BiPredicate<T, U> predicate) {
		this.predicateMethods.put(name, new BinaryPredicateInterpretation<>(predicate));
	}

	public void register(String name, java.util.function.Supplier<Set<List<ConstantTerm<?>>>> supplier) {
		this.predicateMethods.put(name, new SuppliedPredicateInterpretation(supplier));
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
			atoms.add(new BasicAtom(at.ac.tuwien.kr.alpha.common.Predicate.getInstance(name, 1), ConstantTerm.getInstance(it)));
		}

		final Program acc = new Program(Collections.emptyList(), atoms, new InlineDirectives());

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

	public Stream<AnswerSet> solve(Path program) throws IOException {
		return solve(CharStreams.fromPath(program));
	}

	public Stream<AnswerSet> solve(String program) throws IOException {
		return solve(CharStreams.fromString(program));
	}

	private Stream<AnswerSet> solve(CharStream program) throws IOException {
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
		AtomStore atomStore = new AtomStoreImpl();
		Grounder grounder = GrounderFactory.getInstance(grounderName, program, atomStore, debug);
		Solver solver = SolverFactory.getInstance(solverName, storeName, atomStore, grounder, new Random(seed), BranchingHeuristicFactory.Heuristic.NAIVE, debug, false);
		return solver.stream();
	}
}
