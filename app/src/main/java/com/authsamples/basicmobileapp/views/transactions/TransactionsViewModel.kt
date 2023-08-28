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
import com.authsamples.basicmobileapp.views.utilities.ApiViewEvents
import com.authsamples.basicmobileapp.views.utilities.Constants.VIEW_MAIN
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
        options: ApiRequestOptions,
        onComplete: () -> Unit
    ) {

        // Initialize state
        this.apiViewEvents.onViewLoading(VIEW_MAIN)
        this.resetError()

        // Make the remote call on a background thread
        val that = this@TransactionsViewModel
        CoroutineScope(Dispatchers.IO).launch {

            try {
                // Make the API call
                val data = that.apiClient.getCompanyTransactions(that.companyId, options)
                val transactions = data.transactions.toList()

                // Return results on the main thread
                withContext(Dispatchers.Main) {
                    that.updateData(transactions, null)
                    that.apiViewEvents.onViewLoaded(VIEW_MAIN)
                    onComplete()
                }
            } catch (uiError: UIError) {

                withContext(Dispatchers.Main) {

                    that.updateData(ArrayList(), uiError)
                    that.apiViewEvents.onViewLoadFailed(VIEW_MAIN, uiError)
                    onComplete()
                }
            }
        }
    }

    /*
     * Handle 'business errors' received from the API
     */
    fun isExpectedError(): Boolean {

        var isExpected = false
        val error = this.error
        if (error != null) {

            if (error.statusCode == 404 && error.errorCode.equals(ErrorCodes.companyNotFound)) {

                // A deep link could provide an id such as 3, which is unauthorized
                isExpected = true

            } else if (error.statusCode == 400 && error.errorCode.equals(ErrorCodes.invalidCompanyId)) {

                // A deep link could provide an invalid id value such as 'abc'
                isExpected = true
            }
        }

        return isExpected
    }
    /*
     * Observable plumbing to allow XML views to register
     */
    override fun addOnPropertyChangedCallback(callback: Observable.OnPropertyChangedCallback) {
        callbacks.add(callback)
    }

    /*
     * Observable plumbing to allow XML views to unregister
     */
    override fun removeOnPropertyChangedCallback(callback: Observable.OnPropertyChangedCallback) {
        callbacks.remove(callback)
    }

    /*
     * Update data and inform the binding system
     */
    private fun updateData(transactions: List<Transaction>, error: UIError? = null) {
        this.transactionsList = transactions
        this.error = error
        callbacks.notifyCallbacks(this, 0, null)
    }

    /*
     * Clear any errors before attempting an operation
     */
    private fun resetError() {
        this.error = null
        callbacks.notifyCallbacks(this, 0, null)
    }
}
