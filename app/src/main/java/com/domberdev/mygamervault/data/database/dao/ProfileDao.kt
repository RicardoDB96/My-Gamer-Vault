package com.domberdev.mygamervault.data.database.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.domberdev.mygamervault.data.database.entities.ProfileEntity

@Dao
interface ProfileDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(profile: ProfileEntity)

    @Update
    fun update(profile: ProfileEntity)

    @Query("SELECT * FROM profile_table LIMIT 1")
    fun getProfile(): LiveData<MutableList<ProfileEntity>>
}