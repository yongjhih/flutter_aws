#import "FlutterAwsPlugin.h"
#import <flutter_aws/flutter_aws-Swift.h>

@implementation FlutterAwsPlugin
+ (void)registerWithRegistrar:(NSObject<FlutterPluginRegistrar>*)registrar {
  [SwiftFlutterAwsPlugin registerWithRegistrar:registrar];
}
@end
