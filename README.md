# Alpha

[![Build Status](https://travis-ci.org/AntoniusW/Alpha.svg?branch=master)](https://travis-ci.org/AntoniusW/Alpha)

Alpha is the successor of [OMiGA](http://www.kr.tuwien.ac.at/research/systems/omiga/) and currently in development.

## Building

Alpha uses the [Gradle build automation system](https://gradle.org). Executing

```bash
$ gradle build
```

will automatically fetch all dependencies (declared in [`build.gradle`](build.gradle)) and compile the project.

Artifacts generated will be placed in `build/`. Most notably you'll find files ready for distribution at
`build/distributions/`. They contain archives which in turn contain a `bin/` directory with scripts to run Alpha on Linux
and Windows.

If you want to generate a JAR file to be run standalone, execute

```bash
$ gradle bundledJar
```

and pick up `build/libs/alpha-bundled.jar`.

## Usage

Solve 3-colorability for some benchmarking instance and filter for color predicates:

```bash
$ java -jar alpha.jar -i benchmarks/omiga/omiga-testcases/3col/3col-10-18.txt -fblue -fred -fgreen
```

## Suggested Reading

 * [Answer Set programming: A Primer](http://www.kr.tuwien.ac.at/staff/tkren/pub/2009/rw2009-asp.pdf)
 * [ASP-Core-2 Input Language Format](https://www.mat.unical.it/aspcomp2013/files/ASP-CORE-2.01c.pdf)
 * [Conflict-Driven Answer Set Solving: From Theory to Practice](http://www.cs.uni-potsdam.de/wv/pdfformat/gekasc12c.pdf)
 * [Learning Non-Ground Rules for Answer-Set Solving](http://kr.irlab.org/sites/10.56.35.200.gttv13/files/gttv13.pdf#page=31)

## Similar Work

 * [Smodels](http://www.tcs.hut.fi/Software/smodels/), a solver usually used in conjunction with lparse.
 * [DLV](http://www.dlvsystem.com/dlv/)
 * [ASPeRiX](http://www.info.univ-angers.fr/pub/claire/asperix/), a solver that implements grounding-on-the-fly.
