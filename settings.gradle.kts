pluginManagement {
	repositories {
		mavenCentral()
		gradlePluginPortal()
	}
}

rootProject.name = "alpha"
include("alpha-api", "alpha-cli-app", "alpha-core", "alpha-solver", "alpha-commons")
