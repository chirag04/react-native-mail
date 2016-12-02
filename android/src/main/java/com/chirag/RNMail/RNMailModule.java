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
import java.io.File;
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
        i.setType("text/plain");
        
        if (options.hasKey("subject") && !options.isNull("subject")) {
            i.putExtra(Intent.EXTRA_SUBJECT, options.getString("subject"));
        }
        
        if (options.hasKey("body") && !options.isNull("body")) {
            i.putExtra(Intent.EXTRA_TEXT, options.getString("body"));
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
        
        if (options.hasKey("attachment") && !options.isNull("attachment")) {
            ReadableArray attachments = options.getArray("attachment");
            ArrayList<Uri> uris = new ArrayList<Uri>();
            for(int j = 0; j < attachments.size(); j++){
                ReadableMap attachment = attachments.getMap(j);
                if(attachment.hasKey("path") && !attachment.isNull("path")){
                    String path = attachment.getString("path");
                    File file = new File(path);
                    Uri p = Uri.fromFile(file);
                    uris.add(p);
                }
            }
            i.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
            /*
             ReadableMap attachment = options.getMap("attachment");
             if (attachment.hasKey("path") && !attachment.isNull("path")) {
             String path = attachment.getString("path");
             File file = new File(path);
             Uri p = Uri.fromFile(file);
             i.putExtra(Intent.EXTRA_STREAM, p);
             } */
        }
        
        PackageManager manager = reactContext.getPackageManager();
        List<ResolveInfo> list = manager.queryIntentActivities(i, PackageManager.MATCH_ALL);
        
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
            Intent chooser = Intent.createChooser(i, "Send Mail");
            chooser.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            
            try {
                reactContext.startActivity(chooser);
            } catch (Exception ex) {
                callback.invoke("error");
            }
        }
    }
}
