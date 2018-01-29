% Domain: BLOCKS, Problem: BLOCKS-10-1
% Source: https://www.cs.uni-potsdam.de/hclasp/benchmarks/pddl_benchmarks/pddl-benchmarks.zip
% 
% 
% OBJECTS  >>>>>
typedobject( block( a ) ).
typedobject( block( c ) ).
typedobject( block( b ) ).
typedobject( block( e ) ).
typedobject( block( d ) ).
typedobject( block( g ) ).
typedobject( block( f ) ).
typedobject( block( i ) ).
typedobject( block( h ) ).
typedobject( block( j ) ).
% <<<<<  OBJECTS
% 

% 
% 
% INIT  >>>>>
init( on( c,g ) ).
init( on( i,j ) ).
init( ontable( b ) ).
init( ontable( h ) ).
init( on( g,e ) ).
init( on( f,d ) ).
init( clear( c ) ).
init( handempty ).
init( on( j,a ) ).
init( on( a,b ) ).
init( on( e,i ) ).
init( on( d,h ) ).
init( clear( f ) ).
% <<<<<  INIT
% 

% 
% 
% GOAL  >>>>>
goal( on( c,b ),true ).
goal( on( b,d ),true ).
goal( on( d,f ),true ).
goal( on( f,i ),true ).
goal( on( i,a ),true ).
goal( on( a,e ),true ).
goal( on( e,h ),true ).
goal( on( h,g ),true ).
goal( on( g,j ),true ).
% <<<<<  GOAL
% 