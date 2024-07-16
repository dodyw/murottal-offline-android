package com.nicecoder.murottal

import android.app.*
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.upstream.RawResourceDataSource

class AudioService : Service() {

    var player: SimpleExoPlayer? = null
    private val binder = LocalBinder()
    private var currentList: List<Audio> = emptyList()
    private var currentPosition: Int = 0
    private var isPlayingBeforeStop = false

    inner class LocalBinder : Binder() {
        fun getService(): AudioService = this@AudioService
    }

    override fun onCreate() {
        super.onCreate()
        initializePlayer()
    }

    private fun initializePlayer() {
        player = SimpleExoPlayer.Builder(this).build().apply {
            addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(state: Int) {
                    if (state == Player.STATE_ENDED) {
                        if (currentPosition < currentList.size - 1) {
                            next()
                        }
                    }
                }
            })
        }
    }

    override fun onBind(intent: Intent): IBinder {
        return binder
    }

    fun setPlaylist(list: List<Audio>, position: Int) {
        currentList = list
        currentPosition = position
        player?.let { exoPlayer ->
            val mediaItem = MediaItem.fromUri(RawResourceDataSource.buildRawResourceUri(currentList[currentPosition].resourceId))
            exoPlayer.setMediaItem(mediaItem)
            exoPlayer.prepare()
        }
    }

    fun play() {
        player?.play()
        startForeground(NOTIFICATION_ID, createNotification())
    }

    fun pause() {
        player?.pause()
        updateNotification()
    }

    fun next() {
        if (currentPosition < currentList.size - 1) {
            currentPosition++
            setPlaylist(currentList, currentPosition)
            play()
        }
    }

    fun previous() {
        if (currentPosition > 0) {
            currentPosition--
            setPlaylist(currentList, currentPosition)
            play()
        }
    }

    fun getCurrentPosition(): Int {
        return currentPosition
    }

    fun setLooping(looping: Boolean) {
        player?.repeatMode = if (looping) Player.REPEAT_MODE_ONE else Player.REPEAT_MODE_OFF
    }

    fun getCurrentAudioTitle(): String {
        return currentList[currentPosition].title
    }

    private fun createNotification(): Notification {
        val notificationIntent = Intent(this, PlayerActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE)

        val playPauseIntent = Intent(this, AudioService::class.java).apply { action = ACTION_PLAY_PAUSE }
        val playPausePendingIntent = PendingIntent.getService(this, 0, playPauseIntent, PendingIntent.FLAG_IMMUTABLE)

        val nextIntent = Intent(this, AudioService::class.java).apply { action = ACTION_NEXT }
        val nextPendingIntent = PendingIntent.getService(this, 0, nextIntent, PendingIntent.FLAG_IMMUTABLE)

        val prevIntent = Intent(this, AudioService::class.java).apply { action = ACTION_PREV }
        val prevPendingIntent = PendingIntent.getService(this, 0, prevIntent, PendingIntent.FLAG_IMMUTABLE)

        val channelId = "AudioChannel"
        val channelName = "Audio Playback"
        val importance = NotificationManager.IMPORTANCE_LOW
        val notificationChannel = NotificationChannel(channelId, channelName, importance)
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(notificationChannel)

        return NotificationCompat.Builder(this, channelId)
            .setContentTitle(currentList[currentPosition].title)
            .setContentText("Now Playing")
            .setSmallIcon(R.drawable.ic_notification)
            .setContentIntent(pendingIntent)
            .addAction(R.drawable.ic_previous, "Previous", prevPendingIntent)
            .addAction(if (player?.isPlaying == true) R.drawable.ic_pause else R.drawable.ic_play, "Play/Pause", playPausePendingIntent)
            .addAction(R.drawable.ic_next, "Next", nextPendingIntent)
            .setStyle(androidx.media.app.NotificationCompat.MediaStyle()
                .setShowActionsInCompactView(0, 1, 2))
            .build()
    }


    private fun updateNotification() {
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, createNotification())
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_PLAY_PAUSE -> if (player?.isPlaying == true) pause() else play()
            ACTION_NEXT -> next()
            ACTION_PREV -> previous()
        }
        return START_STICKY
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        isPlayingBeforeStop = player?.isPlaying == true
        super.onTaskRemoved(rootIntent)
    }

    override fun onDestroy() {
        if (isPlayingBeforeStop) {
            val intent = Intent(this, AudioService::class.java)
            startService(intent)
        }
        player?.release()
        super.onDestroy()
    }

    companion object {
        const val NOTIFICATION_ID = 1
        const val ACTION_PLAY_PAUSE = "com.nicecoder.murottal.PLAY_PAUSE"
        const val ACTION_NEXT = "com.nicecoder.murottal.NEXT"
        const val ACTION_PREV = "com.nicecoder.murottal.PREV"
    }

}
