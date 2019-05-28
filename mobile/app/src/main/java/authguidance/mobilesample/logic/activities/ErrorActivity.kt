package authguidance.mobilesample.logic.activities

import android.os.Bundle
import android.widget.ListView
import authguidance.mobilesample.R
import authguidance.mobilesample.logic.adapters.ErrorItemArrayAdapter
import authguidance.mobilesample.logic.fragments.HeaderButtonClickListener
import authguidance.mobilesample.plumbing.errors.ErrorField
import authguidance.mobilesample.plumbing.errors.UIError

/*
 * An activity to display unexpected error details
 */
class ErrorActivity : BaseActivity(), HeaderButtonClickListener {

    /*
     * Standard initialisation
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_error)

        // Get the error details
        val error = this.intent.getSerializableExtra("EXCEPTION_DATA") as UIError
        this.renderData(error)
    }

    /*
     * Override the base view to only show the home button
     */
    override fun showAllButtons(): Boolean {
        return false
    }

    /*
     * Render error items in the list view
     */
    private fun renderData(error: UIError) {

        val items = this.getErrorItemList(error)

        // Render the company data via the adapter class
        val list = findViewById<ListView>(R.id.listErrorItems)
        list.adapter = ErrorItemArrayAdapter(this, items.toList())
    }

    /*
     * Translate the error object into an object list to be rendered in a list view
     */
    private fun getErrorItemList(error: UIError): List<ErrorField> {

        val result = ArrayList<ErrorField>()

        // TODO: This should be rendered differently in a larger font
        if (!error.message.isNullOrBlank()) {
            result.add(ErrorField("UserMessage", error.message))
        }

        if (!error.area.isNullOrBlank()) {
            result.add(ErrorField("Area", error.area))
        }

        if (!error.errorCode.isNullOrBlank()) {
            result.add(ErrorField("Error Code", error.errorCode))
        }

        if (!error.utcTime.isNullOrBlank()) {
            result.add(ErrorField("UTC Time", error.utcTime))
        }

        if (error.statusCode != 0) {
            result.add(ErrorField("Status Code", error.statusCode.toString()))
        }

        if (error.instanceId != 0) {
            result.add(ErrorField("Instance Id", error.instanceId.toString()))
        }

        if (!error.details.isNullOrBlank()) {
            result.add(ErrorField("Details", error.details))
        }

        if (!error.url.isNullOrBlank()) {
            result.add(ErrorField("URL", error.url))
        }

        // TODO: Url and stack trace
        return result
    }
}
