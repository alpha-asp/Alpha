plugins {
	jacoco

	id("com.github.kt3k.coveralls") version "2.12.0"
}

tasks.wrapper {
	gradleVersion = "7.3"
	distributionType = Wrapper.DistributionType.ALL
}
