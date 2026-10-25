package com.authsamples.finalmobileapp.app

import com.authsamples.finalmobileapp.api.client.FetchClient
import com.authsamples.finalmobileapp.api.coordination.ApiRequestCoordinator
import com.authsamples.finalmobileapp.views.companies.CompaniesViewModel
import com.authsamples.finalmobileapp.views.transactions.TransactionsViewModel
import com.authsamples.finalmobileapp.views.userinfo.UserInfoViewModel
import org.greenrobot.eventbus.EventBus

/*
 * Manages global view model objects
 */
class ViewModelStore(
    private val fetchClient: FetchClient,
    private val apiRequestCoordinator: ApiRequestCoordinator,
    private val eventBus: EventBus,

) {

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

            this.companiesViewModel = CompaniesViewModel(
                this.fetchClient,
                this.eventBus,
                this.apiRequestCoordinator,
            )
        }

        return this.companiesViewModel!!
    }

    /*
     * Create the view model on first use
     */
    fun getTransactionsViewModel(): TransactionsViewModel {

        if (this.transactionsViewModel == null) {

            this.transactionsViewModel = TransactionsViewModel(
                this.fetchClient,
                this.eventBus,
                this.apiRequestCoordinator,
            )
        }

        return this.transactionsViewModel!!
    }

    /*
     * Create the view model on first use
     */
    fun getUserInfoViewModel(): UserInfoViewModel {

        if (this.userInfoViewModel == null) {

            this.userInfoViewModel = UserInfoViewModel(
                this.fetchClient,
                this.eventBus,
                this.apiRequestCoordinator,
            )
        }

        return this.userInfoViewModel!!
    }
}
