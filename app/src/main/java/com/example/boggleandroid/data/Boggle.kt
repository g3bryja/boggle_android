package com.example.boggleandroid.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlin.math.max
import kotlin.math.min

@Parcelize
data class Boggle(
    var trie: Trie,
    var board: MutableList<BoggleTile> = mutableListOf(),
    var found: MutableMap<String, Boolean> = mutableMapOf(),
    var scoring: MutableList<Int> = mutableListOf(1, 1, 2, 3, 5, 11),
    var boardSize: Int = 4,
    var minWordSize: Int = 3
) : Parcelable {
    enum class SearchResponse {
        FOUND_NEW_WORD,
        ALREADY_FOUND_WORD,
        WORD_DOES_NOT_EXIST
    }

    /**
     * Reinitializes the Boggle board from the given list of [letters] and resets the map of found words.
     */
    fun resetBoard(letters: MutableList<Char>) {
        board.clear()
        found.clear()

        var i = 0
        for (x in 0 until boardSize) {
            for (y in 0 until boardSize) {
                var letter = letters[i].toString()
                if (letter == "Q") {
                    letter = "QU"
                }
                board.add(BoggleTile(letter, x, y))
                i += 1
            }
        }

        findAllWords()
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
        return board[getIndex(x, y)]
    }

    /**
     * Returns all tiles that border the specified [tile].
     */
    fun getNeighbors(tile: BoggleTile): MutableList<BoggleTile> {
        val minBound = 0
        val maxBound = boardSize - 1
        var neighbors = mutableListOf<BoggleTile>()
        var xMin = max(tile.x - 1, minBound)
        var xMax = min(tile.x + 1, maxBound) + 1
        var yMin = max(tile.y - 1, minBound)
        var yMax = min(tile.y + 1, maxBound) + 1

        for (x in xMin until xMax) {
            for (y in yMin until yMax) {
                if (!(tile.equals(x, y))) {
                    neighbors.add(getTileAt(x, y))
                }
            }
        }
        return neighbors
    }

    /**
     * Returns the word spelled by [tiles].
     */
    fun getWord(tiles: MutableList<BoggleTile>): String {
        var word = ""
        for (tile in tiles) {
            word += tile.value
        }
        return word
    }

    /**
     * Returns true if the given [word] exists in the trie.
     */
    fun isValidWord(word: String): Boolean {
        return word.count() >= minWordSize && trie.isFullWord(word)
    }

    /**
     * Returns true if [tile] exists in [tiles].
     */
    fun pathContainsTile(tiles: MutableList<BoggleTile>, tile: BoggleTile): Boolean {
        for (item in tiles) {
            if (tile.equals(item)) {
                return true
            }
        }
        return false
    }

    /**
     * Finds all possible words in the current Boggle board.
     */
    fun findAllWords() {
        var searchStack: MutableList<MutableList<BoggleTile>> = mutableListOf()
        for (tile in board) {
            searchStack.add(mutableListOf(tile))
        }
        while (searchStack.isNotEmpty()) {
            var path = searchStack.last()
            searchStack = searchStack.subList(0, searchStack.lastIndex)
            var word = getWord(path)
            if (isValidWord(word) && !found.containsKey(word)) {
                found[word] = false
            }
            var neighbors = getNeighbors(path.last())
            for (neighbor in neighbors) {
                if (!pathContainsTile(path, neighbor)) {
                    var newPath = path.map{ it.copy() } as MutableList
                    newPath.add(neighbor)
                    if (trie.hasWord(getWord(newPath))) {
                        searchStack.add(newPath)
                    }
                }
            }
        }
    }

    fun findWord(word: String): Boolean {
        return found.containsKey(word)
    }

    /**
     * Returns true if the given [word] is valid and has not yet been found.
     */
    fun tryWord(word: String): SearchResponse {
        // TODO: Refactor this to return an enum with success, fail on faulty word, fail on already found
        if (found.containsKey(word)) {
            if (found[word] == true) {
                return SearchResponse.ALREADY_FOUND_WORD
            } else {
                found[word] = true
                return SearchResponse.FOUND_NEW_WORD
            }
        } else {
            return SearchResponse.WORD_DOES_NOT_EXIST
        }
    }

    /**
     * Returns the point value for the given [word].
     */
    fun getPoints(word: String): Int {
        if (word.length < 3) {
            return 0
        } else {
            return scoring[min(word.length, 8) - 3]
        }
    }
}
