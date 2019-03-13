motive(a).
motive(b).
guilty(b).

% ei incumbit probatio qui dicit, non qui negat
innocent(S) :- motive(S), not guilty(S).
