package com.authsamples.finalmobileapp.views.transactions

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.authsamples.finalmobileapp.api.client.FetchCacheKeys
import com.authsamples.finalmobileapp.api.client.FetchClient
import com.authsamples.finalmobileapp.api.client.FetchOptions
import com.authsamples.finalmobileapp.api.coordination.ApiRequestCoordinator
import com.authsamples.finalmobileapp.api.entities.CompanyTransactions
import com.authsamples.finalmobileapp.plumbing.errors.ErrorCodes
import com.authsamples.finalmobileapp.plumbing.errors.UIError
import com.authsamples.finalmobileapp.views.utilities.ViewLoadOptions
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

    private var companyId: String? = null
    private var companyWasChanged = false
    var data: MutableState<CompanyTransactions?> = mutableStateOf(null)
    var error: MutableState<UIError?> = mutableStateOf(null)

    /*
     * Allow the company ID to be updated during navigation
     */
    fun setCompanyId(companyId: String) {

        if (companyId != this.companyId) {
            this.companyWasChanged = true
        }

        this.companyId = companyId
    }

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
        if (this.companyWasChanged) {
            this.updateData(null)
            this.companyWasChanged = false
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
                        that.updateData(data)
                    }
                }

            } catch (uiError: UIError) {

                withContext(Dispatchers.Main) {

                    // Handle errors on the main thread
                    that.updateData(null)
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
    private fun updateData(data: CompanyTransactions?) {
        this.data.value = data
    }

    /*
     * Update the error state used  by the binding system
     */
    private fun updateError(error: UIError?) {
        this.error.value = error
    }
}
