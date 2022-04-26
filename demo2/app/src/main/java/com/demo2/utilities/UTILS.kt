package com.demo2.utilities

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.graphics.drawable.Drawable
import android.util.Base64
import android.util.TypedValue
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.utils.ViewPortHandler
import com.demo2.R
import com.demo2.view.interfaces.OnListClickListener
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.net.URLConnection
import java.security.MessageDigest
import java.text.DecimalFormat
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec


class UTILS {
    companion object {

        private val SECOND_MILLIS = 1000
        private val MINUTE_MILLIS = 60 * SECOND_MILLIS
        private val HOUR_MILLIS = 60 * MINUTE_MILLIS
        private val DAY_MILLIS = 24 * HOUR_MILLIS

        fun dpToPixel(context: Context, dp: Int): Int {
            val r = context.resources
            return TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dp.toFloat(),
                r.displayMetrics
            ).toInt()
        }

        fun parseDouble(str: String): String {

            val nf = NumberFormat.getNumberInstance(Locale.US)
            val df = nf as DecimalFormat
            //df.applyPattern("##,##,##,##,##,##,##0.00")
            df.applyPattern("#0.00")
            val wallet = java.lang.Double.parseDouble(str)
            return df.format(wallet)

        }



        fun checkForHtml(html: String): Boolean {
            var htmlRegex = "/<([A-Za-z][A-Za-z0-9])\b[^>]>(.*?)<\\1>/"
            if (html.contains(htmlRegex)) {
                return true
            }
            return false
        }

        fun getCurrentMonth(): String {
            val date = Date()
            val timeZone = TimeZone.getTimeZone("UTC")
            val calendar = Calendar.getInstance(timeZone)
            val sdf = SimpleDateFormat("MMM")

            sdf.setTimeZone(timeZone);

            sdf.timeZone = TimeZone.getTimeZone("UTC")
            val utc = sdf.format(date)
            return sdf.format(calendar.getTime())
        }

        fun String.removeComma(): String {
            return this.replace(",", "")
        }

        fun convertDate(fromPattern: String, toPattern: String, dateString: String): String {
            return try {
                val date = SimpleDateFormat(fromPattern).parse(dateString)
                SimpleDateFormat(toPattern).format(date)
            } catch (e: Exception) {
                ""
            }

        }

        fun generateRequestBody(data: String): RequestBody {
            return data.toRequestBody()
            //   return data.toRequestBody("text/plain".toMediaTypeOrNull())
        }

        fun getUserFormattedDate(time: Long, pattern: String): String {
            try {
                val sdf = SimpleDateFormat(pattern)
                val netDate = Date(time * 1000)
                return sdf.format(netDate)
            } catch (e: Exception) {
                return e.toString()
            }
        }


        fun getTimeAgo(context: Context, time: Long): String? {
            var time = time
            if (time < 1000000000000L) {
                time *= 1000
            }

            val now = System.currentTimeMillis()
            if (time > now || time <= 0) {
                return null
            }


            val diff = now - time
            return if (diff < MINUTE_MILLIS) {
                context.getString(R.string.just_now)
            } /*else if (diff < 2 * MINUTE_MILLIS) {
                "a minute ago"
            } else if (diff < 50 * MINUTE_MILLIS) {
                (diff / MINUTE_MILLIS).toString() + " minutes ago"
            } */
            else if (diff < 90 * MINUTE_MILLIS) {
                context.getString(R.string.one_hour_ago)
            } else if (diff < 24 * HOUR_MILLIS) {
                (diff / HOUR_MILLIS).toString() + " ${context.getString(R.string.hours_ago)}"
            }/* else if (diff < 48 * HOUR_MILLIS) {
                "yesterday"
            }*/
            else if ((diff / DAY_MILLIS).toInt() <= 1) {
                context.getString(R.string.day_ago)
            } else if ((diff / DAY_MILLIS).toInt() < 365) {
                (diff / DAY_MILLIS).toString() + " ${context.getString(R.string.days_ago)}"
            } else if ((((diff / DAY_MILLIS).toInt()) / 365) < 2) {
                context.getString(R.string.year_ago)

            } else if ((((diff / DAY_MILLIS).toInt()) / 365) >= 2) {
                "${(((diff / DAY_MILLIS).toInt()) / 365).toInt()} ${context.getString(R.string.years_ago)}"
            } else {
                ""
            }
        }

