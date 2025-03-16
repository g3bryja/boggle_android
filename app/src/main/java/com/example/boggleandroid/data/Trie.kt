package com.example.boggleandroid.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
class Trie(var root: TrieNode = TrieNode(' '), var temp: String = "") : Parcelable {
    /**
     * Inserts [word] into the Trie.
     */
    fun addWord(word: String) {
        var currentNode = root
        for (letter in word) {
            currentNode.addNext(letter)
            if (letter == word.last()) {
                currentNode.end = true
                return
            }
            currentNode = currentNode.getNext(letter)!!
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
        return getLastNode(word)?.end ?: false
    }
}
