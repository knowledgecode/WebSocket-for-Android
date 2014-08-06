# WebSocket for Android
WebSocket for Android is a Cordova/PhoneGap plugin that makes it possible to use WebSockets (RFC 6455) on Android.  
This is using [Jetty](https://github.com/eclipse/jetty.project) under the terms of the Apache License v2.0.  

## Requirements
 - Java 1.6 or later  
 - Android 2.2 or later (recommend 4.0 or later)  
 - Cordova/PhoneGap 3.0.0 or later  

The version for Cordova/Phonegap 2.x is [here](https://github.com/knowledgecode/WebSocket-for-Android/tree/2.x).  

## Supported Versions
| version | ws protocol        | wss protocol       | text message       | binary message      |
|:-------:|:------------------:|:------------------:|:------------------:|:-------------------:|
| 2.2     | support            | not support `*1`   | support            | limited support `*2`|
| 2.3     | support            | not support `*1`   | support            | limited support `*2`|
| 3.x     | -- `*3`            | -- `*3`            | -- `*3`            | -- `*3`             |
| 4.0     | support            | support            | support            | support             |
| 4.1     | support            | support            | support            | support             |
| 4.2     | support            | support            | support            | support             |
| 4.3     | support            | support            | support            | support             |
| 4.4     | native support `*4`| native support `*4`| native support `*4`| native support `*4` |

`*1` Due to Android SSL issues.  
`*2` Supports Base64-encoded data only.  
`*3` May work. But not tested.  
`*4` In KitKat, WebSocket API has been officially supported. The native API of these devices is used in preference to this plugin.  

## Installing
Use the Cordova/PhoneGap Command-Line interface:  
```shell
$ cordova plugin add https://github.com/knowledgecode/WebSocket-for-Android.git
```
or  
```shell
$ phonegap plugin add https://github.com/knowledgecode/WebSocket-for-Android.git
```
This plugin is for Android only. However, when you install this via the CLI, this is also installed all other platforms. It's a feature.  
In fact, the CLI writes only an installation history in those platforms. (There are no tangible ill effects.)  
If you mind that thing, recommended to use Cordova Plugman that can specify an installation platform. Execute this on a project root:  
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
#### Note
When you install this plugin, it adds `INTERNET` permission to `AndroidManifest.xml`. If you remove this plugin, the permission is also removed at the same time.  

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
And then, this plugin has options. Details are as follows:  
```JavaScript
WebSocket.pluginOptions = {
    origin: 'http://websocket-is-fun.com',
    maxConnectTime: 20000,                // 20sec
    maxTextMessageSize: 32768,            // 32kb
    maxBinaryMessageSize: 32768           // 32kb
};
```
All these parameters are omissible. The origin will be set empty if omit it. The maxConnectTime is a wait time for connection. The default value will be 20,000 milliseconds if omit it. The maxTextMessageSize and the maxBinaryMessageSize are receivable maximum size from server. The default values will be 32,768 bytes if omit them.  
To work with common source code for devices supporting the native API (such as Android 4.4 and iOS 6 or later), it is recommended to write as the following:  
```JavaScript
if (WebSocket.pluginOptions) {
    WebSocket.pluginoptions = {
        origin: 'http://chatterchatter.org'
    };
}
var ws = new WebSocket('ws://chatterchatter.org');
```
### *send(data)*
Transmits data to the server over the WebSocket connection. The data takes a string, a blob, or an arraybuffer.  
In devices that are not supported both a blob and an arraybuffer, cannot transmit binary messages. (ex. Android 2.2 and 2.3)  

#### Note
If receives binary messages in unsupported devices, automatically encodes them to Base64.  
```JavaScript
ws.onmessage = function (event) {
    // For example, the receiving data can be used as data URI scheme.
    img.src = 'data:image/jpeg;base64,' + event.data;
};
```
### *close([code, reason])*
Closes the WebSocket connection or connection attempt, if any.  

## Change Log
#### 0.6.3
* fix a bug of a receiving binary size  

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
* forcing the WebSocket of plugin in Android 4.3 or lower (thanks to @rpastorvargas and @punj)  
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
