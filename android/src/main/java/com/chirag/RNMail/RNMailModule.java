package com.chirag.RNMail;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
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

          File temporaryFile = null;
          try {
            temporaryFile = File.createTempFile(name, suffix, reactContext.getExternalCacheDir());
            copy (file, temporaryFile);
          } catch (IOException e) {
            e.printStackTrace();
            Log.e("RNMail", "Error copying to temporary file");
          }

          temporaryFile.setReadable(true, false);
          if (temporaryFile.exists()) {
            if (temporaryFile.length() == 0)
              Log.d ("RNMail", "Warning, attaching empty file!");
            uris.add(Uri.fromFile(temporaryFile));
          } else {
            Log.e("RNMail", "Attachment file does not exist");
          }
        }
      }
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
      Intent chooser = Intent.createChooser(i, "Send email...");
      chooser.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
      try {
        reactContext.startActivity(chooser);
      } catch (Exception ex) {
        callback.invoke("error");
      }

    }
  }

  protected static void copy(File src, File dst) throws IOException {
    InputStream in = new FileInputStream(src);
    try {
      OutputStream out = new FileOutputStream(dst);
      try {
        // Transfer bytes from in to out
        byte[] buf = new byte[1024];
        int len;
        while ((len = in.read(buf)) > 0) {
          out.write(buf, 0, len);
        }
      } finally {
        out.close();
      }
    } finally {
      in.close();
    }
  }
}
