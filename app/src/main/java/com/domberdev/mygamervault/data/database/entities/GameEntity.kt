package com.domberdev.mygamervault.data.database.entities

import android.graphics.Bitmap
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "game_table")
data class GameEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id") val id: Int = 0,
    @ColumnInfo(name = "gameTitle") val gameTitle: String,
    @ColumnInfo(name = "gameStatus") val gameStatus: Int,
    @ColumnInfo(name = "gameCover") val gameCover: Bitmap?,
    @ColumnInfo(name = "gameStart") val gameStart: String?,
    @ColumnInfo(name = "gameEnd") val gameEnd: String?,
    @ColumnInfo(name = "gameDescription") val gameDescription: String?,
    @ColumnInfo(name = "gamePlatforms") val gamePlatforms: String,
    @ColumnInfo(name = "gameScore") val gameScore: Int?,
    @ColumnInfo(name = "gameReview") val gameReview: String?,
    @ColumnInfo(name = "gameFavorite") val gameFavorite: Boolean
)