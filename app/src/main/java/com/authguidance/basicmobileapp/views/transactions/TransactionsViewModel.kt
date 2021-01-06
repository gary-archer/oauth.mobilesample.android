package com.authguidance.basicmobileapp.views.transactions

import com.authguidance.basicmobileapp.api.client.ApiClient
import com.authguidance.basicmobileapp.api.client.ApiRequestOptions
import com.authguidance.basicmobileapp.api.entities.Transaction
import com.authguidance.basicmobileapp.plumbing.errors.ErrorCodes
import com.authguidance.basicmobileapp.plumbing.errors.UIError
import com.authguidance.basicmobileapp.views.utilities.ApiViewEvents
import com.authguidance.basicmobileapp.views.utilities.Constants.VIEW_MAIN
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/*
 * A simple view model class for the transactions view
 */
class TransactionsViewModel(
    val apiClientAccessor: () -> ApiClient?,
    val apiViewEvents: ApiViewEvents,
    val companyId: String,
    val titleFormat: String
) {
    // Data once retrieved
    var transactions: List<Transaction> = ArrayList()

    /*
     * Markup calls this method to get the title including the company id
     */
    fun getTitle(): String {
        return String.format(this.titleFormat, this.companyId)
    }

    /*
     * A method to do the work of calling the API
     */
    fun callApi(
        options: ApiRequestOptions,
        onSuccess: () -> Unit,
        onError: (UIError, Boolean) -> Unit
    ) {

        // Do not try to load API data if the app is not initialised yet
        val apiClient = this.apiClientAccessor()
        if (apiClient == null) {
            return
        }

        // Indicate a loading state
        this.apiViewEvents.onViewLoading(VIEW_MAIN)

        // Make the remote call on a background thread
        val that = this@TransactionsViewModel
        CoroutineScope(Dispatchers.IO).launch {

            try {
                // Make the API call
                val data = apiClient.getCompanyTransactions(that.companyId, options)
                that.transactions = data.transactions.toList()

                // Return results on the main thread
                withContext(Dispatchers.Main) {
                    that.apiViewEvents.onViewLoaded(VIEW_MAIN)
                    onSuccess()
                }
            } catch (uiError: UIError) {

                withContext(Dispatchers.Main) {

                    // Clear data
                    that.transactions = ArrayList()
                    that.apiViewEvents.onViewLoadFailed(VIEW_MAIN, uiError)

                    // Return the error and indicate if it is an expected API error due to user actions
                    val isExpectedError = that.handleApiError(uiError)
                    onError(uiError, isExpectedError)
                }
            }
        }
    }

    /*
     * Handle 'business errors' received from the API
     */
    private fun handleApiError(error: UIError): Boolean {

        var isExpected = false

        if (error.statusCode == 404 && error.errorCode.equals(ErrorCodes.companyNotFound)) {

            // A deep link could provide an id such as 3, which is unauthorized
            isExpected = true
        } else if (error.statusCode == 400 && error.errorCode.equals(ErrorCodes.invalidCompanyId)) {

            // A deep link could provide an invalid id value such as 'abc'
            isExpected = true
        }

        return isExpected
    }
}
