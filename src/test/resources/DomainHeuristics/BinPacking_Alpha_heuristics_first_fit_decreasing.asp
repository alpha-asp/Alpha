% Bin Packing:
% Every item must be assigned to exactly one bin and for every bin, the sum of sizes must be smaller or equal to the bin capacity.

% 1 { item_bin(I,B) : bin(B) } 1 :- item(I).
    item_bin(I,B) :- item(I), item_size(I,IS), bin(B), not not_item_bin(I,B), bin_capacity_geq_0(B), not item_has_other_bin(I,B), ISp1=IS+1, not _h(ISp1).%, bin_room_for(B,I)
not_item_bin(I,B) :- item(I), bin(B), not     item_bin(I,B).
:- item(I), item_bin(I,B1), item_bin(I,B2), B1 < B2.
:- item(I), not item_has_bin(I).
item_has_bin(I) :- item_bin(I,B).
item_has_other_bin(I,B) :- item_bin(I,B2), bin(B), B != B2. % will lead to space consumption problems! (but makes search faster)

% do not put item into bin if not enough room left:
% bin_room_for(B,I) :- item_bin(I,B). % Leads to recursive definition -- we do not have support for item_bin(I,B)!!!

% the following rules seem to work, but to make solving slower:
%bin_room_for(B,I) :- bin(B), item(I), item_size(I,IS), not not_item_bin(I,B), ISp1=IS+1. %, not _h(ISp1)
%bin_room_for(B,I) :- bin(B), item(I), item_size(I,IS), not item_bin(I,B), not bin_not_enough_room_for_itemsize(B,IS), ISp1=IS+1.%, not _h(ISp1)
%bin_not_enough_room_for_itemsize(B,IS) :- bin_size(BS), bin_filled(B,F), item_size(_,IS), FpIS = F + IS, FpIS > BS.

% :- bin_size(B,BS), BS < #sum { IS,I : item_size(I,IS), item_bin(I,B) }.
:- bin(B), not bin_capacity_geq_0(B).

% alternative formulation:
%bin_capacity_geq_0(B) :- bin(B), bin_size(BS), #sum { IS,I : item_bin(I,B), item_size(I,IS) } <= BS.

% formulation for Alpha:
bin_capacity_geq_0(B) :- bin(B), not bin_overfilled(B).
bin_possible_fill(0).
bin_possible_fill(Fp1) :- bin_possible_fill(F), Fp1=F+1, bin_size(BS), F < BS.
bin_overfilled(B) :- bin_filled(B,F), not bin_possible_fill(F).
:- bin(B), bin_overfilled(B).

% determine fill degrees:
max_item(I) :- item(I), Ip1 = I + 1, not item(Ip1).
bin_filled(B,F) :- max_item(I), bin_filled(B,I,F).
bin_filled(B,0,0) :- bin(B).
bin_filled(B,I,F) :- bin_filled(B,Im1,F), Im1 = I - 1, item(I), not item_bin(I,B).
bin_filled(B,I,F) :- bin_filled(B,Im1,F_prev), Im1 = I - 1, item(I), item_size(I,IS), F = F_prev + IS, item_bin(I,B).

% the bin_filled/3 definition above explodes in grounding, but is maybe still better than the following with between/3:
%bin_filled(B,F) :- bin_filled(B,_,F).
%bin_filled(B,0,0) :- bin(B).
%bin_filled(B,I,F) :- bin(B), bin_filled(B,I_prev,F_prev), item_bin(I,B), item_size(I,IS), F=F_prev+IS, I=I_prev+1.
%bin_filled(B,I,F) :- bin(B), bin_filled(B,I_prev,F_prev), item_bin(I,B), item_size(I,IS), F=F_prev+IS, not between(B,I_prev,I).
%between(B,I1,I2) :- item(I1), item(IB), item(I2), item_bin(IB,B), I1 < IB, IB < I2.
%between(B,0 ,I2) :-           item(IB), item(I2), item_bin(IB,B),          IB < I2.

% every bin has the same size (this constraint is just for checking the input instance):
:- bin_size(BS1), bin_size(BS2), BS1 < BS2.