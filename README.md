# react-native-mail

A React Native wrapper for Apple's ``MFMailComposeViewController`` from iOS and Mail Intent on android
Supports emails with attachments.

### Installation

There was a breaking change in RN >=40. So for React Native >= 0.40: use v3.x and higher of this lib. otherwise use v2.x

```bash
npm i --save react-native-mail
```

### Add it to your android project

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

* if MainActivity extends Activity: register module in MainActivity.java


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
* else if MainActivity extends ReactActivity: register module in `MainApplication.java`

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



### Add it to your iOS project

1. Run `npm install react-native-mail --save`
2. Open your project in XCode, right click on `Libraries` and click `Add
   Files to "Your Project Name"` [(Screenshot)](http://url.brentvatne.ca/jQp8) then navigate to node_modules/react-native-mail and select RNMail.xcodeproj [(Screenshot)](https://github.com/pedramsaleh/react-native-mail/blob/master/add-xcodeproj.png?raw=true).
3. Add `libRNMail.a` to `Build Phases -> Link Binary With Libraries`
   [(Screenshot)](http://url.brentvatne.ca/17Xfe).
4. Whenever you want to use it within React code now you can: `var Mailer = require('NativeModules').RNMail;`


## Example
```javascript
var Mailer = require('NativeModules').RNMail;

var MailExampleApp = React.createClass({
  handleHelp: function() {
    Mailer.mail({
      subject: 'need help',
      recipients: ['support@example.com'],
      ccRecipients: ['supportCC@example.com'],
      bccRecipients: ['supportBCC@example.com'],
      body: '',
      isHTML: true, // iOS only, exclude if false
      attachment: {
        path: '',  // The absolute path of the file from which to read data.
        type: '',   // Mime Type: jpg, png, doc, ppt, html, pdf
        name: '',   // Optional: Custom filename for attachment
      }
    }, (error, event) => {
        if(error) {
          AlertIOS.alert('Error', 'Could not send mail. Please send a mail to support@example.com');
        }
    });
  },  
  render: function() {
    return (
      <TouchableHighlight
            onPress={row.handleHelp}
            underlayColor="#f7f7f7">
	      <View style={styles.container}>
	        <Image source={require('image!announcement')} style={styles.image} />
	      </View>
	   </TouchableHighlight>
    );
  }
});
```

### Note

On Android, the `callback` will only be called if an `error` occurs. The `event` argument is unused!

## Here is how it looks:
![Demo gif](https://github.com/chirag04/react-native-mail/blob/master/screenshot.png)
