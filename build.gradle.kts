plugins {
	jacoco

	id("com.github.kt3k.coveralls") version "2.12.0"
}

// TODO project property for dependency versions
tasks.wrapper {
	gradleVersion = "7.2"
	distributionType = Wrapper.DistributionType.ALL
}
