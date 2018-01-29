% Based on pddl.gringo.lp, adapted for Alpha

lasttime(6).
time(0..T) :- lasttime(T).
next(T,T1) :- time(T), time(T1), T1 = T + 1.

% fluents for sat planning
fluent(F) :- init(F). fluent(F) :- adds(A,F).
{ holds(F,T) } :- fluent(F), time(T), T > 0.

% non concurrent actions
%0 { apply(A,T) : action(A) } 1 :- time(T), T!=lasttime.
{ apply(A,T) } :- action(A), time(T), lasttime(LT), T < LT.
:- time(T), apply(A1,T), apply(A2,T), A1<A2.

% initial situation
holds(F,0) :- init(F).

% actions preconditions
:- time(T), not holds(F,T), apply(A,T), demands(A,F,true).
:- time(T), holds(F,T), apply(A,T),    demands(A,F,false).

% actions direct effects
:- not holds(F,T1), apply(A,T), adds(A,F), next(T,T1).
:- holds(F,T1), apply(A,T), deletes(A,F),  next(T,T1).

% inertia
:- holds(F,T1), not holds(F,T), not added(F,T),	next(T,T1).
added(F,T) :- adds(A,F), apply(A,T).
:- not holds(F,T1), holds(F,T), not deleted(F,T), next(T,T1).
deleted(F,T) :- deletes(A,F), apply(A,T).

% final situation
:- lasttime(T), not holds(F,T), goal(F,true).
:- lasttime(T), holds(F,T),  goal(F,false).
