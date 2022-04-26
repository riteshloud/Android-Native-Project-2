package com.demo2.view.adapters

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions

import com.demo2.R
import com.demo2.utilities.makeGone
import kotlinx.android.synthetic.main.row_announcement_item.view.*


class AnnouncementAdapter(
    context: Context,
    announcementList: ArrayList<UserModel.Announcement?>?
) : RecyclerView.Adapter<AnnouncementAdapter.MyHolder>() {

    var context = context
    var announcementList = announcementList!!

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyHolder {
        return MyHolder(
            LayoutInflater.from(context).inflate(
                R.layout.row_announcement_item,
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return announcementList.size
    }

    class MyHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    override fun onBindViewHolder(holder: MyHolder, position: Int) {

        if (announcementList[position]!!.type == "text") {
            holder.itemView.llAnnouncementImage.visibility = View.GONE
            holder.itemView.llAnnouncementTv.visibility = View.VISIBLE
            //           holder.itemView.rvAnnouncementTitle.text = announcementList[position]!!.title

//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//                holder.itemView.rvAnnouncementDesc.setText(Html.fromHtml(announcementList[position]!!.header_description, Html.FROM_HTML_OPTION_USE_CSS_COLORS),
//                    TextView.BufferType.SPANNABLE)
//            }else{
//                holder.itemView.rvAnnouncementDesc.setText(Html.fromHtml(announcementList[position]!!.header_description),TextView.BufferType.SPANNABLE)
//            }

            holder.itemView.webView.loadDataWithBaseURL(
                null,
                announcementList[position]!!.header_description.toString(),
                "text/html",
                "utf-8",
                null
            )
            holder.itemView.webView.setBackgroundColor(Color.TRANSPARENT)
//            holder.itemView.rvAnnouncementDesc.text = HtmlFormatter.formatHtml(
//                HtmlFormatterBuilder().setHtml(announcementList[position]!!.header_description).setImageGetter(
//                    HtmlResImageGetter(this.context)
//                )
//            )
        } else if (announcementList[position]!!.type == "image") {
            holder.itemView.llAnnouncementImage.visibility = View.VISIBLE
            holder.itemView.llAnnouncementTv.visibility = View.GONE

            Glide.with(context).load(announcementList[position]!!.image_url!!)
                //.apply(RequestOptions().placeholder(R.drawable.bbit))
                .into(holder.itemView.imgAnnouncement)

        }


    }
}


