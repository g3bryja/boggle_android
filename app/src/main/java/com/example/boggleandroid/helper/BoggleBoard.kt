package com.example.boggleandroid.helper

import com.example.boggleandroid.data.BoggleTile
import kotlin.math.max
import kotlin.math.min

class BoggleBoard {
    private var boardSize = 0
    private var minWordSize = 0
    private var board: ArrayList<BoggleTile>? = null
    private var trie: Trie? = null

    constructor(boardSize: Int = 4, minWordSize: Int = 3) {
        instantiateBoard(boardSize, minWordSize)
    }

    /**
     * Instantiates a new Boggle board.
     */
    fun instantiateBoard(boardSize: Int = 4, minWordSize: Int = 3) {
        this.boardSize = boardSize
        this.minWordSize = minWordSize
        board = java.util.ArrayList(boardSize * boardSize)
    }

    /**
     * Initializes an existing Boggle board with the given [letters] in order.
     */
    fun initializeBoard(letters: ArrayList<String>, boardSize: Int = 4, minWordSize: Int = 3) {
        instantiateBoard(boardSize, minWordSize)
        for (x in 0 until boardSize) {
            for (y in 0 until boardSize) {
                board!!.add(BoggleTile("", x, y, getIndex(x, y)))
            }
        }
    }

    /**
     * Initializes a Boggle board with random letters.
     */
    fun initializeBoard(boardSize: Int = 4, minWordSize: Int = 3) {
        instantiateBoard(boardSize, minWordSize)
        for (x in 0 until boardSize) {
            for (y in 0 until boardSize) {
                board!!.add(BoggleTile("", x, y, getIndex(x, y)))
            }
        }
    }

    /**
     * Loads the given [trie].
     */
    fun loadDictionary(trie: Trie) {
        this.trie = trie
    }

    /**
     * Returns the index for the tile with the given ([x], [y]) coordinates.
     */
    private fun getIndex(x: Int, y: Int): Int {
        return x + y * boardSize
    }

    /**
     * Returns the tile with the given ([x], [y]) coordinates.
     */
    fun getTileAt(x: Int, y: Int): BoggleTile {
        return board!![getIndex(x, y)]
    }

    /**
     * Returns all tiles that border the specified [tile].
     */
    fun getNeighbors(tile: BoggleTile): ArrayList<BoggleTile> {
        var neighbors = arrayListOf<BoggleTile>()
        var xMin = max(tile.x - 1, 0)
        var xMax = min(tile.x + 1, boardSize - 1) + 1
        var yMin = max(tile.y - 1, 0)
        var yMax = min(tile.y + 1, boardSize - 1) + 1

        for (x in xMin until xMax) {
            for (y in yMin until yMax) {
                if (!(x === tile.x && y === tile.y)) {
                    neighbors.add(getTileAt(x, y))
                }
            }
        }
        return neighbors
    }

    /**
     * Returns the word spelled by [tiles].
     */
    fun getWord(tiles: ArrayList<BoggleTile>): String {
        var string = ""
        for (tile in tiles) {
            string += tile.value
        }
        return string
    }

    fun isValidWord(word: String): Boolean {
        return word.count() >= minWordSize && trie!!.isFullWord(word)
    }

    /**
     * Returns true if [tile] exists in [tiles].
     */
    fun pathContainsTile(tiles: ArrayList<BoggleTile>, tile: BoggleTile): Boolean {
        for (item in tiles) {
//            if (item.index == tile.index) {
            if (item === tile) {
                return true
            }
        }
        return false
    }

    /**
     * Returns all possible words found within the current Boggle board.
     */
    fun getWordList(): ArrayList<String> {
        var results = arrayListOf<String>()
        var searchStack = ArrayList<ArrayList<BoggleTile>>()
        for (tile in board!!) {
            searchStack.add(arrayListOf(tile))
        }
        while (searchStack.isNotEmpty()) {
            var path = searchStack.last()
            searchStack.dropLast(1)
            var word = getWord(path)
            if (word !in results && isValidWord(word)) {
                results.add(word)
            }
            var neighbors = getNeighbors(path.last())
            for (neighbor in neighbors) {
                if (!pathContainsTile(path, neighbor)) {
                    var newPath = path.map{ it.copy() } as ArrayList
                    newPath.add(neighbor)
                    if (trie!!.hasWord(getWord(newPath))) {
                        searchStack.add(newPath)
                    }
                }
            }
        }
        return results
    }
}
