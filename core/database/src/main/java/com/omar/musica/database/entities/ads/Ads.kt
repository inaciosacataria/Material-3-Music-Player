package com.omar.musica.database.entities.ads

import androidx.room.Entity
import androidx.room.PrimaryKey

data class SmartAdsResponse(
    val status: Int,
    val message: String,
    val success: Boolean,
    val data: SmartAdsData
)


data class SmartAdsData(
    val global_time: Long,
    val ads: List<Ad>
)

@Entity(tableName = "ads")
data class Ad(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    val title: String,
    val link: String,
    val time: Long,
    val photo: String,
    var is_active: Boolean = true
)


@Entity(tableName = "clicks")
data class Click(
    @PrimaryKey(autoGenerate = true)
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
