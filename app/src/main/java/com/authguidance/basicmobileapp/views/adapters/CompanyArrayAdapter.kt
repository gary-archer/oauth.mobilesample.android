package com.authguidance.basicmobileapp.views.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.authguidance.basicmobileapp.api.entities.Company
import com.authguidance.basicmobileapp.R
import kotlinx.android.synthetic.main.company_list_item.view.*

/*
 * An adapter to render company items in a custom manner
 */
class CompanyArrayAdapter(
    val context: Context,
    val companies: List<Company>,
    val listener: (Company) -> Unit
) : RecyclerView.Adapter<CompanyArrayAdapter.ViewHolder>() {

    /*
     * Inflate this list item
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(this.context)
        return ViewHolder(inflater.inflate(R.layout.company_list_item, parent, false))
    }

    /*
     * Return the total size
     */
    override fun getItemCount(): Int {
        return this.companies.size
    }

    /*
     * Binds an item to a view
     */
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val currentCompany = this.companies[position]

        // First set the image and text
        val id = this.context.resources.getIdentifier("company_${currentCompany.id}", "drawable", context.packageName)
        holder.item.companyImageId.setImageResource(id)
        holder.item.companyName.text = currentCompany.name

        // Next we show a list of labels and values
        holder.item.targetUsd.text = String.format("%,d", currentCompany.targetUsd)
        holder.item.investmentUsd.text = String.format("%,d", currentCompany.investmentUsd)
        holder.item.noInvestors.text = String.format("%d", currentCompany.noInvestors)

        // Handle the click event
        holder.onClick(currentCompany, this.listener)
    }

    /*
     * Stores and recycles views as they are scrolled off screen
     */
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val item = itemView

        fun onClick(company: Company, listener: (Company) -> Unit) = with(itemView) {
            setOnClickListener { listener(company) }
        }
    }
}