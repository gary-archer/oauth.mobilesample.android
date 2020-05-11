package com.authguidance.basicmobileapp.views.transactions

import com.authguidance.basicmobileapp.api.client.ApiClient
import com.authguidance.basicmobileapp.api.entities.Transaction
import com.authguidance.basicmobileapp.views.utilities.ViewManager

/*
 * A simple view model class for the transactions view
 */
class TransactionsViewModel(
    val apiClientAccessor: () -> ApiClient?,
    val viewManager: ViewManager,
    val companyId: String,
    val titleFormat: String
) {
    // Data once retrieved
    var transactions: List<Transaction> = ArrayList()

    /*
     * Markup calls this method to get the title including the company id
     */
    fun getTitle(): String {
        return String.format(this.titleFormat, this.companyId)
    }
}
