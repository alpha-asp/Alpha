package at.ac.tuwien.kr.alpha.core.externals;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

import org.junit.jupiter.api.Test;
import org.reflections.Reflections;
import org.reflections.util.ConfigurationBuilder;

public class ExternalsTest {

	@Test
	public void loadExternalsFromJarfile() throws MalformedURLException {
/*		// TODO make this a relative path
		URL jarUrl = new URL("jar:file:" + new File("/home/michael/.m2/repository/com/github/madmike200590/alpha-externallibtest/1.0-SNAPSHOT/alpha-externallibtest-1.0-SNAPSHOT-jar-with-dependencies.jar").getAbsolutePath() + "!/");
		Reflections reflections = new Reflections(
					new ConfigurationBuilder()
							.addClassLoader(URLClassLoader.newInstance(new URL[] {jarUrl}))
							.setScanners(Scanners);
		reflections.getAllTypes().forEach(System.out::println);*/
	}
}
