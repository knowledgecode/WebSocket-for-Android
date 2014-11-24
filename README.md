# WebSocket for Android
WebSocket for Android is a Cordova/PhoneGap plugin that allows WebSockets (RFC 6455) to be used on Android.  
This is using [Jetty 8](https://github.com/eclipse/jetty.project/tree/jetty-8) under the terms of the Apache License v2.0.  

## Requirements
 - Java 1.6 or higher  
 - Android 2.2 or higher (recommended 4.0 or higher)  
 - Cordova/PhoneGap 3.0.0 or higher  

The version for Cordova/Phonegap 2.x, [see here](https://github.com/knowledgecode/WebSocket-for-Android/tree/2.x).  

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
 - The WSS protocol is now available on 2.3 or higher (SSLv3 is not supported. TLS only.).  
 - 3.x devices are not supported (maybe work, but not tested).  
 - The WebView has officially supported WebSockets since 4.4 (KitKat). Therefore this plugin is not used on those devices by default.  

## Installing
Use the Cordova/PhoneGap Command-Line interface:  
```shell
$ cordova plugin add https://github.com/knowledgecode/WebSocket-for-Android.git
```
or  
```shell
$ phonegap plugin add https://github.com/knowledgecode/WebSocket-for-Android.git
```
This plugin is for Android. However if install this via the CLI, it also affects all other platforms (such as iOS). This is a specification.  
In fact, it writes only an installation history in those platforms. (There are no tangible ill effects.)  
If you mind this thing, recommended to use Cordova Plugman that can specify an installation platform. Execute this on a project root:  
```shell
$ plugman install --platform android --project platforms/android --plugin https://github.com/knowledgecode/WebSocket-for-Android.git --plugins_dir plugins
```
### Upgrading from previous versions
Just remove and reinstall.  
```shell
$ cordova plugin remove com.knowledgecode.cordova.websocket
$ cordova plugin add https://github.com/knowledgecode/WebSocket-for-Android.git
```
or  
```shell
$ phonegap plugin remove com.knowledgecode.cordova.websocket
$ phonegap plugin add https://github.com/knowledgecode/WebSocket-for-Android.git
```
in the case of Plugman:  
```shell
$ plugman uninstall --platform android --project platforms/android --plugin com.knowledgecode.cordova.websocket --plugins_dir plugins
$ plugman install --platform android --project platforms/android --plugin https://github.com/knowledgecode/WebSocket-for-Android.git --plugins_dir plugins
```
#### BUILD FAILED ?
Try to delete all files (cache) in platforms/android/ant-build/.

#### Caution
When install this plugin, it adds `INTERNET` permission to `AndroidManifest.xml`. If remove this plugin, the permission is also removed at the same time even if it is required for other plugins.  

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

| key                  | default value | remarks      |
|:---------------------|:-------------:|:-------------|
| origin               | (empty)       |              |
| maxConnectTime       | 75000         |              |
| override             | false         | since v0.8.0 |

The `origin` is a value to set the request header field.  
The `maxConnectTime` is time to wait for connection. The default value will be 75,000 milliseconds if omit it.  
The `override` is a flag to override the native WebSocket on 4.4 or higher devices. The default value will be false if omit it. Set to true if you want to enable the plugin WebSocket even though those support WebSockets.  
If you want to change these parameters, need to do before creating a instance:  
```JavaScript
WebSocket.pluginOptions = {
    origin: 'http://example.com',
    maxConnectTime: 5000,
    override: true
};

var ws = new WebSocket('ws://echo.websocket.org');
```
### *send(data)*
Transmits data to the server over the WebSocket connection. The data takes a string, a blob, or an arraybuffer.  

#### Notes
The size of messages that can transmit and receive at a time depends on the heap memory on devices. You would be better to consider a way to split messages if those are large (hundreds of kilobytes).  

### *close([code[, reason]])*
Closes the WebSocket connection or connection attempt, if any.  

## Change Log
#### 0.8.0
* performance tuning (about 5% to 15% faster than previous versions)  
* deployed the sources of Jetty directly (instead the jar file)  
* abolished the maxTextMessageSize/maxBinaryMessageSize options  
* added the "override" option  
* refactoring  

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
