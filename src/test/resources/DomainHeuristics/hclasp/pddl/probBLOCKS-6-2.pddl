% Domain: BLOCKS, Problem: BLOCKS-6-2
% Source: https://www.cs.uni-potsdam.de/hclasp/benchmarks/pddl_benchmarks/pddl-benchmarks.zip
% 
% 
% OBJECTS  >>>>>
typedobject( block( a ) ).
typedobject( block( c ) ).
typedobject( block( b ) ).
typedobject( block( e ) ).
typedobject( block( d ) ).
typedobject( block( f ) ).
% <<<<<  OBJECTS
% 

% 
% 
% INIT  >>>>>
init( on( f,e ) ).
init( on( d,b ) ).
init( ontable( c ) ).
init( on( a,d ) ).
init( clear( a ) ).
init( handempty ).
init( on( e,c ) ).
init( on( b,f ) ).
% <<<<<  INIT
% 

% 
% 
% GOAL  >>>>>
goal( on( e,f ),true ).
goal( on( f,a ),true ).
goal( on( a,b ),true ).
goal( on( b,c ),true ).
goal( on( c,d ),true ).
% <<<<<  GOAL
% 