package ru.wilemyvu.android.smsapp

import android.content.Context
import android.support.v4.content.ContextCompat

enum class PermissionReq(val resolutionCode: Int, val permission: String) {
    ReadSms(1, android.Manifest.permission.READ_SMS),
    PhoneNumResolution(2, android.Manifest.permission.READ_CONTACTS);

    fun granted(ctx: Context) = ContextCompat.checkSelfPermission(ctx, permission) ==
            android.content.pm.PackageManager.PERMISSION_GRANTED

    companion object {
        fun fromCode(code: Int): PermissionReq = PermissionReq.values().find { it.resolutionCode == code }
                ?: throw IllegalStateException("unknown PermissionReq code $code")
    }
}