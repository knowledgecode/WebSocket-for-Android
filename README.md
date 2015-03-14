# WebSocket for Android
WebSocket for Android is a Cordova plugin that allows WebSockets (RFC 6455) to be used on Android.  
This is using [Jetty 8](https://github.com/eclipse/jetty.project/tree/jetty-8) under the terms of the Apache License v2.0.  

## Requirements
 - Android 2.2 or later (recommended 4.1 or later)  
 - Apache Cordova Android 3.0.0 or later or compatible framework  

The plugin for Cordova 2.x, [see here](https://github.com/knowledgecode/WebSocket-for-Android/tree/2.x).  

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
| 4.4.2 (API 19) | -           | -            | -            | -              |
| 5.0.1 (API 21) | -           | -            | -            | -              |

#### Notes
 - The WSS protocol is now available on 2.3 or later (SSLv3 is not supported. TLS only.).  
 - 3.x devices are not supported (maybe work, but not tested).  
 - The WebView has officially supported WebSockets since Android 4.4. Therefore this plugin is **NOT** used on them by default.  
 - If target Android 5.0, would be better to build with Cordova Android 3.7.1 or later.  

## Installing
Use Cordova Command-Line Interface (CLI) :  
```sh
$ cordova plugin add com.knowledgecode.cordova.websocket
```
or  
```sh
$ cordova plugin add https://github.com/knowledgecode/WebSocket-for-Android.git
```
If you are developing the iOS version in parallel, this plugin will be also installed there:  
```sh
$ cordova plugin add com.knowledgecode.cordova.websocket
Fetching plugin "com.knowledgecode.cordova.websocket" via plugin registry
Installing "com.knowledgecode.cordova.websocket" for android
Installing "com.knowledgecode.cordova.websocket" for ios
```
This is a feature of CLI. There are no tangible ill effects. If you want to avoid this thing, use Cordova Plugman:

```sh
$ plugman install --platform android --project ./platforms/android --plugins_dir ./plugins --plugin com.knowledgecode.cordova.websocket
Fetching plugin "com.knowledgecode.cordova.websocket" via plugin registry
npm http GET http://registry.cordova.io/com.knowledgecode.cordova.websocket
npm http 200 http://registry.cordova.io/com.knowledgecode.cordova.websocket
Installing "com.knowledgecode.cordova.websocket" for android
```
### Upgrading from previous versions
Just remove and reinstall:  
```sh
$ cordova plugin remove com.knowledgecode.cordova.websocket
$ cordova plugin add com.knowledgecode.cordova.websocket
```
or  
```sh
$ cordova plugin remove com.knowledgecode.cordova.websocket
$ cordova plugin add https://github.com/knowledgecode/WebSocket-for-Android.git
```
or
```sh
$ plugman uninstall --platform android --project ./platforms/android --plugins_dir ./plugins --plugin com.knowledgecode.cordova.websocket
$ plugman install --platform android --project ./platforms/android --plugins_dir ./plugins --plugin com.knowledgecode.cordova.websocket
```
#### BUILD FAILED ?
Delete all files in ./platforms/android/ant-build/. These are cache.

#### Caution
When install this plugin, it adds `INTERNET` permission to `AndroidManifest.xml`. If remove this plugin, the permission is also removed at the same time even if it is required for other plugins.  

## Usage
### *WebSocket(url[, protocols])*
The WebSocket(url, protocols) constructor takes one or two arguments. The first argument, url, specifies the URL to which to connect. The second, protocols, is either a string or an array of strings.  
A simple code is as follows:  
```javascript
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
This plugin has the following options. All these parameters are optional. Of course these don't affect the native WebSocket.  

| key                  | default value |
|:---------------------|:-------------:|
| origin               | (empty)       |
| maxConnectTime       | 75000         |
| override             | false         |

`origin` is a value to set the request header field. `maxConnectTime` is time to wait for connection. `override` is a flag to override the native WebSocket on Android 4.4 or later devices. Set to true if want to force them to use the plugin. In most cases, it is slower than the native WebSocket.  

If want to change these parameters, need to do before creating a instance:  
```javascript
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
The size of message that can transmit and receive at a time depends on heap size. Would be better to consider a way to split a message if it is quite large.  

### *close([code[, reason]])*
Closes the WebSocket connection or connection attempt, if any.  

## Change Log
#### 0.8.3
* fixed that difference between packages and directories structure (thanks to @digigm)  

#### 0.8.2
* fixed the constructor error on 4.4 or later (thanks to @digigm)  

#### 0.8.1
* fixed the frame aggregation error (thanks to @Atsyn)  
* fixed the binary transmission for the case of using the plugin on 4.4 or later  

#### 0.8.0
* performance tuning (about 5% to 15% faster than previous versions)  
* deployed the sources of Jetty directly (instead the jar file)  
* abolished the maxTextMessageSize/maxBinaryMessageSize options  
* added the "override" option  
* refactor  

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
