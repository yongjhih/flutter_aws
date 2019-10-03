package io.flutter.plugins.aws.example

import android.os.Bundle
import com.google.firebase.FirebaseApp

import io.flutter.app.FlutterActivity
import io.flutter.plugins.GeneratedPluginRegistrant

class MainActivity: FlutterActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    FirebaseApp.initializeApp(this)
    GeneratedPluginRegistrant.registerWith(this)
  }
}
