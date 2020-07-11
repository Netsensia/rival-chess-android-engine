Rival Chess Java Engine
=======================

The Rival chess Java engine libraries used in the Rival Chess Android App.

This is the main library used by the [Rival Chess UCI program](https://github.com/chris-moreton/rivalchess-uci).

### Installation

#### Maven

    <dependency>
        <groupId>com.netsensia.rivalchess</groupId>
        <artifactId>rivalchess-engine</artifactId>
        <version>28.1.0</version>
    </dependency>
    
#### Gradle

    compile group: 'com.netsensia.rivalchess', name: 'rivalchess-model', version: '28.1.0'
    
### Testing
    
    ./gradlew test
    
### Versions

Starting with version 28.1.0, I will be following Semver a bit more accurately.  Version number changes will signify:

* Major version - A breaking change for any code using this library
* Minor version - A new search feature or upgrade to the engine
* Patch version - either a bug fix or a speed increase/code refactoring which breaks no tests
    
### Example

The Search class would normally be run as a thread, allowing a caller to probe for the current state of the search
and to request the search to stop.

The following example doesn't start the process as a thread, but shows the basic usage.

    package com.netsensia.rivalchess.example
    
    import com.netsensia.rivalchess.engine.core.search.Search
    import com.netsensia.rivalchess.model.Board

    fun main(args: Array<String>) {
        val board = Board.fromFen("6k1/6p1/1p2q2p/1p5P/1P3RP1/2PK1B2/1r2N3/8 b - g3 5 56")
        val searcher = Search(board)
        searcher.setMillisToThink(5000)
        searcher.setNodesToSearch(Int.MAX_VALUE)
        searcher.setSearchDepth(5)
        searcher.go()
        println("Path = ${searcher.currentPath}")
    }
    
