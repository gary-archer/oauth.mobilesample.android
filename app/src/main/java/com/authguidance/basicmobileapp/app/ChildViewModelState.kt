package com.authguidance.basicmobileapp.app

import com.authguidance.basicmobileapp.api.client.ApiClient
import com.authguidance.basicmobileapp.views.utilities.ViewManager

/*
 * The goal of this class is a coding model similar to how React or SwiftUI initialise children
 * The main activity pushes fields to this class which child fragments can access
 */
class ChildViewModelState(
    val apiClientAccessor: () -> ApiClient?,
    val viewManager: ViewManager
) {
    // Header buttons are enabled when this evaluates to true
    lateinit var isDataLoadedAccessor: () -> Boolean

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
