plugins {
    id("alpha.java-library-conventions")
}

// Keep this in sync with antlr version to avoid multiple versions in classpath
val stringtemplateVersion = "4.0.8"

dependencies {
	api(project(":alpha-api"))
	
	implementation("org.antlr:ST4:4.0.8")
}

tasks.test {
	useJUnitPlatform()
}