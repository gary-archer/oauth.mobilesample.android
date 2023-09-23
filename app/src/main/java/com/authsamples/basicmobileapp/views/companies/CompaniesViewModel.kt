package com.authsamples.basicmobileapp.views.companies

import android.app.Application
import androidx.databinding.Observable
import androidx.databinding.PropertyChangeRegistry
import androidx.lifecycle.AndroidViewModel
import com.authsamples.basicmobileapp.R
import com.authsamples.basicmobileapp.api.client.FetchCacheKeys
import com.authsamples.basicmobileapp.api.client.FetchClient
import com.authsamples.basicmobileapp.api.client.FetchOptions
import com.authsamples.basicmobileapp.api.entities.Company
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
 * A simple view model class for the companies view
 */
class CompaniesViewModel(
    private val fetchClient: FetchClient,
    val eventBus: EventBus,
    private val viewModelCoordinator: ViewModelCoordinator,
    val app: Application
) : AndroidViewModel(app), Observable {

    var companiesList: List<Company> = ArrayList()
    var error: UIError? = null
    private val callbacks = PropertyChangeRegistry()

    /*
     * A method to do the work of calling the API
     */
    fun callApi(options: ViewLoadOptions?, onComplete: () -> Unit) {

        val fetchOptions = FetchOptions(
            FetchCacheKeys.COMPANIES,
            options?.forceReload ?: false,
            options?.causeError ?: false
        )

        // Initialize state
        this.viewModelCoordinator.onMainViewModelLoading()
        this.updateError(null)

        // Make the remote call on a background thread
        val that = this@CompaniesViewModel
        CoroutineScope(Dispatchers.IO).launch {

            try {

                // Make the API call
                val companies = fetchClient.getCompanyList(fetchOptions)

                // Return success results on the main thread
                withContext(Dispatchers.Main) {

                    if (companies != null) {
                        that.updateData(companies.toList())
                    }
                }

            } catch (uiError: UIError) {

                // Return error results on the main thread
                withContext(Dispatchers.Main) {
                    that.updateData(ArrayList())
                    that.updateError(uiError)
                }

            } finally {

                // Inform the view once complete
                withContext(Dispatchers.Main) {
                    that.viewModelCoordinator.onMainViewModelLoaded(fetchOptions.cacheKey)
                    onComplete()
                }
            }
        }
    }

    /*
     * Data to pass when invoking the child error summary view
     */
    fun errorSummaryData(): ErrorSummaryViewModelData {
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
    private fun updateData(companies: List<Company>) {
        this.companiesList = companies
        this.callbacks.notifyCallbacks(this, 0, null)
    }

    /*
     * Update the error state and inform the binding system
     */
    private fun updateError(error: UIError?) {
        this.error = error
        this.callbacks.notifyCallbacks(this, 0, null)
    }
}
