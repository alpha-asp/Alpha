# Alpha

[![Build Status](https://travis-ci.org/flowlo/alpha.svg?branch=master)](https://travis-ci.org/flowlo/alpha)

Alpha is the successor of [OMiGA](http://www.kr.tuwien.ac.at/research/systems/omiga/) and currently in development.

## Building

Alpha uses the [Gradle build automation system](gradle). Executing

```bash
$ gradle build
```

will automatically fetch all dependencies (declared in [`build.gradle`](build.gradle)) and compile the project.

Artifacts generated will be placed in `build/`. Most notably you'll find files ready for distribution at
`build/distributions`. They contain archives which in turn contain a `bin/` directory with scripts to run Alpha on Linux
and Windows.

If you want to generate a JAR file to be run standalone, execute

```bash
$ gradle bundledJar
```

and pick up `build/libs/alpha-bundled.jar`.