% Domain: BLOCKS, Problem: BLOCKS-5-0
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
init( on( c,e ) ).
init( ontable( d ) ).
init( ontable( a ) ).
init( on( e,b ) ).
init( clear( d ) ).
init( handempty ).
init( on( b,a ) ).
init( clear( c ) ).
% <<<<<  INIT
% 

% 
% 
% GOAL  >>>>>
goal( on( a,e ),true ).
goal( on( e,b ),true ).
goal( on( b,d ),true ).
goal( on( d,c ),true ).
% <<<<<  GOAL
% 