package com.authsamples.finalmobileapp.views.transactions

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.authsamples.finalmobileapp.api.cache.FetchCacheKeys
import com.authsamples.finalmobileapp.api.client.ApiClient
import com.authsamples.finalmobileapp.api.client.GetOptions
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
class TransactionsViewModel(private val apiClient: ApiClient, val eventBus: EventBus) : ViewModel() {

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
    fun callApi(options: ViewLoadOptions?, onForbidden: () -> Unit) {

        val getOptions = GetOptions(
            "${FetchCacheKeys.TRANSACTIONS}-${this.companyId}",
            options?.forceReload ?: false,
            options?.causeError ?: false,
        )

        // Initialize state
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
                val data = that.apiClient.getCompanyTransactions(that.companyId!!, getOptions)

                // Update data on the main thread
                withContext(Dispatchers.Main) {

                    if (data != null) {
                        that.updateData(data)
                    }
                }

            } catch (uiError: UIError) {

                // Handle errors on the main thread
                withContext(Dispatchers.Main) {

                    that.updateData(null)
                    if (that.isForbiddenError(uiError)) {
                        that.updateError(null)
                        onForbidden()
                    } else {
                        that.updateError(uiError)
                    }
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
