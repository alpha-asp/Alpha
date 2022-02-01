# Getting Started with Alpha

The easiest way to start using Alpha is to download a current jar file from [releases](https://github.com/alpha-asp/Alpha/releases).
Note that you will need to have Java 8 or newer installed in order to be able to run the jar file.

To run the commandline application using the standalone jar, download the file `alpha-cli-app-${version}-bundled.jar` (where `${version}` stands for the respective release version).

## Running a simple example

Once you have downloaded the jar, you can run a simple example with the following command:
```
...$ java -jar alpha-cli-app-0.6.0-bundled.jar -str "world. hello :- world."
```
This example uses version 0.6.0. The output should look like this:
```
Answer set 1:
{ hello, world }
SATISFIABLE
```
