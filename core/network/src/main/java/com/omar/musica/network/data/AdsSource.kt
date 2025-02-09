package com.omar.musica.network.data

import android.util.Log
import com.omar.musica.network.model.AdResponse
import com.omar.musica.network.model.Slug
import com.omar.musica.network.service.AdsService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AdsSource @Inject constructor(
    private val api: AdsService,
) {

    suspend fun fetchSmartAds(): List<AdResponse> {
        return withContext(Dispatchers.IO) {
            Log.d("AdRepository", "Fetching smart ads from API...")
            val apiResponse = api.fetchSmartAds()
            if (apiResponse.success) {
                apiResponse.data.ads
            } else {
                emptyList()
            }
        }
    }

    suspend fun submitClicks(slug: String) : Boolean {
       return withContext(Dispatchers.IO) {
            Log.d("AdRepository", "Submitting clicks to API...")
            val apiResponse = api.submitClicksReports(slug)
            if (apiResponse.success){
                Log.d("AdRepository", "Clicks submitted successfully")
                return@withContext true
            } else {
                Log.d("AdRepository", "Failed to submit clicks")
                return@withContext false
            }
        }
    }
}