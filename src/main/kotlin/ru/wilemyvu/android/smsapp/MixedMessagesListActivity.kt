package ru.wilemyvu.android.smsapp

import android.app.Activity
import android.app.LoaderManager
import android.content.CursorLoader
import android.content.Loader
import android.database.Cursor
import android.graphics.Color
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.provider.ContactsContract.PhoneLookup
import android.provider.Telephony
import android.support.v4.content.ContextCompat
import android.support.v4.provider.FontRequest
import android.support.v4.provider.FontsContractCompat
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.util.Base64
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.TextView
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Date

private const val TAG = "smsapp"

private const val SMS_LOADER = 1

private val newlinesRegex = Regex("[\n\r]")

private val googleFontsDevCert = listOf(listOf(Base64.decode(
        "MIIEqDCCA5CgAwIBAgIJANWFuGx90071MA0GCSqGSIb3DQEBBAUAMIGUMQswCQYDVQQGEwJVUzETMBEGA1UECBMKQ2FsaWZvcm5pYTEWMBQGA1UEBxMNTW91bnRhaW4gVmlldzEQMA4GA1UEChMHQW5kcm9pZDEQMA4GA1UECxMHQW5kcm9pZDEQMA4GA1UEAxMHQW5kcm9pZDEiMCAGCSqGSIb3DQEJARYTYW5kcm9pZEBhbmRyb2lkLmNvbTAeFw0wODA0MTUyMzM2NTZaFw0zNTA5MDEyMzM2NTZaMIGUMQswCQYDVQQGEwJVUzETMBEGA1UECBMKQ2FsaWZvcm5pYTEWMBQGA1UEBxMNTW91bnRhaW4gVmlldzEQMA4GA1UEChMHQW5kcm9pZDEQMA4GA1UECxMHQW5kcm9pZDEQMA4GA1UEAxMHQW5kcm9pZDEiMCAGCSqGSIb3DQEJARYTYW5kcm9pZEBhbmRyb2lkLmNvbTCCASAwDQYJKoZIhvcNAQEBBQADggENADCCAQgCggEBANbOLggKv+IxTdGNs8/TGFy0PTP6DHThvbbR24kT9ixcOd9W+EaBPWW+wPPKQmsHxajtWjmQwWfna8mZuSeJS48LIgAZlKkpFeVyxW0qMBujb8X8ETrWy550NaFtI6t9+u7hZeTfHwqNvacKhp1RbE6dBRGWynwMVX8XW8N1+UjFaq6GCJukT4qmpN2afb8sCjUigq0GuMwYXrFVee74bQgLHWGJwPmvmLHC69EH6kWr22ijx4OKXlSIx2xT1AsSHee70w5iDBiK4aph27yH3TxkXy9V89TDdexAcKk/cVHYNnDBapcavl7y0RiQ4biu8ymM8Ga/nmzhRKya6G0cGw8CAQOjgfwwgfkwHQYDVR0OBBYEFI0cxb6VTEM8YYY6FbBMvAPyT+CyMIHJBgNVHSMEgcEwgb6AFI0cxb6VTEM8YYY6FbBMvAPyT+CyoYGapIGXMIGUMQswCQYDVQQGEwJVUzETMBEGA1UECBMKQ2FsaWZvcm5pYTEWMBQGA1UEBxMNTW91bnRhaW4gVmlldzEQMA4GA1UEChMHQW5kcm9pZDEQMA4GA1UECxMHQW5kcm9pZDEQMA4GA1UEAxMHQW5kcm9pZDEiMCAGCSqGSIb3DQEJARYTYW5kcm9pZEBhbmRyb2lkLmNvbYIJANWFuGx90071MAwGA1UdEwQFMAMBAf8wDQYJKoZIhvcNAQEEBQADggEBABnTDPEF+3iSP0wNfdIjIz1AlnrPzgAIHVvXxunW7SBrDhEglQZBbKJEk5kT0mtKoOD1JMrSu1xuTKEBahWRbqHsXclaXjoBADb0kkjVEJu/Lh5hgYZnOjvlba8Ld7HCKePCVePoTJBdI4fvugnL8TsgK05aIskyY0hKI9L8KfqfGTl1lzOv2KoWD0KWwtAWPoGChZxmQ+nBli+gwYMzM1vAkP+aayLe0a1EQimlOalO762r0GXO0ks+UeXde2Z4e+8S/pf7pITEI/tP+MxJTALw9QUWEv9lKTk+jkbqxbsh8nfBUapfKqYn0eidpwq2AzVp3juYl7//fKnaPhJD9gs=",
        Base64.DEFAULT)))

