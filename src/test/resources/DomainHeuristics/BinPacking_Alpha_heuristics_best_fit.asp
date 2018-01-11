% Bin Packing:
% Every item must be assigned to exactly one bin and for every bin, the sum of sizes must be smaller or equal to the bin capacity.

% 1 { item_bin(I,B) : bin(B) } 1 :- item(I).
    item_bin(I,B) :- item(I), item_size(I,IS), bin(B), bin_size(BS), not not_item_bin(I,B).%, not _h(0).
    item_bin(I,B) :- item(I), item_size(I,IS), bin(B), bin_size(BS), rem(B,I,RS), RS >= 0, F=BS-RS, not _h(F,B), not not_item_bin(I,B), not item_has_other_bin(I,B), BS >= IS.
not_item_bin(I,B) :- item(I), item_size(I,IS), bin(B), bin_size(BS), not     item_bin(I,B), not _h(0,0).%, rem(B,I,RS), not _h(RS,0).
rem(B,I,RS) :- item(I), bin(B), bin_size(BS), item_bin(I,B), possible_fill_degree(F), RS=BS-F.
possible_fill_degree(F) :- F=0.
possible_fill_degree(F) :- bin_size(BS), possible_fill_degree(Fm), F=Fm+1, F <= BS.
rem(B,I,RS) :- item(I), item_size(I,IS), bin(B), bin_size(BS), not item_bin(I,B), bin_top(B,T), RS=BS-IS-T, not _h(RS).
:- item(I), item_bin(I,B1), item_bin(I,B2), B1 < B2.
:- item(I), not item_has_bin(I).
item_has_bin(I) :- item_bin(I,B).
item_has_other_bin(I,B) :- bin(B), bin(B2), item_bin(I,B2), B!=B2.

% :- bin_size(BS), BS < #sum { IS,I : item_size(I,IS), item_bin(I,B) }.
:- bin_filled(B,F), bin_size(BS), F > BS.
bin_top(B,T) :- bin_size(BS), bin_filled(B,T), not bin_filled_more_than(B,T).
bin_filled_more_than(B,T) :- bin_size(BS), bin_filled(B,T), bin_filled(B,Tp), Tp > T, Tp <= BS.
bin_filled(B,F) :- bin_filled(B,F,I).
bin_filled(B,0,0) :- bin(B).
bin_filled(B,F,I) :- item_bin(I,B), item_size(I,F).
bin_filled(B,F,I) :- item_bin(I,B), item_size(I,IS), bin_filled(B,Fm,Im), F=Fm+IS, Im<I.
bin_filled(B,F,I) :- bin_filled(B,F,Im), item(I), Im < I.

% every bin has the same size (this constraint is just for checking the input instance):
:- bin_size(BS1), bin_size(BS2), BS1 < BS2.