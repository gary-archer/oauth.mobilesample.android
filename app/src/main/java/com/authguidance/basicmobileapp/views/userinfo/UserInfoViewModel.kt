package com.authguidance.basicmobileapp.views.userinfo

import androidx.databinding.BaseObservable
import com.authguidance.basicmobileapp.api.client.ApiClient
import com.authguidance.basicmobileapp.api.entities.UserInfoClaims
import com.authguidance.basicmobileapp.views.utilities.ViewManager

/*
 * A simple view model class for the user info view
 */
class UserInfoViewModel(
    val apiClientAccessor: () -> ApiClient?,
    val viewManager: ViewManager,
    val shouldLoadAccessor: () -> Boolean
) : BaseObservable() {

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
