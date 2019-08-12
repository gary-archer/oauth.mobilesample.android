package authguidance.mobilesample.logic.fragments

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import authguidance.mobilesample.R
import authguidance.mobilesample.databinding.FragmentCompaniesBinding
import authguidance.mobilesample.logic.activities.MainActivity

class CompaniesFragment : Fragment(), BaseFragment {

    private lateinit var binding: FragmentCompaniesBinding
    private lateinit var mainActivity: MainActivity

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        binding = FragmentCompaniesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        mainActivity = context as MainActivity
    }

    override fun onHome() {
        println("GJA: onHome not implemented in CompaniesFragment")
    }

    override fun onRefreshData() {
        println("GJA: onRefreshData called in CompaniesFragment")
        val args = Bundle()
        mainActivity.navController.navigate(R.id.action_companiesFragment_to_profileFragment, args)
    }
}
