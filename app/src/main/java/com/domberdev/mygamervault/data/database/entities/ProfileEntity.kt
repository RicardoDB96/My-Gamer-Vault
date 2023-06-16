package com.domberdev.mygamervault.data.database.entities

import android.graphics.Bitmap
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "profile_table")
data class ProfileEntity (
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "userUUID") val UUID: String,
    @ColumnInfo(name = "username") val username: String,
    @ColumnInfo(name = "userPicture") val userPicture: Bitmap?,
    @ColumnInfo(name = "newUser") val newUser: Boolean,
    @ColumnInfo(name = "userConsoles") val userConsoles: String?
)