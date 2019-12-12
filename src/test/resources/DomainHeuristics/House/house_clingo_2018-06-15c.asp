% INITIAL PROBLEM
cabinetDomain(C) :- cabinetDomainNew(C).
roomDomain(R) :- roomDomainNew(R).

% definition of lower and upper numbers of cabinets and rooms possible in a configuration
cabinetLower {cabinet(C) : cabinetDomain(C)} cabinetUpper.
roomLower {room(R) : roomDomain(R)} roomUpper.

% ordering of used cabinets and rooms
room(R1) :- roomDomainNew(R1), roomDomainNew(R2), room(R2), R1 < R2.
cabinet(C1) :- cabinetDomainNew(C1), cabinetDomainNew(C2), cabinet(C2), C1 < C2.

% association cabinetTOthing
1 {cabinetTOthing(C,T) : cabinetDomain(C)} 1 :- thing(T).
:- 6 {cabinetTOthing(C,T) : thing(T)}, cabinet(C).

% association roomTOcabinet
1 {roomTOcabinet(R,C) : roomDomain(R)} 1 :- cabinet(C).
:- 5 {roomTOcabinet(R,C) : cabinetDomain(C)}, room(R).

% association personTOroom
% a room belongs to a person, who stores things in cabinets in that room
personTOroom(P,R) :- personTOthing(P,T), cabinetTOthing(C,T), roomTOcabinet(R,C).

% things of one person cannot be placed in a cabinet together with things of another person
:- cabinetTOthing(C,T1), cabinetTOthing(C,T2),
   personTOthing(P1,T1), personTOthing(P2,T2), P1 < P2.

% cabinets of different people cannot be placed in the same room
% i.e. one and the same room cannot belong to 2 different persons
:- personTOroom(P1,R), personTOroom(P2,R), P1 < P2.

room(R) :- roomTOcabinet(R,C).
room(R) :- personTOroom(P,R).

cabinet(C) :- cabinetTOthing(C,T).
cabinet(C) :- roomTOcabinet(R,C).

% MODIFIED PROBLEM
% there are 2 types of things which are disjoint
thing(T) :- thingLong(T).
thing(T) :- thingShort(T).
:- thingLong(T), thingShort(T).

% long things have to be packed in high cabinets
1 {cabinetHigh(C); cabinetSmall(C)} 1 :- cabinet(C).
cabinetHigh(C) :- thingLong(T), cabinetTOthing(C,T).

% at most either 2 high things or 1 high and 2 short or 4 short cabinets
% are allowed to be in a room
% assumption : there are 4 cabinet slots in each room.
% High cabinet requires 2 slots, small - 1 slot
cabinetSize(C,1) :- cabinet(C), cabinetSmall(C).
cabinetSize(C,2) :- cabinet(C), cabinetHigh(C).
roomTOcabinetSize(R,C,S) :- roomTOcabinet(R,C), cabinetSize(C,S).
:- 5 <= #sum { S,C : roomTOcabinetSize(R,C,S), cabinetDomain(C) }, room(R).

% TRANSFORMATION RULES
person(P) :- legacyConfig(person(P)).
thing(T) :- legacyConfig(thing(T)).
personTOthing(P,T) :- legacyConfig(personTOthing(P,T)).

1 {reuse(cabinetTOthing(C,T)); delete(cabinetTOthing(C,T))} 1 :- legacyConfig(cabinetTOthing(C,T)).
1 {reuse(roomTOcabinet(R,C));  delete(roomTOcabinet(R,C)) } 1 :- legacyConfig(roomTOcabinet(R,C)).
1 {reuse(personTOroom(P,R));   delete(personTOroom(P,R))  } 1 :- legacyConfig(personTOroom(P,R)).

% all cabinets from the original solution are added to the new domain
cabinetDomain(C) :- legacyConfig(cabinet(C)).
1 {reuse(cabinet(C)); delete(cabinet(C))} 1 :- legacyConfig(cabinet(C)).

% all rooms from the original solution are added to the new domain
roomDomain(R) :- legacyConfig(room(R)).
1 {reuse(room(R)); delete(room(R))} 1 :- legacyConfig(room(R)).

% all reused atoms should be a part of a reconfiguration
% i.e. they should be defined as facts
cabinetTOthing(C,T) :- reuse(cabinetTOthing(C,T)).
roomTOcabinet(R,C) :- reuse(roomTOcabinet(R,C)).
personTOroom(P,R) :- reuse(personTOroom(P,R)).
cabinet(C) :- reuse(cabinet(C)).
room(R) :- reuse(room(R)).

% all deleted atoms cannot be used in a configuration
:- cabinetTOthing(C,T), delete(cabinetTOthing(C,T)).
:- roomTOcabinet(R,C), delete(roomTOcabinet(R,C)).
:- personTOroom(P,R), delete(personTOroom(P,R)).
:- cabinet(C), delete(cabinet(C)).
:- room(R), delete(room(R)).

% COSTS
% Creation costs
cost(create(cabinetHigh(C)),W) :- cabinetHigh(C), cabinetHighCost(W), cabinetDomainNew(C).
cost(create(cabinetSmall(C)),W) :- cabinetSmall(C), cabinetSmallCost(W), cabinetDomainNew(C).
cost(create(room(R)),W):- room(R), roomCost(W), roomDomainNew(R).
cost(create(cabinetTOthing(C,T)), W) :- cabinetTOthing(C,T), cabinetTOthingCost(W), not legacyConfig(cabinetTOthing(C,T)).
cost(create(roomTOcabinet(R,C)), W) :- roomTOcabinet(R,C), roomTOcabinetCost(W), not legacyConfig(roomTOcabinet(R,C)).
cost(create(personTOroom(P,R)), W) :- personTOroom(P,R), personTOroomCost(W), not legacyConfig(personTOroom(P,R)).

% Reusage costs
cost(reuse(cabinetTOthing(C,T)), W) :- reuse(cabinetTOthing(C,T)), reuseCabinetTOthingCost(W).
cost(reuse(roomTOcabinet(R,C)), W) :- reuse(roomTOcabinet(R,C)), reuseRoomTOcabinetCost(W).
cost(reuse(personTOroom(P,R)), W) :- reuse(personTOroom(P,R)), reusePersonTOroomCost(W).
cost(reuse(cabinet(C)), W) :- reuse(cabinet(C)), cabinetHigh(C), reuseCabinetAsHighCost(W).
cost(reuse(cabinet(C)), W) :- reuse(cabinet(C)), cabinetSmall(C), reuseCabinetAsSmallCost(W).
cost(reuse(room(R)), W) :- reuse(room(R)), reuseRoomCost(W).

% Removal costs
cost(delete(cabinetTOthing(C,T)), W) :- delete(cabinetTOthing(C,T)), removeCabinetTOthingCost(W).
cost(delete(roomTOcabinet(R,C)), W) :- delete(roomTOcabinet(R,C)), removeRoomTOcabinetCost(W).
cost(delete(personTOroom(P,R)), W) :- delete(personTOroom(P,R)), removePersonTOroomCost(W).
cost(delete(cabinet(C)), W) :- delete(cabinet(C)), removeCabinetCost(W).
cost(delete(room(R)), W) :- delete(room(R)), removeRoomCost(W).

% OPTIMIZATION
% Minimize reconfiguration costs
#minimize { W,X : cost(X,W) }.