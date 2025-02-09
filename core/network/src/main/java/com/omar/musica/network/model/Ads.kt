package com.omar.musica.network.model

data class SmartAdsResponse(
    val status: Int,
    val message: String,
    val success: Boolean,
    val data: SmartAdsData
)


data class SmartAdsData(
    val global_time: Long,
    val ads: List<AdResponse>
)


data class AdResponse(
    val id: Int,
    val title: String,
    val link: String,
    val time: Long,
    val photo: String
)


data class ClickModel(
    val id: Int = 0,
    val adId: Int,
    val slug: String,
    val clickTime: Long)

data class ClickUpdateResponse(
    val status: Int,
    val success: Boolean,
    val message: String,
    val data: Slug
)

data class Slug(
    val slug: String
)