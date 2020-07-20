import 'dart:convert';

import 'package:flutter/material.dart';
import 'dart:async';
import 'package:flutter/services.dart';
import 'package:flutter_jumio/flutter_jumio.dart';

void main() => runApp(MyApp());

class MyApp extends StatefulWidget {
  @override
  _MyAppState createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {

  /// if you want to launch this example project
  /// you should either add your apiToken and apiSecret
  /// here, or create a file called secrets.json in the 
  /// assets folder (assets/secrets.json) with the following structure
  /// {
  ///   "apiSecret": "YOUR JUMIO SECRET HERE",
  ///   "apiToken": "YOUR JUMIO TOKEN HERE"
  /// }
  String _apiToken = 'YOUR JUMIO SECRET HERE';
  String _apiSecret = 'YOUR JUMIO TOKEN HERE';

  bool _bamInitialized = false;
  bool _netverifyInitialized = false;
  bool _docVerificationInitialized = false;
  bool _authInitialized = false;

  @override
  void initState() {
    super.initState();
    _loadTokenAndSecret();
  }
  Future _loadTokenAndSecret() async {
    await Future.delayed(Duration(milliseconds: 1000));
    try {
      if (_apiToken == 'YOUR JUMIO SECRET HERE') {
        var secretString = await rootBundle.loadString('assets/secrets.json');
        var secretsData = jsonDecode(secretString);
        _apiToken = secretsData['apiToken'];
        _apiSecret = secretsData['apiSecret'];
        setState(() {});
      }
    }
    catch (e) {
      print(e);
    }
  }

  Future _initNetverify() async {
    await FlutterJumio.initNetverify(
      apiToken: _apiToken,
      apiSecret: _apiSecret,
      dataCenter: DataCenter.EU,
      config: NetverifyConfig(
        documentTypes: [
          DocumentType.PASSPORT, 
          DocumentType.DRIVER_LICENSE,
          DocumentType.IDENTITY_CARD,
          DocumentType.VISA,
        ],
        cameraPosition: CameraPosition.FRONT,
        dataExtractionOnMobileOnly: false,
        enableIdentityVerification: true,
        enableVerification: true,
        preselectedDocumentVariant: PreselectedDocumentVariant.PAPER,
        sendDebugInfoToJumio: true,
        enableWatchlistScreening: EnableWatchlistStreening.DEFAULT,
      )
    );
    setState(() {
      _netverifyInitialized = true;
    });
  }
  Future _startNetverify() async {
     await FlutterJumio.startNetVerify();
     setState(() {});
  }
  Future _initBam() async {
    await FlutterJumio.initBAM(
      apiToken: _apiToken,
      apiSecret: _apiSecret,
      dataCenter: DataCenter.US,
      config: BamConfig(
        cameraPosition: CameraPosition.FRONT,
        cardHolderNameEditable: true,
        cardHolderNameRequired: true,
        cardTypes: [
          CardTypes.MASTER_CARD, 
          CardTypes.VISA,
          CardTypes.AMERICAN_EXPRESS,
          CardTypes.CHINA_UNIONPAY,
          CardTypes.DINERS_CLUB,
          CardTypes.DISCOVER,
          CardTypes.JCB
        ],
        enableFlashOnScanStart: true,
        cvvRequired: true,
        expiryEditable: true,
        expiryRequired: true,
        sortCodeAndAccountNumberRequired: false
      )
    );
    setState(() {
      _bamInitialized = true;
    });
  }
  Future _startBam() async {
    await FlutterJumio.startBam();
  }
  Future _initAuthentication() async {
    await FlutterJumio.initAuthentication(
      apiSecret: _apiSecret,
      apiToken: _apiToken,
      dataCenter: DataCenter.EU,
      config: AuthenticationConfig(
        enrollmentTransactionReference: 'Some enrollment reference',
      )
    );
    setState(() {
      _authInitialized = true;
    });
  }
  Future _startAuthentication() async {
    await FlutterJumio.startAuthentication();
  }
  Future _initDocumentVerification() async {
    await FlutterJumio.initDocunmentVerification(
      apiSecret: _apiSecret,
      apiToken: _apiToken,
      dataCenter: DataCenter.EU,
      config: DocumentVerificationConfig(
        type: DocumentType.DRIVER_LICENSE,
        country: Countries.RussianFederation,
        userReference: 'Some User Reference',
        customerInternalReference: 'Some Customer Internal Reference',
      )
    );
    setState(() {
      _docVerificationInitialized = true;
    });
  }
  Future _startDocumentVerification() async {
    await FlutterJumio.startDocumentVerification();
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('Flutter Jumio Example'),
        ),
        body: Center(
          child: Column(
            mainAxisAlignment: MainAxisAlignment.center,
            crossAxisAlignment: CrossAxisAlignment.center,
            children: <Widget>[
              RaisedButton(
                color: _docVerificationInitialized ? Colors.grey : Colors.green,
                onPressed: _initDocumentVerification,
                child: Text('Init Document Verification'),
              ),
              RaisedButton(
                onPressed: _docVerificationInitialized ? _startDocumentVerification : null,
                child: Text('Start Document Verification'),
              ),
              Divider(height: 10,),
              RaisedButton(
                color: _bamInitialized ? Colors.grey : Colors.green,
                onPressed: _initBam,
                child: Text('Init BAM'),
              ),
              RaisedButton(
                onPressed: _bamInitialized ? _startBam : null,
                child: Text('Start BAM'),
              ),
              Divider(height: 10,),
              RaisedButton(
                color: _netverifyInitialized ? Colors.grey : Colors.green,
                onPressed: _initNetverify,
                child: Text('Init Netverify'),
              ),
              RaisedButton(
                onPressed: _netverifyInitialized ? _startNetverify : null,
                child: Text('Start Netverify'),
              ),
              Divider(height: 10,),
              RaisedButton(
                color: _authInitialized ? Colors.grey : Colors.green,
                onPressed: _initAuthentication,
                child: Text('Init Authentication'),
              ),
              RaisedButton(
                onPressed: _authInitialized ? _startAuthentication : null,
                child: Text('Start Authentication'),
              ),
            ],
          ),
        ),
      ),
    );
  }
}
