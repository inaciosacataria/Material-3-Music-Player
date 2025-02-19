package com.omar.musica.settings

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.DocumentsContract
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Block
import androidx.compose.material.icons.rounded.BlurCircular
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.FastForward
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.LibraryMusic
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.omar.musica.settings.common.ColorPickerDialog
import com.omar.musica.settings.common.GeneralSettingsItem
import com.omar.musica.settings.common.SettingInfo
import com.omar.musica.settings.common.SwitchSettingsItem
import com.omar.musica.settings.encriptation.SevenZipHelper
import com.omar.musica.settings.utils.copyUriToFile
import com.omar.musica.songs.viewmodel.SongsViewModel
import com.omar.musica.ui.common.fromIntToAccentColor
import com.omar.musica.ui.common.toInt
import com.omar.musica.ui.model.AppThemeUi
import com.omar.musica.ui.model.PlayerThemeUi
import com.omar.musica.ui.model.UserPreferencesUi
import getPath
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File


@Composable
fun SettingsScreen(
    modifier: Modifier,
    settingsViewModel: SettingsViewModel = hiltViewModel(),
    songsViewModel: SongsViewModel = hiltViewModel()
) {
    val state by settingsViewModel.state.collectAsState()
    SettingsScreen(
        modifier = modifier,
        state = state,
        settingsCallbacks = settingsViewModel,
        songsViewModel = songsViewModel
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    modifier: Modifier,
    state: SettingsState,
    settingsCallbacks: ISettingsViewModel,
    songsViewModel: SongsViewModel= hiltViewModel()
) {

    val topBarScrollBehaviour = TopAppBarDefaults.pinnedScrollBehavior()
    Scaffold(
        modifier = modifier,
        topBar = { SettingsTopAppBar(topBarScrollBehaviour) }
    )
    { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = paddingValues.calculateTopPadding()),
            contentAlignment = Alignment.Center
        ) {

            if (state is SettingsState.Loading) {
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopCenter)
                )
            } else if (state is SettingsState.Loaded) {
                SettingsList(
                    modifier = Modifier.fillMaxSize(),
                    userPreferences = state.userPreferences,
                    settingsCallbacks = settingsCallbacks,
                    nestedScrollConnection = topBarScrollBehaviour.nestedScrollConnection,
                    songsViewModel = songsViewModel
                )
            }

        }
    }

}


