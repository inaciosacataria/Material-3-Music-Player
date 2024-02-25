package com.omar.musica.state

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation.NavHostController
import com.omar.musica.playlists.navigation.PLAYLIST_DETAILS_ROUTE
import com.omar.musica.songs.navigation.SEARCH_ROUTE
import com.omar.nowplaying.NowPlayingState
import com.omar.nowplaying.viewmodel.NowPlayingViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach


@Composable
fun rememberMusicaAppState(
    navHostController: NavHostController,
    coroutineScope: CoroutineScope = rememberCoroutineScope(),
    isNowPlayingExpanded: Boolean,
    nowPlayingViewModel: NowPlayingViewModel,
    nowPlayingExpansionProgress: () -> Float
): MusicaAppState {
    return remember(
        navHostController,
        coroutineScope,
        isNowPlayingExpanded,
        nowPlayingExpansionProgress
    ) {
        MusicaAppState(
            navHostController,
            coroutineScope,
            isNowPlayingExpanded,
            nowPlayingViewModel,
            nowPlayingExpansionProgress
        )
    }
}


@Stable
class MusicaAppState(
    val navHostController: NavHostController,
    val coroutineScope: CoroutineScope,
    val isNowPlayingExpanded: Boolean,
    val nowPlayingViewModel: NowPlayingViewModel,
    val nowPlayingExpansionProgress: () -> Float
) {


    /**
     * Whether we should show the NowPlaying Screen or not.
     */
    val shouldShowNowPlayingScreen = nowPlayingViewModel.state.map { it is NowPlayingState.Playing }

    val shouldShowBottomBar = navHostController.currentBackStackEntryFlow.onEach { delay(200) }.map {
        val route = it.destination.route ?: return@map true
        return@map !(route.contains(PLAYLIST_DETAILS_ROUTE) || route.contains(SEARCH_ROUTE))
    }


}