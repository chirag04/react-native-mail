Pod::Spec.new do |s|
  s.name         = "react-native-mail"
  s.version      = "0.2.4"
  s.summary      = "A wrapper on top of MFMailComposeViewController"
  s.homepage     = "https://github.com/chirag04/react-native-mail"
  s.license      = "MIT"
  s.platform     = :ios, "7.0"
  s.source       = { :git => "https://github.com/chirag04/react-native-mail.git" }
  s.source_files = "RNMail/*.{h,m}"

  s.dependency "React"
end
