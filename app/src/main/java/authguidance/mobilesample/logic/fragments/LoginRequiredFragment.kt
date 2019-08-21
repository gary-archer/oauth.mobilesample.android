package authguidance.mobilesample.logic.fragments

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import authguidance.mobilesample.R
import authguidance.mobilesample.databinding.FragmentLoginRequiredBinding
import authguidance.mobilesample.logic.activities.MainActivity

/*
 * The fragment to indicate that a login is required
 */
class LoginRequiredFragment : Fragment() {

    private lateinit var binding: FragmentLoginRequiredBinding
    private lateinit var mainActivity: MainActivity

    /*
     * Get a reference to the main activity
     */
    override fun onAttach(context: Context?) {
        super.onAttach(context)
        this.mainActivity = context as MainActivity
    }

    /*
     * Initialise the view
     */
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        this.binding = FragmentLoginRequiredBinding.inflate(inflater, container, false)
        return binding.root
    }

    /*
     * Wire up button click events to call back the activity
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        this.mainActivity.setFragmentTitle(this.getString(R.string.login_required_title))
    }
}
