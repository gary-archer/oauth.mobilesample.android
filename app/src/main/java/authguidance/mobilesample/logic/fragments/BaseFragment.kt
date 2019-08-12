package authguidance.mobilesample.logic.fragments

/*
 * A base fragment interface for receiving button events
 */
interface BaseFragment {

    // Move home
    fun onHome()

    // Get a data refresh from the API
    fun onRefreshData()
}