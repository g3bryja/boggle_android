package com.example.boggleandroid.data

import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class BoardStateTest {
    private lateinit var state: BoardState

    @Before
    fun setup() {
        state = BoardState()
        state.initialize(BoardState.Mode.NORMAL)
    }

    @Test
    fun testResetTiles() {
        val str = "ABCDEFGHIJKLMNOP"
        val letters = mutableListOf<String>()
        for (letter in str) {
            letters.add(letter.toString())
        }
        state.resetTiles(letters)
        assertEquals(str.count(), state.tiles.count())
        val expected = BoggleTile("P", 3, 3)
        assertEquals(expected, state.tiles[str.count() - 1])
    }

    @Test
    fun testGetCharacter() {
        val a = state.getCharacter("A")
        val q = state.getCharacter("Q")
        assertEquals("A", a)
        assertEquals("QU", q)
    }

    @Test
    fun testGetCharacterForUppercase() {
        val upper = state.getCharacter("Q")
        val lower = state.getCharacter("Q", false)
        assertEquals("QU", upper)
        assertEquals("Qu", lower)
    }

    @Test
    fun testGetPoints() {
        val word = "HELLO"
        val points = state.getPoints(word)
        assertEquals(2, points)
    }

    @Test
    fun testGetPointsForShortWord() {
        val word = "HI"
        val points = state.getPoints(word)
        assertEquals(0, points)
    }

    @Test
    fun testGetPointsForBigBoggle() {
        state.initialize(BoardState.Mode.BIG)
        val word = "HELLO"
        val points = state.getPoints(word)
        assertEquals(2, points)
    }

    @Test
    fun testGetPointsForSuperBigBoggle() {
        state.initialize(BoardState.Mode.SUPER_BIG)
        val word = "INCREDIBLE"
        val points = state.getPoints(word)
        assertEquals(20, points)
    }
}
