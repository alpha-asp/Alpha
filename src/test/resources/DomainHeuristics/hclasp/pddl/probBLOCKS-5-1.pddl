% Domain: BLOCKS, Problem: BLOCKS-5-1
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
init( on( b,a ) ).
init( ontable( c ) ).
init( ontable( e ) ).
init( on( a,d ) ).
init( clear( c ) ).
init( ontable( d ) ).
init( clear( e ) ).
init( handempty ).
init( clear( b ) ).
% <<<<<  INIT
% 

% 
% 
% GOAL  >>>>>
goal( on( d,c ),true ).
goal( on( c,b ),true ).
goal( on( b,a ),true ).
goal( on( a,e ),true ).
% <<<<<  GOAL
% 