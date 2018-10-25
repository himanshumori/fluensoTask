package com.fluencsotest.speechhandler

import android.media.MediaPlayer
import java.io.IOException

class PlayerUtility private constructor() {

    private lateinit var completeListener: MediaPlayer.OnCompletionListener
    private lateinit var errorListener: MediaPlayer.OnErrorListener
    private var mediaPlayer: MediaPlayer

    init {
        mediaPlayer = MediaPlayer()
    }

    fun setMediaListeners(completeListener: MediaPlayer.OnCompletionListener, errorListener: MediaPlayer.OnErrorListener) {

        this.completeListener = completeListener
        this.errorListener = errorListener
    }

    companion object {

        private var playerUtility = PlayerUtility()

        // player state
        val STOPPED = 1
        val PLAYING = 2

        @Synchronized
        fun getInstance(): PlayerUtility {

            return playerUtility
        }
    }

    fun startPlaying(fileName: String) {

        try {
            mediaPlayer = MediaPlayer()
            mediaPlayer.setOnCompletionListener(completeListener)
            mediaPlayer.setOnErrorListener(errorListener)
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
            mediaPlayer.reset();
            mediaPlayer.release();
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }


}