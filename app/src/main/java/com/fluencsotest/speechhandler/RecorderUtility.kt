package com.fluencsotest.speechhandler

import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.AsyncTask
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException

class RecorderUtility private constructor(){

    init {
        prepareRecorder()
    }

    companion object {

        val SAMPLE_RATE = 44100 // The sampling rate
        private lateinit var recorder: AudioRecord
        private var recorderUtility = RecorderUtility()
        private var recordTask: RecordTask? = null

        @Synchronized
        fun getInstance(): RecorderUtility {

            return recorderUtility
        }
    }

    private fun prepareRecorder() {

        var bufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT)

        if (bufferSize == AudioRecord.ERROR || bufferSize == AudioRecord.ERROR_BAD_VALUE) {
            bufferSize = SAMPLE_RATE * 2
        }

        recorder = AudioRecord(MediaRecorder.AudioSource.MIC,
                SAMPLE_RATE,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                bufferSize)

        if (recorder.state != AudioRecord.STATE_INITIALIZED) {
            //Log.e(LOG_TAG, "Audio Record can't initialize!")
        }
    }

    fun startRecording(fileName: String) {

        if (recordTask==null){

            recordTask = RecordTask(fileName)
            recordTask!!.execute()

        }else{

            if (recordTask!!.isTaskFinished()) {

                recordTask = RecordTask(fileName)
                recordTask!!.execute()
            }else{
                // exception
            }
        }
    }

    fun stopRecording() {

        if (!recordTask!!.isTaskFinished()) {
            recordTask!!.setIsNeedToContinue(false)
        }
    }

    private inner class RecordTask(var outputFileName: String) : AsyncTask<String, String, String>() {

        private var isNeedToContinue: Boolean = true;
        private var taskFinished = false

        override fun onPreExecute() {
            super.onPreExecute()
        }

        override fun doInBackground(vararg params: String?): String {
            recorder.startRecording()
            //Log.v(LOG_TAG, "Start recording")
            writeAudioDataToFile(recorder)
            recorder.stop()
            recorder.release()
            prepareRecorder()
            return ""
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            taskFinished = true
        }

        fun setIsNeedToContinue(value: Boolean) {

            this.isNeedToContinue = value
        }

        fun isTaskFinished(): Boolean {

            return taskFinished
        }

        private fun writeAudioDataToFile(recorder: AudioRecord) {

            val bufferSize = AudioRecord.getMinBufferSize(8000,
                    AudioFormat.CHANNEL_CONFIGURATION_MONO,
                    AudioFormat.ENCODING_PCM_16BIT)

            val data = ByteArray(bufferSize)
            var os: FileOutputStream? = null

            try {
                os = FileOutputStream(outputFileName)
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
            }

            var read = 0

            if (null != os) {

                while (isNeedToContinue) {

                    read = recorder.read(data, 0, bufferSize)

                    if (AudioRecord.ERROR_INVALID_OPERATION != read) {
                        try {
                            os!!.write(data)
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }
                    }
                }

                try {
                    os!!.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }

            }
        }
    }
}