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
import org.apache.http.client.utils.URIUtils;
import org.eclipse.jetty.websocket.WebSocket.Connection;
import org.eclipse.jetty.websocket.WebSocketClient;
import org.eclipse.jetty.websocket.WebSocketClientFactory;
import org.json.JSONArray;
import org.json.JSONException;

import android.util.SparseArray;

/**
 * Cordova WebSocket Plugin for Android
 * This plugin is using Jetty under the terms of the Apache License v2.0.
 * @author KNOWLEDGECODE
 * @version 0.4.0
 */
public class WebSocket extends CordovaPlugin {
    private static final int CONNECTION_TIMEOUT = 20000;

    private WebSocketClientFactory _factory;
    private SparseArray<Connection> _conn;

    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
        _factory = new WebSocketClientFactory();
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
            client.open(complementPort(url), new org.eclipse.jetty.websocket.WebSocket.OnTextMessage() {
                @Override
                public void onOpen(Connection conn) {
                    if (!callbackContext.isFinished()) {
                        _conn.put(callbackId, conn);
                        String callbackString = createCallbackString("onopen");
                        PluginResult result = new PluginResult(Status.OK, callbackString);
                        result.setKeepCallback(true);
                        callbackContext.sendPluginResult(result);
                    }
                }
                @Override
                public void onMessage(String data) {
                    if (!callbackContext.isFinished()) {
                        String callbackString = createCallbackString("onmessage", data);
                        PluginResult result = new PluginResult(Status.OK, callbackString);
                        result.setKeepCallback(true);
                        callbackContext.sendPluginResult(result);
                    }
                }
                @Override
                public void onClose(int code, String reason) {
                    boolean wasClean = false;
                    if (_conn.indexOfKey(callbackId) >= 0) {
                        _conn.remove(callbackId);
                        wasClean = true;
                    }
                    if (!callbackContext.isFinished()) {
                        reason = reason == null ? "" : reason;
                        String callbackString = createCallbackString("onclose", wasClean, code, reason);
                        PluginResult result = new PluginResult(Status.OK, callbackString);
                        callbackContext.sendPluginResult(result);
                    }
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
            uri = URIUtils.createURI(uri.getScheme(), uri.getHost(), port, uri.getPath(), uri.getQuery(), uri.getFragment());
        }
        return uri;
    }

    /**
     * Sending any data.
     * @param callbackId
     * @param data
     * @throws IOException 
     */
    private void send(int callbackId, String data) throws IOException {
        Connection conn = _conn.get(callbackId);

        if (conn != null) {
            conn.sendMessage(data);
        }
    }

    /**
     * Closing connection.
     * @param callbackId
     * @param code
     * @param reason
     */
    private void close(int callbackId, int code, String reason) {
        Connection conn = _conn.get(callbackId);

        if (conn != null) {
            _conn.remove(callbackId);
            if (code > 0) {
                conn.close(code, reason);
            } else {
                conn.close();
            }
        }
    }

    /**
     * Create Callback JSON String.
     * @param event
     * @return JSON String
     */
    private String createCallbackString(String event) {
        String json = "{\"event\":\"%s\"}";
        return String.format(json, event);
    }

    /**
     * Create Callback JSON String.
     * @param event
     * @param data
     * @return JSON String
     */
    private String createCallbackString(String event, String data) {
        String json = "{\"event\":\"%s\",\"data\":\"%s\"}";
        return String.format(json, event, data.replaceAll("\"", "\\\\\""));
    }

    /**
     * Create Callback JSON String.
     * @param event
     * @param wasClean
     * @param code
     * @param reason
     * @return JSON String
     */
    private String createCallbackString(String event, boolean wasClean, int code, String reason) {
        String json = "{\"event\":\"%s\",\"wasClean\":%b,\"code\":%d,\"reason\":\"%s\"}";
        return String.format(json, event, wasClean, code, reason.replaceAll("\"", "\\\\\""));
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
