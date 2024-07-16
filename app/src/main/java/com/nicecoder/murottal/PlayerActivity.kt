package com.nicecoder.murottal

import android.content.ComponentName

import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ui.PlayerView

class PlayerActivity : AppCompatActivity() {

    private var audioService: AudioService? = null
    private var serviceBound = false

    private lateinit var playerView: PlayerView
    private lateinit var titleTextView: TextView
    private lateinit var playPauseButton: Button
    private lateinit var previousButton: Button
    private lateinit var nextButton: Button
    private lateinit var loopButton: Button
    private var currentPosition: Int = 0
    private var isLooping: Boolean = false
    private lateinit var currentList: List<Audio>

    private val playerListener = object : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            runOnUiThread {
                updatePlayPauseButton()
            }
        }

        override fun onPlaybackStateChanged(state: Int) {
            if (state == Player.STATE_ENDED) {
                audioService?.let { service ->
                    if (service.getCurrentPosition() < currentList.size - 1) {
                        service.next()
                        updateTitle()
                    } else {
                        updatePlayPauseButton()
                    }
                }
            }
        }
    }

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as AudioService.LocalBinder
            audioService = binder.getService()
            serviceBound = true
            initializePlayer()
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            audioService = null
            serviceBound = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player)

        supportActionBar?.title = "Player"

        playerView = findViewById(R.id.playerView)
        titleTextView = findViewById(R.id.titleTextView)
        playPauseButton = findViewById(R.id.playPauseButton)
        previousButton = findViewById(R.id.previousButton)
        nextButton = findViewById(R.id.nextButton)
        loopButton = findViewById(R.id.loopButton)

        currentPosition = intent.getIntExtra("position", 0)
        val isReversed = intent.getBooleanExtra("isReversed", false)
        currentList = if (isReversed) AudioList.list.reversed() else AudioList.list

        val intent = Intent(this, AudioService::class.java)
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
    }

    private fun initializePlayer() {
        audioService?.let { service ->
            playerView.player = service.player
            service.setPlaylist(currentList, currentPosition)
            service.play()
            updateTitle()
            setupButtons()
            setupPlayerListener()
            updatePlayPauseButton() // Update button state initially
        }
    }

    private fun setupPlayerListener() {
        audioService?.player?.addListener(playerListener)
    }

    private fun updateTitle() {
        audioService?.let { service ->
            titleTextView.text = service.getCurrentAudioTitle()
        }
    }

    private fun setupButtons() {
        playPauseButton.setOnClickListener {
            audioService?.let { service ->
                if (service.player?.isPlaying == true) {
                    service.pause()
                } else {
                    service.play()
                }
                updatePlayPauseButton()
            }
        }

        previousButton.setOnClickListener {
            audioService?.previous()
            updateTitle()
        }

        nextButton.setOnClickListener {
            audioService?.next()
            updateTitle()
        }

        loopButton.setOnClickListener {
            isLooping = !isLooping
            audioService?.setLooping(isLooping)
            updateLoopButtonAppearance()
        }
    }

    private fun updateLoopButtonAppearance() {
        loopButton.text = if (isLooping) "Unloop" else "Loop"
    }

    private fun updatePlayPauseButton() {
        audioService?.let { service ->
            val isPlaying = service.player?.isPlaying == true
            playPauseButton.text = if (isPlaying) "Pause" else "Play"
        }
    }

    override fun onStop() {
        super.onStop()
        if (serviceBound) {
            unbindService(serviceConnection)
            serviceBound = false
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        audioService?.player?.removeListener(playerListener)
    }
}