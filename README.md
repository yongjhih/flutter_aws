# flutter_aws

## Installation

pubspec.yaml
```yaml
dependencies:
  # should have firebase_messaging: ^5.1.6 dep already
  flutter_aws:
    git:
      url: https://github.com/yongjhih/flutter_aws.git
```

## Integration with Pinpoint

1. Setup FCM: android/app/src/main/google-services.json from Firebase
2. Setup android/app/src/main/res/raw/awsconfiguration.json with amplify cli (push, analytics)
3. Add a receiver into AndroidManifest.xml:

```xml
        <receiver android:name="com.amazonaws.mobileconnectors.pinpoint.targeting.notification.PinpointNotificationReceiver"
            android:exported="false" >
            <intent-filter>
                <action android:name="com.amazonaws.intent.fcm.NOTIFICATION_OPEN" />
            </intent-filter>
        </receiver>
```

build.gradle:

```
dependencies {
    implementation 'com.amazonaws:aws-android-sdk-pinpoint:2.15.+'
    implementation ('com.amazonaws:aws-android-sdk-mobile-client:2.15.+@aar') { transitive = true }
}
```

4. Initalization when app started

```dart
  @override
  void initState() {
    super.initState();

    Aws.initialize();

    final FirebaseMessaging _firebaseMessaging = FirebaseMessaging();
    _firebaseMessaging.configure(
      onMessage: (Map<String, dynamic> message) async {
        Fimber.d("firebaseMessaging: onMessage: $message");
        await Aws.onMessage(message);
      },
      onLaunch: (Map<String, dynamic> message) async {
        Fimber.d("firebaseMessaging: onLaunch: $message");
      },
      onResume: (Map<String, dynamic> message) async {
        Fimber.d("firebaseMessaging: onResume: $message");
      },
    );

    _firebaseMessaging.requestNotificationPermissions(
        const IosNotificationSettings(sound: true, badge: true, alert: true));

    _firebaseMessaging.onIosSettingsRegistered.listen((IosNotificationSettings settings) {
      Fimber.d("firebaseMessaging: Settings registered: $settings");
    });

    _firebaseMessaging.onTokenRefresh.listen((String token) {
      Fimber.d("firebaseMessaging: Settings registered: $token");
      Aws.registerDeviceToken(token);
    });
    _firebaseMessaging.getToken().then((String token) {
      Fimber.d("firebaseMessaging getToken: $token");
      Aws.registerDeviceToken(token);
    });
  }
```
