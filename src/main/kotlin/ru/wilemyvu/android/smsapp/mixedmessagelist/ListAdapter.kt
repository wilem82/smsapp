package ru.wilemyvu.android.smsapp.mixedmessagelist

import android.content.Context
import android.database.Cursor
import android.graphics.Color
import android.graphics.Typeface
import android.net.Uri
import android.provider.ContactsContract
import android.support.v7.widget.AppCompatTextView
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import ru.wilemyvu.android.smsapp.PermissionReq
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Date

private val newlinesRegex = Regex("[\n\r]")

class ListAdapter(
        private val ctx: Context,
        private val messageFont: Typeface?
) : BaseAdapter() {

    var maybeCursor: Cursor? = null

    private val contactsCache: MutableMap<String, String> = HashMap()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View = run {
        ((convertView as? AppCompatTextView) ?: AppCompatTextView(ctx).also {
            if (messageFont != null) it.typeface = messageFont
        }).also {
            val cursor = maybeCursor ?: return it
            cursor.moveToPosition(position)
//                            it.text = 0.until(cursor.columnCount).asSequence().map {
//                                cursor.getColumnName(it) + "=" + cursor.getString(it)
//                            }.joinToString()
            val date = formatDate(cursor.getLong(0))
            val from = cursor.getString(2).let displayName@ { phoneNum ->
                if (!PermissionReq.PhoneNumResolution.granted(ctx)) return@displayName phoneNum
                contactsCache.getOrElse(phoneNum) {
                    ctx.contentResolver.query(
                            Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNum)),
                            arrayOf(ContactsContract.PhoneLookup.DISPLAY_NAME),
                            null,
                            null,
                            null
                    )?.use queryResult@ {
                        if (it.count == 0) return@queryResult phoneNum
                        it.moveToNext()
                        it.getString(0)
                    }?.also {
                        contactsCache[phoneNum] = it
                    } ?: phoneNum
                }
            }
            val body = cursor.getString(1).trim().replace(newlinesRegex, " ")
            it.text = SpannableString("$date $from > $body").also {
                it.setSpan(ForegroundColorSpan(Color.RED), 0, date.length, Spannable.SPAN_INCLUSIVE_EXCLUSIVE)
                date.length.let { start ->
                    it.setSpan(StyleSpan(Typeface.BOLD), start, start + from.length + 1, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
                }
            }
        }
    }

    override fun getItem(position: Int): Any = "getItem-$position"
    override fun getItemId(position: Int): Long = position.toLong()
    override fun getCount(): Int = maybeCursor?.count ?: 0
}

private fun formatDate(itemUnixtime: Long): String {
    val formatter = SimpleDateFormat.getTimeInstance(DateFormat.SHORT)
    return formatter.format(Date(itemUnixtime))
}
