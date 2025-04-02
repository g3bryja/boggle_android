package com.example.boggleandroid

import android.content.Context
import android.os.Bundle
import android.os.CountDownTimer
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.boggleandroid.data.BoardMetadata
import com.example.boggleandroid.data.BoardState
import com.example.boggleandroid.data.Boggle
import com.example.boggleandroid.data.BoggleTile
import com.example.boggleandroid.data.PlayerState
import com.example.boggleandroid.data.Trie
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.concurrent.TimeUnit
import kotlin.random.Random

@SuppressWarnings
class MainActivity : ComponentActivity() {
    private lateinit var boardMetadata: List<BoardMetadata>
    private lateinit var boggle: Boggle
    private var trie = Trie()

    private val PADDING_SMALL = 20.dp

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val dictionaryString = this.assets.open("StringDictionary.txt").bufferedReader().use {
            it.readLines()
        }
        trie = Trie()
        for (word in dictionaryString) {
            trie.addWord(word.uppercase())
        }
        boggle = Boggle(trie = trie)
        boggle.initialize()

        enableEdgeToEdge()
        setContent {
            boardMetadata = loadBoardMetadata(this, "BoardMetadata.json")
            // TODO: Change from hardcoded board preset
            val board = createBoardFromMetadata(boardMetadata[1])
            boggle.resetBoard(board)
            drawBoggleScreen(boggle)
        }
    }

    val boggleSaver = listSaver<Boggle, Any>(
        save = {
            listOf(
                it.trie,
                it.boardState,
                it.playerState
            )
        },
        restore = {
            Boggle(
                trie = it[0] as Trie,
                boardState = it[1] as BoardState,
                playerState = it[2] as PlayerState
            )
        }
    )

    @Composable
    fun drawBoggleScreen(boggle: Boggle) {
        var boggle by rememberSaveable(stateSaver = boggleSaver) { mutableStateOf(boggle) }
        var currentWord by rememberSaveable { mutableStateOf(boggle.playerState.word) }
        var currentScore by remember { mutableStateOf(boggle.playerState.score) }

        Column {
            // FILLER
            Row(
                modifier = Modifier
                    .background(color = MaterialTheme.colorScheme.primary)
                    .height(PADDING_SMALL)
            ) {
                Spacer(
                    modifier = Modifier
                        .fillMaxWidth()
                )
            }
            // NAVIGATION
            Row(
                modifier = Modifier
                    .background(color = MaterialTheme.colorScheme.primary)
                    .fillMaxWidth()
            ) {
                Icon(
                    Icons.Default.Menu,
                    contentDescription = "Navigation Menu",
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier
                        .padding(PADDING_SMALL)
                )
            }
            // HEADER
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                BoggleTimer(3 * 60 * 1000)
                Text(
                    text = currentScore.toString(),
                    style = MaterialTheme.typography.displaySmall,
                    modifier = Modifier
                        .padding(PADDING_SMALL)
                )
            }
            // BODY
            Row {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Top,
                    modifier = Modifier
                        .fillMaxSize()
                ) {
                    drawBoard(
                        boggle
                    )
                    Text(
                        text = currentWord,
                        style = MaterialTheme.typography.displaySmall
                    )
                    Spacer(
                        modifier = Modifier
                            .height(PADDING_SMALL)
                    )
                    drawSubmitButton(
                        onSubmitWord = {
                            val word = boggle.getWord()
                            if (boggle.findWord(word)) {
                                val result = boggle.tryWord(word)
                                if (result == Boggle.SearchResponse.FOUND_NEW_WORD) {
                                    boggle.playerState.score = boggle.playerState.score + boggle.boardState.getPoints(word)
                                }
                            }
                            boggle.playerState.resetWord()
                        }
                    )
                    Spacer(
                        modifier = Modifier
                            .height(PADDING_SMALL)
                    )
                    drawShuffleButton(
                        onShuffle = { value ->
                            boggle.resetBoard(value)
                        }
                    )
                }
            }
        }
    }

    @Preview
    @Composable
    fun previewBoggleScreen() {
//        val boggle = Boggle(Trie())
//        boggle.board = previewBoggleTiles()
//        drawBoggleScreen(boggle)
    }

    @Composable
    fun BoggleTimer(time: Long) {
        var text by rememberSaveable { mutableStateOf("") }
        val countDownTimer = object : CountDownTimer(time, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val seconds = ((millisUntilFinished / 1000.0) % 60.0).toInt()
                val minutes = (TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished) % 60).toInt()
                text = "${minutes.toString().padStart(1, '0')}:${seconds.toString().padStart(2, '0')}"
            }

            override fun onFinish() {

            }
        }

        LaunchedEffect(key1 = null) {
            countDownTimer.cancel()
            countDownTimer.start()
        }

