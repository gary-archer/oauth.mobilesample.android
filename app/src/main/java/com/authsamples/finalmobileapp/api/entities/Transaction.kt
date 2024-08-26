package com.authsamples.finalmobileapp.api.entities

/*
 * A transaction entity returned from the API
 */
data class Transaction(

    val id: String,

    val investorId: String,

    val amountUsd: Int
)
