package com.authsamples.basicmobileapp.views.companies

import androidx.databinding.Observable
import androidx.databinding.PropertyChangeRegistry
import androidx.lifecycle.ViewModel
import com.authsamples.basicmobileapp.api.client.ApiClient
import com.authsamples.basicmobileapp.api.client.ApiRequestOptions
import com.authsamples.basicmobileapp.api.entities.Company
import com.authsamples.basicmobileapp.plumbing.errors.UIError
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
    private val apiViewEvents: ApiViewEvents
) : ViewModel(), Observable {

    // Observable data
    var companies: List<Company> = ArrayList()
    var error: UIError? = null
    private val callbacks = PropertyChangeRegistry()

    /*
     * A method to do the work of calling the API
     */
    fun callApi(options: ApiRequestOptions, onComplete: () -> Unit,) {

        // Indicate a loading state
        this.apiViewEvents.onViewLoading(VIEW_MAIN)

        // Initialize state
        var companies: List<Company> = ArrayList()
        val error = null
        this.updateData(ArrayList(), error)

        // Make the remote call on a background thread
        val that = this@CompaniesViewModel
        CoroutineScope(Dispatchers.IO).launch {

            try {
                // Make the API call
                companies = apiClient.getCompanyList(options).toList()

                // Return success results on the main thread
                withContext(Dispatchers.Main) {
                    that.updateData(companies, error)
                    that.apiViewEvents.onViewLoaded(VIEW_MAIN)
                    onComplete()
                }

            } catch (uiError: UIError) {

                // Return error results on the main thread
                withContext(Dispatchers.Main) {
                    that.updateData(companies, uiError)
                    that.apiViewEvents.onViewLoadFailed(VIEW_MAIN, uiError)
                    onComplete()
                }
            }
        }
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
    private fun updateData(companies: List<Company>, error: UIError? = null) {
        this.companies = companies
        this.error = error
        callbacks.notifyCallbacks(this, 0, null)
    }
}
