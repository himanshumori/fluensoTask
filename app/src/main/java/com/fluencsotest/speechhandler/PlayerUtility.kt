package com.fluencsotest.speechhandler

import android.media.MediaPlayer
import java.io.IOException

class PlayerUtility private constructor() {

    init {
        mediaPlayer = MediaPlayer()
    }

    companion object {

        private  var playerUtility = PlayerUtility()
        private lateinit var mediaPlayer: MediaPlayer

        @Synchronized
        fun getInstance(): PlayerUtility {

            return playerUtility
        }
    }

    fun startPlaying(fileName: String) {

        try {
            if (mediaPlayer.isPlaying) {
                mediaPlayer.stop()
            }
            mediaPlayer.setDataSource(fileName)
            mediaPlayer.prepare()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        mediaPlayer.start()
    }

    fun stopPlaying() {

        try {
            if (mediaPlayer.isPlaying) {
                mediaPlayer.stop()
            }
            // todo release properly
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun setMediaListeners(completeListener: MediaPlayer.OnCompletionListener, errorListener: MediaPlayer.OnErrorListener) {

        mediaPlayer.setOnCompletionListener(completeListener)
        mediaPlayer.setOnErrorListener(errorListener)
    }
}