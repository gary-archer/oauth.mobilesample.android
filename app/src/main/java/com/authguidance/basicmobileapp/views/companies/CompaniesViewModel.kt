package com.authguidance.basicmobileapp.views.companies

import com.authguidance.basicmobileapp.api.client.ApiClient
import com.authguidance.basicmobileapp.api.client.ApiRequestOptions
import com.authguidance.basicmobileapp.api.entities.Company
import com.authguidance.basicmobileapp.plumbing.errors.UIError
import com.authguidance.basicmobileapp.views.utilities.ViewManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/*
 * A simple view model class for the companies view
 */
class CompaniesViewModel(
    val apiClientAccessor: () -> ApiClient?,
    val viewManager: ViewManager
) {

    // Data once retrieved
    var companies: List<Company> = ArrayList()

    /*
     * A method to do the work of calling the API
     */
    fun callApi(
        options: ApiRequestOptions,
        onSuccess: () -> Unit,
        onError: (UIError) -> Unit
    ) {

        // Do not try to load API data if the app is not initialised yet
        val apiClient = this.apiClientAccessor()
        if (apiClient == null) {
            this.viewManager.onViewLoaded()
            return
        }

        // Indicate a loading state
        this.viewManager.onViewLoading()

        // Make the remote call on a background thread
        val that = this@CompaniesViewModel
        CoroutineScope(Dispatchers.IO).launch {

            try {
                // Make the API call
                that.companies = apiClient.getCompanyList(options).toList()

                // Return results on the main thread
                withContext(Dispatchers.Main) {
                    that.viewManager.onViewLoaded()
                    onSuccess()
                }
            } catch (uiError: UIError) {

                // Return results on the main thread
                withContext(Dispatchers.Main) {
                    that.companies = ArrayList()
                    that.viewManager.onViewLoadFailed(uiError)
                    onError(uiError)
                }
            }
        }
    }
}
