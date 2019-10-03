import 'dart:async';
import 'dart:collection';

import 'package:flutter/services.dart';

class FlutterAws {
  static const MethodChannel _channel =
      const MethodChannel('flutter_aws');

  static Future<void> onMessage(Map<String, Object> message) async {
    await _channel.invokeMethod('onMessage', message);
  }

  static Future<void> initialize() async {
    await _channel.invokeMethod('initialize');
  }

  static Future<void> onNewToken(String token) async {
    await _channel.invokeMethod('onNewToken', token);
  }
}
