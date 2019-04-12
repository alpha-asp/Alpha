package at.ac.tuwien.kr.alpha.common.atoms.external;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.reflections.Reflections;
import org.reflections.scanners.MethodAnnotationsScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;

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

/**
 * Utility class providing functions to work with external atoms
 * 
 * Copyright (c) 2017-2019, the Alpha Team.
 */
public final class ExternalAtoms {

	// private constructor since this is a utility class
	private ExternalAtoms() {

	}

	public static Map<String, PredicateInterpretation> scan(String basePackage) {
		Map<String, PredicateInterpretation> retVal = new HashMap<>();
		Reflections reflections = new Reflections(
				new ConfigurationBuilder().setUrls(ClasspathHelper.forPackage(basePackage)).setScanners(new MethodAnnotationsScanner()));

		Set<Method> methods = reflections.getMethodsAnnotatedWith(Predicate.class);

		for (Method method : methods) {
			String name = method.getAnnotation(Predicate.class).name();

			if (name.isEmpty()) {
				name = method.getName();
			}

			retVal.put(name, ExternalAtoms.processPredicateMethod(method));
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

	public static <T extends Comparable<T>> List<Atom> asFacts(Class<T> clazz, Collection<T> extFacts) {
		// use Class<T> as parameter here, taking simple name from first element might not give desired result if it's a subtype
		List<Atom> retVal = new ArrayList<>();
		String javaName = clazz.getSimpleName();
		String name = javaName.substring(0, 1).toLowerCase() + javaName.substring(1); // camel-cased, but starting with lower case letter
		for (T instance : extFacts) {
			retVal.add(new BasicAtom(at.ac.tuwien.kr.alpha.common.Predicate.getInstance(name, 1), ConstantTerm.getInstance(instance)));
		}
		return retVal;
	}

}
