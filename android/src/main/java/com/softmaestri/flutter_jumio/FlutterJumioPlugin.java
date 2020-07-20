package com.softmaestri.flutter_jumio;

import android.app.Activity;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.embedding.engine.plugins.activity.ActivityAware;
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.jumio.auth.AuthenticationCallback;
import com.jumio.auth.AuthenticationResult;
import com.jumio.bam.BamCardInformation;
import com.jumio.bam.enums.CreditCardType;
import com.jumio.commons.camera.JumioCameraManager;
import com.jumio.core.enums.JumioCameraPosition;
import com.jumio.core.enums.JumioDataCenter;
import com.jumio.core.exceptions.PlatformNotSupportedException;
import com.jumio.core.util.DeviceUtil;
import com.jumio.nv.NetverifyDocumentData;
import com.jumio.nv.data.document.NVDocumentType;
import com.jumio.nv.data.document.NVDocumentVariant;
import com.jumio.nv.enums.NVWatchlistScreening;
import com.jumio.sdk.SDKExpiredException;
import com.jumio.MobileSDK;
import com.jumio.auth.AuthenticationSDK;
import com.jumio.bam.BamSDK;
import com.jumio.core.exceptions.MissingPermissionException;
import com.jumio.dv.DocumentVerificationSDK;
import com.jumio.nv.NetverifySDK;


import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.HashMap;
import java.util.Map;

