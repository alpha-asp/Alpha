# Alpha

[![Travis-CI Build Status](https://travis-ci.org/alpha-asp/Alpha.svg?branch=master)](https://travis-ci.org/alpha-asp/Alpha)
[![AppVeyor Build Status](https://ci.appveyor.com/api/projects/status/3wrwa7on0en01y7u?svg=true)](https://ci.appveyor.com/project/lorenzleutgeb/alpha)
[![Coverage Status](https://coveralls.io/repos/github/alpha-asp/Alpha/badge.svg?branch=master)](https://coveralls.io/github/alpha-asp/Alpha?branch=master)
[![Code Quality Status](https://codebeat.co/badges/10b609be-9774-42a1-b7fe-2bb64382744d)](https://codebeat.co/projects/github-com-alpha-asp-alpha-master)

Alpha is an [Answer Set Programming (ASP)](https://en.wikipedia.org/wiki/Answer_set_programming) system: It reads a
logic program (a set of logical rules) and computes the corresponding answer sets. ASP falls into the category of
declarative and logic programming. Its applications are solving combinatorial problems, but it also is a good tool for
reasoning in the context of knowledge-representation and databases.

Alpha is the successor of [OMiGA](http://www.kr.tuwien.ac.at/research/systems/omiga/) and currently in development.
In contrast to many other ASP systems, Alpha implements a *lazy-grounding* approach in hopes of overcoming memory
constraints when working with large input.

Alpha is not the fastest system available, since its goal is not to be the fastest system with current technology but
to explore new technologies rapidly. Those technologies, like lazy-grounding, allow Alpha to succeed where other ASP
systems fail completely. The project deliberately chooses to trade shorter execution times (which would be possible by
using unmanaged runtimes, e.g. C/C++, and low-level optimization) for a more straight forward system design and
possibilities to interface with the ecosystem built around the Java Virtual Machine.

## Getting Started

Download a current version of `alpha.jar` from [Releases](https://github.com/AntoniusW/Alpha/releases).

Running Alpha is as simple as running any other JAR:

```bash
$ java -jar alpha.jar
```

### Example Usage

Solve 3-colorability for some benchmarking instance and filter for color predicates:

```bash
$ java -jar alpha.jar -i benchmarks/omiga/omiga-testcases/3col/3col-10-18.txt -fblue -fred -fgreen
```

Note that in this example the path to the input file is relative to the root of this repository. If you have not checked out the repository, you can just [download the example file from GitHub](/benchmarks/omiga/omiga-testcases/3col/3col-10-18.txt).

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

## Suggested Reading

 * [Answer Set Programming: A Primer](http://www.kr.tuwien.ac.at/staff/tkren/pub/2009/rw2009-asp.pdf)
 * [ASP-Core-2 Input Language Format](https://www.mat.unical.it/aspcomp2013/files/ASP-CORE-2.01c.pdf)
 * [Conflict-Driven Answer Set Solving: From Theory to Practice](http://www.cs.uni-potsdam.de/wv/pdfformat/gekasc12c.pdf)
 * [Learning Non-Ground Rules for Answer-Set Solving](http://kr.irlab.org/sites/10.56.35.200.gttv13/files/gttv13.pdf#page=31)

### Research Papers on Alpha

 * [Blending Lazy-Grounding and CDNL Search for Answer-Set Solving](https://doi.org/10.1007/978-3-319-61660-5_17) ([preprint](http://www.kr.tuwien.ac.at/research/systems/alpha/blending_lazy_grounding.pdf))
 * [Introducing Heuristics for Lazy-Grounding ASP Solving](https://sites.google.com/site/paoasp2017/Taupe-et-al.pdf)
 * [Lazy-Grounding for Answer Set Programs with External Source Access](https://www.ijcai.org/proceedings/2017/0141.pdf)
 * [Techniques for Efficient Lazy-Grounding ASP Solving](https://www.uni-wuerzburg.de/fileadmin/10030100/Publications/TR_Declare17.pdf#page=131)

## Similar Work

 * [Smodels](http://www.tcs.hut.fi/Software/smodels/), a solver usually used in conjunction with lparse.
 * [DLV](http://www.dlvsystem.com/dlv/)
 * [ASPeRiX](http://www.info.univ-angers.fr/pub/claire/asperix/), a solver that implements grounding-on-the-fly.
