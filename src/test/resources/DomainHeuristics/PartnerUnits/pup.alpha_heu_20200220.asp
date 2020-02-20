% QP*-like heuristic that processes zones and sensors by layers, avoids assigning units that are already full and zones/sensors that are already assigned, and tries existing units in decreasing order first and then a fresh unit.
% Version of pup.alpha_heu_20180510.asp in which heuristic directives have been converted to Alpha's new syntax

elem(z,Z) :- zone2sensor(Z,D).
elem(s,D) :- zone2sensor(Z,D).

{ assign(U,T,X) } :- elem(T,X), comUnit(U).

#heuristic   assign(U,T,X) : comUnit(U), elem(T,X), not F assign(U,T,X), maxLayer(ML), layer(T,X,L), not full(U,T), not assigned(T,X), not T used(U), nUnits(NU), U=1, Level=ML-L+1. [ NU@Level ] % start with 1st unit
#heuristic   assign(U,T,X) : comUnit(U), elem(T,X), not F assign(U,T,X), maxLayer(ML), layer(T,X,L), not full(U,T), not assigned(T,X),     T used(U), Level=2*(ML-L+2)+1. [ U@Level ] % try preceding units in decreasing order
#heuristic   assign(U,T,X) : comUnit(U), elem(T,X), not F assign(U,T,X), maxLayer(ML), layer(T,X,L), not full(U,T), not assigned(T,X), not T used(U), comUnit(Um1), used(Um1), n_assign(Um1,T,X), Level=2*(ML-L+2), Um1=U-1. [ U@Level ] % try the next fresh unit after all preceding units have been tried
#heuristic F assign(U,T,X) : comUnit(U), elem(T,X), not   assign(U,T,X), full(U,T). [ 1@1000 ] % do not use full units
#heuristic F assign(U,T,X) : comUnit(U), elem(T,X), not   assign(U,T,X). [ 2@1 ] % prefer not to assign zones before 1@1 rules start kicking in

:- assign(U,T,X1), assign(U,T,X2), assign(U,T,X3), X1<X2, X2<X3.
:- assign(U1,T,X), assign(U2,T,X), U1<U2.
atLeastOneUnit(T,X):- assign(_,T,X).
:- elem(T,X), not atLeastOneUnit(T,X).

partnerunits(U,P) :- assign(U,z,Z), assign(P,s,D), zone2sensor(Z,D), U!=P.
partnerunits(U,P) :- partnerunits(P,U).
:- partnerunits(U,P1), partnerunits(U,P2), partnerunits(U,P3), P1<P2, P2<P3.

% helpers for heuristics:
full(U,T) :- comUnit(U), assign(U,T,X1), assign(U,T,X2), X1<X2.
assigned(T,X) :- assign(_,T,X).
used(U) :- comUnit(U), assign(U,_,_).
% determine number of units (ATTENTION: this only works if units are numbered consecutively from 1):
nUnits(N) :- comUnit(N), not comUnit(Np1), Np1=N+1.

% translate old breadth-first syntax to new one:
layer(z,X,L) :- zoneLayer(X,L).
layer(s,X,L) :- sensorLayer(X,L).