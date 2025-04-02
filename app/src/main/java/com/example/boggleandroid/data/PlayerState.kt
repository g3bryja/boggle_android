package com.example.boggleandroid.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class PlayerState(
    var path: MutableList<BoggleTile> = mutableListOf(),
    var word: String = "",
    var found: MutableMap<String, Boolean> = mutableMapOf(),
    var score: Int = 0
) : Parcelable {
    fun reset() {
        resetWord()
        resetFound()
        score = 0
    }

    /**
     * Adds or removes the given [tile] from the current [path] and [word].
     */
    fun updateWord(tile: BoggleTile) {
        if (path.isNotEmpty() && path.last() == tile) {
            removeLetter(tile)
        } else {
            addLetter(tile)
        }
    }

    /**
     * Adds the given [tile] from the current [path] and [word].
     */
    private fun addLetter(tile: BoggleTile) {
        path.add(tile)
//        word += tile.value
        word = word + tile.value
    }

    /**
     * Removes the given [tile] from the current [path] and [word].
     */
    private fun removeLetter(tile: BoggleTile) {
        path = path.subList(0, path.lastIndex)
        word = word.substring(0, word.lastIndex)
    }

    /**
     * Resets the current [path] and [word].
     */
    fun resetWord() {
        path.clear()
        word = ""
    }

    /**
     * Returns true if the given [word] exists in the [found] map.
     */
    fun hasWord(word: String): Boolean {
        return found.containsKey(word)
    }

    /**
     * Adds the given [word] to the [found] map.
     */
    fun addFound(word: String) {
        found[word] = false
    }

    /**
     * Returns true if the given [word] has been found.
     */
    fun hasFound(word: String): Boolean {
        return found[word] == true
    }

    /**
     * Updates the value of [found] at the given [word] as true.
     */
    fun markAsFound(word: String) {
        found[word] = true

    }

    /**
     * Clears all elements from [found].
     */
    fun resetFound() {
        found.clear()
    }
}
