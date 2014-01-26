/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.knowledgecode.cordova.websocket;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.TimeUnit;

import org.apache.cordova.CordovaWebView;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.apache.cordova.PluginResult.Status;
import org.eclipse.jetty.websocket.WebSocket.Connection;
import org.eclipse.jetty.websocket.WebSocketClient;
import org.eclipse.jetty.websocket.WebSocketClientFactory;
import org.json.JSONArray;
import org.json.JSONException;

import android.util.Base64;
import android.util.SparseArray;

/**
 * Cordova WebSocket Plugin for Android
 * This plugin is using Jetty under the terms of the Apache License v2.0.
 * @author KNOWLEDGECODE
 * @version 0.4.0
 */
public class WebSocket extends CordovaPlugin {
    // TODO option
    private static final int CONNECTION_TIMEOUT = 20000;

    private WebSocketClientFactory _factory;
    private SparseArray<Connection> _conn;

    private abstract class JettyWebSocket implements
        org.eclipse.jetty.websocket.WebSocket.OnTextMessage,
        org.eclipse.jetty.websocket.WebSocket.OnBinaryMessage,
        org.eclipse.jetty.websocket.WebSocket.OnFrame {
        private static final int BUFFER_SIZE = 8192;

        private FrameConnection _frame;
        private boolean _binary;
        private ByteArrayOutputStream _stream;

        @Override
        public void onOpen(Connection arg0) {
            _stream = new ByteArrayOutputStream(BUFFER_SIZE);
        }

        @Override
        public void onClose(int code, String reason) {
            try {
                if (_stream != null) {
                    _stream.close();
                    _stream = null;
                }
            } catch (IOException e) {
            }
            _frame = null;
        }

        @Override
        public boolean onFrame(byte flags, byte opcode, byte[] data, int offset, int length) {
            if (_frame.isBinary(opcode) || (_frame.isContinuation(opcode) && _binary)) {
                _binary = true;
                _stream.write(data, offset, length);
                if (_frame.isMessageComplete(flags)) {
                    _binary = false;
                    this.onMessage(_stream.toByteArray(), 0, _stream.size());
                    _stream.reset();
                }
                return true;
            }
            return false;
        }

        @Override
        public void onHandshake(FrameConnection connection) {
            _frame = connection;
        }
    }

    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
        _factory = new WebSocketClientFactory();
        // not change
//      _factory.setBufferSize(8192);
        _conn = new SparseArray<Connection>();
        try {
            start();
        } catch (Exception e) {
        }
    }

    @Override
    public boolean execute(final String action, final JSONArray args, final CallbackContext callbackContext) {
        cordova.getThreadPool().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    if ("create".equals(action)) {
                        create(callbackContext, args.getInt(0), args.getString(1), args.getString(2), args.getString(3));
                    } else if ("send".equals(action)) {
                        send(args.getInt(0), args.getString(1));
                        callbackContext.success();
                    } else if ("close".equals(action)) {
                        close(args.getInt(0), args.getInt(1), args.getString(2));
                        callbackContext.success();
                    } else {
                        callbackContext.sendPluginResult(new PluginResult(Status.INVALID_ACTION, action));
                    }
                } catch (JSONException e) {
                    callbackContext.error(action);
                } catch (IOException e) {
                    callbackContext.error(action);
                }
            }
        });
        return true;
    }

    @Override
    public void onReset() {
        super.onReset();

        try {
            stop().start();
        } catch (Exception e) {
        }
    }

    @Override
    public void onDestroy() {
        try {
            stop();
        } catch (Exception e) {
        }
        _conn = null;
        _factory.destroy();
        _factory = null;

        super.onDestroy();
    }

    /**
     * Connect to server.
     * @param callbackContext
     * @param callbackId
     * @param url
     * @param protocol
     * @param origin
     */
    private void create(final CallbackContext callbackContext, final int callbackId, String url, String protocol, String origin) {
        WebSocketClient client = _factory.newWebSocketClient();

        if (protocol.length() > 0) {
            client.setProtocol(protocol);
        }
        if (origin.length() > 0) {
            client.setOrigin(origin);
        }

        try {
            client.open(complementPort(url), new JettyWebSocket() {
                @Override
                public void onOpen(Connection conn) {
                    super.onOpen(conn);

                    _conn.put(callbackId, conn);
                    String callbackString = createCallback("onopen");
                    sendCallback(callbackString, true);
                }

                @Override
                public void onMessage(String data) {
                    String callbackString = createCallback("onmessage", data);
                    sendCallback(callbackString, true);
                }

                @Override
                public void onMessage(byte[] data, int offset, int length) {
                    String callbackString = createCallback("onmessage", data);
                    sendCallback(callbackString, true);
                }

                @Override
                public void onClose(int code, String reason) {
                    super.onClose(code, reason);

                    if (_conn.indexOfKey(callbackId) >= 0) {
                        _conn.remove(callbackId);
                    }
                    String callbackString = createCallback("onclose", code, reason);
                    sendCallback(callbackString, false);
                }

                /**
                 * Send plugin result.
                 * @param callbackString
                 * @param keepCallback
                 */
                private void sendCallback(String callbackString, boolean keepCallback) {
                    if (!callbackContext.isFinished()) {
                        PluginResult result = new PluginResult(Status.OK, callbackString);
                        result.setKeepCallback(keepCallback);
                        callbackContext.sendPluginResult(result);
                    }
                }

                /**
                 * Create Callback JSON String.
                 * @param event
                 * @return JSON String
                 */
                private String createCallback(String event) {
                    String json = "{\"event\":\"%s\"}";
                    return String.format(json, event);
                }

                /**
                 * Create Callback JSON String.
                 * @param event
                 * @param data
                 * @return JSON String
                 */
                private String createCallback(String event, String data) {
                    String json = "{\"event\":\"%s\",\"data\":\"%s\"}";
                    return String.format(json, event, data.replaceAll("\"", "\\\\\""));
                }

                /**
                 * Create Callback JSON String.
                 * @param event
                 * @param input
                 * @return JSON String
                 */
                private String createCallback(String event, byte[] input) {
                    String json = "{\"event\":\"%s\",\"data\":\"%s\",\"binary\":true}";
                    String data = Base64.encodeToString(input, Base64.NO_WRAP);
                    return String.format(json, event, data);
                }

                /**
                 * Create Callback JSON String.
                 * @param event
                 * @param code
                 * @param reason
                 * @return JSON String
                 */
                private String createCallback(String event, int code, String reason) {
                    String json = "{\"event\":\"%s\",\"wasClean\":%b,\"code\":%d,\"reason\":\"%s\"}";
                    boolean wasClean = code == 1000;
                    reason = reason == null ? "" : reason;
                    return String.format(json, event, wasClean, code, reason.replaceAll("\"", "\\\\\""));
                }
            }, CONNECTION_TIMEOUT, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            if (!callbackContext.isFinished()) {
                PluginResult result = new PluginResult(Status.ERROR);
                result.setKeepCallback(true);
                callbackContext.sendPluginResult(result);
            }
        }
    }

    /**
     * Complement default port number.
     * @param url
     * @return URI
     * @throws URISyntaxException
     */
    private URI complementPort(String url) throws URISyntaxException {
        URI uri = new URI(url);
        int port = uri.getPort();

        if (port < 0) {
            if ("ws".equals(uri.getScheme())) {
                port = 80;
            } else if ("wss".equals(uri.getScheme())) {
                port = 443;
            }
            uri = new URI(uri.getScheme(), "", uri.getHost(), port, uri.getPath(), uri.getQuery(), "");
        }
        return uri;
    }

    /**
     * Send text message.
     * @param callbackId
     * @param data
     * @throws IOException 
     */
    private void send(int callbackId, String data) throws IOException {
        send(callbackId, data, false);
    }

    /**
     * Send text/binary data.
     * @param callbackId
     * @param data
     * @param binaryString
     * @throws IOException
     */
    private void send(int callbackId, String data, boolean binaryString) throws IOException {
        Connection conn = _conn.get(callbackId);
        if (conn != null) {
            if (binaryString) {
                byte[] binary = Base64.decode(data, Base64.NO_WRAP);
                conn.sendMessage(binary, 0, binary.length);
            } else {
                conn.sendMessage(data);
            }
        }
    }

    /**
     * Close a connection.
     * @param callbackId
     * @param code
     * @param reason
     */
    private void close(int callbackId, int code, String reason) {
        Connection conn = _conn.get(callbackId);
        if (conn != null) {
            if (code > 0) {
                conn.close(code, reason);
            } else {
                conn.close();
            }
        }
    }

    /**
     * Start WebSocketClientFactory.
     * @return WebSocket
     * @throws Exception
     */
    private WebSocket start() throws Exception {
        _factory.start();
        return this;
    }

    /**
     * Stop WebSocketClientFactory.
     * @return WebSocket
     * @throws Exception
     */
    private WebSocket stop() throws Exception {
        if (_conn != null) {
            for (int i = 0; i < _conn.size(); i++) {
                if (_conn.get(i).isOpen()) {
                    _conn.get(i).close();
                }
            }
            _conn.clear();
        }
        _factory.stop();
        return this;
    }
}
