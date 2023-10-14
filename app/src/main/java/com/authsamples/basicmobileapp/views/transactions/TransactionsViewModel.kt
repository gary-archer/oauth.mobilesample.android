package com.authsamples.basicmobileapp.views.transactions

import android.app.Application
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import com.authsamples.basicmobileapp.R
import com.authsamples.basicmobileapp.api.client.FetchCacheKeys
import com.authsamples.basicmobileapp.api.client.FetchClient
import com.authsamples.basicmobileapp.api.client.FetchOptions
import com.authsamples.basicmobileapp.api.entities.Transaction
import com.authsamples.basicmobileapp.plumbing.errors.ErrorCodes
import com.authsamples.basicmobileapp.plumbing.errors.UIError
import com.authsamples.basicmobileapp.views.errors.ErrorSummaryViewModelData
import com.authsamples.basicmobileapp.views.utilities.ViewLoadOptions
import com.authsamples.basicmobileapp.views.utilities.ViewModelCoordinator
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
    private val viewModelCoordinator: ViewModelCoordinator,
    val app: Application
) : AndroidViewModel(app) {

    // Data once retrieved
    var companyId: String? = null
    var transactionsList: MutableState<List<Transaction>> = mutableStateOf(ArrayList())
    var error: MutableState<UIError?> = mutableStateOf(null)
    val titleFormat = app.getString(R.string.transactions_title)

    /*
     * Markup calls this method to get the title including the company id
     */
    fun getTitle(): String {
        return String.format(this.titleFormat, this.companyId)
    }

    /*
     * A method to do the work of calling the API
     */
    fun callApi(id: String, options: ViewLoadOptions?, onForbidden: () -> Unit) {

        val fetchOptions = FetchOptions(
            "${FetchCacheKeys.TRANSACTIONS}-$id",
            options?.forceReload ?: false,
            options?.causeError ?: false
        )

        // Initialize state
        this.viewModelCoordinator.onMainViewModelLoading()
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

                // Handle errors on the main thread
                withContext(Dispatchers.Main) {

                    if (that.isForbiddenError(uiError)) {

                        // For expected errors, the view redirects back to the home view
                        onForbidden()

                    } else {

                        // Report other types of errors
                        that.updateData(ArrayList())
                        that.updateError(uiError)
                    }
                }
            } finally {

                // Inform the view once complete
                withContext(Dispatchers.Main) {
                    that.viewModelCoordinator.onMainViewModelLoaded(fetchOptions.cacheKey)
                }
            }
        }
    }

    /*
     * Handle expected unauthorized errors that can be received from the API
     */
    private fun isForbiddenError(uiError: UIError): Boolean {

        if (uiError.statusCode == 404 && uiError.errorCode == ErrorCodes.companyNotFound) {

            // A deep link could provide an id such as 3, which is unauthorized
            return true

        } else if (uiError.statusCode == 400 && uiError.errorCode == ErrorCodes.invalidCompanyId) {

            // A deep link could provide an invalid id value such as 'abc'
            return true
        }

        return false
    }

    /*
     * Data to pass when invoking the child error summary view
     */
    fun errorSummaryData(): ErrorSummaryViewModelData {

        return ErrorSummaryViewModelData(
            hyperlinkText = app.getString(R.string.transactions_error_hyperlink),
            dialogTitle = app.getString(R.string.transactions_error_dialogtitle),
            error = this.error.value
        )
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