@Composable
fun SettingsList(
    modifier: Modifier,
    userPreferences: UserPreferencesUi,
    settingsCallbacks: ISettingsViewModel,
    nestedScrollConnection: NestedScrollConnection,
    songsViewModel : SongsViewModel
) {
    val sectionTitleModifier = Modifier
        .fillMaxWidth()
        .padding(start = 32.dp, top = 16.dp)

    val context = LocalContext.current
    val selectedFileName = remember { mutableStateOf("No file selected") }
    val isFileSelected = remember { mutableStateOf(false) }
    val isFileLoading = remember { mutableStateOf(false) }
    val progress = remember { mutableStateOf(0) }


    val fileChooserLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val uri: Uri? = result.data?.data
            if (uri != null) {
                val file = copyUriToFile(context, uri)
                if (file != null) {
                    selectedFileName.value = file.name
                    isFileSelected.value = true
                } else {
                    selectedFileName.value = "Failed to copy file"
                    isFileSelected.value = false
                }
            }
        }
    }




    LazyColumn(
        modifier.nestedScroll(nestedScrollConnection)
    ) {
        item {
            Divider(Modifier.fillMaxWidth())
            if (isFileLoading.value)
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                    //.align(Alignment.TopCenter)
                )
        }

        item {
            SectionTitle(modifier = sectionTitleModifier, title = "Biblioteca")
        }

        item {
            var blacklistDialogVisible by remember {
                mutableStateOf(false)
            }
            ConfirmDialog(
                isVisible = isFileSelected.value,
                onConfirm = {
                    isFileSelected.value = false
                    isFileLoading.value = true
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            Log.i("SevenZipHelper", "Coroutine started")
                            Log.i("SevenZipHelper", "File selected: ${selectedFileName.value}")

                            // Check if the file exists
                            val file = File(context.cacheDir, selectedFileName.value)
                            if (file.exists()) {
                                Log.i("SevenZipHelper", "File exists: ${file.absolutePath}")
                                SevenZipHelper.extractAndSaveToAppStorage(file, context)
                                Log.i("SevenZipHelper", "Extraction finished")
                            } else {
                                Log.e("SevenZipHelper", "File not found: ${file.absolutePath}")
                            }

                        } catch (e: Exception) {
                            Log.e("SevenZipHelper", "Error extracting archive", e)
                        } finally {
                            isFileLoading.value = false
                        }
                    }
                },
                onDismissRequest = { isFileSelected.value = false }
            )

            GeneralSettingsItem(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                            type = "*/*"
                            addCategory(Intent.CATEGORY_OPENABLE)
                        }
                        fileChooserLauncher.launch(intent)
                    }
                    .padding(horizontal = 32.dp, vertical = 16.dp),
                title = "Desbloqueie sua música",
                subtitle = "Selecione um arquivo para descriptografar e dar vida à sua música."
            )
        }

        item {
            SwitchSettingsItem(
                modifier = Modifier.fillMaxWidth(),
                title = "Gravar as musicas na memoria",
                "Gravar as musicas na memoria sem precisar do flash",
                toggled = userPreferences.playerSettings.pauseOnVolumeZero,
                onToggle = { settingsCallbacks.togglePauseVolumeZero() }
            )
        }

        item {
            SwitchSettingsItem(
                modifier = Modifier.fillMaxWidth(),
                title = "Foto do álbum em cache",
                info = SettingInfo(
                    title = "foto do álbum em cache",
                    text = "Se ativado, isso irá armazenar em cache a foto do álbum de uma música e reutilizá-la para todas as músicas que tenham o mesmo nome de álbum.\\n\\n\" +\n" +
                            "                            \"Isso melhorará muito a eficiência e o tempo de carregamento. No entanto, isso poderá causar problemas se houver duas músicas no mesmo álbum \" +\n" +
                            "                            \"não tenho a mesma arte.\\n\\n\" +\n" +
                            "                            \"Se desabilitado, isso carregará a foto do álbum de cada música separadamente, o que resultará em uma foto correta, em detrimento do tempo de carregamento\" +\n" +
                            "                            \" e memória.",
                    icon = Icons.Rounded.Info
                ),
                toggled = userPreferences.librarySettings.cacheAlbumCoverArt,
                onToggle = { settingsCallbacks.onToggleCacheAlbumArt() }
            )
        }

        item {
            HorizontalDivider(
                Modifier
                    .fillMaxWidth()
            )
        }

        item {
            SectionTitle(modifier = sectionTitleModifier, title = "Interface")
        }
        item {
            var appThemeDialogVisible by remember {
                mutableStateOf(false)
            }
            AppThemeDialog(
                visible = appThemeDialogVisible,
                currentSelected = userPreferences.uiSettings.theme,
                onDismissRequest = { appThemeDialogVisible = false },
                onThemeSelected = {
                    appThemeDialogVisible = false
                    settingsCallbacks.onThemeSelected(it)
                }
            )
            val text = when (userPreferences.uiSettings.theme) {
                AppThemeUi.SYSTEM -> "Configurações do sistema"
                AppThemeUi.LIGHT -> "Claro"
                AppThemeUi.DARK -> "Escuro"
            }
            GeneralSettingsItem(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { appThemeDialogVisible = true }
                    .padding(horizontal = 32.dp, vertical = 16.dp),
                title = "Tema do aplicativo",
                subtitle = text
            )
        }

        item {
            SwitchSettingsItem(
                modifier = Modifier
                    .fillMaxWidth(),
                title = "Use fundo preto para tema escuro",
                subtitle = "Preserva a bateria em telas AMOLED",
                toggled = userPreferences.uiSettings.blackBackgroundForDarkTheme,
                onToggle = { settingsCallbacks.toggleBlackBackgroundForDarkTheme() }
            )
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            item {
                SwitchSettingsItem(
                    modifier = Modifier
                        .fillMaxWidth(),
                    title = "Esquema de cores dinâmico",
                    toggled = userPreferences.uiSettings.isUsingDynamicColor,
                    onToggle = { settingsCallbacks.toggleDynamicColorScheme() }
                )
            }
        }

        item {
            var accentColorDialogVisible by remember {
                mutableStateOf(false)
            }
            if (accentColorDialogVisible) {
                ColorPickerDialog(
                    initialColor = userPreferences.uiSettings.accentColor.fromIntToAccentColor(),
                    onColorChanged = { color -> settingsCallbacks.setAccentColor(color.toInt()) },
                    onDismissRequest = { accentColorDialogVisible = false }
                )
            }
            GeneralSettingsItem(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { accentColorDialogVisible = true }
                    .padding(horizontal = 32.dp, vertical = 16.dp),
                title = "Cor de destaque",
                subtitle = "Cor do tema do aplicativo"
            )
        }

        item {
            SwitchSettingsItem(
                modifier = Modifier
                    .fillMaxWidth(),
                title = "Controles extras do Libertas Player",
                subtitle = "Mostrar botões próximo e anterior no Libertas Player",
                toggled = userPreferences.uiSettings.showMiniPlayerExtraControls,
                onToggle = settingsCallbacks::toggleShowExtraControls,
            )
        }

        item {
            var playerThemeDialogVisible by remember {
                mutableStateOf(false)
            }
            PlayerThemeDialog(
                playerThemeDialogVisible,
                userPreferences.uiSettings.playerThemeUi,
                onThemeSelected = {
                    playerThemeDialogVisible = false
                    settingsCallbacks.onPlayerThemeChanged(it)
                },
                onDismissRequest = { playerThemeDialogVisible = false },
            )
            GeneralSettingsItem(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { playerThemeDialogVisible = true }
                    .padding(horizontal = 32.dp, vertical = 16.dp),
                title = "Tema do Libertas Player",
                subtitle = when (userPreferences.uiSettings.playerThemeUi) {
                    PlayerThemeUi.SOLID -> "Sólido"
                    PlayerThemeUi.BLUR -> "Blur"
                }
            )
        }

        item {
            HorizontalDivider(
                Modifier
                    .fillMaxWidth()
            )
        }



        item {
            SectionTitle(modifier = sectionTitleModifier, title = "Player")
        }

        item {
            SwitchSettingsItem(
                modifier = Modifier.fillMaxWidth(),
                title = "Pausa no Volume Zero",
                "Pausa se o volume estiver definido como zero",
                toggled = userPreferences.playerSettings.pauseOnVolumeZero,
                onToggle = { settingsCallbacks.togglePauseVolumeZero() }
            )
        }

        item {
            SwitchSettingsItem(
                modifier = Modifier.fillMaxWidth(),
                title = "Retomar ao aumenta o volume",
                "Retome se o volume aumentar, se foi pausado devido à perda de volume",
                toggled = userPreferences.playerSettings.resumeWhenVolumeIncreases,
                onToggle = { settingsCallbacks.toggleResumeVolumeNotZero() }
            )
        }

        item {
            var jumpDurationDialogVisible by remember {
                mutableStateOf(false)
            }
            JumpDurationDialog(
                jumpDurationDialogVisible,
                userPreferences.playerSettings.jumpInterval,
                onDurationChanged = {
                    jumpDurationDialogVisible = false
                    settingsCallbacks.onJumpDurationChanged(it)
                },
                { jumpDurationDialogVisible = false }
            )
            GeneralSettingsItem(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { jumpDurationDialogVisible = true }
                    .padding(horizontal = 32.dp, vertical = 16.dp),
                title = "Intervalo de salto",
                subtitle = "${userPreferences.playerSettings.jumpInterval / 1000} segundos"
            )
        }


    }


}