//        DisposableEffect(key1 = "key") {
//            countDownTimer.start()
//            onDispose {
//                countDownTimer.cancel()
//            }
//        }

        Text(
            text = text,
            style = MaterialTheme.typography.displaySmall,
            modifier = Modifier
                .padding(PADDING_SMALL)
        )
    }

    @Composable
    fun drawBoard(boggle: Boggle) {
        LazyVerticalGrid(
            GridCells.Fixed(4),
            modifier = Modifier
                .padding(32.dp)
        ) {
            items(boggle.boardState.tiles) { tile ->
                drawTile(tile)
            }
        }
    }

    @Preview
    @Composable
    fun previewBoard() {
        var tiles = previewBoggleTiles()
        tiles[10].value = "QU"
//        val boggle = Boggle(Trie())
//        boggle.board = tiles
//        drawBoard(boggle, mutableListOf(), {})
    }

    @Composable
    fun getBorderStroke(selected: Boolean): BorderStroke {
        return if (selected) {
            return BorderStroke(
                10.dp, MaterialTheme.colorScheme.onPrimary
                    .copy(alpha = 0.5f)
                    .compositeOver(MaterialTheme.colorScheme.primary)
            )
        } else {
            BorderStroke(
                5.dp, MaterialTheme.colorScheme.scrim
                    .copy(alpha = 0.12f)
                    .compositeOver(MaterialTheme.colorScheme.primary)
            )
        }
    }

    @Composable
    fun drawTile(tile: BoggleTile) {
        var selected by rememberSaveable { mutableStateOf(false) }
        selected = boggle.tileInPath(tile)

        Button(
            shape = RoundedCornerShape(8.dp),
            border = getBorderStroke(selected),
            modifier = Modifier
                .padding(4.dp)
                .aspectRatio(1f),
            onClick = {
                if (!selected) {
//                    boggle.playerState.a(tile)
                } else if (tile == boggle.playerState.path.last()) {
//                    path.removeAt(path.lastIndex)
                }
                boggle.playerState.updateWord(tile)
            },
        ) {
            Text(
                text = tile.value,
                style = MaterialTheme.typography.displayMedium,
                modifier = Modifier
                    .requiredWidth(IntrinsicSize.Max)
            )
        }
    }

    @Preview
    @Composable
    fun previewTile() {
        drawTile(BoggleTile("A"))
    }

    @Composable
    fun drawShuffleButton(onShuffle: (MutableList<String>) -> Unit) {
        Button(
            onClick = {
                onShuffle(createBoardFromMetadata(boardMetadata[1]))
            }
        ) {
            getButtonText("RE-ROLL")
        }
    }

    @Preview
    @Composable
    fun previewShuffleButton() {
        drawShuffleButton({})
    }

    @Composable
    fun drawSubmitButton(onSubmitWord: () -> Unit) {
        Button(
            onClick = {
                onSubmitWord()
            }
        ) {
            getButtonText("SUBMIT")
        }
    }

    @Preview
    @Composable
    fun previewSubmitButton() {
        drawSubmitButton({})
    }

    @Composable
    fun getButtonText(text: String): Unit {
        return Text(
            text = text,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.headlineMedium
        )
    }

    fun readJsonFromAssets(context: Context, filename: String): String {
        return context.assets.open(filename).bufferedReader().use {
            it.readText()
        }
    }

    fun parseJsonToModel(json: String): List<BoardMetadata> {
        val gson = Gson()
        return gson.fromJson(json, object: TypeToken<List<BoardMetadata>>() {}.type)
    }

    fun loadBoardMetadata(context: Context, filename: String): List<BoardMetadata> {
        val json = readJsonFromAssets(context, filename)
        return parseJsonToModel(json)
    }

    fun createBoardFromMetadata(metadata: BoardMetadata): MutableList<String> {
        val count = metadata.size * metadata.size
        val board = mutableListOf<String>()
        metadata.dice.shuffle()
        var i = 0
        for (die in metadata.dice) {
            val seed = List(count) { Random.nextInt(0, 6) }
            var letter = boggle.boardState.getCharacter(die[seed[i]].toString())
            board.add(letter)
        }
        return board
    }

    /**
     * Helper to return letters for previewing UI elements.
     */
    fun previewBoggleTiles(seed: String = "ABCDEFGHIJKLMNOP"): MutableList<BoggleTile> {
        val tiles = mutableListOf<BoggleTile>()
        val letters = seed.split("").filterNot { value -> value == "" }.toMutableList()
        for (letter in letters) {
            tiles.add(BoggleTile(letter))
        }
        return tiles
    }
}


