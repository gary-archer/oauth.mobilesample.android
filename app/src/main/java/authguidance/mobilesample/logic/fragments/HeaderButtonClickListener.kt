package authguidance.mobilesample.logic.fragments

/*
 * Enables our main activity to receive events from the header buttons fragment
 */
interface HeaderButtonClickListener {

    // Move home
    fun onHome()

    // Get a data refresh from the API
    fun onRefreshData()
}