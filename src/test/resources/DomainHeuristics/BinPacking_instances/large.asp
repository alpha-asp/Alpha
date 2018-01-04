item(1..10000).
item_size(I,I) :- item(I).

bin(1..1000).
bin_size(B,BS) :- bin(B), BS = (1000-B) * 100.