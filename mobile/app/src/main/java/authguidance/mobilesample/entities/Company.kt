package authguidance.mobilesample.entities

/*
 * The company entity returned from the API
 */
data class Company(

    val id: Int,

    val name: String,

    val targetUsd: Int,

    val investmentUsd: Int,

    val noInvestors: Int
)