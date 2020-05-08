package com.authguidance.basicmobileapp.views.fragments.userinfo

import androidx.databinding.BaseObservable
import com.authguidance.basicmobileapp.api.client.ApiClient
import com.authguidance.basicmobileapp.api.entities.UserInfoClaims
import com.authguidance.basicmobileapp.views.ViewManager

/*
 * A simple view model class for the user info view
 */
class UserInfoViewModel(
    apiClientAccessor: () -> ApiClient?,
    viewManager: ViewManager
) : BaseObservable() {

    // The client with which to retrieve data
    val apiClientAccessor: () -> ApiClient? = apiClientAccessor

    // The view manager is informed about API load events
    val viewManager: ViewManager = viewManager

    // The data once received
    private var claims: UserInfoClaims? = null

    /*
     * Markup calls this method to get the logged in user's full name
     */
    fun getLoggedInUser(): String {

        if (this.claims == null) {
            return ""
        }

        return "${this.claims!!.givenName} ${this.claims!!.familyName}"
    }

    /*
     * Update claims and inform the binding system
     */
    fun setClaims(claims: UserInfoClaims?) {

        this.claims = claims
        this.notifyChange()
    }
}
