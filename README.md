# WebSocket for Android
WebSocket for Android is a PhoneGap plugin that makes possible to use the WebSocket (RFC 6455) on an Android WebView.  
It is using Jetty 8 under the terms of the Apache License v2.0.  

## Product requirements
 - Java 1.6  
 - Android 2.1 (API 7) or higher (recommend 4.0 (API 14) or higher)  
 - PhoneGap 2.2.0 or higher (not available on 3.0+)  

## Preparation for use in Android
### src/org/apache/cordova/plugin/
Add `WebSocket.java` to your Android PhoneGap project.  

### assets/www/
Add `webSocket.min.js` to your Android PhoneGap project.  
And append it to html files as follows.  

    <script src="cordova-2.x.x.js"></script>
    <script src="webSocket.min.js"></script>

### libs/
Add jetty-websocket-8.x.jar and slf4j-android-x.jar to your Android PhoneGap project.  

### res/xml/config.xml
Append the following to the config.xml.  

    <plugins>
        // ...
        // some other plugins
        // ...

        <plugin name="WebSocket" value="org.apache.cordova.plugin.WebSocket" />
    </plugins>

### AndroidManifest.xml
Append the following to the AndroidManifest.xml.  

    <uses-permission android:name="android.permission.INTERNET" />

## Usage
### plugins.WebSocket(uri[, protocol, origin])
Create a new socket.  
The uri is a URI which to connect.  
The protocol is a sub protocol. If don't need this parameter, can omit it.  
The origin is an Origin header field. If don't need this parameter, can omit it.  
For example,  

    var ws = new plugins.WebSocket('ws://echo.websocket.org');

    // onopen callback
    ws.onopen = function () {
        console.log('onopen');
        this.send('hello');
    };

    // onmessage callback
    ws.onmessage = function (data) {
        // The data is received text
        console.log(data);  // hello
        this.close();
    };

    // onerror callback
    ws.onerror = function (message) {
        // The message is the reason of error
        console.log(message);
    };

    // onclose callback
    ws.onclose = function (code) {
        // The code is the reason code of disconnection
        console.log(code);  // 1000
    };

The onopen is called when it has been connected a server.  
The onmessage is called when it has been received any data.  
The onerror is called when the connection has failed. Need not implement if don't handle errors.  
The onclose is called when it has been closed.  

### ws.send(message)
Send a message.  
The message is UTF-8 text.  
If you want to send JSON object, need to serialize it by use of JSON.stringify().  

### ws.close()
Close the socket.  

## Notes
At the moment, this plugin is not supported binary data.  
Also maybe the wss protocol will not work on Android 2.x devices.  

## License
WebSocket for Android is available under the terms of the MIT license.  
