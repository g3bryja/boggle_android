package com.example.boggleandroid

import android.content.Context
import android.os.Bundle
import android.os.CountDownTimer
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.boggleandroid.data.BoardMetadata
import com.example.boggleandroid.data.Boggle
import com.example.boggleandroid.data.BoggleTile
import com.example.boggleandroid.data.Trie
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.DecimalFormat
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlin.random.Random

class MainActivity : ComponentActivity() {
    private lateinit var boardMetadata: List<BoardMetadata>
    private lateinit var boggle: Boggle
    private var trieDictionary = Trie()

    private val PADDING_SMALL = 20.dp

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val dictionaryString = this.assets.open("StringDictionary.txt").bufferedReader().use {
            it.readLines()
        }
        trieDictionary = Trie()
        for (word in dictionaryString) {
            trieDictionary.addWord(word.uppercase())
        }
        boggle = Boggle(trieDictionary)

        enableEdgeToEdge()
        setContent {
            boardMetadata = loadBoardMetadata(this, "BoardMetadata.json")
            // TODO: Change from hardcoded board preset
            val board = createBoardFromMetadata(boardMetadata[1])
            boggle.resetBoard(board)
            drawBoggleScreen(boggle, board)
        }
    }

    val boggleSaver = listSaver<Boggle, Any>(
        save = {
            listOf(
                it.trie,
                it.board,
                it.found,
                it.scoring,
                it.boardSize,
                it.minWordSize
            )
        },
        restore = {
            Boggle(
                trie = it[0] as Trie,
                board = it[1] as MutableList<BoggleTile>,
                found = it[2] as MutableMap<String, Boolean>,
                scoring = it[3] as MutableList<Int>,
                boardSize = it[4] as Int,
                minWordSize = it[5] as Int
            )
        }
    )

    @Composable
    fun drawBoggleScreen(boggle: Boggle, board: MutableList<String>) {
        var letters by rememberSaveable { mutableStateOf(board) }
        var word by rememberSaveable { mutableStateOf("") }
        var score by rememberSaveable { mutableIntStateOf(0) }
        var boggle by rememberSaveable(stateSaver = boggleSaver) { mutableStateOf(boggle) }

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
                    text = score.toString(),
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
                        letters,
                        word, onUpdateWord = { value ->
                            word = value
                        }
                    )
                    Text(
                        text = word,
                        style = MaterialTheme.typography.displaySmall
                    )
                    Spacer(
                        modifier = Modifier
                            .height(PADDING_SMALL)
                    )
                    drawSubmitButton(
                        onSubmitWord = { value ->
                            if (boggle.findWord(word)) {
                                val result = boggle.tryWord(word)
                                if (result == Boggle.SearchResponse.FOUND_NEW_WORD) {
                                    score += boggle.getPoints(word)
                                }
                            }
                            word = value
                        }
                    )
                    Spacer(
                        modifier = Modifier
                            .height(PADDING_SMALL)
                    )
                    drawShuffleButton(
                        letters, onUpdateLetters = { value ->
                            letters = value
                            boggle.resetBoard(letters)
                        },
                        word, onUpdateWord = {
                            word = ""
                            score = 0
                        }
                    )
                }
            }
        }
    }

    @Preview
    @Composable
    fun previewBoggleScreen() {
        drawBoggleScreen(Boggle(Trie()), mockBoggleLetters())
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
                TODO("Not yet implemented")
            }
        }

        DisposableEffect(key1 = "key") {
            countDownTimer.start()
            onDispose {
                countDownTimer.cancel()
            }
        }

        Text(
            text = text,
            style = MaterialTheme.typography.displaySmall,
            modifier = Modifier
                .padding(PADDING_SMALL)
        )
    }

    /**
     * Helper to return letters for previewing UI elements.
     */
    fun mockBoggleLetters(letters: String = "ABCDEFGHIJKLMNOP"): MutableList<String> {
        return letters.split("").filterNot { value -> value == "" }.toMutableList()
    }

    @Composable
    fun drawBoard(letters: MutableList<String>, word: String, onUpdateWord: (String) -> Unit) {
        LazyVerticalGrid(
            GridCells.Fixed(4),
            modifier = Modifier
                .padding(32.dp)
        ) {
            items(letters) { letter ->
                drawTile(letter, word, onUpdateWord)
            }
        }
    }

    @Preview
    @Composable
    fun previewBoard() {
        drawBoard(mockBoggleLetters(), "", {})
    }

    @Composable
    fun drawTile(letter: String, word: String, onUpdateWord: (String) -> Unit) {
        Button(
            onClick = {
                onUpdateWord(word.plus(letter))
            },
        ) {
            Text(
                text = letter,
                style = MaterialTheme.typography.displayMedium,
                modifier = Modifier
                    .requiredHeight(IntrinsicSize.Min)
                    .width(IntrinsicSize.Min)
                    .padding(10.dp)
            )
        }
    }

    @Preview
    @Composable
    fun previewTile() {
        drawTile("A","", {})
    }

    @Preview
    @Composable
    fun previewTileQ() {
        drawTile("Qu", "", {})
    }

    @Composable
    fun drawShuffleButton(letters: MutableList<String>, onUpdateLetters: (MutableList<String>) -> Unit, word: String, onUpdateWord: () -> Unit) {
        Button(
            onClick = {
                onUpdateLetters(createBoardFromMetadata(boardMetadata[1]))
                onUpdateWord()
            }
        ) {
            getButtonText("RE-ROLL")
        }
    }

    @Preview
    @Composable
    fun previewShuffleButton() {
        drawShuffleButton(mutableListOf(), {}, "", {})
    }

    @Composable
    fun drawSubmitButton(onSubmitWord: (String) -> Unit) {
        Button(
            onClick = {
                onSubmitWord("")
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
            var letter = boggle.getCharacter(die[seed[i]].toString())
            board.add(letter)
        }
        return board
    }
}


