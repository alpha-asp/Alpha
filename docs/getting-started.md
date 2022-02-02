# Getting Started with Alpha

The easiest way to start using Alpha is to download a current jar file from [releases](https://github.com/alpha-asp/Alpha/releases).
Note that you will need to have Java 8 or newer installed in order to be able to run the jar file.

## Running Alpha as a standalone application

To run the commandline application using the standalone jar, download the file `alpha-cli-app-${version}-bundled.jar` (where `${version}` stands for the respective release version).

### Running a simple example

Once you have downloaded the jar, you can run a "hello world" program with the following command:
```
...$ java -jar alpha-cli-app-0.6.0-bundled.jar -str "world. hello :- world."
```
This example uses version 0.6.0. The output should look like this:
```
Answer set 1:
{ hello, world }
SATISFIABLE
```

### Running larger ASP programs

Short program fragments can be passed to Alpha directly on the commandline using the `-str` option.
To solve one or more ASP files, use the option `-i`:
```
...$ java -jar alpha-cli-app-0.6.0-bundled.jar -i someFile.asp -i someOtherFile.asp -i oneMoreFile.asp
```

## Using Alpha as a Java Library

Since Alpha is written entirely in Java, it can easily be used as a dependency in other Java applications.
Note that, as of release 0.6.0, it is still necessary to build Alpha locally in order to install the needed jar files to the local maven repo.

### Running ASP programs from Java

This section assumes you are using Maven.
Once you have the Alpha jars in your local Maven repo, create a new project, and add the following dependency to your `pom.xml`
```
<dependency>
    <groupId>at.ac.tuwien.kr.alpha</groupId>
    <artifactId>alpha-solver</artifactId>
    <version>0.6.0</version>
</dependency>
```
