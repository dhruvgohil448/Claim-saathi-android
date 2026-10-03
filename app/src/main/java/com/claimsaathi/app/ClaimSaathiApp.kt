package com.claimsaathi.app

import android.app.Application
import com.claimsaathi.app.data.Network
import com.claimsaathi.app.data.TokenStore

class ClaimSaathiApp : Application() {
    override fun onCreate() {
        super.onCreate()
        TokenStore.init(this)
        Network.api
    }
}