        fun isImageFile(path: String): Boolean {
            val mimeType = URLConnection.guessContentTypeFromName(path)
            return mimeType != null && mimeType!!.startsWith("image")
        }

        fun isPdfFile(path: String): Boolean {
            val mimeType = URLConnection.guessContentTypeFromName(path)
            return mimeType != null && mimeType!!.startsWith("pdf")
        }

        fun loadImageInGlide(
            context: Context,
            img: String,
            progress: ProgressBar,
            imageView: ImageView
        ) {
            Glide.with(context)
                .load(img)
                .listener(object : RequestListener<Drawable> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Drawable>?,
                        isFirstResource: Boolean
                    ): Boolean {
                        progress.visibility = View.GONE
                        return false
                    }

                    override fun onResourceReady(
                        resource: Drawable?,
                        model: Any?,
                        target: Target<Drawable>?,
                        dataSource: com.bumptech.glide.load.DataSource?,
                        isFirstResource: Boolean
                    ): Boolean {
                        progress.visibility = View.GONE
                        return false
                    }
                }).into(imageView)
        }


        fun checkValidDateOrNot(checkin: String, checkOut: String): Boolean {
            val sdf = SimpleDateFormat("dd MMM, yyyy")
            val dateFrom: Date = sdf.parse(checkin)
            val dateTo: Date = sdf.parse(checkOut)
            var differenceInTime = dateTo.time - dateFrom.time
            var differenceInDate = differenceInTime / (1000 * 3600 * 24)

            if (differenceInDate < 1) {
                return false
            }
            return true
        }


        @Throws(java.lang.Exception::class)
        fun encrypt(key: ByteArray, clear: ByteArray): String? {
            val md: MessageDigest = MessageDigest.getInstance("md5")
            val digestOfPassword: ByteArray = md.digest(key)
            val skeySpec = SecretKeySpec(digestOfPassword, "AES")
            val cipher: Cipher = Cipher.getInstance("AES/ECB/PKCS7Padding")
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec)
            val encrypted: ByteArray = cipher.doFinal(clear)
            return Base64.encodeToString(encrypted, Base64.DEFAULT)
        }

        @Throws(java.lang.Exception::class)
        fun decrypt(key: String, encrypted: ByteArray): String? {
            val md = MessageDigest.getInstance("md5")
            val digestOfPassword = md.digest(key.toByteArray(charset("UTF-16LE")))
            val skeySpec =
                SecretKeySpec(digestOfPassword, "AES")
            val cipher = Cipher.getInstance("AES/ECB/PKCS7Padding")
            cipher.init(Cipher.DECRYPT_MODE, skeySpec)
            val decrypted = cipher.doFinal(encrypted)
            return String(decrypted, charset("UTF-16LE"))
        }

        fun showDialog(
            activity: Context,
            message: String,
            onListClickListener: OnListClickListener
        ) {
            val builder =
                AlertDialog.Builder(activity!!, R.style.MyDialogTheme)
            //builder.setTitle(getString(R.string.notice))
            builder.setMessage(
                message
            )
            builder.setCancelable(true)
            builder.setPositiveButton(activity.getString(R.string.yes),
                DialogInterface.OnClickListener { dialog, which ->
                    onListClickListener.onListShow(0, null)
                })
            builder.setNegativeButton(activity.getString(R.string.no),
                DialogInterface.OnClickListener { dialog, which ->
                    dialog.dismiss()
                })
            builder.create()
                .apply {

                    this.setOnShowListener {
                        this.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE)
                            .setTextColor(
                                ContextCompat.getColor(
                                    activity!!,
                                    R.color.red_dark_btn
                                )
                            )

                    }
                }.show()
        }

    }


}