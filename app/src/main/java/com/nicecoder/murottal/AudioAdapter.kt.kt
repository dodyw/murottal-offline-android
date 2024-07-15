package com.nicecoder.murottal

import android.content.Context
import android.media.MediaMetadataRetriever
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView

class AudioAdapter(context: Context, private val audioList: List<Audio>) :
    ArrayAdapter<Audio>(context, R.layout.list_item_audio, audioList) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.list_item_audio, parent, false)

        val audio = audioList[position]
        val titleTextView: TextView = view.findViewById(R.id.titleTextView)
        val durationTextView: TextView = view.findViewById(R.id.durationTextView)

        titleTextView.text = audio.title
        durationTextView.text = getDuration(audio.resourceId)

        return view
    }

    private fun getDuration(resourceId: Int): String {
        val mediaMetadataRetriever = MediaMetadataRetriever()
        val assetFileDescriptor = context.resources.openRawResourceFd(resourceId)
        mediaMetadataRetriever.setDataSource(
            assetFileDescriptor.fileDescriptor,
            assetFileDescriptor.startOffset,
            assetFileDescriptor.length
        )
        val durationMs = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLong() ?: 0
        mediaMetadataRetriever.release()
        assetFileDescriptor.close()

        val seconds = (durationMs / 1000).toInt()
        val minutes = seconds / 60
        val remainingSeconds = seconds % 60
        return String.format("%d:%02d", minutes, remainingSeconds)
    }
}