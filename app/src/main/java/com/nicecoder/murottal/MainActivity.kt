package com.nicecoder.murottal

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.widget.ListView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : AppCompatActivity() {

    private lateinit var listView: ListView
    private lateinit var fabReverse: FloatingActionButton
    private var isReversed = false
    private lateinit var adapter: AudioAdapter
    private var audioService: AudioService? = null
    private var serviceBound = false

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as AudioService.LocalBinder
            audioService = binder.getService()
            serviceBound = true
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            audioService = null
            serviceBound = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        listView = findViewById(R.id.listView)
        fabReverse = findViewById(R.id.fabReverse)

        // Load the saved isReversed status
        val sharedPref = getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
        isReversed = sharedPref.getBoolean("isReversed", false)

        updateList()

        listView.setOnItemClickListener { _, _, position, _ ->
            val intent = Intent(this, PlayerActivity::class.java)
            intent.putExtra("position", position)
            intent.putExtra("isReversed", isReversed)
            startActivity(intent)
        }

        fabReverse.setOnClickListener {
            reverseAudioList()
        }

        // Start and bind to the AudioService
        val intent = Intent(this, AudioService::class.java)
        startService(intent)
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
    }

    private fun updateList() {
        val currentList = if (isReversed) AudioList.list.reversed() else AudioList.list
        adapter = AudioAdapter(this, currentList)
        listView.adapter = adapter
    }

    private fun reverseAudioList() {
        isReversed = !isReversed

        // Save the new isReversed status
        val sharedPref = getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putBoolean("isReversed", isReversed)
            apply()
        }

        updateList()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (serviceBound) {
            unbindService(serviceConnection)
            serviceBound = false
        }
    }
}
