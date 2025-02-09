package com.omar.musica.store.ads

import android.app.Application
import android.content.Context
import android.util.Log
import com.omar.musica.database.dao.AdsDao
import com.omar.musica.database.entities.ads.Ad
import com.omar.musica.database.entities.ads.Click
import com.omar.musica.network.data.AdsSource
import com.omar.musica.network.model.AdResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.FileOutputStream
import java.net.URL
import javax.inject.Inject
import kotlin.collections.map

class AdsRepository @Inject constructor(
    private val adsDao: AdsDao,
    private val adsSource: AdsSource,
    private val context: Application
) {

    suspend fun fetchAds(): List<Ad> {
        return withContext(Dispatchers.IO) {
            try {
                val adsResponse = adsSource.fetchSmartAds()
                val ads = adsResponse.map { adResponse ->
                    val localImagePath =
                        downloadImage(adResponse.photo) // Baixa e retorna o caminho local
                    adResponse.convertAdsResponseToAds(localImagePath)
                }

                adsDao.insertAds(ads)
                syncClicksAds()
                return@withContext ads
            } catch (e: Exception) {
                Log.e("AdsRepository", "Error fetching ads", e)

                val ads = adsDao.getAds()
                Log.d("AdsRepository", "Ads from database: $ads")

                if (ads.isNotEmpty()) {
                    return@withContext ads
                }

                return@withContext emptyList<Ad>()
            }
        }
    }

    suspend fun submitClicks(click: Click) {
        withContext(Dispatchers.IO) {
            try {
                var clickId = adsDao.insertClick(click)
                if (clickId > 0) {
                    var response = adsSource.submitClicks(click.slug)
                    if (response) {
                        Log.d("AdsRepository", "Clicks submitted successfully")
                        adsDao.deleteClicks(click.id)
                        Log.d("AdsRepository", "deleted successfully")
                    } else {
                        Log.d("AdsRepository", "Failed to submit clicks")
                    }
                }
            } catch (e: Exception) {
                var clickId = adsDao.insertClick(click)
                if (clickId > 0) {
                    Log.e("AdsRepository", "submiting clicks offline")
                }else{
                    Log.e("AdsRepository", "error submiting clicks offline")
                }
                Log.e("AdsRepository", "erro submiting clicks offiline", e)
            }
        }
    }

    suspend fun syncClicksAds() {
        withContext(Dispatchers.IO) {
            try {
                var clicks = adsDao.getClicks()
                if (clicks.isNotEmpty()) {
                    clicks.forEach { click ->
                        var response = adsSource.submitClicks(click.slug)
                        if (response) {
                            Log.d("AdsRepository", "Clicks submitted successfully")
                            adsDao.deleteClicks(click.id)
                            Log.d("AdsRepository", "deleted successfully")
                        } else {
                            Log.d("AdsRepository", "Failed to submit clicks")
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("AdsRepository", "erro submiting clicks", e)
            }
        }
    }


    suspend fun deleteAds(id: Int) {
        adsDao.deleteAds(id)
    }

    fun downloadImage(imageUrl: String): String {
        val imageFileName = imageUrl.substringAfterLast("/")
        val localFilePath = context.cacheDir.resolve(imageFileName)

        if (!localFilePath.exists()) {
            val inputStream = URL(imageUrl).openStream()
            val outputStream = FileOutputStream(localFilePath)
            inputStream.copyTo(outputStream)
            inputStream.close()
            outputStream.close()
        }

        return localFilePath.absolutePath
    }


    fun AdResponse.convertAdsResponseToAds(imageUrl: String): Ad {
        return Ad(
            id = this.id,
            title = this.title,
            link = this.link,
            time = this.time,
            photo = imageUrl
        )
    }
}