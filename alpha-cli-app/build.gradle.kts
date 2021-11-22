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
}

val main = "at.ac.tuwien.kr.alpha.Main"

application {
	mainClass.set(main)
}

tasks.create<Jar>("bundledJar") {
	dependsOn(":alpha-api:jar", ":alpha-commons:jar", ":alpha-core:jar", ":alpha-solver:jar")

	manifest {
		attributes["Main-Class"] = main
	}

	with(tasks["jar"] as CopySpec)

	from(configurations.runtimeClasspath.get().map({ if (it.isDirectory()) it else zipTree(it) }))

	archiveFileName.set("${project.name}-bundled.jar")
	
	/*
	 * In order to make sure we don"t overwrite NOTICE and LICENSE files coming from dependency
	 * jars with each other, number them while copying
	 */
	var noticeCount = 1
	rename { it : String ->
		return@rename if ("NOTICE.txt".equals(it) || "NOTICE".equals(it)) {
			noticeCount++
			"NOTICE.${noticeCount}.txt"
		} else {
			it
		}
	}

	var licenseCount = 1
	rename { it : String ->
		return@rename if ("LICENSE.txt".equals(it) || "LICENSE".equals(it)) {
			licenseCount++
			"LICENSE.${licenseCount}.txt"
		} else {
			it
		}
	}
}

tasks.test {
	useJUnitPlatform()
}
