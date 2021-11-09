plugins {
	jacoco

	id("com.github.kt3k.coveralls") version "2.12.0"
}

tasks.wrapper {
	gradleVersion = "7.2"
	distributionType = Wrapper.DistributionType.ALL
}
