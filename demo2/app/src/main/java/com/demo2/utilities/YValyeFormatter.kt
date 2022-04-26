package com.demo2.utilities

import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.utils.ViewPortHandler
import java.text.DecimalFormat

class YValyeFormatter : ValueFormatter() {
    private val mFormat: DecimalFormat


    override fun getFormattedValue(value: Float): String {
        if (value == 0.0f)
            return "";
        return mFormat.format(value).toString()
    }

    init {
        mFormat = DecimalFormat("###,###,##0.00") // use one decimal
    }
}

