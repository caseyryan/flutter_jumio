import 'package:flutter/widgets.dart';
import 'package:flutter_jumio/enums/camera_position.dart';
import 'package:flutter_jumio/enums/document_type.dart';
import 'package:flutter_jumio/flutter_jumio.dart';

class DocumentVerificationConfig {

  final DocumentType type;
  final String customDocumentCode;
  final String country;
  final String reportingCriteria;
  final String callbackUrl;
  final String customerInternalReference;
  final String userReference;
  final String documentName;
  final CameraPosition cameraPosition;
  final bool enableExtraction;

  /// [userReference]  is not used by Jumio itself but it is required for you
  /// to check what user was verified. It will be returned in a response. You may 
  /// usee e.g. a user's email of phone as a userReference
  /// [customerInternalReference] just like with userReference, use some data 
  /// that is necessary for you, e.g. a userId in your system
  /// [country] is a country code. You may obtain one from a static String constants
  /// in a Countries class of this plugin. The code must be at least 1 and at max 3 
  /// characters long 
  DocumentVerificationConfig({
    @required this.country,
    @required this.userReference,
    @required this.customerInternalReference,
    this.type,
    this.customDocumentCode,
    this.reportingCriteria,
    this.callbackUrl,
    this.documentName,
    this.cameraPosition,
    this.enableExtraction
  });
  String validate() {
    if (country == null || country.length < 1 || country.length > 3) {
      return 'DocumentVerificationConfig: country must' + 
      ' contain a country code of not more than 3 characters' + 
      ' You can get one from Countries class as a string constant';
    }
    if (userReference == null || userReference.length < 1 || userReference.length > 100) {
      return 'DocumentVerificationConfig: userReference' + 
      ' must be between 1 and 100 characters long';
    }
    if (customerInternalReference == null || customerInternalReference.length < 1 
      || customerInternalReference.length > 100) {
      return 'DocumentVerificationConfig: customerInternalReference' + 
      ' must be between 1 and 100 characters long';
    }
    return null;
  }

  Map toMap() {
    var map = {};
    if (type != null) {
      map['type'] = FlutterJumio.enumToString(type);
    }
    if (customDocumentCode != null) {
      map['customDocumentCode'] = customDocumentCode;
    }
    if (country != null) {
      map['country'] = country;
    }
    if (reportingCriteria != null) {
      map['reportingCriteria'] = reportingCriteria;
    }
    if (callbackUrl != null) {
      map['callbackUrl'] = callbackUrl;
    }
    if (customerInternalReference != null) {
      map['customerInternalReference'] = customerInternalReference;
    }
    if (userReference != null) {
      map['userReference'] = userReference;
    }
    if (documentName != null) {
      map['documentName'] = documentName;
    }
    if (cameraPosition != null) {
      map['cameraPosition'] = FlutterJumio.enumToString(cameraPosition);
    }
    if (enableExtraction != null) {
      map['enableExtraction'] = enableExtraction;
    }
    return map;
  }
}