package authguidance.mobilesample.logic.fragments

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.navigation.fragment.findNavController
import authguidance.mobilesample.R
import authguidance.mobilesample.logic.activities.MainActivity

/*
 * A simple fragment with the header buttons
 */
class HeaderButtonsFragment : Fragment() {

    private lateinit var mainActivity: MainActivity

    /*
     * Get a reference to the main activity
     */
    override fun onAttach(context: Context?) {
        super.onAttach(context)
        this.mainActivity = context as MainActivity
    }

    /*
     * Inflate the layout
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        return inflater.inflate(R.layout.fragment_header_buttons, container, false)
    }

    /*
     * Wire up button click events to call back the activity
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Navigate to the companies fragment
        val buttonHome = this.view?.findViewById<Button>(R.id.btnHome)
        buttonHome?.setOnClickListener {
            findNavController().navigate(R.id.companiesFragment)
        }

        // Handle the refresh click in the active primary fragment
        val buttonRefresh = this.view?.findViewById<Button>(R.id.btnRefreshData)
        buttonRefresh?.setOnClickListener {

            // Get the current fragment and refresh it if it implements the required interface
            val fragment = this.mainActivity.navHostFragment.childFragmentManager.primaryNavigationFragment as Fragment
            if(fragment is Refreshable) {
                fragment.refreshData()
            }
        }
    }
}
