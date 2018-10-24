package com.fluencsotest.ui

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
import kotlinx.android.synthetic.main.main_fragment.*

class MainFragment : Fragment(), View.OnClickListener, MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener {

    private var CURRENT_MODE: Int = Util.IDLE_MODE // set it default
    private val recorderUtility: RecorderUtility = RecorderUtility.getInstance()
    private val playerUtility: PlayerUtility = PlayerUtility.getInstance()
    private lateinit var audioFilePath: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater?.inflate(R.layout.main_fragment, container, false)
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

        playerUtility.setMediaListeners(this, this)
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
        playerUtility.stopPlaying()
    }

    private fun playRecordedFile() {
        CURRENT_MODE = Util.PLAYING_MODE
        playerUtility.startPlaying(audioFilePath)
    }

    private fun stopRecording() {
        CURRENT_MODE = Util.RECORDED_MODE
        recorderUtility.stopRecording()
    }

    private fun startRecording() {
        audioFilePath = Util.generateRandomFileName()
        CURRENT_MODE = Util.RECORDING_MODE
        recorderUtility.startRecording(audioFilePath)
    }

    override fun onCompletion(mp: MediaPlayer?) {
        CURRENT_MODE = Util.IDLE_MODE
        setUIMode()
    }

    override fun onError(mp: MediaPlayer?, what: Int, extra: Int): Boolean {
        CURRENT_MODE = Util.IDLE_MODE
        setUIMode()
        return true
    }
}