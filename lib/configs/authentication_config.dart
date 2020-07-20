class AuthenticationConfig {

  final String userReference;
  final String enrollmentTransactionReference;
  final String authenticationTransactionReference;
  final String callbackUrl;

  AuthenticationConfig({
    this.authenticationTransactionReference,
    this.callbackUrl,
    this.userReference,
    this.enrollmentTransactionReference,
  });
  String validate() {
    if (authenticationTransactionReference != null && enrollmentTransactionReference != null) {
      return 'Only enrollmentTransactionReference or' + 
      ' authenticationTransactionReference should be set';
    }
    return null;
  }

  Map toMap() {
    var map = {};
    if (userReference != null) {
      map['userReference'] = userReference;
    }
    if (enrollmentTransactionReference != null) {
      map['enrollmentTransactionReference'] = enrollmentTransactionReference;
    }
    if (authenticationTransactionReference != null) {
      map['authenticationTransactionReference'] = authenticationTransactionReference;
    }
    if (callbackUrl != null) {
      map['callbackUrl'] = callbackUrl;
    }
    return map;
  }
}