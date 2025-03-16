package com.example.boggleandroid.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
class TrieNode(
    var value: Char?,
    var end: Boolean = false,
    private var next: Array<TrieNode?> = arrayOfNulls(26)
) : Parcelable {
    /**
     * Returns the index of the provided Char, mapping A:Z to 0:25.
     */
    private fun getIndex(value: Char): Int {
        // TODO: Handle special characters for Super Big Boggle
        return value.code - 'A'.code
    }

    /**
     * Inserts a Trie node with the given [value] as a child, if it does not already exist.
     */
    fun addNext(value: Char): TrieNode {
        var index = getIndex(value)
        if (next[index] == null) {
            next[index] = TrieNode(value)
        }
        return next[index]!!
    }

    /**
     * Returns the child node with the given [value], or null if it does not exist.
     */
    fun getNext(value: Char): TrieNode? {
        return next[getIndex(value)]
    }

    /**
     * Returns true if a Trie node with the given [value] exists as a child.
     */
    fun hasNext(value: Char): Boolean {
        return getNext(value) != null
    }
}
