package com.omar.musica.network.di

import android.content.Context
import com.omar.musica.network.service.AdsService
import com.omar.musica.network.service.LyricsService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create
import javax.inject.Qualifier

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class LyricsRetrofitService

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class AdsRetrofitService

@Module
@InstallIn(SingletonComponent::class)
object RetrofitModule {


    @Provides
    fun provideLyricsService(
        @LyricsRetrofitService lyricsRetrofitService: Retrofit
    ) = lyricsRetrofitService.create<LyricsService>()

    @LyricsRetrofitService
    @Provides
    fun provideRetrofit() = Retrofit.Builder()
        .baseUrl(LyricsService.BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()


    @Provides
    fun provideAdsService(
        @AdsRetrofitService adsRetrofitService: Retrofit
    ) = adsRetrofitService.create<AdsService>()

    @AdsRetrofitService
    @Provides
    fun provideAdsRetrofit() = Retrofit.Builder()
        .baseUrl(AdsService.BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
}