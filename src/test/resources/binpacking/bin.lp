item(1..5).
size(1,4).
size(2,2).
size(3,1).
size(4,3).
size(5,4).

capacity(5).
bin(1..5).

% GUESS
in(I,B):- not out(I,B), item(I), bin(B).
out(I,B):- not in(I,B), item(I), bin(B).

%CHECK
% every item is placed in exactly one bin
%:- item(I), #count{B: in(I,B)} <> 1.

placed(I) :- in(I,_).
:- item(I), not placed(I).

:- in(I, B1), in(I,B2), B1 != B2.

% bin capacity is preserved
:- bin(B), capacity(C), in(I,B), size(I,S1), S1>C.
:- bin(B), capacity(C), in(I,B), size(I,S1), in(I1,B), size(I1,S2), I1!= I2, S1+S2>C.