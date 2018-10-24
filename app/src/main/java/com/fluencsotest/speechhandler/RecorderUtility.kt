package com.fluencsotest.speechhandler

import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.AsyncTask
import com.fluencsotest.Util
import java.io.*

class RecorderUtility {

    private constructor() {
        prepareRecorder()
    }

    val SAMPLE_RATE = 44100 // The sampling rate
    private lateinit var recorder: AudioRecord
    private var recordTask: RecordTask? = null
    private val RECORDER_BPP: Byte = 16
    private val AUDIO_RECORDER_TEMP_FILE = Util.generateRandomRAWFileName()
    private val RECORDER_SAMPLERATE: Long = 44100
    private val RECORDER_CHANNELS = AudioFormat.CHANNEL_IN_MONO
    private var bufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE,
            RECORDER_CHANNELS,
            AudioFormat.ENCODING_PCM_16BIT)

    companion object {

        private var recorderUtility = RecorderUtility()

        @Synchronized
        fun getInstance(): RecorderUtility {

            return recorderUtility
        }
    }

    private fun prepareRecorder() {

        recorder = AudioRecord(MediaRecorder.AudioSource.MIC,
                SAMPLE_RATE,
                RECORDER_CHANNELS,
                AudioFormat.ENCODING_PCM_16BIT,
                bufferSize)

        if (recorder.state != AudioRecord.STATE_INITIALIZED) {
            //Log.e(LOG_TAG, "Audio Record can't initialize!")
        }
    }

    fun startRecording(fileName: String) {

        if (recordTask == null) {

            recordTask = RecordTask(fileName)
            recordTask!!.execute()

        } else {

            if (recordTask!!.isTaskFinished()) {

                recordTask = RecordTask(fileName)
                recordTask!!.execute()

            } else {
                // handle this situation later
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

            // clean old data.. as of now not required further
            Util.deleteFile(outputFileName)
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
                    AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT)

            val data = ByteArray(bufferSize)
            var os: FileOutputStream? = null

            try {
                os = FileOutputStream(AUDIO_RECORDER_TEMP_FILE)
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

            finalizeAudioFile(AUDIO_RECORDER_TEMP_FILE, outputFileName)
            Util.deleteFile(AUDIO_RECORDER_TEMP_FILE)
        }
    }

    private fun finalizeAudioFile(inFilename: String, outFilename: String) {
        var `in`: FileInputStream? = null
        var out: FileOutputStream? = null
        var totalAudioLen: Long = 0
        var totalDataLen = totalAudioLen + 36
        val longSampleRate: Long = RECORDER_SAMPLERATE
        val channels = if (RECORDER_CHANNELS === AudioFormat.CHANNEL_IN_MONO)
            1
        else
            2
        val byteRate = RECORDER_BPP * RECORDER_SAMPLERATE * channels / 8

        val data = ByteArray(bufferSize)

        try {
            `in` = FileInputStream(inFilename)
            out = FileOutputStream(outFilename)
            totalAudioLen = `in`!!.getChannel().size()
            totalDataLen = totalAudioLen + 36

            WriteWaveFileHeader(out, totalAudioLen, totalDataLen,
                    longSampleRate, channels, byteRate)

            while (`in`!!.read(data) !== -1) {
                out.write(data)
            }

            `in`!!.close()
            out.close()
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }

    }

    @Throws(IOException::class)
    private fun WriteWaveFileHeader(out: FileOutputStream, totalAudioLen: Long,
                                    totalDataLen: Long, longSampleRate: Long, channels: Int, byteRate: Long) {
        val header = ByteArray(44)

        header[0] = 'R'.toByte() // RIFF/WAVE header
        header[1] = 'I'.toByte()
        header[2] = 'F'.toByte()
        header[3] = 'F'.toByte()
        header[4] = (totalDataLen and 0xff).toByte()
        header[5] = (totalDataLen shr 8 and 0xff).toByte()
        header[6] = (totalDataLen shr 16 and 0xff).toByte()
        header[7] = (totalDataLen shr 24 and 0xff).toByte()
        header[8] = 'W'.toByte()
        header[9] = 'A'.toByte()
        header[10] = 'V'.toByte()
        header[11] = 'E'.toByte()
        header[12] = 'f'.toByte() // 'fmt ' chunk
        header[13] = 'm'.toByte()
        header[14] = 't'.toByte()
        header[15] = ' '.toByte()
        header[16] = 16 // 4 bytes: size of 'fmt ' chunk
        header[17] = 0
        header[18] = 0
        header[19] = 0
        header[20] = 1 // format = 1
        header[21] = 0
        header[22] = channels.toByte()
        header[23] = 0
        header[24] = (longSampleRate and 0xff).toByte()
        header[25] = (longSampleRate shr 8 and 0xff).toByte()
        header[26] = (longSampleRate shr 16 and 0xff).toByte()
        header[27] = (longSampleRate shr 24 and 0xff).toByte()
        header[28] = (byteRate and 0xff).toByte()
        header[29] = (byteRate shr 8 and 0xff).toByte()
        header[30] = (byteRate shr 16 and 0xff).toByte()
        header[31] = (byteRate shr 24 and 0xff).toByte()
        header[32] = ((if (RECORDER_CHANNELS === AudioFormat.CHANNEL_IN_MONO)
            1
        else
            2) * 16 / 8).toByte() // block align
        header[33] = 0
        header[34] = RECORDER_BPP // bits per sample
        header[35] = 0
        header[36] = 'd'.toByte()
        header[37] = 'a'.toByte()
        header[38] = 't'.toByte()
        header[39] = 'a'.toByte()
        header[40] = (totalAudioLen and 0xff).toByte()
        header[41] = (totalAudioLen shr 8 and 0xff).toByte()
        header[42] = (totalAudioLen shr 16 and 0xff).toByte()
        header[43] = (totalAudioLen shr 24 and 0xff).toByte()

        out.write(header, 0, 44)
    }
}