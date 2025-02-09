package com.omar.musica.ui

import android.content.Context
import android.widget.Toast


fun Context.showShortToast(text: String) =
    Toast.makeText(this, text, Toast.LENGTH_SHORT).show()

fun Context.showSongsAddedToNextToast(numOfSongs: Int) =
    showShortToast("$numOfSongs ${if (numOfSongs == 1) "Música" else "Músicas"} vão tocar a seguir")

fun Context.showSongsAddedToQueueToast(numOfSongs: Int) =
    showShortToast("$numOfSongs ${if (numOfSongs == 1) "Música" else "Músicas"} adicionadas a fila")

fun Context.showSongsAddedToPlaylistsToast(numOfSongs: Int, numOfPlaylists: Int) =
    showShortToast("$numOfSongs ${if(numOfSongs == 1) "Música" else "Músicas"} adicionadas a ${if (numOfPlaylists == 1) "Lista de reprodução" else "Listas de reprodução"}")