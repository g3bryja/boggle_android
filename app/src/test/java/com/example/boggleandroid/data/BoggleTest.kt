package com.example.boggleandroid.data

import junit.framework.TestCase.assertEquals
import org.junit.Before
import org.junit.Test

class BoggleTest {
    lateinit var boggle: Boggle

    @Before
    fun setup() {
        boggle = Boggle(Trie())
        boggle.boardSize = 3
        boggle.resetBoard(mutableListOf<String>(
            "A", "B", "C",  //  0, 1, 2,
            "E", "F", "G",  //  3, 4, 5,
            "H", "I", "J"   //  6, 7, 8
        ))
    }

    @Test
    fun testResetBoardWithSpecialCharacters() {
        boggle.resetBoard(mutableListOf<String>(
            "Z", "Z", "Z",
            "Z", "Z", "Z",
            "Z", "Q", "I"
        ))
        assertEquals("Z", boggle.board[0].value)
        assertEquals("QU", boggle.board[7].value)
    }

    @Test
    fun testGetNeighborsForCenter() {
        val tile = boggle.board[4]
        val expected = mutableListOf<BoggleTile>()
        for (item in boggle.board) {
            if (!tile.equals(item)) {
                expected.add(item)
            }
        }
        val neighbors = boggle.getNeighbors(tile)
        assertEquals(expected, neighbors)
    }

    @Test
    fun testGetNeighborsForTopRight() {
        val tile = boggle.board[2]
        val expected = mutableListOf<BoggleTile>()
        val expectedIndices = mutableListOf(1, 4, 5)
        for (i in expectedIndices) {
            expected.add(boggle.board[i])
        }
        val neighbors = boggle.getNeighbors(tile)
        assertEquals(expected, neighbors)
    }

    @Test
    fun testGetNeighborsForBottomLeft() {
        val tile = boggle.board[6]
        val expected = mutableListOf<BoggleTile>()
        val expectedIndices = mutableListOf(3, 4, 7)
        for (i in expectedIndices) {
            expected.add(boggle.board[i])
        }
        val neighbors = boggle.getNeighbors(tile)
        assertEquals(expected, neighbors)
    }

    @Test
    fun testGetWord() {
        val expected = "FIG"
        var path = mutableListOf<BoggleTile>()
        path.add(boggle.getTileAt(1, 1))
        path.add(boggle.getTileAt(1, 2))
        path.add(boggle.getTileAt(2, 1))
        val word = boggle.getWord(path)
        assertEquals(expected, word)
    }

    @Test
    fun testFindAllWords() {
        val words = mutableListOf("FAB", "FAE", "FIG", "JIG")
        for (word in words) {
            boggle.trie.addWord(word)
        }
        boggle.findAllWords()
        for (word in words) {
            assert(boggle.findWord(word))
        }
        assert(!boggle.findWord("ABC"))
        assert(!boggle.findWord("FA"))
    }

    @Test
    fun testFindAllWordsWithSpecialCharacters() {
        boggle.resetBoard(mutableListOf<String>(
            "Z", "Z", "Z",
            "Z", "Z", "Z",
            "Z", "Q", "I"
        ))
        boggle.trie.addWord("QUIZ")
        boggle.findAllWords()
        assert(boggle.findWord("QUIZ"))
    }

    @Test
    fun testTryWord() {
        val word = "something"
        var response = boggle.tryWord(word)
        assertEquals(Boggle.SearchResponse.WORD_DOES_NOT_EXIST, response)

        boggle.found[word] = false
        response = boggle.tryWord(word)
        assertEquals(Boggle.SearchResponse.FOUND_NEW_WORD, response)

        response = boggle.tryWord(word)
        assertEquals(Boggle.SearchResponse.ALREADY_FOUND_WORD, response)
    }
}
