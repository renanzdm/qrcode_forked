package com.example.qrcode

import android.Manifest
import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.os.Build
import android.os.Bundle
import android.view.View
import com.google.zxing.ResultPoint
import com.journeyapps.barcodescanner.BarcodeCallback
import com.journeyapps.barcodescanner.BarcodeResult
import com.journeyapps.barcodescanner.BarcodeView
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.BinaryMessenger
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.PluginRegistry
import io.flutter.plugin.platform.PlatformView

class QRCaptureView(
    id: Int,
    messenger: BinaryMessenger,
    context: Context
) :
    PlatformView, MethodCallHandler, ActivityAware {

    private lateinit var activity: Activity
    var barcodeView: BarcodeView? = null
    var cameraPermissionContinuation: Runnable? = null
    var requestingPermission = false
    var channel: MethodChannel


    init {
        barcodeView = BarcodeView(context)
        channel = MethodChannel(messenger, "plugins/qr_capture/method_$id")
        channel.setMethodCallHandler(this)
        barcodeView!!.decodeContinuous(
            object : BarcodeCallback {
                override fun barcodeResult(result: BarcodeResult) {
                    channel.invokeMethod("onCaptured", result.text)
                }

                override fun possibleResultPoints(resultPoints: List<ResultPoint>) {}
            }
        )
        barcodeView!!.resume()
    }


    override fun onMethodCall(call: MethodCall, result: MethodChannel.Result) {
        when (call?.method) {
            "checkAndRequestPermission" -> {
                checkAndRequestPermission(result)
            }
        }

        when (call?.method) {
            "resume" -> {
                resume()
            }
        }

        when (call?.method) {
            "pause" -> {
                pause()
            }
        }

        when (call?.method) {
            "setTorchMode" -> {
                val isOn = call.arguments as Boolean
                barcodeView?.setTorch(isOn);
            }
        }
    }

    private fun resume() {
        barcodeView?.resume()
    }

    private fun pause() {
        barcodeView?.pause()
    }

    private fun checkAndRequestPermission(result: MethodChannel.Result?) {
        if (cameraPermissionContinuation != null) {
            result?.error("cameraPermission", "Camera permission request ongoing", null);
        }

        cameraPermissionContinuation = Runnable {
            cameraPermissionContinuation = null
            if (!hasCameraPermission()) {
                result?.error(
                    "cameraPermission", "MediaRecorderCamera permission not granted", null
                )
                return@Runnable
            }
        }

        requestingPermission = false
        if (hasCameraPermission()) {
            cameraPermissionContinuation?.run()
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestingPermission = true
                activity
                    .requestPermissions(
                        arrayOf(Manifest.permission.CAMERA),
                        CAMERA_REQUEST_ID
                    )
            }
        }
    }

    private fun hasCameraPermission(): Boolean {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.M ||
                activity.checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
    }


    companion object {
        const val CAMERA_REQUEST_ID = 513469796
    }


    override fun getView(): View {
        return this.barcodeView!!;
    }

    override fun dispose() {
        barcodeView?.pause()
        barcodeView = null
    }

    private inner class CameraRequestPermissionsListener :
        PluginRegistry.RequestPermissionsResultListener {
        override fun onRequestPermissionsResult(
            id: Int,
            permissions: Array<String>,
            grantResults: IntArray
        ): Boolean {
            if (id == CAMERA_REQUEST_ID && grantResults[0] == PERMISSION_GRANTED) {
                cameraPermissionContinuation?.run()
                return true
            }
            return false
        }
    }

    override fun onAttachedToActivity(binding: ActivityPluginBinding) {
        activity = binding.activity
        binding.addRequestPermissionsResultListener(CameraRequestPermissionsListener())
        checkAndRequestPermission(null)
        activity.application.registerActivityLifecycleCallbacks(
            object : Application.ActivityLifecycleCallbacks {
                override fun onActivityPaused(p0: Activity?) {
                    if (p0 == activity) {
                        barcodeView?.pause()
                    }
                }

                override fun onActivityResumed(p0: Activity?) {
                    if (p0 == activity) {
                        barcodeView?.resume()
                    }
                }

                override fun onActivityStarted(p0: Activity?) {
                }

                override fun onActivityDestroyed(p0: Activity?) {
                }

                override fun onActivitySaveInstanceState(p0: Activity?, p1: Bundle?) {
                }

                override fun onActivityStopped(p0: Activity?) {
                }

                override fun onActivityCreated(p0: Activity?, p1: Bundle?) {
                }

            }
        )

    }

    override fun onDetachedFromActivityForConfigChanges() {
        TODO("Not yet implemented")
    }

    override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {
        TODO("Not yet implemented")
    }

    override fun onDetachedFromActivity() {
        TODO("Not yet implemented")
    }

}
