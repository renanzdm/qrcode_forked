package com.example.qrcode
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.plugin.common.PluginRegistry.Registrar

class QrcodePlugin:FlutterPlugin {



  companion object {
    @JvmStatic
    fun registerWith(registrar: Registrar) {
      registrar.platformViewRegistry().registerViewFactory("plugins/qr_capture_view", QRCaptureViewFactory(registrar.messenger()))
    }
  }

    override fun onAttachedToEngine(binding: FlutterPlugin.FlutterPluginBinding) {
        binding.platformViewRegistry.registerViewFactory("plugins/qr_capture_view",QRCaptureViewFactory(binding.binaryMessenger))
    }

    override fun onDetachedFromEngine(binding: FlutterPlugin.FlutterPluginBinding) {
        TODO("Not yet implemented")
    }
}
