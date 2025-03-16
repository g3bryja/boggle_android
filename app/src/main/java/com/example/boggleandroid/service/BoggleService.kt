package com.example.boggleandroid.service

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import com.example.boggleandroid.helper.BoggleBoard

class BoggleService : Service() {
    private val binder = LocalBinder()
    private val boggleBoard = BoggleBoard()

    inner class LocalBinder : Binder() {
        fun getService(): BoggleService = this@BoggleService
    }

    override fun onBind(intent: Intent?): IBinder? {
        return binder
    }
}
