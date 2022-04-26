package com.demo2.view.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.demo2.R
import com.demo2.utilities.UTILS
import com.demo2.utilities.makeGone
import com.demo2.utilities.makeVisible
import com.demo2.view.ui.base.BaseFragment
import kotlinx.android.synthetic.main.row_history_request_of_capital_withdrawal.view.*

class CapitalWithdrawalRequestHistoryAdapter(
    context: Context,
    history: ArrayList<HistoryMt4RequestModel.Payload.History?>?,
    pipsRebateWallet: BaseFragment
) : RecyclerView.Adapter<CapitalWithdrawalRequestHistoryAdapter.MyHolder>() {

    var context = context
    var lastPosition: Int = 0
    var history = history!!

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyHolder {
        return MyHolder(
            LayoutInflater.from(context).inflate(
                R.layout.row_history_request_of_capital_withdrawal,
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return history.size
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: MyHolder, position: Int) {

        holder.itemView.tv_amount.text = "$${UTILS.parseDouble(history[position]!!.amount!!)}"
        holder.itemView.tv_date.text =
            UTILS.convertDate("yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd", history[position]!!.createdAt!!)

        holder.itemView.tv_status.let {
            if (history[position]!!.status == "1" || history[position]!!.status == "0") {
                it.text = context.getString(R.string.pending_tag)
                holder.itemView.cv_status.setCardBackgroundColor(
                    ContextCompat.getColor(
                        context,
                        R.color.pending
                    )
                )
            }
            if (history[position]!!.status == "2") {
                it.text = context.getString(R.string.approved_tag)
                holder.itemView.cv_status.setCardBackgroundColor(
                    ContextCompat.getColor(
                        context,
                        R.color.approved
                    )
                )
            }
            if (history[position]!!.status == "3") {
                it.text = context.getString(R.string.failed_tag)
                holder.itemView.cv_status.setCardBackgroundColor(
                    ContextCompat.getColor(
                        context,
                        R.color.rejected
                    )
                )
            }
            if (history[position]!!.status == "4") {
                it.text = context.getString(R.string.declined_tag)
                holder.itemView.cv_status.setCardBackgroundColor(
                    ContextCompat.getColor(
                        context,
                        R.color.rejected
                    )
                )
            }
        }

        if (position > lastPosition) {
            lastPosition = position
        }

        if (position == history.size - 1) {
            holder.itemView.viewBottomLine.makeGone()
        } else {
            holder.itemView.viewBottomLine.makeVisible()
        }
    }

    class MyHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}