@Composable
fun JumpDurationDialog(
    visible: Boolean,
    currentDurationMillis: Int,
    onDurationChanged: (Int) -> Unit,
    onDismissRequest: () -> Unit
) {

    if (!visible) return

    var durationString by remember(currentDurationMillis) {
        mutableStateOf((currentDurationMillis / 1000).toString())
    }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        dismissButton = { TextButton(onClick = onDismissRequest) { Text(text = "Fechar") } },
        confirmButton = {
            TextButton(onClick = {
                val duration = durationString.toIntOrNull() ?: return@TextButton
                onDurationChanged(duration * 1000)
            }) { Text(text = "Confirmar") }
        },
        icon = { Icon(Icons.Rounded.FastForward, contentDescription = null) },
        title = { Text(text = "Intervalo de salto") },
        text = {
            TextField(
                value = durationString,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                onValueChange = { durationString = it })
        }
    )


}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BlacklistedFoldersDialog(
    isVisible: Boolean,
    folders: List<String>,
    onFolderAdded: (String) -> Unit,
    onFolderDeleted: (String) -> Unit,
    onDismissRequest: () -> Unit,
) {

    if (!isVisible) return

    val context = LocalContext.current
    val directoryPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree(),
        onResult = { uri ->
            if (uri == null) return@rememberLauncherForActivityResult
            val documentTree = DocumentsContract.buildDocumentUriUsingTree(
                uri,
                DocumentsContract.getTreeDocumentId(uri)
            )
            val path = getPath(context, documentTree) ?: return@rememberLauncherForActivityResult
            onFolderAdded(path)
        }
    )


    AlertDialog(
        onDismissRequest = onDismissRequest,
        dismissButton = { TextButton(onClick = onDismissRequest) { Text(text = "Close") } },
        confirmButton = { },
        icon = { Icon(Icons.Rounded.Block, contentDescription = null) },
        title = { Text(text = "Pastas na lista negra") },
        text = {
            Column(verticalArrangement = Arrangement.SpaceBetween) {
                LazyColumn(modifier = Modifier) {
                    items(folders) {
                        Row(
                            modifier = Modifier.animateItemPlacement(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = it, modifier = Modifier.weight(1f))
                            Spacer(modifier = Modifier.width(4.dp))
                            IconButton(onClick = { onFolderDeleted(it) }) {
                                Icon(
                                    imageVector = Icons.Rounded.Delete,
                                    contentDescription = "Remover pasta da lista negra"
                                )
                            }
                        }
                        if (it != folders.last()) {
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Divider(Modifier.fillMaxWidth())
                Row(
                    verticalAlignment = Alignment.CenterVertically, modifier = Modifier
                        .fillMaxWidth()
                        .clip(
                            RoundedCornerShape(4.dp)
                        )
                        .clickable { directoryPicker.launch(null) }
                        .padding(8.dp)) {
                    Icon(imageVector = Icons.Rounded.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Adicionar caminho")
                }
            }
        }
    )

}


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ConfirmDialog(
    isVisible: Boolean,
    onConfirm: () -> Unit,
    onDismissRequest: () -> Unit,
) {
    if (!isVisible) return

    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismissRequest,
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text(text = "Não")
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(text = "Sim")
            }
        },
        icon = { Icon(Icons.Rounded.LibraryMusic, contentDescription = null) },
        title = { Text(text = "Desbloqueie sua música") },
        text = {
            Text(text = "Tem certeza de que deseja adicionar este arquivo à sua biblioteca?")
        }
    )
}


