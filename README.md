# react-native-mail

A React Native wrapper for Apple's ``MFMailComposeViewController`` from iOS and Mail Intent on android
Supports emails with attachments.

### Installation

For React Native >= 0.40:

```bash
npm install --save react-native-mail@next
```

For React Native < 0.40:

```bash
npm install --save react-native-mail@2
```

To automagically link to your project:

```bash
react-native link react-native-mail
```

### Add it manually to your Android project

* In `android/setting.gradle`

```gradle
...
include ':RNMail', ':app'
project(':RNMail').projectDir = new File(rootProject.projectDir, '../node_modules/react-native-mail/android')
```

* In `android/app/build.gradle`

```gradle
...
dependencies {
    ...
    compile project(':RNMail')
}
```

* register module (in MainActivity.java) if MainActivity extends Activity


```java
import com.chirag.RNMail.*;  // <--- import

public class MainActivity extends Activity implements DefaultHardwareBackBtnHandler {
  ......

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    mReactRootView = new ReactRootView(this);

    mReactInstanceManager = ReactInstanceManager.builder()
      .setApplication(getApplication())
      .setBundleAssetName("index.android.bundle")
      .setJSMainModuleName("index.android")
      .addPackage(new MainReactPackage())
      .addPackage(new RNMail())              // <------ add here
      .setUseDeveloperSupport(BuildConfig.DEBUG)
      .setInitialLifecycleState(LifecycleState.RESUMED)
      .build();

    mReactRootView.startReactApplication(mReactInstanceManager, "ExampleRN", null);

    setContentView(mReactRootView);
  }

  ......

}
```
* register module if MainActivity extends ReactActivity

* In `MainApplication.java`

```java
import com.chirag.RNMail.*; // <--- import

public class MainApplication extends Application implements ReactApplication {
    ....
  
    @Override
    protected List<ReactPackage> getPackages() {
      return Arrays.<ReactPackage>asList(
          new MainReactPackage(),
          new RNMail()      // <------ add here
      );
    }
  };

```



### Add it manually to your iOS project

1. Open your project in XCode, right click on `Libraries` and click `Add
   Files to "Your Project Name"` [(Screenshot)](http://url.brentvatne.ca/jQp8) then [(Screenshot)](https://github.com/pedramsaleh/react-native-mail/blob/master/add-xcodeproj.png?raw=true).
2. Add `libRNMail.a` to `Build Phases -> Link Binary With Libraries`
   [(Screenshot)](http://url.brentvatne.ca/17Xfe).
3. Whenever you want to use it within React code now you can:

```javascript
import { NativeModules } from 'react-native';
const { RNMail } = NativeModules;

// or using old-school require():
var RNMail = require('NativeModules').RNMail;
```


## Example

```javascript
/**
 * Sample React Native App using RNMail
 * @flow
 */

import React, { Component } from 'react';

import {
  AppRegistry,
  StyleSheet,
  Text,
  View,
  TouchableHighlight,
  NativeModules,
  Alert,
  Platform
} from 'react-native';

const { RNMail } = NativeModules;

const _sendMail = () => {
  RNMail.mail({
    recipients: ['support@example.com'],
    ccRecipients: ['supportCC@example.com'],
    bccRecipients: ['supportBCC@example.com'],
    //isHTML: true, // iOS only, exclude if false
    //attachment: {
    //  path: '', // The absolute path of the file from which to read data.
    //  type: '', // Mime Type: jpg, png, doc, ppt, html, pdf
    //  name: '', // Optional: Custom filename for attachment
    //},
    subject: 'need help',
    body: 'Help!'
  }, (error, event) => {
    if (error === 'not_available') {
      const message = Platform.OS === 'ios' ?
        'There is no email account registered with the system.' :
        'There is no email app to handle emails.';
      return Alert.alert('Error', message);
    }

    if (error) {
      return Alert.alert('Error', 'Could not send mail. Please send an email manually to support@example.com');
    }

    // NOTE: Android implementation doesn't send any events!
    if (Platform.OS === 'ios') {
      switch (event) {
        case 'sent': // NOTE: the email was queued for sending
        case 'saved': // NOTE: the email was saved as a draft
        case 'cancelled': // NOTE: the email was discarded
        default:
          Alert.alert(event || 'Unknown event');
      }
    }
  });
}

export default class RNMailExample extends Component {
  render () {
    return (
      <View style={styles.container}>
        <TouchableHighlight onPress={_sendMail} underlayColor="#f7f7f7">
          <Text style={styles.sendMailText}>Send Mail</Text>
        </TouchableHighlight>
      </View>
    );
  }
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    backgroundColor: '#F5FCFF'
  },
  sendMailText: {
    fontSize: 20,
    textAlign: 'center',
    margin: 10
  }
});

AppRegistry.registerComponent('RNMailExample', () => RNMailExample);
```

### Note

On Android, the `callback` will only be called if an `error` occurs. The `event` argument is unused!

## Here is how it looks:

<img src="screenshot.png" alt="iOS 10 Screenshot" width="320" height="568" />
