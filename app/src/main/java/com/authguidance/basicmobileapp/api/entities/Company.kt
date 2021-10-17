package com.authguidance.basicmobileapp.api.entities

/*
 * A company entity returned from the API
 */
data class Company(

    val id: Int,

    val name: String,

    val region: String,

    val targetUsd: Int,

    val investmentUsd: Int,

    val noInvestors: Int
)
