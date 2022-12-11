@file:Suppress("SpacingAroundComma")

package com.authsamples.basicmobileapp.views.companies

import com.authsamples.basicmobileapp.api.entities.Company
import java.util.Locale

/*
 * A simple view model class
 */
class CompanyItemViewModel(val company: Company) {

    /*
     * Return a formatted value
     */
    fun getTargetUsd(): String {
        return String.format(Locale.getDefault(),"%,d", this.company.targetUsd)
    }

    /*
     * Return a formatted value
     */
    fun getInvestmentUsd(): String {
        return String.format(Locale.getDefault(),"%,d", this.company.investmentUsd)
    }

    /*
     * Return a formatted value
     */
    fun getNoInvestors(): String {
        return String.format(Locale.getDefault(), "%d", this.company.noInvestors)
    }
}
