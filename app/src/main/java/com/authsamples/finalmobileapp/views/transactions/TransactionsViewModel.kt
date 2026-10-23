package com.authsamples.finalmobileapp.views.transactions

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.authsamples.finalmobileapp.api.client.FetchCacheKeys
import com.authsamples.finalmobileapp.api.client.FetchClient
import com.authsamples.finalmobileapp.api.client.FetchOptions
import com.authsamples.finalmobileapp.api.entities.Transaction
import com.authsamples.finalmobileapp.plumbing.errors.ErrorCodes
import com.authsamples.finalmobileapp.plumbing.errors.UIError
import com.authsamples.finalmobileapp.views.utilities.ViewLoadOptions
import com.authsamples.finalmobileapp.api.coordination.ApiRequestCoordinator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.greenrobot.eventbus.EventBus

/*
 * A simple view model class for the transactions view
 */
class TransactionsViewModel(
    private val fetchClient: FetchClient,
    val eventBus: EventBus,
    private val apiRequestCoordinator: ApiRequestCoordinator,
) : ViewModel() {

    // Data once retrieved
    var companyId: String? = null
    var transactionsList: MutableState<List<Transaction>> = mutableStateOf(ArrayList())
    var error: MutableState<UIError?> = mutableStateOf(null)

    /*
     * A method to do the work of calling the API
     */
    fun callApi(id: String, options: ViewLoadOptions?, onForbidden: () -> Unit) {

        val fetchOptions = FetchOptions(
            "${FetchCacheKeys.TRANSACTIONS}-$id",
            options?.forceReload ?: false,
            options?.causeError ?: false,
        )

        // Initialize state
        this.apiRequestCoordinator.onApiRequestStart(fetchOptions.cacheKey)
        this.updateError(null)
        if (this.companyId != id) {
            this.companyId = id
            this.updateData(ArrayList())
        }

        // Make the remote call on a background thread
        val that = this@TransactionsViewModel
        CoroutineScope(Dispatchers.IO).launch {

            try {
                // Make the API call
                val data = that.fetchClient.getCompanyTransactions(that.companyId!!, fetchOptions)

                // Update data on the main thread
                withContext(Dispatchers.Main) {

                    if (data != null) {
                        that.updateData(data.transactions.toList())
                    }
                }

            } catch (uiError: UIError) {

                withContext(Dispatchers.Main) {

                    // Handle errors on the main thread
                    that.updateData(ArrayList())
                    that.updateError(uiError)

                    // Notify the view if this is a forbidden error
                    if (that.isForbiddenError(uiError)) {
                        onForbidden()
                    }
                }
            } finally {

                // Inform the view once complete
                withContext(Dispatchers.Main) {
                    that.apiRequestCoordinator.onApiRequestEnd()
                }
            }
        }
    }

    /*
     * Handle expected unauthorized errors that can be received from the API
     */
    private fun isForbiddenError(uiError: UIError): Boolean {

        if (uiError.statusCode == 404 && uiError.errorCode == ErrorCodes.COMPANY_NOT_FOUND) {

            // A deep link could provide an id such as 3, which is unauthorized
            return true

        } else if (uiError.statusCode == 400 && uiError.errorCode == ErrorCodes.INVALID_COMPANY_ID) {

            // A deep link could provide an invalid id value such as 'abc'
            return true
        }

        return false
    }

    /*
     * Update the data used by the binding system
     */
    private fun updateData(transactions: List<Transaction>) {
        this.transactionsList.value = transactions
    }

    /*
     * Update the error state used  by the binding system
     */
    private fun updateError(error: UIError?) {
        this.error.value = error
    }
}
