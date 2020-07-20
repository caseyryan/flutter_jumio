import 'package:flutter_jumio/enums/camera_position.dart';
import 'package:flutter_jumio/flutter_jumio.dart';


class BamConfig {
  final bool cardHolderNameRequired;
  final bool sortCodeAndAccountNumberRequired;
  final bool expiryRequired;
  final bool cvvRequired;
  final bool expiryEditable;
  final bool cardHolderNameEditable;
  final bool merchantReportingCriteria;
  final bool vibrationEffectEnabled;
  final bool enableFlashOnScanStart;
  final bool cardNumberMaskingEnabled;
  final CameraPosition cameraPosition;
  /// a list of values that can be found in CardTypes class 
  /// as string constants
  final List<String> cardTypes;

  BamConfig({
    this.cardHolderNameRequired, 
    this.sortCodeAndAccountNumberRequired, 
    this.expiryRequired, 
    this.cvvRequired, 
    this.expiryEditable, 
    this.cardHolderNameEditable, 
    this.merchantReportingCriteria, 
    this.vibrationEffectEnabled, 
    this.enableFlashOnScanStart, 
    this.cardNumberMaskingEnabled, 
    this.cameraPosition = CameraPosition.FRONT, 
    this.cardTypes
  });

  Map toMap() {
    var map = {};
    if (cardHolderNameRequired != null) {
      map['cardHolderNameRequired'] = cardHolderNameRequired;
    }
    if (sortCodeAndAccountNumberRequired != null) {
      map['sortCodeAndAccountNumberRequired'] = sortCodeAndAccountNumberRequired;
    }
    if (expiryRequired != null) {
      map['expiryRequired'] = expiryRequired;
    }
    if (cvvRequired != null) {
      map['cvvRequired'] = cvvRequired;
    }
    if (expiryEditable != null) {
      map['expiryEditable'] = expiryEditable;
    }
    if (cardHolderNameEditable != null) {
      map['cardHolderNameEditable'] = cardHolderNameEditable;
    }
    if (merchantReportingCriteria != null) {
      map['merchantReportingCriteria'] = merchantReportingCriteria;
    }
    if (vibrationEffectEnabled != null) {
      map['vibrationEffectEnabled'] = vibrationEffectEnabled;
    }
    if (enableFlashOnScanStart != null) {
      map['enableFlashOnScanStart'] = enableFlashOnScanStart;
    }
    if (cardNumberMaskingEnabled != null) {
      map['cardNumberMaskingEnabled'] = cardNumberMaskingEnabled;
    }
    if (cameraPosition != null) {
      map['cameraPosition'] = FlutterJumio.enumToString(cameraPosition);
    }
    if (cardTypes != null) {
      map['cardTypes'] = cardTypes;
    }
    return map;
  }

}