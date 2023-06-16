package com.domberdev.mygamervault.usescases.common

import android.os.Build
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment

class PermissionRequester(
    activity: Fragment,
    private val permission: String,
    private val onRationale: () -> Unit = {},//Shows the reason why this permission is taken
    private val onDenied: () -> Unit = {} //Show denied message of permission
) {

    private var onGranted: () -> Unit = {}
    private val permissionLauncher =
        activity.registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            when {
                isGranted -> onGranted()
                activity.shouldShowRequestPermissionRationale(permission) -> onRationale()
                else -> onDenied()
            }
        }

    fun runWithPermission(body: () -> Unit) {
        onGranted = body
        permissionLauncher.launch(permission)
    }
}