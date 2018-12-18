city(wien).
city(eisenstadt).
%city(st_poelten).
%city(graz).
%city(linz).
%city(salzburg).
%city(klagenfurt).
%city(bregenz).
%city(innsbruck).

road(wien, eisenstadt).
%road(wien, st_poelten).
%road(st_poelten, linz).
%road(wien, graz).
%road(st_poelten, graz).
%road(linz, graz).
%road(linz, salzburg).
%road(graz, klagenfurt).
%road(klagenfurt, salzburg).
%road(salzburg, innsbruck).
%road(innsbruck, bregenz).

%waterway(wien, linz).

% roads can be travelled in both directions (i.e. roads are symmetric)
road(B, A) :- road(A, B), city(A), city(B), A != B.

% roads are transitive
% road(A, C) :- road(A, B), road(B, C), city(A), city(B), city(C), A != B, A != C, B != C.

% one can reach a place via a road or a waterway
reachable_from(B, A) :- road(A, B), city(A), city(B), A != B.
reachable_from(B, A) :- waterway(A, B), city(A), city(B), A != B.

% reachability is transitive
reachable_from(C, A) :- reachable_from(B, A), reachable_from(C, B).

% waterways are also symmetric and transitive
waterway(B, A) :- waterway(A, B), city(A), city(B), A != B.
% waterway(A, C) :- waterway(A, B), waterway(B, C), city(A), city(B), city(C), A != B, A != C, B != C.
