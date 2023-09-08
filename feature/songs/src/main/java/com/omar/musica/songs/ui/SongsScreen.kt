package com.omar.musica.songs.ui

import android.app.Activity
import android.content.Context
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import com.omar.musica.model.Song
import com.omar.musica.songs.SongsScreenUiState
import com.omar.musica.songs.SongsViewModel
import com.omar.musica.ui.common.MenuActionItem
import com.omar.musica.ui.common.SongItem
import com.omar.musica.ui.common.SongsSummary
import com.omar.musica.ui.common.addToPlaylists
import com.omar.musica.ui.common.deleteAction
import com.omar.musica.ui.common.playNext
import com.omar.musica.ui.common.share
import com.omar.musica.ui.common.shareSongs
import com.omar.musica.ui.common.songDeleteAndroid10


private val api30AndUp = Build.VERSION.SDK_INT >= Build.VERSION_CODES.R
private val api29 = Build.VERSION.SDK_INT == Build.VERSION_CODES.Q

@Composable
fun SongsScreen(
    modifier: Modifier = Modifier,
    viewModel: SongsViewModel = hiltViewModel()
) {
    val songsUiState by viewModel.state.collectAsState()
    val context = LocalContext.current
    SongsScreen(
        modifier,
        songsUiState,
        viewModel::onSongClicked,
        viewModel::onPlayNext,
        { shareSongs(context, it) },
        viewModel::onDelete
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SongsScreen(
    modifier: Modifier,
    uiState: SongsScreenUiState,
    onSongClicked: (Song, Int) -> Unit,
    onPlayNext: (List<Song>) -> Unit,
    onShare: (List<Song>) -> Unit,
    onDelete: (List<Song>) -> Unit
) {

    val context = LocalContext.current
    val songs = (uiState as SongsScreenUiState.Success).songs

    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    val deleteRequestLauncher = deleteRequestLauncher()

    Scaffold(
        modifier = modifier,
        topBar = {
            SongsTopAppBar(Modifier, {}, scrollBehavior)
        },
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .padding(paddingValues)
                .nestedScroll(scrollBehavior.nestedScrollConnection),
        ) {

            item {
                SongsSummary(
                    Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, top = 16.dp),
                    songs.count(),
                    songs.sumOf { it.length }
                )
                Spacer(modifier = Modifier.height(8.dp))
                Divider(Modifier.fillMaxSize())
            }

            itemsIndexed(songs) { index, song ->

                val songDeleteAndroid10 = songDeleteAndroid10(songUri = song.uriString.toUri()) {
                    onDelete(listOf(song))
                }

                val menuActions = remember {
                    mutableListOf<MenuActionItem>()
                        .apply {
                            playNext { onPlayNext(listOf(song)) }
                            addToPlaylists { }
                            share { onShare(listOf(song)) }
                            deleteAction {
                                if (api30AndUp) {
                                    deleteRequestLauncher.launch(getIntentSenderRequest(context, song.uriString.toUri()))
                                } else {
                                    onDelete(listOf(song))
                                }
                            }
                        }
                }

                SongItem(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSongClicked(song, index) },
                    song = song,
                    menuOptions = menuActions
                )
                if (song != songs.last()) {
                    Divider(
                        Modifier
                            .fillMaxWidth()
                            .padding(start = (12 + 54 + 8).dp)
                    )
                }

            }

        }
    }

}

@RequiresApi(30)
private fun ComponentActivity.deleteRequestLauncher() =
    registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) {
        Toast.makeText(this, "Song deleted", Toast.LENGTH_SHORT).show()
    }

@Composable
private fun deleteRequestLauncher(): ActivityResultLauncher<IntentSenderRequest> {
    val context = LocalContext.current
    return rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult(),
        onResult = {
            if (it.resultCode == Activity.RESULT_OK) {
                Toast.makeText(context, "Song deleted", Toast.LENGTH_SHORT).show()
            }
        }
    )
}


@RequiresApi(30)
private fun getIntentSenderRequest(context: Context, uri: Uri): IntentSenderRequest {
    return with(context) {

        val deleteRequest =
            android.provider.MediaStore.createDeleteRequest(contentResolver, listOf(uri))

        IntentSenderRequest.Builder(deleteRequest)
            .setFillInIntent(null)
            .setFlags(android.content.Intent.FLAG_GRANT_WRITE_URI_PERMISSION, 0)
            .build()
    }
}