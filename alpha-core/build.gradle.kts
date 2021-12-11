plugins {
	id("antlr")
	id("alpha.java-library-conventions")
}

val antlrVersion = "4.7"
// Keep this in sync with antlr version to avoid multiple versions in classpath
val stringtemplateVersion = "4.0.8"

dependencies {
	api(project(":alpha-api"))
	api(project(":alpha-commons"))

	// We need to give the ANTLR Plugin a hint.
	antlr("org.antlr:antlr4:${antlrVersion}")

	// Re-introduce antlr4-runtime as compile dependency.
	implementation("org.antlr:antlr4-runtime:${antlrVersion}")

	implementation("org.antlr:ST4:${stringtemplateVersion}")
}

//tasks.withType(AntlrTask) {
tasks.generateGrammarSource {
	// See https://github.com/antlr/antlr4/blob/master/doc/tool-options.md
	arguments.addAll(listOf(
		"-visitor",
		"-no-listener",
		"-long-messages",
		"-package", "at.ac.tuwien.kr.alpha.core.antlr",
		"-Werror",
		"-Xlog",
		"-lib", "src/main/antlr/at/ac/tuwien/kr/alpha/core/antlr"
	))
}

tasks.test {
	useJUnitPlatform()

	configure<JacocoTaskExtension> {
		// Here, we exclude ANTLR generated classes from coverage analysis,
		// but not from report generation.
		// Exclusion from report generation is done in tasks.jacocoTestReport
		excludes = listOf("at.ac.tuwien.kr.alpha.core.antlr.*")
	}

	finalizedBy(tasks.jacocoTestReport)
}

tasks.jacocoTestReport {
	reports {
		xml.required.set(true)
	}

	classDirectories.setFrom(classDirectories.map {
		// Here, we exclude ANTLR generated classes from report generation,
		// but not from coverage analysis.
		// Exclusion from coverage analysis is done in tasks.test
		fileTree(it).exclude("at/ac/tuwien/kr/alpha/core/antlr/**")
	})
}
