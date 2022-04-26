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
import com.demo2.utilities.setCardColor
import com.demo2.view.ui.base.BaseFragment
import kotlinx.android.synthetic.main.row_history_pips_rebate_wallet.view.tv_amount
import kotlinx.android.synthetic.main.row_history_pips_rebate_wallet.view.tv_date
import kotlinx.android.synthetic.main.row_history_pips_rebate_wallet.view.tv_status
import kotlinx.android.synthetic.main.row_history_pips_rebate_wallet_transfer.view.*
import kotlinx.android.synthetic.main.row_history_pips_rebate_wallet_transfer.view.cv_status
import kotlinx.android.synthetic.main.row_history_pips_rebate_wallet_transfer.view.viewBottomLine
import kotlinx.android.synthetic.main.row_history_request_of_capital_withdrawal.view.*

class CashWalletTransferHistoryAdapter(
    context: Context,
    history: ArrayList<CashTransferHistoryModel.Payload.History>,
    baseFragment: BaseFragment
) : RecyclerView.Adapter<CashWalletTransferHistoryAdapter.MyHolder>() {

    var context = context
    var lastPosition: Int = 0
    var history = history

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyHolder {
        return MyHolder(
            LayoutInflater.from(context).inflate(
                R.layout.row_history_pips_rebate_wallet_transfer,
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

        holder.itemView.tv_amount.text = "$${UTILS.parseDouble(history[position].amount!!)}"
        holder.itemView.tv_description.text = history[position].description!!
        holder.itemView.tv_date.text = UTILS.convertDate("yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd", history[position].createdAt!!)
        if(history[position].mt5_request==1){
            holder.itemView.ll_remark.visibility=View.VISIBLE

            holder.itemView.tv_remark.text = history[position].remarks

            if(history[position].finalAmount!!.toDouble()>0){
                holder.itemView.tv_amount.text =  "$" +  UTILS.parseDouble(history[position].finalAmount!!)+" ("+"$"+UTILS.parseDouble(history[position].amount!!) + ")"
            }
        }

        if (history[position].type == "0") {
            holder.itemView.tv_status.text = context.getString(R.string.admin_reduced_tag)
            holder.itemView.cv_status.setCardBackgroundColor(ContextCompat.getColor(context, R.color.reduced_tag))
        }
        if (history[position].type == "1") {
            holder.itemView.tv_status.text = context.getString(R.string.admin_added_tag)
            holder.itemView.cv_status.setCardBackgroundColor(ContextCompat.getColor(context, R.color.added_tag))
        }
        if (history[position].type == "4") {
            holder.itemView.tv_status.text = context.getString(R.string.admin_added)
            holder.itemView.cv_status.setCardBackgroundColor(ContextCompat.getColor(context, R.color.admin_added_tag))
        }

        if (history[position].type == "2") {
            holder.itemView.tv_status.text = context.getString(R.string.pending_tag)
            holder.itemView.cv_status.setCardBackgroundColor(ContextCompat.getColor(context, R.color.pending))
        }
        if (history[position].type == "3") {
            holder.itemView.tv_status.text = context.getString(R.string.rejected_tag)
            holder.itemView.cv_status.setCardBackgroundColor(ContextCompat.getColor(context, R.color.rejected_tag))
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