private val googleFontsProdCert = listOf(listOf(Base64.decode(
        "MIIEQzCCAyugAwIBAgIJAMLgh0ZkSjCNMA0GCSqGSIb3DQEBBAUAMHQxCzAJBgNVBAYTAlVTMRMwEQYDVQQIEwpDYWxpZm9ybmlhMRYwFAYDVQQHEw1Nb3VudGFpbiBWaWV3MRQwEgYDVQQKEwtHb29nbGUgSW5jLjEQMA4GA1UECxMHQW5kcm9pZDEQMA4GA1UEAxMHQW5kcm9pZDAeFw0wODA4MjEyMzEzMzRaFw0zNjAxMDcyMzEzMzRaMHQxCzAJBgNVBAYTAlVTMRMwEQYDVQQIEwpDYWxpZm9ybmlhMRYwFAYDVQQHEw1Nb3VudGFpbiBWaWV3MRQwEgYDVQQKEwtHb29nbGUgSW5jLjEQMA4GA1UECxMHQW5kcm9pZDEQMA4GA1UEAxMHQW5kcm9pZDCCASAwDQYJKoZIhvcNAQEBBQADggENADCCAQgCggEBAKtWLgDYO6IIrgqWbxJOKdoR8qtW0I9Y4sypEwPpt1TTcvZApxsdyxMJZ2JORland2qSGT2y5b+3JKkedxiLDmpHpDsz2WCbdxgxRczfey5YZnTJ4VZbH0xqWVW/8lGmPav5xVwnIiJS6HXk+BVKZF+JcWjAsb/GEuq/eFdpuzSqeYTcfi6idkyugwfYwXFU1+5fZKUaRKYCwkkFQVfcAs1fXA5V+++FGfvjJ/CxURaSxaBvGdGDhfXE28LWuT9ozCl5xw4Yq5OGazvV24mZVSoOO0yZ31j7kYvtwYK6NeADwbSxDdJEqO4k//0zOHKrUiGYXtqw/A0LFFtqoZKFjnkCAQOjgdkwgdYwHQYDVR0OBBYEFMd9jMIhF1Ylmn/Tgt9r45jk14alMIGmBgNVHSMEgZ4wgZuAFMd9jMIhF1Ylmn/Tgt9r45jk14aloXikdjB0MQswCQYDVQQGEwJVUzETMBEGA1UECBMKQ2FsaWZvcm5pYTEWMBQGA1UEBxMNTW91bnRhaW4gVmlldzEUMBIGA1UEChMLR29vZ2xlIEluYy4xEDAOBgNVBAsTB0FuZHJvaWQxEDAOBgNVBAMTB0FuZHJvaWSCCQDC4IdGZEowjTAMBgNVHRMEBTADAQH/MA0GCSqGSIb3DQEBBAUAA4IBAQBt0lLO74UwLDYKqs6Tm8/yzKkEu116FmH4rkaymUIE0P9KaMftGlMexFlaYjzmB2OxZyl6euNXEsQH8gjwyxCUKRJNexBiGcCEyj6z+a1fuHHvkiaai+KL8W1EyNmgjmyy8AW7P+LLlkR+ho5zEHatRbM/YAnqGcFh5iZBqpknHf1SKMXFh4dd239FJ1jWYfbMDMy3NS5CTMQ2XFI1MvcyUTdZPErjQfTbQe3aDQsQcafEQPD+nqActifKZ0Np0IS9L9kR/wbNvyz6ENwPiTrjV2KRkEjH78ZMcUQXg0L3BYHJ3lc69Vs5Ddf9uUGGMYldX3WfMBEmh/9iFBDAaTCK",
        Base64.DEFAULT)))

class MixedMessagesListActivity : Activity() {

    private val contactsCache: MutableMap<String, String> = HashMap()

    private var haveContactsPermission = false
    private var flattenMultiline = false

    private var messageFont: Typeface? = null

    private lateinit var messagesListView: ListView

