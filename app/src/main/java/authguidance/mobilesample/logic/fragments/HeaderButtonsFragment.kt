package authguidance.mobilesample.logic.fragments

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import authguidance.mobilesample.databinding.FragmentHeaderButtonsBinding
import authguidance.mobilesample.R
import authguidance.mobilesample.logic.activities.MainActivity

/*
 * A simple fragment with the header buttons
 */
class HeaderButtonsFragment : Fragment() {

    private lateinit var binding: FragmentHeaderButtonsBinding
    private lateinit var mainActivity: MainActivity

    /*
     * Inflate the layout
     */
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        this.binding = FragmentHeaderButtonsBinding.inflate(inflater, container, false)
        return this.binding.root
    }

    override fun onAttach(context: Context?) {

        super.onAttach(context)
        mainActivity = context as MainActivity
        println("GJA: Got main activity")
    }

    /*
     * Wire up button click events
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // TODO: Raise events between fragments like this, via the activity
        // https://developer.android.com/training/basics/fragments/communicating

        // Ask the activity to handle the home click
        this.binding.btnHome.setOnClickListener {

            // val args = Bundle()
            // args.putString("passed_string", binding.homeTvString.text.toString())
            // mainActivity.navController.navigate(R.id.action_homeFragment_to_detailsFragment, args)

            println("GJA: button fragment onHome")
            if(mainActivity == null) {
                println("Its null")
            }
            else {
                mainActivity.onHome()
            }
        }

        // Ask the activity to handle the refresh click
        this.binding.btnRefreshData.setOnClickListener {
            println("GJA: button fragment onRefresh")
        }
    }
}
