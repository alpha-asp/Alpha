item(1..1000).
item_size(I,I) :- item(I).

bin(1..100).
bin_size(B,BS) :- bin(B), BS = (100-B) * 100.