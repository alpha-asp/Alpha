plugins {
	id("alpha.java-application-conventions")
}

dependencies {
	implementation(project(":alpha-solver"))
	implementation(project(":alpha-commons"))

	implementation("commons-cli:commons-cli:1.3.1")

	val poiVersion = "4.1.1"
	implementation("org.apache.poi:poi:${poiVersion}")
	implementation("org.apache.poi:poi-ooxml:${poiVersion}")

	// Logging Implementation
	implementation("org.slf4j:slf4j-simple:1.7.32") {
		// Exclude the SLF4J API, because we already have it via `alpha.java-application-conventions`.
		exclude("org.slf4j", "slf4j-api")
	}
}

val main = "at.ac.tuwien.kr.alpha.Main"

application {
	mainClass.set(main)
}

tasks.create<Jar>("bundledJar") {
	dependsOn(":alpha-api:jar", ":alpha-commons:jar", ":alpha-core:jar", ":alpha-solver:jar")

	manifest {
		attributes["Main-Class"] = main
		attributes["Multi-Release"] = true
	}

	with(tasks["jar"] as CopySpec)

	from(configurations.runtimeClasspath.get().map({ if (it.isDirectory()) it else zipTree(it) }))

	archiveFileName.set("${project.name}-${project.version}-bundled.jar")

	exclude("META-INF/DEPENDENCIES")
	
	/*
	 * In order to make sure we don"t overwrite NOTICE and LICENSE files coming from dependency
	 * jars with each other, number them while copying
	 */
	var noticeCount = 0
	rename { it : String ->
		return@rename if ("NOTICE.txt".equals(it) || "NOTICE".equals(it)) {
			noticeCount++
			"NOTICE.${noticeCount}.txt"
		} else {
			it
		}
	}

	var licenseCount = 0
	rename { it : String ->
		return@rename if ("LICENSE.txt".equals(it) || "LICENSE".equals(it)) {
			licenseCount++
			"LICENSE.${licenseCount}.txt"
		} else {
			it
		}
	}

	duplicatesStrategy = DuplicatesStrategy.FAIL
}

tasks.test {
	dependsOn(":alpha-solver:test")
	useJUnitPlatform()
}
