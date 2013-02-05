# WebSocket for Android
WebSocket for Android is a PhoneGap / Cordova plugin.  
This plugin is using Jetty-8 in terms of Apache License 2.0.  

## Preparation for use in Android
### src/org/apache/cordova/plugin/
Add `WebSocket.java` to your PhoneGap project.  

### assets/www/
Add `webSocket.min.js` to your PhoneGap project.  
And append it to html files as follows.  

    <script src="cordova-2.3.0.js"></script>
    <script src="webSocket.min.js"></script>

### libs/
Add four *.jar files to your PhoneGap project.  

### res/xml/config.xml
Append the following to the config.xml.  

    <plugins>
        // ...
        // some other plugins
        // ...

        <plugin name="WebSocket" value="org.apache.cordova.plugin.WebSocket" />
    </plugins>

## Usage
### plugins.WebSocket.create(uri, protocol, onopen, onmessage, onerror, onclose)
Create a new WebSocket.  
The "uri" is the URL to which to connect.  
The "protocol" is a sub protocol. If need not this parameter, set an empty.  
The "onopen" is a function which is called when it has been connected a server.  
The "onmessage" is a function which is called when it has been received data.  
The "onerror" is a function which is called when it has been failed.  
The "onclose" is a function which is called when it has been closed.  

    var ws = plugins.WebSocket.create(
        'ws://echo.websocket.org:80',
        '',
        function () {
            // onopen callback
            console.log('onopen');
            this.send('hello');
        },
        function (data) {
            // onmessage callback
            // "data" is a received string
            console.log(data);  // hello
            this.close();
        },
        function (message) {
            // onerror callback
            // "message" is a reason of error
            console.log(message);
        },
        function (code) {
            // onclose callback
            // "code" is a reason code of disconnection
            console.log(code);  // 1000
        }
    );

## Notes
Sorry, WebSocket for Android is not supported wss uri scheme and binary data.  

## License
WebSocket for Android is available under the terms of the MIT license.  
