import org.gradle.api.JavaVersion
import org.gradle.api.tasks.testing.logging.TestExceptionFormat

plugins {
    id("java")
    id("jacoco")
    id("checkstyle")
    id("maven-publish")
    id("java-test-fixtures")
}

repositories {
    mavenCentral { metadataSources { mavenPom() } }
}

java.sourceCompatibility = JavaVersion.VERSION_1_8
java.targetCompatibility = JavaVersion.VERSION_1_8

dependencies {
    implementation("org.apache.commons:commons-collections4:4.4")
    implementation("org.apache.commons:commons-lang3:3.12.0")
    implementation("org.apache.commons:commons-text:1.9")

    implementation("org.reflections:reflections:0.9.11")
    implementation("org.slf4j:slf4j-api:1.7.20")

    // JUnit 5
    val jupiterVersion = "5.7.1"
    fun jupiter(component: String): String {
        return "org.junit.jupiter:junit-jupiter-${component}:${jupiterVersion}"
    }
    testImplementation(jupiter("api"))
    testImplementation(jupiter("params"))
    testImplementation(jupiter("engine"))
    
    testFixturesApi(jupiter("api"))
}

// JUnit 5
tasks.withType<Test> {
    useJUnitPlatform()

	testLogging {
		exceptionFormat = TestExceptionFormat.FULL
	}
}

// Fix checkstyle version.
checkstyle {
	toolVersion = "7.6"
}

tasks.withType<JavaCompile> {
    val compilerArgs = options.compilerArgs
    //compilerArgs.add("-Xdoclint:all,-missing")
    compilerArgs.add("-Xlint:all")
    compilerArgs.add("-Xmaxerrs")
    compilerArgs.add("1000")
}

tasks.jacocoTestReport {
    reports {
        xml.required.set(true)
    }
}

publishing {
    publications {
        create<MavenPublication>("binary") {
            from(components["java"])
        }
    }
}