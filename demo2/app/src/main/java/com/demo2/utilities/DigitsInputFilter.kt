package com.demo2.utilities

import android.text.InputFilter
import android.text.Spanned

/**
 * Created by demo on 30/11/18.
 */

class DigitsInputFilter(
    private val mMaxIntegerDigitsLength: Int,
    private val mMaxDigitsAfterLength: Int,
    private val mMax: Double
) : InputFilter {

    private val DOT = "."

    override fun filter(
        source: CharSequence,
        start: Int,
        end: Int,
        dest: Spanned,
        dstart: Int,
        dend: Int
    ): CharSequence? {
        val allText = getAllText(source, dest, dstart)
        val onlyDigitsText = getOnlyDigitsPart(allText)

        if (allText.isEmpty()) {
            return null
        } else {
            val enteredValue: Double
            try {
                enteredValue = java.lang.Double.parseDouble(onlyDigitsText)
            } catch (e: NumberFormatException) {
                return ""
            }

            return checkMaxValueRule(enteredValue, onlyDigitsText)
        }
    }


    private fun checkMaxValueRule(enteredValue: Double, onlyDigitsText: String): CharSequence? {
        return if (enteredValue > mMax) {
            ""
        } else {
            handleInputRules(onlyDigitsText)
        }
    }

    private fun handleInputRules(onlyDigitsText: String): CharSequence? {
        return if (isDecimalDigit(onlyDigitsText)) {
            checkRuleForDecimalDigits(onlyDigitsText)
        } else {
            checkRuleForIntegerDigits(onlyDigitsText.length)
        }
    }

    private fun isDecimalDigit(onlyDigitsText: String): Boolean {
        return onlyDigitsText.contains(DOT)
    }

    private fun checkRuleForDecimalDigits(onlyDigitsPart: String): CharSequence? {
        val afterDotPart =
            onlyDigitsPart.substring(onlyDigitsPart.indexOf(DOT), onlyDigitsPart.length - 1)
        return if (afterDotPart.length > mMaxDigitsAfterLength) {
            ""
        } else null
    }

    private fun checkRuleForIntegerDigits(allTextLength: Int): CharSequence? {
        return if (allTextLength > mMaxIntegerDigitsLength) {
            ""
        } else null
    }

    private fun getOnlyDigitsPart(text: String): String {
        return text.replace("[^0-9?!\\.]".toRegex(), "")
    }

    private fun getAllText(source: CharSequence, dest: Spanned, dstart: Int): String {
        var allText = ""
        if (!dest.toString().isEmpty()) {
            if (source.toString().isEmpty()) {
                allText = deleteCharAtIndex(dest, dstart)
            } else {
                allText = StringBuilder(dest).insert(dstart, source).toString()
            }
        }
        return allText
    }

    private fun deleteCharAtIndex(dest: Spanned, dstart: Int): String {
        val builder = StringBuilder(dest)
        if (dstart < builder.toString().length) {
            builder.deleteCharAt(dstart)
        }
        return builder.toString()
    }
}