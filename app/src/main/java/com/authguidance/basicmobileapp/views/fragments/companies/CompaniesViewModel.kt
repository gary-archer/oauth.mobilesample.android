package com.authguidance.basicmobileapp.views.fragments.companies

import com.authguidance.basicmobileapp.api.client.ApiClient
import com.authguidance.basicmobileapp.views.ViewManager

/*
 * A simple view model class for the companies view
 */
class CompaniesViewModel(

    // The client with which to retrieve data
    val apiClientAccessor: () -> ApiClient?,

    // The view manager is informed about API load events
    val viewManager: ViewManager
)
