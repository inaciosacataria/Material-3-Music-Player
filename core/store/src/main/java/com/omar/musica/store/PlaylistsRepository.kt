package com.omar.musica.store

import com.omar.musica.database.dao.PlaylistDao
import com.omar.musica.database.model.PlaylistInfoWithNumberOfSongs
import com.omar.musica.model.Playlist
import com.omar.musica.model.PlaylistInfo
import com.omar.musica.model.Song
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject


class PlaylistsRepository @Inject constructor(
    private val playlistsDao: PlaylistDao,
    private val mediaRepository: MediaRepository,
    coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) {

    val playlistsWithInfoFlows =
        playlistsDao.getPlaylistsInfoFlow()
            .map {
                it.toDomainPlaylists()
            }
            .stateIn(coroutineScope, SharingStarted.WhileSubscribed(5000), listOf())


    fun getPlaylistWithSongsFlow(playlistId: Int): Flow<Playlist> =
        combine(
            mediaRepository.songsFlow,
            playlistsDao.getPlaylistWithSongsFlow(playlistId)
        ) { songs, playlistWithSongs ->

            // Convert the songs to a map to enable fast retrieval
            val songsSet = songs.associateBy { it.uriString }

            // The uris of the song
            val playlistSongsUriStrings = playlistWithSongs.songUris

            val playlistSongs = mutableListOf<Song>()
            for (uriString in playlistSongsUriStrings.map { it.songUriString }) {
                val song = songsSet[uriString]
                if (song != null) {
                    playlistSongs.add(song)
                }
            }

            val playlistInfo = playlistWithSongs.playlistEntity
            Playlist(
                PlaylistInfo(playlistInfo.id, playlistInfo.name, playlistSongs.size),
                playlistSongs
            )
        }


    private fun PlaylistInfoWithNumberOfSongs.toDomainPlaylist() =
        PlaylistInfo(
            id = playlistEntity.id,
            name = playlistEntity.name,
            numberOfSongs = numberOfSongs
        )

    private fun List<PlaylistInfoWithNumberOfSongs>.toDomainPlaylists() =
        map { it.toDomainPlaylist() }


}