package at.ac.tuwien.kr.alpha;

import org.reflections.Reflections;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class System {
	private final Map<String, Method> predicateMethods = new HashMap<>();

	public void scan(String base) {
		Reflections reflections = new Reflections(base);
		Set<Method> predicateMethods = reflections.getMethodsAnnotatedWith(Predicate.class);

		for (Method method : predicateMethods) {
			this.predicateMethods.put(method.getName(), method);
		}
	}

	public void setProgram()
	{

	}
}
