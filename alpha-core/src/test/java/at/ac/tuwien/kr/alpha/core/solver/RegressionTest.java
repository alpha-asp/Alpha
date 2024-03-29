package at.ac.tuwien.kr.alpha.core.solver;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@ParameterizedTest
@MethodSource("at.ac.tuwien.kr.alpha.core.solver.RegressionTestConfigProvider#provideConfigs")
public @interface RegressionTest {

}
