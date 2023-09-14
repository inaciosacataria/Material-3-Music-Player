package com.omar.musica.database.di

import android.content.Context
import androidx.room.Room
import com.omar.musica.database.MusicaDatabase
import com.omar.musica.database.entities.DB_NAME
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Singleton
    @Provides
    fun provideRoomDatabase(
        @ApplicationContext context: Context
    ): MusicaDatabase =
        Room.databaseBuilder(context, MusicaDatabase::class.java, name = DB_NAME)
            .build()

    @Singleton
    @Provides
    fun providePlaylistDao(
        appDatabase: MusicaDatabase
    ) = appDatabase.playlistsDao()

}
