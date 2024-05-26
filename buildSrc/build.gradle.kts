plugins {
	`kotlin-dsl`
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    kotlinOptions {
        jvmTarget = "11"
    }
}
repositories {
	mavenCentral { metadataSources { mavenPom() } }
}
