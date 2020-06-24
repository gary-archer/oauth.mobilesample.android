package com.authguidance.basicmobileapp.views.companies

import com.authguidance.basicmobileapp.api.entities.Company

/*
 * A simple view model class
 */
class CompanyItemViewModel(val company: Company) {

    /*
     * Return a formatted value
     */
    fun getTargetUsd(): String {
        return String.format("%,d", this.company.targetUsd)
    }

    /*
     * Return a formatted value
     */
    fun getInvestmentUsd(): String {
        return String.format("%,d", this.company.investmentUsd)
    }

    /*
     * Return a formatted value
     */
    fun getNoInvestors(): String {
        return String.format("%d", this.company.noInvestors)
    }
}
