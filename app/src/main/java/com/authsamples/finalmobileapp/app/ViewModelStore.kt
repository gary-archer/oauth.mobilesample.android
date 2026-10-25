package com.authsamples.finalmobileapp.app

import com.authsamples.finalmobileapp.api.client.ApiClient
import com.authsamples.finalmobileapp.views.companies.CompaniesViewModel
import com.authsamples.finalmobileapp.views.transactions.TransactionsViewModel
import com.authsamples.finalmobileapp.views.userinfo.UserInfoViewModel
import org.greenrobot.eventbus.EventBus

/*
 * Manages global view model objects
 */
class ViewModelStore(private val apiClient: ApiClient, private val eventBus: EventBus) {

    private var companiesViewModel: CompaniesViewModel?
    private var transactionsViewModel: TransactionsViewModel?
    private var userInfoViewModel: UserInfoViewModel?

    init {
        this.companiesViewModel = null
        this.transactionsViewModel = null
        this.userInfoViewModel = null
    }

    /*
     * Create the view model on first use
     */
    fun getCompaniesViewModel(): CompaniesViewModel {

        if (this.companiesViewModel == null) {
            this.companiesViewModel = CompaniesViewModel(this.apiClient, this.eventBus)
        }

        return this.companiesViewModel!!
    }

    /*
     * Create the view model on first use
     */
    fun getTransactionsViewModel(): TransactionsViewModel {

        if (this.transactionsViewModel == null) {
            this.transactionsViewModel = TransactionsViewModel(this.apiClient, this.eventBus)
        }

        return this.transactionsViewModel!!
    }

    /*
     * Create the view model on first use
     */
    fun getUserInfoViewModel(): UserInfoViewModel {

        if (this.userInfoViewModel == null) {
            this.userInfoViewModel = UserInfoViewModel(this.apiClient, this.eventBus)
        }

        return this.userInfoViewModel!!
    }
}
