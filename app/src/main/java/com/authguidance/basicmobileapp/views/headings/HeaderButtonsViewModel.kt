package com.authguidance.basicmobileapp.views.headings

import androidx.databinding.BaseObservable

/*
 * A simple view model class for the header buttons fragment
 */
class HeaderButtonsViewModel(
    val isMainViewLoadedAccessor: () -> Boolean,
    val onHomeCallback: () -> Unit,
    val onReload: (Boolean) -> Unit,
    val onExpireAccessTokenCallback: () -> Unit,
    val onExpireRefreshTokenCallback: () -> Unit,
    val onLogoutCallback: () -> Unit
) : BaseObservable() {

    /*
     * The Android binding system requires real member functions rather than lambdas
     */
    fun onHome() {
        this.onHomeCallback()
    }

    fun onExpireAccessToken() {
        this.onExpireAccessTokenCallback()
    }

    fun onExpireRefreshToken() {
        this.onExpireRefreshTokenCallback()
    }

    fun onLogout() {
        this.onLogoutCallback()
    }

    /*
     * The binding system calls this and we call back the main activity
     */
    fun sessionButtonsEnabled(): Boolean {
        return this.isMainViewLoadedAccessor()
    }

    /*
     * The enabled state is updated during API calls and when logging out
     */
    fun updateSessionButtonEnabledState() {
        this.notifyChange()
    }
}
