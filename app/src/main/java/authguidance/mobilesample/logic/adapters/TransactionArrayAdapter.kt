package authguidance.mobilesample.logic.adapters

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import authguidance.mobilesample.R
import authguidance.mobilesample.logic.entities.Transaction
import kotlinx.android.synthetic.main.transaction_list_item.view.*

/*
 * An adapter to render transaction items
 */
class TransactionArrayAdapter(val context: Context, val transactions: List<Transaction>) : androidx.recyclerview.widget.RecyclerView.Adapter<TransactionArrayAdapter.ViewHolder>() {

    /*
     * Inflate this list item
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(this.context)
        return ViewHolder(inflater.inflate(R.layout.transaction_list_item, parent, false))
    }

    /*
     * Return the total size
     */
    override fun getItemCount(): Int {
        return this.transactions.size
    }

    /*
     * Binds an item to a view
     */
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        // TODO: Update all list views to recycler views and fix scrolling problems

        val currentTransaction = this.transactions[position]
        holder.item.transactionId.text = currentTransaction.id
        holder.item.investorId.text = currentTransaction.investorId
        holder.item.amountUsd.text = String.format("%,d", currentTransaction.amountUsd)
    }

    /*
     * Stores and recycles views as they are scrolled off screen
     */
    inner class ViewHolder(itemView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView) {
        val item = itemView
    }
}