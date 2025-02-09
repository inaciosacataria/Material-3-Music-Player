package com.omar.musica.network.service

import com.omar.musica.network.model.ClickUpdateResponse
import com.omar.musica.network.model.SmartAdsResponse
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface AdsService {

    companion object {
        const val BASE_URL = "https://nebula.cclawyers.co.mz/"
    }

    @GET("/api/ads")
    suspend fun fetchSmartAds(): SmartAdsResponse

    @FormUrlEncoded
    @POST("/api/smart-banner-update-clicks")
    suspend fun submitClicksReports(
        @Field("slug") slug: String
    ): ClickUpdateResponse
}