package com.demo2.view.service

import android.app.Activity
import android.widget.Toast
import com.demo2.R
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

/**
 * Created by user on 11/5/2017.
 */


class ConnectivityInterceptor(private val mContext: Activity) : Interceptor {

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        if (!NetworkUtil.isInternetAvailable(mContext)) {

            mContext.runOnUiThread {
                Toast.makeText(
                    mContext,
                    "" + mContext.getString(R.string.no_internet_connection),
                    Toast.LENGTH_SHORT
                ).show()
            }
            throw NetworkUtil.NoConnectivityException()
        }
        val builder = chain.request().newBuilder()
        return chain.proceed(builder.build())
    }

}

