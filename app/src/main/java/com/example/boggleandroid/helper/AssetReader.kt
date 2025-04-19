package com.example.boggleandroid.helper

import android.content.Context
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type

class AssetReader {
    fun getLines(context: Context, filepath: String): List<String> {
        return context.assets.open(filepath).bufferedReader().use {
            it.readLines()
        }
    }

    fun <T> getJson(context: Context, filepath: String, typeToken: Type): T{
        val json = context.assets.open(filepath).bufferedReader().use {
            it.readText()
        }
        var gson = GsonBuilder().create()
        return gson.fromJson(json, typeToken)
//        return gson.fromJson(json, object: TypeToken<List<T>>() {}.type)
    }
}
