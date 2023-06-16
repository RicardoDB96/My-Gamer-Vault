package com.domberdev.mygamervault.data.database.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.domberdev.mygamervault.data.database.entities.GameEntity

@Dao
interface GameDao {

    //Get all games
    @Query("SELECT * FROM game_table")
    fun getAllGames(): LiveData<MutableList<GameEntity>>

    //Get all want games from db
    @Query("SELECT * FROM game_table WHERE gameStatus = 0")
    fun getWantGames(): LiveData<MutableList<GameEntity>>

    //Get all playing games from db
    @Query("SELECT * FROM game_table WHERE gameStatus = 1")
    fun getPlayingGames(): LiveData<MutableList<GameEntity>>

    //Get all played games from db
    @Query("SELECT * FROM game_table WHERE gameStatus = 2 ORDER BY gameScore DESC")
    fun getPlayedGames(): LiveData<MutableList<GameEntity>>

    //Get all favorite games from db
    @Query("SELECT * FROM game_table WHERE gameStatus = 2 AND gameFavorite = 1 ORDER BY gameScore DESC")
    fun getFavoriteGames(): LiveData<MutableList<GameEntity>>

    @Query("SELECT * FROM game_table WHERE gameStatus = 2 AND gameFavorite = 1 ORDER BY gameScore DESC LIMIT 1")
    fun getFavoriteGame(): LiveData<MutableList<GameEntity>>

    //Get game by id
    @Query("SELECT * FROM game_table WHERE id = :id")
    fun getGameById(id: Int): GameEntity

    @Update
    fun updateGame(game: GameEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertGame(game: GameEntity)

    @Delete
    fun deleteGameById(GameEntity: GameEntity)
}