package com.fluencsotest.ui

import android.Manifest.permission.RECORD_AUDIO
import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import com.fluencsotest.R
import com.fluencsotest.Util

class MainActivity : AppCompatActivity() {

    private val requestedCode: Int = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (!Util.checkPermission(this)) {
            requestPermission()
        }else{
            Util.prepareDefaultDirectory()
        }
    }

    private fun requestPermission() {
        ActivityCompat.requestPermissions(this@MainActivity, arrayOf(WRITE_EXTERNAL_STORAGE, RECORD_AUDIO), requestedCode)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {

        when (requestCode) {

            requestedCode ->

                if (grantResults.isNotEmpty()) {

                    var storagePermission = grantResults[0] == PackageManager.PERMISSION_GRANTED;

                    var recordPermission = grantResults[1] == PackageManager.PERMISSION_GRANTED;

                    if (storagePermission && recordPermission) {
                        Toast.makeText(this, R.string.permission_granted,
                                Toast.LENGTH_LONG).show();
                        Util.prepareDefaultDirectory()
                    } else {
                        Toast.makeText(this, R.string.permission_denied, Toast.LENGTH_LONG).show();
                    }
                }
        }
    }

}
