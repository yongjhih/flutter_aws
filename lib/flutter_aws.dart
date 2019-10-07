import 'dart:async';
import 'dart:collection';

import 'package:flutter/services.dart';

class Aws {
  static const MethodChannel _channel =
      const MethodChannel('flutter_aws');

  static Future<void> initialize() async {
    await _channel.invokeMethod('initialize');
  }

  @deprecated
  static Future<void> onNewToken(String token) async {
    await _channel.invokeMethod('onNewToken', token);
  }

  static Future<void> registerDeviceToken(String token) async {
    await _channel.invokeMethod('registerDeviceToken', token);
  }

  static Future<String> get endpointId async => await _channel.invokeMethod('endpointId');

  static Future<void> onMessage(Map<String, Object> message) async {
    await _channel.invokeMethod('onMessage', message);
  }

  static Future<void> submitEvents() async {
    await _channel.invokeMethod('submitEvents');
  }

  static Future<void> submitLoginEvent() async {
    await _channel.invokeMethod('submitLoginEvent');
  }

  static Future<void> startSession() async {
    await _channel.invokeMethod('startSession');
  }

  static Future<void> stopSession() async {
    await _channel.invokeMethod('stopSession');
  }

  static Future<void> setUserId(String id) async {
    await _channel.invokeMethod('setUserId', id);
  }

  static Future<void> addAttribute(String key, List<String> value) async {
    await _channel.invokeMethod('addAttribute', {
      "key": key,
      "value": value,
    });
  }
}
