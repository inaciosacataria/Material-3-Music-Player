package com.omar.musica

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.anchoredDraggable
import androidx.compose.foundation.gestures.animateTo
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.omar.musica.navigation.MusicaBottomNavBar
import com.omar.musica.navigation.TopLevelDestination
import com.omar.musica.navigation.navigateToTopLevelDestination
import com.omar.musica.songs.navigation.SONGS_NAVIGATION_GRAPH
import com.omar.musica.songs.navigation.songsGraph
import com.omar.musica.ui.theme.MusicaTheme
import com.omar.nowplaying.ui.BarState
import com.omar.nowplaying.ui.NowPlayingScreen
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import kotlin.math.roundToInt


val PLAYLIST_NAVIGATION_GRAPH = "playlists"
val SETTINGS_NAVIGATION_GRAPH = "settings"

@AndroidEntryPoint
class MainActivity : ComponentActivity() {




    @OptIn(ExperimentalFoundationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {

            MusicaTheme {
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                ) {

                    val navController = rememberNavController()

                    val scope = rememberCoroutineScope()
                    val density = LocalDensity.current
                    val anchorState = remember {
                        AnchoredDraggableState(
                            BarState.COLLAPSED,
                            positionalThreshold = { distance: Float -> 0.5f * distance },
                            velocityThreshold = { with(density) { 100.dp.toPx() } },
                            animationSpec = tween()
                        )
                    }

                    val barHeight = 64.dp
                    val barHeightPx = with(density) { barHeight.toPx() }

                    var boxMinOffset by remember { mutableFloatStateOf(0.0f) }


                    val topLevelDestinations = remember {
                        listOf(
                            TopLevelDestination.SONGS,
                            TopLevelDestination.PLAYLISTS,
                            TopLevelDestination.SETTINGS
                        )
                    }


                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        bottomBar = {
                            val backStackState by navController.currentBackStackEntryAsState()
                            MusicaBottomNavBar(
                                modifier = Modifier.fillMaxWidth(),
                                topLevelDestinations = topLevelDestinations,
                                currentDestination = backStackState?.destination,
                                onDestinationSelected = { navController.navigateToTopLevelDestination(it) }
                            )
                        }
                    ) { paddingValues ->

                        Box(modifier = Modifier
                            .fillMaxSize()
                            .padding(
                                bottom = paddingValues.calculateBottomPadding()
                            )) {
                            NavHost(
                                modifier = Modifier.fillMaxSize(),
                                navController = navController,
                                startDestination = SONGS_NAVIGATION_GRAPH
                            ) {
                                songsGraph(navController) { navController.navigate("nowplaying") }

                                composable(PLAYLIST_NAVIGATION_GRAPH) {
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("Playlists")
                                    }
                                }

                                composable(SETTINGS_NAVIGATION_GRAPH) {
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("Settings")
                                    }
                                }

                            }


                            val isExpanded by remember {
                                derivedStateOf { (1 - (anchorState.offset / boxMinOffset)) >= 0.9f }
                            }

                            NowPlayingScreen(
                                barHeight = barHeight,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .offset {
                                        IntOffset(
                                            // 2
                                            x = 0,
                                            y = anchorState
                                                .requireOffset()
                                                .roundToInt(),
                                        )
                                    }
                                    .onSizeChanged { layoutSize ->
                                        anchorState.updateAnchors(
                                            DraggableAnchors {
                                                // 5
                                                val offset = (-barHeightPx + layoutSize.height)
                                                boxMinOffset = offset
                                                BarState.COLLAPSED at offset
                                                BarState.EXPANDED at 0.0f
                                            }
                                        )
                                    }
                                    .anchoredDraggable(anchorState, Orientation.Vertical),
                                onCollapseNowPlaying = {
                                    scope.launch {
                                        anchorState.animateTo(BarState.COLLAPSED)
                                    }
                                },
                                onExpandNowPlaying = {
                                    scope.launch {
                                        anchorState.animateTo(BarState.EXPANDED)
                                    }
                                },
                                isExpanded = isExpanded,
                                progressProvider = { 1 - (anchorState.offset / boxMinOffset) }
                            )


                        }


                    }


                }
            }
        }
    }

}
