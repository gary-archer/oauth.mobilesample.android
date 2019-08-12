package authguidance.mobilesample.logic.activities

import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import authguidance.mobilesample.R
import authguidance.mobilesample.databinding.ActivityMainBinding
import authguidance.mobilesample.logic.fragments.BaseFragment
import kotlinx.android.synthetic.main.activity_main.*

/*
 * Our single activity application's activity
 */
class MainActivity : AppCompatActivity() {

    /*
     * Navigation controls accessed from fragments
     */
    lateinit var navController: NavController
    lateinit var navHostFragment: NavHostFragment


    /*
     * Navigation links
     * https://blog.usejournal.com/single-activity-app-using-android-navigation-architecture-component-1d41fb29ede6
     * https://github.com/mlostar/NavigationTutorial
     */

    private lateinit var binding: ActivityMainBinding

    /*
     * Activity creation
     */
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment

        navController = navHostFragment.navController

        // NavigationUI.setupWithNavController(binding.mainBottomNavigation, navHostFragment.navController)

        // Load data on creation
        //getData()
    }

    /*
     * Handle onHome clicks
     */
    fun onHome() {

        // TODO: Throws an exception and never gets here I think
        println("GJA: finding fragment in main activity onHome")
        val fragmentId = NavHostFragment.findNavController(nav_host_fragment).currentDestination!!.id

        // TODO: Throws an exception and never gets here I think
        val fragment = supportFragmentManager.findFragmentById(fragmentId)
        if(fragment is BaseFragment) {
            println("GJA: fragment found in main activity onHome")
            fragment.onHome()
        }
    }

    /*
     * Handle onRefresh clicks
     */
    fun onRefreshData() {

        println("GJA: finding fragment in main activity onRefreshData")
        val fragmentId = NavHostFragment.findNavController(nav_host_fragment).currentDestination!!.id
        println("fragment id is")
        val fragment = supportFragmentManager.findFragmentById(fragmentId)
        if(fragment is BaseFragment) {
            println("GJA: fragment found in main activity onRefresh")
            fragment.onRefreshData()
        }
    }

    /*
     * Do the work of calling the API
     */
    /*
    private fun getData() {

        // Make the HTTP call on a background thread
        CoroutineScope(Dispatchers.IO).launch {

            val httpClient = super.getHttpClient()
            val result = httpClient.callApi("GET", "companies", null, Array<Company>::class.java)

            // Switch back to the UI thread for rendering
            CoroutineScope(Dispatchers.Main).launch {
                renderData(result)
            }
        }
    }*/

    /*
     * Render API response data on the UI thread
     */
    /*
    private fun renderData(companies: Array<Company>) {

        // Render the company data via the adapter class
        val list = findViewById<ListView>(R.id.listCompanies);
        list.adapter = CompanyArrayAdapter(this, companies.toList())

        // When an item is tapped, move to the transactions activity
        list.onItemClickListener = AdapterView.OnItemClickListener{ parent, _, position, _ ->

            // Get the company
            val company = parent.getItemAtPosition(position) as Company

            // Move to the transactions view
            val intent = Intent(this, TransactionsActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            intent.putExtra("COMPANY_ID", company.id)
            startActivity(intent)
        }
    }*/
}
