plugins {
	jacoco

	id("com.github.kt3k.coveralls") version "2.12.0"
	id("org.ajoberstar.reckon") version "0.13.1"
}

tasks.wrapper {
	gradleVersion = "7.3.2"
	distributionType = Wrapper.DistributionType.ALL
}

reckon {
	scopeFromProp()
	stageFromProp("rc")
}
