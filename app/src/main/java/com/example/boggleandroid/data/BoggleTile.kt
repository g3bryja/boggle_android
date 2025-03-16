package com.example.boggleandroid.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class BoggleTile(
    var value: String = "",
    var x: Int = 0,
    var y: Int = 0
) : Parcelable
