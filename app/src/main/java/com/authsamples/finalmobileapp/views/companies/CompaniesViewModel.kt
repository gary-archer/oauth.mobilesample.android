package com.authsamples.finalmobileapp.views.companies

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.authsamples.finalmobileapp.api.client.FetchCacheKeys
import com.authsamples.finalmobileapp.api.client.FetchClient
import com.authsamples.finalmobileapp.api.client.FetchOptions
import com.authsamples.finalmobileapp.api.entities.Company
import com.authsamples.finalmobileapp.plumbing.errors.UIError
import com.authsamples.finalmobileapp.views.utilities.ViewLoadOptions
import com.authsamples.finalmobileapp.views.utilities.ViewModelCoordinator
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
    private val viewModelCoordinator: ViewModelCoordinator
) : ViewModel() {

    var companiesList: MutableState<List<Company>> = mutableStateOf(ArrayList())
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
     * Update data used by the binding system
     */
    private fun updateData(companies: List<Company>) {
        this.companiesList.value = companies
    }

    /*
     * Update error state used by the binding system
     */
    private fun updateError(error: UIError?) {
        this.error.value = error
    }
}
