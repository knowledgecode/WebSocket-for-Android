# WebSocket for Android
WebSocket for Android is a PhoneGap / Cordova plugin.  
This plugin is using Jetty-8 in terms of Apache License 2.0.  

## Preparation for use in Android
### src/org/apache/cordova/plugin/
Add `WebSocket.java` to your PhoneGap project.  

### assets/www/
Add `webSocket.min.js` to your PhoneGap project.  
And append it to html files as follows.  

    <script src="cordova-2.x.x.js"></script>
    <script src="webSocket.min.js"></script>

### libs/
Add jetty-websocket-8.x.jar and slf4j-android-x.jar to your PhoneGap project.  

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
### plugins.WebSocket(uri[, protocol])
Create a new socket.  
The "uri" is a URL which to connect.  
The "protocol" is a sub protocol. If don't need this parameter, omit it.  
For example,  

    var ws = new plugins.WebSocket('ws://echo.websocket.org:80');

    // onopen callback
    ws.onopen = function () {
        console.log('onopen');
        this.send('hello');
    };

    // onmessage callback
    ws.onmessage = function (data) {
        // "data" is a received string
        console.log(data);  // hello
        this.close();
    };

    // onerror callback
    ws.onerror = function (message) {
        // "message" is a reason of error
        console.log(message);
    };

    // onclose callback
    ws.onclose = function (code) {
        // "code" is a reason code of disconnection
        console.log(code);  // 1000
    };

The "onopen" is a function which is called when it has been connected a server.  
The "onmessage" is a function which is called when it has been received data.  
The "onerror" is a function which is called when it has been failed.  
The "onclose" is a function which is called when it has been closed.  

### ws.send(message)
Send a message.  
The "message" is a string.  
If you want to send a JSON, need to serialize it by use of JSON.stringify().  

### ws.close()
Close the socket.  

## Notes
At the moment, WebSocket for Android is not supported wss uri scheme and binary data.  

## License
WebSocket for Android is available under the terms of the MIT license.  
