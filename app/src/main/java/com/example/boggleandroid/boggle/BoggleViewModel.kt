package com.example.boggleandroid.boggle

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.boggleandroid.data.BoardMetadata
import com.example.boggleandroid.data.BoggleTile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class BoggleViewModel() : ViewModel() {
    private val _state = MutableStateFlow(BoggleState())
    val state: StateFlow<BoggleState> = _state.asStateFlow()

    var seed by mutableStateOf(mutableListOf<String>())
        private set

    private fun updateSeed(seed: MutableList<String>) {
        this.seed = seed
    }

    fun loadMetadata(metadata: MutableList<BoardMetadata>) {
        val mapping = mutableMapOf<BoardType, BoardMetadata>()
        for (datum in metadata) {
            mapping[enumValueOf(datum.name)] = datum
        }
        _state.update { currentState ->
            currentState.copy(
                metadata = mapping
            )
        }
    }

    private fun createSeedFromMetadata(metadata: BoardMetadata) {
        val seed = mutableListOf<String>()
        metadata.dice.shuffle()
        for (die in metadata.dice) {
            val rand = (0 until 6).random()
            val letter = getCharacter(die[rand].toString())
            seed.add(letter)
        }
        updateSeed(seed)
    }

    fun reset(metadata: BoardMetadata = _state.value.metadata[BoardType.NORMAL]!!) {
        createSeedFromMetadata(metadata)
        resetBoard()
    }

    private fun resetBoard(seed: MutableList<String> = this.seed) {
        val board = mutableListOf<BoggleTile>()
        var i = 0
        for (y in 0 until _state.value.boardSize) {
            for (x in 0 until _state.value.boardSize) {
                val letter = getCharacter(seed[i])
                board.add(BoggleTile(letter, x, y))
                i += 1
            }
        }
        _state.update { currentState ->
            currentState.copy(
                board = board
            )
        }
    }

    private fun getCharacter(letter: String): String {
        var character: String = letter
        if (letter == "Q") {
            character = "Qu"
        }
        return character
    }

    private fun getWord(tiles: MutableList<BoggleTile>): String {
        var word = ""
        for (tile in tiles) {
            word += tile.value
        }
        return word
    }

    fun getCurrentWord(): String {
        return getWord(_state.value.path);
    }

    fun addTile(tile: BoggleTile) {
        _state.update { currentState ->
            currentState.copy(
                path = _state.value.path.plus(tile).toMutableList()
            )
        }
    }

    fun removeTile() {
        _state.update { currentState ->
            currentState.copy(
                path = _state.value.path.subList(0, _state.value.path.lastIndex)
            )
        }
    }

    private fun pathContainsTile(tile: BoggleTile, path: MutableList<BoggleTile> = _state.value.path): Boolean {
        for (step in path) {
            if (tile.equals(step)) {
                return true
            }
        }
        return false
    }

    fun tileSelected(tile: BoggleTile): Boolean {
        return pathContainsTile(tile)
    }
}
