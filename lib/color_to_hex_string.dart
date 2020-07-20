import 'package:flutter/material.dart';

String colorToHexString(Color color) {
  return '#${color.value.toRadixString(16)}';
}