package com.example.boggleandroid.data

import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class PlayerStateTest {
    lateinit var state: PlayerState

    @Before
    fun setup() {
        state = PlayerState()
        state.addFound("FOO")
    }

    @Test
    fun testUpdateAddsWordToEmptyPath() {
        val tile = BoggleTile(value = "A")
        state.updateWord(tile)
        val expected = mutableListOf(tile)
        assertEquals(expected, state.path)
        assertEquals("A", state.word)
    }

    @Test
    fun testUpdateWord() {
        val expected = mutableListOf<BoggleTile>()
        for (i in 0 until 4) {
            val tile = BoggleTile(value = "A", x = i)
            state.updateWord(tile)
            expected.add(tile)
        }
        assertEquals(expected, state.path)
        assertEquals("AAAA", state.word)
        for (i in 3 downTo 2) {
            val tile = BoggleTile(value = "A", x = i)
            state.updateWord(tile)
            expected.removeAt(i)
        }
        assertEquals(expected, state.path)
        assertEquals("AA", state.word)
    }

    @Test
    fun testResetWord() {
        val expected = mutableListOf<BoggleTile>()
        val tile = BoggleTile(value = "A")
        state.updateWord(tile)
        state.resetWord()
        assertEquals(expected, state.path)
        assertEquals("", state.word)
    }
}
