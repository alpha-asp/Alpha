package at.ac.tuwien.kr.alpha.api.externals;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.reflections.Reflections;
import org.reflections.scanners.MethodAnnotationsScanner;

import at.ac.tuwien.kr.alpha.api.externals.stdlib.AspStandardLibrary;
import at.ac.tuwien.kr.alpha.common.Program;
import at.ac.tuwien.kr.alpha.common.atoms.Atom;
import at.ac.tuwien.kr.alpha.common.atoms.BasicAtom;
import at.ac.tuwien.kr.alpha.common.fixedinterpretations.BinaryPredicateInterpretation;
import at.ac.tuwien.kr.alpha.common.fixedinterpretations.BindingMethodPredicateInterpretation;
import at.ac.tuwien.kr.alpha.common.fixedinterpretations.IntPredicateInterpretation;
import at.ac.tuwien.kr.alpha.common.fixedinterpretations.LongPredicateInterpretation;
import at.ac.tuwien.kr.alpha.common.fixedinterpretations.MethodPredicateInterpretation;
import at.ac.tuwien.kr.alpha.common.fixedinterpretations.PredicateInterpretation;
import at.ac.tuwien.kr.alpha.common.fixedinterpretations.SuppliedPredicateInterpretation;
import at.ac.tuwien.kr.alpha.common.fixedinterpretations.UnaryPredicateInterpretation;
import at.ac.tuwien.kr.alpha.common.terms.ConstantTerm;
import at.ac.tuwien.kr.alpha.grounder.parser.InlineDirectives;

public final class Externals {

	// private constructor since this is a utility class
	private Externals() {

	}

	/**
	 * Returns a map of external definitions making up the "standard library" of
	 * exterals that are always available in programs for Alpha.
	 * This method scans all predicate-annotated methods in the package holding the
	 * class {@link AspStandardLibrary}.
	 */
	public static Map<String, PredicateInterpretation> getStandardLibraryExternals() {
		return Externals.scan(AspStandardLibrary.class.getPackage());
	}

	public static Map<String, PredicateInterpretation> scan(Package basePackage) {
		Reflections reflections = new Reflections(basePackage.getName(), new MethodAnnotationsScanner());
		Set<Method> methods = reflections.getMethodsAnnotatedWith(Predicate.class);
		return Externals.scanMethods(methods);
	}

	public static Map<String, PredicateInterpretation> scan(Class<?> classWithPredicateMethods) {
		Method[] methods = classWithPredicateMethods.getMethods();
		Set<Method> predicateMethods = new HashSet<>();
		for (Method method : methods) {
			if (method.isAnnotationPresent(Predicate.class)) {
				predicateMethods.add(method);
			}
		}
		return Externals.scanMethods(predicateMethods);
	}

	private static Map<String, PredicateInterpretation> scanMethods(Iterable<Method> methods) {
		Map<String, PredicateInterpretation> retVal = new HashMap<>();
		String name;
		for (Method method : methods) {
			name = method.getAnnotation(Predicate.class).name();
			if (name.isEmpty()) {
				name = method.getName();
			}
			retVal.put(name, Externals.processPredicateMethod(method));
		}
		return retVal;
	}

	public static PredicateInterpretation processPredicateMethod(Method method) {
		if (method.getReturnType().equals(boolean.class)) {
			return new MethodPredicateInterpretation(method);
		}

		if (method.getGenericReturnType().getTypeName().startsWith(PredicateInterpretation.EVALUATE_RETURN_TYPE_NAME_PREFIX)) {
			return new BindingMethodPredicateInterpretation(method);
		}

		throw new IllegalArgumentException("Passed method has unexpected return type. Should be either boolean or start with "
				+ PredicateInterpretation.EVALUATE_RETURN_TYPE_NAME_PREFIX + ".");
	}

	public static <T> PredicateInterpretation processPredicate(java.util.function.Predicate<T> predicate) {
		return new UnaryPredicateInterpretation<>(predicate);
	}

	public static PredicateInterpretation processPredicate(java.util.function.IntPredicate predicate) {
		return new IntPredicateInterpretation(predicate);
	}

	public static PredicateInterpretation processPredicate(java.util.function.LongPredicate predicate) {
		return new LongPredicateInterpretation(predicate);
	}

	public static <T, U> PredicateInterpretation processPredicate(java.util.function.BiPredicate<T, U> predicate) {
		return new BinaryPredicateInterpretation<>(predicate);
	}

	public static PredicateInterpretation processPredicate(java.util.function.Supplier<Set<List<ConstantTerm<?>>>> supplier) {
		return new SuppliedPredicateInterpretation(supplier);
	}

	/**
	 * Adds facts generated from a collection of <code>Comparable</code>s to a given
	 * program.
	 * 
	 * @param instances
	 * @param name
	 */
	public static <T extends Comparable<T>> Program addExternalFactsToProgram(Program program, Collection<T> instances, String name) {
		if (instances.isEmpty()) {
			return program;
		}

		final List<Atom> atoms = new ArrayList<>();

		for (T instance : instances) {
			atoms.add(new BasicAtom(at.ac.tuwien.kr.alpha.common.Predicate.getInstance(name, 1), ConstantTerm.getInstance(instance)));
		}

		final Program acc = new Program(Collections.emptyList(), atoms, new InlineDirectives());

		program.accumulate(acc);
		return program;
	}

	public static <T extends Comparable<T>> Program addExternalFactsToProgram(Program program, Collection<T> c) {
		if (c.isEmpty()) {
			return program;
		}

		T first = c.iterator().next();

		String simpleName = first.getClass().getSimpleName();
		return addExternalFactsToProgram(program, c, simpleName.substring(0, 1).toLowerCase() + simpleName.substring(1));
	}

}
