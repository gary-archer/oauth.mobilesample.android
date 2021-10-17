package com.authguidance.basicmobileapp.app

import androidx.lifecycle.ViewModel
import com.authguidance.basicmobileapp.api.client.ApiClient
import com.authguidance.basicmobileapp.views.utilities.ApiViewEvents

/*
 * Details from the main activity that are shared with child fragments
 * This is done by the Android system using 'by viewModels()' and 'by activityViewModels()' calls
 */
class MainActivitySharedViewModel : ViewModel() {

    // Properties used by fragments that do data access
    lateinit var apiClientAccessor: () -> ApiClient?
    lateinit var apiViewEvents: ApiViewEvents

    // Header buttons are enabled when this evaluates to true
    lateinit var isMainViewLoadedAccessor: () -> Boolean

    // Callbacks used by the header buttons view
    lateinit var onHome: () -> Unit
    lateinit var onReload: (Boolean) -> Unit
    lateinit var onExpireAccessToken: () -> Unit
    lateinit var onExpireRefreshToken: () -> Unit
    lateinit var onLogout: () -> Unit

    // The user info view loads data when this evaluates to true
    lateinit var shouldLoadUserInfoAccessor: () -> Boolean

    // The session view is shown when this evaluates to true
    lateinit var shouldShowSessionIdAccessor: () -> Boolean
}
