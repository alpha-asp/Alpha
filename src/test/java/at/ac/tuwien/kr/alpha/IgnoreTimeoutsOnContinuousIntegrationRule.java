package at.ac.tuwien.kr.alpha;

import org.junit.Test;
import org.junit.internal.runners.statements.FailOnTimeout;
import org.junit.internal.runners.statements.InvokeMethod;
import org.junit.internal.runners.statements.RunBefores;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;

import java.lang.reflect.Field;
import java.util.List;

public class IgnoreTimeoutsOnContinuousIntegrationRule implements TestRule {
	private static final boolean CI = Boolean.parseBoolean(System.getenv("CI"));

	@Override
	public Statement apply(final Statement base, final Description description) {
		if (!CI) {
			return base;
		}

		final Test testAnnotation = description.getAnnotation(Test.class);
		if (testAnnotation == null) {
			throw new RuntimeException("Expected Test annotation on test method");
		}

		final long timeout = testAnnotation.timeout();
		if (timeout == 0) {
			return base;
		}

		try {
			return stripTimeout(base);
		} catch (final Exception e) {
			throw new RuntimeException("Failed to adjust timeout", e);
		}
	}

	@SuppressWarnings("unchecked")
	private static Statement stripTimeout(Statement base) throws NoSuchFieldException, IllegalAccessException {
		if (base instanceof RunBefores) {
			RunBefores r = (RunBefores) base;

			Class<?> clazz = r.getClass();

			Field nextField = clazz.getDeclaredField("next");
			nextField.setAccessible(true);
			Field beforesField = clazz.getDeclaredField("befores");
			beforesField.setAccessible(true);
			Field targetField = clazz.getDeclaredField("target");
			targetField.setAccessible(true);

			Statement next = (Statement) nextField.get(r);
			List<FrameworkMethod> befores = (List<FrameworkMethod>) beforesField.get(r);
			Object target = targetField.get(r);

			return new RunBefores(stripTimeout(next), befores, target);
		} else if (base instanceof FailOnTimeout) {
			FailOnTimeout f = (FailOnTimeout) base;

			Class<?> clazz = f.getClass();

			Field originalStatementField = clazz.getDeclaredField("originalStatement");
			originalStatementField.setAccessible(true);

			return (Statement) originalStatementField.get(f);
		} else if (base instanceof InvokeMethod) {
			return base;
		} else {
			throw new RuntimeException("Cannot handle statement type");
		}
	}
}
