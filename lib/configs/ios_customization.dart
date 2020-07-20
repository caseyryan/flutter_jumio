import 'package:flutter/material.dart';
import 'package:flutter_jumio/color_to_hex_string.dart';
/// TODO: implement this

class IOSCustomization {
  /// this customization is applied to iOS part
  /// only and will have no effect on Android
  final bool disableBlur;
  final bool enableDarkMode;
  final Color backgroundColor;
  final Color tintColor;
  final Color barTintColor;
  final Color textTitleColor;
  final Color foregroundColor;
  final Color documentSelectionHeaderBackgroundColor;
  final Color documentSelectionHeaderTitleColor;
  final Color documentSelectionHeaderIconColor;
  final Color documentSelectionButtonBackgroundColor;
  final Color documentSelectionButtonTitleColor;
  final Color documentSelectionButtonIconColor;
  final Color fallbackButtonBackgroundColor;
  final Color fallbackButtonBorderColor;
  final Color fallbackButtonTitleColor;
  final Color positiveButtonBackgroundColor;
  final Color positiveButtonBorderColor;
  final Color positiveButtonTitleColor;
  final Color negativeButtonBackgroundColor;
  final Color negativeButtonBorderColor;
  final Color negativeButtonTitleColor;
  final Color scanOverlayStandardColor;
  final Color scanOverlayValidColor;
  final Color scanOverlayInvalidColor;
  final Color scanBackgroundColor;
  final Color faceOvalColor;
  final Color faceProgressColor;
  final Color faceFeedbackBackgroundColor;
  final Color faceFeedbackTextColor;

  IOSCustomization(
      {this.disableBlur,
      this.enableDarkMode,
      this.backgroundColor,
      this.tintColor,
      this.barTintColor,
      this.textTitleColor,
      this.foregroundColor,
      this.documentSelectionHeaderBackgroundColor,
      this.documentSelectionHeaderTitleColor,
      this.documentSelectionHeaderIconColor,
      this.documentSelectionButtonBackgroundColor,
      this.documentSelectionButtonTitleColor,
      this.documentSelectionButtonIconColor,
      this.fallbackButtonBackgroundColor,
      this.fallbackButtonBorderColor,
      this.fallbackButtonTitleColor,
      this.positiveButtonBackgroundColor,
      this.positiveButtonBorderColor,
      this.positiveButtonTitleColor,
      this.negativeButtonBackgroundColor,
      this.negativeButtonBorderColor,
      this.negativeButtonTitleColor,
      this.scanOverlayStandardColor,
      this.scanOverlayValidColor,
      this.scanOverlayInvalidColor,
      this.scanBackgroundColor,
      this.faceOvalColor,
      this.faceProgressColor,
      this.faceFeedbackBackgroundColor,
      this.faceFeedbackTextColor
    });

  

  Map toMap() {
    var map = {};
    if (backgroundColor != null) {
      map['backgroundColor'] = colorToHexString(backgroundColor);
    }

    return map;
  }
}
