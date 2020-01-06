package com.authguidance.basicmobileapp.views.fragments

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.authguidance.basicmobileapp.databinding.FragmentErrorSummaryBinding
import com.authguidance.basicmobileapp.plumbing.errors.ErrorCodes
import com.authguidance.basicmobileapp.plumbing.errors.UIError

/*
 * The fragment to show an initial error indication
 */
class ErrorSummaryFragment : androidx.fragment.app.Fragment() {

    private lateinit var binding: FragmentErrorSummaryBinding
    private var error: UIError? = null
    private var detailsDialog: AlertDialog? = null

    /*
     * Initialise the view
     */
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        this.binding = FragmentErrorSummaryBinding.inflate(inflater, container, false)
        return binding.root
    }


    /*
     * Wire up click events
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        this.binding.errorSummaryText.setOnClickListener {
            this.onClick()
        }
    }

    /*
     * Set the title and store the details to render in a modal dialog
     */
    fun reportError(title: String, error: UIError) {

        // Record error details unless this a login is required, which is not a real error
        if (!error.errorCode.equals(ErrorCodes.loginRequired)) {
            this.binding.errorSummaryText.text = title
            this.error = error
        }


    }

    /*
     * Clear error details when required
     */
    fun clearError() {
        this.binding.errorSummaryText.text = ""
    }

    /*
     * Invoke a modal error details dialog when the red error summary text is clicked
     */
    private fun onClick() {

        val error = this.error
        if(error != null) {

            // http://technxt.net/how-to-create-a-custom-alert-dialog-in-android/
            /*final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            LayoutInflater inflater = getLayoutInflater();
            View view = inflater.inflate(R.layout.list_layout,null);
            TextView tv = (TextView)view.findViewById(R.id.head);
            ImageView iv = (ImageView)view.findViewById(R.id.iv);
            builder.setView(view);
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // Dismiss the dialog here
                    dialog.dismiss();
                }
            });
            builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // Add ok operation here
                }
            });

            builder.show();*/

            println("GJA: Error Summary: ${error.errorCode}, ${error.message}")

            val builder = AlertDialog.Builder(this.context)
            //builder.setView()
            this.detailsDialog = builder.create()
            this.detailsDialog!!.show()

            // Otherwise navigate to the error fragment and render error details
            /*val args = Bundle()
            args.putSerializable(Constants.ARG_ERROR_DATA, error as Serializable)
            NavigationHelper().navigate(
                this.navController,
                this.navHostFragment.childFragmentManager.primaryNavigationFragment,
                R.id.errorFragment,
                args)*/
        }
    }
}