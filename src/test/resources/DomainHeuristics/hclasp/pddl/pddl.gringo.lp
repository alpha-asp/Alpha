% Source: https://www.cs.uni-potsdam.de/hclasp/benchmarks/pddl_benchmarks/pddl-benchmarks.zip

time(0..lasttime).
next(T,T1) :- time(T), time(T1), T1 = T + 1.

% fluents for sat planning
fluent(F) :- init(F). fluent(F) :- adds(A,F).
{ holds(F,T) } :- fluent(F), time(T), T > 0.

% non concurrent actions
0 { apply(A,T) : action(A) } 1 :- time(T), T!=lasttime.

% initial situation
holds(F,0) :- init(F).

% actions preconditions
:- time(T), not holds(F,T), apply(A,T), demands(A,F,true).
:- time(T), holds(F,T), apply(A,T),    demands(A,F,false).

% actions direct effects
:- not holds(F,T1), apply(A,T), adds(A,F), next(T,T1).
:- holds(F,T1), apply(A,T), deletes(A,F),  next(T,T1).

% inertia
:- holds(F,T1), not holds(F,T), not apply(A,T) : adds(A,F);    next(T,T1).
:- not holds(F,T1), holds(F,T), not apply(A,T) : deletes(A,F); next(T,T1).

% final situation
:- not holds(F,lasttime), goal(F,true).
:- holds(F,lasttime),  goal(F,false).

% display
#show apply/2.