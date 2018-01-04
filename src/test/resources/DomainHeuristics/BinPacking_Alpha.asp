% Bin Packing:
% Every item must be assigned to exactly one bin and for every bin, the sum of sizes must be smaller or equal to the bin capacity.

% 1 { item_bin(I,B) : bin(B) } 1 :- item(I).
    item_bin(I,B) :- item(I), bin(B), not not_item_bin(I,B).
not_item_bin(I,B) :- item(I), bin(B), not     item_bin(I,B).
:- item(I), item_bin(I,B1), item_bin(I,B2), B1 < B2.
:- item(I), not item_has_bin(I).
item_has_bin(I) :- item_bin(I,B).

% :- bin_size(B,BS), BS < #sum { IS,I : item_size(I,IS), item_bin(I,B) }.
:- bin_filled(B,F), bin_size(B,S), F > S.
bin_filled(B,F) :- bin_filled(B,F,I).
bin_filled(B,0,0) :- bin(B).
bin_filled(B,F,I) :- bin(B), item(I), item_bin(I,B), item_size(I,I_size), bin_filled(B,F_prev,I_prev), F = F_prev + I_size, I_prev < I, not between(B,I_prev,I).
between(B,I_prev,I) :- bin(B), item(I_prev), item(I_bet), item(I), item_bin(I_prev,B), item_bin(I_bet,B), item_bin(I,B), I_prev < I_bet, I_bet < I.