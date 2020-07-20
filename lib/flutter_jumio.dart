export 'configs/authentication_config.dart';
export 'configs/bam_config.dart';
export 'configs/netverify_config.dart';
export 'configs/document_verification_config.dart';
export 'enums/card_types.dart';
export 'enums/data_center.dart';
export 'enums/document_type.dart';
export 'enums/camera_position.dart';
export 'enums/enable_watch_list_screening.dart';
export 'enums/preselected_document_variant.dart';
export 'countries.dart';
export 'color_to_hex_string.dart';

import 'dart:async';
import 'dart:convert';

import 'package:flutter/services.dart';
import 'package:flutter/widgets.dart';
import 'package:flutter_jumio/configs/authentication_config.dart';
import 'package:flutter_jumio/configs/bam_config.dart';
import 'package:flutter_jumio/configs/document_verification_config.dart';
import 'package:flutter_jumio/configs/netverify_config.dart';
import 'package:flutter_jumio/enums/data_center.dart';

class FlutterJumio {

  static ValueChanged<String> _errorHandler;

  static const MethodChannel _channel =
      const MethodChannel('flutter_jumio');
  static bool _isInitialized = false;

  static void setErrorHandler(ValueChanged<String> errorHandler) {
    _errorHandler = errorHandler;
  }
  
  static void _init() {
    if (_isInitialized) return;
    _isInitialized = true;
    _channel.setMethodCallHandler(_methodCallHandler);
  } 

  static Future initNetverify({
    String apiToken, 
    String apiSecret, 
    DataCenter dataCenter = DataCenter.US, 
    NetverifyConfig config
  }) async {
    
    var params = {
      'apiToken': apiToken,
      'apiSecret': apiSecret,
      'dataCenter': enumToString(dataCenter),
      'options': config.toMap()
    };
    _init();
    await _sendParams(_Method.InitNetverify, params);
  }
  static Future startNetVerify() async {
    await _sendParams(_Method.StartNetverify, null);
  } 
  static Future startBam() async {
    await _sendParams(_Method.StartBAM, null);
  } 
  static Future startAuthentication() async {
    await _sendParams(_Method.StartAuthentication, null);
  }
  static Future startDocumentVerification() async {
    await _sendParams(_Method.StartDocumentVerification, null);
  }
  static Future<String> showHello() async {
    return await _channel.invokeMethod('InitBAM', {'someParam' : 'bbbb'});
  }

  static Future<void> initBAM({
    String apiSecret,
    String apiToken,
    DataCenter dataCenter = DataCenter.US,
    BamConfig config
  }) async {
    var params = {
      'apiToken': apiToken,
      'apiSecret': apiSecret,
      'dataCenter': enumToString(dataCenter),
      'options': config.toMap()
    };
    _init();
    _sendParams(_Method.InitBAM, params);
  }
  static Future<void> initAuthentication({
    String apiSecret,
    String apiToken,
    DataCenter dataCenter = DataCenter.US,
    AuthenticationConfig config
  }) async {
    var params = {
      'apiToken': apiToken,
      'apiSecret': apiSecret,
      'dataCenter': enumToString(dataCenter),
      'options': config.toMap()
    };
    var configValidationError = config.validate();
    if (configValidationError != null) {
      _onError(configValidationError);
      return;
    }
    _init();
    await _sendParams(_Method.InitAuthentication, params);
  }
  static Future<void> initDocunmentVerification({
    String apiSecret,
    String apiToken,
    DataCenter dataCenter = DataCenter.US,
    DocumentVerificationConfig config
  }) async {
    var params = {
      'apiToken': apiToken,
      'apiSecret': apiSecret,
      'dataCenter': enumToString(dataCenter),
      'options': config.toMap()
    };
    var configValidationError = config.validate();
    if (configValidationError != null) {
      _onError(configValidationError);
      return;
    } 
    _init();
    await _sendParams(_Method.InitDocumentVerification, params);
  }

  static Future _sendParams(_Method method, Map params) async {
    var methodName = enumToString(method);
    _printParams(methodName, jsonEncode(params));
    _channel.invokeMethod(methodName, params);
  }

  static void _printParams(String methodName, String params) {
    print('[FlutterJumio] called: $methodName, with params: $params');
  }

  /// принимает ответ с натива
  static Future<dynamic> _methodCallHandler(MethodCall methodCall) async {
    switch (methodCall.method) {
      case 'onError':
        _onError(methodCall.arguments?.toString());
        break;
      case 'onNativeMessage':
        _onNativeMessage(methodCall.arguments?.toString());
        break;
    }
  }
  static void _onNativeMessage(String message) {
    print('[Flutter Jumio Native Side Message] $message');
  }

  static void _onError(String errorText) {
    var text = '[Flutter Jumio Native Side Error] $errorText';
    print(text);
    if (_errorHandler != null) {
      _errorHandler(text);
    }
  }
  static String enumToString(enumValue) {
    if (enumValue == null) return null;
    return enumValue.toString().split('.')[1];
  }

}
enum _Method {
  InitBAM,
  StartBAM,
  InitNetverify,
  StartNetverify,
  InitAuthentication,
  StartAuthentication,
  InitDocumentVerification,
  StartDocumentVerification
}
