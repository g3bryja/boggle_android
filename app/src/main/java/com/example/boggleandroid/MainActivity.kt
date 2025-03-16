package com.example.boggleandroid

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.example.boggleandroid.data.TrieNode
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlin.math.min
import kotlin.random.Random

class MainActivity : ComponentActivity() {
    private lateinit var boardMetadata: List<BoardMetadata>
    private var dictionaryTrie = Trie()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val dictionaryString = this.assets.open("StringDictionary.txt").bufferedReader().use {
            it.readLines()
        }
        dictionaryTrie = Trie()
        for (word in dictionaryString) {
            dictionaryTrie.addWord(word.uppercase())
        }

        enableEdgeToEdge()
        setContent {
            boardMetadata = loadBoardMetadata(this, "BoardMetadata.json")
            // TODO: Change from hardcoded board preset
            val board = createBoardFromMetadata(boardMetadata[1])
            drawBoggleScreen(board)
        }
    }

    fun getPoints(word: String): Int {
        if (word.length < 3) {
            return 0
        } else {
            var scoring = mutableListOf(1, 1, 2, 3, 5, 11)
            return scoring[min(word.length, 8) - 3]
        }
    }

    val trieSaver = listSaver<Trie, Any>(
        save = { listOf(it.root, it.temp) },
        restore = { Trie(root = it[0] as TrieNode, it[1] as String) }
    )

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
    fun drawBoggleScreen(board: MutableList<Char>) {
        var letters by rememberSaveable { mutableStateOf(board) }
        var word by rememberSaveable { mutableStateOf("") }
        var score by rememberSaveable { mutableIntStateOf(0) }
        var foundTrie by rememberSaveable(stateSaver = trieSaver) { mutableStateOf(Trie()) }
        var boggle by rememberSaveable(stateSaver = boggleSaver) { mutableStateOf(Boggle(dictionaryTrie)) }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Text(
                text = score.toString(),
                style = MaterialTheme.typography.displaySmall
            )
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
            Spacer(modifier = Modifier.height(25.dp))
            drawSubmitButton(
                word, onSubmitWord = { value ->
                    if (dictionaryTrie.hasWord(word) && !foundTrie.hasWord(word)) {
                        score += getPoints(word)
                        foundTrie.temp = word
                        foundTrie.addWord(word)
                        boggle.tryWord(word)
                    }
                    word = value
                }
            )
            Spacer(modifier = Modifier.height(25.dp))
            drawShuffleButton(
                letters, onUpdateLetters = { value ->
                    letters = value
                    boggle.resetBoard(letters)
                },
                word, onUpdateWord = {
                    word = ""
                    score = 0
                    foundTrie = Trie()
                }
            )
        }
    }

    @Preview
    @Composable
    fun previewBoggleScreen() {
        val letters = "ABCDEFGHIJKLMNOP".toMutableList()
        drawBoggleScreen(letters)
    }

    @Composable
    fun drawBoard(letters: MutableList<Char>, word: String, onUpdateWord: (String) -> Unit) {
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
        val letters = "ABCDEFGHIJKLMNOP".toMutableList()
        drawBoard(letters, "", {})
    }

    @Composable
    fun drawTile(letter: Char, word: String, onUpdateWord: (String) -> Unit) {
        Button(
            onClick = {
                onUpdateWord(word.plus(letter))
            },
        ) {
            Text(
                text = letter.toString(),
                style = MaterialTheme.typography.displayLarge,
                modifier = Modifier
                    .requiredHeight(IntrinsicSize.Min)
                    .width(IntrinsicSize.Min)
                    .padding(0.dp)
            )
        }
    }

    @Preview
    @Composable
    fun previewTile() {
        drawTile('A',"", {})
    }

    @Composable
    fun drawShuffleButton(letters: MutableList<Char>, onUpdateLetters: (MutableList<Char>) -> Unit, word: String, onUpdateWord: () -> Unit) {
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
        drawShuffleButton(mutableListOf<Char>(), {}, "", {})
    }

    @Composable
    fun drawSubmitButton(word: String, onSubmitWord: (String) -> Unit) {
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
        drawSubmitButton("", {})
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

    fun createBoardFromMetadata(metadata: BoardMetadata): MutableList<Char> {
        val count = metadata.size * metadata.size
        val board = mutableListOf<Char>()
        metadata.dice.shuffle()
        var i = 0
        for (die in metadata.dice) {
            val seed = List(count) { Random.nextInt(0, 6) }
            board.add(die[seed[i]])
        }
        return board
    }
}


