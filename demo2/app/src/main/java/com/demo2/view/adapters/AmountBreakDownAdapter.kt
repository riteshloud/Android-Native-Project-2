package com.demo2.view.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.demo2.R
import com.demo2.utilities.UTILS
import kotlinx.android.synthetic.main.row_amount_breakdown.view.*

class AmountBreakDownAdapter(
    context: Context,
    history: List<HistoryAmountBreakDownModel.Payload.History?>?
) : RecyclerView.Adapter<AmountBreakDownAdapter.MyHolder>() {

    var context = context
    var history = history!!

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyHolder {
        return MyHolder(
            LayoutInflater.from(context).inflate(
                R.layout.row_amount_breakdown_v2,
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return history!!.size
    }

    override fun onBindViewHolder(holder: MyHolder, position: Int) {

        if (position % 2 != 0) {
            holder.itemView.setBackgroundColor(
                ContextCompat.getColor(
                    context,
                    R.color.colorPrimary
                )
            )
        } else {
            holder.itemView.setBackgroundColor(
                ContextCompat.getColor(
                    context,
                    R.color.colorPrimaryDark
                )
            )

        }
        holder.itemView.tv_commission_amount.text =
            "${UTILS.parseDouble(history[position]!!.commissionAmount!!)}"
        holder.itemView.tv_amount.text = "$${UTILS.parseDouble(history[position]!!.amount!!)}"
        holder.itemView.tv_percentage.text = "(${UTILS.parseDouble(history[position]!!.commissionPercent.toString())}%)"
        holder.itemView.tv_description.text = history[position]!!.fromUser
        holder.itemView.tv_date.text =
            UTILS.convertDate(
                "yyyy-MM-dd HH:mm:ss",
                "yyyy-MM-dd",
                history[position]!!.createdAt!!
            )

    }

    class MyHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}