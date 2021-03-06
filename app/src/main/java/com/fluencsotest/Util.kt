package com.fluencsotest

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Environment
import android.support.v4.content.ContextCompat
import java.io.File
import android.util.TypedValue
import android.util.DisplayMetrics



class Util {

    companion object {

        const val PERSONA: Int = 21
        const val USER: Int = 22

        const val IDLE_MODE: Int = 31
        const val RECORDING_MODE: Int = 32
        const val RECORDED_MODE: Int = 33
        const val PLAYING_MODE: Int = 34

        val defaultDir: String = Environment.getExternalStorageDirectory().absolutePath + "/fluenso/"

        fun prepareDefaultDirectory() {

            var fileDir = File(defaultDir)

            if (!fileDir.exists()) {

                try {
                    fileDir.mkdir()
                } catch (ex: Exception) {
                }
            }
        }

        fun generateRandomRAWFileName(): String {

            return (defaultDir + "rec_" + System.currentTimeMillis() + ".raw")
        }

        fun generateRandomWAVFileName(): String {

            return (defaultDir + "rec_" + System.currentTimeMillis() + ".wav")
        }

        fun cleanDirectory() {
            //TODO delete files when there is no use of it
        }

        fun checkPermission(applicationContext: Context): Boolean {

            val storageStatus = ContextCompat.checkSelfPermission(applicationContext,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)
            val micStatus = ContextCompat.checkSelfPermission(applicationContext,
                    Manifest.permission.RECORD_AUDIO)
            return storageStatus == PackageManager.PERMISSION_GRANTED && micStatus == PackageManager.PERMISSION_GRANTED
        }

        fun deleteFile(fileName: String) {
            val file = File(fileName)
            if (file.exists()) {
                try {
                    file.delete()
                } catch (ex: Exception) {
                    ex.printStackTrace()
                }
            }
        }

        fun getRatingText( rating: Int): String{

            when (rating){

                0 -> return "\uD83D\uDE4F"
                1 -> return "\uD83D\uDE4F"
                2 -> return "\uD83D\uDC4E"
                3 -> return "\uD83D\uDC4F\uD83D\uDC4F"
                4 -> return "\uD83D\uDC4F\uD83D\uDC4F\uD83D\uDC4F"
                5 -> return "\uD83D\uDC4F\uD83D\uDC4F\uD83D\uDC4F\uD83D\uDC4F"

            }

            return  "uD83D"
        }

        fun dipToPixels(context: Context, dipValue: Float): Float {
            val metrics = context.resources.displayMetrics
            return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dipValue, metrics)
        }
    }


}