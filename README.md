# flutter_jumio

This plugin is a Flutter wrapper for the official Jumio mobile sdk

# usage

For Android add a permission to your AndroidManifest.xml

```
<uses-permission android:name="android.permission.INTERNET"/>
```

on iOS add a description for camera usage to the Info.plist
```
<key>NSCameraUsageDescription</key>
<string>The camera is used to scan documents for a verification process</string>
```

The usage is simple. First you call one of 4 initialization methods
with your apiSecret and apiToken and the necessary parameters

For example:

```dart

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

```

after the plugin is initialized start the corresponding SDK.
In this case you have initialized Netverify, so simply call 

```dart
await FlutterJumio.startNetVerify();
```

That's it.

## how to use example project

if you want your token and secret to be stored in a JSON config
just open example project folder and create an assets folder right next to the folder 
called lib, and put a file called secrets.json inside it like this: example/assets/secrets.json 

add the following content to it:
```
{
    "apiToken": "YOUR TOKEN GOES HERE",
    "apiSecret": "YOUR SECRET GOES HERE"
}
```

But if you don't wanna do it it's ok, just put your token and secret right 
to the parameters of an initialization method


### IMPORTANT ###
You must use apiToken and apiSecret for transaction creation NOT for transaction administration

and please, pay attention to the fact that the defferent tokens and secrets are used for each part of the sdk. E.g. you can't use a Netverify token for BAMCheckout and so on