package com.demo2.view.adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.demo2.R
import com.demo2.view.interfaces.OnListClickListener
import kotlinx.android.synthetic.main.row_funding_method_item.view.*

class CashWalletFundAdapter (
    context: Context,
    history: List<CashTransferHistoryModel.Payload.FundOptionItem?>?,
    onClickListener: OnListClickListener
) : RecyclerView.Adapter<CashWalletFundAdapter.MyHolder>() {

    var context = context
    var lastPosition: Int = 0
    var history = history!!
    var onListClickListener = onClickListener

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MyHolder {
        return MyHolder(
            LayoutInflater.from(context).inflate(R.layout.row_funding_method_item, parent, false)
        )
    }

    override fun getItemCount(): Int {
        return history.size
    }

    override fun onBindViewHolder(
        holder: MyHolder,
        position: Int
    ) {
        if (history[position]!!.selected) {
            holder.itemView.llItemFund.setBackgroundResource(R.drawable.bg_register_tab_selected)
        } else {
            holder.itemView.llItemFund.setBackgroundResource(android.R.color.transparent)
        }

        holder.itemView.tvMethodName.text = history[position]!!.text

        holder.itemView.setOnClickListener {
            if (!history[position]!!.selected) {

                Log.e("TAG", "onBindViewHolder: "+ history[position]!!.text+ "selected value "+history[position]!!.selected)
                onListClickListener.onListClickSimple(position, history[position]!!.value)
            }
        }
    }

    class MyHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}