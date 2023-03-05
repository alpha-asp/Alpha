import net.researchgate.release.ReleaseExtension

plugins {
	jacoco
	id("com.github.kt3k.coveralls") version "2.12.0"
	id("jacoco-report-aggregation")
	id("net.researchgate.release") version "3.0.2"
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
	maven ("https://plugins.gradle.org/m2/")
	mavenCentral { metadataSources { mavenPom() } }
}

configure<ReleaseExtension> {
	ignoredSnapshotDependencies.set(listOf("net.researchgate:gradle-release"))
	failOnUnversionedFiles.set(false)
	tagTemplate.set("v${version}")
	with(git) {
		requireBranch.set("revamp-release-workflow")
		// to disable branch verification: requireBranch.set(null as String?)
	}
}