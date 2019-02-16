city(wien).
city(eisenstadt).
city(st_poelten).
%city(graz).
%city(linz).
%city(salzburg).
%city(klagenfurt).
%city(bregenz).
%city(innsbruck).

road(wien, eisenstadt).
road(wien, st_poelten).
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

% one can reach a place via a road or a waterway
reachable_via_road_from(B, A) :- road(A, B), city(A), city(B), A != B.
reachable_via_waterway_from(B, A) :- waterway(A, B), city(A), city(B), A != B.

% reachability is transitive
reachable_from(C, A) :- reachable_from(B, A), reachable_from(C, B).

% waterways are also symmetric
waterway(B, A) :- waterway(A, B), city(A), city(B), A != B.

% *** integrity-checking constraints ***

% a city doesn't have a road leading to itself
:- road(A, A), city(A).

% ... the same goes for waterways
:- waterway(A, A), city(A).

