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

import org.apache.cordova.CordovaWebView;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.eclipse.jetty.websocket.WebSocket.Connection;
import org.eclipse.jetty.websocket.WebSocketClientFactory;

import android.util.SparseArray;

/**
 * Cordova WebSocket Plugin for Android
 * This plugin is using Jetty under the terms of the Apache License v2.0.
 *
 * @author KNOWLEDGECODE <knowledgecode@gmail.com>
 * @version 0.8.0
 */
public class WebSocket extends CordovaPlugin {

    private WebSocketClientFactory _factory;
    private SparseArray<Connection> _conn;
    private ConnectionTask _create;
    private SendingTask _send;
    private DisconnectionTask _close;

    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
        _factory = new WebSocketClientFactory();
        _conn = new SparseArray<Connection>();
        _create = new ConnectionTask(_factory, _conn);
        _send = new SendingTask(_conn);
        _close = new DisconnectionTask(_conn);
        try {
            start();
        } catch (Exception e) {
        }
    }

    @Override
    public boolean execute(String action, String rawArgs, CallbackContext ctx) {
        if ("send".equals(action)) {
            cordova.getThreadPool().execute(new TaskRunner(_send, rawArgs, ctx));
        } else if ("create".equals(action)) {
            cordova.getThreadPool().execute(new TaskRunner(_create, rawArgs, ctx));
        } else if ("close".equals(action)) {
            cordova.getThreadPool().execute(new TaskRunner(_close, rawArgs, ctx));
        } else {
            return false;
        }
        return true;
    };

    /**
     * Start WebSocketClientFactory.
     *
     * @return WebSocket
     * @throws Exception
     */
    private WebSocket start() throws Exception {
        _factory.start();
        return this;
    }

    /**
     * Stop WebSocketClientFactory.
     *
     * @return WebSocket
     * @throws Exception
     */
    private WebSocket stop() throws Exception {
        if (_conn != null) {
            for (int i = 0; i < _conn.size(); i++) {
                int key = _conn.keyAt(i);

                if (_conn.get(key).isOpen()) {
                    _conn.get(key).close();
                }
            }
            _conn.clear();
        }
        _factory.stop();
        return this;
    }

    @Override
    public void onReset() {
        try {
            if (_factory != null) {
                stop().start();
            }
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
    }
}
