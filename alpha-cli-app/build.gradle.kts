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

val mainClassName = "at.ac.tuwien.kr.alpha.Main"

application {
    mainClass.set(mainClassName)
}

tasks.create<Jar>("bundledJar") {
	manifest {
		attributes["Main-Class"] = mainClassName
	}

	with(tasks["jar"] as CopySpec)

	from(configurations.compileClasspath.get().map({ if (it.isDirectory()) it else zipTree(it) }))
	// TODO: is there a neater way to do this?
	from(configurations.runtimeClasspath.get().map({ it -> if (it.isDirectory()) it else zipTree(it) }))

	archiveFileName.set("${project.name}-bundled.jar")
	
	/*
	 * In order to make sure we don"t overwrite NOTICE and LICENSE files coming from dependency
	 * jars with each other, number them while copying
	 */

	/* TODO
	var i = 1
	rename(
       delegateClosureOf({ it ->
				if (it.equals("NOTICE.txt") || it.equals("NOTICE")) {
					i++
					return "NOTICE.${i}.txt"
				} else {
					return null
				}
			})
	)

	var j = 1
	rename(closureOf<Transformer<String, String>> { name ->
		if (name.equals("LICENSE.txt") || name.equals("LICENSE")) {
			j++
			name = "LICENSE.${j}.txt"
		} else {
			null
		}
	})
	 */

	with(tasks["jar"] as CopySpec)
}

tasks.test {
	useJUnitPlatform()
}