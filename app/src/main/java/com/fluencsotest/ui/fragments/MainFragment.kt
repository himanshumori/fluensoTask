package com.fluencsotest.ui.fragments

import android.media.MediaPlayer
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.fluencsotest.R
import com.fluencsotest.Util
import com.fluencsotest.speechhandler.PlayerUtility
import com.fluencsotest.speechhandler.RecorderUtility
import kotlinx.android.synthetic.main.fragment_main.*

class MainFragment : Fragment(), View.OnClickListener, MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener {

    private var CURRENT_MODE: Int = Util.IDLE_MODE // set it default
    private var audioFilePath = Util.generateRandomWAVFileName()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater?.inflate(R.layout.fragment_main, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initUI()
        prepareListeners()
    }

    private fun initUI() {
        controller_button.setOnClickListener(this)
        setUIMode()
    }

    private fun prepareListeners() {

        PlayerUtility.getInstance().setMediaListeners(this, this)
    }

    private fun setUIMode() {

        var buttonTextId: Int = R.string.record // default

        when (CURRENT_MODE) {

            Util.IDLE_MODE -> {
                buttonTextId = R.string.record
                progressBar.visibility = View.GONE
            }

            Util.RECORDING_MODE -> {
                progressBar.visibility = View.VISIBLE
                buttonTextId = R.string.stop
            }

            Util.RECORDED_MODE -> {
                progressBar.visibility = View.GONE
                buttonTextId = R.string.play
            }

            Util.PLAYING_MODE -> {
                buttonTextId = R.string.finish;
                progressBar.visibility = View.VISIBLE
            }
        }

        controller_button.setText(buttonTextId)
    }

    override fun onClick(v: View) {

        when (v.id) {

            R.id.controller_button -> {

                if (Util.checkPermission(activity!!.applicationContext)) {

                    when (CURRENT_MODE) {

                        Util.IDLE_MODE -> startRecording()

                        Util.RECORDING_MODE -> stopRecording()

                        Util.RECORDED_MODE -> playRecordedFile()

                        Util.PLAYING_MODE -> stopPlaying()
                    }

                    setUIMode()

                } else {

                    Toast.makeText(context, R.string.permission_denied, Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    private fun stopPlaying() {

        CURRENT_MODE = Util.IDLE_MODE
        PlayerUtility.getInstance().stopPlaying()
    }

    private fun playRecordedFile() {
        CURRENT_MODE = Util.PLAYING_MODE
        PlayerUtility.getInstance().startPlaying(audioFilePath)
    }

    private fun stopRecording() {
        CURRENT_MODE = Util.RECORDED_MODE
        RecorderUtility.getInstance().stopRecording()
    }

    private fun startRecording() {
        CURRENT_MODE = Util.RECORDING_MODE
        RecorderUtility.getInstance().startRecording(audioFilePath)
    }

    override fun onCompletion(mp: MediaPlayer?) {
        CURRENT_MODE = Util.IDLE_MODE
        setUIMode()
        Util.deleteFile(audioFilePath)
    }

    override fun onError(mp: MediaPlayer?, what: Int, extra: Int): Boolean {
        CURRENT_MODE = Util.IDLE_MODE
        setUIMode()
        return true
        Util.deleteFile(audioFilePath)
    }
}