package com.authsamples.basicmobileapp.views.companies

import android.app.Application
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
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
) : AndroidViewModel(app) {

    var companiesList: MutableState<List<Company>?> = mutableStateOf(ArrayList())
    var error: MutableState<UIError?> = mutableStateOf(null)

    /*
     * A method to do the work of calling the API
     */
    fun callApi(options: ViewLoadOptions?) {

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
            error = this.error.value
        )
    }

    /*
     * Update data and inform the binding system
     */
    private fun updateData(companies: List<Company>) {
        this.companiesList.value = companies
    }

    /*
     * Update the error state and inform the binding system
     */
    private fun updateError(error: UIError?) {
        this.error.value = error
    }

    /*
     * Make error details available to the view
     */
    fun errorData(): UIError? {
        return this.error.value
    }
}
