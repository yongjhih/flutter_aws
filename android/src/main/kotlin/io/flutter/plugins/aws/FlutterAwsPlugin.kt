package io.flutter.plugins.aws

import android.content.Context
import android.util.Log
import com.amazonaws.mobile.client.AWSMobileClient
import com.amazonaws.mobile.client.UserStateDetails
import com.amazonaws.mobile.config.AWSConfiguration
import com.amazonaws.mobileconnectors.pinpoint.PinpointConfiguration
import com.amazonaws.mobileconnectors.pinpoint.PinpointManager
import com.amazonaws.mobileconnectors.pinpoint.targeting.notification.NotificationClient
import com.amazonaws.mobileconnectors.pinpoint.targeting.notification.NotificationDetails
import com.google.firebase.iid.FirebaseInstanceId
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import io.flutter.plugin.common.PluginRegistry.Registrar
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import android.content.Intent


/**
 * ref. https://aws-amplify.github.io/docs/android/push-notifications
 */
class FlutterAwsPlugin(private val registrar: Registrar,
                       private val context: Context): MethodCallHandler {
    companion object {
        private const val FLUTTER_AWS = "flutter_aws"
        private const val CHANNEL = FLUTTER_AWS

        @JvmStatic
        fun registerWith(registrar: Registrar) {
            val channel = MethodChannel(registrar.messenger(), CHANNEL)
            channel.setMethodCallHandler(FlutterAwsPlugin(registrar, registrar.context()))
        }

        const val ACTION_PUSH_NOTIFICATION = "push-notification"

        const val ON_NEW_TOKEN = "onNewToken"
        const val DATA = "data"
        const val NOTIFICATION = "notification"
        const val ON_MESSAGE = "onMessage"
        const val INITIALIZE = "initialize"
    }

    private val pinpoint: PinpointManager by lazy {
        PinpointManager(PinpointConfiguration(
                context,
                awsClient,
                awsConfig))
    }

    private val awsConfig: AWSConfiguration by lazy { AWSConfiguration(context) }

    private val awsClient: AWSMobileClient by lazy {
        AWSMobileClient.getInstance().initializes(context, awsConfig) {
            onResult {
                Log.d(TAG, "${it.userState}")
            }
            onError { e ->
                e.printStackTrace()
            }
        }
    }

    override fun onMethodCall(call: MethodCall, result: Result) {
        when (call.method) {
            ON_NEW_TOKEN -> {
                val token = call.argumentsOrNull<String>()
                token?.let { pinpoint.notificationClient.registerDeviceToken(token) }
                result.success(token)
            }
            ON_MESSAGE -> {
                val data = call.argument<Map<String, String>>(DATA) // Map<String, String> RemoteMessage.getData()
                val notification = call.argumentOrNull<Map<String, Any>>(NOTIFICATION) // RemoteMessage.Notification RemoteMessage.getNotification

                val pushResult = pinpoint.notificationClient.handleCampaignPush(NotificationDetails.builder()
                        //.from(remoteMessage.getFrom()) // TODO
                        .mapData(data)
                        .intentAction(NotificationClient.FCM_INTENT_ACTION)
                        .build())

                if (NotificationClient.CampaignPushResult.NOT_HANDLED != pushResult) {
                    /**
                     * The push message was due to a Pinpoint campaign.
                     * If the app was in the background, a local notification was added
                     * in the notification center. If the app was in the foreground, an
                     * event was recorded indicating the app was in the foreground,
                     * for the demo, we will broadcast the notification to let the main
                     * activity display it in a dialog.
                     */
                    if (NotificationClient.CampaignPushResult.APP_IN_FOREGROUND == pushResult) {
                        /* Create a message that will display the raw data of the campaign push in a dialog. */
                        // from?.let { broadcast(from, HashMap(data)) } ?? // TODO
                        data?.let { broadcast(HashMap(data)) }
                    }
                }
                result.success(null)
            }
            INITIALIZE -> {
                FirebaseInstanceId.getInstance().instanceId.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val token = task.result?.token
                        Log.d(TAG, "Registering push notifications token: ${token}")
                        token?.let { pinpoint.notificationClient.registerDeviceToken(token) }
                    } else {
                        Log.w(TAG, "getInstanceId failed", task.exception)
                    }
                }
                result.success(null)
            }
            else -> {
                result.notImplemented()
            }
        }
    }

    private fun broadcast(dataMap: HashMap<String, String>) {
        LocalBroadcastManager.getInstance(context).sendBroadcast(Intent(ACTION_PUSH_NOTIFICATION).apply {
            putExtra(NotificationClient.INTENT_SNS_NOTIFICATION_DATA, dataMap)
        })
    }

    private fun broadcast(from: String, dataMap: HashMap<String, String>) {
        LocalBroadcastManager.getInstance(context).sendBroadcast(Intent(ACTION_PUSH_NOTIFICATION).apply {
            putExtra(NotificationClient.INTENT_SNS_NOTIFICATION_FROM, from)
            putExtra(NotificationClient.INTENT_SNS_NOTIFICATION_DATA, dataMap)
        })
    }
}

fun <T> MethodCall.argumentOrNull(key: String): T? = try { argument(key) } catch (e: Throwable) { null }
fun <T> MethodCall.argumentsOrNull(): T? = arguments() as? T?

val Any.TAG: String
    get() {
        val tag = javaClass.simpleName
        val max = 23
        return if (tag.length <= max) tag else tag.substring(0, max)
    }

fun AWSMobileClient.initializes(context: Context, config: AWSConfiguration, init: Callbacks<UserStateDetails>.() -> Unit): AWSMobileClient {
  val callbacks = Callbacks<UserStateDetails>()
  callbacks.init()
  this.initialize(context, config, callbacks)
  return this
}

class Callbacks<T> : com.amazonaws.mobile.client.Callback<T> {
  var onResultFunc: (T) -> Unit = {}
  var onErrorFunc: (Throwable) -> Unit = {}

  override fun onResult(result: T) {
    onResultFunc(result)
  }

  fun onResult(onResult: (T) -> Unit) {
    this.onResultFunc = onResult
  }

  override fun onError(e: Exception?) {
    onErrorFunc(e ?: Exception())
  }

  fun onError(onError: (Throwable) -> Unit) {
    this.onErrorFunc = onError
  }
}
