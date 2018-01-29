% Domain: BLOCKS
% Source: https://www.cs.uni-potsdam.de/hclasp/benchmarks/pddl_benchmarks/pddl-benchmarks.zip
% 
% 
% EFFECTS: ADD  >>>>>
adds( putdown( X ),clear( X ) ) :- typedobject( block( X ) ).
adds( putdown( X ),handempty ) :- typedobject( block( X ) ).
adds( putdown( X ),ontable( X ) ) :- typedobject( block( X ) ).
adds( pickup( X ),holding( X ) ) :- typedobject( block( X ) ).
adds( unstack( X,Y ),holding( X ) ) :- typedobject( block( X ) ), typedobject( block( Y ) ).
adds( unstack( X,Y ),clear( Y ) ) :- typedobject( block( X ) ), typedobject( block( Y ) ).
adds( stack( X,Y ),clear( X ) ) :- typedobject( block( X ) ), typedobject( block( Y ) ).
adds( stack( X,Y ),handempty ) :- typedobject( block( X ) ), typedobject( block( Y ) ).
adds( stack( X,Y ),on( X,Y ) ) :- typedobject( block( X ) ), typedobject( block( Y ) ).
% <<<<<  EFFECTS: ADD
% 

% 
% 
% EFFECTS: DEL  >>>>>
deletes( putdown( X ),holding( X ) ) :- typedobject( block( X ) ).
deletes( pickup( X ),ontable( X ) ) :- typedobject( block( X ) ).
deletes( pickup( X ),clear( X ) ) :- typedobject( block( X ) ).
deletes( pickup( X ),handempty ) :- typedobject( block( X ) ).
deletes( unstack( X,Y ),clear( X ) ) :- typedobject( block( X ) ), typedobject( block( Y ) ).
deletes( unstack( X,Y ),handempty ) :- typedobject( block( X ) ), typedobject( block( Y ) ).
deletes( unstack( X,Y ),on( X,Y ) ) :- typedobject( block( X ) ), typedobject( block( Y ) ).
deletes( stack( X,Y ),holding( X ) ) :- typedobject( block( X ) ), typedobject( block( Y ) ).
deletes( stack( X,Y ),clear( Y ) ) :- typedobject( block( X ) ), typedobject( block( Y ) ).
% <<<<<  EFFECTS: DEL
% 

% 
% 
% TYPES  >>>>>
object( X ) :- typedobject( block( X ) ).
% <<<<<  TYPES
% 

% 
% 
% REIFIED ENCODING OF DOMAIN ACTIONS  >>>>>
action( putdown( X ) ) :- typedobject( block( X ) ).
action( pickup( X ) ) :- typedobject( block( X ) ).
action( unstack( X,Y ) ) :- typedobject( block( X ) ), typedobject( block( Y ) ).
action( stack( X,Y ) ) :- typedobject( block( X ) ), typedobject( block( Y ) ).
% <<<<<  REIFIED ENCODING OF DOMAIN ACTIONS
% 

% 
% 
% REIFIED ENCODING OF PRECONDITIONS  >>>>>
demands( putdown( X ),holding( X ),true ) :- typedobject( block( X ) ).
demands( pickup( X ),clear( X ),true ) :- typedobject( block( X ) ).
demands( pickup( X ),ontable( X ),true ) :- typedobject( block( X ) ).
demands( pickup( X ),handempty,true ) :- typedobject( block( X ) ).
demands( unstack( X,Y ),on( X,Y ),true ) :- typedobject( block( X ) ), typedobject( block( Y ) ).
demands( unstack( X,Y ),clear( X ),true ) :- typedobject( block( X ) ), typedobject( block( Y ) ).
demands( unstack( X,Y ),handempty,true ) :- typedobject( block( X ) ), typedobject( block( Y ) ).
demands( stack( X,Y ),holding( X ),true ) :- typedobject( block( X ) ), typedobject( block( Y ) ).
demands( stack( X,Y ),clear( Y ),true ) :- typedobject( block( X ) ), typedobject( block( Y ) ).
% <<<<<  REIFIED ENCODING OF PRECONDITIONS
% 