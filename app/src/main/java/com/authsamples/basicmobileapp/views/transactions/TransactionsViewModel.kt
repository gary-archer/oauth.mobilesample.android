package com.authsamples.basicmobileapp.views.transactions

import android.app.Application
import androidx.databinding.Observable
import androidx.databinding.PropertyChangeRegistry
import androidx.lifecycle.AndroidViewModel
import com.authsamples.basicmobileapp.R
import com.authsamples.basicmobileapp.api.client.ApiClient
import com.authsamples.basicmobileapp.api.client.ApiRequestOptions
import com.authsamples.basicmobileapp.api.entities.Transaction
import com.authsamples.basicmobileapp.plumbing.errors.ErrorCodes
import com.authsamples.basicmobileapp.plumbing.errors.UIError
import com.authsamples.basicmobileapp.views.errors.ErrorSummaryViewModelData
import com.authsamples.basicmobileapp.views.utilities.ApiViewEvents
import com.authsamples.basicmobileapp.views.utilities.Constants.VIEW_MAIN
import com.authsamples.basicmobileapp.views.utilities.ViewLoadOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/*
 * A simple view model class for the transactions view
 */
class TransactionsViewModel(
    val apiClient: ApiClient,
    val apiViewEvents: ApiViewEvents,
    val companyId: String,
    val app: Application
) : AndroidViewModel(app), Observable {

    // Data once retrieved
    var transactionsList: List<Transaction> = ArrayList()
    var error: UIError? = null
    val titleFormat = app.getString(R.string.transactions_title)
    private val callbacks = PropertyChangeRegistry()

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
        options: ViewLoadOptions?,
        onComplete: (isForbidden: Boolean) -> Unit
    ) {

        // Initialize state
        this.apiViewEvents.onViewLoading(VIEW_MAIN)
        this.updateData(ArrayList(), null)

        // Make the remote call on a background thread
        val that = this@TransactionsViewModel
        CoroutineScope(Dispatchers.IO).launch {

            try {
                // Make the API call
                val fetchOptions = ApiRequestOptions(options?.causeError ?: false)
                val data = that.apiClient.getCompanyTransactions(that.companyId, fetchOptions)
                val transactions = data.transactions.toList()

                // Update data on the main thread
                withContext(Dispatchers.Main) {
                    that.updateData(transactions, null)
                    that.apiViewEvents.onViewLoaded(VIEW_MAIN)
                    onComplete(false)
                }
            } catch (uiError: UIError) {

                // Handle errors on the main thread
                withContext(Dispatchers.Main) {

                    if (that.isForbiddenError(uiError)) {

                        // For expected errors, the view redirects back to the home view
                        onComplete(true)

                    } else {

                        // Report other types of errors
                        that.updateData(ArrayList(), uiError)
                        that.apiViewEvents.onViewLoadFailed(VIEW_MAIN, uiError)
                        onComplete(false)
                    }
                }
            }
        }
    }

    /*
     * Handle expected unauthorized errors that can be received from the API
     */
    private fun isForbiddenError(uiError: UIError): Boolean {

        if (uiError.statusCode == 404 && uiError.errorCode.equals(ErrorCodes.companyNotFound)) {

            // A deep link could provide an id such as 3, which is unauthorized
            return true

        } else if (uiError.statusCode == 400 && uiError.errorCode.equals(ErrorCodes.invalidCompanyId)) {

            // A deep link could provide an invalid id value such as 'abc'
            return true
        }

        return false
    }

    /*
     * Data to pass when invoking the child error summary view
     */
    fun errorSummaryViewModel(): ErrorSummaryViewModelData {

        return ErrorSummaryViewModelData(
            hyperlinkText = app.getString(R.string.transactions_error_hyperlink),
            dialogTitle = app.getString(R.string.transactions_error_dialogtitle),
            error = this.error
        )
    }

    /*
     * Observable plumbing to allow XML views to register
     */
    override fun addOnPropertyChangedCallback(callback: Observable.OnPropertyChangedCallback) {
        this.callbacks.add(callback)
    }

    /*
     * Observable plumbing to allow XML views to unregister
     */
    override fun removeOnPropertyChangedCallback(callback: Observable.OnPropertyChangedCallback) {
        this.callbacks.remove(callback)
    }

    /*
     * Update data and inform the binding system
     */
    private fun updateData(transactions: List<Transaction>, error: UIError? = null) {
        this.transactionsList = transactions
        this.error = error
        this.callbacks.notifyCallbacks(this, 0, null)
    }
}
