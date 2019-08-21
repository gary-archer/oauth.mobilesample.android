package authguidance.mobilesample.logic.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import authguidance.mobilesample.R
import authguidance.mobilesample.plumbing.errors.ErrorField
import kotlinx.android.synthetic.main.error_list_item.view.*

/*
 * An adapter to render error items in a custom manner
 */
class ErrorItemArrayAdapter(context: Context, items: List<ErrorField>) :
    ArrayAdapter<ErrorField>(context, 0, items) {

    /*
     * Each item in the list is an error field and value
     */
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {

        val rootView = convertView ?: LayoutInflater.from(context).inflate(R.layout.error_list_item, parent, false)
        val currentItem = getItem(position)
        if(currentItem != null) {
            rootView.errorField.text = currentItem.name
            rootView.errorValue.text = currentItem.value.toString()
        }

        return rootView
    }
}