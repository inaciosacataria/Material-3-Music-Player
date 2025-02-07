package com.omar.musica.network.service

import com.omar.musica.network.model.ClickUpdateResponse
import com.omar.musica.network.model.SmartAdsResponse
import retrofit2.http.GET
import retrofit2.http.POST

interface AdsService {

    companion object {
        const val BASE_URL = "https://nebula.cclawyers.co.mz/"
    }

    @GET("/api/ads")
    suspend fun fetchSmartAds(): SmartAdsResponse

    @POST
    suspend fun submitClicksReports(
        slug: String,
    ): ClickUpdateResponse
}