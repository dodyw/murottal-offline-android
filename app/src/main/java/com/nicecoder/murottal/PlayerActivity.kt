package com.nicecoder.murottal

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.RawResourceDataSource

class PlayerActivity : AppCompatActivity() {

    private lateinit var player: SimpleExoPlayer
    private lateinit var playerView: PlayerView
    private lateinit var titleTextView: TextView
    private lateinit var playPauseButton: Button
    private lateinit var previousButton: Button
    private lateinit var nextButton: Button
    private var currentPosition: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_player)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        playerView = findViewById(R.id.playerView)
        titleTextView = findViewById(R.id.titleTextView)
        playPauseButton = findViewById(R.id.playPauseButton)
        previousButton = findViewById(R.id.previousButton)
        nextButton = findViewById(R.id.nextButton)

        currentPosition = intent.getIntExtra("position", 0)

        initializePlayer()
        updateTitle()
        setupButtons()
    }

    private fun initializePlayer() {
        player = SimpleExoPlayer.Builder(this).build()
        playerView.player = player

        val mediaItem = MediaItem.fromUri(RawResourceDataSource.buildRawResourceUri(AudioList.list[currentPosition].resourceId))
        player.setMediaItem(mediaItem)
        player.prepare()
        player.play()
    }

    private fun updateTitle() {
        titleTextView.text = AudioList.list[currentPosition].title
    }

    private fun setupButtons() {
        playPauseButton.setOnClickListener {
            if (player.isPlaying) player.pause() else player.play()
        }

        previousButton.setOnClickListener {
            if (currentPosition > 0) {
                currentPosition--
                playNewTrack()
            }
        }

        nextButton.setOnClickListener {
            if (currentPosition < AudioList.list.size - 1) {
                currentPosition++
                playNewTrack()
            }
        }
    }

    private fun playNewTrack() {
        val mediaItem = MediaItem.fromUri(RawResourceDataSource.buildRawResourceUri(AudioList.list[currentPosition].resourceId))
        player.setMediaItem(mediaItem)
        player.prepare()
        player.play()
        updateTitle()
    }

    override fun onDestroy() {
        super.onDestroy()
        player.release()
    }
}