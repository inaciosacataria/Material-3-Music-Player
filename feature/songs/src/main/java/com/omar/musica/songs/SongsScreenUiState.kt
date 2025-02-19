package com.omar.musica.songs

import androidx.compose.runtime.Immutable
import com.omar.musica.database.entities.ads.Ad
import com.omar.musica.model.SongSortOption
import com.omar.musica.store.model.song.Song


sealed interface SongsScreenUiState {

    @Immutable
    data class Success(
        val songs: List<Song>,
        val songSortOption: SongSortOption = SongSortOption.TITLE,
        val isSortedAscendingly: Boolean = true
    ) : SongsScreenUiState
}


sealed interface AdsScreenUiState {
    @Immutable
    data class Success(
        val ads: List<Ad>,
    ) : AdsScreenUiState
}