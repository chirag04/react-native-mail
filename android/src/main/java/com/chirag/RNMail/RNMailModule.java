package com.chirag.RNMail;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.support.v4.content.FileProvider;
import android.text.Html;
import android.util.Log;

import com.facebook.common.file.FileUtils;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
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

  /**
   * Converts a ReadableArray to a String array
   *
   * @param r the ReadableArray instance to convert
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
    i.setType("message/rfc822");

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
      ArrayList<Uri> uris = new ArrayList<Uri>();
      for (int keyIndex = 0; keyIndex < length; keyIndex++) {
        ReadableMap clip = r.getMap(keyIndex);
        if (clip.hasKey("path") && !clip.isNull("path")) {
          String path = clip.getString("path");
          Log.d ("RNMail", "Attachment file path: " + path);

          File file = new File(path);

          String name, suffix = "";
          if (clip.hasKey("name"))
            name = clip.getString("name");
          else
            name = file.getName();

          if (clip.hasKey("type"))
            suffix = "." + clip.getString("type");


          file.setReadable(true, false);
          if (file.exists()) {

            if (file.length() == 0)
              Log.d ("RNMail", "Warning, attaching empty file!");
            // Use the FileProvider to get a content URI
            try {
              Uri fileUri = FileProvider.getUriForFile(
                      getCurrentActivity(),
                      reactContext.getPackageName() + ".fileprovider",
                      file);
              if (fileUri != null) {
                // Grant temporary read permission to the content URI
                uris.add(fileUri);
              }
            } catch (Exception e) {
              String message = "There was a problem sharing the file " + file.getName();
              Log.e("RNMail", message);
              callback.invoke("error", message + "\n" + e.getMessage());
            }
          } else {
            Log.e("RNMail", "Attachment file does not exist");
          }
        }
      }
      i.setType("*/*");
      i.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
      i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
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
      } catch (Exception e) {
        callback.invoke("error", e.getMessage());
      }
    } else {
      Intent chooser = Intent.createChooser(i, "Send email...");
      chooser.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
      try {
        reactContext.startActivity(chooser);
      } catch (Exception e) {
        callback.invoke("error", e.getMessage());
      }

    }
  }
}
