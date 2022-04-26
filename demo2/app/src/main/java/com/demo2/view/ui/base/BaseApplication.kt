package com.demo2.view.ui.base

import android.app.Application
import com.google.firebase.FirebaseApp
import com.google.firebase.crashlytics.FirebaseCrashlytics


class BaseApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
       // FirebaseCrashlytics.getInstance()
    }

  /*  override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(Pref.updateResources(base!!, Pref.getLocalization(base!!)))
    }

    override fun onConfigurationChanged(newConfig: Configuration?) {
        super.onConfigurationChanged(newConfig)
        Pref.updateResources(this, Pref.getLocalization(this))
    }*/
}