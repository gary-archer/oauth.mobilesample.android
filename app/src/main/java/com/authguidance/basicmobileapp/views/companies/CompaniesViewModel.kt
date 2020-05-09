package com.authguidance.basicmobileapp.views.companies

import com.authguidance.basicmobileapp.api.client.ApiClient
import com.authguidance.basicmobileapp.views.utilities.ViewManager

/*
 * A simple view model class for the companies view
 */
class CompaniesViewModel(
    val apiClientAccessor: () -> ApiClient?,
    val viewManager: ViewManager
)
