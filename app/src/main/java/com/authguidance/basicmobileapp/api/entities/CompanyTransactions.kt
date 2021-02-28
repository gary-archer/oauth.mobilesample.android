package com.authguidance.basicmobileapp.api.entities

/*
 * A composite entity of a company and its transactions
 */
data class CompanyTransactions(

    val id: Int,

    val company: Company,

    val transactions: Array<Transaction>
)
