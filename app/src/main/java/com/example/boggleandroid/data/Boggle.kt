package com.example.boggleandroid.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlin.math.max
import kotlin.math.min

@Parcelize
data class Boggle(
    var trie: Trie = Trie(),
    var boardState: BoardState = BoardState(),
    var playerState: PlayerState = PlayerState()
) : Parcelable {
    enum class SearchResponse {
        FOUND_NEW_WORD,
        ALREADY_FOUND_WORD,
        WORD_DOES_NOT_EXIST
    }

    /**
     * Initializes the board state based on the given game [mode].
     */
    fun initialize(mode: BoardState.Mode = BoardState.Mode.NORMAL) {
        boardState.initialize(mode)
    }

    /**
     * Reinitializes the Boggle board from the given list of [letters] and resets the map of found words.
     */
    fun resetBoard(letters: MutableList<String>) {
        boardState.resetTiles(letters)
        playerState.reset()
        findAllWords()
    }

    /**
     * Returns the index for the tile with the given ([x], [y]) coordinates.
     */
    private fun getIndex(x: Int, y: Int): Int {
        return x + y * boardState.boardSize
    }

    /**
     * Returns the tile with the given ([x], [y]) coordinates.
     */
    fun getTileAt(x: Int, y: Int): BoggleTile {
        return boardState.tiles[getIndex(x, y)]
    }

    /**
     * Returns all tiles that border the specified [tile].
     */
    fun getNeighbors(tile: BoggleTile): MutableList<BoggleTile> {
        val minBound = 0
        val maxBound = boardState.boardSize - 1
        var neighbors = mutableListOf<BoggleTile>()
        var xMin = max(tile.x - 1, minBound)
        var xMax = min(tile.x + 1, maxBound) + 1
        var yMin = max(tile.y - 1, minBound)
        var yMax = min(tile.y + 1, maxBound) + 1

        for (y in yMin until yMax) {
            for (x in xMin until xMax) {
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
    fun getWord(tiles: MutableList<BoggleTile> = playerState.path): String {
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
        return word.count() >= boardState.minWordSize && trie.isFullWord(word)
    }

    /**
     * Returns true if [tile] exists in [tiles].
     */
    fun tileInPath(tile: BoggleTile, tiles: MutableList<BoggleTile> = playerState.path): Boolean {
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
        for (tile in boardState.tiles) {
            searchStack.add(mutableListOf(tile))
        }
        while (searchStack.isNotEmpty()) {
            var path = searchStack.last()
            searchStack = searchStack.subList(0, searchStack.lastIndex)
            var word = getWord(path)
            if (isValidWord(word) && !playerState.hasWord(word)) {
                playerState.addFound(word)
            }
            var neighbors = getNeighbors(path.last())
            for (neighbor in neighbors) {
                if (!tileInPath(neighbor, path)) {
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
        return playerState.hasWord(word)
    }

    /**
     * Returns true if the given [word] is valid and has not yet been found.
     */
    fun tryWord(word: String): SearchResponse {
        if (playerState.hasWord(word)) {
            if (playerState.hasFound(word)) {
                return SearchResponse.ALREADY_FOUND_WORD
            } else {
                playerState.markAsFound(word)
                return SearchResponse.FOUND_NEW_WORD
            }
        } else {
            return SearchResponse.WORD_DOES_NOT_EXIST
        }
    }
}
