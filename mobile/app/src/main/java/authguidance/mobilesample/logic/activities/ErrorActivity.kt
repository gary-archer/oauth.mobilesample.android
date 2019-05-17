package authguidance.mobilesample.logic.activities

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView
import authguidance.mobilesample.R

/*
 * An activity to display unexpected error details
 */
class ErrorActivity : BaseActivity() {

    /*
     * Standard initialisation
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_error)

        // Customise the title
        this.title = "Error View"

        val exception = this.intent.getSerializableExtra("EXCEPTION_DATA") as Throwable
        this.renderError(exception);
    }

    /*
     * Render error items in the list view
     */
    fun renderError(error: Throwable) {

        // Set error items
        val items = mutableListOf<String>()
        items += "Message: ${this.getErrorDescription(error)}"

        // Update the adapter
        val list = findViewById<ListView>(R.id.listErrorItems);
        list.adapter = ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, items)
    }

    /*
     * Get the error message property's value
     */
    fun getErrorDescription(error: Throwable): String {

        val result = error.message
        if(result != null) {
            return result
        } else {
            return error.toString()
        }
    }
}
