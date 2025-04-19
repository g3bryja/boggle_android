package com.example.boggleandroid.boggle

import com.example.boggleandroid.data.BoardMetadata
import com.example.boggleandroid.data.BoggleTile

data class BoggleState(
    val type: BoardType = BoardType.NORMAL,
    val boardSize: Int = 4,
    val minWordSize: Int = 3,
    val maxScoreWordSize: Int = 8,
    val scoring: MutableList<Int> = mutableListOf(),
    val score: Int = 0,
    val board: MutableList<BoggleTile> = mutableListOf(),
    val path: MutableList<BoggleTile> = mutableListOf(),
    val found: MutableMap<String, Boolean> = mutableMapOf(),
    val metadata: MutableMap<BoardType, BoardMetadata> = mutableMapOf()
)

enum class BoardType {
    NORMAL,
    LEGACY,
    BIG,
    SUPER_BIG
}
