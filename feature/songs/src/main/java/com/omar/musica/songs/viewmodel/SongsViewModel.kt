package com.omar.musica.songs.viewmodel

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.omar.musica.database.entities.ads.Ad
import com.omar.musica.database.entities.ads.Click
import com.omar.musica.model.SongSortOption
import com.omar.musica.playback.PlaybackManager
import com.omar.musica.songs.SongsScreenUiState
import com.omar.musica.store.MediaRepository
import com.omar.musica.store.ads.AdsRepository
import com.omar.musica.store.model.song.Song
import com.omar.musica.store.model.song.SongLibrary
import com.omar.musica.store.preferences.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class SongsViewModel @Inject constructor(
    private val adsRepository: AdsRepository,
    private val mediaRepository: MediaRepository,
    private val mediaPlaybackManager: PlaybackManager,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val sortOptionFlow = userPreferencesRepository.librarySettingsFlow
        .map { it.songsSortOrder }.distinctUntilChanged()

    var ads = mutableStateOf(listOf<Ad>())

//    var state: StateFlow<SongsScreenUiState> =
//        mediaRepository.songsFlow
//            .map { it.songs }
//            .combine(sortOptionFlow) { songList, sortOptionPair ->
//
//                val ascending = sortOptionPair.second
//                val sortedList = if (ascending)
//                    songList.sortedByOptionAscending(sortOptionPair.first)
//                else
//                    songList.sortedByOptionDescending(sortOptionPair.first)
//                SongsScreenUiState.Success(sortedList, sortOptionPair.first, sortOptionPair.second)
//            }
//            .stateIn(
//                viewModelScope,
//                started = SharingStarted.WhileSubscribed(5000),
//                initialValue = SongsScreenUiState.Success(listOf())
//            )

    // last change
//    var state: StateFlow<SongsScreenUiState> =
//        mediaRepository.songsFlow
//            .map { it.songs }
//            .combine(sortOptionFlow) { songList, sortOptionPair ->
//
//                val ascending = sortOptionPair.second
//                val sortedList = if (ascending)
//                    songList.sortedByOptionAscending(sortOptionPair.first)
//                else
//                    songList.sortedByOptionDescending(sortOptionPair.first)
//
//                SongsScreenUiState.Success(sortedList, sortOptionPair.first, sortOptionPair.second)
//            }
//            .stateIn(
//                viewModelScope,
//                started = SharingStarted.WhileSubscribed(5000),
//                initialValue = SongsScreenUiState.Success(listOf())
//            )


    var state: StateFlow<SongsScreenUiState> =
        mediaRepository.songsFlow
            .flatMapLatest { songLibrary: SongLibrary? ->
                if (songLibrary == null || songLibrary.songs.isNullOrEmpty()) {
                    flowOf(SongsScreenUiState.Success(emptyList(), SongSortOption.TITLE, true))
                } else {
                    flowOf(SongsScreenUiState.Success(songLibrary.songs, SongSortOption.TITLE, true))
                }
            }
            .stateIn(
                viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = SongsScreenUiState.Success(emptyList(), SongSortOption.TITLE, true)
            )


    /**
     * Helper function to sort songs based on the given option and order.
     */
    private fun sortSongs(
        songs: List<Song>,
        sortOption: SongSortOption,
        ascending: Boolean
    ): List<Song> {
        return if (ascending) {
            songs.sortedByOptionAscending(sortOption)
        } else {
            songs.sortedByOptionDescending(sortOption)
        }
    }


    /**
     * User clicked a song in the list. Default action is to play
     */
    fun onSongClicked(song: Song, index: Int) {
        val songs = (state.value as SongsScreenUiState.Success).songs
        mediaPlaybackManager.setPlaylistAndPlayAtIndex(songs, index)
    }

    fun onPlayNext(songs: List<Song>) {
        mediaPlaybackManager.playNext(songs)
    }

    /**
     * User changed the sorting order of the songs screen
     */
    fun onSortOptionChanged(songSortOption: SongSortOption, isAscending: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.changeLibrarySortOrder(songSortOption, isAscending)
        }
    }


    /**
     * User wants to delete songs.
     * This is only intended for Android versions lower than R, since R and higher have different methods to delete songs.
     * Mainly, in Android R and above, we will have to send an intent to delete a media item and the system will ask the user for permission.
     * So they are implemented as part of the UI in Jetpack Compose
     */


    fun onDelete(songs: List<Song>) {
        mediaRepository.deleteSong(songs[0])
    }

    private fun List<Song>.sortedByOptionAscending(songSortOption: SongSortOption): List<Song> =
        when (songSortOption) {
            SongSortOption.TITLE -> this.sortedBy { it.metadata.title.lowercase() }
            SongSortOption.ARTIST -> this.sortedBy { it.metadata.artistName?.lowercase() }
            SongSortOption.FileSize -> this.sortedBy { it.metadata.sizeBytes }
            SongSortOption.ALBUM -> this.sortedBy { it.metadata.albumName }
            SongSortOption.Duration -> this.sortedBy { it.metadata.durationMillis }
        }


    private fun List<Song>.sortedByOptionDescending(songSortOption: SongSortOption): List<Song> =
        when (songSortOption) {
            SongSortOption.TITLE -> this.sortedByDescending { it.metadata.title.lowercase() }
            SongSortOption.ARTIST -> this.sortedByDescending { it.metadata.artistName?.lowercase() }
            SongSortOption.FileSize -> this.sortedByDescending { it.metadata.sizeBytes }
            SongSortOption.ALBUM -> this.sortedByDescending { it.metadata.albumName }
            SongSortOption.Duration -> this.sortedByDescending { it.metadata.durationMillis }
        }


     fun fetchAds() {
        viewModelScope.launch {
            ads.value = adsRepository.fetchAds()
        }
    }


    fun submitCLicks(click: Click){
        viewModelScope.launch {
            adsRepository.submitClicks(click)
        }
    }






}


