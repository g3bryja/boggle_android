package com.example.boggleandroid.boggle

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.boggleandroid.data.BoardMetadata
import com.example.boggleandroid.data.BoggleTile

@Composable
fun BoggleScreen(metadata: MutableList<BoardMetadata>, boggle: BoggleViewModel = BoggleViewModel()) {
    // TODO: This is not storing state properly, may need to add mutableState saver
    val state by boggle.state.collectAsState()

    Column {
        Row {
            if (state.board.isNotEmpty()) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Top,
                    modifier = Modifier
                        .fillMaxSize()
                ) {
                    BoggleBoard(
                        state.board,
                        onTileClicked = { tile ->
                            if (boggle.tileSelected(tile)) {
                                boggle.removeTile()
                            } else {
                                boggle.addTile(tile)
                            }
                        }
                    )
                }
            }
        }
        Row {

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top,
                modifier = Modifier
                    .fillMaxSize()
            ) {
                ShuffleButton(
                    onButtonClicked = {
                        boggle.loadMetadata(metadata)
                        boggle.reset()
                    }
                )
            }
        }
    }
}

@Composable
fun BoggleBoard(
    tiles: MutableList<BoggleTile>,
    onTileClicked: (BoggleTile) -> Unit
) {
    LazyVerticalGrid(
        GridCells.Fixed(4),
        modifier = Modifier
            .padding(32.dp)
    ) {
        items(tiles) { tile ->
            BoggleTile(tile, onTileClicked)
        }
    }
}

@Preview
@Composable
fun PreviewBoggleBoard() {
    val tiles = mockBoggleTiles()
    tiles[14].value = "Qu"
    BoggleBoard(tiles, {})
}

@Composable
fun BoggleTile(
    tile: BoggleTile,
    onTileClicked: (BoggleTile) -> Unit
) {
    Button(
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
            .padding(4.dp)
            .aspectRatio(1f),
        onClick = {
            onTileClicked(tile)
        }
    ) {
        Text(
            text = tile.value,
            style = MaterialTheme.typography.displayMedium,
            modifier = Modifier
                .requiredWidth(IntrinsicSize.Max)
        )
    }
}

@Composable
fun ShuffleButton(onButtonClicked: () -> Unit
) {
    Button(
        onClick = {
            onButtonClicked()
        }
    ) {
        Text(
            text = "RE-ROLL",
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.headlineMedium
        )
    }
}

fun mockBoggleTiles(seed: String = "ABCDEFGHIJKLMNOP"): MutableList<BoggleTile> {
    val tiles = mutableListOf<BoggleTile>()
    val letters = seed.split("").filterNot { value -> value == "" }.toMutableList()
    for (letter in letters) {
        tiles.add(BoggleTile(letter))
    }
    return tiles
}
