package com.nicecoder.murottal

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.RawResourceDataSource

class PlayerActivity : AppCompatActivity() {

    private lateinit var currentList: List<Audio>

    private lateinit var player: SimpleExoPlayer
    private lateinit var playerView: PlayerView
    private lateinit var titleTextView: TextView
    private lateinit var playPauseButton: Button
    private lateinit var previousButton: Button
    private lateinit var nextButton: Button
    private lateinit var loopButton: Button
    private var currentPosition: Int = 0
    private var isLooping: Boolean = false
    private var isReversed: Boolean = false

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
        loopButton = findViewById(R.id.loopButton)

        currentPosition = intent.getIntExtra("position", 0)
        isReversed = intent.getBooleanExtra("isReversed", false)

        currentList = if (isReversed) AudioList.list.reversed() else AudioList.list

        initializePlayer()
        updateTitle()
        setupButtons()
    }

    private fun initializePlayer() {
        player = SimpleExoPlayer.Builder(this).build()
        playerView.player = player

        val mediaItem = MediaItem.fromUri(RawResourceDataSource.buildRawResourceUri(currentList[currentPosition].resourceId))
        player.setMediaItem(mediaItem)
        player.prepare()
        player.play()
    }

    private fun updateTitle() {
        titleTextView.text = currentList[currentPosition].title
    }

    private fun setupButtons() {
        playPauseButton.setOnClickListener {
            if (player.isPlaying) {
                player.pause()
            } else {
                player.play()
            }

            updatePlayPauseButton()
        }

        previousButton.setOnClickListener {
            if (currentPosition > 0) {
                currentPosition--
                playNewTrack()
            }
        }

        nextButton.setOnClickListener {
            if (currentPosition < currentList.size - 1) {
                currentPosition++
                playNewTrack()
            }
        }

        loopButton.setOnClickListener {
            isLooping = !isLooping
            player.repeatMode = if (isLooping) Player.REPEAT_MODE_ONE else Player.REPEAT_MODE_OFF
            updateLoopButtonAppearance()
        }

        player.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                updatePlayPauseButton()
            }

            override fun onPlaybackStateChanged(state: Int) {
                if (state == Player.STATE_ENDED) {
                    if (currentPosition < currentList.size - 1) {
                        currentPosition++
                        playNewTrack()
                    } else {
                        updatePlayPauseButton()
                    }
                }
            }
        })
    }

    private fun playNewTrack() {
        val mediaItem = MediaItem.fromUri(RawResourceDataSource.buildRawResourceUri(currentList[currentPosition].resourceId))
        player.setMediaItem(mediaItem)
        player.prepare()
        player.play()
        updateTitle()
    }

    private fun updateLoopButtonAppearance() {
        loopButton.text = if (isLooping) "Unloop" else "Loop"
    }

    private fun updatePlayPauseButton() {
        playPauseButton.text = if (player.isPlaying) "Pause" else "Play"
    }

    override fun onDestroy() {
        super.onDestroy()
        player.release()
    }
}