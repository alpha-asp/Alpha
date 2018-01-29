% Domain: BLOCKS, Problem: BLOCKS-4-2
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
init( on( c,b ) ).
init( ontable( d ) ).
init( ontable( b ) ).
init( clear( d ) ).
init( ontable( a ) ).
init( clear( c ) ).
init( handempty ).
init( clear( a ) ).
% <<<<<  INIT
% 

% 
% 
% GOAL  >>>>>
goal( on( a,b ),true ).
goal( on( b,c ),true ).
goal( on( c,d ),true ).
% <<<<<  GOAL
% 