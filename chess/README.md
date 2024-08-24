
# How does this work ?

Basically A Negamax (vs MinMax) implementation using HAT to offload board generation and scoring to the GPU for each ply.

## Algorithm Notes

We need a way to capture the state of play after a specific move in a game for chess. 

At each stage of a game, a player has a number of possible valid moves.  We will call the 'set' of possible
moves from a given board position a 'ply' (or plie?)      

From  [www.chessprogramming.org](https://www.chessprogramming.org/Encoding_Moves#MoveIndex)we learn 
> For the standard chess piece set we have an upper bound of 12*8 + 8*2 + 13*2 + 14*2 + 27 + 8 == 201
> possible moves before considering promotions. 
 
> Each knight promotion reduces it by 12-8=4.
> Each bishop promotion increases it by 1. rook promotion by 2; queen promotion by 15.
> In the parametrized worst case (all pawns promoted to either rooks or queens) we have n queen and 8-n rook promotions,
> so the upper bound is f(n) = 8*2 + 13*2 + 14*(2+8-n) + 27*(1+n) + 8 == 217 + 13n. f(n=3)=256 <= 2^8.

So a conservative number of possible valid moves (ply size) for each
position (ignoring castling, en passant and exotic endgames where all pawns get promoted)
is 218 moves. 

From [rec.games.chess](https://groups.google.com/g/rec.games.chess/c/RspnvkCEY7s/m/W4kUZ0uH7jMJ) we learn 
that from analysis of real games that we need on average 5.5 bits to encode the next ply size (so between 32 and 64... 48?)

Given that the ply size basically determines the average number of nodes that each board in a 'tree' will generate,
there is a huge difference between the number of tree of nodes with 218 children per node and one with 48 per node. :)

If we were not trying to use the GPU for this, we might just create a Board Class

```java
import java.util.ArrayList;

class Board {
    // some state goes here
    List<Board> children = new ArrayList<>();
}
```
or possibly 

```java
class Board{
    // some state goes here
    Board[] children = new Board[218];
}
```
Let's assume we want our engine to look 2 opponent moves ahead.  

We create a Board for with our initial position (ply1) and dynamically build the tree under this for all 'white' possible moves, 
then under all the nodes in this 'layer' (ply2), then we create subtrees for possible 'black' responses (ply3), then another
for the white responses to blacks responses (ply4) and finally another black layer, (ply5)

Each 'layer' (nodes at the same depth) represent a 'ply'

Apparently  [kaabilkids.com/blog](https://kaabilkids.com/blog/how-many-moves-can-you-see-ahead-in-chess) intermediate
are expected to see 3-4 moves ahead.

So lets head for 3 moves.

For the hardcoded 218 per board tree that's a lot of nodes. ....

| Ply | Side  | Boards | Nodes      | Assume 80 bytes per board |
|-----|-------|--------|------------|---------------------------|
| 1   | Init  | 218^0  | 1          | 80                        |
| 2   | White | 218^1  | 218        | 17KB                      |
| 3   | Black | 218^2  | 46225      | 3MB                       |
| 4   | White | 218^3  | 10360232   | 828MB                     |
| 5   | Black | 218^4  | 2258530576 | 180GB                     |

Great Scott ! 
![Thats a lot!](https://images.squarespace-cdn.com/content/v1/545f5b33e4b0719cb5aee3a5/1611821441350-N6LB022SDONMF9SREN6W/Doc+Brown?format=15w)

For our '5.5 bits' per ply case, the numbers still get pretty big

| Ply | Side  | Boards | Nodes    | Assume 80 bytes per board |
|-----|-------|--------|----------|---------------------------|
| 1   | Init  | 48^0   | 1        | 80                        |
| 2   | White | 48^1   | 48       | 3.8KB                     |
| 3   | Black | 48^2   | 2304     | 184KB                     |
| 4   | White | 48^3   | 110592   | 8.8MB                     |
| 5   | Black | 48^4   | 5308416  | 425MB                     |

but at least it will fit in memory.

[Humbling... When I was a lad note :-](https://en.wikipedia.org/wiki/1K_ZX_Chess) 
> 1K ZX Chess (for 1982 Sinclair ZX81) was able to play an (albeit poor) game in 672 bytes ;) and is generally regarded as...
> "history's greatest game programming feat"

Of course we can't use Java heap if we wish to run in the GPU, instead we need to allocate (one or more)
MemorySegment to contain all of our nodes.   

Using HAT interface mapping we create a Table of boards. Say 5308416+110592+2304+48+1 nodes = 5421361  

To help is manage data, all boards must be the same size... and 5.5 bits was just for the average

So assuming we can arrange for all Boards in our child plys to be in consecutive 'table ids'   

We use an iface mapping like this 

```java
public interface PlyWood extends Buffer {
    interface Board extends Struct {
        /* 64 bytes of board state... say 
         ♖♘♗♕♔♗♘♖|♙♙♙♙♙♙♙♙|        |        |        |        |♟♟♟♟♟♟♟♟|♜♞♝♛♚♝♞♜|
        */
        int firstChildId();
        void firstChildId(int firstChildId);
        int childCount();
        void childCount(int childCount);
    }
    int length(); //5421361 
    Board board(long idx);
}
```
 
So board(0) is initialized to current position and is ply1 ;) 

Let's assume it is start of game
board(0).state = ```♖♘♗♕♔♗♘♖|♙♙♙♙♙♙♙♙|        |        |        |        |♟♟♟♟♟♟♟♟|♜♞♝♛♚♝♞♜|```
board(0).moves(20) ;  
board(0).firstChildIdx(20);

To calc the moves for each ply we essentially have one kernel walk the squares and compute the count of moves
for a given side from the current position and store it in the board. This is embarrassingly parallel

We then need to determine space for the next ply. So we prefix scan each of these counts (feeding the scanned value as bases back 
into the board.

A board has two fields one for the local move count, and one for the resulting prefix scan.

Each ply has a startIndex and a count

So lets say we just completed ply 'n' which had 5 boards (plyStartIdx=1, plyCount=5)
with move counts of 20,31,42,30 and 70 moves

At this point firstChild =0 and is unknown.

```
                  |     board     |     board     |     board     |     board     |     board     |
                  |       0       |       1       |       2       |       3       |       4       |
                  | first | move  | first | move  | first | move  | first | move  | first | move  |
                  | child | count | child | count | child | count | child | count | child | count |
                  |   ?   |   20  |   ?   |   31  |   ?   |   42  |   ?   |   30  |   ?   |   70  |
```
Now we initialize this ply's first board's firstChildIdx with this plys {startIdx+count) 

```
                   |     board     |     board     |     board     |     board     |     board     |
                   |       0       |       1       |       2       |       3       |       4       |
         ply(n)    | first | move  | first | move  | first | move  | first | move  | first | move  |
  startIdx | count | child | count | child | count | child | count | child | count | child | count |
      1    |    5  |   6   |   20  |    ?  |   31  |   ?   |   42  |   ?  |   30  |    ? |   70  |   199
       \       /       ^
         \   /        /  
          + ---------/    
```

We can now prefix scan all moveCount fields into firstChild fields, by adding the sum of the previous board's 
firstChildId()+moveCount() fields to populate this board's firstChildId().

```
                   |     board     |     board     |     board     |     board     |     board     |
                   |       0       |       1       |       2       |       3       |       4       |
         ply(n)    | first | move  | first | move  | first | move  | first | move  | first | move  |
  startIdx | count | child | count | child | count | child | count | child | count | child | count |
      1    |    5  |   6   |   20  |   26  |   31  |  57   |   42  |   99  |   30  |   129 |   70  |   199
       \       /       ^\     /        ^\     /        ^\     /        ^\     /        ^\     /        ^
         \   /        /  \   /        /  \   /        /  \   /        /  \   /        /  \   /        / 
          + ---------/     + --------/     + --------/     + --------/     + --------/     + --------/  
```


So now the  firstChildId() in this ply is now set up for populating the next ply

So we execute the doMoves() kernel with range ply(n).size() to walk through the boards in this ply 
starting at ply(n).startIdx. to create and score

This kernel will create and score these new boards as well as count the number of moves that each new board will need.

When we are done. 

```java
var ply = plyTable(n);
var nextPly = plyTable(n+1);
nextPly.startIdx(board(ply.startIdx().firstChildId());
nextPly.count(board(ply.{startIdx()+size()-1}){firstChildId+moveCount}- nextPly.startIdx()
```

And we are now ready to prefix sum the next ply and repeat the process. 


## GPU/HAT challenges.

### Today we have naming of the parts

I forgot that C99 (unlike Java) can't distinguish between function and var identifiers.

So 
```
var pieceValue = pieceValue(bits);
```
Would really confuse the OpenCL compiler :)   We might consider a '_fn_' prefix for all functions

### Prefix sum is 'integral' to many parallel solutions ;)
[Prefix Sum](https://en.wikipedia.org/wiki/Prefix_sum)
<img src="https://www.intel.com/content/dam/developer/articles/technical/optimize-scan-operations-explicit-vectorization/optimize-scan-operations-explicit-vectorization-fig2.jpg"/>

From Guy Steel's paper 
<img style="background:white" src="https://upload.wikimedia.org/wikipedia/commons/thumb/8/81/Prefix_sum_16.svg/600px-Prefix_sum_16.svg.png"/>

<!--
![From wikipedia](https://upload.wikimedia.org/wikipedia/commons/thumb/8/81/Prefix_sum_16.svg/600px-Prefix_sum_16.svg.png)
-->

At present HAT does not provide a prefix sum, it should provide both exclusive and inclusive.

Possibly also integral image intrinsics.

* [Data parallel algorithms, Authors: W. Daniel Hillis, Guy L. Steele, Jr.](https://dl.acm.org/doi/pdf/10.1145/7902.7903)
* [NVidia prefix sum link](https://developer.nvidia.com/gpugems/gpugems3/part-vi-gpu-computing/chapter-39-parallel-prefix-sum-scan-cuda)
* [SSE/AVX](https://www.intel.com/content/www/us/en/developer/articles/technical/optimize-scan-operations-explicit-vectorization.html)
* [Kogge Stone](https://gwern.net/doc/cs/algorithm/1973-kogge.pdf)
* [ButterFly](https://jtristan.github.io/papers/topc19.pdf)

## Links
[GPU Notes From www.chessprogramming.org](https://www.chessprogramming.org/GPU#:~:text=There%20are%20in%20main%20four,and%20position%20evaluation%20on%20GPU)

## Prior work

[Chess Engine](https://github.com/dkozykowski/Chess-Engine-GPU/blob/main/src/moves.cu)

* Dawid Kożykowski
* Jan Karchut
* BSC project from 2017

[Perft implementation CUDA](https://github.com/ankan-ban/perft_gpu)

* Ankan Banerjee
* A CUDA chess engine
* [Discussion of perft on www.talkchess.com](https://www.talkchess.com/forum3/viewtopic.php?t=64983&start=4#p729152)

### And of course 

[Neural Net](https://lczero.org/)


## Links

[The Problem In short :)](https://stackoverflow.com/questions/31213219/facing-performance-problems-implementing-minimax-for-a-chess-game)

> I am trying to implement minimax algorithm for a little chess game. Maybe my premise is wrong and this is not something that should be attempted. Is it?

> The program works but there is a big performance issue:

>> Depth = 0, 1 or 2 the result is immediate.
>> Depth = 3 the result takes 15 seconds.
>> Depth = 4 - haven't got a result yet.


## One day 

[Alpha Beta](https://en.wikipedia.org/wiki/Alpha%E2%80%93beta_pruning)
[A Java  UIC Chess UI](https://github.com/nomemory/neat-chess) 
[Forsyth Edwards Notation (FEN)](https://support.chess.com/en/articles/8598397-what-are-pgn-fen)
[From WikiPedia](https://en.wikipedia.org/wiki/Forsyth%E2%80%93Edwards_Notation)

[Scalability but at what cost](https://www.usenix.org/system/files/conference/hotos15/hotos15-paper-mcsherry.pdf)


## Some Numbers 

So I got the chess engine to run 5 ply  on the GPU (I think :wink: )

```
Ply 0 boards 0 - 1 count = 1
Prefix 0 ms
Ply compute 42ms
-----------------------------------------------------
Ply 1 boards 1 - 21 count = 20
Prefix 0 ms
Ply compute 43ms
-----------------------------------------------------
Ply 2 boards 21 - 421 count = 400
Prefix 2 ms
Ply compute 47ms
-----------------------------------------------------
Ply 3 boards 421 - 8747 count = 8326
Prefix 6 ms
Ply compute 42ms
-----------------------------------------------------
Ply 4 boards 8747 - 185580 count = 176833
Prefix 10 ms
Ply compute 107ms
-----------------------------------------------------
ms305
```

Sadly improvement over multithreaded is not huge

Here are the multithreaded java #’s

```
Ply 0 boards 0 - 1 count = 1
Prefix 0 ms
Ply compute 6ms
-----------------------------------------------------
Ply 1 boards 1 - 21 count = 20
Prefix 0 ms
Ply compute 15ms
-----------------------------------------------------
Ply 2 boards 21 - 421 count = 400
Prefix 0 ms
Ply compute 44ms
-----------------------------------------------------
Ply 3 boards 421 - 8747 count = 8326
Prefix 2 ms
Ply compute 64ms
-----------------------------------------------------
Ply 4 boards 8747 - 185580 count = 176833
Prefix 13 ms
Ply compute 254ms
-----------------------------------------------------
ms402

```

I was worried that the prefix sum (needed to compact the board space between kernels) would dominate perf, but this does not seem to be the case.
Just realized that I need to ‘mod up the ply sizes’ to next GPU group size.

A ply size of  176833 for example will not fully occupy the GPU.

OpenCL (specifically) needs all groups to be the same size.  So given a global size of 
176833 (with factors 1, 19, 41, 227, 779, 4313, 9307, 176833) opencl will determine which of these sizes 
is closest to it's natural group size (usually 32,64,256 maybe 1024 ...)  

So we are probably only using 19/32 or 41/64 or 227/256 or 227/512 or 779/1024 lanes.

At least the count is not a prime  :) or we would only use one lane ;)