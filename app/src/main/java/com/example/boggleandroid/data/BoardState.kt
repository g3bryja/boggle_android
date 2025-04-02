package com.example.boggleandroid.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlin.math.min

@Parcelize
data class BoardState(
    var tiles: MutableList<BoggleTile> = mutableListOf(),
    var mode : Mode = Mode.NORMAL,
    var boardSize: Int = 0,
    var minWordSize: Int = 0,
    var maxScoreWordSize: Int = 0,
    var time: Int = 0,
    var scoring: MutableList<Int> = mutableListOf()
) : Parcelable {
    enum class Mode {
        NORMAL,
        BIG,
        SUPER_BIG
    }

    /**
     * Initializes the board state based on the given game [mode].
     */
    fun initialize(mode: Mode) {
        this.mode = mode
        if (mode == Mode.NORMAL) {
            boardSize = 4
            minWordSize = 3
            maxScoreWordSize = 8
            time = 3 * 60
            scoring = mutableListOf(1, 1, 2, 3, 5, 11)
        } else if (mode == Mode.BIG) {
            boardSize = 5
            minWordSize = 4
            maxScoreWordSize = 8
            time = 3 * 60
            scoring = mutableListOf(1, 2, 3, 5, 11)
        } else if (mode == Mode.SUPER_BIG) {
            boardSize = 6
            minWordSize = 4
            maxScoreWordSize = 9
            time = 4 * 60
            scoring = mutableListOf(1, 2, 3, 5, 11)
        }
    }

    /**
     * Resets [tiles] with Boggle Tiles from the given [letters], in order.
     */
    fun resetTiles(letters: MutableList<String>) {
        tiles.clear()
        var i = 0
        for (y in 0 until boardSize) {
            for (x in 0 until boardSize) {
                val letter = getCharacter(letters[i])
                tiles.add(BoggleTile(letter, x, y))
                i += 1
            }
        }
    }

    /**
     * Translates the given [letter] to a special character, if necessary.
     * Return value will be converted to uppercase if [uppercase] is true.
     */
    fun getCharacter(letter: String, uppercase: Boolean = true): String {
        var character: String
        if (letter == "Q") {
            character = "Qu"
        } else {
            character = letter
        }
        return if (uppercase) character.uppercase() else character
    }

    /**
     * Returns the point value for the given [word].
     */
    fun getPoints(word: String): Int {
        if (word.length < minWordSize) {
            return 0
        } else if (mode == Mode.SUPER_BIG && word.length >= maxScoreWordSize) {
            // Super Big Boggle scores words of length 9+ with 2 points per letter
            return word.length * 2
        } else {
            return scoring[min(word.length, maxScoreWordSize) - minWordSize]
        }
    }
}
