package com.example.boggleandroid.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class BoggleTile(
    var value: String = "",
    var x: Int = 0,
    var y: Int = 0
) : Parcelable {
    /**
     * Returns true if this tile matches the (x, y) coordinates for the given [tile].
     */
    fun equals(tile: BoggleTile): Boolean {
        return this.x == tile.x && this.y == tile.y
    }

    /**
     * Returns true if this tile mathces the given ([x], [y]) coordinates.
     */
    fun equals(x: Int, y: Int): Boolean {
        return this.x == x && this.y == y
    }
}
