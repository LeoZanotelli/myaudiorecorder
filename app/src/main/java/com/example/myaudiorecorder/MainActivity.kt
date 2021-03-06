package com.example.myaudiorecorder


import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.File
import java.io.IOException

class MainActivity : AppCompatActivity() {
    lateinit var status: TextView
    lateinit var btnPlay: ImageView
    lateinit var btnStop: ImageView
    lateinit var btnRecordingStart: ImageView
    lateinit var btnRecordingStop: Button
    private var permissions: Array<String> = arrayOf(
        android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
        android.Manifest.permission.RECORD_AUDIO
    )
    var pathSave: String = ""
    private lateinit var mediaRecorder: MediaRecorder
    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var nameFile: String
    private var REQUEST_PERMISSION_CODE: Int = 1000
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // create a File object for the parent directory
        val wallpaperDirectory = File("/AudioRec/")
        // have the object build the directory structure, if needed.
        wallpaperDirectory.mkdirs()

        if (!checkPermissionFromDevice()){
            requestPermission()
        }
        setContentView(R.layout.activity_main)
        btnPlay = findViewById(R.id.play)
        btnStop = findViewById(R.id.stop)
        btnRecordingStart = findViewById(R.id.btnRecord)
        btnRecordingStop = findViewById(R.id.btnStopRecord)
        status = findViewById(R.id.textView2)


        nameFile = "audioRec.mp3"
        findViewById<Button>(R.id.btnFileName).setOnClickListener {
            addFileName(it)
        }


        btnRecordingStart.setOnClickListener {
            if (checkPermissionFromDevice()) {
                pathSave = File(
                    Environment.getExternalStorageDirectory(),
                    "/AudioRec/$nameFile"
                ).toString()
                setMediaRecorder()
                try {
                    mediaRecorder.prepare()
                    mediaRecorder.start()

                } catch (e: IOException) {
                    e.printStackTrace()
                }
                btnPlay.isEnabled = false
                btnStop.isEnabled = false
                status.text = "Recording..."
            } else {
                requestPermission()
            }
            btnRecordingStop.setOnClickListener {
                mediaRecorder.stop()
                btnRecordingStop.isEnabled = false
                btnPlay.isEnabled = true
                btnRecordingStop.isEnabled = true
                btnStop.isEnabled = false
                status.text="Stopping Recording "

                val shareIntent: Intent = Intent().apply {
                    action = Intent.ACTION_SEND
                    var uri = Uri.parse(pathSave)
                    putExtra(Intent.EXTRA_STREAM, uri)
                    type = "audio/mp3"
                }
                startActivity(Intent.createChooser(shareIntent, "Share audio to.."))
            }
            btnPlay.setOnClickListener {
                btnStop.isEnabled = true
                btnRecordingStop.isEnabled = false
                btnRecordingStart.isEnabled = false
                mediaPlayer = MediaPlayer()
                try {
                    mediaPlayer.setDataSource(pathSave)
                    mediaPlayer.prepare()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
                mediaPlayer.start()
                status.text = "Playing...."
            }
            btnStop.setOnClickListener {
                btnRecordingStop.isEnabled = false
                btnRecordingStart.isEnabled = true
                btnStop.isEnabled = false
                btnPlay.isEnabled = true
                if (mediaPlayer != null) {
                    mediaPlayer.stop()
                    mediaPlayer.release()
                    status.text="Stop Playing"
                    setMediaRecorder()

                }
            }

        }
    }

    private fun setMediaRecorder() {
        mediaRecorder = MediaRecorder()
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC)
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
        mediaRecorder.setAudioEncoder(MediaRecorder.OutputFormat.AMR_NB)
        mediaRecorder.setOutputFile(pathSave)

    }

    private fun requestPermission() {
        ActivityCompat.requestPermissions(this, permissions, REQUEST_PERMISSION_CODE)
    }

    private fun checkPermissionFromDevice(): Boolean {
        val write_external_storage_result: Int = ContextCompat.checkSelfPermission(
            this,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        val record_audio_result: Int =
            ContextCompat.checkSelfPermission(this, android.Manifest.permission.RECORD_AUDIO)
        return write_external_storage_result == PackageManager.PERMISSION_GRANTED &&
                record_audio_result == PackageManager.PERMISSION_GRANTED
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            REQUEST_PERMISSION_CODE -> if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission Granted", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_LONG).show()
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

    }

//  set the name of the file from the edit text
    private fun addFileName(view: View) {
        val editText = findViewById<EditText>(R.id.fileName)
        nameFile = editText.text.toString() + ".mp3"
        editText.setText("File Name Set")
    }

}


