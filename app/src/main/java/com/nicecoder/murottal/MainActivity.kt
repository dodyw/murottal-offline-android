package com.nicecoder.murottal

import android.content.Intent
import android.os.Bundle
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

        adapter = AudioAdapter(this, AudioList.list)
        listView.adapter = adapter

        listView.setOnItemClickListener { _, _, position, _ ->
            val intent = Intent(this, PlayerActivity::class.java)
            intent.putExtra("position", position)
            intent.putExtra("isReversed", isReversed)
            startActivity(intent)
        }

        fabReverse.setOnClickListener {
            reverseAudioList()
        }
    }

    private fun reverseAudioList() {
        isReversed = !isReversed
        val reversedList = if (isReversed) {
            AudioList.list.reversed()
        } else {
            AudioList.list
        }

        adapter = AudioAdapter(this, reversedList)
        listView.adapter = adapter
    }
}
