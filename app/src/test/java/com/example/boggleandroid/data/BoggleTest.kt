package com.example.boggleandroid.data

import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class BoggleTest {
    private lateinit var boggle: Boggle

    @Before
    fun setup() {
        boggle = Boggle()
        boggle.initialize(BoardState.Mode.NORMAL)
        //  A B C D
        //  E F G H
        //  I J K L
        //  M N O P
        boggle.resetBoard(getMockTileInput("ABCDEFGHIJKLMNOP"))
    }

    @Test
    fun testResetBoard() {
        boggle.trie.addWord("QUIZ")
        //  Q I Z Z
        //  Z Z Z Z
        //  Z Z Z Z
        //  Z Z Z Z
        boggle.resetBoard(getMockTileInput("QIZZZZZZZZZZZZZZ"))
        val expectedTile = BoggleTile("QU", 0, 0)
        val expectedFound = mutableMapOf("QUIZ" to false)
        assertEquals(expectedTile, boggle.boardState.tiles[0])
        assertEquals(expectedFound, boggle.playerState.found)
    }

    @Test
    fun testGetNeighborsForCenter() {
        val tile = boggle.boardState.tiles[5]
        val expectedIndices = mutableListOf(0, 1, 2, 4, 6, 8, 9, 10)
        val expected = mutableListOf<BoggleTile>()
        for (i in expectedIndices) {
            expected.add(boggle.boardState.tiles[i])
        }
        val neighbors = boggle.getNeighbors(tile)
        assertEquals(expected, neighbors)
    }

    @Test
    fun testGetNeighborsForTopRight() {
        val tile = boggle.boardState.tiles[3]
        val expectedIndices = mutableListOf(2, 6, 7)
        val expected = mutableListOf<BoggleTile>()
        for (i in expectedIndices) {
            expected.add(boggle.boardState.tiles[i])
        }
        val neighbors = boggle.getNeighbors(tile)
        assertEquals(expected, neighbors)
    }

    @Test
    fun testGetNeighborsForBottomLeft() {
        val tile = boggle.boardState.tiles[12]
        val expectedIndices = mutableListOf(8, 9, 13)
        val expected = mutableListOf<BoggleTile>()
        for (i in expectedIndices) {
            expected.add(boggle.boardState.tiles[i])
        }
        val neighbors = boggle.getNeighbors(tile)
        assertEquals(expected, neighbors)
    }

    @Test
    fun testGetWord() {
        val expected = "ABF"
        var path = mutableListOf<BoggleTile>()
        path.add(boggle.getTileAt(0, 0))
        path.add(boggle.getTileAt(1, 0))
        path.add(boggle.getTileAt(1, 1))
        val word = boggle.getWord(path)
        assertEquals(expected, word)
    }

    @Test
    fun testFindAllWords() {
        val words = mutableListOf("FAE", "FIN", "MIN")
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
    fun testTryWord() {
        val word = "SOMETHING"
        var response = boggle.tryWord(word)
        assertEquals(Boggle.SearchResponse.WORD_DOES_NOT_EXIST, response)

        boggle.playerState.found[word] = false
        response = boggle.tryWord(word)
        assertEquals(Boggle.SearchResponse.FOUND_NEW_WORD, response)

        response = boggle.tryWord(word)
        assertEquals(Boggle.SearchResponse.ALREADY_FOUND_WORD, response)
    }

    fun getMockTileInput(str: String): MutableList<String> {
        val letters = mutableListOf<String>()
        for (letter in str) {
            letters.add(letter.toString())
        }
        return letters
    }
}
