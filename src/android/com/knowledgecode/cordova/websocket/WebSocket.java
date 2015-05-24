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

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
 * @version 0.9.2
 */
public class WebSocket extends CordovaPlugin {

    private WebSocketClientFactory _factory;
    private SparseArray<Connection> _conn;
    private ExecutorService _executor;
    private TaskRunner _runner;

    @Override
    public void initialize(CordovaInterface cordova, final CordovaWebView webView) {
        super.initialize(cordova, webView);
        _factory = new WebSocketClientFactory();
        _conn = new SparseArray<Connection>();
        _executor = Executors.newSingleThreadExecutor();
        _runner = new TaskRunner();
        _runner.setTask("create", new ConnectionTask(_factory, _conn));
        _runner.setTask("send", new SendingTask(_conn));
        _runner.setTask("close", new DisconnectionTask(_conn));
        _executor.execute(_runner);
        _executor.shutdown();
        start();
    }

    @Override
    public boolean execute(String action, String rawArgs, CallbackContext ctx) {
        return _runner.addTaskQueue(new TaskBean(action, rawArgs, ctx));
    };

    /**
     * Start WebSocketClientFactory.
     *
     * @return WebSocket
     */
    private WebSocket start() {
        try {
            _factory.start();
        } catch (Exception e) {
        }
        return this;
    }

    /**
     * Stop WebSocketClientFactory.
     *
     * @return WebSocket
     */
    private WebSocket stop() {
        if (_conn != null) {
            for (int i = 0; i < _conn.size(); i++) {
                int key = _conn.keyAt(i);

                if (_conn.get(key).isOpen()) {
                    _conn.get(key).close();
                }
            }
            _conn.clear();
        }
        try {
            _factory.stop();
        } catch (Exception e) {
        }
        return this;
    }

    @Override
    public void onReset() {
        if (_factory != null) {
            stop().start();
        }
    }

    @Override
    public void onDestroy() {
        stop();
        _conn = null;
        _executor.shutdownNow();
        _executor = null;
        _runner = null;
        _factory.destroy();
        _factory = null;
    }
}
