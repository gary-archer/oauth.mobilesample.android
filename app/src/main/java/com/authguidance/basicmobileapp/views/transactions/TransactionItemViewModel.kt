@file:Suppress("SpacingAroundComma")

package com.authguidance.basicmobileapp.views.transactions

import com.authguidance.basicmobileapp.api.entities.Transaction
import java.util.Locale

/*
 * A simple view model class for the transactions view
 */
class TransactionItemViewModel(val transaction: Transaction) {

    /*
     * Return a formatted value
     */
    fun getAmountUsd(): String {
        return String.format(Locale.getDefault(),"%,d", this.transaction.amountUsd)
    }
}