/** FlutterJumioPlugin */
public class FlutterJumioPlugin implements FlutterPlugin, MethodCallHandler, ActivityAware,
        PluginRegistry.RequestPermissionsResultListener, PluginRegistry.ActivityResultListener
{

  private static String TAG = "JumioMobileSDK";
  private static final int PERMISSION_REQUEST_CODE_BAM = 300;
  private static final int PERMISSION_REQUEST_CODE_NETVERIFY = 301;
  private static final int PERMISSION_REQUEST_CODE_DOCUMENT_VERIFICATION = 303;
  private static final int PERMISSION_REQUEST_CODE_AUTHENTICATION = 304;

  private static final String ACTION_BAM_INIT = "InitBAM";
  private static final String ACTION_BAM_START = "StartBAM";
  private static final String ACTION_NV_INIT = "InitNetverify";
  private static final String ACTION_NV_START = "StartNetverify";
  private static final String ACTION_AUTH_INIT = "InitAuthentication";
  private static final String ACTION_AUTH_START = "StartAuthentication";
  private static final String ACTION_DV_INIT = "InitDocumentVerification";
  private static final String ACTION_DV_START = "StartDocumentVerification";

  private Context context;
  private Activity activity;
  private MethodChannel channel;

  private BamSDK bamSDK;
  private NetverifySDK netverifySDK;
  private AuthenticationSDK authenticationSDK;
  private DocumentVerificationSDK documentVerificationSDK;
  private boolean initiateSuccessful = false;

  @Override
  public void onAttachedToEngine(@NonNull FlutterPluginBinding flutterPluginBinding) {
    context = flutterPluginBinding.getApplicationContext();
    channel = new MethodChannel(flutterPluginBinding.getBinaryMessenger(), "flutter_jumio");
    channel.setMethodCallHandler(this);
    Log.i("Info", "Channel created");
  }
  private Context getContext() {
    return context;
  }
  private Activity getCurrentActivity() {
    return activity;
  }

  private void sendNativeMessage(String message) {
    sendToFlutter("onNativeMessage", message);
  }


  private void checkInternetConnection() {
    sendNativeMessage("Start checking internet connection");
    new AsyncRequest().execute();

  }
  class AsyncRequest extends AsyncTask<String, Integer, String> {

    @Override
    protected String doInBackground(String... arg) {
      String networkState = "unavailable";
      try {
        HttpURLConnection urlc = (HttpURLConnection) (new URL("https://google.com").openConnection());
        urlc.setRequestProperty("User-Agent", "Test");
        urlc.setRequestProperty("Connection", "close");
        urlc.setConnectTimeout(1500);
        urlc.connect();
        boolean available = (urlc.getResponseCode() == 200);
        if (available) {
          networkState = "available";
        }
      } catch (Exception e) {
        sendErrorMessage(convertStackTraceToString(e));
      }
      return networkState;
    }

    @Override
    protected void onPostExecute(String s) {
      super.onPostExecute(s);
      sendNativeMessage("Internet: " + s);
    }
  }



  private static String convertStackTraceToString(Throwable throwable)
  {
    try (StringWriter sw = new StringWriter();
         PrintWriter pw = new PrintWriter(sw))
    {
      throwable.printStackTrace(pw);
      return sw.toString();
    }
    catch (IOException ioe)
    {
      throw new IllegalStateException(ioe);
    }
  }

  @Override
  public void onMethodCall(@NonNull MethodCall call, @NonNull Result result) {

    Map args = null;
    if (call.arguments != null) {
      try {
        args = (Map)(call.arguments);
      }
      catch (Exception e) {
        sendErrorMessage(e.getMessage());
      }
    }

    if (call.method.equals("getPlatformVersion")) {
      result.success("Android " + android.os.Build.VERSION.RELEASE);
    }

    if (call.method.equals(ACTION_BAM_INIT)) {
      initBAM(args);
      checkInternetConnection();
    }
    else if (call.method.equals(ACTION_BAM_START)) {
      startBAM();
    }
    else if (call.method.equals(ACTION_NV_INIT)) {
      initNetverify(args);
    }
    else if (call.method.equals(ACTION_NV_START)) {
      startNetverify();
    }
    else if (call.method.equals(ACTION_AUTH_INIT)) {
      initAuthentication(args);
    }
    else if (call.method.equals(ACTION_AUTH_START)) {
      startAuthentication();
    }
    else if (call.method.equals(ACTION_DV_INIT)) {
      initDocumentVerification(args);
    }
    else if (call.method.equals(ACTION_DV_START)) {
      startDocumentVerification();
    }
    else if (call.method.equals("loopback")) {
      String argsFromFlutter = call.arguments.toString();
      Map<String, Object> testParams = new HashMap<>();
      testParams.put("test", "if you see this, the communication between Flutter and Java is ok");
      testParams.put("argsFromFlutter", argsFromFlutter);
      sendToFlutter("onError", testParams);
    }
    else {
      result.notImplemented();
    }
  }

  private void sendToFlutter(String methodName, @Nullable Object arguments) {
    if (channel == null) {
      Log.e("Error crap", "Channel was null");
      return;
    }
    channel.invokeMethod(methodName, arguments);
  }
  private void sendSuccessMessage(String message) {
    Map<String, Object> params = new HashMap<>();
    params.put("successMessage", message);
    sendSuccessObject(params);
  }
  private void sendErrorMessage(String message) {
    Map<String, Object> params = new HashMap<>();
    params.put("errorMessage", message);
    sendErrorObject(params);
  }
  private void sendErrorObject(Object params) {
    sendToFlutter("onError", params);
  }
  private void sendSuccessObject(Object params) {
    sendToFlutter("onSuccess", params);
  }

  private void sendDataToFlutter(@Nullable Object arguments) {
    sendToFlutter("onData", arguments);
  }

  // BAM Checkout

  private void initBAM(Map data) {
    Log.d("initBAM", data.toString());
    if (BamSDK.isRooted(getContext())) {
      showErrorMessage("The BAM SDK can't run on a rooted device.");
      return;
    }

    if (!isPlatformSupported()){
      return;
    }

    try {
      Map options = (Map)data.get("options");
      if (options.containsKey("offlineToken")) {
        String offlineToken = (String)options.get("offlineToken");
        bamSDK = BamSDK.create(getCurrentActivity(), offlineToken);
      } else {
        if (!data.containsKey("apiToken") || !data.containsKey("apiSecret") || !data.containsKey("dataCenter")) {
          showErrorMessage("Missing required parameters apiToken, apiSecret or dataCenter.");
          return;
        }

        String apiToken = (String)data.get("apiToken");
        String apiSecret = (String)data.get("apiSecret");

        JumioDataCenter dataCenter = null;
        dataCenter = JumioDataCenter.valueOf(((String)(data.get("dataCenter"))).toUpperCase());

        sendNativeMessage(String.format(
            "With token: %s, api secret: %s, and a datacenter: %s",
              apiToken, apiSecret, dataCenter.toString()
            )
        );

        sendNativeMessage(
          "BAM initialized with apiToken: " + apiToken +
          ", and secret: " + apiSecret +
          ", data center: " + dataCenter
        );


        bamSDK = BamSDK.create(getCurrentActivity(), apiToken, apiSecret, dataCenter);
      }

      // Configuration options
      try {
        if (data.containsKey("options")) {
          options = (Map)data.get("options");
          Iterator<String> keys = options.keySet().iterator();
          while (keys.hasNext()) {
            String key = keys.next();

            if (key.equalsIgnoreCase("cardHolderNameRequired")) {
              bamSDK.setCardHolderNameRequired((boolean)options.get(key));
            }
            else if (key.equalsIgnoreCase("sortCodeAndAccountNumberRequired")) {
              bamSDK.setSortCodeAndAccountNumberRequired((boolean)options.get(key));
            }
            else if (key.equalsIgnoreCase("expiryRequired")) {
              bamSDK.setExpiryRequired((boolean)options.get(key));
            }
            else if (key.equalsIgnoreCase("cvvRequired")) {
              bamSDK.setCvvRequired((boolean)options.get(key));
            }
            else if (key.equalsIgnoreCase("expiryEditable")) {
              bamSDK.setExpiryEditable((boolean)options.get(key));
            }
            else if (key.equalsIgnoreCase("cardHolderNameEditable")) {
              bamSDK.setCardHolderNameEditable((boolean)options.get(key));
            }
            else if (key.equalsIgnoreCase("merchantReportingCriteria")) {
              bamSDK.setMerchantReportingCriteria((String)options.get(key));
            }
            else if (key.equalsIgnoreCase("vibrationEffectEnabled")) {
              bamSDK.setVibrationEffectEnabled((boolean)options.get(key));
            }
            else if (key.equalsIgnoreCase("enableFlashOnScanStart")) {
              bamSDK.setEnableFlashOnScanStart((boolean)options.get(key));
            }
            else if (key.equalsIgnoreCase("cardNumberMaskingEnabled")) {
              bamSDK.setCardNumberMaskingEnabled((boolean)options.get(key));
            }
            else if (key.equalsIgnoreCase("cameraPosition")) {
              JumioCameraPosition cameraPosition = (((String)(options.get(key))).toLowerCase().equals("front"))
                      ? JumioCameraPosition.FRONT
                      : JumioCameraPosition.BACK;
              bamSDK.setCameraPosition(cameraPosition);
            }
            else if (key.equalsIgnoreCase("cardTypes")) {
              List jsonTypes = (ArrayList<String>)options.get(key);
              ArrayList<String> types = new ArrayList<String>();
              if (jsonTypes != null) {
                int len = jsonTypes.size();
                for (int i = 0; i < len; i++) {
                  types.add(jsonTypes.get(i).toString());
                }
              }

              ArrayList<CreditCardType> creditCardTypes = new ArrayList<CreditCardType>();
              for (String type : types) {
                if (type.toLowerCase().equals("visa")) {
                  creditCardTypes.add(CreditCardType.VISA);
                }
                else if (type.toLowerCase().equals("master_card")) {
                  creditCardTypes.add(CreditCardType.MASTER_CARD);
                }
                else if (type.toLowerCase().equals("american_express")) {
                  creditCardTypes.add(CreditCardType.AMERICAN_EXPRESS);
                }
                else if (type.toLowerCase().equals("china_unionpay")) {
                  creditCardTypes.add(CreditCardType.CHINA_UNIONPAY);
                }
                else if (type.toLowerCase().equals("diners_club")) {
                  creditCardTypes.add(CreditCardType.DINERS_CLUB);
                }
                else if (type.toLowerCase().equals("discover")) {
                  creditCardTypes.add(CreditCardType.DISCOVER);
                }
                else if (type.toLowerCase().equals("jcb")) {
                  creditCardTypes.add(CreditCardType.JCB);
                }
              }

              bamSDK.setSupportedCreditCardTypes(creditCardTypes);
            }
            sendNativeMessage("Adding key to bam: " + key + " value: " + options.get(key));
          }
        }
      } catch (Exception e) {
        sendErrorMessage(e.getMessage());
      }
    }
    catch (PlatformNotSupportedException e) {
      showErrorMessage("Error initializing the BAM SDK: " + e.getLocalizedMessage());
    }
    catch (SDKExpiredException e) {
      showErrorMessage("Error initializing the BAM SDK: " + e.getLocalizedMessage());
    }
    catch (Exception e) {
      showErrorMessage("Invalid parameters: " + e.getLocalizedMessage());
    }
  }

  private void startBAM() {
    Log.d("startBAM", "");
    if (bamSDK == null) {
      showErrorMessage("The BAM SDK is not initialized yet. Call initBAM() first.");
      return;
    }

    Runnable runnable = new Runnable() {
      @Override
      public void run() {
        try {
          sendNativeMessage("CHECKING PERMISSIONS FOR BAM");
          checkPermissionsAndStart(bamSDK);
        } catch (Exception e) {
          showErrorMessage("Error starting the BAM SDK: " + e.getLocalizedMessage());
        }
      }
    };
    this.getCurrentActivity().runOnUiThread(runnable);
  }

  private boolean isPlatformSupported() {
    if (Build.VERSION.SDK_INT < 16) {
      showErrorMessage("SDK Version 16 required");
      return false;
    } else if (JumioCameraManager.getNumberOfCameras(context) == 0) {
      showErrorMessage("No useable camera present");
      return false;
    } else if (!DeviceUtil.isSupportedPlatform(false)) {
      showErrorMessage("ARMv7 CPU Architecture with NEON Intrinsics required");
      return false;
    }
    return true;
  }

  // Netverify
  private void initNetverify(Map data) {
    Log.d("initNetverify", data.toString());
    sendNativeMessage("initNetverify() with args: " + data.toString());

    if (!isPlatformSupported()) {
      return;
    }

    try {
      if (!data.containsKey("apiToken") || !data.containsKey("apiSecret") || !data.containsKey("dataCenter")) {
        showErrorMessage("Missing required parameters apiToken, apiSecret or dataCenter.");
        return;
      }

      String apiToken = (String)data.get("apiToken");
      String apiSecret = (String)data.get("apiSecret");

      JumioDataCenter dataCenter = null;
      dataCenter = JumioDataCenter.valueOf(((String)data.get("dataCenter")).toUpperCase());

      sendNativeMessage(
        "Netverify init with token: " + apiToken +
        ", secret: " + apiSecret +
        ", data center: " + dataCenter
      );

      netverifySDK = NetverifySDK.create(getCurrentActivity(), apiToken, apiSecret, dataCenter);

      // Configuration options
      if (data.containsKey("options")) {
        Map options = (Map)data.get("options");
        Iterator<String> keys = options.keySet().iterator();
        while (keys.hasNext()) {
          String key = keys.next();

          if (key.equalsIgnoreCase("enableVerification")) {
            netverifySDK.setEnableVerification((boolean)options.get(key));
          }
          else if (key.equalsIgnoreCase("callbackUrl")) {
            netverifySDK.setCallbackUrl((String)options.get(key));
          }
          else if (key.equalsIgnoreCase("enableIdentityVerification")) {
            netverifySDK.setEnableIdentityVerification((boolean)options.get(key));
          }
          else if (key.equalsIgnoreCase("preselectedCountry")) {
            netverifySDK.setPreselectedCountry((String)options.get(key));
          }
          else if (key.equalsIgnoreCase("customerInternalReference")) {
            netverifySDK.setCustomerInternalReference((String)options.get(key));
          }
          else if (key.equalsIgnoreCase("reportingCriteria")) {
            netverifySDK.setReportingCriteria((String)options.get(key));
          }
          else if (key.equalsIgnoreCase("userReference")) {
            netverifySDK.setUserReference((String)options.get(key));
          }
          else if (key.equalsIgnoreCase("enableEpassport")) {
            netverifySDK.setEnableEMRTD((boolean)options.get(key));
          }
          else if (key.equalsIgnoreCase("enableWatchlistScreening")) {
            NVWatchlistScreening watchlistScreeningState;
            switch (((String)options.get(key)).toLowerCase()) {
              case "enabled": watchlistScreeningState = NVWatchlistScreening.ENABLED;
                break;
              case "disabled": watchlistScreeningState = NVWatchlistScreening.DISABLED;
                break;
              default: watchlistScreeningState = NVWatchlistScreening.DEFAULT;
                break;
            }
            netverifySDK.setWatchlistScreening(watchlistScreeningState);
          }
          else if (key.equalsIgnoreCase("watchlistSearchProfile")) {
            netverifySDK.setWatchlistSearchProfile((String)options.get(key));
          }
          else if (key.equalsIgnoreCase("sendDebugInfoToJumio")) {
            netverifySDK.sendDebugInfoToJumio((boolean)options.get(key));
          }
          else if (key.equalsIgnoreCase("dataExtractionOnMobileOnly")) {
            netverifySDK.setDataExtractionOnMobileOnly((boolean)options.get(key));
          }
          else if (key.equalsIgnoreCase("cameraPosition")) {
            JumioCameraPosition cameraPosition = (((String)options.get(key)).toLowerCase().equals("front"))
                    ? JumioCameraPosition.FRONT
                    : JumioCameraPosition.BACK;
            netverifySDK.setCameraPosition(cameraPosition);
          }
          else if (key.equalsIgnoreCase("preselectedDocumentVariant")) {
            NVDocumentVariant variant = (((String)options.get(key)).toLowerCase().equals("paper"))
                    ? NVDocumentVariant.PAPER
                    : NVDocumentVariant.PLASTIC;
            netverifySDK.setPreselectedDocumentVariant(variant);
          }
          else if (key.equalsIgnoreCase("documentTypes")) {
            ArrayList<String> jsonTypes = (ArrayList<String>)options.get(key);
            ArrayList<String> types = new ArrayList<String>();
            if (jsonTypes != null) {
              int len = jsonTypes.size();
              for (int i = 0; i < len; i++) {
                types.add(jsonTypes.get(i).toString());
              }
            }

            ArrayList<NVDocumentType> documentTypes = new ArrayList<NVDocumentType>();
            for (String type : types) {
              if (type.toLowerCase().equals("passport")) {
                documentTypes.add(NVDocumentType.PASSPORT);
              }
              else if (type.toLowerCase().equals("driver_license")) {
                documentTypes.add(NVDocumentType.DRIVER_LICENSE);
              }
              else if (type.toLowerCase().equals("identity_card")) {
                documentTypes.add(NVDocumentType.IDENTITY_CARD);
              }
              else if (type.toLowerCase().equals("visa")) {
                documentTypes.add(NVDocumentType.VISA);
              }
            }

            netverifySDK.setPreselectedDocumentTypes(documentTypes);
          }
        }
      }
    }
    catch (PlatformNotSupportedException e) {
      showErrorMessage("Error initializing the Netverify SDK: " + e.getLocalizedMessage());
    }
    catch (Exception e) {
      showErrorMessage("Invalid parameters: " + e.getLocalizedMessage());
    }
  }

  private void startNetverify() {
    Log.d("startNetverify", "");
    sendNativeMessage("Starting netverify");
    if (netverifySDK == null) {
      showErrorMessage("The Netverify SDK is not initialized yet. Call initNetverify() first.");
      return;
    }

    Runnable runnable = new Runnable() {
      @Override
      public void run() {
        try {
          checkPermissionsAndStart(netverifySDK);
        }
        catch (Exception e) {
          showErrorMessage("Error starting the Netverify SDK: " + e.getLocalizedMessage());
        }
      }
    };

    this.getCurrentActivity().runOnUiThread(runnable);
    sendNativeMessage("Has run netverify on UI thread");
  }

  // Authentication

  private void initAuthentication(Map data){
    Log.d("initAuthentication", data.toString());
    if (!isPlatformSupported()){
      return;
    }

    try{
      if (!data.containsKey("apiToken") || !data.containsKey("apiSecret") || !data.containsKey("dataCenter")) {
        showErrorMessage("Missing required parameters apiToken, apiSecret or dataCenter.");
        return;
      }

      String apiToken = (String)data.get("apiToken");
      String apiSecret = (String)data.get("apiSecret");

      JumioDataCenter dataCenter = null;
      dataCenter = JumioDataCenter.valueOf(((String)data.get("dataCenter")).toUpperCase());

      authenticationSDK = AuthenticationSDK.create(getCurrentActivity(), apiToken, apiSecret, dataCenter);

      // Configuration options
      String enrollmentTransactionReference = null;
      String authenticationTransactionReference = null;
      if (data.containsKey("options")){
        Map options = (Map)data.get("options");
        Iterator<String> keys = options.keySet().iterator();
        while (keys.hasNext()){
          String key = keys.next();

          if (key.equalsIgnoreCase("userReference")){
            authenticationSDK.setUserReference((String)options.get(key));
          }
          else if (key.equalsIgnoreCase("enrollmentTransactionReference")){
            enrollmentTransactionReference = (String)options.get(key);
          }
          else if (key.equalsIgnoreCase("authenticationTransactionReference")){
            authenticationTransactionReference = (String)options.get(key);
          }
          else if (key.equalsIgnoreCase("callbackUrl")) {
            authenticationSDK.setCallbackUrl((String)options.get(key));
          }
        }
      }

      if (enrollmentTransactionReference != null || authenticationTransactionReference != null){
        if (authenticationTransactionReference != null) {
          authenticationSDK.setAuthenticationTransactionReference(authenticationTransactionReference);
        } else {
          authenticationSDK.setEnrollmentTransactionReference(enrollmentTransactionReference);
        }

        authenticationSDK.initiate(new AuthenticationCallback(){
          @Override
          public void onAuthenticationInitiateSuccess(){
            initiateSuccessful = true;
            sendSuccessMessage("Authentication SDK initialized successfully");
          }

          @Override
          public void onAuthenticationInitiateError(String errorCode, String errorMessage, boolean retryPossible){
            initiateSuccessful = false;
            showErrorMessage("Authentication initiate failed - " + errorCode + ": " + errorMessage);
          }
        });
      }
    }
    catch (PlatformNotSupportedException e) {
      showErrorMessage("Error initializing the Authentication SDK: " + e.getLocalizedMessage());
    }
    catch (MissingPermissionException e) {
      showErrorMessage("Missing permission: " + e.getLocalizedMessage());
    }
    catch (Exception e) {
      showErrorMessage("Invalid parameters: " + e.getLocalizedMessage());
    }
  }


  private void startAuthentication() {
    if (authenticationSDK == null || !initiateSuccessful) {
      showErrorMessage("The Authentication SDK has not been initialized with a valid transaction reference.");
      return;
    }

    Runnable runnable = new Runnable() {
      @Override
      public void run() {
        try {
          checkPermissionsAndStart(authenticationSDK);
        } catch (Exception e) {
          showErrorMessage("Error starting the Authentication SDK: " + e.getLocalizedMessage());
        }
      }
    };

    this.getCurrentActivity().runOnUiThread(runnable);
  }

  // Document Verification

  private void initDocumentVerification(Map data) {
    if (!isPlatformSupported()){
      return;
    }

    try {
      if (!data.containsKey("apiToken") || !data.containsKey("apiSecret") || !data.containsKey("dataCenter")) {
        showErrorMessage("Missing required parameters apiToken, apiSecret or dataCenter.");
        return;
      }

      String apiToken = (String)data.get("apiToken");
      String apiSecret = (String)data.get("apiSecret");

      JumioDataCenter dataCenter = null;
      dataCenter = JumioDataCenter.valueOf(((String)data.get("dataCenter")).toUpperCase());

      documentVerificationSDK = DocumentVerificationSDK.create(getCurrentActivity(), apiToken, apiSecret, dataCenter);

      // Configuration options
      if (data.containsKey("options")) {
        Map options = (Map)data.get("options");
        Iterator<String> keys = options.keySet().iterator();
        while (keys.hasNext()) {
          String key = keys.next();

          if (key.equalsIgnoreCase("type")) {
            documentVerificationSDK.setType((String)options.get(key));
          }
          else if (key.equalsIgnoreCase("customDocumentCode")) {
            documentVerificationSDK.setCustomDocumentCode((String)options.get(key));
          }
          else if (key.equalsIgnoreCase("country")) {
            documentVerificationSDK.setCountry((String)options.get(key));
          }
          else if (key.equalsIgnoreCase("reportingCriteria")) {
            documentVerificationSDK.setReportingCriteria((String)options.get(key));
          }
          else if (key.equalsIgnoreCase("callbackUrl")) {
            documentVerificationSDK.setCallbackUrl((String)options.get(key));
          }
          else if (key.equalsIgnoreCase("customerInternalReference")) {
            documentVerificationSDK.setCustomerInternalReference((String)options.get(key));
          }
          else if (key.equalsIgnoreCase("userReference")) {
            documentVerificationSDK.setUserReference((String)options.get(key));
          }
          else if (key.equalsIgnoreCase("documentName")) {
            documentVerificationSDK.setDocumentName((String)options.get(key));
          }
          else if (key.equalsIgnoreCase("cameraPosition")) {
            JumioCameraPosition cameraPosition = (((String)options.get(key)).toLowerCase().equals("front"))
                    ? JumioCameraPosition.FRONT
                    : JumioCameraPosition.BACK;
            documentVerificationSDK.setCameraPosition(cameraPosition);
          }
          else if (key.equalsIgnoreCase("enableExtraction")) {
            documentVerificationSDK.setEnableExtraction((boolean)options.get(key));
          }
        }
      }
    }
    catch (PlatformNotSupportedException e) {
      showErrorMessage("Error initializing the Document Verification SDK: " + e.getLocalizedMessage());
    }
    catch (Exception e) {
      showErrorMessage("Invalid parameters: " + e.getLocalizedMessage());
    }
  }

  private void startDocumentVerification() {
    Log.d("documentVerification", "");
    if (documentVerificationSDK == null) {
      showErrorMessage("The Document Verification SDK is not initialized yet. Call initDocumentVerification() first.");
      return;
    }

    Runnable runnable = new Runnable() {
      @Override
      public void run() {
        try {
          checkPermissionsAndStart(documentVerificationSDK);
        } catch (Exception e) {
          showErrorMessage("Error starting the Document Verification SDK: " + e.getLocalizedMessage());
        }
      }
    };
    this.getCurrentActivity().runOnUiThread(runnable);
  }

  // Permissions
  private void checkPermissionsAndStart(MobileSDK sdk) {

    if (!MobileSDK.hasAllRequiredPermissions(getContext())){
      //Acquire missing permissions.
      String[] mp = MobileSDK.getMissingPermissions(getContext());

      int code;
      if (sdk instanceof BamSDK){
        code = PERMISSION_REQUEST_CODE_BAM;
      }
      else if (sdk instanceof AuthenticationSDK){
        code = PERMISSION_REQUEST_CODE_AUTHENTICATION;
      }
      else if (sdk instanceof NetverifySDK){
        code = PERMISSION_REQUEST_CODE_NETVERIFY;
      }
      else if (sdk instanceof DocumentVerificationSDK){
        code = PERMISSION_REQUEST_CODE_DOCUMENT_VERIFICATION;
      }
      else {
        showErrorMessage("Invalid SDK instance");
        return;
      }
      sendNativeMessage("Checking required permissions");
      ActivityCompat.requestPermissions(getCurrentActivity(), mp, code);
    }
    else {
      sendNativeMessage("Starting Java SDK");
      this.startSdk(sdk);
    }
  }
  @Override
  public boolean onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
    Log.d("onPermissionsResult",
  "Request code: " + requestCode +
      ", permissions: " + TextUtils.join(",", permissions) +
      ", grant result: " + grantResults.toString()
    );
    boolean allGranted = true;
    for (int grantResult : grantResults) {
      if (grantResult != PackageManager.PERMISSION_GRANTED) {
        allGranted = false;
        break;
      }
    }
    if (allGranted) {
      if (requestCode == PERMISSION_REQUEST_CODE_BAM) {
        startSdk(this.bamSDK);
      }
      else if (requestCode == PERMISSION_REQUEST_CODE_NETVERIFY) {
        startSdk(this.netverifySDK);
      }
      else if (requestCode == PERMISSION_REQUEST_CODE_AUTHENTICATION) {
        startSdk(this.authenticationSDK);
      }
      else if (requestCode == PERMISSION_REQUEST_CODE_DOCUMENT_VERIFICATION) {
        startSdk(this.documentVerificationSDK);
      }
    }
    else {
      showErrorMessage("You need to grant all required permissions to continue");
    }
    return allGranted;
  }


  // SDK Result
  public boolean onActivityResult(int requestCode, int resultCode, Intent intent) {
    Log.i("onActivityResult", "Request code " + requestCode + ", result code: " + resultCode);
    sendNativeMessage("onActivityResult: Request code " + requestCode + ", result code: " + resultCode);
    // BAM Checkout Results
    if (requestCode == BamSDK.REQUEST_CODE) {
      if (intent == null) {
        return false;
      }
      if (resultCode == Activity.RESULT_OK) {
        BamCardInformation cardInformation = intent.getParcelableExtra(BamSDK.EXTRA_CARD_INFORMATION);

        Map<String, Object> result = new HashMap<>();
        try {
          result.put("cardType", cardInformation.getCardType());
          result.put("cardNumber", String.valueOf(cardInformation.getCardNumber()));
          result.put("cardNumberGrouped", String.valueOf(cardInformation.getCardNumberGrouped()));
          result.put("cardNumberMasked", String.valueOf(cardInformation.getCardNumberMasked()));
          result.put("cardExpiryMonth", String.valueOf(cardInformation.getCardExpiryDateMonth()));
          result.put("cardExpiryYear", String.valueOf(cardInformation.getCardExpiryDateYear()));
          result.put("cardExpiryDate", String.valueOf(cardInformation.getCardExpiryDateYear()));
          result.put("cardCVV", String.valueOf(cardInformation.getCardCvvCode()));
          result.put("cardHolderName", String.valueOf(cardInformation.getCardHolderName()));
          result.put("cardSortCode", String.valueOf(cardInformation.getCardSortCode()));
          result.put("cardAccountNumber", String.valueOf(cardInformation.getCardAccountNumber()));
          result.put("cardSortCodeValid", cardInformation.isCardSortCodeValid());
          result.put("cardAccountNumberValid", cardInformation.isCardAccountNumberValid());

          ArrayList<String> scanReferenceList = intent.getStringArrayListExtra(BamSDK.EXTRA_SCAN_ATTEMPTS);
          if (scanReferenceList != null && scanReferenceList.size() > 0) {
            for (int i = scanReferenceList.size() - 1; i >= 0; i--) {
              result.put(String.format(Locale.getDefault(), "Scan reference %d", i), scanReferenceList.get(i));
            }
          } else {
            result.put("Scan reference 0", "N/A");
          }

          sendSuccessObject(result);
          cardInformation.clear();
        }
        catch (Exception e) {
          showErrorMessage("Result could not be sent. Try again.");
        }
      }
      else if (resultCode == Activity.RESULT_CANCELED) {
        String errorCode = intent.getStringExtra(BamSDK.EXTRA_ERROR_CODE);
        String errorMsg = intent.getStringExtra(BamSDK.EXTRA_ERROR_MESSAGE);
        ArrayList<String> scanReferenceList = intent.getStringArrayListExtra(BamSDK.EXTRA_SCAN_ATTEMPTS);
        String scanRef = null;
        if (scanReferenceList != null && scanReferenceList.size() > 0) {
          scanRef = scanReferenceList.get(0);
        }
        sendErrorObject(errorCode, errorMsg, scanRef != null ? scanRef : "");
      }

      if (bamSDK != null) {
        sendNativeMessage("Bam destroyed");
        bamSDK.destroy();
        bamSDK = null;
      }
      // Netverify Results
    }
    else if (requestCode == NetverifySDK.REQUEST_CODE){
      if (intent == null){
        return false;
      }
      String scanReference = intent.getStringExtra(NetverifySDK.EXTRA_SCAN_REFERENCE) != null
              ? intent.getStringExtra(NetverifySDK.EXTRA_SCAN_REFERENCE)
              : "";

      if (resultCode == Activity.RESULT_OK){
        NetverifyDocumentData documentData = intent.getParcelableExtra(NetverifySDK.EXTRA_SCAN_DATA);
        Map<String, Object> result = new HashMap<>();
        try{
          result.put("scanReference", scanReference);
          result.put("selectedCountry", documentData.getSelectedCountry());
          result.put("selectedDocumentType", documentData.getSelectedDocumentType());
          result.put("idNumber", documentData.getIdNumber());
          result.put("personalNumber", documentData.getPersonalNumber());
          result.put("issuingDate", documentData.getIssuingDate());
          result.put("expiryDate", documentData.getExpiryDate());
          result.put("issuingCountry", documentData.getIssuingCountry());
          result.put("lastName", documentData.getLastName());
          result.put("firstName", documentData.getFirstName());
          result.put("dob", documentData.getDob());
          result.put("gender", documentData.getGender());
          result.put("originatingCountry", documentData.getOriginatingCountry());
          result.put("addressLine", documentData.getAddressLine());
          result.put("city", documentData.getCity());
          result.put("subdivision", documentData.getSubdivision());
          result.put("postCode", documentData.getPostCode());
          result.put("optionalData1", documentData.getOptionalData1());
          result.put("optionalData2", documentData.getOptionalData2());
          result.put("placeOfBirth", documentData.getPlaceOfBirth());
          result.put("extractionMethod", documentData.getExtractionMethod());

          // MRZ data if available
          if (documentData.getMrzData() != null){
            Map<String, Object> mrzData = new HashMap<>();
            mrzData.put("format", documentData.getMrzData().getFormat());
            mrzData.put("line1", documentData.getMrzData().getMrzLine1());
            mrzData.put("line2", documentData.getMrzData().getMrzLine2());
            mrzData.put("line3", documentData.getMrzData().getMrzLine3());
            mrzData.put("idNumberValid", documentData.getMrzData().idNumberValid());
            mrzData.put("dobValid", documentData.getMrzData().dobValid());
            mrzData.put("expiryDateValid", documentData.getMrzData().expiryDateValid());
            mrzData.put("personalNumberValid", documentData.getMrzData().personalNumberValid());
            mrzData.put("compositeValid", documentData.getMrzData().compositeValid());
            result.put("mrzData", mrzData);
          }
          sendSuccessObject(result);
        }
        catch (Exception e){
          showErrorMessage("Result could not be sent: " + e.getLocalizedMessage());
        }
      }
      else if (resultCode == Activity.RESULT_CANCELED){
        String errorCode = intent.getStringExtra(NetverifySDK.EXTRA_ERROR_CODE);
        String errorMsg = intent.getStringExtra(NetverifySDK.EXTRA_ERROR_MESSAGE);
        sendErrorObject(errorCode, errorMsg, scanReference);
      }

      if (netverifySDK != null) {
        sendNativeMessage("Netverify destroyed");
        netverifySDK.destroy();
        netverifySDK = null;
      }

      // Authentication results
    }
    else if (requestCode == AuthenticationSDK.REQUEST_CODE) {
      if (intent == null)
        return false;
      if (resultCode == Activity.RESULT_OK) {
        String transactionReference = intent.getStringExtra(AuthenticationSDK.EXTRA_TRANSACTION_REFERENCE);
        AuthenticationResult authenticationResult = (AuthenticationResult) intent.getSerializableExtra(AuthenticationSDK.EXTRA_SCAN_DATA);
        try {
          Map<String, Object> result = new HashMap<>();
          result.put("transactionReference", transactionReference);
          result.put("authenticationResult", authenticationResult.toString());
          sendSuccessObject(result);
        }
        catch (Exception e) {
          showErrorMessage("Result could not be sent: " + e.getLocalizedMessage());
        }
      }
      else if (resultCode == Activity.RESULT_CANCELED) {
        String errorMessage = intent.getStringExtra(AuthenticationSDK.EXTRA_ERROR_MESSAGE);
        String errorCode = intent.getStringExtra(AuthenticationSDK.EXTRA_ERROR_CODE);
        Log.e(TAG, errorCode != null ? errorCode : "" + " " + errorMessage);
        sendErrorObject(errorCode, errorMessage, null);
      }

      //At this point, the SDK is not needed anymore. It is highly advisable to call destroy(), so that
      //internal resources can be freed.
      if (authenticationSDK != null) {
        sendNativeMessage("Authentication sdk destroyed");
        authenticationSDK.destroy();
        authenticationSDK = null;
      }

      // Document Verification Results
    }
    else if (requestCode == DocumentVerificationSDK.REQUEST_CODE) {
      String scanReference = intent.getStringExtra(DocumentVerificationSDK.EXTRA_SCAN_REFERENCE) != null
              ? intent.getStringExtra(DocumentVerificationSDK.EXTRA_SCAN_REFERENCE)
              : "";

      if (resultCode == Activity.RESULT_OK) {
        try {
          Map<String, Object> result = new HashMap<>();
          result.put("successMessage", "Document-Verification finished successfully.");
          result.put("scanReference", scanReference);
          sendSuccessObject(result);
        } catch (Exception e) {
          showErrorMessage("Result could not be sent: " + e.getLocalizedMessage());
        }
      } else if (resultCode == Activity.RESULT_CANCELED) {
        String errorCode = intent.getStringExtra(DocumentVerificationSDK.EXTRA_ERROR_CODE);
        String errorMsg = intent.getStringExtra(DocumentVerificationSDK.EXTRA_ERROR_MESSAGE);
        Log.e(TAG, errorCode != null ? errorCode : "" + " " + errorMsg);
        sendErrorObject(errorCode, errorMsg, scanReference);
      }

      if (documentVerificationSDK != null) {
        sendNativeMessage("Document verification sdk destroyed");
        documentVerificationSDK.destroy();
        documentVerificationSDK = null;
      }
    }
    return true;
  }

  // Helper methods

  private void startSdk(MobileSDK sdk) {
    try {
      sendNativeMessage("STARTING SDK " + sdk.getClass().getName());
      sdk.start();
    } catch (MissingPermissionException e) {
      Toast.makeText(getCurrentActivity(), e.getMessage(), Toast.LENGTH_LONG).show();
    }
  }

  private void showErrorMessage(String msg) {
    Log.e(TAG, msg);
    try{
      Map<String, Object> errorResult = new HashMap<>();
      errorResult.put("errorMessage", msg != null ? msg : "");
      sendErrorObject(errorResult);
    }
    catch (Exception e) {
      Log.e(TAG, e.getLocalizedMessage());
    }
  }

  private void sendErrorObject(String errorCode, String errorMsg, String scanReference) {
    try {
      Map<String, Object> errorResult = new HashMap<>();
      errorResult.put("errorCode", errorCode != null ? errorCode : "");
      errorResult.put("errorMessage", errorMsg != null ? errorMsg : "");
      errorResult.put("scanReference", scanReference != null ? scanReference : "");
      sendErrorObject(errorResult);
    }
    catch (Exception e) {
      showErrorMessage("Result could not be sent: " + e.getLocalizedMessage());
    }
  }

  @Override
  public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
  }

  @Override
  public void onAttachedToActivity(ActivityPluginBinding binding) {
    activity = binding.getActivity();
  }

  @Override
  public void onDetachedFromActivityForConfigChanges() {
    activity = null;
  }

  @Override
  public void onReattachedToActivityForConfigChanges(ActivityPluginBinding binding) {
    activity = binding.getActivity();
  }

  @Override
  public void onDetachedFromActivity() {
    activity = null;
  }


}
