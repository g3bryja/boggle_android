package com.example.boggleandroid.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
class Trie(
    var root: TrieNode = TrieNode(' '),
    var temp: String = ""
) : Parcelable {
    /**
     * Inserts [word] into the Trie.
     */
    fun addWord(word: String) {
        var currentNode = root
        var i = 0
        for (letter in word) {
            currentNode = currentNode.addNext(letter)
            if (i == word.length - 1) {
                currentNode.end = true
                return
            }
            i += 1
        }
    }

    /**
     * Returns the final Trie node for the given [word], or null if the word is not found.
     */
    fun getLastNode(word: String): TrieNode? {
        var currentNode = root
        for (letter in word) {
            if (!currentNode.hasNext(letter)) {
                return null
            }
            currentNode = currentNode.getNext(letter)!!
        }
        return currentNode
    }

    /**
     * Returns true if the given [word] exists in the current Trie.
     */
    fun hasWord(word: String): Boolean {
        return getLastNode(word) != null
    }

    /**
     * Returns true if the given [word] exists in the current Trie and the last letter is an ending node.
     */
    fun isFullWord(word: String): Boolean {
        val last = getLastNode(word)
        if (last != null) {
            return last.end
        } else {
            return false
        }
    }
}
