package com.omar.musica.songs.ui

import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Sync
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberImagePainter
import com.omar.musica.database.entities.ads.Ad
import com.omar.musica.model.SongSortOption
import com.omar.musica.songs.SongsScreenUiState
import com.omar.musica.songs.viewmodel.SongsViewModel
import com.omar.musica.store.model.song.Song
import com.omar.musica.ui.common.LocalCommonSongsAction
import com.omar.musica.ui.common.MultiSelectState
import com.omar.musica.ui.menu.buildCommonMultipleSongsActions
import com.omar.musica.ui.menu.buildCommonSongActions
import com.omar.musica.ui.songs.SongsSummary
import com.omar.musica.ui.songs.selectableSongsList
import com.omar.musica.ui.topbar.SelectionTopAppBarScaffold
import kotlinx.coroutines.delay


@Composable
fun Banner(ads: List<Ad>) {
    var currentAdIndex by remember { mutableStateOf(0) }
    val context = LocalContext.current


    LaunchedEffect(ads, currentAdIndex) {
        if (ads.isNotEmpty()) {
            delay(ads[currentAdIndex].time * 500)
            currentAdIndex = (currentAdIndex + 1) % ads.size
            Log.d("ads", "Current Ad Index: $currentAdIndex")
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                Log.d("Banner, ", "Banner Clicked")
                val intent =
                    Intent(Intent.ACTION_VIEW, Uri.parse(ads.getOrNull(currentAdIndex)?.link))
                context.startActivity(intent)
            },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Log.d("ads", "ads: $ads")
        Image(
            painter = rememberImagePainter(data = ads.getOrNull(currentAdIndex)?.photo),
            contentDescription = "Advertisement Banner",
            modifier = Modifier.height(120.dp)
        )
    }
}


@Composable
fun SongsScreen(
    modifier: Modifier = Modifier,
    viewModel: SongsViewModel = hiltViewModel(),
    onSearchClicked: () -> Unit,
) {

    LaunchedEffect(Unit) {
        viewModel.fetchAds()
    }

    val ads = viewModel.ads.value
    Log.d("SongsScreen", "Ads: $ads")


    val songsUiState by viewModel.state.collectAsState()

    Column {
        SongsScreenList(
            modifier,
            songsUiState,
            viewModel::onSongClicked,
            onSearchClicked,
            ads = ads,
            viewModel::onSortOptionChanged
        )
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SongsScreenList(
    modifier: Modifier,
    uiState: SongsScreenUiState,
    onSongClicked: (Song, Int) -> Unit,
    onSearchClicked: () -> Unit,
    ads: List<Ad>,
    onSortOptionChanged: (SongSortOption, isAscending: Boolean) -> Unit
) {

    val context = LocalContext.current


    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    val multiSelectState = remember {
        MultiSelectState<Song>()
    }

    val multiSelectEnabled by remember {
        derivedStateOf { multiSelectState.selected.size > 0 }
    }

    BackHandler(multiSelectEnabled) {
        multiSelectState.clear()
    }

    val commonSongActions = LocalCommonSongsAction.current

    Scaffold(
        modifier = modifier,
        topBar = {

            SelectionTopAppBarScaffold(
                modifier = Modifier.fillMaxWidth(),
                multiSelectState = multiSelectState,
                isMultiSelectEnabled = multiSelectEnabled,
                actionItems = buildCommonMultipleSongsActions(
                    multiSelectState.selected,
                    context,
                    commonSongActions.playbackActions,
                    commonSongActions.addToPlaylistDialog,
                    commonSongActions.shareAction
                ),
                numberOfVisibleIcons = 2,
                scrollBehavior = scrollBehavior
            ) {
                Column {


                    TopAppBar(
                        modifier = Modifier.fillMaxWidth(),
                        title = { Text(text = "Songs", fontWeight = FontWeight.SemiBold) },
                        actions = {
                            IconButton(onSearchClicked) {
                                Icon(Icons.Rounded.Search, contentDescription = null)
                            }
                        },
                        scrollBehavior = scrollBehavior
                    )


                }

            }
        },
    ) { paddingValues ->
        val layoutDirection = LocalLayoutDirection.current
        Column(
            Modifier
                .padding(
                    top = paddingValues.calculateTopPadding(),
                    end = paddingValues.calculateEndPadding(layoutDirection),
                    start = paddingValues.calculateStartPadding(layoutDirection)
                )
                .nestedScroll(scrollBehavior.nestedScrollConnection)
        ) {
            Banner(ads = ads)

            LazyColumn(

            ) {
                Log.d("SongsScreenList", "uiState: $uiState")
                when (uiState) {

                    is SongsScreenUiState.Success -> {
                        Log.d("SongsScreenList", "when success uiState: $uiState")
                        val songs = (uiState as SongsScreenUiState.Success).songs
                        when {
                            songs.size > 0 -> {
                                item {
                                    AnimatedVisibility(visible = !multiSelectEnabled) {
                                        SongsSummary(
                                            Modifier
                                                .fillMaxWidth()
                                                .padding(start = 16.dp, top = 16.dp),
                                            songs.count(),
                                            songs.sumOf { it.metadata.durationMillis }
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Divider(Modifier.fillMaxWidth())
                                    }
                                }

//                                item {
//                                    AnimatedVisibility(visible = !multiSelectEnabled) {
//                                        SortChip(
//                                            modifier = Modifier.padding(
//                                                top = 8.dp,
//                                                start = 12.dp,
//                                                bottom = 4.dp
//                                            ),
//                                            songSortOptions = SongSortOption.entries,
//                                            onSortOptionSelected = onSortOptionChanged,
//                                            currentSongSortOption = uiState.songSortOption,
//                                            isAscending = uiState.isSortedAscendingly
//                                        )
//                                    }
//                                }

                                selectableSongsList(
                                    songs,
                                    multiSelectState,
                                    multiSelectEnabled,
                                    menuActionsBuilder = { song: Song ->
                                        with(commonSongActions) {
                                            buildCommonSongActions(
                                                song = song,
                                                context = context,
                                                songPlaybackActions = this.playbackActions,
                                                songInfoDialog = this.songInfoDialog,
                                                addToPlaylistDialog = this.addToPlaylistDialog,
                                                shareAction = this.shareAction,
                                                setAsRingtoneAction = this.setRingtoneAction,
                                                songDeleteAction = this.deleteAction,
                                                tagEditorAction = this.openTagEditorAction
                                            )
                                        }
                                    },
                                    onSongClicked = onSongClicked
                                )

                                item {
                                    Spacer(modifier = Modifier.navigationBarsPadding())
                                }
                            }

                            songs.size < 1 -> {
                                item {
                                    Column(
                                        Modifier
                                            .fillMaxWidth()
                                            .height(200.dp),
                                        verticalArrangement = Arrangement.Center,
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text("list empty")
                                    }

                                }
                            }

                        }
                    }

                    else -> {
                        Log.d("SongsScreenList", "when else uiState: $uiState")
                        item {
                            Column(
                                Modifier
                                    .fillMaxWidth()
                                    .height(200.dp),
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("list empty")
                            }
                        }
                    }
                }
            }
        }
    }
}



