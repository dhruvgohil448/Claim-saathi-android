package com.claimsaathi.app.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.claimsaathi.app.data.Claim
import com.claimsaathi.app.data.ClaimQuery
import com.claimsaathi.app.data.Home
import com.claimsaathi.app.data.Network
import com.claimsaathi.app.data.Policy
import com.claimsaathi.app.data.TokenStore
import com.claimsaathi.app.data.UploadResponse
import com.claimsaathi.app.data.User
import kotlinx.coroutines.launch

class AppVm : ViewModel() {
    var authed by mutableStateOf(TokenStore.token != null)
        private set
    var user by mutableStateOf<User?>(null)
    var home by mutableStateOf<Home?>(null)
    var policies by mutableStateOf<List<Policy>>(emptyList())
    var claims by mutableStateOf<List<Claim>>(emptyList())
    var queries by mutableStateOf<List<ClaimQuery>>(emptyList())
    var unread by mutableStateOf(0)
    var busy by mutableStateOf(false)
    var error by mutableStateOf<String?>(null)
    var lastUpload by mutableStateOf<UploadResponse?>(null)
    var tab by mutableStateOf(AppTab.Home)
    /** Set by "Ask Saathi" buttons; the chat tab sends it once and clears it. */
    var chatPrompt by mutableStateOf<String?>(null)
    var chatClaimId by mutableStateOf<String?>(null)
    var claimsLoaded by mutableStateOf(false)

    fun logout() {
        TokenStore.token = null
        authed = false
        user = null
        home = null
    }

    fun signIn(token: String, signedIn: User) {
        TokenStore.token = token
        user = signedIn
        authed = true
    }

    fun replaceToken(token: String, updated: User) {
        TokenStore.token = token
        user = updated
    }

    fun work(block: suspend () -> Unit) {
        viewModelScope.launch {
            busy = true
            error = null
            try {
                block()
            } catch (e: Exception) {
                error = Network.apiMessage(e)
            } finally {
                busy = false
            }
        }
    }

    fun refreshHome() {
        viewModelScope.launch {
            runCatching { Network.api.home() }
                .onSuccess {
                    home = it
                    unread = it.counts.unreadNotifications
                }
                .onFailure { error = Network.apiMessage(it) }
        }
    }
}

enum class AppTab { Home, Claims, Assistant, Alerts, Profile }
