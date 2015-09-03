# react-native-mail

A React Native wrapper for Apple's ``MFMailComposeViewController``.
Supports emails with attachments.

### Add it to your project

1. Run `npm install react-native-mail --save`
2. Open your project in XCode, right click on `Libraries` and click `Add
   Files to "Your Project Name"` [(Screenshot)](http://url.brentvatne.ca/jQp8) then [(Screenshot)](http://url.brentvatne.ca/1gqUD).
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
      body: '',
      attachmentPath: '',  // The absolute path of the file from which to read data.
      attachmentType: '',   // Mime Type: jpg, png, doc, ppt, html, pdf
      attachmentName: '',   // Optional: Custom filename for attachment
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

## Here is how it looks:
![Demo gif](https://github.com/chirag04/react-native-mail/blob/master/screenshot.jpg)
