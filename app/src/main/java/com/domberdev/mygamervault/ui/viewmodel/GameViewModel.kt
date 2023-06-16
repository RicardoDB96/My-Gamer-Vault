package com.domberdev.mygamervault.ui.viewmodel

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.domberdev.mygamervault.data.database.AppDB
import com.domberdev.mygamervault.data.database.entities.GameEntity
import kotlinx.coroutines.*

class GameViewModel(application: Application) : ViewModel() {

    private val db = AppDB.getInstance(application)
    internal val allGames: LiveData<MutableList<GameEntity>> = db.gameDao().getAllGames()
    internal val wantGames: LiveData<MutableList<GameEntity>> = db.gameDao().getWantGames()
    internal val playingGames: LiveData<MutableList<GameEntity>> = db.gameDao().getPlayingGames()
    internal val playedGames: LiveData<MutableList<GameEntity>> = db.gameDao().getPlayedGames()
    internal val favoriteGames: LiveData<MutableList<GameEntity>> = db.gameDao().getFavoriteGames()
    internal val favoriteGame: LiveData<MutableList<GameEntity>> = db.gameDao().getFavoriteGame()

    fun insert(gameEntity: GameEntity) {
        viewModelScope.launch(Dispatchers.IO + NonCancellable) {
            db.gameDao().insertGame(gameEntity)
        }
    }

    fun update(gameEntity: GameEntity) {
        viewModelScope.launch(Dispatchers.IO + NonCancellable) {
            db.gameDao().updateGame(gameEntity)
        }
    }

    fun delete(id: Int) {
        viewModelScope.launch(Dispatchers.IO + NonCancellable) {
            db.gameDao().deleteGameById(db.gameDao().getGameById(id))
        }
    }

    fun getGameByID(id: Int): LiveData<GameEntity>{
        val result = MutableLiveData<GameEntity>()
        viewModelScope.launch(Dispatchers.IO + NonCancellable) {
            val returnedrepo = db.gameDao().getGameById(id)
            result.postValue(returnedrepo)
        }
        return result
    }
}