package at.ac.tuwien.kr.alpha.core.externals;

import java.net.URL;
import java.util.Map;

import at.ac.tuwien.kr.alpha.api.common.fixedinterpretations.PredicateInterpretation;
import javassist.ClassPath;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.NotFoundException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ExternalsTest {

	@Test
	public void loadExternalsFromJarfile() {
		URL jarUrl = ExternalsTest.class.getResource("/jarWithExternalPredicateFunctions.jar");
		Map<String, PredicateInterpretation> scannedExternals = Externals.scan(jarUrl);
		assertEquals(1, scannedExternals.size());
		assertTrue(scannedExternals.containsKey("something_cool"));
	}

	@Test
	public void loadingWithJavassist() throws NotFoundException {
		URL jarUrl = ExternalsTest.class.getResource("/jarWithExternalPredicateFunctions.jar");
		ClassPool pool = ClassPool.getDefault();
		pool.insertClassPath(jarUrl.getPath());
		pool.getImportedPackages().forEachRemaining(System.out::println);
		//pool.
		// TODO unfinished
	}

}
