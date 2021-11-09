plugins {
    id("antlr")
    id("alpha.java-library-conventions")
}

val antlrVersion = "4.7"
// Keep this in sync with antlr version to avoid multiple versions in classpath
val stringtemplateVersion = "4.0.8"

dependencies {
	api(project(":alpha-api")) // TODO does it make more sense to use implementation scope?
	api(project(":alpha-commons")) // TODO does it make more sense to use implementation scope?

    // We need to give the ANTLR Plugin a hint.
	antlr("org.antlr:antlr4:${antlrVersion}")

    // Re-introduce antlr4-runtime as compile dependency.
	implementation("org.antlr:antlr4-runtime:${antlrVersion}")

	implementation("org.antlr:ST4:${stringtemplateVersion}")
}

//tasks.withType(AntlrTask) {
tasks.generateGrammarSource {
	// See https://github.com/antlr/antlr4/blob/master/doc/tool-options.md
	arguments = arguments + listOf(
			"-visitor",
			"-no-listener",
			"-long-messages",
			"-package", "at.ac.tuwien.kr.alpha.core.antlr",
			"-Werror",
			"-Xlog",
			"-lib", "src/main/antlr/at/ac/tuwien/kr/alpha/core/antlr"
	)
}

tasks.jacocoTestReport {
	reports {
		xml.required.set(true)
	}
}

	// NOTE: Contents of the antlr subpackage are autogenerated (see configuration of
	//       AntlrTasks above). It does not make sense to include them in our coverage
	//       report.
	// TODO: Translate to Kotlin.
	//afterEvaluate {
		//getClassDirectories().setFrom(files(classDirectories.files.collect {
			//fileTree(dir: it, exclude: "at/ac/tuwien/kr/alpha/antlr/**")
		//}))
	//}

tasks.test {
	useJUnitPlatform()
}