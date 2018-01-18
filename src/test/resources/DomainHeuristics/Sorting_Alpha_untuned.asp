% input: n(N) - input number
% output: pos(N,P) - mapping of input number to position in sorted list

% 1 { pos(N,P) : index(P) } 1 :- n(N).
% 1 { pos(N,P) : n(N) } 1 :- index(P).
   pos(N,P) :- n(N), index(P), not no_pos(N,P).
no_pos(N,P) :- n(N), index(P), not    pos(N,P).
:- pos(N1,P), pos(N2,P), N1<N2.
:- pos(N,P1), pos(N,P2), P1<P2.
index_has_n(I) :- pos(_,I).
:- index(I), not index_has_n(I).
n_has_index(N) :- pos(N,_).
:- n(N), not n_has_index(N).

:- pos(N1,P1), pos(N2,P2), P1<P2, N1>=N2.