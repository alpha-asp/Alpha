% Domain: BLOCKS, Problem: BLOCKS-4-0
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
init( clear( d ) ).
init( ontable( a ) ).
init( ontable( c ) ).
init( clear( b ) ).
init( ontable( b ) ).
init( ontable( d ) ).
init( clear( a ) ).
init( handempty ).
init( clear( c ) ).
% <<<<<  INIT
% 

% 
% 
% GOAL  >>>>>
goal( on( d,c ),true ).
goal( on( c,b ),true ).
goal( on( b,a ),true ).
% <<<<<  GOAL
% 

