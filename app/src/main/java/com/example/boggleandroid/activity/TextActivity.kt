package com.example.boggleandroid.activity

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.boggleandroid.data.BoardMetadata
import com.example.boggleandroid.service.FileManagerService
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlin.random.Random

class TextActivity : ComponentActivity() {
    private lateinit var fileManagerService: FileManagerService
    private var bound: Boolean = false
    private var jstr: String = ""
    private var printstr: String = ""
    private var printlist: ArrayList<String> = ArrayList()

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as FileManagerService.LocalBinder
            fileManagerService = binder.getService()
            bound = true
//            setContent {
//                Text(fileManagerService.getText())
//            }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            bound = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        jstr = readJsonFromAssets(this, "BoardMetadata.json")
        val rand = List(10) { Random.nextInt(0, 6)}
        val list = parseJsonToModel(jstr)
        createBoard(list[1])

        setContent {
//            MessageCard(printstr)
//            Text(printstr)
            GridTest(printlist!!)

        }
    }

    override fun onStart() {
        super.onStart()
        Intent(this, FileManagerService::class.java).also { intent ->
            bindService(intent, connection, Context.BIND_AUTO_CREATE)
        }
    }

    override fun onStop() {
        super.onStop()
        unbindService(connection)
        bound = false
    }

    @Composable
    fun MessageCard(str: String) {
        Column(modifier = Modifier.padding(all = 2.dp)) {
            Spacer(modifier = Modifier )
            Text(
                text = str,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }

    @Composable
    fun GridTest(items: ArrayList<String>) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            LazyVerticalGrid(
                GridCells.Fixed(4),
                modifier = Modifier
                    .padding(32.dp)
            ) {
                items(items) { item ->
                    Button(
                        onClick = {
                            handleClick()
                        }
                    ) {
                        Text(
                            text = item,
                            style = MaterialTheme.typography.displayLarge,
                            modifier = Modifier
                                .fillMaxHeight()
                                .requiredHeight(IntrinsicSize.Min)
                                .padding(0.dp)
                        )
                    }
                }
            }
        }

    }

    @Preview
    @Composable
    fun GridPreview() {
        val bullshit: ArrayList<String> = ArrayList()
        val shit = "ABCDEFGHIJKLMNOP"
        for (letter in shit) {
            bullshit.add(letter.toString())
        }
        GridTest(bullshit)
    }

    fun handleClick() {

    }

    fun readJsonFromAssets(context: Context, fileName: String): String {
        return context.assets.open(fileName).bufferedReader().use { it.readText() }
    }

    fun parseJsonToModel(jsonString: String): List<BoardMetadata> {
        val gson = Gson()
        return gson.fromJson(jsonString, object : TypeToken<List<BoardMetadata>>() {}.type)
    }

    fun createBoard(metadata: BoardMetadata) {
        val count = metadata.size * metadata.size
        val seed = List(count) { Random.nextInt(0, 6)}
        var letters: String = ""
        var substr = ""
        var iter: Int = 0
        metadata.dice.shuffle()
        for (die in metadata.dice) {
            val letter = die[seed[iter]]
            letters += letter
            printstr += letter
            substr += letter
            printlist.add(letter.toString())
            iter++
            if (iter % 4 == 0 && iter > 0) {
//                printstr +=  "\n"
//                printlist?.add(substr)
//                substr = ""
            }
        }
    }
}