@Composable
fun AppThemeDialog(
    visible: Boolean,
    currentSelected: AppThemeUi,
    onDismissRequest: () -> Unit,
    onThemeSelected: (AppThemeUi) -> Unit,
) {
    if (!visible) return
    val optionsStrings = listOf("Follow System Settings", "Light", "Dark")
    val options = listOf(AppThemeUi.SYSTEM, AppThemeUi.LIGHT, AppThemeUi.DARK)
    val selectedOptionIndex by remember {
        mutableStateOf(
            options.indexOf(currentSelected).coerceAtLeast(0)
        )
    }
    AlertDialog(
        onDismissRequest = onDismissRequest,
        dismissButton = { TextButton(onClick = onDismissRequest) { Text(text = "Cancelar") } },
        confirmButton = { },
        icon = { Icon(Icons.Rounded.LightMode, contentDescription = null) },
        title = { Text(text = "Tema do aplicativo") },
        text = {
            Column {
                optionsStrings.forEachIndexed { index, option ->
                    val onSelected = {
                        if (index == selectedOptionIndex) {
                            Unit
                        } else {
                            onThemeSelected(options[index])
                        }
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = selectedOptionIndex == index,
                            onClick = { onSelected() }
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = option, modifier = Modifier.clickable { onSelected() })
                    }
                }
            }
        }
    )
}

@Composable
fun PlayerThemeDialog(
    visible: Boolean,
    currentSelected: PlayerThemeUi,
    onDismissRequest: () -> Unit,
    onThemeSelected: (PlayerThemeUi) -> Unit,
) {
    if (!visible) return
    val optionsStrings = listOf("Sólido", "Blur")
    val options = listOf(PlayerThemeUi.SOLID, PlayerThemeUi.BLUR)
    val selectedOptionIndex by remember {
        mutableStateOf(
            options.indexOf(currentSelected).coerceAtLeast(0)
        )
    }
    AlertDialog(
        onDismissRequest = onDismissRequest,
        dismissButton = { TextButton(onClick = onDismissRequest) { Text(text = "Cancel") } },
        confirmButton = { },
        icon = { Icon(Icons.Rounded.BlurCircular, contentDescription = null) },
        title = { Text(text = "Tema do Libertas player") },
        text = {
            Column {
                optionsStrings.forEachIndexed { index, option ->
                    val onSelected = {
                        if (index == selectedOptionIndex) {
                            Unit
                        } else {
                            onThemeSelected(options[index])
                        }
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = selectedOptionIndex == index,
                            onClick = { onSelected() }
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = option, modifier = Modifier.clickable { onSelected() })
                    }
                }
            }
        }
    )
}


@Composable
fun SectionTitle(
    modifier: Modifier,
    title: String
) {
    Text(
        modifier = modifier,
        text = title,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        color = MaterialTheme.colorScheme.tertiary
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsTopAppBar(topAppBarScrollBehavior: TopAppBarScrollBehavior) {
    TopAppBar(
        title = { Text(text = "Definições", fontWeight = FontWeight.SemiBold) },
        scrollBehavior = topAppBarScrollBehavior
    )
}