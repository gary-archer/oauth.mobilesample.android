package authguidance.mobilesample.logic.fragments

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import authguidance.mobilesample.R
import authguidance.mobilesample.databinding.FragmentHeaderButtonsBinding
import authguidance.mobilesample.logic.activities.MainActivity

/*
 * A simple fragment with the header buttons
 */
class HeaderButtonsFragment : androidx.fragment.app.Fragment() {

    private lateinit var binding: FragmentHeaderButtonsBinding
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

        // TODO: Button enablement per fragment

        this.binding = FragmentHeaderButtonsBinding.inflate(inflater, container, false)
        return this.binding.root
    }

    /*
     * Wire up button click events to call back the activity
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // When home is clicked, navigate to the companies fragment
        this.binding.btnHome.setOnClickListener {
            findNavController().navigate(R.id.companiesFragment)
        }

        // When refresh is clicked, handle the click in the active primary fragment
        this.binding.btnReloadData.setOnClickListener {

            val fragment = this.mainActivity.navHostFragment.childFragmentManager.primaryNavigationFragment as androidx.fragment.app.Fragment
            if(fragment is ReloadableFragment) {
                fragment.reloadData()
            }
        }

        // When expire access token is clicked, call the main activity
        this.binding.btnExpireAccessToken.setOnClickListener {
            this.mainActivity.expireAccessToken()
        }

        // When expire refresh token is clicked, call the main activity
        this.binding.btnExpireRefreshToken.setOnClickListener {
            this.mainActivity.expireRefreshToken()
        }

        // When logout is clicked, call the main activity
        this.binding.btnLogout.setOnClickListener {
            this.mainActivity.logout()
        }
    }
}
