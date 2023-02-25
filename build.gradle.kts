plugins {
	jacoco
	id("com.github.kt3k.coveralls") version "2.12.0"
	id("jacoco-report-aggregation")
}

tasks.wrapper {
	gradleVersion = "7.6"
	distributionType = Wrapper.DistributionType.ALL
}

dependencies {
	jacocoAggregation(project(":alpha-cli-app"))
}

reporting {
	reports {
		val jacocoAggregatedTestReport by creating(JacocoCoverageReport::class) {
			testType.set(TestSuiteType.UNIT_TEST)
		}
	}
}

repositories {
	mavenCentral { metadataSources { mavenPom() } }
}