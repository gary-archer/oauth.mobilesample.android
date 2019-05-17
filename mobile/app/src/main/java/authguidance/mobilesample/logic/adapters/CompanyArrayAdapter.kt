package authguidance.mobilesample.logic.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import authguidance.mobilesample.logic.entities.Company
import kotlinx.android.synthetic.main.company_list_item.view.*
import authguidance.mobilesample.R

/*
 * An adapter to render company items in a custom manner
 */
class CompanyArrayAdapter(context: Context, companies: List<Company>) :
    ArrayAdapter<Company>(context, 0, companies) {

    /*
     * Return the view for a company item
     */
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {

        val rootView = convertView ?: LayoutInflater.from(context).inflate(R.layout.company_list_item, parent, false)
        val currentCompany = getItem(position)
        if(currentCompany != null) {

            // The first row shows the company image and name
            rootView.companyImageId.setImageResource(currentCompany.id)
            rootView.companyName.text = currentCompany.name;
            // Next we show a list of labels and values

            rootView.targetUsd.text = currentCompany.targetUsd.toString()
            rootView.investmentUsd.text = currentCompany.investmentUsd.toString()
            rootView.noInvestors.text = currentCompany.noInvestors.toString()
        }

        return rootView
    }
}