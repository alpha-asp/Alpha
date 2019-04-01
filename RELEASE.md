# Release

This document explains how to build and release Alpha.

First, decide which revision (= Git commit) of Alpha you want to release. This revision should be on `master`
and ideally ahead of any previously released revisions. We denote the Git commit hash of the revision to be
released as `$rev` (it is a reference to a commit, so it could be the name of a branch, say, `master` or just
a hash like `cafebabe`).

You probably have already checked out this version, but if you haven't, do so now:

```bash
$ git checkout $rev
```

Now is the time to make sure the revision you are about to release actually works fine. Run tests, try it out
manually, maybe go paranoid and `./gradlew clean` everything and try again. Only continue if you are happy with
the revision you are about to release, and avoid list minute fixes.

Next, give `$rev` a name, by tagging it. The naming convention for tags is
[Semantic Versioning 2](http://semver.org/spec/v2.0.0.html). **Read through the specification of Semantic
Versioning carefully, before releasing anything. If in doubt, read it again and seek advice.**

Make sure the version you want to release does not collide and fits with any
[tags already present on GitHub](https://github.com/alpha-asp/Alpha/tags). We denote the version to be released
as `$version` (it looks like "v0.1.0", "v1.0.0-beta", note it starts with the letter "v").

```bash
$ git tag $version
```

Now you push this tag to GitHub:

```bash
$ git push --tags
```

Navigate to (Releases)[https://github.com/alpha-asp/Alpha/releases] and press "Draft a new release"). Fill in `$version`
as the name of your tag, and change the Release Title to `$version`. Feel free to write a release note (we currently
have no policy in place for release notes). If `$version` is below 1 or you are releasing a preview version, check the
box.

Now, let's generate two distribution packages of Alpha for convenience. You will attach two files to the release on
GitHub. `alpha.jar`, a self-contained and executbale JAR and `alpha.zip` which contains Alpha as JAR file, dependencies
as JAR files and two scripts to run Alpha on Unix-like and Windows respectively.

To generate `alpha.jar`:

```bash
$ ./gradlew bundledJar
$ cp build/libs/alpha-bundled.jar alpha.jar
```

To generate `alpha.zip`:

```bash
$ ./gradlew distZip
$ cp build/distributions/alpha.zip alpha.zip
```

Attach the two files to the release on GitHub, then publish the release. Lastly, check that everything is fine,
e.g. that the tag/version really points at the revision you wanted to rlease and that `alpha.zip` and `alpha.jar
downloaded from GitHub do what they are supposed to do.
