package authguidance.mobilesample.logic.fragments

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import authguidance.mobilesample.R
import authguidance.mobilesample.databinding.FragmentProfileBinding
import authguidance.mobilesample.logic.activities.MainActivity

class ProfileFragment : Fragment(), BaseFragment  {

    private lateinit var binding: FragmentProfileBinding
    private lateinit var mainActivity: MainActivity

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        mainActivity = context as MainActivity
    }

    override fun onHome() {
        println("GJA: onHome not implemented in ProfileFragment")

    }

    override fun onRefreshData() {
        println("GJA: onRefreshData called in ProfilesFragment")
        val args = Bundle()
        mainActivity.navController.navigate(R.id.action_profileFragment_to_companiesFragment, args)
    }
}
