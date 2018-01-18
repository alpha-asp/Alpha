% input: n(N) - input number
% output: pos(N,P) - mapping of input number to position in sorted list

% 1 { pos(N,P) : index(P) } 1 :- n(N).
% 1 { pos(N,P) : n(N) } 1 :- index(P).
   pos(N,P) :- n(N), index(P), not no_pos(N,P), count(C), max_n(M), H1=M-N+2, H2=C-P+2, not _h(H1,H2).
no_pos(N,P) :- n(N), index(P), index(P2), pos(N,P2), P != P2.
no_pos(N,P) :- n(N), n(N2), index(P), pos(N2,P), N != N2.

:- pos(N1,P1), pos(N2,P2), P1<P2, N1>=N2.