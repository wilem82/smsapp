package ru.wilemyvu.android.smsapp.mixedmessagelist

import android.app.AlertDialog
import android.app.LoaderManager
import android.content.Context
import android.content.CursorLoader
import android.content.Loader
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Typeface
import android.os.Bundle
import android.provider.Telephony
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import ru.wilemyvu.android.smsapp.PermissionReq

private const val TAG = "smsapp"
private const val SMS_LOADER = 1

class MixedMessagesListActivity : AppCompatActivity() {

    private lateinit var ctx: Context

    private lateinit var messagesListViewAdapter: RecyclerAdapter
    private lateinit var messagesListView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ctx = applicationContext

        messagesListViewAdapter = ru.wilemyvu.android.smsapp.mixedmessagelist.RecyclerAdapter(
                ctx,
                Typeface.createFromAsset(assets, "PTM55FT.ttf")
        )

        setTheme(android.support.v7.appcompat.R.style.Theme_AppCompat_DayNight)
        setContentView(RecyclerView(ctx).also {
            it.layoutManager = LinearLayoutManager(ctx)
            messagesListView = it
        })

    }

    override fun onStart() {
        super.onStart()

        checkAndAskPermission(
                PermissionReq.PhoneNumResolution,
                "To display names instead of phone numbers, permission to read contacts is needed.  This is optional.",
                { messagesListViewAdapter.notifyDataSetChanged() }
        )

        checkAndAskPermission(
                PermissionReq.ReadSms,
                "A permission to read SMS messages is required to run this app.",
                {
                    messagesListView.adapter = messagesListViewAdapter
                    startSmsLoader()
                },
                { finishAffinity() }
        )

    }

    override fun onResume() {
        super.onResume()
        /* Nothing yet. */
    }

    override fun onRestart() {
        super.onRestart()
        loaderManager.getLoader<Cursor>(SMS_LOADER)?.reset() ?: Log.e(TAG, "No loader on restart")
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        val callbacks = permissionResultCallbacks[PermissionReq.fromCode(requestCode)]!!
        if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
            callbacks.deniedOp.invoke()
            return
        }
        callbacks.grantedOp.invoke()
    }

    private fun startSmsLoader() {
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
    }

    private class PermissionReqCallback(val grantedOp: () -> Unit, val deniedOp: () -> Unit)
    private val permissionResultCallbacks: MutableMap<PermissionReq, PermissionReqCallback> = HashMap()

    private fun checkAndAskPermission(
            permissionReq: PermissionReq,
            explanation: String,
            grantedOp: () -> Unit,
            deniedOp: () -> Unit = {}
    ) {
        permissionResultCallbacks[permissionReq] = PermissionReqCallback(grantedOp, deniedOp)

        (ContextCompat.checkSelfPermission(
                ctx,
                permissionReq.permission
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED).let {
            if (it) {
                grantedOp.invoke()
                return@let
            }

            AlertDialog.Builder(this)
                    .setTitle("Attention")
                    .setMessage(explanation)
                    .setPositiveButton("OK", { dialog, _ ->
                        dialog.dismiss()
                        ActivityCompat.requestPermissions(
                                this,
                                arrayOf(permissionReq.permission),
                                permissionReq.resolutionCode
                        )

                    })
                    .create()
                    .show()
        }
    }


}

/*

        if (false) {
            FontRequest(
                    "com.google.android.gms.fonts",
                    "com.google.android.gms",
                    "PT+Mono",
                    Certs.googleFontsProd
            ).let { fontRequest ->
                FontsContractCompat.requestFont(
                        this,
                        fontRequest,
                        object : FontsContractCompat.FontRequestCallback() {
                            override fun onTypefaceRetrieved(typeface: Typeface) {
                                messageFont = typeface
                                messagesListView.asSequence<TextView>().forEach { it.typeface = typeface }
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

 */