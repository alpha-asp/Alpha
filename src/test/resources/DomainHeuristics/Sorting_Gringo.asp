% input: n(N) - input number
% output: pos(N,P) - mapping of input number to position in sorted list

1 { pos(N,P) : index(P) } 1 :- n(N).
1 { pos(N,P) : n(N) } 1 :- index(P).
:- pos(N1,P1), pos(N2,P2), P1<P2, N1>=N2.