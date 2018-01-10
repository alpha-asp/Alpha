% Bin Packing:
% Every item must be assigned to exactly one bin and for every bin, the sum of sizes must be smaller or equal to the bin capacity.

1 { item_bin(I,B) : bin(B) } 1 :- item(I).
:- bin(B), bin_size(BS), BS < #sum { IS,I : item_size(I,IS), item_bin(I,B) }.

% every bin has the same size (this constraint is just for checking the input instance):
:- bin_size(BS1), bin_size(BS2), BS1 < BS2.