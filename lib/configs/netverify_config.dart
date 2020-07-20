import 'package:flutter_jumio/enums/camera_position.dart';
import 'package:flutter_jumio/enums/document_type.dart';
import 'package:flutter_jumio/enums/enable_watch_list_screening.dart';
import 'package:flutter_jumio/enums/preselected_document_variant.dart';
import 'package:flutter_jumio/flutter_jumio.dart';

class NetverifyConfig {

  final bool enableVerification;
  final String callbackUrl;
  final bool enableIdentityVerification;
  final String preselectedCountry;
  final String customerInternalReference;
  final String reportingCriteria;
  final String userReference;
  final String enableEpassport;
  final EnableWatchlistStreening enableWatchlistScreening;
  final String watchlistSearchProfile;
  final bool sendDebugInfoToJumio;
  final bool dataExtractionOnMobileOnly;
  final CameraPosition cameraPosition;
  final PreselectedDocumentVariant preselectedDocumentVariant;
  final List<DocumentType> documentTypes;

  NetverifyConfig({
    this.enableVerification, 
    this.callbackUrl, 
    this.enableIdentityVerification, 
    this.preselectedCountry, 
    this.customerInternalReference, 
    this.reportingCriteria, 
    this.userReference, 
    this.enableEpassport, 
    this.enableWatchlistScreening, 
    this.watchlistSearchProfile, 
    this.sendDebugInfoToJumio, 
    this.dataExtractionOnMobileOnly, 
    this.cameraPosition, 
    this.preselectedDocumentVariant, 
    this.documentTypes
  });
   

  Map toMap() {
    var map = {};
    if (enableVerification != null) {
      map['enableVerification'] = enableVerification;
    }
    if (callbackUrl != null) {
      map['callbackUrl'] = callbackUrl;
    }
    if (enableIdentityVerification != null) {
      map['enableIdentityVerification'] = enableIdentityVerification;
    }
    if (preselectedCountry != null) {
      map['preselectedCountry'] = preselectedCountry;
    }
    if (customerInternalReference != null) {
      map['customerInternalReference'] = customerInternalReference;
    }
    if (reportingCriteria != null) {
      map['reportingCriteria'] = reportingCriteria;
    }
    if (userReference != null) {
      map['userReference'] = userReference;
    }
    if (enableEpassport != null) {
      map['enableEpassport'] = enableEpassport;
    }
    if (enableWatchlistScreening != null) {
      map['enableWatchlistScreening'] = FlutterJumio.enumToString(enableWatchlistScreening);
    }
    if (watchlistSearchProfile != null) {
      map['watchlistSearchProfile'] = watchlistSearchProfile;
    }
    if (sendDebugInfoToJumio != null) {
      map['sendDebugInfoToJumio'] = sendDebugInfoToJumio;
    }
    if (dataExtractionOnMobileOnly != null) {
      map['dataExtractionOnMobileOnly'] = dataExtractionOnMobileOnly;
    }
    if (cameraPosition != null) {
      map['cameraPosition'] = FlutterJumio.enumToString(cameraPosition);
    }
    if (preselectedDocumentVariant != null) {
      map['preselectedDocumentVariant'] = FlutterJumio.enumToString(preselectedDocumentVariant);
    }
    if (documentTypes != null) {
      map['documentTypes'] = documentTypes.map((dt) => FlutterJumio.enumToString(dt)).toList();
    }
    return map;
  }
}