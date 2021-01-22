package com.example.raaga

import android.content.Context
import java.io.IOException


    fun getAssetJsonData(context: Context): String? {
        val json: String
        try {
            val inputStream = context.assets.open("Samay.json")
            val size = inputStream.available()
            val buffer = ByteArray(size)
            inputStream.use { it.read(buffer) }
            json = String(buffer)
        } catch (ioException: IOException) {
            ioException.printStackTrace()
            return null
        }
        // print the data
        return json
    }
