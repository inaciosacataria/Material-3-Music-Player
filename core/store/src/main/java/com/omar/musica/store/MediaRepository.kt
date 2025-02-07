package com.omar.musica.store

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.content.ContentUris
import android.content.Context
import android.database.ContentObserver
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.os.FileObserver
import android.provider.MediaStore
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import com.omar.musica.model.song.BasicSongMetadata
import com.omar.musica.store.MediaRepository.PermissionListener
import com.omar.musica.store.model.song.Song
import com.omar.musica.store.model.song.SongLibrary
import com.omar.musica.store.preferences.UserPreferencesRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton


private const val TAG = "MediaRepository"


/**
 * A class that is responsible for manipulating songs on the Android device.
 * It uses the MediaStore as the underlying database and exposes all the user's
 * library inside a [StateFlow] which automatically updates when the MediaStore updates.
 * Also, it provides methods to delete songs, and change their tags.
 */
@Singleton
class MediaRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    userPreferencesRepository: UserPreferencesRepository
) {


    private var mediaSyncJob: Job? = null
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Default)


    private lateinit var permissionListener: PermissionListener

    /** A state flow that contains all the songs in the user's device
    Automatically updates when the MediaStore changes
     */


    val songsFlow =
        callbackFlow {
            Timber.d(TAG, "Initializing callback flow to get all songs")

            var lastChangedUri: Uri? = null
            val observer = object : ContentObserver(null) {
                override fun onChange(selfChange: Boolean, uri: Uri?) {
                    if (uri == lastChangedUri) return
                    lastChangedUri = uri

                    // Evita criar múltiplos jobs simultaneamente
                    if (mediaSyncJob?.isActive == true) return

                    mediaSyncJob = launch {
                        try {
                            // Enviar as músicas após alteração
                            send(getAllPrivateSongs())
                        } catch (e: Exception) {
                            Timber.e(e.message)
                        } finally {
                            mediaSyncJob = null
                        }
                    }
                }
            }

            // 📌 Adicionando o FileObserver para monitorar o diretório privado
            val fileObserver = object : FileObserver(context.filesDir.absolutePath, CREATE or DELETE or MOVED_TO or MOVED_FROM) {
                @SuppressLint("TimberArgCount")
                override fun onEvent(event: Int, path: String?) {
                    if (path != null) {
                        Timber.d(TAG, "Private file changed: $path")
                        // Evita iniciar múltiplos jobs simultaneamente
                        if (mediaSyncJob?.isActive == true) return

                        mediaSyncJob = launch {
                            try {
                                send(getAllPrivateSongs()) // Atualiza as músicas privadas
                            } catch (e: Exception) {
                                Timber.e(e.message)
                            } finally {
                                mediaSyncJob = null
                            }
                        }
                    }
                }
            }
            fileObserver.startWatching() // Inicia a observação do diretório privado

            // 📌 Listener para mudanças de permissões
            permissionListener = PermissionListener {
                mediaSyncJob = launch {
                    try {
                        send(getAllPrivateSongs()) // Envia as músicas
                    } catch (e: Exception) {
                        Timber.e(e.message)
                    } finally {
                        mediaSyncJob = null
                    }
                }
            }

            // Registra o ContentObserver para monitorar mudanças de conteúdo
            context.contentResolver.registerContentObserver(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                true,
                observer
            )

            // 📌 Sincronização inicial
            mediaSyncJob = launch {
                try {
                    send(getAllPrivateSongs()) // Envia as músicas iniciais
                } catch (e: Exception) {
                    Timber.e(e.message)
                } finally {
                    mediaSyncJob = null
                }
            }

            // 📌 Para de observar mudanças quando o Flow for cancelado
            awaitClose {
                context.contentResolver.unregisterContentObserver(observer)
                fileObserver.stopWatching()
            }
        }.combine(
            userPreferencesRepository.librarySettingsFlow.map { it.excludedFolders }
        ) { songs: List<Song>, excludedFolders: List<String> ->
            val filteredSongs = songs.filter { song ->
                !excludedFolders.any { folder ->
                    song.filePath.startsWith(folder)
                }
            }
            SongLibrary(filteredSongs)
        }.flowOn(Dispatchers.IO).stateIn(
            scope = scope,
            started = SharingStarted.Eagerly,
            initialValue = SongLibrary(listOf())
        )


    /**
     * Função para recuperar todas as músicas privadas
     */
    suspend fun getAllPrivateSongs(): List<Song> = withContext(Dispatchers.IO) {
        val privateDir = context.filesDir // Diretório privado do app
        val results = mutableListOf<Song>()

        privateDir.listFiles()
            ?.filter { file -> file.extension == "mp3" || file.extension == "wav" }
            ?.forEach { file ->
                val fileUri = file.toUri()

                // Extrair metadados do arquivo
                val retriever = MediaMetadataRetriever()
                try {
                    retriever.setDataSource(file.absolutePath)
                    val title = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE) ?: file.nameWithoutExtension
                    val artist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST) ?: "<unknown>"
                    val album = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM) ?: "<unknown>"
                    val duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull() ?: 0L

                    val basicMetadata = BasicSongMetadata(
                        title = title,
                        artistName = artist,
                        albumName = album,
                        durationMillis = duration,
                        sizeBytes = file.length(),
                        trackNumber = 0
                    )

                    // Criar a instância de Song e adicionar à lista
                    Song(
                        uri = fileUri,
                        metadata = basicMetadata,
                        filePath = file.absolutePath,
                        albumId = 0L
                    ).apply { Timber.d(this.toString()) }.also(results::add)

                } catch (e: Exception) {
                    Timber.e(e) // Ignora o arquivo se houver erro
                } finally {
                    retriever.release()
                }
            }

        results
    }


