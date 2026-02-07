package com.example.jgsmusicplayer.data

import android.content.ContentUris
import android.content.Context
import android.provider.MediaStore

import com.example.jgsmusicplayer.model.AudioFile
import com.example.jgsmusicplayer.model.VideoFile

fun queryVideo(context: Context): List<VideoFile> {
    val cr = context.contentResolver
    val collection = MediaStore.Video.Media.EXTERNAL_CONTENT_URI

    val projection = arrayOf(
        MediaStore.Video.Media._ID,
        MediaStore.Video.Media.DISPLAY_NAME,
        MediaStore.Video.Media.RELATIVE_PATH
    )

    val selection = "${MediaStore.Video.Media.MIME_TYPE}=?"
    val selectionArgs = arrayOf("video/mp4")

    val sortOrder =
        "${MediaStore.Video.Media.RELATIVE_PATH} ASC, ${MediaStore.Video.Media.DISPLAY_NAME} ASC"

    val out = mutableListOf<VideoFile>()

    cr.query(collection, projection, selection, selectionArgs, sortOrder)?.use { c ->
        val idCol = c.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
        val nameCol = c.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME)
        val pathCol = c.getColumnIndexOrThrow(MediaStore.Video.Media.RELATIVE_PATH)

        while (c.moveToNext()) {
            val id = c.getLong(idCol)
            val name = c.getString(nameCol) ?: "unknown.mp4"
            val folder = c.getString(pathCol) ?: ""
            val uri = ContentUris.withAppendedId(collection, id)
            out += VideoFile(name, folder, uri)
        }
    }
    return out
}

fun queryAudio(context: Context): List<AudioFile> {
    val cr = context.contentResolver
    val collection = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI

    val projection = arrayOf(
        MediaStore.Audio.Media._ID,
        MediaStore.Audio.Media.DISPLAY_NAME,
        MediaStore.Audio.Media.RELATIVE_PATH
    )

    val selection = "(${MediaStore.Audio.Media.MIME_TYPE}=? OR ${MediaStore.Audio.Media.DISPLAY_NAME} LIKE ?)"
    val selectionArgs = arrayOf("audio/mpeg", "%.mp3")

    val sortOrder =
        "${MediaStore.Audio.Media.RELATIVE_PATH} ASC, ${MediaStore.Audio.Media.DISPLAY_NAME} ASC"

    val out = mutableListOf<AudioFile>()

    cr.query(collection, projection, selection, selectionArgs, sortOrder)?.use { c ->
        val idCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
        val nameCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME)
        val pathCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.RELATIVE_PATH)

        while (c.moveToNext()) {
            val id = c.getLong(idCol)
            val name = c.getString(nameCol) ?: "unknown.mp3"
            val folder = c.getString(pathCol) ?: ""
            val uri = ContentUris.withAppendedId(collection, id)
            out += AudioFile(name, folder, uri)
        }
    }
    return out
}
