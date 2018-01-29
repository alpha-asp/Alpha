% Domain: BLOCKS, Problem: BLOCKS-5-2
% Source: https://www.cs.uni-potsdam.de/hclasp/benchmarks/pddl_benchmarks/pddl-benchmarks.zip
% 
% 
% OBJECTS  >>>>>
typedobject( block( a ) ).
typedobject( block( c ) ).
typedobject( block( b ) ).
typedobject( block( e ) ).
typedobject( block( d ) ).
% <<<<<  OBJECTS
% 

% 
% 
% INIT  >>>>>
init( on( a,b ) ).
init( on( e,c ) ).
init( ontable( b ) ).
init( on( d,e ) ).
init( clear( d ) ).
init( handempty ).
init( on( c,a ) ).
% <<<<<  INIT
% 

% 
% 
% GOAL  >>>>>
goal( on( d,c ),true ).
goal( on( c,b ),true ).
goal( on( b,e ),true ).
goal( on( e,a ),true ).
% <<<<<  GOAL
% 