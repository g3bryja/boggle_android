package com.example.boggleandroid.service

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import java.io.BufferedReader

class FileManagerService : Service() {
    private val binder = LocalBinder()

    inner class LocalBinder : Binder() {
        fun getService(): FileManagerService = this@FileManagerService
    }

    override fun onBind(intent: Intent?): IBinder? {
        return binder
    }

    fun getText(): String {
        val data: List<String>
        assets.open("data.txt").bufferedReader().use {
            data = it.readLines()
        }
        return data[0]
//        return "FAK"
    }

}