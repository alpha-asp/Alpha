# Release

This document explains how to build and release Alpha.

The regular release workflow for Alpha assumes that a release will always be based on the latest commit in the `master` branch. In order to release Alpha, first checkout the `master` branch and make sure you're up to date:

```bash
$ git checkout master
$ git pull
```

Now is the time to make sure the revision you are about to release actually works. Build it using `./gradlew clean build`, potentially do some manual tests, play around a bit, maybe go paranoid and `./gradlew clean build` everything once more. Only continue if you are happy with the state you are about to release, and avoid last minute fixes.

The actual release process consists of following steps:
- Create a release tag using gradle release plugin.
- From the tag created by gradle release plugin, create a release on github.

#### Creating a release tag using Gradle Release Plugin

Before running the release plugin, decide on a release version identifier and the next development version. Version identifiers must confirm to [Semantic Versioning 2](http://semver.org/spec/v2.0.0.html). **Read through the specification of Semantic Versioning carefully, before releasing anything. If in doubt, read it again and seek advice.** Also, make sure the version you want to release does not collide with any [tag already present on GitHub](https://github.com/alpha-asp/Alpha/tags). We denote the version to be released as `$version` (it looks like "0.1.0", "1.0.0-beta").

The next development version (denoted `$nextVersion`) is the version you expect to be the next release version. Note that it isn't set in stone since actual release versions are set during the release process. For example, if your release version is `0.9.0`, your next version could be `0.10.0` or `1.0.0`.

To create a release tag and set the correct versions in the `gradle.properties` file, call the gradle release plugin:

```bash
$ ./gradlew release -Prelease.useAutomaticVersion=true -Prelease.releaseVersion=$version -Prelease.newVersion=$nextVersion-SNAPSHOT
```
**Make sure you add the `-SNAPSHOT` qualifier to the next version!** This is the version that will be put into `gradle.properties` in the `master` branch after creating the release tag, i.e. subsequent builds of `master` (e.g. nightly builds from CI workflows) will have this version.

The release plugin automatically pushes all changes it makes in the repository. After running it, make sure:
- `gradle.properties` in `master` has the new `SNAPSHOT` version.
- On github, you can see a new tag with the same name as your release version.
- When you check out the tag, `gradle.properties` contains the correct version, i.e. the tag name. 

Navigate to (Releases)[https://github.com/alpha-asp/Alpha/releases] and press "Draft a new release"). Fill in `$version`
as the name of your tag, and change the Release Title to `$version`. Feel free to write a release note (we currently
have no policy in place for release notes). If `$version` is below 1 or you are releasing a preview version, check the
box.

Now, let's generate some distribution packages of Alpha for convenience. You will attach the following files to the release on GitHub:
- `alpha-cli-app-$version-bundled.jar: A "fat-jar" containingthe Alpha CLI applications along with all dependencies in one jar file.
- Distribution zip- and tar-archives for the CLI application: Zip- and tar-versions of an archive with Alpha, all dependencies, and launcher scripts for windows and UNIX-like systems.
- A zip- and tar-archive of the source code for Alpha.

To generate these artifacts, check out your release tag:

```bash
$ git checkout $version
```

To generate the "fat-jar":

```bash
$ ./gradlew alpha-cli-app:bundledJar
```

The jar file can be found in `./alpha-cli-app/build/libs/alpha-cli-app-$version-bundled.jar`

To generate distribution archives:

```bash
$ ./gradlew alpha-cli-app:distZip && ./gradlew alpha-cli-app:distTar
```

The finished archives are located in `./alpha-cli-app/build/distributions/`.
Last `./gradlew clean` the project to get rid of all build artifacts, then create a `sources.zip` (using e.g. `zip -r sources.zip .`) and a `sources.tar.gz` (using e.g. `tar -czf /tmp/sources.tar.gz .`)

Attach the generated files to the release on GitHub, then publish the release. Last but not least, check that everything is fine, i.e. the files - when downloaded from the github release - do what they're supposed to.

Optionally, archive the release on Zenodo. If you do so, add the new identifier to `CITATION.cff`.
