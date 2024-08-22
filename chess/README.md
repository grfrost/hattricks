
# How does this work ?

Basically A Negamax (vs MinMax) implementation using HAT to offload board generation and scoring to the GPU for each ply.

One kernel doMoves()
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
