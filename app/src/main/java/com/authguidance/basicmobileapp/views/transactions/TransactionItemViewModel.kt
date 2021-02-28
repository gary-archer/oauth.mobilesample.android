package com.authguidance.basicmobileapp.views.transactions

import com.authguidance.basicmobileapp.api.entities.Transaction

/*
 * A simple view model class for the transactions view
 */
class TransactionItemViewModel(val transaction: Transaction) {

    /*
     * Return a formatted value
     */
    fun getAmountUsd(): String {
        return String.format("%,d", this.transaction.amountUsd)
    }
}
