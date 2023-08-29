package com.authsamples.basicmobileapp.views.companies

import android.app.Application
import androidx.databinding.Observable
import androidx.databinding.PropertyChangeRegistry
import androidx.lifecycle.AndroidViewModel
import com.authsamples.basicmobileapp.R
import com.authsamples.basicmobileapp.api.client.ApiClient
import com.authsamples.basicmobileapp.api.client.ApiRequestOptions
import com.authsamples.basicmobileapp.api.entities.Company
import com.authsamples.basicmobileapp.plumbing.errors.UIError
import com.authsamples.basicmobileapp.views.errors.ErrorSummaryViewModelData
import com.authsamples.basicmobileapp.views.utilities.ApiViewEvents
import com.authsamples.basicmobileapp.views.utilities.Constants.VIEW_MAIN
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/*
 * A simple view model class for the companies view
 */
class CompaniesViewModel(
    private val apiClient: ApiClient,
    private val apiViewEvents: ApiViewEvents,
    val app: Application
) : AndroidViewModel(app), Observable {

    // Observable data
    var companiesList: List<Company> = ArrayList()
    var error: UIError? = null
    private val callbacks = PropertyChangeRegistry()

    /*
     * A method to do the work of calling the API
     */
    fun callApi(options: ApiRequestOptions, onComplete: () -> Unit,) {

        // Initialize state
        this.apiViewEvents.onViewLoading(VIEW_MAIN)
        this.updateData(ArrayList(), null)

        // Make the remote call on a background thread
        val that = this@CompaniesViewModel
        CoroutineScope(Dispatchers.IO).launch {

            try {
                // Make the API call
                val companies = apiClient.getCompanyList(options).toList()

                // Return success results on the main thread
                withContext(Dispatchers.Main) {
                    that.updateData(companies, null)
                    that.apiViewEvents.onViewLoaded(VIEW_MAIN)
                    onComplete()
                }

            } catch (uiError: UIError) {

                // Return error results on the main thread
                withContext(Dispatchers.Main) {
                    that.updateData(ArrayList(), uiError)
                    that.apiViewEvents.onViewLoadFailed(VIEW_MAIN, uiError)
                    onComplete()
                }
            }
        }
    }

    /*
     * Data to pass when invoking the child error summary view
     */
    fun errorSummaryViewModel(): ErrorSummaryViewModelData {
        return ErrorSummaryViewModelData(
            hyperlinkText = app.getString(R.string.companies_error_hyperlink),
            dialogTitle = app.getString(R.string.companies_error_dialogtitle),
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
    private fun updateData(companies: List<Company>, error: UIError? = null) {
        this.companiesList = companies
        this.error = error
        this.callbacks.notifyCallbacks(this, 0, null)
    }
}