//    val songsFlow =
//        callbackFlow {
//
//            Timber.d(TAG, "Initializing callback flow to get all songs")
//
//            var lastChangedUri: Uri? = null
//            val observer = object : ContentObserver(null) {
//                override fun onChange(selfChange: Boolean, uri: Uri?) {
//                    if (uri == lastChangedUri) return
//                    lastChangedUri = uri
//
//                    if (mediaSyncJob?.isActive == true) return
//
//                    mediaSyncJob = launch {
//                        try {
//                            send(getAllPrivateSongs())
//                        } catch (e: Exception) {
//                            Timber.e(e.message)
//                        } finally {
//                            mediaSyncJob = null
//                        }
//                    }
//                }
//            }
//
//            // 📌 Adicionando o FileObserver para monitorar o diretório privado
//            val fileObserver = object : FileObserver(context.filesDir.absolutePath, CREATE or DELETE or MOVED_TO or MOVED_FROM) {
//                @SuppressLint("TimberArgCount")
//                override fun onEvent(event: Int, path: String?) {
//                    if (path != null) {
//                        Timber.d(TAG, "Private file changed: $path")
//                        mediaSyncJob = launch {
//                            try {
//                                send(getAllPrivateSongs()) // Atualiza as músicas privadas
//                            } catch (e: Exception) {
//                                Timber.e(e.message)
//                            } finally {
//                                mediaSyncJob = null
//                            }
//                        }
//                    }
//                }
//            }
//            fileObserver.startWatching() // Inicia a observação do diretório privado
//
//            permissionListener = PermissionListener {
//                mediaSyncJob = launch {
//                    send(getAllPrivateSongs())
//                    mediaSyncJob = null
//                }
//            }
//
//            context.contentResolver.registerContentObserver(
//                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
//                true,
//                observer
//            )
//
//            // 📌 Sincronização inicial
//            mediaSyncJob = launch {
//                try {
//                    send(getAllPrivateSongs())
//                } catch (e: Exception) {
//                    Timber.e(e.message)
//                } finally {
//                    mediaSyncJob = null
//                }
//            }
//
//            // 📌 Para de observar mudanças quando o Flow for cancelado
//            awaitClose {
//                context.contentResolver.unregisterContentObserver(observer)
//                fileObserver.stopWatching()
//            }
//        }.combine(
//            userPreferencesRepository.librarySettingsFlow.map { it.excludedFolders }
//        ) { songs: List<Song>, excludedFolders: List<String> ->
//            val filteredSongs = songs.filter { song ->
//                !excludedFolders.any { folder ->
//                    song.filePath.startsWith(folder)
//                }
//            }
//            SongLibrary(filteredSongs)
//        }.flowOn(Dispatchers.IO).stateIn(
//            scope = scope,
//            started = SharingStarted.Eagerly,
//            initialValue = SongLibrary(listOf())
//        )
//
//
//    /**
//     * Retrieves all the user's songs on the device along with their [BasicSongMetadata]
//     */
//
//
//
//    suspend fun getAllPrivateSongs(): List<Song> = withContext(Dispatchers.IO) {
//        val privateDir = context.filesDir // Diretório privado do app
//        val results = mutableListOf<Song>()
//
//        privateDir.listFiles()
//            ?.filter { file -> file.extension == "mp3" || file.extension == "wav" }
//            ?.forEach { file ->
//                val fileUri = file.toUri()
//
//                // Extrair metadados do arquivo
//                val retriever = MediaMetadataRetriever()
//                try {
//                    retriever.setDataSource(file.absolutePath)
//                    val title = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE) ?: file.nameWithoutExtension
//                    val artist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST) ?: "<unknown>"
//                    val album = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM) ?: "<unknown>"
//                    val duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull() ?: 0L
//
//                    val basicMetadata = BasicSongMetadata(
//                        title = title,
//                        artistName = artist,
//                        albumName = album,
//                        durationMillis = duration,
//                        sizeBytes = file.length(),
//                        trackNumber = 0
//                    )
//
//                    Song(
//                        uri = fileUri,
//                        metadata = basicMetadata,
//                        filePath = file.absolutePath,
//                        albumId = 0L
//                    ).apply { Timber.d(this.toString()) }.also(results::add)
//
//                } catch (e: Exception) {
//                    Timber.e(e) // Ignora o arquivo se houver erro
//                } finally {
//                    retriever.release()
//                }
//            }
//
//        results
//    }



    suspend fun getAllSongs(): List<Song> = withContext(Dispatchers.IO) {

        val projection =
            arrayOf(
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.DISPLAY_NAME,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.SIZE,
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.ALBUM_ID,
                MediaStore.Audio.Media.TRACK
            )

        with(context) {

            val cursor = contentResolver.query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                projection,
                null, null, null, null
            ) ?: throw Exception("Invalid cursor")

            val results = mutableListOf<Song>()
            cursor.use { c ->
                while (c.moveToNext() && isActive) {
                    val idColumn = cursor.getColumnIndex(MediaStore.Audio.Media._ID)
                    val fileNameColumn = cursor.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME)
                    val titleColumn = cursor.getColumnIndex(MediaStore.Audio.Media.TITLE)
                    val artistColumn = cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)
                    val durationColumn = cursor.getColumnIndex(MediaStore.Audio.Media.DURATION)
                    val sizeColumn = cursor.getColumnIndex(MediaStore.Audio.Media.SIZE)
                    val pathColumn = cursor.getColumnIndex(MediaStore.Audio.Media.DATA)
                    val albumColumn = cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM)
                    val albumIdColumn = cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID)
                    val trackNumberColumn = cursor.getColumnIndex(MediaStore.Audio.Media.TRACK)

                    val fileUri = ContentUris.withAppendedId(
                        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                        cursor.getInt(idColumn).toLong()
                    )

                    val basicMetadata = BasicSongMetadata(
                        title = c.getString(titleColumn),
                        artistName = c.getString(artistColumn) ?: "<unknown>",
                        albumName = c.getString(albumColumn) ?: "<unknown>",
                        durationMillis = c.getLong(durationColumn),
                        sizeBytes = c.getLong(sizeColumn),
                        trackNumber = c.getInt(trackNumberColumn) % 1000
                    )

                    try {
                        Song(
                            uri = fileUri,
                            metadata = basicMetadata,
                            filePath = c.getString(pathColumn),
                            albumId = c.getLong(albumIdColumn)
                        ).apply { Timber.d(this.toString()) }.also(results::add)
                    } catch (e: Exception) {
                        Timber.e(e) // ignore the song for now if any problems occurred
                    }
                }
            }

            results
        }
    }


    @TargetApi(29)
    fun deleteSong(song: Song) {

        Timber.d("Deleting song $song")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Timber.e("Attempting to delete song in R or Higher. Use Activity Contracts instead")
            return
        }

        try {
            val file = File(song.filePath)
            file.delete()
            context.contentResolver.delete(song.uri, null, null)
        } catch (e: Exception) {
            Timber.e(e)
        }
    }

