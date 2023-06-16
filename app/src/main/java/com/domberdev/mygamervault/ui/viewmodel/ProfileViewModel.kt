package com.domberdev.mygamervault.ui.viewmodel

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.domberdev.mygamervault.data.database.AppDB
import com.domberdev.mygamervault.data.database.entities.ProfileEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.launch

class ProfileViewModel(application: Application): ViewModel() {

    private val db = AppDB.getInstance(application)
    internal val getProfile: LiveData<MutableList<ProfileEntity>> = db.profileDao().getProfile()

    fun insert(profileEntity: ProfileEntity) {
        viewModelScope.launch(Dispatchers.IO + NonCancellable) {
            db.profileDao().insert(profileEntity)
        }
    }

    fun update(profileEntity: ProfileEntity) {
        viewModelScope.launch(Dispatchers.IO + NonCancellable) {
            db.profileDao().update(profileEntity)
        }
    }
}