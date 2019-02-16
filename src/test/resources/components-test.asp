f0.
f1.
f2.
f3.

a :- f0, f1, not b.
b :- f0, f1, not a.
c :- f2, f3, not d.
d :- f2, f3, not c.

x :- a, c, y.
y :- b, d, x.
z :- x, y, z.
