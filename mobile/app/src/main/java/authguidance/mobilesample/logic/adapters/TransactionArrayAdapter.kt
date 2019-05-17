package authguidance.mobilesample.logic.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import authguidance.mobilesample.R
import authguidance.mobilesample.logic.entities.Transaction
import kotlinx.android.synthetic.main.transaction_list_item.view.*

/*
 * An adapter to render transaction items in a custom manner
 */
class TransactionArrayAdapter(context: Context, transactions: List<Transaction>) :
    ArrayAdapter<Transaction>(context, 0, transactions) {

    /*
     * Return the view for a company item
     */
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {

        val rootView = convertView ?: LayoutInflater.from(context).inflate(R.layout.transaction_list_item, parent, false)
        val currentTransaction = getItem(position)
        if(currentTransaction != null) {

            // Show a list of labels and values
            rootView.transactionId.text = currentTransaction.id
            rootView.investorId.text = currentTransaction.investorId
            rootView.amountUsd.text = currentTransaction.amountUsd.toString()
        }

        return rootView
    }
}