    override fun onStart() {
        super.onStart()

        haveContactsPermission = ContextCompat.checkSelfPermission(
                applicationContext,
                android.Manifest.permission.READ_CONTACTS
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED

        flattenMultiline = true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val ctx = applicationContext

        val messagesListViewAdapter = object : BaseAdapter() {
            var maybeCursor: Cursor? = null

            override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View = run {
                ((convertView as? TextView) ?: TextView(ctx).also {
                    if (messageFont != null) it.typeface = messageFont
                }).also {
                    maybeCursor?.let { cursor ->
                        cursor.moveToPosition(position)
//                            it.text = 0.until(cursor.columnCount).asSequence().map {
//                                cursor.getColumnName(it) + "=" + cursor.getString(it)
//                            }.joinToString()
                        val date = formatDate(cursor.getLong(0))
                        val from = cursor.getString(2).let displayName@ { phoneNum ->
                            if (!this@MixedMessagesListActivity.haveContactsPermission) return@displayName phoneNum
                            contactsCache.getOrElse(phoneNum) {
                                this@MixedMessagesListActivity.contentResolver.query(
                                        Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNum)),
                                        arrayOf(PhoneLookup.DISPLAY_NAME),
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
            }

            override fun getItem(position: Int): Any = "getItem-$position"
            override fun getItemId(position: Int): Long = position.toLong()
            override fun getCount(): Int = maybeCursor?.count ?: 0
        }
        messagesListView = ListView(ctx).also {
            it.adapter = messagesListViewAdapter
        }

        @Suppress("UNCHECKED_CAST")
        fun <T : View> ListView.iterateChildren(): Sequence<T> = object : Iterator<T> {
            private var i = -1
            override fun hasNext(): Boolean = i < this@iterateChildren.childCount - 1
            override fun next(): T = this@iterateChildren.getChildAt(++i) as T
        }.asSequence()

        if (false) {
            FontRequest(
                    "com.google.android.gms.fonts",
                    "com.google.android.gms",
                    "PT+Mono",
                    googleFontsProdCert
            ).let { fontRequest ->
                FontsContractCompat.requestFont(
                        this,
                        fontRequest,
                        object : FontsContractCompat.FontRequestCallback() {
                            override fun onTypefaceRetrieved(typeface: Typeface) {
                                messageFont = typeface
                                messagesListView.iterateChildren<TextView>().forEach { it.typeface = typeface }
                            }

                            override fun onTypefaceRequestFailed(reason: Int) {
                                Log.e(TAG, "Failed to download the font $fontRequest, reason=$reason")
                            }
                        },
                        Handler(HandlerThread("fonts").also {
                            it.start()
                        }.looper)
                )
            }
        }

        loaderManager.initLoader(SMS_LOADER, null, object : LoaderManager.LoaderCallbacks<Cursor> {
            override fun onCreateLoader(id: Int, args: Bundle?): Loader<Cursor> = run {
                CursorLoader(
                        ctx,
                        Telephony.Sms.Inbox.CONTENT_URI,
//                        null,
                        arrayOf(
                                Telephony.Sms.Inbox.DATE,
                                Telephony.Sms.Inbox.BODY,
                                Telephony.Sms.Inbox.ADDRESS
                        ),
                        null,
                        null,
                        Telephony.Sms.Inbox.DATE
                )
            }

            override fun onLoadFinished(loader: Loader<Cursor>?, cursor: Cursor?) {
                messagesListViewAdapter.apply {
                    maybeCursor = cursor
                    notifyDataSetChanged()
                }
            }

            override fun onLoaderReset(loader: Loader<Cursor>?) {
                messagesListViewAdapter.apply {
                    maybeCursor = null
                    notifyDataSetChanged()
                }
            }
        })

        setContentView(LinearLayout(ctx).also {
            it.addView(ListView(ctx).also {
                it.adapter = messagesListViewAdapter
            })
        })

    }

    override fun onRestart() {
        super.onRestart()
        loaderManager.getLoader<Cursor>(SMS_LOADER)?.reset() ?: Log.e(TAG, "No loader on restart")
    }
}

private fun formatDate(itemUnixtime: Long): String {
    val formatter = SimpleDateFormat.getTimeInstance(DateFormat.SHORT)
    return formatter.format(Date(itemUnixtime))
}

fun <T> benchmark(label: String, op: () -> T): T {
    val started = System.nanoTime()
    val result = op.invoke()
    val ended = System.nanoTime()
    Log.d(TAG, "Benchmark, $label took ${(ended - started) / 1000000} ms")
    return result
}