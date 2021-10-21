package com.authguidance.basicmobileapp.app

import androidx.lifecycle.ViewModel

/*
 * Details from the main activity that are shared with child fragments
 * This is done by the Android system using 'by activityViewModels()' calls
 */
class MainActivitySharedViewModel(
    mainModel: MainActivityViewModel,
    activityEvents: MainActivityEvents,
    private val isInLoginRequired: () -> Boolean
) : ViewModel() {

    // Properties used for API calls
    val apiClient = mainModel.apiClient
    val apiViewEvents = mainModel.apiViewEvents

    // Header buttons
    val onHome = activityEvents::onHome
    val onReload = activityEvents::onReloadData
    val onExpireAccessToken = mainModel::onExpireAccessToken
    val onExpireRefreshToken = mainModel::onExpireRefreshToken
    val onLogout = activityEvents::onStartLogout

    // Properties passed to the user info fragment
    val shouldLoadUserInfo = {
        mainModel.isDeviceSecured && !isInLoginRequired()
    }

    // Properties passed to the session fragment
    val shouldShowSessionId = {
        mainModel.isDeviceSecured && mainModel.authenticator.isLoggedIn()
    }
}
