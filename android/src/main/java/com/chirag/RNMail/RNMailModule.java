package com.chirag.RNMail;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.Callback;

import java.util.List;

/**
 * NativeModule that allows JS to open emails sending apps chooser.
 */

public class RNMailModule extends ReactContextBaseJavaModule {

  ReactApplicationContext reactContext;

  public RNMailModule(ReactApplicationContext reactContext) {
    super(reactContext);
    this.reactContext = reactContext;
  }

  @Override
  public String getName() {
    return "RNMail";
  }

  @ReactMethod
  public void mail(ReadableMap options, Callback callback) {
    Intent i = new Intent(Intent.ACTION_SENDTO);
    i.setData(Uri.parse("mailto:"));

    if (options.hasKey("subject") && !options.isNull("subject")) {
      i.putExtra(Intent.EXTRA_SUBJECT, options.getString("subject"));
    }

    if (options.hasKey("body") && !options.isNull("body")) {
      i.putExtra(Intent.EXTRA_TEXT, options.getString("body"));
    }

    if (options.hasKey("recipients") && !options.isNull("recipients")) {
      ReadableArray r = options.getArray("recipients");
      int length = r.size();
      String[] recipients = new String[length];
      for (int keyIndex = 0; keyIndex < length; keyIndex++) {
        recipients[keyIndex] = r.getString(keyIndex);
      }
      i.putExtra(Intent.EXTRA_EMAIL, recipients);
    }

    PackageManager manager = reactContext.getPackageManager();
    List<ResolveInfo> list = manager.queryIntentActivities(i, 0);

    if (list == null || list.size() == 0) {
      callback.invoke("not_available");
      return;
    }

    Intent chooser = Intent.createChooser(i, "Send Mail");
    chooser.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

    try {
      reactContext.startActivity(chooser);
    } catch (Exception ex) {
      callback.invoke("error");
    }
  }
}
