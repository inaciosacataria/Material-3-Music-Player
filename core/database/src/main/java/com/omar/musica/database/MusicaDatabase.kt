package com.omar.musica.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.omar.musica.database.dao.ActivityDao
import com.omar.musica.database.dao.AdsDao
import com.omar.musica.database.dao.BlacklistedFoldersDao
import com.omar.musica.database.dao.LyricsDao
import com.omar.musica.database.dao.PlaylistDao
import com.omar.musica.database.dao.QueueDao
import com.omar.musica.database.entities.activity.ListeningSessionEntity
import com.omar.musica.database.entities.ads.Ad
import com.omar.musica.database.entities.ads.Click
import com.omar.musica.database.entities.lyrics.LyricsEntity
import com.omar.musica.database.entities.playlist.PlaylistEntity
import com.omar.musica.database.entities.playlist.PlaylistsSongsEntity
import com.omar.musica.database.entities.prefs.BlacklistedFolderEntity
import com.omar.musica.database.entities.queue.QueueEntity


@Database(
    entities = [
        PlaylistEntity::class,
        PlaylistsSongsEntity::class,
        BlacklistedFolderEntity::class,
        QueueEntity::class,
        ListeningSessionEntity::class,
        LyricsEntity::class,
        Ad::class,
        Click::class
    ],
    version = 6, exportSchema = false
)
abstract class MusicaDatabase : RoomDatabase() {

    abstract fun playlistsDao(): PlaylistDao
    abstract fun blacklistDao(): BlacklistedFoldersDao
    abstract fun queueDao(): QueueDao
    abstract fun activityDao(): ActivityDao
    abstract fun lyricsDao(): LyricsDao
    abstract fun adsDao(): AdsDao
}