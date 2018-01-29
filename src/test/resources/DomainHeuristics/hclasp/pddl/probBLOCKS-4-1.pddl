% Domain: BLOCKS, Problem: BLOCKS-4-1
% Source: https://www.cs.uni-potsdam.de/hclasp/benchmarks/pddl_benchmarks/pddl-benchmarks.zip
% 
% 
% OBJECTS  >>>>>
typedobject( block( a ) ).
typedobject( block( c ) ).
typedobject( block( b ) ).
typedobject( block( d ) ).
% <<<<<  OBJECTS
% 

% 
% 
% INIT  >>>>>
init( on( b,c ) ).
init( ontable( d ) ).
init( on( a,d ) ).
init( clear( b ) ).
init( handempty ).
init( on( c,a ) ).
% <<<<<  INIT
% 

% 
% 
% GOAL  >>>>>
goal( on( d,c ),true ).
goal( on( c,a ),true ).
goal( on( a,b ),true ).
% <<<<<  GOAL
% 