package authguidance.mobilesample.logic.fragments

/*
 * Enables an activity to receive header button events
 */
interface HeaderButtonClickListener {

    // Ask the activity which buttons to show
    fun showAllButtons(): Boolean

    // Move home
    fun onHome()

    // Get a data refresh from the API
    fun onRefreshData()
}