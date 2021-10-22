package com.authguidance.basicmobileapp.views.headings

import androidx.databinding.BaseObservable
import com.authguidance.basicmobileapp.plumbing.events.ExpireAccessTokenEvent
import com.authguidance.basicmobileapp.plumbing.events.ExpireRefreshTokenEvent
import com.authguidance.basicmobileapp.plumbing.events.HomeEvent
import com.authguidance.basicmobileapp.plumbing.events.StartLogoutEvent
import com.authguidance.basicmobileapp.plumbing.events.StartReloadEvent
import org.greenrobot.eventbus.EventBus

/*
 * A simple view model class for the header buttons fragment
 */
class HeaderButtonsViewModel : BaseObservable() {

    private var hasData = false

    /*
     * Send an event when home is clicked
     */
    fun onHome() {
        EventBus.getDefault().post(HomeEvent())
    }

    /*
     * Send events when reload is clicked, and a long press of this button is used for error simulation
     */
    fun onReload(causeError: Boolean) {
        EventBus.getDefault().post(StartReloadEvent(causeError))
    }

    /*
     * Send an event to make the access token act expired
     */
    fun onExpireAccessToken() {
        EventBus.getDefault().post(ExpireAccessTokenEvent())
    }

    /*
     * Send an event to make the refresh token act expired
     */
    fun onExpireRefreshToken() {
        EventBus.getDefault().post(ExpireRefreshTokenEvent())
    }

    /*
     * Send an event to tell the main view to initiate logout
     */
    fun onLogout() {
        this.updateDataStatus(false)
        EventBus.getDefault().post(StartLogoutEvent())
    }

    /*
     * The binding system calls this and only enables buttons when we have data
     */
    fun hasData(): Boolean {
        return this.hasData
    }

    /*
     * The enabled state is updated during API calls and when logging out
     */
    fun updateDataStatus(hasData: Boolean) {
        this.hasData = hasData
        this.notifyChange()
    }
}
