package com.chirag.RNMail;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.text.Html;
import androidx.core.content.FileProvider;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.Callback;

import java.util.List;
import java.io.File;
import java.net.URI;
import java.util.ArrayList;

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

  /**
    * Converts a ReadableArray to a String array
    *
    * @param r the ReadableArray instance to convert
    *
    * @return array of strings
  */
  private String[] readableArrayToStringArray(ReadableArray r) {
    int length = r.size();
    String[] strArray = new String[length];

    for (int keyIndex = 0; keyIndex < length; keyIndex++) {
      strArray[keyIndex] = r.getString(keyIndex);
    }

    return strArray;
  }

  @ReactMethod
  public void mail(ReadableMap options, Callback callback) {
    Intent i = new Intent(Intent.ACTION_SEND_MULTIPLE);
    Intent selectorIntent = new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:"));
    i.setSelector(selectorIntent);

    if (options.hasKey("subject") && !options.isNull("subject")) {
      i.putExtra(Intent.EXTRA_SUBJECT, options.getString("subject"));
    }

    if (options.hasKey("body") && !options.isNull("body")) {
      String body = options.getString("body");
      if (options.hasKey("isHTML") && options.getBoolean("isHTML")) {
        i.putExtra(Intent.EXTRA_TEXT, Html.fromHtml(body));
      } else {
        i.putExtra(Intent.EXTRA_TEXT, body);
      }
    }

    if (options.hasKey("recipients") && !options.isNull("recipients")) {
      ReadableArray recipients = options.getArray("recipients");
      i.putExtra(Intent.EXTRA_EMAIL, readableArrayToStringArray(recipients));
    }

    if (options.hasKey("ccRecipients") && !options.isNull("ccRecipients")) {
      ReadableArray ccRecipients = options.getArray("ccRecipients");
      i.putExtra(Intent.EXTRA_CC, readableArrayToStringArray(ccRecipients));
    }

    if (options.hasKey("bccRecipients") && !options.isNull("bccRecipients")) {
      ReadableArray bccRecipients = options.getArray("bccRecipients");
      i.putExtra(Intent.EXTRA_BCC, readableArrayToStringArray(bccRecipients));
    }

    if (options.hasKey("attachments") && !options.isNull("attachments")) {
      ReadableArray r = options.getArray("attachments");
      int length = r.size();

      String provider = reactContext.getApplicationContext().getPackageName() + ".rnmail.provider";
      List<ResolveInfo> resolvedIntentActivities = reactContext.getPackageManager().queryIntentActivities(i,
            PackageManager.MATCH_DEFAULT_ONLY);

      ArrayList<Uri> uris = new ArrayList<Uri>();
      for (int keyIndex = 0; keyIndex < length; keyIndex++) {
        ReadableMap clip = r.getMap(keyIndex);
        Uri uri;
        if (clip.hasKey("path") && !clip.isNull("path")) {
          String path = clip.getString("path");
          File file = new File(path);
          uri = FileProvider.getUriForFile(reactContext, provider, file);
        } else if (clip.hasKey("uri") && !clip.isNull("uri")) {
          String uriPath = clip.getString("uri");
          uri = Uri.parse(uriPath);
        } else {
          callback.invoke("not_found");
          return;
        }
        uris.add(uri);

        for (ResolveInfo resolvedIntentInfo : resolvedIntentActivities) {
          String packageName = resolvedIntentInfo.activityInfo.packageName;
          reactContext.grantUriPermission(packageName, uri,
              Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
        }
      }

      i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
      i.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
    }

    PackageManager manager = reactContext.getPackageManager();
    List<ResolveInfo> list = manager.queryIntentActivities(i, 0);

    if (list == null || list.size() == 0) {
      callback.invoke("not_available");
      return;
    }

    if (list.size() == 1) {
      i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
      try {
        reactContext.startActivity(i);
      } catch (Exception ex) {
        callback.invoke("error");
      }
    } else {
      String chooserTitle = "Send Mail";

      if (options.hasKey("customChooserTitle") && !options.isNull("customChooserTitle")) {
        chooserTitle = options.getString("customChooserTitle");
      }

      Intent chooser = Intent.createChooser(i, chooserTitle);
      chooser.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

      try {
        reactContext.startActivity(chooser);
      } catch (Exception ex) {
        callback.invoke("error");
      }
    }
  }
}
