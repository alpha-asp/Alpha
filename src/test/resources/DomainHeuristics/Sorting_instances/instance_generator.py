import sys
from random import randint

count = int(sys.argv[1])
i = 1
set = set()
for i in range(count):
	r = randint(1,count)
	if (r not in set):
		set.add(r)
		print("n({}).".format(r))
print("index(1..{}).".format(len(set)))
print("count({}).".format(len(set)))
print("max_n({}).".format(max(set)))