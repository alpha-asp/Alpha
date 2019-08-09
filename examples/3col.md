# The *3-Coloring Problem*

The *3-Coloring Problem* is a decision problem: Given a *graph*, consisting of
*nodes* and *edges* that connect two nodes each, can we find a solution, i.e.
an assignment of one of three colors (here we call them red, green, and blue)
to every node, such that no nodes that are connected are assigned the same
color?

## Instance

We will be looking at ten nodes

    node(0..9).

that are connected as follows:

    connected(0,2).
    connected(0,3).
    connected(0,4).
    connected(1,3).
    connected(1,5).
    connected(1,7).
    connected(1,8).
    connected(2,3).
    connected(2,6).
    connected(2,7).
    connected(2,9).
    connected(3,6).
    connected(3,8).
    connected(4,5).
    connected(5,8).
    connected(5,9).
    connected(6,7).
    connected(7,8).

To ensure undirectedness of connections, we add the following rule
that establishes symmetry:

    connected(N,M) :- connected(M,N).

## Guess

We will use the predicates `red`, `green` and `blue` to mark vertices of the
respective color. Note that we enforce two conditions here:
  1. A node that is not of two colors, must be of the third color.
  2. A node cannot be colored by two colors at the same time.
  3. Following from (1.) and (2.) every node is assigned exactly one color.

    red(N)   :- node(N), not green(N), not blue(N).
    green(N) :- node(N), not red(N), not blue(N).
    blue(N)  :- node(N), not red(N), not green(N).

## Check

Additionally we need to impose restrictions such that two connected nodes
are not assigned the same color.

    :- connected(N1,N2), blue(N1), blue(N2).
    :- connected(N1,N2), red(N1), red(N2).
    :- connected(N1,N2), green(N1), green(N2).

## Evaluating the Program in this File

Run the following command, substituting the name of the JAR file and the path
to this file as required:

```sh
java -jar alpha-bundled.jar -l -i .../3col.md
```

Note that the flag `-l` indicates that the input is written in "literate"
style, i.e. that code is indented by four spaces.
