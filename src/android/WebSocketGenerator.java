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

import org.apache.cordova.CallbackContext;
import org.apache.cordova.PluginResult;
import org.apache.cordova.PluginResult.Status;
import org.json.JSONObject;

import android.util.Base64;

class WebSocketGenerator extends AbstractWebSocket {

    interface OnOpenListener {
        public void onOpen(int id, Connection conn);
    }

    interface OnCloseListener {
        public void onClose(int id);
    }

    private final int _id;
    private final CallbackContext _ctx;
    private OnOpenListener _openListener;
    private OnCloseListener _closeListener;

    /**
     * Constructor
     *
     * @param id
     * @param ctx
     */
    public WebSocketGenerator(int id, CallbackContext ctx) {
        _id = id;
        _ctx = ctx;
        _openListener = new OnOpenListener() {
            @Override
            public void onOpen(int id, Connection conn) {
            }
        };
        _closeListener = new OnCloseListener() {
            @Override
            public void onClose(int id) {
            }
        };
    }

    /**
     * Set OnOpen listener.
     *
     * @param l
     */
    public void setOnOpenListener(OnOpenListener l) {
        _openListener = l;
    }

    /**
     * Set OnClose listener.
     *
     * @param l
     */
    public void setOnCloseListener(OnCloseListener l) {
        _closeListener = l;
    }

    /**
     * Create Callback JSON String.
     *
     * @param protocol
     * @return JSON String
     */
    private static String createJsonForOpen(String protocol) {
        String json = "{\"event\":\"onopen\",\"protocol\":%s}";
        protocol = protocol == null ? "" : protocol;
        return String.format(json, JSONObject.quote(protocol));
    }

    /**
     * Create Callback JSON String.
     *
     * @param data
     * @return JSON String
     */
    private static String createJsonForMessage(String data) {
        String json = "{\"event\":\"onmessage\",\"data\":%s}";
        return String.format(json, JSONObject.quote(data));
    }

    /**
     * Create Callback JSON String.
     *
     * @param input
     * @return JSON String
     */
    private static String createJsonForMessage(byte[] input) {
        String json = "{\"event\":\"onmessage\",\"data\":\"%s\",\"binary\":true}";
        String data = Base64.encodeToString(input, Base64.NO_WRAP);
        return String.format(json, data);
    }

    /**
     * Create Callback JSON String.
     *
     * @param code
     * @param reason
     * @return JSON String
     */
    private static String createJsonForClose(int code, String reason) {
        String json = "{\"event\":\"onclose\",\"wasClean\":%b,\"code\":%d,\"reason\":%s}";
        boolean wasClean = code == 1000;
        reason = reason == null ? "" : reason;
        return String.format(json, wasClean, code, JSONObject.quote(reason));
    }

    /**
     * Send plugin result.
     *
     * @param callbackString
     * @param keepCallback
     */
    private void sendCallback(String callbackString, boolean keepCallback) {
        if (!_ctx.isFinished()) {
            PluginResult result = new PluginResult(Status.OK, callbackString);
            result.setKeepCallback(keepCallback);
            _ctx.sendPluginResult(result);
        }
    }

    @Override
    public void onOpen(Connection conn) {
        _openListener.onOpen(_id, conn);
        String callbackString = createJsonForOpen(conn.getProtocol());
        sendCallback(callbackString, true);
    }

    @Override
    public void onMessage(String data) {
        String callbackString = createJsonForMessage(data);
        sendCallback(callbackString, true);
    }

    @Override
    public void onMessage(byte[] data, int offset, int length) {
        String callbackString = createJsonForMessage(data);
        sendCallback(callbackString, true);
    }

    @Override
    public void onClose(int code, String reason) {
        _closeListener.onClose(_id);
        String callbackString = createJsonForClose(code, reason);
        sendCallback(callbackString, false);
    }
}