//    suspend fun getSongPath(uri: Uri): String = withContext(Dispatchers.IO) {
//
//        val projection =
//            arrayOf(
//                MediaStore.Audio.Media.DATA,
//            )
//        val selection = "${MediaStore.Audio.Media._ID} = ${uri.lastPathSegment!!}"
//
//        val cursor = context.contentResolver.query(
//            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
//            projection,
//            selection,
//            null,
//            null,
//            null
//        ) ?: throw Exception("Invalid cursor")
//
//        cursor.use {
//            it.moveToFirst()
//            val pathColumn = it.getColumnIndex(MediaStore.Audio.Media.DATA)
//            return@withContext it.getString(pathColumn)
//        }
//    }

    // Método modificado para buscar apenas no diretório privado do aplicativo
    suspend fun getSongPath(uri: Uri): String = withContext(Dispatchers.IO) {
        val privateDir = context.filesDir // Diretório privado do app
        val fileName = uri.lastPathSegment ?: return@withContext ""

        // Verifica se o arquivo existe no diretório privado do app
        val privateFile = File(privateDir, fileName)
        if (privateFile.exists()) {
            return@withContext privateFile.absolutePath // Retorna o caminho se encontrado no diretório privado
        }

        // Se não encontrar, lança uma exceção, já que estamos restringindo a busca ao conteúdo privado
        throw Exception("Arquivo não encontrado no diretório privado do aplicativo.")
    }


    /**
     * Called by the MainActivity to inform the repo that the user
     * granted the READ permission, in order to refresh the music library
     */
    fun onPermissionAccepted() {
        permissionListener.onPermissionGranted()
    }

    /**
     * Interface implemented inside the callback flow of the [MediaRepository]
     * to force refresh of the song library when the user grants the permission
     */
    private fun interface PermissionListener {
        fun onPermissionGranted()
    }

}