# WebSocket for Android
WebSocket for Android is a Cordova/PhoneGap plugin that makes it possible to use WebSockets (RFC 6455) on Android.  
This is using [Jetty 8](https://github.com/eclipse/jetty.project/tree/jetty-8) under the terms of the Apache License v2.0.  

## Requirements
 - Java 1.6 or higher  
 - Android 2.2 or higher (recommended 4.0 or higher)  
 - Cordova/PhoneGap 2.3.0 - 2.9.x  

The version for Cordova/Phonegap 3 is [see here](https://github.com/knowledgecode/WebSocket-for-Android/tree/master).  

## Supported Features
| version        | WS protocol | WSS protocol | text message | binary message |
|:--------------:|:-----------:|:------------:|:------------:|:--------------:|
| 2.2 (API 8)    | ✓           |              | ✓            |                |
| 2.3.3 (API 10) | ✓           | ✓            | ✓            |                |
| 4.0 (API 14)   | ✓           | ✓            | ✓            | ✓              |
| 4.0.3 (API 15) | ✓           | ✓            | ✓            | ✓              |
| 4.1.2 (API 16) | ✓           | ✓            | ✓            | ✓              |
| 4.2.2 (API 17) | ✓           | ✓            | ✓            | ✓              |
| 4.3.1 (API 18) | ✓           | ✓            | ✓            | ✓              |
| 4.4.2 (API 19) | ✓           | ✓            | ✓            | ✓              |

#### Notes
 - The WSS protocol is now available on 2.3 or higher  
 - 3.x devices are not supported (maybe work, but not tested).  
 - The WebView has officially supported WebSockets since 4.4 (KitKat). Therefore this plugin is not used on those devices by default.  

## Preparation
### src/com/knowledgecode/cordova/WebSocket.java
Copy `src/com/knowledgecode/cordova/WebSocket.java` to your project.  

### assets/www/websocket.js
Copy `assets/www/websocket.js` to your project. And append it to html files as follows:  
```HTML
<script src="cordova.js"></script>
<script src="websocket.js"></script>
```
### libs/jetty-websocket-8.x.jar
Copy `libs/jetty-websocket-8.x.jar` to your project.  

### res/xml/config.xml
Append the following to the config.xml:  
```XML
<plugins>
  // ...
  // some other plugins
  // ...
  <plugin name="WebSocket" value="com.knowledgecode.cordova.WebSocket" />
</plugins>
```
In the case of Cordova/Phonegap 2.8.0 or later:  
```XML
<feature name="WebSocket">
  <param name="android-package" value="com.knowledgecode.cordova.WebSocket" />
</feature>
```
### AndroidManifest.xml
Append the following to the AndroidManifest.xml:  
```XML
<uses-permission android:name="android.permission.INTERNET" />
```
## Usage
### *WebSocket(url[, protocols])*
The WebSocket(url, protocols) constructor takes one or two arguments. The first argument, url, specifies the URL to which to connect. The second, protocols, is either a string or an array of strings.  
A simple code is as follows:  
```JavaScript
document.addEventListener('deviceready', function () {
    var ws = new WebSocket('ws://echo.websocket.org');

    ws.onopen = function () {
        console.log('open');
        this.send('hello');         // transmit "hello" after connecting
    };

    ws.onmessage = function (event) {
        console.log(event.data);    // will be "hello"
        this.close();
    };

    ws.onerror = function () {
        console.log('error occurred!');
    };

    ws.onclose = function (event) {
        console.log('close code=' + event.code);
    };
}, false);
```
#### Options
This plugin has the following options. All these parameters are omissible. Of course these don't affect the native WebSocket.  

| key                  | default value | remarks                   |
|:---------------------|:-------------:|:--------------------------|
| origin               | (empty)       |                           |
| maxConnectTime       | 20000         |                           |
| maxTextMessageSize   | 32768         | scheduled to be abolished |
| maxBinaryMessageSize | 32768         | scheduled to be abolished |

The `origin` is a value to set the request header field.  
The `maxConnectTime` is time to wait for connection. The default value will be 20,000 milliseconds if omit it.  
The maxTextMessageSize and the maxBinaryMessageSize are receivable maximum size from server. The default values will be 32,768 bytes if omit them.  
If you want to change these parameters, need to do before creating a instance:  
```JavaScript
WebSocket.pluginOptions = {
    origin: 'http://example.com',
    maxConnectTime: 5000,
    maxTextMessageSize: 65536,
    maxBinaryMessageSize: 65536
};

var ws = new WebSocket('ws://echo.websocket.org');
```
### *send(data)*
Transmits data to the server over the WebSocket connection. The data takes a string, a blob, or an arraybuffer.  

### *close([code[, reason]])*
Closes the WebSocket connection or connection attempt, if any.  

## Change Log
#### 0.7.0
* resolved the issue of SSL on 4.0 and 2.3 (thanks to @agalazis and koush/AndroidAsync)  

#### 0.6.3
* fixed a bug of a receiving binary size  

#### 0.6.2a
* limit installation target to Android (thanks to @peli44)  

#### 0.6.2
* updated Jetty WebSocket library  

#### 0.6.1
* added escaping of special characters (thanks to @odbol)  

#### 0.6.0
* cookie support (thanks to @ericfong)  
* removed a second argument from the send() method  

#### 0.5.2
* clobbered buggy websockets on 4.3 or lower (thanks to @rpastorvargas and @punj)  
* bug fix  

#### 0.5.1
* bug fix  

#### 0.5.0
* change the way to set plugin options  
* multiple subprotocol support  
* readyState property support (thanks to @jrpereirajr)  

#### 0.4.0
* Cordova/Phonegap 3 support  
* binary support  
* event listener support  
* more compliant with the WebSocket API requirements  
* license change from MIT to Apache v2.0  

#### 0.3.2
* bug fix  

#### 0.3.1
* bug fix  

#### 0.3.0
* origin support (thanks to @rgillan)  

#### 0.2.0
* comply with the WebSocket API requirements  

#### 0.1.0
* first release  

## License
This plugin is available under the terms of the Apache License Version 2.0.
