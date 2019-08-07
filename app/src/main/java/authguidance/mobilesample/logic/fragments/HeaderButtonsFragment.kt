package authguidance.mobilesample.logic.fragments

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import authguidance.mobilesample.R

/*
 * A simple fragment with the header buttons
 */
class HeaderButtonsFragment : Fragment() {

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

        if(this.activity is HeaderButtonClickListener) {

            val activity = this.activity as HeaderButtonClickListener

            // Ask the activity to handle the home click
            val buttonHome = this.view?.findViewById<Button>(R.id.btnHome)
            buttonHome?.setOnClickListener {
                activity.onHome()
            }

            // Ask the activity to handle the refresh click
            val buttonRefresh = this.view?.findViewById<Button>(R.id.btnRefreshData)
            buttonRefresh?.setOnClickListener {
                activity.onRefreshData()
            }
        }
    }
}
