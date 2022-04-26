package com.demo2.utilities

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.os.Bundle
import android.text.*
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ScrollView
import android.widget.TextView
import androidx.annotation.LayoutRes
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import com.bumptech.glide.Glide
import com.demo2.R
import java.util.*


inline fun <reified T : Activity> Context.start(vararg params: Pair<String, Any>) {
    val intent = Intent(this, T::class.java).apply {
        params.forEach {
            when (val value = it.second) {
                is Int -> putExtra(it.first, value)
                is String -> putExtra(it.first, value)
                is Double -> putExtra(it.first, value)
                is Float -> putExtra(it.first, value)
                is Boolean -> putExtra(it.first, value)
                is Bundle -> putExtra(it.first, value)
                else -> throw IllegalArgumentException("Wrong param type!")
            }
            return@forEach
        }
    }
    startActivity(intent)
}

class OnDebouncedClickListener(private val delayInMilliSeconds: Long, val action: () -> Unit) :
    View.OnClickListener {
    var enable = true
    override fun onClick(view: View?) {
        if (enable) {
            enable = false
            view?.postDelayed({ enable = true }, delayInMilliSeconds)
            action()
        }
    }
}

fun View.setOnMyClickListener(delayInMilliSeconds: Long = 500, action: () -> Unit) {
    val onDebouncedClickListener = OnDebouncedClickListener(delayInMilliSeconds, action)
    setOnClickListener(onDebouncedClickListener)
}

fun ViewGroup.inflate(@LayoutRes layoutRes: Int, attachToRoot: Boolean = false): View {
    return LayoutInflater.from(context).inflate(layoutRes, this, attachToRoot)
}

//Todo For Any View Visible and Gone
fun View.isVisible(): Boolean = visibility == View.VISIBLE

fun View.isGone(): Boolean = visibility == View.GONE

fun CardView.setCardColor(color: Int){
    this.setCardBackgroundColor(ContextCompat.getColor(context,color))
}

fun View.isInvisible(): Boolean = visibility == View.INVISIBLE

fun View.makeVisible() {
    visibility = View.VISIBLE
}

fun View.makeGone() {
    visibility = View.GONE
}

fun View.makeInvisible() {
    visibility = View.INVISIBLE
}

fun TextView.setCopyright() {
    val year = Calendar.getInstance().get(Calendar.YEAR)
    text = context.getString(R.string.copyright_tag)+" @"+year+" demo2 | "+context.getString(R.string.copyright_2020_demo2_all_right_reserved_tag)
}

fun ScrollView.smoothScrollToBottom(view: View){
    smoothScrollTo(0,view.top)
}

//Todo For Load Any Image
fun ImageView.loadFromUrl(url: String) {
    Glide.with(context).load(url).into(this)
}

//Todo For UpperCase First Latter
fun String.upperCaseFirstLetter(): String {
    return this.substring(0, 1).toUpperCase().plus(this.substring(1))
}

fun isPasswordValid(text: Editable?): Boolean {
    return text != null && text.trim().length >= 6
}

fun Context.isInternetAvailable(): Boolean {
    val connectivityManager =
        getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val activeNetworkInfo = connectivityManager.activeNetworkInfo
    return activeNetworkInfo != null && activeNetworkInfo.isConnected
}

fun <T> LiveData<T>.reObserve(owner: LifecycleOwner, observer: Observer<T>) {
    removeObserver(observer)
    observe(owner, observer)
}

fun <T, U> combine(first: Array<T>, second: Array<U>): Array<Any> {
    val list: MutableList<Any> = first.map { i -> i as Any }.toMutableList()
    list.addAll(second.map { i -> i as Any })
    return list.toTypedArray()
}

fun TextView.makeLinks(vararg links: Pair<String, View.OnClickListener>) {
    val spannableString = SpannableString(this.text)
    for (link in links) {
        val clickableSpan = object : ClickableSpan() {
            override fun onClick(view: View) {
                Selection.setSelection((view as TextView).text as Spannable, 0)
                view.invalidate()
                link.second.onClick(view)
            }
        }
        val startIndexOfLink = this.text.toString().indexOf(link.first)
        spannableString.setSpan(clickableSpan, startIndexOfLink, startIndexOfLink + link.first.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
    }
    this.movementMethod = LinkMovementMethod.getInstance() // without LinkMovementMethod, link can not click
    this.setText(spannableString, TextView.BufferType.SPANNABLE)
}

