# WebSocket for Android
WebSocket for Android is a Cordova/PhoneGap plugin that makes possible to use WebSocket (RFC 6455) on Android.  
This is using Jetty under the terms of the Apache License v2.0.  

## Requirements
 - Java 1.6 or greater  
 - Android 2.2 or greater (recommend 4.0 or greater)  
 - Cordova/PhoneGap 3.0.0 or greater  

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

`*1` Due to SSL issue with Android.  
`*2` Supports Base64-encoded data only.  
`*3` May work. But not tested.  
`*4` WebSocket is supported by WebView in KitKat. The native API of these devices is used in preference to this plugin.  

## Installing
Use the Cordova/PhoneGap Command-Line interface:  

    $ cordova plugin add https://github.com/knowledgecode/WebSocket-for-Android.git

or  

    $ phonegap plugin add https://github.com/knowledgecode/WebSocket-for-Android.git

### Upgrading from previous versions

    $ cordova plugin remove com.knowledgecode.cordova.websocket
    $ cordova plugin add https://github.com/knowledgecode/WebSocket-for-Android.git

or  

    $ phonegap plugin remove com.knowledgecode.cordova.websocket
    $ phonegap plugin add https://github.com/knowledgecode/WebSocket-for-Android.git

## Usage
### WebSocket(url[, protocols])
The WebSocket(url, protocols) constructor takes one or two arguments. The first argument, url, specifies the URL to which to connect. The second, protocols, is either a string or an array of strings.  
A simple code is as follows:  

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

And then, this plugin has options. Details are as follows:  

    WebSocket.pluginOptions = {
        origin: 'http://websocket-is-fun.com',
        maxConnectTime: 20000,                // 20sec
        maxTextMessageSize: 32768,            // 32kb
        maxBinaryMessageSize: 32768           // 32kb
    };

All these parameters are omissible. The origin will be set empty if omit it. The maxConnectTime is a wait time for connection. The default value will be 20,000 milliseconds if omit it. The maxTextMessageSize and the maxBinaryMessageSize are receivable maximum size from server. The default values will be 32,768 bytes if omit them.  
Recommend to do as the following to make common source code available for devices such as Android 4.4 and iOS 6 or greater which support the native API:  

    if (WebSocket.pluginOptions) {
        WebSocket.pluginoptions = {
            origin: 'http://chatterchatter.org'
        };
    }
    var ws = new WebSocket('ws://chatterchatter.org');

### send(data[, asBinary])
`deprecated` *the second argument will be removed soon!*  

Transmits data to the server over the WebSocket connection. The data takes a string, a blob, or an arraybuffer. The second argument, asBinary, is the unique argument of this plugin. Usually is not used.  
In Android 2.2 and 2.3, cannot transmit binary messages because both blob and arraybuffer are not supported. Therefore, needs to transmit them by Base64 encoding.  

    var data = btoa(binaryString);  // Base64 encoding
    ws.send(data, true);            // If omit the second argument, this is transmitted as text message.

#### Note
If receives binary messages in Android 2.2 and 2.3, automatically encodes them to Base64.  

    ws.onmessage = function (event) {
        // For example, the receiving data can be used as data URI scheme.
        img.src = 'data:image/jpeg;base64,' + event.data;
    };

### close([code, reason])
Closes the WebSocket connection or connection attempt, if any.  

## Change Log
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
* origin support

#### 0.2.0
* comply with the WebSocket API requirements  

#### 0.1.0
* first release

## License
This plugin is available under the terms of the Apache License Version 2.0.
