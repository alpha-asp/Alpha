# Alpha

[![Latest DOI](https://zenodo.org/badge/62882005.svg)](https://zenodo.org/badge/latestdoi/62882005)
[![Travis-CI Build Status](https://travis-ci.com/alpha-asp/Alpha.svg?branch=master)](https://travis-ci.com/alpha-asp/Alpha)
[![AppVeyor Build Status](https://ci.appveyor.com/api/projects/status/github/alpha-asp/alpha?svg=true&branch=master)](https://ci.appveyor.com/project/lorenzleutgeb/alpha)
[![codecov](https://codecov.io/gh/alpha-asp/Alpha/branch/master/graph/badge.svg)](https://codecov.io/gh/alpha-asp/Alpha)
[![Code Quality Status](https://codebeat.co/badges/10b609be-9774-42a1-b7fe-2bb64382744d)](https://codebeat.co/projects/github-com-alpha-asp-alpha-master)
[![Coverage Status](https://coveralls.io/repos/github/alpha-asp/Alpha/badge.svg?branch=master)](https://coveralls.io/github/alpha-asp/Alpha?branch=master)

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

Download a current version of `alpha.jar` from [Releases](https://github.com/alpha-asp/Alpha/releases).

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

[A coder's guide to answer set programming](https://madmike200590.github.io/asp-guide/) provides a short and high-level tutorial on Answer Set Programming.

## Building

Alpha uses the [Gradle build automation system](https://gradle.org). Executing

```bash
$ ./gradlew build
```

will automatically fetch all dependencies (declared in [`build.gradle`](build.gradle)) and compile the project.

Artifacts generated will be placed in `build/`. Most notably you'll find files ready for distribution at
`build/distributions/`. They contain archives which in turn contain a `bin/` directory with scripts to run Alpha on Linux
and Windows.

If you want to generate a JAR file to be run standalone, execute

```bash
$ ./gradlew bundledJar
```

and pick up `build/libs/alpha-bundled.jar`.

### A Note on IDEs

We have contributors using [IntelliJ IDEA](https://www.jetbrains.com/idea/) as well as [Eclipse IDE](https://www.eclipse.org/).
However, we decided to not check in files related to project configuration. For both tools, standard features to "import"
the project based on its Gradle build configuration are available, and they will infer sane defaults. If you
run into trouble feel free to file an issue.

## Suggested Reading

 * [Answer Set Programming: A Primer](http://www.kr.tuwien.ac.at/staff/tkren/pub/2009/rw2009-asp.pdf)
 * [ASP-Core-2 Input Language Format](https://www.mat.unical.it/aspcomp2013/files/ASP-CORE-2.01c.pdf)
 * [Conflict-Driven Answer Set Solving: From Theory to Practice](http://www.cs.uni-potsdam.de/wv/pdfformat/gekasc12c.pdf)
 * [Learning Non-Ground Rules for Answer-Set Solving](http://kr.irlab.org/sites/10.56.35.200.gttv13/files/gttv13.pdf#page=31)

### Research Papers on Alpha

Peer-reviewed publications part of journals, conferences and workshops:

#### 2021

 * [Solving Configuration Problems with ASP and Declarative Domain-Specific Heuristics](http://ceur-ws.org/Vol-2945/21-RT-ConfWS21_paper_4.pdf)

#### 2020

 * [Conflict Generalisation in ASP: Learning Correct and Effective Non-Ground Constraints](https://doi.org/10.1017/S1471068420000368)
 * [Advancing Lazy-Grounding ASP Solving Techniques - Restarts, Phase Saving, Heuristics, and More](https://doi.org/10.1017/S1471068420000332)

#### 2019

 * [Exploiting Partial Knowledge in Declarative Domain-Specific Heuristics for ASP](https://doi.org/10.4204/EPTCS.306.9) ([supplementary material](https://git-ainf.aau.at/DynaCon/website/tree/master/supplementary_material/2019_ICLP_Domain-Specific_Heuristics))
 * [Degrees of Laziness in Grounding: Effects of Lazy-Grounding Strategies on ASP Solving](https://doi.org/10.1007/978-3-030-20528-7_22) ([preprint](https://arxiv.org/abs/1903.12510) | [supplementary material](https://git-ainf.aau.at/DynaCon/website/tree/master/supplementary_material/2019_LPNMR_Degrees_of_Laziness))

#### 2018

 * [Exploiting Justifications for Lazy Grounding of Answer Set Programs](https://doi.org/10.24963/ijcai.2018/240)
 * [Lazy Grounding for Dynamic Configuration: Efficient Large-Scale (Re)Configuration of Cyber-Physical Systems with ASP](https://doi.org/10.1007/s13218-018-0536-x)

#### 2017

 * [Blending Lazy-Grounding and CDNL Search for Answer-Set Solving](https://doi.org/10.1007/978-3-319-61660-5_17) ([preprint](http://www.kr.tuwien.ac.at/research/systems/alpha/blending_lazy_grounding.pdf))
 * [Introducing Heuristics for Lazy-Grounding ASP Solving](https://sites.google.com/site/paoasp2017/Taupe-et-al.pdf)
 * [Lazy-Grounding for Answer Set Programs with External Source Access](https://doi.org/10.24963/ijcai.2017/141)
 * [Techniques for Efficient Lazy-Grounding ASP Solving](https://doi.org/10.1007/978-3-030-00801-7_9) ([technical report](https://www.uni-wuerzburg.de/fileadmin/10030100/Publications/TR_Declare17.pdf#page=131))

Others (e.g. non-peer-reviewed publications, less formal articles, newsletters):

 * [The Alpha Solver for Lazy-Grounding Answer-Set Programming](https://www.cs.nmsu.edu/ALP/2019/04/the-alpha-solver-for-lazy-grounding-answer-set-programming/)

## Similar Work

 * [Smodels](http://www.tcs.hut.fi/Software/smodels/), a solver usually used in conjunction with lparse.
 * [DLV](http://www.dlvsystem.com/dlv/)
 * [ASPeRiX](http://www.info.univ-angers.fr/pub/claire/asperix/), a solver that implements grounding-on-the-fly.
