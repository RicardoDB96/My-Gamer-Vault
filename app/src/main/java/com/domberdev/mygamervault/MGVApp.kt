package com.domberdev.mygamervault

import android.app.Application
import com.google.android.gms.ads.MobileAds

class MGVApp : Application() {

    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.BUILD_TYPE == "release"){
            MobileAds.initialize(this)
        }
    }
}