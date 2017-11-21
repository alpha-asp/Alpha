% Simple assignment problem in which
% an item must be placed in some bin

item(1..5).
bin(1..5).

% GUESS
in(I,B):- not out(I,B), item(I), bin(B), _h(B,2).
out(I,B):- not in(I,B), item(I), bin(B), _h(B,1).

%CHECK
placed(I) :- in(I,_).
:- item(I), not placed(I).

:- in(I, B1), in(I,B2), B1 != B2.