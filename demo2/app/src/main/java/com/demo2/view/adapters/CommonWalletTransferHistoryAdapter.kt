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
import com.demo2.view.ui.base.BaseFragment
import kotlinx.android.synthetic.main.row_history_pips_rebate_wallet.view.tv_amount
import kotlinx.android.synthetic.main.row_history_pips_rebate_wallet.view.tv_date
import kotlinx.android.synthetic.main.row_history_pips_rebate_wallet.view.tv_status
import kotlinx.android.synthetic.main.row_history_pips_rebate_wallet_transfer.view.*

class CommonWalletTransferHistoryAdapter(
    context: Context,
    history: ArrayList<TransferFundsResponseModel.Payload.History?>?,
    pipsRebateWallet: BaseFragment
) : RecyclerView.Adapter<CommonWalletTransferHistoryAdapter.MyHolder>() {

    var context = context
    var lastPosition: Int = 0
    var history = history!!

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
        Log.e("zxczxc", "itemcount from adapter")
        return history.size
    }

    @SuppressLint("SetTextI18n")
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
        holder.itemView.tv_amount.text = "$${UTILS.parseDouble(history[position]!!.amount!!)}"
        holder.itemView.tv_description.text = history[position]!!.description!!
        holder.itemView.tv_date.text =
            UTILS.convertDate(
                "yyyy-MM-dd HH:mm:ss",
                "yyyy-MM-dd",
                history[position]!!.createdAt!!
            )

        if (history[position]!!.type == "0") {
            holder.itemView.tv_status.text = context.getString(R.string.admin_reduced_tag)
            holder.itemView.cv_status.setCardBackgroundColor(
                ContextCompat.getColor(
                    context,
                    R.color.admin_reduced
                )
            )
        }
        if (history[position]!!.type == "1") {
            holder.itemView.tv_status.text = context.getString(R.string.admin_added_tag)
            holder.itemView.cv_status.setCardBackgroundColor(
                ContextCompat.getColor(
                    context,
                    R.color.admin_added
                )
            )
        }
        if (history[position]!!.type == "2") {
            holder.itemView.tv_status.text = context.getString(R.string.admin_added)
            holder.itemView.cv_status.setCardBackgroundColor(
                ContextCompat.getColor(
                    context,
                    R.color.admin_added
                )
            )
        }


        if (position > lastPosition) {
            lastPosition = position
        }
    }

    class MyHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}