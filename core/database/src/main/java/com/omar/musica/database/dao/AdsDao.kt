package com.omar.musica.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.omar.musica.database.entities.ads.Ad
import com.omar.musica.database.entities.ads.Click

@Dao
interface AdsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAds(ads: List<Ad>)

    @Query("SELECT * FROM ads WHERE is_active = 1")
    fun getAds(): List<Ad>

    @Query("DELETE FROM ads WHERE id = :id")
    fun deleteAds(id: Int)

    @Query("DELETE FROM ads")
    fun deleteAllAds()


    //Clicks
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertClick(click: Click)

    @Query("SELECT * FROM clicks")
    fun getClicks(): List<Click>

    @Query("DELETE FROM clicks WHERE id = :id")
    fun deleteClicks(id: Int)

    @Query("DELETE FROM clicks")
    fun deleteAllClicks()
}