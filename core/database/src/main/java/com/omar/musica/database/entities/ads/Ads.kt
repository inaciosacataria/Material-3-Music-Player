package com.omar.musica.database.entities.ads

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
    val clickTime: String = formatTimeToDateAndTime(System.currentTimeMillis()),
    var isSyncend : Boolean = false)

data class ClickUpdateResponse(
    val status: Int,
    val success: Boolean,
    val message: String,
    val data: Slug
)

data class Slug(
    val slug: String
)


fun formatTimeToDateAndTime(clickTimeMillis: Long): String {
    val sdf = SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault())
    val date = Date(clickTimeMillis)
    return sdf.format(date)
}